package net.gazeplay.commons.utils.games;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;

import java.io.File;
import java.net.URL;

@Slf4j
public class ForegroundSoundsUtils {

    private static MediaPlayer lastSoundPlayer;

    public static synchronized void playSound(String resource) {
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
        stopSound();
        final Configuration configuration = ActiveConfigurationContext.getInstance();
        Media media = new Media(path);
        MediaPlayer soundPlayer = new MediaPlayer(media);
        soundPlayer.setVolume(configuration.getEffectsVolumeProperty().getValue());
        soundPlayer.volumeProperty().bindBidirectional(configuration.getEffectsVolumeProperty());
        soundPlayer.play();
        lastSoundPlayer = soundPlayer;
    }

    public static synchronized void stopSound() {
        MediaPlayer activeSoundPlayer = lastSoundPlayer;
        if (activeSoundPlayer != null) {
            activeSoundPlayer.stop();
        }
    }

}
