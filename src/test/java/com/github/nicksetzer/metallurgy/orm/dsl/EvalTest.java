package com.github.nicksetzer.metallurgy.orm.dsl;

import org.junit.Test;

public class EvalTest {



    @Test
    public void test_add_integer() throws ParserBase.ParseError, DslException {

        QueryParser parser = new QueryParser();
        Token mod = parser.parse("x = 2 + - 4");

        mod = mod.children().get(0).children().get(1);

        System.out.println(mod.toDebugString());

        QObject obj = new Eval().eval(mod);

        System.out.println(obj.getClass().toString());
        System.out.println(obj.toString());

    }

    @Test
    public void test_add_dates() throws ParserBase.ParseError, DslException {

        QueryParser parser = new QueryParser();
        Token mod = parser.parse("x = \"2020/01/01\" + 4w");

        System.out.println("parsed: " + mod.toDebugString());
        mod = mod.children().get(0).children().get(1);

        System.out.println("expression: " + mod.toDebugString());

        QObject obj = new Eval().eval(mod);

        System.out.println(obj.getClass().toString());
        System.out.println(obj.toString());

    }

    @Test
    public void test_duration() throws ParserBase.ParseError, DslException {

        QueryParser parser = new QueryParser();
        Token mod = parser.parse("x = - (4:30.02 + 5)");

        System.out.println("parsed: " + mod.toDebugString());
        mod = mod.children().get(0).children().get(1);

        System.out.println("expression: " + mod.toDebugString());

        QObject obj = new Eval().eval(mod);

        System.out.println(obj.getClass().toString());
        System.out.println(obj.toString());

    }

    @Test
    public void test_add_duration() throws ParserBase.ParseError, DslException {

        QueryParser parser = new QueryParser();
        Token mod = parser.parse("x = \"2020/01/01\" + 4:30.12");

        System.out.println("parsed: " + mod.toDebugString());
        mod = mod.children().get(0).children().get(1);

        System.out.println("expression: " + mod.toDebugString());

        QObject obj = new Eval().eval(mod);

        System.out.println(obj.getClass().toString());
        System.out.println(obj.toString());

    }
}
