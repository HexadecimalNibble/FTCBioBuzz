package org.firstinspires.ftc.teamcode

import com.bylazar.telemetry.JoinedTelemetry
import com.bylazar.telemetry.PanelsTelemetry
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.util.ElapsedTime
import dev.anygeneric.blazeftc.DummyPlugOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry

@TeleOp(name = "NeutrinoTest")
//@Configurable //replace with whatever configurables library you use
class NeutrinoTest : DummyPlugOpModeCustom() {

    //@Configurable
    companion object {
        //feel free to decrease these, but note: if the "saturation" log in telemetry (you'll see it)
        //reaches 100%, the system will crash. No, I have not fixed it yet.
        @JvmStatic
        var millisToWait = 5L
        @JvmStatic
        var nanosToWait = 0
    }

    override fun runOpModeInBlaze() {
        //pass this function a telemetry, then use the opmode's telemetry after that.
        initializeBlazeFTC(JoinedTelemetry(telemetry, PanelsTelemetry.ftcTelemetry))
        //this mecanum code is just an example. do whatever you want here:
        val mecanum = Mecanum(hardwareMap.get(DcMotorEx::class.java, "flMotor"),
            hardwareMap.get(DcMotorEx::class.java, "frMotor"),
            hardwareMap.get(DcMotorEx::class.java, "blMotor"),
            hardwareMap.get(DcMotorEx::class.java, "brMotor"))
        waitForStart()
        runBlazeFTC(0)//call this and pass 0 to start the default neutrino handler
        val time = ElapsedTime()
        while (!isStopRequested) {
            mecanum.mecanumLoop(gamepad1)//call whatever you want, your code goes here!
            mecanum.telemetry(telemetry)
            //updateGamepads()//no need for this because neutrino does not use them
            val ms = time.milliseconds()
            telemetry.addData("java loop time", "${ms - millisToWait - nanosToWait / 999999}[ms]")
            time.reset()
            Thread.sleep(millisToWait, nanosToWait)
        }
    }
    class Mecanum(private val flMotor: DcMotorEx,
                  private val frMotor: DcMotorEx,
                  private val blMotor: DcMotorEx,
                  private val brMotor: DcMotorEx) {

        init {
            blMotor.direction = DcMotorSimple.Direction.FORWARD
            flMotor.direction = DcMotorSimple.Direction.FORWARD
            frMotor.direction = DcMotorSimple.Direction.REVERSE
            brMotor.direction = DcMotorSimple.Direction.REVERSE
        }

        fun telemetry(telemetry: Telemetry) {
            telemetry.addData("Front Left Power", flMotor.power)
            telemetry.addData("Front Right Power", frMotor.power)
            telemetry.addData("Back Left Power", blMotor.power)
            telemetry.addData("Back Right Power", brMotor.power)
        }

        fun mecanumLoop(gamepad1: Gamepad){
            val y = gamepad1.left_stick_y.toDouble()
            val x = -gamepad1.left_stick_x.toDouble()
            val turn = -gamepad1.right_stick_x.toDouble()

            flMotor.power = (y + x + turn)
            blMotor.power = (y - x + turn)
            frMotor.power = (y - x - turn)
            brMotor.power = (y + x - turn)
        }
    }
}