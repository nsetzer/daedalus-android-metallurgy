package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.ArrayList;

public class LexerBase {

    public interface Iterator {
        int getch();
        int peek();
        Position position();
    }

    public static class StringIterator implements Iterator {
        String m_text;
        int m_length = 0;
        int m_offset = 0;
        int m_line = 1;
        int m_column = -1;

        StringIterator(String text) {
            m_text = text;
            m_offset = 0;
            m_length = text.length();
        }

        public int getch() {
            if (m_offset >= m_length) {
                return 0;
            }

            int codepoint = m_text.codePointAt(m_offset);
            m_offset += Character.charCount(codepoint);

            if (codepoint == '\n') {
                m_line += 1;
                m_column = 0;
            } else {
                m_column += 1;
            }

            return codepoint;
        }

        public int peek() {
            if (m_offset >= m_length) {
                return 0;
            }
            int codepoint = m_text.codePointAt(m_offset);
            return codepoint;
        }

        public Position position() {
            return new Position(m_line, m_column);
        }
    }

    protected Iterator m_iter;
    protected TokenKind m_default_kind;
    protected TokenKind m_current_kind;
    protected StringBuilder m_current_token;
    protected Position m_initial_position;
    protected ArrayList<Token> m_tokens;

    public LexerBase(Iterator iter) {
        m_iter = iter;
        m_default_kind = TokenKind.L_IDENTIFIER;
        m_current_kind = TokenKind.L_IDENTIFIER;
        m_current_token = new StringBuilder();
        m_initial_position = null;
        m_tokens = new ArrayList<>();
    }

    public LexerBase(Iterator iter, TokenKind default_kind) {
        m_iter = iter;
        m_default_kind = default_kind;
        m_current_kind = default_kind;
        m_current_token = new StringBuilder();
        m_initial_position = null;
        m_tokens = new ArrayList<>();
    }

    public void putch(int codepoint) {
        if (m_initial_position == null) {
            m_initial_position = m_iter.position();
        }
        m_current_token.appendCodePoint(codepoint);
    }

    public void push() {
        Token token = new Token(m_current_kind, m_current_token.toString(), m_initial_position);
        m_tokens.add(token);

        reset();
    }

    public void maybe_push() {
        if (m_current_token.length() > 0) {
            push();
        }
    }

    public void push_endl() {
        Token tail = this.tail();

        // prevent duplicates
        if (tail.kind() == TokenKind.L_NEWLINE) {
            return;
        }

        Token token = new Token(m_current_kind, m_current_token.toString(), m_initial_position);
        m_tokens.add(token);

        reset();
    }

    public void reset() {
        m_current_kind = m_default_kind;
        m_initial_position = null;
        m_current_token.setLength(0);
    }

    public Token tail() {
        if (this.m_tokens.size() > 0) {
            return m_tokens.get(m_tokens.size()-1);
        }
        // return an empty token to avoid null checking
        return new Token();
    }

    public String symbol(int codepoint) {
        StringBuilder sb = new StringBuilder(m_current_token);
        sb.appendCodePoint(codepoint);
        return sb.toString();
    }
    /**
     * produce a single token composed only of characters in a limited charset
     * @param kind
     * @param charset
     */
    /*
    public void lex_charset(TokenKind kind, Set<Integer> charset) {

        maybe_push();
        m_current_kind = kind;

        while (true) {
            if (!charset.contains(m_iter.peek())) {
                break;
            }
            putch(m_iter.getch());
        }

        push();

        return;

    }
    */

}
