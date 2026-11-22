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

class HiveUpAuto(
  val follower: Follower,
  allianceColor: AllianceColor,
  val robot: BiobuzzRobot,
) : AutoProgram {

  private val poseFactory: PoseFactory = if (allianceColor == AllianceColor.Blue) {
    PoseFactory.degrees().mirrorAroundPoint(70.75, 70.75)
  } else {
    PoseFactory.degrees()
  }

  private val start = poseFactory.of(32.0, BioBuzzConstants.robotWidth / 2, 180.0)
  private val gardenEndCoord = poseFactory.of(10.0, BioBuzzConstants.robotWidth / 2, 180.0)
  private val parkControl = poseFactory.of(35.0, 50.0, 0.0)
  private val park = poseFactory.of(13.0, 96.0, 0.0)

  fun driveToGarden(): Path = Paths.line(
    start, gardenEndCoord
  ).linear(start, gardenEndCoord)

  fun park(): Path = Paths.curve(gardenEndCoord, parkControl, park).heading(
      Interpolator.tangent
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
        robot.shooter.setTurretAngle(120.0.degrees)
        robot.shooter.setShooterVelocity(2400.0.rpm)
      }, waitMs(3000.0), robot.autoShoot(), parallel(
        instant {
          robot.shooter.setTurretAngle(135.0.degrees)
          robot.shooter.setShooterVelocity(2500.0.rpm)
        },
        sequential(
          follow(follower, driveToGarden()).withTimeout(2000.0.milliseconds),
          waitMs(500.0),
        ),
        robot.autoIntake().withTimeout(2000.0.milliseconds),
      ), follow(follower, park()),

      instant {
        stopFollower()
        println("Autonomous drivetrain stopped")
      }), infinite {
      follower.update()
      RobotData.pose = follower.pose()
    })
  }

  private fun stopFollower() {
    follower.stop()
    follower.update()

    RobotData.pose = follower.pose()
  }
}