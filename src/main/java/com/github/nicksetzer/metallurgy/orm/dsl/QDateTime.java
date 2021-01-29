package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.Calendar;
import java.util.Locale;

public class QDateTime extends QObject {

    int year;
    int month;
    int day;
    int hours;
    int minutes;
    int seconds;
    int milliseconds;

    public QDateTime(int y, int m, int d) {
        year = y;
        month = m;
        day = d;
        hours = 0;
        minutes = 0;
        seconds = 0;
        milliseconds = 0;
    }

    public QDateTime(int y, int m, int d, int H, int M, int S) {
        year = y;
        month = m;
        day = d;
        hours = H;
        minutes = M;
        seconds = S;
        milliseconds = 0;
    }

    public QDateTime(int y, int m, int d, int H, int M, int S, int MS) {
        year = y;
        month = m;
        day = d;
        hours = H;
        minutes = M;
        seconds = S;
        milliseconds = MS;
    }

    @Override
    public QObject add(QObject other) throws EvalException {
        if (other.getClass() == QDateDelta.class) {
            QDateTime dt = copy();
            QDateDelta td = (QDateDelta) other;

            DateUtil.add_date_delta(dt, td.years, td.months, td.days);
            DateUtil.add_time_delta(dt, td.hours, td.minutes, td.seconds, td.milliseconds);
            return dt;
        } else if (other.getClass() == QDuration.class) {
            QDateTime dt = copy();
            QDuration duration = QDuration.class.cast(other);
            //DateUtil.add_time_delta(dt, duration.hours, duration.minutes, duration.seconds, duration.milliseconds);
            DateUtil.add_time_delta(dt, duration.milliseconds);
            return dt;
        } else {
            throw EvalException.invalidType(other);
        }
    }

    @Override
    public QObject sub(QObject other) throws EvalException {
        if (other.getClass() == QDateDelta.class) {
            QDateTime dt = copy();
            QDateDelta td = (QDateDelta) other;
            DateUtil.add_date_delta(dt, -td.years, -td.months, -td.days);
            DateUtil.add_time_delta(dt, -td.hours, -td.minutes, -td.seconds, -td.milliseconds);
            return dt;
        } else if (other.getClass() == QDuration.class) {
            QDateTime dt = copy();
            QDuration duration = QDuration.class.cast(other);
            //DateUtil.add_time_delta(dt, -duration.hours, -duration.minutes, -duration.seconds, -duration.milliseconds);
            DateUtil.add_time_delta(dt, -duration.milliseconds);
            return dt;
        } else {
            throw EvalException.invalidType(other);
        }
    }

    private static String parseNumber(LexerBase.StringIterator iter, int terminal1, int terminal2) throws EvalException  {
        StringBuilder sb = new StringBuilder();

        while (true) {
            int codepoint = iter.peek();

            if (codepoint == 0) {
                break;
            } else if (codepoint == ' ') {
                iter.getch();
            } else if (codepoint == terminal1 || codepoint == terminal2) {
                iter.getch();
                break;
            } else if ('0' <= codepoint && codepoint <= '9') {
                sb.appendCodePoint(iter.getch());
            } else {
                sb.appendCodePoint(iter.getch());
                throw new EvalException("QDateUtil: unexpected symbol: " + sb.toString());
            }
        }

        return sb.toString();
    }

