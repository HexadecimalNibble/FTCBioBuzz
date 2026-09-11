package org.firstinspires.ftc.teamcode.opmodes

import com.pedropathing.api.PoseFactory
import com.pedropathing.follower.Follower
import com.pedropathing.ivy.Command
import com.pedropathing.ivy.groups.Groups.sequential
import org.firstinspires.ftc.teamcode.AllianceColor

class TestAuto(val follower: Follower, val allianceColor: AllianceColor) : AutoProgram {
  val p = PoseFactory.degrees()

  init {
    follower.setPose(p.of(0.0, 0.0, 90.0))
  }
  override fun createAuto(): Command = sequential(
  )
}