package org.firstinspires.ftc.teamcode.robot

import com.pedropathing.ivy.Command
import com.pedropathing.ivy.Scheduler
import com.pedropathing.ivy.commands.Commands.infinite
import com.pedropathing.ivy.commands.Commands.instant
import com.pedropathing.ivy.commands.Commands.waitUntil
import com.pedropathing.ivy.groups.Groups.race
import com.pedropathing.ivy.groups.Groups.sequential
import dev.nextftc.hardware.actuators.NextRGBIndicator
import dev.nextftc.robot.NextRobot
import dev.nextftc.robot.coroutine.command
import dev.nextftc.units.milliseconds

class BiobuzzRobot : NextRobot {
  val intake: Intake = Intake()
  val transfer: Transfer = Transfer()
  val shooter: Shooter = Shooter()

  override val mechanisms = setOf(intake, transfer, shooter)

  // region Intake & Transfer
  fun autoIntake(): Command = sequential(
    instant {
      shooter.engageBlocker()
      transfer.startTransfer(1.0)
      intake.startIntake(1.0)

      transfer.resetCounter()
      transfer.setTransferLEDColor(NextRGBIndicator.Color.RED)
    },
    // command end stuff
    race(
      // Finishes when 4 balls detected
      waitUntil { transfer.getNumBallsCounted() > 3 },
      // Also finishes when 3 balls and distance sensor detects for a decent amount of time
      sequential(
        waitUntil { transfer.getNumBallsCounted() == 3 },
        instant { transfer.stopTransfer() }, // stop transfer to prevent stalling
        intake.waitForContinuousDistanceSensorDetection(250.0.milliseconds)
      ),
      // Also finishes if distance sensor detects for a long time
      intake.waitForContinuousDistanceSensorDetection(750.0.milliseconds),

      // Continuously monitor transfer motor current to stop it if it spikes
//      infinite {
//        sequential(
//          waitUntil { transfer.transferMotorRunning && transfer.transferMotorOverCurrent() },
//          instant { transfer.stopTransfer() }
//        )
//      }
      // Continuously monitor transfer motor current.
      infinite {
        if (transfer.transferMotorRunning && transfer.transferMotorOverCurrent()) {
          println("TRANSFER OVERCURRENT DETECTED! STOPPING TRANSFER")

          transfer.stopTransfer()
        }
      },

      // End entire command if intake is overcurrent
      waitUntil {
        if (intake.intakeMotorOverCurrent()) {
          println("INTAKE OVERCURRENT DETECTED! STOPPING AUTO INTAKE")
          true
        } else {
          false
        }
      }
    ),
    // Cleanup
    instant {
      intake.stopIntake()
      transfer.stopTransfer()
      transfer.setTransferLEDColor(NextRGBIndicator.Color.GREEN)
    }
  ).requiring(intake, transfer)
  // endregion
  // region Shooter
  fun autoShoot() = command {
    shooter.disengageBlocker()
    intake.startIntake()
    transfer.startTransfer()
    wait(1.0)
    stopAutoShoot()
    transfer.setTransferLEDColor(NextRGBIndicator.Color.RED)
  }.requiring(shooter, intake, transfer)

  fun stopAutoShoot() {
    // Just cancel the command instead of having to make another command with requirements and stuff
    Scheduler.cancel(autoShoot())
    shooter.engageBlocker()
    intake.stopIntake()
    transfer.stopTransfer()
  }
  // endregion
}