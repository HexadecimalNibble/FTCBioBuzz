package org.hexnibble.hexlib.opmode

import org.hexnibble.hexlib.L

/**
 * ?
 *
 * @author Benjamin Kang
 */
open class CoreAutoOpMode : CoreLinearOpMode() {
  private val logTag = "CoreAutoOpMode"

  override fun onPressInit() {
    super.onPressInit()
    autoSetup()
    queueAutoCommands()
  }

  /**
   * Override this function to set custom auto setup behavior
   * Users of this function should call super.autoSetup(), set a starting robot position, and
   * do any other things
   */
  open fun autoSetup() {
    L.i(logTag, "Initializing auto")
  }

  /**
   * Override this function to run the auto.
   * Users should queue through the command controller what their auto will do.
   */
  open fun queueAutoCommands() {
    L.i(logTag, "Queueing auto commands")

    telemetry.addLine("PLEASE WAIT\nQueueing auto commands...")
    telemetry.update()
  }

  override fun onPressStop() {
    super.onPressStop()
  }

  override val opModeComplete: Boolean
    get() = !rcController.busy
}