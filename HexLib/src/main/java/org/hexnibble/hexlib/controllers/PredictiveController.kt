package org.hexnibble.hexlib.controllers

/**
 * Class that should be overridden to create season-specific predictive controllers
 * A predictive controller is a controller that determines the inputs of a component such as setting
 * a motor power based on its current state. The controller can do this by modeling the system
 * with physics, incorporating things like friction. Users may want to incorporate a PID controller.
 *
 * @author Benjamin Kang
 */
abstract class PredictiveController(inputState: Number) {
    abstract fun calculateResult(): Number
}