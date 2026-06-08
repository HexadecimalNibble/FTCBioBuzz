//package org.firstinspires.ftc.teamcode
//
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp
//import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot
//import org.hexnibble.hexlib.gamepad.ButtonGroupController
//import org.hexnibble.hexlib.opmode.CoreTeleOpMode
//
//@TeleOp(name = "OLD Biobuzz Teleop")
//class OldTeleop : CoreTeleOpMode() {
//  override fun createRobot() {
//    robot = BiobuzzRobot(hardwareMap)
//  }
//
//  override fun onPressInit() {
//    super.onPressInit()
//
//    // Prompt alliance info if no saved data from auto or options button pushed on either controller to force set alliance info
//    controller1.updateGamepadData()
//    controller2.updateGamepadData()
//    if (RobotData.allianceColor == null || RobotData.allianceSide == null || controller1.options.pressed() || controller2.options.pressed()) {
//      setAllianceInfo()
//
//      // Save alliance info
//      RobotData.allianceColor = allianceColor
//      RobotData.allianceSide = allianceSide
//    }
//
//    // Put controller bindings here
//    ButtonGroupController.add(controller1.cross) {}
//  }
//}