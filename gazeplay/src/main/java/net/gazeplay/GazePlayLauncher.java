package net.gazeplay;

import javafx.application.Application;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.threads.CustomThreadFactory;
import net.gazeplay.commons.threads.GroupingThreadFactory;
import net.gazeplay.commons.threads.LoggingUncaughtExceptionHandler;
import net.gazeplay.commons.utils.games.Utils;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class GazePlayLauncher {

    @Setter
    public static boolean doStartBootstrapThread = true;

    public static void main(String[] args) {

        Thread.currentThread().setName(GazePlayLauncher.class.getSimpleName() + "-main");
        Thread.currentThread().setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());

        Runnable runnable = () -> {

            SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();

            final String versionInfo = VersionInfo.findVersionInfo();

            for (int i = 0; i < 5; i++) {
                log.info("***********************");
            }
            log.info("GazePlay");
            log.info("Version : " + versionInfo);
            Utils.logSystemProperties();
            log.info("Max Memory: " + Runtime.getRuntime().maxMemory());

            try {
                System.setProperty("file.encoding", "UTF-8");
                Field charset = Charset.class.getDeclaredField("defaultCharset");
                charset.setAccessible(true);
                charset.set(null, null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("Exception", e);
            }

            File workingDirectory = new File(".").getAbsoluteFile();
            log.info("workingDirectory = {}", workingDirectory.getAbsolutePath());

            // creation of GazePlay default folder if it does not exist.
            File gazePlayFolder = new File(Utils.getGazePlayFolder());
            if (!gazePlayFolder.exists()) {
                boolean gazePlayFolderCreated = gazePlayFolder.mkdir();
                log.debug("gazePlayFolderCreated = " + gazePlayFolderCreated);
            }

            Runnable runnable1 = () -> Application.launch(GazePlay.class, args);

            ThreadFactory threadFactory = new CustomThreadFactory("javafx-bootsrap",
                new GroupingThreadFactory("javafx-bootsrap-group"));
            Thread bootstrapThread = threadFactory.newThread(runnable1);

            if (doStartBootstrapThread) {
                bootstrapThread.start();
            } else {
                try {
                    Thread.sleep(1000 * 60 * 10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        ThreadFactory threadFactory = new CustomThreadFactory("application-bootsrap",
            new GroupingThreadFactory("application-bootsrap-group"));
        Thread bootstrapThread = threadFactory.newThread(runnable);
        bootstrapThread.start();
    }

}
