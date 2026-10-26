package net.gazeplay.commons.utils.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Groups the samples kept for the areas of interest into areas, free of JavaFX. Extracted from {@code Stats} without any
 * change (behaviour pinned by {@code StatsRecordingCharacterizationTest}):
 * <ul>
 * <li>a sample closer than {@link #GROUP_DISTANCE} px to the previous one, more than {@link #MIN_INTERVAL} ms later,
 * joins the current group;</li>
 * <li>otherwise a group of more than 2 samples becomes an area: rectangle around its samples, with a
 * {@link #MARGIN} px margin;</li>
 * <li>a group of 0 to 2 samples followed by a jump longer than {@link #ISOLATED_JUMP} px becomes, with the sample of
 * the jump, a square of 2 x {@link #MARGIN} px (plus the margin) around its first sample, i.e. around the sample of the
 * jump when the group was empty;</li>
 * <li>in both cases, and for a shorter jump, a new group starts.</li>
 * </ul>
 * Which samples are kept (one per change of position for the gaze, see R13 for the mouse) is decided by {@code Stats}.
 */
final class AreaOfInterestGrouper {

    static final double GROUP_DISTANCE = 150;
    static final long MIN_INTERVAL = 10;
    static final double ISOLATED_JUMP = 700;
    static final int MARGIN = 15;

    /**
     * An area of interest.
     *
     * @param samples   the samples of the area
     * @param start     index, in the kept samples, of the first sample of the area (as computed before the extraction)
     * @param end       index of the sample that closed the area
     * @param rectangle corners of the area: x1, y1, ..., x4, y4
     */
    record Area(List<CoordinatesTracker> samples, int start, int end, Double[] rectangle) {
    }

    private List<CoordinatesTracker> group = new ArrayList<>();

    /**
     * @param history the samples kept so far
     * @param index   index of the new sample in history (at least 1)
     * @return the area closed by this sample, if any
     */
    Optional<Area> add(final List<CoordinatesTracker> history, final int index) {
        final CoordinatesTracker sample = history.get(index);
        final CoordinatesTracker previous = history.get(index - 1);
        final double distance = Math.sqrt(Math.pow(previous.getXValue() - sample.getXValue(), 2)
            + Math.pow(previous.getYValue() - sample.getYValue(), 2));

        if (distance < GROUP_DISTANCE && sample.getIntervalTime() > MIN_INTERVAL) {
            if (index == 1) {
                group.add(history.get(0));
            }
            group.add(sample);
            return Optional.empty();
        }

        Area area = null;
        if (group.size() > 2) {
            area = new Area(new ArrayList<>(group), index - group.size(), index, rectangleAround(group, 0));
        } else if (distance > ISOLATED_JUMP) {
            group.add(sample);
            // Square around the first sample: the 8 points +/- the margin of the former code give its corners
            area = new Area(new ArrayList<>(group), index - group.size(), index,
                rectangleAround(List.of(group.get(0)), MARGIN));
        }
        group = new ArrayList<>();
        return Optional.ofNullable(area);
    }

    /**
     * @param spread 0 for the rectangle around all the samples, otherwise the half side of a square around the first
     *   sample
     */
    private static Double[] rectangleAround(final List<CoordinatesTracker> samples, final int spread) {
        final double[] xs = new double[samples.size()];
        final double[] ys = new double[samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            xs[i] = samples.get(i).getXValue();
            ys[i] = samples.get(i).getYValue();
        }
        if (spread == 0) {
            return rectangle(xs, ys);
        }
        return rectangle(new double[]{xs[0] - spread, xs[0] + spread}, new double[]{ys[0] - spread, ys[0] + spread});
    }

    /**
     * Rectangle around the points, with a {@link #MARGIN} px margin, in the order expected by the drawing code:
     * (left, top), (right, top), (right, bottom), (left, bottom), where "top" is the largest y.
     */
    static Double[] rectangle(final double[] xs, final double[] ys) {
        double left = xs[0];
        double right = xs[0];
        double top = ys[0];
        double bottom = ys[0];
        for (int i = 1; i < xs.length; i++) {
            left = Math.min(left, xs[i]);
            right = Math.max(right, xs[i]);
            top = Math.max(top, ys[i]);
            bottom = Math.min(bottom, ys[i]);
        }
        return new Double[]{
            left - MARGIN, top + MARGIN,
            right + MARGIN, top + MARGIN,
            right + MARGIN, bottom - MARGIN,
            left - MARGIN, bottom - MARGIN
        };
    }
}
