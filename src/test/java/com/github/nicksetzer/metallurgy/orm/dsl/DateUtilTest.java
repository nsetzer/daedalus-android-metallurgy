package com.github.nicksetzer.metallurgy.orm.dsl;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class DateUtilTest {

    @Test
    public void test_daysInMonth() {

        Assert.assertEquals(DateUtil.daysInMonth(2020, 1), 31);

        Assert.assertEquals(DateUtil.daysInMonth(2020, 2), 29);
        Assert.assertEquals(DateUtil.daysInMonth(2021, 2), 28);

        Assert.assertEquals(DateUtil.daysInMonth(2020, 12), 31);
    }

    @Test
    public void test_date_delta() {

        QDateTime dt = new QDateTime(2020, 1, 1, 12, 34,00);
        Assert.assertEquals(dt.toString(), "2020/01/01T12:34:00.000");

        DateUtil.add_date_delta(dt, 1, 13, 35);
        Assert.assertEquals(dt.toString(), "2022/03/08T12:34:00.000");

    }

    @Test
    public void test_time_delta() {

        QDateTime dt = new QDateTime(2020, 1, 1, 0, 0,0, 0);
        Assert.assertEquals(dt.toString(), "2020/01/01T0:00:00.000");

        DateUtil.add_time_delta(dt, 1, 90, 15, 123);
        Assert.assertEquals(dt.toString(), "2020/01/01T2:30:15.123");

    }

    @Test
    public void test_fromString() throws DslException {

        QDateTime dt = QDateTime.fromString("2020/01/01T12:34:56.789");
        Assert.assertEquals(dt.toString(), "2020/01/01T12:34:56.789");

        dt = QDateTime.fromString("20/12/15");
        Assert.assertEquals(dt.toString(), "2020/12/15T0:00:00.000");

        dt = QDateTime.fromString("67/11/18");
        Assert.assertEquals(dt.toString(), "1967/11/18T0:00:00.000");


    }

    @Test
    public void test_add() throws DslException {

        QDateTime dt = QDateTime.fromString("2020/01/01T12:34:56.789");
        dt.add(new QTimeDelta(QTimeDelta.Mode.HOURS, 4));


    }

    @Test
    public void test_timedelta_fromString() throws DslException {

        QTimeDelta td = QTimeDelta.fromString("12:34:56.123");
        Assert.assertEquals(td.toString(), "12:34:56.123");

        td = QTimeDelta.fromString("34:56");
        Assert.assertEquals(td.toString(), "0:34:56");

        td = QTimeDelta.fromString("1y1w");
        Assert.assertEquals(td.toString(), "1y7d");

    }
}
