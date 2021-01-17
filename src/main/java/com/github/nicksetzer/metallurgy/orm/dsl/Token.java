package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.ArrayList;
import java.util.List;

public class Token {

    TokenKind m_kind;
    String m_value;
    Position m_pos;
    List<Token> m_children;

    public Token() {
        m_kind = TokenKind.UNKNOWN;
        m_value = "";
        m_pos = new Position(-1, -1);
        m_children = new ArrayList<Token>();
    }

    public Token(TokenKind kind, String value, Position pos) {
        m_kind = kind;
        m_value = value;
        m_pos = pos;
        m_children = new ArrayList<Token>();
    }

    public Token(TokenKind kind, String value, Position pos,  List<Token> children) {
        m_kind = kind;
        m_value = value;
        m_pos = pos;
        m_children = children;
    }

    public TokenKind kind() {
        return m_kind;
    }

    public String value() {
        return m_value;
    }

    public Position position() {
        return m_pos;
    }

    public List<Token> children() {
        return m_children;
    }

    public String toString() {
        return m_kind.name() + "<" + StringUtil.escape(m_value) + ">";
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append(toString());
        if (m_children.size() > 0) {
            sb.append("{");

            boolean first = true;
            for (int i=0; i<m_children.size(); i++) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(m_children.get(i).toDebugString());
                first = false;
            }

            sb.append("}");
        }
        return sb.toString();
    }
}
