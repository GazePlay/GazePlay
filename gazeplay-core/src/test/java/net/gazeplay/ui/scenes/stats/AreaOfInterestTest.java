package net.gazeplay.ui.scenes.stats;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class AreaOfInterestTest {

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
        when(mocksStats.getMovementHistoryWithTime()).thenReturn(coordinatesTrackers);

        AreaOfInterest areaOfInterest = new AreaOfInterest(mockGazePlay, mocksStats);

        HBox result = areaOfInterest.createButtonBox();

        assertEquals(5, result.getChildren().size());
    }

    @Test
    void shouldCalculateInfoBox() {
        List<CoordinatesTracker> coordinatesTrackers = List.of(
            new CoordinatesTracker(100, 200, 1234, 3456),
            new CoordinatesTracker(200, 200, 4321, 4567)
        );
        when(mocksStats.getMovementHistoryWithTime()).thenReturn(coordinatesTrackers);

        AreaOfInterest areaOfInterest = new AreaOfInterest(mockGazePlay, mocksStats);

        Polygon currentArea = new Polygon();
        currentArea.getPoints().addAll(1d, 1d, 2d, 1d, 2d, 3d, 1d, 3d);
        InfoBoxProps result1 = areaOfInterest.calculateInfoBox("id", 12.34, 1.23, 3, 300, 400, currentArea);
        InfoBoxProps result2 = areaOfInterest.calculateInfoBox("id", 12.34, 1.23, 3, 500, 400, currentArea);

        assertEquals(401, result1.getInfoBox().getLayoutX());
        assertEquals(209, result2.getInfoBox().getLayoutX());
    }

    @Test
    void shouldCalculateRectangle() {
        Point2D[] input = new Point2D[]{
            new Point2D(706.0, 685.0),
            new Point2D(710.0, 670.0),
            new Point2D(708.0, 690.0)
        };

        Double[] expected = new Double[]{
            706.0 - 15,
            690.0 + 15,
            710.0 + 15,
            690.0 + 15,
            710.0 + 15,
            670.0 - 15,
            706.0 - 15,
            670.0 - 15
        };

        Double[] actual = AreaOfInterest.calculateRectangle(input);

        assertArrayEquals(expected, actual);
    }

    @Test
    void shouldCalculateOrientation() {
        // Collinear
        assertEquals(0, AreaOfInterest.orientation(
            new Point2D(0, 0),
            new Point2D(1, 1),
            new Point2D(2, 2)
        ));

        // Clockwise
        assertEquals(1, AreaOfInterest.orientation(
            new Point2D(2, 5),
            new Point2D(1, 2),
            new Point2D(0, 0)
        ));

        // Counterclockwise
        assertEquals(-1, AreaOfInterest.orientation(
            new Point2D(0, 0),
            new Point2D(1, 2),
            new Point2D(2, 5)
        ));
    }

    @Test
    void shouldCalculateConvexHull() {
        Point2D[] input = new Point2D[]{
            new Point2D(2, 2),
            new Point2D(2, 3),
            new Point2D(3, 5),
            new Point2D(1, 2),
            new Point2D(1.25, 3),
            new Point2D(2, 1),
            new Point2D(4, 2)
        };

        Double[] expected = new Double[]{
            1d, 2d, 2d, 1d, 4d, 2d, 3d, 5d, 1.25, 3d
        };

        Double[] actual = AreaOfInterest.calculateConvexHull(input);

        assertArrayEquals(expected, actual);
    }

    @Test
    void shouldCalculateTargetAOI() {
        TargetAOI t1 = new TargetAOI(500, 500, 300, 1234);
        TargetAOI t2 = new TargetAOI(650, 700, 200, 1234);

        Double[] e1 = new Double[]{
            385d, 615d, 815d, 615d, 815d, 185d, 385d, 185d
        };
        Double[] e2 = new Double[]{
            535d, 815d, 865d, 815d, 865d, 485d, 535d, 485d
        };

        ArrayList<TargetAOI> input = new ArrayList<>(List.of(t1, t2));

        AreaOfInterest.calculateTargetAOI(input);

        Double[] r1 = new Double[8], r2 = new Double[8];
        input.get(0).getPolygon().getPoints().toArray(r1);
        input.get(1).getPolygon().getPoints().toArray(r2);

        assertArrayEquals(r1, e1);
        assertArrayEquals(r2, e2);
    }

    @Test
    void shouldMakeInfoBox() {
        String aoiID = "id";
        String ttff = "12.345";
        String timeSpent = "45.678";
        int fixations = 5;

        GridPane result1 = AreaOfInterest.makeInfoBox(aoiID, ttff, timeSpent, fixations, 10);
        GridPane result2 = AreaOfInterest.makeInfoBox(aoiID, ttff, timeSpent, fixations, 0);

        assertEquals(3, result1.getColumnCount());
        assertEquals(6, result1.getRowCount());
        assertEquals(5, result2.getRowCount());
    }
}
