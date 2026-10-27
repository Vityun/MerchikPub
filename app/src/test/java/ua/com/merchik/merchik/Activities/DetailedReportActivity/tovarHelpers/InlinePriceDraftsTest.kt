package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers

import org.junit.Assert.*
import org.junit.Test

class InlinePriceDraftsTest {
    @Test
    fun equalPricesSaveWithoutWarningAndWithoutChangingStorageFormat() {
        val fixture = Fixture().apply { prices = InlinePriceValues("80", "95.90", "Product", 20) }
        val controller = fixture.controller()
        controller.change(current, "95.9")
        assertEquals("95.9", fixture.prices.price)
        controller.change(before, "95.899999")
        assertEquals("95.899999", fixture.prices.priceMin)
        controller.finishAll()
        assertNull(controller.nextError())
        assertTrue(controller.drafts.value.isEmpty())
        assertEquals(2, fixture.writes)
    }

    @Test
    fun disablingPromotionBeforeDeferredWarningAllowsSavingTheDraft() {
        val fixture = Fixture().apply { prices = InlinePriceValues("80", "100", "Product", 20) }
        val controller = fixture.controller()
        controller.change(before, "180")
        controller.finishAll()
        fixture.prices = fixture.prices.copy(discountPercent = 0)
        assertNull(controller.nextError())
        assertEquals("180", fixture.prices.priceMin)
        assertTrue(fixture.stored.isEmpty())
        controller.change(before, "79")
        controller.finishAll()
        assertTrue(controller.nextError()!!.draft.invalidPrice)
    }

    @Test
    fun discountWarningRetainsPromotionNameAndProductLabelWhenEditingBeforePrice() {
        val fixture = Fixture()
        fixture.prices = InlinePriceValues("80", "100", "(10) Juice 0.95 l", 20, "Summer 20%")
        val controller = fixture.controller()
        controller.change(before, "180")
        controller.finish(before)
        val prices = controller.nextError()!!.draft.prices
        assertEquals("(10) Juice 0.95 l", prices.product)
        assertEquals("Summer 20%", prices.discountCheck().promotionName)
        assertEquals("36", prices.discountCheck().expectedDiscountText())
    }

    private val current = InlinePriceField(1, "price", "10", false)
    private val before = InlinePriceField(1, "price_before_promotion", "10", true)

    private class Fixture {
        var prices = InlinePriceValues("1234", "1235", "Product")
        var stored = emptyMap<InlinePriceField, String>()
        var writes = 0
        var rejectWrite = false
        var promotion = "0"
        var priceMax = "0"
        val completed = mutableListOf<InlinePriceField>()

        fun controller(autoFill: Boolean = false) = InlinePriceDrafts(stored, { prices }, { field, value ->
            if (rejectWrite) false else {
                prices = if (field.beforePromotion) prices.copy(priceMin = value) else prices.copy(price = value)
                writes++
                true
            }
        }, { stored = it }, onInputFinished = { field ->
            completed.add(field)
            if (autoFill && !field.beforePromotion) {
                PriceSaveGuard.missingPricesForNoPromotion(promotion, prices.price, prices.priceMin, priceMax)?.let {
                    prices = prices.copy(priceMin = it)
                    priceMax = it
                }
            }
        })
    }

    @Test
    fun noPromotionSelectedFirstFillsBothPricesOnlyAfterFullInput() {
        val fixture = Fixture().apply {
            promotion = "2"
            prices = InlinePriceValues("0", "0", "Product")
        }
        val controller = fixture.controller(autoFill = true)
        for (value in listOf("2", "20", "20.5", "20.50")) {
            controller.change(current, value)
            assertEquals(value, fixture.prices.price)
            assertEquals("0", fixture.prices.priceMin)
            assertEquals("0", fixture.priceMax)
            assertTrue(fixture.completed.isEmpty())
            assertTrue(controller.drafts.value.isEmpty())
        }
        controller.finish(current)
        assertEquals("20.50", fixture.prices.priceMin)
        assertEquals("20.50", fixture.priceMax)
        assertNull(controller.nextError())
        controller.finish(current)
        controller.finishAll()
        assertEquals(listOf(current), fixture.completed)
    }

