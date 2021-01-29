package com.github.nicksetzer.metallurgy.orm.dsl;

public class QInteger extends QObject {

    int value;

    public QInteger(int value) {
        this.value = value;
    }

    public QInteger(QDouble value) {
        this.value = (int)value.value;
    }

    public QObject add(QObject other) throws EvalException {
        Class c = other.getClass();
        if (c == QInteger.class) {
            QInteger v = (QInteger) other;
            return new QInteger(this.value + v.value);
        } else if (c == QDouble.class) {
            QDouble v = (QDouble) other;
            return new QDouble(this.value + v.value);
        } else {
            return other.add(this);
        }
    }

    public QObject sub(QObject other) throws EvalException {
        Class c = other.getClass();
        if (c == QInteger.class) {
            QInteger v = (QInteger) other;
            return new QInteger(this.value - v.value);
        } else if (c == QDouble.class) {
            QDouble v = (QDouble) other;
            return new QDouble(this.value - v.value);
        } else {
            return other.sub(this);
        }
    }

    public QObject mul(QObject other) throws EvalException {
        Class c = other.getClass();
        if (c == QInteger.class) {
            QInteger v = (QInteger) other;
            return new QInteger(this.value * v.value);
        } else if (c == QDouble.class) {
            QDouble v = (QDouble) other;
            return new QDouble(this.value * v.value);
        } else {
            return other.mul(this);
        }
    }

    public QObject div(QObject other) throws EvalException {
        Class c = other.getClass();
        if (c == QInteger.class) {
            QInteger v = (QInteger) other;
            return new QInteger(this.value / v.value);
        } else if (c == QDouble.class) {
            QDouble v = (QDouble) other;
            return new QDouble(this.value / v.value);
        } else {
            return other.div(this);
        }
    }

    public QObject negate() throws EvalException {
        return new QInteger(-value);
    }

    public String toString() {
        return Integer.toString(value);
    }

    public static QInteger fromString(String str) {
        str = str.replaceAll("_", "");
        int ivalue = 0;
        if (str.startsWith("0x")) {
            ivalue = Integer.parseInt(str.substring(2), 16);
        } else if (str.startsWith("0o")) {
            ivalue = Integer.parseInt(str.substring(2), 8);
        } else if (str.startsWith("0n")) {
            ivalue = Integer.parseInt(str.substring(2), 4);
        } else if (str.startsWith("0b")) {
            ivalue = Integer.parseInt(str.substring(2), 2);
        } else {
            ivalue = Integer.parseInt(str);
        }
        return new QInteger(ivalue);
    }
}
