package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class QueryLexer extends LexerBase {

    Set<Integer> charset_lowercase;
    Set<Integer> charset_uppercase;
    Set<Integer> charset_natural;
    Set<Integer> charset_number;
    Set<Integer> charset_string;
    Set<Integer> charset_symbol1;
    Set<Integer> charset_symbol2;

    Set<String> strset_operator1;
    Set<String> strset_operator2;
    Set<String> strset_operator3;

    public QueryLexer(LexerBase.Iterator iter) {
        super(iter);

        charset_natural = mkSet("0123456789");
        charset_lowercase = mkSet("abcdefghijklmnopqrstuvwxyz");
        charset_uppercase = mkSet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        charset_natural = mkSet("0123456789");
        // numbers can have suffix:
        //  s,h,d,w,m,y : seconds, hours, days, weeks, months, years
        //  j : imaginary
        // numbers can have prefix:
        //  0x 0o 0n 0b
        // numbers can be separated using _
        // numbers can be hex, oct, nibble, or binary
        //
        charset_number = mkSet("0123456789nxob_.jtgshwmykABCDEFabcdef");
        charset_string = mkSet("\"'`");
        charset_symbol1 = mkSet("{}[](),~;:#?"); // symbols that never combine
        charset_symbol2 = mkSet("+-*/&|^=<>%!@."); // symbols that may combine

        strset_operator1 = mkSet(new String[]{
                "+", "-", "~", "*", "/", "%", "@", "&", "^", "|", "!",
                ":", ".", ",", ";", "=", "(", ")", "{", "}", "[", "]",
                "#"});

        strset_operator2 = mkSet(new String[]{
                "<=", ">=", "==", "!=", "!==", "||", "&&"});
    }

    private Set<Integer> mkSet(String str) {
        Set<Integer> set = new HashSet<>();
        int len = str.length();
        for (int i=0; i < len; i++) {
            int c = str.charAt(i);
            set.add(c);
        }
        return set;
    }

    private Set<String> mkSet(String[] strs) {
        Set<String> set = new HashSet<String>();
        int len = strs.length;
        for (int i=0; i < len; i++) {
            set.add(strs[i]);
        }
        return set;
    }
    public ArrayList<Token> lex() {

        m_tokens = new ArrayList<>();

        while (true) {

            int codepoint = m_iter.peek();

            if (codepoint == 0) {
                break;
            }

            int rv = 0;
            if (codepoint == '\n') {
                m_iter.getch();
                push_endl();
            } else if (charset_symbol1.contains(codepoint)) {
                rv = lex_symbol();
            } else if (charset_symbol2.contains(codepoint)) {
                rv = lex_combining_symbol();
            } else if (m_current_token.length()==0 && charset_natural.contains(codepoint)) {
                // lex_charset(TokenKind.L_NUMBER, charset_number);
                rv = lex_number();
            } else if (charset_string.contains(codepoint)) {
                rv = lex_string();
            } else if (Character.isWhitespace(codepoint)) {
                m_iter.getch();
                maybe_push();
            } else {
                putch(m_iter.getch());
            }

            if (rv != 0) {
                break;
            }

        }

        maybe_push();

        return m_tokens;
    }

    public int lex_number() {

        maybe_push();
        m_current_kind = TokenKind.L_NUMBER;

        while (true) {
            int codepoint = m_iter.peek();
            if (!charset_number.contains(codepoint)) {
                break;
            }
            putch(m_iter.getch());
        }

        push();

        return 0;

    }

    public int lex_string() {

        maybe_push();
        this.m_current_kind = TokenKind.L_STRING;

        int quote = m_iter.getch();
        putch(quote);

        while (true) {
            int codepoint = m_iter.peek();

            if (codepoint == 0) {
                return 1; // unexpected end of stream
            } else if (codepoint == '\\') {
                putch(m_iter.getch());
                codepoint = m_iter.peek();

                if (codepoint == 0) {
                    return 2; // unexpected end of stream, invalid escape sequence
                }

                putch(m_iter.getch());

            } else if (codepoint == quote) {
                // string terminal found
                putch(m_iter.getch());
                break;
            } else {
                putch(m_iter.getch());
            }

        }

        push();

        return 0;

    }

    public int lex_symbol() {
        maybe_push();
        this.m_current_kind = TokenKind.L_SYMBOL;
        putch(m_iter.getch());
        push();

        return 0;
    }

    public int lex_combining_symbol() {
        maybe_push();
        m_current_kind = TokenKind.L_SYMBOL;

        while (true) {
            int codepoint = m_iter.peek();
            if (!charset_symbol2.contains(codepoint)) {
                break;
            }
            // push any existing symbol if the combination with this
            // new code point is not valid
            if (!strset_operator2.contains(symbol(codepoint))) {
                maybe_push();
                m_current_kind = TokenKind.L_SYMBOL;
            }
            putch(m_iter.getch());
        }

        push();

        return 0;
    }
}
