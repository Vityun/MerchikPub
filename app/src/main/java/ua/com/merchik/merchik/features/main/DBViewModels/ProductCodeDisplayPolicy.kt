package ua.com.merchik.merchik.features.main.DBViewModels

import ua.com.merchik.merchik.Activities.DetailedReportActivity.MarketingResearchSurvey
import ua.com.merchik.merchik.Globals.OptionControlName
import ua.com.merchik.merchik.data.TovarOptions

internal class ProductCodeDisplayPolicy(
    themeId: Int,
    priceCorrection: Boolean,
    shelfLengthCorrection: Boolean = false
) {
    val measurementOnly = shelfLengthCorrection || MarketingResearchSurvey.supportsTheme(themeId)
    private val measurementFields = listOf(OptionControlName.AMOUNT, OptionControlName.FACE)
    val requiredCodeFields = if (measurementOnly) measurementFields else emptyList()
    val expandByDefault = measurementOnly || priceCorrection
    val hiddenFields: Set<OptionControlName> = when {
        measurementOnly -> OptionControlName.values().toSet() - measurementFields.toSet()
        priceCorrection -> setOf(OptionControlName.UP, OptionControlName.FACE)
        else -> emptySet()
    }
    val showPhoto = !measurementOnly

    fun editorOptions(
        requiredOptions: List<TovarOptions>,
        allOptions: List<TovarOptions>,
        showAll: Boolean
    ): List<TovarOptions> {
        if (measurementOnly) {
            return measurementFields.mapNotNull { field ->
                allOptions.firstOrNull { it.getOptionControlName() == field }
            }
        }
        return (if (showAll) allOptions else requiredOptions)
            .filterNot { it.getOptionControlName() in hiddenFields }
    }

    fun amountTitle(optionText: String?): String {
        val title = optionText?.trim()?.takeIf { it.isNotEmpty() }
            ?: "Длина полочного пространства"
        return "$title (метрiв)"
    }
}
