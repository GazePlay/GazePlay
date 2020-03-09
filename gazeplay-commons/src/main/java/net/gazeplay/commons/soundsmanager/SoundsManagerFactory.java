package net.gazeplay.commons.soundsmanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class SoundsManagerFactory {

    private final AtomicReference<SoundManager> currentInstanceReference = new AtomicReference<>();


    public SoundsManagerFactory() {
    }

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
