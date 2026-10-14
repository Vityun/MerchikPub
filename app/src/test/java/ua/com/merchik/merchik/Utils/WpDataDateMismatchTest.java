package ua.com.merchik.merchik.Utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.*;

public class WpDataDateMismatchTest {
    private static final long DAD2 = 1121026036773061419L;
    private static final ZoneId KYIV = ZoneId.of("Europe/Kiev");

    @Rule public TemporaryFolder files = new TemporaryFolder();

    @Test
    public void extractsDateFromDad2() {
        assertEquals(LocalDate.of(2026, 10, 12), WpDataDateMismatch.dateFromDad2(DAD2));
        assertEquals(LocalDate.of(2026, 10, 2), WpDataDateMismatch.dateFromDad2(1021026038634061377L));
        assertEquals(LocalDate.of(2026, 9, 30), WpDataDateMismatch.dateFromDad2(1300926045012061223L));
    }

    @Test
    public void matchingDayDoesNotDependOnTimeOfDay() {
        LocalDate day = LocalDate.of(2026, 10, 12);
        assertNull(WpDataDateMismatch.find(DAD2, date(day), KYIV));
        assertNull(WpDataDateMismatch.find(DAD2, Date.from(day.atTime(23, 59).atZone(KYIV).toInstant()), KYIV));
    }

    @Test
    public void usesLocalCalendarDateRatherThanUtcDate() {
        Date localMidnight = date(LocalDate.of(2026, 10, 12));
        assertEquals(LocalDate.of(2026, 10, 11), localMidnight.toInstant().atZone(ZoneId.of("UTC")).toLocalDate());
        assertNull(WpDataDateMismatch.find(DAD2, localMidnight, KYIV));
    }

    @Test
    public void findsChangedDayAndDoesNotModifySourceDate() {
        Date dt = date(LocalDate.of(2026, 10, 13));
        long before = dt.getTime();
        WpDataDateMismatch mismatch = WpDataDateMismatch.find(DAD2, dt, KYIV);
        assertNotNull(mismatch);
        assertEquals(LocalDate.of(2026, 10, 12), mismatch.expectedDate);
        assertEquals(LocalDate.of(2026, 10, 13), mismatch.actualDate);
        assertEquals(before, dt.getTime());
    }

    @Test
    public void alsoComparesMonthAndYear() {
        assertNotNull(WpDataDateMismatch.find(DAD2, date(LocalDate.of(2026, 11, 12)), KYIV));
        assertNotNull(WpDataDateMismatch.find(DAD2, date(LocalDate.of(2027, 10, 12)), KYIV));
    }

    @Test
    public void invalidDad2OrMissingDateDoesNotTriggerEmail() {
        assertNull(WpDataDateMismatch.find(DAD2, null, KYIV));
        assertNull(WpDataDateMismatch.dateFromDad2(0));
        assertNull(WpDataDateMismatch.dateFromDad2(-DAD2));
        assertNull(WpDataDateMismatch.dateFromDad2(11210260367730614L));
        assertNull(WpDataDateMismatch.dateFromDad2(2121026036773061419L));
        assertNull(WpDataDateMismatch.dateFromDad2(1310226036773061419L));
        assertNull(WpDataDateMismatch.dateFromDad2(1001026036773061419L));
        assertNull(WpDataDateMismatch.dateFromDad2(1121326036773061419L));
    }

    @Test
    public void validatesLeapDayStrictly() {
        assertEquals(LocalDate.of(2024, 2, 29), WpDataDateMismatch.dateFromDad2(1290224036773061419L));
        assertNull(WpDataDateMismatch.dateFromDad2(1290226036773061419L));
    }

    @Test
    public void filenameUsesWrongVisitDateThenUsualLogName() {
        WpDataDateMismatch mismatch = WpDataDateMismatch.find(DAD2, date(LocalDate.of(2026, 10, 13)), KYIV);
        assertEquals("WpData_13.10_m_log_14.10.26.v01.gz", mismatch.archiveName(LocalDate.of(2026, 10, 14)));
    }

    @Test
    public void snapshotContainsOriginalLogAndFullWpJson() throws Exception {
        File source = files.newFile("M_LOG.txt");
        File target = files.newFile("snapshot.txt");
        Files.write(source.toPath(), "existing log\n".getBytes(StandardCharsets.UTF_8));
        Date dt = date(LocalDate.of(2026, 10, 13));
        WpDataDateMismatch mismatch = WpDataDateMismatch.find(DAD2, dt, KYIV);
        String json = "{\"code_dad2\":1121026036773061419,\"dt\":\"Oct 13, 2026\",\"addr_txt\":\"Address\"}";
        WpDataDateMismatch.writeSnapshot(source, target, mismatch.diagnostic(json, 238572));
        String text = new String(Files.readAllBytes(target.toPath()), StandardCharsets.UTF_8);
        assertTrue(text.startsWith("existing log\n"));
        assertTrue(text.contains("expectedDate=2026-10-12, actualDate=2026-10-13"));
        assertTrue(text.contains("dtMillis=" + dt.getTime()));
        assertTrue(text.contains("timezone=Europe/Kiev"));
        assertTrue(text.contains("currentUserId=238572"));
        assertTrue(text.contains("wp=" + json));
        assertEquals("existing log\n", new String(Files.readAllBytes(source.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void missingLogStillProducesDiagnosticSnapshot() throws Exception {
        File target = files.newFile("snapshot.txt");
        WpDataDateMismatch.writeSnapshot(new File(files.getRoot(), "missing.txt"), target, "full wp snapshot");
        assertEquals("\nfull wp snapshot\n", new String(Files.readAllBytes(target.toPath()), StandardCharsets.UTF_8));
    }

    private static Date date(LocalDate day) {
        return Date.from(day.atStartOfDay(KYIV).toInstant());
    }
}
