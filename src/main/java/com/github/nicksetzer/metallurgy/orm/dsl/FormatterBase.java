package com.github.nicksetzer.metallurgy.orm.dsl;

public class FormatterBase {

    public static class FormatError extends Exception {

        public FormatError(Token token, String message) {
            super(message);
        }
    }

    public FormatterBase() {

    }

    public String format(Token token) {

        return "";
    }
}
