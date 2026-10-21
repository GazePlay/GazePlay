package net.gazeplay.commons.utils.stats;

import net.gazeplay.commons.utils.FixationPoint;

import java.util.LinkedList;

/**
 * Builds the fixation sequence of a round ("fixationSequence" of replayData.json) from the samples, free of JavaFX and
 * of any clock.
 * <p>
 * A sample closer than {@link #FIXATION_TRAIL} pixels on each axis to the first sample of the last point is merged
 * into it. The duration of a point is the time from its first sample to the first sample of the next point, or to its
 * last sample for the last point of the sequence (format version 2, see {@link ReplayDataFormat}). Version 1 never
 * merged the second sample, and stored twice the elapsed time for a merged point and the duration of the previous
 * point for a new one (R2).
 * <p>
 * The point keeps the convention of the memory: Y in its "x" field and X in its "y" field (see
 * {@link ReplayDataFormat}). The behaviour is pinned by {@code StatsRecordingCharacterizationTest}.
 */
final class FixationSequenceRecorder {

    /**
     * Maximum distance, in pixels on each axis, for a sample to be merged into the last point.
     */
    static final int FIXATION_TRAIL = 50;

    private FixationSequenceRecorder() {
    }

    static void add(final LinkedList<FixationPoint> fixationSequence, final long time, final int x, final int y) {
        if (!fixationSequence.isEmpty()) {
            final FixationPoint last = fixationSequence.getLast();
            // Same convention as the stored points: Y in "x", X in "y"
            final boolean inLastFixation = Math.abs(y - last.getX()) <= FIXATION_TRAIL
                && Math.abs(x - last.getY()) <= FIXATION_TRAIL;
            last.setGazeDuration(time - last.getTimeGaze());
            if (inLastFixation) {
                return;
            }
        }
        fixationSequence.add(new FixationPoint(time, 0, y, x));
    }
}
