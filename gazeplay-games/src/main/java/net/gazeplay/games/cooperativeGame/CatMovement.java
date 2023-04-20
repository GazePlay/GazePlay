package net.gazeplay.games.cooperativeGame;

import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import javafx.scene.input.KeyEvent;

public class CatMovement extends Cat{

    private final EventHandler<Event> event;
    private Timeline timelineProgressBar;

    public CatMovement(double positionX, double positionY, double width, double height, IGameContext gameContext, Stats stats, CooperativeGame gameInstance) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance);

        event = buildEvent();
    }

    public EventHandler<Event> buildEvent() {

        return e -> {
            if (e.getEventType() == KeyEvent.KEY_PRESSED ){

                KeyEvent key = (KeyEvent) e;
                System.out.println("input pressed: " + key.getCode());

                if (key.getCode() == KeyCode.UP || key.getCode() == KeyCode.DOWN || key.getCode() == KeyCode.LEFT || key.getCode() == KeyCode.RIGHT) {

                    switch(key.getCode()){
                        case UP:
                            System.out.println("up");
                            positionY += 10;
                            break;
                        case DOWN:
                            System.out.println("down");
                            positionY -= 10;
                            break;
                        case LEFT:
                            System.out.println("left");
                            positionX -= 10;
                            break;
                        case RIGHT:
                            System.out.println("right");
                            positionX += 10;
                            break;
                    }
                }

            }
        };
    }
}
