package org.hexnibble.hexlib.hardware

import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.ServoImplEx

class Servo(hwMap: HardwareMap, servoName: String, ) {
  val servo = hwMap.get(ServoImplEx::class.java, servoName)

  fun getLastSetServoPosition() = servo.position

  fun setServoPosition(position: Double) {
    servo.position = position
  }
}