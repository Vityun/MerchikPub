package ua.com.merchik.merchik.Options.Controls;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import ua.com.merchik.merchik.data.Database.Room.ResultsDynamicSDB;

import static org.junit.Assert.*;

public class FaceCountPolicyTest {
    private static final LocalDate ACTIVE = LocalDate.of(2026, 11, 2);

    @Test public void onlyExplicitPromotionPresenceCounts() {
        assertTrue(product("1", "10", "1").promotion);
        assertTrue(product("1", "10", " 1 ").promotion);
        for (String value : new String[]{null, "", "0", "2", "186"}) {
            assertFalse(product("1", "10", value).promotion);
        }
    }

    @Test public void skuCountsOnlyPositiveFacesButKeepsAllProducts() {
        FaceCountPolicy.Visit visit = visit(product("1", "6", "0"),
                product("2", "0", "2"), product("3", "", null));
        assertEquals(3, visit.products.size());
        assertEquals(1, visit.sku);
        assertEquals("6", FaceCountPolicy.text(visit.faces));
        assertEquals("6", FaceCountPolicy.text(visit.facesPerSku));
    }

    @Test public void foldsSameProductAndPromotionBeforeCountingSku() {
        FaceCountPolicy.Visit visit = visit(product("001", "2", "1"),
                product("1", "4", "1"), product("2", "3", "2"));
        assertEquals(2, visit.products.size());
        assertEquals(2, visit.sku);
        assertEquals(1, visit.promotions);
        assertEquals("9", FaceCountPolicy.text(visit.faces));
        assertEquals("4.5", FaceCountPolicy.text(visit.facesPerSku));
    }

    @Test public void differentPromotionPresenceKeepsSeparateGroups() {
        FaceCountPolicy.Visit visit = visit(product("1", "2", "1"), product("1", "4", "2"));
        assertEquals(2, visit.products.size());
        assertEquals(1, visit.promotions);
    }

    @Test public void networkUsesRatioOfTotalsNotAverageOfRatios() {
        FaceCountPolicy.Network network = FaceCountPolicy.network(Arrays.asList(
                history("1", "100", "1", "5"), history("2", "100", "99", "5")));
        assertEquals("2", FaceCountPolicy.text(network.facesPerSku));
        assertEquals("10", FaceCountPolicy.text(network.documents));
        assertEquals(2, network.rows);
    }

    @Test public void networkFoldsAddressAcrossMonthsWithoutDroppingTotals() {
        FaceCountPolicy.Network network = FaceCountPolicy.network(Arrays.asList(
                history("1", "10", "5", "6"), history("1", "30", "15", "7")));
        assertEquals(1, network.rows);
        assertEquals("40", FaceCountPolicy.text(network.faces));
        assertEquals("13", FaceCountPolicy.text(network.documents));
    }

    @Test public void ratiosRoundHalfUpToTwoDecimalPlaces() {
        assertEquals("1.67", FaceCountPolicy.text(visit(product("1", "1", "0"),
                product("2", "2", "0"), product("3", "2", "0")).facesPerSku));
        assertEquals("1.01", FaceCountPolicy.text(FaceCountPolicy.network(Collections.singletonList(
                history("1", "201", "200", "10"))).facesPerSku));
    }

    @Test public void emptyFacesAndZeroDenominatorsProduceZero() {
        assertEquals("0", FaceCountPolicy.text(visit(product("1", null, null)).facesPerSku));
        assertEquals("0", FaceCountPolicy.text(FaceCountPolicy.network(Collections.singletonList(
                history("1", "50", "0", "10"))).facesPerSku));
    }

    @Test public void supportsCommaDecimals() {
        assertEquals("2.5", FaceCountPolicy.text(visit(product("1", " 2,5 ", "0")).facesPerSku));
    }

    @Test(expected = NumberFormatException.class) public void malformedFaceDoesNotBecomeValidZero() {
        product("1", "broken", "0");
    }

