package com.github.nicksetzer.metallurgy.orm.dsl;

public class QObject {

    // add for string and time delta will fail
    // but time_delta + string could cast the string to a datetime.
    // runtime exceptions should automatically attempt the reverse order for add and sub.

    public QObject add(QObject other) {
        return null;
    }

    public QObject sub(QObject other) {
        return null;
    }

    public QObject mul(QObject other) {
        return null;
    }

    public QObject div(QObject other) {
        return null;
    }

    public void negate() {
        return;
    }

}
