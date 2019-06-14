package net.gazeplay.games.magicPotions;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import net.gazeplay.GameContext;

public class Explosion {

    private static final Image FRAME_IMAGE = new Image("data/common/images/explosion-animation/allFrames.png");

    private static final int COLUMNS  =   4;

    private static final int COUNT    =  10;
    private static final int OFFSET_X =  18;
    private static final int OFFSET_Y =  25;
    private static final int WIDTH    = 374;
    private static final int HEIGHT   = 243;

    private final ImageView imageView = new ImageView(FRAME_IMAGE);

    public Explosion(){
        imageView.setViewport(new Rectangle2D(OFFSET_X, OFFSET_Y, WIDTH, HEIGHT));

        final Animation animation = new FrameAnimation(imageView, Duration.millis(5000), COUNT, COLUMNS, OFFSET_X, OFFSET_Y, WIDTH, HEIGHT);

        animation.setCycleCount(1);
        animation.play();

    }


}




//
//import javafx.animation.*;
//import javafx.geometry.Dimension2D;
//import javafx.scene.image.Image;
//import javafx.scene.media.AudioClip;
//import javafx.scene.paint.ImagePattern;
//import javafx.scene.shape.Rectangle;
//import javafx.util.Duration;
//import lombok.Getter;
//import lombok.Setter;
//import net.gazeplay.GameContext;
//
//import java.util.function.DoubleBinaryOperator;
//
//
//public class Explosion extends Transition {
//
//    private final GameContext gameContext;
//
//    private final Dimension2D gameDimension;
//
//    private static final int soundClipDuration = 5000;
//
//    private static final int animationDelayDuration = 500;
//
//    private static final int fadeInDuration = animationDelayDuration / 2; // check this
//
//    private static final int delayBeforeNextRoundDuration = 1000;
//
//    private static final String defaultPictureResourceLocation = "data/common/images/explosion-animation/exp-";
//
//    private static final String defaultSoundResourceLocation = "data/common/sounds/explosion.mp3";
//
//    private final String pictureResourceLocation;
//
//    private final String soundResource;
//
//    //private AudioClip soundClip;
//    @Setter
//    @Getter
//    private Rectangle explosionFrame;
//    @Setter
//    @Getter
//    private Image explosionFrameImage;
//
//    //private SequentialTransition fullTransition;
//
//    public Explosion(GameContext gc) {
//
//        this(defaultPictureResourceLocation, defaultSoundResourceLocation,gc);
//    }
//    public Explosion(String pictureResourceLocation, String soundResourceLocation, GameContext gc){
//
//        this.gameContext = gc;
//        this.gameDimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
//        this.pictureResourceLocation = pictureResourceLocation;
//
//        ClassLoader classLoader = this.getClass().getClassLoader();
//
//        this.soundResource = soundResourceLocation;
//        this.explosionFrameImage = new Image(defaultPictureResourceLocation+"1.png");
//        this.explosionFrame = new Rectangle((gameDimension.getWidth() - explosionFrameImage.getWidth())/2,gameDimension.getHeight() - explosionFrameImage.getHeight(),explosionFrameImage.getWidth(), explosionFrameImage.getHeight());
//        explosionFrame.setFill(new ImagePattern(explosionFrameImage));
//
//        createAnimation();
//    }
//    protected void interpolate(double k){
//        this.explosionFrameImage = new Image(defaultPictureResourceLocation+ (int) k +".png");
//        this.explosionFrame.setFill(new ImagePattern(explosionFrameImage));
//    }
//
//    private void createAnimation(){
//
//
//        final Timeline timeline = new Timeline();
//        timeline.setCycleCount(1);
//        timeline.setAutoReverse(false);
//        KeyFrame kf  = new KeyFrame(Duration.ZERO);
//        KeyValue kv = new KeyValue(explosionFrame.heightProperty(), explosionFrame.getHeight());;
//        kf = new KeyFrame(new Duration(625));  // end position
//        for(int frame = 2 ; frame < 9 ; frame ++ ){
//           // nextExplosionFrame(frame);
//            kf = new KeyFrame(new Duration(625*frame));
//            //nextExplosionFrame(frame);
//            //kv = new KeyValue();
//        }
//        timeline.getKeyFrames().addAll()
//
//    }
//
//}
