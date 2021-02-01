package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class QueryTransform {

    public static class TransformError extends DslException {

        public TransformError(Token token, String message) {
            super(token, message);
        }
    }

    public enum SqlType {
        INTEGER,
        FLOAT,
        STRING,
        EPOCHTIME_SECONDS,
        EPOCHTIME_MILLISECONDS,
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
    QDateTime m_current_time;

    public QueryTransform() {
        m_columns = new HashMap<>();
        m_tables = new HashMap<>();
        m_seq = new Stack<>();
        m_out = new StringBuilder();
        m_params = new ArrayList<>();
        m_alltext = false;
        m_alltext_columns = null;
        m_current_time = null;
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

    public Pair<String, List<String>> transform(Token token) throws DslException {

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
                    throw new DslException(tok, "Unhandled Kind for SQL Transform");
            }

        }
        return new Pair<>(m_out.toString(), m_params);
    }

    public void push_children(Token token) {
        for (Token child : Reversed.reversed(token.children())) {
            m_seq.add(child);
        }
    }

    public void handle_compare(Token token) throws DslException {

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

                String rhs_value = this.parse(def.m_type, rhs);
                switch (def.m_type) {
                    case INTEGER:
                        SqlFormat.format_number(m_out, m_params, token.value(), def.m_column, rhs_value);
                        break;
                    case STRING:
                        SqlFormat.format_string(m_out, m_params, token.value(), def.m_column, rhs_value);
                        break;
                    case EPOCHTIME_SECONDS:
                        SqlFormat.format_number(m_out, m_params, token.value(), def.m_column, rhs_value);
                        break;
                    case EPOCHTIME_MILLISECONDS:
                        SqlFormat.format_number(m_out, m_params, token.value(), def.m_column, rhs_value);
                        break;
                    case DURATION:
                        SqlFormat.format_number(m_out, m_params, token.value(), def.m_column, rhs_value);
                        break;
                    default:
                        throw new TransformError(lhs, "unhandled type: " + def.m_type.name());
                }
                /*
                switch (def.m_type) {
                    case INTEGER:
                        SqlFormat.format_number(m_out, m_params, token.value(), def.m_column, parse_integer(rhs));
                        break;
                    case STRING:
                        SqlFormat.format_string(m_out, m_params, token.value(), def.m_column, parse_string(rhs));
                        break;
                    case EPOCHTIME_SECONDS:
                        long epoch_time = System.currentTimeMillis()/1000;
                        epoch_time += _parse_integer(rhs);
                        SqlFormat.format_number(m_out, m_params, token.value(), def.m_column, Long.toString(epoch_time));
                        break;
                    case DURATION:
                    default:
                        throw new TransformError(lhs, "unhandled type: " + def.m_type.name());
                }
                */

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

    public String parse(SqlType type, Token token) throws DslException {

        QObject obj = new Eval().eval(token);

        String result;
        switch (type) {
            case INTEGER:
                result = parse_v2_integer(obj);
                break;
            case FLOAT:
                result = parse_v2_float(obj);
                break;
            case STRING:
                result = parse_v2_string(obj);
                break;
            case EPOCHTIME_SECONDS:
                result = parse_v2_epochtime_seconds(obj);
                break;
            case EPOCHTIME_MILLISECONDS:
                result = parse_v2_epochtime_milliseconds(obj);
                break;
            case DURATION:
                result = parse_v2_duration(obj);
                break;
            default:
                throw new DslException("invalid type");
        }

        return result;
    }

    private String parse_v2_integer(QObject obj) throws DslException {

        Class c = obj.getClass();

        if (c == QInteger.class) {
            return obj.toString();
        } else if (c == QDouble.class) {
            return new QInteger(QDouble.class.cast(obj)).toString();
        }

        throw newTypeError("Integer", c.getSimpleName());
    }

    private String parse_v2_float(QObject obj) throws DslException {
        Class c = obj.getClass();

        if (c == QInteger.class) {
            return new QDouble(QInteger.class.cast(obj)).toString();
        } else if (c == QDouble.class) {
            return obj.toString();
        }

        throw newTypeError("Float", c.getSimpleName());
    }

    private String parse_v2_string(QObject obj) throws DslException {
        Class c = obj.getClass();

        if (c == QString.class) {
            return obj.toString();
        }

        throw newTypeError("String", c.getSimpleName());
    }

    private String parse_v2_epochtime_seconds(QObject obj) throws DslException {
        Class c = obj.getClass();

        if (c == QDateTime.class) {
            QDateTime dt = QDateTime.class.cast(obj);
            return Long.toString(dt.toEpochTime() / 1000);
        } else if (c == QString.class) {
            QString s = QString.class.cast(obj);
            QDateTime dt = QDateTime.fromString(s.toString());
            return Long.toString(dt.toEpochTime() / 1000);
        } else if (m_current_time != null && c == QDateDelta.class) {
            QDateDelta dd = QDateDelta.class.cast(obj);
            QDateTime result = QDateTime.class.cast(m_current_time.add(dd));
            return Long.toString(result.toEpochTime()/1000);
        } else if (m_current_time != null && c == QDuration.class) {
            QDuration dd = QDuration.class.cast(obj);
            QDateTime result = QDateTime.class.cast(m_current_time.add(dd));
            return Long.toString(result.toEpochTime()/1000);
        }

        throw newTypeError("DateTime", c.getSimpleName());
    }

    private String parse_v2_epochtime_milliseconds(QObject obj) throws DslException {
        Class c = obj.getClass();
        if (c == QDateTime.class) {
            QDateTime dt = QDateTime.class.cast(obj);
            return Long.toString(dt.toEpochTime());
        } else if (c == QString.class) {
            QString s = QString.class.cast(obj);
            QDateTime dt = QDateTime.fromString(s.toString());
            return Long.toString(dt.toEpochTime());
        } else if (m_current_time != null && c == QDateDelta.class) {
            QDateDelta dd = QDateDelta.class.cast(obj);
            QDateTime result = QDateTime.class.cast(m_current_time.add(dd));
            return Long.toString(result.toEpochTime());
        } else if (m_current_time != null && c == QDuration.class) {
            QDuration dd = QDuration.class.cast(obj);
            QDateTime result = QDateTime.class.cast(m_current_time.add(dd));
            return Long.toString(result.toEpochTime());
        }
        throw newTypeError("DateTime", c.getSimpleName());
    }

    private String parse_v2_duration(QObject obj) throws DslException {
        Class c = obj.getClass();

        if (c == QDuration.class) {
            QDuration dd = QDuration.class.cast(obj);
            return Long.toString(dd.toSeconds());
        } else if (c == QInteger.class) {
            return obj.toString();
        } else if (c == QDouble.class) {
            return new QInteger(QDouble.class.cast(obj)).toString();
        }

        throw newTypeError("Duration", c.getSimpleName());
    }

    private DslException newTypeError(String expected, String received) {
        return new DslException("Type Error. Expected: " + expected +
                " But received type: " + received);
    }

    public void setCurrentDateTime(QDateTime dt) {
        m_current_time = dt;
    }
}
