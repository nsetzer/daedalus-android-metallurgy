package com.github.nicksetzer.metallurgy.orm.dsl;

import java.util.List;

public class SqlFormat {

    public static String like_value(String value){
        // when using like or not like construct, the value parameter must also be transformed
        StringBuilder sb = new StringBuilder();
        sb.append("%");
        sb.append(value);
        sb.append("%");
        return sb.toString();
    }

    public static String like(String column_name){
        StringBuilder sb = new StringBuilder();
        sb.append("lower(");
        sb.append(column_name);
        sb.append(") LIKE lower(?)");
        return sb.toString();
    }

    public static String not_like(String column_name){
        StringBuilder sb = new StringBuilder();
        sb.append("lower(");
        sb.append(column_name);
        sb.append(") NOT LIKE lower(?)");
        return sb.toString();
    }

    public static String equal(String column_name) {
        StringBuilder sb = new StringBuilder();
        sb.append(column_name);
        sb.append(" = ?");
        return sb.toString();
    }

    public static String not_equal(String column_name){
        StringBuilder sb = new StringBuilder();
        sb.append(column_name);
        sb.append(" != ?");
        return sb.toString();
    }

    public static String lessthan(String column_name){
        StringBuilder sb = new StringBuilder();
        sb.append(column_name);
        sb.append(" < ?");
        return sb.toString();
    }

    public static String greaterthan(String column_name){
        StringBuilder sb = new StringBuilder();
        sb.append(column_name);
        sb.append(" > ?");
        return sb.toString();
    }

    public static String lessthan_equal(String column_name){
        StringBuilder sb = new StringBuilder();
        sb.append(column_name);
        sb.append(" <= ?");
        return sb.toString();
    }

    public static String greaterthan_equal(String column_name){
        StringBuilder sb = new StringBuilder();
        sb.append(column_name);
        sb.append(" >= ?");
        return sb.toString();
    }

    public static void format_string(StringBuilder out, List<String> params, String operation, String column_name, String value) {

        switch (operation) {
            case "=":
                out.append(SqlFormat.like(column_name));
                params.add(SqlFormat.like_value(value));
                break;
            case "!=":
                out.append(SqlFormat.not_like(column_name));
                params.add(SqlFormat.like_value(value));
                break;
            case "==":
                out.append(SqlFormat.equal(column_name));
                params.add(value);
                break;
            case "!==":
                out.append(SqlFormat.not_equal(column_name));
                params.add(value);
                break;
            case "<":
                out.append(SqlFormat.lessthan(column_name));
                params.add(value);
                break;
            case "<=":
                out.append(SqlFormat.lessthan_equal(column_name));
                params.add(value);
                break;
            case ">":
                out.append(SqlFormat.greaterthan(column_name));
                params.add(value);
                break;
            case ">=":
                out.append(SqlFormat.greaterthan_equal(column_name));
                params.add(value);
                break;
        }
    }

    public static void format_number(StringBuilder out, List<String> params, String operation, String column_name, String value) {

        switch (operation) {
            case "=":
                out.append(SqlFormat.equal(column_name));
                params.add(value);
                break;
            case "!=":
                out.append(SqlFormat.not_equal(column_name));
                params.add(value);
                break;
            case "==":
                out.append(SqlFormat.equal(column_name));
                params.add(value);
                break;
            case "!==":
                out.append(SqlFormat.not_equal(column_name));
                params.add(value);
                break;
            case "<":
                out.append(SqlFormat.lessthan(column_name));
                params.add(value);
                break;
            case "<=":
                out.append(SqlFormat.lessthan_equal(column_name));
                params.add(value);
                break;
            case ">":
                out.append(SqlFormat.greaterthan(column_name));
                params.add(value);
                break;
            case ">=":
                out.append(SqlFormat.greaterthan_equal(column_name));
                params.add(value);
                break;
        }
    }


}
