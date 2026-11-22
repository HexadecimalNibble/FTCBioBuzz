package org.firstinspires.ftc.teamcode.pedro.procedures

import com.pedropathing.drivetrain.DrivePowers
import com.pedropathing.drivetrain.Drivetrain
import com.pedropathing.follower.Follower
import com.pedropathing.localization.Localizer
import com.pedropathing.math.Pose
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.pedro.PedroConstants
import kotlin.math.abs

/**
 * Measures powered acceleration in either the robot's forward (+X) or
 * strafe-left (+Y) direction.
 *
 * During INIT, use D-pad LEFT to select strafe or D-pad RIGHT to select forward.
 * Press START to run the selected test.
 *
 * Acceleration is calculated from successive body-frame velocity samples and
 * System.nanoTime() timestamps, following the approach in the PedroPathing
 * ForwardDeceleration and StrafeDeceleration tuning procedures.
 */
@TeleOp(name = "Acceleration Measurement", group = "Tuning")
class AccelerationMeasurementOpMode : LinearOpMode() {

    companion object {
        private const val TEST_POWER = 1.0
        private const val TEST_DURATION_SECONDS = 1.5

        private const val MIN_SAMPLE_DT_SECONDS = 0.0001
        private const val MAX_SAMPLE_DT_SECONDS = 0.100
        private const val TELEMETRY_INTERVAL_NANOS = 100_000_000L
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

            val result = measureAcceleration(localizer, drivetrain, selectedAxis)

            telemetry.clearAll()
            telemetry.addLine("Acceleration results (in/s^2)")
            telemetry.addData("Selected direction", selectedAxis.label)
            addResultToTelemetry(result)
            telemetry.addLine("Signed acceleration is positive in the selected direction.")
            telemetry.update()

            // Keep the result visible until STOP is pressed.
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
        axis: Axis
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

        telemetry.clearAll()
        telemetry.addData("Test", axis.label)
        telemetry.addLine("Accelerating...")
        telemetry.update()

        // Drive directly, as in the supplied PedroPathing tuning procedures.
        // Do not call follower.update(), which would run the follower's drive state machine.
        drivetrain.drive(powers, false)

        val startTimeNano = System.nanoTime()
        var previousTimeNano = startTimeNano
        var lastTelemetryNano = startTimeNano
        var currentTimeNano = startTimeNano

        try {
            while (opModeIsActive()) {
                localizer.update()

                val currentVelocity = componentVelocity(localizer, axis)
                currentTimeNano = System.nanoTime()
                val dt = (currentTimeNano - previousTimeNano) / 1e9
                val elapsedSeconds = (currentTimeNano - startTimeNano) / 1e9

                if (dt in MIN_SAMPLE_DT_SECONDS..MAX_SAMPLE_DT_SECONDS) {
                    val acceleration = (currentVelocity - previousVelocity) / dt
                    accelerations.add(acceleration)
                }

                previousVelocity = currentVelocity
                previousTimeNano = currentTimeNano
                endVelocity = currentVelocity

                if (currentTimeNano - lastTelemetryNano >= TELEMETRY_INTERVAL_NANOS) {
                    telemetry.addData("Test", axis.label)
                    telemetry.addData(
                        "Elapsed",
                        "%.2f / %.2f s",
                        elapsedSeconds,
                        TEST_DURATION_SECONDS
                    )
                    telemetry.addData("Velocity", "%.2f in/s", currentVelocity)
                    telemetry.addData("Acceleration samples", accelerations.size)
                    telemetry.update()
                    lastTelemetryNano = currentTimeNano
                }

                if (elapsedSeconds >= TEST_DURATION_SECONDS) break
                idle()
            }
        } finally {
            // Stop applying power when the timed measurement ends.
            drivetrain.stop(false)
        }

        val elapsedSeconds = ((currentTimeNano - startTimeNano) / 1e9).coerceAtLeast(1e-6)

        if (accelerations.isEmpty()) {
            return AccelerationResult(
                averageSignedAcceleration = 0.0,
                averageAbsoluteAcceleration = 0.0,
                peakAbsoluteAcceleration = 0.0,
                netAcceleration = (endVelocity - startVelocity) / elapsedSeconds,
                startVelocity = startVelocity,
                endVelocity = endVelocity,
                sampleCount = 0
            )
        }

        return AccelerationResult(
            averageSignedAcceleration = accelerations.average(),
            averageAbsoluteAcceleration = accelerations.map { abs(it) }.average(),
            peakAbsoluteAcceleration = accelerations.maxOf { abs(it) },
            netAcceleration = (endVelocity - startVelocity) / elapsedSeconds,
            startVelocity = startVelocity,
            endVelocity = endVelocity,
            sampleCount = accelerations.size
        )
    }

    private fun componentVelocity(localizer: Localizer, axis: Axis): Double {
        val bodyVelocity = localizer.twist().toVector2D()
        return when (axis) {
            Axis.FORWARD -> bodyVelocity.x()
            Axis.STRAFE -> bodyVelocity.y()
        }
    }

    private fun addResultToTelemetry(result: AccelerationResult) {
        telemetry.addData(
            "Mean signed acceleration",
            "%.2f in/s^2",
            result.averageSignedAcceleration
        )
        telemetry.addData(
            "Mean |acceleration|",
            "%.2f in/s^2",
            result.averageAbsoluteAcceleration
        )
        telemetry.addData(
            "Peak |acceleration|",
            "%.2f in/s^2",
            result.peakAbsoluteAcceleration
        )
        telemetry.addData("Delta-v / time", "%.2f in/s^2", result.netAcceleration)
        telemetry.addData(
            "Velocity",
            "%.2f -> %.2f in/s (%d samples)",
            result.startVelocity,
            result.endVelocity,
            result.sampleCount
        )
    }
}
