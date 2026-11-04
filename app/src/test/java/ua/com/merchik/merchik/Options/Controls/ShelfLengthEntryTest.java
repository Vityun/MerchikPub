package ua.com.merchik.merchik.Options.Controls;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;

import static org.junit.Assert.*;

public class ShelfLengthEntryTest {
    private static ReportPrepareDB reportRow(String id, String face, int metres) {
        ReportPrepareDB row = new ReportPrepareDB();
        row.setTovarId(id);
        row.setKli("9382");
        row.setFace(face);
        row.setAmount(metres);
        return row;
    }

    private static Map<String, ShelfLengthPolicy.Measurement> check(ReportPrepareDB row, TovarDB card, String mainOption) {
        return OptionControlShelfLength.measurements(Collections.singletonList(row),
                card == null ? Collections.emptyMap() : Collections.singletonMap(row.getTovarId(), card),
                "9382", mainOption);
    }

    @Test public void visit03243435LowDensityNamesUnknownGroup() {
        Map<String, ShelfLengthPolicy.Measurement> products = check(reportRow("175194", "20", 20), null, "1465");
        assertEquals(1, products.size());
        ShelfLengthPolicy.Measurement m = products.get("175194");
        assertNull(m.groupId);
        assertTrue(m.issue(m.groupLabel(null), null).contains("Не визначено на одном метре полки стоит 1.0 фейсов"));
        assertEquals("1/1", ShelfLengthPolicy.progress(products.values()));
    }

    @Test public void visit03243434MissingMeasurementsAreNotZeroPlannedGroups() {
        ShelfLengthPolicy.Measurement m = check(reportRow("175194", "0", 0), null, "1465").get("175194");
        assertTrue(m.issue(m.groupLabel(null), null).contains("Не визначено не указаны ни КОЛИЧЕСТВО"));
        assertEquals("0/1", ShelfLengthPolicy.progress(Collections.singletonList(m)));
    }

    @Test public void missingCardExceptionIsLimitedToThisProductTaskAndClient() {
        assertTrue(check(reportRow("123", "20", 20), null, "1465").isEmpty());
        ReportPrepareDB row = reportRow("175194", "20", 20);
        assertTrue(check(row, null, "574").isEmpty());
        row.setKli("999");
        assertTrue(check(row, null, "1465").isEmpty());
    }

    @Test public void groupAndManufacturerComeFromTheCardWhenPresent() {
        ReportPrepareDB row = reportRow("175194", "20", 20);
        TovarDB card = new TovarDB();
        card.setiD("175194");
        card.setGroupId("88");
        card.setManufacturerId("3094");
        assertTrue(check(row, card, "1465").isEmpty());
        card.setManufacturerId("5491");
        ShelfLengthPolicy.Measurement m = check(row, card, "1465").get("175194");
        assertEquals(Integer.valueOf(88), m.groupId);
        assertEquals("(Название из справочника [88])", m.groupLabel("Название из справочника"));
        assertEquals("(Не визначено [88])", m.groupLabel(" "));
    }

    @Test public void missingOrInvalidCatalogGroupIdDoesNotInventAGroup() {
        ReportPrepareDB row = reportRow("175194", "20", 20);
        TovarDB card = new TovarDB();
        card.setiD("175194");
        card.setManufacturerId("5491");
        for (String group : new String[]{null, "", "0", "?"}) {
            card.setGroupId(group);
            ShelfLengthPolicy.Measurement m = check(row, card, "1465").get("175194");
            assertNull(m.groupId);
            assertEquals("Не визначено", m.groupLabel(null));
        }
    }

    @Test public void pickerUsesProductIdWhenCardIsMissingOnlyInItsVisitTask() {
        List<ReportPrepareDB> rows = Arrays.asList(reportRow("175194", "20", 20), reportRow("123", "20", 20));
        Map<String, TovarDB> noCards = Collections.emptyMap();
        assertEquals(Collections.singletonMap("175194", "(175194)"),
                ShelfLengthEntry.choices(rows, noCards, "9382", "1465"));
        assertTrue(ShelfLengthEntry.choices(rows, noCards, "9382", "574").isEmpty());
        assertTrue(ShelfLengthEntry.choices(rows, noCards, "111", "1465").isEmpty());
    }

    @Test public void counterCountsPartialInputOncePerEligibleProduct() {
        TovarDB measured = new TovarDB();
        measured.setManufacturerId("5491");
        List<ReportPrepareDB> rows = Arrays.asList(
                reportRow("175194", "20", 0), reportRow("175194", "20", 0),
                reportRow("10", "0", 4), reportRow("11", "", 0), reportRow("123", "99", 99));
        Map<String, TovarDB> cards = new java.util.HashMap<>();
        cards.put("10", measured);
        cards.put("11", measured);
        Map<String, ShelfLengthPolicy.Measurement> products = OptionControlShelfLength.measurements(rows, cards, "9382", "1465");
        assertEquals("2/3", ShelfLengthPolicy.progress(products.values()));
        assertEquals("0/0", ShelfLengthPolicy.progress(Collections.emptyList()));
    }

    @Test public void editorRechecksExistingRowAndDoesNotOverrideARealCard() {
        ReportPrepareDB rp = reportRow("175194", "20", 20);
        assertTrue(ShelfLengthEntry.eligible("9382", "1465", rp, null));
        assertFalse(ShelfLengthEntry.eligible("9382", "1465", null, null));
        TovarDB card = new TovarDB();
        card.setManufacturerId("3094");
        assertFalse(ShelfLengthEntry.eligible("9382", "1465", rp, card));
    }

    @Test public void onlyVisitClientAndMeasurementManufacturerAreEligible() {
        ReportPrepareDB rp = new ReportPrepareDB();
        rp.setKli("9382");
        TovarDB card = new TovarDB();
        card.setManufacturerId("3094");
        assertFalse(ShelfLengthEntry.eligible("9382", "1465", rp, card));
        card.setManufacturerId("5491");
        assertTrue(ShelfLengthEntry.eligible("9382", "1465", rp, card));
        assertFalse(ShelfLengthEntry.eligible("111", "1465", rp, card));
        assertFalse(ShelfLengthEntry.eligible("9382", "1465", null, card));
    }

    @Test public void fallsBackToCardClientOnlyWhenRowClientIsMissing() {
        assertTrue(ShelfLengthPolicy.eligible("9382", null, "9382", "5491"));
        assertFalse(ShelfLengthPolicy.eligible("9382", "111", "9382", "5491"));
        assertFalse(ShelfLengthPolicy.eligible("0", "", "0", "5491"));
    }

    @Test public void displayKeepsControlRowAndLeavesOtherVisitsAndButtonsAlone() {
        OptionsDB a = option("138023", "0", "1041126039410061655");
        OptionsDB b = option("138023", "1465", a.getCodeDad2());
        OptionsDB c = option("138023", "0", "1041126027710061655");
        OptionsDB other = option("138520", "0", a.getCodeDad2());
        List<OptionsDB> display = new ArrayList<>(Arrays.asList(a, other, b, c));
        ShelfLengthEntry.removeUncontrolledCopies(display);
        assertEquals(Arrays.asList(other, b, c), display);
    }

    private static OptionsDB option(String id, String control, String dad2) {
        OptionsDB option = new OptionsDB();
        option.setOptionId(id);
        option.setOptionControlId(control);
        option.setCodeDad2(dad2);
        return option;
    }
}
