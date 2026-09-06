package org.firstinspires.ftc.teamcode

import org.firstinspires.ftc.teamcode.opmodes.RobotAuto
import com.pedropathing.geometry.Pose

/**
 * Singleton to store robot data that should be persistent between OpMode runs
 *
 * @author Benjamin Kang
 */
object RobotData {
    var allianceColor: AllianceColor? = null
    var auto: RobotAuto? = null

    var pose: Pose? = null
}