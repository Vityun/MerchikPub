package ua.com.merchik.merchik.features.main.Main

import ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.PriceDisplay

internal data class InlineNumberInputState(
    val focused: Boolean = false,
    private val hideZero: Boolean = false
) {
    fun onFocusChanged(hasFocus: Boolean, value: String): InlineNumberInputState {
        if (focused == hasFocus) return this
        return copy(focused = hasFocus, hideZero = hasFocus && value.isNumericZero())
    }

    fun onInput(value: String): InlineNumberInputState = copy(hideZero = value.isBlank())

    fun displayedValue(value: String, formatAsPrice: Boolean = false): String = when {
        formatAsPrice && !focused -> PriceDisplay.format(value)
        focused && hideZero && value.isNumericZero() -> ""
        else -> value
    }
}

private fun String.isNumericZero(): Boolean =
    replace(',', '.').toBigDecimalOrNull()?.signum() == 0

internal fun refreshedInlineInputValue(
    previous: String,
    local: String,
    current: String,
    numeric: Boolean = false
): String {
    // A partial decimal or a comment rejected by validation is still the user's draft.
    val completeNumber = local.isNotBlank() && !local.endsWith('.') && !local.endsWith(',')
    val acknowledgedNumber = if (numeric && completeNumber) {
        val entered = local.replace(',', '.').toBigDecimalOrNull()
        val saved = current.replace(',', '.').toBigDecimalOrNull()
        entered != null && saved != null && entered.compareTo(saved) == 0
    } else false
    return if (local == previous || acknowledgedNumber) current else local
}
