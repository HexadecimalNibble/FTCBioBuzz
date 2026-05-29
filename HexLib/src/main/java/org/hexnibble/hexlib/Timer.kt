package org.hexnibble.hexlib

import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

// TODO: FIX
class Timer(
  private var timeRemaining: Duration,
) {
  private var startTime = 0L

  var isPaused: Boolean = true
    private set

  /**
   * Used to initially start the timer or resume it after being paused
   */
  fun start() {
    if (isPaused) {
      startTime = System.nanoTime()
      isPaused = false
    }
  }

  fun pause() {
    if (!isPaused) {
      updateTimeRemaining(System.nanoTime())
      isPaused = true
    }
  }

  private fun updateTimeRemaining(now: Long) = adjustTimeRemaining((startTime - now).nanoseconds)

  fun adjustTimeRemaining(change: Duration) {
    // Update timeRemaining var before doing calculations
    if (!isPaused) getTimeRemaining()

    try {
      timeRemaining += change
      if (timeRemaining.isNegative()) {
        timeRemaining = Duration.ZERO
      }
    } catch (_: IllegalArgumentException) {
      return
    }
  }

  fun getTimeRemaining(): Duration {
    if (!isPaused) { // if currently running
      val now = System.nanoTime()
      updateTimeRemaining(now)
      startTime = now
    }
    return if (timeRemaining.isNegative()) Duration.ZERO
    else timeRemaining
  }
}