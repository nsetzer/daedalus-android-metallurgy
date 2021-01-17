package com.github.nicksetzer.metallurgy.orm.dsl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LexerBaseTest {


    @Test
    public void test_StringIterator() {

        LexerBase.Iterator iter = new LexerBase.StringIterator("x = 1");

        int codepoint;

        codepoint = iter.getch();
        assertEquals('x', codepoint);

        codepoint = iter.getch();
        assertEquals(' ', codepoint);

        codepoint = iter.peek();
        assertEquals('=', codepoint);

        codepoint = iter.getch();
        assertEquals('=', codepoint);

        Position pos = iter.position();
        assertEquals(1, pos.line());
        assertEquals(2, pos.column());
    }


    @Test
    public void test_unescape() {

        /*
            W1 = 0xD800 + 0x3FF
            W2 = 0xDC00 + 0x3FF
            W1 = 0xdbff
            W2 = 0xdfff
         */

        String s1 = StringUtil.escape("\udbff\udfff");
        System.out.println("str is: " + s1);
        String str = StringUtil.unescape("'abc'");

        System.out.println("str is: " + str);
    }
}
