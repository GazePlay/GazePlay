package net.gazeplay.ui.scenes.ingame;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.soundsmanager.SoundManager;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.Bravo;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * This test class demonstrates how to use JMockit and Mockito within the same tests.
 */
@ExtendWith(ApplicationExtension.class)
class GameContextTest {

    //region Mocks
    @Mock
    private GazePlay mockGazePlay;

    @Mock
    private Translator mockTranslator;

    @Mock
    private Pane mockRoot;

    @Mock
    private Pane mockGamingRoot;

    @Mock
    private Bravo mockBravo;

    @Mock
    private HBox mockHBox;

    @Mock
    private GazeDeviceManager mockGazeDeviceManager;

    @Mock
    private SoundManager mockSoundManager;

    @Mock
    private Pane mockConfigPane;

    @Mock
    private Configuration mockConfiguration;

    @Mock
    private ObservableList<Node> mockList;

    @Mock
    private Stats mockStats;

    @Mock
    private GameLifeCycle mockGameLifeCycle;
    //endregion

    private SavedStatsInfo mockSavedStatsInfo = new SavedStatsInfo(
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv")
    );

    @AfterEach
    void restoreGlobalState() {
        ActiveConfigurationContext.switchToDefaultUser();
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        ActiveConfigurationContext.setInstance(mockConfiguration);
        when(mockStats.getFixationSequence()).thenReturn(new ArrayList<>(List.of(new LinkedList<>(), new LinkedList<>())));
    }

    /**
     * The time before a selection is that of the settings, for every game (maintainer's request of 4/11/2026).
     */
    @Test
    void shouldBlockTheGameAreaForTheDelayOfTheSettings() throws Exception {
        when(mockRoot.getChildren()).thenReturn(mockList);
        when(mockConfiguration.getGameStartDelay()).thenReturn(50);
        GameContext gameContext = new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo,
            mockHBox, mockGazeDeviceManager, mockSoundManager, mockConfigPane);

        org.testfx.util.WaitForAsyncUtils.asyncFx(gameContext::onGameStarted).get();

