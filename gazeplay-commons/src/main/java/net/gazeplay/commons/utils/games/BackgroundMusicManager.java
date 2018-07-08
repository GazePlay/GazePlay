package net.gazeplay.commons.utils.games;

import javafx.beans.property.*;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.threads.CustomThreadFactory;
import net.gazeplay.commons.threads.GroupingThreadFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BackgroundMusicManager {

    public static final List<String> SUPPORTED_FILE_EXTENSIONS = Arrays.asList(".aif", ".aiff", ".fxm", ".flv", ".m3u8",
            ".mp3", ".mp4", ".m4v", ".m4a", ".mp4", ".wav");

    @Setter
    @Getter
    private static BackgroundMusicManager instance = new BackgroundMusicManager();

    @Getter
    private final List<MediaPlayer> playlist = new ArrayList<MediaPlayer>();

    private final List<MediaPlayer> defaultPlayList = new ArrayList<MediaPlayer>();
    @Getter
    private MediaPlayer currentMusic;

    private final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 3, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(), new CustomThreadFactory(this.getClass().getSimpleName(),
                    new GroupingThreadFactory(this.getClass().getSimpleName())));

    private final Configuration config;

    @Getter
    private final BooleanProperty isPlayingPoperty = new SimpleBooleanProperty(this, "isPlaying", false);
    @Getter
    private final IntegerProperty musicIndexProperty = new SimpleIntegerProperty(this, "musicIndex", 0);

    @Getter
    private final BooleanProperty isCustomMusicSet = new SimpleBooleanProperty(this, "isCustomMusicSet", false);

    // If there is a change event and the new value is fales, then it means
    // that the music has been changed (see isChangingProperty from Slider)
    private final ReadOnlyBooleanWrapper isMusicChanging = new ReadOnlyBooleanWrapper(this, "musicChanged", false);

    public BackgroundMusicManager() {
        config = Configuration.getInstance();

        isPlayingPoperty.addListener((observable) -> {

            if (currentMusic != null) {
                if (isPlayingPoperty.getValue()) {
                    this.currentMusic.play();
                } else {
                    this.currentMusic.pause();
                }
            }

        });

        // If music is playing and index is changed, then change the music playing
        musicIndexProperty.addListener((observable, oldValue, newValue) -> {

            if (newValue.intValue() < 0 || newValue.intValue() >= playlist.size()) {
                musicIndexProperty.setValue(0);
                log.warn("Invalid music index set. 0 will be set instead");
            }
            changeCurrentMusic();
        });

    }

    public void onEndGame() {

        if (!isCustomMusicSet.getValue()) {
            log.info("replaying default music");
            emptyPlaylist();
            playlist.addAll(defaultPlayList);
            changeCurrentMusic();
        }
    }

    public void getAudioFromFolder(String folderPath) {

        log.info("audio folder : {}", folderPath);
        final File folder = new File(folderPath);
        if (!folder.exists()) {
            // throw new RuntimeException("invalid path for audio folder : " + folderPath);
            log.warn("invalid path for audio folder : " + folderPath);
            return;
        } else if (!folder.isDirectory()) {
            // throw new RuntimeException("path for audio folder is not a directory : " + folderPath);
            log.warn("path for audio folder is not a directory : " + folderPath);
            return;
        }

        addFolderRecursively(folder);

        // If no current music, update it
        if (currentMusic == null) {
            changeCurrentMusic();
        }

        if (!folderPath.equals(Configuration.DEFAULT_VALUE_MUSIC_FOLDER)) {
            isCustomMusicSet.setValue(true);
        } else {
            defaultPlayList.clear();
            defaultPlayList.addAll(playlist);
        }
    }

    private void addFolderRecursively(final File folder) {

        for (File file : folder.listFiles((File dir, String name) -> {
            for (String ext : SUPPORTED_FILE_EXTENSIONS) {
                if (name.endsWith(ext)) {
                    return true;
                }
            }

            return false;
        })) {

            playlist.add(createMediaPlayer(file.toURI().toString()));
        }

        for (File file : folder.listFiles()) {

            if (file.isDirectory()) {
                addFolderRecursively(file);
            }
        }
    }

    private void changeCurrentMusic() {
        if (playlist.isEmpty()) {
            return;
        }

        final boolean wasPlaying = isPlaying();

        if (currentMusic != null) {
            stop();
        }

        isMusicChanging.setValue(true);

        log.info("current index : {}", musicIndexProperty.getValue());
        final MediaPlayer nextMusic = playlist.get(musicIndexProperty.getValue());

        this.currentMusic = nextMusic;

        log.info("Changing current music : {}", getMusicTitle(nextMusic));
        isMusicChanging.setValue(false);

        if (wasPlaying) {
            play();
        }
    }

    public boolean isPlaying() {

        return this.isPlayingPoperty.getValue();
    }

    /**
     * Change the current selected music. If invalid index then nothing will be done. If everything is correct, then it
     * will play the newly selected music.
     * 
     * @param newMusicIndex
     *            The new index to use. Must be >= 0 and < playlist.size() otherwise nothing will be done.
     */
    public void changeMusic(int newMusicIndex) {

        if (newMusicIndex < 0 || newMusicIndex >= playlist.size()) {
            return;
        }

        boolean isPlaying = isPlaying();

        musicIndexProperty.setValue(newMusicIndex);

        if (isPlaying) {

            play();
        }
    }

    public void emptyPlaylist() {
        if (currentMusic != null) {
            stop();
            currentMusic = null;
        }
        isMusicChanging.setValue(true);
        playlist.clear();
        musicIndexProperty.setValue(0);
        isMusicChanging.setValue(false);
    }

    public void pause() {
        this.isPlayingPoperty.setValue(false);
    }

    /**
     * Play the current selected music in the playlist. If it was paused then it will start from when it was.
     */
    public void play() {

        if (currentMusic != null) {
            this.isPlayingPoperty.setValue(true);
        }
    }

    public void stop() {
        if (currentMusic != null) {
            if (isPlaying()) {
                pause();
            }
            currentMusic.stop();
        }
    }

    public void next() {
        if (playlist.isEmpty()) {
            return;
        }
        int currentMusicIndex = (musicIndexProperty.getValue() + 1) % playlist.size();
        changeMusic(currentMusicIndex);
    }

    public void previous() {
        if (playlist.isEmpty()) {
            return;
        }
        int currentMusicIndex = (musicIndexProperty.getValue() + playlist.size() - 1) % playlist.size();
        changeMusic(currentMusicIndex);
    }

    public void setVolume(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("volume must be between 0 and 1");
        }
        if (value > 1) {
            throw new IllegalArgumentException("volume must be between 0 and 1");
        }
        config.getMusicVolumeProperty().setValue(value);
    }

    public void playRemoteSound(String resourceUrlAsString) {

        Runnable asyncTask = () -> {

            MediaPlayer localMediaPlayer = getMediaPlayerFromSource(resourceUrlAsString);
            // If there is already the music in playlist, just play it
            if (localMediaPlayer == null) {

                // parse the URL early
                // in order to fail early if the URL is invalid
                URL resourceURL;
                try {
                    resourceURL = new URL(resourceUrlAsString);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Invalid URL provided as sound resource : " + resourceUrlAsString, e);
                }

                final String resourceUrlExternalForm = resourceURL.toExternalForm();
                final File mediaFile = downloadAndGetFromCache(resourceURL, resourceUrlExternalForm);

                final String localResourceName = mediaFile.toURI().toString();
                log.info("Playing sound {}", localResourceName);

                try {
                    localMediaPlayer = createMediaPlayer(resourceUrlAsString);
                } catch (RuntimeException e) {
                    log.error("Exception while playing media file {} ", localResourceName, e);
                }

            }

            if (localMediaPlayer != null) {
                playlist.add(localMediaPlayer);
                changeMusic(playlist.indexOf(localMediaPlayer));
                // Music hasn't changed (for exemple if previous index is the same),
                // then do the change manually
                if (currentMusic != localMediaPlayer) {
                    changeCurrentMusic();
                }
                play();
            }
        };

        executorService.execute(asyncTask);
    }

    /**
     * Play a music without adding it to the playlist.
     * 
     * @param resourceUrlAsString
     *            The resource to the music
     */
    public void playMusicAlone(String resourceUrlAsString) {
        Runnable asyncTask = () -> {

            MediaPlayer localMediaPlayer = getMediaPlayerFromSource(resourceUrlAsString);
            // If there is already the music in playlist, just play it
            if (localMediaPlayer == null) {

                // parse the URL early
                // in order to fail early if the URL is invalid
                URL resourceURL;
                try {
                    resourceURL = new URL(resourceUrlAsString);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Invalid URL provided as sound resource : " + resourceUrlAsString, e);
                }

                final String resourceUrlExternalForm = resourceURL.toExternalForm();
                final File mediaFile = downloadAndGetFromCache(resourceURL, resourceUrlExternalForm);

                final String localResourceName = mediaFile.toURI().toString();

                try {
                    localMediaPlayer = createMediaPlayer(resourceUrlAsString);
                } catch (RuntimeException e) {
                    log.error("Exception while playing media file {} ", localResourceName, e);
                }

            }

            if (localMediaPlayer != null) {

                log.info("Playing sound {}", localMediaPlayer.getMedia().getSource());
                if (isPlaying()) {
                    pause();
                }
                playlist.add(localMediaPlayer);
                changeMusic(playlist.size() - 1);
                // If Music hasn't changed (for exemple if previous index is the same),
                // then do the change manually
                if (currentMusic != localMediaPlayer) {
                    changeCurrentMusic();
                }

                play();
            }
        };

        executorService.execute(asyncTask);
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

    private MediaPlayer createMediaPlayer(String source) {

        try {
            final Media media = new Media(source);
            final MediaPlayer player = new MediaPlayer(media);
            player.setOnError(() -> {
                log.error("error on audio media loading : " + player.getError());
            });
            player.volumeProperty().bind(config.getMusicVolumeProperty());
            player.setOnEndOfMedia(() -> {
                next();
            });

            return player;
        } catch (MediaException e) {
            log.error("error while loading media {}, type : {}", source, e.getType(), e);
        }
        return null;
    }

    /**
     * Look through playlist and search for a corresponding mediaplayer
     * 
     * @param source
     *            The source to look for.
     * @return The media player found or null.
     */
    private MediaPlayer getMediaPlayerFromSource(final String source) {

        for (MediaPlayer mediaPlayer : playlist) {
            final Media media = mediaPlayer.getMedia();
            if (media.getSource().equals(source)) {
                return mediaPlayer;
            }
        }
        return null;
    }

    public static String getMusicTitle(final MediaPlayer music) {

        if (music == null) {
            return "None";
        }

        ObservableMap<String, Object> metaData = music.getMedia().getMetadata();
        String title = null;
        try {
            title = (String) metaData.get("title");
        } catch (Throwable e) {
        }

        if (title == null) {
            title = getMusicTitle(music.getMedia().getSource());
        }
        return title;
    }

    public static String getMusicTitle(final String musicPath) {

        String title = "unknown";
        try {
            String decodedUri = URLDecoder.decode(musicPath, "UTF-8");
            title = FilenameUtils.getBaseName(decodedUri);
        } catch (UnsupportedEncodingException ex) {
            log.warn("Wrong format to get music title: {}", musicPath, ex);
        }

        return title;
    }

    public ReadOnlyBooleanProperty getIsMusicChanging() {
        return isMusicChanging.getReadOnlyProperty();
    }
}
