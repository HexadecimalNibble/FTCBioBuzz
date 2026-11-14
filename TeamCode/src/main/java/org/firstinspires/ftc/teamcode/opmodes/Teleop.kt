package org.firstinspires.ftc.teamcode.opmodes

import NewCommandGamepad
import com.pedropathing.follower.ManualDrive
import com.pedropathing.ivy.commands.Commands.instant
import com.pedropathing.math.Pose
import dev.nextftc.robot.opmode.BulkReadHook
import dev.nextftc.robot.opmode.NextOpMode
import dev.nextftc.robot.opmode.NextTeleop
import dev.nextftc.units.RotationsPerMinute
import dev.nextftc.units.degrees
import dev.nextftc.units.rpm
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.BioBuzzConstants
import org.firstinspires.ftc.teamcode.RobotData
import org.firstinspires.ftc.teamcode.pedro.PedroConstants
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot
import org.firstinspires.ftc.teamcode.robot.HivePosition

@NextTeleop(name = "Biobuzz Teleop")
class Teleop(val robot: BiobuzzRobot) : NextOpMode(robot, BulkReadHook), HexOpMode {
  lateinit var driverController: NewCommandGamepad
  lateinit var shooterController: NewCommandGamepad
  val follower = PedroConstants.create(hardwareMap)

  private var manualTurretWasActive = false

