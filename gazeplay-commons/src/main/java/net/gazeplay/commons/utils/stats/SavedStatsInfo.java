package net.gazeplay.commons.utils.stats;

import lombok.Data;

import java.io.File;
import java.util.Observable;

@Data
public class SavedStatsInfo extends Observable {
    private final File heatMapPngFile;
    private final File heatMapCsvFile;
    private final File screenshotFile;
    private final File fixationPointsPngFile;

    public void notifyFilesReady() {
        this.notifyObservers();
    }

}
