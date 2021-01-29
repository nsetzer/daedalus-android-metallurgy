package com.github.nicksetzer.metallurgy.orm.dsl;

public enum TokenKind {
    UNKNOWN,
    L_NEWLINE,
    L_IDENTIFIER,
    L_STRING,
    L_NUMBER,
    L_SYMBOL,
    L_DATE_DELTA,
    L_DURATION,

    P_MODULE,
    P_GROUPING,
    P_UNARY,
    P_BINARY,
    P_COMPARE,
    P_REFERNCE,
    P_LOGICAL_NOT,
    P_LOGICAL_AND,
    P_LOGICAL_OR,
    P_ALL_TEXT,

    T_LITERAL,
}
