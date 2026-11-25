package ua.com.merchik.merchik.features.main.componentsUI

internal const val ONE_TIME_COMMENT_MIN_LETTERS = 10

internal fun hasEnoughOneTimeCommentLetters(comment: String): Boolean {
    var letters = 0
    var index = 0
    while (index < comment.length) {
        val codePoint = Character.codePointAt(comment, index)
        // Ukrainian text may use U+02BC as an apostrophe, although Unicode classifies it as a letter.
        if (codePoint != 0x02BC && Character.isLetter(codePoint)) {
            if (++letters == ONE_TIME_COMMENT_MIN_LETTERS) return true
        }
        index += Character.charCount(codePoint)
    }
    return false
}

internal fun isOneTimeDurationBelowAverage(
    isOneTimePaymentIncrease: Boolean,
    currentMinutes: Double?,
    averageMinutes: Double
): Boolean = isOneTimePaymentIncrease &&
        currentMinutes != null && currentMinutes.isFinite() && currentMinutes >= 0.0 &&
        averageMinutes.isFinite() && averageMinutes > 0.0 &&
        currentMinutes < averageMinutes
