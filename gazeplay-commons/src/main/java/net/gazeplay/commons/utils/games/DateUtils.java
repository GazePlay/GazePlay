package net.gazeplay.commons.utils.games;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    /**
     * @return current date in the format yyyy-MM-dd
     */
    public static String today() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * @return current date in the format dd/MM/yyyy
     */
    public static String todayCSV() {
        final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        final Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * @return current time in the format HH:MM:ss
     */
    public static String timeNow() {
        final DateFormat dateFormat = new SimpleDateFormat("HH:MM:ss");
        final Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * @return current time with respect to the format yyyy-MM-dd-HH-MM-ss
     */
    public static String dateTimeNow() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        final Date date = new Date();
        return dateFormat.format(date);
    }
}
