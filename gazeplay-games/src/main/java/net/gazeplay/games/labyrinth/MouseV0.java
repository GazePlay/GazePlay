package net.gazeplay.games.labyrinth;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

/*
 * MouseV0 :
 * To move the mouse :
 * you have to look at a box that is adjacent to it.
 * This version does not work with the eye tracker :
 * It is replaced by the version MouseTransparentArrows
 * which produces the same effect on the user side
 */

public class MouseV0 extends Mouse {

    private final EventHandler<Event> event;
    private Timeline timelineProgressBar;

    public MouseV0(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext,
                   final Stats stats, final Labyrinth gameInstance) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance);

        event = buildEvent();
        mettreAJourLesEventHandler();

    }

    private void mettreAJourLesEventHandler() {

        for (int i = 0; i < gameInstance.nbBoxesLine; i++) {
            for (int j = 0; j < gameInstance.nbBoxesColumns; j++) {
                // We remove the old EventHandler
                if (gameInstance.getBoxAt(i, j).isNextToTheMouse()) {
                    gameInstance.getBoxAt(i, j).removeEventHandler(MouseEvent.ANY, event);
                    gameInstance.getBoxAt(i, j).removeEventHandler(GazeEvent.ANY, event);
                    gameContext.getGazeDeviceManager().removeEventFilter(gameInstance.getBoxAt(i, j));
                    gameInstance.getBoxAt(i, j).setNextToTheMouse(false);
                }
                // We add the new eventHandler
                if (gameInstance.getBoxAt(i, j).isNextTo(indiceY, indiceX) && gameInstance.isFreeForMouse(i, j)) {
                    gameInstance.getBoxAt(i, j).setNextToTheMouse(true);
                    gameInstance.getBoxAt(i, j).addEventHandler(MouseEvent.ANY, event);
                    gameInstance.getBoxAt(i, j).addEventHandler(GazeEvent.ANY, event);
                    gameContext.getGazeDeviceManager().addEventFilter(gameInstance.getBoxAt(i, j));
                }
            }
        }
    }

    public EventHandler<Event> buildEvent() {
        return e -> {

            if ((e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED)) {
                final GameBox gb = (GameBox) e.getSource();

                gb.getIndicator().setOpacity(1);
                gb.getIndicator().setProgress(0);

                timelineProgressBar = new Timeline();
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameInstance.fixationlength),
                    new KeyValue(gb.getIndicator().progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {
                    gb.getIndicator().setOpacity(0);
                    reOrientateMouse(indiceX, indiceY, gb.numCol, gb.numRow);
                    indiceX = gb.numCol;
                    indiceY = gb.numRow;
                    mouse.setX(gameInstance.positionX(indiceX));
                    mouse.setY(gameInstance.positionY(indiceY));
                    gameInstance.testIfCheese(indiceY, indiceX);
                    mettreAJourLesEventHandler();
                });
                timelineProgressBar.play();

            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                final GameBox gb = (GameBox) e.getSource();

                final Timeline timeline = new Timeline();
                timeline.play();
                if (timelineProgressBar != null) {
                    timelineProgressBar.stop();
                }

                gb.getIndicator().setOpacity(0);
                gb.getIndicator().setProgress(0);
            }
        };

    }

}
