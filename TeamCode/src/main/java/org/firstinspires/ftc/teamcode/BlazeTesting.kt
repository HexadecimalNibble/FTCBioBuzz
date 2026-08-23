package org.firstinspires.ftc.teamcode

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.util.ElapsedTime
import dev.anygeneric.blazeftc.DummyPlugOpMode
import dev.anygeneric.blazeftc_pedro.PedroSingleDataLocalizer
import org.firstinspires.ftc.teamcode.pedroPathing.PedroConstants

@TeleOp
class BlazeTesting : DummyPlugOpMode() {
  override fun runOpModeInBlaze() {
    val tele = initializeBlazeFTC(telemetry)
    // Normal manual cache setup
    hardwareMap.getAll(LynxModule::class.java).forEach { it.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL) }
    engageMotorAcceleration()
    val follower = PedroConstants.createFollower(hardwareMap)
    waitForStart()

    // Pedro thread
    val elt = ElapsedTime()
    PedroSingleDataLocalizer.setup(follower) {
      tele.addData("pedro loop time (ms)", elt.milliseconds())
      elt.reset()
      follower.update()
      tele.addData("x,y", follower.pose.x.toString() + ", " + follower.pose.y)
    }

    follower.startTeleOpDrive(true)

//    val e1 = ElapsedTime()
//    engageBulkReadAcceleration(true, 1) {
//      tele.addData("bulk read loop time (ms)", e1.milliseconds())
//      e1.reset()
//      telemetry.update()
//
//      follower.setTeleOpDrive(
//        -gamepad1.left_stick_y.toDouble(),
//        -gamepad1.left_stick_x.toDouble(),
//        (-gamepad1.right_trigger + gamepad1.left_trigger).toDouble(),
//        true
//      )
//    }

    runBlazeFTC(0)

    val servo = hardwareMap.get("servo") as Servo

    val elt2 = ElapsedTime()
    while (!isStopRequested) {
      sleep(5)

      val joystick = gamepad1.right_stick_x.toDouble()
      val pos = (joystick + 1) / 2
      servo.position = pos
      tele.addData("main loop time (ms)", elt2.milliseconds())
      tele.addLine("r joystick x pos: $joystick; corrected: $pos")
      tele.update()
      elt2.reset()
    }
  }
}