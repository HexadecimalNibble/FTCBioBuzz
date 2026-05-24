package org.hexnibble.hexlib

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class Timer(initialDuration: Duration) {
  // Keep track of the current total configuration
  var totalDuration: Duration = initialDuration
    private set

  private var durationRemaining = initialDuration
  private var lastStartMark: TimeMark? = null

  val isPaused: Boolean
    get() = lastStartMark == null

  fun start() {
    if (!isPaused) return
    lastStartMark = TimeSource.Monotonic.markNow()
  }

  fun pause() {
    val startMark = lastStartMark ?: return
    durationRemaining = (durationRemaining - startMark.elapsedNow()).coerceAtLeast(Duration.ZERO)
    lastStartMark = null
  }

  fun getRemainingTime(): Duration {
    val startMark = lastStartMark ?: return durationRemaining
    return (durationRemaining - startMark.elapsedNow()).coerceAtLeast(Duration.ZERO)
  }

  /**
   * Updates the total duration.
   * @param newDuration The new total time.
   * @param resetIfRunning If true, the timer resets completely to the new time.
   * If false, it dynamically adjusts the remaining time by the difference.
   */
  fun updateDuration(newDuration: Duration, resetIfRunning: Boolean = false) {
    if (resetIfRunning) {
      totalDuration = newDuration
      reset()
    } else {
      // Calculate the difference between old and new duration
      val difference = newDuration - totalDuration
      totalDuration = newDuration
      // Adjust the remaining time by that difference
      durationRemaining = (durationRemaining + difference).coerceAtLeast(Duration.ZERO)
    }
  }

  fun reset() {
    lastStartMark = null
    durationRemaining = totalDuration
  }
}