package com.github.nicksetzer.metallurgy.orm.dsl;

public interface IObject {

    QObject add(QObject other) throws EvalException;

    QObject sub(QObject other) throws EvalException;

    QObject mul(QObject other) throws EvalException;

    QObject div(QObject other) throws EvalException;

    QObject negate() throws EvalException;

    String toString();

}
