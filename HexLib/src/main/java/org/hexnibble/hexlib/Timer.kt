package org.hexnibble.hexlib

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

// Accept an optional timeSource that defaults to the Monotonic standard
class Timer(
  var totalDuration: Duration,
  private val timeSource: TimeSource = TimeSource.Monotonic
) {
  var isPaused: Boolean = true
    private set

  private var durationRemaining: Duration = totalDuration
  private var lastStartMark: TimeMark? = null

  fun start() {
    if (!isPaused) return
    isPaused = false
    // Use our injected timeSource
    lastStartMark = timeSource.markNow()
  }

  fun pause() {
    if (isPaused) return
    val startMark = lastStartMark ?: return
    durationRemaining = (durationRemaining - startMark.elapsedNow()).coerceAtLeast(Duration.ZERO)
    isPaused = true
    lastStartMark = null
  }

  fun reset() {
    isPaused = true
    durationRemaining = totalDuration
    lastStartMark = null
  }

  fun getRemainingTime(): Duration {
    if (isPaused) return durationRemaining
    val startMark = lastStartMark ?: return durationRemaining
    return (durationRemaining - startMark.elapsedNow()).coerceAtLeast(Duration.ZERO)
  }

  fun updateDuration(newDuration: Duration, resetIfRunning: Boolean = false) {
    if (resetIfRunning) {
      totalDuration = newDuration
      reset()
    } else {
      val difference = newDuration - totalDuration
      totalDuration = newDuration
      durationRemaining = (durationRemaining + difference).coerceAtLeast(Duration.ZERO)

      // If running, shift our time anchor point forward/backward to adjust dynamically
      if (!isPaused) {
        lastStartMark = timeSource.markNow()
      }
    }
  }
}