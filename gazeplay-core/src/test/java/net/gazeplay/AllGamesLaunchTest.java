package net.gazeplay;

import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.NoVariantGenerator;
import net.gazeplay.gameslocator.GameSpecSourceInstantiator;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Launches each game of the catalog in its own JVM, as the menu does ({@code --game} and {@code --variant}), with the
 * first of its variants, then checks that the game was launched, that it is still running after a while and that it
 * logged no unexpected error. Most games have no other test: a game broken at its start would go unnoticed.
 * <p>
 * Run by the {@code allGamesTest} Gradle task (excluded from {@code test} and {@code smokeTest}): several games run at
 * the same time, a few minutes in all. In mouse mode, nobody plays: only the start of each game and its first seconds
 * are checked.
 */
@Tag("all-games")
class AllGamesLaunchTest {

    private static final long RUNNING_DELAY_SECONDS = 20;

    private static final int PARALLEL_LAUNCHES = 4;

    private record Launch(String variant, GazePlayProcess.Result result) {
    }

    @TestFactory
    List<DynamicTest> eachGameShouldStartAndKeepRunning() throws Exception {
        // allGames.only (a name code) and allGames.repeat: one game launched several times, for an occasional failure
        String only = System.getProperty("allGames.only", "");
        int repeat = Integer.parseInt(System.getProperty("allGames.repeat", "1"));
        List<GameSpec> gameSpecs = loadCatalog().stream()
            .filter(gameSpec -> only.isEmpty() || only.equals(gameSpec.getGameSummary().getNameCode()))
            .toList();
        assertFalse(gameSpecs.isEmpty(), "Empty game catalog, or no game " + only);

        ExecutorService executor = Executors.newFixedThreadPool(PARALLEL_LAUNCHES);
        try {
            Map<String, Future<Launch>> launches = new LinkedHashMap<>();
            for (int run = 1; run <= repeat; run++) {
                for (GameSpec gameSpec : gameSpecs) {
                    String nameCode = gameSpec.getGameSummary().getNameCode();
                    String variant = firstVariant(gameSpec);
                    List<String> arguments = new ArrayList<>(List.of("--default-user", "--game", nameCode));
                    if (variant != null) {
                        arguments.addAll(List.of("--variant", variant));
                    }
                    // Folder recreated by the allGamesTest task at each run (see LaunchSmokeTest). Numbered: some
                    // name codes are no valid folder names (e.g. "Math101: Addition")
                    File home = new File(System.getProperty("smoke.home"), "game-" + launches.size());
                    assertTrue(home.mkdirs(), "Cannot create " + home);
                    launches.put(repeat == 1 ? nameCode : nameCode + " #" + run, executor.submit(
                        () -> new Launch(variant, GazePlayProcess.run(home, arguments, RUNNING_DELAY_SECONDS, true))));
                }
            }

            List<DynamicTest> tests = new ArrayList<>();
            for (Map.Entry<String, Future<Launch>> entry : launches.entrySet()) {
                String testName = entry.getKey();
                String nameCode = testName.replaceFirst(" #\\d+$", "");
                Launch launch = entry.getValue().get();
                tests.add(dynamicTest(testName + " (" + launch.variant() + ")", () -> check(nameCode, launch)));
            }
            return tests;
        } finally {
            // Interrupts the launches still running: GazePlayProcess then stops their processes
            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    private static void check(String nameCode, Launch launch) {
        if (launch.variant() != null) {
            // The menu gives the variant to the JVM of the game as text: it must come back as the same variant
            IGameVariant parsed = IGameVariant.toGameVariant(launch.variant());
            assertNotNull(parsed, "Variant not recognized from its text: " + launch.variant());
            assertEquals(launch.variant(), parsed.toString(), "Variant changed by its text");
        }
        GazePlayProcess.Result result = launch.result();
        assertFalse(result.exited(), "GazePlay stopped with exit code " + result.exitValue() + ":\n" + result.log());
        assertTrue(result.log().contains("Game launched : " + nameCode + ","),
            "The game was not launched:\n" + result.log());
        List<String> unexpected = result.unexpectedErrors();
        assertTrue(unexpected.isEmpty(), "Unexpected errors:\n" + String.join("\n", unexpected));
    }

    /**
     * @return the text of the first variant (in alphabetical order, for a stable choice), or null for a game without
     * variants
     */
    private static String firstVariant(GameSpec gameSpec) {
        if (gameSpec.getGameVariantGenerator() instanceof NoVariantGenerator) {
            return null;
        }
        return gameSpec.getGameVariantGenerator().getVariants().stream()
            .map(Object::toString)
            .min(Comparator.naturalOrder())
            .orElse(null);
    }

    /**
     * The games of games-catalog.txt, as CatalogBasedGamesLocator reads them.
     */
    @SuppressWarnings("rawtypes")
    private static List<GameSpec> loadCatalog() throws Exception {
        List<Class> classes = new ArrayList<>();
        try (InputStream catalog = Objects.requireNonNull(
            AllGamesLaunchTest.class.getClassLoader().getResourceAsStream("games-catalog.txt"))) {
            for (String className : new String(catalog.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                if (!className.isBlank()) {
                    classes.add(Class.forName(className.trim()));
                }
            }
        }
        List<GameSpec> gameSpecs = new ArrayList<>();
        for (GameSpecSource source : new GameSpecSourceInstantiator().instantiateGameSpecSources(classes)) {
            gameSpecs.add(source.getGameSpec());
        }
        return gameSpecs;
    }
}
