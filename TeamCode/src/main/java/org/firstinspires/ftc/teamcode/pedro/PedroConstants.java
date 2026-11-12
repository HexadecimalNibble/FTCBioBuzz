package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
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
    c.xPodOffset.set(2.703127523106853);
    c.yPodOffset.set(0.5665956707451287);
    c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
    c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
    c.globalDistanceUnit.set(DistanceUnit.INCH);
    c.offsetUnits.set(DistanceUnit.INCH);
  });

  public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
    Controller primaryTranslationalForward = Controller.proportional(0.24842924819926);
    Controller secondaryTranslationalForward = Controller.proportional(0.0917879690578581);
    Controller primaryTranslationalLateral = Controller.proportional(0.294175189947573);
    Controller secondaryTranslationalLateral = Controller.proportional(0.10868987217978383);

    c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
    c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

    c.coast.set(Controller.proportionalFeedforward(0.011154563627894364));
    c.brake.set(Controller.proportionalFeedforward(0.009481379083710209));

    c.headingFeedback.set(Controller.proportional(3.784040308219561));
    c.headingBrakeCoefficients.set(Vector2D.cartesian(0.016565706627917814, 0.01312675913483693));

    c.linearBrakeCoefficients.set(Matrix.diag(0.10952332896629176, 0.04521185582241415));
    c.quadraticBrakeCoefficients.set(Matrix.diag(0.001105647051444757, 0.0022912528363498246));

    c.maxAchievableForwardVelocity.set(82.75065305837963);
    c.maxAchievableStrafeVelocity.set(68.01791789460792);
    c.naturalForwardDeceleration.set(20.936167554932727);
    c.naturalStrafeDeceleration.set(52.38600640849694);
  });

  public static Follower create(HardwareMap h) {
    return new Follower(new PinpointLocalizer(h, localizerConfig), new Mecanum(h, drivetrainConfig), new Foresight(foresightConfig));
  }
}