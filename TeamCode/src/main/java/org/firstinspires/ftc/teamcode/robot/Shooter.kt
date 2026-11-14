package org.firstinspires.ftc.teamcode.robot

import com.bylazar.configurables.annotations.Configurable
import com.pedropathing.math.Pose
import com.pedropathing.math.Velocity
import dev.nextftc.control.feedback.PIDController
import dev.nextftc.control.feedforward.SimpleFeedforward
import dev.nextftc.control.model.MotionState
import dev.nextftc.hardware.actuators.NextFeedbackCRServo
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.actuators.NextServo
import dev.nextftc.hardware.sensors.NextAnalogInput
import dev.nextftc.robot.Mechanism
import dev.nextftc.units.Radians
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
import kotlin.math.sqrt

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
    var shooterP = 0.005
    @JvmField
    var shooterD = 0.0
    @JvmField
    var shooterkV = 0.00024

    @JvmField
    var turretP = 0.4
    @JvmField
    var turretD = 0.025
    @JvmField
    var turretkS = 0.03

    @JvmField
    var turretAutoAlign = true
    @JvmField
    var shooterAutoAlign = true

    // https://www.desmos.com/calculator/z10no2pcxe
    @JvmField
    var shooterVelocity1 = 0.100715
    @JvmField
    var shooterVelocity2 = 1.77419
    @JvmField
    var shooterVelocity3 = 2288.48708

    // SOM settings
    @JvmField
    var shootOnMoveEnabled = false

    // Effective horizontal projectile velocity (in/s).
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

  private val blockerServo = NextServo("BlockerServo")

  // Turret is 28 mm behind the wheel-base center, with intake at the front.
  // TODO: UPDATE
  private val turretFBOffsetIn = -28.0 / 25.4

  // Tracks continuous servo rotations across encoder wraparound.
  private var servoNumRotations = 0

  private var prevRawTurretAngle: Angle? = null

  var totalTurretAngle: Angle = 0.0.degrees
    private set

  private var servoOffsetAngle: Angle = 0.0.degrees

  // Used to wait for the encoder to stabilize during zeroing.
  private var turretZeroPending = false
  private var turretZeroCandidate: Angle? = null
  private var turretZeroStableSamples = 0

  private val turretPID = PIDController(
    kP = turretP,
    kD = turretD
  )

  var targetTurretAngle: Angle = 0.0.degrees

  var hiveAlignmentTurretOffset: Angle = 0.0.degrees
  // endregion
  // region Shooter Motor Vars
  // Shooter motor 1 has an encoder.
  val shooterMotor1 = NextMotor(
    "ShooterMotor1",
    anglePerCount = (2 * PI / 28).radians
  )

