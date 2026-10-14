package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers

import org.junit.Assert.*
import org.junit.Test

class InlinePriceDraftsTest {
    private val current = InlinePriceField(1, "price", "10", false)
    private val before = InlinePriceField(1, "price_before_promotion", "10", true)

    private class Fixture {
        var prices = InlinePriceValues("1234", "1235", "Product")
        var stored = emptyMap<InlinePriceField, String>()
        var writes = 0
        var rejectWrite = false

        fun controller() = InlinePriceDrafts(stored, { prices }, { field, value ->
            if (rejectWrite) false else {
                prices = if (field.beforePromotion) prices.copy(priceMin = value) else prices.copy(price = value)
                writes++
                true
            }
        }, { stored = it })
    }

    @Test
    fun invalidPrefixesStayInDraftWithoutSavingOrRequestingDialogs() {
        val fixture = Fixture()
        val controller = fixture.controller()
        for (value in listOf("1", "12", "123")) {
            controller.change(before, value)
            assertEquals(value, controller.drafts.value[before]?.value)
            assertTrue(controller.drafts.value.getValue(before).invalidPrice)
            assertEquals(value, fixture.stored[before])
            assertNull(controller.nextError())
        }
        assertEquals(0, fixture.writes)
        controller.change(before, "1235")
        assertEquals(1, fixture.writes)
        assertTrue(controller.drafts.value.isEmpty())
        assertTrue(fixture.stored.isEmpty())
    }

    @Test
    fun currentPriceUsesTheSameDraftValidation() {
        val fixture = Fixture()
        val controller = fixture.controller()
        controller.change(current, "1236")
        assertTrue(controller.drafts.value.getValue(current).invalidPrice)
        assertEquals("1234", fixture.prices.price)
        controller.finish(current)
        assertEquals("1236", controller.nextError()?.draft?.prices?.price)
    }

    @Test
    fun blurEnterAndPauseDoNotDuplicateTheSameError() {
        val fixture = Fixture()
        val controller = fixture.controller()
        controller.change(before, "1")
        controller.finish(before)
        controller.finishAll()
        val error = controller.nextError()!!
        controller.markShown(error)
        controller.finish(before)
        controller.finishAll()
        assertNull(controller.nextError())
        assertEquals("1", fixture.stored[before])
        controller.change(before, "12")
        controller.finish(before)
        assertEquals("12", controller.nextError()?.draft?.value)
    }

    @Test
    fun deferredErrorIsRevalidatedAgainstFreshDatabasePrices() {
        val fixture = Fixture()
        val controller = fixture.controller()
        controller.change(before, "1000")
        controller.finishAll()
        fixture.prices = fixture.prices.copy(price = "900")
        assertNull(controller.nextError())
        assertEquals("1000", fixture.prices.priceMin)
        assertTrue(fixture.stored.isEmpty())
    }

    @Test
    fun draftAndDeferredErrorSurviveControllerRecreation() {
        val fixture = Fixture()
        fixture.controller().change(before, "12")
        val restored = fixture.controller()
        assertEquals("12", restored.drafts.value[before]?.value)
        assertEquals("12", restored.nextError()?.draft?.value)
        assertEquals("1235", fixture.prices.priceMin)
    }

    @Test
    fun refreshDoesNotReplaceDraftWithPersistedValue() {
        val fixture = Fixture()
        val controller = fixture.controller()
        controller.change(before, "12")
        fixture.prices = fixture.prices.copy(priceMin = "1500")
        controller.revalidate()
        assertEquals("12", controller.drafts.value[before]?.value)
        assertTrue(controller.drafts.value.getValue(before).invalidPrice)
    }

    @Test
    fun incompleteDecimalIsKeptUntilEditingEnds() {
        val fixture = Fixture()
        val controller = fixture.controller()
        controller.change(before, "1500.")
        assertEquals(0, fixture.writes)
        assertEquals("1500.", fixture.stored[before])
        controller.finishAll()
        assertEquals("1500", fixture.prices.priceMin)
        assertTrue(fixture.stored.isEmpty())
    }

    @Test
    fun zeroCanStillBeSavedInBothFields() {
        val fixture = Fixture()
        val controller = fixture.controller()
        controller.change(before, "0")
        controller.change(current, "0")
        assertEquals("0", fixture.prices.price)
        assertEquals("0", fixture.prices.priceMin)
        assertEquals(2, fixture.writes)
        assertTrue(fixture.stored.isEmpty())
    }

    @Test
    fun deletingEitherPriceImmediatelySavesZero() {
        val fixture = Fixture()
        val controller = fixture.controller()
        controller.change(current, "")
        assertEquals("0", fixture.prices.price)
        controller.change(before, "")
        assertEquals("0", fixture.prices.priceMin)
        assertEquals(2, fixture.writes)
        assertTrue(fixture.stored.isEmpty())
        controller.finishAll()
        assertNull(controller.nextError())
    }

    @Test
    fun clearingAnInvalidDraftSavesZeroAndRemovesThePendingError() {
        val fixture = Fixture()
        val controller = fixture.controller()
        controller.change(before, "1")
        controller.finish(before)
        controller.change(before, "")
        assertEquals("0", fixture.prices.priceMin)
        assertTrue(controller.drafts.value.isEmpty())
        assertNull(controller.nextError())
    }

    @Test
    fun previouslyStoredEmptyDraftIsRestoredAsZero() {
        val fixture = Fixture()
        fixture.stored = mapOf(before to "")
        val controller = fixture.controller()
        assertNull(controller.nextError())
        assertEquals("0", fixture.prices.priceMin)
        assertTrue(fixture.stored.isEmpty())
    }

    @Test
    fun rejectedTransactionDoesNotLoseDraft() {
        val fixture = Fixture()
        fixture.rejectWrite = true
        val controller = fixture.controller()
        controller.change(before, "1500")
        controller.finishAll()
        assertEquals("1500", fixture.stored[before])
        assertNotNull(controller.nextError())
        assertEquals("1235", fixture.prices.priceMin)
    }
}
