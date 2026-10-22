package net.gazeplay.commons.utils.stats;

import lombok.Data;

import java.io.File;

/**
 * Files of the results of a round. It used to extend java.util.Observable (deprecated) to notify the statistics screen
 * when the files were written, but it never notified anything (no setChanged()), and the files are written before the
 * statistics screen is shown.
 */
@Data
public class SavedStatsInfo {
    private final File heatMapCsvFile;
    private final File gazeMetricsFileMouse;
    private final File gazeMetricsFileGaze;
    private final File gazeMetricsFileMouseAndGaze;
    private final File screenshotFile;
    private final File colorBandsFile;
    private final File replayDataFile;
}
