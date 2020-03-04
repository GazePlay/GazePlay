package net.gazeplay.commons.soundsmanager;

import javafx.application.Platform;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SoundPlayerRunnable implements Runnable {

    private final SoundsManager soundsManager;

    @Setter
    private transient boolean stopRequested = false;

    public SoundPlayerRunnable(final SoundsManager soundsManager) {
        this.soundsManager = soundsManager;
    }

    @Override
    public void run() {
        while (!stopRequested) {
            try {
                poll();
            } catch (final RuntimeException e) {
                log.warn("Exception while polling a sound request", e);
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException | RuntimeException e) {
                log.warn("Exception while sleeping until next poll", e);
            }
        }
    }

    private void poll() {
        Platform.runLater(soundsManager::playRequestedSounds);
    }

}
