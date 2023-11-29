package ua.com.merchik.merchik.retrofit;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomDateTypeAdapter extends TypeAdapter<Date> {
    @Override
    public Date read(JsonReader reader) throws IOException {
        String dateString = reader.nextString();
        // Проверяем, если значение равно "0000-00-00", то заменяем на null
        if ("0000-00-00".equals(dateString)) {
            return null; // Заменяем на NULL
        }
        // Иначе, если значение не "0000-00-00", преобразуем его в Date
        // (это пример, как можно преобразовать строку в Date, вам может понадобиться другой способ)
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void write(JsonWriter writer, Date value) throws IOException {
        // Этот метод не используется в данном контексте
    }
}
