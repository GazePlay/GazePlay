package net.gazeplay.games.labyrinth;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Objects;

/*
 * Abstract class for all Mouse version containing arrows
 */
public abstract class MouseArrows extends Mouse {

    ProgressIndicator indicatorUp;
    ProgressIndicator indicatorDown;
    ProgressIndicator indicatorRight;
    ProgressIndicator indicatorLeft;

    Rectangle buttonUp;
    Rectangle buttonDown;
    Rectangle buttonRight;
    Rectangle buttonLeft;

    private Timeline timelineProgressBar;

    double buttonDimHeight;
    double buttonDimWidth;

    MouseArrows(double positionX, double positionY, double width, double height, IGameContext gameContext,
                Stats stats, Labyrinth gameInstance) {

        super(positionX, positionY, width, height, gameContext, stats, gameInstance);

        EventHandler<Event> buttonUpEvent = buildButtonUp();
        EventHandler<Event> buttonDownEvent = buildButtonDownEvent();
        EventHandler<Event> buttonRightEvent = buildButtonRightEvent();
        EventHandler<Event> buttonLeftEvent = buildButtonLeftEvent();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        buttonDimHeight = dimension2D.getHeight() / gameInstance.nbBoxesLine;
        buttonDimWidth = dimension2D.getWidth() / gameInstance.nbBoxesColumns;

        placementFleche();

        if (Objects.equals(gameContext.getConfiguration().getEyeTracker(), "tobii")){
            this.buttonUp.addEventHandler(GazeEvent.ANY, buttonUpEvent);
            this.buttonDown.addEventHandler(GazeEvent.ANY, buttonDownEvent);
            this.buttonLeft.addEventHandler(GazeEvent.ANY, buttonLeftEvent);
            this.buttonRight.addEventHandler(GazeEvent.ANY, buttonRightEvent);
        }else {
            this.buttonUp.addEventHandler(MouseEvent.ANY, buttonUpEvent);
            this.buttonDown.addEventHandler(MouseEvent.ANY, buttonDownEvent);
            this.buttonLeft.addEventHandler(MouseEvent.ANY, buttonLeftEvent);
            this.buttonRight.addEventHandler(MouseEvent.ANY, buttonRightEvent);
        }

        gameContext.getGazeDeviceManager().addEventFilter(this.buttonUp);
        gameContext.getGazeDeviceManager().addEventFilter(this.buttonDown);
        gameContext.getGazeDeviceManager().addEventFilter(this.buttonLeft);
        gameContext.getGazeDeviceManager().addEventFilter(this.buttonRight);

        this.getChildren().addAll(buttonUp, buttonDown, buttonLeft, buttonRight);
        this.getChildren().addAll(indicatorUp, indicatorDown, indicatorLeft, indicatorRight);

        updateArrowsColor();

    }

    protected abstract void placementFleche();

    protected abstract void recomputeArrowsPositions();

    protected void updateArrowsColor() {
        // If we can go up
        if (indiceY - 1 >= 0 && gameInstance.isFreeForMouse(indiceY - 1, indiceX)) {
            putInBold("up", this.buttonUp);
        } else {
            putInLight("up", this.buttonUp);
        }
        // If we can go down
        if (indiceY + 1 < gameInstance.nbBoxesLine && gameInstance.isFreeForMouse(indiceY + 1, indiceX)) {
            putInBold("down", this.buttonDown);
        } else {
            putInLight("down", this.buttonDown);
        }
        // If we can go right
        if (indiceX + 1 < gameInstance.nbBoxesColumns && gameInstance.isFreeForMouse(indiceY, indiceX + 1)) {
            putInBold("right", this.buttonRight);
        } else {
            putInLight("right", this.buttonRight);
        }
        // If we can go left
        if (indiceX - 1 >= 0 && gameInstance.isFreeForMouse(indiceY, indiceX - 1)) {
            putInBold("left", this.buttonLeft);
        } else {
            putInLight("left", this.buttonLeft);
        }
    }

    private Boolean isActivated(Event e) {
        return (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED);
    }

