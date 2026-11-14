//package org.firstinspires.ftc.teamcode.opmodes
//
//import com.pedropathing.api.Paths
//import com.pedropathing.api.PoseFactory
//import com.pedropathing.follower.Follower
//import com.pedropathing.ivy.Command
//import com.pedropathing.ivy.commands.Commands.infinite
//import com.pedropathing.ivy.commands.Commands.instant
//import com.pedropathing.ivy.commands.Commands.waitMs
//import com.pedropathing.ivy.commands.Commands.waitUntil
//import com.pedropathing.ivy.groups.Groups.parallel
//import com.pedropathing.ivy.groups.Groups.sequential
//import com.pedropathing.ivy.pedro.PedroCommands.follow
//import com.pedropathing.paths.Path
//import com.pedropathing.paths.interpolator.Interpolator
//import dev.nextftc.units.degrees
//import dev.nextftc.units.milliseconds
//import dev.nextftc.units.rpm
//import org.firstinspires.ftc.teamcode.AllianceColor
//import org.firstinspires.ftc.teamcode.BioBuzzConstants
//import org.firstinspires.ftc.teamcode.robot.BiobuzzRobot
//import org.firstinspires.ftc.teamcode.withTimeout
//
//class HiveDownAuto(
//  val follower: Follower,
//  allianceColor: AllianceColor,
//  val robot: BiobuzzRobot,
//) : AutoProgram {
//
//  private val poseFactory: PoseFactory =
//    if (allianceColor == AllianceColor.Blue) {
//      PoseFactory.degrees().mirrorAroundPoint(70.75, 70.75)
//    } else {
//      PoseFactory.degrees()
//    }
//
//  // cords and paths
//
//  init {
//    // Set starting pose
//    println("Setting pedro starting pose: $start")
//    follower.setPose(start)
//  }
//
//  override fun createAuto(): Command {
//    robot.shooter.setTurretAngle(125.0.degrees)
//
//    return parallel(
//      sequential(
//        instant {
//          robot.shooter.setShooterVelocity(3300.0.rpm)
//        },
//        waitMs(2000.0),
//        robot.autoShoot(),
//        parallel(
//          follow(follower, driveToGarden()),
//          sequential(
//            waitUntil { follower.parametricCompletion() > 0.2 },
//            robot.autoIntake().withTimeout(1000.0.milliseconds),
//          ),
//        ),
//        follow(follower, driveToNonAudienceSideFlower()),
////        follow(follower, driveToHumanPlayerFlower())
//      ),
//      infinite { follower.update() }
//    )
//  }
//}