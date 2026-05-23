package org.hexnibble.hexlib.opmode

import org.hexnibble.hexlib.gamepad.ButtonGroupController

/**
 * ?
 *
 * @author Benjamin Kang
 */
open class CoreTeleOpMode : CoreLinearOpMode() {
  override fun onPressInit() {
    super.onPressInit()
    // Bind controller actions & telemetry only in teleop
    ButtonGroupController.add(controller1.options and controller1.square) {
      // reset imu
    }

    ButtonGroupController.add(controller1.right_stick_button.newlyPressed) {
      // enable slow mode
    }

    ButtonGroupController.add(controller1.right_stick_button.newlyReleased) {
      // disable slow mode
    }
  }

  override fun opModeLoop() {
    super.opModeLoop()

    // Update controls & run button groups
    controller1.updateGamepadData()
    controller2.updateGamepadData()
    ButtonGroupController.processButtonGroups()

    // Run teleop drive
    // TODO("Teleop drive")
  }
}