    @Test public void periodMatchesBeginningOfMonthTwentyDaysBeforeVisit() {
        assertEquals("2609", FaceCountPolicy.monthFrom(LocalDate.of(2026, 10, 20)));
        assertEquals("2610", FaceCountPolicy.monthFrom(LocalDate.of(2026, 10, 21)));
        assertEquals("2610", FaceCountPolicy.monthTo(LocalDate.of(2026, 10, 20)));
        assertEquals("2612", FaceCountPolicy.monthFrom(LocalDate.of(2027, 1, 1)));
        assertEquals("2701", FaceCountPolicy.monthTo(LocalDate.of(2027, 1, 1)));
    }

    @Test public void visitDateUsesKievCalendarDay() {
        assertEquals(LocalDate.of(2026, 10, 30), FaceCountPolicy.visitDate(
                Instant.parse("2026-10-29T22:30:00Z").toEpochMilli()));
    }

    @Test public void insufficientVisitTakesPriorityOverMissingHistory() {
        FaceCountPolicy.Result result = evaluate(visit(product("1", "500", "0")),
                FaceCountPolicy.network(Collections.emptyList()), ACTIVE, true);
        assertFalse(result.signal);
        assertEquals("Данные о наличии товаров на ТТ по данному отчету не репрезентативны. Расчет пропускаем!", result.message);
    }

    @Test public void emptyVisitDoesNotNeedMaximumProduct() {
        FaceCountPolicy.Visit visit = FaceCountPolicy.visit(Collections.emptyList());
        assertNull(FaceCountPolicy.maximum(visit));
        assertFalse(evaluate(visit, baseline("2", "10"), ACTIVE, true).signal);
    }

    @Test public void fewerThanTenDocumentsSkipsEvenObviousError() {
        FaceCountPolicy.Result result = evaluate(uniform("100"), baseline("2", "9"), ACTIVE, true);
        assertFalse(result.signal);
        assertEquals("Данные о наличии товаров на ТТ по данной сети не репрезентативны. Расчет пропускаем!", result.message);
    }

    @Test public void emptyHistorySkipsCalculation() {
        assertFalse(evaluate(uniform("100"), FaceCountPolicy.network(Collections.emptyList()), ACTIVE, true).signal);
    }

    @Test public void exactlyTenDocumentsAllowsCheck() {
        assertTrue(evaluate(uniform("100"), baseline("2", "10"), ACTIVE, false).signal);
    }

    @Test public void exactlyHalfAverageIsAllowed() {
        assertFalse(evaluate(uniform("2"), baseline("4", "10"), ACTIVE, true).signal);
    }

    @Test public void lessThanHalfCreatesSignalWithoutForcingBlock() {
        FaceCountPolicy.Result result = evaluate(uniform("1.99"), baseline("4", "10"), ACTIVE, false);
        assertTrue(result.signal);
        assertFalse(result.blockPns);
        assertEquals("Данные о колич. Фейсов/СКЮ (1.99) намного меньше, чем среднее по этому Клиент/Сеть: 4. Отчет скорее всего не корректный.", result.message);
    }

    @Test public void roundingOccursBeforeThresholdComparison() {
        assertFalse(evaluate(uniform("1.999"), baseline("4", "10"), ACTIVE, true).signal);
    }

    @Test public void lowValueCheckPrecedesPromotionExceptions() {
        FaceCountPolicy.Visit visit = visit(product("1", "1", "1"), product("2", "1", "1"), product("3", "1", "1"));
        assertTrue(evaluate(visit, baseline("4", "10"), ACTIVE, true).signal);
    }

    @Test public void exactlyDoubleAverageIsAllowed() {
        FaceCountPolicy.Result result = evaluate(uniform("4"), baseline("2", "10"), ACTIVE, true);
        assertFalse(result.signal);
        assertEquals("Данные о колич. Фейсов/СКЮ (4) мало отличаются от среднего по этому Клиент/Сеть: 2. Зауважень нема.", result.message);
    }

