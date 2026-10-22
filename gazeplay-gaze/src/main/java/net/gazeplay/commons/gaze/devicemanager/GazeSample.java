package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Point2D;

/**
 * A position given by a gaze source, in screen coordinates.
 *
 * @param time           acquisition time (System.currentTimeMillis()), taken when GazePlay receives the position: the
 *                       Tobii wrapper gives no timestamp of the eye tracker (see CLAUDE.md, "Flux du regard"). It is
 *                       the "time" of the gaze samples of replayData.json since format version 3
 * @param screenPosition position on the screen
 * @param source         gaze, or mouse used as a gaze (replay of the recorded mouse samples)
 */
public record GazeSample(long time, Point2D screenPosition, Source source) {

    /**
     * Source of a sample. Its name is the "event" value of the raw samples of replayData.json ("gaze", "mouse").
     */
    public enum Source {
        GAZE("gaze"), MOUSE("mouse");

        private final String eventName;

        Source(final String eventName) {
            this.eventName = eventName;
        }

        public String eventName() {
            return eventName;
        }

        /**
         * @param eventName "gaze" or "mouse"; any other value is a mouse sample, as the dispatch always treated it
         */
        public static Source fromEventName(final String eventName) {
            return GAZE.eventName.equals(eventName) ? GAZE : MOUSE;
        }
    }

    public static GazeSample now(final Point2D screenPosition, final Source source) {
        return new GazeSample(System.currentTimeMillis(), screenPosition, source);
    }
}
