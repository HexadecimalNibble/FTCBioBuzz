package org.firstinspires.ftc.teamcode.robot
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.robot.Mechanism
import kotlin.math.abs
import kotlin.math.floor

class Transfer : Mechanism {
  private val transferMotor = NextMotor("TransferMotor")

  private var lastCounterTicks = 0.0
  private val countsForRotation = 1600

  private val counter = NextMotor("LFMotor")

  var numBallsInTransfer = 0
    private set

  fun startTransfer(power: Double = 1.0) {
    transferMotor.throttle = power
  }

  fun reverseTransfer(power: Double = -1.0) {
    transferMotor.throttle = power
  }

  fun stopTransfer() {
    transferMotor.throttle = 0.0
  }

  // region counter
  // For some reason counter ticks are reversed
  fun getCounterTicks() = counter.encoderPosition.baseUnitMagnitude

  fun printCounterTicks() {
    println("Counter ticks: ${getCounterTicks()}")
  }

  fun resetCounter() {
    println("Reseting counter")
    lastCounterTicks = getCounterTicks()
  }

  fun getNumBallsCounted(): Int {
    printCounterTicks()
    val totalDegrees = abs(getCounterTicks() - lastCounterTicks) * 360 / 8192
    var rotations = floor(totalDegrees / 120).toInt()
    println("initial rotations: $rotations")
    val remainder = (totalDegrees % 120).toInt()
    // 80 deg threshold to count rotation
    if (remainder > 80) {
      println("remainder: $remainder")
      rotations += 1
    }
    println("final rotations: $rotations")
    return rotations
  }

  /**
   * Return whether a ball has moved the counter
   * Note: this resets the counter if the ball moved
   */
  fun checkMoveCounterAndReset(): Boolean {
    printCounterTicks()
    if (getCounterTicks() < lastCounterTicks - countsForRotation) {
      resetCounter()
      return true
    }
    return false
  }
}