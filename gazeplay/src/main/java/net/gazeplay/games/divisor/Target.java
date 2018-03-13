package net.gazeplay.games.divisor;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
    private final EventHandler<Event> enterEvent;
    private final GameContext gameContext;
    private final Divisor gameInstance;
    private final Image[] images;
    private final long startTime;

    public Target(GameContext gameContext, RandomPositionGenerator randomPositionGenerator, Stats stats, Image[] images,
            int level, long start, Divisor gameInstance) {
        super((int) 200 / (level + 1), randomPositionGenerator, images);
        this.level = level;
        this.difficulty = 3;
        this.randomPosGenerator = randomPositionGenerator;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.stats = stats;
        this.images = images;
        this.startTime = start;

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
        final Position newPosition = randomPosGenerator.newRandomPosition(getInitialRadius());

        TranslateTransition translation = new TranslateTransition(new Duration(3000), this);
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
        if (currentTranslation != null) {
            currentTranslation.stop();
        }

        this.removeEventFilter(MouseEvent.ANY, enterEvent);
        this.removeEventFilter(GazeEvent.ANY, enterEvent);

        gameContext.getChildren().remove(this);

        if (level < difficulty) {
            createChildren();
        } else if (gameContext.getChildren().isEmpty()) {
            long totalTime = (System.currentTimeMillis() - startTime) / 1000;
            Label l = new Label("Temps : " + Long.toString(totalTime) + "s");
            l.setTextFill(Color.WHITE);
            l.setFont(Font.font(50));
            l.setLineSpacing(10);
            l.setLayoutX(15);
            l.setLayoutY(14);
            gameContext.getChildren().add(l);
            gameContext.playWinTransition(50, new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent actionEvent) {
                    gameInstance.dispose();
                    gameContext.clear();
                    gameInstance.launch();
                }
            });
        }
    }

    private void createChildren() {
        for (int i = 0; i < 2; i++) {
            Target target = new Target(gameContext, randomPosGenerator, stats, images, level + 1, startTime,
                    gameInstance);
            target.setPosition(this.getPosition());
            gameContext.getChildren().add(target);
        }
    }
}
