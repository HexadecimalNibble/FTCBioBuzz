package org.hexnibble.hexlib.hardware

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorImplEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import org.hexnibble.hexlib.controllers.PIDCoefficients
import org.hexnibble.hexlib.controllers.PIDController
import kotlin.math.abs

// The minimum difference between the current and requested motor power between motor writes
private const val cachingTolerance = 0.01

/**
 * @param hwMap HardwareMap object.
 * @param motorName Name of the motor object in the HwMap.
 * @param motorType Type of the motor. If a different type is needed, a custom type can be specified.
 * @param runDirection Direction the motor will run in.
 * @param runMode Mode the motor will run in.
 * @param encoderType Type of encoder being used for the motor.
 * @param encoderDirection Set external encoder run direction. Only used if encoder is external.
 * @param externalGearChange The external gear change of the motor. Ex: 40 tooth gear to 20 tooth gear is 2.0 (2nd gear runs twice as fast).
 */
class Motor @JvmOverloads constructor(
  hwMap: HardwareMap,
  motorName: String,
  val motorType: MotorType,
  val externalGearChange: Double = 1.0,
  val runDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
  runMode: DcMotor.RunMode = DcMotor.RunMode.RUN_WITHOUT_ENCODER,
  val encoderType: MotorEncoder = MotorEncoder.GOBILDA_INTERNAL,
  var encoderDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
  val pidController: PIDController? = null,
) {
  var runMode: DcMotor.RunMode = runMode
    private set

  private val motorObject: DcMotorImplEx = hwMap.get(DcMotorImplEx::class.java, motorName)
  var power: Double = 0.0
    private set
  var zeroPowerBehavior: DcMotor.ZeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    private set

  // Previous target position used to know when targetPositionDeg updates
  var prevTargetPositionDeg: Double = 0.0
    private set
  var targetPositionDeg: Double = 0.0

  init {
    if (encoderType is MotorEncoder.GOBILDA_INTERNAL || encoderType is MotorEncoder.REV_CORE_HEX_INTERNAL) {
      this.encoderDirection = runDirection
    }
    reset()
  }

//  fun getRunMode(): DcMotor.RunMode = motorObject.mode

  /**
   * Set the motor to the requested run mode. For RUN_TO_POSITION, the motor's target position will
   * first be set to its current position so that it doesn't start moving.
   *
   * @param runMode Desired run mode
   */
  fun setRunMode(runMode: DcMotor.RunMode) {
    this.runMode = runMode
    motorObject.setMode(runMode)
  }

  fun setZeroPowerBehavior(zeroPowerBehavior: DcMotor.ZeroPowerBehavior) {
    motorObject.zeroPowerBehavior = zeroPowerBehavior
    this.zeroPowerBehavior = zeroPowerBehavior
  }

  /** Reset the associated encoder.  */
  fun resetEncoder() {
    val currentRunMode: DcMotor.RunMode = motorObject.mode // Store current run mode
    power = 0.0
    setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER)

    setRunMode(currentRunMode) // Put motor back into previous run mode
  }

  /**
   *
   * Set the motor power. A motor command will only be sent if the requested power
   * exceeds the threshold, and if the difference from the current power also exceeds the threshold.
   *
   * The SDK will clamp the power so check will not be done here.
   * @param motorPower Requested motor power
   */
  fun setPower(motorPower: Double) {
    if (abs(motorPower - this.power) > cachingTolerance) {
      this.power = motorPower
      motorObject.setPower(this.power)
    }
  }

  fun getMotorCurrent(currentUnit: CurrentUnit): Double {
    return motorObject.getCurrent(currentUnit)
  }

  /**
   * Query the encoder for the current position (in counts)
   *
   * @return Current position (in counts)
   */
  fun getCurrentPosition(): Int {
    return if (encoderType is MotorEncoder.GOBILDA_INTERNAL || encoderType is MotorEncoder.REV_CORE_HEX_INTERNAL) {
      motorObject.getCurrentPosition()
    } else {
      if (((runDirection == DcMotorSimple.Direction.FORWARD) && (encoderDirection == DcMotorSimple.Direction.FORWARD))
        || ((runDirection == DcMotorSimple.Direction.REVERSE) && (encoderDirection == DcMotorSimple.Direction.REVERSE))
      ) {
        motorObject.getCurrentPosition()
      } else {
        -motorObject.getCurrentPosition()
      }
    }
  }

  /**
   * @return Output shaft position (degrees)
   */
  fun getCurrentPositionDeg(): Double {
    val numberOfRotations = motorType.getPosition(getCurrentPosition().toDouble(), encoderType.CPR, externalGearChange)
    return numberOfRotations * 360.0
  }

  /**
   * Obtain the current velocity in rpm of the motor accounting for gearing ratios.
   *
   * @return Output shaft current velocity (rpm)
   */
  fun getCurrentVelocityRPM(): Double = motorType.getPosition(motorObject.velocity, encoderType.CPR, externalGearChange)

  fun setPIDFCoefficients(pidfCoefficients: PIDCoefficients) {
    if (pidController == null) return
    pidController.coefficients.setCoefficients(pidfCoefficients)
  }

  fun pidfAtTargetPosition(): Boolean {
    if (pidController == null) return false
    return abs(prevTargetPositionDeg - targetPositionDeg) < 0.25 && pidController.atTargetPosition()
  }

  /**
   * Function to run the PIDF.
   * This should be called every loop to update the PIDF.
   * This will only run if the pidf is not at the target position.
   */
  fun processPIDF() {
    if (pidController == null) return
    if (!pidfAtTargetPosition()) {
      val error = targetPositionDeg - getCurrentPositionDeg()
      pidController.updateError(error)
      setPower(pidController.run())
    }
    prevTargetPositionDeg = targetPositionDeg
  }

  // TODO: FIX THIS
  fun processPIDFForce() {
    if (pidController == null) return
    val error = targetPositionDeg - getCurrentPositionDeg()
    pidController.updateError(error)
    setPower(pidController.run())
    prevTargetPositionDeg = targetPositionDeg
  }

  /**
   * Call this function to reinitialize this motor when restarting an OpMode. It will revert back to
   * the most recent stored values. This function will NOT reset encoders.
   */
  fun reset() {
    motorObject.setPower(0.0)
    power = 0.0

    motorObject.setDirection(runDirection)
    motorObject.zeroPowerBehavior = zeroPowerBehavior

    setRunMode(runMode) // Set the motor to the requested run mode

    pidController?.reset()
  }
}

