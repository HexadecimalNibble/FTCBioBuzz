package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.internal.system.AppUtil
import org.firstinspires.ftc.robotcore.internal.ui.UILocation
import org.hexnibble.hexlib.L
import java.lang.reflect.Field
import java.lang.reflect.Modifier

@TeleOp
class StopTesting : LinearOpMode() {
  init {
    AppUtil.getInstance().showToast(UILocation.BOTH, "Toast message! 25620 was here.")
    val opModeInternal = Class.forName("com.qualcomm.robotcore.eventloop.opmode.OpModeInternal")
    val constructor = opModeInternal.getDeclaredConstructor()
    constructor.isAccessible = true
    val field = opModeInternal::class.java.getDeclaredField("MS_BEFORE_FORCE_STOP_AFTER_STOP_REQUESTED")
    field.isAccessible = true

    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())

    field.setInt(null, 5000)
  }
  override fun runOpMode() {
    waitForStart()
    while (opModeIsActive()) {
      L.i("StopTesting", "running!")
    }
    while (true) {
      L.i("StopTesting", "Stopped but still running!")
    }
  }
}