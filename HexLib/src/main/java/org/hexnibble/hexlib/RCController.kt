package org.hexnibble.hexlib

/**
 * ?
 *
 * @author Benjamin Kang
 */
class RCController {
    val busy: Boolean
        get() = toExecuteCommands.length > 0
}