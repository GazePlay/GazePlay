package net.gazeplay.commons.utils.stats;

import lombok.Data;

import java.io.File;
import java.util.Observable;

@Data
public class SavedStatsInfo extends Observable {
    private final File heatMapCsvFile;
    private final File gazeMetricsFile;
    private final File screenshotFile;

    public void notifyFilesReady() {
        this.notifyObservers();
    }

}
