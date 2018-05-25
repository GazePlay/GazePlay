package net.gazeplay.games.labyrinth;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.labyrinth.Labyrinth;

public class MouseV0 extends Mouse {

    private EventHandler<Event> event;
    private Timeline timelineProgressBar;

    public MouseV0(double positionX, double positionY, double width, double height, GameContext gameContext,
            Stats stats, Labyrinth gameInstance) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance);

        event = buildEvent();
        mettreAJourLesEventHandler();

    }

    private void mettreAJourLesEventHandler() {

        for (int i = 0; i < gameInstance.nbCasesLignes; i++) {
            for (int j = 0; j < gameInstance.nbCasesColonne; j++) {
                // We remove the old EventHandler
                if (gameInstance.walls[i][j].wasNextToTheMouse) {
                    gameInstance.walls[i][j].removeEventHandler(MouseEvent.ANY, event);
                    gameInstance.walls[i][j].removeEventHandler(GazeEvent.ANY, event);
                    gameInstance.walls[i][j].wasNextToTheMouse = false;
                }
                // We add the new eventHandler
                if (gameInstance.walls[i][j].isNextTo(indiceY, indiceX) && gameInstance.isFreeForMouse(i, j)) {
                    gameInstance.walls[i][j].wasNextToTheMouse = true;
                    gameInstance.walls[i][j].addEventHandler(MouseEvent.ANY, event);
                    gameInstance.walls[i][j].addEventHandler(GazeEvent.ANY, event);
                }
            }
        }
    }

    public EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if ((e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED)) {
                    GameBox gb = (GameBox) e.getSource();

                    gb.indicator.setOpacity(1);
                    gb.indicator.setProgress(0);

                    timelineProgressBar = new Timeline();
                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameInstance.fixationlength),
                            new KeyValue(gb.indicator.progressProperty(), 1)));
                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            gb.indicator.setOpacity(0);
                            reOrientateMouse(indiceX, indiceY, gb.numCol, gb.numRow);
                            indiceX = gb.numCol;
                            indiceY = gb.numRow;
                            mouse.setX(gameInstance.positionX(indiceX));
                            mouse.setY(gameInstance.positionY(indiceY));
                            gameInstance.testIfCheese(indiceY, indiceX);
                            mettreAJourLesEventHandler();
                        }

                    });

                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    GameBox gb = (GameBox) e.getSource();

                    Timeline timeline = new Timeline();
                    timeline.play();
                    if (timelineProgressBar != null)
                        timelineProgressBar.stop();

                    gb.indicator.setOpacity(0);
                    gb.indicator.setProgress(0);
                }
            }
        };

    }

}
