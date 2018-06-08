package net.gazeplay.games.labyrinth;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

/*
 * Mouse V4 
 * To move the mouse you must first select it,
 * Then the mouse will follow the player's gaze
 * If the player looks too far or out of the labyrinth, the mouse is deselected
 */
public class MouseV4 extends Mouse {

    private EventHandler<Event> eventMouse;
    private EventHandler<Event> eventBox;

    private Timeline timelineProgressBar;
    private ProgressIndicator indicator;

    protected boolean isSelectioned;

    public MouseV4(double positionX, double positionY, double width, double height, GameContext gameContext,
            Stats stats, Labyrinth gameInstance) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance);

        isSelectioned = false;
        eventMouse = buildEventMouse();
        eventBox = buildEventBox();
        indicator = createProgressIndicator(mouse.getX(), mouse.getY(), width, height);
        this.mouse.addEventHandler(GazeEvent.ANY, eventMouse);
        this.mouse.addEventHandler(MouseEvent.ANY, eventMouse);
        gameContext.getGazeDeviceManager().addEventFilter(this.mouse);
        mettreLesEventHandler();
        this.getChildren().add(indicator);

    }

    private void mettreLesEventHandler() {
        for (int i = 0; i < gameInstance.nbBoxesLine; i++) {
            for (int j = 0; j < gameInstance.nbBoxesColumns; j++) {
                gameInstance.walls[i][j].addEventHandler(MouseEvent.ANY, eventBox);
                gameInstance.walls[i][j].addEventHandler(GazeEvent.ANY, eventBox);
                gameContext.getGazeDeviceManager().addEventFilter(gameInstance.walls[i][j]);

            }
        }
    }

    private boolean conditionToMove(Event e) {
        if ((e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED)
                && isSelectioned) {
            GameBox gb = (GameBox) e.getSource();
            if (gb.isNextTo(indiceY, indiceX) && gameInstance.isFreeForMouse(gb.numRow, gb.numCol)) {
                return true;
            }
        }
        return false;
    }

    private boolean conditionToStop(Event e) {
        if ((e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED)
                && isSelectioned) {
            GameBox gb = (GameBox) e.getSource();
            if (gb.isNextTo(indiceY, indiceX)) { // fo comfort with eye tracker
                return false;
            }
            if (!(gb.isNextTo(indiceY, indiceX) && gameInstance.isFreeForMouse(gb.numRow, gb.numCol))) {
                return true;
            }
        }
        return false;
    }

    private boolean isCurrentBox(Event e) {
        GameBox gb = (GameBox) e.getSource();
        return (gb.numCol == indiceX && gb.numRow == indiceY);
    }

    public EventHandler<Event> buildEventBox() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (isCurrentBox(e)) {
                    return;
                }

                if (conditionToMove(e)) {
                    GameBox gb = (GameBox) e.getSource();
                    gb.indicator.setOpacity(0);
                    reOrientateMouse(indiceX, indiceY, gb.numCol, gb.numRow);
                    putInBold();
                    indiceX = gb.numCol;
                    indiceY = gb.numRow;
                    double coordX = gameInstance.positionX(indiceX);
                    double coordY = gameInstance.positionY(indiceY);
                    mouse.setX(coordX);
                    mouse.setY(coordY);
                    indicator.setTranslateX(coordX);
                    indicator.setTranslateY(coordY);
                    gameInstance.testIfCheese(indiceY, indiceX);

                } else if (conditionToStop(e)) {
                    putInLight();
                    isSelectioned = false;

                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                    Timeline timeline = new Timeline();
                    timeline.play();
                    if (timelineProgressBar != null)
                        timelineProgressBar.stop();
                }
            }
        };

    }

    public EventHandler<Event> buildEventMouse() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if ((e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED)
                        && !isSelectioned) {
                    indicator.setOpacity(1);
                    indicator.setProgress(0);

                    timelineProgressBar = new Timeline();
                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameInstance.fixationlength),
                            new KeyValue(indicator.progressProperty(), 1)));
                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            isSelectioned = true;
                            putInBold();
                            indicator.setOpacity(0);
                        }
                    });

                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                    Timeline timeline = new Timeline();
                    timeline.play();
                    if (timelineProgressBar != null)
                        timelineProgressBar.stop();

                    indicator.setOpacity(0);
                    indicator.setProgress(0);
                }
            }
        };

    }

}
