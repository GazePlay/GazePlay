package net.gazeplay.commons.utils.games;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * Created by schwab on 23/12/2016.
 */
@Slf4j
public class Utils {

    public static final String FILESEPARATOR = System.getProperties().getProperty("file.separator");
    public static final String LINESEPARATOR = System.getProperties().getProperty("line.separator");

    private static final String tempFolder = "temp";

    public static MenuBar buildLicence() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String licenseFileAsString = loadLicenseFileAsString(classLoader);

        MenuItem licenseMenuItem = new MenuItem(licenseFileAsString);

        Menu menu = new Menu("GazePlay");
        menu.getItems().add(licenseMenuItem);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(menu);
        menuBar.setPrefHeight(40);
        menuBar.setPrefWidth(80);

        return menuBar;
    }

    private static String loadLicenseFileAsString(ClassLoader classLoader) {
        try {
            try (InputStream is = classLoader.getResourceAsStream("data/common/licence.txt")) {
                return IOUtils.toString(is, Charset.forName("UTF-8"));
            }
        } catch (IOException e) {
            return "Failed to load the license file";
        }
    }

    public static void playSound(String ressource) {

        log.debug("Try to play " + ressource);

        URL url = ClassLoader.getSystemResource(ressource);

        String path = null;

        if (url == null) {
            final File file = new File(ressource);
            log.debug("using file");
            if (!file.exists()) {
                log.warn("file doesn't exist : {}", ressource);
            }
            path = file.toURI().toString();
        } else {
            log.debug("using url");
            path = url.toString();
        }

        log.debug("path " + path);

        try {
            Media media = new Media(path);
            MediaPlayer mp = new MediaPlayer(media);
            final Configuration configuration = Configuration.getInstance();
            mp.setVolume(configuration.getEffectsVolume());
            mp.volumeProperty().bind(configuration.getEffectsVolumeProperty());
            mp.play();
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    public static InputStream getInputStream(String ressource) {

        log.debug("Try to play " + ressource);

        return ClassLoader.getSystemResourceAsStream(ressource);

    }

    /**
     * @return Default directory for GazePlay : in user's home directory, in a folder called GazePlay
     */
    public static String getGazePlayFolder() {

        return System.getProperties().getProperty("user.home") + FILESEPARATOR + "GazePlay" + FILESEPARATOR;
    }

    /**
     * @return Temp directory for GazePlay : in the default directory of GazePlay, a folder called Temp
     */
    public static String getTempFolder() {

        return getGazePlayFolder() + tempFolder + FILESEPARATOR;
    }

    /**
     * @return images directory for GazePlay : by default in the default directory of GazePlay, in a folder called files
     *         but can be configured through interface and/or GazePlay.properties file
     */

    private static String getFilesFolder() {

        Configuration config = Configuration.getInstance();
        String filesFolder = config.getFileDir();

        log.info("filesFolder : " + filesFolder);
        return filesFolder;
    }

    /**
     * @return images directory for GazePlay : in the files directory another folder called images
     */

    public static File getBaseImagesDirectory() {
        File filesDirectory = new File(getFilesFolder());
        return new File(filesDirectory, "images");
    }

    public static File getImagesSubDirectory(String subfolderName) {
        File baseImagesDirectory = getBaseImagesDirectory();
        return new File(baseImagesDirectory, subfolderName);
    }

    /**
     * @return sounds directory for GazePlay : in the files directory another folder called sounds
     */

    public static String getSoundsFolder() {

        return getFilesFolder() + "sounds" + FILESEPARATOR;
    }

    /**
     * @return statistics directory for GazePlay : in the default directory of GazePlay, in a folder called statistics
     */

    public static String getStatsFolder() {

        return getGazePlayFolder() + "statistics" + FILESEPARATOR;
    }

    public static String getUserStatsFolder(String user) {

        if (!user.equals("")) {
            return getGazePlayFolder() + "profiles" + FILESEPARATOR + user + FILESEPARATOR + "statistics"
                    + FILESEPARATOR;
        } else {
            return getStatsFolder();
        }
    }

    /**
     * @return current date with respect to the format yyyy-MM-dd
     */
    public static String today() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * @return current date with respect to the format dd/MM/yyyy
     */
    public static String todayCSV() {

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        return dateFormat.format(date);

    }

    /**
     * @return current time with respect to the format HH:MM:ss
     */
    public static String time() {

        DateFormat dateFormat = new SimpleDateFormat("HH:MM:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * @return current time with respect to the format yyyy-MM-dd-HH-MM-ss
     */
    public static String now() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);

    }

    public static boolean copyFromJar(String filePath, String destinationPath) {
        InputStream sourceFile = null;
        OutputStream destinationFile = null;
        try {
            sourceFile = ClassLoader.getSystemResourceAsStream(filePath);
            if (sourceFile == null) {
                throw new IOException("Resource not found " + filePath);
            }
            destinationFile = new FileOutputStream(destinationPath);
            org.apache.commons.io.IOUtils.copy(sourceFile, destinationFile);
        } catch (IOException e) {
            log.error("Exception", e);
            return false; // Erreur
        } finally {
            IOUtils.closeQuietly(destinationFile);
            IOUtils.closeQuietly(sourceFile);
        }
        return true; // Résultat OK
    }

    public static String convertWindowsPath(String path) {

        path = path.replace("\\", "/");
        path = path.replaceAll("\\\\", "/");

        return path;
    }

    /**
     * @return true if the operating system is a Windows
     */
    public static boolean isWindows() {

        return System.getProperty("os.name").indexOf("indow") > 0;
    }

    public static void logSystemProperties() {

        Enumeration<?> E = System.getProperties().propertyNames();
        while (E.hasMoreElements()) {
            String element = (String) E.nextElement();
            log.info(String.format("%s: %s", element, System.getProperty(element)));
        }
    }
}
