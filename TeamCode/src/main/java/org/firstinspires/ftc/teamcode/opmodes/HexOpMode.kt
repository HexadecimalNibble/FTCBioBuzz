package org.firstinspires.ftc.teamcode.opmodes

import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.RobotData

interface HexOpMode {
  /**
   * Prompts to select an alliance color and sets it if user selects one
   */
  fun setAllianceColor(gamepad: Gamepad, telemetry: Telemetry) {
    telemetry.addLine("<big><b>Select an alliance color</b></big>")
    telemetry.addLine("🟦 Blue (Left Bumper)")
    telemetry.addLine("🟥 Red (Right Bumper)")

    if (gamepad.leftBumperWasPressed()) {
      RobotData.allianceColor = AllianceColor.Blue
    } else if (gamepad.rightBumperWasPressed()) {
      RobotData.allianceColor = AllianceColor.Red
    }
  }
}