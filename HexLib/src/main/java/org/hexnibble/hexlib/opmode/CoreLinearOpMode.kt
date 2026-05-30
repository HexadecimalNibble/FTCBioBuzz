package org.hexnibble.hexlib.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.internal.system.AppUtil
import org.firstinspires.ftc.robotcore.internal.ui.UILocation
import org.hexnibble.hexlib.AllianceColor
import org.hexnibble.hexlib.AllianceSide
import org.hexnibble.hexlib.gamepad.ControllerWrapper
import org.hexnibble.hexlib.L
import org.hexnibble.hexlib.commands.RCController
import org.hexnibble.hexlib.BaseRobot
import org.hexnibble.hexlib.StopOpModeException
import org.hexnibble.hexlib.gamepad.ButtonGroupController

/**
 * ?
 *
 * @author Benjamin Kang
 */

open class CoreLinearOpMode : LinearOpMode() {
  private val logTag = "CoreLinearOpMode"
  val rcController = RCController()

  lateinit var controller1: ControllerWrapper
  lateinit var controller2: ControllerWrapper

  lateinit var allianceColor: AllianceColor
  lateinit var allianceSide: AllianceSide

  lateinit var robot: BaseRobot

  /**
   * Flag to end OpMode
   * Override
   */
  open val opModeComplete: Boolean
    get() = false

  /**
   * Override this function to set the robot variable to the robot class for this season
   */
  open fun createRobot() {}

  override fun runOpMode() {
    try {
      L.i(logTag, "onPressInit()")

      L.d(logTag, "Creating controllers")
      controller1 = ControllerWrapper(gamepad1)
      controller2 = ControllerWrapper(gamepad2)

      L.d(logTag, "Creating robot")
      createRobot()
      if (!this::robot.isInitialized) {
        L.e(logTag, "No robot object initialized!")
        throw Exception("Override createRobot() and assign a value to the robot variable!")
      }
      robot.createFollower()
      if (!robot.isFollowerInitialized()) {
        L.e(logTag, "Follower not initialized!")
        throw Exception("Override createFollower() in the robot class and create the follower!")
      }
      onPressInit()

      L.d(logTag, "Waiting for Start")
      AppUtil.getInstance().showToast(UILocation.BOTH, "Initialization Complete. Waiting for Start.", 0)
    } catch (_: StopOpModeException) {
      L.i(logTag, "OpMode stopped")
    }
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

  fun setAllianceInfo(): String {
    var leftPressed = false
    var rightPressed = false

    // region * Select Starting Position *
    ButtonGroupController.add(controller1.left_bumper.newlyPressed or controller2.left_bumper.newlyPressed) {
      leftPressed = true
    }
    ButtonGroupController.add(controller1.right_bumper.newlyPressed or controller2.right_bumper.newlyPressed) {
      rightPressed = true
    }

    telemetry.addLine("Please choose ALLIANCE color:")
    telemetry.addLine("\tLEFT BUMPER = BLUE Alliance 🔵")
    telemetry.addLine("\tRIGHT BUMPER = RED Alliance 🔴\n")
    telemetry.update()
    while (true) {
      if (isStopRequested) throw StopOpModeException()

      // Process controls
      ButtonGroupController.processButtonGroups()
      controller1.updateGamepadData()
      controller2.updateGamepadData()

      if (leftPressed) {
        allianceColor = AllianceColor.Blue
        break
      }
      if (rightPressed) {
        allianceColor = AllianceColor.Red
        break
      }
    }
    val allianceColorText: String = when (allianceColor) {
      AllianceColor.Blue -> "$allianceColor 🔵"
      else -> "$allianceColor 🔴"
    }

    leftPressed = false
    rightPressed = false

    telemetry.addLine("Selected Alliance Color: $allianceColorText\n")
    telemetry.addLine("Please choose ALLIANCE side:")
    telemetry.addLine("\tLEFT BUMPER = LEFT Side")
    telemetry.addLine("\tRIGHT BUMPER = RIGHT Side\n")
    telemetry.update()
    while (true) {
      if (isStopRequested) throw StopOpModeException()
      // Process controls
      ButtonGroupController.processButtonGroups()
      controller1.updateGamepadData()
      controller2.updateGamepadData()

      if (leftPressed) {
        allianceSide = AllianceSide.Left
        break
      }
      if (rightPressed) {
        allianceSide = AllianceSide.Right
        break
      }
    }

    ButtonGroupController.clearButtonGroups()

    return allianceColorText
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