package org.firstinspires.ftc.teamcode.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.pedro.PedroConstants
import kotlin.math.abs

@TeleOp(name = "Maximum Turn Speed", group = "Pedro Tuning")
class MaxTurnSpeed : LinearOpMode() {

  override fun runOpMode() {

    val follower = PedroConstants.create(hardwareMap)

    var maxPositiveOmega = 0.0
    var maxNegativeOmega = 0.0
    var maxAbsoluteOmega = 0.0

    // Initialize localization
    follower.update()

    telemetry.addLine("Maximum Turning Speed Test")
    telemetry.addLine("Use right joystick to rotate.")
    telemetry.addLine("Hold fully left or right for maximum speed.")
    telemetry.addLine("Press STOP to finish.")
    telemetry.update()

    waitForStart()

    try {
      while (opModeIsActive()) {

        // Manual robot-centric turning
        val turn = gamepad1.right_stick_x.toDouble()

        follower.manual(0.0, 0.0, turn)
        follower.update()

        // Pedro 3 angular velocity (rad/s)
        val omega = follower.velocity().omega

        // Only record measurements at >=95% turning input
        if (abs(turn) >= 0.95) {

          if (omega > maxPositiveOmega) {
            maxPositiveOmega = omega
          }

          if (omega < maxNegativeOmega) {
            maxNegativeOmega = omega
          }

          maxAbsoluteOmega =
            maxOf(maxAbsoluteOmega, abs(omega))
        }

        telemetry.addData("Turn Input", "%.2f", turn)
        telemetry.addData("Current Omega", "%.3f rad/s", omega)

        telemetry.addData(
          "Maximum Positive Omega",
          "%.3f rad/s",
          maxPositiveOmega
        )

        telemetry.addData(
          "Maximum Negative Omega",
          "%.3f rad/s",
          maxNegativeOmega
        )

        telemetry.addData(
          "MAX TURNING SPEED",
          "%.3f rad/s",
          maxAbsoluteOmega
        )

        telemetry.update()
        idle()
      }
    } finally {
      follower.stop()
      follower.update()
    }
  }
}
