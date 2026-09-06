package org.firstinspires.ftc.teamcode.opmodes

import dev.nextftc.robot.opmode.BulkReadHook
import dev.nextftc.robot.opmode.NextAutonomous
import dev.nextftc.robot.opmode.NextOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.RobotData
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot

enum class RobotAuto {
  Close,
  Far,
}

@NextAutonomous("Biobuzz Auto")
class Auto(robot: BiobuzzRobot) : NextOpMode(robot, BulkReadHook) {
  var selectedAutoIndex = 0
  init {
    // Set telemetry display format to HTML for cooler setup menu
    telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML)
  }

  fun checkAndSetAllianceInfo() {
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
    } else if (RobotData.auto == null) {
      telemetry.addLine("<big><b>Select an auto</b></big>")
      for (auto in RobotAuto.entries) {
        telemetry.addLine("[${if (auto.ordinal == selectedAutoIndex) "x" else ""}] ${auto.name}")
      }

      if (gamepad1.leftBumperWasPressed() || gamepad1.dpadUpWasPressed()) {
        selectedAutoIndex = if (selectedAutoIndex > 0) selectedAutoIndex - 1 else 0
      } else if (gamepad1.rightBumperWasPressed() || gamepad1.dpadDownWasPressed()) {
        selectedAutoIndex = if (selectedAutoIndex < RobotAuto.entries.size - 1) selectedAutoIndex + 1 else RobotAuto.entries.size - 1
      } else if (gamepad1.crossWasPressed()) {
        RobotData.auto = RobotAuto.entries[selectedAutoIndex]
      }
    } else {
      // TODO: Set up follower

      // TODO: Create auto

      telemetry.addLine("<big><b>Ready to start</b></big>")
    }

    telemetry.update()
  }

  override fun disabledPeriodic() {
    checkAndSetAllianceInfo()
  }

  override fun start() {
    // Reset telemetry display format
    telemetry.setDisplayFormat(Telemetry.DisplayFormat.CLASSIC)
  }
}