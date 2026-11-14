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

  private val start = poseFactory.of(32.0, BioBuzzConstants.robotWidth / 2, 180.0)
  private val gardenEndCoord = poseFactory.of(10.0, BioBuzzConstants.robotWidth / 2, 180.0)
  private val gardenToInitialFlowerControl = poseFactory.of(29.0, 91.0, 0.0)
  private val nonAudienceInitialSideFlowerCoord = poseFactory.of(47.0, 108.0, 90.0)

  private val nonAudienceFinalSideFlowerCoord = poseFactory.of (47.0, 118.0, 90.0)

//  private val humanPlayerFlowerCoord = poseFactory.of(10.7052, 46.715, 180.0)
//  private val point3Control1 = poseFactory.of(24.0106, 92.1561, 0.0)
//  private val point3Control2 = poseFactory.of(41.0673, 46.5558, 0.0)
//  private val point3Segment1Heading = poseFactory.of(10.7052, 46.715, 90.0)

  private val park = poseFactory.of(8.0, 115.0, 0.0)
  private val parkControl = poseFactory.of(43.0, 114.0, 0.0)


  fun driveToGarden(): Path = Paths.line(
    start,
    gardenEndCoord
  ).linear(start, gardenEndCoord)

  fun driveToNonAudienceSideInitialFlower(): Path =
    Paths.curve(
      gardenEndCoord,
      gardenToInitialFlowerControl,
      nonAudienceInitialSideFlowerCoord
    ).heading(
      Interpolator.linear(
        gardenEndCoord,
        nonAudienceInitialSideFlowerCoord
      )
    )

  fun driveIntoFlower(): Path =
    Paths.line(
      nonAudienceInitialSideFlowerCoord,
      nonAudienceFinalSideFlowerCoord,
    ).heading(
      Interpolator.linear(
        nonAudienceInitialSideFlowerCoord,
        nonAudienceFinalSideFlowerCoord,
      )
    )

  fun park(): Path = Paths.curve(nonAudienceInitialSideFlowerCoord, parkControl, park)
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
          robot.shooter.setTurretAngle(120.0.degrees)
          robot.shooter.setShooterVelocity(3000.0.rpm)
        },
        waitMs(1000.0),
        robot.autoShoot(),
        parallel(
          sequential(
            follow(follower, driveToGarden()).withTimeout(2000.0.milliseconds),
            waitMs(500.0),
          ),
          robot.autoIntake().withTimeout(2000.0.milliseconds),
        ),

        parallel(
          instant {
            robot.intake.stopIntake()
            robot.transfer.stopTransfer()
            robot.shooter.setTurretAngle(175.0.degrees)
          },
          follow(follower, driveToNonAudienceSideInitialFlower()).withTimeout(5000.0.milliseconds),
        ),

        robot.autoShoot().withTimeout(1000.0.milliseconds),
        instant { robot.stopAutoShoot() },

        race(
          robot.autoIntake(),
          sequential(
            instant { robot.intake.extendFlowerIntake() },
            waitMs(500.0), // wait for flower intake to extend
            follow(follower, driveIntoFlower()).withTimeout(1000.0.milliseconds),
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