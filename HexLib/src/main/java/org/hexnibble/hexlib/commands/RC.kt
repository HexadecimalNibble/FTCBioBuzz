package org.hexnibble.hexlib.commands

import kotlin.time.Duration

abstract class RC(val commandName: String = "RC", var maxCommandDuration: Duration) {
  private val tag = "RC"

  val requirements: MutableSet<Any> = HashSet()
  var priority: Byte = 0

  var blockedBehavior = Behavior.Blocked.Cancel
  var conflictBehavior = Behavior.Conflict.Override
  var interruptedBehavior = Behavior.Interrupted.End

  val commandDurationTimer:
}

class Behavior {
  // Behavior when running command has higher priority than newly scheduled command
  enum class Blocked {
    Cancel,
    Queue,
  }

  // Behavior when running command has equal priority to newly scheduled command
  enum class Conflict {
    Cancel,
    Override,
    Queue,
  }

  // Behavior for running command when newly scheduled command has higher priority
  enum class Interrupted {
    End,
    Suspend,
  }
}

enum class EndStatus {
  Completed, // Command successfully completed
  Interrupted, // Command interrupted
  Suspended, // Command suspended and can be resumed at a future time
}