  init {
    // Set telemetry display format to HTML for cooler setup menu
    telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML)
  }

  fun checkAndSetAllianceColor() {
    // If options pressed at any time, reset alliance info
    if (gamepad1.options) {
      RobotData.allianceColor = null
    }

    if (RobotData.pose != null) {
      println("Setting saved pose: ${RobotData.pose}")
      follower.setPose(RobotData.pose)
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
    robot.intake.retractFlowerIntake()

    // Reset telemetry display format
    telemetry.setDisplayFormat(Telemetry.DisplayFormat.CLASSIC)

    // Create teleop controller
    driverController = NewCommandGamepad(gamepad1)
    shooterController = NewCommandGamepad(gamepad2)

    // Bind teleop controls
    // region * Driver *
    driverController.cross.and(driverController.share.negate().and(driverController.options.negate())).onTrue(robot.autoIntake())
    driverController.cross.and(driverController.share).onTrue(instant {
      robot.intake.reverseIntake()
      robot.transfer.reverseTransfer()
    })

    driverController.square.and(driverController.options.negate()).onTrue(instant {
      robot.intake.stopIntake()
      robot.transfer.stopTransfer()
    }.requiring(robot.intake, robot.transfer).setPriority(999))

    // reset imu
    driverController.square.and(driverController.options).onTrue(
      instant {
        if (RobotData.allianceColor == AllianceColor.Red) {
          follower.setHeading(0.0)
        } else {
          follower.setHeading(Math.PI)
        }
      }
    )


    driverController.dpadUp.and(driverController.ps).onTrue(instant {
      // TODO
    })

    // Reset in bottom-left corner w/ robot facing away
    driverController.dpadDown.and(driverController.ps).onTrue(instant {
      if (RobotData.allianceColor == AllianceColor.Red) {
        follower.setPose(Pose(BioBuzzConstants.robotLength / 2, 140.0 - BioBuzzConstants.robotWidth / 2, 0.0))
      } else {
        follower.setPose(Pose(140.0 - BioBuzzConstants.robotLength / 2, BioBuzzConstants.robotWidth / 2, Math.PI))
      }
    })

    driverController.dpadUp.and(driverController.ps.negate()).onTrue(instant { robot.intake.retractFlowerIntake() })

    driverController.dpadDown.and(driverController.ps.negate()).onTrue(instant {
      robot.intake.extendFlowerIntake()
      robot.intake.startIntake()
    })

    // endregion * Driver *
    // region * Shooter *
    shooterController.cross.and(driverController.share.negate().and(driverController.options.negate())).onTrue(robot.autoShoot())

    // region Turret
    shooterController.options.and(shooterController.leftTrigger.isOver(0.2)).onTrue(instant {
      robot.shooter.resetTurretAngle()
    })

    shooterController.share.and(shooterController.rightTrigger.isOver(0.2)).onTrue(instant {
      robot.shooter.resetTurretAngle()
    })

    shooterController.ps.and(shooterController.leftTrigger.isOver(0.2)).and(shooterController.options.negate()).onTrue(instant {
      robot.shooter.setTurretAngle((-90.0).degrees)
    })

    shooterController.ps.and(shooterController.rightTrigger.isOver(0.2)).and(shooterController.options.negate()).onTrue(instant {
      robot.shooter.setTurretAngle(90.0.degrees)
    })
    // endregion

    shooterController.touchpad.onTrue(instant {
      robot.shooter.toggleHivePosition()
    })

    shooterController.triangle.and(shooterController.share.negate()).onTrue(instant {
      robot.shooter.autoAlign = true
    })

    shooterController.square.onTrue(instant {
      robot.shooter.autoAlign = false
      robot.stopAutoShoot()
    })

    shooterController.circle.onTrue(instant {
      robot.shooter.stopShooter()
    })

    shooterController.rightStickY.isUnder(-0.2).and(shooterController.share.negate()).onTrue(instant {
      robot.shooter.engageBlocker()
    })

    shooterController.rightStickY.isOver(0.2).and(shooterController.share.negate()).onTrue(instant {
      robot.shooter.disengageBlocker()
    })

    shooterController.rightStickY.isUnder(-0.2).and(shooterController.share).onTrue(instant {
      robot.shooter.decrementBlockerEngaged()
    })

    shooterController.rightStickY.isOver(0.2).and(shooterController.share).onTrue(instant {
      robot.shooter.incrementBlockerEngaged()
    })

    shooterController.dpadUp.onTrue(instant {
      robot.shooter.changeShooterVelocity(100.0.rpm)
    })
    shooterController.dpadDown.onTrue(instant {
      robot.shooter.changeShooterVelocity((-100.0).rpm)
    })

    shooterController.leftBumper.onTrue(instant {
      robot.shooter.offsetTurretAlignmentLeft()
    })
    shooterController.rightBumper.onTrue(instant {
      robot.shooter.offsetTurretAlignmentRight()
    })

    // Fail-safe shooter position
    shooterController.triangle.and(shooterController.share).onTrue(instant {
      robot.shooter.autoAlign = false
      robot.shooter.setTurretAngle(0.0.degrees)
      robot.shooter.setShooterVelocity(2300.0.rpm)
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

//    // Move hood to default position
//    robot.shooter.setHoodPosition()
  }

  override fun periodic() {
    // Manual turret control
    val rightTrigger = shooterController.rightTrigger.value.coerceIn(0.0, 1.0)
    val leftTrigger = shooterController.leftTrigger.value.coerceIn(0.0, 1.0)

    val turretInput = rightTrigger - leftTrigger

    val manualTurretActive = !gamepad2.ps && !gamepad2.options && (rightTrigger > 0.02 || leftTrigger > 0.02)

    if (manualTurretActive) {
      // Deadband to prevent accidental movement
      val deadband = 0.1

      // Rescale input after deadband
      val adjustedInput = when {
        turretInput > deadband -> (turretInput - deadband) / (1.0 - deadband)
        turretInput < -deadband -> (turretInput + deadband) / (1.0 - deadband)
        else -> 0.0
      }

      // Limit maximum manual power
      val turretPower = adjustedInput * 0.5

      robot.shooter.setTurretPower(turretPower)

    } else if (manualTurretWasActive) {
      // Stop turret when triggers are released
      robot.shooter.setTurretPower(0.0)
    }

    manualTurretWasActive = manualTurretActive

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