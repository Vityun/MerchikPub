package ua.com.merchik.merchik.features.main.DBViewModels

import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date

class AchievementNextVisitTest {
    private val today = LocalDate.of(2026, 11, 23)
    private val zone = ZoneOffset.UTC
    private val achievement = AchievementsSDB().apply {
        clientId = "27"
        addrId = 53
        codeDad2 = 100
        dt_ut = today.minusDays(2).atTime(17, 0).toEpochSecond(zone)
    }

    @Test fun choosesNearestUnfinishedVisitForSameClientAddressAndWorker() {
        val nearest = visit(6, hour = 12).apply { status = 3 }
        val visits = listOf(
            visit(1).apply { status = 1 },
            visit(2).apply { status = 2 },
            visit(3).apply { visit_end_dt = 20 },
            visit(4).apply { client_end_dt = 20 },
            visit(5).apply { user_id = 76 },
            visit(7).apply { addr_id = 56 },
            visit(8).apply { client_id = "28" },
            visit(9, day = today.minusDays(1)),
            visit(10, hour = 10).apply { code_dad2 = achievement.codeDad2 },
            visit(11, day = today.plusDays(1)),
            visit(12, hour = 14),
            nearest
        )
        assertSame(nearest, nextVisitForAchievement(visits, achievement, null, 75, today, zone))
    }

    @Test fun nextVisitCanBeLaterOnSameDayAsTheOriginalVisit() {
        val origin = visit(1, hour = 10).apply { code_dad2 = achievement.codeDad2 }
        val later = visit(3, hour = 11)
        val earlier = visit(2, hour = 9)
        val visits = listOf(visit(4, day = today.plusDays(1)), origin, earlier, later)
        assertSame(later, nextVisitForAchievement(visits, achievement, origin, 75, today, zone))
        assertNull(nextVisitForAchievement(listOf(origin, earlier), achievement, origin, 75, today, zone))
    }

    @Test fun usesAchievementTimeIfOriginalVisitIsMissing() {
        achievement.dt_ut = today.atTime(11, 0).toEpochSecond(zone)
        val later = visit(2, hour = 12)
        assertSame(later, nextVisitForAchievement(listOf(visit(1), later), achievement, null, 75, today, zone))
    }

    private fun visit(visitId: Long, day: LocalDate = today, hour: Int = 9) = WpDataDB().apply {
        id = visitId
        code_dad2 = 1000 + visitId
        dt = Date.from(day.atStartOfDay(zone).toInstant())
        dt_start = day.atTime(hour, 0).toEpochSecond(zone)
        client_id = "27"
        addr_id = 53
        user_id = 75
    }
}
