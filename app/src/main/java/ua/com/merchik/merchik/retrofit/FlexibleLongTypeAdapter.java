package ua.com.merchik.merchik.retrofit;

import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ua.com.merchik.merchik.Globals;

public class FlexibleLongTypeAdapter extends TypeAdapter<Long> {

    private static final String TAG = "FlexibleLongAdapter";
    private static final String[] DATE_PATTERNS = {
            "MMM d yyyy hh:mm:ss:a",
            "MMM dd yyyy hh:mm:ss:a",
            "MMM d yyyy h:mm:ss:a",
            "MMM dd yyyy h:mm:ss:a",
            "MMM d yyyy hh:mm:ss a",
            "MMM dd yyyy hh:mm:ss a",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "dd.MM.yyyy HH:mm:ss",
            "dd.MM.yyyy"
    };

    private final Long fallbackValue;

    public FlexibleLongTypeAdapter(Long fallbackValue) {
        this.fallbackValue = fallbackValue;
    }

    @Override
    public void write(JsonWriter out, Long value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value);
        }
    }

    @Override
    public Long read(JsonReader in) throws IOException {
        JsonToken token = in.peek();

        if (token == JsonToken.NULL) {
            in.nextNull();
            return fallbackValue;
        }

        if (token == JsonToken.NUMBER) {
            return parseString(in.nextString());
        }

        if (token == JsonToken.BOOLEAN) {
            return in.nextBoolean() ? 1L : 0L;
        }

        if (token == JsonToken.STRING) {
            return parseString(in.nextString());
        }

        in.skipValue();
        logBadValue("unexpected json token: " + token);
        return fallbackValue;
    }

    private Long parseString(String rawValue) {
        if (rawValue == null) {
            return fallbackValue;
        }

        String value = rawValue.trim().replaceAll("\\s+", " ");
        if (value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return fallbackValue;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            // Continue with decimal/date formats.
        }

        try {
            return new BigDecimal(value).toBigIntegerExact().longValueExact();
        } catch (ArithmeticException | NumberFormatException ignored) {
            // The server sometimes returns a textual date in fields declared as Long.
        }

        Long dateSec = parseDateToSeconds(value);
        if (dateSec != null) {
            return dateSec;
        }

        logBadValue(value);
        return fallbackValue;
    }

    private static Long parseDateToSeconds(String value) {
        for (String pattern : DATE_PATTERNS) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                format.setLenient(false);
                Date date = format.parse(value);

                if (date != null) {
                    return date.getTime() / 1000L;
                }
            } catch (ParseException ignored) {
                // Try the next known server format.
            }
        }

        return null;
    }

    private static void logBadValue(String value) {
        String message = "Cannot parse Long value: " + value;
        Log.e(TAG, message);
        try {
            Globals.writeToMLOG("ERROR", TAG, message);
        } catch (Exception ignored) {
        }
    }
}
