package net.gazeplay.commons.utils.games;

import javafx.beans.property.*;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.threads.CustomThreadFactory;
import net.gazeplay.commons.threads.GroupingThreadFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    private static final List<String> SUPPORTED_FILE_EXTENSIONS = Arrays.asList(".aif", ".aiff", ".fxm", ".flv", ".m3u8",
        ".mp3", ".mp4", ".m4v", ".m4a", ".wav");

    @Getter
    private static BackgroundMusicManager instance = new BackgroundMusicManager();

    public static void onConfigurationChanged() {
        instance.stop();
        instance = new BackgroundMusicManager();
    }

    @Getter
    private final List<Media> playlist = new ArrayList<>();

    private final List<Media> defaultPlayList = new ArrayList<>();

    @Getter
    private final List<Media> backupPlaylist = new ArrayList<>();

    @Getter
    private Media currentMedia;

    @Getter
    private MediaPlayer currentMediaPlayer;

    @Getter
    private Process currentProcessBuilder;

    private final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 3, TimeUnit.MINUTES,
        new LinkedBlockingQueue<>(), new CustomThreadFactory(this.getClass().getSimpleName(),
        new GroupingThreadFactory(this.getClass().getSimpleName())));

    @Getter
    private final BooleanProperty isPlayingProperty = new SimpleBooleanProperty(this, "isPlaying", false);

    @Getter
    private final IntegerProperty musicIndexProperty = new SimpleIntegerProperty(this, "musicIndex", 0);

    @Getter
    private final BooleanProperty isCustomMusicSet = new SimpleBooleanProperty(this, "isCustomMusicSet", false);

    // If there is a change event and the new value is false, then it means
    // that the music has been changed (see isChangingProperty from Slider)
    private final ReadOnlyBooleanWrapper isMusicChanging = new ReadOnlyBooleanWrapper(this, "musicChanged", false);

    public BackgroundMusicManager() {
        isPlayingProperty.addListener((observable) -> {
            if (currentMediaPlayer != null && (Utils.isWindows() || !currentMedia.getSource().endsWith(".mp3"))) {
                // currentMediaPlayer is not null
                if (isPlayingProperty.getValue()) {
                    this.currentMediaPlayer.play();
                    log.info("NOW PLAYING :" + getMusicTitle(this.currentMediaPlayer.getMedia()));
                } else {
                    pauseCurentMediaPlayer();
                    pauseCurrentProcessBuilder();
                }
            } else {
                // currentMediaPlayer is null, we use ffplay instead
                if (isPlayingProperty.getValue()) {
                    log.warn("Invalid music extension. try using ffplay player instead");
                    ffplayCurrentMedia(currentMedia);
                } else {
                    pauseCurrentProcessBuilder();
                    pauseCurentMediaPlayer();
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

    public void ffplayCurrentMedia(Media currentMedia) {
        try {
            currentProcessBuilder = new ProcessBuilder("ffplay",
                "-nodisp",
                "-autoexit",
                "-volume",
                "" + (int) (ActiveConfigurationContext.getInstance().getMusicVolumeProperty().getValue() * 100),
                currentMedia.getSource()).start();
            log.info("NOW FFPLAYING :" + getMusicTitle(currentMedia));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onEndGame() {
        stopCurrentMediaPlayer();
        stopCurrentProcessBuilder();
        // currentMedia = null;
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
            log.warn("invalid path for audio folder : " + folderPath);
            return;
        } else if (!folder.isDirectory()) {
            log.warn("path for audio folder is not a directory : " + folderPath);
            return;
        }

        addFolderRecursively(folder);

        // If no current music, update it
        if (currentMedia == null) {
            //To trigger the change event
            changeMusic(-1);
        }

        if (!folderPath.equals(Configuration.DEFAULT_VALUE_MUSIC_FOLDER)) {
            isCustomMusicSet.setValue(true);
        } else {
            defaultPlayList.clear();
            defaultPlayList.addAll(playlist);
        }
    }

    private void addFolderRecursively(final File folder) {
        FilenameFilter supportedFilesFilter = (dir, name) -> {
            for (String ext : SUPPORTED_FILE_EXTENSIONS) {
                if (name.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        };
        FileFilter directoryFilter = File::isDirectory;
        File[] matchingFiles = folder.listFiles(supportedFilesFilter);
        if (matchingFiles != null) {
            for (File file : matchingFiles) {
                playlist.add(new Media(file.toURI().toString()));
            }
        }
        File[] subDirectories = folder.listFiles(directoryFilter);
        if (subDirectories != null) {
            for (File file : subDirectories) {
                addFolderRecursively(file);
            }
        }
    }

    void changeCurrentMusic() {
        log.info("LIST SIZE IS EQUAL TO " + playlist.size());
        if (playlist.isEmpty()) {
            return;
        }

        final boolean wasPlaying = isPlaying();

        if (currentMedia != null) {
            stop();
        }

        isMusicChanging.setValue(true);

        log.info("current index : {}", musicIndexProperty.getValue());
        this.currentMedia = playlist.get(musicIndexProperty.getValue());

        this.currentMediaPlayer = createMediaPlayer(this.currentMedia);

        log.info("Changing current music : {}", getMusicTitle(this.currentMedia));

        isMusicChanging.setValue(false);

        if (wasPlaying) {
            play();
        }
    }

    public boolean isPlaying() {
        return this.isPlayingProperty.getValue();
    }

    /**
     * Change the current selected music. If invalid index then nothing will be done. If everything is correct, then it
     * will play the newly selected music.
     *
     * @param newMusicIndex The new index to use. Must be >= 0 and < playlist.size() otherwise nothing will be done.
     */
    public void changeMusic(int newMusicIndex) {
        musicIndexProperty.setValue(newMusicIndex);
    }

    public void emptyPlaylist() {
        if (currentMedia != null) {
            stop();
        }
        isMusicChanging.setValue(true);
        playlist.clear();
        musicIndexProperty.setValue(-1);
        isMusicChanging.setValue(false);
    }

    /**
     * Save the current playlist.
     */
    public void backupPlaylist() {
        if (!playlist.isEmpty()) {
            backupPlaylist.clear();
            log.info("Backing up playlist");
            backupPlaylist.addAll(playlist);
        }
    }

    /**
     * Restores the playlist from backup if it's empty.
     */
    public void restorePlaylist() {
        if (!backupPlaylist.isEmpty()) {
            log.info("Restoring playlist");
            playlist.addAll(backupPlaylist);
            backupPlaylist.clear();
        }
    }

    public void pause() {
        this.isPlayingProperty.setValue(false);
    }

    /**
     * Play the current selected music in the playlist. If it was paused then it will start from when it was.
     */
    public void play() {
        if (currentMedia != null) {
            this.isPlayingProperty.setValue(true);
        }
    }

    public void stop() {
        if (isPlaying()) {
            pause();
        }
        stopCurrentMediaPlayer();
        stopCurrentProcessBuilder();

    }

    public void pauseCurentMediaPlayer() {
        if (currentMediaPlayer != null) {
            currentMediaPlayer.pause();
        }
    }

    public void pauseCurrentProcessBuilder() {
        // Impossible for now, stop instead
        stopCurrentProcessBuilder();
    }

    public void stopCurrentMediaPlayer() {
        if (currentMediaPlayer != null) {
            currentMediaPlayer.stop();
        }
    }

    public void stopCurrentProcessBuilder() {
        if (currentProcessBuilder != null) {
            currentProcessBuilder.destroy();
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
        if ((value < 0) || (value > 1)) {
            throw new IllegalArgumentException("volume must be between 0 and 1");
        }

        if (currentMediaPlayer != null) {
            currentMediaPlayer.setVolume(value);
        }
    }

    /**
     * Play a music without adding it to the playlist.
     *
     * @param resourceUrlAsString The resource to the music
     */
    public void playMusicAlone(String resourceUrlAsString) {
        Runnable asyncTask = () -> {

            Media localMedia = getMediaFromSource(resourceUrlAsString);
            // If there is already the music in playlist, just play it
            if (localMedia == null) {

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
                    localMedia = new Media(resourceUrlAsString);
                } catch (RuntimeException e) {
                    log.error("Exception while playing media file {} ", localResourceName, e);
                }

            }

            if (localMedia != null) {
                log.info("Playing sound {}", localMedia.getSource());
                if (isPlaying()) {
                    stop();
                }
                playlist.add(localMedia);
                musicIndexProperty.set(-1);
                changeMusic(playlist.size() - 1);
                changeCurrentMusic();
                play();
            }
        };

        executorService.execute(asyncTask);
    }

    File downloadAndGetFromCache(URL resourceURL, String resourceUrlExternalForm) {
        // the local cache filename is a Base64 encoding of the URL
        // so that we avoid name clash,
        // and so that we have the same local file for the same resource URL
        final Charset utf8 = StandardCharsets.UTF_8;
        byte[] encodedUrl = Base64.getEncoder().encode(resourceUrlExternalForm.getBytes(utf8));
        final String localCacheFileName = new String(encodedUrl, utf8);

        File musicCacheFolder = new File(GazePlayDirectories.getGazePlayFolder(), "cache/music");
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
            boolean renamed = tempOutputFile.renameTo(outputFile);
            log.debug("renamed = {}", renamed);
            log.info("Finished downloading music file {}", resourceURL);
        } else {
            log.info("Found music file in cache for {}", resourceURL);
        }
        return outputFile;
    }

    MediaPlayer makeMediaPlayer(Media media) {
        return new MediaPlayer(media);
    }

    MediaPlayer createMediaPlayer(Media media) {
        try {
            final MediaPlayer player = makeMediaPlayer(media);
            player.volumeProperty().bindBidirectional(ActiveConfigurationContext.getInstance().getMusicVolumeProperty());
            player.setOnEndOfMedia(this::next);
            log.info("CREATED");
            return player;
        } catch (MediaException e) {
            log.error("error while loading media {}, type : {}", media.getSource(), e.getType());
            return null;
        }
    }

    MediaPlayer createMediaPlayer(String source) {
        try {
            final Media media = new Media(source);
            return createMediaPlayer(media);
        } catch (MediaException e) {
            log.error("error while loading media {}, type : {}", source, e.getType());
            return null;
        }
    }

    /**
     * Look through playlist and search for a corresponding mediaplayer
     *
     * @param source The source to look for.
     * @return The media player found or null.
     */
    private Media getMediaFromSource(final String source) {
        for (Media media : playlist) {
            if (media.getSource().equals(source)) {
                return media;
            }
        }
        return null;
    }

    public static String getMusicTitle(final Media music) {
        if (music == null) {
            return "None";
        }

        ObservableMap<String, Object> metaData = music.getMetadata();
        String title = null;
        try {
            title = (String) metaData.get("title");
        } catch (Throwable e) {
            log.warn("Failed to get title from metadata", e);
        }

        if (title == null) {
            title = getMusicTitle(music.getSource());
        }
        return title;
    }

    public static String getMusicTitle(final String musicPath) {

        String title;
        String decodedUri = URLDecoder.decode(musicPath, StandardCharsets.UTF_8);
        title = FilenameUtils.getBaseName(decodedUri);

        return title;
    }

    public ReadOnlyBooleanProperty getIsMusicChanging() {
        return isMusicChanging.getReadOnlyProperty();
    }
}
