package net.gazeplay.commons.soundsmanager;

import javafx.application.Platform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SoundManager {
    @Getter
    private ExecutorService executorService;

    @Getter
    private SoundPlayerRunnable soundPlayerRunnable;

    @Getter
    private Queue<String> listOfMusicToPlay = new LinkedList<>();

    public void init() {
        soundPlayerRunnable = new SoundPlayerRunnable(this);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(soundPlayerRunnable);
    }

    public void destroy() {
        soundPlayerRunnable.setStopRequested(true);
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public void playRequestedSounds() {
        if (listOfMusicToPlay.size() > 0) {
            String nextToPlay = listOfMusicToPlay.poll();
            try {
                ForegroundSoundsUtils.playSound(nextToPlay);
            } catch (Exception e) {
                log.info("Sound {} can't be played", listOfMusicToPlay);
            }
        }
    }

    public void clear() {
            listOfMusicToPlay.clear();
    }

    public void add(String resource) {
            listOfMusicToPlay.add(resource);
    }

    public void stop(){
        ForegroundSoundsUtils.stopSound();
    }
}
