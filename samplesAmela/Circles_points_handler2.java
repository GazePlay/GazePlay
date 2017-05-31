package samplesAmela;

//choose the image not by clicking but by fading

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Amela Fejza on 5/30/2017.
 */
public class Circles_points_handler2 extends Application{

    double gridSize = 100;
    ProgressIndicator indicator;
    GridPane canvas ;
    int arraypts[][] ={
            {1, 4, 2, 1, 3, 4},   // tri
            {1, 1, 1, 5, 5, 5, 5, 1 },   // quad
            {2, 5, 2, 2, 4, 1, 6, 2, 6, 5 }   // pent

    };

    int array[] = {0,1,2};
    ArrayList<Integer> array_sizes = new ArrayList<Integer>();
    ArrayList<Circle> array_count = new ArrayList<Circle>();
    int count = 0;
    Circle circle;
    final double min_time = 1000;
    long entry;
    double center_x=50, center_y=50;
    Scene scene;
    HBox hboxFirst;
    int size, index;
    FadeTransition ft;
    Bubble bubble;

    public Circles_points_handler2(){
        for (int index = 0; index < array.length; index++)
        {
            array_sizes.add(array[index]);
        }
    }


    private static File myFile7 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "bravo.jpg");
    private static String bravo = myFile7.toURI().toString();
    private static Image imgBravo = new Image(bravo);
    private static ImageView imgViewBravo = new ImageView(imgBravo);

    private static ImageView imgViewClick;

    private static File myFile4 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "triangle.jpg");
    private static String triangle = myFile4.toURI().toString();
    private static Image imgTriangle = new Image(triangle);
    private static ImageView imgViewTriangle = new ImageView(imgTriangle);


    private static File myFile5 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "square.jpg");
    private static String square = myFile5.toURI().toString();
    private static Image imgSquare = new Image(square);
    private static ImageView imgViewSquare = new ImageView(imgSquare);

    private static File myFile6 = new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "myNode" + File.separator + "pentagon.png");
    private static String trapez = myFile6.toURI().toString();
    private static Image imgTrapez = new Image(trapez);
    private static ImageView imgViewTrapez = new ImageView(imgTrapez);
    private static EventHandler<Event> enterEvent;
    private static  EventHandler<Event> bravoEvent;

    //triangleAnimation triangle = new triangleAnimation();
    @Override
    public void start(Stage primaryStage) {

        canvas = new GridPane();
        canvas.setPrefSize(800,800);
        canvas.setGridLinesVisible(true);
        final int numCols = 9 ;
        final int numRows = 8 ;
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

        scene = new Scene(canvas, 800, 800);
        primaryStage.setScene(scene);
        bubble = new Bubble(scene);
        primaryStage.show();

        scene.setFill(createGridPattern());


    }


    public void createAnimation(){

        imgViewBravo.setFitWidth(200);
        imgViewBravo.setFitHeight(200);

        imgViewTriangle.setFitWidth(200);
        imgViewTriangle.setFitHeight(200);

        imgViewSquare.setFitWidth(200);
        imgViewSquare.setFitHeight(200);

        imgViewTrapez.setFitWidth(200);
        imgViewTrapez.setFitHeight(200);

        Animation animation = new Transition() {

            {
                setCycleDuration(Duration.millis(1000));

                canvas.getChildren().removeAll(indicator, circle);
                Circle c = new Circle();
                c.setRadius(20.0f);
                c.setOpacity(1);
                c.setFill(Color.LIGHTGRAY);
                c.setVisible(true);
                array_count.add(c);
                canvas.add(c,arraypts[index][count], arraypts[index][count+1]);

                count+=2;

                // root.getChildren().remove(indicator);
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
                if(count<size && count<4){
                    canvas.add(indicator, arraypts[index][count], arraypts[index][count+1]);
                    canvas.add(circle,arraypts[index][count], arraypts[index][count+1]);
                   /* array_count.add(circle.getCenterX());
                    array_count.add(circle.getCenterY());*/
                    //add the line
                   /* Line line = new Line(0,0,arraypts[index][count]*100,arraypts[index][count+1]*100 );
                    line.setStroke(Color.BLACK);
                    line.setStrokeWidth(3.0);
                    canvas.getChildren().add(line);*/

                }
                else if(count<size && count>2){

                    canvas.add(indicator, arraypts[index][count], arraypts[index][count+1]);
                    canvas.add(circle,arraypts[index][count], arraypts[index][count+1]);


                }

                else {
                    System.out.println("HELLOOO");
                    canvas.setGridLinesVisible(false);
                    //canvas.getChildren().clear();
                    canvas.getChildren().removeAll(circle, indicator);
                    for(Circle ar : array_count){
                        canvas.getChildren().remove(ar);
                    }
                    hboxFirst=new HBox();
                    // hboxFirst.setPadding(new Insets(5,5,5,5));
                    // hboxFirst.setPadding(new Insets(15, 12, 15, 12));

                    canvas.add(imgViewTriangle, 1, 1);
                    canvas.add(imgViewSquare, 3, 3);
                    canvas.add(imgViewTrapez, 5, 5);
                      /* String musicFileKot = "src/sample/sounds/applause.mp3";

                        Media kot = new Media(new File(musicFileKot).toURI().toString());
                        MediaPlayer mediaPlayerKot = new MediaPlayer(kot);
                        mediaPlayerKot.play();*/

                    switch (size){
                        case 6:  imgViewClick = imgViewTriangle; break;
                        case 8: imgViewClick = imgViewSquare; break;
                        case 10: imgViewClick = imgViewTrapez; break;
                    }

                    GazeUtils.addEventFilter(imgViewClick);
                    bravoEvent = buildBravoEvent();
                    imgViewClick.addEventFilter(MouseEvent.ANY, bravoEvent);
                    imgViewClick.addEventFilter(GazeEvent.ANY, bravoEvent);


                    //scene.setFill(Color.TRANSPARENT);
                        /*canvas.getChildren().removeAll(imgViewTriangle, imgViewSquare, imgViewTrapez);
                        canvas.add(imgViewBravo, 3, 3);

                        String musicFileBravo = "src/sample/sounds/applause.mp3";

                        Media soundBravo = new Media(new File(musicFileBravo).toURI().toString());
                        MediaPlayer mediaPlayerBravo = new MediaPlayer(soundBravo);
                        mediaPlayerBravo.play();

                        System.out.println("Image 1 Clicked! BRAVO!");*/
                        /*Animation animation = new Transition() {

                            {
                                setCycleDuration(Duration.millis(5000));
                                canvas.getChildren().add(bubble);


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
                                canvas.getChildren().remove(imgViewBravo);
                                canvas.getChildren().remove(bubble);

                                count=0;
                                Image image = canvas.snapshot(new SnapshotParameters(), null);
                                ImagePattern pattern = new ImagePattern(image, 0, 0, 0, 0, false);
                                pattern=createGridPattern();
                            }
                        });*/




                }
            }
        });


    }


    //bravo event handler
    private  EventHandler<Event> buildBravoEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {



                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    System.out.println("I eggcited");
                    entry = (new Date()).getTime();

                }

                else if (e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_MOVED) {

                    System.out.println("I moved");

                    long now = (new Date()).getTime();
                    //ft = new FadeTransition(Duration.millis(3000), imgViewClick );
                    System.out.println((now - entry) / min_time);
                    if ((now - entry) > min_time && entry != -1) {


                        Animation animation = new Transition() {

                            {
                                setCycleDuration(Duration.millis(2000));

                                ft = new FadeTransition(Duration.millis(1000), imgViewClick);
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
                                canvas.getChildren().removeAll(imgViewTriangle, imgViewSquare, imgViewTrapez);
                                canvas.add(imgViewBravo, 3, 3);

                                String musicFileBravo = "samplesAmela/sounds/applause.mp3";

                                Media soundBravo = new Media(new File(musicFileBravo).toURI().toString());
                                MediaPlayer mediaPlayerBravo = new MediaPlayer(soundBravo);
                                mediaPlayerBravo.play();

                                System.out.println("Image 1 Clicked! BRAVO!");


                                //create an animation just to remove bravo
                                Animation animation = new Transition() {

                                    {
                                        setCycleDuration(Duration.millis(3000));
                                        canvas.getChildren().add(bubble);


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
                                        canvas.getChildren().remove(imgViewBravo);
                                        canvas.getChildren().remove(bubble);

                                        count = 0;
                                        Image image = canvas.snapshot(new SnapshotParameters(), null);
                                        ImagePattern pattern = new ImagePattern(image, 0, 0, 0, 0, false);
                                        pattern = createGridPattern();
                                    }
                                });
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

    //event handler
    private  EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {



                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    System.out.println("I eggcited");
                    entry = (new Date()).getTime();
                    indicator.setVisible(true);
                    indicator.setOpacity(1);
                    circle.setOpacity(0.5);

                }
                else if (e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_MOVED) {

                    System.out.println("I moved");
                    indicator.setVisible(true);
                    indicator.setOpacity(1);
                    circle.setOpacity(0.5);
                    long now = (new Date()).getTime();
                    indicator.setProgress((now - entry)/min_time);
                    System.out.println((now - entry)/min_time);
                    if((now - entry)>min_time && entry != -1){
                        createAnimation();
                    }

                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    entry = -1;
                    indicator.setOpacity(1);
                    circle.setOpacity(1);

                }
            }
        };

    }



    public ImagePattern createGridPattern() {
        Collections.shuffle(array_sizes);
        index = array_sizes.get(0);
        size = arraypts[ index ].length;
        //size = 10;
        indicator = new ProgressIndicator(0);
        double w = gridSize;
        double h = gridSize;

        circle = new Circle();
        circle.setRadius(45.0f);

        circle.setOpacity(1);
        circle.setFill(Color.BLUE);
        circle.setVisible(true);
        canvas.setGridLinesVisible(true);

        GazeUtils.addEventFilter(circle);
        enterEvent = buildEvent();
        circle.addEventFilter(MouseEvent.ANY, enterEvent);
        circle.addEventFilter(GazeEvent.ANY, enterEvent);

        indicator.setOpacity(0.0);
        canvas.add(indicator, arraypts[index][count], arraypts[index][count+1]);
        canvas.add(circle,arraypts[index][count], arraypts[index][count+1]);
        Image image = canvas.snapshot(new SnapshotParameters(), null);
        ImagePattern pattern = new ImagePattern(image, 0, 0, w, h, false);

        return pattern;

    }


    public static void main(String[] args) {
        Application.launch(args);
    }


}
