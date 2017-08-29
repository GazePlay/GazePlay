package utils.games;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import org.tc33.jheatchart.HeatChart;
import utils.games.stats.Stats;
import utils.games.stats.StatsDisplay;

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
public class Utils {

    public static final String FILESEPARATOR = System.getProperties().getProperty("file.separator");
    private static final String heatMapFileName = "java-heat-chart.png";

    private static final String tempFolder = "temp";

    private static final Bravo bravo = new Bravo();

    public static Image[] getImages(String folder) {

        return getImages(folder, -1);
    }

    public static Image[] getImages(String folder, int nbMax) {

        File directory = new File(folder);

        ArrayList<Image> images = new ArrayList<>(directory.list().length);

        for (String imagePath : directory.list()) {

            String fileSeparator = System.getProperties().getProperty("file.separator");

            String file = "file:" + directory.getAbsoluteFile() + fileSeparator + imagePath;

            if (file.indexOf("\\.") < 0 && isImage(file)) //Problems with files starting with a point on Windows
                images.add(new Image(file));
        }

        Image[] Timages =  obj2im(images.toArray());

        if(nbMax <= 0)
            return Timages;

        Image[] Rimages = new Image[nbMax];

        for(int i = 0; i < nbMax; i++){

            Rimages[i] = Timages[(int)(Math.random()*Timages.length)];
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

    public static void clear(Scene scene, Group root, ChoiceBox<String> cbxGames) {

        scene.setFill(Color.BLACK);

        for (Node N : root.getChildren()) {

            N.setTranslateX(-10000);
        }

        root.getChildren().remove(0, root.getChildren().size());

        root.getChildren().add(bravo);


    }

    public static void home(Scene scene, Group root, ChoiceBox<String> cbxGames, Stats stats) {

        double width = scene.getWidth() / 10;
        double height = width;
        double X = scene.getWidth() * 0.9;
        double Y = scene.getHeight() - height * 1.1;

        Home home = new Home(X, Y, width, height);

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    System.out.println("stats = " + stats);

                    if (stats == null) {

                        goHome(scene, root, cbxGames);
                    }
                    else{

                        StatsDisplay.displayStats(stats, scene, root, cbxGames);
                    }
                }
            }
        };

