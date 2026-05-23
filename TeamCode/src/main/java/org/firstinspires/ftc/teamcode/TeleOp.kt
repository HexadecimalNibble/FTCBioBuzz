package org.firstinspires.ftc.teamcode

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.hexnibble.hexlib.L

@TeleOp(name = "Biobuzz Teleop")
class Teleop : LinearOpMode() {
    override fun runOpMode() {
        if (opModeIsActive()) {
            L.i("Teleop", "Saved robot position: ${SavedData.robotPosition}")
        }
    }
}