    @Test public void overDoubleWithoutPromotionDoesNotForceBlock() {
        FaceCountPolicy.Result result = evaluate(uniform("4.01"), baseline("2", "10"), ACTIVE, false);
        assertTrue(result.signal);
        assertFalse(result.blockPns);
        assertEquals("Данные о колич. Фейсов/СКЮ (4.01) заметно больше, чем среднее по этому Клиент/Сеть: 2 и при этом ни по одному товару не проводится акция! Отчет скорее всего не корректный.", result.message);
    }

    @Test public void exactlyFiveTimesUsesDoubleBranchNotForcedBlock() {
        FaceCountPolicy.Result result = evaluate(uniform("10"), baseline("2", "10"), ACTIVE, false);
        assertTrue(result.signal);
        assertFalse(result.blockPns);
        assertTrue(result.message.contains("ни по одному товару"));
    }

    @Test public void overFiveTimesWithoutPromotionForcesBlock() {
        FaceCountPolicy.Result result = evaluate(uniform("10.01"), baseline("2", "10"), ACTIVE, false);
        assertTrue(result.signal);
        assertTrue(result.blockPns);
        assertEquals("Данные о колич. Фейсов/СКЮ (10.01) намного больше, чем среднее по этому Клиент/Сеть: 2 и нет акции! Отчет наверняка не корректный.", result.message);
    }

