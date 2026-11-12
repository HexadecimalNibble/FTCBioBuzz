package org.firstinspires.ftc.teamcode.opmodes

import com.pedropathing.api.Paths
import com.pedropathing.api.PoseFactory
import com.pedropathing.follower.Follower
import com.pedropathing.ivy.Command
import com.pedropathing.ivy.commands.Commands.infinite
import com.pedropathing.ivy.commands.Commands.instant
import com.pedropathing.ivy.commands.Commands.waitMs
import com.pedropathing.ivy.groups.Groups.parallel
import com.pedropathing.ivy.groups.Groups.sequential
import com.pedropathing.paths.Path
import com.pedropathing.paths.interpolator.Interpolator
import dev.nextftc.robot.NextRobot
import dev.nextftc.robot.coroutine.CoroutineCommandBuilder
import dev.nextftc.robot.coroutine.command
import dev.nextftc.units.rpm
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.BioBuzzConstants
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot

class TestAuto(val follower: Follower, val allianceColor: AllianceColor, val robot: BiobuzzRobot) : AutoProgram {

  private val poseFactory = PoseFactory.degrees()

  private val start = poseFactory.of(56.0, 8.0, 180.0)
  private val path1 = poseFactory.of(10.3739, 8.2688, 180.0)
  private val point2 = poseFactory.of(47.0886, 128.0232, 90.4528)
  private val point2Control1 = poseFactory.of(27.2208, 106.4198, 0.0)
  private val point2Control2 = poseFactory.of(47.2667, 105.4864, 0.0)
  private val point2Segment1Heading = poseFactory.of(47.0886, 128.0232, 180.0)
  private val point3 = poseFactory.of(10.7052, 46.715, 179.6996)
  private val point3Control1 = poseFactory.of(24.0106, 92.1561, 0.0)
  private val point3Control2 = poseFactory.of(41.0673, 46.5558, 0.0)
  private val point3Segment1Heading = poseFactory.of(10.7052, 46.715, 90.0)

  fun path1(): Path = Paths.line(start, path1).linear(start, path1)

  fun path2(): Path = Paths.curve(path1, point2Control1, point2Control2, point2).heading(Interpolator.piecewise().until(0.05, Interpolator.constant(point2Segment1Heading)).until(1.0, Interpolator.tangent))

  fun path3(): Path = Paths.curve(point2, point3Control1, point3Control2, point3).heading(Interpolator.piecewise().until(0.05, Interpolator.constant(point3Segment1Heading)).until(1.0, Interpolator.tangent))






  val p = PoseFactory.degrees()

  // TODO: ADJUST STARTING X COORD
  val startPose = p.of(58.0, BioBuzzConstants.robotWidth / 2, 90.0)
  val gardenPoseStart = startPose.withY(startPose.y() + 4)
  val gardenPose = p.of(BioBuzzConstants.robotLength / 2 + 5, BioBuzzConstants.robotWidth / 2 + 1, 180.0)

  init {
    // Set starting pose
    follower.setPose(startPose)
  }

  fun grabGarden(): Path = Paths.line(gardenPoseStart, gardenPose).heading(
    Interpolator.piecewise()
      .until(0.1, Interpolator.constant(90.0))
      .until(1.0, Interpolator.constant(gardenPose.heading()))
  )

  override fun createAuto() = parallel(
    sequential(
      instant {
        robot.intake.retractFlowerIntake()
        robot.shooter.setShooterVelocity(3300.0.rpm)
      },
      waitMs(2000.0),
      robot.autoShoot(),
      instant { robot.stopAutoShoot() },
      instant { follower.follow(grabGarden()) },
    ),
    infinite { follower.update() }
  )
}