package org.firstinspires.ftc.teamcode.pedro.procedures

import com.pedropathing.drivetrain.DrivePowers
import com.pedropathing.drivetrain.Drivetrain
import com.pedropathing.follower.Follower
import com.pedropathing.localization.Localizer
import com.pedropathing.math.Pose
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.internal.system.AppUtil
import org.firstinspires.ftc.teamcode.pedro.PedroConstants
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Measures powered acceleration in either the robot's forward (+X) or
 * strafe-left (+Y) direction.
 *
 * During INIT, use D-pad LEFT to select strafe or D-pad RIGHT to select forward.
 * Press START to run the selected test.
 *
 * Each sample records Localizer.debug(), velocity, and calculated acceleration
 * to a timestamped CSV file in the FTC Robot Controller data directory.
 */
@TeleOp(name = "Acceleration Measurement", group = "Tuning")
class AccelerationMeasurementOpMode : LinearOpMode() {

    companion object {
        private const val TEST_POWER = 1.0
        private const val TEST_DURATION_SECONDS = 1.5

        private const val MIN_SAMPLE_DT_SECONDS = 0.0001
        private const val MAX_SAMPLE_DT_SECONDS = 0.100
    }

    private data class AccelerationResult(
        val averageSignedAcceleration: Double,
        val averageAbsoluteAcceleration: Double,
        val peakAbsoluteAcceleration: Double,
        val netAcceleration: Double,
        val startVelocity: Double,
        val endVelocity: Double,
        val sampleCount: Int
    )

    private enum class Axis(val label: String) {
        FORWARD("Forward (+X)"),
        STRAFE("Strafe left (+Y)")
    }

    override fun runOpMode() {
        val follower: Follower = PedroConstants.create(hardwareMap)
        val localizer: Localizer = follower.localizer
        val drivetrain: Drivetrain = follower.drivetrain

        try {
            var selectedAxis = Axis.FORWARD
            var previousDpadLeft = false
            var previousDpadRight = false

            // Direction selection takes place during INIT, before START.
            while (!isStarted && !isStopRequested) {
                val dpadLeft = gamepad1.dpad_left
                val dpadRight = gamepad1.dpad_right

                // Rising-edge checks prevent a held button from repeatedly changing selection.
                if (dpadLeft && !previousDpadLeft) {
                    selectedAxis = Axis.STRAFE
                }
                if (dpadRight && !previousDpadRight) {
                    selectedAxis = Axis.FORWARD
                }

                previousDpadLeft = dpadLeft
                previousDpadRight = dpadRight

                telemetry.clearAll()
                telemetry.addLine("Acceleration Measurement")
                telemetry.addLine("Select the test direction before pressing START:")
                telemetry.addLine("D-pad LEFT: Strafe left (+Y)")
                telemetry.addLine("D-pad RIGHT: Forward (+X)")
                telemetry.addData("Selected direction", selectedAxis.label)
                telemetry.addData("Power", "%.0f%%", TEST_POWER * 100.0)
                telemetry.addData("Powered duration", "%.2f s", TEST_DURATION_SECONDS)
                telemetry.addLine("Ensure there is sufficient clear space in the selected direction.")
                telemetry.update()
                idle()
            }

            if (isStopRequested) return

            waitForStart()
            if (!opModeIsActive()) return

            localizer.setPose(Pose.zero())
            localizer.update()

            val logFile = try {
                createLogFile()
            } catch (exception: IOException) {
                telemetry.clearAll()
                telemetry.addLine("Could not create acceleration log file:")
                telemetry.addLine(exception.message ?: exception.javaClass.simpleName)
                telemetry.update()
                return
            }

            BufferedWriter(FileWriter(logFile, false)).use { writer ->
                writeCsvRow(
                    writer,
                    listOf(
                        "record_type",
                        "sample",
                        "elapsed_seconds",
                        "dt_seconds",
                        "axis",
                        "velocity_inches_per_second",
                        "acceleration_inches_per_second_squared",
                        "average_signed_acceleration",
                        "average_absolute_acceleration",
                        "peak_absolute_acceleration",
                        "net_acceleration",
                        "start_velocity",
                        "end_velocity",
                        "valid_acceleration_samples",
                        "localizer_debug"
                    )
                )

                writeCsvRow(
                    writer,
                    listOf(
                        "metadata", "", "", "", selectedAxis.label,
                        "", "", "", "", "", "", "", "", "",
                        formatDebugMap(localizer.debug())
                    )
                )
                writer.flush()

                val result = measureAcceleration(localizer, drivetrain, selectedAxis, writer)
                writeCsvRow(
                    writer,
                    listOf(
                        "summary", "", "", "", selectedAxis.label,
                        "", "",
                        formatNumber(result.averageSignedAcceleration),
                        formatNumber(result.averageAbsoluteAcceleration),
                        formatNumber(result.peakAbsoluteAcceleration),
                        formatNumber(result.netAcceleration),
                        formatNumber(result.startVelocity),
                        formatNumber(result.endVelocity),
                        result.sampleCount.toString(),
                        formatDebugMap(localizer.debug())
                    )
                )
                writer.flush()
            }

            // The only post-test telemetry is the file location; measured samples/results are in the CSV.
            telemetry.clearAll()
            telemetry.addLine("Acceleration measurement complete.")
            telemetry.addData("Direction", selectedAxis.label)
            telemetry.addData("Samples", "Saved to CSV")
            telemetry.addData("Log file", logFile.name)
            telemetry.addData("Directory", logFile.parent)
            telemetry.update()

            while (opModeIsActive()) {
                idle()
            }
        } finally {
            // Always stop the drivetrain, including if STOP is pressed during measurement.
            drivetrain.stop(false)
        }
    }

