package net.gazeplay.commons.utils.games;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Created by schwab on 23/12/2016.
 */
@Slf4j
public class Utils {

    /**
     * Gets an InputStream to the provided resource.
     *
     * @param resource Path to the resource
     * @return The InputStream of the resource
     */
    public static InputStream getInputStream(final String resource) {
        log.debug("Try to play " + resource);
        return ClassLoader.getSystemResourceAsStream(resource);
    }

    /**
     * Provides the default GazePlay file directory. By default this will be {user.home}/GazePlay/files,
     * but can be overridden by the user in the app or in the GazePlay.properties file.
     *
     * @return GazePlay files directory
     */
    public static String getFilesFolder() {
        final Configuration config = ActiveConfigurationContext.getInstance();
        final String filesFolder = config.getFileDir();

        if (new File(filesFolder).mkdirs()){
            log.info("Folder files created !");
        }else {
            log.info("Folder files already created !");
        }

        log.info("filesFolder : {}", filesFolder);
        return filesFolder;
    }

    /**
     * @return The image directory within the GazePlay files directory
     */
    public static File getBaseImagesDirectory() {
        final File filesDirectory = new File(getFilesFolder());
        return new File(filesDirectory, "images");
    }

    public static File getImagesSubdirectory(final String subdirectoryName) {
        final File baseImagesDirectory = getBaseImagesDirectory();
        createFolder(baseImagesDirectory, subdirectoryName);
        log.info("baseImagesDirectory : {}", baseImagesDirectory);
        log.info("subdirectoryName : {}", subdirectoryName);
        return new File(baseImagesDirectory, subdirectoryName);
    }

    public static void createFolder(File baseImagesDirectory, String subdirectoryName){
        if (baseImagesDirectory.mkdirs()){
            log.info("Folder images created !");
        }else {
            log.info("Folder images already created !");
        }

        if (new File(getBaseImagesDirectory() + "/" + subdirectoryName).mkdirs()){
            addImages(subdirectoryName);
            log.info(subdirectoryName + " created !");
        }else {
            log.info(subdirectoryName + " already created !");
        }
    }

    public static void addImages(String subdirectoryName){
        File imagesFolder = new File("gazeplay-commons/src/main/resources/images/");
        List<File> images = List.of(imagesFolder.listFiles());

        for (File image : images){
            Path newName = Paths.get(getBaseImagesDirectory() + "/" + subdirectoryName + "/" + image.getName());
            log.info(String.valueOf(newName));
            try{
                Files.copy(new File(String.valueOf(image)).toPath(), newName, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String convertWindowsPath(String path) {
        String windowsPath = path;
        windowsPath = windowsPath.replace("\\", "/");
        windowsPath = windowsPath.replaceAll("\\\\", "/");
        return windowsPath;
    }

    /**
     * @return true if the operating system is Windows
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static void logSystemProperties() {
        final Enumeration<?> propertyNames = System.getProperties().propertyNames();
        while (propertyNames.hasMoreElements()) {
            final String element = (String) propertyNames.nextElement();
            log.info(String.format("%s: %s", element, System.getProperty(element)));
        }
    }

    /**
     * @param tf        textfield to limit the number of characters
     * @param maxLength the max number of characters allowed
     */
    public static void addTextLimiter(final TextField tf, final int maxLength) {
        tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (tf.getText().length() > maxLength) {
                    String s = tf.getText().substring(0, maxLength);
                    tf.setText(s);
                }
            }
        });
    }
}
