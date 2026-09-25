package ua.com.merchik.merchik.dialogs.features.masterCode

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MasterCodeAccessStateTest {
    private val nowMs = 1_790_000_000_000L

    @Test
    fun allowsFourAttemptsAndLeavesThreeAfterFirstFailure() {
        var state = MasterCodeAccessState()
        assertEquals(4, state.attemptsRemaining)
        for (remaining in 3 downTo 1) {
            state = state.afterFailure(nowMs)
            assertEquals(remaining, state.attemptsRemaining)
            assertFalse(state.isBlocked(nowMs))
        }
        state = state.afterFailure(nowMs)
        assertEquals(0, state.attemptsRemaining)
        assertTrue(state.isBlocked(nowMs))
        assertEquals(nowMs + 900_000L, state.blockedUntilMs)
    }

    @Test
    fun restoredStateKeepsFailedAttemptsAndLockDeadline() {
        val failed = MasterCodeAccessState().afterFailure(nowMs)
        val reopened = MasterCodeAccessState(failed.failedAttempts, failed.blockedUntilMs)
            .refreshed(nowMs + 1000L)
        assertEquals(3, reopened.attemptsRemaining)

        val locked = MasterCodeAccessState(3).afterFailure(nowMs)
        val restored = MasterCodeAccessState(locked.failedAttempts, locked.blockedUntilMs)
            .refreshed(nowMs + 60_000L)
        assertTrue(restored.isBlocked(nowMs + 60_000L))
        assertEquals(locked.blockedUntilMs, restored.blockedUntilMs)
        assertEquals(840L, restored.remainingSeconds(nowMs + 60_000L))
    }

    @Test
    fun blockedAttemptDoesNotExtendLock() {
        val locked = MasterCodeAccessState(3).afterFailure(nowMs)
        assertEquals(locked, locked.afterFailure(nowMs + 10_000L))
    }

    @Test
    fun countdownRoundsUpUntilLockExpires() {
        val locked = MasterCodeAccessState(3).afterFailure(nowMs)
        assertEquals(900L, locked.remainingSeconds(nowMs))
        assertEquals(900L, locked.remainingSeconds(nowMs + 1L))
        assertEquals(899L, locked.remainingSeconds(nowMs + 1000L))
        assertEquals(1L, locked.remainingSeconds(locked.blockedUntilMs - 1L))
        assertEquals(0L, locked.remainingSeconds(locked.blockedUntilMs))
    }

    @Test
    fun expiryRestoresAllFourAttemptsIncludingWhenDialogWasClosed() {
        val locked = MasterCodeAccessState(3).afterFailure(nowMs)
        assertEquals(locked, locked.refreshed(locked.blockedUntilMs - 1L))
        assertEquals(MasterCodeAccessState(), locked.refreshed(locked.blockedUntilMs))
        assertEquals(MasterCodeAccessState(), locked.refreshed(locked.blockedUntilMs + 60_000L))
        assertEquals(3, locked.afterFailure(locked.blockedUntilMs).attemptsRemaining)
        assertFalse(locked.afterFailure(locked.blockedUntilMs).isBlocked(locked.blockedUntilMs))
    }
}
