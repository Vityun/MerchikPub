package ua.com.merchik.merchik.features.main

import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB
import java.io.File

class AchievementsBonusTest {
    @Test fun accruedBonusShowsStatusAmountAndLightGreenBackground() {
        assertEquals("Нараховано премію: 150 грн", AchievementsSDBOverride.bonusStatus("150.00"))
        assertEquals(Color(0xFFA9FFD5), AchievementsSDBOverride.bonusBackground("150.00"))
    }

    @Test fun absentOrZeroBonusIsNotHighlighted() {
        for (amount in listOf(null, "", " ", "0", "0.00", "0,00", "-10", "null")) {
            assertEquals("Премія ще не нарахована", AchievementsSDBOverride.bonusStatus(amount))
            assertNull(AchievementsSDBOverride.bonusBackground(amount))
        }
    }

    @Test fun malformedAmountDoesNotCrashOrLookAccrued() {
        for (amount in listOf("not a number", "NaN", "Infinity", "15 грн", "1.2.3")) {
            assertEquals("Премія ще не нарахована", AchievementsSDBOverride.bonusStatus(amount))
            assertNull(AchievementsSDBOverride.bonusBackground(amount))
        }
    }

    @Test fun decimalAmountsAreReadableInUkrainian() {
        assertEquals("Нараховано премію: 21,01 грн", AchievementsSDBOverride.bonusStatus("21.01"))
        assertEquals("Нараховано премію: 150,5 грн", AchievementsSDBOverride.bonusStatus(" 150,50 "))
        assertEquals("Нараховано премію: 0,01 грн", AchievementsSDBOverride.bonusStatus("0.01"))
        assertEquals("Нараховано премію: 21,02 грн", AchievementsSDBOverride.bonusStatus("21.015"))
    }

    @Test fun serverAmountIsUsedWithoutChangingStoredData() {
        val row = Gson().fromJson(
            """{"ID":260837,"prem_amount":"150.00","prem_amount_dt":"2026-10-07","confirm_state":1}""",
            AchievementsSDB::class.java
        )
        val before = Gson().toJson(row)
        assertEquals("Нараховано премію: 150 грн", AchievementsSDBOverride.bonusStatus(row.premAmount))
        assertEquals(Color(0xFFA9FFD5), AchievementsSDBOverride.bonusBackground(row.premAmount))
        assertEquals(before, Gson().toJson(row))
        assertFalse(before.contains(AchievementsSDBOverride.BONUS_STATUS_KEY))
    }

    @Test fun approvalAndUploadDoNotCountAsAnAccruedBonus() {
        val row = AchievementsSDB().apply { confirmState = 1; dvi = 123; premAmount = "0" }
        assertNull(AchievementsSDBOverride.bonusBackground(row.premAmount))
        assertEquals("Премія ще не нарахована", AchievementsSDBOverride.bonusStatus(row.premAmount))
    }

    @Test fun statusAndBackgroundFollowUpdatedAmount() {
        val row = AchievementsSDB().apply { premAmount = "0" }
        assertNull(AchievementsSDBOverride.bonusBackground(row.premAmount))
        row.premAmount = "150"
        assertEquals(Color(0xFFA9FFD5), AchievementsSDBOverride.bonusBackground(row.premAmount))
        assertEquals("Нараховано премію: 150 грн", AchievementsSDBOverride.bonusStatus(row.premAmount))
        row.premAmount = "0"
        assertNull(AchievementsSDBOverride.bonusBackground(row.premAmount))
        assertEquals("Премія ще не нарахована", AchievementsSDBOverride.bonusStatus(row.premAmount))
    }

    @Test fun newStatusHasTitleAndIsAvailableWithExistingHiddenBonusFields() {
        assertEquals("Статус премії", AchievementsSDBOverride.getFallbackTitle(AchievementsSDBOverride.BONUS_STATUS_KEY))
        val viewModel = source("features/main/DBViewModels/AchievementsSDBViewModel.kt")
        assertFalse(viewModel.contains("\"bonus_status\""))
        for (file in listOf("dataLayer/Mappers.kt", "dataLayer/MainRepository.kt")) {
            val code = source(file)
            assertTrue(file, code.contains("AchievementsSDBOverride.prepareUiFields(jsonObject)"))
            assertTrue(file, code.contains("AchievementsSDBOverride.getFallbackTitle(key)"))
        }
    }

    @Test fun modelUsesAchievementHighlightAndPhotosUseTheSameColor() {
        assertTrue(source("data/Database/Room/AchievementsSDB.java").contains(
            "return AchievementsSDBOverride.INSTANCE.getContainerModifier(jsonObject);"
        ))
        val photoStyle = source("features/main/Wrappers.kt").substringAfter("object StackPhotoDBOverride")
        assertTrue(photoStyle.contains("MerchModifier(background = completedItemBackground)"))
        assertTrue(source("features/main/Main/MainUI.kt").contains(
            "if (item.selected) colorResource(id = R.color.selected_item)"
        ))
    }

    private fun source(path: String): String {
        val root = File("app/src/main/java/ua/com/merchik/merchik")
            .takeIf { it.isDirectory } ?: File("src/main/java/ua/com/merchik/merchik")
        return File(root, path).readText()
    }
}
