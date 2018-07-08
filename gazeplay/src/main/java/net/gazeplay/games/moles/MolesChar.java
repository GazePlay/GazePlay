package net.gazeplay.games.moles;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
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
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Random;

@Slf4j
public class MolesChar extends Parent {

    private Rectangle mole;

    private Rectangle moleMoved;

    private final GameContext gameContext;

    private final Moles gameInstance;

    private final double distTranslation;

    private boolean touched;
    private boolean canTouched;
    public boolean canGoOut;
    public boolean out;

    private ProgressIndicator progressIndicator;

    private Timeline timeMinToWhackTheMole;

    private final double timeMinToWhackMole = 400;

    private Timeline timeMoleOut;

    private final int timeMoleStayOut = 2500;

    private final double posX;
    private final double posY;

    final Stats stats;

    public final EventHandler<Event> enterEvent;

    public MolesChar(double positionX, double positionY, double width, double height, double distTrans,
            GameContext gameContext, Stats stats, Moles gameInstance) {

        this.gameContext = gameContext;

        this.stats = stats;

        this.out = false;

        this.distTranslation = distTrans;

        this.posX = positionX;

        this.posY = positionY;

        touched = false;
        canTouched = false;
        canGoOut = true;

        this.gameInstance = gameInstance;

        this.progressIndicator = createProgressIndicator(width, height);

        this.enterEvent = buildEvent();

        this.moleMoved = new Rectangle(this.posX, this.posY - distTrans, width, height);
        this.moleMoved.setFill(new ImagePattern(new Image("data/whackmole/images/bibouleMole.png"), 5, 5, 1, 1, true));
        this.moleMoved.opacityProperty().set(1);
        this.moleMoved.addEventHandler(MouseEvent.ANY, enterEvent);
        this.moleMoved.addEventHandler(GazeEvent.ANY, enterEvent);
        gameContext.getGazeDeviceManager().addEventFilter(this.moleMoved);

        this.mole = new Rectangle(this.posX, this.posY, width, height);
        this.mole.setFill(new ImagePattern(new Image("data/whackmole/images/bibouleMole.png"), 5, 5, 1, 1, true));
        this.getChildren().add(mole);
        this.mole.opacityProperty().set(0);

    }

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    public EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (!canTouched || !out || touched) // If the mole is not out or already touched: nothing append
                    return;

                /* If the mole is out and touched */
                if (out && canTouched
                        && (e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_MOVED
                                || e.getEventType() == MouseEvent.MOUSE_ENTERED
                                || e.getEventType() == GazeEvent.GAZE_ENTERED)) {

                    canTouched = false;
                    if (!touched && out) {
                        gameInstance.OneMoleWhacked();
                        touched = true;
                        goIn();
                    }

                    /*
                     * progressIndicator.setOpacity(0); progressIndicator.setProgress(0); timeMinToWhackTheMole = new
                     * Timeline(); timeMinToWhackTheMole.getKeyFrames().add(new KeyFrame(new
                     * Duration(timeMinToWhackMole), new KeyValue(progressIndicator.progressProperty(), 1)));
                     * timeMinToWhackTheMole.play(); timeMinToWhackTheMole.setOnFinished(new EventHandler<ActionEvent>()
                     * { // If the user watch the mole enough time (timeMinToWhackMole)
                     * 
                     * @Override public void handle(ActionEvent actionEvent) { canTouched = false; if (!touched && out)
                     * { gameInstance.OneMoleWacked(); touched = true; goIn(); }
                     * 
                     * } });
                     */

                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    Timeline timeline = new Timeline();

                    timeline.play();
                    if (timeMinToWhackTheMole != null)
                        timeMinToWhackTheMole.stop();
                    progressIndicator.setOpacity(0);
                    progressIndicator.setProgress(0);
                }
            }
        };
    }

    /* This mole must go out */
    public void getOut() {

        this.canGoOut = false;

        gameInstance.nbMolesOut++;

        TranslateTransition translation = new TranslateTransition(new Duration(1500), this);
        translation.setByX(0);
        translation.setByY(-this.distTranslation);
        translation.play();
        this.mole.opacityProperty().set(1);

        translation.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                canTouched = true;

                gameInstance.gameContext.getChildren().add(moleMoved);
                gameContext.getGazeDeviceManager().addEventFilter(moleMoved);

                mole.opacityProperty().set(0);

                out = true;

                timeMoleOut = new Timeline(); // New time this mole go out
                Random r = new Random();
                int time = r.nextInt(timeMoleStayOut) + 2000;

                timeMoleOut.getKeyFrames()
                        .add(new KeyFrame(new Duration(Configuration.getInstance().getSpeedEffects() * time),
                                new KeyValue(progressIndicator.progressProperty(), 1)));
                timeMoleOut.play();
                timeMoleOut.setOnFinished(new EventHandler<ActionEvent>() {

                    /* If the Mole is stay out without being touching */
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        if (!touched && out) {

                            goIn(); // Go back in the hole
                        }
                    }
                });

            }
        });
    }

    public void goIn() {
        canTouched = false;
        out = false;

        this.mole.opacityProperty().set(1);
        gameInstance.gameContext.getChildren().remove(moleMoved);
        gameContext.getGazeDeviceManager().removeEventFilter(this.moleMoved);

        double timeGoIn = 1500;
        if (touched) {
            timeGoIn = 500;
        }
        touched = false;
        TranslateTransition translation = new TranslateTransition(new Duration(timeGoIn), this);
        translation.setByX(0);
        translation.setByY(this.distTranslation);
        translation.play();
        translation.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                gameInstance.nbMolesOut--;
                mole.opacityProperty().set(0);
                canGoOut = true;
            }
        });
    }

}
