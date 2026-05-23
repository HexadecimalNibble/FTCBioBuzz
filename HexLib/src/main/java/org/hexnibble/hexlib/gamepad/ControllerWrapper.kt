package org.hexnibble.hexlib.gamepad

import com.qualcomm.robotcore.hardware.Gamepad

fun interface ButtonCondition {
  fun invoke(): Boolean
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
  ) {
    fun invoke(): Boolean = buttonState(currentGamepad)

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

  private val pressedActions = mutableListOf<PressedAction>()

  val prevGamepad = Gamepad()
  val currentGamepad = Gamepad()

  val dpad_up by lazy { Button { it.dpad_up } }
  val dpad_down by lazy { Button { it.dpad_down } }
  val dpad_left by lazy { Button { it.dpad_left } }
  val dpad_right by lazy { Button { it.dpad_right } }
  val a by lazy { Button { it.a } }
  val b by lazy { Button { it.b } }
  val x by lazy { Button { it.x } }
  val y by lazy { Button { it.y } }
  val guide by lazy { Button { it.guide } }
  val start by lazy { Button { it.start } }
  val back by lazy { Button { it.back } }
  val left_bumper by lazy { Button { it.left_bumper } }
  val right_bumper by lazy { Button { it.right_bumper } }
  val left_stick_button by lazy { Button { it.left_stick_button } }
  val right_stick_button by lazy { Button { it.right_stick_button } }

  fun stateMatchesButton(prevButtonState: Boolean, currentButtonState: Boolean, requestedState: ButtonState): Boolean {
    return when (requestedState) {
      ButtonState.Pressed -> currentButtonState
      ButtonState.NewlyPressed -> currentButtonState && !prevButtonState
      ButtonState.ContinuedPressed -> currentButtonState && prevButtonState
      ButtonState.NewlyReleased -> !currentButtonState && prevButtonState
      ButtonState.ContinuedReleased -> !currentButtonState && !prevButtonState
      ButtonState.Released -> !currentButtonState
    }
  }

  fun addButtonGroup(buttonSequence: ButtonCondition, action: () -> Unit) {
    pressedActions.add(PressedAction(buttonSequence, action))
  }

  fun processButtonGroups() {
    // For all buttonSequences in list, if valid run associated action
    for (pressedAction: PressedAction in pressedActions) {
      if (pressedAction.condition()) {

      }
    }
  }

  fun updateGamepadData() {
    // Update previous gamepad state
    prevGamepad.copy(currentGamepad)
    // Update current gamepad state
    currentGamepad.copy(gamepad)
  }
}