package ua.com.merchik.merchik.retrofit;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomDateTypeAdapter extends TypeAdapter<Date> {
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public Date read(JsonReader reader) throws IOException {
        try {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return null;
            }

            String dateString = reader.nextString();

            if (dateString == null) {
                return null;
            }

            dateString = dateString.trim();

            // Пустые и "нулевые" значения считаем отсутствием даты
            if (dateString.isEmpty()
                    || "0".equals(dateString)
                    || "0000-00-00".equals(dateString)
                    || "null".equalsIgnoreCase(dateString)) {
                return null;
            }

            // Если внезапно пришёл timestamp строкой
            if (dateString.matches("^\\d+$")) {
                long value = Long.parseLong(dateString);
                return value <= 0L ? null : new Date(value);
            }

            // Обычный формат yyyy-MM-dd
            return format.parse(dateString);

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void write(JsonWriter writer, Date value) throws IOException {
        if (value == null) {
            writer.nullValue();
        } else {
            writer.value(format.format(value));
        }
    }
}
