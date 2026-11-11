package org.firstinspires.ftc.teamcode.robot
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.actuators.NextServo
import dev.nextftc.robot.Mechanism

class Intake : Mechanism {
  val intakeMotor = NextMotor("IntakeMotor")
  val flowerIntakeServo = NextServo("FlowerIntakeServo")

  fun startIntake(power: Double = 1.0) {
    intakeMotor.throttle = power
  }

  fun reverseIntake(power: Double = -1.0) {
    intakeMotor.throttle = power
  }

  fun stopIntake() {
    intakeMotor.throttle = 0.0
  }

  fun extendFlowerIntake() {
    println("extending flower intake set pos")
    flowerIntakeServo.position = 0.155
  }

  fun retractFlowerIntake() {
    println("retracting flower intake set pos")
    flowerIntakeServo.position = 0.4
  }
}