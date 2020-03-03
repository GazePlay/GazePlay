package net.gazeplay.commons.soundsmanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class SoundsManagerFactory {

    private final AtomicReference<SoundsManager> currentInstanceReference = new AtomicReference<>();


    public SoundsManagerFactory() {
    }

    public synchronized SoundsManager get() {

        SoundsManager soundsManager = currentInstanceReference.get();
        if (soundsManager != null) {
            soundsManager.clear();
            soundsManager.destroy();
        }

        soundsManager = create();
        currentInstanceReference.set(soundsManager);
        return soundsManager;
    }

    private SoundsManager create() {

        final SoundsManager soundsManager = new SoundsManager();

        soundsManager.init();
        return soundsManager;
    }

}
