package com.github.nicksetzer.metallurgy.orm;
/**
 * run-as com.github.nicksetzer.daedalus
 * cd /data/data/com.github.nicksetzer.daedalus
 * cd /storage/emulated/0/Android/data/com.github.nicksetzer.daedalus/files/app-v1.sqlite
 *
 * as of 2023 this no longer works unless the device is rooted
 *      adb pull /storage/emulated/0/Android/data/com.github.nicksetzer.daedalus/files/app-v1.sqlite .
 * instead
 *      adb root
 *      adb shell run-as com.github.nicksetzer.daedalus ls /data/data/com.github.nicksetzer.daedalus/files
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Database {

    TableSchema[] buildTestSchema() {
        TableSchema table = new TableSchema("songs");

        table.addColumn("spk", "INTEGER PRIMARY KEY AUTOINCREMENT");
        table.addColumn("artist", "VARCHAR", true);
        table.addColumn("album", "VARCHAR");
        table.addColumn("title", "VARCHAR");
        table.addColumn("playcount", "INTEGER");

        TableSchema[] schema = new TableSchema[]{table};

        return schema;
    }

    @Test
    public void test_prepareTableSchema() {

        TableSchema[] schema = buildTestSchema();

        Statement[] statements = StatementBuilder.prepareTableSchema(schema);

        for (Statement statement : statements) {
            System.out.print(statement.text);
        }

        assertEquals(4, 4);

    }

    @Test
    public void test_prepareInsert() {

        TableSchema[] schema = buildTestSchema();

        JSONObject values = new JSONObject();
        try {
            values.put("artist", "test");
            values.put("playcount", 0);
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        Statement statement = StatementBuilder.prepareInsert(schema[0], values);

        System.out.print(statement.text);

        assertEquals("INSERT INTO songs (artist, playcount) VALUES (?, 0)", statement.text);
        assertEquals(1, statement.params.size());


    }

    @Test
    public void test_prepareInsertBulk() {

        TableSchema[] schema = buildTestSchema();

        JSONObject obj = new JSONObject();
        try {
            obj.put("test", "Test");
        } catch (JSONException e) {

        }

        JSONObject values = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            values.put("artist", "test");
            values.put("album", "test");

            array.put(values);
            array.put(values);
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        Statement statement = StatementBuilder.prepareInsertBulk(schema[0], array);

        System.out.print(statement.text);

        assertEquals("INSERT INTO songs (artist, album, title, playcount) VALUES (?, ?, ?, ?), (?, ?, ?, ?)", statement.text);


    }

    @Test
    public void test_prepareUpdate() {

        TableSchema[] schema = buildTestSchema();

        JSONObject obj = new JSONObject();
        try {
            obj.put("test", "Test");
        } catch (JSONException e) {

        }

        JSONObject item = new JSONObject();
        try {
            item.put("artist", "test");
            item.put("album", "test");
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        Statement statement = StatementBuilder.prepareUpdate(schema[0], 0, item);

        System.out.print(statement.text);

        assertEquals("UPDATE songs SET (artist, album) = (?, ?) WHERE (songs.spk == 0)", statement.text);


    }

    @Test
    public void test_prepareDelete() {

        TableSchema[] schema = buildTestSchema();

        Statement statement = StatementBuilder.prepareDelete(schema[0], 1);

        System.out.print(statement.text);

        assertEquals("DELETE FROM songs WHERE (spk == 1)", statement.text);
    }

    @Test
    public void test_prepareDeleteBulk() {

        TableSchema[] schema = buildTestSchema();

        Statement statement = StatementBuilder.prepareDeleteBulk(schema[0], new long[]{1,2,3});

        System.out.print(statement.text);

        assertEquals("DELETE FROM songs WHERE spk in (1, 2, 3)", statement.text);
    }

    @Test
    public void test_prepareSelect() {

        TableSchema[] schema = buildTestSchema();

        NaturalPrimaryKey npk = new NaturalPrimaryKey();
        npk.put("artist", "abc");

        Statement statement = StatementBuilder.prepareSelect(schema[0], npk, 10, 0);

        System.out.print(statement.text);

        assertEquals("SELECT * FROM songs WHERE (artist == ?) LIMIT 10 OFFSET 0", statement.text);
    }

    @Test
    public void test_prepareSelectColumns() {

        TableSchema[] schema = buildTestSchema();

        NaturalPrimaryKey npk = new NaturalPrimaryKey();
        npk.put("artist", "abc");

        Statement statement = StatementBuilder.prepareSelect(schema[0], new String[]{"spk", "artist"}, npk, -1, -1);

        System.out.print(statement.text);

        assertEquals("SELECT spk, artist FROM songs WHERE (artist == ?)", statement.text);
    }

    @Test
    public void test_prepareExists() {

        TableSchema[] schema = buildTestSchema();

        Statement statement = StatementBuilder.prepareExists(schema[0], "artist", "test");

        System.out.print(statement.text);

        assertEquals("SELECT spk, artist FROM songs WHERE (artist == ?)", statement.text);
    }

    @Test
    public void test_prepareExistsBulk() {

        TableSchema[] schema = buildTestSchema();

        Statement statement = StatementBuilder.prepareExistsBulk(schema[0], "artist", new Object[]{"test1", "test2"});

        System.out.print(statement.text);

        assertEquals("SELECT spk, artist FROM songs WHERE artist IN (?, ?)", statement.text);
    }

    @Test
    public void test_prepareUpsertSelect() {

        TableSchema[] schema = buildTestSchema();

        NaturalPrimaryKey npk = new NaturalPrimaryKey();
        npk.set("artist", "test");

        Statement statement = StatementBuilder.prepareUpsertSelect(schema[0], npk);

        System.out.print(statement.text);

        assertEquals("SELECT * FROM songs WHERE (artist == ?) LIMIT 1", statement.text);
    }

    @Test
    public void test_prepareUpsertSelect2() {

        TableSchema[] schema = buildTestSchema();

        NaturalPrimaryKey npk = new NaturalPrimaryKey();
        npk.set("artist", "test");
        npk.set("album", "test");

        Statement statement = StatementBuilder.prepareUpsertSelect(schema[0], npk);

        System.out.print(statement.text);

        assertEquals("SELECT * FROM songs WHERE (artist == ? && album == ?) LIMIT 1", statement.text);
    }
}
