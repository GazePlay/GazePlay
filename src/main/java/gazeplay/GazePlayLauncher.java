package gazeplay;

import javafx.application.Application;
import utils.games.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class GazePlayLauncher {

    public static void main(String[] args) {

        String versionInfo;
        try {
            versionInfo = findVersionInfo("GazePlay");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the version info", e);
        }

        System.out.println("***********************");
        System.out.println("GazePlay");
        System.out.println("Version : " + versionInfo);
        System.out.println("Operating System " + System.getProperty("os.name"));
        System.out.println("***********************");

        try {
            System.setProperty("file.encoding", "UTF-8");
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        // creation of GazePlay default folder if it does not exist.
        File gazePlayFolder = new File(Utils.getGazePlayFolder());
        if (!gazePlayFolder.exists()) {
            boolean gazePlayFolderCreated = gazePlayFolder.mkdir();
            System.out.println("gazePlayFolderCreated = " + gazePlayFolderCreated);
        }

        Application.launch(GazePlay.class, args);
    }


    private static String findVersionInfo(String applicationName) throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("META-INF/MANIFEST.MF");
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

}
