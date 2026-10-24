package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class PedroConstants {
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("LFMotor");
        c.frontRightName.set("RFMotor");
        c.backLeftName.set("LBMotor");
        c.backRightName.set("RBMotor");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(2.3); // TODO: CALCULATE BETTER POD OFFSETS LATER FROM CAD
        c.yPodOffset.set(-5.2);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(
        c -> {
            Controller primaryTranslationalForward = Controller.proportional(0.27609702412862436);
            Controller secondaryTranslationalForward = Controller.proportional(0.10201047296716959);
            Controller primaryTranslationalLateral = Controller.proportional(0.3948318817493024);
            Controller secondaryTranslationalLateral = Controller.proportional(0.14587983020418288);

            c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
            c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

            c.coast.set(Controller.proportionalFeedforward(0.012069986114700762));
            c.brake.set(Controller.proportionalFeedforward(0.010259488197495647));

            c.headingFeedback.set(Controller.proportional(4.789447423379754));
            c.headingBrakeCoefficients.set(Vector2D.cartesian(0.05007495875576989, 0.005754055312643777));

            c.linearBrakeCoefficients.set(Matrix.diag(0.0960879159806794, 0.056484381760295674));
            c.quadraticBrakeCoefficients.set(Matrix.diag(0.0011496017128293761, 0.0017778987123169227));

            c.maxAchievableForwardVelocity.set(78.37491921931237);
            c.maxAchievableStrafeVelocity.set(67.71163640297846);
            c.naturalForwardDeceleration.set(37.17458380790165);
            c.naturalStrafeDeceleration.set(62.78021283643583);
        }
    );

    public static Follower create(HardwareMap h) {
        // return new Follower(Drivetrain, Localizer, Foresight);
        return null;
    }
}