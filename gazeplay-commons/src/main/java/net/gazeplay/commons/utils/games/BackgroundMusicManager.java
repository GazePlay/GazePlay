package net.gazeplay.commons.utils.games;

import javafx.application.Platform;
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

@Slf4j
public class BackgroundMusicManager {

    @Getter
    private static final BackgroundMusicManager instance = new BackgroundMusicManager();

    private MediaPlayer mediaPlayer;

    public void stop() {
        MediaPlayer localMediaPlayer = this.mediaPlayer;
        if (localMediaPlayer != null) {
            localMediaPlayer.stop();
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

                // the local cache filename is a Base64 encoding of the URL
                // so that we avoid name clash,
                // and so that we have the same local file for the same resource URL
                final Charset utf8 = Charset.forName("UTF-8");
                byte[] encodedUrl = Base64.getEncoder().encode(resourceURL.toExternalForm().getBytes(utf8));
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

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        final String localResourceName = outputFile.toURI().toString();
                        log.info("Playing sound {}", localResourceName);
                        try {
                            Media media = new Media(localResourceName);
                            MediaPlayer mediaPlayer = new MediaPlayer(media);
                            mediaPlayer.setCycleCount(Integer.MAX_VALUE);
                            if (BackgroundMusicManager.this.mediaPlayer != null) {
                                BackgroundMusicManager.this.mediaPlayer.stop();
                            }
                            BackgroundMusicManager.this.mediaPlayer = mediaPlayer;
                            mediaPlayer.play();
                        } catch (RuntimeException e) {
                            log.error("Exception while playing media file {}", localResourceName, e);
                        }
                    }
                });
            }
        };

        Thread thread = new Thread(asyncTask);
        thread.start();
    }

}
