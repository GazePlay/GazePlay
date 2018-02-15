package net.gazeplay.games.divisor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.stats.Stats;

/**
 *
 * @author vincent
 */
@Slf4j
class Target extends Portrait {

    private static final int radius = 200;
    private final int level;
    private final int nbChildren;
    private final Bounds bounds;
    private final double width;
    private final double height;

    public Target(GameContext gameContext, RandomPositionGenerator randomPositionGenerator, Stats stats,
            Image[] images) {
        super(radius, randomPositionGenerator, images);
        this.level = 2;
        this.nbChildren = 2;
        this.bounds = gameContext.getRoot().getBoundsInLocal();
        this.height = gameContext.getGazePlay().getPrimaryStage().getHeight();
        this.width = gameContext.getGazePlay().getPrimaryStage().getWidth();

        move();

        stats.start();
    }

    private void move() {
        Portrait target = this;
        // log.info("height : {} ; width : {}", height, width);
        // log.info("bounds X min : {} ; bounds X max : {}", bounds.getMinX(), bounds.getMaxX());
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(20), new EventHandler<ActionEvent>() {

            double dx = 3;
            double dy = 3;

            @Override
            public void handle(ActionEvent t) {
                target.setLayoutX(target.getLayoutX() + dx);
                target.setLayoutY(target.getLayoutY() + dy);

                if (target.getLayoutX() <= (bounds.getMinX() + target.getRadius())
                        || target.getLayoutX() >= (bounds.getMaxX() - target.getRadius())) {
                    dx = -dx;
                }

                if (target.getLayoutY() <= (bounds.getMinY() + target.getRadius())
                        || target.getLayoutY() >= (bounds.getMaxY() - target.getRadius())) {
                    dy = -dy;
                }

            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        // timeline.play();
    }
}
