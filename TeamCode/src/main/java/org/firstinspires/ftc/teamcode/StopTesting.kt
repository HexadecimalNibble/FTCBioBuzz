package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotorImplEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.robotcore.internal.system.AppUtil
import org.firstinspires.ftc.robotcore.internal.ui.UILocation
import kotlin.math.abs

@TeleOp
class StopTesting : LinearOpMode() {
  init {
    AppUtil.getInstance().showToast(UILocation.BOTH, "Hi", 1)
  }
  override fun runOpMode() {
    var lastLoopTime = 0UL

    val lFMotor = hardwareMap.get(DcMotorImplEx::class.java, "LFMotor").also { it.direction = DcMotorSimple.Direction.REVERSE }
    val lBMotor = hardwareMap.get(DcMotorImplEx::class.java, "LBMotor").also { it.direction = DcMotorSimple.Direction.REVERSE }
    val rFMotor = hardwareMap.get(DcMotorImplEx::class.java, "RFMotor")
    val rBMotor = hardwareMap.get(DcMotorImplEx::class.java, "RBMotor")

    waitForStart()
    while (opModeIsActive()) {
      val robotX = gamepad1.left_stick_x
      val robotY = -gamepad1.left_stick_y
      val triggers = gamepad1.right_trigger - gamepad1.left_trigger

      val lFRaw = robotY + robotX + triggers
      val lBRaw = robotY - robotX + triggers
      val rFRaw = robotY - robotX - triggers
      val rBRaw = robotY + robotX - triggers

      val denom = maxOf(abs(lFRaw), abs(lBRaw), abs(rFRaw), abs(rBRaw), 1f).toDouble()

      lFMotor.power = lFRaw / denom
      lBMotor.power = lBRaw / denom
      rFMotor.power = rFRaw / denom
      rBMotor.power = rBRaw / denom

      val currentTime = (System.nanoTime() / 1e6).toULong()
      println("Last loop time: ${currentTime - lastLoopTime}ms")
      lastLoopTime = currentTime
    }
  }
}