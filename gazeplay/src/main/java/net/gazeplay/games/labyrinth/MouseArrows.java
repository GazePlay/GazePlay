package net.gazeplay.games.labyrinth;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.labyrinth.Labyrinth;

public abstract class MouseArrows extends Mouse {

    protected ProgressIndicator indicatorUp;
    protected ProgressIndicator indicatorDown;
    protected ProgressIndicator indicatorRight;
    protected ProgressIndicator indicatorLeft;

    protected Rectangle buttonUp;
    protected Rectangle buttonDown;
    protected Rectangle buttonRight;
    protected Rectangle buttonLeft;

    protected final EventHandler<Event> buttonUpEvent;
    protected final EventHandler<Event> buttonDownEvent;
    protected final EventHandler<Event> buttonRightEvent;
    protected final EventHandler<Event> buttonLeftEvent;

    private Timeline timelineProgressBar;

    protected double buttonDimHeight;
    protected double buttonDimWidth;

    public MouseArrows(double positionX, double positionY, double width, double height, GameContext gameContext,
            Stats stats, Labyrinth gameInstance) {

        super(positionX, positionY, width, height, gameContext, stats, gameInstance);

        this.buttonUpEvent = buildButtonUp();
        this.buttonDownEvent = buildButtonDownEvent();
        this.buttonRightEvent = buildButtonRightEvent();
        this.buttonLeftEvent = buildButtonLeftEvent();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        buttonDimHeight = dimension2D.getHeight() / 12;
        buttonDimWidth = dimension2D.getWidth() / 15;

        placementFleche();

    }

    protected abstract void placementFleche();

    protected abstract void recomputeArrowsPositions();

    private Boolean isActivated(Event e) {
        return (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED);
    }

    public EventHandler<Event> buildButtonUp() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (indiceY - 1 >= 0 && gameInstance.isFreeForMouse(indiceY - 1, indiceX) && isActivated(e)) {

                    indicatorUp.setOpacity(1);
                    indicatorUp.setProgress(0);
                    timelineProgressBar = new Timeline();
                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameInstance.fixationlength),
                            new KeyValue(indicatorUp.progressProperty(), 1)));
                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            indicatorUp.setOpacity(0);
                            reOrientateMouse(indiceX, indiceY, indiceX, indiceY - 1);
                            indiceY = indiceY - 1;
                            mouse.setX(gameInstance.positionX(indiceX));
                            mouse.setY(gameInstance.positionY(indiceY));
                            recomputeArrowsPositions();
                            gameInstance.testIfCheese(indiceY, indiceX);
                        }
                    });

                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    Timeline timeline = new Timeline();
                    timeline.play();
                    if (timelineProgressBar != null)
                        timelineProgressBar.stop();

                    indicatorUp.setOpacity(0);
                    indicatorUp.setProgress(0);
                }
            }
        };
    }

    public EventHandler<Event> buildButtonDownEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (indiceY + 1 < gameInstance.nbCasesLignes && gameInstance.isFreeForMouse(indiceY + 1, indiceX)
                        && isActivated(e)) {

                    indicatorDown.setOpacity(1);
                    indicatorDown.setProgress(0);
                    timelineProgressBar = new Timeline();
                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameInstance.fixationlength),
                            new KeyValue(indicatorDown.progressProperty(), 1)));
                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            indicatorDown.setOpacity(0);
                            reOrientateMouse(indiceX, indiceY, indiceX, indiceY + 1);
                            indiceY = indiceY + 1;
                            mouse.setX(gameInstance.positionX(indiceX));
                            mouse.setY(gameInstance.positionY(indiceY));
                            recomputeArrowsPositions();

                            gameInstance.testIfCheese(indiceY, indiceX);
                        }
                    });

                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    Timeline timeline = new Timeline();
                    timeline.play();
                    if (timelineProgressBar != null)
                        timelineProgressBar.stop();

                    indicatorDown.setOpacity(0);
                    indicatorDown.setProgress(0);
                }
            }
        };
    }

    public EventHandler<Event> buildButtonRightEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (indiceX + 1 < gameInstance.nbCasesColonne && gameInstance.isFreeForMouse(indiceY, indiceX + 1)
                        && isActivated(e)) {

                    indicatorRight.setOpacity(1);
                    indicatorRight.setProgress(0);
                    timelineProgressBar = new Timeline();
                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameInstance.fixationlength),
                            new KeyValue(indicatorRight.progressProperty(), 1)));
                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            indicatorRight.setOpacity(0);
                            reOrientateMouse(indiceX, indiceY, indiceX + 1, indiceY);
                            indiceX = indiceX + 1;
                            mouse.setX(gameInstance.positionX(indiceX));
                            mouse.setY(gameInstance.positionY(indiceY));
                            recomputeArrowsPositions();
                            gameInstance.testIfCheese(indiceY, indiceX);
                        }
                    });

                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    Timeline timeline = new Timeline();
                    timeline.play();
                    if (timelineProgressBar != null)
                        timelineProgressBar.stop();

                    indicatorRight.setOpacity(0);
                    indicatorRight.setProgress(0);
                }

            }
        };
    }

    public EventHandler<Event> buildButtonLeftEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (indiceX - 1 >= 0 && gameInstance.isFreeForMouse(indiceY, indiceX - 1) && isActivated(e)) {

                    indicatorLeft.setOpacity(1);
                    indicatorLeft.setProgress(0);
                    timelineProgressBar = new Timeline();
                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameInstance.fixationlength),
                            new KeyValue(indicatorLeft.progressProperty(), 1)));
                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            indicatorLeft.setOpacity(0);
                            reOrientateMouse(indiceX, indiceY, indiceX - 1, indiceY);
                            indiceX = indiceX - 1;
                            mouse.setX(gameInstance.positionX(indiceX));
                            mouse.setY(gameInstance.positionY(indiceY));
                            recomputeArrowsPositions();
                            gameInstance.testIfCheese(indiceY, indiceX);
                        }
                    });

                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    Timeline timeline = new Timeline();
                    timeline.play();
                    if (timelineProgressBar != null)
                        timelineProgressBar.stop();

                    indicatorLeft.setOpacity(0);
                    indicatorLeft.setProgress(0);
                }

            }
        };
    }

    protected ProgressIndicator createProgressIndicator(double x, double y, double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(x + width * 0.05);
        indicator.setTranslateY(y + height * 0.2);
        indicator.setMouseTransparent(true);
        indicator.setMinWidth(width);
        indicator.setMinHeight(height);
        indicator.setOpacity(0);
        return indicator;
    }

}