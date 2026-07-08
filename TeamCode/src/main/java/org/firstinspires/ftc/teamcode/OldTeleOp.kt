package org.firstinspires.ftc.teamcode

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp

@TeleOp(name = "Test Teleop")
class OldTeleop : LinearOpMode() {
  override fun runOpMode() {
    for (module in hardwareMap.getAll(LynxModule::class.java)) {
      println("Module: ${module.moduleAddress}; ${module.revProductNumber}")
    }
  }
}