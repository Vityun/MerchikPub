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
        throw invalidValue("unexpected json token: " + token);
    }

    private Integer parseString(String rawValue) throws IOException {
        if (rawValue == null) {
            return fallbackValue;
        }

        String value = rawValue.trim().replaceAll("\\s+", " ");
        if (value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return fallbackValue;
        }

        try {
            // Accept integral decimal notation, never round identifiers or statuses.
            return new BigDecimal(value).intValueExact();
        } catch (ArithmeticException | NumberFormatException ignored) {
            throw invalidValue(value);
        }
    }

    private static IOException invalidValue(String value) {
        logBadValue(value);
        return new IOException("Invalid Integer value: " + value);
    }

    private static void logBadValue(String value) {
        String message = "Cannot parse Integer value: " + value;
        try {
            Log.e(TAG, message);
            Globals.writeToMLOG("ERROR", TAG, message);
        } catch (Exception ignored) {
        }
    }
}
