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
     * @return current time in the format HH:mm:ss
     */
    public static String timeNow() {
        return time(new Date());
    }

    /**
     * The pattern used to be "HH:MM:ss", which printed the month instead of the minutes (e.g. 14:10:05
     * for 14:37:05 in October). Files written with the old pattern keep wrong minutes in their "Time" column; the
     * minutes can be recovered from the yyyy-MM-dd-HH-mm-ss prefix of their name (...-info-game.csv).
     *
     * @return the given time in the format HH:mm:ss
     */
    static String time(final Date date) {
        final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
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
