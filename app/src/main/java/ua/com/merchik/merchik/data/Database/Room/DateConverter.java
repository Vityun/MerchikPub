package ua.com.merchik.merchik.data.Database.Room;

import androidx.room.TypeConverter;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DateConverter {

//    @TypeConverter
//    public static Date toDate(String date) {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        try {
//            return new Date(format.parse(date).getTime());
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }



    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    static {
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @TypeConverter
    public static Date timeToDate(String value) {
        if (value != null) {
            try {
                return new Date(df.parse(value).getTime());
            } catch (ParseException e) {
            }
            return null;
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String dateToTime(Date value) {
        if (value != null) {
            return df.format(value);
        } else {
            return null;
        }
    }


//    @TypeConverter
//    public static Date toDate(Long timestamp) {
//        Log.e("MIGRATION_2_3", "CONVERTER/toDate: " + timestamp);
//        return timestamp == null ? null : new Date(timestamp);
//    }

//    @TypeConverter
//    public static String toSTimestamp(Date date) {
//        Log.e("MIGRATION_2_3", "CONVERTER/toTimestamp: " + date);
//        return String.valueOf(date == null ? null : date.getTime());
//    }

//    @TypeConverter
//    public static Long toTimestamp(Date date) {
//        Log.e("MIGRATION_2_3", "CONVERTER/toTimestamp: " + date);
//        return date == null ? null : date.getTime();
//    }


//    @TypeConverter
//    public static Date dateToDate(Date date){
//        return new Date(date.getTime());
//    }

}
