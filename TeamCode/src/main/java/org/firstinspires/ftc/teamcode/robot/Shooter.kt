//package org.firstinspires.ftc.teamcode.robot
//
//import com.pedropathing.geometry.Pose
//import com.pedropathing.math.Vector
//import com.qualcomm.robotcore.hardware.HardwareMap
//import dev.nextftc.control.KineticState
//import dev.nextftc.control.builder.controlSystem
//import org.hexnibble.hexlib.RobotSystem
//import org.hexnibble.hexlib.hardware.Motor
//import org.hexnibble.hexlib.hardware.MotorType
//
//class Shooter(hwMap: HardwareMap): RobotSystem() {
//  val turretMotor = Motor(hwMap, "TurretMotor", MotorType.GOBILDA_RPM_312, 81.0 / 207.0)
//
//  val shooterMotor1 = Motor(hwMap, "ShooterMotor1", MotorType.GOBILDA_BARE)
//  val shooterMotor2 = Motor(hwMap, "ShooterMotor2", MotorType.GOBILDA_BARE)
//
//  var turretControlSystem = controlSystem {
//    posPid(kP = 0.1, kD = 0.0025)
//  }
//
//  var shooterControlSystem = controlSystem {
//    velPid(kP = 0.007, kD = 0.0)
//  }
//
//  init {
//    turretControlSystem.goal = KineticState(0.0, 0.0, 0.0)
//    shooterControlSystem.goal = KineticState(0.0, 0.0, 0.0)
//    turretMotor.resetEncoder()
//  }
//
//  fun setTurretHeading(targetAngle: Double) {
//    turretControlSystem.goal = KineticState(position = targetAngle)
//  }
//
//  fun setShooterVelocity(velocity: Double) {
//    shooterControlSystem.goal = KineticState(velocity = velocity)
//  }
//
//  fun setPD(p: Double, d: Double) {
//    shooterControlSystem = controlSystem {
//      velPid(kP = p, kD = d)
//    }
//  }
//
//  fun goalAlign(pose: Pose, velocity: Vector) {
//    val goalPose = Pose(140.0, 140.0)
//
//    val turretFieldVector = pose.asVector + Vector(1.5695, pose.heading)
//    val turretFieldPose = Pose(turretFieldVector.xComponent, turretFieldVector.yComponent, pose.heading)
//
//    velocity.rotateVector(-turretFieldPose.heading)
//
//    val shotTime = 0.6
//
//    val goalOffset = Vector(Pose(-velocity.xComponent * shotTime, -velocity.yComponent * shotTime))
//    goalOffset.rotateVector(pose.heading)
//
//    val redGoalPose = Pose(goalPose.x + goalOffset.xComponent, goalPose.y + goalOffset.yComponent)
//
//    val robotToGoalVector = redGoalPose.minus(turretFieldPose).asVector
//
//    val offsetDistance = robotToGoalVector.magnitude
//    var newTargetHdgDegrees = Math.toDegrees(turretFieldPose.heading - robotToGoalVector.theta)
//
//    while (newTargetHdgDegrees < -180) {
//      newTargetHdgDegrees += 360
//    }
//    while (newTargetHdgDegrees > 180) {
//      newTargetHdgDegrees -= 360
//    }
//
//    setTurretHeading(-newTargetHdgDegrees)
//
//    setShooterVelocity(0.52 * offsetDistance + 1460)
//  }
//
//  override fun processCommands() {
//    val turretPower = turretControlSystem.calculate(KineticState(
//      position = turretMotor.getCurrentPositionDeg(),
//      velocity = turretMotor.getCurrentVelocityRPM()
//    ))
//    turretMotor.setPower(turretPower)
//
//    val shooterPower = shooterControlSystem.calculate(KineticState(
//      position = shooterMotor1.getCurrentPosition().toDouble(),
//      velocity = shooterMotor1.getCurrentVelocityRPM()
//    ))
//    shooterMotor1.setPower(shooterPower)
//    shooterMotor2.setPower(-shooterPower)
//  }
//}