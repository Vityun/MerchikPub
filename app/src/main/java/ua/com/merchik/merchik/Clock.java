package ua.com.merchik.merchik;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Clock {

    public static String yesterday;
    public static String today;
    public static String tomorrow;
    public static String tomorrow7; // +7 дней
    public static String today_7; // -7 дней

    public static void initTime() {
        yesterday = getYesterdayDateString();
        today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        tomorrow = getTomorrowDateString();
        tomorrow7 = getTomorrowDateString7();
        today_7 = getTomorrowDateString_7();


        Log.e("initTime", "yesterday: " + yesterday);
        Log.e("initTime", "today: " + today);
        Log.e("initTime", "tomorrow: " + tomorrow);
        Log.e("initTime", "tomorrow7: " + tomorrow7);
    }


    private static String getYesterdayDateString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return dateFormat.format(cal.getTime());
    }

    private static String getTomorrowDateString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, +1);
        return dateFormat.format(cal.getTime());
    }

    public static String getTomorrowDateString7() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, +7);
        return dateFormat.format(cal.getTime());
    }

    public static String getTomorrowDateString_7() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        return dateFormat.format(cal.getTime());
    }

    public static String getDatePeriod(int day) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, day);
        return dateFormat.format(cal.getTime());
    }

    public static long getDatePeriodLong(int day) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, day);
        return cal.getTime().getTime();
    }

    /**
     * 20.09.2022
     * Передаю дату в формате long и добавляю дни указанные вторым числом.
     * Второе число может быть отрицательным
     * 86400000 - 1 день в миллисекундах
     * */
    public static long getDatePeriodLong(long date, int day) {
        long res;
        res = date + (day * 86400000);
        return res;
    }


    // Сегодня - 7 дней
    public static String lastWeek() {
        Date df = new java.util.Date(System.currentTimeMillis() - 604800000); // -7 дней
        return new SimpleDateFormat("yyyy-MM-dd").format(df);
    }


    public static String getHumanTime() {
        Date df = new java.util.Date(System.currentTimeMillis());
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(df);
    }


    public static String getHumanTime2(Long l) {
        Date df = new java.util.Date(l * 1000);
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(df);
    }

    public static String getHumanTime3(Long l) {
        Date df = new java.util.Date(l * 1000);
        return new SimpleDateFormat("dd-MM-yyyy").format(df);
    }

    public static String getHumanTimeYYYYMMDD(Long l) {
        Date df = new java.util.Date(l * 1000);
        return new SimpleDateFormat("yyyy-MM-dd").format(df);
    }

    public static String getHumanTimeOpt(long l) {
        if (l == 0) {
            return "0";
        }
        Date df = new java.util.Date(l);
        return new SimpleDateFormat("HH:mm").format(df);
    }

    /**
     * 16.03.2021
     *
     * @return
     */
    public static Date timeLongToDAte(long l) {
        l = l * 1000;
        if (l == 0) {
            return null;
        }
        return new java.util.Date(l);
    }

    public static long dateConvertToLong(String str) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = formatter.parse(str);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Date stringDateConvertToDate(String str) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = formatter.parse(str);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date stringDateConvertToDate2(String str) {
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date date = formatter.parse(str);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 04.05.2021
     * Динамическое получение нужной даты
     * Нужно дорабатывать, но уже по ходу движения
     */
    public static String getDateString(long unix_time) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date ();
        date.setTime(unix_time);
        return dateFormat.format(date);
    }

    public static Date getDateLong(int date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, date);
        return cal.getTime();
    }


    /*Получение прошлого Понедельника*/
    public static Calendar getLastMonday(){
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.add(Calendar.DAY_OF_WEEK, -7);

        return cal;
    }

    public static Calendar getLastSunday(){
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.SUNDAY);

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.add(Calendar.DAY_OF_WEEK, -1);

        return cal;
    }

    /*Получение текущего понедельника*/
    public static Calendar getCurrentMonday(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal;
    }


}
