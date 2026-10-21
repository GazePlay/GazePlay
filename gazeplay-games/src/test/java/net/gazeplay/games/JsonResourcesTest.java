package net.gazeplay.games;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The JSON data files of the games (positions of Goose Game and Horses, levels of Dot to Dot, Sounds of Life, Spot the
 * Differences...) are read when a game starts: each one must be valid JSON, read with the parser the games use.
 */
class JsonResourcesTest {

    @Test
    void shouldParseEveryJsonDataFileOfTheGames() throws IOException {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(Path.of("src/main/resources/data"))) {
            files = walk.filter(path -> path.toString().endsWith(".json")).collect(Collectors.toList());
        }
        assertFalse(files.isEmpty());

        List<String> invalid = new ArrayList<>();
        for (Path file : files) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonObject() && !root.isJsonArray()) {
                    invalid.add(file + ": not an object nor an array");
                }
            } catch (RuntimeException e) {
                invalid.add(file + ": " + e.getMessage());
            }
        }
        assertEquals(List.of(), invalid);
    }
}
