package org.firstinspires.ftc.teamcode.opmodes

import com.pedropathing.follower.ManualDrive
import dev.nextftc.robot.opmode.BulkReadHook
import dev.nextftc.robot.opmode.NextOpMode
import dev.nextftc.robot.opmode.NextTeleop
import dev.nextftc.robot.triggers.CommandGamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.RobotData
import org.firstinspires.ftc.teamcode.pedro.PedroConstants
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot

@NextTeleop(name = "Biobuzz Teleop")
class Teleop(val robot: BiobuzzRobot) : NextOpMode(robot, BulkReadHook), HexOpMode {
  lateinit var driver: CommandGamepad
  val follower = PedroConstants.create(hardwareMap)

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
      setAllianceColor(gamepad1, telemetry)
    } else {
      telemetry.addLine("<big><b>Ready to start</b></big>")
    }
  }

  override fun disabledPeriodic() {
    checkAndSetAllianceColor()
  }

  override fun start() {
    // Reset telemetry display format
    telemetry.setDisplayFormat(Telemetry.DisplayFormat.CLASSIC)

    // Create teleop controller
    driver = CommandGamepad(gamepad1)

    // Bind teleop controls
    driver.cross.onTrue(robot.intake.startAutoIntake())
    driver.triangle.onTrue(robot.intake.startIntake())
    driver.cross.and(driver.start).onTrue(robot.intake.reverseIntake())

    driver.square.onTrue(robot.intake.stopIntake())
  }

  override fun periodic() {
    // Driving
    val powers = ManualDrive.fieldCentric(
      -gamepad1.left_stick_y.toDouble(), // Forward
      gamepad1.left_stick_x.toDouble(), // Strafe
      (gamepad1.right_trigger - gamepad1.left_trigger).toDouble(), // Turn
      follower.pose().heading()
    )
    follower.manual(powers)
    follower.update()

    telemetry.addData("Robot X", follower.pose().x())
    telemetry.addData("Robot Y", follower.pose().y())
    telemetry.addData("Robot Heading", Math.toDegrees(follower.pose().heading()))
  }
}