package org.firstinspires.ftc.teamcode

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.hexnibble.hexlib.L

@Autonomous(name = "Biobuzz Auto")
class Auto : LinearOpMode() {
    override fun runOpMode() {
        waitForStart()
        if (opModeIsActive()) {
            RobotData.robotPosition = Pose(100.0, 100.0, 100.0)
            L.i("Teleop", "Saving robot position: ${RobotData.robotPosition}")
        }
    }
}