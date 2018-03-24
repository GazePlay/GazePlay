package net.gazeplay.commons.utils.games;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import org.apache.commons.io.IOUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public static Image[] getImages(String folder) {

        return getImages(folder, -1);
    }

    public static Image[] getImages(final String folder, final int nbMax) {

        File directory = new File(folder);

        ArrayList<Image> images = new ArrayList<>(directory.list().length);

        for (String imagePath : directory.list()) {
            final String fileUrl = "file:" + directory.getAbsoluteFile() + FILESEPARATOR + imagePath;
            boolean added;
            if (!imagePath.startsWith(".") && isImage(fileUrl)) { // Problems with files starting with a point on
                // Windows
                images.add(new Image(fileUrl));
                added = true;
            } else {
                added = false;
            }
            log.debug("{} : added = {}", fileUrl, added);
        }

        Image[] Timages = obj2im(images.toArray());

        if (nbMax <= 0) {
            return Timages;
        }

        Image[] rimages = new Image[nbMax];

        for (int i = 0; i < nbMax; i++) {
            rimages[i] = Timages[(int) (Math.random() * Timages.length)];
        }

        return rimages;
    }

    private static Image[] obj2im(Object[] objects) {

        Image[] images = new Image[objects.length];

        for (int i = 0; i < objects.length; i++) {

            images[i] = (Image) objects[i];
        }

        return images;
    }

    public static boolean isImage(String file) {
        String mimetype = new MimetypesFileTypeMap().getContentType(file);
        log.debug("{} : mimetype = {}", file, mimetype);
        return mimetype.startsWith("image");
    }

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

    public static Image[] images(String folder) {

        log.info("Try to find images in folder : " + folder);

        if ((new File(folder)).exists()) {

            Image[] T = getImages(folder);

            if (T.length != 0) {

                log.debug("I found " + T.length + " images in folder : " + folder);
                return T;
            } else {

                log.info("No image in folder : " + folder);
                return defaultImage();
            }
        } else {

            log.info("Folder doesn't exist : " + folder);
            return defaultImage();
        }

    }

    private static Image[] defaultImage() {

        Image[] defaultImages = new Image[10];
        defaultImages[0] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/animal-807308_1920.png"));
        defaultImages[1] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/bulldog-1047518_1920.jpg"));
        defaultImages[2] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/businessman-607786_1920.png"));
        defaultImages[3] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/businessman-607834_1920.png"));
        defaultImages[4] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/crocodile-614386_1920.png"));
        defaultImages[5] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/goldfish-30837_1280.png"));
        defaultImages[6] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/graphic_missbone17.gif"));
        defaultImages[7] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/nurse-37322_1280.png"));
        defaultImages[8] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/owl-161583_1280.png"));
        defaultImages[9] = new Image(ClassLoader.getSystemResourceAsStream(
                "data/common/default/images/pez-payaso-animales-el-mar-pintado-por-teoalmeyra-9844979.jpg"));
        return defaultImages;
    }

    public static void playSound(String ressource) {

        log.info("Try to play " + ressource);

        URL url = ClassLoader.getSystemResource(ressource);

        String path = null;

        if (url == null) {
            path = new File(ressource).toURI().toString();
        } else
            path = url.toString();

        log.info("path " + path);

        try {
            Media media = new Media(path);
            MediaPlayer mp = new MediaPlayer(media);
            mp.setVolume(BackgroundMusicManager.getInstance().volumeProperty().getValue());
            mp.play();
        } catch (Exception e) {
            log.error("Exception", e);
        }
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

    public static String getFilesFolder() {

        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        String filesFolder = config.getFiledir();

        log.info("filesFolder : " + filesFolder);
        return filesFolder;
    }

    /**
     * @return images directory for GazePlay : in the files directory another folder called images
     */

    public static String getImagesFolder() {

        return getFilesFolder() + FILESEPARATOR + "images" + FILESEPARATOR;
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

    public static File createInfoStatsFile(String outputDirectoryPath) {
        final File outputDirectory = new File(outputDirectoryPath);
        final String fileName = Utils.now() + "-info-game.csv";
        return new File(outputDirectory, fileName);
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
