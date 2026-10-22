package net.gazeplay;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Launches the real application in a child JVM, without any window (Monocle) and with a temporary user.home, then
 * checks that it is still running after a while and that it logged no unexpected exception.
 * <p>
 * Run by the {@code smokeTest} Gradle task (excluded from {@code test}). It covers the assembly of the application
 * (command line, composition root, JavaFX start, game catalog) but not the rendering on a real screen, nor the eye
 * trackers.
 */
@Tag("smoke")
class LaunchSmokeTest {

    private static final long RUNNING_DELAY_SECONDS = 25;

    /**
     * Messages expected on a fresh user.home without eye tracker.
     */
    private static final List<String> EXPECTED_ERRORS = List.of(
        "Failure while loading Properties file", "NoSuchFileException", "WindowsException", "UnixException",
        "at java.base/", "at net.gazeplay.commons.configuration.ConfigurationSource");

    @ParameterizedTest
    @ValueSource(strings = {
        "--default-user",
        "--default-user --game WhereIsTheShape --variant DimensionDifficultyGameVariant:2:2:Easy"
    })
    void shouldStartAndKeepRunning(String arguments) throws IOException, InterruptedException {
        // Folder recreated by the smokeTest task at each run: a killed process can keep its files locked for a while
        // on Windows, which JUnit's @TempDir does not tolerate
        File home = new File(System.getProperty("smoke.home"), Integer.toHexString(arguments.hashCode()));
        assertTrue(home.mkdirs(), "Cannot create " + home);
        File argsFile = new File(home, "argsGazeplay.txt");
        Files.writeString(argsFile.toPath(), "gazeplay", StandardCharsets.UTF_8);
        File output = new File(home, "output.log");

        List<String> command = new ArrayList<>(List.of(
            new File(System.getProperty("java.home"), "bin/java").getPath(),
            "-cp", System.getProperty("java.class.path"),
            "-Duser.home=" + home.getAbsolutePath(),
            "-D" + GazePlayArgs.ARGS_FILE_PROPERTY + "=" + argsFile.getAbsolutePath(),
            "-Dglass.platform=Monocle", "-Dmonocle.platform=Headless", "-Dprism.order=sw", "-Dprism.text=t2k",
            "--enable-native-access=ALL-UNNAMED", "--sun-misc-unsafe-memory-access=allow",
            GazePlayLauncher.class.getName()));
        command.addAll(Arrays.asList(arguments.split(" ")));
        // A fixed size avoids the full screen mode, which Monocle does not support before the window is shown
        command.addAll(List.of("--width", "1280", "--height", "800"));

        Process process = new ProcessBuilder(command)
            .directory(home)
            .redirectErrorStream(true)
            .redirectOutput(output)
            .start();
        boolean exited = process.waitFor(RUNNING_DELAY_SECONDS, TimeUnit.SECONDS);
        if (!exited) {
            process.destroyForcibly().waitFor(10, TimeUnit.SECONDS);
        }

        List<String> lines = Files.readAllLines(output.toPath(), StandardCharsets.UTF_8);
        String log = String.join("\n", lines);
        assertFalse(exited, "GazePlay stopped with exit code " + (exited ? process.exitValue() : "") + ":\n" + log);
        assertTrue(log.contains("Games found : "), "The game catalog was not loaded:\n" + log);
        List<String> unexpected = lines.stream()
            .filter(line -> line.contains("Exception") || line.contains(" ERROR "))
            .filter(line -> EXPECTED_ERRORS.stream().noneMatch(line::contains))
            .collect(Collectors.toList());
        assertTrue(unexpected.isEmpty(), "Unexpected errors:\n" + String.join("\n", unexpected));
    }
}
