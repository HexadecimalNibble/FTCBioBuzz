package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.pedroPathing.PedroConstants
import org.hexnibble.hexlib.BaseRobot

class BiobuzzRobot(val hwMap: HardwareMap) : BaseRobot(hwMap) {
  override fun getFollower() {
    follower = PedroConstants.createFollower(hwMap)
  }
}