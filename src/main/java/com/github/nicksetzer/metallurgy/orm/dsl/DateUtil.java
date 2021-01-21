package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.Calendar;

public class DateUtil {

    public static int daysInMonth(int y, int m) {
        int day = 1;
        Calendar calendar = new Calendar.Builder().setDate(y, m-1, day).build();
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static void add_date_delta(QDateTime dt, int dy, int dm, int dd) {
        Calendar calendar = new Calendar.Builder().setDate(dt.year, dt.month-1, dt.day).build();
        calendar.add(Calendar.YEAR, dy);
        calendar.add(Calendar.MONTH, dm);
        calendar.add(Calendar.DAY_OF_MONTH, dd);

        dt.year = calendar.get(Calendar.YEAR);
        dt.month = calendar.get(Calendar.MONTH)+1;
        dt.day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static void add_time_delta(QDateTime dt, int dH, int dM, int dS, int dMS) {
        Calendar calendar = new Calendar.Builder()
                .setDate(dt.year, dt.month-1, dt.day)
                .setTimeOfDay(dt.hours, dt.minutes, dt.seconds, dt.milliseconds)
                .build();
        calendar.add(Calendar.HOUR, dH);
        calendar.add(Calendar.MINUTE, dM);
        calendar.add(Calendar.SECOND, dS);
        calendar.add(Calendar.MILLISECOND, dMS);

        dt.year = calendar.get(Calendar.YEAR);
        dt.month = calendar.get(Calendar.MONTH)+1;
        dt.day = calendar.get(Calendar.DAY_OF_MONTH);
        dt.hours = calendar.get(Calendar.HOUR);
        dt.minutes = calendar.get(Calendar.MINUTE);
        dt.seconds = calendar.get(Calendar.SECOND);
        dt.milliseconds = calendar.get(Calendar.MILLISECOND);

        calendar.getTimeInMillis();
    }

    public static long epoch_time(QDateTime dt) {
        Calendar calendar = new Calendar.Builder()
                .setDate(dt.year, dt.month-1, dt.day)
                .setTimeOfDay(dt.hours, dt.minutes, dt.seconds, dt.milliseconds)
                .build();
        return calendar.getTimeInMillis();
    }

}