//  private val shooterMotor2 =
//    NextMotor("ShooterMotor2").also {
//      it.direction = NextMotor.Direction.REVERSE
//    }

  private var targetShooterVelocity = 3100.0.rpm

  private val shooterMotorPID = PIDController(
    kP = shooterP,
    kD = shooterD
  )

  private val shooterMotorFeedforward = SimpleFeedforward(
    kV = shooterkV,
    kS = 0.0,
    kA = 0.0
  )

  var autoAlign = false

  var hivePosition = HivePosition.RedAudience

  lateinit var poseSupplier: () -> Pose
  lateinit var velocitySupplier: () -> Velocity

  private var turretPIDOn = false
  private var shooterPIDOn = false

  private var blockerEngagedPos = 0.24
  private var blockerRetractedPos = 0.43
  // endregion

  fun initSystem() {
    leftTurretServo.power = 0.0
    rightTurretServo.power = 0.0
  }

  // region Turret
  fun resetTurretAngle() {
    // Disable the PID while resetting.
    turretPIDOn = false

    leftTurretServo.power = 0.0
    rightTurretServo.power = 0.0

    servoNumRotations = 0
    totalTurretAngle = 0.0.degrees

    prevRawTurretAngle = null

    // Wait for stable encoder readings before establishing zero.
    turretZeroPending = true
    turretZeroCandidate = null
    turretZeroStableSamples = 0
  }

  /**
   * Updates turret position by reading the servo encoder.
   */
  fun updateTurretAngle() {
    val rawAngle = leftTurretServo.angle

    // Wait for three consistent readings before establishing zero.
    if (turretZeroPending) {
      val candidate = turretZeroCandidate

      if (candidate != null) {
        val difference = wrapRadians(
          rawAngle.baseUnitMagnitude -
              candidate.baseUnitMagnitude
        )

        if (abs(difference) < Math.toRadians(1.0)) {
          turretZeroStableSamples++
        } else {
          turretZeroStableSamples = 0
        }
      }

      turretZeroCandidate = rawAngle

      if (turretZeroStableSamples >= 2) {
        servoOffsetAngle = -rawAngle
        servoNumRotations = 0
        prevRawTurretAngle = rawAngle
        totalTurretAngle = 0.0.degrees

        turretZeroPending = false

        println("Turret encoder zero established: $rawAngle")
      }

      return
    }

    val previousRaw = prevRawTurretAngle

    // Initialize the encoder if no reference has been established.
    if (previousRaw == null) {
      servoOffsetAngle = -rawAngle
      prevRawTurretAngle = rawAngle
      totalTurretAngle = 0.0.degrees
      return
    }

    val previousRadians = previousRaw.baseUnitMagnitude
    val currentRadians = rawAngle.baseUnitMagnitude

    val delta = currentRadians - previousRadians

    // Only recognize actual transitions across the 0/360 boundary.
    val lowThreshold = PI / 2.0
    val highThreshold = 3.0 * PI / 2.0

    if (
      delta > PI &&
      previousRadians < lowThreshold &&
      currentRadians > highThreshold
    ) {
      // Crossed 0 degrees in the negative direction.
      servoNumRotations -= 1

    } else if (
      delta < -PI &&
      previousRadians > highThreshold &&
      currentRadians < lowThreshold
    ) {
      // Crossed 360 degrees in the positive direction.
      servoNumRotations += 1
    }

    prevRawTurretAngle = rawAngle

    totalTurretAngle =
      (servoNumRotations * 360.0).degrees +
          rawAngle +
          servoOffsetAngle
  }

  fun setTurretAngle(angle: Angle) {
    turretPIDOn = true
    targetTurretAngle = wrapRadians(angle.into(Radians)).radians
  }

  fun offsetTurretAlignmentRight() {
    hiveAlignmentTurretOffset += 1.0.degrees
  }

  fun offsetTurretAlignmentLeft() {
    hiveAlignmentTurretOffset -= 1.0.degrees
  }

  // endregion
  // region Shooter
  fun stopShooter() {
    shooterPIDOn = false
    shooterMotor1.throttle = 0.0
//    shooterMotor2.throttle = 0.0
  }

  fun setShooterVelocity(velocity: AngularVelocity) {
    shooterPIDOn = true
    targetShooterVelocity = velocity
  }

  fun changeShooterVelocity(change: AngularVelocity) {
    setShooterVelocity(targetShooterVelocity + change)
  }
  // endregion
  // region Blocker

  fun decrementBlockerEngaged() {
    blockerEngagedPos -= 0.02
  }

  fun incrementBlockerEngaged() {
    blockerEngagedPos += 0.02
  }

  fun engageBlocker() {
    blockerServo.position = blockerEngagedPos
  }

  fun disengageBlocker() {
    blockerServo.position = blockerRetractedPos
  }

  fun toggleHivePosition() {
    hivePosition = when (hivePosition) {
      HivePosition.RedAudience -> HivePosition.RedNonAudience
      HivePosition.RedNonAudience -> HivePosition.RedAudience
      HivePosition.BlueAudience -> HivePosition.BlueNonAudience
      HivePosition.BlueNonAudience -> HivePosition.BlueAudience
    }
  }

  /**
   * Wraps an angle to [-PI, PI].
   */
  private fun wrapRadians(angle: Double): Double {
    return atan2(sin(angle), cos(angle))
  }

