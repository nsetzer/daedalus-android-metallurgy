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
}
