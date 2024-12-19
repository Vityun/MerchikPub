package ua.com.merchik.merchik;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Clock {

    public static String yesterday;
    public static String today;
    public static String tomorrow;
    public static String tomorrow7; // +7 дней
    public static String today_7; // -7 дней
    public static String today_30; // -30 дней

    public static void initTime() {
        yesterday = getYesterdayDateString();
        today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        tomorrow = getTomorrowDateString();
        tomorrow7 = getTomorrowDateString7();
        today_7 = getTomorrowDateString_7();
        today_30 = getTomorrowDateString_30();


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

    public static String getTomorrowDateString_30() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -30);
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
     * 86400000 - 1 день в секундах
     *
     * 05.10.2022
     * P.S. work in milliseconds
     *
     * 16.12.2024
     * Заменил старую логику на Calendar с учетом часовых поясов и зимнего/летнего времени
     * */
    public static long getDatePeriodLong(long date, int day) {
//        long res;
//        res = date + (day * 86400000L);
//        return res;
        // Получаем время в миллисекундах и учитываем часовой пояс
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(date);
        // Добавляем или вычитаем дни с учетом часового пояса
        calendar.add(Calendar.DAY_OF_YEAR, day);
        // Возвращаем новое время в миллисекундах
        return calendar.getTimeInMillis();
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

    public static String getHumanTimeSecPattern(Long time, String pattern){
        Date df = new java.util.Date(time * 1000);
        return new SimpleDateFormat(pattern).format(df);
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

    /**
     * Эта штука работает В МИЛЛИСЕКУНДАХ
     * */
    public static long dateConvertToLong(String str) {
        // Это понадобилось в момент работы с Доп.Требованиями. Мне надо получить с 0000-00-00 нормальную дату
        if (str.equals("0000-00-00")) {
            return 0;
        }
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

    /*Получение текущего конца недели*/
    public static Calendar getCurrentSunday(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return cal;
    }

    public static Calendar getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1); // Устанавливаем день месяца на первый день
        cal.set(Calendar.HOUR_OF_DAY, 0); // Устанавливаем часы на начало дня
        cal.set(Calendar.MINUTE, 0);      // Устанавливаем минуты на 0
        cal.set(Calendar.SECOND, 0);      // Устанавливаем секунды на 0
        cal.set(Calendar.MILLISECOND, 0); // Устанавливаем миллисекунды на 0
        return cal;
    }

    public static Calendar getEndOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); // Устанавливаем день месяца на последний день
        cal.set(Calendar.HOUR_OF_DAY, 23); // Устанавливаем часы на конец дня
        cal.set(Calendar.MINUTE, 59);      // Устанавливаем минуты на 59
        cal.set(Calendar.SECOND, 59);      // Устанавливаем секунды на 59
        cal.set(Calendar.MILLISECOND, 999); // Устанавливаем миллисекунды на 999
        return cal;
    }

    public static String getDatePremium(String inputDate){
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yy");

        String outputDate = "";

        try {
            Date date = inputFormat.parse(inputDate);

            outputDate = outputFormat.format(date);
            System.out.println(outputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputDate;
    }

    public static String getDatePremiumDownloadFormat(String inputDate){
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

        String outputDate = "";

        try {
            Date date = inputFormat.parse(inputDate);

            outputDate = outputFormat.format(date);
            System.out.println(outputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputDate;
    }


}
