package org.firstinspires.ftc.teamcode.robot
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.robot.Mechanism

class IntakeTransfer : Mechanism {
  val intakeMotor = NextMotor("IntakeMotor")
  val transferMotor = NextMotor("TransferMotor").also { it.direction = NextMotor.Direction.REVERSE }

  fun startIntake() = instant { intakeMotor.throttle = 1.0 }
  fun stopIntake() = instant { intakeMotor.throttle = 0.0 }
  fun reverseIntake() = instant { intakeMotor.throttle = -1.0 }

  fun startAutoIntake() = coroutine {
    intakeMotor.throttle = 1.0
    wait(2.0)
    intakeMotor.throttle = 0.0
  }
}