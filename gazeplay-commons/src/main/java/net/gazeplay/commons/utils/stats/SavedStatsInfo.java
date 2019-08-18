package net.gazeplay.commons.utils.stats;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.util.Observable;

@Data
@EqualsAndHashCode(callSuper=false)
public class SavedStatsInfo extends Observable {
    private final File heatMapCsvFile;
    private final File gazeMetricsFile;
    private final File screenshotFile;
    private final File colorBandsFile;

    public void notifyFilesReady() {
        this.notifyObservers();
    }

}
