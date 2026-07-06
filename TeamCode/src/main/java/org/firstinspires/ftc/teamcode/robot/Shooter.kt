package org.firstinspires.ftc.teamcode.robot

import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.control.KineticState
import dev.nextftc.control.builder.controlSystem
import org.hexnibble.hexlib.RobotSystem
import org.hexnibble.hexlib.hardware.Motor
import org.hexnibble.hexlib.hardware.MotorType

class Shooter(hwMap: HardwareMap): RobotSystem() {
  val shooterMotor1 = Motor(hwMap, "ShooterMotor1", MotorType.GOBILDA_RPM_312, 81.0 / 207.0)
  val shooterMotor2 = Motor(hwMap, "ShooterMotor2", MotorType.GOBILDA_RPM_312, 81.0 / 207.0)
  val controlSystem = controlSystem {
    velPid(kP = 1.0)
    basicFF(kS = 1.0)
  }
  init {
    controlSystem.goal = KineticState(0.0, 0.0, 0.0)
    shooterMotor1.resetEncoder()
  }

  fun setTargetRPM(targetRPM: Double) {
    controlSystem.goal = KineticState(velocity = targetRPM)
  }

  override fun processCommands() {
    val power = controlSystem.calculate(KineticState(
      position = shooterMotor1.getCurrentPositionDeg(),
      velocity = shooterMotor1.getCurrentVelocityRPM()
    ))
    shooterMotor1.setPower(-power)
    shooterMotor2.setPower(power)
  }
}