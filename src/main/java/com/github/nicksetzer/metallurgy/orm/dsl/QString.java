package com.github.nicksetzer.metallurgy.orm.dsl;

public class QString extends QObject {

    String value;

    public QString(String value) {
        this.value = value;
    }

    public QObject add(QObject other) throws EvalException {
        Class c = other.getClass();
        if (c == QString.class) {
            QString v = (QString) other;
            return new QString(this.value + v.value);
        } else if (c == QDateDelta.class) {
            // RHS rule: cast self (str) to date time, add time delta
            QDateTime dt = QDateTime.fromString(value);
            return dt.add(other);
        } else if (c == QDuration.class) {
            // RHS rule: cast self (str) to date time, add time delta
            QDateTime dt = QDateTime.fromString(value);
            return dt.add(other);
        } else {
            throw EvalException.invalidType(other);
        }
    }

    public QObject sub(QObject other) throws EvalException {
        Class c = other.getClass();
        if (c == QDateDelta.class) {
            // RHS rule: cast self (str) to date time, add time delta
            QDateTime dt = QDateTime.fromString(value);
            return dt.sub(other);
        } else if (c == QDuration.class) {
            // RHS rule: cast self (str) to date time, add time delta
            QDateTime dt = QDateTime.fromString(value);
            return dt.sub(other);
        } else {
            throw EvalException.invalidType(other);
        }
    }

    public QObject mul(QObject other) throws EvalException {
        throw EvalException.notImplemented("mul");
    }

    public QObject div(QObject other) throws EvalException {
        throw EvalException.notImplemented("div");
    }

    public QObject negate() throws EvalException {
        throw EvalException.notImplemented("negate");
    }

    public String toString() {
        return value;
    }
}
