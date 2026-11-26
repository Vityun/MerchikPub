package ua.com.merchik.merchik.features.maps.domain

import org.junit.Assert.*
import org.junit.Test
import ua.com.merchik.merchik.data.RealmModels.WpDataDB

class AdditionalWorksDistanceTest {
    private val district = GeoPoint(50.45, 30.52)
    private val distantPoint = GeoPoint(-12.04, -77.05)

    private fun shop(lat: String?, lon: String?) = WpDataDB().apply {
        addr_location_xd = lat
        addr_location_yd = lon
    }

    @Test fun districtSearchUsesTheMapDistanceForBothRadiusAndColumn() {
        val near = shop("50,46034", "30.52")
        val far = shop("-12.04", "-77.05")
        additionalWorksInRadius(distantPoint, listOf(near, far), Float.MAX_VALUE)
        assertTrue(near.distanceForTT!! > 12_000_000)

        val found = additionalWorksInRadius(district, listOf(near, far), 1200f)
        assertSame(near, found.single())
        assertEquals(1150.0, near.distanceForTT!!, 1.0)
        assertTrue(additionalWorksInRadius(district, found, 1100f).isEmpty())
    }

    @Test fun missingOriginClearsPreviousDistanceAndDoesNotHideShops() {
        val near = shop("50.45", "30.52")
        val unknown = shop(null, "30.52")
        val shops = listOf(near, unknown)
        assertSame(near, additionalWorksInRadius(district, shops, 0f).single())
        assertEquals(0.0, near.distanceForTT!!, 0.0)
        assertNull(unknown.distanceForTT)

        assertEquals(shops, additionalWorksInRadius(null, shops, 0f))
        assertNull(near.distanceForTT)
        assertNull(unknown.distanceForTT)
    }
}
