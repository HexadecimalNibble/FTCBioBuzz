package org.firstinspires.ftc.teamcode.robot

import dev.nextftc.robot.NextRobot
import dev.nextftc.robot.coroutine.command

class BiobuzzRobot : NextRobot {
  val intake: Intake = Intake()
  val shooter: Shooter = Shooter()

  override val mechanisms = setOf(intake, shooter)

  fun autoIntake() = command {
    intake.stopIntakeAndTransfer()
    intake.startIntake()
  }.requiring(intake)

  fun stopAutoShoot() {
    shooter.engageBlocker()
    shooter.stopShooter()
    intake.stopIntakeAndTransfer()
  }

  fun autoShoot() = command {
    intake.startIntake()
    intake.startTransfer()
    shooter.disengageBlocker()
    shooter.setHoodPosition()
//    shooter.setShooterVelocity(3000.0.rpm)
  }
}