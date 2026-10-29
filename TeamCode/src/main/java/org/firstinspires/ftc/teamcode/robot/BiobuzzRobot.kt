package org.firstinspires.ftc.teamcode.robot

import dev.nextftc.robot.NextRobot
import dev.nextftc.robot.coroutine.command

class BiobuzzRobot : NextRobot {
  val intake: Intake = Intake()
  val shooter: Shooter = Shooter()

  override val mechanisms = setOf(intake, shooter)

  fun autoIntake() = command {
    intake.intakeMotor.throttle = 1.0
  }.requiring(intake)

  fun autoShoot() = command {
    intake.intakeMotor.throttle = 1.0
    intake.transferMotor.throttle = 1.0
  }
}