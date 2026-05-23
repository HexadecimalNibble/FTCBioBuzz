package org.hexnibble.hexlib.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.hexnibble.hexlib.ControllerWrapper
import org.hexnibble.hexlib.L
import org.hexnibble.hexlib.RCController
import org.hexnibble.hexlib.Robot

/**
 * ?
 *
 * @author Benjamin Kang
 */

open class CoreLinearOpMode : LinearOpMode() {
    private val logTag = "CoreLinearOpMode"
    val rcController = RCController()

    val controller1 = ControllerWrapper()
    val controller2 = ControllerWrapper()

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

            }
        }

        onPressStop()
    }

    /**
     * Custom logic that should be run when initialize is pressed
     */
    open fun onPressInit() {
        // Bind controller actions & telemetry only in teleop
    }

    /**
     * Override to run custom code when play pressed
     */
    open fun onPressPlay() {}

    /**
     * Override to run custom code when stop pressed
     * The custom code shouldn't run for too long or the OpMode will be force stopped by the SDK
     */
    open fun onPressStop() {}
}