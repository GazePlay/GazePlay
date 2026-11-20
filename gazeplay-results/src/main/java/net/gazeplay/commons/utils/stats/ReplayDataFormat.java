package net.gazeplay.commons.utils.stats;

import net.gazeplay.commons.utils.FixationPoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Versions of replayData.json. Two versions only (decision of the maintainer, 20/11/2026): version 1, written by the
 * published versions of GazePlay up to 1.11 (no "formatVersion"), and version 6, written by GazePlay Learning 2.0
 * ("formatVersion": 6). The numbers 2 to 5 were steps of the development, written only by beta versions whose files
 * are not kept; version 6 includes all their changes. The files are never rewritten: a reader must look at
 * "formatVersion" to interpret them. Differences of version 6 (see MODERNIZATION.md, steps 2i, 2v, 2aj, 2ap, 2aq):
 * <ul>
 * <li>"coordinatesAndTimeStamp" only holds the samples of the round (version 1: also the previous rounds, R5), and
 * the mouse samples are the movements only (version 1: also the button events, R1);</li>
 * <li>"fixationSequence": "x" is the horizontal and "y" the vertical coordinate (version 1: swapped, R11), and the
 * durations are those of the fixations (version 1: twice the time elapsed, R2);</li>
 * <li>the "time" of the gaze samples of "coordinatesAndTimeStamp", and the "timeGaze" (hence the durations) of the
 * gaze fixation sequence, are the times the samples were acquired, not the times they were handled on the JavaFX
 * thread (version 1: samples queued while this thread was busy got the same time, and a later one). Each source
 * ("gaze", "mouse") stays in increasing time order, but a mouse sample can precede a gaze sample of a slightly earlier
 * time in the array;</li>
 * <li>the gaze samples of a Tobii eye tracker are the new measures only (version 1: the position read every 10 ms,
 * repeated when the eye tracker had sent nothing new, about 70 % of the samples with an Eye Tracker 5, R6): about 3
 * times fewer gaze samples in "coordinatesAndTimeStamp" and in the gaze heatmaps, whose values are lower in
 * proportion;</li>
 * <li>in the other files of the round, the heatmaps (heatmap.csv and metricsMouseAndGaze.png, mouse and gaze
 * together) are also written for each source, a heatmap and a metrics image of the gaze and of the mouse (version
 * 1: the matrix of both sources in every file, R1), and always have 400 columns, cells of width / 400 pixels of the scene
 * (version 1: width / 200 when the JVM had less than 1 GiB of memory, R7);</li>
 * <li>"statsNbMistakes" is the number of mistakes of the round: 0 for a game that counts the mistakes when there is
 * none, -1 only for a game that does not count them (version 1: N mistakes were written N - 1, -1 for none, 0 for
 * one, R14; a reader of a file of version 1 must add 1 to a value of 0 or more, except for the former "Apprends les
 * Maths", which counted right; -1 means no mistake, or a game that did not count them);</li>
 * <li>the time spent with the menu of the game screen open (Esc) is not counted in the durations. "lifeCycle" has
 * "menuDuration", the time spent in the menu between "startTime" and "stopTime" (times of the clock): the duration of
 * the game is "stopTime" - "startTime" - "menuDuration". The durations of the rounds ("roundsDurationReport":
 * "totalAdditiveDuration", "durationBetweenGoals") do not include the menu time. The "time" of the samples and the
 * "timeGaze" of the fixations are times of the clock: nothing is recorded while the menu is open, which leaves a gap.
 * In version 1, the durations included the time spent in the menu, which cannot be known afterwards.</li>
 * </ul>
 * <p>
 * In memory, {@link FixationPoint} keeps the convention of version 1 (Y in "x", X in "y"), which the drawing code
 * relies on: the conversion happens when the file is written and read.
 */
public final class ReplayDataFormat {

    /**
     * Version written in the new files.
     */
    public static final int CURRENT_VERSION = 6;

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
