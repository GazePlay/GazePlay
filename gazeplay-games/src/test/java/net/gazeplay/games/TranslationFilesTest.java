package net.gazeplay.games;

import net.gazeplay.commons.utils.multilinguism.I18N;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integrity of the translation files (CSV read by {@link I18N}) shipped with the games and the application.
 * <p>
 * Spreadsheet editors can silently shift the columns of these files: in April 2024 the Greek question mark ";" of
 * whereIsTheShape.csv was turned into a column separator, shifting every language after "ell" and making the game
 * crash at load time.
 */
class TranslationFilesTest {

    private static final Path GAMES_RESOURCES = Paths.get(System.getProperty("user.dir"), "src", "main", "resources");
    private static final Path CORE_RESOURCES = Paths.get(System.getProperty("user.dir"), "..", "gazeplay-core", "src", "main", "resources");

    static Stream<Path> translationFiles() throws IOException {
        List<Path> csvFiles = new ArrayList<>();
        for (Path resources : List.of(GAMES_RESOURCES, CORE_RESOURCES)) {
            try (Stream<Path> files = Files.walk(resources)) {
                List<Path> found = files
                    .filter(path -> path.toString().endsWith(".csv"))
                    .filter(TranslationFilesTest::isTranslationFile)
                    .collect(Collectors.toList());
                assertFalse(found.isEmpty(), "No translation file found in " + resources);
                csvFiles.addAll(found);
            }
        }
        return csvFiles.stream();
    }

    private static boolean isTranslationFile(Path path) {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            // The header lists the languages, after an empty cell or "AVAILABLE LANGUAGES" (multilinguism.csv)
            return !lines.isEmpty() && (lines.get(0).startsWith(",") || lines.get(0).startsWith("AVAILABLE LANGUAGES,"));
        } catch (IOException e) {
            return false;
        }
    }

    @ParameterizedTest
    @MethodSource("translationFiles")
    void shouldNotHaveMoreColumnsThanTheHeader(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        // Same splitting as I18N
        int headerColumns = lines.get(0).split(",").length;

        List<String> malformedLines = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            if (line.split(",").length > headerColumns) {
                malformedLines.add(line.split(",")[0]);
            }
        }

        assertTrue(malformedLines.isEmpty(), file + " has lines with more columns than its header: " + malformedLines);
    }

    /**
     * Keys known to lack an english translation (they are displayed as "[untranslated!] key"). To be completed by
     * translators, then removed from this list.
     */
    private static final Set<String> KNOWN_KEYS_WITHOUT_ENGLISH = Set.of("Petit Ours Brun");

    @ParameterizedTest
    @MethodSource("translationFiles")
    void shouldTranslateEveryKeyInEnglish(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        I18N i18n = new I18N(file.toString());

        List<String> untranslatedKeys = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            String key = line.split(",")[0].strip();
            // multilinguism.csv uses blank lines and "# SECTION" lines to organise the keys
            if (key.isEmpty() || key.startsWith("#") || KNOWN_KEYS_WITHOUT_ENGLISH.contains(key)) {
                continue;
            }
            String translation = i18n.translate(key, "eng");
            if (translation == null || translation.isBlank()) {
                untranslatedKeys.add(key);
            }
        }

        assertTrue(untranslatedKeys.isEmpty(), file + " has keys without english translation: " + untranslatedKeys);
    }
}
