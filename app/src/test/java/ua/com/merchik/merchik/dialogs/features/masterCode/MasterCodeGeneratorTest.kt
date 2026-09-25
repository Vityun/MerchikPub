package ua.com.merchik.merchik.dialogs.features.masterCode

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class MasterCodeGeneratorTest {
    @Test
    fun takesFourDigitsFromThirdPositionAfterRemovingDecimalPoint() {
        assertEquals("919", MasterCodeGenerator.generate(LocalDate.of(2026, 9, 25)))
        assertEquals("803", MasterCodeGenerator.generate(LocalDate.of(2026, 9, 26)))
        assertEquals("500", MasterCodeGenerator.generate(LocalDate.of(2026, 9, 27)))
    }

    @Test
    fun addsWeekOfYearWhenFirstRootIsInteger() {
        // 09012004 -> 9012004; sqrt = 3002; add week 2, then sqrt(9012006).
        assertEquals("200", MasterCodeGenerator.generate(LocalDate.of(2004, 1, 9)))
    }

    @Test
    fun padsDayAndMonthAndUsesAllFourYearDigits() {
        // 02042026 and 09072020, not 2426 or 9720.
        assertEquals("2899", MasterCodeGenerator.generate(LocalDate.of(2026, 4, 2)))
        assertEquals("1197", MasterCodeGenerator.generate(LocalDate.of(2020, 7, 9)))
    }

    @Test
    fun dropsLeadingZeroWithoutRemovingOtherZeros() {
        // sqrt(27092026) = 5205.000096061479; digits 3..6 without the point = "0500".
        assertEquals("500", MasterCodeGenerator.generate(LocalDate.of(2026, 9, 27)))
    }

    @Test
    fun supportsLeapDay() {
        assertEquals("8720", MasterCodeGenerator.generate(LocalDate.of(2024, 2, 29)))
    }

    @Test
    fun resultDoesNotDependOnDeviceLocale() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("ar"))
            assertEquals("919", MasterCodeGenerator.generate(LocalDate.of(2026, 9, 25)))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun allowsExactlyThreeDaysInEitherDirectionAcrossYearBoundary() {
        val today = LocalDate.of(2026, 1, 1)
        for (offset in -3L..3L) {
            assertTrue(MasterCodeGenerator.isDateAllowed(today.plusDays(offset), today))
        }
        assertFalse(MasterCodeGenerator.isDateAllowed(today.minusDays(4), today))
        assertFalse(MasterCodeGenerator.isDateAllowed(today.plusDays(4), today))
    }
}
