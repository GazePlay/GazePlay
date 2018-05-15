package net.gazeplay.games.order;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.Position;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;

/**
 *
 * @author vincent
 */
public class Target extends Parent {
    private final Stats stats;
    private final int num;
    private final EventHandler enterEvent;
    private final Order gameInstance;
    private final GameContext gameContext;
    private final Rectangle rectangle;
    private final RandomPositionGenerator randomPos;

    public Target(RandomPositionGenerator randomPositionGenerator, Stats stats, Order gameInstance,
            GameContext gameContext, int num) {
        this.stats = stats;
        this.num = num;
        this.gameInstance = gameInstance;
        this.gameContext = gameContext;
        this.randomPos = randomPositionGenerator;
        Position p = randomPos.newRandomPosition(100);
        this.rectangle = new Rectangle(p.getX(), p.getY(), 200, 200);
        this.rectangle
                .setFill(new ImagePattern(new Image("data/order/images/target-placeholder.png"), 0, 0, 1, 1, true));

        this.getChildren().add(rectangle);

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
