package org.firstinspires.ftc.teamcode.opmodes

import NewCommandGamepad
import com.pedropathing.follower.ManualDrive
import com.pedropathing.ivy.commands.Commands.instant
import dev.nextftc.robot.opmode.BulkReadHook
import dev.nextftc.robot.opmode.NextOpMode
import dev.nextftc.robot.opmode.NextTeleop
import dev.nextftc.units.RotationsPerMinute
import dev.nextftc.units.degrees
import dev.nextftc.units.rpm
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.RobotData
import org.firstinspires.ftc.teamcode.pedro.PedroConstants
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot
import org.firstinspires.ftc.teamcode.robot.HivePosition

@NextTeleop(name = "Biobuzz Teleop")
class Teleop(val robot: BiobuzzRobot) : NextOpMode(robot, BulkReadHook), HexOpMode {
  lateinit var driverController: NewCommandGamepad
  lateinit var shooterController: NewCommandGamepad
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
    // Start = options
    // Back = share

    // Reset telemetry display format
    telemetry.setDisplayFormat(Telemetry.DisplayFormat.CLASSIC)

    // Create teleop controller
    driverController = NewCommandGamepad(gamepad1)
    shooterController = NewCommandGamepad(gamepad2)

    // Bind teleop controls
    // region * Driver *
    driverController.cross.and(driverController.back.negate().and(driverController.start.negate())).onTrue(robot.autoIntake())
    driverController.cross.and(driverController.back).onTrue(robot.intake.reverseIntake())

    driverController.square.onTrue(robot.intake.stopIntake())

    driverController.square.and(driverController.start).onTrue(
      instant {
        if (RobotData.allianceColor == AllianceColor.Red) {
          follower.setHeading(0.0)
        } else {
          follower.setHeading(Math.PI)
        }
      }
    )

    driverController.dpadUp.onTrue(instant {
      robot.shooter.setShooterVelocity(3500.0.rpm)
    })

    driverController.dpadDown.onTrue(instant {
      robot.shooter.setShooterVelocity(0.0.rpm)
    })
    // endregion * Driver *
    // region * Shooter *
    shooterController.cross.and(driverController.share.negate().and(driverController.options.negate())).onTrue(robot.autoShoot())

    shooterController.options.and(shooterController.leftTrigger.isOver(0.2)).onTrue(instant {
      robot.shooter.resetTurretAngle()
    })

    shooterController.ps.and(shooterController.leftTrigger.isOver(0.2)).onTrue(instant {
      robot.shooter.setTurretAngle((-90.0).degrees)
    })

    shooterController.ps.and(shooterController.rightTrigger.isOver(0.2)).onTrue(instant {
      robot.shooter.setTurretAngle(90.0.degrees)
    })

    shooterController.touchpad.onTrue(instant {
      robot.shooter.toggleHivePosition()
    })

    shooterController.triangle.onTrue(instant {
      robot.shooter.autoAlign = true
    })

    shooterController.square.onTrue(instant {
      robot.shooter.autoAlign = false
    })
    // endregion * Shooter *

    // Initialize shooter subsystem
    robot.shooter.initSystem()

    when (RobotData.allianceColor) {
      AllianceColor.Red -> robot.shooter.hivePosition = HivePosition.RedAudience
      AllianceColor.Blue -> robot.shooter.hivePosition = HivePosition.BlueNonAudience
      else -> {}
    }
    robot.shooter.poseSupplier = { follower.pose() }
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
    telemetry.addData("Shooter rpm: ", robot.shooter.shooterMotor1.encoderVelocity.into(RotationsPerMinute))
  }
}