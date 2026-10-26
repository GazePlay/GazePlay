package net.gazeplay.commons.utils.games;

import javafx.application.Platform;
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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The background music: a playlist of the music files of a folder (MUSIC_DIR setting, by default the "music" folder of
 * the GazePlay folder), played one after the other at the music volume of the settings. A game can also play its own
 * music "alone" ({@link #playMusicAlone(String)}); the default playlist comes back at the end of the game, unless the
 * user chose a music folder.
 * <p>
 * One shared instance ({@link #getInstance()}), replaced when the configuration changes (another profile).
 */
@Slf4j
public class BackgroundMusicManager {

    private static final List<String> SUPPORTED_FILE_EXTENSIONS = Arrays.asList(".aif", ".aiff", ".fxm", ".flv", ".m3u8",
        ".mp3", ".mp4", ".m4v", ".m4a", ".mp4", ".wav");

    @Getter
    private static BackgroundMusicManager instance = new BackgroundMusicManager();

    /**
     * Stops the music and replaces the shared instance (called when another profile is selected).
     */
    public static void onConfigurationChanged() {
        instance.stop();
        instance = new BackgroundMusicManager();
    }

    /**
     * Replaces the shared instance, e.g. by a mock in tests. The caller is responsible for restoring the previous one.
     */
    public static void setInstance(final BackgroundMusicManager backgroundMusicManager) {
        instance = backgroundMusicManager;
    }

    @Getter
    private final List<MediaPlayer> playlist = new ArrayList<>();

    private final List<MediaPlayer> defaultPlayList = new ArrayList<>();

    @Getter
    private final List<MediaPlayer> backupPlaylist = new ArrayList<>();

    @Getter
    private MediaPlayer currentMusic;

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

            if (currentMusic != null) {
                if (isPlayingProperty.getValue()) {
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

    /**
     * At the end of a game, puts the default playlist back in place of the music of the game, unless the user chose a
     * music folder.
     */
    public void onEndGame() {

        if (!isCustomMusicSet.getValue()) {
            log.info("replaying default music");
            emptyPlaylist();
            playlist.addAll(defaultPlayList);
            changeCurrentMusic();
        }
    }

    /**
     * Adds the music files of a folder, and of its subfolders, to the playlist. A missing or blank folder is logged and
     * ignored.
     */
    public void getAudioFromFolder(String folderPath) {

        log.info("audio folder : {}", folderPath);
        // An empty value (e.g. an old MUSIC_FOLDER= setting) is no folder: since Java 25, new File("") is the current
        // folder, which would have been searched recursively for music
        if (folderPath == null || folderPath.isBlank()) {
            log.warn("no audio folder");
            return;
        }
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
        if (currentMusic == null) {
            changeCurrentMusic();
        }

        if (!folderPath.equals(Configuration.DEFAULT_VALUE_MUSIC_DIR)) {
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
                playlist.add(createMediaPlayer(file.toURI().toString()));
            }
        }
        File[] subDirectories = folder.listFiles(directoryFilter);
        if (subDirectories != null) {
            for (File file : subDirectories) {
                addFolderRecursively(file);
            }
        }
    }

    /**
     * Makes the music at the current index of the playlist the current one, still playing if the previous one was.
     */
    public void changeCurrentMusic() {
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

    /**
     * @return whether the music is playing (not paused)
     */
    public boolean isPlaying() {

        return this.isPlayingProperty.getValue();
    }

    /**
     * Change the current selected music. If invalid index then nothing will be done. If everything is correct, then it
     * will play the newly selected music.
     *
     * @param newMusicIndex The new index to use. Must be at least 0 and less than the size of the playlist, otherwise nothing will be done.
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

    /**
     * Stops the music and empties the playlist.
     */
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

    /**
     * Pauses the current music.
     */
    public void pause() {
        this.isPlayingProperty.setValue(false);
    }

    /**
     * Play the current selected music in the playlist. If it was paused then it will start from when it was.
     */
    public void play() {

        if (currentMusic != null) {
            this.isPlayingProperty.setValue(true);
        }
    }

    /**
     * Stops the current music (it will start again from the beginning).
     */
    public void stop() {
        if (currentMusic != null) {
            if (isPlaying()) {
                pause();
            }
            currentMusic.stop();
        }
    }

    /**
     * Goes to the next music of the playlist, after the last one the first.
     */
    public void next() {
        if (playlist.isEmpty()) {
            return;
        }
        int currentMusicIndex = (musicIndexProperty.getValue() + 1) % playlist.size();
        changeMusic(currentMusicIndex);
    }

    /**
     * Goes to the previous music of the playlist, before the first one the last.
     */
    public void previous() {
        if (playlist.isEmpty()) {
            return;
        }
        int currentMusicIndex = (musicIndexProperty.getValue() + playlist.size() - 1) % playlist.size();
        changeMusic(currentMusicIndex);
    }

    /**
     * Sets the volume of the current music, from 0 to 1.
     */
    public void setVolume(double value) {
        if ((value < 0) || (value > 1)) {
            throw new IllegalArgumentException("volume must be between 0 and 1");
        }
        currentMusic.setVolume(value);
    }

    /**
     * Play a music without adding it to the playlist.
     *
     * @param resourceUrlAsString The resource to the music
     */
    public void playMusicAlone(String resourceUrlAsString) {
        // If there is already the music in playlist, just play it (played from the cache, or from its URL before the
        // cache was used for the playback)
        MediaPlayer mediaPlayerInPlaylist = getMediaPlayerFromSource(resourceUrlAsString);
        if (mediaPlayerInPlaylist == null) {
            mediaPlayerInPlaylist = getMediaPlayerFromSource(cacheFileOf(resourceUrlAsString).toURI().toString());
        }
        if (mediaPlayerInPlaylist != null) {
            playAlone(mediaPlayerInPlaylist);
            return;
        }

        // Only the download runs in the background: the playlist and the properties shown by the music controls
        // change on the JavaFX thread
        Runnable asyncTask = () -> {

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

            // The copy in the cache is played: the music used to be downloaded, then played from its URL again
            MediaPlayer localMediaPlayer = null;
            try {
                localMediaPlayer = createMediaPlayer(localResourceName);
            } catch (RuntimeException e) {
                log.error("Exception while playing media file {} ", localResourceName, e);
            }

            if (localMediaPlayer != null) {
                final MediaPlayer createdMediaPlayer = localMediaPlayer;
                Platform.runLater(() -> playAlone(createdMediaPlayer));
            }
        };

        executorService.execute(asyncTask);
    }

    private void playAlone(final MediaPlayer localMediaPlayer) {
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

    /**
     * The file of a music in the cache: its URL encoded in URL-safe Base64 (one file per URL, no "/" in the name), with
     * the extension of the URL, from which JavaFX recognises the format. The former names (standard Base64, without
     * extension) are no longer used: those musics are downloaded again once.
     *
     * @param resourceUrlExternalForm the URL of the music
     * @return the file of the music in the cache/music folder of the GazePlay folder
     */
    static File cacheFileOf(final String resourceUrlExternalForm) {
        final String encodedUrl = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(resourceUrlExternalForm.getBytes(StandardCharsets.UTF_8));
        final String extension = extensionOf(resourceUrlExternalForm);
        final String name = extension.isEmpty() ? encodedUrl : encodedUrl + "." + extension;
        return new File(new File(GazePlayDirectories.getGazePlayFolder(), "cache/music"), name);
    }

    /**
     * @return the extension of the file of a URL ("mp3"), also inside a jar ("jar:file:...!/a.mp3"); empty when it has
     *   none, or not a plain one
     */
    static String extensionOf(final String url) {
        String path;
        try {
            final URI uri = URI.create(url);
            path = uri.isOpaque() ? uri.getSchemeSpecificPart() : uri.getPath();
        } catch (IllegalArgumentException e) {
            path = url;
        }
        if (path == null) {
            return "";
        }
        final String file = path.substring(path.lastIndexOf('/') + 1);
        final int dot = file.lastIndexOf('.');
        final String extension = dot < 0 ? "" : file.substring(dot + 1);
        return extension.matches("[A-Za-z0-9]{1,5}") ? extension : "";
    }

    /**
     * Copies a music file into the cache of the GazePlay folder (see {@link #cacheFileOf(String)}), unless it is already
     * there.
     *
     * @return the file in the cache
     */
    File downloadAndGetFromCache(URL resourceURL, String resourceUrlExternalForm) {
        File outputFile = cacheFileOf(resourceUrlExternalForm);

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

    /**
     * @return a player of a music, following the music volume of the settings and going to the next music at its end;
     *   null when the media cannot be loaded
     */
    MediaPlayer createMediaPlayer(String source) {
        try {
            final Media media = new Media(source);
            final MediaPlayer player = makeMediaPlayer(media);
            player.setOnError(() -> log.error("error on audio media loading : " + player.getError()));
            player.volumeProperty().bindBidirectional(ActiveConfigurationContext.getInstance().getMusicVolumeProperty());
            player.setOnEndOfMedia(this::next);

            return player;
        } catch (MediaException e) {
            log.error("error while loading media {}, type : {}", source, e.getType(), e);
        }
        return null;
    }

    /**
     * Look through playlist and search for a corresponding mediaplayer
     *
     * @param source The source to look for.
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

    /**
     * @return the title of a music: from its metadata, else the name of its file; "None" without music
     */
    public static String getMusicTitle(final MediaPlayer music) {
        if (music == null) {
            return "None";
        }

        ObservableMap<String, Object> metaData = music.getMedia().getMetadata();
        String title = null;
        try {
            title = (String) metaData.get("title");
        } catch (Throwable e) {
            log.warn("Failed to get title from metadata", e);
        }

        if (title == null) {
            title = getMusicTitle(music.getMedia().getSource());
        }
        return title;
    }

    /**
     * @return the name of a music file, without its folder and extension
     */
    public static String getMusicTitle(final String musicPath) {

        String title;
        String decodedUri = URLDecoder.decode(musicPath, StandardCharsets.UTF_8);
        title = FilenameUtils.getBaseName(decodedUri);

        return title;
    }

    /**
     * @return a property set to true while the current music changes, then to false (followed by the music controls)
     */
    public ReadOnlyBooleanProperty getIsMusicChanging() {
        return isMusicChanging.getReadOnlyProperty();
    }

    /**
     * Creates the "music" folder of the GazePlay folder if needed, and puts the default music in it when it is empty.
     */
    public void checkDefaultMusic(){
        File defaultPath = GazePlayDirectories.getGazePlayFolder();
        if (new File(defaultPath + "/music/").mkdirs()){
            log.info("Folder music created !");
            this.addDefaultSong(defaultPath);
        }else {
            log.info("Folder music already created !");
            String[] files = new File(defaultPath + "/music/").list();
            assert files != null;
            if (files.length == 0){
                this.addDefaultSong(defaultPath);
            }
        }
    }

    /**
     * Copies the default music of GazePlay into the "music" folder of a GazePlay folder.
     */
    public void addDefaultSong(File path){

        File gazePlayMusicFolder = new File(path, "music");
        String resourcePath = "data/home/sounds/" + Configuration.DEFAULT_VALUE_BACKGROUND_MUSIC;

        try {
            InputStream defaultMusicTrack = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            if (defaultMusicTrack != null){
                Files.copy(defaultMusicTrack,
                    Paths.get(new File(gazePlayMusicFolder, Configuration.DEFAULT_VALUE_BACKGROUND_MUSIC).getAbsolutePath()),
                    StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ie) {
            log.debug(String.format("Could not copy file at %s to %s: %s", resourcePath, gazePlayMusicFolder, ie.toString()));
        }
    }
}
