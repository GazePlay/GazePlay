package net.gazeplay.games.surviveAgainstRobots;

import javafx.animation.AnimationTimer;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.Random;

public class SurviveAgainstRobots extends Parent implements GameLifeCycle {


    private final IGameContext gameContext;
    private final Stats stats;
    private ArrayList<Rectangle> obstacles;
    private ArrayList<Robot> robots;
    private final SurviveAgainstRobotsVariant gameVariant;

    protected Player player;
    private int scorePoint;
    private int nbMaxRobot;
    private AnimationTimer timerGame;



    public SurviveAgainstRobots(final IGameContext gameContext, SurviveAgainstRobotsVariant gameVariant, Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.obstacles = new ArrayList<>();
        this.robots = new ArrayList<>();
        this.scorePoint = 1;
        this.nbMaxRobot = 1;
    }

    public void startGame(){

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.player = new Player(new Rectangle(500,500,100,100),10,gameContext,this, 0.5);

        this.player.hitbox.setFill(Color.BLACK);
        gameContext.getChildren().add(this.player.hitbox);
        this.player.hitbox.toFront();
        this.obstacles.add(this.player.hitbox);



        timerGame = new AnimationTimer() {

            int nbframes = 0;
            int nbSecond = 0;
            int scoreIncrementRobot = 20;
            @Override
            public void handle(long now) {

                nbframes++;
                if (nbframes == 60){
                    nbframes = 0;
                    nbSecond++;
                    if (nbSecond % 3 == 0){
                        scorePoint++;
                    }
                    if (robots.size() < nbMaxRobot){
                        createRobot();
                    }
                }

                if (scorePoint > scoreIncrementRobot){
                    if (nbMaxRobot < 4){
                        nbMaxRobot++;
                    }
                    scoreIncrementRobot+=20;
                }



            }
        };
        timerGame.start();
    }

    private void onRobotKilled(Rectangle robot){
        if (robot instanceof Robot){
            gameContext.getChildren().remove(robot);
            obstacles.remove(robot);
            robots.remove(robot);
        }
        scorePoint++;
    }

    private void createRobot(){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double x;
        double y;
        Random randomPosX = new Random();
        x = randomPosX.nextDouble(200,dimension2D.getWidth()-200);
        y = randomPosX.nextDouble(200,dimension2D.getHeight()-200);
        Rectangle verifPos = new Rectangle(x,y,100,100);

        for (Robot value : robots) {
            if (isCollidingWithASpecificObstacle(verifPos, value)) {
                createRobot();
                return;
            }
        }
        if (isCollidingWithASpecificObstacle(this.player.hitbox,verifPos)){
            createRobot();
        }else{
            Robot robot = new Robot(x,y,100,100,5,gameContext,this);
            robots.add(robot);
            this.obstacles.add(robot);
            gameContext.getChildren().add(robot);
        }

    }

    @Override
    public void launch() {

        this.obstacles.clear();
        this.robots.clear();
        if (this.timerGame != null){
            this.timerGame.stop();
        }
        if (this.player != null){
            this.player.playerAnimationMovement.stop();
        }


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
        gameContext.getChildren().clear();
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

                    if (obstacle instanceof Robot){
                        onRobotKilled(obstacle);
                    }

                }

                if (object instanceof Robot || obstacle instanceof Robot){
                    if (obstacle == player.hitbox || object == player.hitbox){
                        endOfGame();
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

    protected void endOfGame(){
        dispose();
        launch();

    }
}


