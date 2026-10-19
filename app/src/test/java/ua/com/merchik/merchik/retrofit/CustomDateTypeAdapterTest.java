package ua.com.merchik.merchik.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CustomDateTypeAdapterTest {
    private static final ZoneId KYIV = ZoneId.of("Europe/Kiev");
    private TimeZone originalTimeZone;
    private Gson gson;

    @Before
    public void setUp() {
        originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new CustomDateTypeAdapter())
                .create();
    }

    @After
    public void restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    public void readsCalendarDateAsKyivMidnightWhenDeviceIsInLima() {
        assertEquals(new Date(1792098000000L), gson.fromJson("\"2026-10-16\"", Date.class));
    }

    @Test
    public void writesCalendarDateInKyivWhenDeviceIsInLima() {
        assertEquals("\"2026-10-16\"", gson.toJson(new Date(1792098000000L), Date.class));
    }

    @Test
    public void usesKyivSeasonalOffsetsRatherThanFixedOffset() {
        Date winter = Date.from(Instant.parse("2026-01-15T22:00:00Z"));
        Date summer = Date.from(Instant.parse("2026-07-15T21:00:00Z"));
        assertEquals(winter, gson.fromJson("\"2026-01-16\"", Date.class));
        assertEquals(summer, gson.fromJson("\"2026-07-16\"", Date.class));
        assertEquals("\"2026-01-16\"", gson.toJson(winter, Date.class));
        assertEquals("\"2026-07-16\"", gson.toJson(summer, Date.class));
    }

    @Test
    public void keepsKyivDateAfterDeviceTimeZoneChanges() {
        for (String zone : new String[]{"Europe/Kiev", "UTC", "Asia/Tokyo", "America/Lima"}) {
            TimeZone.setDefault(TimeZone.getTimeZone(zone));
            Date expected = new Date(1792357200000L);
            assertEquals(zone, expected, gson.fromJson("\"2026-10-19\"", Date.class));
            assertEquals(zone, "\"2026-10-19\"", gson.toJson(expected, Date.class));
        }
    }

    @Test
    public void doesNotChangeDeviceTimeZone() {
        gson.fromJson("\"2026-10-16\"", Date.class);
        gson.toJson(new Date(1792098000000L), Date.class);
        assertEquals("America/Lima", TimeZone.getDefault().getID());
    }

    @Test
    public void readsJsonNull() {
        assertNull(gson.fromJson("null", Date.class));
    }

    @Test
    public void preservesEmptyAndZeroDateHandling() {
        for (String input : new String[]{"", " ", "0", "0000-00-00", "null", "NULL"}) {
            assertNull(input, gson.fromJson(gson.toJson(input), Date.class));
        }
        assertNull(gson.fromJson("0", Date.class));
    }

    @Test
    public void trimsCalendarDates() {
        assertEquals(new Date(1792098000000L), gson.fromJson("\" 2026-10-16 \"", Date.class));
    }

    @Test
    public void preservesMillisecondTimestampStrings() {
        assertEquals(new Date(1792098000123L), gson.fromJson("\"1792098000123\"", Date.class));
    }

    @Test
    public void preservesNumericTimestampValues() {
        assertEquals(new Date(1792098000123L), gson.fromJson("1792098000123", Date.class));
    }

    @Test
    public void writesJsonNull() {
        assertEquals("null", gson.toJson(null, Date.class));
    }

    @Test
    public void formattingDoesNotModifySourceDate() {
        Date input = new Date(1792098000123L);
        assertEquals("\"2026-10-16\"", gson.toJson(input, Date.class));
        assertEquals(1792098000123L, input.getTime());
    }

    @Test(timeout = 30000)
    public void concurrentReadsDoNotMixDates() throws Exception {
        exerciseSharedAdapter(false);
    }

    @Test(timeout = 30000)
    public void concurrentReadsAndWritesDoNotMixDates() throws Exception {
        exerciseSharedAdapter(true);
    }

    private void exerciseSharedAdapter(boolean write) throws Exception {
        String[] inputs = {"2026-10-16", "2026-10-01", "2026-10-14", "2026-10-17",
                "2026-10-19", "2026-10-23", "2026-01-16", "2025-12-31"};
        ExecutorService pool = Executors.newFixedThreadPool(inputs.length);
        CountDownLatch ready = new CountDownLatch(inputs.length);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> jobs = new ArrayList<>();
        try {
            for (String input : inputs) {
                String json = "\"" + input + "\"";
                Date expected = Date.from(LocalDate.parse(input).atStartOfDay(KYIV).toInstant());
                jobs.add(pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    for (int i = 0; i < 5000; i++) {
                        assertEquals(input, expected, gson.fromJson(json, Date.class));
                        if (write) {
                            assertEquals(input, json, gson.toJson(expected, Date.class));
                        }
                    }
                    return null;
                }));
            }
            assertTrue("Workers did not start", ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            for (Future<?> job : jobs) {
                job.get(10, TimeUnit.SECONDS);
            }
        } finally {
            start.countDown();
            pool.shutdownNow();
            assertTrue("Workers did not stop", pool.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
}
