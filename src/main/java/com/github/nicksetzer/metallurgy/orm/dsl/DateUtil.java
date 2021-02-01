package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.Calendar;

public class DateUtil {

    public static int daysInMonth(int y, int m) {
        int day = 1;
        Calendar calendar = new Calendar.Builder().setDate(y, m-1, day).build();
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static void add_date_delta(QDateTime dt, int dy, int dm, int dd) {
        Calendar calendar = new Calendar.Builder()
                .setDate(dt.year, dt.month-1, dt.day)
                .setTimeOfDay(dt.hours, dt.minutes, dt.seconds, dt.milliseconds)
                .build();
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
        calendar.add(Calendar.HOUR_OF_DAY, dH);
        calendar.add(Calendar.MINUTE, dM);
        calendar.add(Calendar.SECOND, dS);
        calendar.add(Calendar.MILLISECOND, dMS);

        dt.year = calendar.get(Calendar.YEAR);
        dt.month = calendar.get(Calendar.MONTH)+1;
        dt.day = calendar.get(Calendar.DAY_OF_MONTH);
        dt.hours = calendar.get(Calendar.HOUR_OF_DAY);
        dt.minutes = calendar.get(Calendar.MINUTE);
        dt.seconds = calendar.get(Calendar.SECOND);
        dt.milliseconds = calendar.get(Calendar.MILLISECOND);

    }

    public static void add_time_delta(QDateTime dt, long dMS) {
        Calendar calendar = new Calendar.Builder()
                .setDate(dt.year, dt.month-1, dt.day)
                .setTimeOfDay(dt.hours, dt.minutes, dt.seconds, dt.milliseconds)
                .build();
        // a better way to do this is
        // h := ms / (60 * 60 * 1000); // 3600000
        // ms := ms % (60 * 60 * 1000); // 3600000
        long ms, s, m , h;
        ms = dMS;
        int sign = 1;
        if (ms < 0) {
            ms *= -1;
            sign = -1;
        }
        s = ms / 1000;
        ms = ms % 1000;
        m = s / 60;
        s = s % 60;
        h = m / 60;
        m = m % 60;

        calendar.add(Calendar.HOUR_OF_DAY, sign * (int) h);
        calendar.add(Calendar.MINUTE, sign *(int) m);
        calendar.add(Calendar.SECOND, sign *(int) s);
        calendar.add(Calendar.MILLISECOND, sign * (int) ms);

        dt.year = calendar.get(Calendar.YEAR);
        dt.month = calendar.get(Calendar.MONTH)+1;
        dt.day = calendar.get(Calendar.DAY_OF_MONTH);
        dt.hours = calendar.get(Calendar.HOUR_OF_DAY);
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

    public static QDateTime epoch_time(long epochtime_ms) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochtime_ms);

        QDateTime dt = new QDateTime(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                calendar.get(Calendar.MILLISECOND)
        );

        return dt;
    }

    public static QDateTime now() {
        Calendar calendar = Calendar.getInstance();

        QDateTime dt = new QDateTime(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
            calendar.get(Calendar.MILLISECOND)
        );

        return dt;
    }

}
