package net.gazeplay.games.divisor;

import java.util.Random;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.Position;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.stats.Stats;

/**
 *
 * @author vincent
 */
@Slf4j
class Target extends Portrait {

    private TranslateTransition currentTranslation;
    private final RandomPositionGenerator randomPosGenerator;
    private final Stats stats;
    private final int difficulty;
    private final int level;
    private final int speed;
    private final EventHandler<Event> enterEvent;
    private final GameContext gameContext;
    private final Image[] images;

    public Target(GameContext gameContext, RandomPositionGenerator randomPositionGenerator, Stats stats, Image[] images,
            int level) {
        super((int)200/(level+1), randomPositionGenerator, images);
        this.level = level;
        this.difficulty = 3;
        this.speed = 1;
        this.randomPosGenerator = randomPositionGenerator;
        this.gameContext = gameContext;
        this.stats = stats;
        this.images = images;

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    enter();
                }
            }
        };
        
        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventHandler(GazeEvent.ANY, enterEvent);

        move();

        stats.start();
    }

    private void move() {
        Random r = new Random();
        final int length = speed * 1000;

        final Position newPosition = randomPosGenerator.newRandomPosition(getInitialRadius());

        TranslateTransition translation = new TranslateTransition(new Duration(length), this);
        translation.setByX(-this.getCenterX() + newPosition.getX());
        translation.setByY(-this.getCenterY() + newPosition.getY());
        translation.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                Target.this.setScaleX(1);
                Target.this.setScaleY(1);
                Target.this.setScaleZ(1);

                Target.this.setPosition(newPosition);

                Target.this.setTranslateX(0);
                Target.this.setTranslateY(0);
                Target.this.setTranslateZ(0);

                move();
            }
        });

        currentTranslation = translation;
        translation.play();
    }

    private void enter() {
        Transition runningTranslation = currentTranslation;
        if (runningTranslation != null) {
            runningTranslation.stop();
        }

        this.removeEventFilter(MouseEvent.ANY, enterEvent);
        this.removeEventFilter(GazeEvent.ANY, enterEvent);

        this.gameContext.getChildren().remove(this);

        if (this.level < this.difficulty) {
            createChildren();
        } else if (this.level == this.difficulty && this.gameContext.getChildren().isEmpty()) {
            log.info("VICTOIRE !!!");
        }
    }

    private void createChildren() {
        for (int i = 0; i < 2; i++) {
            Target target = new Target(this.gameContext, this.randomPosGenerator, this.stats, this.images,
                    this.level + 1);
            this.gameContext.getChildren().add(target);
        }
    }

}
