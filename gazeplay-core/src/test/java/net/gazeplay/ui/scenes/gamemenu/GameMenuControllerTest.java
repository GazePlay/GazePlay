package net.gazeplay.ui.scenes.gamemenu;

import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GazePlayLauncher;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.ui.MusicControl;
import net.gazeplay.ui.scenes.ingame.GameContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class GameMenuControllerTest {

    @InjectMocks
    private GameMenuController gameMenuController;

    @Mock
    private GameContext mockGameContext;

    @Mock
    private GameSpec mockGameSpec;

    @Mock
    private GameSummary mockGameSummary;

    @Mock
    private MusicControl mockMusicControl;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BackgroundMusicManager mockMusicManager;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldSetBackgroundMusic() {
        when(mockMusicManager.getCurrentMusic().getMedia().getSource()).thenReturn("songidea(copycat)_0.mp3");
        when(mockMusicManager.getPlaylist().isEmpty()).thenReturn(false);

        when(mockGameSpec.getGameSummary()).thenReturn(mockGameSummary);
        when(mockGameSummary.getBackgroundMusicUrl()).thenReturn("https://opengameart.org/sites/default/files/DST-TowerDefenseTheme_1.mp3");
        when(mockGameContext.getMusicControl()).thenReturn(mockMusicControl);

        gameMenuController.playBackgroundMusic(mockGameContext, mockGameSpec, mockMusicManager);

        verify(mockMusicManager).backupPlaylist();
    }

    @Test
    void shouldNotSetBackgroundMusic() {
        when(mockMusicManager.getCurrentMusic().getMedia().getSource()).thenReturn("another-song.mp3");
        when(mockMusicManager.getPlaylist().isEmpty()).thenReturn(false);

        gameMenuController.playBackgroundMusic(mockGameContext, mockGameSpec, mockMusicManager);

        assertTrue(BackgroundMusicManager.getInstance().getBackupPlaylist().isEmpty());
    }

    /**
     * The JVM of a game gets the same JVM options as GazePlay (it used to get none, hence the warnings of Java 25).
     */
    @Test
    void shouldGiveTheJvmOptionsToTheGameProcess() {
        List<String> command = gameMenuController.createBuilder("WhereIsTheShape", null, 0, 0).command();

        // The tests load JavaFX from the classpath: no JavaFX module to name
        List<String> options = List.of("--enable-native-access=ALL-UNNAMED", "--sun-misc-unsafe-memory-access=allow");
        assertEquals(options, GazePlayLauncher.jvmOptions());
        assertTrue(command.containsAll(options), command.toString());
        int launcher = command.indexOf(GazePlayLauncher.class.getName());
        options.forEach(option -> assertTrue(command.indexOf(option) < launcher, option));
        assertEquals(List.of("--game", "WhereIsTheShape"), command.subList(command.size() - 2, command.size()));
    }

}