    @Test public void beforeAndOnNovemberFirstEvenForcedBlockIsDisabled() {
        for (LocalDate date : Arrays.asList(LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 1))) {
            FaceCountPolicy.Result result = evaluate(uniform("100"), baseline("2", "10"), date, true);
            assertTrue(result.signal);
            assertFalse(result.blockPns);
        }
    }

    @Test public void configuredBlockAppliesFromNovemberSecond() {
        assertTrue(evaluate(uniform("5"), baseline("2", "10"), ACTIVE, true).blockPns);
    }

    @Test public void maximumProductWithoutPromotionCheckedFromOctoberThirtieth() {
        FaceCountPolicy.Visit visit = visit(product("1", "30", "2"), product("2", "3", "1"), product("3", "3", "0"));
        assertFalse(evaluate(visit, baseline("2", "10"), LocalDate.of(2026, 10, 29), true).signal);
        FaceCountPolicy.Result result = evaluate(visit, baseline("2", "10"), LocalDate.of(2026, 10, 30), true);
        assertTrue(result.signal);
        assertFalse(result.blockPns);
        assertEquals("Данные о колич. Фейсов/СКЮ (12) заметно больше, чем среднее по этому Клиент/Сеть: 2. Отчет скорее всего не корректный.", result.message);
    }

    @Test public void promotionElsewhereDoesNotForceFiveTimesBlock() {
        FaceCountPolicy.Visit visit = visit(product("1", "30", "2"), product("2", "3", "1"), product("3", "3", "0"));
        FaceCountPolicy.Result result = evaluate(visit, baseline("2", "10"), ACTIVE, false);
        assertTrue(result.signal);
        assertFalse(result.blockPns);
    }

    @Test public void promotionOnMaximumLeavesWarningWithoutSignal() {
        FaceCountPolicy.Visit visit = visit(product("1", "30", "1"), product("2", "3", "0"), product("3", "3", "2"));
        FaceCountPolicy.Result result = evaluate(visit, baseline("2", "10"), ACTIVE, true);
        assertFalse(result.signal);
        assertEquals("Данные о колич. Фейсов/СКЮ (12) заметно больше, чем среднее по этому Клиент/Сеть: 2 но при этом есть 1 акций! Отчет не мешало бы проверить!", result.message);
    }

    @Test public void promotionIsCountedEvenForProductWithZeroFacesLikeInOneC() {
        FaceCountPolicy.Visit visit = visit(product("1", "30", "2"), product("2", "3", "0"), product("3", "0", "1"));
        assertEquals(1, visit.promotions);
        assertFalse(evaluate(visit, baseline("2", "10"), LocalDate.of(2026, 10, 29), true).signal);
    }

    @Test public void maximumPreservesFirstProductWhenFacesEqual() {
        FaceCountPolicy.Visit visit = visit(product("2", "10", "1"), product("1", "10", "2"), product("3", "1", "0"));
        assertEquals("2", FaceCountPolicy.maximum(visit).id);
        assertFalse(evaluate(visit, baseline("2", "10"), ACTIVE, true).signal);
    }

    @Test public void zeroBaselineFollowsSameOrderedBranchesAsOneC() {
        FaceCountPolicy.Network network = FaceCountPolicy.network(Collections.singletonList(history("1", "0", "0", "10")));
        FaceCountPolicy.Result result = evaluate(uniform("1"), network, ACTIVE, false);
        assertTrue(result.signal);
        assertTrue(result.blockPns);
    }

    @Test public void conductMessagesMatchOneCForAllStates() {
        assertEquals("", FaceCountPolicy.conductMessage(false, true, false));
        assertEquals(" Документ проведеним не буде!", FaceCountPolicy.conductMessage(true, true, false));
        assertEquals(" Документ будет ДЕпроведен!", FaceCountPolicy.conductMessage(true, true, true));
        assertEquals(" Ви можете отримати БІЛЬШІ преміальні, якщо будете вносити коректно дет.отчет.", FaceCountPolicy.conductMessage(true, false, false));
    }

    @Test public void clientIdsAreNormalizedLikeSnapshotKeys() {
        assertEquals("5327", FaceCountPolicy.normalizeId("05327"));
        assertEquals("9382", FaceCountPolicy.normalizeId("9382"));
    }

    @Test public void formatsFiveThousandFacesWithOnlyProductLabelLinked() {
        FaceCountPolicy.Visit visit = visit(product("159773", "5000", "2"),
                product("2", "3", "1"), product("3", "3", "0"));
        FaceCountPolicy.Network network = baseline("1.79", "67");
        FaceCountPolicy.Result result = evaluate(visit, network, ACTIVE, false);
        String label = "(159773) Сік яблуко-персик 1.93л";
        FaceCountPolicy.Message message = FaceCountPolicy.formatMessage(result, visit, network, row -> label);
        assertEquals(result.message + "\n\n" + label
                + " - указано 5000 фейсов, при этом по товару нет акции.", message.text);
        assertEquals(1, message.links.size());
        FaceCountPolicy.ProductLink link = message.links.get(0);
        assertEquals("159773", link.productId);
        assertEquals(label, message.text.substring(link.start, link.end));
        assertEquals('\n', message.text.charAt(link.start - 1));
        assertTrue(message.text.substring(link.end).startsWith(" - указано 5000"));
    }

    @Test public void multipleProductsHaveSeparateLinesAndOneCommonConclusion() {
        FaceCountPolicy.Visit visit = visit(product("1", "3000", "2"), product("2", "5000", "0"),
                product("3", "100", "1"), product("4", "3", "0"));
        FaceCountPolicy.Network network = baseline("2", "10");
        FaceCountPolicy.Result result = evaluate(visit, network, ACTIVE, false);
        FaceCountPolicy.Message message = FaceCountPolicy.formatMessage(result, visit, network,
                row -> FaceCountPolicy.productLabel(row.id, "Сік", "1л"));
        assertEquals(2, message.links.size());
        assertEquals("2", message.links.get(0).productId);
        assertEquals("1", message.links.get(1).productId);
        for (FaceCountPolicy.ProductLink link : message.links) {
            assertEquals('\n', message.text.charAt(link.start - 1));
            assertEquals("(" + link.productId + ") Сік 1л", message.text.substring(link.start, link.end));
        }
        String conclusion = "Отчет скорее всего не корректный.";
        assertEquals(message.text.indexOf(conclusion), message.text.lastIndexOf(conclusion));
        assertTrue(message.text.contains(conclusion));
        assertFalse(message.text.contains("(3)"));
        assertFalse(message.text.contains("(4)"));
        assertTrue(result.signal);
        assertFalse(result.blockPns);
    }

    @Test public void productLinksUseStrictDoubleThresholdAndStableTieOrder() {
        FaceCountPolicy.Visit visit = visit(product("2", "5", "0"), product("1", "5", "2"), product("3", "4", "0"));
        FaceCountPolicy.Network network = baseline("2", "10");
        FaceCountPolicy.Message message = FaceCountPolicy.formatMessage(evaluate(visit, network, ACTIVE, false),
                visit, network, row -> "(" + row.id + ") Product");
        assertEquals(2, message.links.size());
        assertEquals("2", message.links.get(0).productId);
        assertEquals("1", message.links.get(1).productId);
        assertFalse(message.text.contains("(3) Product"));
    }

    @Test public void missingHistoryAndPromotionExceptionDoNotAddProductAccusations() {
        FaceCountPolicy.Visit visit = visit(product("1", "5000", "1"), product("2", "3000", "0"), product("3", "2", "0"));
        for (FaceCountPolicy.Network network : Arrays.asList(baseline("2", "10"), baseline("2", "9"))) {
            FaceCountPolicy.Result result = evaluate(visit, network, ACTIVE, false);
            FaceCountPolicy.Message message = FaceCountPolicy.formatMessage(result, visit, network, row -> row.id);
            assertEquals(result.message, message.text);
            assertTrue(message.links.isEmpty());
        }
    }

    @Test public void lowFacesSignalDoesNotListHighFaceCandidates() {
        FaceCountPolicy.Visit visit = uniform("1");
        FaceCountPolicy.Network network = baseline("4", "10");
        FaceCountPolicy.Result result = evaluate(visit, network, ACTIVE, false);
        assertTrue(result.signal);
        assertTrue(FaceCountPolicy.formatMessage(result, visit, network, row -> row.id).links.isEmpty());
    }

    @Test public void productLabelHasCodeNameAndWeightWithoutNulls() {
        assertEquals("(159773) Сік 1.93л", FaceCountPolicy.productLabel("159773", " Сік ", " 1.93л "));
        assertEquals("(159773)", FaceCountPolicy.productLabel("159773", null, null));
        assertEquals("(159773) Сік", FaceCountPolicy.productLabel("159773", "Сік", ""));
    }

    @Test public void faceInputNormalizesNumbersIncludingZeroForTheExistingSaveGuard() {
        assertEquals("5", FaceCountPolicy.faceInput(" 5.00 "));
        assertEquals("0", FaceCountPolicy.faceInput("0.0"));
        assertEquals("0", FaceCountPolicy.faceInput("0,00"));
        assertEquals("2.5", FaceCountPolicy.faceInput("2,5"));
    }

    @Test public void invalidFaceInputCannotBeStored() {
        for (String input : Arrays.asList(null, "", " ", ".", "NaN", "-1", "1.2.3")) {
            try {
                FaceCountPolicy.faceInput(input);
                fail("Accepted invalid input: " + input);
            } catch (IllegalArgumentException expected) {
                // The editor must reject this before writing ReportPrepare.
            }
        }
    }

    private static FaceCountPolicy.Product product(String id, String faces, String akciya) {
        return new FaceCountPolicy.Product(id, faces, akciya);
    }

    private static FaceCountPolicy.Visit visit(FaceCountPolicy.Product... products) {
        return FaceCountPolicy.visit(Arrays.asList(products));
    }

    private static FaceCountPolicy.Visit uniform(String faces) {
        return visit(product("1", faces, "0"), product("2", faces, "2"), product("3", faces, null));
    }

    private static ResultsDynamicSDB history(String address, String faces, String sku, String documents) {
        ResultsDynamicSDB row = new ResultsDynamicSDB();
        row.clientId = "9382";
        row.addrId = address;
        row.totalFace = faces;
        row.totalSku = sku;
        row.totalDocuments = documents;
        return row;
    }

    private static FaceCountPolicy.Network baseline(String ratio, String documents) {
        return FaceCountPolicy.network(Collections.singletonList(history("1",
                new BigDecimal(ratio).multiply(BigDecimal.TEN).toPlainString(), "10", documents)));
    }

    private static FaceCountPolicy.Result evaluate(FaceCountPolicy.Visit visit, FaceCountPolicy.Network network,
                                                   LocalDate date, boolean block) {
        return FaceCountPolicy.evaluate(visit, network, FaceCountPolicy.maximum(visit), date, block);
    }
}
