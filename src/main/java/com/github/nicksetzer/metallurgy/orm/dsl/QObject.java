package com.github.nicksetzer.metallurgy.orm.dsl;

public class QObject implements IObject {

    // add for string and time delta will fail
    // but time_delta + string could cast the string to a datetime.
    // runtime exceptions should automatically attempt the reverse order for add and sub.

    public QObject add(QObject other) throws EvalException {
        throw EvalException.notImplemented("add");
    }

    public QObject sub(QObject other) throws EvalException {
        throw EvalException.notImplemented("sub");
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

}
