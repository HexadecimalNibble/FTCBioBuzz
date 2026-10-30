import com.qualcomm.robotcore.hardware.Gamepad
import dev.nextftc.hardware.util.EventLoop
import dev.nextftc.robot.triggers.CommandGamepad
import dev.nextftc.robot.triggers.Trigger

class NewCommandGamepad @JvmOverloads constructor(
  private val gamepad: Gamepad,
  private val eventLoop: EventLoop = Trigger.defaultEventLoop,
) {
  private val base = CommandGamepad(gamepad, eventLoop)

  // Existing buttons
  val leftStickX get() = base.leftStickX
  val leftStickY get() = base.leftStickY
  val rightStickX get() = base.rightStickX
  val rightStickY get() = base.rightStickY

  val a get() = base.a
  val b get() = base.b
  val x get() = base.x
  val y get() = base.y

  val leftBumper get() = base.leftBumper
  val rightBumper get() = base.rightBumper

  val dpadUp get() = base.dpadUp
  val dpadDown get() = base.dpadDown
  val dpadLeft get() = base.dpadLeft
  val dpadRight get() = base.dpadRight
  val back get() = base.back
  val start get() = base.start

  val square get() = base.square
  val circle get() = base.circle
  val cross get() = base.cross
  val triangle get() = base.triangle

  val rightTrigger get() = base.rightTrigger
  val leftTrigger get() = base.leftTrigger

  // New buttons
  val leftStickButton get() = base.leftStickButton
  val rightStickButton get() = base.rightStickButton

  val options = Trigger(eventLoop) { gamepad.options }
  val share = Trigger(eventLoop) { gamepad.share }
  val ps = Trigger(eventLoop) { gamepad.ps }
}