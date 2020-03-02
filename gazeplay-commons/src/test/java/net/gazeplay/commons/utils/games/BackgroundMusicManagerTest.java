package net.gazeplay.commons.utils.games;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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
        final double actualVolume = ActiveConfigurationContext.getInstance().getMusicVolumeProperty().getValue();
        final double currentVolume = mediaPlayer.getVolume();
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
}
