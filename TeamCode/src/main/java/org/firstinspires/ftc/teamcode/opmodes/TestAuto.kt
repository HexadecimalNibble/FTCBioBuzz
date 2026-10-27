package org.firstinspires.ftc.teamcode.opmodes

import com.pedropathing.api.Paths
import com.pedropathing.api.PoseFactory
import com.pedropathing.follower.Follower
import com.pedropathing.ivy.Command
import com.pedropathing.ivy.groups.Groups.sequential
import com.pedropathing.paths.Path
import dev.nextftc.robot.coroutine.CoroutineCommandBuilder
import dev.nextftc.robot.coroutine.command
import org.firstinspires.ftc.teamcode.AllianceColor
import org.firstinspires.ftc.teamcode.BioBuzzConstants

class TestAuto(val follower: Follower, val allianceColor: AllianceColor) : AutoProgram {
  val p = PoseFactory.degrees()

  // TODO: ADJUST STARTING X COORD
  val startPose = p.of(58.0, BioBuzzConstants.robotWidth / 2, 180.0)
  val gardenPose = p.of(BioBuzzConstants.robotLength / 2, BioBuzzConstants.robotWidth / 2 + 1, 180.0)

  init {
    // Set starting pose
    follower.setPose(startPose)
  }

  fun grabGarden(): Path = Paths.line(startPose, gardenPose).linear(startPose, gardenPose)

  override fun createAuto(): Command {
    println("Creating auto")
    return command {
      wait(0.5)
      follower.follow(grabGarden())
      while (!follower.isBusy) {
        follower.update()
        println("in loop")
        yield()
      }
    }
  }
}