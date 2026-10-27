package net.gazeplay.games.gazeplayEval;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * What happens during a session of an evaluation: the screens shown (with each attempt when a grid is done again),
 * the events (entries in and exits from the targets, selections, sounds, keys of the examiner...) and the positions of
 * the gaze and of the mouse. Without JavaFX: {@link EvalResultsWriter} writes it.
 * <p>
 * The times are in milliseconds since the start of the session ({@link #getStartTime()}, System.currentTimeMillis()).
 * The positions are in pixels of the game area.
 */
public final class EvalSessionRecorder {

    /**
     * Version of the files of results of GazePlay Eval written by {@link EvalResultsWriter} (session.json
     * "formatVersion"). To be increased at each change of their columns or of their meaning.
     */
    public static final int FORMAT_VERSION = 1;

    /** Sources of the events. */
    public static final String GAZE = "regard";
    public static final String MOUSE = "souris";
    public static final String KEYBOARD = "clavier";
    public static final String SYSTEM = "systeme";

    /**
     * Why a screen ended, as written in the results.
     */
    public enum EndReason {
        /** A grid: the number of selections to make was reached. */
        VALIDATED("validé"),
        /** A grid: its maximum time elapsed. */
        TIME_ELAPSED("temps écoulé"),
        /** A transition or an instruction: its delay elapsed. */
        DELAY_ELAPSED("délai écoulé"),
        /** The fixation cross or the start button was fixated. */
        FIXATION("fixation"),
        /** The examiner went to the next screen (space key). */
        EXAMINER("passé par l'examinateur"),
        /** The session was interrupted (game left). */
        INTERRUPTED("interrompu");

        @Getter
        private final String label;

        EndReason(String label) {
            this.label = label;
        }
    }

    /**
     * An event. {@code x} and {@code y} are null when the event has no position.
     */
    public record Event(long time, int screen, int attempt, String screenType, String name, String source,
                        String target, Double x, Double y, String detail) {
    }

    /**
     * A position of the gaze or of the mouse, and the target under it ("-" when none).
     */
    public record Sample(long time, int screen, int attempt, String source, double x, double y, String target) {
    }

    /**
     * A selection in a grid.
     *
     * @param position the cell, e.g. "L1C2" (row 1, column 2)
     */
    public record Selection(long time, String position, String label, boolean correct, String source) {
    }

    /**
     * A screen shown once (an attempt).
     */
    @Getter
    public static final class ScreenRecord {
        private final int screen;
        private final int attempt;
        private final String type;
        private final long start;
        private long end = -1;
        private EndReason endReason;
        // Grids only
        private int rows;
        private int columns;
        private String rule = "";
        private int toSelect;
        private final List<String> layout = new ArrayList<>();
        private final List<String> expected = new ArrayList<>();
        private final List<Selection> selections = new ArrayList<>();
        private int countedSelections;

        ScreenRecord(int screen, int attempt, String type, long start) {
            this.screen = screen;
            this.attempt = attempt;
            this.type = type;
            this.start = start;
        }

        public boolean isGrid() {
            return rows > 0;
        }

        public long correctCount() {
            return selections.stream().filter(Selection::correct).count();
        }

        public long wrongCount() {
            return selections.size() - correctCount();
        }
    }

    private final LongSupplier clock;

    @Getter
    private final String evaluationName;

    @Getter
    private final long startTime;

    @Getter
    private long endTime = -1;

    @Getter
    private boolean completed;

    @Getter
    private final Map<String, String> participant = new LinkedHashMap<>();

    /** Settings and context of the session, written in session.json. */
    @Getter
    private final Map<String, Object> context = new LinkedHashMap<>();

    private final List<ScreenRecord> screens = new ArrayList<>();
    private final List<Event> events = new ArrayList<>();
    private final List<Sample> samples = new ArrayList<>();
    private ScreenRecord current;

    public EvalSessionRecorder(String evaluationName, LongSupplier clock) {
        this.evaluationName = evaluationName;
        this.clock = clock;
        this.startTime = clock.getAsLong();
        event("debut_session", SYSTEM, "", null, null, "");
    }

    /**
     * @return milliseconds since the start of the session
     */
    public long elapsed() {
        return clock.getAsLong() - startTime;
    }

    /**
     * @return milliseconds since the start of the session, of a time of System.currentTimeMillis() (0 or less: now)
     */
    public long elapsedAt(long absoluteTime) {
        return absoluteTime > 0 ? absoluteTime - startTime : elapsed();
    }

    public void beginScreen(int screen, int attempt, String type) {
        current = new ScreenRecord(screen, attempt, type, elapsed());
        screens.add(current);
        event("debut_ecran", SYSTEM, "", null, null, "");
    }

    /**
     * Describes the grid of the current screen.
     *
     * @param layout   the label of each cell, in the order shown (row by row), "-" for an empty slot
     * @param expected the labels of the correct answers
     */
    public void describeGrid(int rows, int columns, String rule, int toSelect, List<String> layout, List<String> expected) {
        current.rows = rows;
        current.columns = columns;
        current.rule = rule;
        current.toSelect = toSelect;
        current.layout.addAll(layout);
        current.expected.addAll(expected);
    }

    /**
     * Records a selection in the grid of the current screen.
     *
     * @param counted whether it counts to end the screen (selection rule)
     */
    public void select(String position, String label, boolean correct, String source, boolean counted, double x, double y) {
        current.selections.add(new Selection(elapsed(), position, label, correct, source));
        if (counted) {
            current.countedSelections++;
        }
        event("selection", source, position + ":" + label, x, y, correct ? "bonne réponse" : "mauvaise réponse");
    }

    public void endScreen(EndReason reason) {
        if (current == null || current.end >= 0) {
            return;
        }
        current.end = elapsed();
        current.endReason = reason;
        event("fin_ecran", SYSTEM, "", null, null, reason.getLabel());
    }

    /**
     * Records an event of the current screen (or of the session, before the first screen).
     */
    public void event(String name, String source, String target, Double x, Double y, String detail) {
        eventAt(elapsed(), name, source, target, x, y, detail);
    }

    /**
     * Same as {@link #event}, at a given time since the start of the session (e.g. the acquisition time of a gaze
     * sample).
     */
    public void eventAt(long time, String name, String source, String target, Double x, Double y, String detail) {
        int screen = current == null ? 0 : current.screen;
        int attempt = current == null ? 0 : current.attempt;
        String type = current == null ? "" : current.type;
        events.add(new Event(time, screen, attempt, type, name, source, target, x, y, detail));
    }

    public void sample(long time, String source, double x, double y, String target) {
        int screen = current == null ? 0 : current.screen;
        int attempt = current == null ? 0 : current.attempt;
        samples.add(new Sample(time, screen, attempt, source, x, y, target));
    }

    /**
     * Ends the session.
     *
     * @param completed false when it was interrupted
     */
    public void finish(boolean completed) {
        if (endTime >= 0) {
            return;
        }
        if (current != null && current.end < 0) {
            endScreen(EndReason.INTERRUPTED);
        }
        this.completed = completed;
        endTime = elapsed();
        event("fin_session", SYSTEM, "", null, null, completed ? "terminée" : "interrompue");
    }

    public ScreenRecord getCurrentScreen() {
        return current;
    }

    public List<ScreenRecord> getScreens() {
        return Collections.unmodifiableList(screens);
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public List<Sample> getSamples() {
        return Collections.unmodifiableList(samples);
    }

    /**
     * @return the samples of a screen attempt
     */
    public List<Sample> samplesOf(int screen, int attempt) {
        return samples.stream().filter(s -> s.screen() == screen && s.attempt() == attempt).toList();
    }
}
