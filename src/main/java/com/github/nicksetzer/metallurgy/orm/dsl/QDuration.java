package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.Locale;

public class QDuration extends QObject {

    public enum Mode {
        DAYS,
        HOURS,
        MINUTES,
        SECONDS,
        MILLISECONDS,
    }

    //int hours;
    //int minutes;
    //int seconds;
    long milliseconds;

    public QDuration() {
        //hours = 0;
        //minutes = 0;
        //seconds = 0;
        milliseconds = 0;
    }

    public QDuration(long milliseconds) {
        //hours = 0;
        //minutes = 0;
        //seconds = 0;
        this.milliseconds = milliseconds;
    }

    public QDuration(Mode mode, int magnitude) {
        //hours = 0;
        //minutes = 0;
        //seconds = 0;
        milliseconds = 0;
        set(mode, magnitude);
    }

    public QDuration(int H, int M, int S, int MS) {
        //hours = H;
        //minutes = M;
        //seconds = S;
        milliseconds = H * 60 * 60 * 1000 +
                       M * 60 * 1000 +
                       S * 1000 +
                       MS;
    }

    @Override
    public QObject add(QObject other) throws EvalException {
        if (other.getClass() == QInteger.class) {
            QInteger o = QInteger.class.cast(other);
            return new QDuration(milliseconds + o.value * 1000);
        } else if (other.getClass() == QDouble.class) {
            QDouble o = QDouble.class.cast(other);
            return new QDuration(milliseconds + ((long)(o.value * 1000)));
        } else if (other.getClass() == QDuration.class) {
            QDuration o = QDuration.class.cast(other);
            return new QDuration(milliseconds + o.milliseconds);
        }
        throw EvalException.invalidType(other);
    }

    @Override
    public QObject sub(QObject other) throws EvalException {
        if (other.getClass() == QInteger.class) {
            QInteger o = QInteger.class.cast(other);
            return new QDuration(milliseconds - o.value * 1000);
        } else if (other.getClass() == QDouble.class) {
            QDouble o = QDouble.class.cast(other);
            return new QDuration(milliseconds - ((long)(o.value * 1000)));
        } else if (other.getClass() == QDuration.class) {
            QDuration o = QDuration.class.cast(other);
            return new QDuration(milliseconds - o.milliseconds);
        }
        throw EvalException.invalidType(other);
    }

    @Override
    public QObject mul(QObject other) throws EvalException {
        if (other.getClass() == QInteger.class) {
            QInteger o = QInteger.class.cast(other);
            return new QDuration(milliseconds * o.value );
        } else if (other.getClass() == QDouble.class) {
            QDouble o = QDouble.class.cast(other);
            return new QDuration((long) (milliseconds * o.value));
        } else if (other.getClass() == QDuration.class) {
            QDuration o = QDuration.class.cast(other);
            return new QDuration(milliseconds * o.milliseconds);
        }
        throw EvalException.invalidType(other);
    }

    @Override
    public QObject div(QObject other) throws EvalException {
        if (other.getClass() == QInteger.class) {
            QInteger o = QInteger.class.cast(other);
            return new QDuration(milliseconds / o.value);
        } else if (other.getClass() == QDouble.class) {
            QDouble o = QDouble.class.cast(other);
            return new QDuration((long) (milliseconds / o.value));
        } else if (other.getClass() == QDuration.class) {
            QDuration o = QDuration.class.cast(other);
            return new QDuration(milliseconds / o.milliseconds);
        }
        throw EvalException.invalidType(other);
    }

    @Override
    public QObject negate() throws EvalException {
        //hours *= -1;
        //minutes *= -1;
        //seconds *= -1;
        //milliseconds *= -1;
        return new QDuration(-1 * milliseconds);
    }

    public void set(Mode mode, long magnitude) {
        switch (mode) {
            case DAYS:
                milliseconds = magnitude * 24 * 60 * 60 * 1000;
                break;
            case HOURS:
                milliseconds = magnitude * 60 * 60 * 1000;
                break;
            case MINUTES:
                milliseconds = magnitude * 60 * 1000;
                break;
            case SECONDS:
                milliseconds = magnitude * 1000;
                break;
            case MILLISECONDS:
                milliseconds = magnitude;
                break;
            default:
                milliseconds = 0;
                break;
        }
    }

    public Long toSeconds() {
        return milliseconds / 1000;
    }

    public Long toMilliseconds() {
        long duration;
        return milliseconds;
    }

    public QDuration copy() {
        return new QDuration(milliseconds);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        long ms, s, m , h;
        ms = milliseconds;
        if (ms < 0) {
            ms *= -1;
            sb.append("-");
        }
        s = ms / 1000;
        ms = ms % 1000;
        m = s / 60;
        s = s % 60;
        h = m / 60;
        m = m % 60;

        sb.append(h);
        sb.append(":");
        sb.append(String.format(Locale.US, "%02d", m));
        sb.append(":");
        sb.append(String.format(Locale.US, "%02d", s));
        if (ms > 0) {
            sb.append(".");
            sb.append(String.format(Locale.US, "%03d", ms));
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
                throw new EvalException("QDuration: unexpected symbol: " + sb.toString());
            }
        }

        return sb.toString();
    }

    private static QDuration fromDurationString(String str) throws EvalException {

        LexerBase.StringIterator iter = new LexerBase.StringIterator(str);

        // format:
        //  HH:MM:SS.RRR
        //  MM:SS.RRR
        //

        String tmp;
        int hours=0, minutes=0, seconds=0, milliseconds=0;
        int cont = 1;

        long delim_count = str.chars().filter(c -> c == ':').count();
        boolean skip_hours = false;
        if (delim_count == 1) {
            skip_hours = true;
        }

        while (cont==1) {

            if (!skip_hours) {
                tmp = parseNumber(iter, ':', 0);
                if (tmp.length() == 0) {
                    break;
                }
                if (tmp.length() == 1 || tmp.length() == 2) {
                    hours = Integer.parseInt(tmp);
                } else {
                    throw new EvalException("QDateTime: invalid hours");
                }
            }

            tmp = parseNumber(iter, ':', 0);
            if (tmp.length() == 0) {
                break;
            }
            if (tmp.length() == 1 || tmp.length() == 2) {
                minutes = Integer.parseInt(tmp);
            } else {
                throw new EvalException("QDateTime: invalid hours");
            }

            tmp = parseNumber(iter, '.', 0);
            if (tmp.length() == 0) {
                break;
            }
            if (tmp.length() == 1 || tmp.length() == 2) {
                seconds = Integer.parseInt(tmp);
            } else {
                throw new EvalException("QDateTime: invalid hours");
            }

            tmp = parseNumber(iter, 0, 0);
            if (tmp.length() == 0) {
                break;
            }
            if (tmp.length() >= 1 && tmp.length() <= 3) {
                while (tmp.length() < 3) {
                    tmp += "0";
                }
                milliseconds = Integer.parseInt(tmp);
            } else {
                throw new EvalException("QDateTime: invalid milliseconds");
            }

            cont=0;
        }
        return new QDuration(hours, minutes, seconds, milliseconds);
    }

    public static QDuration fromString(String str) throws EvalException {

        return QDuration.fromDurationString(str);

    }

}
