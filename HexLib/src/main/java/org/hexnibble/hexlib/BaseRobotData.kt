package org.hexnibble.hexlib

import com.pedropathing.geometry.Pose

/**
 * Parent class for singleton `RobotData` object in TeamCode that stores persistent OpMode data
 * This parent class has variables to store data that should always be persistent across OpModes
 *
 * @author Benjamin Kang
 */
abstract class BaseRobotData {
    var robotPosition: Pose? = null
}