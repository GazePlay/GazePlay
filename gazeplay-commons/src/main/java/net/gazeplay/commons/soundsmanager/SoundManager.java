package net.gazeplay.commons.soundsmanager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Plays the sounds requested by a game one after the other: a background thread asks every 10 ms to play the next sound
 * of the queue on the JavaFX thread.
 */
@Slf4j
public class SoundManager {
    @Getter
    private ExecutorService executorService;

    @Getter
    private SoundPlayerRunnable soundPlayerRunnable;

    @Getter
    private Queue<String> listOfMusicToPlay = new LinkedList<>();

    /**
     * Starts the background thread.
     */
    public void init() {
        soundPlayerRunnable = new SoundPlayerRunnable(this);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(soundPlayerRunnable);
    }

    /**
     * Stops the background thread.
     */
    public void destroy() {
        soundPlayerRunnable.setStopRequested(true);
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * Plays the next sound of the queue, if any. On the JavaFX thread.
     */
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

    /**
     * Empties the queue.
     */
    public void clear() {
        listOfMusicToPlay.clear();
    }

    /**
     * Adds a sound to the queue.
     *
     * @param resource path of the sound (resource of the classpath or file)
     */
    public void add(String resource) {
        listOfMusicToPlay.add(resource);
    }
}
