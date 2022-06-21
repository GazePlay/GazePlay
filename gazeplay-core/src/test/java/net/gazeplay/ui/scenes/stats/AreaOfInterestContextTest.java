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
        MockitoAnnotations.openMocks(this);
        when(mocksStats.getSavedStatsInfo()).thenReturn(statsInfo);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(supplier);
    }

    @Test
    void shouldCreateButtonBox() {
        List<CoordinatesTracker> coordinatesTrackers = List.of(
            new CoordinatesTracker(100, 200, 1234, 3456, "mouse"),
            new CoordinatesTracker(200, 200, 4321, 4567, "mouse")
        );
        when(mocksStats.getMovementHistory()).thenReturn(coordinatesTrackers);

        AreaOfInterestContext areaOfInterestContext = new AreaOfInterestContext(mockGazePlay, mocksStats, false);

        HBox result = areaOfInterestContext.createButtonBox();

        assertEquals(5, result.getChildren().size());
    }

    @Test
    void shouldCalculateAOIView() {
        List<CoordinatesTracker> coordinatesTrackers = List.of(
            new CoordinatesTracker(100, 200, 1234, 3456, "mouse"),
            new CoordinatesTracker(200, 200, 4321, 4567, "mouse"),
            new CoordinatesTracker(300, 300, 1234, 5678, "mouse")
        );
        when(mocksStats.getMovementHistory()).thenReturn(coordinatesTrackers);

        AreaOfInterestContext areaOfInterestContext = new AreaOfInterestContext(mockGazePlay, mocksStats, false);

        Double[] convexPoints = {1d, 1d, 2d, 1d, 2d, 3d, 1d, 3d};
        AreaOfInterest aoi1 = new AreaOfInterest("id", coordinatesTrackers, 300, 400, convexPoints, 0, 2, 1.23, 12.34);
        AreaOfInterest aoi2 = new AreaOfInterest("id", coordinatesTrackers, 500, 400, convexPoints, 0, 2, 1.23, 12.34);
        AreaOfInterestView aoiView1 = areaOfInterestContext.calculateAOIView(aoi1, 0);
        AreaOfInterestView aoiView2 = areaOfInterestContext.calculateAOIView(aoi2, 0);

        assertEquals(403, aoiView1.getInfoBox().getLayoutX());
        assertEquals(207, aoiView2.getInfoBox().getLayoutX());
    }

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
