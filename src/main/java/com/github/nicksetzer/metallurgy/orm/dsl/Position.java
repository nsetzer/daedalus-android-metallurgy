package com.github.nicksetzer.metallurgy.orm.dsl;

public class Position {
    int m_line;
    int m_column;

    public Position(int line, int column) {
        m_line = line;
        m_column = column;
    }

    public int line() {
        return m_line;
    }

    public int column() {
        return m_column;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("line: ");
        sb.append(m_line);
        sb.append(" column: ");
        sb.append(m_column);
        return sb.toString();
    }
}
