package org.firstinspires.ftc.teamcode

import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot
import org.hexnibble.hexlib.BaseRobot

val TeleOp = Mercurial.teleop {
  val robot: BaseRobot = BiobuzzRobot(hardwareMap)

  schedule(
    sequence(
      // Wait for Start to be pressed
      wait { inLoop },

    )
  )
}