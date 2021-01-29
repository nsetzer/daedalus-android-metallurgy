package com.github.nicksetzer.metallurgy.orm.dsl;

public class EvalException extends DslException {

    public EvalException(String message) {
        super(message);
        m_user_message = message;
    }

    public static EvalException invalidType(QObject obj) {
        return new EvalException("Invalid Type: " + obj.getClass().toString());
    }

    public static EvalException notImplemented(String msg) {
        return new EvalException("Not Implemented: " + msg);
    }
}
