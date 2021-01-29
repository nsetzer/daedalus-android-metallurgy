package com.github.nicksetzer.metallurgy.orm.dsl;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class QueryTransformTest {


    private static class TestQueryTransform extends QueryTransform {

        TestQueryTransform() {
            addColumnDef("name", new String[]{"name"}, SqlType.STRING);
            addColumnDef("rating", new String[]{"rating", "rte"}, SqlType.INTEGER);
            addColumnDef("date", new String[]{"date"}, SqlType.EPOCHTIME_SECONDS);
            addColumnDef("duration", new String[]{"duration"}, SqlType.DURATION);

            TableDef tbl = addTableDef("user", new String[]{"user"});
            tbl.addColumnDef("user.name", new String[]{"name"}, SqlType.STRING);
            enableAllText(new String[]{"artist", "albums"});

        }

        static Pair<String, List<String>> do_transform(Token mod) throws DslException {
            TestQueryTransform xform = new TestQueryTransform();
            return xform.transform(mod);
        }
    }

    Pair<String, List<String>> parse_and_transform(String str) throws DslException {
        QDateTime dt = DateUtil.now();
        try {
            dt = QDateTime.fromString("2020/01/01");
        } catch(EvalException e) {
            System.out.println(e.toString());
        }

        QueryParser parser = new QueryParser();
        parser.setCurrentDateTime(dt);

        Token mod = parser.parse(str);

        TestQueryTransform xform = new TestQueryTransform();
        xform.setCurrentDateTime(dt);
        Pair<String, List<String>> pair = xform.transform(mod);
        return pair;
    }

    @Test
    public void test_all_text_1() throws DslException {

        Pair<String, List<String>> pair = parse_and_transform("stp");

        Assert.assertEquals("((lower(artist) LIKE lower(?) OR lower(albums) LIKE lower(?)))", pair.first);
        Assert.assertEquals(2, pair.second.size());
        Assert.assertEquals("%stp%", pair.second.get(0));
        Assert.assertEquals("%stp%", pair.second.get(1));
    }

    @Test
    public void test_number_1() throws DslException {

        Pair<String, List<String>> pair = parse_and_transform("rte < 123");

        Assert.assertEquals("(rating < ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("123", pair.second.get(0));
    }

    @Test
    public void test_number_2() throws DslException {

        Pair<String, List<String>> pair = parse_and_transform("rte < 0xF");

        Assert.assertEquals("(rating < ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("15", pair.second.get(0));
    }

    @Test
    public void test_number_3() throws DslException {

        Pair<String, List<String>> pair = parse_and_transform("rte < - 7/2");

        Assert.assertEquals("(rating < ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("-3", pair.second.get(0));
    }

    @Test
    public void test_number_4() throws DslException {

        Pair<String, List<String>> pair = parse_and_transform("rte < - 7.0/2");

        Assert.assertEquals("(rating < ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("-3", pair.second.get(0));
    }

    @Test
    public void test_str_1() throws DslException {

        Pair<String, List<String>> pair = parse_and_transform("name == 'a' + \"b\"");

        Assert.assertEquals("(name = ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("ab", pair.second.get(0));
    }

    @Test
    public void test_duration_1() throws DslException {

        Pair<String, List<String>> pair = parse_and_transform("duration < 3:00");

        Assert.assertEquals("(duration < ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("180", pair.second.get(0));
    }

    @Test
    public void test_table_def() throws DslException {

        Pair<String, List<String>> pair = parse_and_transform("user.name == 'bob'");

        Assert.assertEquals("(user.name = ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("bob", pair.second.get(0));
    }

    @Test
    public void test_keyword() throws DslException {

        QueryParser parser = new QueryParser();
        Token mod_1 = parser.parse("\"ABC\" or \"DEF\"");
        Token mod_2 = parser.parse("\"ABC\" || \"DEF\"");
        //System.out.println(mod.toDebugString());

        Pair<String, List<String>> pair_1 = TestQueryTransform.do_transform(mod_1);
        Pair<String, List<String>> pair_2 = TestQueryTransform.do_transform(mod_2);

        Assert.assertEquals("((lower(artist) LIKE lower(?) OR lower(albums) LIKE lower(?)) OR (lower(artist) LIKE lower(?) OR lower(albums) LIKE lower(?)))", pair_1.first);
        Assert.assertEquals(pair_2.first, pair_1.first);
        Assert.assertEquals(4, pair_1.second.size());
        Assert.assertEquals("%ABC%", pair_1.second.get(0));
        Assert.assertEquals("%ABC%", pair_1.second.get(1));
        Assert.assertEquals("%DEF%", pair_1.second.get(2));
        Assert.assertEquals("%DEF%", pair_1.second.get(3));
    }

    @Test
    public void test_reference() throws DslException {

        Pair<String, List<String>> pair = parse_and_transform("rte = &rte");
        Assert.assertEquals("(rating = rating)", pair.first);

    }

    @Test
    public void test_epochtime_1() throws DslException{

        Pair<String, List<String>> pair = parse_and_transform("date > -5d");
        Assert.assertEquals("(date > ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("1577422800", pair.second.get(0));
    }
    @Test
    public void test_epochtime_2() throws DslException{

        Pair<String, List<String>> pair = parse_and_transform("date > \"2020/01/01\"");
        Assert.assertEquals("(date > ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("1577854800", pair.second.get(0));

    }

    @Test
    public void test_epochtime_3() throws DslException{

        Pair<String, List<String>> pair = parse_and_transform("date > \"2020/01/01\" + 1:2:3");
        Assert.assertEquals("(date > ?)", pair.first);
        Assert.assertEquals(1, pair.second.size());
        Assert.assertEquals("1577858523", pair.second.get(0));

    }

    @Test
    public void test_epochtime_4() throws DslException {

        Pair<String, List<String>> pair = parse_and_transform("(name = \"ONE\" || name = \"TWO\") or date lt -5d");
        Assert.assertEquals("((lower(name) LIKE lower(?) OR lower(name) LIKE lower(?)) OR date < ?)", pair.first);
        Assert.assertEquals(3, pair.second.size());
        Assert.assertEquals("%ONE%", pair.second.get(0));
        Assert.assertEquals("%TWO%", pair.second.get(1));
        Assert.assertEquals("1577422800", pair.second.get(2));

    }
}
