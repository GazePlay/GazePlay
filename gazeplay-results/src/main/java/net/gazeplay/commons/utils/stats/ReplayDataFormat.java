package net.gazeplay.commons.utils.stats;

import net.gazeplay.commons.utils.FixationPoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Versions of replayData.json. The files are never rewritten: a reader must look at "formatVersion" (absent before
 * version 2) to interpret them. Differences of version 2 (see MODERNIZATION.md, "Format de résultats v2"):
 * <ul>
 * <li>"coordinatesAndTimeStamp" only holds the samples of the round (version 1: also the previous rounds, R5), and
 * the mouse samples are the movements only (version 1: also the button events, R1);</li>
 * <li>"fixationSequence": "x" is the horizontal and "y" the vertical coordinate (version 1: swapped, R11), and the
 * durations are those of the fixations (R2).</li>
 * </ul>
 * Difference of version 3: the "time" of the gaze samples of "coordinatesAndTimeStamp", and the "timeGaze" (hence the
 * durations) of the gaze fixation sequence, are the times the samples were acquired, no longer the times they were
 * handled on the JavaFX thread. Samples queued while this thread was busy used to get the same time (several samples
 * with the same "time"), and a later one. Each source ("gaze", "mouse") stays in increasing time order, but a mouse
 * sample can now precede a gaze sample of a slightly earlier time in the array. The mouse samples are unchanged.
 * <p>
 * Differences of version 4:
 * <ul>
 * <li>the gaze samples of a Tobii eye tracker are the new measures only. The position was read every 10 ms and
 * repeated when the eye tracker had sent nothing new (about 70 % of the samples with an Eye Tracker 5, R6): about 3
 * times fewer gaze samples in "coordinatesAndTimeStamp" and in the gaze heatmaps, whose values are lower in
 * proportion. The durations of the fixations are computed from the times of the samples: unchanged;</li>
 * <li>in the other files of the round, the heatmaps (heatmap*.csv, metrics*.png) always have 400 columns (cells of
 * width / 400 pixels of the scene). Before, the cells were width / 200 pixels when the JVM had less than 1 GiB of
 * memory (R7), so the resolution depended on the machine.</li>
 * </ul>
 * <p>
 * In memory, {@link FixationPoint} keeps the convention of version 1 (Y in "x", X in "y"), which the drawing code
 * relies on: the conversion happens when the file is written and read.
 */
public final class ReplayDataFormat {

    /**
     * Version written in the new files.
     */
    public static final int CURRENT_VERSION = 4;

    /**
     * Version of the files written without "formatVersion".
     */
    public static final int LEGACY_VERSION = 1;

    private ReplayDataFormat() {
    }

    /**
     * @param formatVersion the "formatVersion" of a file, null when absent
     * @return the version of the file
     */
    public static int versionOf(final Integer formatVersion) {
        return formatVersion == null ? LEGACY_VERSION : formatVersion;
    }

    /**
     * @return the sequences as written in a file of the current version
     */
    static List<LinkedList<FixationPoint>> toFile(final List<LinkedList<FixationPoint>> inMemory) {
        return swapCoordinates(inMemory);
    }

    /**
     * @return the sequences read from a file of the given version, in the convention used in memory; null when the file
     *   has none, as the replay received before this conversion existed (an empty list would change the replay)
     */
    public static ArrayList<LinkedList<FixationPoint>> fromFile(
        final int version, final List<LinkedList<FixationPoint>> inFile) {
        if (inFile == null) {
            return null;
        }
        return version >= 2 ? swapCoordinates(inFile) : new ArrayList<>(inFile);
    }

    private static ArrayList<LinkedList<FixationPoint>> swapCoordinates(final List<LinkedList<FixationPoint>> sequences) {
        final ArrayList<LinkedList<FixationPoint>> result = new ArrayList<>();
        for (final LinkedList<FixationPoint> sequence : sequences) {
            final LinkedList<FixationPoint> swapped = new LinkedList<>();
            for (final FixationPoint point : sequence) {
                swapped.add(new FixationPoint(point.getTimeGaze(), point.getGazeDuration(), point.getY(), point.getX()));
            }
            result.add(swapped);
        }
        return result;
    }
}
