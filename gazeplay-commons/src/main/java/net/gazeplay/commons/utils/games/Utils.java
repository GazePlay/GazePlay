package net.gazeplay.commons.utils.games;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * Created by schwab on 23/12/2016.
 */
@Slf4j
public class Utils {

    public static InputStream getInputStream(final String ressource) {
        log.debug("Try to play " + ressource);
        return ClassLoader.getSystemResourceAsStream(ressource);
    }

    /**
     * @return images directory for GazePlay : by default in the default directory of GazePlay, in a folder called files
     * but can be configured through option interface and/or GazePlay.properties file
     */

    private static String getFilesFolder() {

        final Configuration config = ActiveConfigurationContext.getInstance();
        final String filesFolder = config.getFileDir();

        log.info("filesFolder : " + filesFolder);
        return filesFolder;
    }

    /**
     * @return images directory for GazePlay : in the files directory another folder called images
     */

    public static File getBaseImagesDirectory() {
        final File filesDirectory = new File(getFilesFolder());
        return new File(filesDirectory, "images");
    }

    public static File getImagesSubDirectory(final String subfolderName) {
        final File baseImagesDirectory = getBaseImagesDirectory();
        log.info("baseImagesDirectory {}", baseImagesDirectory);
        log.info("subfolderName {}", subfolderName);
        return new File(baseImagesDirectory, subfolderName);
    }

    /**
     * @return current date with respect to the format yyyy-MM-dd
     */
    public static String today() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * @return current date with respect to the format dd/MM/yyyy
     */
    public static String todayCSV() {
        final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        final Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * @return current time with respect to the format HH:MM:ss
     */
    public static String time() {
        final DateFormat dateFormat = new SimpleDateFormat("HH:MM:ss");
        final Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * @return current time with respect to the format yyyy-MM-dd-HH-MM-ss
     */
    public static String now() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        final Date date = new Date();
        return dateFormat.format(date);
    }

    public static boolean copyFromJar(final String filePath, final String destinationPath) {
        InputStream sourceFile = null;
        OutputStream destinationFile = null;
        try {
            sourceFile = ClassLoader.getSystemResourceAsStream(filePath);
            if (sourceFile == null) {
                throw new IOException("Resource not found " + filePath);
            }
            destinationFile = new FileOutputStream(destinationPath);
            org.apache.commons.io.IOUtils.copy(sourceFile, destinationFile);
        } catch (final IOException e) {
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
        final Enumeration<?> E = System.getProperties().propertyNames();
        while (E.hasMoreElements()) {
            final String element = (String) E.nextElement();
            log.info(String.format("%s: %s", element, System.getProperty(element)));
        }
    }
}
