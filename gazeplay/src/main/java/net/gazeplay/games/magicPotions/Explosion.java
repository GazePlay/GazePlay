package net.gazeplay.games.magicPotions;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.GameContext;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.utils.games.Utils;


public class Explosion extends Rectangle {

//    private final GameContext gameContext;
//
//    private final Dimension2D gameDimension;

    private static final int soundClipDuration = 5000;

    private static final int animationDelayDuration = 500;

    private static final int fadeInDuration = animationDelayDuration / 2; // check this

    private static final int delayBeforeNextRoundDuration = 1000;

    private static final String defaultPictureResourceLocation = "data/common/images/explosion-animation/exp-";

    private static final String defaultSoundResourceLocation = "data/common/sounds/explosion.mp3";

    private final String pictureResourceLocation;

    private final String soundResource;

    private double Width;
    private double Height;
    //private AudioClip soundClip;
    @Setter
    @Getter
    private Rectangle explosionFrame;
    @Setter
    @Getter
    private Image explosionFrameImage;
    @Getter
    private Timeline explosionTimeline;

    public Explosion() {

        this(defaultPictureResourceLocation, defaultSoundResourceLocation);
    }
    public Explosion(String pictureResourceLocation, String soundResourceLocation){

        //this.gameContext =;
        this.Width =  GazePlay.getInstance().getPrimaryStage().getWidth();
        this.Height = GazePlay.getInstance().getPrimaryScene().getHeight();
        //this.gameDimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.pictureResourceLocation = pictureResourceLocation;

        ClassLoader classLoader = this.getClass().getClassLoader();

        this.soundResource = soundResourceLocation;
        this.explosionFrameImage = new Image(defaultPictureResourceLocation+"1.png");
        this.explosionFrame = new Rectangle((Width - explosionFrameImage.getWidth())/2,Height - explosionFrameImage.getHeight(),explosionFrameImage.getWidth(), explosionFrameImage.getHeight());
        explosionFrame.setFill(new ImagePattern(explosionFrameImage));

        createAnimation();
    }
//    protected void nextFrame(int frameNb){
//        this.explosionFrameImage = new Image(defaultPictureResourceLocation+ frameNb +".png");
//        this.explosionFrame.setFill(new ImagePattern(explosionFrameImage));
//    }

    private void createAnimation(){

        //Group frames = new Group();
        explosionTimeline = new Timeline();
        explosionTimeline.setCycleCount(1);
        explosionTimeline.setAutoReverse(false);

        for(int i = 1; i <= 8 ; i ++){
            Image frameImage = new Image(defaultPictureResourceLocation+ i +".png");
            Rectangle Frame = new Rectangle(( Width - frameImage.getWidth())/2,Height - frameImage.getHeight(),frameImage.getWidth(), frameImage.getHeight());
            Frame.setFill(new ImagePattern(frameImage));
            //frames.getChildren().add(Frame);
            KeyValue kv = new KeyValue(Frame.widthProperty(), Frame.getWidth());
            KeyFrame kf = new KeyFrame(Duration.millis(650),kv);
            explosionTimeline.getKeyFrames().add(kf);

        }

        //return explosionTimeline;

           // log.debug("Playing graphic animation ...");
            explosionTimeline.play();

               // log.debug("Playing sound animation ...");
                try {
                    Utils.playSound(soundResource);
                } catch (Exception e) {

                    //log.warn("file doesn't exist : {}", soundResource);
                    //log.warn(e.getMessage());
                }


            //log.debug("Finished JavaFX task");
    }

    public void playExplosion(EventHandler<ActionEvent> onFinishedEventHandler){
        explosionTimeline.setOnFinished(actionEvent -> {
            //log.debug("finished explosion Timeline");
            onFinishedEventHandler.handle(actionEvent);
            //.debug("finished onFinishedEventHandler for explosion");
        });
    }
}

/*
* ===========================================================
 */

//import javafx.animation.Animation;
//import javafx.animation.Interpolator;
//import javafx.animation.Transition;
//import javafx.geometry.Dimension2D;
//import javafx.geometry.Rectangle2D;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.util.Duration;
//import lombok.Getter;
//import net.gazeplay.GameContext;
//
//public class Explosion {
//
//    private static final Image FRAME_IMAGE = new Image("data/common/images/explosion-animation/allFrames.png");
//
//    private static final int COLUMNS  =   4;
//
//    private static  int COUNT    =  8;
//    private static  int OFFSET_X =  18;
//    private static int OFFSET_Y =  25;
//    private static int WIDTH    = 374;
//    private static  int HEIGHT   = 243;
//    private final GameContext gc;
//    @Getter
//    private final ImageView imageView = new ImageView(FRAME_IMAGE);
//
//    public Explosion(GameContext gameContext){
//        this.gc = gameContext;
//        //Dimension2D gameDimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
//        imageView.setViewport(new Rectangle2D(0, 0, imageView.getFitWidth()/4, imageView.getFitHeight()/2));
//
//        final Animation animation = new FrameAnimation(imageView, Duration.millis(5000), COUNT, COLUMNS, OFFSET_X, OFFSET_Y, WIDTH, HEIGHT);
//
//        animation.setCycleCount(1);
//        animation.play();
//
//    }
//
//
//}
