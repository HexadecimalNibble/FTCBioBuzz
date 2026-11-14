package org.firstinspires.ftc.teamcode.robot
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.actuators.NextRGBIndicator
import dev.nextftc.robot.Mechanism
import dev.nextftc.units.amperes
import kotlin.math.abs
import kotlin.math.floor

class Transfer : Mechanism {
  private val transferMotor = NextMotor("TransferMotor")

  private var lastCounterTicks = 0.0
  private val degPerTick = 360.0 / 8192
  private val counterDegThreshold = 80

  private val counter = NextMotor("LFMotor")

  private val backLED = NextRGBIndicator("BackLED")

  var transferMotorRunning = false
    private set

  init {
    transferMotor.currentAlert = 6.0.amperes
  }

  // TODO: Prob can consolidate startTransfer() and reverseTransfer()
  fun startTransfer(power: Double = 1.0) {
    transferMotor.throttle = power
    transferMotorRunning = true
  }

  fun reverseTransfer(power: Double = -1.0) {
    transferMotor.throttle = power
    transferMotorRunning = true
  }

  fun stopTransfer() {
    transferMotor.throttle = 0.0
    transferMotorRunning = false
  }

  fun transferMotorOverCurrent(): Boolean {
    println("Checking transfer motor over current. Is: ${transferMotor.isOverCurrent}")
    return transferMotor.isOverCurrent
  }

  // region counter
  // For some reason counter ticks are reversed
  fun getCounterTicks() = counter.encoderPosition.baseUnitMagnitude

  fun printCounterTicks() {
    println("Counter ticks: ${getCounterTicks()}")
  }

  fun resetCounter() {
    println("Resetting counter")
    lastCounterTicks = getCounterTicks()
  }

  fun getNumBallsCounted(): Int {
    printCounterTicks()
    val totalDegrees = abs(getCounterTicks() - lastCounterTicks) * degPerTick
    var rotations = floor(totalDegrees / 120).toInt()
    println("initial rotations: $rotations")
    val remainder = (totalDegrees % 120).toInt()
    // Threshold to count rotation
    if (remainder > counterDegThreshold) {
      println("remainder: $remainder")
      rotations += 1
    }
    println("final rotations: $rotations")
    return rotations
  }

  fun setTransferLEDColor(color: NextRGBIndicator.Color) = backLED.setColor(color)

  override fun periodic() {
    println("Transfer motor current: ${transferMotor.current}; overcurrent: ${transferMotor.isOverCurrent}; current alert: ${transferMotor.currentAlert}")
  }
}