package ua.com.merchik.merchik.features.main.DBViewModels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB

class TovarReportPrepareSnapshotTest {
    private fun row(id: Long = 1, tovarId: String = "10") = ReportPrepareDB().apply {
        setID(id)
        setCodeDad2("123")
        setTovarId(tovarId)
        setPrice("10")
        setPriceMin("20")
        setPriceMax("20")
        setDtChange(100)
    }

    private fun snapshots(vararg rows: ReportPrepareDB) = indexReportPrepareByTovar(rows.toList())
        .mapValues { TovarReportPrepareSnapshot.from(it.value) }

    @Test
    fun unchangedDetachedCopiesDoNotRequireCardUpdates() {
        assertTrue(changedReportPrepareTovarIds(snapshots(row()), snapshots(row())).isEmpty())
    }

    @Test
    fun pricesChangedWithinTheSameSecondAreDetected() {
        val previous = snapshots(row())
        val updated = row().apply {
            setPrice("15")
            setPriceMin("25")
            setPriceMax("25")
        }
        assertEquals(setOf("10"), changedReportPrepareTovarIds(previous, snapshots(updated)))
    }

    @Test
    fun saveCallbackCannotMutateThePreviousSnapshot() {
        val cached = row()
        val previous = snapshots(cached)
        cached.setPrice("15")
        assertEquals("10", previous.getValue("10").price)
        assertEquals(setOf("10"), changedReportPrepareTovarIds(previous, snapshots(cached)))
    }

    @Test
    fun onlyAffectedProductsAreReturned() {
        val previous = snapshots(row(), row(2, "20"), row(3, "30"))
        val current = snapshots(row(), row(2, "20").apply { setNotes("new comment") }, row(3, "30"))
        assertEquals(setOf("20"), changedReportPrepareTovarIds(previous, current))
    }

    @Test
    fun uploadAcknowledgementChangesTplEvenIfPricesAreUnchanged() {
        val previous = snapshots(row().apply { setUploadStatus(1) })
        assertEquals(setOf("10"), changedReportPrepareTovarIds(previous, snapshots(row())))
    }

    @Test
    fun allEditorAndBalanceValuesParticipateInComparison() {
        val changes: List<(ReportPrepareDB) -> Unit> = listOf(
            { it.setPrice("0") },
            { it.setPriceMin("0") },
            { it.setPriceMax("0") },
            { it.setFace("3") },
            { it.setAmount(4) },
            { it.setUp("5") },
            { it.setDtExpire("2026-10-08") },
            { it.setExpireLeft("6") },
            { it.setNotes("comment") },
            { it.setOborotvedNum("7") },
            { it.oborotved_num_date = "123456" },
            { it.setErrorId("8") },
            { it.setErrorComment("error comment") },
            { it.setAkciyaId("9") },
            { it.setAkciya("1") }
        )
        val original = TovarReportPrepareSnapshot.from(row())
        changes.forEach { change ->
            val updated = row().also(change)
            assertNotEquals(original, TovarReportPrepareSnapshot.from(updated))
        }
    }

    @Test
    fun bookkeepingTimestampAloneDoesNotRebuildCards() {
        val previous = snapshots(row())
        val current = snapshots(row().apply { setDtChange(101) })
        assertTrue(changedReportPrepareTovarIds(previous, current).isEmpty())
    }

    @Test
    fun addedAndRemovedProductsChangeTheMembership() {
        val previous = snapshots(row(), row(2, "20"))
        val current = snapshots(row(), row(3, "30"))
        assertNotEquals(previous.keys, current.keys)
        assertEquals(setOf("20", "30"), changedReportPrepareTovarIds(previous, current))
    }

    @Test
    fun replacementDatabaseRowInvalidatesTheOldCopy() {
        assertEquals(setOf("10"), changedReportPrepareTovarIds(snapshots(row()), snapshots(row(2))))
    }

    @Test
    fun duplicateProductsUseTheFirstRowLikeTheExistingLookup() {
        val first = row()
        val duplicate = row(2).apply { setPrice("99") }
        val indexed = indexReportPrepareByTovar(listOf(first, duplicate, row(3, "")))
        assertEquals(setOf("10"), indexed.keys)
        assertSame(first, indexed["10"])
    }
}
