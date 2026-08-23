package org.firstinspires.ftc.teamcode

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.util.ElapsedTime
import dev.anygeneric.blazeftc.DummyPlugOpMode

@TeleOp
class DuplicateTelemetry : DummyPlugOpMode() {
  override fun runOpModeInBlaze() {
    val tele = initializeBlazeFTC(telemetry)
    hardwareMap.getAll(LynxModule::class.java).forEach { it.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL) }
    engageMotorAcceleration()
    waitForStart()

    runBlazeFTC(0)

    val servo = hardwareMap.get("servo") as Servo

    val elt2 = ElapsedTime()
    while (!isStopRequested) {
      sleep(5)
      servo.position = (gamepad1.right_stick_x.toDouble() + 1) / 2
      tele.addData("main loop time (ms)", elt2.milliseconds())
      tele.update()
      elt2.reset()
    }
  }
}