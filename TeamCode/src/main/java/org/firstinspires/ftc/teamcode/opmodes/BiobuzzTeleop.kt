package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.robot.opmode.BulkReadHook
import dev.nextftc.robot.opmode.NextOpMode
import dev.nextftc.robot.opmode.NextTeleop
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot

@NextTeleop(name = "Biobuzz Teleop")
class BiobuzzTeleop(robot: BiobuzzRobot) : NextOpMode(robot, BulkReadHook) {
}