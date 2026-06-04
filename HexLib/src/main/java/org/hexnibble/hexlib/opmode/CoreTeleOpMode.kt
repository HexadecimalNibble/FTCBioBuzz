package org.hexnibble.hexlib.opmode

import org.hexnibble.hexlib.Constants
import org.hexnibble.hexlib.gamepad.ButtonGroupController
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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
      robot.follower.poseTracker.resetIMU()
//      setAllianceCFZeroIMUHdgDeg(0.0)
    }

    ButtonGroupController.add(controller1.right_stick_button.newlyPressed) {
      // enable slow mode
      // TODO: FIX PROB WON'T UPDATE WITH OVERRIDDEN CLASS
      dtSpeedMultiplier = Constants().slowModeDrivetrainSpeedMultiplier
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
    teleOpDrive(controller1.left_stick_x, controller1.left_stick_y, controller1.right_trigger - controller1.left_trigger, robot.follower.pose.heading)
  }

  /**
   * Simple efficient function to drive field-centric in teleop
   */
  fun teleOpDrive(x: Float, y: Float, triggers: Float, imuHeading: Double) {
    val robotX = x * cos(-imuHeading) - y * sin(-imuHeading)
    val robotY = x * sin(-imuHeading) + y * cos(-imuHeading)

    val lFRaw = robotY + robotX + triggers
    val lBRaw = robotY - robotX + triggers
    val rFRaw = robotY - robotX - triggers
    val rBRaw = robotY + robotX - triggers

    val denom = maxOf(abs(lFRaw), abs(lBRaw), abs(rFRaw), abs(rBRaw), 1.0)

    val lFMotor = lFRaw / denom * dtSpeedMultiplier
    val lBMotor = lBRaw / denom * dtSpeedMultiplier
    val rFMotor = rFRaw / denom * dtSpeedMultiplier
    val rBMotor = rBRaw / denom * dtSpeedMultiplier

    robot.follower.drivetrain.runDrive(doubleArrayOf(lFMotor, lBMotor, rFMotor, rBMotor))
  }
}