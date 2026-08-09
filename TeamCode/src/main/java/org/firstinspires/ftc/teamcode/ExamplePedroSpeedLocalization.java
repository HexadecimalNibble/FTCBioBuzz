package org.firstinspires.ftc.teamcode;

import static com.qualcomm.robotcore.hardware.configuration.LynxConstants.EXPANSION_HUB_PRODUCT_NUMBER;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedroPathing.PedroConstants;

import java.lang.reflect.Field;

import dev.anygeneric.blazeftc.DummyPlugOpMode;
import dev.anygeneric.blazeftc_pedro.PedroSingleDataLocalizer;

@TeleOp(name = "Example Pedro High Speed Localization")
public class ExamplePedroSpeedLocalization extends DummyPlugOpMode {
  @Override
  public void runOpModeInBlaze() {
    //this does several important things internally. You *must* call it before anything else.
    //You can pass whatever telemetry object in you want, including the split ones that go to a web dashboard.
    //However, you need to use the object it returns, and you should under no circumstances replace it with
    //`telemetry = initializeBlazeFTC(telemetry);` which replaces the OpMode's telemetry and breaks everything.
    //Feel free to try and fix this, but it's out of scope for me, sorry.
    Telemetry tele = initializeBlazeFTC(telemetry);
    //Normal manual cache setup.
    for (LynxModule i : hardwareMap.getAll(LynxModule.class))
      i.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
    //this sends motor cmds directly to blaze, skipping Java serialization completely
    //it reaches into the hwMap to replace the motors there so don't pull the motors out/init pedro before calling it
//    engageMotorAcceleration();
    //we create the follower. NOTE that this uses the pinpoint java driver to set all your settings and offsets
    Follower follower = PedroConstants.createFollower(hardwareMap);
    waitForStart();
    ElapsedTime elt = new ElapsedTime();
    //the closure you pass will be called every time we get new data.
    //you may call `setup` at any time during the opmode, but it *must* be called before runBlazeFTC(0);
    //if you call it later, it will be ignored.
    PedroSingleDataLocalizer.setup(follower, () -> {
      tele.addData("pedro loop time (ms)", elt.milliseconds());
      elt.reset();
      follower.update();
      tele.addData("x,y", follower.getPose().getX() + ", " + follower.getPose().getY());
    });
    //this is a test path. Replace it with your team's logic
//    follower.followPath(new Path(new BezierLine(new Pose(0, 0), new Pose(10, 0))));
    follower.startTeleOpDrive(true);

    //this turns control over to Blaze. The 0 tells blaze to use Neutrino, not a different rust opmode.
    //If you wrote other rust opmodes, you would start them instead by passing in a different number.
    //You absolutely have to call this some time after waitForStart

    ElapsedTime e1 = new ElapsedTime();
    engageBulkReadAcceleration(true, 1, (byteArray) -> {
      tele.addData("bulk read loop time (ms)", e1.milliseconds());
      e1.reset();
      try {
        for (LynxModule i : hardwareMap.getAll(LynxModule.class)) {
          Field bulkData = LynxModule.class.getDeclaredField("lastBulkData");
          bulkData.setAccessible(true);
          LynxModule.BulkData b = (LynxModule.BulkData) bulkData.get(i);
          if (b != null) {
            System.out.println(i.hashCode() + b.toString() + "; shooter motor pos: " + b.getMotorCurrentPosition(1) + "; Intake loader: " + b.getAnalogInputVoltage(0));
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      telemetry.update();

      follower.setTeleOpDrive(-gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_trigger + gamepad1.left_trigger, true);

      return null;
    });

    runBlazeFTC(0);

    //This should be replaced with your own code.
    ElapsedTime elt2 = new ElapsedTime();
    follower.setPose(new Pose(72.0, 72.0, 0.0));

    LynxModule exHub = null;
    for (LynxModule l : hardwareMap.getAll(LynxModule.class)) {
      if (!l.isParent() && l.getRevProductNumber() == EXPANSION_HUB_PRODUCT_NUMBER) {
        exHub = l;
        break;
      }
    }

    while (!isStopRequested()) {
//      for (LynxModule i : hardwareMap.getAll(LynxModule.class)) i.clearBulkCache();

      sleep(5);
//      tele.addData("turret pos", shooter.getTurretMotor().getCurrentPositionDeg());
//      tele.addData("shooter vel", shooter.getShooterMotor1().getCurrentVelocityRPM());
//      tele.addData("shooter target vel", shooter.getShooterControlSystem().getGoal().getVelocity());
//      tele.addData("main loop time (ms)", elt2.milliseconds());
//      tele.addData("exhub voltage", exHub.getInputVoltage(VoltageUnit.VOLTS));
      elt2.reset();
//      tele.update();
    }
  }
}