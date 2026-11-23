package net.gazeplay.commons.utils.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.LinkedList;
import java.util.List;

/**
 * Content of replayData.json, written at the end of each round: free of JavaFX, extracted from {@code Stats}.
 * Persistent format, read by the replay of the games and by researchers: version {@link ReplayDataFormat#CURRENT_VERSION},
 * written in "formatVersion"; the other keys and their order are those of version 1 (structure pinned by
 * {@code StatsTest.shouldSaveStats} and {@code ReplayDataWriterTest}).
 */
final class ReplayDataWriter {

    /**
     * Data of a round.
     *
     * @param fixationSequence mouse then gaze fixation sequences (convention of the memory, see
     *                         {@link ReplayDataFormat}), null when disabled in the settings
     * @param coordinates      raw samples ("coordinatesAndTimeStamp")
     * @param settings         the settings of the game at its start ("settings"), null when unknown (not written)
     * @param selectionDelays  the delays before a selection of the round, each its start and its end in ms since
     *                         "gameStartedTime", as the "time" of the samples ("selectionDelays"), null when unknown (not
     *                         written)
     */
    record Round(
        double gameSeed,
        String gameName,
        String gameVariant,
        long gameStartedTime,
        String screenAspectRatio,
        double sceneAspectRatio,
        int nbGoalsReached,
        int nbGoalsToReach,
        int nbUnCountedGoalsReached,
        int nbMistakes,
        Long lifeCycleStartTime,
        Long lifeCycleStopTime,
        Long lifeCycleMenuDuration,
        long totalAdditiveDuration,
        List<Long> durationBetweenGoals,
        List<LinkedList<FixationPoint>> fixationSequence,
        JsonArray coordinates,
        GameSettings settings,
        List<long[]> selectionDelays
    ) {

        /**
         * A round without its settings nor its delays before a selection (tests of the other keys).
         */
        Round(final double gameSeed, final String gameName, final String gameVariant, final long gameStartedTime,
              final String screenAspectRatio, final double sceneAspectRatio, final int nbGoalsReached,
              final int nbGoalsToReach, final int nbUnCountedGoalsReached, final int nbMistakes,
              final Long lifeCycleStartTime, final Long lifeCycleStopTime, final Long lifeCycleMenuDuration,
              final long totalAdditiveDuration, final List<Long> durationBetweenGoals,
              final List<LinkedList<FixationPoint>> fixationSequence, final JsonArray coordinates) {
            this(gameSeed, gameName, gameVariant, gameStartedTime, screenAspectRatio, sceneAspectRatio, nbGoalsReached,
                nbGoalsToReach, nbUnCountedGoalsReached, nbMistakes, lifeCycleStartTime, lifeCycleStopTime,
                lifeCycleMenuDuration, totalAdditiveDuration, durationBetweenGoals, fixationSequence, coordinates, null,
                null);
        }
    }

    private ReplayDataWriter() {
    }

    /**
     * @return the content of replayData.json for a round, in the order of the keys of the format
     */
    static JsonObject toJson(final Round round) {
        final Gson gson = new GsonBuilder().create();
        // The fixation sequence is null when it is disabled in the settings: the conversion used to fail ("Not a JSON
        // Array: null"), leaving an empty replayData.json and no heatmap.csv nor metrics images. Two empty lists
        // (mouse, gaze) keep the shape expected by the replay, as for a round without any sample
        final JsonArray fixationSequenceArray = gson.toJsonTree(
            round.fixationSequence() == null ? List.of(List.of(), List.of())
                : ReplayDataFormat.toFile(round.fixationSequence())).getAsJsonArray();
        final JsonArray durationBetweenGoalsArray = gson.toJsonTree(round.durationBetweenGoals()).getAsJsonArray();

        final JsonObject json = new JsonObject();
        json.addProperty("formatVersion", ReplayDataFormat.CURRENT_VERSION);
        json.addProperty("gameSeed", round.gameSeed());
        json.addProperty("gameName", round.gameName());
        json.addProperty("gameVariant", round.gameVariant());
        json.addProperty("gameStartedTime", round.gameStartedTime());
        json.addProperty("screenAspectRatio", round.screenAspectRatio());
        json.addProperty("sceneAspectRatio", round.sceneAspectRatio());
        json.addProperty("statsNbGoalsReached", round.nbGoalsReached());
        json.addProperty("statsNbGoalsToReach", round.nbGoalsToReach());
        json.addProperty("statsNbUnCountedGoalsReached", round.nbUnCountedGoalsReached());
        json.addProperty("statsNbMistakes", round.nbMistakes());

        final JsonObject lifeCycleObject = new JsonObject();
        lifeCycleObject.addProperty("startTime", round.lifeCycleStartTime());
        lifeCycleObject.addProperty("stopTime", round.lifeCycleStopTime());
        // Since the format 6: the time spent in the menu (Esc), not counted in the durations
        lifeCycleObject.addProperty("menuDuration", round.lifeCycleMenuDuration());
        json.add("lifeCycle", lifeCycleObject);

        final JsonObject roundsDurationReportObject = new JsonObject();
        roundsDurationReportObject.addProperty("totalAdditiveDuration", round.totalAdditiveDuration());
        roundsDurationReportObject.add("durationBetweenGoals", durationBetweenGoalsArray);
        json.add("roundsDurationReport", roundsDurationReportObject);

        // Since 23/11/2026 (format 6): the settings of the game, and the delays before a selection, during which the
        // gaze and the mouse are recorded though nothing can be chosen
        if (round.settings() != null) {
            final JsonObject settingsObject = new JsonObject();
            settingsObject.addProperty("fixationLength", round.settings().fixationLength());
            settingsObject.addProperty("selectionDelay", round.settings().selectionDelay());
            settingsObject.addProperty("inputMode", round.settings().inputMode());
            settingsObject.addProperty("movingFixationKept", round.settings().movingFixationKept());
            settingsObject.addProperty("animationSpeedRatio", round.settings().animationSpeedRatio());
            json.add("settings", settingsObject);
        }
        if (round.selectionDelays() != null) {
            final JsonArray delaysArray = new JsonArray();
            for (final long[] delay : round.selectionDelays()) {
                final JsonObject delayObject = new JsonObject();
                delayObject.addProperty("start", delay[0]);
                delayObject.addProperty("end", delay[1]);
                delaysArray.add(delayObject);
            }
            json.add("selectionDelays", delaysArray);
        }

        json.add("fixationSequence", fixationSequenceArray);
        json.add("coordinatesAndTimeStamp", round.coordinates());
        return json;
    }
}
