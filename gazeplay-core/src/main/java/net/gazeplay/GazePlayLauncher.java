package net.gazeplay;

import net.gazeplay.commons.VersionInfo;
import net.gazeplay.commons.threads.CustomThreadFactory;
import net.gazeplay.commons.threads.GroupingThreadFactory;
import net.gazeplay.commons.threads.LoggingUncaughtExceptionHandler;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * Main class of GazePlay Learning (see {@link #main(String[])}).
 */
public class GazePlayLauncher {

    /**
     * Created at the first log, not when the class is loaded: the log file is in the GazePlay folder, which has to be
     * found (and possibly moved) first, see {@link GazePlayDirectories#initGazePlayFolder()}.
     */
    private static final class LazyLog {
        private static final Logger log = LoggerFactory.getLogger(GazePlayLauncher.class);
    }

    private static Logger log() {
        return LazyLog.log;
    }

    /**
     * Modules of JavaFX loading native libraries, when JavaFX is part of the Java runtime (bundled Zulu FX runtimes).
     */
    private static final List<String> NATIVE_JAVAFX_MODULES = List.of("javafx.graphics", "javafx.media");

    /**
     * JVM options of GazePlay, given to the JVM of each game. The launch scripts of the distributions and the IntelliJ
     * configurations give the same ones.
     * <ul>
     * <li>JavaFX and Tobii load native libraries: without this option, Java 25 warns at each launch, and a future Java
     * will refuse to load them. JavaFX is a module of the bundled runtimes, and on the classpath (unnamed module) when
     * GazePlay runs from the sources: naming a module absent from the runtime would be warned about too;</li>
     * <li>JavaFX 21 (used when GazePlay runs from the sources: the distributions bundle JavaFX 25) calls the memory
     * methods of sun.misc.Unsafe in its renderer, which Java 25 reports and a future Java will deny.</li>
     * </ul>
     */
    public static List<String> jvmOptions() {
        List<String> nativeAccess = new ArrayList<>();
        NATIVE_JAVAFX_MODULES.stream()
            .filter(module -> ModuleLayer.boot().findModule(module).isPresent())
            .forEach(nativeAccess::add);
        nativeAccess.add("ALL-UNNAMED");
        return List.of(
            "--enable-native-access=" + String.join(",", nativeAccess),
            "--sun-misc-unsafe-memory-access=allow");
    }

    /**
     * Entry point of GazePlay Learning, for the menu and for each game (one JVM per game). The GazePlay folder is found
     * (and moved from its former location if needed) before anything is logged, since the log file is in it; the
     * application then runs on a bootstrap thread, and the process exits with the exit code of the command line.
     */
    public static void main(String[] args) {

        // Before anything is logged
        final List<String> gazePlayFolderMessages = GazePlayDirectories.initGazePlayFolder();

        Thread.currentThread().setName(GazePlayLauncher.class.getSimpleName() + "-main");
        Thread.currentThread().setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());

        Runnable rootTask = () -> {

            SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
            printStartupInfo();

            initGazePlayDirectory(gazePlayFolderMessages);
            keepTheWindowsAudioLibraryLoaded();

            System.exit(run(args));
        };

        ThreadFactory threadFactory = new CustomThreadFactory("application-bootsrap",
            new GroupingThreadFactory("application-bootsrap-group"));
        Thread bootstrapThread = threadFactory.newThread(rootTask);
        bootstrapThread.start();
    }

    /**
     * Parses the command line (see {@link MainCommand}) and runs the JavaFX application until it is closed.
     *
     * @return the exit code of the process
     */
    static int run(String[] args) {
        final CompositionRoot compositionRoot = new CompositionRoot();
        return new CommandLine(new MainCommand(compositionRoot)).execute(args);
    }

    /**
     * Logs how the GazePlay folder was found, and creates it if missing.
     */
    private static void initGazePlayDirectory(List<String> gazePlayFolderMessages) {
        // What was done when the folder was found (moved from its former location in the home directory...)
        gazePlayFolderMessages.forEach(message -> log().info(message));
        // creation of GazePlay default folder if it does not exist.
        File gazePlayDirectory = GazePlayDirectories.getGazePlayFolder();
        log().info("FOLDER PATH " + gazePlayDirectory);
        if (!gazePlayDirectory.exists()) {
            boolean gazePlayDirectoryCreated = gazePlayDirectory.mkdirs();
            log().debug("gazePlayDirectoryCreated = " + gazePlayDirectoryCreated);
        }
    }

    /**
     * Keeps the audio library of Windows (MMDevAPI.dll) loaded until the end of the process, before any sound.
     * <p>
     * The sounds of JavaFX Media (DirectSound) load it; it can be unloaded again while a thread of Windows (RPC) still
     * calls back into it: the process then stops with an access violation (0xC0000005) in rpcrt4.dll, without
     * a crash report of the JVM. Seen in October 2026 in the dumps of Windows (CrashDumps) of Connect4, which plays a
     * sound in its first seconds (3 launches out of 40 in a row). Loaded once more by Java, the library is never
     * unloaded. Nothing is done on the other systems, nor when the library is missing or cannot be loaded (the sound
     * then works as before).
     */
    static void keepTheWindowsAudioLibraryLoaded() {
        if (!System.getProperty("os.name", "").startsWith("Windows")) {
            return;
        }
        final String systemRoot = System.getenv("SystemRoot");
        final File library = new File(systemRoot == null ? "C:\\Windows" : systemRoot, "System32\\MMDevAPI.dll");
        if (!library.isFile()) {
            log().warn("Audio library of Windows not found: {}", library);
            return;
        }
        try {
            System.load(library.getAbsolutePath());
            log().info("Audio library of Windows kept loaded: {}", library);
        } catch (final UnsatisfiedLinkError | SecurityException e) {
            log().warn("Audio library of Windows not kept loaded: {}", library, e);
        }
    }

    /**
     * Logs the version, the system properties and the memory of the JVM, to help with the problems reported by users.
     */
    private static void printStartupInfo() {
        final String versionInfo = VersionInfo.findVersionInfo();

        log().info("GazePlay Learning");
        log().info("Version : " + versionInfo);
        Utils.logSystemProperties();
        log().info("Max Memory: " + Runtime.getRuntime().maxMemory());

        File workingDirectory = new File(".").getAbsoluteFile();
        log().info("workingDirectory = {}", workingDirectory.getAbsolutePath());
    }

}
