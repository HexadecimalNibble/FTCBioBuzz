package org.hexnibble.hexlib

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.hardware.HardwareMap

/**
 * Override this class for each season's robot
 */
abstract class BaseRobot(hwMap: HardwareMap) {
  private val logTag = "BaseRobot"
  val controlHub = hwMap.get(LynxModule::class.java, "Control Hub")

  val expansionHub: LynxModule? = try {
    hwMap.get(LynxModule::class.java, "Expansion Hub 2")
  } catch (_: IllegalArgumentException) {
    L.e(logTag, "Expansion Hub was missing!")
    null
  }

  val servoHub: LynxModule? = try {
    hwMap.get(LynxModule::class.java, "Servo Hub 3")
  } catch (_: IllegalArgumentException) {
    L.e(logTag, "Servo Hub was missing!")
    null
  }

  init {
    // Set bulk caching mode for hubs.
    controlHub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL)
    expansionHub?.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL)
  }

  fun bulkReadControlHub() {
    controlHub.clearBulkCache()
  }

  /**
   * Bulk read the expansion hub if not null
   */
  fun bulkReadExpansionHub() {
    expansionHub?.clearBulkCache()
  }
}