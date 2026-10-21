package net.gazeplay.commons.utils.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.LinkedList;
import java.util.List;

/**
 * Content of replayData.json, written at the end of each round: free of JavaFX, extracted from {@link Stats}.
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
        long totalAdditiveDuration,
        List<Long> durationBetweenGoals,
        List<LinkedList<FixationPoint>> fixationSequence,
        JsonArray coordinates
    ) {
    }

    private ReplayDataWriter() {
    }

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
        json.add("lifeCycle", lifeCycleObject);

        final JsonObject roundsDurationReportObject = new JsonObject();
        roundsDurationReportObject.addProperty("totalAdditiveDuration", round.totalAdditiveDuration());
        roundsDurationReportObject.add("durationBetweenGoals", durationBetweenGoalsArray);
        json.add("roundsDurationReport", roundsDurationReportObject);

        json.add("fixationSequence", fixationSequenceArray);
        json.add("coordinatesAndTimeStamp", round.coordinates());
        return json;
    }
}
