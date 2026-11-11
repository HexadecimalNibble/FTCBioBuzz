//package org.firstinspires.ftc.teamcode.opmodes
//
//import dev.nextftc.hardware.RobotController
//import dev.nextftc.robot.NextRobot
//import dev.nextftc.robot.opmode.BulkReadHook
//import dev.nextftc.robot.opmode.NextOpMode
//
//class ServoTestUtilityOpMode(robot: NextRobot) : NextOpMode(robot, BulkReadHook) {
//  // Control Hub, Expansion Hub, Servo Hub (left-most bit 1-2)
//  // 00 - Control Hub
//  // 01 - Expansion Hub
//  // 10 - Servo Hub
//
//  // Type (left-most bit 3)
//  // 0 - Motor
//  // 1 - Servo
//
//  // Port (left-most bit 4-6)
//  // 000 - 0
//  // 001 - 1
//  // etc...
//
//  val devices = arrayOf(
//    // Control hub motors
//    0b00_0_000,
//    0b00_0_001,
//    0b00_0_010,
//    0b00_0_011,
//
//    // Control hub servos
//    0b00_1_000,
//    0b00_1_001,
//    0b00_1_010,
//    0b00_1_011,
//    0b00_1_100,
//    0b00_1_101,
//
//    // Expansion hub motors
//    0b01_0_000,
//    0b01_0_001,
//    0b01_0_010,
//    0b01_0_011,
//
//    // Expansion hub servos
//    0b01_1_000,
//    0b01_1_001,
//    0b01_1_010,
//    0b01_1_011,
//    0b01_1_100,
//    0b01_1_101,
//
//    // Servo hub servos
//    0b10_1_000,
//    0b10_1_001,
//    0b10_1_010,
//    0b10_1_011,
//    0b10_1_100,
//    0b10_1_101,
//  )
//
//  enum class Menu {
//    SelectDevice,
//
//  }
//
//  var power = 0.0
//  var selectedIndex = 0
//
//
//  override fun periodic() {
//    printDevices()
//
//    if (gamepad1.dpadUpWasPressed() || gamepad2.dpadUpWasPressed()) {
//      selectedIndex = (selectedIndex - 1).coerceIn(0, devices.size - 1)
//    } else if (gamepad1.dpadDownWasPressed() || gamepad2.dpadDownWasPressed()) {
//      selectedIndex = (selectedIndex + 1).coerceIn(0, devices.size - 1)
//    } else if (gamepad1.crossWasPressed() || gamepad2.crossWasPressed()) {
//
//    }
//    RobotController.controlHub.motorController.setMotorPower()
//  }
//
//  fun printDevices() {
//    var output = ""
//
//    for ((i, device) in devices.withIndex()) {
//      val hub = if ((device and (1 shl 6)) != 0) { // Servo hub
//        "Servo Hub"
//      } else if ((device and (1 shl 5)) != 0) { // Expansion hub
//        "Expansion Hub"
//      } else { // Control hub
//        "Control Hub"
//      }
//
//      val port = device and 0b111
//
//      val hardwareType = if ((device and (1 shl 4)) != 0) { // Servo
//        "Servo"
//      } else { // Motor
//        "Motor"
//      }
//
//      output += "[${if (selectedIndex == i) {"x"} else {" "}}] $hub: $hardwareType Port $port\n"
//    }
//
//    telemetry.addLine(output)
//  }
//}