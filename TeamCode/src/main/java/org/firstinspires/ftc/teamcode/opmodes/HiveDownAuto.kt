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

  private val start = poseFactory.of(30.0, 133.0, -90.0)
  private val park = poseFactory.of(10.0, 95.0, -90.0)


  fun park(): Path = Paths.line(start, park)
    .heading(
      Interpolator.constant(start)
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
          robot.shooter.setTurretAngle((-30.0).degrees)
          robot.shooter.setShooterVelocity(2450.0.rpm)
        },
        waitMs(5000.0),
        robot.autoShoot(),
        instant { robot.stopAutoShoot() },
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