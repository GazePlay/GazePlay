package net.gazeplay.games.order;

import java.util.List;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.stats.Stats;

/**
 *
 * @author vincent
 */
public class Target extends Portrait {
    private final Stats stats;
    private final int num;
    private final EventHandler enterEvent;
    private final Order gameInstance;
    private final GameContext gameContext;

    public Target(int initialRadius, RandomPositionGenerator randomPositionGenerator, Stats stats,
            List<Image> availableImages, Order gameInstance, GameContext gameContext, int num) {
        super(initialRadius, randomPositionGenerator, availableImages);
        this.stats = stats;
        this.num = num;
        this.gameInstance = gameInstance;
        this.gameContext = gameContext;

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    double x = ((MouseEvent) e).getX();
                    double y = ((MouseEvent) e).getY();
                    enter((int) x, (int) y);
                } else if (e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    double x = ((GazeEvent) e).getX();
                    double y = ((GazeEvent) e).getY();
                    enter((int) x, (int) y);
                }
            }
        };
    }

    private void enter(int x, int y) {
        this.gameInstance.enter(this.num, this, x, y);
    }

    public void addEvent() {
        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        gameContext.getGazeDeviceManager().addEventFilter(this);
    }
}
