package net.gazeplay;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.VersionInfo;
import net.gazeplay.commons.threads.CustomThreadFactory;
import net.gazeplay.commons.threads.GroupingThreadFactory;
import net.gazeplay.commons.threads.LoggingUncaughtExceptionHandler;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.Utils;
import picocli.CommandLine;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class GazePlayLauncher {

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

    public static void main(String[] args) {

        final String edition = GazePlayArgs.editionToSave(args);
        if (edition != null) {
            log.info("Edition: {}", edition);
            saveArgs(edition);
        }

        Thread.currentThread().setName(GazePlayLauncher.class.getSimpleName() + "-main");
        Thread.currentThread().setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());

        Runnable rootTask = () -> {

            SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
            printStartupInfo();
            //fixDefaultCharset();

            initGazePlayDirectory();

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

    private static void initGazePlayDirectory() {
        // creation of GazePlay default folder if it does not exist.
        File gazePlayDirectory = GazePlayDirectories.getGazePlayFolder();
        log.info("FOLDER PATH " + gazePlayDirectory);
        if (!gazePlayDirectory.exists()) {
            boolean gazePlayDirectoryCreated = gazePlayDirectory.mkdir();
            log.debug("gazePlayDirectoryCreated = " + gazePlayDirectoryCreated);
        }
    }

    private static void printStartupInfo() {
        final String versionInfo = VersionInfo.findVersionInfo();

        log.info("GazePlay");
        log.info("Version : " + versionInfo);
        Utils.logSystemProperties();
        log.info("Max Memory: " + Runtime.getRuntime().maxMemory());

        File workingDirectory = new File(".").getAbsoluteFile();
        log.info("workingDirectory = {}", workingDirectory.getAbsolutePath());
    }

    private static void fixDefaultCharset() {
        try {
            System.setProperty("file.encoding", "UTF-8");
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Exception while fixing default Charset", e);
        }
    }

    private static void saveArgs(String args){

        FileWriter myWritter = null;

        try {
            File myFile = GazePlayArgs.getArgsFile();
            if (myFile != null) {
                File myFolder = myFile.getParentFile();
                if (myFolder != null) {
                    boolean createFolder = myFolder.mkdirs();
                    log.info("Folder created, path = " + createFolder);
                }
                log.info("Fil args is : " + myFile);
                myWritter = new FileWriter(myFile, StandardCharsets.UTF_8);
                myWritter.write(args);
            }
        } catch (IOException e){
            log.info(String.valueOf(e));
        } finally {
            try {
                if (myWritter != null){
                    myWritter.close();
                }
            } catch (IOException e) {
                log.warn("Cannot close the edition file", e);
            }
        }
    }

}
