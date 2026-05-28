//package org.hexnibble.hexlib
//
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertFalse
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import kotlin.time.Duration
//import kotlin.time.Duration.Companion.seconds
//import kotlin.time.TimeMark
//import kotlin.time.TimeSource
//
//class TimerTest {
//
//  private class TestTimeSource : TimeSource {
//    var reading: Duration = Duration.ZERO
//
//    override fun markNow(): TimeMark = object : TimeMark {
//      val markReading = reading
//      override fun elapsedNow(): Duration = reading - markReading
//    }
//  }
//
//  private lateinit var testTimeSource: TestTimeSource
//
//  @BeforeEach
//  fun setUp() {
//    testTimeSource = TestTimeSource()
//  }
//
//  private fun elapseTime(duration: Duration) {
//    testTimeSource.reading += duration
//  }
//
//  // =========================================================================
//  // 1. INITIAL STATE TESTS
//  // =========================================================================
//
//  @Test
//  fun `test initialization sets correct properties`() {
//    val timer = Timer(10.seconds, testTimeSource)
//    assertEquals(10.seconds, timer.timeRemaining)
//    assertEquals(10.seconds, timer.getRemainingTime())
//    assertTrue(timer.isPaused)
//  }
//
//  // =========================================================================
//  // 2. START & PAUSE TESTS (Branch Coverage)
//  // =========================================================================
//
//  @Test
//  fun `test start transitions isPaused to false`() {
//    val timer = Timer(10.seconds, testTimeSource)
//    timer.start()
//    assertFalse(timer.isPaused)
//  }
//
//  @Test
//  fun `test start when already running hits early return`() {
//    val timer = Timer(10.seconds, testTimeSource)
//    timer.start()
//
//    // This hits the 'if (!isPaused) return' branch
//    timer.start()
//
//    elapseTime(3.seconds)
//    assertEquals(7.seconds, timer.getRemainingTime())
//  }
//
//  @Test
//  fun `test pause when already paused hits early return`() {
//    val timer = Timer(10.seconds, testTimeSource)
//
//    // This hits the 'val startMark = lastStartMark ?: return' branch
//    timer.pause()
//
//    assertTrue(timer.isPaused)
//    assertEquals(10.seconds, timer.getRemainingTime())
//  }
//
//  @Test
//  fun `test normal start, pause, and resume sequence`() {
//    val timer = Timer(10.seconds, testTimeSource)
//
//    timer.start()
//    elapseTime(2.seconds) // 8s left
//
//    timer.pause() // durationRemaining locks at 8s
//    assertTrue(timer.isPaused)
//
//    elapseTime(5.seconds) // Time passes while paused
//    assertEquals(8.seconds, timer.getRemainingTime()) // Stays 8s
//
//    timer.start() // Resume
//    elapseTime(3.seconds) // 5s left
//    assertEquals(5.seconds, timer.getRemainingTime())
//  }
//
//  // =========================================================================
//  // 3. BOUNDARY & COERCE TESTS (Negative Limits)
//  // =========================================================================
//
//  @Test
//  fun `test running timer bottoms out at zero and does not go negative`() {
//    val timer = Timer(5.seconds, testTimeSource)
//    timer.start()
//
//    elapseTime(6.seconds) // Exceed total duration
//
//    // Verifies coerceAtLeast(Duration.ZERO) inside getRemainingTime()
//    assertEquals(Duration.ZERO, timer.getRemainingTime())
//  }
//
//  @Test
//  fun `test pause after expiration bottoms out remaining duration at zero`() {
//    val timer = Timer(5.seconds, testTimeSource)
//    timer.start()
//
//    elapseTime(6.seconds)
//
//    // Verifies coerceAtLeast(Duration.ZERO) inside pause()
//    timer.pause()
//
//    assertEquals(Duration.ZERO, timer.getRemainingTime())
//  }
//
//  // =========================================================================
//  // 4. UPDATE DURATION TESTS (All Permutations)
//  // =========================================================================
//
//  @Test
//  fun `test updateDuration with resetIfRunning true resets fully`() {
//    val timer = Timer(10.seconds, testTimeSource)
//    timer.start()
//    elapseTime(3.seconds)
//
//    // Hits the 'if (resetIfRunning)' branch
//    timer.updateDuration(20.seconds, resetIfRunning = true)
//
//    assertEquals(20.seconds, timer.timeRemaining)
//    assertEquals(20.seconds, timer.getRemainingTime())
//    assertTrue(timer.isPaused)
//  }
//
//  @Test
//  fun `test updateDuration without reset increases time dynamically while running`() {
//    val timer = Timer(10.seconds, testTimeSource)
//    timer.start()
//    elapseTime(3.seconds) // 7s remaining
//
//    // Hits the 'else' branch (adds 5s to total, adds 5s to remaining)
//    timer.updateDuration(15.seconds, resetIfRunning = false)
//
//    assertEquals(15.seconds, timer.timeRemaining)
//    assertEquals(12.seconds, timer.getRemainingTime())
//    assertFalse(timer.isPaused) // Remains running
//  }
//
//  @Test
//  fun `test updateDuration without reset decreases time dynamically while running`() {
//    val timer = Timer(10.seconds, testTimeSource)
//    timer.start()
//    elapseTime(3.seconds) // 7s remaining
//
//    // Hits the 'else' branch (removes 4s from total, removes 4s from remaining)
//    timer.updateDuration(6.seconds, resetIfRunning = false)
//
//    assertEquals(6.seconds, timer.timeRemaining)
//    assertEquals(3.seconds, timer.getRemainingTime())
//  }
//
//  @Test
//  fun `test updateDuration without reset bottoms out remaining time at zero if difference is too large`() {
//    val timer = Timer(10.seconds, testTimeSource)
//    timer.start()
//    elapseTime(4.seconds) // 6s remaining
//
//    // Drop total duration by 8s. 6s + (-8s) = -2s -> Coerced to ZERO
//    timer.updateDuration(2.seconds, resetIfRunning = false)
//
//    assertEquals(2.seconds, timer.timeRemaining)
//    assertEquals(Duration.ZERO, timer.getRemainingTime())
//  }
//
//  @Test
//  fun `test updateDuration without reset updates paused timer successfully`() {
//    val timer = Timer(10.seconds, testTimeSource)
//    // Timer remains paused throughout this test
//
//    timer.updateDuration(15.seconds, resetIfRunning = false)
//
//    assertEquals(15.seconds, timer.timeRemaining)
//    assertEquals(15.seconds, timer.getRemainingTime())
//    assertTrue(timer.isPaused)
//  }
//
//  // =========================================================================
//  // 5. RESET TESTS
//  // =========================================================================
//
//  @Test
//  fun `test reset forces paused state and restores full duration`() {
//    val timer = Timer(10.seconds, testTimeSource)
//    timer.start()
//    elapseTime(5.seconds)
//
//    timer.reset()
//
//    assertTrue(timer.isPaused)
//    assertEquals(10.seconds, timer.timeRemaining)
//    assertEquals(10.seconds, timer.getRemainingTime())
//  }
//}