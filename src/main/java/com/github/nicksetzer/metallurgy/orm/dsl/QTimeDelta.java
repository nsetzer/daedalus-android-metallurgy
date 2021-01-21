package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.Locale;

public class QTimeDelta extends QObject {

    public enum Mode {
        YEAR,
        MONTH,
        DAY,
        HOURS,
        MINUTES,
        SECONDS,
        MILLISECONDS,
    }

    int year;
    int month;
    int day;
    int hours;
    int minutes;
    int seconds;
    int milliseconds;

    public QTimeDelta() {
        year = 0;
        month = 0;
        day = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        milliseconds = 0;
    }

    public QTimeDelta(Mode mode, int magnitude) {
        year = 0;
        month = 0;
        day = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        milliseconds = 0;
        set(mode, magnitude);
    }

    public QTimeDelta(int H, int M, int S, int MS) {
        hours = H;
        minutes = M;
        seconds = S;
        milliseconds = MS;
    }

    public QTimeDelta(int y, int m, int d, int H, int M, int S, int MS) {
        year = y;
        month = m;
        day = d;
        hours = H;
        minutes = M;
        seconds = S;
        milliseconds = MS;
    }

    public void set(Mode mode, int magnitude) {
        switch (mode) {
            case YEAR:
                year = magnitude;
                break;
            case MONTH:
                year = magnitude;
                break;
            case DAY:
                year = magnitude;
                break;
            case HOURS:
                year = magnitude;
                break;
            case MINUTES:
                year = magnitude;
                break;
            case SECONDS:
                year = magnitude;
                break;
            case MILLISECONDS:
                year = magnitude;
                break;
            default:
                break;
        }
    }

    private static String parseNumber(LexerBase.StringIterator iter, int terminal1, int terminal2) throws DslException  {
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
                throw new DslException("QDateUtil: unexpected symbol: " + sb.toString());
            }
        }

        return sb.toString();
    }

    private static QTimeDelta fromDurationString(String str) throws DslException {

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
                    throw new DslException("QDateTime: invalid hours");
                }
            }

            tmp = parseNumber(iter, ':', 0);
            if (tmp.length() == 0) {
                break;
            }
            if (tmp.length() == 1 || tmp.length() == 2) {
                minutes = Integer.parseInt(tmp);
            } else {
                throw new DslException("QDateTime: invalid hours");
            }

            tmp = parseNumber(iter, '.', 0);
            if (tmp.length() == 0) {
                break;
            }
            if (tmp.length() == 1 || tmp.length() == 2) {
                seconds = Integer.parseInt(tmp);
            } else {
                throw new DslException("QDateTime: invalid hours");
            }

            tmp = parseNumber(iter, 0, 0);
            if (tmp.length() == 0) {
                break;
            }
            if (tmp.length() >= 1 && tmp.length() <= 3) {
                milliseconds = Integer.parseInt(tmp);
            } else {
                throw new DslException("QDateTime: invalid milliseconds");
            }

            cont=0;
        }
        return new QTimeDelta(hours, minutes, seconds, milliseconds);
    }

    private static QTimeDelta fromSuffixString(String str) throws DslException {

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
                    throw new DslException("invalid time delta number format");
                }

                switch (codepoint) {
                    case 'y':
                        year = Integer.parseInt(sb.toString());
                        break;
                    case 'm':
                        month = Integer.parseInt(sb.toString());
                        break;
                    case 'w':
                        day = 7 * Integer.parseInt(sb.toString());
                        break;
                    case 'd':
                        day = Integer.parseInt(sb.toString());
                        break;
                    // TODO: disabled suffix mode for units less than one day
                    // there is confusion of 'm' : month or minutes
                    // this syntax is not as natural as the colon syntax
                    /*
                    case 'h':
                        hours = Integer.parseInt(sb.toString());
                        break;
                    case 'm':
                        if (iter.peek() == 's') {
                            iter.getch();
                            milliseconds = Integer.parseInt(sb.toString());
                        } else {
                            minutes = Integer.parseInt(sb.toString());
                        }
                        break;

                    case 's':
                        seconds = Integer.parseInt(sb.toString());
                        break;
                    */
                    default:
                        sb.appendCodePoint(codepoint);
                        throw new DslException("unexpected sequence:" + sb.toString());
                }

                sb.setLength(0);
            }
        }

        return new QTimeDelta(year, month, day, hours, minutes, seconds, milliseconds);
    }

    public static QTimeDelta fromString(String str) throws DslException {

        if (str.indexOf(':') >= 0) {
            return QTimeDelta.fromDurationString(str);
        } else {
            return QTimeDelta.fromSuffixString(str);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (year > 0) {
            sb.append(year);
            sb.append("y");
        }
        if (month > 0) {
            sb.append(month);
            sb.append("m");
        }
        if (day > 0) {
            sb.append(day);
            sb.append("d");
        }
        if (hours > 0 || minutes > 0 || seconds > 0 || milliseconds > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(hours);
            sb.append(":");
            sb.append(String.format(Locale.US, "%02d", minutes));
            sb.append(":");
            sb.append(String.format(Locale.US, "%02d", seconds));
            if (milliseconds > 0) {
                sb.append(".");
                sb.append(String.format(Locale.US, "%03d", milliseconds));
            }
        }

        if (sb.length() == 0) {
            sb.append("0d");
        }
        return sb.toString();
    }
}
