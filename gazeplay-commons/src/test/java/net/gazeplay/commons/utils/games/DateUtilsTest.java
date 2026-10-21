package net.gazeplay.commons.utils.games;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void shouldGetToday() {
        assertTrue(DateUtils.today().matches("[0-9]{4}[-][0-9]{2}[-][0-9]{2}"));
    }

    @Test
    void shouldGetTodayInCSVFormat() {
        assertTrue(DateUtils.todayCSV().matches("[0-9]{2}[/][0-9]{2}[/][0-9]{4}"));
    }

    @Test
    void shouldGetTimeNow() {
        assertTrue(DateUtils.timeNow().matches("[0-9]{2}[:][0-9]{2}[:][0-9]{2}"));
    }

    /**
     * Regression test for anomaly R3: the pattern used to be "HH:MM:ss", printing the month instead of the minutes.
     */
    @Test
    void shouldPrintMinutesNotMonthInTime() {
        Date date = new GregorianCalendar(2026, Calendar.OCTOBER, 20, 14, 37, 5).getTime();

        assertEquals("14:37:05", DateUtils.time(date));
    }

    @Test
    void shouldGetDateTimeNow() {
        assertTrue(DateUtils.dateTimeNow().matches("[0-9]{4}[-][0-9]{2}[-][0-9]{2}[-][0-9]{2}[-][0-9]{2}[-][0-9]{2}"));
    }
}