    @Test
    fun pauseCompletesAlreadySavedPriceWithoutRemainingDraft() {
        val fixture = Fixture().apply {
            promotion = "2"
            prices = InlinePriceValues("0", "0", "Product")
        }
        val controller = fixture.controller(autoFill = true)
        controller.change(current, "20")
        assertTrue(controller.drafts.value.isEmpty())
        controller.finishAll()
        assertEquals("20", fixture.prices.priceMin)
        assertEquals("20", fixture.priceMax)
        assertEquals(listOf(current), fixture.completed)
    }

    @Test
    fun incompleteCurrentPriceFillsBothPricesAfterDecimalIsFinished() {
        val fixture = Fixture().apply {
            promotion = "2"
            prices = InlinePriceValues("0", "0", "Product")
        }
        val controller = fixture.controller(autoFill = true)
        controller.change(current, "20.")
        assertEquals("0", fixture.prices.price)
        controller.finishAll()
        assertEquals("20", fixture.prices.price)
        assertEquals("20", fixture.prices.priceMin)
        assertEquals("20", fixture.priceMax)
        assertEquals(listOf(current), fixture.completed)
    }

    @Test
    fun completionPreservesPricesEnteredInAnotherEditor() {
        val fixture = Fixture().apply {
            promotion = "2"
            prices = InlinePriceValues("0", "0", "Product")
        }
        val controller = fixture.controller(autoFill = true)
        controller.change(current, "20")
        fixture.prices = fixture.prices.copy(priceMin = "30")
        fixture.priceMax = "35"
        controller.finishAll()
        assertEquals("30", fixture.prices.priceMin)
        assertEquals("35", fixture.priceMax)
    }

    @Test
    fun completionDoesNotFillPricesIfPromotionWasEnabledAgain() {
        val fixture = Fixture().apply {
            promotion = "2"
            prices = InlinePriceValues("0", "0", "Product")
        }
        val controller = fixture.controller(autoFill = true)
        controller.change(current, "20")
        fixture.promotion = "1"
        controller.finishAll()
        assertEquals("0", fixture.prices.priceMin)
        assertEquals("0", fixture.priceMax)
    }

    @Test
    fun rejectedSaveCompletesOnlyAfterSuccessfulRetry() {
        val fixture = Fixture().apply {
            promotion = "2"
            prices = InlinePriceValues("0", "0", "Product")
            rejectWrite = true
        }
        val controller = fixture.controller(autoFill = true)
        controller.change(current, "20")
        controller.finishAll()
        assertTrue(fixture.completed.isEmpty())
        assertEquals("0", fixture.prices.priceMin)
        fixture.rejectWrite = false
        assertNull(controller.nextError())
        assertEquals("20", fixture.prices.priceMin)
        assertEquals("20", fixture.priceMax)
        assertEquals(listOf(current), fixture.completed)
    }

