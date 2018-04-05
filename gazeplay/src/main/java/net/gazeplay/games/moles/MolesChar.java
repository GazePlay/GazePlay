package net.gazeplay.games.moles;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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

    /**
     * true if the mole is out
     */
    public boolean out;

    private ProgressIndicator progressIndicator;

    private Timeline timeMinToWhackTheMole;

    private final double timeMinToWhackMole;

    private Timeline timeMoleOut;

    private final double timeMoleStayOut;

    final Stats stats;

    final EventHandler<Event> enterEvent;

    public MolesChar(double positionX, double positionY, double width, double height, GameContext gameContext,
            Stats stats, Moles gameInstance) {

        this.gameContext = gameContext;

        this.stats = stats;

        this.out = false;

        this.timeMoleStayOut = 90; // TODO : How much time ?

        this.gameInstance = gameInstance;

        this.timeMinToWhackMole = 2; // TODO How much time ?

        // this.progressIndicator = createProgressIndicator(width, height);
        // this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent();

        this.mole = new Rectangle(positionX, positionY, width, height);
        this.mole.setFill(new ImagePattern(new Image("data/wackmole/images/bibouleMole.png"), 5, 5, 1, 1, true));
        this.getChildren().add(mole);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);
    }

    /*
     * private ProgressIndicator createProgressIndicator(double width, double height) { ProgressIndicator indicator =
     * new ProgressIndicator(0); indicator.setTranslateX(mole.getX() + width * 0.05);
     * indicator.setTranslateY(mole.getY() + height * 0.2); indicator.setMinWidth(width * 0.9);
     * indicator.setMinHeight(width * 0.9); indicator.setOpacity(0); return indicator; }
     */

    private EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (!out) // If the mole is not out : nothing append
                    return;

                /* If the mole is out and be touched */
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

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
                            gameInstance.nbMolesWacked++;
                            goIn();

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

        this.timeMoleOut = new Timeline(); // New time this mole go out

        this.getChildren().add(mole);

        timeMoleOut.getKeyFrames().add(
                new KeyFrame(new Duration(timeMoleStayOut), new KeyValue(progressIndicator.progressProperty(), 1)));

        timeMoleOut.play();
        timeMoleOut.setOnFinished(new EventHandler<ActionEvent>() {
            /* If the Mole is stay out without being touching */
            @Override
            public void handle(ActionEvent actionEvent) {
                goIn(); // Go back in the hole
            }
        });
        // gameContext.getGazeDeviceManager().addEventFilter(mole);
    }

    private void goIn() {
        this.out = false;
        this.gameInstance.chooseMoleToOut();
        this.getChildren().remove(this);
    }

}
