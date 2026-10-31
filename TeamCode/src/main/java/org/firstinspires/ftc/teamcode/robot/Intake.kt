package org.firstinspires.ftc.teamcode.robot
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.actuators.NextServo
import dev.nextftc.robot.Mechanism

class Intake : Mechanism {
  val intakeMotor = NextMotor("IntakeMotor")
  val transferMotor = NextMotor("TransferMotor").also { it.direction = NextMotor.Direction.REVERSE }
  val flowerIntakeServo = NextServo("FlowerIntakeServo")

  fun startIntake() {
    intakeMotor.throttle = 1.0
  }
  fun startTransfer() {
    transferMotor.throttle = 1.0
  }

  fun stopIntakeAndTransfer() {
    intakeMotor.throttle = 0.0
    transferMotor.throttle = 0.0
  }
  fun reverseIntake() = instant { intakeMotor.throttle = -1.0 }

  fun extendFlowerIntake() {
    println("extending flower intake set pos")
    flowerIntakeServo.position = 0.155
  }
  fun retractFlowerIntake() = instant {
    println("retracting flower intake set pos")
    flowerIntakeServo.position = 0.4
  }
}