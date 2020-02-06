package net.gazeplay.games.spotthedifferences;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class Difference extends Circle {

    private static final long GAZE_TIME = 1000;

    @Getter
    @Setter
    private Difference pair;
    private long timer;

    public Difference(final IGameContext gameContext, final SpotTheDifferences mainGame, final double centerX, final double centerY,
                      final double radius) {
        super(centerX, centerY, radius, Color.TRANSPARENT);
        setStrokeWidth(5);
        setStroke(Color.RED);
        timer = 0;
        setOpacity(0);

        final EventHandler handler = (EventHandler<Event>) e -> {
            if (getOpacity() == 0) {
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    timer = System.currentTimeMillis();
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                    timer = 0;
                } else if (e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_MOVED) {
                    final long timeElapsed = System.currentTimeMillis() - timer;
                    if (timer > 0 && timeElapsed > GAZE_TIME) {
                        timer = 0;
                        this.setOpacity(1);
                        pair.setOpacity(1);
                        mainGame.differenceFound();
                    }
                }
            }
        };

        gameContext.getGazeDeviceManager().addEventFilter(this);
        this.addEventFilter(MouseEvent.ANY, handler);
        this.addEventFilter(GazeEvent.ANY, handler);

        gameContext.getChildren().add(this);
    }
}
