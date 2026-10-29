package org.firstinspires.ftc.teamcode.robot

import dev.nextftc.hardware.actuators.NextCRServo
import dev.nextftc.hardware.actuators.NextFeedbackCRServo
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.sensors.NextAnalogInput
import dev.nextftc.robot.Mechanism
import dev.nextftc.units.degrees
import dev.nextftc.units.measuretypes.Angle
import dev.nextftc.units.rpm

class Shooter : Mechanism {
  val leftTurretServo = NextFeedbackCRServo("LTurretServo", NextAnalogInput("TurretServoEncoder"))
  val rightTurretServo = NextCRServo("RTurretServo")

  val shooterMotor1 = NextMotor("ShooterMotor1")
  val shooterMotor2 = NextMotor("ShooterMotor2")

//  val turretOffset = Vector.zero(2)

  // Keep track of num rotations to determine total servo position
  private var servoNumRotations: Int = 0
  private var prevTurretAngle: Angle = 0.0.degrees

  var totalTurretAngle: Angle = 0.0.degrees
  var servoOffsetAngle: Angle = 0.0.degrees

  init {
    shooterMotor1.velocityPID.coefficients.kP
  }

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
    val dtheta = newTurretAngle - prevTurretAngle

    // Significant dtheta in 1 loop is prob wrap around
    // There is always the possibility tho that a significant loop delay could offset the turret position by 360deg
    if (dtheta > 200.0.degrees) { // 0 -> 360
      servoNumRotations -= 1
    } else if (dtheta < (-200.0).degrees) { // 360 -> 0
      servoNumRotations += 1
    }

    val totalServoAngle = (servoNumRotations * 360.0).degrees + newTurretAngle

    totalTurretAngle = totalServoAngle * (40.0 / 20.0) * (80.0 / 176.0)
  }

  override fun periodic() {
    updateTurretAngle()
  }

  fun setShooterRPM(rpm: Int) {
    shooterMotor1.setVelocitySetpoint(rpm.toDouble().rpm)
  }

  fun setShooterPower(power: Double) {
    shooterMotor1.throttle = power.coerceIn(-1.0, 1.0)
    shooterMotor2.throttle = (-power).coerceIn(-1.0, 1.0)
  }
}