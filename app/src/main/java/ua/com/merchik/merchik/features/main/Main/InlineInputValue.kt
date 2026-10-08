package ua.com.merchik.merchik.features.main.Main

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
