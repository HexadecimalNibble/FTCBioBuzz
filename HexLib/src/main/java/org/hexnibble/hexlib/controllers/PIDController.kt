package org.hexnibble.hexlib.controllers

import org.hexnibble.hexlib.L
import kotlin.math.abs
import kotlin.math.pow

/**
 * PID Controller
 */
class PIDController(val coefficients: PIDCoefficients) {
  private val logTag = "PIDController"

  var previousError = 0.0
    private set
  var error = 0.0
    private set

  private var position = 0.0
  var targetPosition = 0.0

  var errorIntegral = 0.0
    private set
  var errorDerivative = 0.0
    private set
  var feedForwardInput = 0.0

  private var previousUpdateTimeNano = System.nanoTime()
  private var deltaTime: Double = 0.0

  var atTargetCounter = 0
    private set

  /**
   * Threshold for how many consecutive loops the error has to be less than targetTolerance to be atTargetPosition.
   * NOTE: This is only used if targetTolerance != 0.0.
   */
  var targetCounterThreshold = 2
    private set

  /**
   * Setter for targetCounterThreshold to use in a builder.
   * @see targetCounterThreshold
   */
  fun setTargetCounterThreshold(targetCounterThreshold: Int): PIDController {
    this.targetCounterThreshold = targetCounterThreshold
    return this
  }

  /**
   * Whether the feedforward value should be returned if at the target position.
   * If this variable is false, 0.0 will be returned when at target position instead.
   * For example, if a PIDController is used for a shooter motor, it might be helpful for the feedforward value to be returned when at the target position.
   * In contrast, for something like a turret movement, it would be more helpful to return a value of 0 to stop movement completely.
   */
  var shouldReturnFeedforwardWhenAtTarget = false
    private set

  /**
   * Setter for shouldReturnFeedforwardWhenAtTarget to use in a builder.
   * @see shouldReturnFeedforwardWhenAtTarget
   */
  fun setShouldReturnFeedforwardWhenAtTarget(shouldReturnFeedforwardWhenAtTarget: Boolean): PIDController {
    this.shouldReturnFeedforwardWhenAtTarget = shouldReturnFeedforwardWhenAtTarget
    return this
  }

  /**
   * Function to return whether PID at target position
   * Will always return false if targetTolerance = 0.0
   */
  fun atTargetPosition(): Boolean = atTargetCounter > targetCounterThreshold

  /**
   * This takes the current error and runs the PID on it.
   * Call this function to run the PID!
   * @return this returns the value of the PID from the current error.
   */
  fun run(): Double {
    // If the target tolerance was changed, we are using it so update it
    if (coefficients.tolerance != 0.0) {
      // If the current error is within the threshold, increase the counter
      if (abs(error) <= coefficients.tolerance) {
        L.d(logTag, "At target position. Increasing counter.")
        atTargetCounter++
      } else {
        L.d(logTag, "Not at target position. Resetting counter.")
        atTargetCounter = 0
      }

      if (atTargetPosition()) {
        L.d(logTag, "PID Controller at target position.")
        return if (shouldReturnFeedforwardWhenAtTarget) {
          feedForwardInput * coefficients.kF
        } else 0.0
      }
    }
    return coefficients.kS + error * coefficients.kP + errorDerivative * coefficients.kD +
        errorIntegral * coefficients.kI + feedForwardInput * coefficients.kF
  }

  /**
   * This can be used to update the PIDF's current position when inputting a current position and
   * a target position to calculate error. This will update the error from the current position to
   * the target position specified.
   *
   * @param position This is the current position.
   */
  fun updatePosition(position: Double) {
    this.position = position
    previousError = error
    error = targetPosition - this.position

    deltaTime = (System.nanoTime() - previousUpdateTimeNano) / 10.0.pow(9.0)
    previousUpdateTimeNano = System.nanoTime()

    errorIntegral += error * deltaTime
    errorDerivative = (error - previousError) / deltaTime
  }

  /**
   * As opposed to updating position against a target position, this just sets the error to some
   * specified value.
   *
   * @param error The error specified.
   */
  fun updateError(error: Double) {
    previousError = this.error
    this.error = error
    val nanoTime = System.nanoTime()

    deltaTime = (System.nanoTime() - previousUpdateTimeNano) / 10.0.pow(9.0)
    previousUpdateTimeNano = nanoTime

    errorIntegral += error * deltaTime
    errorDerivative = (error - previousError) / deltaTime
  }

  /**
   * This resets all the PID's error and position values, as well as the time stamps.
   */
  fun reset() {
    previousError = 0.0
    error = 0.0
    position = 0.0
    targetPosition = 0.0
    errorIntegral = 0.0
    errorDerivative = 0.0
    previousUpdateTimeNano = System.nanoTime()
  }

  /**
   * This resets all the PID's errors as well as the time stamps.
   */
  fun resetErrors() {
    previousError = 0.0
    error = 0.0
    errorIntegral = 0.0
    errorDerivative = 0.0
    previousUpdateTimeNano = System.nanoTime()
  }

  fun setTargetPosition(targetPosition: Double, resetPIDErrors: Boolean = true) {
    this.targetPosition = targetPosition
    if (resetPIDErrors) {
      reset()
    }
  }
}

class PIDCoefficients(
  var kS: Double = 0.0,
  var kP: Double = 0.0,
  var kI: Double = 0.0,
  var kD: Double = 0.0,
  var kF: Double = 0.0,
  /**
   * Tolerance for PID controller to be considered at target.
   * The value that the error has to be less than for targetCounterThreshold consecutive loops to be atTargetPosition.
   * Set this to something other than 0.0 to activate using the target tolerance.
   */
  var tolerance: Double = 0.0,
)