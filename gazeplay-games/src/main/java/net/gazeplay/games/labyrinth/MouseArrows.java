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

        this.buttonUp.addEventHandler(MouseEvent.ANY, buttonUpEvent);
        this.buttonUp.addEventHandler(GazeEvent.ANY, buttonUpEvent);
        gameContext.getGazeDeviceManager().addEventFilter(this.buttonUp);

        this.buttonDown.addEventHandler(MouseEvent.ANY, buttonDownEvent);
        this.buttonDown.addEventHandler(GazeEvent.ANY, buttonDownEvent);
        gameContext.getGazeDeviceManager().addEventFilter(this.buttonDown);

        this.buttonLeft.addEventHandler(MouseEvent.ANY, buttonLeftEvent);
        this.buttonLeft.addEventHandler(GazeEvent.ANY, buttonLeftEvent);
        gameContext.getGazeDeviceManager().addEventFilter(this.buttonLeft);

        this.buttonRight.addEventHandler(MouseEvent.ANY, buttonRightEvent);
        this.buttonRight.addEventHandler(GazeEvent.ANY, buttonRightEvent);
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

                indicatorUp.setOpacity(1);
                indicatorUp.setProgress(0);
                timelineProgressBar = new Timeline();
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameInstance.fixationlength),
                    new KeyValue(indicatorUp.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {
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

                indicatorDown.setOpacity(1);
                indicatorDown.setProgress(0);
                timelineProgressBar = new Timeline();
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameInstance.fixationlength),
                    new KeyValue(indicatorDown.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {
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

                indicatorRight.setOpacity(1);
                indicatorRight.setProgress(0);
                timelineProgressBar = new Timeline();
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameInstance.fixationlength),
                    new KeyValue(indicatorRight.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {
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

                indicatorLeft.setOpacity(1);
                indicatorLeft.setProgress(0);
                timelineProgressBar = new Timeline();
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameInstance.fixationlength),
                    new KeyValue(indicatorLeft.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {
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
