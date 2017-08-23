package utils.games;

import javafx.embed.swing.SwingNode;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.tc33.jheatchart.HeatChart;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by schwab on 23/12/2016.
 */
public class Utils {

    public static final String FILESEPARATOR = System.getProperties().getProperty("file.separator");

    private static Bravo bravo = new Bravo();

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

                    System.out.println(stats);

                    if (stats == null) {

                        goHome(scene, root, cbxGames);
                    }
                    else{

                        Utils.show(stats, scene, root, cbxGames);

                    }
                }
            }
        };

        home.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        root.getChildren().add(home);
    }

    private static void show (Stats stats, Scene scene, Group root, ChoiceBox<String> cbxGames){

        stats.stop();

        clear(scene, root, cbxGames);

        Text statistics = new Text("Statistiques");

        statistics.setX(scene.getWidth()*0.4);
        statistics.setY(60);
        statistics.setFont(new Font(60));
        statistics.setFill(new Color(1,1,1,1));

        Text totalTime = new Text("Temps total de jeu : " + convert(stats.getTotalTime()));

        totalTime.setX(100);
        totalTime.setY(150);
        totalTime.setFont(new Font(20));
        totalTime.setFill(new Color(1,1,1,1));

        Text shoots = new Text("Tirs : " + stats.getNbshoots());

        shoots.setX(100);
        shoots.setY(200);
        shoots.setFont(new Font(20));
        shoots.setFill(new Color(1,1,1,1));

        Text length = new Text("Temps effectif de jeu : " + convert(stats.getLength()));

        length.setX(100);
        length.setY(250);
        length.setFont(new Font(20));
        length.setFill(new Color(1,1,1,1));

        Text averageLength = new Text("Temps de réaction moyen : " + convert(stats.getAverageLength()));

        averageLength.setX(100);
        averageLength.setY(300);
        averageLength.setFont(new Font(20));
        averageLength.setFill(new Color(1,1,1,1));

        Text standDev = new Text("Écart-type : " + convert((long)stats.getSD()));

        standDev.setX(100);
        standDev.setY(350);
        standDev.setFont(new Font(20));
        standDev.setFill(new Color(1,1,1,1));

        Text UncountedShoot = new Text("Tirs non comptés : " + stats.getNbUnCountedShoots());

        UncountedShoot.setX(scene.getWidth()/2);
        UncountedShoot.setY(150);
        UncountedShoot.setFont(new Font(20));
        UncountedShoot.setFill(new Color(1,1,1,1));

        LineChart<String,Number> chart = buildLineChart(stats);

        chart.setTranslateY(scene.getHeight()/2);
        chart.setMaxWidth(scene.getWidth()*0.4);
        chart.setMaxHeight(scene.getHeight()*0.4);

        chart.setLegendVisible(false);

        Rectangle heatChart = BuildHeatChart(stats, scene);

        heatChart.setX(scene.getWidth()*5/9);
        heatChart.setY(scene.getHeight()/2+15);
        heatChart.setWidth(scene.getWidth()*0.35);
        heatChart.setHeight(scene.getHeight()*0.35);

        root.getChildren().addAll(statistics, shoots, totalTime, length, averageLength, standDev, UncountedShoot, chart, heatChart);

        home(scene, root, cbxGames, null);
    }

    private static Rectangle BuildHeatChart(Stats stats, Scene scene){

        double[][] data = stats.getHeatMap();

        // Step 1: Create our heat map chart using our data.
        HeatChart map = new HeatChart(data);

        map.setHighValueColour(java.awt.Color.RED);
        map.setLowValueColour(java.awt.Color.lightGray);

        map.setShowXAxisValues(false);
        map.setShowYAxisValues(false);
        map.setChartMargin(0);

        // Step 2: Customise the chart.
       // map.setTitle("This is my heat chart title");
       // map.setXAxisLabel("X Axis");
       // map.setYAxisLabel("Y Axis");

        try {
            map.saveToFile(new File("/Users/schwab/Documents/TET-Communicator/src/java-heat-chart.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Rectangle heatMap = new Rectangle();
        //Rectangle heatMap = new Rectangle();
        //heatMap.setVisible(true);

        heatMap.setFill(new ImagePattern(new Image("file:/Users/schwab/Documents/TET-Communicator/src/java-heat-chart.png"),0,0,1,1, true));

        EventHandler<Event> openEvent = openChartEvent(heatMap, scene);

        heatMap.addEventHandler(MouseEvent.MOUSE_CLICKED, openEvent);

        return heatMap;
    }

    private static EventHandler<Event> openChartEvent(Rectangle heatMap, Scene scene){

        return new EventHandler<javafx.event.Event>() {

        @Override
        public void handle(javafx.event.Event e) {

            if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                System.out.println("Increase Size");

                heatMap.setX(scene.getWidth()*5/9);
                heatMap.setY(scene.getHeight()/2+15);
                heatMap.setWidth(scene.getWidth()*0.35);
                heatMap.setHeight(scene.getHeight()*0.35);

                heatMap.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                heatMap.addEventHandler(MouseEvent.MOUSE_CLICKED, closeChartEvent(heatMap, scene));

            }
        }

        };

    }

    private static EventHandler<Event> closeChartEvent(Rectangle heatMap, Scene scene){

        return new EventHandler<javafx.event.Event>() {

            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    System.out.println("Decrease Size");

                    heatMap.setX(scene.getWidth()*1/10);
                    heatMap.setY(scene.getHeight()*1/10);
                    heatMap.setWidth(scene.getWidth()*0.8);
                    heatMap.setHeight(scene.getHeight()*0.8);

                    heatMap.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                    heatMap.addEventHandler(MouseEvent.MOUSE_CLICKED, openChartEvent(heatMap, scene));

                }
            }

        };

    }

    private static LineChart<String,Number> buildLineChart(Stats stats) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        LineChart<String,Number> lineChart =
                new LineChart<String,Number>(xAxis,yAxis);

       // lineChart.setTitle("Réaction");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        //series.setName("Temps de réaction");

        XYChart.Series average = new XYChart.Series();
       // average.setName("Moyenne");

        XYChart.Series sdp = new XYChart.Series();
        //sdp.setName("Moyenne");

        XYChart.Series sdm = new XYChart.Series();
        //sdm.setName("Moyenne");

      //  xAxis.setLabel("Lancer");
      //  yAxis.setLabel("ms");

        //populating the series with data

        ArrayList<Integer> shoots = stats.getShoots();

        double sd = stats.getSD();

        int i = 0;

        average.getData().add(new XYChart.Data(0+"", stats.getAverageLength()));
        sdp.getData().add(new XYChart.Data(0+"", stats.getAverageLength()+sd));
        sdm.getData().add(new XYChart.Data(0+"", stats.getAverageLength()-sd));

        for(Integer I: shoots){

            i++;
            series.getData().add(new XYChart.Data(i+"", I.intValue()));
            average.getData().add(new XYChart.Data(i+"", stats.getAverageLength()));

            sdp.getData().add(new XYChart.Data(i+"", stats.getAverageLength()+sd));
            sdm.getData().add(new XYChart.Data(i+"", stats.getAverageLength()-sd));
        }

        i++;
        average.getData().add(new XYChart.Data(i+"", stats.getAverageLength()));
        sdp.getData().add(new XYChart.Data(i+"", stats.getAverageLength()+sd));
        sdm.getData().add(new XYChart.Data(i+"", stats.getAverageLength()-sd));

        lineChart.setCreateSymbols(false);

        lineChart.getData().add(average);
        lineChart.getData().add(sdp);
        lineChart.getData().add(sdm);
        lineChart.getData().add(series);


        series.getNode().setStyle("-fx-stroke-width: 3; -fx-stroke: red; -fx-stroke-dash-offset:5;");
        average.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: lightgreen;");
        sdp.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: grey;");
        sdm.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: grey;");

        return lineChart;
    }

    private static String convert(long totalTime) {

        System.out.println(totalTime);


        long days = TimeUnit.MILLISECONDS.toDays(totalTime);
        totalTime -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(totalTime);
        totalTime -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime);
        totalTime -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime);
        totalTime -= TimeUnit.SECONDS.toMillis(seconds);

        StringBuilder builder = new StringBuilder(1000);

        if(days>0) {
            builder.append(days);
            builder.append(" d ");
        }
        if(hours>0) {
            builder.append(hours);
            builder.append(" h ");
        }
        if(minutes>0) {
            builder.append(minutes);
            builder.append(" m ");
        }
        if(seconds>0) {
            builder.append(seconds);
            builder.append(" s ");
        }
        if(totalTime>0) {
            builder.append(totalTime);
            builder.append(" ms");
        }

        return builder.toString();
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
}
