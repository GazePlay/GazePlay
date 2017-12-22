package net.gazeplay;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.Utils;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Slf4j
public class GazePlayLauncher {

    private static final String artifactId = "gazeplay";

    public static void main(String[] args) {

        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();

        Thread.currentThread().setUncaughtExceptionHandler(new GazePlayUncaughtExceptionHandler());

        String versionInfo;
        try {
            versionInfo = findVersionInfo(artifactId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the version info", e);
        }

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
            log.info("gazePlayFolderCreated = " + gazePlayFolderCreated);
        }

        Application.launch(GazePlay.class, args);
    }

    private static String findVersionInfo(String applicationName) throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
                .getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            URL manifestUrl = resources.nextElement();
            Manifest manifest = new Manifest(manifestUrl.openStream());
            Attributes mainAttributes = manifest.getMainAttributes();
            String implementationTitle = mainAttributes.getValue("Implementation-Title");
            if (implementationTitle != null && implementationTitle.equals(applicationName)) {
                String implementationVersion = mainAttributes.getValue("Implementation-Version");
                String buildTime = mainAttributes.getValue("Build-Time");
                return implementationVersion + " (" + buildTime + ")";
            }
        }
        return "Current Version";
    }

    @Slf4j
    private static class GazePlayUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            log.error("uncaughtException", e);
        }
    }
}
