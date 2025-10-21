package ua.com.merchik.merchik.features.maps.domain

import ua.com.merchik.merchik.dataLayer.model.FieldValue
import kotlin.math.roundToInt


fun isValidLatLon(lat: Double?, lon: Double?): Boolean =
    lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0 && !(lat == 0.0 && lon == 0.0)


fun List<FieldValue>.stringByKey(key: String): String? {
    val fv = firstOrNull { it.key == key } ?: return null
    return fv.value.value.takeIf { it.isNotBlank() }
        ?: fv.value.rawValue?.toString()?.takeIf { it.isNotBlank() }
}


fun String.parseDoubleSafe(): Double? {
    var s = this.replace('\u00A0', ' ')
        .trim()
        .replace(Regex("[^0-9,.-]"), "")
        .replace(" ", "")
    if (s.isEmpty()) return null
    s = s.replace(',', '.')
    val lastDot = s.lastIndexOf('.')
    if (lastDot > -1) {
        val before = s.substring(0, lastDot).replace(".", "")
        val after = s.substring(lastDot + 1)
        s = if (after.isEmpty()) before else "$before.$after"
    }
    return s.toDoubleOrNull()
}


fun List<FieldValue>.addrIdInt(): Int? =
    stringByKey("addr_id")?.trim()?.toIntOrNull()


fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2).let { it * it } +
            kotlin.math.cos(Math.toRadians(lat1)) *
            kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2).let { it * it }
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return earthRadius * c
}

fun formatSum(sum: Double?): String =
    sum?.let { "${it.roundToInt()} грн" } ?: ""