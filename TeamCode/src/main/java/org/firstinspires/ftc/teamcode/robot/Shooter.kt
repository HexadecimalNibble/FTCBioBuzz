package org.firstinspires.ftc.teamcode.robot

import com.bylazar.configurables.annotations.Configurable
import dev.nextftc.control.feedback.PIDController
import dev.nextftc.control.feedforward.SimpleFeedforward
import dev.nextftc.control.model.MotionState
import dev.nextftc.hardware.actuators.NextCRServo
import dev.nextftc.hardware.actuators.NextFeedbackCRServo
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.sensors.NextAnalogInput
import dev.nextftc.robot.Mechanism
import dev.nextftc.units.degrees
import dev.nextftc.units.measuretypes.Angle
import dev.nextftc.units.measuretypes.AngularVelocity
import dev.nextftc.units.radians
import dev.nextftc.units.rpm
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
  }
  val leftTurretServo = NextFeedbackCRServo("LTurretServo", NextAnalogInput("TurretServoEncoder"))
  val rightTurretServo = NextCRServo("RTurretServo")

  // Shooter motor 1 has encoder
  val shooterMotor1 = NextMotor("ShooterMotor1", anglePerCount = (2 * PI / 28).radians)
  val shooterMotor2 = NextMotor("ShooterMotor2").also { it.direction = NextMotor.Direction.REVERSE }

  var targetShooterVelocity = 0.0.rpm

//  val turretOffset = Vector.zero(2)

  // Keep track of num rotations to determine total servo position
  private var servoNumRotations: Int = 0
  private var prevTurretAngle: Angle = 0.0.degrees

  var totalTurretAngle: Angle = 0.0.degrees
  var servoOffsetAngle: Angle = 0.0.degrees

  val shooterMotorPID = PIDController(kP = shooterP, kD = shooterD)
  val shooterMotorFeedforward = SimpleFeedforward(
    kV = shooterkV,
    kS = 0.0,
    kA = 0.0
  )

  fun resetTurretAngle() {
    servoOffsetAngle = -leftTurretServo.angle
    servoNumRotations = 0
    totalTurretAngle = 0.0.degrees
    prevTurretAngle = 0.0.degrees
  }

  /**
   * Updates turret position reading by reading the servo encoder
   */
  fun updateTurretAngle() {
    val newTurretAngle = leftTurretServo.angle + servoOffsetAngle
    val dTheta = newTurretAngle - prevTurretAngle

    // Significant dTheta in 1 loop is prob wrap around
    // There is always the possibility tho that a significant loop delay could offset the turret position by 360deg
    if (dTheta > 200.0.degrees) { // 0 -> 360
      servoNumRotations -= 1
    } else if (dTheta < (-200.0).degrees) { // 360 -> 0
      servoNumRotations += 1
    }

    val totalServoAngle = (servoNumRotations * 360.0).degrees + newTurretAngle

    totalTurretAngle = totalServoAngle * (40.0 / 20.0) * (80.0 / 176.0)
  }

  fun setShooterVelocity(velocity: AngularVelocity) {
    targetShooterVelocity = velocity
  }

  override fun periodic() {
    updateTurretAngle()

    // For PID Tuning
    val motorVelocity = shooterMotor1.encoderVelocity

    shooterMotorPID.coefficients.kP = shooterP
    shooterMotorPID.coefficients.kD = shooterD
    shooterMotorFeedforward.coefficients.kV = shooterkV

    val ff = shooterMotorFeedforward.calculate(MotionState(
      position = shooterMotor1.encoderPosition,
      velocity = targetShooterVelocity,
    ))

    val velocityError = (targetShooterVelocity - motorVelocity).magnitude

    val shooterPower = (shooterMotorPID.calculate(error = velocityError) + ff).coerceIn(-1.0, 1.0)
    println("Shooter power: $shooterPower")
    shooterMotor1.throttle = shooterPower
    shooterMotor2.throttle = shooterPower
  }
}