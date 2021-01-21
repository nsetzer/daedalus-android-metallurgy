package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;

public class QueryTransform {

    public static class TransformError extends DslException {

        public TransformError(Token token, String message) {
            super(token, message);
        }
    }

    public enum SqlType {
        NUMBER,
        STRING,
        EPOCHTIME,
        DURATION,
    }

    public class ColumnDef {

        String m_column;
        SqlType m_type;
        Set<String> m_aliases;

        public ColumnDef(String column, String[] aliases, SqlType type) {
            m_aliases = new HashSet<String>();
            for (int i=0; i < aliases.length; i++) {
                m_aliases.add(aliases[i]);
            }
            m_column = column;
            m_type = type;
        }

    }

    /**
     * A TableDef allows for more complicated queries by referencing
     * other tables or views.
     *
     * the query syntax is:
     *      <table_alias>.<column_alias>
     * and this will compile to:
     *      <column_name>
     * The column name for columns defined on the table should be the correct
     * reference for the chosen SQL engine and database
     */
    public class TableDef {
        String m_table_name;
        Set<String> m_aliases;
        Map<String, ColumnDef> m_columns;

        public TableDef(String table_name, String[] aliases) {
            m_table_name = table_name;
            m_aliases = new HashSet<>();
            for (int i=0; i < aliases.length; i++) {
                m_aliases.add(aliases[i]);
            }
            m_columns = new HashMap<>();
        }

        public void addColumnDef(String column, String[] aliases, SqlType type) {
            m_columns.put(column, new ColumnDef(column, aliases, type));
        }

        public ColumnDef get_column(String column_alias) {
            ColumnDef def = null;
            for (ColumnDef tmp : m_columns.values()) {
                if (tmp.m_aliases.contains(column_alias)) {
                    def = tmp;
                }
            }
            return def;
        }

    }

    Map<String, ColumnDef> m_columns;
    Map<String, TableDef> m_tables;
    Stack<Token> m_seq;
    StringBuilder m_out;
    List<String> m_params;

    boolean m_alltext;
    String[] m_alltext_columns;

    public QueryTransform() {
        m_columns = new HashMap<>();
        m_tables = new HashMap<>();
        m_seq = new Stack<>();
        m_out = new StringBuilder();
        m_params = new ArrayList<>();
        m_alltext = false;
        m_alltext_columns = null;
    }

    public void enableAllText(String[] columns) {
        m_alltext = true;
        m_alltext_columns = columns;
    }

    public void addColumnDef(String column, String[] aliases, SqlType type) {
        m_columns.put(column, new ColumnDef(column, aliases, type));
    }

    public TableDef addTableDef(String table_name, String[] aliases) {
        TableDef def = new TableDef(table_name, aliases);
        m_tables.put(table_name, def);
        return def;
    }

    public Pair<String, List<String>> transform(Token token) throws TransformError {

        m_seq.add(token);

        while (m_seq.size() > 0) {

            Token tok = m_seq.pop();

            switch (tok.kind()) {
                case P_GROUPING:
                    m_seq.add(new Token(TokenKind.T_LITERAL, ")", tok.position()));
                    push_children(tok);
                    m_seq.add(new Token(TokenKind.T_LITERAL, "(", tok.position()));
                    break;
                case P_COMPARE:
                    handle_compare(tok);
                    break;
                case T_LITERAL:
                    m_out.append(tok.value());
                    break;
                case P_LOGICAL_NOT:
                    Token child = tok.children().get(0);
                    m_seq.add(child);
                    m_seq.add(new Token(TokenKind.T_LITERAL, " NOT ", tok.position()));
                    break;
                case P_LOGICAL_AND:
                    Token lhs = tok.children().get(0);
                    Token rhs = tok.children().get(1);
                    m_seq.add(rhs);
                    m_seq.add(new Token(TokenKind.T_LITERAL, " AND ", tok.position()));
                    m_seq.add(lhs);
                    break;
                case P_LOGICAL_OR:
                    lhs = tok.children().get(0);
                    rhs = tok.children().get(1);
                    m_seq.add(rhs);
                    m_seq.add(new Token(TokenKind.T_LITERAL, " OR ", tok.position()));
                    m_seq.add(lhs);
                    break;
                default:
                    System.out.println("unhandled kind: " + tok.kind().name());
                    break;
            }

        }
        return new Pair<>(m_out.toString(), m_params);
    }

    public void push_children(Token token) {
        for (Token child : new Reversed<>(token.children())) {
            m_seq.add(child);
        }
    }

    public void handle_compare(Token token) throws TransformError {

        Token lhs = token.children().get(0);
        Token rhs = token.children().get(1);

        if (m_alltext && lhs.kind() == TokenKind.P_ALL_TEXT) {
            boolean first = true;
            m_out.append("(");
            for (String column : m_alltext_columns) {
                if (!first) {
                    m_out.append(" OR ");
                }
                String value = parse_string(rhs);

                SqlFormat.format_string(m_out, m_params, token.value(), column, value);

                first = false;
            }
            m_out.append(")");

        } else {

            ColumnDef def = parse_identifier(lhs);

            if (rhs.kind() == TokenKind.P_REFERNCE) {
                ColumnDef ref = parse_identifier(rhs.children().get(0));
                // TODO: this should have a LHS column-type dependant construction
                m_out.append(def.m_column);
                m_out.append(" ");
                m_out.append(token.value());
                m_out.append(" ");
                m_out.append(ref.m_column);
            } else {

                switch (def.m_type) {
                    case NUMBER:
                        SqlFormat.format_number(m_out, m_params, token.value(), def.m_column, parse_integer(rhs));
                        break;
                    case STRING:
                        SqlFormat.format_string(m_out, m_params, token.value(), def.m_column, parse_string(rhs));
                        break;
                    case EPOCHTIME:
                        long epoch_time = System.currentTimeMillis()/1000;
                        epoch_time += _parse_integer(rhs);
                        SqlFormat.format_number(m_out, m_params, token.value(), def.m_column, Long.toString(epoch_time));
                        break;
                    case DURATION:
                    default:
                        throw new TransformError(lhs, "unhandled type: " + def.m_type.name());
                }

            }

        }


    }

