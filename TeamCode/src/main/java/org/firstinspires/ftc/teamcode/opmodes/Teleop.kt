package org.firstinspires.ftc.teamcode.opmodes

import com.pedropathing.follower.ManualDrive
import com.pedropathing.ivy.commands.Commands.instant
import dev.nextftc.robot.opmode.BulkReadHook
import dev.nextftc.robot.opmode.NextOpMode
import dev.nextftc.robot.opmode.NextTeleop
import dev.nextftc.robot.triggers.CommandGamepad
import dev.nextftc.units.RotationsPerMinute
import dev.nextftc.units.rpm
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.RobotData
import org.firstinspires.ftc.teamcode.pedro.PedroConstants
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot

@NextTeleop(name = "Biobuzz Teleop")
class Teleop(val robot: BiobuzzRobot) : NextOpMode(robot, BulkReadHook), HexOpMode {
  lateinit var driver: CommandGamepad
  lateinit var shooter: CommandGamepad
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
    shooter = CommandGamepad(gamepad2)

    // Bind teleop controls
    driver.cross.and(driver.back.negate().and(driver.start.negate())).onTrue(robot.autoIntake())
    driver.triangle.onTrue(robot.intake.startFullIntakeTesting())
    driver.cross.and(driver.back).onTrue(robot.intake.reverseIntake())

    driver.square.onTrue(robot.intake.stopIntake())

    driver.square.and(driver.start).onTrue(
      instant {
        if (RobotData.allianceColor == AllianceColor.Red) {
          follower.setHeading(0.0)
        } else {
          follower.setHeading(Math.PI)
        }
      }
    )

    shooter.cross.and(driver.back.negate().and(driver.start.negate())).onTrue(robot.autoShoot())

    driver.dpadUp.onTrue(instant {
      robot.shooter.setShooterPower(robot.shooter.shooterMotor1.throttle + 0.05)
    })

    driver.dpadDown.onTrue(instant {
      robot.shooter.setShooterPower(robot.shooter.shooterMotor1.throttle - 0.05)
    })
    5.0.rpm
  }

  override fun periodic() {
    // Driving
    var heading = follower.pose().heading()
    if (RobotData.allianceColor == AllianceColor.Blue) {
      heading = (heading + Math.PI) % (Math.PI * 2)
    }
    val powers = ManualDrive.fieldCentric(
      -gamepad1.left_stick_y.toDouble(), // Forward
      -gamepad1.left_stick_x.toDouble(), // Strafe
      (gamepad1.left_trigger -gamepad1.right_trigger).toDouble(), // Turn
      heading
    )
    follower.manual(powers)
    follower.update()

    telemetry.addData("Robot X", follower.pose().x())
    telemetry.addData("Robot Y", follower.pose().y())
    telemetry.addData("Robot Heading", Math.toDegrees(follower.pose().heading()))
    telemetry.addData("Shooter power: ", robot.shooter.shooterMotor1.throttle)
    telemetry.addData("Shooter rpm: ", robot.shooter.shooterMotor1.encoderVelocity / 28 * 60)
  }
}