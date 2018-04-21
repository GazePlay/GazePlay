package net.gazeplay.games.order;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.games.ImageLibrary;
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
            ImageLibrary imageLibrary, Order gameInstance, GameContext gameContext, int num) {
        super(initialRadius, randomPositionGenerator, imageLibrary);
        this.stats = stats;
        this.num = num;
        this.gameInstance = gameInstance;
        this.gameContext = gameContext;

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    enter();
                }
            }
        };
    }

    private void enter() {
        this.gameInstance.enter(this.num, this);
    }

    public void addEvent() {
        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        gameContext.getGazeDeviceManager().addEventFilter(this);
    }
}
