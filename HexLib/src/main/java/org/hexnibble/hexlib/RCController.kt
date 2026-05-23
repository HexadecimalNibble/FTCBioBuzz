package org.hexnibble.hexlib

class RCController {
    val busy: Boolean
        get() = toExecuteCommands.length > 0
}