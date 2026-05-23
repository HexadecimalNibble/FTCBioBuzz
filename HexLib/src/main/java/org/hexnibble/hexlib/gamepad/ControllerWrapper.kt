package org.hexnibble.hexlib.gamepad

import com.qualcomm.robotcore.hardware.Gamepad

/**
 * Functional interface to allow custom conditions like: button1 and button2 or !button3
 */
fun interface ButtonCondition {
  operator fun invoke(): Boolean
  infix fun and(other: ButtonCondition) = ButtonCondition { this.invoke() && other.invoke() }
  infix fun or(other: ButtonCondition) = ButtonCondition { this.invoke() || other.invoke() }
  operator fun not() = ButtonCondition { !this.invoke() }
}

private class PressedAction(
    @JvmField val condition: ButtonCondition,
    @JvmField val action: () -> Unit,
)

enum class ButtonState {
  Pressed,
  Released,
  NewlyPressed,
  ContinuedPressed,
  NewlyReleased,
  ContinuedReleased,
}

class ControllerWrapper(val gamepad: Gamepad) {
  inner class Button(
    private val buttonState: (Gamepad) -> Boolean
  ) : ButtonCondition {
    override fun invoke(): Boolean = buttonState(currentGamepad)

    val pressed: ButtonCondition
      get() = ButtonCondition { stateMatchesButton(buttonState(prevGamepad), buttonState(currentGamepad), ButtonState.Pressed) }
    val newlyPressed: ButtonCondition
      get() = ButtonCondition { stateMatchesButton(buttonState(prevGamepad), buttonState(currentGamepad), ButtonState.NewlyPressed) }
    val newlyReleased: ButtonCondition
      get() = ButtonCondition { stateMatchesButton(buttonState(prevGamepad), buttonState(currentGamepad), ButtonState.NewlyReleased) }
    val continuedPressed: ButtonCondition
      get() = ButtonCondition { stateMatchesButton(buttonState(prevGamepad), buttonState(currentGamepad), ButtonState.ContinuedPressed) }
    val continuedReleased: ButtonCondition
      get() = ButtonCondition { stateMatchesButton(buttonState(prevGamepad), buttonState(currentGamepad), ButtonState.ContinuedReleased) }
    val released: ButtonCondition
      get() = ButtonCondition { stateMatchesButton(buttonState(prevGamepad), buttonState(currentGamepad), ButtonState.Released) }
  }

  private val prevGamepad = Gamepad()
  private val currentGamepad = Gamepad()

  val touchpad_finger_1 by lazy { Button { it.touchpad_finger_1 } }
  val touchpad_finger_2 by lazy { Button { it.touchpad_finger_2 } }
  val touchpad by lazy { Button { it.touchpad } }
  val left_stick_button by lazy { Button { it.left_stick_button } }
  val right_stick_button by lazy { Button { it.right_stick_button } }
  val dpad_up by lazy { Button { it.dpad_up } }
  val dpad_down by lazy { Button { it.dpad_down } }
  val dpad_left by lazy { Button { it.dpad_left } }
  val dpad_right by lazy { Button { it.dpad_right } }
  val a by lazy { Button { it.a } }
  val cross by lazy { Button { it.cross } }
  val b by lazy { Button { it.b } }
  val circle by lazy { Button { it.circle } }
  val x by lazy { Button { it.x } }
  val square by lazy { Button { it.square } }
  val y by lazy { Button { it.y } }
  val triangle by lazy { Button { it.triangle } }
  val guide by lazy { Button { it.guide } }
  val ps by lazy { Button { it.ps } }
  val start by lazy { Button { it.start } }
  val options by lazy { Button { it.options } }
  val back by lazy { Button { it.back } }
  val share by lazy { Button { it.share } }
  val left_bumper by lazy { Button { it.left_bumper } }
  val right_bumper by lazy { Button { it.right_bumper } }

  private fun stateMatchesButton(prevButtonState: Boolean, currentButtonState: Boolean, requestedState: ButtonState): Boolean {
    return when (requestedState) {
      ButtonState.Pressed -> currentButtonState
      ButtonState.NewlyPressed -> currentButtonState && !prevButtonState
      ButtonState.ContinuedPressed -> currentButtonState && prevButtonState
      ButtonState.NewlyReleased -> !currentButtonState && prevButtonState
      ButtonState.ContinuedReleased -> !currentButtonState && !prevButtonState
      ButtonState.Released -> !currentButtonState
    }
  }

  /**
   * Update previous and current gamepad data
   * This should be called every loop
   */
  fun updateGamepadData() {
    // Update previous gamepad state
    prevGamepad.copy(currentGamepad)
    // Update current gamepad state
    currentGamepad.copy(gamepad)
  }
}

object ButtonGroupController {
  private val buttonGroups = mutableListOf<PressedAction>()

  fun clearButtonGroups() = buttonGroups.clear()

  fun add(buttonSequence: ButtonCondition, action: () -> Unit) {
    buttonGroups.add(PressedAction(buttonSequence, action))
  }

  /**
   * Process all the assigned button groups
   * This should be called every loop after updateGamepadData is called
   */
  fun processButtonGroups() {
    // For all button groups, if condition true, run action
    for (buttonGroup in buttonGroups) {
      if (buttonGroup.condition()) {
        buttonGroup.action()
      }
    }
  }
}