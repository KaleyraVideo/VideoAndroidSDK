package com.kaleyra.video_common_ui.utils

import org.junit.Assert
import org.junit.Test

class TimeParserTest {

    @Test
    fun testLessThan10SecondsTimestampFormat() {
        val time = 9L
        Assert.assertEquals("00:09", TimerParser.parseTimestamp(time))
    }

    @Test
    fun testLessThanOneMinuteTimestampFormat() {
        val time = 20L
        Assert.assertEquals("00:20", TimerParser.parseTimestamp(time))
    }

    @Test
    fun testLessThanTenMinutesTimestampFormat() {
        val time = 540L
        Assert.assertEquals("09:00", TimerParser.parseTimestamp(time))
    }

    @Test
    fun testLessThanOneHourTimestampFormat() {
        val time = 1800L
        Assert.assertEquals("30:00", TimerParser.parseTimestamp(time))
    }

    @Test
    fun testLessThanTenHoursTimestampFormat() {
        val time = 14400L
        Assert.assertEquals("04:00:00", TimerParser.parseTimestamp(time))
    }

    @Test
    fun testMoreThanTenHoursTimestampFormat() {
        val time = 39600L
        Assert.assertEquals("11:00:00", TimerParser.parseTimestamp(time))
    }
}