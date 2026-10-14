package ua.com.merchik.merchik.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public final class WpDataDateMismatch {
    private static final DateTimeFormatter PREFIX_DATE = DateTimeFormatter.ofPattern("dd.MM", Locale.ROOT);
    private static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ofPattern("dd.MM.yy", Locale.ROOT);

    public final long codeDad2;
    public final LocalDate expectedDate;
    public final LocalDate actualDate;
    private final long dtMillis;
    private final ZoneId zone;

    private WpDataDateMismatch(long codeDad2, LocalDate expectedDate, LocalDate actualDate,
                              long dtMillis, ZoneId zone) {
        this.codeDad2 = codeDad2;
        this.expectedDate = expectedDate;
        this.actualDate = actualDate;
        this.dtMillis = dtMillis;
        this.zone = zone;
    }

    public static WpDataDateMismatch find(long codeDad2, Date dt, ZoneId zone) {
        LocalDate expected = dateFromDad2(codeDad2);
        if (expected == null || dt == null) return null;

        long millis = dt.getTime();
        LocalDate actual = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate();
        return expected.equals(actual) ? null
                : new WpDataDateMismatch(codeDad2, expected, actual, millis, zone);
    }

    static LocalDate dateFromDad2(long codeDad2) {
        String dad2 = Long.toString(codeDad2);
        if (dad2.length() != 19 || dad2.charAt(0) != '1') return null;
        try {
            // DAD2 starts with 1 + ddMMyy; the remaining digits identify the visit.
            return LocalDate.of(2000 + Integer.parseInt(dad2.substring(5, 7)),
                    Integer.parseInt(dad2.substring(3, 5)),
                    Integer.parseInt(dad2.substring(1, 3)));
        } catch (DateTimeException | NumberFormatException ignored) {
            return null;
        }
    }

    public String archiveName(LocalDate capturedOn) {
        return "WpData_" + actualDate.format(PREFIX_DATE)
                + "_m_log_" + capturedOn.format(LOG_DATE) + ".v01.gz";
    }

    public String diagnostic(String wpJson, int currentUserId) {
        return "WpDataDateMismatch: dad2=" + codeDad2
                + ", expectedDate=" + expectedDate + ", actualDate=" + actualDate
                + ", dtMillis=" + dtMillis + ", timezone=" + zone.getId()
                + ", currentUserId=" + currentUserId + "\nwp=" + wpJson;
    }

    public static void writeSnapshot(File source, File target, String diagnostic) throws IOException {
        try (FileOutputStream output = new FileOutputStream(target)) {
            if (source.isFile()) {
                try (RandomAccessFile input = new RandomAccessFile(source, "r")) {
                    // Other threads keep writing M_LOG: copy only the size observed at opening.
                    long remaining = input.length();
                    byte[] buffer = new byte[8192];
                    while (remaining > 0) {
                        int count = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        if (count < 0) break;
                        output.write(buffer, 0, count);
                        remaining -= count;
                    }
                }
            }
            // Keep the snapshot even if M_LOG was rotated or its writer failed.
            output.write(("\n" + diagnostic + "\n").getBytes(StandardCharsets.UTF_8));
        }
    }
}
