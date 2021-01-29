package com.github.nicksetzer.metallurgy.orm.dsl;

public class QDouble extends QObject {

    double value;

    public QDouble(double value) {
        this.value = value;
    }

    public QDouble(QInteger value) {
        this.value = (double)value.value;
    }

    public QObject add(QObject other) throws EvalException {
        Class c = other.getClass();
        if (c == QInteger.class) {
            QInteger v = (QInteger) other;
            return new QDouble(this.value + v.value);
        } else if (c == QDouble.class) {
            QDouble v = (QDouble) other;
            return new QDouble(this.value + v.value);
        } else {
            throw EvalException.invalidType(other);
        }
    }

    public QObject sub(QObject other) throws EvalException {
        Class c = other.getClass();
        if (c == QInteger.class) {
            QInteger v = (QInteger) other;
            return new QDouble(this.value - v.value);
        } else if (c == QDouble.class) {
            QDouble v = (QDouble) other;
            return new QDouble(this.value - v.value);
        } else {
            throw EvalException.invalidType(other);
        }
    }

    public QObject mul(QObject other) throws EvalException {
        Class c = other.getClass();
        if (c == QInteger.class) {
            QInteger v = (QInteger) other;
            return new QDouble(this.value * v.value);
        } else if (c == QDouble.class) {
            QDouble v = (QDouble) other;
            return new QDouble(this.value * v.value);
        } else {
            throw EvalException.invalidType(other);
        }
    }

    public QObject div(QObject other) throws EvalException {
        Class c = other.getClass();
        if (c == QInteger.class) {
            QInteger v = (QInteger) other;
            return new QDouble(this.value / v.value);
        } else if (c == QDouble.class) {
            QDouble v = (QDouble) other;
            return new QDouble(this.value / v.value);
        } else {
            throw EvalException.invalidType(other);
        }
    }

    public QObject negate() throws EvalException {
        return new QDouble(-value);
    }

    public String toString() {
        return Double.toString(value);
    }

    public static QDouble fromString(String str) {
        str = str.replaceAll("_", "");
        return new QDouble(Double.parseDouble(str));
    }
}
