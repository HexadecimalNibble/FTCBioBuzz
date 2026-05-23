package org.firstinspires.ftc.teamcode

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.hexnibble.hexlib.AllianceColor
import org.hexnibble.hexlib.AllianceSide
import org.hexnibble.hexlib.L
import org.hexnibble.hexlib.gamepad.ButtonGroupController
import org.hexnibble.hexlib.opmode.CoreAutoOpMode

@Autonomous(name = "Biobuzz Auto")
class Auto : CoreAutoOpMode() {
  lateinit var allianceColor: AllianceColor
  lateinit var allianceSide: AllianceSide
  lateinit var allianceColorText: String
  lateinit var allianceSideText: String

  override fun autoSetup() {
    super.autoSetup()

    var leftPressed = false
    var rightPressed = false

    // region * Select Starting Position *
    ButtonGroupController.add(controller1.left_bumper.newlyPressed or controller2.left_bumper.newlyPressed and controller2.share) {
      leftPressed = true
    }
    ButtonGroupController.add(controller1.right_bumper.newlyPressed or controller2.right_bumper.newlyPressed) {
      rightPressed = true
    }

    telemetry.addLine("Please choose ALLIANCE color:")
    telemetry.addLine("\tLEFT BUMPER = BLUE Alliance 🔵")
    telemetry.addLine("\tRIGHT BUMPER = RED Alliance 🔴\n")
    telemetry.update()
    while (true) {
      // Process controls
      ButtonGroupController.processButtonGroups()
      controller1.updateGamepadData()
      controller2.updateGamepadData()

      if (leftPressed) {
        allianceColor = AllianceColor.Blue
        break
      }
      if (rightPressed) {
        allianceColor = AllianceColor.Red
        break
      }
    }
    val allianceColorText = when (allianceColor) {
      AllianceColor.Blue -> "$allianceColor 🔵"
      else -> "$allianceColor 🔴"
    }

    leftPressed = false
    rightPressed = false

    telemetry.addLine("Selected Alliance Color: $allianceColorText")
    telemetry.addLine("Please choose ALLIANCE side:")
    telemetry.addLine("\tLEFT BUMPER = LEFT Side")
    telemetry.addLine("\tRIGHT BUMPER = RIGHT Side\n")
    telemetry.update()
    while (true) {
      // Process controls
      ButtonGroupController.processButtonGroups()
      controller1.updateGamepadData()
      controller2.updateGamepadData()

      if (leftPressed) {
        allianceSide = AllianceSide.Left
        break
      }
      if (rightPressed) {
        allianceSide = AllianceSide.Right
        break
      }
    }
    allianceSideText = when (allianceSide) {
      AllianceSide.Left -> "$allianceSide ⬅️"
      else -> "$allianceSide ➡️"
    }

    ButtonGroupController.clearButtonGroups()
    // endregion * Select Starting Position *
  }

  override fun queueAutoCommands() {
    super.queueAutoCommands()
  }

  override fun readyMessage() {
    telemetry.addLine("Selected Alliance Color: $allianceColorText")
    telemetry.addLine("Selected Alliance Side: $allianceSideText\n")
    super.readyMessage()
  }

  override fun onPressStop() {
    RobotData.robotPosition = Pose(100.0, 100.0, 100.0)
    L.i("Teleop", "Saving robot position: ${RobotData.robotPosition}")
  }
}