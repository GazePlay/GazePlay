package net.gazeplay.ui.scenes.ingame;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

public class VideoRecordingContext {

    private final EventHandler<Event> pointerEvent;
    private final Circle mousePointer;
    private final Circle gazePointer;
    private final Pane root;
    private final GameContext gameContext;

    public VideoRecordingContext(final Pane originalRoot, final GameContext gameCtxt) {

        root = originalRoot;
        gameContext = gameCtxt;

        mousePointer = new Circle(10, Color.RED);
        mousePointer.setMouseTransparent(true);
        mousePointer.setOpacity(0.5);
        root.getChildren().add(mousePointer);


        gazePointer = new Circle(10, Color.BLUE);
        gazePointer.setMouseTransparent(true);
        gazePointer.setOpacity(0.5);
        root.getChildren().add(gazePointer);
        gameContext.getGazeDeviceManager().addEventFilter(root);

        pointerEvent = e -> {
            if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
                mousePointer.setCenterX(((MouseEvent) e).getX());
                mousePointer.setCenterY(((MouseEvent) e).getY());
                mousePointer.toFront();
            } else if (e.getEventType() == GazeEvent.GAZE_MOVED) {
                gazePointer.setCenterX(((GazeEvent) e).getX());
                gazePointer.setCenterY(((GazeEvent) e).getY());
                gazePointer.toFront();
            }
        };
        root.addEventFilter(MouseEvent.ANY, pointerEvent);
        root.addEventFilter(GazeEvent.ANY, pointerEvent);
    }

    public void pointersClear() {
        root.removeEventFilter(MouseEvent.ANY, pointerEvent);
        root.removeEventFilter(GazeEvent.ANY, pointerEvent);
        root.getChildren().remove(gazePointer);
        root.getChildren().remove(mousePointer);
        gameContext.getGazeDeviceManager().removeEventFilter(root);
    }
}
