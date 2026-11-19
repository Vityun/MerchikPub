package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity

import org.junit.Assert.*
import org.junit.Test
import ua.com.merchik.merchik.data.Database.Room.AddressSDB

class TARVisitMapTest {
    private fun address(lat: Float? = 50.45f, lon: Float? = 30.59f) = AddressSDB().apply {
        id = 123
        nm = "Task address"
        locationXd = lat
        locationYd = lon
    }

    @Test fun validAddressCanBeShownWithoutVisit() {
        assertTrue(hasTarMapCoordinates(address()))
    }

    @Test fun absentCoordinatesCannotBecomeZeroMarker() {
        assertFalse(hasTarMapCoordinates(null))
        assertFalse(hasTarMapCoordinates(address(null, 30.59f)))
        assertFalse(hasTarMapCoordinates(address(50.45f, null)))
        assertFalse(hasTarMapCoordinates(address(0f, 0f)))
    }

    @Test fun invalidCoordinatesAreRejected() {
        assertFalse(hasTarMapCoordinates(address(91f, 30f)))
        assertFalse(hasTarMapCoordinates(address(50f, 181f)))
        assertFalse(hasTarMapCoordinates(address(Float.NaN, 30f)))
        assertFalse(hasTarMapCoordinates(address(50f, Float.POSITIVE_INFINITY)))
    }

    @Test fun fallbackContainsOnlyTheTaskAddressNotAnInventedVisitOrTrack() {
        val input = tarAddressMapInput(address())
        assertEquals(123, input.center?.addrId)
        assertEquals("Task address", input.center?.title)
        assertEquals(50.45, input.center!!.pos.latitude, 0.00001)
        assertEquals(30.59, input.center!!.pos.longitude, 0.00001)
        assertTrue(input.points.isEmpty())
        assertNull(input.userLat)
        assertNull(input.userLon)
    }

    @Test(expected = IllegalArgumentException::class)
    fun fallbackRequiresCoordinates() {
        tarAddressMapInput(address(null, null))
    }
}