    /* s = "up" / "down" / "right" / Left */
    private void putInBold(String s, Rectangle b) {
        b.setFill(new ImagePattern(new Image("data/labyrinth/images/" + s + "Arrow.png"), 5, 5, 1, 1, true));
        b.setOpacity(1);
    }

    private void putInLight(String s, Rectangle b) {
        b.setFill(new ImagePattern(new Image("data/labyrinth/images/" + s + "ArrowLight.png"), 5, 5, 1, 1, true));
        b.setOpacity(0.5);
    }

    private EventHandler<Event> buildButtonUp() {
        return e -> {

            if (indiceY - 1 >= 0 && gameInstance.isFreeForMouse(indiceY - 1, indiceX) && isActivated(e)) {

                gameInstance.eventButtonUp.add("Entered");
                gameInstance.fixationLengthButtonUp.add("");
                gameInstance.eventButtonDown.add("");
                gameInstance.fixationLengthButtonDown.add("");
                gameInstance.eventButtonRight.add("");
                gameInstance.fixationLengthButtonRight.add("");
                gameInstance.eventButtonLeft.add("");
                gameInstance.fixationLengthButtonLeft.add("");
                gameInstance.updateStats();

                indicatorUp.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                indicatorUp.setOpacity(1);
                indicatorUp.setProgress(0);
                timelineProgressBar = new Timeline();
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(this.gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(indicatorUp.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {

                    gameInstance.eventButtonUp.add("Validate");
                    gameInstance.fixationLengthButtonUp.add(String.valueOf(gameContext.getConfiguration().getFixationLength()));
                    gameInstance.eventButtonDown.add("");
                    gameInstance.fixationLengthButtonDown.add("");
                    gameInstance.eventButtonRight.add("");
                    gameInstance.fixationLengthButtonRight.add("");
                    gameInstance.eventButtonLeft.add("");
                    gameInstance.fixationLengthButtonLeft.add("");
                    gameInstance.updateStats();

                    indicatorUp.setOpacity(0);
                    reOrientateMouse(indiceX, indiceY, indiceX, indiceY - 1);
                    indiceY = indiceY - 1;
                    mouse.setX(gameInstance.positionX(indiceX));
                    mouse.setY(gameInstance.positionY(indiceY));
                    recomputeArrowsPositions();
                    updateArrowsColor();
                    gameInstance.testIfCheese(indiceY, indiceX);
                });
                timelineProgressBar.play();

            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                double fixation = gameContext.getConfiguration().getFixationLength() * indicatorUp.getProgress();
                if (fixation > 0.0 && fixation < gameContext.getConfiguration().getFixationLength()){
                    gameInstance.eventButtonUp.add("Exited");
                    gameInstance.fixationLengthButtonUp.add(String.valueOf(fixation));
                    gameInstance.eventButtonDown.add("");
                    gameInstance.fixationLengthButtonDown.add("");
                    gameInstance.eventButtonRight.add("");
                    gameInstance.fixationLengthButtonRight.add("");
                    gameInstance.eventButtonLeft.add("");
                    gameInstance.fixationLengthButtonLeft.add("");
                    gameInstance.updateStats();
                }

                Timeline timeline = new Timeline();
                timeline.play();
                if (timelineProgressBar != null)
                    timelineProgressBar.stop();

                indicatorUp.setOpacity(0);
                indicatorUp.setProgress(0);
            }
        };
    }

    private EventHandler<Event> buildButtonDownEvent() {
        return e -> {

            if (indiceY + 1 < gameInstance.nbBoxesLine && gameInstance.isFreeForMouse(indiceY + 1, indiceX)
                && isActivated(e)) {

                gameInstance.eventButtonUp.add("");
                gameInstance.fixationLengthButtonUp.add("");
                gameInstance.eventButtonDown.add("Entered");
                gameInstance.fixationLengthButtonDown.add("");
                gameInstance.eventButtonRight.add("");
                gameInstance.fixationLengthButtonRight.add("");
                gameInstance.eventButtonLeft.add("");
                gameInstance.fixationLengthButtonLeft.add("");
                gameInstance.updateStats();

                indicatorDown.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                indicatorDown.setOpacity(1);
                indicatorDown.setProgress(0);
                timelineProgressBar = new Timeline();
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(this.gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(indicatorDown.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {

                    gameInstance.eventButtonUp.add("");
                    gameInstance.fixationLengthButtonUp.add("");
                    gameInstance.eventButtonDown.add("Validate");
                    gameInstance.fixationLengthButtonDown.add(String.valueOf(gameContext.getConfiguration().getFixationLength()));
                    gameInstance.eventButtonRight.add("");
                    gameInstance.fixationLengthButtonRight.add("");
                    gameInstance.eventButtonLeft.add("");
                    gameInstance.fixationLengthButtonLeft.add("");
                    gameInstance.updateStats();

                    indicatorDown.setOpacity(0);
                    reOrientateMouse(indiceX, indiceY, indiceX, indiceY + 1);
                    indiceY = indiceY + 1;
                    mouse.setX(gameInstance.positionX(indiceX));
                    mouse.setY(gameInstance.positionY(indiceY));
                    recomputeArrowsPositions();
                    updateArrowsColor();

                    gameInstance.testIfCheese(indiceY, indiceX);
                });
                timelineProgressBar.play();

            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                double fixation = gameContext.getConfiguration().getFixationLength() * indicatorDown.getProgress();
                if (fixation > 0.0 && fixation < gameContext.getConfiguration().getFixationLength()){
                    gameInstance.eventButtonUp.add("");
                    gameInstance.fixationLengthButtonUp.add("");
                    gameInstance.eventButtonDown.add("Exited");
                    gameInstance.fixationLengthButtonDown.add(String.valueOf(fixation));
                    gameInstance.eventButtonRight.add("");
                    gameInstance.fixationLengthButtonRight.add("");
                    gameInstance.eventButtonLeft.add("");
                    gameInstance.fixationLengthButtonLeft.add("");
                    gameInstance.updateStats();
                }

                Timeline timeline = new Timeline();
                timeline.play();
                if (timelineProgressBar != null)
                    timelineProgressBar.stop();

                indicatorDown.setOpacity(0);
                indicatorDown.setProgress(0);
            }
        };
    }

    private EventHandler<Event> buildButtonRightEvent() {
        return e -> {
            if (indiceX + 1 < gameInstance.nbBoxesColumns && gameInstance.isFreeForMouse(indiceY, indiceX + 1)
                && isActivated(e)) {

                gameInstance.eventButtonUp.add("");
                gameInstance.fixationLengthButtonUp.add("");
                gameInstance.eventButtonDown.add("");
                gameInstance.fixationLengthButtonDown.add("");
                gameInstance.eventButtonRight.add("Entered");
                gameInstance.fixationLengthButtonRight.add("");
                gameInstance.eventButtonLeft.add("");
                gameInstance.fixationLengthButtonLeft.add("");
                gameInstance.updateStats();

                indicatorRight.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                indicatorRight.setOpacity(1);
                indicatorRight.setProgress(0);
                timelineProgressBar = new Timeline();
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(this.gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(indicatorRight.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {

                    gameInstance.eventButtonUp.add("");
                    gameInstance.fixationLengthButtonUp.add("");
                    gameInstance.eventButtonDown.add("");
                    gameInstance.fixationLengthButtonDown.add("");
                    gameInstance.eventButtonRight.add("Validate");
                    gameInstance.fixationLengthButtonRight.add(String.valueOf(gameContext.getConfiguration().getFixationLength()));
                    gameInstance.eventButtonLeft.add("");
                    gameInstance.fixationLengthButtonLeft.add("");
                    gameInstance.updateStats();

                    indicatorRight.setOpacity(0);
                    reOrientateMouse(indiceX, indiceY, indiceX + 1, indiceY);
                    indiceX = indiceX + 1;
                    mouse.setX(gameInstance.positionX(indiceX));
                    mouse.setY(gameInstance.positionY(indiceY));
                    recomputeArrowsPositions();
                    updateArrowsColor();
                    gameInstance.testIfCheese(indiceY, indiceX);
                });
                timelineProgressBar.play();

            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                double fixation = gameContext.getConfiguration().getFixationLength() * indicatorRight.getProgress();
                if (fixation > 0.0 && fixation < gameContext.getConfiguration().getFixationLength()){
                    gameInstance.eventButtonUp.add("");
                    gameInstance.fixationLengthButtonUp.add("");
                    gameInstance.eventButtonDown.add("");
                    gameInstance.fixationLengthButtonDown.add("");
                    gameInstance.eventButtonRight.add("Exited");
                    gameInstance.fixationLengthButtonRight.add(String.valueOf(fixation));
                    gameInstance.eventButtonLeft.add("");
                    gameInstance.fixationLengthButtonLeft.add("");
                    gameInstance.updateStats();
                }

                Timeline timeline = new Timeline();
                timeline.play();
                if (timelineProgressBar != null)
                    timelineProgressBar.stop();

                indicatorRight.setOpacity(0);
                indicatorRight.setProgress(0);
            }

        };
    }

    private EventHandler<Event> buildButtonLeftEvent() {
        return e -> {

            if (indiceX - 1 >= 0 && gameInstance.isFreeForMouse(indiceY, indiceX - 1) && isActivated(e)) {

                gameInstance.eventButtonUp.add("");
                gameInstance.fixationLengthButtonUp.add("");
                gameInstance.eventButtonDown.add("");
                gameInstance.fixationLengthButtonDown.add("");
                gameInstance.eventButtonRight.add("");
                gameInstance.fixationLengthButtonRight.add("");
                gameInstance.eventButtonLeft.add("Entered");
                gameInstance.fixationLengthButtonLeft.add("");
                gameInstance.updateStats();

                indicatorLeft.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                indicatorLeft.setOpacity(1);
                indicatorLeft.setProgress(0);
                timelineProgressBar = new Timeline();
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(this.gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(indicatorLeft.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {

                    gameInstance.eventButtonUp.add("");
                    gameInstance.fixationLengthButtonUp.add("");
                    gameInstance.eventButtonDown.add("");
                    gameInstance.fixationLengthButtonDown.add("");
                    gameInstance.eventButtonRight.add("");
                    gameInstance.fixationLengthButtonRight.add("");
                    gameInstance.eventButtonLeft.add("Validate");
                    gameInstance.fixationLengthButtonLeft.add(String.valueOf(gameContext.getConfiguration().getFixationLength()));
                    gameInstance.updateStats();

                    indicatorLeft.setOpacity(0);
                    reOrientateMouse(indiceX, indiceY, indiceX - 1, indiceY);
                    indiceX = indiceX - 1;
                    mouse.setX(gameInstance.positionX(indiceX));
                    mouse.setY(gameInstance.positionY(indiceY));
                    recomputeArrowsPositions();
                    updateArrowsColor();
                    gameInstance.testIfCheese(indiceY, indiceX);
                });
                timelineProgressBar.play();

            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                double fixation = gameContext.getConfiguration().getFixationLength() * indicatorLeft.getProgress();
                if (fixation > 0.0 && fixation < gameContext.getConfiguration().getFixationLength()){
                    gameInstance.eventButtonUp.add("");
                    gameInstance.fixationLengthButtonUp.add("");
                    gameInstance.eventButtonDown.add("");
                    gameInstance.fixationLengthButtonDown.add("");
                    gameInstance.eventButtonRight.add("");
                    gameInstance.fixationLengthButtonRight.add("");
                    gameInstance.eventButtonLeft.add("Exited");
                    gameInstance.fixationLengthButtonLeft.add(String.valueOf(fixation));
                    gameInstance.updateStats();
                }

                Timeline timeline = new Timeline();
                timeline.play();
                if (timelineProgressBar != null)
                    timelineProgressBar.stop();

                indicatorLeft.setOpacity(0);
                indicatorLeft.setProgress(0);
            }

        };
    }

    protected void creationButton(double x, double y, double width, double height, String s,
                                  EventHandler<Event> e) {
        Rectangle b = new Rectangle(x, y, width, height);
        b.setFill(new ImagePattern(new Image("data/labyrinth/images/" + s + "Arrow.png"), 5, 5, 1, 1, true));
        b.addEventHandler(MouseEvent.ANY, e);
        b.addEventHandler(GazeEvent.ANY, e);
    }

}
