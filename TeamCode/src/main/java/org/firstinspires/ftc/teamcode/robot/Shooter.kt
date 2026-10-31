package org.firstinspires.ftc.teamcode.robot

import com.bylazar.configurables.annotations.Configurable
import com.pedropathing.math.Pose
import com.pedropathing.math.Velocity
import dev.nextftc.control.feedback.PIDController
import dev.nextftc.control.feedforward.SimpleFeedforward
import dev.nextftc.control.model.MotionState
import dev.nextftc.hardware.actuators.NextFeedbackCRServo
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.sensors.NextAnalogInput
import dev.nextftc.robot.Mechanism
import dev.nextftc.units.degrees
import dev.nextftc.units.measuretypes.Angle
import dev.nextftc.units.measuretypes.AngularVelocity
import dev.nextftc.units.radians
import dev.nextftc.units.rpm
import dev.nextftc.units.volts
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
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

    // SOM settings
    @JvmField
    var shootOnMoveEnabled = false

    // Effective horizontal projectile velocity (in/s)
    @JvmField
    var projectileHorizontalSpeedInPerSec = 0.0
  }

  // region Turret Vars
  private val leftTurretServo =
    NextFeedbackCRServo(
      "LTurretServo",
      NextAnalogInput(
        "TurretServoEncoder1",
        maxVoltage = 3.2633.volts
      )
    ).also {
      it.reverse()
    }

  private val rightTurretServo =
    NextFeedbackCRServo(
      "RTurretServo",
      NextAnalogInput("TurretServoEncoder2")
    ).also {
      it.reverse()
    }

  // Turret is 28mm behind wheel-base center with intake at front.
  private val turretFBOffsetIn = -28.0 / 25.4

  // Keep track of servo rotations to determine total servo position.
  private var servoNumRotations: Int = 0

  private var prevTurretAngle: Angle = 0.0.degrees

  private var totalTurretAngle: Angle = 0.0.degrees

  private var servoOffsetAngle: Angle = 0.0.degrees

  private val turretPID = PIDController(
    kP = turretP,
    kD = turretD
  )

  private var targetTurretAngle: Angle = 0.0.degrees
  // endregion
  // region Shooter Motor Vars
  // Shooter motor 1 has encoder.
  val shooterMotor1 = NextMotor(
    "ShooterMotor1",
    anglePerCount = (2 * PI / 28).radians
  )

  private val shooterMotor2 =
    NextMotor("ShooterMotor2").also {
      it.direction = NextMotor.Direction.REVERSE
    }

  private var targetShooterVelocity = 0.0.rpm

  private val shooterMotorPID = PIDController(kP = shooterP, kD = shooterD)

  private val shooterMotorFeedforward = SimpleFeedforward(kV = shooterkV, kS = 0.0, kA = 0.0)

  var autoAlign = false

  var hivePosition = HivePosition.RedAudience

  lateinit var poseSupplier: () -> Pose

  lateinit var velocitySupplier: () -> Velocity
  // endregion Shooter Motor Vars

  fun initSystem() {
    leftTurretServo.power = 0.0
    rightTurretServo.power = 0.0
  }

  // region Turret
  fun resetTurretAngle() {
    servoOffsetAngle = -leftTurretServo.angle
    servoNumRotations = 0
    totalTurretAngle = 0.0.degrees
    prevTurretAngle = 0.0.degrees
  }

  /**
   * Updates turret position by reading the servo encoder.
   */
  fun updateTurretAngle() {
    val newTurretAngle = leftTurretServo.angle + servoOffsetAngle
    val dTheta = newTurretAngle - prevTurretAngle
    // Detect analog encoder wraparound.
    if (dTheta > 200.0.degrees) {
      servoNumRotations -= 1
    } else if (dTheta < (-200.0).degrees) {
      servoNumRotations += 1
    }

    val totalServoAngle = (servoNumRotations * 360.0).degrees + newTurretAngle

    totalTurretAngle = totalServoAngle * (40.0 / 20.0) * (80.0 / 176.0)
    prevTurretAngle = newTurretAngle
  }


  fun setTurretAngle(angle: Angle) {
    targetTurretAngle = angle
  }
  // endregion
  // region Shooter
  fun setShooterVelocity(velocity: AngularVelocity) {
    targetShooterVelocity = velocity
  }
  // endregion

  fun toggleHivePosition() {
    hivePosition = when (hivePosition) {
      HivePosition.RedAudience -> HivePosition.RedNonAudience
      HivePosition.RedNonAudience -> HivePosition.RedAudience
      HivePosition.BlueAudience -> HivePosition.BlueNonAudience
      HivePosition.BlueNonAudience -> HivePosition.BlueAudience
    }
  }

  /**
   * Wrap an angle to [-PI, PI].
   */
  private fun wrapRadians(angle: Double): Double {
    return atan2(sin(angle), cos(angle))
  }

  /**
   * Automatically aligns turret and shooter with the selected HIVE
   */
  fun autoAlign(currentPosition: Pose) {
    val heading = currentPosition.heading()
    // Calculate turret pivot offset in field coordinates.
    val offsetX = turretFBOffsetIn * cos(heading)
    val offsetY = turretFBOffsetIn * sin(heading)
    val turretOffsetFieldCentric = Pose(offsetX, offsetY)

    val turretFieldCentric = turretOffsetFieldCentric + currentPosition

    // Vector from turret pivot to HIVE
    val vecToHive = (hivePosition.pose - turretFieldCentric).toVector2D()

    val dx = vecToHive.x()
    val dy = vecToHive.y()

    // Default aiming vector is the direct vector to the HIVE.
    var aimX = dx
    var aimY = dy
    var distanceForShot = hypot(dx, dy)

    // region Shoot on the Move
    if (shootOnMoveEnabled && projectileHorizontalSpeedInPerSec > 0.0) {
      val fieldVelocity = velocitySupplier()
      // Velocity of the turret pivot, including the velocity
      // caused by rotation around the robot's center.
      val turretVx = fieldVelocity.vx - fieldVelocity.omega * offsetY
      val turretVy = fieldVelocity.vy + fieldVelocity.omega * offsetX
      val projectileSpeed = projectileHorizontalSpeedInPerSec

      val turretSpeed = hypot(turretVx, turretVy)

      // A projectile cannot reliably intercept a target if
      // its effective horizontal speed is not greater than
      // the turret's translational speed.
      if (projectileSpeed > turretSpeed + 1e-6) {

        // Initial estimate assuming no movement.
        var flightTime = distanceForShot / projectileSpeed

        // Iteratively calculate interception time.
        repeat(6) {

          // Predict where the HIVE will be relative to
          // the moving turret pivot at impact.
          aimX = dx - turretVx * flightTime
          aimY = dy - turretVy * flightTime
          distanceForShot = hypot(aimX, aimY)

          // Update estimated projectile flight time.
          flightTime = distanceForShot / projectileSpeed
        }
      }
    }

    // endregion

    val angleToHive = atan2(aimY, aimX)

    // Use the predicted distance for shooter RPM compensation.
    // When stationary, this is simply the ordinary distance.
    if (turretAutoAlign) {
      // Convert field-relative bearing into robot-relative angle.
      val robotRelativeAngle = wrapRadians(angleToHive - heading)

      // Select the equivalent target angle nearest to the
      // turret's current continuous encoder position.
      val currentTurretRadians = totalTurretAngle.baseUnitMagnitude
      val continuousTarget = currentTurretRadians + wrapRadians(robotRelativeAngle - currentTurretRadians)
      setTurretAngle(continuousTarget.radians)
    }
    if (shooterAutoAlign) {
      val shooterVelocity = (shooterVelocity1 * distanceForShot.pow(2) + shooterVelocity2 * distanceForShot + shooterVelocity3).rpm
      setShooterVelocity(shooterVelocity)
    }
    if (hoodAutoAlign) {
      // TODO: IMPLEMENT
    }
  }


  override fun periodic() {
    updateTurretAngle()
    if (autoAlign) {
      autoAlign(poseSupplier())
    }

    // PID tuning
    turretPID.coefficients.kP = turretP
    turretPID.coefficients.kD = turretD
    shooterMotorPID.coefficients.kP = shooterP
    shooterMotorPID.coefficients.kD = shooterD
    shooterMotorFeedforward.coefficients.kV = shooterkV

    // region Turret
    val turretError = (targetTurretAngle - totalTurretAngle).baseUnitMagnitude

    // Static friction compensation
    val turretFF = if (abs(turretError) > 0.01) {
      turretkS * sign(turretError)
    } else {
      0.0
    }

    val turretPower = (turretPID.calculate(error = turretError) + turretFF).coerceIn(-1.0, 1.0)
    leftTurretServo.power = turretPower
    rightTurretServo.power = turretPower
    // endregion
    // region Shooter Motor
    val motorVelocity = shooterMotor1.encoderVelocity
    val motorFF = shooterMotorFeedforward.calculate(
      MotionState(
        position = shooterMotor1.encoderPosition,
        velocity = targetShooterVelocity
      )
    )

    val velocityError = (targetShooterVelocity - motorVelocity).baseUnitMagnitude

    val shooterPower = (shooterMotorPID.calculate(error = velocityError) + motorFF).coerceIn(-1.0, 1.0)

    shooterMotor1.throttle = shooterPower
    shooterMotor2.throttle = shooterPower
    // endregion
  }
}