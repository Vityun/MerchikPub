package ua.com.merchik.merchik.retrofit;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Field-only adapter: do not register globally for String or Integer. */
public final class RoundedCountAdapterFactory implements TypeAdapterFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<?> rawType = type.getRawType();
        boolean string = rawType == String.class;
        boolean primitive = rawType == int.class;
        if (!string && !primitive && rawType != Integer.class) {
            throw new IllegalArgumentException("Unsupported count type: " + type);
        }
        TypeAdapter<T> delegate = gson.getAdapter(type);
        T missing = primitive ? (T) Integer.valueOf(0) : null;
        return new TypeAdapter<T>() {
            @Override
            public T read(JsonReader in) throws IOException {
                JsonToken token = in.peek();
                if (token == JsonToken.NULL) {
                    in.nextNull();
                    return missing;
                }
                if (token == JsonToken.BOOLEAN && !string) {
                    return delegate.read(in);
                }
                if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
                    throw new IOException("Invalid count token at " + in.getPath() + ": " + token);
                }
                String raw = in.nextString();
                String value = raw.trim();
                if (value.isEmpty() || "null".equalsIgnoreCase(value)) {
                    return string ? (T) raw : missing;
                }
                try {
                    int rounded = round(value);
                    return string ? (T) Integer.toString(rounded) : (T) Integer.valueOf(rounded);
                } catch (NumberFormatException | ArithmeticException e) {
                    throw new IOException("Invalid count at " + in.getPath() + ": " + raw, e);
                }
            }

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }
        };
    }

    private static int round(String value) {
        int start = value.charAt(0) == '-' || value.charAt(0) == '+' ? 1 : 0;
        int separator = value.indexOf('.');
        if (separator < 0) separator = value.indexOf(',');
        if (separator >= 0) {
            // Integral decimal notation does not need BigDecimal or rounding.
            int fractionEnd = separator + 1;
            while (fractionEnd < value.length() && value.charAt(fractionEnd) == '0') {
                fractionEnd++;
            }
            if (separator > start && fractionEnd == value.length()) {
                return Integer.parseInt(value.substring(0, separator));
            }
        } else {
            int end = start;
            while (end < value.length() && value.charAt(end) >= '0' && value.charAt(end) <= '9') {
                end++;
            }
            if (end > start && end == value.length()) return Integer.parseInt(value);
        }
        return new BigDecimal(value.replace(',', '.'))
                .setScale(0, RoundingMode.HALF_UP).intValueExact();
    }
}