    public static QDateTime fromString(String str) throws EvalException {

        LexerBase.StringIterator iter = new LexerBase.StringIterator(str);

        // format:
        //  YYYY/MM/DDTHH:MM:SS.RRR
        //  YY/MM/DD HH:MM:SS.RRR
        //
        // modes:
        // 0 : year
        // 1 : month
        // 2 : day
        // 4 : hour
        // 5 : minute
        // 6 : second
        // 7 : milliseconds
        int mode = 0;
        int cont = 1;
        String tmp;

        int year=0, month=0, day=0, hours=0, minutes=0, seconds=0, milliseconds=0;
        while (cont==1) {

            switch (mode) {
                case 0:
                    tmp = parseNumber(iter, '/', 0);
                    if (tmp.length() == 0) {
                        cont = 0;
                        break;
                    }
                    if (tmp.length()==2 || tmp.length()==4) {
                        year = Integer.parseInt(tmp);
                    } else {
                        throw new EvalException("QDateTime: invalid year");
                    }
                    mode += 1;
                    break;
                case 1:
                    tmp = parseNumber(iter, '/', 0);
                    if (tmp.length() == 0) {
                        cont = 0;
                        break;
                    }
                    if (tmp.length()==1 || tmp.length()==2) {
                        month = Integer.parseInt(tmp);
                    } else {
                        throw new EvalException("QDateTime: invalid month");
                    }
                    mode += 1;
                    break;
                case 2:
                    tmp = parseNumber(iter, 'T', ' ');
                    if (tmp.length() == 0) {
                        cont = 0;
                        break;
                    }
                    if (tmp.length()==1 || tmp.length()==2) {
                        day = Integer.parseInt(tmp);
                    } else {
                        throw new EvalException("QDateTime: invalid day");
                    }
                    mode += 1;
                    break;
                case 3:
                    tmp = parseNumber(iter, ':', 0);
                    if (tmp.length() == 0) {
                        cont = 0;
                        break;
                    }
                    if (tmp.length()==1 || tmp.length()==2) {
                        hours = Integer.parseInt(tmp);
                    } else {
                        throw new EvalException("QDateTime: invalid hours");
                    }
                    mode += 1;
                    break;
                case 4:
                    tmp = parseNumber(iter, ':', 0);
                    if (tmp.length() == 0) {
                        cont = 0;
                        break;
                    }
                    if (tmp.length()==1 || tmp.length()==2) {
                        minutes = Integer.parseInt(tmp);
                    } else {
                        throw new EvalException("QDateTime: invalid hours");
                    }
                    mode += 1;
                    break;
                case 5:
                    tmp = parseNumber(iter, '.', 0);
                    if (tmp.length() == 0) {
                        cont = 0;
                        break;
                    }
                    if (tmp.length()==1 || tmp.length()==2) {
                        seconds = Integer.parseInt(tmp);
                    } else {
                        throw new EvalException("QDateTime: invalid hours");
                    }
                    mode += 1;
                    break;
                case 6:
                    tmp = parseNumber(iter, 0, 0);
                    cont = 0;
                    if (tmp.length() == 0) {
                        break;
                    }
                    if (tmp.length()>=1 && tmp.length()<=3) {
                        milliseconds = Integer.parseInt(tmp);
                    } else {
                        throw new EvalException("QDateTime: invalid milliseconds");
                    }
                    break;
                default:
                    break;
            }
        }

        // fix two digit years to be within the last 100 years

        if (0 <= year && year <= 100) {
            int current_year = Calendar.getInstance().get(Calendar.YEAR);
            // round down to nearest 100 years
            // then subtract 100 to get 1900.
            int base = ((current_year/1000) * 1000) - 100;
            year += base;
            if (year < current_year - 100) {
                year += 100;
            }
        }

        return new QDateTime(year, month, day, hours, minutes, seconds, milliseconds);

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(year);
        sb.append("/");
        sb.append(String.format(Locale.US, "%02d", month));
        sb.append("/");
        sb.append(String.format(Locale.US,"%02d", day));

        if (hours > 0 || minutes > 0 || seconds > 0 || milliseconds > 0) {
            sb.append("T");
            sb.append(hours);
            sb.append(":");
            sb.append(String.format(Locale.US,"%02d", minutes));
            sb.append(":");
            sb.append(String.format(Locale.US,"%02d", seconds));
            if (milliseconds > 0) {
                sb.append(".");
                sb.append(String.format(Locale.US, "%03d", milliseconds));
            }
        }

        return sb.toString();
    }

    public Long toEpochTime() {
        return DateUtil.epoch_time(this);
    }

    public QDateTime copy() {
        return new QDateTime(year, month, day, hours, minutes, seconds, milliseconds);
    }

}
