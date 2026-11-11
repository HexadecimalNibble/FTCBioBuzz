package org.firstinspires.ftc.teamcode.robot
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.robot.Mechanism

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