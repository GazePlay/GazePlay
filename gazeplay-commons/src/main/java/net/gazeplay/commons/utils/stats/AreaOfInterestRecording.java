package net.gazeplay.commons.utils.stats;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Areas of interest of one source (gaze or mouse), computed during the game and shown by the AreaOfInterest screen
 * (never written to disk).
 * <p>
 * Each source has its own samples and areas (R13). Until October 2026 the gaze and the mouse were mixed in a single
 * list, with a single previous time, and the mouse kept a vertical move only one event in three (condition
 * {@code x != previousX || y != previousY && counter == 2}): both sources now keep every change of position.
 */
public class AreaOfInterestRecording {

    private static final Color[] COLORS = {
        Color.PURPLE, Color.WHITE, Color.PINK, Color.ORANGE, Color.BLUE, Color.RED, Color.CHOCOLATE
    };

    /**
     * The samples kept: one per change of position.
     */
    @Getter
    private final List<CoordinatesTracker> movementHistory = new ArrayList<>();

    @Getter
    private final List<List<CoordinatesTracker>> allAOIListTemp = new ArrayList<>();

    @Getter
    private final List<int[]> startAndEndIdx = new ArrayList<>();

    @Getter
    private final List<Polygon> allAOIListPolygon = new ArrayList<>();

    @Getter
    private final List<Double[]> allAOIListPolygonPt = new ArrayList<>();

    private final AreaOfInterestGrouper grouper = new AreaOfInterestGrouper();

    private int previousX = 0;
    private int previousY = 0;
    private long previousTime = 0;

    /**
     * @param x    position in the scene
     * @param y    position in the scene
     * @param time time of the sample since the start of the round, in ms
     */
    public void add(final int x, final int y, final long time) {
        if (x == previousX && y == previousY) {
            return;
        }
        previousX = x;
        previousY = y;
        movementHistory.add(new CoordinatesTracker(x, y, time - previousTime, System.currentTimeMillis()));
        previousTime = time;

        final int index = movementHistory.size() - 1;
        if (index >= 1) {
            grouper.add(movementHistory, index).ifPresent(area -> {
                allAOIListTemp.add(area.samples());
                startAndEndIdx.add(new int[]{area.start(), area.end()});
                allAOIListPolygonPt.add(area.rectangle());

                final Polygon areaOfInterest = new Polygon();
                areaOfInterest.getPoints().addAll(area.rectangle());
                areaOfInterest.setStroke(COLORS[index % COLORS.length]);
                allAOIListPolygon.add(areaOfInterest);
            });
        }
    }
}
