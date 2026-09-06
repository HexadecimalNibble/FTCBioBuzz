package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.robot.opmode.BulkReadHook
import dev.nextftc.robot.opmode.NextOpMode
import dev.nextftc.robot.opmode.NextTeleop
import dev.nextftc.robot.triggers.CommandGamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.RobotData
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot

@NextTeleop(name = "Biobuzz Teleop")
class Teleop(robot: BiobuzzRobot) : NextOpMode(robot, BulkReadHook) {
  lateinit var driver: CommandGamepad

  init {
    // Set telemetry display format to HTML for cooler setup menu
    telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML)
  }

  fun checkAndSetAllianceColor() {
    // If options pressed at any time, reset alliance info
    if (gamepad1.options) {
      RobotData.allianceColor = null
    }

    // Set alliance color if not previously set
    if (RobotData.allianceColor == null) {
      telemetry.addLine("<big><b>Select an alliance color</b></big>")

      telemetry.addLine("🟦 Blue (Left Bumper)")
      telemetry.addLine("🟥 Red (Right Bumper)")

      if (gamepad1.leftBumperWasPressed()) {
        RobotData.allianceColor = AllianceColor.Blue
      } else if (gamepad1.rightBumperWasPressed()) {
        RobotData.allianceColor = AllianceColor.Red
      }
    } else {

    }

    telemetry.update()
  }

  override fun disabledPeriodic() {
    checkAndSetAllianceColor()
  }

  override fun start() {
    driver = CommandGamepad(gamepad1)

    // Reset telemetry display format
    telemetry.setDisplayFormat(Telemetry.DisplayFormat.CLASSIC)
  }
}