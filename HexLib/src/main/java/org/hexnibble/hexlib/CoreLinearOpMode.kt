//package org.hexnibble.hexlib
//
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
//
//class CoreLinearOpMode : LinearOpMode() {
//    private val logTag = "CoreLinearOpMode"
//    val rcController = RCController()
//    override fun runOpMode() {
//        L.d(logTag, "Initializing OpMode")
//        onPressInit()
//        // Play pressed
//        if (opModeIsActive()) {
//            onPressPlay()
//
//            while (!isStopRequested && !opModeComplete) {
//                // Write sensor data to shared memory
//                runOpModeLoop(rcController, CoreLinearOpMode.robot)
//                onLoopCustom()
//
//                processTelemetry()
//                getOpModeTimer().processLoopTime()
//            }
//        }
//    }
//
//    fun onPressInit() {
//        // Bind controller actions & telemetry only in teleop
//    }
//
//    fun onPressPlay() {
//
//    }
//}