package net.gazeplay.commons.utils.games;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BackgroundMusicManager {

    @Getter
    private static final BackgroundMusicManager instance = new BackgroundMusicManager();

    private final Map<String, MediaPlayer> mediaPlayersMap = new ConcurrentHashMap<>();

    private DoubleProperty volume = new SimpleDoubleProperty(0.25);

    public DoubleProperty volumeProperty() {
        return volume;
    }

    public void setVolume(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("volume must be between 0 and 1");
        }
        if (value > 1) {
            throw new IllegalArgumentException("volume must be between 0 and 1");
        }
        this.volume.setValue(value);
    }

    public void pause(String resourceUrlAsString) {
        MediaPlayer localMediaPlayer = mediaPlayersMap.get(resourceUrlAsString);
        if (localMediaPlayer != null) {
            localMediaPlayer.pause();
        }
    }

    public void pauseAll() {
        for (Map.Entry<String, MediaPlayer> entry : mediaPlayersMap.entrySet()) {
            MediaPlayer localMediaPlayer = entry.getValue();
            if (localMediaPlayer != null) {
                localMediaPlayer.pause();
            }
        }
    }

    public void stop(String resourceUrlAsString) {
        MediaPlayer localMediaPlayer = mediaPlayersMap.get(resourceUrlAsString);
        if (localMediaPlayer != null) {
            localMediaPlayer.pause();
        }
    }

    public void stopAll() {
        for (Map.Entry<String, MediaPlayer> entry : mediaPlayersMap.entrySet()) {
            MediaPlayer localMediaPlayer = entry.getValue();
            if (localMediaPlayer != null) {
                localMediaPlayer.stop();
            }
        }
    }

    public void playRemoteSound(String resourceUrlAsString) {

        Runnable asyncTask = new Runnable() {
            @Override
            public void run() {
                // parse the URL early
                // in order to fail early if the URL is invalid
                URL resourceURL = null;
                try {
                    resourceURL = new URL(resourceUrlAsString);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Invalid URL provided as sound resource : " + resourceUrlAsString, e);
                }

                final String resourceUrlExternalForm = resourceURL.toExternalForm();
                final File mediaFile = downloadAndGetFromCache(resourceURL, resourceUrlExternalForm);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        final String localResourceName = mediaFile.toURI().toString();
                        log.info("Playing sound {}", localResourceName);

                        MediaPlayer localMediaPlayer = mediaPlayersMap.get(resourceUrlAsString);
                        if (localMediaPlayer != null) {
                            localMediaPlayer.play();
                        } else {
                            try {
                                Media media = new Media(localResourceName);
                                localMediaPlayer = new MediaPlayer(media);
                                localMediaPlayer.setCycleCount(Integer.MAX_VALUE);
                                localMediaPlayer.volumeProperty().bind(volume);
                                //
                                mediaPlayersMap.put(resourceUrlExternalForm, localMediaPlayer);
                                localMediaPlayer.play();
                            } catch (RuntimeException e) {
                                log.error("Exception while playing media file {}", localResourceName, e);
                            }
                        }
                    }
                });
            }
        };

        Thread thread = new Thread(asyncTask);
        thread.start();
    }

    private File downloadAndGetFromCache(URL resourceURL, String resourceUrlExternalForm) {
        // the local cache filename is a Base64 encoding of the URL
        // so that we avoid name clash,
        // and so that we have the same local file for the same resource URL
        final Charset utf8 = Charset.forName("UTF-8");
        byte[] encodedUrl = Base64.getEncoder().encode(resourceUrlExternalForm.getBytes(utf8));
        final String localCacheFileName = new String(encodedUrl, utf8);

        File musicCacheFolder = new File("cache/music");
        File outputFile = new File(musicCacheFolder, localCacheFileName);

        if (!outputFile.exists()) {
            // use a temporary file while downloading
            // to avoid using corrupted file is the download is interrupted
            File tempOutputFile = new File(outputFile.getAbsolutePath() + ".downloading");
            if (tempOutputFile.exists()) {
                boolean tempOutputFileDeleted = tempOutputFile.delete();
                log.trace("tempOutputFileDeleted = {}", tempOutputFileDeleted);
            }
            try {
                log.info("Downloading music file {}", resourceURL);

                FileUtils.copyURLToFile(resourceURL, tempOutputFile, 10000, 10000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            tempOutputFile.renameTo(outputFile);
            log.info("Finished downloading music file {}", resourceURL);
        } else {
            log.info("Found music file in cache for {}", resourceURL);
        }
        return outputFile;
    }

}
