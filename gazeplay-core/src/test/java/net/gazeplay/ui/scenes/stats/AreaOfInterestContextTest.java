package net.gazeplay.ui.scenes.stats;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.utils.stats.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class AreaOfInterestContextTest {

    @Mock
    private GazePlay mockGazePlay;

    @Mock
    private Stats mocksStats;

    SavedStatsInfo statsInfo = new SavedStatsInfo(
        new File("file1.csv"),
        new File("metricsMouse.csv"),
        new File("metricsGaze.csv"),
        new File("metricsBoth.csv"),
        new File("screenshot.png"),
        new File("colors.txt"),
        new File("replayData.txt")
    );
    Dimension2D screen = new Dimension2D(700, 800);
    Supplier<Dimension2D> supplier = () -> screen;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(mocksStats.getSavedStatsInfo()).thenReturn(statsInfo);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(supplier);
    }

    @Test
    void shouldCreateButtonBox() {
        List<CoordinatesTracker> coordinatesTrackers = List.of(
            new CoordinatesTracker(100, 200, 1234, 3456),
            new CoordinatesTracker(200, 200, 4321, 4567)
        );
        when(mocksStats.getMovementHistory()).thenReturn(coordinatesTrackers);

        AreaOfInterestContext areaOfInterestContext = new AreaOfInterestContext(mockGazePlay, mocksStats);

        HBox result = areaOfInterestContext.createButtonBox();

        assertEquals(5, result.getChildren().size());
    }
/*
    @Test
    void shouldCalculateInfoBox() {
        List<CoordinatesTracker> coordinatesTrackers = List.of(
            new CoordinatesTracker(100, 200, 1234, 3456),
            new CoordinatesTracker(200, 200, 4321, 4567)
        );
        when(mocksStats.getMovementHistoryWithTime()).thenReturn(coordinatesTrackers);

        AreaOfInterestView areaOfInterestView = new AreaOfInterestView(mockGazePlay, mocksStats);

        Polygon currentArea = new Polygon();
        currentArea.getPoints().addAll(1d, 1d, 2d, 1d, 2d, 3d, 1d, 3d);
        InfoBoxProps result1 = areaOfInterestView.calculateInfoBox("id", 12.34, 1.23, 3, 300, 400, currentArea);
        InfoBoxProps result2 = areaOfInterestView.calculateInfoBox("id", 12.34, 1.23, 3, 500, 400, currentArea);

        assertEquals(401, result1.getInfoBox().getLayoutX());
        assertEquals(209, result2.getInfoBox().getLayoutX());
    }
*/
    @Test
    void shouldMakeInfoBox() {
        String aoiID = "id";
        String ttff = "12.345";
        String timeSpent = "45.678";
        int fixations = 5;

        GridPane result1 = AreaOfInterestContext.makeInfoBox(aoiID, ttff, timeSpent, fixations, 10);
        GridPane result2 = AreaOfInterestContext.makeInfoBox(aoiID, ttff, timeSpent, fixations, 0);

        assertEquals(3, result1.getColumnCount());
        assertEquals(6, result1.getRowCount());
        assertEquals(5, result2.getRowCount());
    }
}
