package org.firstinspires.ftc.teamcode.robot

import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.pedroPathing.PedroConstants
import org.hexnibble.hexlib.BaseRobot

class BiobuzzRobot(val hwMap: HardwareMap) : BaseRobot(hwMap) {
  val intake: Intake = Intake()
  override fun createFollower() {
    follower = PedroConstants.createFollower(hwMap)
  }
}