package org.hexnibble.hexlib

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode

class CoreLinearOpMode : LinearOpMode() {
    private val logTag = "CoreLinearOpMode"
    val rcController = RCController()
    override fun runOpMode() {
        L.d(logTag, "Initializing OpMode")
        onPressInit()
    }
}