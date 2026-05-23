package org.firstinspires.ftc.teamcode

import org.hexnibble.hexlib.RobotData

/**
 * Singleton to store robot data that should be persistent between OpModes
 *
 * @author Benjamin Kang
 */
object RobotData : RobotData() {
    var value: Int? = null
}