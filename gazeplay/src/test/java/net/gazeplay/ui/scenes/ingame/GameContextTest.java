package net.gazeplay.ui.scenes.ingame;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import mockit.MockUp;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.Bravo;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.scenes.stats.StatsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.mockito.Mockito.*;

/**
 * This test class demonstrates how to use JMockit and Mockito within the same tests.
 */
@RunWith(MockitoJUnitRunner.class)
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        new MockUp<ActiveConfigurationContext>() {
            @mockit.Mock
            public Configuration getInstance() {
                return mockConfiguration;
            }
        };
    }

    @Test
    void shouldInitialisePointersOnConstruction() {
        when(mockConfiguration.isVideoRecordingEnabled()).thenReturn(true);
        when(mockRoot.getChildren()).thenReturn(mockList);

        new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo, mockHBox, mockGazeDeviceManager, mockConfigPane);

        verify(mockList, times(2)).add(any());
        verify(mockRoot, times(2)).addEventFilter(any(), any());
    }

    @Test
    void shouldNotInitialisePointersOnConstruction() {
        when(mockConfiguration.isVideoRecordingEnabled()).thenReturn(false);
        when(mockRoot.getChildren()).thenReturn(mockList);

        new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo, mockHBox, mockGazeDeviceManager, mockConfigPane);

        verify(mockList, never()).add(any());
        verify(mockRoot, never()).addEventFilter(any(), any());
    }

    @Test
    void shouldRemoveEventFiltersWhenRecording() {
        new MockUp<StatsContext>() {
            @mockit.Mock
            public StatsContext newInstance(final GazePlay gazePlay, final Stats stats) {
                return mock(StatsContext.class);
            }
        };

        when(mockConfiguration.isVideoRecordingEnabled()).thenReturn(true);
        when(mockRoot.getChildren()).thenReturn(mockList);
        when(mockGamingRoot.getChildren()).thenReturn(mockList);

        final GameContext context =
            new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo, mockHBox, mockGazeDeviceManager, mockConfigPane);

        context.exitGame(mockStats, mockGazePlay, mockGameLifeCycle);

        verify(mockRoot).removeEventFilter(eq(MouseEvent.ANY), any());
        verify(mockRoot).removeEventFilter(eq(GazeEvent.ANY), any());
        verify(mockRoot, atLeast(2)).getChildren();
        verify(mockGazeDeviceManager).removeEventFilter(eq(mockRoot));
    }

    @Test
    void shouldNotRemoveEventFiltersWhenNotRecording() {
        new MockUp<StatsContext>() {
            @mockit.Mock
            public StatsContext newInstance(final GazePlay gazePlay, final Stats stats) {
                return mock(StatsContext.class);
            }
        };

        when(mockConfiguration.isVideoRecordingEnabled()).thenReturn(false);
        when(mockRoot.getChildren()).thenReturn(mockList);
        when(mockGamingRoot.getChildren()).thenReturn(mockList);

        final GameContext context =
            new GameContext(mockGazePlay, mockTranslator, mockRoot, mockGamingRoot, mockBravo, mockHBox, mockGazeDeviceManager, mockConfigPane);

        context.exitGame(mockStats, mockGazePlay, mockGameLifeCycle);

        verify(mockRoot, never()).removeEventFilter(eq(MouseEvent.ANY), any());
        verify(mockRoot, never()).removeEventFilter(eq(GazeEvent.ANY), any());
        verify(mockRoot, never()).getChildren();
        verify(mockGazeDeviceManager, never()).removeEventFilter(eq(mockRoot));
    }
}
