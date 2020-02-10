package net.gazeplay.commons.utils.games;

import javafx.scene.media.MediaPlayer;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class BackgroundMusicManagerTest {

    private final BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
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

    @BeforeEach
    void setup() {
        final String uri = new File(localDataFolder + "song.mp3").toURI().toString();
        musicManager.getAudioFromFolder(localDataFolder);
        mediaPlayer = musicManager.createMediaPlayer(uri);
        previousVolume = musicManager.getCurrentMusic().getVolume();
    }

    @AfterEach
    void teardown() {
        if (musicManager.getCurrentMusic() != null) {
            musicManager.getCurrentMusic().setVolume(previousVolume);
        }
    }

    @Test
    void shouldCreateMediaPlayer() {
        assert mediaPlayer != null;
    }

    @Test
    void shouldReturnNullOnError() {
        final String uri = new File(localDataFolder + "test.properties").toURI().toString();
        mediaPlayer = musicManager.createMediaPlayer(uri);
        assert mediaPlayer == null;
    }

    @Test
    void shouldHaveTheSameVolumeAsVolumeProperty() {
        final double actualVolume = ActiveConfigurationContext.getInstance().getMusicVolumeProperty().getValue();
        final double currentVolume = mediaPlayer.getVolume();
        assert currentVolume == actualVolume;
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 0.1, 0.5, 1})
    void shouldSetTheVolume(final double volume) {
        musicManager.setVolume(volume);
        assert musicManager.getCurrentMusic().getVolume() == volume;
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 100, 1.1})
    void shouldNotSetTheVolume(final double volume) {
        assertThrows(IllegalArgumentException.class, () -> musicManager.setVolume(volume));
    }

    @Test
    void shouldBackupAndRestorePlaylist() {
        final int numberOfTracks = musicManager.getPlaylist().size();
        musicManager.backupPlaylist();
        musicManager.emptyPlaylist();
        musicManager.restorePlaylist();
        assertEquals(numberOfTracks, musicManager.getPlaylist().size());
    }

    @Test
    void shouldNotBackupOrRestoreEmptyPlaylist() {
        final int numberOfTracks = musicManager.getPlaylist().size();
        musicManager.emptyPlaylist();

        musicManager.backupPlaylist();
        musicManager.restorePlaylist();

        assertNotEquals(numberOfTracks, musicManager.getPlaylist().size());
    }

    @Test
    void shouldOnlyBackupThePlaylistOnce() {
        final int numberOfTracks = musicManager.getPlaylist().size();
        musicManager.backupPlaylist();
        musicManager.backupPlaylist();

        musicManager.emptyPlaylist();

        musicManager.restorePlaylist();
        assertEquals(numberOfTracks, musicManager.getPlaylist().size());
    }

    @Test
    void shouldOnlyRestoreThePlaylistOnce() {
        final int numberOfTracks = musicManager.getPlaylist().size();
        musicManager.backupPlaylist();

        musicManager.emptyPlaylist();

        musicManager.restorePlaylist();
        musicManager.restorePlaylist();
        assertEquals(numberOfTracks, musicManager.getPlaylist().size());
    }
}
