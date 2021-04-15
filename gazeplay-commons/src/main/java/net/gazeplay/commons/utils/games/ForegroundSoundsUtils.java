package net.gazeplay.commons.utils.games;

import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class ForegroundSoundsUtils {

    private static MediaPlayer lastSoundPlayer;
    private static Process lastFfplayProcess;

    public static synchronized void playSound(String resource) throws IOException {
        log.debug("Try to play " + resource);
        URL url = ClassLoader.getSystemResource(resource);
        String path;

        if (url == null) {
            final File file = new File(resource);
            log.debug("using file");
            if (!file.exists()) {
                log.warn("file doesn't exist : {}", resource);
                return;
            }
            path = file.toURI().toString();
        } else {
            log.debug("using url");
            path = url.toString();
        }

        final Configuration configuration = ActiveConfigurationContext.getInstance();
        Media media = new Media(path);

        try {
            MediaPlayer soundPlayer = new MediaPlayer(media);
            soundPlayer.setVolume(configuration.getEffectsVolumeProperty().getValue());
            soundPlayer.volumeProperty().bindBidirectional(configuration.getEffectsVolumeProperty());
            soundPlayer.play();
            lastSoundPlayer = soundPlayer;
        } catch (MediaException me) {
            log.info("MediaException: MediaPlayer can't be created, trying to use ffplay instead");
            lastFfplayProcess = new ProcessBuilder("ffplay",
                "-nodisp",
                "-autoexit",
                media.getSource()).start();
        }
    }

    public static synchronized void stopSound() {
        MediaPlayer activeSoundPlayer = lastSoundPlayer;
        if (activeSoundPlayer != null) {
            activeSoundPlayer.stop();
        }

        Process activeFfplayProcess = lastFfplayProcess;
        if (activeFfplayProcess != null) {
            activeFfplayProcess.destroy();
        }

    }

}
