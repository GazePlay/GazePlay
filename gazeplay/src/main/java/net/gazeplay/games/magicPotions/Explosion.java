package net.gazeplay.games.magicPotions;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Region;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.Image;
import javafx.util.Duration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.AsyncUiTaskExecutor;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.Utils;

public class Explosion extends Rectangle {

    private static final int soundClipDuration = 5000;

    private static final int animationDelayDuration = 500;

    private static final int fadeInDuration = animationDelayDuration / 2; // check this

    private static final int delayBeforeNextRoundDuration = 1000;

    private static final String defaultPictureResourceLocation = "data/common/images/explosion.gif";

    private static final String defaultSoundResourceLocation = "data/common/sounds/explosion.mp3";

    private final String pictureResourceLocation;

    private final String soundResource;

    private AudioClip soundClip;

    private SequentialTransition fullTransition;

    @Setter
    private boolean enableRewardSound;

    public Explosion() {

        this(defaultPictureResourceLocation, defaultSoundResourceLocation,
                Configuration.getInstance().isEnableRewardSound());
    }

    public Explosion(String pictureResourceLocation, String soundResourceLocation, boolean enableRewardSound) {
        super(0, 0, 0, 0);
        // this.gameContext =;
        // this.Width = GazePlay.getInstance().getPrimaryStage().getWidth();
        // this.Height = GazePlay.getInstance().getPrimaryScene().getHeight();
        // this.gameDimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.pictureResourceLocation = pictureResourceLocation;

        ClassLoader classLoader = this.getClass().getClassLoader();

        this.soundResource = soundResourceLocation;
        // this.explosionFrameImage = new Image(defaultPictureResourceLocation+"1.png");
        // this.explosionFrame = new Rectangle((Width - explosionFrameImage.getWidth())/2,Height -
        // explosionFrameImage.getHeight(),explosionFrameImage.getWidth(), explosionFrameImage.getHeight());
        // explosionFrame.setFill(new ImagePattern(explosionFrameImage));

        this.fullTransition = createAnimation();
        this.enableRewardSound = enableRewardSound;
    }

    private SequentialTransition createAnimation() {
        PauseTransition delayAfterAnimationEndsBeforeStartingNextRoundTransition = new PauseTransition(
                Duration.millis(delayBeforeNextRoundDuration));
        FadeTransition fade = createFadeTransition();
        Transition scale = createScaleTransition();

//        ParallelTransition parallelTransition = new ParallelTransition();
//        parallelTransition.getChildren().add()

        SequentialTransition full = new SequentialTransition();
        full.getChildren().add(scale);
        full.getChildren().add(delayAfterAnimationEndsBeforeStartingNextRoundTransition);
        full.getChildren().add(fade);

        return full;
    }

    private FadeTransition createFadeTransition() {
        FadeTransition fadeInTransititon = new FadeTransition(new Duration((fadeInDuration)), this);
        fadeInTransititon.setFromValue(1.0);
        fadeInTransititon.setToValue(0.0);
        fadeInTransititon.setCycleCount(1);
        fadeInTransititon.setAutoReverse(false);
        // fadeInTransititon.setOnFinished(actionEvent -> log.debug("fade transition for explosion has finished"));
        return fadeInTransititon;
    }

    public void playExplosion(final Region root, EventHandler<ActionEvent> onFinishedEventHandler) {
        resetState(root);
        delayedStart(0);

    }
    private ScaleTransition createScaleTransition(){
        ScaleTransition scale = new ScaleTransition(Duration.millis(1000),this);
        //Image img = new Image(pictureResourceLocation);

        scale.setByX(1);
        scale.setByY(1);
        scale.setCycleCount(1);
        scale.setAutoReverse(false);
        return  scale;
    }

    private void delayedStart(long initialDelay) {
        final Runnable uiRunnable = () -> {
            // set visible only after all other property as been set
            // and just before the animation starts
            setVisible(true);

            // log.debug("Playing graphic animation ...");
            fullTransition.play();

            if (this.enableRewardSound) {
                // log.debug("Playing sound animation ...");
                try {
                    Utils.playSound(soundResource);
                } catch (Exception e) {

                    // log.warn("file doesn't exist : {}", soundResource);
                    // log.warn(e.getMessage());
                }
            }

            // log.debug("Finished JavaFX task");
        };
        final Runnable deferedAnimationRunnable = () -> {
            try {
                Thread.sleep(initialDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // log.debug("Adding task to JavaFX thread queue ...");
            Platform.runLater(uiRunnable);
        };

        AsyncUiTaskExecutor.getInstance().getExecutorService().submit(deferedAnimationRunnable);
    }

    private void resetState(Region root) {
        Image img = new Image(pictureResourceLocation);
        double imgWidth = img.getWidth();
        double imgHeight = img.getHeight();

        double posX = (root.getWidth() - imgWidth) / 2;
        double posY = (root.getHeight() - imgHeight);

        setFill(new ImagePattern(img));
        setX(posX);
        setY(posY);
        setWidth(imgWidth);
        setHeight(imgHeight);

        setTranslateX(0);
        setScaleX(1);
        setScaleY(1);
        setScaleZ(1);

        setOpacity(0);

        toFront();

    }
}