    @Test
    fun clearingCurrentPriceDoesNotFillBeforePrices() {
        val fixture = Fixture().apply {
            promotion = "2"
            prices = InlinePriceValues("20", "0", "Product")
        }
        val controller = fixture.controller(autoFill = true)
        controller.change(current, "")
        controller.finishAll()
        assertEquals("0", fixture.prices.price)
        assertEquals("0", fixture.prices.priceMin)
        assertEquals("0", fixture.priceMax)
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

    @Test
    fun discountWarningStaysInDraftUntilEditingEndsAndCanBeOverridden() {
        val fixture = Fixture().apply { prices = InlinePriceValues("80", "100", "Product", 20) }
        val controller = fixture.controller()
        controller.change(before, "180")
        assertTrue(controller.drafts.value.getValue(before).invalidDiscount)
        assertNull(controller.nextError())
        assertEquals("100", fixture.prices.priceMin)
        controller.finish(before)
        val error = controller.nextError()!!
        controller.markShown(error)
        controller.approveDiscount(error)
        assertEquals("180", fixture.prices.priceMin)
        assertEquals(1, fixture.writes)
        assertTrue(fixture.stored.isEmpty())
        assertNull(controller.nextError())
        assertEquals(listOf(before), fixture.completed)
    }

    @Test
    fun refusalDiscardsOnlyLastFieldAndRequestsItsFocus() {
        val fixture = Fixture().apply { prices = InlinePriceValues("80", "100", "Product", 20) }
        val controller = fixture.controller()
        controller.change(before, "180")
        controller.finishAll()
        controller.refuseDiscount(controller.nextError()!!)
        assertEquals("100", fixture.prices.priceMin)
        assertEquals("80", fixture.prices.price)
        assertEquals(0, fixture.writes)
        assertTrue(fixture.stored.isEmpty())
        assertEquals(before, controller.focusRequest.value)
        controller.focusRestored(before)
        assertNull(controller.focusRequest.value)
        controller.finishAll()
        assertTrue(fixture.completed.isEmpty())
    }

    @Test
    fun discountedDraftSurvivesBackgroundAndRecreation() {
        val fixture = Fixture().apply { prices = InlinePriceValues("80", "100", "Product", 20) }
        val controller = fixture.controller()
        controller.change(current, "90")
        controller.finishAll()
        val restored = fixture.controller()
        assertTrue(restored.nextError()!!.draft.invalidDiscount)
        assertEquals("80", fixture.prices.price)
    }

    @Test
    fun changedPercentageInvalidatesPendingApproval() {
        val fixture = Fixture().apply { prices = InlinePriceValues("80", "100", "Product", 20) }
        val controller = fixture.controller()
        controller.change(before, "180")
        controller.finish(before)
        val error = controller.nextError()!!
        controller.markShown(error)
        fixture.prices = fixture.prices.copy(discountPercent = 30)
        controller.approveDiscount(error)
        assertEquals(0, fixture.writes)
        assertEquals(30, controller.nextError()!!.draft.prices.discountPercent)
    }

    @Test
    fun approvalCannotBypassOldPriceOrderErrorAfterOtherPriceChanges() {
        val fixture = Fixture().apply { prices = InlinePriceValues("80", "100", "Product", 20) }
        val controller = fixture.controller()
        controller.change(before, "180")
        controller.finish(before)
        val error = controller.nextError()!!
        fixture.prices = fixture.prices.copy(price = "200")
        controller.approveDiscount(error)
        assertEquals(0, fixture.writes)
        val pending = controller.nextError()!!.draft
        assertTrue(pending.invalidPrice)
        assertFalse(pending.invalidDiscount)
    }

    @Test
    fun correctingDiscountWarningSavesWithoutDialogAndZeroStillClearsBothPrices() {
        val fixture = Fixture().apply { prices = InlinePriceValues("80", "100", "Product", 20) }
        val controller = fixture.controller()
        controller.change(current, "9")
        assertEquals(0, fixture.writes)
        controller.change(current, "83")
        assertEquals("83", fixture.prices.price)
        assertTrue(controller.drafts.value.isEmpty())
        controller.change(current, "")
        controller.change(before, "")
        assertEquals("0", fixture.prices.price)
        assertEquals("0", fixture.prices.priceMin)
        controller.finishAll()
        assertNull(controller.nextError())
    }

    @Test
    fun refusingAnOldWarningDoesNotDiscardANewerDraft() {
        val fixture = Fixture().apply { prices = InlinePriceValues("80", "100", "Product", 20) }
        val controller = fixture.controller()
        controller.change(before, "180")
        controller.finish(before)
        val oldError = controller.nextError()!!
        controller.change(before, "190")
        controller.refuseDiscount(oldError)
        assertEquals("190", fixture.stored[before])
        assertNull(controller.focusRequest.value)
    }

    @Test
    fun rejectedConfirmedWriteKeepsDraftAndRequestsAnotherMessage() {
        val fixture = Fixture().apply { prices = InlinePriceValues("80", "100", "Product", 20) }
        val controller = fixture.controller()
        controller.change(before, "180")
        controller.finish(before)
        val error = controller.nextError()!!
        controller.markShown(error)
        fixture.rejectWrite = true
        controller.approveDiscount(error)
        assertEquals("180", fixture.stored[before])
        assertNotNull(controller.nextError())
        assertEquals("100", fixture.prices.priceMin)
    }
}
