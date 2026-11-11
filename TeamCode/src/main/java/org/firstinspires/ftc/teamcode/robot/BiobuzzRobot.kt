package org.firstinspires.ftc.teamcode.robot

import com.pedropathing.ivy.CommandBuilder
import com.pedropathing.ivy.Scheduler
import dev.nextftc.robot.NextRobot
import dev.nextftc.robot.coroutine.command

class BiobuzzRobot : NextRobot {
  val intake: Intake = Intake()
  val transfer: Transfer = Transfer()
  val shooter: Shooter = Shooter()

  override val mechanisms = setOf(intake, transfer, shooter)

  // region Intake & Transfer
  fun autoIntake() = command { // TODO: DEAL WITH BALLS ALREADY BEING IN TRANSFER
    shooter.engageBlocker()
    transfer.startTransfer(1.0)
    intake.startIntake()

    // Wait a little longer after the first ball goes through then turn off the transfer to prevent stalling
    println("Waiting for counter now")
    transfer.resetCounter()
    waitUntil { transfer.checkMoveCounterAndReset() } // 1st ball
    println("0.25s delay")
    wait(0.25)
    transfer.stopTransfer()

    waitUntil { transfer.checkMoveCounterAndReset() } // 2nd ball
    waitUntil { transfer.checkMoveCounterAndReset() } // 3rd ball
    waitUntil { intake.distanceSensorObjectDetected() } // 4th ball
    intake.stopIntake()
  }.requiring(intake, transfer)
  // endregion

  // region Shooter
  fun autoShoot() = command {
    intake.startIntake()
    transfer.startTransfer()
    shooter.disengageBlocker()

    // TODO: USE DISTANCE SENSORS OR SOMETHING
    wait(1.0)

    stopAutoShoot()
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