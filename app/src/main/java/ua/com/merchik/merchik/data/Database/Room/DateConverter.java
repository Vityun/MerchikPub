package ua.com.merchik.merchik.data.Database.Room;

import androidx.room.TypeConverter;
import android.util.Log;


import java.sql.Date;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DateConverter {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final ThreadLocal<SimpleDateFormat> DF_DATE =
            new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    df.setTimeZone(GMT);
                    df.setLenient(false);
                    return df;
                }
            };

    private static final ThreadLocal<SimpleDateFormat> DF_DATE_TIME =
            new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                    df.setTimeZone(GMT);
                    df.setLenient(false);
                    return df;
                }
            };

    private static final ThreadLocal<SimpleDateFormat> DF_DATE_TIME_SECONDS =
            new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    df.setTimeZone(GMT);
                    df.setLenient(false);
                    return df;
                }
            };

    @TypeConverter
    public static Date timeToDate(String value) {
        if (value == null) return null;

        String s = value.trim();

        if (s.isEmpty()
                || s.equalsIgnoreCase("null")
                || s.equals("0000-00-00")
                || s.equals("0000-00-00 00:00")
                || s.equals("0000-00-00 00:00:00")) {
            return null;
        }

        java.util.Date parsed;

        // Сначала полный формат, потом короче
        parsed = parseExact(DF_DATE_TIME_SECONDS.get(), s);
        if (parsed != null) return new Date(parsed.getTime());

        parsed = parseExact(DF_DATE_TIME.get(), s);
        if (parsed != null) return new Date(parsed.getTime());

        parsed = parseExact(DF_DATE.get(), s);
        if (parsed != null) return new Date(parsed.getTime());

        Log.e("DateConverter", "Cannot parse date value: [" + value + "]");
        return null;
    }

    private static java.util.Date parseExact(SimpleDateFormat format, String value) {
        try {
            ParsePosition position = new ParsePosition(0);
            java.util.Date date = format.parse(value, position);

            if (date == null) return null;

            // Запрещаем частичный парсинг.
            // Например yyyy-MM-dd не должен успешно парсить "2026-06-05 12:30"
            if (position.getIndex() != value.length()) return null;

            return date;
        } catch (Throwable e) {
            Log.e("DateConverter", "Parse error. value=[" + value + "]", e);
            return null;
        }
    }

    @TypeConverter
    public static String dateToTime(Date value) {
        if (value == null) return null;
        return DF_DATE.get().format(value);
    }

    @TypeConverter
    public static java.util.Date fromTimestamp(Long value) {
        return value == null ? null : new java.util.Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(java.util.Date date) {
        return date == null ? null : date.getTime();
    }
}