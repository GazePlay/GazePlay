package net.gazeplay.commons.utils;

import lombok.*;

/**
 * A point of a fixation sequence ("fixationSequence" of replayData.json): where the gaze (or the mouse) stayed, from
 * when, and for how long.
 * <p>
 * In memory, {@code x} holds the vertical coordinate and {@code y} the horizontal one (convention of the drawing code,
 * see {@link net.gazeplay.commons.utils.stats.ReplayDataFormat}); the file has them the right way round in format
 * version 6.
 */
@Data
@AllArgsConstructor
public class FixationPoint {

    private long timeGaze;
    private long gazeDuration;
    private int x;
    private int y;

}
