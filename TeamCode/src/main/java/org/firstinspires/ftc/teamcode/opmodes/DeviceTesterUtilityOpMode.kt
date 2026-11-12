package org.firstinspires.ftc.teamcode.opmodes

import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType
import dev.nextftc.hardware.RobotController
import dev.nextftc.robot.NextRobot
import dev.nextftc.robot.opmode.BulkReadHook
import dev.nextftc.robot.opmode.NextOpMode
import dev.nextftc.robot.opmode.NextUtility
import org.firstinspires.ftc.robotcore.external.Telemetry
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@NextUtility("Device Tester")
class DeviceTesterUtilityOpMode(robot: NextRobot) :
  NextOpMode(robot, BulkReadHook) {

  companion object {
    private const val MOTOR_PORTS = 4
    private const val SERVO_PORTS = 6
    private const val ANALOG_PORTS = 4
    private const val DIGITAL_PORTS = 8

    private const val PWM_MIN = 500.0
    private const val PWM_MAX = 2500.0
    private const val PWM_GAP = 100.0
    private const val PWM_FRAME_MIN = 10000.0
    private const val PWM_FRAME_MAX = 40000.0

    private const val DEADZONE = 0.12
    private const val REFRESH_NS = 100_000_000L

    private const val BLUE = "#43BFFF"
    private const val GREEN = "#55DDA0"
    private const val PURPLE = "#C69AFF"
    private const val ORANGE = "#FFBC58"
    private const val RED = "#FF626E"
    private const val WHITE = "#E2EAF4"
    private const val GRAY = "#AAB5C4"
    private const val MUTED = "#65758B"
  }

  // ============================================================
  // FILTERS
  // ============================================================

  private enum class DeviceFilter(
    val label: String,
    val type: String?
  ) {
    ALL("ALL", null),
    MOTORS("MOTORS", "MOTOR"),
    SERVOS("SERVOS", "SERVO"),
    ANALOG("ANALOG", "ANALOG"),
    DIGITAL("DIGITAL", "DIGITAL")
  }

  private var filterIndex = 0

  private val currentFilter: DeviceFilter
    get() = DeviceFilter.values()[filterIndex]

  private val visibleDevices: List<TestDevice>
    get() {
      val type = currentFilter.type

      return if (type == null) {
        devices
      } else {
        devices.filter { it.type == type }
      }
    }

  // ============================================================
  // DATA MODEL
  // ============================================================

  private data class ControlChannel(
    val id: String,
    val label: String,
    val unit: String,
    val min: Double,
    val max: Double,
    val step: Double,
    val stickRate: Double = 0.0,
    val read: () -> Double,
    val write: (Double) -> Unit,
    val display: (Double) -> String = {
      String.format(Locale.US, "%.2f", it)
    }
  )

  private class TestDevice(
    val name: String,
    val type: String,
    val port: Int,
    val channels: List<ControlChannel>,
    val readState: () -> String,
    val stop: () -> Unit = {}
  ) {
    var channelIndex = 0
    var state = "Not read yet"
    var initializationError: String? = null

    val channel: ControlChannel?
      get() = channels.getOrNull(channelIndex)

    fun nextChannel() {
      if (channels.isNotEmpty()) {
        channelIndex = (channelIndex + 1) % channels.size
      }
    }
  }

  // ============================================================
  // STATE
  // ============================================================

  private val devices: List<TestDevice> by lazy {
    discoverDevices()
  }

  private val selectedDevices = mutableSetOf<String>()

  private var selectedIndex = 0
  private var lastMessage = "Ready"

  private var previousStart = false
  private var lastRefresh = 0L
  private var lastLoop = System.nanoTime()

  private var htmlConfigured = false

  private val selected: TestDevice?
    get() = visibleDevices.getOrNull(selectedIndex)

  private fun targets(): List<TestDevice> {
    if (selectedDevices.isEmpty()) {
      return listOfNotNull(selected)
    }

    return devices.filter {
      it.name in selectedDevices
    }
  }

  // ============================================================
  // OP MODE
  // ============================================================

  override fun periodic() {

    if (!htmlConfigured) {
      telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML)
      telemetry.setMsTransmissionInterval(100)
      htmlConfigured = true
    }

    val allDevices = devices

    if (allDevices.isEmpty()) {
      telemetry.clearAll()
      ui("DEVICE TESTER", BLUE)
      ui("No hub ports available.", RED)
      return
    }

    val now = System.nanoTime()

    val deltaTime = (
        (now - lastLoop) / 1_000_000_000.0
        ).coerceIn(0.0, 0.06)

    lastLoop = now

    // Emergency motor stop.
    val startDown = gamepad1.start || gamepad2.start
    val startPressed = startDown && !previousStart

    previousStart = startDown

    if (startPressed) {
      emergencyStop()
      refreshState(force = true)
      render()
      return
    }

    // Navigation.
    when {
      gamepad1.leftBumperWasPressed() ||
          gamepad2.leftBumperWasPressed() -> {
        changeFilter(-1)
      }

      gamepad1.rightBumperWasPressed() ||
          gamepad2.rightBumperWasPressed() -> {
        changeFilter(1)
      }

      gamepad1.dpadUpWasPressed() ||
          gamepad2.dpadUpWasPressed() -> {
        moveSelection(-1)
      }

      gamepad1.dpadDownWasPressed() ||
          gamepad2.dpadDownWasPressed() -> {
        moveSelection(1)
      }

      gamepad1.triangleWasPressed() ||
          gamepad2.triangleWasPressed() -> {
        nextChannel()
      }

      gamepad1.dpadLeftWasPressed() ||
          gamepad2.dpadLeftWasPressed() -> {
        adjustTargets(-1)
      }

      gamepad1.dpadRightWasPressed() ||
          gamepad2.dpadRightWasPressed() -> {
        adjustTargets(1)
      }

      gamepad1.crossWasPressed() ||
          gamepad2.crossWasPressed() -> {
        toggleSelection()
      }

      gamepad1.circleWasPressed() ||
          gamepad2.circleWasPressed() -> {
        clearSelection()
      }

      gamepad1.squareWasPressed() ||
          gamepad2.squareWasPressed() -> {
        stopTargets()
      }
    }

    applyStick(deltaTime)

    refreshState()
    render()
  }

  override fun end() {
    emergencyStop()
  }

  // ============================================================
  // FILTER NAVIGATION
  // ============================================================

  private fun changeFilter(direction: Int) {
    val filters = DeviceFilter.values()

    filterIndex = (
        filterIndex + direction + filters.size
        ) % filters.size

    selectedIndex = 0
    lastRefresh = 0L

    lastMessage = "Filter: ${currentFilter.label}"
  }

  private fun moveSelection(direction: Int) {
    val count = visibleDevices.size

    if (count == 0) return

    selectedIndex =
      (selectedIndex + direction + count) % count

    lastRefresh = 0L

    lastMessage = "Selected ${selected?.name}"
  }

  private fun nextChannel() {
    val device = selected ?: return

    if (device.channels.isEmpty()) {
      lastMessage = "Read-only device"
      return
    }

    device.nextChannel()
    lastMessage = "Control: ${device.channel?.label}"
  }

  // ============================================================
  // RAW PORT DISCOVERY
  // ============================================================

  private fun discoverDevices(): List<TestDevice> {
    val result = mutableListOf<TestDevice>()

    // CONTROL HUB
    val control = RobotController.controlHub

    addStandardHub(
      result = result,
      name = "Control Hub",
      motorFactory = {
        DcMotorImplEx(control.motorController, it)
      },
      servoFactory = {
        ServoImplEx(
          control.servoController as ServoControllerEx,
          it,
          ServoConfigurationType.getStandardServoType()
        )
      },
      analogFactory = {
        AnalogInput(control.analogController, it)
      },
      digitalFactory = {
        DigitalChannelImpl(control.digitalController, it)
      }
    )

    // EXPANSION HUB
    val expansion = RobotController.expansionHub

    if (expansion != null) {
      addStandardHub(
        result = result,
        name = "Expansion Hub",
        motorFactory = {
          DcMotorImplEx(expansion.motorController, it)
        },
        servoFactory = {
          ServoImplEx(
            expansion.servoController as ServoControllerEx,
            it,
            ServoConfigurationType.getStandardServoType()
          )
        },
        analogFactory = {
          AnalogInput(expansion.analogController, it)
        },
        digitalFactory = {
          DigitalChannelImpl(expansion.digitalController, it)
        }
      )
    }

    // SERVO HUBS
    RobotController.servoHubs.forEachIndexed { index, hub ->
      val controller = hub.servoController as ServoControllerEx

      for (port in 0 until SERVO_PORTS) {
        addSafely(
          result,
          "Servo Hub ${index + 1} / Servo $port",
          "SERVO",
          port
        ) {
          createServo(
            hub = "Servo Hub ${index + 1}",
            port = port,
            servo = ServoImplEx(
              controller,
              port,
              ServoConfigurationType.getStandardServoType()
            )
          )
        }
      }
    }

    return result.sortedWith(
      compareBy<TestDevice> { deviceOrder(it.type) }
        .thenBy { it.name }
    )
  }

  private fun addStandardHub(
    result: MutableList<TestDevice>,
    name: String,
    motorFactory: (Int) -> DcMotorEx,
    servoFactory: (Int) -> ServoImplEx,
    analogFactory: (Int) -> AnalogInput,
    digitalFactory: (Int) -> DigitalChannel
  ) {
    for (port in 0 until MOTOR_PORTS) {
      addSafely(result, "$name / Motor $port", "MOTOR", port) {
        createMotor(name, port, motorFactory(port))
      }
    }

    for (port in 0 until SERVO_PORTS) {
      addSafely(result, "$name / Servo $port", "SERVO", port) {
        createServo(name, port, servoFactory(port))
      }
    }

    for (port in 0 until ANALOG_PORTS) {
      addSafely(result, "$name / Analog $port", "ANALOG", port) {
        createAnalog(name, port, analogFactory(port))
      }
    }

    for (port in 0 until DIGITAL_PORTS) {
      addSafely(result, "$name / Digital $port", "DIGITAL", port) {
        createDigital(name, port, digitalFactory(port))
      }
    }
  }

  private inline fun addSafely(
    result: MutableList<TestDevice>,
    name: String,
    type: String,
    port: Int,
    create: () -> TestDevice
  ) {
    try {
      result.add(create())
    } catch (e: Exception) {
      result.add(
        TestDevice(
          name = name,
          type = type,
          port = port,
          channels = emptyList(),
          readState = {
            "PORT INITIALIZATION ERROR"
          }
        ).apply {
          initializationError =
            "${e.javaClass.simpleName}: ${e.message}"
        }
      )
    }
  }

  private fun deviceOrder(type: String): Int {
    return when (type) {
      "MOTOR" -> 0
      "SERVO" -> 1
      "ANALOG" -> 2
      "DIGITAL" -> 3
      else -> 4
    }
  }

  // ============================================================
  // MOTOR
  // ============================================================

  private fun createMotor(
    hub: String,
    port: Int,
    motor: DcMotorEx
  ): TestDevice {

    var velocityTarget = 0.0

    val channels = listOf(

      ControlChannel(
        id = "motor.power",
        label = "Motor Power",
        unit = "%",
        min = -1.0,
        max = 1.0,
        step = 0.05,
        stickRate = 1.0,
        read = { motor.power },
        write = {
          motor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
          motor.power = it
        },
        display = { "${format(it * 100.0)}%" }
      ),

      ControlChannel(
        id = "motor.position",
        label = "Encoder Target",
        unit = "ticks",
        min = -100000.0,
        max = 100000.0,
        step = 10.0,
        stickRate = 500.0,
        read = {
          if (motor.mode == DcMotor.RunMode.RUN_TO_POSITION) {
            motor.targetPosition.toDouble()
          } else {
            motor.currentPosition.toDouble()
          }
        },
        write = { target ->
          motor.targetPosition = target.roundToInt()
          motor.mode = DcMotor.RunMode.RUN_TO_POSITION

          if (motor.power < 0.01) {
            motor.power = 0.25
          }
        },
        display = { "${it.roundToInt()} ticks" }
      ),

      ControlChannel(
        id = "motor.velocity",
        label = "Velocity Target",
        unit = "ticks/s",
        min = -5000.0,
        max = 5000.0,
        step = 50.0,
        stickRate = 2000.0,
        read = { velocityTarget },
        write = {
          velocityTarget = it
          motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
          motor.velocity = it
        },
        display = { "${format(it)} ticks/s" }
      )
    )

    return TestDevice(
      name = "$hub / Motor $port",
      type = "MOTOR",
      port = port,
      channels = channels,
      readState = {
        buildString {
          appendLine("Power: ${format(motor.power)}")
          appendLine("Encoder: ${motor.currentPosition}")
          appendLine("Target: ${motor.targetPosition}")
          appendLine("Velocity: ${format(motor.velocity)} ticks/s")
          append("Mode: ${motor.mode} | Busy: ${motor.isBusy}")
        }
      },
      stop = {
        motor.power = 0.0
        motor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        velocityTarget = 0.0
      }
    )
  }

  // ============================================================
  // SERVO
  // ============================================================

  private fun createServo(
    hub: String,
    port: Int,
    servo: ServoImplEx
  ): TestDevice {

    fun range() = servo.pwmRange

    fun setRange(lower: Double, upper: Double, frame: Double) {
      require(lower >= PWM_MIN)
      require(upper <= PWM_MAX)
      require(upper - lower >= PWM_GAP)
      require(frame in PWM_FRAME_MIN..PWM_FRAME_MAX)

      servo.pwmRange = PwmControl.PwmRange(
        lower,
        upper,
        frame
      )
    }

    val channels = listOf(

      ControlChannel(
        id = "servo.position",
        label = "Servo Position",
        unit = "",
        min = 0.0,
        max = 1.0,
        step = 0.01,
        stickRate = 0.5,
        read = { servo.position },
        write = { servo.position = it }
      ),

      ControlChannel(
        id = "servo.pulse",
        label = "PWM Pulse Width",
        unit = "us",
        min = PWM_MIN,
        max = PWM_MAX,
        step = 10.0,
        stickRate = 500.0,
        read = {
          val r = range()
          r.usPulseLower +
              servo.position * (r.usPulseUpper - r.usPulseLower)
        },
        write = { pulse ->
          val r = range()
          val width = r.usPulseUpper - r.usPulseLower

          require(width > 0.0)

          val clamped = pulse.coerceIn(
            r.usPulseLower,
            r.usPulseUpper
          )

          servo.position = (clamped - r.usPulseLower) / width
        },
        display = { "${format(it)} us" }
      ),

      ControlChannel(
        id = "servo.pwm.lower",
        label = "PWM Minimum",
        unit = "us",
        min = PWM_MIN,
        max = PWM_MAX - PWM_GAP,
        step = 10.0,
        stickRate = 200.0,
        read = { range().usPulseLower },
        write = { requested ->
          val r = range()
          val lower = requested.coerceIn(
            PWM_MIN,
            min(PWM_MAX - PWM_GAP, r.usPulseUpper - PWM_GAP)
          )
          setRange(lower, r.usPulseUpper, r.usFrame)
        },
        display = { "${format(it)} us" }
      ),

      ControlChannel(
        id = "servo.pwm.upper",
        label = "PWM Maximum",
        unit = "us",
        min = PWM_MIN + PWM_GAP,
        max = PWM_MAX,
        step = 10.0,
        stickRate = 200.0,
        read = { range().usPulseUpper },
        write = { requested ->
          val r = range()
          val upper = requested.coerceIn(
            max(PWM_MIN + PWM_GAP, r.usPulseLower + PWM_GAP),
            PWM_MAX
          )
          setRange(r.usPulseLower, upper, r.usFrame)
        },
        display = { "${format(it)} us" }
      ),

      ControlChannel(
        id = "servo.pwm.frame",
        label = "PWM Frame Period",
        unit = "us",
        min = PWM_FRAME_MIN,
        max = PWM_FRAME_MAX,
        step = 1000.0,
        stickRate = 5000.0,
        read = { range().usFrame },
        write = { frame ->
          val r = range()
          setRange(r.usPulseLower, r.usPulseUpper, frame)
        },
        display = { "${format(it)} us" }
      ),

      ControlChannel(
        id = "servo.pwm.enabled",
        label = "PWM Enabled",
        unit = "",
        min = 0.0,
        max = 1.0,
        step = 1.0,
        read = {
          if (servo.isPwmEnabled()) 1.0 else 0.0
        },
        write = {
          if (it >= 0.5) servo.setPwmEnable()
          else servo.setPwmDisable()
        },
        display = {
          if (it >= 0.5) "ENABLED" else "DISABLED"
        }
      )
    )

    return TestDevice(
      name = "$hub / Servo $port",
      type = "SERVO",
      port = port,
      channels = channels,
      readState = {
        val r = range()

        val pulse = r.usPulseLower +
            servo.position * (r.usPulseUpper - r.usPulseLower)

        buildString {
          appendLine("Position: ${format(servo.position)}")
          appendLine("Pulse: ${format(pulse)} us")
          appendLine(
            "Range: ${format(r.usPulseLower)}-${format(r.usPulseUpper)} us"
          )
          appendLine("Frame: ${format(r.usFrame)} us")
          append("PWM: ${if (servo.isPwmEnabled()) "ON" else "OFF"}")
        }
      }
    )
  }

  // ============================================================
  // ANALOG INPUT
  // ============================================================

  private fun createAnalog(
    hub: String,
    port: Int,
    input: AnalogInput
  ): TestDevice {
    return TestDevice(
      name = "$hub / Analog $port",
      type = "ANALOG",
      port = port,
      channels = emptyList(),
      readState = {
        val voltage = input.voltage
        val maximum = input.maxVoltage

        val fraction = if (maximum > 0.0) {
          voltage / maximum
        } else {
          0.0
        }

        buildString {
          appendLine("Voltage: ${format(voltage)} V")
          appendLine("Maximum: ${format(maximum)} V")
          append("Level: ${format(fraction * 100.0)}%")
        }
      }
    )
  }

  // ============================================================
  // DIGITAL I/O
  // ============================================================

  private fun createDigital(
    hub: String,
    port: Int,
    digital: DigitalChannel
  ): TestDevice {

    val channels = listOf(

      ControlChannel(
        id = "digital.mode",
        label = "Digital Mode",
        unit = "",
        min = 0.0,
        max = 1.0,
        step = 1.0,
        read = {
          if (digital.mode == DigitalChannel.Mode.OUTPUT) 1.0
          else 0.0
        },
        write = {
          digital.mode = if (it >= 0.5) {
            DigitalChannel.Mode.OUTPUT
          } else {
            DigitalChannel.Mode.INPUT
          }
        },
        display = {
          if (it >= 0.5) "OUTPUT" else "INPUT"
        }
      ),

      ControlChannel(
        id = "digital.state",
        label = "Digital State",
        unit = "",
        min = 0.0,
        max = 1.0,
        step = 1.0,
        read = {
          if (digital.state) 1.0 else 0.0
        },
        write = {
          check(digital.mode == DigitalChannel.Mode.OUTPUT) {
            "Set port to OUTPUT before writing"
          }
          digital.state = it >= 0.5
        },
        display = {
          if (it >= 0.5) "HIGH" else "LOW"
        }
      )
    )

    return TestDevice(
      name = "$hub / Digital $port",
      type = "DIGITAL",
      port = port,
      channels = channels,
      readState = {
        "Mode: ${digital.mode} | State: ${
          if (digital.state) "HIGH" else "LOW"
        }"
      }
    )
  }

  // ============================================================
  // SELECTION AND GROUPS
  // ============================================================

  private fun toggleSelection() {
    val device = selected ?: return

    if (selectedDevices.remove(device.name)) {
      lastMessage = "Removed from group"
    } else {
      selectedDevices.add(device.name)
      lastMessage = "Added to group"
    }
  }

  private fun clearSelection() {
    selectedDevices.clear()
    lastMessage = "Group cleared"
  }

  // ============================================================
  // MULTI-DEVICE CONTROLS
  // ============================================================

  private fun adjustTargets(direction: Int) {
    val active = selected?.channel ?: run {
      lastMessage = "No adjustable control"
      return
    }

    var succeeded = 0
    var failed = 0

    targets().forEach { device ->
      val channel = device.channels.firstOrNull {
        it.id == active.id
      }

      if (channel == null) {
        failed++
        return@forEach
      }

      try {
        val value = (
            channel.read() + direction * channel.step
            ).coerceIn(channel.min, channel.max)

        channel.write(value)
        succeeded++
      } catch (e: Exception) {
        failed++
      }
    }

    lastMessage = "Updated $succeeded | Skipped $failed"
  }

  private fun applyStick(deltaTime: Double) {
    val active = selected?.channel ?: return
    if (active.stickRate <= 0.0) return

    val stick = when {
      abs(gamepad1.right_stick_y) > DEADZONE ->
        -gamepad1.right_stick_y.toDouble()

      abs(gamepad2.right_stick_y) > DEADZONE ->
        -gamepad2.right_stick_y.toDouble()

      else -> return
    }

    val change = stick * active.stickRate * deltaTime

    var succeeded = 0
    var failed = 0

    targets().forEach { device ->
      val channel = device.channels.firstOrNull {
        it.id == active.id
      } ?: run {
        failed++
        return@forEach
      }

      try {
        val value = (
            channel.read() + change
            ).coerceIn(channel.min, channel.max)

        channel.write(value)
        succeeded++
      } catch (e: Exception) {
        failed++
      }
    }

    lastMessage = "Joystick: $succeeded updated | $failed skipped"
  }

  // ============================================================
  // STOP
  // ============================================================

  private fun stopTargets() {
    var stopped = 0
    var failed = 0

    targets()
      .filter { it.type == "MOTOR" }
      .forEach { device ->
        try {
          device.stop()
          stopped++
        } catch (e: Exception) {
          failed++
        }
      }

    lastMessage = "Motors stopped: $stopped | Failed: $failed"
  }

  private fun emergencyStop() {
    var stopped = 0
    var failed = 0

    devices.filter { it.type == "MOTOR" }.forEach { device ->
      try {
        device.stop()
        stopped++
      } catch (e: Exception) {
        failed++
      }
    }

    selectedDevices.clear()
    lastMessage = "EMERGENCY STOP: $stopped stopped | $failed failed"
  }

  // ============================================================
  // STATE REFRESH
  // ============================================================

  private fun refreshState(force: Boolean = false) {
    val now = System.nanoTime()

    if (!force && now - lastRefresh < REFRESH_NS) return

    lastRefresh = now

    val device = selected ?: return

    device.state = try {
      device.readState()
    } catch (e: Exception) {
      "READ ERROR: ${e.javaClass.simpleName}: ${e.message}"
    }
  }

  // ============================================================
  // HTML HELPERS
  // ============================================================

  private fun escapeHtml(value: String): String {
    return value
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#39;")
  }

  private fun ui(
    text: String,
    color: String = WHITE
  ) {
    telemetry.addLine(
      "<font color='$color' face=monospace>${escapeHtml(text)}</font>"
    )
  }

  private fun uiRaw(text: String) {
    // Only call with internally generated font markup.
    telemetry.addLine(text)
  }

  private fun typeColor(type: String): String {
    return when (type) {
      "MOTOR" -> BLUE
      "SERVO" -> PURPLE
      "ANALOG" -> GREEN
      "DIGITAL" -> ORANGE
      else -> GRAY
    }
  }

  private fun filterColor(filter: DeviceFilter): String {
    return when (filter) {
      DeviceFilter.ALL -> WHITE
      DeviceFilter.MOTORS -> BLUE
      DeviceFilter.SERVOS -> PURPLE
      DeviceFilter.ANALOG -> GREEN
      DeviceFilter.DIGITAL -> ORANGE
    }
  }

  // ============================================================
  // SELECTION PROGRESS DIAGRAM
  // ============================================================

  private fun progressBar(
    index: Int,
    total: Int,
    width: Int = 12
  ): String {
    if (total <= 1) {
      return "[>...........]"
    }

    val position = (
        index.toDouble() / (total - 1) * (width - 1)
        ).roundToInt().coerceIn(0, width - 1)

    return buildString {
      append('[')

      for (i in 0 until width) {
        append(
          when {
            i < position -> '='
            i == position -> '>'
            else -> '.'
          }
        )
      }

      append(']')
    }
  }

  private fun shortDeviceName(device: TestDevice): String {
    val hub = device.name.substringBefore(" / ")

    val hubTag = when {
      hub == "Control Hub" -> "CH"
      hub == "Expansion Hub" -> "EH"
      hub.startsWith("Servo Hub") ->
        "SH${hub.removePrefix("Servo Hub ").trim()}"
      else -> hub.take(2).uppercase(Locale.US)
    }

    val typeTag = when (device.type) {
      "MOTOR" -> "M"
      "SERVO" -> "S"
      "ANALOG" -> "A"
      "DIGITAL" -> "D"
      else -> "?"
    }

    return "$hubTag-$typeTag${device.port}"
  }

  private fun renderFilterTabs() {
    val line = DeviceFilter.values().joinToString("  ") { filter ->
      val color = if (filter == currentFilter) {
        filterColor(filter)
      } else {
        MUTED
      }

      val label = if (filter == currentFilter) {
        "[${filter.label}]"
      } else {
        filter.label
      }

      "<font color='$color' face=monospace>$label</font>"
    }

    uiRaw(line)
  }

  private fun renderPortMap(device: TestDevice) {
    val hub = device.name.substringBefore(" / ")

    val localPorts = devices
      .filter {
        it.type == device.type &&
            it.name.substringBefore(" / ") == hub
      }
      .sortedBy { it.port }

    if (localPorts.isEmpty()) return

    ui("PORT MAP - $hub", GRAY)

    // Split into rows of four so the map fits on the Driver Station.
    localPorts.chunked(4).forEach { row ->
      val line = row.joinToString("   ") { portDevice ->
        val focused = portDevice.name == device.name
        val marked = portDevice.name in selectedDevices

        val marker = when {
          focused -> ">"
          marked -> "*"
          else -> " "
        }

        val color = when {
          focused -> GREEN
          marked -> PURPLE
          else -> GRAY
        }

        "<font color='$color' face=monospace>${portDevice.port}[$marker]</font>"
      }

      uiRaw(line)
    }

    ui(" > Current port   * Group selected", MUTED)
  }

  private fun renderNearbyDevices() {
    val list = visibleDevices

    if (list.isEmpty()) return

    ui("DEVICE LIST", BLUE)

    val first = (selectedIndex - 2).coerceAtLeast(0)
    val last = (selectedIndex + 2).coerceAtMost(list.lastIndex)

    for (index in first..last) {
      val entry = list[index]

      val marker = when {
        index == selectedIndex -> ">"
        entry.name in selectedDevices -> "*"
        else -> " "
      }

      val color = when {
        index == selectedIndex -> GREEN
        entry.name in selectedDevices -> PURPLE
        else -> GRAY
      }

      ui("$marker ${shortDeviceName(entry)}", color)
    }
  }

  // ============================================================
  // TELEMETRY RENDERING
  // ============================================================

  private fun render() {
    telemetry.clearAll()

    val device = selected ?: return
    val channel = device.channel
    val list = visibleDevices

    val motorCount = devices.count { it.type == "MOTOR" }
    val servoCount = devices.count { it.type == "SERVO" }
    val analogCount = devices.count { it.type == "ANALOG" }
    val digitalCount = devices.count { it.type == "DIGITAL" }

    // --------------------------------------------------------
    // HEADER AND CATEGORY FILTER
    // --------------------------------------------------------

    ui("DEVICE LAB  /  RAW PORT TESTER", BLUE)

    ui(
      "M:${motorCount.toString().padStart(2, '0')}  " +
          "S:${servoCount.toString().padStart(2, '0')}  " +
          "A:${analogCount.toString().padStart(2, '0')}  " +
          "D:${digitalCount.toString().padStart(2, '0')}",
      GRAY
    )

    renderFilterTabs()

    ui("LB/RB CHANGE FILTER", MUTED)

    // --------------------------------------------------------
    // GLOBAL SELECTION PROGRESS
    // --------------------------------------------------------

    ui("--------------------------------", MUTED)

    ui(
      "DEVICE ${selectedIndex + 1} OF ${list.size}",
      BLUE
    )

    ui(
      progressBar(selectedIndex, list.size),
      GREEN
    )

    // --------------------------------------------------------
    // CURRENT DEVICE
    // --------------------------------------------------------

    ui("--------------------------------", MUTED)

    ui(
      device.name,
      typeColor(device.type)
    )

    ui(
      "${device.type}  |  PORT ${device.port}",
      GRAY
    )

    if (device.initializationError != null) {
      ui("PORT ERROR: ${device.initializationError}", RED)
    }

    // --------------------------------------------------------
    // PHYSICAL PORT MAP
    // --------------------------------------------------------

    renderPortMap(device)

    // --------------------------------------------------------
    // ACTIVE CONTROL
    // --------------------------------------------------------

    ui("--------------------------------", MUTED)

    if (channel != null) {
      val value = try {
        channel.read()
      } catch (e: Exception) {
        Double.NaN
      }

      val valueText = if (value.isNaN()) {
        "READ ERROR"
      } else {
        channel.display(value)
      }

      ui(
        "CONTROL ${device.channelIndex + 1}/${device.channels.size}",
        ORANGE
      )

      ui(channel.label, WHITE)

      ui(
        "VALUE: $valueText ${channel.unit}",
        GREEN
      )

      ui(
        "STEP: ${channel.step} ${channel.unit}",
        GRAY
      )

      val compatible = targets().count { target ->
        target.channels.any { it.id == channel.id }
      }

      ui(
        "GROUP: $compatible compatible device(s)",
        PURPLE
      )

      ui("LEFT/RIGHT: ADJUST", BLUE)

      if (channel.stickRate > 0.0) {
        ui("RIGHT STICK: SMOOTH ADJUST", BLUE)
      }
    } else {
      ui("READ ONLY DEVICE", GRAY)
      ui("No output controls for this device.", GRAY)
    }

    // --------------------------------------------------------
    // SHORT CONTROL GUIDE
    // --------------------------------------------------------

    ui("--------------------------------", MUTED)

    ui("UP/DOWN: DEVICE   TRIANGLE: CONTROL", BLUE)
    ui("CROSS: GROUP   CIRCLE: CLEAR GROUP", BLUE)
    ui("SQUARE: STOP MOTORS   START: STOP ALL", RED)

    // --------------------------------------------------------
    // LIVE HARDWARE STATE
    // --------------------------------------------------------

    ui("--------------------------------", MUTED)

    ui("LIVE READINGS", GREEN)

    val readings = device.state
      .lineSequence()
      .filter { it.isNotBlank() }
      .toList()

    if (readings.isEmpty()) {
      ui("No readings available", GRAY)
    } else {
      readings.take(5).forEach {
        ui(it, WHITE)
      }

      if (readings.size > 5) {
        ui("+ ${readings.size - 5} more readings", GRAY)
      }
    }

    // --------------------------------------------------------
    // GROUP SELECTION
    // --------------------------------------------------------

    if (selectedDevices.isNotEmpty()) {
      ui("--------------------------------", MUTED)

      ui(
        "GROUP SELECTION: ${selectedDevices.size}",
        PURPLE
      )

      selectedDevices.take(3).forEach {
        val entry = devices.firstOrNull { device ->
          device.name == it
        }

        if (entry != null) {
          ui(shortDeviceName(entry), PURPLE)
        }
      }

      if (selectedDevices.size > 3) {
        ui("+ ${selectedDevices.size - 3} more", GRAY)
      }
    }

    // --------------------------------------------------------
    // NEARBY DEVICES
    // --------------------------------------------------------

    ui("--------------------------------", MUTED)

    renderNearbyDevices()

    // --------------------------------------------------------
    // STATUS
    // --------------------------------------------------------

    ui("--------------------------------", MUTED)

    val statusColor = when {
      lastMessage.contains("ERROR", ignoreCase = true) -> RED
      lastMessage.contains("FAILED", ignoreCase = true) -> ORANGE
      lastMessage.contains("EMERGENCY", ignoreCase = true) -> RED
      else -> GREEN
    }

    ui(lastMessage, statusColor)
  }

  // ============================================================
  // FORMATTING
  // ============================================================

  private fun format(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
  }
}
