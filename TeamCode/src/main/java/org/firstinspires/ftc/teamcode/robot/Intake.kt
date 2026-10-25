package org.firstinspires.ftc.teamcode.robot
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.robot.Mechanism

class Intake : Mechanism {
  val intakeMotor = NextMotor("IntakeMotor")
  val transferMotor = NextMotor("TransferMotor").also { it.direction = NextMotor.Direction.REVERSE }

  fun startIntake() = instant { intakeMotor.throttle = 1.0 }
  fun startFullIntakeTesting() = instant {
    intakeMotor.throttle = 1.0
    transferMotor.throttle = 1.0
  }
  fun stopIntake() = instant {
    intakeMotor.throttle = 0.0
    transferMotor.throttle = 0.0
  }
  fun reverseIntake() = instant { intakeMotor.throttle = -1.0 }
}