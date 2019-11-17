package net.gazeplay;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.VersionInfo;
import net.gazeplay.commons.threads.CustomThreadFactory;
import net.gazeplay.commons.threads.GroupingThreadFactory;
import net.gazeplay.commons.threads.LoggingUncaughtExceptionHandler;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.concurrent.ThreadFactory;

@Slf4j
@SpringBootApplication
public class GazePlayLauncher {

    @Setter
    public static boolean doStartBootstrapThread = true;

    public static void main(String[] args) {

        Thread.currentThread().setName(GazePlayLauncher.class.getSimpleName() + "-main");
        Thread.currentThread().setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());

        Runnable rootTask = () -> {

            SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
            printStartupInfo();
            //fixDefaultCharset();

            initGazePlayDirectory();

            System.exit(SpringApplication.exit(SpringApplication.run(GazePlayLauncher.class, args)));
        };

        ThreadFactory threadFactory = new CustomThreadFactory("application-bootsrap",
            new GroupingThreadFactory("application-bootsrap-group"));
        Thread bootstrapThread = threadFactory.newThread(rootTask);
        bootstrapThread.start();
    }

    private static void initGazePlayDirectory() {
        // creation of GazePlay default folder if it does not exist.
        File gazePlayDirectory = GazePlayDirectories.getGazePlayFolder();
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

}
