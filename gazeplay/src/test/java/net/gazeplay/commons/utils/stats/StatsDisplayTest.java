package net.gazeplay.commons.utils.stats;

import javafx.scene.Cursor;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.ui.scenes.stats.StatsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(ApplicationExtension.class)
@RunWith(MockitoJUnitRunner.class)
class StatsDisplayTest {

    @Mock
    private GazePlay mockGazePlay;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StatsContext mockStatsContext;

    @Captor
    private ArgumentCaptor<Cursor> captor;

    @BeforeEach
    void setup() {
        initMocks();
    }

    void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldCreateHomeButton() {
        HomeButton button = StatsDisplay.createHomeButtonInStatsScreen(mockGazePlay, mockStatsContext);
        assert button.isVisible();
    }

    @Test
    void shouldSetTheCursorWhenPressed() {
        StatsDisplay.returnToMenu(mockGazePlay, mockStatsContext);

        verify(mockStatsContext.getRoot(), times(2)).setCursor(captor.capture());
        assertTrue(captor.getAllValues().contains(Cursor.WAIT));
        assertTrue(captor.getAllValues().contains(Cursor.DEFAULT));
    }
}
