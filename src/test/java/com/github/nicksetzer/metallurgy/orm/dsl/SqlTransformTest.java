package com.github.nicksetzer.metallurgy.orm.dsl;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SqlTransformTest {


    private static class TestSqlTransform extends SqlTransform {

        TestSqlTransform() {
            addColumnDef("rating", new String[]{"rating", "rte"}, SqlType.NUMBER);
            TableDef tbl = addTableDef("user", new String[]{"user"});
            tbl.addColumnDef("user.name", new String[]{"name"}, SqlType.STRING);
            enableAllText(new String[]{"artist", "albums"});
        }

        static Pair<String, List<String>> do_transform(Token mod) throws TransformError {
            TestSqlTransform xform = new TestSqlTransform();
            return xform.transform(mod);
        }
    }

    @Test
    public void test_all_text_1() throws ParserBase.ParseError, SqlTransform.TransformError {

        QueryParser parser = new QueryParser();
        Token mod = parser.parse("stp");
        //System.out.println(mod.toDebugString());

        Pair<String, List<String>> pair = TestSqlTransform.do_transform(mod);

        Assert.assertEquals("((lower(artist) LIKE lower(?) OR lower(albums) LIKE lower(?)))", pair.first);
        Assert.assertEquals(2, pair.second.size());
        Assert.assertEquals("%stp%", pair.second.get(0));
        Assert.assertEquals("%stp%", pair.second.get(1));
    }

    @Test
    public void test_number_1() throws ParserBase.ParseError, SqlTransform.TransformError {

        QueryParser parser = new QueryParser();
        Token mod = parser.parse("rte < 0xF");
        //System.out.println(mod.toDebugString());

        Pair<String, List<String>> pair = TestSqlTransform.do_transform(mod);

        Assert.assertEquals("(rating < ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("15", pair.second.get(0));
    }

    @Test
    public void test_table_def() throws ParserBase.ParseError, SqlTransform.TransformError {

        QueryParser parser = new QueryParser();
        Token mod = parser.parse("user.name == 'bob'");
        //System.out.println(mod.toDebugString());

        Pair<String, List<String>> pair = TestSqlTransform.do_transform(mod);

        Assert.assertEquals("(user.name = ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("bob", pair.second.get(0));
    }

    @Test
    public void test_all_text_2() throws ParserBase.ParseError, SqlTransform.TransformError {

        System.out.println(DslException.format(new Token(TokenKind.P_COMPARE, "==", new Position(1,0)), "sample error"));
        QueryParser parser = new QueryParser();
        //Token mod = parser.parse("rte < 5 || stp");
        Token mod = parser.parse("rating = -+-0b1010");
        System.out.println(mod.toDebugString());

        SqlTransform xform = new SqlTransform();
        xform.addColumnDef("rating", new String[]{"rte"}, SqlTransform.SqlType.NUMBER);
        xform.enableAllText(new String[]{"artist", "albums"});

        Pair<String, List<String>> pair = xform.transform(mod);


        System.out.println("sql:" + pair.first);
        for (int i=0; i < pair.second.size(); i++) {
            System.out.println(i + ":" + pair.second.get(i));
        }
    }
}
