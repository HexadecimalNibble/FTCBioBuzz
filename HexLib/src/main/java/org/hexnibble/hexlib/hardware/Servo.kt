package org.hexnibble.hexlib.hardware

import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.PwmControl
import com.qualcomm.robotcore.hardware.ServoImplEx

/**
 * Class to hold information for establishing the current servo position based on the encoder readings
 */
data class ServoEncoderInfo(
  val lowerPos: Double,
  val lowerAngleDeg: Double,
  val upperPos: Double,
  val upperAngleDeg: Double,
  val minEncoderVoltage: Double = 0.0,
  val maxEncoderVoltage: Double = 3.3,
  val reversed: Boolean = false,
)

class Servo(hwMap: HardwareMap, servoName: String) {
  private val servo: ServoImplEx = hwMap.get(ServoImplEx::class.java, servoName)
  var reversed: Boolean = false
    private set

  private var encoder: AnalogInput? = null
  var servoEncoderInfo: ServoEncoderInfo? = null
    private set

  // Builder functions
  fun withEncoder(encoder: AnalogInput, servoEncoderInfo: ServoEncoderInfo): Servo {
    this.encoder = encoder
    this.servoEncoderInfo = servoEncoderInfo
    return this
  }

  fun setReversed(reversed: Boolean = true): Servo {
    this.reversed = reversed
    return this
  }

  /**
   * Get the current position of the servo according to the encoder
   */
  fun getEncoderPosition(): Double {
    val encoder = checkNotNull(encoder) { "Encoder was not specified" }
    val encoderInfo = checkNotNull(servoEncoderInfo) { "servoEncoderInfo not specified" }
    val pos = encoder.voltage / (encoderInfo.maxEncoderVoltage - encoderInfo.minEncoderVoltage)
    return if (encoderInfo.reversed) 1 - pos else pos
  }

  fun getEncoderPositionDegrees(): Double {
    val pos = getEncoderPosition()
    // servoEncoderInfo isn't null bc no exception thrown in getEncoderPosition() to reach here
    val degPerPos = (servoEncoderInfo!!.upperAngleDeg - servoEncoderInfo!!.lowerAngleDeg) /
        (servoEncoderInfo!!.upperPos - servoEncoderInfo!!.lowerPos)
    return pos * degPerPos
  }

  // Class-level functions
  /**
   * Get the last servo position sent
   * Returns Double.NaN if that is unavailable
   */
  fun getLastSetServoPosition() = servo.position

  /**
   * Set servo to a given position.
   * Calling this function will freeze thread for a couple ms bc sdk badly written
   * @param position Servo position in range [0,1]
   */
  fun setServoPosition(position: Double) {
    servo.position = if (reversed) 1 - position else position
  }

  /**
   * Set a custom servo PWM range to get more range out of the servo
   * @param lower Min PWM rate used in microseconds
   * @param upper Max PWM rate used in microseconds
   * @param framePeriod Set the rate in microseconds the PWM is transmitted. Must be in range [2,65535]
   */
  fun setPWMRange(lower: Double, upper: Double, framePeriod: Double = 20000.0) {
    servo.pwmRange = PwmControl.PwmRange(lower, upper, framePeriod)
  }

  fun enablePWM() = servo.setPwmEnable()

  fun disablePWM() = servo.setPwmDisable()
}