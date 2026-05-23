package org.firstinspires.ftc.teamcode

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.hexnibble.hexlib.L

@TeleOp(name = "Biobuzz Auto")
class Auto : LinearOpMode() {
    override fun runOpMode() {
        if (opModeIsActive()) {
            SavedData.robotPosition = Pose(100.0, 100.0, 100.0)
            L.i("Teleop", "Saving robot position: ${SavedData.robotPosition}")
        }
    }
}