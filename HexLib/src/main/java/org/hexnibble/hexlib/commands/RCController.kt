package org.hexnibble.hexlib.commands

/**
 * ?
 *
 * @author Benjamin Kang
 */
class RCController {
    val busy: Boolean
        get() = toExecuteCommands.length > 0
}