package org.firstinspires.ftc.teamcode.opmodes

import com.pedropathing.api.Paths
import com.pedropathing.api.PoseFactory
import com.pedropathing.follower.Follower
import com.pedropathing.ivy.Command
import com.pedropathing.ivy.commands.Commands.infinite
import com.pedropathing.ivy.commands.Commands.instant
import com.pedropathing.ivy.commands.Commands.waitMs
import com.pedropathing.ivy.groups.Groups.parallel
import com.pedropathing.ivy.groups.Groups.race
import com.pedropathing.ivy.groups.Groups.sequential
import com.pedropathing.ivy.pedro.PedroCommands.follow
import com.pedropathing.paths.Path
import com.pedropathing.paths.interpolator.Interpolator
import dev.nextftc.units.degrees
import dev.nextftc.units.milliseconds
import dev.nextftc.units.rpm
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.BioBuzzConstants
import org.firstinspires.ftc.teamcode.RobotData
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot
import org.firstinspires.ftc.teamcode.withTimeout

class HiveDownAuto(
  val follower: Follower,
  allianceColor: AllianceColor,
  val robot: BiobuzzRobot,
) : AutoProgram {

  private val poseFactory: PoseFactory =
    if (allianceColor == AllianceColor.Blue) {
      PoseFactory.degrees().mirrorAroundPoint(70.75, 70.75)
    } else {
      PoseFactory.degrees()
    }

  private val start = poseFactory.of(62.0, 141.5 - BioBuzzConstants.robotWidth / 2, 180.0)
  private val flowerInitialControl = poseFactory.of(54.7886, 99.8731, 0.0)
  private val flowerInitial = poseFactory.of(47.0, 126.0, 90.0)
  private val flowerFinal = poseFactory.of(47.0, 130.0, 90.0)

  fun initialFlowerApproach(): Path = Paths.curve(start, flowerInitialControl, flowerInitial).linear(start, flowerInitial)
  fun finalFlowerApproach(): Path = Paths.line(flowerInitial, flowerFinal).linear(flowerInitial, flowerFinal)

  private val park = poseFactory.of(8.0, 115.0, 0.0)
  private val parkControl = poseFactory.of(43.0, 114.0, 0.0)


  fun park(): Path = Paths.curve(flowerFinal, parkControl, park)
    .heading(
      Interpolator.tangent.reverse()
    )

  init {
    // Set starting pose
    println("Setting pedro starting pose: $start")
    follower.setPose(start)
  }

  override fun createAuto(): Command {
    return race(
      sequential(
        instant {
          println("new current turret angle ${robot.shooter.totalTurretAngle}")
          robot.shooter.setTurretAngle(170.0.degrees)
          robot.shooter.setShooterVelocity(2450.0.rpm)
        },
        waitMs(1000.0),
        robot.autoShoot(),
        instant { robot.stopAutoShoot() },
        follow(follower, initialFlowerApproach()).withTimeout(2000.0.milliseconds),

        race(
          robot.autoIntake(),
          sequential(
            instant { robot.intake.extendFlowerIntake() },
            waitMs(500.0), // wait for flower intake to extend
            follow(follower, finalFlowerApproach()).withTimeout(1000.0.milliseconds),
            waitMs(2000.0), // wait for balls to go into intake
          )
        ),
        instant {
          robot.intake.stopIntake()
          robot.transfer.stopTransfer()
        },
        robot.autoShoot().withTimeout(1000.0.milliseconds),
        instant {
          robot.stopAutoShoot()
          robot.intake.retractFlowerIntake()
        },
        follow(follower, park()).withTimeout(5000.0.milliseconds),

        instant {
          stopFollower()
          println("Autonomous drivetrain stopped")
        }
      ),
      infinite {
        follower.update()
        RobotData.pose = follower.pose()
      }
    )
  }

  private fun stopFollower() {
    follower.stop()
    follower.update()

    RobotData.pose = follower.pose()
  }
}