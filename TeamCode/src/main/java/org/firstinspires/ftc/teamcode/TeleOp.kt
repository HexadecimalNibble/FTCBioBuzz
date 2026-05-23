package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.hexnibble.hexlib.L

@TeleOp(name = "Biobuzz Teleop")
class Teleop : LinearOpMode() {
    override fun runOpMode() {
        waitForStart()
        if (opModeIsActive()) {
            L.i("Teleop", "Saved robot position: ${RobotData.robotPosition}")
        }


    }
}