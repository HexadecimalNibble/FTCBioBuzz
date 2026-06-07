package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import kotlin.time.Duration.Companion.seconds

@Suppress("UNUSED")
val myFirstMercurialTeleOp = Mercurial.teleop("Mercurial Test OpMode") {
  val fl = hardwareMap.get(DcMotorEx::class.java, "LFMotor").also { it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE }
  val bl = hardwareMap.get(DcMotorEx::class.java, "LBMotor").also { it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE }
  val fr = hardwareMap.get(DcMotorEx::class.java, "RFMotor").also { it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE }
  val br = hardwareMap.get(DcMotorEx::class.java, "RBMotor").also { it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE }

  var throttle = 1.0

  bl.direction = DcMotorSimple.Direction.REVERSE
  fl.direction = DcMotorSimple.Direction.REVERSE

  var lastLoopTime = 0UL

  // POV drive
  schedule(
    sequence(
      // wait can also take a boolean supplier,
      // we'll start this process now,
      // but it will wait until we press play to actually start running
      wait { inLoop },
      loop(exec {
        val drive = -gamepad1.left_stick_y.toDouble()
        val turn = (gamepad1.right_trigger - gamepad1.left_trigger).toDouble()

        // a simple POV drive
        fl.power = (drive + turn) * throttle
        bl.power = (drive + turn) * throttle
        br.power = (drive - turn) * throttle
        fr.power = (drive - turn) * throttle

        val currentTime = (System.nanoTime() / 1e6).toULong()
        println("Last loop time: ${currentTime - lastLoopTime}ms")
        lastLoopTime = currentTime
      })
    )
  )

  // throttle controls
  bindSpawn(
    risingEdge { gamepad1.left_stick_button },
    exec { throttle = 0.3 }
  )

  bindSpawn(
    // inverting the condition will convert our rising edge detector to a falling edge detector!
    risingEdge { !gamepad1.left_stick_button },
    exec { throttle = 1.0 }
  )

  bindSpawn(
    risingEdge { gamepad1.cross },
    sequence(
      exec { throttle = 0.1 },
      wait(2.0),
      exec { throttle = 1.0 },
    )
  )

  dropToScheduler()
}