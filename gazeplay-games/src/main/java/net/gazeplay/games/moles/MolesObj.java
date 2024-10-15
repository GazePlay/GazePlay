package net.gazeplay.games.moles;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.components.Portrait;

public class MolesObj extends Parent {

    @Getter
    private final double positionX;
    @Getter
    private final double positionY;

    private final Rectangle mole;

    private final Rectangle moleMoved;

    private final IGameContext gameContext;

    private final Moles gameInstance;

    private final double distTranslation;

    private final double width;
    private final double height;

    private boolean touched;
    private boolean canTouched;
    boolean canGoOut;
    public boolean out;

    private final ProgressIndicator progressIndicatorMoles;

    private Timeline timeMoleOut;
    private Timeline timelineProgressBarMoles;

    private final int timeMoleStayOut = 2500;

    public final EventHandler<Event> enterEvent;
    private int bonk;

    @Setter
    private int TargetAOIListIndex;

    //url bidoule if bidoule or default case, and DefaultUser if the user don't has a portrait
    private String url;

    MolesObj(
        final double positionX, final double positionY,
        final double width, final double height,
        final double distTrans,
        final IGameContext gameContext,
        final Moles gameInstance,
        final MolesGameVariant variant,
        final ReplayablePseudoRandom randomGenerator
    ) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.width = width;
        this.height = height;
        this.gameContext = gameContext;

        this.out = false;

        this.distTranslation = distTrans;

        touched = false;
        canTouched = false;
        canGoOut = true;

        this.gameInstance = gameInstance;

        this.enterEvent = buildEvent();

        this.progressIndicatorMoles = createProgressIndicatorMoles();

        final ImageLibrary imageLibrary = Portrait.createImageLibrary(randomGenerator);

        Image moleImage;

        moleImage = new Image("data/whackmole/images/molesCarrot.png");

        this.moleMoved = new Rectangle(positionX, positionY - distTrans, width, height);
        this.moleMoved.setFill(new ImagePattern(moleImage, 5, 5, 1, 1, true));
        this.moleMoved.opacityProperty().set(1);
        this.moleMoved.addEventFilter(MouseEvent.ANY, enterEvent);
        this.moleMoved.addEventFilter(GazeEvent.ANY, enterEvent);
        gameContext.getGazeDeviceManager().addEventFilter(this.moleMoved);

        this.mole = new Rectangle(positionX, positionY, width, height);
        this.mole.setFill(new ImagePattern(moleImage, 5, 5, 1, 1, true));
        this.getChildren().add(mole);
        this.mole.opacityProperty().set(0);

        bonk = 0;

    }

    private ProgressIndicator createProgressIndicatorMoles() {
        final ProgressIndicator indicatorMoles = new ProgressIndicator(0);
        indicatorMoles.setMinSize(this.width / 2, this.height / 2);
        indicatorMoles.setTranslateX(this.positionX + (distTranslation / 3));
        indicatorMoles.setTranslateY(this.positionY - distTranslation);
        indicatorMoles.setOpacity(0);
        indicatorMoles.setMouseTransparent(true);
        indicatorMoles.toFront();
        gameContext.getChildren().add(indicatorMoles);
        return indicatorMoles;
    }

    public EventHandler<Event> buildEvent() {
        return e -> {

            if (!canTouched || !out || touched) // If the mole is not out or already touched: nothing append
            {
                return;
            }

            /* If the mole is out and touched */
            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                progressIndicatorMoles.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                progressIndicatorMoles.setOpacity(1);
                progressIndicatorMoles.toFront();
                progressIndicatorMoles.setProgress(0);

                timelineProgressBarMoles = new Timeline();

                timelineProgressBarMoles.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(progressIndicatorMoles.progressProperty(), 1)));

                timelineProgressBarMoles.setOnFinished((ActionEvent actionEvent) -> {
                    canTouched = false;

                    if (!touched && out) {
                        gameContext.getSoundManager().add("data/whackmole/sounds/"+(bonk++%2==0 ? "bonk1.wav":"bonk2.wav"));
                        gameInstance.oneMoleWhacked();
                        touched = true;
                        goIn();
                    }
                });

                timelineProgressBarMoles.play();
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                if (timelineProgressBarMoles != null) {
                    timelineProgressBarMoles.stop();
                }

                progressIndicatorMoles.setOpacity(0);
                progressIndicatorMoles.setProgress(0);
            }
        };
    }

    /* This mole must go out */
    void getOut(ReplayablePseudoRandom random) {

        this.canGoOut = false;

        gameInstance.getNbObjOut().incrementAndGet();

        final TranslateTransition translation = new TranslateTransition(new Duration(1500), this);
        translation.setByX(0);
        translation.setByY(-this.distTranslation);

        this.mole.opacityProperty().set(0.5);

        translation.setOnFinished(actionEvent -> {

            canTouched = true;

            gameInstance.getGameContext().getChildren().add(moleMoved);
            gameContext.getGazeDeviceManager().addEventFilter(moleMoved);

            mole.opacityProperty().set(0);

            out = true;

            DoubleProperty timeOut = new SimpleDoubleProperty(0);
            timeMoleOut = new Timeline(); // New time this mole go out
            final int time = random.nextInt(timeMoleStayOut) + 2000;

            timeMoleOut.getKeyFrames()
                .add(new KeyFrame(new Duration(time),
                    new KeyValue(timeOut, 1)));
            timeMoleOut.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
            /* If the Mole is stay out without being touching */
            timeMoleOut.setOnFinished(actionEvent1 -> {
                if (!touched && out) {

                    goIn(); // Go back in the hole
                }
            });
            timeMoleOut.play();
        });

        translation.play();
    }

    private void goIn() {
        canTouched = false;
        out = false;
        progressIndicatorMoles.setOpacity(0);
        gameInstance.getTargetAOIList().get(TargetAOIListIndex).setTimeEnded(System.currentTimeMillis());

        this.mole.opacityProperty().set(0.5);
        gameInstance.getGameContext().getChildren().remove(moleMoved);
        gameContext.getGazeDeviceManager().removeEventFilter(this.moleMoved);

        double timeGoIn = 1500;
        if (touched) {
            timeGoIn = 500;
        }
        touched = false;
        final TranslateTransition translation = new TranslateTransition(new Duration(timeGoIn), this);
        translation.setByX(0);
        translation.setByY(this.distTranslation);
        translation.setOnFinished(actionEvent -> {
            gameInstance.getNbObjOut().decrementAndGet();
            mole.opacityProperty().set(0);
            canGoOut = true;
        });
        translation.play();
    }
}
