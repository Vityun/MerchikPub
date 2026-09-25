package ua.com.merchik.merchik.dialogs.features.masterCode

import android.util.Log
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.sqrt

object MasterCodeGenerator {
    private val dateNumberFormatter = DateTimeFormatter.ofPattern("ddMMyyyy", Locale.ROOT)

    // 1C counts the week containing January 1 as week 1, starting on Monday.
    private val weekOfYear = WeekFields.of(DayOfWeek.MONDAY, 1).weekOfYear()

    fun generate(date: LocalDate): String {
        // 2.0. Concatenate the date as ddMMyyyy, then convert it to a number.
        Log.e("MasterCodeGenerator","0: ${date.dayOfMonth} ${date.monthValue} ${date.year}" );

        var number = date.format(dateNumberFormatter).toLong()

        Log.e("MasterCodeGenerator","number: $number");
        // 3.0. Recalculate an integer root once, adding the week of the year.
        var result = sqrt(number.toDouble())
        if (result == result.toInt().toDouble()) {
            number += date.get(weekOfYear)
            result = sqrt(number.toDouble())
        }
        Log.e("MasterCodeGenerator","result: $result");

        // Remove the decimal point, then take four digits starting at position 3 (1-based).
        val digits = result.toString().replace(".", "").drop(2).take(4)
        Log.e("MasterCodeGenerator","digits: $digits");

        return (digits.toIntOrNull() ?: 0).toString()
    }

    fun isDateAllowed(date: LocalDate, today: LocalDate): Boolean =
        !date.isBefore(today.minusDays(3)) && !date.isAfter(today.plusDays(3))
}
