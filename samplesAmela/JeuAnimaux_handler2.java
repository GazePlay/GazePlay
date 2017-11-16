package samplesAmela;

import gaze.GazeEvent;
import gaze.GazeUtils;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.*;

/**
 * Created by Amela Fejza on 5/24/2017.
*/

public class JeuAnimaux_handler2 extends Application {

    private static Scene sceneFirst;
    private static HBox hboxFirst;

    private static String firstImage4;

    private static String music;
    private static String musicFileApp;

    private static ImageView imgView1;
    private static ImageView imgView2;
    private static ImageView imgView3;
    private static ImageView imgView4;
    private static ImageView imgViewClick;
    private static EventHandler<Event> aEvent;
    private static FadeTransition ft;
    private int number;
    long entry;
    final double min_time = 1000;
    Bubble bubble;


    public static void main(String[] args)  {
        Application.launch();
    }


    private  EventHandler<Event> createAnimation() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {


                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {


                    System.out.println("I eggcited");
                    entry = (new Date()).getTime();
                }

                if (e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_MOVED){


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
                                hboxFirst.getChildren().removeAll(imgView1, imgView2, imgView3);
                                hboxFirst.getChildren().add(imgView4);

                                String musicFileBravo = "samplesAmela/sounds/applause.mp3";

                                Media soundBravo = new Media(new File(musicFileBravo).toURI().toString());
                                MediaPlayer mediaPlayerBravo = new MediaPlayer(soundBravo);
                                mediaPlayerBravo.play();

                                System.out.println("Image 1 Clicked! BRAVO!");


                                //create an animation just to remove bravo and add bubbles
                                Animation animation = new Transition() {

                                    {
                                        setCycleDuration(Duration.millis(3000));
                                        //hboxFirst.getChildren().add(bubble);


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

                                        hboxFirst.getChildren().removeAll(imgView4);
                                        //hboxFirst.getChildren().remove(bubble);
                                        createGame(sceneFirst, hboxFirst);
                                    }
                                });
                            }
                        });


                    }
                }
                if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED)

                            {

                                entry = -1;
                            }



        }

        };
    }


    public void start(Stage primaryStage)  {


        primaryStage.setTitle("Animal Images");


        hboxFirst=new HBox();
        hboxFirst.setPadding(new Insets(15, 12, 15, 12));
        hboxFirst.setSpacing(10);



        sceneFirst = new Scene(hboxFirst);

        createGame(sceneFirst, hboxFirst);
        primaryStage.setScene(sceneFirst);
        primaryStage.show();


    }

    public void createGame(Scene scene,HBox hbox ){

        sceneFirst = scene;
        hboxFirst = hbox;

        //firstImage4 = imag4;


        //musicFileApp = musicFileBravo;

        String a = System.getProperties().getProperty("user.home"); //get the User.Home
        String pathImg=a + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "images";
        File fImage = new File(pathImg);
        ArrayList<String> imgNames = new ArrayList<String>(Arrays.asList(fImage.list()));

        //I have put the names of chosen files in namesShuffle

        ArrayList<String> imgNamesShuffle = new ArrayList<>();
        Collections.shuffle(imgNames);
        imgNamesShuffle.add(imgNames.get(0));
        imgNamesShuffle.add(imgNames.get(1));
        imgNamesShuffle.add(imgNames.get(2));


        ArrayList<String> imgCorrect = new ArrayList<>();

        for(int i=0;i<imgNamesShuffle.size();i++){
            int in = imgNamesShuffle.get(i).indexOf(".");
            String element = imgNamesShuffle.get(i).substring(0,in);
            imgCorrect.add(element);
        }
        //now imgCorrect has the names of images without .jpg that we are going to put on HBox


        //Random function
        int idx = new Random().nextInt(imgCorrect.size());
        String random = (imgCorrect.get(idx));
        //System.out.println(random);
        //now we have the name of the element and need to connect with the sounds so we can get which sound to play


        String pathSound=a + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame" + File.separator + "sounds";
        File fSound = new File(pathSound);
        //ArrayList<String> soundNames = new ArrayList<String>(Arrays.asList(fSound.list()));

        String random2;
        random2 = random + ".m4a";
        //String yes=null;

        /*for(int i=0;i<soundNames.size();i++){
            if(random2.equals(soundNames.get(i))){
                yes = soundNames.get(i);
            }
        }*/

       //System.out.println(yes);

        System.out.println(random2);

        Media sound = new Media(new File(pathSound+ File.separator+random2).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);

        String firstImage1=imgNamesShuffle.get(0);
        String firstImage2=imgNamesShuffle.get(1);
        String firstImage3=imgNamesShuffle.get(2);


        File myFile1=new File(pathImg + File.separator + firstImage1);
        String myUrl1 = myFile1.toURI().toString();

        File myFile2=new File(pathImg + File.separator + firstImage2);
        String myUrl2 = myFile2.toURI().toString();

        File myFile3=new File(pathImg + File.separator + firstImage3);
        String myUrl3 = myFile3.toURI().toString();

        File myFile4=new File(System.getProperties().getProperty("user.home") + File.separator + "GazePlay" + File.separator + "files" + File.separator + "myGame"+File.separator+"bravo.jpg");
        String myUrl4 = myFile4.toURI().toString();

        Image img = new Image( myUrl1);
        Image img2 = new Image(myUrl2);
        Image img3 = new Image(myUrl3);
        Image img4 = new Image(myUrl4);


        imgView1 = new ImageView(img);
        imgView1.setFitHeight(200);
        imgView1.setFitWidth(200);

        imgView2 = new ImageView(img2);
        imgView2.setFitHeight(200);
        imgView2.setFitWidth(200);

        imgView3 = new ImageView(img3);
        imgView3.setFitHeight(200);
        imgView3.setFitWidth(200);

        imgView4 = new ImageView(img4);
        imgView4.setFitHeight(200);
        imgView4.setFitWidth(200);

        switch (number){
            case 1:  imgViewClick = imgView1; break;
            case 2: imgViewClick = imgView2; break;
            case 3: imgViewClick = imgView3; break;
        }

        hboxFirst.getChildren().addAll(imgView1, imgView2, imgView3);

        random = random + ".jpg";
        mediaPlayer.play();
        if(random.equals(firstImage1)){
            number=1;
            imgViewClick=imgView1;

            GazeUtils.addEventFilter(imgViewClick);
            aEvent = createAnimation();
            imgViewClick.addEventFilter(MouseEvent.ANY, aEvent);
            imgViewClick.addEventFilter(GazeEvent.ANY, aEvent);


        } else if (random.equals(firstImage2)) {
            number=2;
            imgViewClick=imgView2;
            //mediaPlayerDog.play();

            GazeUtils.addEventFilter(imgViewClick);
            aEvent = createAnimation();
            imgViewClick.addEventFilter(MouseEvent.ANY, aEvent);
            imgViewClick.addEventFilter(GazeEvent.ANY, aEvent);

        } else if (random.equals(firstImage3)) {
            number =3;
            imgViewClick=imgView3;
            //mediaPlayerHorse.play();

            GazeUtils.addEventFilter(imgViewClick);
            aEvent = createAnimation();
            imgViewClick.addEventFilter(MouseEvent.ANY, aEvent);
            imgViewClick.addEventFilter(GazeEvent.ANY, aEvent);

        }
    }



}
