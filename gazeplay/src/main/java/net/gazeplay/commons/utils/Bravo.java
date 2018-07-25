package net.gazeplay.commons.utils;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.AsyncUiTaskExecutor;
import net.gazeplay.GameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.Utils;

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

    private static final String defaultPictureResourceLocation = "data/common/images/bravo.gif";

    private static final String defaultSoundResourceLocation = "data/common/sounds/applause.mp3";

    private final String pictureResourceLocation;

    // private final String soundResourceLocation;

    private final String soundResource;

    private AudioClip soundClip;

    private SequentialTransition fullTransition;

    @Setter
    private boolean enableRewardSound;

    public Bravo() {

        this(defaultPictureResourceLocation, defaultSoundResourceLocation,
                Configuration.getInstance().isEnableRewardSound());
    }

    public Bravo(String pictureResourceLocation, String soundResourceLocation, boolean enableRewardSound) {
        super(0, 0, 0, 0);
        this.pictureResourceLocation = pictureResourceLocation;
        // this.soundResourceLocation = soundResourceLocation;

        ClassLoader classLoader = this.getClass().getClassLoader();

        soundResource = soundResourceLocation;

        fullTransition = createFullTransition();

        this.enableRewardSound = enableRewardSound;
    }

    public void playWinTransition(final Region root, EventHandler<ActionEvent> onFinishedEventHandler) {
        playWinTransition(root, 0, onFinishedEventHandler);
    }

    public void playWinTransition(final Region root, long initialDelay,
            EventHandler<ActionEvent> onFinishedEventHandler) {
        resetState(root);

        fullTransition.setOnFinished(actionEvent -> {
            log.debug("finished fullTransition");
            onFinishedEventHandler.handle(actionEvent);
            log.debug("finished onFinishedEventHandler");
        });

        delayedStart(initialDelay);
    }

    public void setConfetiOnStart(GameContext gc) {
        Dimension2D dim = gc.getGamePanelDimensionProvider().getDimension2D();

        for (int i = 0; i <= 100; i++) {
            Rectangle r = new Rectangle(-100, -100, dim.getHeight() / 30, dim.getHeight() / 15);
            r.setFill(Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
            gc.getChildren().add(r);

            Duration duration = Duration.millis(1500 + (Math.random() * 5000));

            TranslateTransition tt = new TranslateTransition(duration, r);
            tt.setInterpolator(Interpolator.LINEAR);
            tt.setFromX(Math.random() * (110 * dim.getWidth() / 100));
            tt.setFromY(0);
            tt.setToY(dim.getHeight());

            RotateTransition rt = new RotateTransition(duration, r);
            rt.setInterpolator(Interpolator.LINEAR);
            rt.setByAngle(-360 + Math.random() * 2 * 720);

            FadeTransition ft = new FadeTransition(duration, r);
            ft.setInterpolator(Interpolator.LINEAR);
            ft.setFromValue(0);
            ft.setToValue(0.8);

            ParallelTransition pt = new ParallelTransition();

            pt.getChildren().addAll(tt, rt, ft);

            pt.setOnFinished(actionEvent -> gc.getChildren().remove(r));
            pt.play();
        }
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
                Utils.playSound(soundResource);
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

        AsyncUiTaskExecutor.getInstance().getExecutorService().submit(deferedAnimationRunnable);
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

    private void resetState(Region root) {
        Image image = new Image(pictureResourceLocation);

        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double imageHeightToWidthRatio = imageHeight / imageWidth;

        double initialHeight = root.getHeight() * pictureInitialHeightToSceneHeightRatio;
        double initialWidth = initialHeight / imageHeightToWidthRatio;

        double positionX = (root.getWidth() - initialWidth) / 2;
        double positionY = (root.getHeight() - initialHeight) / 2;

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
