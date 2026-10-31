package org.firstinspires.ftc.teamcode.robot

import com.bylazar.configurables.annotations.Configurable
import com.pedropathing.math.Pose
import dev.nextftc.control.feedback.PIDController
import dev.nextftc.control.feedforward.SimpleFeedforward
import dev.nextftc.control.model.MotionState
import dev.nextftc.hardware.actuators.NextFeedbackCRServo
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.sensors.NextAnalogInput
import dev.nextftc.robot.Mechanism
import dev.nextftc.units.Degrees
import dev.nextftc.units.degrees
import dev.nextftc.units.measuretypes.Angle
import dev.nextftc.units.measuretypes.AngularVelocity
import dev.nextftc.units.radians
import dev.nextftc.units.rpm
import dev.nextftc.units.volts
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

enum class HivePosition(val pose: Pose) {
  RedAudience(Pose(58.0, 54.0)),
  RedNonAudience(Pose(58.0, 86.0)),
  BlueAudience(Pose(84.0, 54.0)),
  BlueNonAudience(Pose(84.0, 86.0)),
}

@Configurable
class Shooter : Mechanism {
  companion object {
    @JvmField
    var shooterP = 0.0
    @JvmField
    var shooterD = 0.0
    @JvmField
    var shooterkV = 0.01

    @JvmField
    var turretP = 0.4
    @JvmField
    var turretD = 0.008
    @JvmField
    var turretkS = 0.03

    @JvmField
    var turretAutoAlign = true
    @JvmField
    var shooterAutoAlign = true
    @JvmField
    var hoodAutoAlign = true

    @JvmField
    var shooterVelocity1 = 0.0
    @JvmField
    var shooterVelocity2 = 0.0
    @JvmField
    var shooterVelocity3 = 0.0
  }
  // region * Turret Vars *
  private val leftTurretServo = NextFeedbackCRServo("LTurretServo", NextAnalogInput("TurretServoEncoder1", maxVoltage = 3.2633.volts)).also { it.reverse() }
  private val rightTurretServo = NextFeedbackCRServo("RTurretServo", NextAnalogInput("TurretServoEncoder2")).also { it.reverse() }

  // Turret is 28mm behind wheel-base center with intake at front
  private val turretFBOffsetIn = -28.0 / 25.4

  // Keep track of num rotations to determine total servo position
  private var servoNumRotations: Int = 0
  private var prevTurretAngle: Angle = 0.0.degrees

  private var totalTurretAngle: Angle = 0.0.degrees
  private var servoOffsetAngle: Angle = 0.0.degrees

  private val turretPID = PIDController(kP = turretP, kD = turretD)

  private var targetTurretAngle: Angle = 0.0.degrees
  // endregion * Turret Vars *
  // region * Shooter Motor Vars *
  // Shooter motor 1 has encoder
  val shooterMotor1 = NextMotor("ShooterMotor1", anglePerCount = (2 * PI / 28).radians)
  private val shooterMotor2 = NextMotor("ShooterMotor2").also { it.direction = NextMotor.Direction.REVERSE }

  private var targetShooterVelocity = 0.0.rpm

  private val shooterMotorPID = PIDController(kP = shooterP, kD = shooterD)
  private val shooterMotorFeedforward = SimpleFeedforward(
    kV = shooterkV,
    kS = 0.0,
    kA = 0.0
  )
  var autoAlign = false

  var hivePosition = HivePosition.RedAudience

  lateinit var poseSupplier: () -> Pose
  // endregion * Shooter Motor Vars *

  fun initSystem() {
    // Turn on turret servo pwm
    leftTurretServo.power = 0.0
    rightTurretServo.power = 0.0
  }

  // region * Turret *
  fun resetTurretAngle() {
    servoOffsetAngle = -leftTurretServo.angle
    servoNumRotations = 0
    totalTurretAngle = 0.0.degrees
    prevTurretAngle = 0.0.degrees
  }

