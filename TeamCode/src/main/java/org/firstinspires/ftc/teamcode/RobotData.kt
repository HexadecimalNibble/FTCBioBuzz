package org.firstinspires.ftc.teamcode

import org.hexnibble.hexlib.BaseRobotData

/**
 * Singleton to store robot data that should be persistent between OpMode runs
 *
 * @author Benjamin Kang
 */
object RobotData : BaseRobotData() {
    var value: Int? = null
}