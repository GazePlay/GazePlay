package net.gazeplay.games.surviveAgainstRobots;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;

public class SurviveAgainstRobots extends Parent implements GameLifeCycle {


    private final IGameContext gameContext;
    private final Stats stats;
    private ArrayList<Rectangle> obstacles;
    private final SurviveAgainstRobotsVariant gameVariant;

    protected Player player;


    public SurviveAgainstRobots(final IGameContext gameContext, SurviveAgainstRobotsVariant gameVariant, Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.obstacles = new ArrayList<>();
    }

    public void startGame(){

        this.player = new Player(new Rectangle(500,500,100,100),10,gameContext,this, 0.5);

        this.player.hitbox.setFill(Color.BLACK);
        gameContext.getChildren().add(this.player.hitbox);
        this.player.hitbox.toFront();

        System.out.println("size of obstacles" + obstacles.size());

    }


    @Override
    public void launch() {

        this.obstacles.clear();



        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        gameContext.setLimiterAvailable();
        Rectangle background = new Rectangle(0,0,dimension2D.getWidth(), dimension2D.getHeight());
        background.setFill(Color.WHITE);

        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.getChildren().add(background);
        initGameBox();
        startGame();
        stats.notifyNewRoundReady();
        gameContext.firstStart();
    }

    @Override
    public void dispose() {

    }

    /**
     * Initializes the game box by creating and adding four wall obstacles to the game panel.
     * The walls are black and are positioned at the edges of the game panel to prevent the cat from leaving the playing area.
     * The walls are also added to the game's list of obstacles.
     */
    private void initGameBox(){

        // Get the dimensions of the game panel from the game context
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        // Create and position the four wall obstacles
        Rectangle upWall = new Rectangle(0,0,dimension2D.getWidth(),50);
        upWall.setFill(Color.BLACK);

        Rectangle downWall = new Rectangle(0,dimension2D.getHeight()-50,dimension2D.getWidth(),50);
        downWall.setFill(Color.BLACK);

        Rectangle leftWall = new Rectangle(0,0,50,dimension2D.getHeight());
        leftWall.setFill(Color.BLACK);

        Rectangle rightWall = new Rectangle(dimension2D.getWidth()-50,0,50,dimension2D.getHeight());
        rightWall.setFill(Color.BLACK);

        // Add the wall obstacles to the game's list of obstacles
        this.obstacles.add(upWall);
        this.obstacles.add(downWall);
        this.obstacles.add(leftWall);
        this.obstacles.add(rightWall);

        // Add the wall obstacles to the game panel
        gameContext.getChildren().add(upWall);
        gameContext.getChildren().add(downWall);
        gameContext.getChildren().add(leftWall);
        gameContext.getChildren().add(rightWall);
    }




    /**
     * Method to check if the object will collide with an obstacle in a given direction.
     *
     * @param direction direction to check for collision
     * @param object object that has to be checked
     * @param speed speed of the object
     * @return true if the object will collide with an obstacle, false otherwise
     */
    protected boolean willCollideWithAnObstacle(String direction, double speed, Rectangle object) {
        // Calculate the next position of the object based on the given direction and speed
        double nextX = object.getX();
        double nextY = object.getY();
        switch (direction) {
            case "left" -> nextX -= speed;
            case "right" -> nextX += speed;
            case "up" -> nextY -= speed;
            case "down" -> nextY += speed;
        }
        Rectangle futurePos = new Rectangle(nextX, nextY, object.getWidth(), object.getHeight());

        // Check if the object collides with any of the obstacles
        for (Rectangle obstacle : obstacles) {
            // If the object is the same as the obstacle, skip the current iteration
            if (obstacle.equals(object)) {
                continue;
            }


            if (isCollidingWithASpecificObstacle(futurePos, obstacle)) {
                if (object instanceof Bullet){
                    if (obstacle == player.hitbox){
                        continue;
                    }else{
                        gameContext.getChildren().remove(object);
                    }

                }


                return true;
            }
        }
        return false;
    }



    /**
     * Determines whether two rectangles are colliding with each other.
     * @param object1 the first rectangle to check for collision
     * @param object2 the second rectangle to check for collision against
     * @return true if the two rectangles are colliding, false otherwise
     */
    protected boolean isCollidingWithASpecificObstacle(Rectangle object1, Rectangle object2){

        return object1.getX() < object2.getX() + object2.getWidth() && object1.getX() + object1.getWidth() > object2.getX()
            && object1.getY() < object2.getY() + object2.getHeight() && object1.getY() + object1.getHeight() > object2.getY();

    }
}


