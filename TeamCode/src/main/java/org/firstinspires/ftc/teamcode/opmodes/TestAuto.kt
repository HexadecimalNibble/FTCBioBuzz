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