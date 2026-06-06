//package org.firstinspires.ftc.teamcode
//
//import com.qualcomm.hardware.lynx.LynxModule
//import com.qualcomm.hardware.lynx.LynxUsbDevice
//import com.qualcomm.hardware.lynx.commands.LynxDatagram
//import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorConstantPowerCommand
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp
//import com.qualcomm.robotcore.hardware.usb.serial.RobotUsbDeviceTty
//import com.qualcomm.robotcore.hardware.usb.serial.SerialPort
//import org.firstinspires.ftc.robotcore.internal.system.AppUtil
//import org.firstinspires.ftc.robotcore.internal.ui.UILocation
//import org.hexnibble.hexlib.L
//import java.io.FileInputStream
//import java.io.FileOutputStream
//import java.lang.reflect.Field
//import java.lang.reflect.Modifier
//
//@TeleOp
//class StopTesting : LinearOpMode() {
//  init {
//    AppUtil.getInstance().showToast(UILocation.BOTH, "Hi", 1)
////    AppUtil.getInstance().showAlertDialog(UILocation.BOTH, "ALERT TITLE!!!", "COOL ALERT MESSAGE!")
//  }
//  override fun runOpMode() {
//    val controlHub: LynxModule = hardwareMap.get(LynxModule::class.java, "Control Hub")
//
//    val usbDevField = LynxModule::class.java.getDeclaredField("lynxUsbDevice").also { it.isAccessible = true }
//    val usbDev = usbDevField.get(controlHub) as LynxUsbDevice
//
//    val serialPortField = RobotUsbDeviceTty::class.java.getDeclaredField("serialPort").also { it.isAccessible = true }
//    val port = serialPortField.get(usbDev.robotUsbDevice) as SerialPort
//
//    val inputStream = port.inputStream
//    val outputStream = port.outputStream
//
//    val cmd = LynxSetMotorConstantPowerCommand(controlHub, 0, 32767)
//    val msgNumFunction = LynxModule::class.java.getDeclaredMethod("getNewMessageNumber").also { it.isAccessible = true }
//    cmd.messageNumber = (msgNumFunction(controlHub) as Byte).toInt()
//
//    val serialization = LynxDatagram(cmd).toByteArray()
//
//    waitForStart()
//    while (opModeIsActive()) {
//      L.i("StopTesting", "running!")
//      if (gamepad1.crossWasPressed()) {
//        outputStream.write(serialization)
//        inputStream.read()
//      }
//    }
//    while (true) {
//      L.i("StopTesting", "Stopped but still running!")
//    }
//  }
//}