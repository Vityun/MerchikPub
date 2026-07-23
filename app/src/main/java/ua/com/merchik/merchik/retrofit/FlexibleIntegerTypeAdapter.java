package ua.com.merchik.merchik.retrofit;

import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;

import ua.com.merchik.merchik.Globals;

public class FlexibleIntegerTypeAdapter extends TypeAdapter<Integer> {

    private static final String TAG = "FlexibleIntegerAdapter";

    private final Integer fallbackValue;

    public FlexibleIntegerTypeAdapter(Integer fallbackValue) {
        this.fallbackValue = fallbackValue;
    }

    @Override
    public void write(JsonWriter out, Integer value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value);
        }
    }

    @Override
    public Integer read(JsonReader in) throws IOException {
        JsonToken token = in.peek();

        if (token == JsonToken.NULL) {
            in.nextNull();
            return fallbackValue;
        }

        if (token == JsonToken.NUMBER) {
            return parseString(in.nextString());
        }

        if (token == JsonToken.BOOLEAN) {
            return in.nextBoolean() ? 1 : 0;
        }

        if (token == JsonToken.STRING) {
            return parseString(in.nextString());
        }

        in.skipValue();
        logBadValue("unexpected json token: " + token);
        return fallbackValue;
    }

    private Integer parseString(String rawValue) {
        if (rawValue == null) {
            return fallbackValue;
        }

        String value = rawValue.trim().replaceAll("\\s+", " ");
        if (value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return fallbackValue;
        }

        try {
            return safeLongToInt(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            // Continue with decimal numbers.
        }

        try {
            return safeLongToInt(new BigDecimal(value).toBigIntegerExact().longValueExact());
        } catch (ArithmeticException | NumberFormatException ignored) {
            // Some responses contain non-numeric placeholders in numeric fields.
        }

        logBadValue(value);
        return fallbackValue;
    }

    private Integer safeLongToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            logBadValue(String.valueOf(value));
            return fallbackValue;
        }

        if (value < Integer.MIN_VALUE) {
            logBadValue(String.valueOf(value));
            return fallbackValue;
        }

        return (int) value;
    }

    private static void logBadValue(String value) {
        String message = "Cannot parse Integer value: " + value;
        Log.e(TAG, message);
        try {
            Globals.writeToMLOG("ERROR", TAG, message);
        } catch (Exception ignored) {
        }
    }
}
