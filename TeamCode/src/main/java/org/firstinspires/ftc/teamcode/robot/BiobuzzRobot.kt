package org.firstinspires.ftc.teamcode.robot

import dev.nextftc.robot.NextRobot

class BiobuzzRobot : NextRobot {
  val intake: IntakeTransfer = IntakeTransfer()

  override val mechanisms = setOf(intake)
}