package net.gazeplay.commons.utils.stats;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.gazeplay.commons.utils.FixationPoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplayDataWriterTest {

    private static ReplayDataWriter.Round round(List<LinkedList<FixationPoint>> fixationSequence) {
        JsonArray coordinates = new JsonArray();
        JsonObject sample = new JsonObject();
        sample.addProperty("X", 0.25);
        sample.addProperty("Y", 0.5);
        sample.addProperty("time", 120);
        sample.addProperty("event", "gaze");
        coordinates.add(sample);
        return new ReplayDataWriter.Round(3.5, "Divisor", "IntGameVariant:2", 1000L, "16:9", 1.6,
            2, 5, 1, 0, 1000L, null, 4500L, List.of(1200L, 3300L), fixationSequence, coordinates);
    }

    /**
     * Exact text of replayData.json, key order included: persistent format, version 3. Compared to version 1, only
     * "formatVersion" is added in front and "x"/"y" of the fixation points are no longer swapped (the point in memory
     * holds Y in "x": 40 is the Y coordinate). Version 3 changes the meaning of the gaze times, not the structure.
     */
    @Test
    void shouldWriteTheReplayDataFormat() {
        List<LinkedList<FixationPoint>> fixationSequence = new ArrayList<>(List.of(
            new LinkedList<>(), new LinkedList<>(List.of(new FixationPoint(1100, 300, 40, 10)))));

        assertEquals("{\"formatVersion\":3,\"gameSeed\":3.5,\"gameName\":\"Divisor\",\"gameVariant\":\"IntGameVariant:2\","
                + "\"gameStartedTime\":1000,\"screenAspectRatio\":\"16:9\",\"sceneAspectRatio\":1.6,"
                + "\"statsNbGoalsReached\":2,\"statsNbGoalsToReach\":5,\"statsNbUnCountedGoalsReached\":1,"
                + "\"statsNbMistakes\":0,\"lifeCycle\":{\"startTime\":1000,\"stopTime\":null},"
                + "\"roundsDurationReport\":{\"totalAdditiveDuration\":4500,\"durationBetweenGoals\":[1200,3300]},"
                + "\"fixationSequence\":[[],[{\"timeGaze\":1100,\"gazeDuration\":300,\"x\":10,\"y\":40}]],"
                + "\"coordinatesAndTimeStamp\":[{\"X\":0.25,\"Y\":0.5,\"time\":120,\"event\":\"gaze\"}]}",
            ReplayDataWriter.toJson(round(fixationSequence)).toString());
    }

    /**
     * Regression test: with the fixation sequence disabled in the settings, the conversion used to fail, leaving an
     * empty replayData.json and no heatmap.csv nor metrics images.
     */
    @Test
    void shouldWriteAnEmptyFixationSequenceWhenItIsDisabled() {
        JsonObject json = ReplayDataWriter.toJson(round(null));

        // Mouse and gaze lists, empty: the shape expected by the replay
        assertEquals("[[],[]]", json.getAsJsonArray("fixationSequence").toString());
    }
}
