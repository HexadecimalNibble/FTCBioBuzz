package org.firstinspires.ftc.teamcode

/**
 * Singleton to store robot data that should be persistent between OpMode runs
 *
 * @author Benjamin Kang
 */
object RobotData {
    var value: Int? = null
    var allianceColor: AllianceColor? = null
}