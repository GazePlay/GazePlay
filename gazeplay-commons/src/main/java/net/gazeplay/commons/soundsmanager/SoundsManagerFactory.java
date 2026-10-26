package net.gazeplay.commons.soundsmanager;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Creates the sound manager of a game. Only one is active at a time: getting a new one stops the previous one.
 */
@Slf4j
public class SoundsManagerFactory {

    private final AtomicReference<SoundManager> currentInstanceReference = new AtomicReference<>();


    public SoundsManagerFactory() {
    }

    /**
     * Stops the current sound manager, if any, and creates a started new one.
     */
    public synchronized SoundManager get() {

        SoundManager soundManager = currentInstanceReference.get();
        if (soundManager != null) {
            soundManager.clear();
            soundManager.destroy();
        }

        soundManager = create();
        currentInstanceReference.set(soundManager);
        return soundManager;
    }

    private SoundManager create() {

        final SoundManager soundManager = new SoundManager();

        soundManager.init();
        return soundManager;
    }

}
