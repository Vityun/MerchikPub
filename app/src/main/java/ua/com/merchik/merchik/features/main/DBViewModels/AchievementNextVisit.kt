package ua.com.merchik.merchik.features.main.DBViewModels

import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal fun nextVisitForAchievement(
    visits: Iterable<WpDataDB>,
    achievement: AchievementsSDB,
    origin: WpDataDB?,
    userId: Int,
    today: LocalDate,
    zone: ZoneId
): WpDataDB? {
    val originStart = origin?.dt_start?.takeIf { it > 0 } ?: achievement.dt_ut?.takeIf { it > 0 }
    val originDay = origin?.dt?.toInstant()?.atZone(zone)?.toLocalDate()
        ?: originStart?.let { Instant.ofEpochSecond(it).atZone(zone).toLocalDate() }

    return visits.asSequence()
        .filter { visit ->
            visit.id > 0 && visit.code_dad2 > 0 && visit.code_dad2 != achievement.codeDad2 &&
                visit.user_id == userId && visit.client_id == achievement.clientId && visit.addr_id == achievement.addrId &&
                visit.status != 1 && visit.status != 2 && visit.visit_end_dt <= 0 && visit.client_end_dt <= 0
        }
        .mapNotNull { visit -> visit.dt?.toInstant()?.atZone(zone)?.toLocalDate()?.let { it to visit } }
        .filter { (day, visit) ->
            day >= today && (originDay == null || day > originDay ||
                (day == originDay && originStart != null && visit.dt_start > originStart))
        }
        .minWithOrNull(compareBy(
            { it.first },
            { it.second.dt_start.takeIf { start -> start > 0 } ?: Long.MAX_VALUE },
            { it.second.id }
        ))
        ?.second
}
