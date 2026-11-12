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
import dev.nextftc.units.rpm
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot

class AudienceAuto(val follower: Follower, val allianceColor: AllianceColor, val robot: BiobuzzRobot) : AutoProgram {

  private val poseFactory = PoseFactory.degrees()

  private val start = poseFactory.of(56.0, 8.0, 180.0)
  private val gardenEndCoord = poseFactory.of(10.3739, 8.2688, 180.0)
  private val point2 = poseFactory.of(47.0886, 128.0232, 90.4528)
  private val point2Control1 = poseFactory.of(27.2208, 106.4198, 0.0)
  private val point2Control2 = poseFactory.of(47.2667, 105.4864, 0.0)
  private val point2Segment1Heading = poseFactory.of(47.0886, 128.0232, 180.0)
  private val point3 = poseFactory.of(10.7052, 46.715, 179.6996)
  private val point3Control1 = poseFactory.of(24.0106, 92.1561, 0.0)
  private val point3Control2 = poseFactory.of(41.0673, 46.5558, 0.0)
  private val point3Segment1Heading = poseFactory.of(10.7052, 46.715, 90.0)

  fun driveToGarden(): Path = Paths.line(start, gardenEndCoord).linear(start, gardenEndCoord)

  fun path2(): Path = Paths.curve(gardenEndCoord, point2Control1, point2Control2, point2).heading(Interpolator.piecewise().until(0.05, Interpolator.constant(point2Segment1Heading)).until(1.0, Interpolator.tangent))

  fun path3(): Path = Paths.curve(point2, point3Control1, point3Control2, point3).heading(Interpolator.piecewise().until(0.05, Interpolator.constant(point3Segment1Heading)).until(1.0, Interpolator.tangent))

  init {
    if (allianceColor == AllianceColor.Blue) {
      poseFactory.mirrorX(70.75)
    }

    // Set starting pose
    println("Setting pedro starting pose: $start")
    follower.setPose(start)
  }

  override fun createAuto(): Command {
    return parallel(
      sequential(
        instant {
          robot.shooter.setShooterVelocity(3300.0.rpm)
        },
        waitMs(2000.0),
        robot.autoShoot(),
        parallel(
          follow(follower, driveToGarden()),
          waitUntil { follower.parametricCompletion() > 0.2 }.then(instant { robot.intake.extendFlowerIntake() }),
        )
      ),
      infinite { follower.update() }
    )
  }
}