    private fun measureAcceleration(
        localizer: Localizer,
        drivetrain: Drivetrain,
        axis: Axis,
        writer: BufferedWriter
    ): AccelerationResult {
        val accelerations = ArrayList<Double>()

        localizer.update()
        val startVelocity = componentVelocity(localizer, axis)
        var previousVelocity = startVelocity
        var endVelocity = startVelocity

        val powers = when (axis) {
            Axis.FORWARD -> DrivePowers(TEST_POWER, 0.0, 0.0)
            Axis.STRAFE -> DrivePowers(0.0, TEST_POWER, 0.0)
        }

        // Drive directly, as in the supplied PedroPathing tuning procedures.
        // Do not call follower.update(), which would run the follower's drive state machine.
        drivetrain.drive(powers, false)

        val startTimeNano = System.nanoTime()
        var previousTimeNano = startTimeNano
        var currentTimeNano = startTimeNano
        var sampleNumber = 0

        try {
            while (opModeIsActive()) {
                localizer.update()

                val currentVelocity = componentVelocity(localizer, axis)
                currentTimeNano = System.nanoTime()
                val dt = (currentTimeNano - previousTimeNano) / 1e9
                val elapsedSeconds = (currentTimeNano - startTimeNano) / 1e9

                val acceleration = if (dt in MIN_SAMPLE_DT_SECONDS..MAX_SAMPLE_DT_SECONDS) {
                    ((currentVelocity - previousVelocity) / dt).also { accelerations.add(it) }
                } else {
                    null
                }

                sampleNumber++
                // Capture the Localizer's debug map after its update for this exact sample.
                val localizerDebug = formatDebugMap(localizer.debug())
                writeCsvRow(
                    writer,
                    listOf(
                        "sample",
                        sampleNumber.toString(),
                        formatNumber(elapsedSeconds),
                        formatNumber(dt),
                        axis.label,
                        formatNumber(currentVelocity),
                        acceleration?.let { formatNumber(it) } ?: "",
                        "", "", "", "", "", "", "",
                        localizerDebug
                    )
                )

                previousVelocity = currentVelocity
                previousTimeNano = currentTimeNano
                endVelocity = currentVelocity

                if (elapsedSeconds >= TEST_DURATION_SECONDS) break
                idle()
            }
        } finally {
            // Stop applying power when the timed measurement ends or STOP is pressed.
            drivetrain.stop(false)
            writer.flush()
        }

        val elapsedSeconds = ((currentTimeNano - startTimeNano) / 1e9).coerceAtLeast(1e-6)

        return if (accelerations.isEmpty()) {
            AccelerationResult(
                averageSignedAcceleration = 0.0,
                averageAbsoluteAcceleration = 0.0,
                peakAbsoluteAcceleration = 0.0,
                netAcceleration = (endVelocity - startVelocity) / elapsedSeconds,
                startVelocity = startVelocity,
                endVelocity = endVelocity,
                sampleCount = 0
            )
        } else {
            AccelerationResult(
                averageSignedAcceleration = accelerations.average(),
                averageAbsoluteAcceleration = accelerations.map { abs(it) }.average(),
                peakAbsoluteAcceleration = accelerations.maxOf { abs(it) },
                netAcceleration = (endVelocity - startVelocity) / elapsedSeconds,
                startVelocity = startVelocity,
                endVelocity = endVelocity,
                sampleCount = accelerations.size
            )
        }
    }

    private fun componentVelocity(localizer: Localizer, axis: Axis): Double {
        val bodyVelocity = localizer.twist().toVector2D()
        return when (axis) {
            Axis.FORWARD -> bodyVelocity.x()
            Axis.STRAFE -> bodyVelocity.y()
        }
    }

    /** Creates a unique CSV file in the FTC SDK's persistent Robot Controller data directory. */
    @Throws(IOException::class)
    private fun createLogFile(): File {
        val directory = AppUtil.ROBOT_DATA_DIR
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Unable to create directory: ${directory.absolutePath}")
        }
        if (!directory.isDirectory) {
            throw IOException("Robot data path is not a directory: ${directory.absolutePath}")
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        return File.createTempFile("Acceleration_${timestamp}_", ".csv", directory)
    }

    /** Writes one correctly escaped CSV row. */
    private fun writeCsvRow(writer: BufferedWriter, fields: List<String>) {
        writer.write(fields.joinToString(",") { csvEscape(it) })
        writer.newLine()
    }

    private fun csvEscape(value: String): String {
        return "\"${value.replace("\"", "\"\"")}\""
    }

    private fun formatDebugMap(debug: Map<String, Any>): String {
        return debug.entries
            .sortedBy { it.key }
            .joinToString("; ") { entry -> "${entry.key}=${entry.value}" }
    }

    private fun formatNumber(value: Double): String {
        return String.format(Locale.US, "%.9f", value)
    }
}
