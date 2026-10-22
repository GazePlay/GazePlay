package net.gazeplay.commons.utils.stats;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.gazeplay.commons.utils.FixationPoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReplayDataFormatTest {

    private static final java.lang.reflect.Type SEQUENCES = new TypeToken<ArrayList<LinkedList<FixationPoint>>>() {
    }.getType();

    @Test
    void shouldReadTheFilesWithoutVersionAsVersion1() {
        assertEquals(1, ReplayDataFormat.versionOf(null));
        assertEquals(2, ReplayDataFormat.versionOf(2));
    }

    /**
     * A historical file (version 1) already holds the convention of the memory: Y in "x", X in "y".
     */
    @Test
    void shouldKeepTheCoordinatesOfAVersion1File() {
        List<LinkedList<FixationPoint>> inFile = new Gson().fromJson(
            "[[],[{\"timeGaze\":1100,\"gazeDuration\":300,\"x\":40,\"y\":10}]]", SEQUENCES);

        FixationPoint point = ReplayDataFormat.fromFile(1, inFile).get(1).get(0);

        assertEquals(40, point.getX());
        assertEquals(10, point.getY());
    }

    /**
     * Versions 2 and 3 have the same structure: "x" holds X and "y" holds Y in both.
     */
    @Test
    void shouldReadTheCoordinatesOfVersions2And3TheSameWay() {
        List<LinkedList<FixationPoint>> inFile = new Gson().fromJson(
            "[[],[{\"timeGaze\":1100,\"gazeDuration\":300,\"x\":10,\"y\":40}]]", SEQUENCES);

        assertEquals(ReplayDataFormat.fromFile(2, inFile), ReplayDataFormat.fromFile(3, inFile));
        assertEquals(40, ReplayDataFormat.fromFile(3, inFile).get(1).get(0).getX());
    }

    @Test
    void shouldReadBackWhatIsWritten() {
        LinkedList<FixationPoint> gaze = new LinkedList<>(List.of(new FixationPoint(1100, 300, 40, 10)));
        JsonObject json = ReplayDataWriter.toJson(new ReplayDataWriter.Round(0, "", "", 0, "", 0, 0, 0, 0, 0,
            null, null, 0, List.of(), List.of(new LinkedList<>(), gaze), new JsonArray()));

        List<LinkedList<FixationPoint>> inFile = new Gson().fromJson(json.get("fixationSequence"), SEQUENCES);
        List<LinkedList<FixationPoint>> read = ReplayDataFormat.fromFile(
            ReplayDataFormat.versionOf(json.get("formatVersion").getAsInt()), inFile);

        assertEquals(List.of(new LinkedList<>(), gaze), read);
    }

    @Test
    void shouldAcceptAMissingSequence() {
        assertNull(ReplayDataFormat.fromFile(2, null));
    }
}
