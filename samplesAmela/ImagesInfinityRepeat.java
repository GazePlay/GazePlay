package samplesAmela;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Random;

/**
 * Created by Amela Fejza on 4/7/2017.
 */
public class ImagesInfinityRepeat extends Application {

    private static Scene sceneFirst;
    private static HBox hboxFirst;

    private static String firstImage1;
    private static String firstImage2;
    private static String firstImage3;
    private static String firstImage4;

    private static String musicFileH;
    private static String musicFileD;
    private static String musicFileC;
    private static String musicFileApp;

    private static ImageView imgView1;
    private static ImageView imgView2;
    private static ImageView imgView3;
    private static ImageView imgView4;



    public void createAnimation(){

        Animation animation = new Transition() {

            {
                setCycleDuration(Duration.millis(3000));
                hboxFirst.getChildren().removeAll(imgView1, imgView2, imgView3);
                hboxFirst.getChildren().add(imgView4);
                String musicFileBravo = "samplesAmela/sounds/applause.mp3";

                Media soundBravo = new Media(new File(musicFileBravo).toURI().toString());
                MediaPlayer mediaPlayerBravo = new MediaPlayer(soundBravo);
                mediaPlayerBravo.play();
            }

            @Override
            protected void interpolate(double frac) {
                final int n = Math.round(3000 * (float) frac);

            }
        };

        animation.play();

        animation.setOnFinished(new EventHandler<ActionEvent>() {


            @Override
            public void handle(ActionEvent actionEvent) {

                hboxFirst.getChildren().removeAll(imgView4);
                createGame(sceneFirst, hboxFirst, firstImage1, firstImage2, firstImage3, firstImage4, musicFileH, musicFileC, musicFileD, musicFileApp );

            }
        });

    }

    public static void main(String[] args) {
        Application.launch();
    }

    public void start(Stage primaryStage) {


        primaryStage.setTitle("Animal Images");


        hboxFirst=new HBox();
        hboxFirst.setPadding(new Insets(15, 12, 15, 12));
        hboxFirst.setSpacing(10);


        sceneFirst = new Scene(hboxFirst);

        createGame(sceneFirst, hboxFirst,"samplesAmela/images/cat.jpg" , "samplesAmela/images/dog.jpg", "samplesAmela/images/horse.jpg",
                "samplesAmela/images/bravo.jpg","samplesAmela/sounds/horse.m4a", "samplesAmela/sounds/cat.m4a",
                "samplesAmela/sounds/dog.m4a", "samplesAmela/sounds/applause.mp3" );
        primaryStage.setScene(sceneFirst);
        primaryStage.show();


    }

    public void createGame(Scene scene,HBox hbox, String imag1, String imag2, String imag3, String imag4, String musicFileHorse, String musicFileCat, String musicFileDog, String musicFileBravo ){

        sceneFirst = scene;
        hboxFirst = hbox;
        firstImage1 = imag1;
        firstImage2 = imag2;
        firstImage3 = imag3;
        firstImage4 = imag4;
        musicFileH = musicFileHorse;
        musicFileC = musicFileCat;
        musicFileD = musicFileDog;
        musicFileApp = musicFileBravo;

        Media soundHorse = new Media(new File(musicFileH).toURI().toString());
        MediaPlayer mediaPlayerHorse = new MediaPlayer(soundHorse);


        Media soundCat = new Media(new File(musicFileC).toURI().toString());
        MediaPlayer mediaPlayerCat = new MediaPlayer(soundCat);



        Media soundDog = new Media(new File(musicFileD).toURI().toString());
        MediaPlayer mediaPlayerDog = new MediaPlayer(soundDog);


        String[] myStringArray = {musicFileH, musicFileC, musicFileD};
        int idx = new Random().nextInt(myStringArray.length);
        String random = (myStringArray[idx]);
        System.out.println(random);


        Image img = new Image(firstImage1);
        Image img2 = new Image(firstImage2);
        Image img3 = new Image(firstImage3);
        Image img4 = new Image(firstImage4);



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

        hboxFirst.getChildren().addAll(imgView1, imgView2, imgView3);

        if (random.equals(musicFileC)) {
            mediaPlayerCat.play();

            imgView1.setOnMouseClicked((MouseEvent e) -> {
                createAnimation();

            });

            imgView2.setOnMouseClicked((MouseEvent e) -> {

                System.out.println("Image 2 Clicked!");
            });

            imgView3.setOnMouseClicked((MouseEvent e) -> {


                System.out.println("Image 3 Clicked!");
            });

        } else if (random.equals(musicFileD)) {

            mediaPlayerDog.play();

            imgView1.setOnMouseClicked((MouseEvent e) -> {
                System.out.println("Image 1 Clicked!");
            });

            imgView2.setOnMouseClicked((MouseEvent e) -> {


                createAnimation();

            });

            imgView3.setOnMouseClicked((MouseEvent e) -> {

                System.out.println("Image 3 Clicked!");
            });

        } else if (random.equals(musicFileH)) {

            mediaPlayerHorse.play();

            imgView1.setOnMouseClicked((MouseEvent e) -> {
                System.out.println("Image 1 Clicked!");
            });

            imgView2.setOnMouseClicked((MouseEvent e) -> {

                System.out.println("Image 2 Clicked!");
            });

            imgView3.setOnMouseClicked((MouseEvent e) -> {


                createAnimation();

            });

        }


    }


}
