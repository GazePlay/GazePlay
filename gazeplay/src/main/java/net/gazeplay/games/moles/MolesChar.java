package net.gazeplay.games.moles;

import java.util.Random;

import javafx.animation.Animation;
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
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class MolesChar extends Parent {

    private Rectangle mole;

    private final GameContext gameContext;

    private final Moles gameInstance;

    private final double distTranslation;

    private boolean touched;

    private boolean canTouched;

    public boolean canGoOut;

    /**
     * true if the mole is out
     */
    public boolean out;

    private ProgressIndicator progressIndicator;

    private Timeline timeMinToWhackTheMole;

    private final double timeMinToWhackMole = 200; // TODO How much time ?

    private Timeline timeMoleOut;

    private final int timeMoleStayOut = 2500; // TODO : How much time ?

    private final double posX;
    private final double posY;

    final Stats stats;

    final EventHandler<Event> enterEvent;

    public MolesChar(double positionX, double positionY, double width, double height, double distTrans,
            GameContext gameContext, Stats stats, Moles gameInstance) {

        this.gameContext = gameContext;

        this.stats = stats;

        this.out = false;

        this.distTranslation = distTrans;

        this.posX = positionX;

        this.posY = positionY + this.distTranslation;

        touched = false;
        canTouched = false;
        canGoOut = true;

        this.gameInstance = gameInstance;

        this.progressIndicator = createProgressIndicator(width, height);
        // this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent();

        this.mole = new Rectangle(this.posX, this.posY, width, height);
        this.mole.setFill(new ImagePattern(new Image("data/wackmole/images/bibouleMole.png"), 5, 5, 1, 1, true));
        this.getChildren().add(mole); // The mole is visible

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);
    }

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    private EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (!canTouched || !out || touched) // If the mole is not out or already touched: nothing append
                    return;

                /* If the mole is out and touched */
                if (canTouched && (e.getEventType() == MouseEvent.MOUSE_ENTERED
                        || e.getEventType() == GazeEvent.GAZE_ENTERED)) {

                    progressIndicator.setOpacity(0);
                    progressIndicator.setProgress(0);

                    timeMinToWhackTheMole = new Timeline();

                    timeMinToWhackTheMole.getKeyFrames().add(new KeyFrame(new Duration(timeMinToWhackMole),
                            new KeyValue(progressIndicator.progressProperty(), 1)));

                    timeMinToWhackTheMole.play();
                    timeMinToWhackTheMole.setOnFinished(new EventHandler<ActionEvent>() {
                        /* If the user watch the mole enough time (timeMinToWhackMole) */
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            if (!touched && out) {
                                canTouched = false;
                                gameInstance.OneMoleWacked();
                                touched = true;
                                goIn();
                            }

                        }
                    });

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

        translation.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                canTouched = true;
                out = true;

                timeMoleOut = new Timeline(); // New time this mole go out

                Random r = new Random();

                int time = r.nextInt(timeMoleStayOut) + 2000;

                timeMoleOut.getKeyFrames()
                        .add(new KeyFrame(new Duration(time), new KeyValue(progressIndicator.progressProperty(), 1)));

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

    private void goIn() {
        canTouched = false;
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
                out = false;
                canGoOut = true;
            }
        });

    }

}
