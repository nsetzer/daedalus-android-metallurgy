package com.github.nicksetzer.metallurgy.orm.dsl;

public class DslException extends Exception {

    String m_user_message;

    public DslException(String message) {
        super(message);
        m_user_message = message;
    }

    public DslException(Token token, String message) {
        super(DslException.format(token, message));
        m_user_message = message;
    }

    public static String format(Token token, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error: ");
        sb.append(token.position().toString());
        sb.append(" ");
        sb.append(token.toString());
        sb.append(" ");
        sb.append(message);
        return sb.toString();
    }

    public String userMessage() {
        return m_user_message;
    }
}