        home.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        root.getChildren().add(home);
    }

    private static void goHome(Scene scene, Group root, ChoiceBox<String> cbxGames) {

        clear(scene, root, cbxGames);

        if (cbxGames != null) {
            cbxGames.getSelectionModel().clearSelection();
            root.getChildren().add(cbxGames);

            cbxGames.setTranslateX(scene.getWidth() * 0.9 / 2);
            cbxGames.setTranslateY(scene.getHeight() * 0.9 / 2);

            addButtons(scene, root, cbxGames);
        }
    }

    public static void addButtons(Scene scene, Group root, ChoiceBox<String> cbxGames) {

        double width = scene.getWidth() / 10;
        double heigth = width;
        double XExit = scene.getWidth() * 0.9;
        double XLicence = scene.getWidth() * 0.0;
        double Y = scene.getHeight() - heigth * 1.1;

        //  License license = new License(XLicence, Y, width, heigth, scene, root, cbxGames);

        // root.getChildren().add(license);

        Rectangle exit = new Rectangle(XExit, Y, width, heigth);

        exit.setFill(new ImagePattern(new Image("data/common/images/power-off.png"), 0, 0, 1, 1, true));

        EventHandler<javafx.event.Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    System.exit(0);

                }
            }
        };

        exit.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        root.getChildren().add(exit);

        root.getChildren().add(logo(scene));

        root.getChildren().add(Utils.BuildLicence());
    }

    public static MenuBar BuildLicence() {

        MenuBar menuBar = new MenuBar();

        // --- Menu File
        Menu menu = new Menu("GazePlay");

        StringBuilder licence = new StringBuilder(50000);
        String line;

        try {

            InputStream is =  ClassLoader.getSystemClassLoader().getResourceAsStream("data/common/licence.txt");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                licence.append('\n');
                licence.append(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MenuItem MenuLicence = new MenuItem(licence.toString());

        menu.getItems().add(MenuLicence);

        menuBar.getMenus().addAll(menu);

        return menuBar;
    }

    public static Node logo(Scene scene) {

        double width = scene.getWidth()*0.5;
        double height = scene.getHeight()*0.2;

        double posY = scene.getHeight()*0.1;
        double posX = (scene.getWidth() - width)/2;

        Rectangle logo = new Rectangle(posX,posY, width,height);
        logo.setFill(new ImagePattern(new Image("data/common/images/gazeplay.jpg"),0,0,1,1, true));

        return logo;
    }

    public static Image[] images(String folder){

        if((new File(folder)).exists()) {

            return getImages(folder);
        }
        else{

            System.out.println("folder doesn't exit : " + folder);

            Image[] defaultImages = new Image[10];
            defaultImages[0] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/animal-807308_1920.png"));
            defaultImages[1] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/bulldog-1047518_1920.jpg"));
            defaultImages[2] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/businessman-607786_1920.png"));
            defaultImages[3] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/businessman-607834_1920.png"));
            defaultImages[4] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/crocodile-614386_1920.png"));
            defaultImages[5] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/goldfish-30837_1280.png"));
            defaultImages[6] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/graphic_missbone17.gif"));
            defaultImages[7] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/nurse-37322_1280.png"));
            defaultImages[8] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/owl-161583_1280.png"));
            defaultImages[9] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/pez-payaso-animales-el-mar-pintado-por-teoalmeyra-9844979.jpg"));
            return defaultImages;
        }

    }

    public static void playSound(String ressource) {

        URL url = ClassLoader.getSystemResource(ressource);
        String path = url.toString();
        Media media = new Media(path);
        MediaPlayer mp = new MediaPlayer(media);
        mp.play();
    }

    /**
     *
     * @return Default directory for GazePlay : in user's home directory, in a folder called GazePlay
     */
    public static String getGazePlayFolder(){

        return System.getProperties().getProperty("user.home") + FILESEPARATOR +"GazePlay"+ FILESEPARATOR;
    }

    /**
     *
     * @return Temp directory for GazePlay : in the default directory of GazePlay, a folder called Temp
     */
    public static String getTempFolder(){

        return getGazePlayFolder()+tempFolder+FILESEPARATOR;
    }

    /**
     *
     * @return images directory for GazePlay : in the default directory of GazePlay, in a folder called files another folder called images
     */

    public static String getImagesFolder(){

        return getGazePlayFolder() + "files" +FILESEPARATOR + "images" + FILESEPARATOR;
    }

    /**
     *
     * @return sounds directory for GazePlay : in the default directory of GazePlay, in a folder called files another folder called sounds
     */

    public static String getSoundsFolder(){

        return getGazePlayFolder() + "files" + FILESEPARATOR + "sounds" + FILESEPARATOR;
    }

    /**
     *
     * @return statistics directory for GazePlay : in the default directory of GazePlay, in a folder called statistics
     */

    public static String getStatsFolder(){

        return getGazePlayFolder() + "statistics" + FILESEPARATOR;
    }

    /**
     *
     * @return current date with respect to the format yyyy-MM-dd
     */
    public static String today(){

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);

    }

    /**
     *
     * @return current date with respect to the format dd/MM/yyyy
     */
    public static String todayCSV(){

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        return dateFormat.format(date);

    }

    /**
     *
     * @return current time with respect to the format HH:MM:ss
     */
    public static String time(){

        DateFormat dateFormat = new SimpleDateFormat("HH:MM:ss");
        Date date = new Date();
        return dateFormat.format(date);

    }

    /**
     *
     * @return current time with respect to the format yyyy-MM-dd-HH-MM-ss
     */
    public static String now(){

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);

    }

    public static void save(String S, File F){

        try {
            PrintWriter out = new PrintWriter(F);

            out.println(S);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void buildHeatMap(double[][] data){

        // Step 1: Create our heat map chart using our data.
        HeatChart map = new HeatChart(data);

        map.setHighValueColour(java.awt.Color.RED);
        map.setLowValueColour(java.awt.Color.lightGray);

        map.setShowXAxisValues(false);
        map.setShowYAxisValues(false);
        map.setChartMargin(0);

        //creation of temp folder if it doesn't already exist.
        (new File(Utils.getTempFolder())).mkdir();

        String heatMapPath = Utils.getTempFolder() + heatMapFileName;

        File saveFile = new File(heatMapPath);

        try {
            map.saveToFile(saveFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getHeatMapPath(){

        return Utils.getTempFolder() + heatMapFileName;
    }
}
