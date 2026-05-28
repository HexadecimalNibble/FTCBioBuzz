//package org.firstinspires.ftc.teamcode
//
//import com.pedropathing.geometry.Pose
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous
//import org.firstinspires.ftc.teamcode.pedroPathing.PedroConstants
//import org.hexnibble.hexlib.AllianceColor
//import org.hexnibble.hexlib.AllianceSide
//import org.hexnibble.hexlib.L
//import org.hexnibble.hexlib.StopOpModeException
//import org.hexnibble.hexlib.gamepad.ButtonGroupController
//import org.hexnibble.hexlib.opmode.CoreAutoOpMode
//
//@Autonomous(name = "Biobuzz Auto")
//class Auto : CoreAutoOpMode() {
//  lateinit var allianceColorText: String
//
//  override fun setRobot() {
//    robot = BiobuzzRobot(hardwareMap)
//  }
//
//  override fun autoSetup() {
//    super.autoSetup()
//    allianceColorText = super.setAllianceInfo()
//
//    // Save alliance info for teleop
//    RobotData.allianceColor = allianceColor
//    RobotData.allianceSide = allianceSide
//    // endregion * Select Starting Position *
//  }
//
//  override fun queueAutoCommands() {
//    super.queueAutoCommands()
//    rcController.qRC()
//  }
//
//  override fun readyMessage() {
//    telemetry.addLine("Selected Alliance Color: $allianceColorText")
//    telemetry.addLine("Selected Alliance Side: $allianceSide\n")
//    super.readyMessage()
//  }
//
//  override fun onPressStop() {
//    RobotData.robotPosition = Pose(100.0, 100.0, 100.0)
//    L.i("Teleop", "Saving robot position: ${RobotData.robotPosition}")
//  }
//}