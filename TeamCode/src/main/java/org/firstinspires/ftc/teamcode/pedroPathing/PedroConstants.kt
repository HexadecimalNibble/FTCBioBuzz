package org.firstinspires.ftc.teamcode.pedroPathing

import com.pedropathing.control.PIDFCoefficients
import com.pedropathing.control.PredictiveBrakingCoefficients
import com.pedropathing.follower.Follower
import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.constants.PinpointConstants
import com.pedropathing.paths.PathConstraints
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

object PedroConstants {
  val mecanumConstants = MecanumConstants()
    .maxPower(1.0)
    .leftFrontMotorName("LFMotor")
    .leftRearMotorName("LBMotor")
    .rightFrontMotorName("RFMotor")
    .rightRearMotorName("RBMotor")
    .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
    .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
    .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
    .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
    .xVelocity(75.16)
    .yVelocity(55.16)
    .useBrakeModeInTeleOp(true)

  val localizerConstants = PinpointConstants()
    .forwardPodY(88.0)
    .strafePodX(-162.05)
    .distanceUnit(DistanceUnit.MM)
    .hardwareMapName("pinpoint")
    .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)
    .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
    .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)

  val followerConstants: FollowerConstants = FollowerConstants()
    .mass(17.0)
    .predictiveBrakingCoefficients(PredictiveBrakingCoefficients(0.3, 0.12, 0.00156)
      .withMaximumBrakingPower(0.5))
    .forwardZeroPowerAcceleration(-28.203)
    .lateralZeroPowerAcceleration(-64.166)
    .useSecondaryDrivePIDF(true)
    .useSecondaryTranslationalPIDF(true)
    .useSecondaryHeadingPIDF(true)
    .headingPIDFCoefficients(PIDFCoefficients(2.5, 0.0, 0.1, 0.2))
    .secondaryHeadingPIDFCoefficients(PIDFCoefficients(2.0, 0.0, 0.25, 0.05))
    .centripetalScaling(0.0)

  val pathConstraints = PathConstraints(
    0.95, // t-value
    0.1, // velocity
    0.1, // translational
    0.007, // heading
    100.0, // timeout
    1.0, // breaking strength
    10, // BEZIER_CURVE_SEARCH_LIMIT
    1.0 // brakingStart
  )

  @JvmStatic
  fun createFollower(hardwareMap: HardwareMap?): Follower {
    return FollowerBuilder(followerConstants, hardwareMap)
      .mecanumDrivetrain(mecanumConstants)
      .pinpointLocalizer(localizerConstants)
      .pathConstraints(pathConstraints)
      .build()
  }
}