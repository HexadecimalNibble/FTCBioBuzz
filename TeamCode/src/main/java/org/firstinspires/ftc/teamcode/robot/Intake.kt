package org.firstinspires.ftc.teamcode.robot
import dev.nextftc.hardware.actuators.NextFeedbackServo
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.sensors.NextAnalogInput
import dev.nextftc.robot.Mechanism

class Intake : Mechanism {
  val intakeMotor = NextMotor("intakeMotor")

  val servoAnalogInput = NextAnalogInput("servoAnalog")
  val servo = NextFeedbackServo("servo", servoAnalogInput)

  fun moveServo() = instant { servo.position = 0.3 }

  fun startIntake() = instant { intakeMotor.throttle = 1.0 }
  fun stopIntake() = instant { intakeMotor.throttle = 0.0 }
  fun reverseIntake() = instant { intakeMotor.throttle = -1.0 }
}