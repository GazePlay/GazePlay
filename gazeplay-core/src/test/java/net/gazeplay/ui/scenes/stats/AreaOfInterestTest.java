package net.gazeplay.ui.scenes.stats;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class AreaOfInterestTest {

    @Mock
    private GazePlay mockGazePlay;

    @Mock
    private Stats mocksStats;

    @Mock
    private AreaOfInterestRecording gazeAreas;

    @Mock
    private AreaOfInterestRecording mouseAreas;

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
        when(mocksStats.getGazeAreasOfInterest()).thenReturn(gazeAreas);
        when(mocksStats.getMouseAreasOfInterest()).thenReturn(mouseAreas);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(supplier);
        when(mockGazePlay.getTranslator()).thenReturn(mock(Translator.class));
    }

    private static Polygon square(final double x, final double y, final double side) {
        final Polygon polygon = new Polygon();
        polygon.getPoints().addAll(x, y, x + side, y, x + side, y + side, x, y + side);
        return polygon;
    }

    /**
     * R13: the areas of each source are shown separately. A source without any sample (the mouse in a game played
     * with an eye tracker) used to make the screen fail on its first sample; it now says that nothing was recorded.
     */
    @Test
    void shouldShowASourceWithoutSamples() {
        when(gazeAreas.getMovementHistory()).thenReturn(List.of(new CoordinatesTracker(100, 200, 1234, 3456)));

        AreaOfInterest areaOfInterest = new AreaOfInterest(mockGazePlay, mocksStats, AreaOfInterest.Source.MOUSE);

        assertTrue(areaOfInterest.getZones().isEmpty());
        assertTrue(areaOfInterest.getReplayButton().isDisabled());
        verify(mouseAreas).getMovementHistory();
    }

    @Test
    void shouldShowTheGazeFirstWhenThereIsGaze() {
        when(gazeAreas.getMovementHistory()).thenReturn(List.of(new CoordinatesTracker(100, 200, 1234, 3456)));
        assertEquals(AreaOfInterest.Source.GAZE, AreaOfInterest.defaultSource(mocksStats));

        when(gazeAreas.getMovementHistory()).thenReturn(List.of());
        assertEquals(AreaOfInterest.Source.MOUSE, AreaOfInterest.defaultSource(mocksStats));
    }

    @Test
    void shouldOfferTheSourcesOfTheInputs() {
        assertEquals(List.of(AreaOfInterest.Source.MOUSE), AreaOfInterest.sourcesShown(EyeTracker.mouse_control));
        assertEquals(List.of(AreaOfInterest.Source.GAZE), AreaOfInterest.sourcesShown(EyeTracker.tobii));
        assertEquals(List.of(AreaOfInterest.Source.GAZE, AreaOfInterest.Source.MOUSE),
            AreaOfInterest.sourcesShown(EyeTracker.tobii_and_mouse));
    }

    @Test
    void shouldOfferThreeSpeedsAndReplay() {
        List<CoordinatesTracker> coordinatesTrackers = List.of(
            new CoordinatesTracker(100, 200, 1234, 3456),
            new CoordinatesTracker(200, 200, 4321, 4567)
        );
        when(gazeAreas.getMovementHistory()).thenReturn(coordinatesTrackers);

        AreaOfInterest areaOfInterest = new AreaOfInterest(mockGazePlay, mocksStats, AreaOfInterest.Source.GAZE);

        assertEquals(3, areaOfInterest.getSpeedTabs().size());
        assertTrue(areaOfInterest.getSpeedTabs().get(0).isSelected());
        assertFalse(areaOfInterest.getReplayButton().isDisabled());

        areaOfInterest.startReplay();
        assertTrue(areaOfInterest.isPlaying());
        areaOfInterest.stopReplay();
        assertFalse(areaOfInterest.isPlaying());
    }

    /**
     * Two stays at the same place make one area: its first fixation is the one of its first stay, its positions and
     * its time are added up, and the second stay is a return.
     */
    @Test
    void shouldMergeTheStaysAtTheSamePlace() {
        final List<AreaOfInterest.Stay> stays = List.of(
            new AreaOfInterest.Stay(square(100, 100, 50), 1.5, 2.0, 40, 0, 39, 1000),
            new AreaOfInterest.Stay(square(500, 500, 50), 3.0, 0.5, 10, 40, 49, 2000),
            new AreaOfInterest.Stay(square(102, 102, 50), 6.0, 1.0, 25, 50, 74, 3000)
        );

        final List<AreaOfInterest.Zone> zones = AreaOfInterest.mergeStays(stays);

        assertEquals(2, zones.size());
        final AreaOfInterest.Zone merged = zones.get(0);
        assertEquals(1, merged.number());
        assertEquals(1.5, merged.firstFixation());
        assertEquals(3.0, merged.timeSpent());
        assertEquals(65, merged.samples());
        assertEquals(1, merged.returns());
        assertEquals(List.of(0, 2), merged.stayIndexes());
        final AreaOfInterest.Zone alone = zones.get(1);
        assertEquals(2, alone.number());
        assertEquals(10, alone.samples());
        assertEquals(0, alone.returns());
    }

    /**
     * The list of the areas (maquette A) opens the first area; hovering another area opens it instead.
     */
    @Test
    void shouldOpenTheFirstAreaInTheList() {
        final List<CoordinatesTracker> positions = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            positions.add(new CoordinatesTracker(i < 10 ? 100 : 500, i < 10 ? 100 : 500, 20, 1000 + 20L * i));
        }
        when(gazeAreas.getMovementHistory()).thenReturn(positions);
        when(gazeAreas.getAllAOIListTemp()).thenReturn(List.of(positions.subList(0, 10), positions.subList(10, 30)));
        when(gazeAreas.getAllAOIListPolygon()).thenReturn(List.of(square(90, 90, 20), square(490, 490, 20)));
        when(gazeAreas.getStartAndEndIdx()).thenReturn(List.of(new int[]{0, 9}, new int[]{10, 29}));

        AreaOfInterest areaOfInterest = new AreaOfInterest(mockGazePlay, mocksStats, AreaOfInterest.Source.GAZE);

        assertEquals(2, areaOfInterest.getZones().size());
        assertEquals(1, areaOfInterest.getSelectedZone().number());
        areaOfInterest.selectZone(areaOfInterest.getZones().get(1));
        assertEquals(2, areaOfInterest.getSelectedZone().number());
    }

    @Test
    void shouldScoreTheStaysOnTheTargets() {
        final TargetAOI target = new TargetAOI(150, 150, 100, 0);
        target.setTimeEnded(5000);
        final List<AreaOfInterest.Stay> stays = List.of(
            new AreaOfInterest.Stay(square(100, 100, 50), 1, 1, 10, 0, 9, 1000),
            new AreaOfInterest.Stay(square(900, 900, 50), 2, 1, 10, 10, 19, 2000)
        );

        final double score = AreaOfInterest.targetScore(stays, new ArrayList<>(List.of(target)));

        assertTrue(score > 0.4 && score <= 0.5, "one stay on the target out of two: " + score);
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

        Double[] actual = AoiGeometry.calculateRectangle(input);

        assertArrayEquals(expected, actual);
    }

    @Test
    void shouldCalculateOrientation() {
        // Collinear
        assertEquals(0, AoiGeometry.orientation(
            new Point2D(0, 0),
            new Point2D(1, 1),
            new Point2D(2, 2)
        ));

        // Clockwise
        assertEquals(1, AoiGeometry.orientation(
            new Point2D(2, 5),
            new Point2D(1, 2),
            new Point2D(0, 0)
        ));

        // Counterclockwise
        assertEquals(-1, AoiGeometry.orientation(
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

        Double[] actual = AoiGeometry.calculateConvexHull(input);

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
}
