package net.gazeplay.commons.utils.games;

import org.junit.jupiter.api.Test;

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

    @Test
    void shouldGetDateTimeNow() {
        assertTrue(DateUtils.dateTimeNow().matches("[0-9]{4}[-][0-9]{2}[-][0-9]{2}[-][0-9]{2}[-][0-9]{2}[-][0-9]{2}"));
    }
}
