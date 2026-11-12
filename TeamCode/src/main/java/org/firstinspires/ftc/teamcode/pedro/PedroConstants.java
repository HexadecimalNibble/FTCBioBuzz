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
    c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
    c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
    c.globalDistanceUnit.set(DistanceUnit.INCH);
    c.offsetUnits.set(DistanceUnit.INCH);
  });

  public static ForesightConfig foresightConfig = new ForesightConfig(
      c -> {
        Controller primaryTranslationalForward = Controller.proportional(0.23146581635583954);
        Controller secondaryTranslationalForward = Controller.proportional(0.08552043426296112);
        Controller primaryTranslationalLateral = Controller.proportional(0.2873742798082907);
        Controller secondaryTranslationalLateral = Controller.proportional(0.10617711760698477);

        c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
        c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

        c.coast.set(Controller.proportionalFeedforward(0.011619762987512458));
        c.brake.set(Controller.proportionalFeedforward(0.00987679853938559));

        c.headingFeedback.set(Controller.proportional(3.5794801980736644));
        c.headingBrakeCoefficients.set(Vector2D.cartesian(0.023593325693593965, 0.011811599300684057));

        c.linearBrakeCoefficients.set(Matrix.diag(0.07517376464816927, 0.03147821428888475));
        c.quadraticBrakeCoefficients.set(Matrix.diag(0.002062426834583176, 0.002476571084943919));

        c.maxAchievableForwardVelocity.set(82.11643127807221);
        c.maxAchievableStrafeVelocity.set(66.56272146391163);
        c.naturalForwardDeceleration.set(20.09351875966161);
        c.naturalStrafeDeceleration.set(51.66804366986948);
      }
  );

  public static Follower create(HardwareMap h) {
    return new Follower(new PinpointLocalizer(h, localizerConfig), new Mecanum(h, drivetrainConfig), new Foresight(foresightConfig));
  }
}