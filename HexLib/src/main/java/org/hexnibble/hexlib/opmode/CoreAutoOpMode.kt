package org.hexnibble.hexlib.opmode

/**
 * ?
 *
 * @author Benjamin Kang
 */
class CoreAutoOpMode() : CoreLinearOpMode() {
  override fun onPressInit() {
    super.onPressInit()
  }

  override fun onPressPlay() {
    super.onPressPlay()
  }

  override fun onPressStop() {
    super.onPressStop()
  }

  override val opModeComplete: Boolean
    get() = !rcController.busy
}
