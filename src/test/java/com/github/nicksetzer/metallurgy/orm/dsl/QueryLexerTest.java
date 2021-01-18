package com.github.nicksetzer.metallurgy.orm.dsl;

import org.junit.Test;

import java.util.ArrayList;

public class QueryLexerTest {


    @Test
    public void test_simple1() {

        LexerBase.Iterator iter = new LexerBase.StringIterator("abc = 0x12_34_DEAD");
        QueryLexer lexer = new QueryLexer(iter);

        ArrayList<Token> tokens = lexer.lex();

        System.out.println("token count: " + tokens.size());
        for (int i=0; i < tokens.size(); i++) {
            System.out.println(tokens.get(i).toString());
        }
    }

    @Test
    public void test_simple2() {

        LexerBase.Iterator iter = new LexerBase.StringIterator("{\":\":\":\"}");
        QueryLexer lexer = new QueryLexer(iter);

        ArrayList<Token> tokens = lexer.lex();

        System.out.println("token count: " + tokens.size());
        for (int i=0; i < tokens.size(); i++) {
            System.out.println(tokens.get(i).toString());
        }
    }


    @Test
    public void test_simple3() {

        LexerBase.Iterator iter = new LexerBase.StringIterator("pi=-3.14");
        QueryLexer lexer = new QueryLexer(iter);

        ArrayList<Token> tokens = lexer.lex();

        System.out.println("token count: " + tokens.size());
        for (int i=0; i < tokens.size(); i++) {
            System.out.println(tokens.get(i).toString());
        }
    }

    @Test
    public void test_datetime() {

        LexerBase.Iterator iter = new LexerBase.StringIterator("date > -5d");
        QueryLexer lexer = new QueryLexer(iter);

        ArrayList<Token> tokens = lexer.lex();

        System.out.println("token count: " + tokens.size());
        for (int i=0; i < tokens.size(); i++) {
            System.out.println(tokens.get(i).toString());
        }
    }
}
