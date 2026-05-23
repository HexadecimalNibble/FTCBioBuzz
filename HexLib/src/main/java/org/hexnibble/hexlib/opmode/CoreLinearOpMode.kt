package org.hexnibble.hexlib.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.hexnibble.hexlib.gamepad.ControllerWrapper
import org.hexnibble.hexlib.L
import org.hexnibble.hexlib.commands.RCController
import org.hexnibble.hexlib.Robot
import org.hexnibble.hexlib.gamepad.ButtonGroupController

/**
 * ?
 *
 * @author Benjamin Kang
 */

open class CoreLinearOpMode : LinearOpMode() {
  private val logTag = "CoreLinearOpMode"
  val rcController = RCController()

  val controller1 = ControllerWrapper(gamepad1)
  val controller2 = ControllerWrapper(gamepad2)

  val robot = Robot()

  /**
   * Flag to end OpMode
   * Override
   */
  open val opModeComplete: Boolean
    get() = false

  override fun runOpMode() {
    L.d(logTag, "onPressInit()")
    onPressInit()

    L.d(logTag, "Waiting for Start")
    waitForStart()

    // Play pressed
    if (opModeIsActive()) {
      L.d(logTag, "onPressPlay()")
      onPressPlay()

      while (!isStopRequested && !opModeComplete) {
        opModeLoop()
      }
    }

    onPressStop()
  }

  /**
   * Custom logic that should be run when initialize is pressed
   */
  open fun onPressInit() {
    // Make sure controllers are reset properly
    ButtonGroupController.clearButtonGroups()
  }

  /**
   * Override to run custom code when play pressed
   */
  open fun onPressPlay() {}

  /**
   * Override to run custom code when opmode is running
   */
  open fun opModeLoop() {
//    rcController.processCommands()
  }

  /**
   * Override to run custom code when stop pressed
   * The custom code shouldn't run for too long or the OpMode will be force stopped by the SDK
   */
  open fun onPressStop() {}
}