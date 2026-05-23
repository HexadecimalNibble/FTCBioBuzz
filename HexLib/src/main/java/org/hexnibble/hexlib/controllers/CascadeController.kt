package org.hexnibble.hexlib.controllers

/**
 * Class for using two PIDF controllers to control something.
 * When the first controller gets within a certain error, it switches over to the second one.
 *
 * @author Benjamin Kang
 */
class CascadeController(coefficients1: PIDCoefficients, coefficient2: PIDCoefficients, val errorSwitchThreshold: Double) {
  val controller1 = PIDController(coefficients1)
  val controller2 = PIDController(coefficient2)

  var controller1Active = true

  fun run() {
    if (controller1Active) {
      controller1.run()
      if (controller1.error < errorSwitchThreshold) {
        controller1Active = false
        resetErrors()
      }
    } else {
      controller2.run()
      if (controller2.error > errorSwitchThreshold) {
        controller1Active = true
        resetErrors()
      }
    }
  }

  fun updatePosition(position: Double) {
    if (controller1Active) {
      controller1
    } else {
      controller2
    }.updatePosition(position)
  }

  fun updateError(error: Double) {
    if (controller1Active) {
      controller1
    } else {
      controller2
    }.updateError(error)
  }

  fun reset() {
    controller1.reset()
    controller2.reset()
  }

  fun resetErrors() {
    controller1.resetErrors()
    controller2.resetErrors()
  }

  fun setTargetPosition(targetPosition: Double, resetPIDErrors: Boolean = true) {
    controller1.setTargetPosition(targetPosition, resetPIDErrors)
    controller2.setTargetPosition(targetPosition, resetPIDErrors)
  }
}