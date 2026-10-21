package net.gazeplay.commons.utils.games;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class BackgroundMusicManagerTest {

    @Mock
    MediaPlayer mockMediaPlayer;

    @Mock
    Media mockMedia;

    private BackgroundMusicManager musicManagerSpy;
    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep
            + "src" + sep
            + "test" + sep
            + "resources" + sep
            + "data" + sep
            + "music" + sep;

    private MediaPlayer mediaPlayer;
    private double previousVolume;

    private SimpleDoubleProperty volumeProperty = new SimpleDoubleProperty(0.5);
    ObservableMap<String, Object> metadata = FXCollections.observableHashMap();

    @BeforeEach
    void setup() {
        initMocks();
        final String uri = new File(localDataFolder + "song.mp3").toURI().toString();

        musicManagerSpy.getAudioFromFolder(localDataFolder);
        mediaPlayer = musicManagerSpy.createMediaPlayer(uri);
        previousVolume = musicManagerSpy.getCurrentMusic().getVolume();
    }

    void initMocks() {
        MockitoAnnotations.initMocks(this);

        musicManagerSpy = spy(new BackgroundMusicManager());

        doReturn(mockMediaPlayer).when(musicManagerSpy).makeMediaPlayer(any());
        when(mockMediaPlayer.volumeProperty()).thenReturn(volumeProperty);

        metadata.put("title", "Title");
        when(mockMedia.getMetadata()).thenReturn(metadata);
        when(mockMediaPlayer.getMedia()).thenReturn(mockMedia);
    }

    @AfterEach
    void teardown() {
        if (musicManagerSpy.getCurrentMusic() != null) {
            musicManagerSpy.getCurrentMusic().setVolume(previousVolume);
        }
    }

    /**
     * Regression test: an empty folder setting (e.g. an old MUSIC_FOLDER=) is no folder. Since Java 25, new File("") is
     * the current folder, which used to be searched recursively for music.
     */
    @Test
    void shouldIgnoreAnEmptyAudioFolder() {
        int before = musicManagerSpy.getPlaylist().size();

        musicManagerSpy.getAudioFromFolder("");
        musicManagerSpy.getAudioFromFolder("   ");

        assertEquals(before, musicManagerSpy.getPlaylist().size());
    }

    @Test
    void shouldCreateMediaPlayer() {
        assertNotNull(mediaPlayer);
    }

    @Test
    void shouldReturnNullOnError() {
        final String uri = new File(localDataFolder + "test.properties").toURI().toString();
        mediaPlayer = musicManagerSpy.createMediaPlayer(uri);
        assertNull(mediaPlayer);
    }

    @Test
    void shouldHaveTheSameVolumeAsVolumeProperty() {
        final double actualVolume = ActiveConfigurationContext.getInstance().getMusicVolume();
        final double currentVolume = volumeProperty.get();
        assertEquals(currentVolume, actualVolume);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 0.1, 0.5, 1})
    void shouldSetTheVolume(final double volume) {
        musicManagerSpy.setVolume(volume);
        verify(mockMediaPlayer).setVolume(volume);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 100, 1.1})
    void shouldNotSetTheVolume(final double volume) {
        assertThrows(IllegalArgumentException.class, () -> musicManagerSpy.setVolume(volume));
    }

    @Test
    void shouldBackupAndRestorePlaylist() {
        final int numberOfTracks = musicManagerSpy.getPlaylist().size();
        musicManagerSpy.backupPlaylist();
        musicManagerSpy.emptyPlaylist();
        musicManagerSpy.restorePlaylist();
        assertEquals(numberOfTracks, musicManagerSpy.getPlaylist().size());
    }

    @Test
    void shouldNotBackupOrRestoreEmptyPlaylist() {
        final int numberOfTracks = musicManagerSpy.getPlaylist().size();
        musicManagerSpy.emptyPlaylist();

        musicManagerSpy.backupPlaylist();
        musicManagerSpy.restorePlaylist();

        assertNotEquals(numberOfTracks, musicManagerSpy.getPlaylist().size());
    }

    @Test
    void shouldOnlyBackupThePlaylistOnce() {
        final int numberOfTracks = musicManagerSpy.getPlaylist().size();
        musicManagerSpy.backupPlaylist();
        musicManagerSpy.backupPlaylist();

        musicManagerSpy.emptyPlaylist();

        musicManagerSpy.restorePlaylist();
        assertEquals(numberOfTracks, musicManagerSpy.getPlaylist().size());
    }

    @Test
    void shouldOnlyRestoreThePlaylistOnce() {
        final int numberOfTracks = musicManagerSpy.getPlaylist().size();
        musicManagerSpy.backupPlaylist();

        musicManagerSpy.emptyPlaylist();

        musicManagerSpy.restorePlaylist();
        musicManagerSpy.restorePlaylist();
        assertEquals(numberOfTracks, musicManagerSpy.getPlaylist().size());
    }

    @Test
    void shouldDownloadAndGetFromCache() throws IOException {
        // user.home is a temporary folder during tests (see build.gradle); a local URL keeps the test offline
        File cacheFolder = new File(GazePlayDirectories.getGazePlayFolder(), "cache");
        URL resource = new File(localDataFolder, "song.mp3").toURI().toURL();

        File result = musicManagerSpy.downloadAndGetFromCache(resource, resource.toExternalForm());

        File expected = new File(cacheFolder, "music/" +
            new String(Base64.getEncoder().encode(resource.toExternalForm().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        assertEquals(expected, result);
        assertTrue(expected.isFile());

        FileUtils.deleteDirectory(cacheFolder);
    }

    @Test
    void shouldFindExistingInCache() throws IOException {
        File cacheFolder = new File(GazePlayDirectories.getGazePlayFolder(), "cache");
        URL resource = new URL("https://opengameart.org/sites/default/files/TalkingCuteChiptune_0.mp3");
        File folder = new File(cacheFolder, "music");
        folder.mkdirs();

        File expected = new File(folder,
            new String(Base64.getEncoder().encode(resource.toExternalForm().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        expected.createNewFile();

        // Nothing is downloaded: the file is already in the cache
        File result = musicManagerSpy.downloadAndGetFromCache(resource, resource.toExternalForm());

        assertEquals(expected, result);
        FileUtils.deleteDirectory(cacheFolder);
    }
}
