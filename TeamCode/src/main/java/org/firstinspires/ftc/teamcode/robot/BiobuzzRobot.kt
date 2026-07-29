package org.firstinspires.ftc.teamcode.robot

import dev.nextftc.robot.NextRobot
import org.firstinspires.ftc.teamcode.pedroPathing.PedroConstants

class BiobuzzRobot : NextRobot {
  val intake: Intake = Intake()

  override val mechanisms = setOf(intake)
}