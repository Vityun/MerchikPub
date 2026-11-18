package ua.com.merchik.merchik.Options.Controls;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/** Calendar days before the report date; reclamations are dated by their posting time. */
final class PremiumReclamationPeriod {
    final long fromSeconds;
    final long toExclusiveSeconds;

    PremiumReclamationPeriod(Date documentDate, TimeZone zone) {
        Calendar day = Calendar.getInstance(zone);
        day.setTime(documentDate);
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);
        toExclusiveSeconds = day.getTimeInMillis() / 1000;
        day.add(Calendar.DAY_OF_YEAR, -15);
        fromSeconds = day.getTimeInMillis() / 1000;
    }

    static boolean afterTwentiethReport(Long postedSeconds, Date twentiethReport) {
        return postedSeconds != null && postedSeconds > 0 && twentiethReport != null
                && postedSeconds > twentiethReport.getTime() / 1000;
    }
}
