package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryParser extends ParserBase {


    public class BinaryParserRule extends ParserBase.ParserRuleBase {

        Set<String> m_operators;
        TokenKind m_output_kind;

        public BinaryParserRule(int direction, Set<String> operators) {
            super(direction);
            m_operators = operators;
            m_output_kind = TokenKind.P_BINARY;
        }

        public BinaryParserRule(int direction, TokenKind output_kind, Set<String> operators) {
            super(direction);
            m_operators = operators;
            m_output_kind = output_kind;
        }

        /**
         * return and remove token.children().get(index + direction) if it exists
         *
         * @param token
         * @param index
         * @param direction
         * @return
         */
        public Token consume(Token token, int index, int direction) throws ParserBase.ParseError {

            int j = index + direction;
            while (j >= 0 && j < token.children().size()) {

                Token tok = token.children().get(j);
                if (tok.kind() == TokenKind.L_SYMBOL && (";".equals(tok.value()) || ",".equals(tok.value()))) {
                    break;
                } else if (tok.kind() == TokenKind.L_NEWLINE) {
                    j += direction;
                } else {
                    token.children().remove(j);
                    return tok;
                }
            }

            String side = (direction<0?"lhs":"rhs");
            throw new ParserBase.ParseError(token.children().get(index),
                    "invalid token on " + side + ".");
        }

        public int execute(Token token, int child_index) throws ParserBase.ParseError {

            Token child = token.children().get(child_index);

            // test accept
            if (child.kind() != TokenKind.L_SYMBOL || !m_operators.contains(child.value())) {
                return 1;
            }

            Token rhs = consume(token, child_index, 1);
            Token lhs = consume(token, child_index, -1);

            child.m_kind = m_output_kind;

            child.children().add(lhs);
            child.children().add(rhs);

            return 0;
        }
    }

    public class UnaryPrefixParserRule extends ParserBase.ParserRuleBase {

        Set<String> m_operators;
        TokenKind m_output_kind;

        public UnaryPrefixParserRule(int direction, Set<String> operators) {
            super(direction);
            m_operators = operators;
            m_output_kind = TokenKind.P_UNARY;
        }

        public UnaryPrefixParserRule(int direction, TokenKind output_kind, Set<String> operators) {
            super(direction);
            m_operators = operators;
            m_output_kind = output_kind;
        }

        /**
         * return and remove token.children().get(index + direction) if it exists
         *
         * @param token
         * @param index
         * @param direction
         * @return
         */
        public Token consume(Token token, int index, int direction) throws ParserBase.ParseError {

            int j = index + direction;
            while (j >= 0 && j < token.children().size()) {

                Token tok = token.children().get(j);
                if (tok.kind() == TokenKind.L_SYMBOL && (";".equals(tok.value()) || ",".equals(tok.value()))) {
                    break;
                } else if (tok.kind() == TokenKind.L_NEWLINE) {
                    j += direction;
                } else {
                    token.children().remove(j);
                    return tok;
                }
            }

            String side = (direction<0?"lhs":"rhs");
            throw new ParserBase.ParseError(token.children().get(index),
                    "invalid token on " + side + ".");
        }

        public int execute(Token token, int child_index) throws ParserBase.ParseError {

            Token child = token.children().get(child_index);

            // test accept
            if (child.kind() != TokenKind.L_SYMBOL || !m_operators.contains(child.value())) {

                return 1;
            }

            if (child_index > 0) {

                Token lhs = token.children().get(child_index - 1);
                if (lhs.kind() != TokenKind.L_SYMBOL && lhs.kind() != TokenKind.P_COMPARE) {

                    return 1;
                }
            }

            Token rhs = consume(token, child_index, 1);

            child.m_kind = m_output_kind;

            child.children().add(rhs);

            return 0;
        }
    }

    public class KeywordParserRule extends ParserBase.ParserRuleBase {

        private String m_now_string_value = null;

        public KeywordParserRule() {
            super(L2R);
        }

        public int execute(Token token, int child_index) throws ParserBase.ParseError {
            Token child = token.children().get(child_index);

            if (child.kind()==TokenKind.L_IDENTIFIER) {

                if (child.value().equals("not")) {
                    child.m_kind = TokenKind.L_SYMBOL;
                    child.m_value = "!";
                }

                if (child.value().equals("and")) {
                    child.m_kind = TokenKind.L_SYMBOL;
                    child.m_value = "&&";
                }

                if (child.value().equals("or")) {
                    child.m_kind = TokenKind.L_SYMBOL;
                    child.m_value = "||";
                }

                if (child.value().equals("eq")) {
                    child.m_kind = TokenKind.L_SYMBOL;
                    child.m_value = "=";
                }

                if (child.value().equals("ne")) {
                    child.m_kind = TokenKind.L_SYMBOL;
                    child.m_value = "!=";
                }

                if (child.value().equals("lt")) {
                    child.m_kind = TokenKind.L_SYMBOL;
                    child.m_value = "<";
                }

                if (child.value().equals("gt")) {
                    child.m_kind = TokenKind.L_SYMBOL;
                    child.m_value = ">";
                }

                if (child.value().equals("le")) {
                    child.m_kind = TokenKind.L_SYMBOL;
                    child.m_value = "<=";
                }

                if (child.value().equals("ge")) {
                    child.m_kind = TokenKind.L_SYMBOL;
                    child.m_value = ">=";
                }

                if (m_now_string_value != null && child.value().equals("now")) {
                    child.m_kind = TokenKind.L_STRING;
                    child.m_value = m_now_string_value;
                }
            }

            return 1;
        }

        public void setCurrentDateTime(QDateTime dt) {
            m_now_string_value = StringUtil.escape(dt.toString());
        }
    }

    public class AllTextParserRule extends ParserBase.ParserRuleBase {

        public AllTextParserRule() {
            super(ParserBase.L2R);
        }

        public int execute(Token token, int child_index) throws ParserBase.ParseError {
            Token child = token.children().get(child_index);

            if (child.kind()==TokenKind.L_STRING || child.kind()==TokenKind.L_NUMBER || child.kind()==TokenKind.L_IDENTIFIER) {
                Token cmp = new Token(TokenKind.P_COMPARE, "=", child.position());
                cmp.children().add(new Token(TokenKind.P_ALL_TEXT, "", child.position()));
                cmp.children().add(child);
                token.children().set(child_index, cmp);
            }

            return 1;
        }
    }

    public class DefaultAndParserRule extends ParserBase.ParserRuleBase {
        public DefaultAndParserRule() {
            super(ParserBase.L2R);
        }

        /**
         * apply a fix to Parenthetical Grouping.
         *
         *
         *
         * @param token
         * @param child_index
         * @return
         * @throws ParserBase.ParseError
         */
        public int execute(Token token, int child_index) throws ParserBase.ParseError {

            if (token.kind() == TokenKind.P_GROUPING && token.value().equals("()")) {

                while (token.children().size() > 1) {
                    Token tmp = new Token(TokenKind.P_LOGICAL_AND, "&&", token.position());
                    tmp.children().add(token.children().remove(0));
                    tmp.children().add(token.children().remove(0));
                    token.children().add(0, tmp);
                }

            }

            return 1;
        }
    }

    KeywordParserRule m_keyword_rule;

    public QueryParser() {

        super();

        m_root_kind = TokenKind.P_GROUPING;
        m_root_value = "()";

        // only allow grouping using parentheticals
        m_group_pairs.put("(", ")");

        m_keyword_rule = new KeywordParserRule();

        addRule(new KeywordParserRule()); // detect keywords
        addRule(new BinaryParserRule(L2R, newStrSet(new String[]{"."}))); // for attribute access
        addRule(new BinaryParserRule(L2R, newStrSet(new String[]{"/"}))); // for dates
        addRule(new BinaryParserRule(L2R, newStrSet(new String[]{":"}))); // for time

        addRule(new UnaryPrefixParserRule(R2L, newStrSet(new String[]{"+", "-"})));
        addRule(new BinaryParserRule(L2R, newStrSet(new String[]{"*", "/", "//"})));
        addRule(new BinaryParserRule(L2R, newStrSet(new String[]{"+", "-",})));
        addRule(new UnaryPrefixParserRule(R2L, TokenKind.P_REFERNCE, newStrSet(new String[]{"&"})));
        addRule(new BinaryParserRule(L2R, TokenKind.P_COMPARE, newStrSet(new String[]{"=", "==", "<", "<=", ">=", ">"})));
        addRule(new AllTextParserRule());

        addRule(new UnaryPrefixParserRule(L2R, TokenKind.P_LOGICAL_NOT, newStrSet(new String[]{"!"})));
        addRule(new BinaryParserRule(L2R, TokenKind.P_LOGICAL_AND, newStrSet(new String[]{"&&"})));
        addRule(new BinaryParserRule(L2R, TokenKind.P_LOGICAL_OR, newStrSet(new String[]{"||"})));
        addRule(new DefaultAndParserRule());

    }

    public Token parse(String text) throws ParseError {
        LexerBase.Iterator iter = new LexerBase.StringIterator(text);
        QueryLexer lexer = new QueryLexer(iter);
        List<Token> tokens = lexer.lex();
        return parse(tokens);
    }

    public void setCurrentDateTime(QDateTime dt) {
        m_keyword_rule.setCurrentDateTime(dt);
    }

}
