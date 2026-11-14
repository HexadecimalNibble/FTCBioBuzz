package org.firstinspires.ftc.teamcode.opmodes

import com.pedropathing.api.Paths
import com.pedropathing.api.PoseFactory
import com.pedropathing.follower.Follower
import com.pedropathing.ivy.Command
import com.pedropathing.ivy.commands.Commands.infinite
import com.pedropathing.ivy.commands.Commands.instant
import com.pedropathing.ivy.commands.Commands.waitMs
import com.pedropathing.ivy.commands.Commands.waitUntil
import com.pedropathing.ivy.groups.Groups.parallel
import com.pedropathing.ivy.groups.Groups.sequential
import com.pedropathing.ivy.pedro.PedroCommands.follow
import com.pedropathing.paths.Path
import com.pedropathing.paths.interpolator.Interpolator
import dev.nextftc.units.degrees
import dev.nextftc.units.milliseconds
import dev.nextftc.units.rpm
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.BioBuzzConstants
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot
import org.firstinspires.ftc.teamcode.withTimeout

class HiveUpAuto(
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
  private val gardenEndCoord = poseFactory.of(15.0, BioBuzzConstants.robotWidth / 2, 180.0)
  private val nonAudienceSideFlowerCoord = poseFactory.of(47.0, 122.0, 90.0)
  private val point2Control1 = poseFactory.of(33.0, 102.0, 0.0)
  private val humanPlayerFlowerCoord = poseFactory.of(10.7052, 46.715, 180.0)
  private val point3Control1 = poseFactory.of(24.0106, 92.1561, 0.0)
  private val point3Control2 = poseFactory.of(41.0673, 46.5558, 0.0)
  private val point3Segment1Heading = poseFactory.of(10.7052, 46.715, 90.0)

  fun driveToGarden(): Path = Paths.line(
    start,
    gardenEndCoord
  ).linear(start, gardenEndCoord)

  fun driveToNonAudienceSideFlower(): Path = Paths.curve(
    gardenEndCoord,
    point2Control1,
    nonAudienceSideFlowerCoord
  ).heading(
    Interpolator.tangent
  )

  fun driveToHumanPlayerFlower(): Path = Paths.curve(
    nonAudienceSideFlowerCoord,
    point3Control1,
    point3Control2,
    humanPlayerFlowerCoord
  ).heading(
    Interpolator.piecewise()
      .until(0.05, Interpolator.constant(point3Segment1Heading))
      .until(1.0, Interpolator.tangent)
  )

  init {
    // Set starting pose
    println("Setting pedro starting pose: $start")
    follower.setPose(start)
  }

  override fun createAuto(): Command {
    robot.shooter.setTurretAngle(125.0.degrees)

    return parallel(
      sequential(
        instant {
          robot.shooter.setShooterVelocity(3300.0.rpm)
        },
        waitMs(2000.0),
        robot.autoShoot(),
        parallel(
          follow(follower, driveToGarden()),
          sequential(
            waitUntil { follower.parametricCompletion() > 0.2 },
            robot.autoIntake().withTimeout(1000.0.milliseconds),
          ),
        ),
        follow(follower, driveToNonAudienceSideFlower()),
//        follow(follower, driveToHumanPlayerFlower())
      ),
      infinite { follower.update() }
    )
  }
}