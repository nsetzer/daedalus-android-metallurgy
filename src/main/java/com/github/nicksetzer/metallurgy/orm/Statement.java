package com.github.nicksetzer.metallurgy.orm;

public class Statement {

    String text;
    ParamList params;

    public Statement(String text) {
        this.text = text;
        this.params = new ParamList();
    }

    public Statement(String text, ParamList params) {
        this.text = text;
        this.params = params;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.text);
        builder.append(" | params (");
        for (String param : this.params) {
            builder.append(param);
            builder.append(", ");
        }
        builder.append(")");


        return builder.toString();
    }
}
