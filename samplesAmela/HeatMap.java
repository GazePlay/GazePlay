package samplesAmela;


import com.theeyetribe.clientsdk.GazeManager;
import gaze.GazeEvent;
import gaze.GazeUtils;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Amela Fejza on 6/2/2017.
 */
public class HeatMap extends Application {

    double gridSize = 100;
    ProgressIndicator indicator;
    GridPane canvas ;

    int array[] = {0,1,2};
    int clicked_array[] = {0,0,0,0};
    ArrayList<Integer> array_sizes = new ArrayList<Integer>();
    ArrayList<Circle> array_count = new ArrayList<Circle>();
    ArrayList<ImageView> animal_Images = new ArrayList<ImageView>();
    ArrayList<ImageView> tick_Images = new ArrayList<ImageView>();


    Circle circle;
    final double min_time = 1000;
    long entry;
    double center_x=50, center_y=50;
    Scene scene;
    FadeTransition ft;
    HBox hboxFirst;
    int size, index;
    double count[][]= new double[13][13] ;

   //final GazeManager gm = GazeManager.getInstance();

    File myFile8 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "dog.jpg");
    String dog = myFile8.toURI().toString();
    Image imgDog = new Image(dog);
    ImageView imgViewDog = new ImageView(imgDog);

    File myFile7 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "horse.jpg");
    String horse = myFile7.toURI().toString();
    Image imgHorse = new Image(horse);
    ImageView imgViewHorse = new ImageView(imgHorse);


    File myFile6 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "cat.jpg");
    String cat = myFile6.toURI().toString();
    Image imgCat = new Image(cat);
    ImageView imgViewCat = new ImageView(imgCat);

    File myFile5 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "fox.jpg");
    String fox = myFile5.toURI().toString();
    Image imgFox = new Image(fox);
    ImageView imgViewFox = new ImageView(imgFox);

    File myFile4 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "tick_black.png");
    String btick = myFile4.toURI().toString();
    Image imgbtick = new Image(btick);
    ImageView imgViewbTick = new ImageView(imgbtick);

    File myFile3 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "tick_blue.png");
    String bltick = myFile3.toURI().toString();
    Image imgbltick = new Image(bltick);
    ImageView imgViewblTick = new ImageView(imgbltick);

    File myFile2 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "tick_red.png");
    String rtick = myFile2.toURI().toString();
    Image imgrtick = new Image(rtick);
    ImageView imgViewrTick = new ImageView(imgrtick);

    File myFile1 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "tick_green.png");
    String gtick = myFile1.toURI().toString();
    Image imggtick = new Image(gtick);
    ImageView imgViewgTick = new ImageView(imggtick);

    File myFile9 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "gray.png");
    String gray = myFile9.toURI().toString();
    Image imgGray = new Image(gray);

    private static File myFile40 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "bravo.jpg");
    private static String bravo = myFile40.toURI().toString();
    private static Image imgBravo = new Image(bravo);
    private static ImageView imgViewBravo = new ImageView(imgBravo);

    ArrayList<ImageView> imgViewList = new ArrayList<ImageView>();
    ImageView imgV ;
    int clicked = 0;
    int img_index = 0;
    int ix = 0;
    int countDog=0;
    int countCat=0;
    int countFox=0;
    int countHorse=0;



    private static EventHandler<Event> dogEvent;
    private static  EventHandler<Event> catEvent;
    private static EventHandler<Event> horseEvent;
    private static  EventHandler<Event> foxEvent;
    private static  EventHandler<Event> normalEvent;



    public HeatMap(){

        for (int index = 0; index < array.length; index++)
        {
            array_sizes.add(array[index]);
        }

        for(int i=0;i<182;i++){
            ImageView imgViewGray = new ImageView(imgGray);
            imgViewGray.setId(Integer.toString(i));
            imgViewList.add(imgViewGray);

        }
        imgViewDog.setId(Integer.toString(1));
        imgViewCat.setId(Integer.toString(2));
        imgViewFox.setId(Integer.toString(3));
        imgViewHorse.setId(Integer.toString(4));

    }

    public void start(Stage primaryStage) {

        canvas = new GridPane();

        canvas.setPrefSize(1300,741);
        canvas.setGridLinesVisible(false);
        final int numCols = 13 ;
        final int numRows = 13 ;
        for (int i = 0; i < numCols; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / numCols);
            canvas.getColumnConstraints().add(colConst);
        }
        for (int i = 0; i < numRows; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / numRows);
            canvas.getRowConstraints().add(rowConst);
        }

        scene = new Scene(canvas);
        primaryStage.setScene(scene);

        //primaryStage.sizeToScene();
        //primaryStage.setFullScreen(true);
        primaryStage.show();

        scene.setFill(createGridPattern());

    }

    //event handler Dog
    private EventHandler<Event> buildDogEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {



                Object source = e.getSource();
                ImageView x = (ImageView)source;
                String theImage =x.getId();



                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    //System.out.println("I entered");
                    entry = (new Date()).getTime();

                    if(theImage.equals(imgViewDog.getId())){
                        countDog++;
                    }
                    else if(theImage.equals(imgViewCat.getId())){
                        countCat++;

                    }

                    else if(theImage.equals(imgViewFox.getId())){
                        countFox++;

                    }

                    else if(theImage.equals(imgViewHorse.getId())){
                        countHorse++;

                    }

                }

                else if (e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_MOVED) {

                    //System.out.println("I moved");

                    long now = (new Date()).getTime();
                    //ft = new FadeTransition(Duration.millis(3000), imgViewClick );
                    System.out.println((now - entry) / min_time);

                    if(theImage.equals(imgViewDog.getId())){
                        countDog++;
                    }
                    else if(theImage.equals(imgViewCat.getId())){
                        countCat++;

                    }

                    else if(theImage.equals(imgViewFox.getId())){
                        countFox++;

                    }

                    else if(theImage.equals(imgViewHorse.getId())){
                        countHorse++;

                    }

                    if ((now - entry) > min_time && entry != -1) {

                        Animation animation = new Transition() {

                            {
                                setCycleDuration(Duration.millis(1000));
                                Object source = e.getSource();
                                ImageView x = (ImageView)source;
                                img_index =Integer.parseInt(x.getId());

                                switch (img_index) {
                                    case (1):imgV = imgViewDog; break;
                                    case (2):imgV = imgViewCat; break;
                                    case (3):imgV = imgViewFox; break;
                                    case (4):imgV = imgViewHorse; break;

                                }

                                clicked_array[img_index - 1] = 1;
                                ft = new FadeTransition(Duration.millis(500), imgV);
                                ft.setFromValue(0.3);
                                ft.setToValue(1);
                                ft.setCycleCount(1);
                                ft.setAutoReverse(true);

                                ft.play();



                            }

                            @Override
                            protected void interpolate(double frac) {
                                final int n = Math.round(100 * (float) frac);

                            }

                        };
                        animation.play();
                        animation.setOnFinished(new EventHandler<ActionEvent>() {


                            @Override
                            public void handle(ActionEvent actionEvent) {

                                //puts BRAVO image
                                canvas.getChildren().remove(imgV);
                                //canvas.getChildren().clear();
                                int x=0,y=0;
                                switch (img_index){
                                    case (1):x = 1;y=1; break;
                                    case (2):x = 1;y=11; break;
                                    case (3):x = 11;y=1; break;
                                    case (4):x = 11;y=11; break;
                                }
                                System.out.println(clicked);
                                ix = 0;
                                for(Integer i:clicked_array){
                                    ix = ix+i;
                                }

                                canvas.add(tick_Images.get(ix -1), x,y);




                                //System.out.println("Image"+img_index+" Clicked! BRAVO!");

                               // clicked += 1;

                                if(ix==4){
                                    System.out.println("Bravo L!!!");
                                    for(ImageView img: tick_Images){
                                        canvas.getChildren().remove(img);
                                    }

                                    //canvas.getChildren().removeAll(tick_Images);


                                    for(ImageView i: imgViewList){
                                        i.removeEventHandler( MouseEvent.ANY ,normalEvent);
                                        i.removeEventHandler( GazeEvent.ANY ,normalEvent);
                                /*GazeUtils.addEventFilter(i);
                                normalEvent = buildEvent();
                                i.addEventFilter(GazeEvent.ANY, normalEvent);
                                i.addEventFilter(MouseEvent.ANY, normalEvent);*/
                                    }

                                    canvas.add(imgViewBravo,6,6);

                                    String musicFileBravo = "samplesAmela/sounds/applause.mp3";

                                    Media soundBravo = new Media(new File(musicFileBravo).toURI().toString());
                                    MediaPlayer mediaPlayerBravo = new MediaPlayer(soundBravo);
                                    mediaPlayerBravo.play();

                                    count[0][1]=countDog;
                                    count[0][2]=countDog;
                                    count[1][1]=countDog;
                                    count[1][2]=countDog;
                                    count[2][1]=countDog;
                                    count[2][2]=countDog;

                                    count[10][1]=countCat;
                                    count[10][2]=countCat;
                                    count[11][1]=countCat;
                                    count[11][2]=countCat;
                                    count[12][1]=countCat;
                                    count[12][2]=countCat;

                                    count[0][11]=countFox;
                                    count[0][12]=countFox;
                                    count[1][11]=countFox;
                                    count[1][12]=countFox;
                                    count[2][11]=countFox;
                                    count[2][12]=countFox;

                                    count[10][11]=countHorse;
                                    count[10][12]=countHorse;
                                    count[11][11]=countHorse;
                                    count[11][12]=countHorse;
                                    count[12][11]=countHorse;
                                    count[12][12]=countHorse;

                                    HeatChart map = new HeatChart(count);

                                    // Step 2: Customise the chart.
                                    //map.setHighValueColour(java.awt.Color.red);
                                    //map.setLowValueColour(java.awt.Color.cyan);
                                    map.setTitle("Heat Chart");
                                    map.setXAxisLabel("X Axis");
                                    map.setYAxisLabel("Y Axis");

                                    // Step 3: Output the chart to a file.
                                    try {
                                        map.saveToFile(new File("Heat Chart.png"));
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }


                            }
                        });




                    }
                }
                else if(e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    entry = -1;


                }

            }

        };

    }


        //normal event handler heat map
        private EventHandler<Event> buildEvent () {

            return new EventHandler<Event>() {


                @Override
                public void handle(Event e) {
                    int row=0, col =0, j=0;
                    Object source = e.getSource();
                    ImageView x = (ImageView)source;
                    int y =Integer.parseInt(x.getId());
                    row =  y/ 13;
                    col =  y% 13;


                    if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                        System.out.println("I entered");
                        count[row][col]++;
                        //System.out.println("count["+row+"]["+col+"] = "+count[row][col]);

                    } else if (e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_MOVED) {

                        System.out.println("I moved");
                        count[row][col]++;
                        //System.out.println("count["+row+"]["+col+"] = "+count[row][col]);

                    } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                        //nothing happens with the count
                        //what to put?

                    }

                }

            };

        }

    public ImagePattern createGridPattern() {

        int nr_size1=170;
        int nr_size2=210;

        imgViewCat.setFitWidth(nr_size2);
        imgViewCat.setFitHeight(nr_size1);

        imgViewDog.setFitWidth(nr_size2);
        imgViewDog.setFitHeight(nr_size1);

        imgViewHorse.setFitWidth(nr_size2);
        imgViewHorse.setFitHeight(nr_size1);

        imgViewFox.setFitWidth(nr_size2);
        imgViewFox.setFitHeight(nr_size1);

        imgViewbTick.setFitWidth(nr_size2);
        imgViewbTick.setFitHeight(nr_size1);

        imgViewblTick.setFitWidth(nr_size2);
        imgViewblTick.setFitHeight(nr_size1);

        imgViewgTick.setFitWidth(nr_size2);
        imgViewgTick.setFitHeight(nr_size1);

        imgViewrTick.setFitWidth(nr_size2);
        imgViewrTick.setFitHeight(nr_size1);

        tick_Images.add(imgViewbTick);
        tick_Images.add(imgViewblTick);
        tick_Images.add(imgViewgTick);
        tick_Images.add(imgViewrTick);

        for(ImageView i: imgViewList){
            i.setFitWidth(210);
            i.setFitHeight(170);
            i.setOpacity(0.0);
        }


        int j = 0;
        for(ImageView i: imgViewList){
            canvas.add(i,j%13,j/13);
            j+=1;
        }

        canvas.add(imgViewDog, 1,1);
        canvas.add(imgViewCat, 1,11);
        canvas.add(imgViewFox, 11,1);
        canvas.add(imgViewHorse, 11,11);



        for(ImageView i: imgViewList){
            GazeUtils.addEventFilter(i);
            normalEvent = buildEvent();
            i.addEventFilter(GazeEvent.ANY, normalEvent);
            i.addEventFilter(MouseEvent.ANY, normalEvent);
        }

        //Dog
        GazeUtils.addEventFilter(imgViewDog);
        dogEvent = buildDogEvent();
        imgViewDog.addEventFilter(MouseEvent.ANY, dogEvent);
        imgViewDog.addEventFilter(GazeEvent.ANY, dogEvent);
        //Cat
        GazeUtils.addEventFilter(imgViewCat);
        catEvent = buildDogEvent();
        imgViewCat.addEventFilter(MouseEvent.ANY, catEvent);
        imgViewCat.addEventFilter(GazeEvent.ANY, catEvent);
        //Horse
        GazeUtils.addEventFilter(imgViewHorse);
        horseEvent = buildDogEvent();
        imgViewHorse.addEventFilter(MouseEvent.ANY, horseEvent);
        imgViewHorse.addEventFilter(GazeEvent.ANY, horseEvent);
        //Fox
        GazeUtils.addEventFilter(imgViewFox);
        foxEvent = buildDogEvent();
        imgViewFox.addEventFilter(MouseEvent.ANY, foxEvent);
        imgViewFox.addEventFilter(GazeEvent.ANY, foxEvent);


        Image image = canvas.snapshot(new SnapshotParameters(), null);
        ImagePattern pattern = new ImagePattern(image, 0, 0, 0, 0, false);

        return pattern;

    }

    public static void main(String[] args) {
        Application.launch(args);

    }



}
