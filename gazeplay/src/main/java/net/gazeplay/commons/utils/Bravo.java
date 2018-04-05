package net.gazeplay.commons.utils;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import lombok.Setter;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;

/**
 * Created by schwab on 30/10/2016.
 */
@Slf4j
public class Bravo extends Rectangle {

    private static final int soundClipDuration = 6000;

    private static final int zoomInAndOutCyclesCount = 3;

    private static final int animationDelayDuration = 500;

    private static final int zoomInDuration = (soundClipDuration - animationDelayDuration) / (zoomInAndOutCyclesCount);

    private static final int fadeInDuration = zoomInDuration / 2;

    private static final int delayBeforeNextRoundDuration = 1000;

    /**
     * initially, the image will take this ratio of the scene height double between 0 and 1
     */
    private static final double pictureInitialHeightToSceneHeightRatio = 1d / 3d;

    /**
     * when fully scaled, the image will take this ratio of the scene height double between 0 and 1
     */
    private static final double pictureFinalHeightToSceneHeightRatio = 0.95d;

    private static final String defaultPictureResourceLocation = "data/common/images/bravo.png";

    private static final String defaultSoundResourceLocation = "data/common/sounds/applause.mp3";

    private final String pictureResourceLocation;

    // private final String soundResourceLocation;

    private final URL soundResourceUrl;

    private AudioClip soundClip;

    private SequentialTransition fullTransition;

    private static final boolean DEFAULT_VALUE_ENABLE_REWARD_SOUND = ConfigurationBuilder.DEFAULT_VALUE_ENABLE_REWARD_SOUND;

    @Setter
    private boolean enableRewardSound;

    public Bravo() {

        this(defaultPictureResourceLocation, defaultSoundResourceLocation,
                ConfigurationBuilder.createFromPropertiesResource().build().isEnableRewardSound());
    }

    public Bravo(String pictureResourceLocation, String soundResourceLocation, boolean enableRewardSound) {
        super(0, 0, 0, 0);
        this.pictureResourceLocation = pictureResourceLocation;
        // this.soundResourceLocation = soundResourceLocation;

        ClassLoader classLoader = this.getClass().getClassLoader();

        soundResourceUrl = classLoader.getResource(soundResourceLocation);

        soundClip = new AudioClip(soundResourceUrl.toExternalForm());

        fullTransition = createFullTransition();

        this.enableRewardSound = enableRewardSound;
    }

    public void playWinTransition(Scene scene, EventHandler<ActionEvent> onFinishedEventHandler) {
        playWinTransition(scene, 0, onFinishedEventHandler);
    }

    public void playWinTransition(Scene scene, long initialDelay, EventHandler<ActionEvent> onFinishedEventHandler) {
        resetState(scene);

        fullTransition.setOnFinished(actionEvent -> {
            log.debug("finished fullTransition");
            onFinishedEventHandler.handle(actionEvent);
            log.debug("finished onFinishedEventHandler");
        });

        delayedStart(initialDelay);
    }

    private void delayedStart(long initialDelay) {
        final Runnable uiRunnable = () -> {
            // set visible only after all other property as been set
            // and just before the animation starts
            setVisible(true);

            log.debug("Playing graphic animation ...");
            fullTransition.play();

            if (this.enableRewardSound) {
                log.debug("Playing sound animation ...");
                soundClip.play(BackgroundMusicManager.getInstance().volumeProperty().getValue());
            }

            log.debug("Finished JavaFX task");
        };

        final Runnable deferedAnimationRunnable = () -> {
            try {
                Thread.sleep(initialDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("Adding task to JavaFX thread queue ...");
            Platform.runLater(uiRunnable);
        };

        Thread startPlayingThread = new Thread(deferedAnimationRunnable);

        log.debug("Starting new thread ...");
        startPlayingThread.start();
    }

    private SequentialTransition createFullTransition() {
        PauseTransition delayBetweenSoundStartAndAnimationStartTransition = new PauseTransition(
                Duration.millis(animationDelayDuration));
        delayBetweenSoundStartAndAnimationStartTransition
                .setOnFinished(actionEvent -> log.debug("finished delayBetweenSoundStartAndAnimationStartTransition"));

        PauseTransition delayAfterAnimationEndsBeforeStartingNextRoundTransition = new PauseTransition(
                Duration.millis(delayBeforeNextRoundDuration));
        delayAfterAnimationEndsBeforeStartingNextRoundTransition.setOnFinished(
                actionEvent -> log.debug("finished delayAfterAnimationEndsBeforeStartingNextRoundTransition"));

        FadeTransition fadeInTransition = createFadeInTransition();

        Transition scaleTransition = createScaleTransition();

        ParallelTransition animationTransition = new ParallelTransition();
        animationTransition.getChildren().add(fadeInTransition);
        animationTransition.getChildren().add(scaleTransition);
        animationTransition.setOnFinished(actionEvent -> log.debug("finished animationTransition"));

        SequentialTransition fullTransition = new SequentialTransition();
        fullTransition.getChildren().add(delayBetweenSoundStartAndAnimationStartTransition);
        fullTransition.getChildren().add(animationTransition);
        fullTransition.getChildren().add(delayAfterAnimationEndsBeforeStartingNextRoundTransition);
        return fullTransition;
    }

    private void resetState(Scene scene) {
        Image image = new Image(pictureResourceLocation);

        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double imageHeightToWidthRatio = imageHeight / imageWidth;

        double initialHeight = scene.getHeight() * pictureInitialHeightToSceneHeightRatio;
        double initialWidth = initialHeight / imageHeightToWidthRatio;

        double positionX = (scene.getWidth() - initialWidth) / 2;
        double positionY = (scene.getHeight() - initialHeight) / 2;

        setFill(new ImagePattern(image));

        setX(positionX);
        setY(positionY);
        setWidth(initialWidth);
        setHeight(initialHeight);

        setTranslateX(0);
        setScaleX(1);
        setScaleY(1);
        setScaleZ(1);

        setOpacity(0);

        toFront(); // bug when it is uncommented (with bloc at least).
    }

    private ScaleTransition createScaleTransition() {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(zoomInDuration), this);
        scaleTransition.setInterpolator(Interpolator.LINEAR);

        // scale to the actual height of the scene
        // so that the image takes the full height of the scene when it is fully scaled
        double scaleRatio = (1 / pictureInitialHeightToSceneHeightRatio) / (1 / pictureFinalHeightToSceneHeightRatio)
                - 1d;
        scaleTransition.setByX(scaleRatio);
        scaleTransition.setByY(scaleRatio);

        scaleTransition.setCycleCount(zoomInAndOutCyclesCount);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setOnFinished(actionEvent -> log.debug("finished scaleTransition"));
        return scaleTransition;
    }

    private FadeTransition createFadeInTransition() {
        FadeTransition fadeInTransition = new FadeTransition(new Duration(fadeInDuration), this);
        fadeInTransition.setFromValue(0.0);
        fadeInTransition.setToValue(1.0);
        fadeInTransition.setCycleCount(1);
        fadeInTransition.setAutoReverse(false);
        fadeInTransition.setOnFinished(actionEvent -> log.debug("finished fadeInTransition"));
        return fadeInTransition;
    }
}
