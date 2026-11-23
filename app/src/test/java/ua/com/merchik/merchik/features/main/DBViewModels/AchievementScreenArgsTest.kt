package ua.com.merchik.merchik.features.main.DBViewModels

import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.features.main.Main.clearEditableValues

class AchievementScreenArgsTest {
    private val visitDad2 = 1211126038_637061792L

    @Test fun visitAndServerIdRemainIndependentArguments() {
        val args = AchievementScreenArgs.fromJson(
            AchievementScreenArgs(visitDad2 = visitDad2, achievementServerId = 267632).toJson()
        )

        assertEquals(visitDad2, args.visitDad2)
        assertEquals(267632, args.achievementServerId)
    }

    @Test fun existingVisitArgumentsRemainSupported() {
        for (data in listOf(
            """{"wpDataDBId":"$visitDad2"}""",
            visitDad2.toString(),
            """{"nameValuePairs":{"code_dad2":$visitDad2}}"""
        )) {
            val args = AchievementScreenArgs.fromJson(data)
            assertEquals(AchievementScreenArgs(visitDad2 = visitDad2), args)
        }
    }

    @Test fun serverIdDoesNotRequireAVisit() {
        val args = AchievementScreenArgs.fromJson("""{"achievementServerId":267632}""")
        assertEquals(AchievementScreenArgs(achievementServerId = 267632), args)
    }

    @Test fun chatReusesTheAchievementDisplaySettingsOnly() {
        assertEquals(ContextUI.ACHIEVEMENT, ContextUI.ACHIEVEMENT_FROM_CHAT.settingsContext)
        assertEquals(ContextUI.ACHIEVEMENT, ContextUI.ACHIEVEMENT.settingsContext)
        assertEquals(ContextUI.TASKS_IN_CONTAINER, ContextUI.TASKS_IN_CONTAINER.settingsContext)
    }

    @Test fun linkedWindowShowsOnlyAReadOnlyServerIdFilter() {
        val achievement = AchievementsSDB().apply {
            id = 42
            serverId = 267632
        }
        val filters = linkedAchievementFilters(achievement.serverId, "Achievement", "Read-only", "")
        val filter = filters.items.single()
        val fields = JsonParser.parseString(Gson().toJson(achievement)).asJsonObject

        assertFalse(filter.enabled)
        assertNull(filters.rangeDataByKey)
        assertEquals(listOf(fields[filter.leftField].asString), filter.rightValuesRaw)
        assertEquals(listOf("267632"), filter.rightValuesUI)
        assertEquals(listOf(filter), filters.clearEditableValues().items)
    }

    @Test fun invalidIdsDoNotProduceArguments() {
        for (raw in listOf(null, "not json", """{"wpDataDBId":-1,"achievementServerId":2147483648}""")) {
            assertEquals(AchievementScreenArgs(), AchievementScreenArgs.fromJson(raw))
        }
    }
}
