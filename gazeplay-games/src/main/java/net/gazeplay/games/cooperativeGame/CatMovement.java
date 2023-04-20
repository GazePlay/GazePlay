package net.gazeplay.games.cooperativeGame;

import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import javafx.scene.input.KeyEvent;

import java.util.ArrayList;

/**
 * Class for managing the movement of the cat character in the cooperative game.
 * This class inherits from the Cat class.
 */
public class CatMovement extends Cat{

    private Timeline timelineProgressBar;

    /**
     * Constructor for creating a CatMovement object with an initial position, size, game context,
     * stats, cooperative game instance, speed, and a list of obstacles.
     *
     * @param positionX initial X position of the cat
     * @param positionY initial Y position of the cat
     * @param width width of the cat
     * @param height height of the cat
     * @param gameContext context of the game
     * @param stats statistics for the game
     * @param gameInstance instance of the cooperative game
     * @param speed speed of the cat's movement
     * @param obstacles list of obstacles
     */
    public CatMovement(double positionX, double positionY, double width, double height, IGameContext gameContext, Stats stats, CooperativeGame gameInstance, float speed, ArrayList<Rectangle> obstacles) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance, speed, obstacles);

        gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, key-> {
            if (key.getCode() == KeyCode.UP || key.getCode() == KeyCode.DOWN || key.getCode() == KeyCode.LEFT || key.getCode() == KeyCode.RIGHT) {

                switch (key.getCode()) {
                    case UP -> {
                        if (!willCollideWithAnObstacle(obstacles, "up")) {
                            this.cat.setY(this.cat.getY() - speed);
                        }
                    }
                    case DOWN -> {
                        if (!willCollideWithAnObstacle(obstacles, "down")) {
                            this.cat.setY(this.cat.getY() + speed);
                        }
                    }
                    case LEFT -> {
                        if (!willCollideWithAnObstacle(obstacles, "left")) {
                            this.cat.setX(this.cat.getX() - speed);
                        }
                    }
                    case RIGHT -> {
                        if (!willCollideWithAnObstacle(obstacles, "right")) {
                            this.cat.setX(this.cat.getX() + speed);
                        }
                    }
                }
            }
        });
    }

    /**
     * Method to check if the cat will collide with an obstacle in a given direction.
     *
     * @param obstacles list of obstacles
     * @param direction direction to check for collision
     * @return true if the cat will collide with an obstacle, false otherwise
     */
    private boolean willCollideWithAnObstacle(ArrayList<Rectangle> obstacles, String direction){

        double nextX = this.cat.getX();
        double nextY = this.cat.getY();

        switch (direction) {
            case "left" -> nextX -= speed;
            case "right" -> nextX += speed;
            case "up" -> nextY -= speed;
            case "down" -> nextY += speed;
        }

        for (Rectangle obstacle : obstacles) {
            if (nextX < obstacle.getX() + obstacle.getWidth() && nextX + this.cat.getWidth() > obstacle.getX()
                && nextY < obstacle.getY() + obstacle.getHeight() && nextY + this.cat.getHeight() > obstacle.getY()) {
                return true;
            }
        }

        return false;
    }
}
