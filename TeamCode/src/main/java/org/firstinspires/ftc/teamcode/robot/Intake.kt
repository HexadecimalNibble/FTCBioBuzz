package org.firstinspires.ftc.teamcode.robot

import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.control.KineticState
import dev.nextftc.control.builder.controlSystem
import org.hexnibble.hexlib.RobotSystem
import org.hexnibble.hexlib.hardware.Motor
import org.hexnibble.hexlib.hardware.MotorType

class Intake(hwMap: HardwareMap): RobotSystem() {
  val intakeMotor = Motor(hwMap, "IntakeMotor", MotorType.GOBILDA_BARE)
  val targetPos: Double = 0.0
  val controlSystem = controlSystem {
    posPid(kP = 1.0)
    basicFF(kS = 1.0)
  }
  init {
    controlSystem.goal = KineticState(0.0, 1.0, 2.0)
  }

  fun startIntake() = intakeMotor.setPower(1.0)
  fun stopIntake() = intakeMotor.setPower(0.0)
  fun reverseIntake() = intakeMotor.setPower(-1.0)

  override fun processCommands() {
    intakeMotor.setPower(controlSystem.calculate(KineticState(
      position = intakeMotor.getCurrentPosition().toDouble(),
      velocity = intakeMotor.getCurrentVelocityRPM()
    )))
  }
}