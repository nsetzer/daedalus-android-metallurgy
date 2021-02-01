package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.Locale;

public class QDateDelta extends QObject {

    public enum Mode {
        YEARS,
        MONTHS,
        DAYS,
        HOURS,
        MINUTES,
        SECONDS,
        MILLISECONDS,
    }

    int years;
    int months;
    int days;
    int hours;
    int minutes;
    int seconds;
    int milliseconds;

    public QDateDelta() {
        years = 0;
        months = 0;
        days = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        milliseconds = 0;
    }

    public QDateDelta(Mode mode, int magnitude) {
        years = 0;
        months = 0;
        days = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        milliseconds = 0;
        set(mode, magnitude);
    }

    public QDateDelta(int y, int m, int d) {
        years = y;
        months = m;
        days = d;
        hours = 0;
        minutes = 0;
        seconds = 0;
        milliseconds = 0;
    }

    public QDateDelta(int y, int m, int d, int H, int M, int S, int MS) {
        years = y;
        months = m;
        days = d;
        hours = H;
        minutes = M;
        seconds = S;
        milliseconds = MS;
    }

    @Override
    public QObject add(QObject other) throws EvalException {
        if (other.getClass() == QInteger.class) {
            //QDateDelta dd = this.copy();
            //QInteger o = QInteger.class.cast(other);
            //return new QDateDelta(milliseconds / o.value);
        } else if (other.getClass() == QDouble.class) {
            //QDateDelta dd = this.copy();
            //QDouble o = QDouble.class.cast(other);
            //return new QDateDelta((long) (milliseconds / o.value));
        } else if (other.getClass() == QDuration.class) {
            QDateDelta dd = this.copy();
            QDuration o = QDuration.class.cast(other);
            dd.milliseconds += o.milliseconds;
            return dd;
        } else if (other.getClass() == QDateDelta.class) {
            QDateDelta dd = this.copy();
            QDateDelta o = QDateDelta.class.cast(other);
            dd.years += o.years;
            dd.months += o.months;
            dd.days += o.days;
            dd.hours += o.hours;
            dd.minutes += o.minutes;
            dd.seconds += o.seconds;
            dd.milliseconds += o.milliseconds;
            return dd;
        }
        throw EvalException.invalidType(other);
    }

    @Override
    public QObject sub(QObject other) throws EvalException {
        if (other.getClass() == QInteger.class) {
            //QDateDelta dd = this.copy();
            //QInteger o = QInteger.class.cast(other);
            //return new QDateDelta(milliseconds / o.value);
        } else if (other.getClass() == QDouble.class) {
            //QDateDelta dd = this.copy();
            //QDouble o = QDouble.class.cast(other);
            //return new QDateDelta((long) (milliseconds / o.value));
        } else if (other.getClass() == QDuration.class) {
            QDateDelta dd = this.copy();
            QDuration o = QDuration.class.cast(other);
            dd.milliseconds -= o.milliseconds;
            return dd;
        } else if (other.getClass() == QDateDelta.class) {
            QDateDelta dd = this.copy();
            QDateDelta o = QDateDelta.class.cast(other);
            dd.years -= o.years;
            dd.months -= o.months;
            dd.days -= o.days;
            dd.hours -= o.hours;
            dd.minutes -= o.minutes;
            dd.seconds -= o.seconds;
            dd.milliseconds -= o.milliseconds;
            return dd;
            //return new QDateDelta(milliseconds / o.milliseconds);
        }
        throw EvalException.invalidType(other);
    }

    @Override
    public QObject mul(QObject other) throws EvalException {
        throw EvalException.invalidType(other);
    }

    @Override
    public QObject div(QObject other) throws EvalException {

        throw EvalException.invalidType(other);
    }

    @Override
    public QObject negate() throws EvalException {
        years *= -1;
        months *= -1;
        days *= -1;
        hours *= -1;
        minutes *= -1;
        seconds *= -1;
        milliseconds *= -1;
        return this;
    }

    public void set(Mode mode, int magnitude) {
        switch (mode) {
            case YEARS:
                years = magnitude;
                break;
            case MONTHS:
                months = magnitude;
                break;
            case DAYS:
                days = magnitude;
                break;
            case HOURS:
                hours = magnitude;
                break;
            case MINUTES:
                minutes = magnitude;
                break;
            case SECONDS:
                seconds = magnitude;
                break;
            case MILLISECONDS:
                milliseconds = magnitude;
                break;
            default:
                break;
        }
    }


    public QDateDelta copy() {
        return new QDateDelta(years, months, days, hours, minutes, seconds, milliseconds);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (years != 0) {
            sb.append(years);
            sb.append("y");
        }
        if (months != 0) {
            sb.append(months);
            sb.append("m");
        }
        sb.append(days);
        sb.append("d");
        if (hours != 0 || minutes != 0 || seconds != 0 || milliseconds != 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(hours);
            sb.append(":");
            sb.append(String.format(Locale.US, "%02d", minutes));
            sb.append(":");
            sb.append(String.format(Locale.US, "%02d", seconds));
            if (milliseconds != 0) {
                sb.append(".");
                sb.append(String.format(Locale.US, "%03d", milliseconds));
            }
        }

        if (sb.length() == 0) {
            sb.append("0d");
        }
        return sb.toString();
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

    private static QDateDelta fromSuffixString(String str) throws EvalException {

        LexerBase.StringIterator iter = new LexerBase.StringIterator(str);
        StringBuilder sb = new StringBuilder();
        int year=0, month=0, day=0, hours=0, minutes=0, seconds=0, milliseconds=0;

        while (true) {

            int codepoint = iter.getch();

            if (codepoint == 0) {
                break;
            } else if ('0' <= codepoint && codepoint <= '9') {
                sb.appendCodePoint(codepoint);
            } else {

                if (sb.length() == 0) {
                    throw new EvalException("invalid time delta number format");
                }

                switch (codepoint) {
                    case 'y':
                        year = Integer.parseInt(sb.toString());
                        break;
                    case 'm':
                        month = Integer.parseInt(sb.toString());
                        break;
                    case 'w':
                        day += 7 * Integer.parseInt(sb.toString());
                        break;
                    case 'd':
                        day += Integer.parseInt(sb.toString());
                        break;

                    default:
                        sb.appendCodePoint(codepoint);
                        throw new EvalException("unexpected sequence:" + sb.toString());
                }

                sb.setLength(0);
            }
        }

        return new QDateDelta(year, month, day);
    }

    public static QDateDelta fromString(String str) throws EvalException {

        return QDateDelta.fromSuffixString(str);
    }




}
