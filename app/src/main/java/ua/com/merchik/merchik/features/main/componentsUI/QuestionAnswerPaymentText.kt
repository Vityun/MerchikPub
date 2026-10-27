package ua.com.merchik.merchik.features.main.componentsUI

import ua.com.merchik.merchik.Utils.WorkTimeRules
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal fun questionAnswerStatementTemplate(
    isOneTimePaymentIncrease: Boolean,
    isPlannedDuration: Boolean = false,
    isServerDuration: Boolean = false,
    translate: (String, Long) -> String
): String = if (isOneTimePaymentIncrease) {
    // No translation ID has been assigned to the one-time statement yet.
    (if (isPlannedDuration) "Планова тривалість роботи у цій ТТ становить {DURATION_FACT} хвилин, "
    else if (isServerDuration) "Середня фактична тривалість попереднiх роботи у цій ТТ становить {DURATION_FACT} хвилин, "
    else "Зазвичай, пiд час вiзиту, я витрачаю на роботу у цiй ТТ у середньому близько {DURATION_FACT} хвилин, ") +
            "при цьому підіймаю на вітрину приблизно {UP_FACT} одиниць товару, але пiд час ПОТОЧНОГО вiзиту (за {VISIT_DATE}) " +
            "фактична тривалiсть склала {CURRENT_VISIT_DURATION} хв. Прошу надати РАЗОВУ премiю у розмiрi {DESIRED_CASH} грн. " +
            "Хочу зазначити що пiд час ПОТОЧНОГО вiзиту я виконав наступнi дії:"
} else {
    translate(
        "Я, {USER}, зазвичай витрачаю на роботу з цим клієнтом у цiй ТТ {DURATION_FACT} хвилин, " +
                "при цьому, зазвичай, за вiзит підіймаю на вітрину приблизно {UP_FACT} одиниць товару і отримую на руки близько {CASH_FACT} грн/вiзит, " +
                "при цьому втрачаю на сигналах за опціями {CASH_PENALTY_FACT} грн/вiзит. " +
                "Вважаю, що оплата недостатня. Хочу, щоб вона становила {DESIRED_CASH} грн/вiзит. " +
                "Або збільшити кількість візитів, зараз в середньому {VISITS_PER_WEEK_FACT} вiзитiв на тиждень, хочу щоб становила {VISITS_PER_WEEK} вiзитiв в тиждень. " +
                "Крім того, хочу зазначити:",
        100500L
    )
}

internal fun questionAnswerCommentPlaceholder(isOneTimePaymentIncrease: Boolean): String =
    if (isOneTimePaymentIncrease) {
        "Тут додайте усі фактори, котрі суттєво вплинули на тривалість виконання роботи пiдчас ПОТОЧНОГО вiзиту. " +
                "Позаплановi роботи з: (планограмами, персоналом, складом, фасуванням, цiнниками і т.д.) та вкажіть свої аргументи щодо надання РАЗОВОГО бонусу."
    } else {
        "Тут додайте усі фактори, котрі суттєво впливають на тривалість виконання робіт. " +
                "Складнощі з: (планограмами, персоналом, складом, фасуванням, і т.д.) та вкажіть свої аргументи щодо збільшення кількості візитів, або вартості."
    }

internal fun questionAnswerHasEmptyRequiredFields(
    isOneTimePaymentIncrease: Boolean,
    desiredCash: String,
    visitsPerWeek: String
): Boolean = desiredCash.isBlank() || (!isOneTimePaymentIncrease && visitsPerWeek.isBlank())

internal data class QuestionAnswerVisitDetails(
    val date: String = "не вказано",
    val durationMinutes: String = "не визначено",
    val preciseDurationMinutes: Double? = null
)

internal fun questionAnswerVisitDetails(
    date: Date?,
    visitStart: Long,
    clientStart: Long,
    visitEnd: Long,
    clientEnd: Long,
    nowSeconds: Long
): QuestionAnswerVisitDetails {
    val dateText = date?.let {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Europe/Kiev")
        }.format(it)
    } ?: "не вказано"
    val minutes = runCatching {
        val times = WorkTimeRules.normalize(visitStart, clientStart, visitEnd, clientEnd)
        val end = times.end.takeIf { it > 0L } ?: nowSeconds
        if (times.start <= 0L || end < times.start) return@runCatching null
        (end - times.start) / 60.0
    }.getOrNull()
    val durationText = minutes?.let {
        BigDecimal.valueOf(it).setScale(0, RoundingMode.HALF_UP).toPlainString()
    } ?: "не визначено"
    return QuestionAnswerVisitDetails(dateText, durationText, minutes)
}