    /**
     * parse a token as a column definition, possibly for a specific table
     *
     * @param token
     * @return
     * @throws TransformError
     */
    public ColumnDef parse_identifier(Token token) throws TransformError {
        if (token.kind() == TokenKind.P_BINARY && token.value().equals(".")) {
            Token lhs = token.children().get(0);
            if (lhs.kind() != TokenKind.L_IDENTIFIER) {
                throw new TransformError(lhs, "expected identifier on lhs");
            }
            Token rhs = token.children().get(1);
            if (rhs.kind() != TokenKind.L_IDENTIFIER) {
                throw new TransformError(rhs, "expected identifier on rhs");
            }

            // find the table corresponding the the lhs alias
            TableDef tbl = null;
            for (TableDef tmp : m_tables.values()) {
                if (tmp.m_aliases.contains(lhs.value())) {
                    tbl = tmp;
                }
            }
            if (tbl == null) {
                throw new TransformError(lhs, "invalid table alias");
            }

            // find the column of the selected table
            ColumnDef def = tbl.get_column(rhs.value());

            if (def == null) {
                throw new TransformError(rhs, "invalid column alias for table " + StringUtil.escape(tbl.m_table_name));
            }

            return def;
        } else {

            if (token.kind() != TokenKind.L_IDENTIFIER) {
                throw new TransformError(token, "expected identifier");
            }

            ColumnDef def = null;
            for (ColumnDef tmp : m_columns.values()) {
                if (tmp.m_aliases.contains(token.value())) {
                    def = tmp;
                }
            }

            if (def == null) {
                throw new TransformError(token, "invalid column alias");
            }

            return def;
        }
    }
    /**
     * parse a token and return the date as an epoch time
     *  L_NUMBER -> year/01/01
     *  L_NUMBER / L_NUMBER -> year/month/01
     *  L_NUMBER / L_NUMBER / L_NUMBER -> year/month/day
     *
     * @param token
     * @return epoch time
     */
    public int parse_date(Token token) {

        return 0;
    }

    /**
     * parse a token and return 24-hour time as seconds
     * @param token
     * @return
     */
    public int parse_time(Token token) {
        return 0;
    }

    /**
     * parse a token and return duration as seconds
     * @param token
     * @return
     */
    public int parse_duration(Token token) {
        return 0;
    }
    /**
     * parse a token and return 24-hour time as seconds
     *   L_NUMBER
     *   UNARY<-> L_NUMBER
     * @param token
     * @return
     */
    public int _parse_integer(Token token) throws TransformError {

        boolean negate = false;

        while (token.kind() == TokenKind.P_UNARY) {
            if (token.children().size()==0) {
                throw new TransformError(token, "missing rhs");
            }
            if (token.value().equals("-")) {
                negate = !negate;
            } else if (token.value().equals("+")) {
                negate = false;
            }
            token = token.children().get(0);
        }

        if (token.kind() != TokenKind.L_NUMBER) {
            throw new TransformError(token, "not a number");
        }

        String value = token.value();
        value = value.replaceAll("_", "");

        int ivalue;

        int multiplier = 1;
        /*if (value.endsWith("ms")) {
            multiplier = 1;
            value = value.substring(0, value.length() - 2);
        } else */
        if (value.endsWith("s")) {
            multiplier = 1;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("m")) {
            multiplier = 60;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("h")) {
            multiplier = 60*60;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("d")) {
            // TODO: should be now.day +/- 1...
            multiplier = 60*60*24;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("w")) {
            // TODO: should be now.day +/- 7...
            multiplier = 60*60*24*7;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("M")) {
            // TODO: should be now.month +/- 1...
            multiplier = 60*60*24*28;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("y")) {
            // TODO: should be now.year +/- 1...
            multiplier = 60*60*24*52;
            value = value.substring(0, value.length() - 1);
        }

        if (value.startsWith("0x")) {
            ivalue = Integer.parseInt(value.substring(2), 16);
        } else if (value.startsWith("0o")) {
            ivalue = Integer.parseInt(value.substring(2), 8);
        } else if (value.startsWith("0n")) {
            ivalue = Integer.parseInt(value.substring(2), 4);
        } else if (value.startsWith("0b")) {
            ivalue = Integer.parseInt(value.substring(2), 2);
        } else {
            ivalue = Integer.parseInt(value);
        }

        if (negate) {
            ivalue *= -1;
        }

        ivalue *= multiplier;

        return ivalue;
    }

    public String parse_integer(Token token) throws TransformError {
        return Integer.toString(_parse_integer(token));
    }

    /**
     * parse a token and return 24-hour time as seconds
     *   L_STRING :: a javascript string literal to string
     * @param token
     * @return
     */
    public String parse_string(Token token) throws TransformError {
        switch (token.kind()) {
            case L_IDENTIFIER:
                return token.value();
            case L_STRING:
                return StringUtil.unescape(token.value());
            default:
                throw new TransformError(token, "not a string");
        }
    }
}
