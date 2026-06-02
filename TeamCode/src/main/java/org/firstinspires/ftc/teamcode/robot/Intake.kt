package org.firstinspires.ftc.teamcode.robot

import com.qualcomm.robotcore.hardware.HardwareMap
import org.hexnibble.hexlib.hardware.Motor
import org.hexnibble.hexlib.hardware.MotorType

class Intake(hwMap: HardwareMap) {
  val intakeMotor = Motor(hwMap, "IntakeMotor", MotorType.GOBILDA_BARE)

  fun startIntake() = intakeMotor.setPower(1.0)
  fun stopIntake() = intakeMotor.setPower(0.0)
  fun reverseIntake() = intakeMotor.setPower(-1.0)
}