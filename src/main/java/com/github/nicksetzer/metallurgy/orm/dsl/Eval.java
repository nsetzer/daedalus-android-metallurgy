package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.Stack;

public class Eval {


    Stack<Token> m_sequence;
    Stack<QObject> m_stack;

    public Eval() {

    }


    public QObject eval(Token token) throws DslException {

        unravel(token);

        while (!m_sequence.empty()) {
            Token tok = m_sequence.pop();
            switch (tok.kind()) {
                case P_BINARY:
                    QObject lhs = m_stack.pop();
                    QObject rhs = m_stack.pop();
                    //System.out.println("eval binary: " + lhs.toString() + " " + tok.value() + " " + rhs.toString());
                    switch (tok.value()) {
                        case "+":
                            m_stack.add(lhs.add(rhs));
                            break;
                        case "-":
                            m_stack.add(lhs.sub(rhs));
                            break;
                        case "*":
                            m_stack.add(lhs.mul(rhs));
                            break;
                        case "/":
                            m_stack.add(lhs.div(rhs));
                            break;
                        default:
                            throw new EvalException("illegal unary operation: " + tok.value());
                    }
                    break;
                case P_UNARY:
                    QObject child = m_stack.pop();
                    //System.out.println("eval unary: " + tok.value() + " " + child.toString());
                    switch (tok.value()) {
                        case "-":
                            m_stack.add(child.negate());
                            break;
                        default:
                            throw new EvalException("illegal unary operation: " + tok.value());
                    }
                    break;
                case L_IDENTIFIER:
                    //System.out.println("push identifier: " + tok.toString());
                    m_stack.add(new QString(tok.value()));
                    break;
                case L_STRING:
                    //System.out.println("push string: " + tok.toString());
                    m_stack.add(new QString(StringUtil.unescape(tok.value())));
                    break;
                case L_NUMBER:
                    //System.out.println("push number: " + tok.toString());
                    if (tok.value().contains(".")) {
                        m_stack.add(QDouble.fromString(tok.value()));
                    } else {
                        m_stack.add(QInteger.fromString(tok.value()));
                    }
                    break;
                case L_DATE_DELTA:
                    //System.out.println("push date delta: " + tok.toString());
                    m_stack.add(QDateDelta.fromString(tok.value()));
                    break;
                case L_DURATION:
                    //System.out.println("push duration: " + tok.toString());
                    m_stack.add(QDuration.fromString(tok.value()));
                    break;
                default:
                    throw new EvalException("runtime error");
            }
        }

        //System.out.println("stack: " + m_stack.toString());
        return m_stack.pop();
    }

    /**
     * Unravel an AST into a sequence to be evaluated
     * @param token
     * @throws DslException
     */
    private void unravel(Token token) throws DslException {
        m_sequence = new Stack<>();
        m_stack = new Stack<>();
        Stack<Token> seq = new Stack<>();

        seq.add(token);

        while (!seq.empty()) {
            Token tok = seq.pop();

            switch (tok.kind()) {

                case P_GROUPING:
                    for (Token child : Reversed.reversed(tok.children())) {
                        seq.add(child);
                    }
                    break;
                case P_BINARY:
                case P_UNARY:
                    //System.out.println("1: " + tok.toString());
                    m_sequence.add(tok);
                    for (Token child : Reversed.reversed(tok.children())) {
                        seq.add(child);
                    }
                    break;
                case L_IDENTIFIER:
                case L_STRING:
                case L_NUMBER:
                case L_DATE_DELTA:
                case L_DURATION:
                    //System.out.println("2: " + tok.toString());
                    m_sequence.add(tok);
                    break;
                case P_LOGICAL_AND:
                case P_LOGICAL_NOT:
                case P_LOGICAL_OR:
                default:
                    throw new RuntimeException("unexpected kind: " + tok.kind().name());


            }
        }
    }
}