//  /**
//   * Calculates the time needed for a projectile to intercept a stationary
//   * target while the shooter is moving.
//   *
//   * Returns null when no positive, finite interception time exists.
//   *
//   * All velocities must use compatible units.
//   */
//  private fun calculateInterceptTime(
//    dx: Double,
//    dy: Double,
//    velocityX: Double,
//    velocityY: Double,
//    projectileSpeed: Double
//  ): Double? {
//    val a =
//      velocityX * velocityX +
//          velocityY * velocityY -
//          projectileSpeed * projectileSpeed
//
//    val b = -2.0 * (dx * velocityX + dy * velocityY)
//    val c = dx * dx + dy * dy
//
//    if (!a.isFinite() || !b.isFinite() || !c.isFinite()) {
//      return null
//    }
//
//    // Already at the target.
//    if (c <= 1e-12) {
//      return 0.0
//    }
//
//    // Handle the approximately linear case.
//    if (abs(a) < 1e-9) {
//      if (abs(b) < 1e-9) {
//        return null
//      }
//
//      val time = -c / b
//      return time.takeIf { it > 0.0 && it.isFinite() }
//    }
//
//    val discriminant = b * b - 4.0 * a * c
//
//    if (discriminant < 0.0 || !discriminant.isFinite()) {
//      return null
//    }
//
//    val sqrtDiscriminant = sqrt(discriminant)
//    val denominator = 2.0 * a
//
//    val time1 = (-b - sqrtDiscriminant) / denominator
//    val time2 = (-b + sqrtDiscriminant) / denominator
//
//    var bestTime = Double.POSITIVE_INFINITY
//
//    if (time1 > 0.0 && time1.isFinite()) {
//      bestTime = time1
//    }
//
//    if (time2 > 0.0 && time2.isFinite() && time2 < bestTime) {
//      bestTime = time2
//    }
//
//    return bestTime.takeIf { it.isFinite() }
//  }

  /**
   * Automatically aligns the turret and shooter with the selected HIVE.
   */
  fun autoAlign(currentPosition: Pose) {
    println("Aligning")
    val heading = currentPosition.heading()

    // Calculate turret pivot offset in field coordinates.
    val offsetX = turretFBOffsetIn * cos(heading)
    val offsetY = turretFBOffsetIn * sin(heading)

    val turretOffsetFieldCentric = Pose(offsetX, offsetY)
    val turretFieldCentric = turretOffsetFieldCentric + currentPosition

    // Vector from turret pivot to HIVE.
    val vecToHive = (hivePosition.pose - turretFieldCentric).toVector2D()

    val dx = vecToHive.x()
    val dy = vecToHive.y()

    // Default aiming vector is the direct vector to the HIVE.
    var aimX = dx
    var aimY = dy
    var distanceForShot = hypot(dx, dy)

//    // region Shoot on the Move
//    if (
//      shootOnMoveEnabled &&
//      projectileHorizontalSpeedInPerSec > 0.0 &&
//      projectileHorizontalSpeedInPerSec.isFinite() &&
//      ::velocitySupplier.isInitialized
//    ) {
//      val fieldVelocity = velocitySupplier()
//
//      // Velocity of the turret pivot, including velocity caused by
//      // rotation around the robot's center.
//      val turretVx = fieldVelocity.vx - fieldVelocity.omega * offsetY
//      val turretVy = fieldVelocity.vy + fieldVelocity.omega * offsetX
//
//      val flightTime = calculateInterceptTime(
//        dx = dx,
//        dy = dy,
//        velocityX = turretVx,
//        velocityY = turretVy,
//        projectileSpeed = projectileHorizontalSpeedInPerSec
//      )
//
//      // If no valid intercept exists, retain the direct aiming vector.
//      if (flightTime != null) {
//        val predictedAimX = dx - turretVx * flightTime
//        val predictedAimY = dy - turretVy * flightTime
//
//        if (predictedAimX.isFinite() && predictedAimY.isFinite()) {
//          aimX = predictedAimX
//          aimY = predictedAimY
//          distanceForShot = hypot(aimX, aimY)
//        }
//      }
//    }
//    // endregion

    val angleToHive = atan2(aimY, aimX)

    if (turretAutoAlign) {
      // Convert field-relative bearing into robot-relative angle.
      val robotRelativeAngle = wrapRadians(heading - angleToHive)

      println(
        """
    ----- AUTO ALIGN DEBUG -----
    Robot X: ${currentPosition.x()}
    Robot Y: ${currentPosition.y()}
    Robot Heading: ${Math.toDegrees(heading)}

    HIVE X: ${hivePosition.pose.x()}
    HIVE Y: ${hivePosition.pose.y()}

    Turret X: ${turretFieldCentric.x()}
    Turret Y: ${turretFieldCentric.y()}

    Field Bearing: ${Math.toDegrees(angleToHive)}
    Target Turret: ${Math.toDegrees(robotRelativeAngle)}
    Current Turret: ${Math.toDegrees(totalTurretAngle.baseUnitMagnitude)}
    ----------------------------
    """.trimIndent()
      )

      // Select the equivalent target angle nearest to the current
      // continuous encoder position.
      val currentTurretRadians = totalTurretAngle.baseUnitMagnitude

      val continuousTarget = currentTurretRadians + wrapRadians(robotRelativeAngle - currentTurretRadians)

      setTurretAngle(continuousTarget.radians + hiveAlignmentTurretOffset)
    }
    println("Distance for shot: $distanceForShot")
    if (shooterAutoAlign) {
      val shooterVelocity = (
          shooterVelocity1 * distanceForShot.pow(2) +
              shooterVelocity2 * distanceForShot +
              shooterVelocity3
          ).rpm

      setShooterVelocity(shooterVelocity)
    }
  }

  /**
   * Manually sets turret power.
   * Used for manual teleop turret movements.
   */
  fun setTurretPower(power: Double) {
    turretPIDOn = false
    autoAlign = false
    leftTurretServo.power = power
    rightTurretServo.power = power
  }

  override fun periodic() {
    updateTurretAngle()

    if (autoAlign && ::poseSupplier.isInitialized) {
      autoAlign(poseSupplier())
    }

    // region Turret PID
    if (turretPIDOn && !turretZeroPending) {
      println("Target turret angle: $targetTurretAngle")
      turretPID.coefficients.kP = turretP
      turretPID.coefficients.kD = turretD

      val turretError = (targetTurretAngle - totalTurretAngle).baseUnitMagnitude

      // Static friction compensation.
      val turretFF = if (abs(turretError) > 0.01) {
        turretkS * sign(turretError)
      } else {
        0.0
      }

      val turretPower = (turretPID.calculate(error = turretError) + turretFF).coerceIn(-1.0, 1.0)

      leftTurretServo.power = turretPower
      rightTurretServo.power = turretPower
    }
    // endregion
    // region Shooter Motor PID
    if (shooterPIDOn) {
      shooterMotorPID.coefficients.kP = shooterP
      shooterMotorPID.coefficients.kD = shooterD
      shooterMotorFeedforward.coefficients.kV = shooterkV

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
//      shooterMotor2.throttle = shooterPower
    }
    // endregion
  }
}