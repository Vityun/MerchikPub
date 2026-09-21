package ua.com.merchik.merchik.dialogs.features.calendar

import org.junit.Assert.assertEquals
import org.junit.Test

class MerchikDatePickerLauncherTest {
    @Test
    fun formatsStoredDateForDisplay() {
        assertEquals("09.02.2026", MerchikDatePickerLauncher.formatForDisplay("2026-02-09"))
        assertEquals("09.02.2026", MerchikDatePickerLauncher.formatForDisplay("2026-2-9"))
    }

    @Test
    fun preservesStorageFormat() {
        assertEquals("2026-02-09", MerchikDatePickerLauncher.toStorageDate("09.02.2026"))
        assertEquals("2026-02-09", MerchikDatePickerLauncher.toStorageDate("2026-2-9"))
    }

    @Test
    fun emptyDateShowsHintAndKeepsServerSentinel() {
        for (value in listOf(null, "", "0000-00-00", "00.00.0000")) {
            assertEquals("", MerchikDatePickerLauncher.formatForDisplay(value))
            assertEquals("0000-00-00", MerchikDatePickerLauncher.toStorageDate(value))
        }
    }

    @Test
    fun rejectsInvalidDatesWithoutMovingThemToAnotherMonth() {
        assertEquals("", MerchikDatePickerLauncher.formatForDisplay("2026-02-29"))
        assertEquals("", MerchikDatePickerLauncher.formatForDisplay("31.04.2026"))
        assertEquals("29.02.2028", MerchikDatePickerLauncher.formatForDisplay("2028-02-29"))
    }
}
