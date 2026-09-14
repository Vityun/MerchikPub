package ua.com.merchik.merchik.data.synchronization

import org.junit.Assert.*
import org.junit.Test
import ua.com.merchik.merchik.data.Database.Room.InitStateEntity
import ua.com.merchik.merchik.database.room.DaoInterfaces.InitStateDao

class InitStateReadinessTest {
    private class MemoryDao(var stored: InitStateEntity? = null) : InitStateDao {
        var writes = 0
        override fun getState() = stored
        override fun saveState(state: InitStateEntity) {
            this.stored = state
            writes++
        }
    }

    @Test fun recoversMissingFlagsFromCurrentStores() {
        val dao = MemoryDao()
        val state = dao.mergeLocalReadiness(true, true, true, true)
        assertTrue(state.wpLoaded && state.siteLoaded && state.optionsLoaded && state.themeLoaded)
        assertFalse(state.customerLoaded)
        assertFalse(state.userLoaded)
        assertEquals(1, dao.writes)
    }

    @Test fun preservesSuccessfullyDownloadedEmptyPlan() {
        val dao = MemoryDao(InitStateEntity(wpLoaded = true))
        val state = dao.mergeLocalReadiness(false, true, true, true)
        assertTrue(state.wpLoaded && state.siteLoaded && state.optionsLoaded && state.themeLoaded)
    }

    @Test fun doesNotInventMissingData() {
        val dao = MemoryDao()
        val state = dao.mergeLocalReadiness(true, false, true, false)
        assertTrue(state.wpLoaded)
        assertTrue(state.optionsLoaded)
        assertFalse(state.siteLoaded)
        assertFalse(state.themeLoaded)
    }

    @Test fun neverResetsReadyFlagsOrUnrelatedFields() {
        val initial = InitStateEntity(wpLoaded = true, siteLoaded = true, optionsLoaded = true,
            themeLoaded = true, customerLoaded = true, userLoaded = true)
        val dao = MemoryDao(initial)
        assertEquals(initial, dao.mergeLocalReadiness(false, false, false, false))
        assertEquals(0, dao.writes)
    }

    @Test fun pollingDoesNotRewriteUnchangedState() {
        val dao = MemoryDao()
        repeat(10) { dao.mergeLocalReadiness(true, false, false, false) }
        assertEquals(1, dao.writes)
    }

    @Test fun failedInitialDownloadsLeaveFlagsAbsent() {
        val dao = MemoryDao()
        val state = dao.mergeLocalReadiness(false, false, false, false)
        assertFalse(state.wpLoaded || state.siteLoaded || state.optionsLoaded || state.themeLoaded)
        assertNull(dao.stored)
        assertEquals(0, dao.writes)
    }
}
