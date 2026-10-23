package net.gazeplay;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Starts the real application in a child JVM, without any window (Monocle) and with its own user.home, lets it run for
 * a while, then stops it. Used by the launch tests ({@link LaunchSmokeTest}, {@link AllGamesLaunchTest}).
 */
final class GazePlayProcess {

    /**
     * Messages expected on a fresh user.home without eye tracker.
     */
    private static final List<String> EXPECTED_ERRORS = List.of(
        "Failure while loading Properties file", "NoSuchFileException", "WindowsException", "UnixException",
        "at java.base/", "at net.gazeplay.commons.configuration.ConfigurationSource");

    /**
     * @param exited true if GazePlay stopped by itself before the end of the delay
     * @param lines  the console output (the log of the application)
     */
    record Result(boolean exited, int exitValue, List<String> lines) {

        String log() {
            return String.join("\n", lines);
        }

        List<String> unexpectedErrors() {
            return lines.stream()
                .filter(line -> line.contains("Exception") || line.contains(" ERROR "))
                .filter(line -> EXPECTED_ERRORS.stream().noneMatch(line::contains))
                .collect(Collectors.toList());
        }
    }

    private GazePlayProcess() {
    }

    /**
     * @param home         an empty folder, used as user.home
     * @param arguments    the arguments of GazePlay
     * @param delaySeconds how long GazePlay runs before being stopped
     * @param mouseMode    true to configure the mouse mode; otherwise GazePlay starts with the Tobii driver, without
     *                     eye tracker (the driver copies its library in a folder shared by all the processes, outside
     *                     user.home: several processes started at the same time fail on it)
     */
    static Result run(File home, List<String> arguments, long delaySeconds, boolean mouseMode)
        throws IOException, InterruptedException {
        // Never the GazePlay folder of the user (in Documents, which does not depend on user.home on Windows)
        return run(home, arguments, delaySeconds, mouseMode,
            List.of("-Dgazeplay.folder=" + new File(home, "GazePlay").getAbsolutePath()));
    }

    /**
     * @param jvmOptions options of the JVM, which must keep GazePlay away from the folders of the user
     */
    static Result run(File home, List<String> arguments, long delaySeconds, boolean mouseMode, List<String> jvmOptions)
        throws IOException, InterruptedException {
        if (mouseMode) {
            File gazePlayFolder = new File(home, "GazePlay");
            if (!gazePlayFolder.isDirectory() && !gazePlayFolder.mkdirs()) {
                throw new IOException("Cannot create " + gazePlayFolder);
            }
            Files.writeString(new File(gazePlayFolder, "GazePlay.properties").toPath(), "EYE_TRACKER=mouse_control\n",
                StandardCharsets.UTF_8);
        }
        File argsFile = new File(home, "argsGazeplay.txt");
        Files.writeString(argsFile.toPath(), "gazeplay", StandardCharsets.UTF_8);
        File output = new File(home, "output.log");

        List<String> command = new ArrayList<>(List.of(
            new File(System.getProperty("java.home"), "bin/java").getPath(),
            "-cp", System.getProperty("java.class.path"),
            "-Duser.home=" + home.getAbsolutePath()));
        command.addAll(jvmOptions);
        command.addAll(List.of(
            "-D" + GazePlayArgs.ARGS_FILE_PROPERTY + "=" + argsFile.getAbsolutePath(),
            "-Dglass.platform=Monocle", "-Dmonocle.platform=Headless", "-Dprism.order=sw", "-Dprism.text=t2k",
            "--enable-native-access=ALL-UNNAMED", "--sun-misc-unsafe-memory-access=allow",
            GazePlayLauncher.class.getName()));
        command.addAll(arguments);
        // A fixed size avoids the full screen mode, which Monocle does not support before the window is shown
        command.addAll(List.of("--width", "1280", "--height", "800"));

        Process process = new ProcessBuilder(command)
            .directory(home)
            .redirectErrorStream(true)
            .redirectOutput(output)
            .start();
        boolean exited;
        try {
            exited = process.waitFor(delaySeconds, TimeUnit.SECONDS);
        } finally {
            // Also when the wait is interrupted (e.g. a test stopped early): the process must not outlive the test
            if (process.isAlive()) {
                process.destroyForcibly().waitFor(10, TimeUnit.SECONDS);
            }
        }

        List<String> lines = Files.readAllLines(output.toPath(), StandardCharsets.UTF_8);
        return new Result(exited, exited ? process.exitValue() : 0, lines);
    }
}