        verify(mockGamingRoot).setDisable(true);
        verify(mockGamingRoot, timeout(5000)).setDisable(false);
    }

    @Test
    void shouldNotBlockTheGameAreaWithoutDelay() throws Exception {
        when(mockRoot.getChildren()).thenReturn(mockList);
        when(mockConfiguration.getGameStartDelay()).thenReturn(0);
        GameContext gameContext = new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo,
            mockHBox, mockGazeDeviceManager, mockSoundManager, mockConfigPane);

        org.testfx.util.WaitForAsyncUtils.asyncFx(gameContext::onGameStarted).get();

        verify(mockGamingRoot, never()).setDisable(true);
    }

    /**
     * Decision of the maintainer of 5/11/2026: the full screen button stays for the games, greyed for the evaluations.
     */
    @Test
    void shouldAskTheGameWhetherTheFullScreenCanChange() {
        when(mockGameLifeCycle.allowsFullScreenChange()).thenReturn(false);
        assertFalse(GameContext.allowsFullScreenChange(mockGameLifeCycle));

        when(mockGameLifeCycle.allowsFullScreenChange()).thenReturn(true);
        assertTrue(GameContext.allowsFullScreenChange(mockGameLifeCycle));

        assertTrue(GameContext.allowsFullScreenChange(null));
    }

    @Test
    void shouldInitialisePointersOnConstruction() {
        when(mockConfiguration.isVideoRecordingEnabled()).thenReturn(true);
        when(mockRoot.getChildren()).thenReturn(mockList);

        new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo, mockHBox, mockGazeDeviceManager, mockSoundManager, mockConfigPane);

        verify(mockList, times(2)).add(any());
        verify(mockRoot, times(2)).addEventFilter(any(), any());
    }

    @Test
    void shouldNotInitialisePointersOnConstruction() {
        when(mockConfiguration.isVideoRecordingEnabled()).thenReturn(false);
        when(mockRoot.getChildren()).thenReturn(mockList);

        new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo, mockHBox, mockGazeDeviceManager, mockSoundManager, mockConfigPane);

        verify(mockList, never()).add(any());
        verify(mockRoot, never()).addEventFilter(any(), any());
    }

    /**
     * The games pause the background music through their context (they used to call the music manager directly).
     */
    @Test
    void shouldPauseTheBackgroundMusicForTheGames() {
        BackgroundMusicManager musicManager = mock(BackgroundMusicManager.class);
        BackgroundMusicManager.setInstance(musicManager);
        try {
            when(mockRoot.getChildren()).thenReturn(mockList);
            GameContext gameContext = new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo,
                mockHBox, mockGazeDeviceManager, mockSoundManager, mockConfigPane);

            gameContext.pauseBackgroundMusic();

            verify(musicManager).pause();
        } finally {
            BackgroundMusicManager.setInstance(new BackgroundMusicManager());
        }
    }

    /**
     * Nothing saved (GazePlay Eval Test, an evaluation left on its participant form): no statistics to show before the
     * replay; the statistics screen would read metrics images that do not exist.
     */
    @Test
    void shouldNotShowTheStatisticsOfAReplayWhenNothingIsSaved() throws Exception {
        when(mockRoot.getChildren()).thenReturn(mockList);
        when(mockGamingRoot.getChildren()).thenReturn(mockList);
        when(mockStats.isSavingDisabled()).thenReturn(true);
        GameContext context = new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo, mockHBox,
            mockGazeDeviceManager, mockSoundManager, mockConfigPane);

        org.testfx.util.WaitForAsyncUtils.asyncFx(() -> context.exitReplayGame(mockStats, mockGazePlay, mockGameLifeCycle)).get();

        verify(mockStats, never()).saveStats();
        verify(mockGazePlay, never()).onDisplayStats(any());
    }

    @Test
    void shouldRemoveEventFiltersWhenRecording() {

        when(mockConfiguration.isVideoRecordingEnabled()).thenReturn(true);
        when(mockConfiguration.getAreaOfInterestDisabledProperty()).thenReturn(new SimpleBooleanProperty(true));
        when(mockConfiguration.isFixationSequenceDisabled()).thenReturn(false);
        when(mockRoot.getChildren()).thenReturn(mockList);
        when(mockGamingRoot.getChildren()).thenReturn(mockList);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
        when(mockTranslator.currentLocale()).thenReturn(Locale.ENGLISH);
        when(mockStats.getSavedStatsInfo()).thenReturn(mockSavedStatsInfo);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> new Dimension2D(1920, 1080));


        final GameContext context =
            new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo, mockHBox, mockGazeDeviceManager, mockSoundManager, mockConfigPane);

        context.exitGame(mockStats, mockGazePlay, mockGameLifeCycle);

        verify(mockRoot).removeEventFilter(eq(MouseEvent.ANY), any());
        verify(mockRoot).removeEventFilter(eq(GazeEvent.ANY), any());
        verify(mockRoot, atLeast(2)).getChildren();
        verify(mockGazeDeviceManager).removeEventFilter(eq(mockRoot));
    }

    @Test
    void shouldNotRemoveEventFiltersWhenNotRecording() {

        when(mockConfiguration.isVideoRecordingEnabled()).thenReturn(false);
        when(mockConfiguration.getAreaOfInterestDisabledProperty()).thenReturn(new SimpleBooleanProperty(true));
        when(mockConfiguration.isFixationSequenceDisabled()).thenReturn(false);
        when(mockRoot.getChildren()).thenReturn(mockList);
        when(mockGamingRoot.getChildren()).thenReturn(mockList);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
        when(mockTranslator.currentLocale()).thenReturn(Locale.ENGLISH);
        when(mockStats.getSavedStatsInfo()).thenReturn(mockSavedStatsInfo);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> new Dimension2D(1920, 1080));


        final GameContext context =
            new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo, mockHBox, mockGazeDeviceManager, mockSoundManager, mockConfigPane);

        context.exitGame(mockStats, mockGazePlay, mockGameLifeCycle);

        verify(mockRoot, never()).removeEventFilter(eq(MouseEvent.ANY), any());
        verify(mockRoot, never()).removeEventFilter(eq(GazeEvent.ANY), any());
        verify(mockRoot, never()).getChildren();
        verify(mockGazeDeviceManager, never()).removeEventFilter(eq(mockRoot));
    }
}