sealed class MotorEncoder(
  val CPR: Int
) {
  object GOBILDA_INTERNAL : MotorEncoder(28)
  object REV_CORE_HEX_INTERNAL : MotorEncoder(4)
  object REV : MotorEncoder(8192)
  object GO_BILDA_ODOPOD : MotorEncoder(2000)

  data class CUSTOM(
    val cpr: Int
  ) : MotorEncoder(cpr)
}

sealed class MotorType(
  val RPM: Int,
  val internalGearReduction: Double,
) {
  // internal: (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_30 : MotorType(30, 188.61078)
  // internal: (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 11.0))
  object GOBILDA_RPM_43 : MotorType(43, 139.13824)
  // internal: (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_60 : MotorType(60, 99.50754)
  // internal: (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_84 : MotorType(84, 71.16483)
  // internal: (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_117 : MotorType(117, 50.89497)
  // internal: (1.0 + (46.0 / 11.0)) * (1.0 + (46.0 / 11.0))
  object GOBILDA_RPM_223 : MotorType(223, 26.85124)
  // internal: (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 11.0))
  object GOBILDA_RPM_312 : MotorType(312, 19.20321)
  // internal: (1.0 + (46.0 / 17.0)) * (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_435 : MotorType(435, 13.73356)
  // internal: (1.0 + (46.0 / 11.0))
  object GOBILDA_RPM_1150 : MotorType(1150, 5.18182)
  // internal: (1.0 + (46.0 / 17.0))
  object GOBILDA_RPM_1620 : MotorType(1620, 3.70588)
  // internal: 1.0
  object GOBILDA_BARE : MotorType(6000, 1.0)

  data class CUSTOM(
    val rpm: Int,
    val igr: Double,
  ) : MotorType(rpm, igr)

  fun getAchievableMaxTicksPerSecond(CPR: Int) = CPR * RPM

  /**
   * Get actual position of motor accounting for internal gear ratios from a raw motor position.
   */
  fun getPosition(rawValue: Double, CPR: Int, externalGearChange: Double): Double = rawValue / CPR / internalGearReduction * externalGearChange

  /**
   * Return the velocity of the motor in RPM
   */
  fun getVelocity(rawValue: Double, CPR: Int, externalGearChange: Double): Double = rawValue / CPR / internalGearReduction * 60 * externalGearChange
}