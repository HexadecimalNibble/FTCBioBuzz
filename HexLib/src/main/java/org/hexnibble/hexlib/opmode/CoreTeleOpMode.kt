package org.hexnibble.hexlib.opmode

import org.hexnibble.hexlib.gamepad.ButtonGroupController

/**
 * ?
 *
 * @author Benjamin Kang
 */
open class CoreTeleOpMode : CoreLinearOpMode() {
  var dtSpeedMultiplier = 1.0
  override fun onPressInit() {
    super.onPressInit()
    // Bind controller actions & telemetry only in teleop
    ButtonGroupController.add(controller1.options and controller1.square) {
      // reset imu
    }

    ButtonGroupController.add(controller1.right_stick_button.newlyPressed) {
      // enable slow mode
      dtSpeedMultiplier = 0.5 // TODO: SET BASED ON CONSTANTS SOMEWHERE
    }

    ButtonGroupController.add(controller1.right_stick_button.newlyReleased) {
      // disable slow mode
      dtSpeedMultiplier = 1.0
    }
  }

  override fun opModeLoop() {
    super.opModeLoop()

    // Update controls & run button groups
    controller1.updateGamepadData()
    controller2.updateGamepadData()
    ButtonGroupController.processButtonGroups()

    // Run teleop drive
    teleOpDrive(controller1.left_stick_x, controller1.left_stick_y, controller1.right_trigger - controller1.left_trigger, TODO())
  }

  /**
   * Simple efficient function to drive field-centric in teleop
   */
  fun teleOpDrive(x: Float, y: Float, triggers: Float, imuHeading: Double) {
    TODO()
//    use dtSpeedMultiplier
  }
}