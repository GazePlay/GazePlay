package samplesAmela;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;
import java.util.Random;


/**
 * Created by Amela Fejza on 3/10/2017.
 */

public class SomeImagesAdvanced extends Application {



    public void start(Stage primaryStage) {

        primaryStage.setTitle("Animal Images");

        String musicFileHorse = "samplesAmela/sounds/horse.m4a";
        Media soundHorse = new Media(new File(musicFileHorse).toURI().toString());
        MediaPlayer mediaPlayerHorse = new MediaPlayer(soundHorse);
        //mediaPlayerHorse.play();

        String musicFileCat = "samplesAmela/sounds/cat.m4a";
        Media soundCat = new Media(new File(musicFileCat).toURI().toString());
        MediaPlayer mediaPlayerCat = new MediaPlayer(soundCat);
        //mediaPlayerCat.play();

        String musicFileDog = "samplesAmela/sounds/dog.m4a";
        Media soundDog = new Media(new File(musicFileDog).toURI().toString());
        MediaPlayer mediaPlayerDog = new MediaPlayer(soundDog);
        //mediaPlayerDog.play();

        String[] myStringArray = {musicFileHorse, musicFileCat ,musicFileDog};
        int idx = new Random().nextInt(myStringArray.length);
        String random = (myStringArray[idx]);
        System.out.println(random);



        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);


        Image img = new Image("samplesAmela/images/cat.jpg");
        Image img2 = new Image("samplesAmela/images/dog.jpg");
        Image img3 = new Image("samplesAmela/images/horse.jpg");
        Image img4 = new Image("samplesAmela/images/bravo.jpg");

        ImageView imgView4 = new ImageView(img4);
        imgView4.setFitHeight(200);
        imgView4.setFitWidth(200);

        ImageView imgView1 = new ImageView(img);
        imgView1.setFitHeight(200);
        imgView1.setFitWidth(200);

        ImageView imgView2 = new ImageView(img2);
        imgView2.setFitHeight(200);
        imgView2.setFitWidth(200);

        ImageView imgView3 = new ImageView(img3);
        imgView3.setFitHeight(200);
        imgView3.setFitWidth(200);

        if(random.equals(musicFileCat)){
            mediaPlayerCat.play();

            imgView1.setOnMouseClicked((MouseEvent e) -> {

                hbox.getChildren().removeAll(imgView1, imgView2, imgView3);
                hbox.getChildren().add(imgView4);

                String musicFileBravo = "samplesAmela/sounds/applause.mp3";

                Media soundBravo = new Media(new File(musicFileBravo).toURI().toString());
                MediaPlayer mediaPlayerBravo = new MediaPlayer(soundBravo);
                mediaPlayerBravo.play();

                System.out.println("Image 1 Clicked!");
            });

            imgView2.setOnMouseClicked((MouseEvent e) -> {

                System.out.println("Image 2 Clicked!");
            });

            imgView3.setOnMouseClicked((MouseEvent e) -> {



                System.out.println("Image 3 Clicked!");
            });

        }

        else if (random.equals(musicFileDog)){

            mediaPlayerDog.play();

            imgView1.setOnMouseClicked((MouseEvent e) -> {
                System.out.println("Image 1 Clicked!");
            });

            imgView2.setOnMouseClicked((MouseEvent e) -> {

                hbox.getChildren().removeAll(imgView1, imgView2, imgView3);
                hbox.getChildren().add(imgView4);

                String musicFileBravo = "samplesAmela/sounds/applause.mp3";

                Media soundBravo = new Media(new File(musicFileBravo).toURI().toString());
                MediaPlayer mediaPlayerBravo = new MediaPlayer(soundBravo);
                mediaPlayerBravo.play();

                System.out.println("Image 2 Clicked!");
            });

            imgView3.setOnMouseClicked((MouseEvent e) -> {

                System.out.println("Image 3 Clicked!");
            });

        }

        else if(random.equals(musicFileHorse)) {

            mediaPlayerHorse.play();

            imgView1.setOnMouseClicked((MouseEvent e) -> {
                System.out.println("Image 1 Clicked!");
            });

            imgView2.setOnMouseClicked((MouseEvent e) -> {

                System.out.println("Image 2 Clicked!");
            });

            imgView3.setOnMouseClicked((MouseEvent e) -> {

                hbox.getChildren().removeAll(imgView1, imgView2, imgView3);
                hbox.getChildren().add(imgView4);

                String musicFileBravo = "samplesAmela/sounds/applause.mp3";

                Media soundBravo = new Media(new File(musicFileBravo).toURI().toString());
                MediaPlayer mediaPlayerBravo = new MediaPlayer(soundBravo);
                mediaPlayerBravo.play();

                System.out.println("Image 3 Clicked!");
            });

        }

        hbox.getChildren().addAll(imgView1, imgView2, imgView3);
        Scene scene = new Scene(hbox);
        primaryStage.setScene(scene);
        primaryStage.show();

    }



    public static void main(String[] args) {
        launch();
    }
}

