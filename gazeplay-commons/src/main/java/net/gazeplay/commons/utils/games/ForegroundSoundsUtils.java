package net.gazeplay.commons.utils.games;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;

import java.io.File;
import java.net.URL;

/**
 * Plays the sound effects of the games, at the volume of the effects set in the settings (and following its changes).
 */
@Slf4j
public class ForegroundSoundsUtils {

    /**
     * Private lock: synchronizing on the class itself exposed the lock to any other code.
     */
    private static final Object LOCK = new Object();

    private static MediaPlayer lastSoundPlayer;

    /**
     * Plays a sound, without stopping the previous one.
     *
     * @param resource path of the sound: a resource of the classpath, or else a file
     */
    public static void playSound(String resource) {
        synchronized (LOCK) {
            playSoundLocked(resource);
        }
    }

    private static void playSoundLocked(String resource) {
        log.debug("Try to play " + resource);
        URL url = ClassLoader.getSystemResource(resource);
        String path;
        if (url == null) {
            final File file = new File(resource);
            log.debug("using file");
            if (!file.exists()) {
                log.warn("file doesn't exist : {}", resource);
            }
            path = file.toURI().toString();
        } else {
            log.debug("using url");
            path = url.toString();
        }
        final Configuration configuration = ActiveConfigurationContext.getInstance();
        Media media = new Media(path);
        MediaPlayer soundPlayer = new MediaPlayer(media);
        soundPlayer.setVolume(configuration.getEffectsVolumeProperty().getValue());
        soundPlayer.volumeProperty().bindBidirectional(configuration.getEffectsVolumeProperty());
        soundPlayer.play();
        lastSoundPlayer = soundPlayer;
    }

    /**
     * Stops the last sound played.
     */
    public static void stopSound() {
        synchronized (LOCK) {
            MediaPlayer activeSoundPlayer = lastSoundPlayer;
            if (activeSoundPlayer != null) {
                activeSoundPlayer.stop();
            }
        }
    }

}
