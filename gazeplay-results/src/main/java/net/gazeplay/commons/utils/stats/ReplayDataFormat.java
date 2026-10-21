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
 * In memory, {@link FixationPoint} keeps the convention of version 1 (Y in "x", X in "y"), which the drawing code
 * relies on: the conversion happens when the file is written and read.
 */
public final class ReplayDataFormat {

    public static final int CURRENT_VERSION = 2;

    /**
     * Version of the files written without "formatVersion".
     */
    public static final int LEGACY_VERSION = 1;

    private ReplayDataFormat() {
    }

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
     * @return the sequences read from a file of the given version, in the convention used in memory
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
