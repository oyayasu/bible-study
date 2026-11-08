package com.oyayasu.biblestudy.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderTimesTest {
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test fun morningSameDayWhenBeforeSix() {
        val now = ZonedDateTime.of(2026, 11, 8, 5, 50, 0, 0, zone)
        val next = ReminderTimes.next(now, 6, 0)
        assertEquals(6, next.hour)
        assertEquals(8, next.dayOfMonth)
    }

    @Test fun eveningMovesToFollowingDayAfterTime() {
        val now = ZonedDateTime.of(2026, 11, 8, 20, 31, 0, 0, zone)
        val next = ReminderTimes.next(now, 20, 30)
        assertEquals(9, next.dayOfMonth)
        assertEquals(20, next.hour)
        assertEquals(30, next.minute)
    }

    @Test fun exactBoundarySchedulesTomorrow() {
        val now = ZonedDateTime.of(2026, 11, 8, 6, 0, 0, 0, zone)
        assertTrue(ReminderTimes.next(now, 6, 0).isAfter(now))
        assertEquals(9, ReminderTimes.next(now, 6, 0).dayOfMonth)
    }
}
