package org.firstinspires.ftc.teamcode.opmodes

import com.pedropathing.geometry.Pose
import dev.nextftc.robot.opmode.BulkReadHook
import dev.nextftc.robot.opmode.NextAutonomous
import dev.nextftc.robot.opmode.NextOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.RobotData
import org.firstinspires.ftc.teamcode.pedroPathing.PedroConstants
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot

enum class RobotAuto {
  Close,
  Far,
}

@NextAutonomous("Biobuzz Auto")
class Auto(robot: BiobuzzRobot) : NextOpMode(robot, BulkReadHook), HexOpMode {
  var selectedAutoIndex = 0
  val follower = PedroConstants.createFollower(hardwareMap)

  init {
    // Set telemetry display format to HTML for cooler setup menu
    telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML)
  }

  fun checkAndSetAllianceInfo() {
    // If options pressed at any time, reset alliance info
    if (gamepad1.options) {
      RobotData.allianceColor = null
      RobotData.auto = null
    }

    // Set alliance color if not previously set
    if (RobotData.allianceColor == null) {
      setAllianceColor(gamepad1, telemetry)
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
      when (RobotData.auto) {
        RobotAuto.Close -> {
          follower.setStartingPose(Pose(0.0, 0.0, Math.toRadians(0.0)))

          // TODO: Create auto
        }
        RobotAuto.Far -> {
          follower.setStartingPose(Pose(0.0, 0.0, Math.toRadians(0.0)))

          // TODO: Create auto
        }
        else -> {
          throw Error("RobotData.auto is null?")
        }
      }
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