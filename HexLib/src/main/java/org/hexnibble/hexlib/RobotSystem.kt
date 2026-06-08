package org.hexnibble.hexlib

abstract class RobotSystem {
  init {
    robotSystems.add(this)
  }

  abstract fun processCommands()

  companion object {
    val robotSystems = arrayListOf<RobotSystem>()
  }
}