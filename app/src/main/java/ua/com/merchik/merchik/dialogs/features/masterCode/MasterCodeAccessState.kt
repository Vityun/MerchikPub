package ua.com.merchik.merchik.dialogs.features.masterCode

internal data class MasterCodeAccessState(
    val failedAttempts: Int = 0,
    val blockedUntilMs: Long = 0L
) {
    val attemptsRemaining: Int
        get() = (MAX_ATTEMPTS - failedAttempts).coerceIn(0, MAX_ATTEMPTS)

    fun remainingSeconds(nowMs: Long): Long =
        ((blockedUntilMs - nowMs).coerceAtLeast(0L) + 999L) / 1000L

    fun isBlocked(nowMs: Long): Boolean = blockedUntilMs > nowMs

    fun refreshed(nowMs: Long): MasterCodeAccessState =
        if (blockedUntilMs != 0L && !isBlocked(nowMs)) MasterCodeAccessState() else this

    fun afterFailure(nowMs: Long): MasterCodeAccessState {
        val current = refreshed(nowMs)
        if (current.isBlocked(nowMs)) return current
        val failures = (current.failedAttempts + 1).coerceAtMost(MAX_ATTEMPTS)
        return MasterCodeAccessState(
            failedAttempts = failures,
            blockedUntilMs = if (failures == MAX_ATTEMPTS) nowMs + LOCK_DURATION_MS else 0L
        )
    }

    companion object {
        const val MAX_ATTEMPTS = 4
        const val LOCK_DURATION_MS = 15 * 60 * 1000L
    }
}
