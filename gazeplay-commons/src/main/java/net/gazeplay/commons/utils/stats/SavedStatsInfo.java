package net.gazeplay.commons.utils.stats;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.util.Observable;

@Data
@EqualsAndHashCode(callSuper = false)
public class SavedStatsInfo extends Observable {
    private final File gazeMetricsFileMouse;
    private final File gazeMetricsFileGaze;
    private final File gazeMetricsFileMouseAndGaze;
    private final File screenshotFile;
    private final File colorBandsFile;
    private final File replayDataFile;

    public void notifyFilesReady() {
        this.notifyObservers();
    }
}
