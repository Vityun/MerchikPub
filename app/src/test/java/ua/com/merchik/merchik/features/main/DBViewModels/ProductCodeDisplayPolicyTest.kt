package ua.com.merchik.merchik.features.main.DBViewModels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.com.merchik.merchik.Globals.OptionControlName
import ua.com.merchik.merchik.data.TovarOptions

class ProductCodeDisplayPolicyTest {
    private val allOptions = OptionControlName.values().map { field ->
        TovarOptions(field, field.name, field.name, field.name, "main", field.ordinal)
    }

    @Test fun shortClickShowsBothMeasurementsEvenWithoutRequiredOptions() {
        val options = ProductCodeDisplayPolicy(1182, false)
            .editorOptions(emptyList(), allOptions, false)
        assertEquals(listOf(OptionControlName.AMOUNT, OptionControlName.FACE), fields(options))
    }

    @Test fun longClickShowsTheSameTwoMeasurements() {
        val policy = ProductCodeDisplayPolicy(1182, false)
        val required = allOptions.filter { it.getOptionControlName() == OptionControlName.PRICE }
        val short = policy.editorOptions(required, allOptions, false)
        val long = policy.editorOptions(required, allOptions, true)
        assertEquals(fields(short), fields(long))
        assertEquals(listOf(OptionControlName.AMOUNT, OptionControlName.FACE), fields(long))
    }

    @Test fun shelfLengthCorrectionShowsOnlyMeasurementsForAnyVisitTheme() {
        for (themeId in listOf(0, 977, 998, 1182)) {
            val policy = ProductCodeDisplayPolicy(themeId, false, shelfLengthCorrection = true)
            for (showAll in listOf(false, true)) {
                assertEquals(listOf(OptionControlName.AMOUNT, OptionControlName.FACE),
                    fields(policy.editorOptions(emptyList(), allOptions, showAll)))
            }
            assertEquals(setOf(OptionControlName.AMOUNT, OptionControlName.FACE),
                OptionControlName.values().toSet() - policy.hiddenFields)
            assertFalse(policy.showPhoto)
            assertEquals(listOf(OptionControlName.AMOUNT, OptionControlName.FACE), policy.requiredCodeFields)
            assertTrue(policy.expandByDefault)
        }
    }

    @Test fun marketingResearchTabHasTheSameRequiredCodeAndDefaultExpansionAsCorrection() {
        val tab = ProductCodeDisplayPolicy(1182, false)
        val correction = ProductCodeDisplayPolicy(1182, false, shelfLengthCorrection = true)
        assertEquals(listOf(OptionControlName.AMOUNT, OptionControlName.FACE), tab.requiredCodeFields)
        assertEquals(correction.requiredCodeFields, tab.requiredCodeFields)
        assertEquals(correction.expandByDefault, tab.expandByDefault)
        assertTrue(tab.expandByDefault)
    }

    @Test fun otherContextsKeepRequiredCodeFieldsFromTheirDocument() {
        for (themeId in listOf(0, 977, 998)) {
            for (priceCorrection in listOf(false, true)) {
                assertTrue(ProductCodeDisplayPolicy(themeId, priceCorrection).requiredCodeFields.isEmpty())
            }
        }
    }

    @Test fun regularTabsStayCollapsedButPriceCorrectionStillExpandsByDefault() {
        for (themeId in listOf(0, 977, 998)) {
            assertFalse(ProductCodeDisplayPolicy(themeId, false).expandByDefault)
            assertTrue(ProductCodeDisplayPolicy(themeId, true).expandByDefault)
        }
    }

    @Test fun shelfLengthCorrectionUsesTheSameDocumentTitleAndFallback() {
        val policy = ProductCodeDisplayPolicy(998, false, shelfLengthCorrection = true)
        assertEquals("Контроль довжини полиць (метрiв)", policy.amountTitle("Контроль довжини полиць"))
        assertEquals("Длина полочного пространства (метрiв)", policy.amountTitle(null))
    }

    @Test fun collapsedCodeHidesEveryOtherField() {
        val policy = ProductCodeDisplayPolicy(1182, false)
        assertEquals(setOf(OptionControlName.AMOUNT, OptionControlName.FACE),
            OptionControlName.values().toSet() - policy.hiddenFields)
        assertFalse(policy.showPhoto)
    }

    @Test fun regularVisitRetainsRequiredAndFullModes() {
        val policy = ProductCodeDisplayPolicy(998, false)
        val required = allOptions.take(2)
        assertEquals(fields(required), fields(policy.editorOptions(required, allOptions, false)))
        assertEquals(fields(allOptions), fields(policy.editorOptions(required, allOptions, true)))
        assertTrue(policy.hiddenFields.isEmpty())
        assertTrue(policy.showPhoto)
    }

    @Test fun theme977DoesNotEnableMarketingResearchMode() {
        val policy = ProductCodeDisplayPolicy(977, false)
        assertFalse(policy.measurementOnly)
        assertTrue(policy.showPhoto)
        assertTrue(policy.hiddenFields.isEmpty())
    }

    @Test fun priceCorrectionStillHidesFacesAndRaisedStockInBothModes() {
        val policy = ProductCodeDisplayPolicy(998, true)
        for (showAll in listOf(false, true)) {
            val fields = fields(policy.editorOptions(allOptions, allOptions, showAll))
            assertFalse(fields.contains(OptionControlName.FACE))
            assertFalse(fields.contains(OptionControlName.UP))
            assertTrue(fields.contains(OptionControlName.PRICE))
            assertTrue(fields.contains(OptionControlName.PRICE_BEFORE_PROMOTION))
        }
        assertEquals(setOf(OptionControlName.UP, OptionControlName.FACE), policy.hiddenFields)
        assertTrue(policy.showPhoto)
    }

    @Test fun marketingResearchKeepsFacesEvenInPriceCorrectionContext() {
        val policy = ProductCodeDisplayPolicy(1182, true)
        assertEquals(listOf(OptionControlName.AMOUNT, OptionControlName.FACE),
            fields(policy.editorOptions(emptyList(), allOptions, true)))
        assertFalse(policy.hiddenFields.contains(OptionControlName.FACE))
        assertFalse(policy.showPhoto)
    }

    @Test fun amountUsesDocumentOptionTitleWithMeters() {
        assertEquals("Замер полки (метрiв)",
            ProductCodeDisplayPolicy(1182, false).amountTitle("  Замер полки  "))
    }

    @Test fun missingOrBlankTitleFallsBackToShelfLength() {
        val policy = ProductCodeDisplayPolicy(1182, false)
        for (title in listOf(null, "", "   ")) {
            assertEquals("Длина полочного пространства (метрiв)", policy.amountTitle(title))
        }
    }

    @Test fun templatesKeepTheirOriginalFieldsAndTitles() {
        val amount = allOptions.first { it.getOptionControlName() == OptionControlName.AMOUNT }
        val policy = ProductCodeDisplayPolicy(1182, false)
        val options = policy.editorOptions(emptyList(), allOptions, false)
        policy.amountTitle("Замер полки")
        assertSame(amount, options.first())
        assertEquals("AMOUNT", amount.getOptionLong())
        assertEquals("AMOUNT", amount.getOrderField())
    }

    private fun fields(options: List<TovarOptions>) = options.map { it.getOptionControlName() }
}
