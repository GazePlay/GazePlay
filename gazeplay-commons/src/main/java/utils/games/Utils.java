package utils.games;

import gaze.configuration.Configuration;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    public static Image[] getImages(String folder, int nbMax) {

        File directory = new File(folder);

        ArrayList<Image> images = new ArrayList<>(directory.list().length);

        for (String imagePath : directory.list()) {

            String file = "file:" + directory.getAbsoluteFile() + FILESEPARATOR + imagePath;

            if (!imagePath.startsWith(".") && isImage(file)) // Problems with files starting with a point on Windows
                images.add(new Image(file));
        }

        Image[] Timages = obj2im(images.toArray());

        if (nbMax <= 0)
            return Timages;

        Image[] Rimages = new Image[nbMax];

        for (int i = 0; i < nbMax; i++) {

            Rimages[i] = Timages[(int) (Math.random() * Timages.length)];
        }

        return Rimages;
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

        return mimetype.startsWith("image/");
    }

    public static MenuBar BuildLicence() {

        MenuBar menuBar = new MenuBar();

        // --- Menu File
        Menu menu = new Menu("GazePlay");

        StringBuilder licence = new StringBuilder(50000);
        String line;

        try {

            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("data/common/licence.txt");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                licence.append('\n');
                licence.append(line);
            }
            br.close();
        } catch (IOException e) {
            log.error("Exception", e);
        }

        MenuItem MenuLicence = new MenuItem(licence.toString());

        menu.getItems().add(MenuLicence);

        menuBar.getMenus().addAll(menu);

        return menuBar;
    }

    public static Image[] images(String folder) {

        log.debug("Try to find images in folder : " + folder);

        if ((new File(folder)).exists()) {

            Image[] T = getImages(folder);

            if (T.length != 0) {

                log.debug("I found images in folder : " + folder);
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

        URL url = ClassLoader.getSystemResource(ressource);
        String path = url.toString();
        Media media = new Media(path);
        try {
            MediaPlayer mp = new MediaPlayer(media);
            mp.play();
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     *
     * @return Default directory for GazePlay : in user's home directory, in a folder called GazePlay
     */
    public static String getGazePlayFolder() {

        return System.getProperties().getProperty("user.home") + FILESEPARATOR + "GazePlay" + FILESEPARATOR;
    }

    /**
     *
     * @return DLL directory for GazePlay : in the default directory of GazePlay, a folder called DLL
     */
    public static String getDllFolder() {

        return getGazePlayFolder() + "DLL" + FILESEPARATOR;
    }

    /**
     *
     * @return styles directory for GazePlay : in the default directory of GazePlay, a folder called styles
     */
    public static String getStylesFolder() {

        return getGazePlayFolder() + "styles" + FILESEPARATOR;
    }

    /**
     *
     * @return CSS files found in the styles folder
     */
    public static ObservableList<String> addStylesheets(ObservableList<String> styleSheets) {

        File F = new File(getStylesFolder());

        if (F.exists()) {

            File[] Tfiles = F.listFiles();
            for (int i = 0; i < Tfiles.length; i++) {

                if (Tfiles[i].toString().endsWith(".css"))
                    styleSheets.add("file://" + Tfiles[i].toString());
            }
        }

        return styleSheets;
    }

    /**
     *
     * @return Temp directory for GazePlay : in the default directory of GazePlay, a folder called Temp
     */
    public static String getTempFolder() {

        return getGazePlayFolder() + tempFolder + FILESEPARATOR;
    }

    /**
     *
     * @return images directory for GazePlay : by default in the default directory of GazePlay, in a folder called files
     *         but can be configured through interface and/or GazePlay.properties file
     */

    public static String getFilesFolder() {

        String filesFolder = new Configuration().filedir;

        log.info("filesFolder : " + filesFolder);
        return filesFolder;
    }

    /**
     *
     * @return images directory for GazePlay : in the files directory another folder called images
     */

    public static String getImagesFolder() {

        return getFilesFolder() + FILESEPARATOR + "images" + FILESEPARATOR;
    }

    /**
     *
     * @return sounds directory for GazePlay : in the files directory another folder called sounds
     */

    public static String getSoundsFolder() {

        return getFilesFolder() + "sounds" + FILESEPARATOR;
    }

    /**
     *
     * @return statistics directory for GazePlay : in the default directory of GazePlay, in a folder called statistics
     */

    public static String getStatsFolder() {

        return getGazePlayFolder() + "statistics" + FILESEPARATOR;
    }

    /**
     *
     * @return current date with respect to the format yyyy-MM-dd
     */
    public static String today() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     *
     * @return current date with respect to the format dd/MM/yyyy
     */
    public static String todayCSV() {

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        return dateFormat.format(date);

    }

    /**
     *
     * @return current time with respect to the format HH:MM:ss
     */
    public static String time() {

        DateFormat dateFormat = new SimpleDateFormat("HH:MM:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     *
     * @return current time with respect to the format yyyy-MM-dd-HH-MM-ss
     */
    public static String now() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);

    }

    public static void save(String S, File F) {

        try {
            PrintWriter out = new PrintWriter(F);

            out.println(S);
            out.flush();
        } catch (FileNotFoundException e) {
            log.error("Exception", e);
        }
    }

    public static PrintWriter getInfoStatsFile(String folder) {

        PrintWriter out = null;

        try {
            out = new PrintWriter(folder + Utils.now() + "-info-game.csv");

        } catch (FileNotFoundException e) {
            log.error("Exception", e);
        }

        return out;
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
     *
     * @return true if the operating system is a Windows
     */
    public static boolean isWindows() {

        return System.getProperty("os.name").indexOf("indow") > 0;
    }
}