  /**
   * Updates turret position by reading the servo encoder
   */
  fun updateTurretAngle() {
    println("Raw servo angle: ${leftTurretServo.angle.into(Degrees)}")
    println("Servo offset angle: ${servoOffsetAngle.into(Degrees)}")
    val newTurretAngle = leftTurretServo.angle + servoOffsetAngle
    println("Prev turret angle: $prevTurretAngle")
    println("New turret angle: $newTurretAngle")
    val dTheta = newTurretAngle - prevTurretAngle
    println("dTheta: $dTheta")

    // Significant dTheta in 1 loop is prob wrap around
    // There is always the possibility tho that a significant loop delay could offset the turret position by 360deg
    if (dTheta > 200.0.degrees) { // 0 -> 360
      servoNumRotations -= 1
    } else if (dTheta < (-200.0).degrees) { // 360 -> 0
      servoNumRotations += 1
    }
    println("Servo num rotations: $servoNumRotations")

    val totalServoAngle = (servoNumRotations * 360.0).degrees + newTurretAngle
    println("total servo angle: $totalServoAngle")

    totalTurretAngle = totalServoAngle * (40.0 / 20.0) * (80.0 / 176.0)
    println("total turret angle: $totalTurretAngle")
    prevTurretAngle = newTurretAngle
  }

  fun setTurretAngle(angle: Angle) {
    targetTurretAngle = angle
  }
  // endregion * Turret *

  // region * Shooter *
  fun setShooterVelocity(velocity: AngularVelocity) {
    targetShooterVelocity = velocity
  }
  // endregion * Shooter *
  fun toggleHivePosition() {
    println("Toggling hive position. Was: $hivePosition")
    hivePosition = when (hivePosition) {
      HivePosition.RedAudience -> HivePosition.RedNonAudience
      HivePosition.RedNonAudience -> HivePosition.RedAudience
      HivePosition.BlueAudience -> HivePosition.BlueNonAudience
      HivePosition.BlueNonAudience -> HivePosition.BlueAudience
    }
    println("New hive position: $hivePosition")
  }

  fun autoAlign(currentPosition: Pose) {
    println("Current position: $currentPosition")
    println("Hive position: ${hivePosition.pose}")

    // rotate calculation: x * Math.cos(angle) - y * Math.sin(angle), x * Math.sin(angle) + y * Math.cos(angle)
    val turretOffsetFieldCentric = Pose(turretFBOffsetIn * cos(currentPosition.heading()), turretFBOffsetIn * sin(currentPosition.heading()))
    println("Turret Offset Field Centric: $turretOffsetFieldCentric")
    val turretFieldCentric = turretOffsetFieldCentric + currentPosition
    println("Turret Field Centric: $turretFieldCentric")

    val vecToHive = (hivePosition.pose - turretFieldCentric).toVector2D()
    val distanceToHive = vecToHive.magnitude()
    println("Distance to hive: $distanceToHive")

    val angleToHive = Math.toDegrees(vecToHive.theta())
    println("Angle to hive: $angleToHive")

    if (turretAutoAlign) {
      setTurretAngle(angleToHive.degrees - currentPosition.heading().radians)
    }
    if (shooterAutoAlign) {
      val shooterVelocity = (shooterVelocity1 * distanceToHive.pow(2) + shooterVelocity2 * distanceToHive + shooterVelocity3).rpm
      setShooterVelocity(shooterVelocity)
    }
    if (hoodAutoAlign) {
    }
  }

  override fun periodic() {
    updateTurretAngle()

    if (autoAlign) {
      autoAlign(poseSupplier())
    }

    // For PID tuning
    turretPID.coefficients.kP = turretP
    turretPID.coefficients.kD = turretD
    shooterMotorPID.coefficients.kP = shooterP
    shooterMotorPID.coefficients.kD = shooterD
    shooterMotorFeedforward.coefficients.kV = shooterkV

    // region * Turret *
    val turretError = (targetTurretAngle - totalTurretAngle).baseUnitMagnitude

    val turretFF = if (abs(turretError) > 0.01) {
      turretkS * sign(turretError)
    } else {
      0.0
    }
    println("Turret FF: $turretFF")

//    println("turret error: $turretError")

    val turretPower = (turretPID.calculate(error = turretError) + turretFF).coerceIn(-1.0, 1.0)
//    println("turret power: $turretPower")
    leftTurretServo.power = turretPower
    rightTurretServo.power = turretPower
    // endregion * Turret *
    // region * Shooter Motor *
    val motorVelocity = shooterMotor1.encoderVelocity

    val motorFF = shooterMotorFeedforward.calculate(MotionState(
      position = shooterMotor1.encoderPosition,
      velocity = targetShooterVelocity,
    ))

    val velocityError = (targetShooterVelocity - motorVelocity).baseUnitMagnitude

    val shooterPower = (shooterMotorPID.calculate(error = velocityError) + motorFF).coerceIn(-1.0, 1.0)
//    println("Shooter power: $shooterPower")
    shooterMotor1.throttle = shooterPower
    shooterMotor2.throttle = shooterPower
    // endregion * Shooter Motor *
  }
}