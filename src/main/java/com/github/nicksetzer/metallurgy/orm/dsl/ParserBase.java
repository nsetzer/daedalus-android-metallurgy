package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParserBase {

    public static final int L2R = 1;
    public static final int R2L = -1;

    public static class ParseError extends DslException {

        public ParseError(Token token, String message) {

            super(token, message);
        }
    }

    public interface ParserRule {
        /**
         * specifies the scanning direction for when applying this rule
         * @return -1 (R2L) or 1 (L2R)
         */
        int direction();

        /**
         * perform a parser rule on the child at index child_index for a given token
         * @param token the token to process
         * @param child_index the index of the token's child to process
         * @return the count of tokens to advance the iterator (positive or negative)
         *         for example a rule which does not apply should return 1 (skip the current node)
         *         a rule which consumes the nodes to the left and right (a binary rule)
         *         should return 0. (where 0 is +1 (skip this node) -1 (consumed one node to the left).
         * @throws ParseError
         */
        int execute(Token token, int child_index) throws ParseError;

    }

    public static class ParserRuleBase implements ParserRule {

        int m_direction;

        public ParserRuleBase(int direction) {
            m_direction = direction;
        }

        public int direction() {
            return m_direction;
        }

        public int execute(Token token, int child_index) throws ParseError {
            return 0;
        }
    }

    List<ParserRule> m_rules;
    TokenKind m_root_kind;
    String m_root_value;
    Map<String, String> m_group_pairs;

    public ParserBase() {
        m_rules = new ArrayList<>();
        m_root_kind = TokenKind.P_MODULE;
        m_root_value = "";

        m_group_pairs = new HashMap<>();
    }

    public void addRule(ParserRule rule) {
        m_rules.add(rule);
    }

    public Token parse(List<Token> tokens) throws ParseError {

        int index = tokens.size() - 1;
        while (index >= 0) {
            Token tok = tokens.get(index);
            if (tok.kind() == TokenKind.L_SYMBOL && m_group_pairs.containsKey(tok.value())) {
                Token grp = group(tokens, index, tok.value(), m_group_pairs.get(tok.value()));
                scan(grp);
            }
            index -= 1;
        }

        //Token mod = new Token(TokenKind.P_MODULE, "", new Position(1, 0), tokens);
        Token mod = new Token(m_root_kind, m_root_value, new Position(1, 0), tokens);

        scan(mod);

        return mod;
    }

    public Token group(List<Token> tokens, int index, String open, String close) throws ParseError {

        Token grp = tokens.get(index);
        grp.m_value = open + close;
        grp.m_kind = TokenKind.P_GROUPING;

        int start = index;
        int end = -1;
        int length = tokens.size();
        while (index < length) {
            Token tok = tokens.get(index);
            if (tok.kind() == TokenKind.L_SYMBOL && tok.value().equals(close)) {
                end = index;
                break;
            }
            index += 1;
        }

        if (end < 0) {
            throw new ParseError(grp, "matching '" + close + "' not found");
        }

        tokens.remove(end);
        List<Token> sub = tokens.subList(start+1, end);
        grp.m_children = new ArrayList<>(sub);
        sub.clear();
        return grp;
    }

    public void scan(Token token) throws ParseError {

        for (ParserRule rule : m_rules) {
            int i = 0;

            while (i < token.children().size()) {

                int j = i;
                if (rule.direction() < 0) {
                    j = token.children().size() - i - 1;
                }

                i += rule.execute(token, j);
            }
        }
    }

    public Set<String> newStrSet(String[] strs) {
        Set<String> set = new HashSet<>();
        for (int i=0; i < strs.length; i++) {
            set.add(strs[i]);
        }
        return set;
    }
}
