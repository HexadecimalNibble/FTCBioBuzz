package org.firstinspires.ftc.teamcode.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple

@TeleOp(name = "Sans opmode")
class sansteleop : LinearOpMode() {
    lateinit var frontLeftMotor : DcMotor
    lateinit var frontRightMotor : DcMotor
    lateinit var backLeftMotor : DcMotor
    lateinit var backRightMotor : DcMotor

    override fun runOpMode() {
         frontLeftMotor = hardwareMap.get(DcMotor::class.java,"LFMotor")
         backLeftMotor = hardwareMap.get(DcMotor::class.java,"LBMotor")
         frontRightMotor = hardwareMap.get(DcMotor::class.java,"RFMotor")
         backRightMotor = hardwareMap.get(DcMotor::class.java,"RBMotor")
        frontLeftMotor.direction = DcMotorSimple.Direction.REVERSE
        backLeftMotor.direction = DcMotorSimple.Direction.REVERSE

        waitForStart()

        if (isStopRequested) return

        while (opModeIsActive()) {
            // Controller Inputs
            val y = -gamepad1.left_stick_y.toDouble()
            val x = gamepad1.left_stick_x.toDouble()
            val rx = gamepad1.right_trigger.toDouble() - gamepad1.left_trigger.toDouble()
            var frontLeftPower = y + x + rx
            var backLeftPower = y - x + rx
            var frontRightPower = y - x - rx
            var backRightPower = y + x - rx
        val max = maxOf (
            kotlin.math.abs(frontLeftPower),
            kotlin.math.abs(frontRightPower),
            kotlin.math.abs(backLeftPower),
            kotlin.math.abs(backRightPower),
            1.0

        )
    frontLeftPower /= max
    frontRightPower /= max
    backRightPower /= max
    backLeftPower /= max
        frontLeftMotor.power = frontLeftPower
        backLeftMotor.power = backLeftPower
        frontRightMotor.power = frontRightPower
        backRightMotor.power = backRightPower
    }

} }