package org.hexnibble.hexlib.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.hexnibble.hexlib.L
import org.hexnibble.hexlib.RCController

class CoreLinearOpMode : LinearOpMode() {
    private val logTag = "CoreLinearOpMode"
    val rcController = RCController()
    override fun runOpMode() {
        L.d(logTag, "Initializing OpMode")
        onPressInit()
        // Play pressed
        if (opModeIsActive()) {
            onPressPlay()

            while (!isStopRequested && rcController.hasCommands) {

            }
        }
    }

    fun onPressInit() {
        // Bind controller actions & telemetry only in teleop
    }

    fun onPressPlay() {

    }
}