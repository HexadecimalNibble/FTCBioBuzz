package org.firstinspires.ftc.teamcode.robot
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.actuators.NextServo
import dev.nextftc.hardware.sensors.NextDigitalSensor
import dev.nextftc.robot.Mechanism
import dev.nextftc.units.Seconds
import dev.nextftc.units.measuretypes.Time

class Intake : Mechanism {
  private val intakeMotor = NextMotor("IntakeMotor")
  private val flowerIntakeServo = NextServo("FlowerIntakeServo").also { it.setPwmRange(500.0, 2475.0) }
  private val distanceSensor = NextDigitalSensor("IntakeDistanceSensor", false)

  fun startIntake(power: Double = 1.0) {
    intakeMotor.throttle = power
  }

  fun reverseIntake(power: Double = -1.0) {
    intakeMotor.throttle = power
  }

  fun stopIntake() {
    intakeMotor.throttle = 0.0
  }

  fun intakeMotorOverCurrent(): Boolean {
    return intakeMotor.isOverCurrent
  }

  fun extendFlowerIntake() {
    println("extending flower intake set pos")
    flowerIntakeServo.position = 0.808
  }

  fun retractFlowerIntake() {
    println("retracting flower intake set pos")
    flowerIntakeServo.position = 0.41
  }

  fun distanceSensorObjectDetected() = distanceSensor.isTriggered

  /**
   * Command to wait until the distance sensor is continuously activated for [totalTime]
   */
  fun waitForContinuousDistanceSensorDetection(totalTime: Time) = coroutine {
    val requiredNanos = totalTime.into(Seconds) * 1_000_000_000.0
    require(requiredNanos > 0.0 && requiredNanos.isFinite()) {
      "totalTime must be a positive, finite duration"
    }

    var activeSince: Long? = null
    while (true) {
      val now = System.nanoTime()
      if (distanceSensor.isTriggered) {
        println("Distance sensor triggered")
        // Start a new timing window if one hasn't started
        val start = activeSince ?: now.also {
          activeSince = it
        }
        // Check whether the continuous active time is sufficient
        if ((now - start).toDouble() >= requiredNanos) {
          println("Continuously triggered for: $totalTime")
          break
        }
      } else {
        // Any inactive reading resets the timer
        activeSince = null
      }
      yield()
    }
  }
}