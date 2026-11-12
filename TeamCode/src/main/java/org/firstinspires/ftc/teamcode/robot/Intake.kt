package org.firstinspires.ftc.teamcode.robot
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.actuators.NextServo
import dev.nextftc.hardware.sensors.NextDigitalSensor
import dev.nextftc.robot.Mechanism

class Intake : Mechanism {
  private val intakeMotor = NextMotor("IntakeMotor")
  private val flowerIntakeServo = NextServo("FlowerIntakeServo").also { it.setPwmRange(500.0, 2475.0) }
  private val distanceSensor = NextDigitalSensor("IntakeDistanceSensor")

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
    flowerIntakeServo.position = 0.808
  }

  fun retractFlowerIntake() {
    println("retracting flower intake set pos")
    flowerIntakeServo.position = 0.4
  }

  fun distanceSensorObjectDetected() = distanceSensor.isTriggered
}