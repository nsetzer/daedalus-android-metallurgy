package com.github.nicksetzer.metallurgy.orm.dsl;

import org.junit.Test;

import java.util.ArrayList;

public class QueryParserTest {


    @Test
    public void test_simple2() throws ParserBase.ParseError {

        LexerBase.Iterator iter = new LexerBase.StringIterator("x= -1");
        QueryLexer lexer = new QueryLexer(iter);

        ArrayList<Token> tokens = lexer.lex();

        System.out.println("token count: " + tokens.size());
        for (int i=0; i < tokens.size(); i++) {
            System.out.println(tokens.get(i).toString());
        }

        QueryParser parser = new QueryParser();

        Token mod = parser.parse(tokens);


        System.out.println(mod.toDebugString());
    }

    @Test
    public void test_simple3() throws ParserBase.ParseError {

        QueryParser parser = new QueryParser();
        Token mod = parser.parse("(y=3)");

        System.out.println(mod.toDebugString());
    }

    @Test
    public void test_not() throws ParserBase.ParseError {

        QueryParser parser = new QueryParser();
        Token mod = parser.parse("!x");

        System.out.println(mod.toDebugString());
    }

    @Test
    public void test_simple4() throws ParserBase.ParseError {

        LexerBase.Iterator iter = new LexerBase.StringIterator("a.b<5&&(y=3)");
        QueryLexer lexer = new QueryLexer(iter);

        ArrayList<Token> tokens = lexer.lex();

        System.out.println("token count: " + tokens.size());
        for (int i=0; i < tokens.size(); i++) {
            System.out.println(tokens.get(i).toString());
        }

        QueryParser parser = new QueryParser();

        Token mod = parser.parse(tokens);


        System.out.println(mod.toDebugString());
    }
}
