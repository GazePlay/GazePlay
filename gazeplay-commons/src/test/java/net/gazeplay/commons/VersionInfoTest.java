package net.gazeplay.commons;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionInfoTest {

    void createMockManifest(final List<String> lines) throws IOException {
        new File("build/resources/test/META-INF").mkdir();
        final Path file = Paths.get("build/resources/test/META-INF/MANIFEST.MF");
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    void deleteMockManifest() {
        if (new File("build/resources/test/META-INF/MANIFEST.MF").isFile()) {
            new File("build/resources/test/META-INF/MANIFEST.MF").delete();
            new File("build/resources/test/META-INF").delete();
        }
    }

    @AfterEach
    void tearDown() {
        deleteMockManifest();
    }

    @Test
    void shouldFindTheDefaultVersionInfo() throws IOException {
        final List<String> lines = Arrays.asList("Implementation-Title: gazeplay", "Implementation-Version: 1.7", "Build-Time: 1234");
        createMockManifest(lines);

        final String result = VersionInfo.findVersionInfo();

        assertEquals("1.7 (1234)", result);
    }

    @Test
    void shouldReturnCurrentVersion() throws IOException {
        final List<String> lines = Arrays.asList("Implementation-Title: wrong-game", "Implementation-Version: 1.7", "Build-Time: 1234");
        createMockManifest(lines);

        final String result = VersionInfo.findVersionInfo();

        assertEquals("Current Version", result);
    }

    @Test
    void shouldFindVersionWithoutBuildTime() throws IOException {
        final List<String> lines = Arrays.asList("Implementation-Title: gazeplay", "Implementation-Version: 1.7");
        createMockManifest(lines);

        final String result = VersionInfo.findVersionInfo("gazeplay", false).get();

        assertEquals("1.7", result);
    }

    @Test
    void shouldReturnEmptyWithIncorrectManifest() throws IOException {
        final List<String> lines = Arrays.asList("Implementation-Title: wrong-game", "Implementation-Version: 1.7");
        createMockManifest(lines);

        final Optional<String> result = VersionInfo.findVersionInfo("gazeplay", false);

        assertTrue(result.isEmpty());
    }
}
