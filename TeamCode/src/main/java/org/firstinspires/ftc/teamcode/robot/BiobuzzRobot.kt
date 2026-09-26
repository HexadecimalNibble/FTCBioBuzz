package org.firstinspires.ftc.teamcode.robot

import dev.nextftc.robot.NextRobot

class BiobuzzRobot : NextRobot {
  val intake: Intake = Intake()

  override val mechanisms = setOf(intake)
}