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
//                mainLoopWriteSensorsToSharedMemory()
//
//                runOpModeLoop(rcController, CoreLinearOpMode.robot)
//                onLoopCustom()
//
//                // Read sensor data back from shared memory
//                CoreLinearOpMode.robot.readDataFromSharedMemory()
//                CoreLinearOpMode.robot.sendCommandsToDevices()
//
//                processTelemetry()
//                getOpModeTimer().processLoopTime()
//            }
//        }
//    }
//}