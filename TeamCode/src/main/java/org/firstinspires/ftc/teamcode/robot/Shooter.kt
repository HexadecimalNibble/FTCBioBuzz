package org.firstinspires.ftc.teamcode.robot

import com.bylazar.configurables.annotations.Configurable
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
    var turretkS = 0.0
  }
  // region * Turret *
  private val leftTurretServo = NextFeedbackCRServo("LTurretServo", NextAnalogInput("TurretServoEncoder1", maxVoltage = 3.2633.volts)).also { it.reverse() }
  private val rightTurretServo = NextFeedbackCRServo("RTurretServo", NextAnalogInput("TurretServoEncoder2")).also { it.reverse() }

  // Turret is 28mm behind wheel-base center with intake at front
  private val turretXOffsetMm = -28.0

  // Keep track of num rotations to determine total servo position
  private var servoNumRotations: Int = 0
  private var prevTurretAngle: Angle = 0.0.degrees

  private var totalTurretAngle: Angle = 0.0.degrees
  private var servoOffsetAngle: Angle = 0.0.degrees

  private val turretPID = PIDController(kP = turretP, kD = turretD)
  private val turretFeedforward = SimpleFeedforward(
    kV = 0.0,
    kS = turretkS,
    kA = 0.0
  )

  var targetTurretAngle: Angle = 0.0.degrees
  // endregion * Turret *
  // region * Shooter Motor *
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
  // endregion * Shooter Motor *

  fun initSystem() {
    // Turn on turret servo pwm
    leftTurretServo.power = 0.0
    rightTurretServo.power = 0.0
  }

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

  fun setShooterVelocity(velocity: AngularVelocity) {
    targetShooterVelocity = velocity
  }

  override fun periodic() {
    updateTurretAngle()

    // For PID tuning
    turretPID.coefficients.kP = turretP
    turretPID.coefficients.kD = turretD
    turretFeedforward.coefficients.kS = turretkS
    shooterMotorPID.coefficients.kP = shooterP
    shooterMotorPID.coefficients.kD = shooterD
    shooterMotorFeedforward.coefficients.kV = shooterkV

    // region * Turret *
    val turretFF = turretFeedforward.calculate(MotionState(
      position = totalTurretAngle
    ))
    // TODO: FIX NOT CHANGING?
//    println("Turret FF: $turretFF")

    val turretError = (targetTurretAngle - totalTurretAngle).baseUnitMagnitude
//    println("turret error: $turretError")

    val turretPower = (turretPID.calculate(error = turretError) + turretFF).coerceIn(-1.0, 1.0)
    println("turret power: $turretPower")
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
    println("Shooter power: $shooterPower")
    shooterMotor1.throttle = shooterPower
    shooterMotor2.throttle = shooterPower
    // endregion * Shooter Motor *
  }
}