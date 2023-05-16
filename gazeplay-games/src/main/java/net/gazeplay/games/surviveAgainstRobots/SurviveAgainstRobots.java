package net.gazeplay.games.surviveAgainstRobots;

import javafx.animation.AnimationTimer;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.Random;
/**
 * A class representing a game of surviving against robots.
 * This class extends the Parent class and implements the GameLifeCycle interface.
 */
public class SurviveAgainstRobots extends Parent implements GameLifeCycle {


    private final IGameContext gameContext;
    private final Stats stats;
    protected ArrayList<Rectangle> obstacles;
    protected ArrayList<Robot> robots;
    private final SurviveAgainstRobotsVariant gameVariant;

    protected Player player;
    private int nbMaxRobot;
    private AnimationTimer timerGame;
    protected ArrayList<Bonus> bonuses;

    private final boolean isMouseEnable;
    protected double robotSpeed;
    protected double playerFireRate;

    private final ImagePattern death = new ImagePattern(new Image("data/surviveAgainstRobots/Flash.png"));

    protected boolean automaticShoot;
    protected boolean isShieldEnabled;


    /**
     * Constructs a new SurviveAgainstRobots object with the specified parameters.
     *
     * @param gameContext   an object of type IGameContext
     * @param gameVariant   an object of type SurviveAgainstRobotsVariant
     * @param stats         an object of type Stats
     * @param isMouseEnable a boolean representing whether the mouse is enabled
     * @param automaticShoot a boolean representing whether automatic shooting is enabled
     */
    public SurviveAgainstRobots(final IGameContext gameContext, SurviveAgainstRobotsVariant gameVariant, Stats stats, boolean isMouseEnable, boolean automaticShoot){
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.obstacles = new ArrayList<>();
        this.robots = new ArrayList<>();
        this.bonuses = new ArrayList<>();
        this.nbMaxRobot = 0;
        this.isMouseEnable = isMouseEnable;
        this.robotSpeed = 3;
        this.automaticShoot = automaticShoot;
        this.playerFireRate = 0.5;
        this.isShieldEnabled = false;
    }

    /**
     * Starts the game by initializing the player, adding the player's hitbox to the game context's children,
     * creating a score label, and setting up an animation timer for the game.
     */
    public void startGame(){


        // Create a new player instance depending on whether the mouse is enabled or not
        if (!isMouseEnable){
            this.player = new Player(new Rectangle(500,500,100,85),8,gameContext,this, playerFireRate, null);
        } else {
            Rectangle target = new Rectangle(0,0,10,10);
            this.player = new Player(new Rectangle(500,500,100,85),5,gameContext,this, playerFireRate, target);
        }

        // Add the player's hitbox to the game context's children and obstacles list
        gameContext.getChildren().add(this.player.hitbox);
        this.player.hitbox.toFront();
        this.obstacles.add(this.player.hitbox);

        // Create and add the score label to the game context's children
        Label score = new Label();
        score.setLayoutX(50);
        score.setLayoutY(50);
        score.setFont(new Font("Arial", 50));
        score.toFront();
        score.setText("Score : ");
        score.setStyle("-fx-text-fill: gray ;");
        score.setOpacity(0.8);
        gameContext.getChildren().add(score);

        // Set up an animation timer to handle game logic and update the score label
        timerGame = new AnimationTimer() {

            int nbframes = 0;
            int nbSecond = 0;
            int scoreIncrementRobot = 40;
            int nbRobot = 1;

            @Override
            public void handle(long now) {

                nbframes++;
                if (nbframes == 60){
                    nbframes = 0;
                    nbSecond++;
                    if (nbSecond % 3 == 0){
                        stats.incrementNumberOfGoalsReached();
                        gameContext.updateScore(stats, SurviveAgainstRobots.this);
                    }
                    if (robots.size() < nbRobot){
                        createRobot();
                    }
                }

                if (nbframes == 10){
                    score.setText("Score : " + stats.getNbGoalsReached());
                }
                if (stats.getNbGoalsReached() > scoreIncrementRobot){
                    if (nbRobot < nbMaxRobot){
                        nbRobot++;
                    }
                    scoreIncrementRobot+=40;
                }
            }
        };

        // Start the game's animation timer
        timerGame.start();
    }

    /**
     * Triggered when a robot is killed by the player
     * @param robot the robot that has been killed
     */
    private void onRobotKilled(Rectangle robot){
        // Generate a random number between 0 and 10
        Random random = new Random();
        int bonusrand = random.nextInt(0,3);

        // If the random number is 1, create a new bonus
        if (bonusrand == 1){
            Bonus bonus = new Bonus(robot.getX(),robot.getY(),100,100,this,gameContext);
            bonuses.add(bonus);
        }

        // Remove the obstacle and robot from their respective lists
        if (robot instanceof Robot){
            obstacles.remove(robot);
            robots.remove(robot);
        }

        // Increase the score by 5
        for (int i = 0; i <= 5;i++){
            stats.incrementNumberOfGoalsReached();
        }

        gameContext.updateScore(stats, this);
    }



    /**
     * Create a new Robot instance and add it to the game.
     * The robot's position is randomly generated, but is ensured not to collide with any existing obstacles or the player's zone.
     * The robot's shooting ability is also randomly determined based on the game variant.
     */
    private void createRobot(){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double x;
        double y;
        double widthRange;
        double heightRange;
        Random randomPosX = new Random();
        Random randomShoot = new Random();

        boolean isValid = true;

        // Generate a random position for the robot, ensuring it doesn't collide with the player's zone
        x = randomPosX.nextDouble(200,dimension2D.getWidth()-200);
        y = randomPosX.nextDouble(200,dimension2D.getHeight()-200);
        widthRange = 100;
        heightRange = 100;
        Rectangle range = new Rectangle(x,y,widthRange,heightRange);
        Rectangle playerZone = new Rectangle(player.hitbox.getX() - 200, player.hitbox.getY() - 200, 600, 600);
        if (isCollidingWithASpecificObstacle(playerZone,range)) {
            isValid = false;
        }

        // Check if the robot's position collides with any existing obstacle
        if (isValid){
            if (isCollidingWithAnObstacle(range)){
                isValid = false;
            }
        }

        if (isValid){
            Robot robot;
            int res;

            // Determine the robot's shooting ability based on the game variant
            if (gameVariant.compareTo(SurviveAgainstRobotsVariant.DIFFICULTY_EASY) == 0){
                res = 0;
            }else if (gameVariant.compareTo(SurviveAgainstRobotsVariant.DIFFICULTY_NORMAL) == 0){
                res = randomShoot.nextInt(0,6);
            }else{
                res = randomShoot.nextInt(0,4);
            }

            // Create the robot and add it to the game
            if (res == 2){
                robot = new Robot(range.getX(), range.getY(), 100, 100, robotSpeed, gameContext, this, true);
            }else{
                robot = new Robot(range.getX() , range.getY(), 100, 100, robotSpeed, gameContext, this, false);
            }
            robots.add(robot);
            this.obstacles.add(robot);
            gameContext.getChildren().add(robot);
        }else{
            // Try again if the robot's position is invalid
            createRobot();
        }
    }


    /**
     * Resets the game and launches a new round.
     * Clears obstacles, robots, bonuses, and stops existing timers and animations.
     * Initializes a new game board, sets the maximum number of robots, and starts the game.
     * Notifies the stats object that a new round is ready and initializes the gaze device manager.
     */
    @Override
    public void launch() {

        // Clear obstacles, robots, bonuses, and stop existing timers and animations
        this.obstacles.clear();
        this.robots.clear();
        if (this.timerGame != null){
            this.timerGame.stop();
        }
        if (this.player != null){
            this.player.playerAnimationMovement.stop();
        }
        for (Bonus bonus : bonuses){
            if (bonus.timer != null){
                bonus.timer.stop();
            }
        }
        bonuses.clear();

        // Reset score and maximum number of robots
        this.stats.reset();
        nbMaxRobot = 3;

        // Initialize game board and start game
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        gameContext.setLimiterAvailable();
        Rectangle background = new Rectangle(0,0,dimension2D.getWidth(), dimension2D.getHeight());
        background.setFill(Color.WHITE);

        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.getChildren().add(background);
        initGameBox();
        startGame();

        // Notify stats object that a new round is ready and initialize gaze device manager
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

                    if (obstacle == player.hitbox && object.getId().compareToIgnoreCase("playerBullet") == 0 ){
                        continue;
                    }else if (object.getId().compareToIgnoreCase("robotBullet") == 0 && obstacle instanceof Robot){
                        continue;
                    }else{
                        gameContext.getChildren().remove(object);
                    }

                    if (object.getId().compareToIgnoreCase("playerBullet") == 0){
                        if (obstacle instanceof Robot){
                            playDeathAnimation(1,obstacle);
                        }
                        if (obstacle instanceof Bonus){
                            for (Bonus bonus : bonuses) {
                                if (bonus == obstacle) {
                                    bonus.isDestroyed = true;
                                }
                            }
                        }
                    }

                    if (obstacle == player.hitbox && !isShieldEnabled && object.getId().compareToIgnoreCase("robotBullet") == 0){
                        playDeathAnimation(1,player.hitbox);
                    }
                }

                if (object instanceof Robot || obstacle instanceof Robot){
                    if ((obstacle == player.hitbox || object == player.hitbox) && !isShieldEnabled ){
                        playDeathAnimation(1,player.hitbox);
                    }
                }

                if (object == player.hitbox){
                    if (obstacle instanceof Bonus){
                        for (Bonus bonus : bonuses) {
                            if (bonus == obstacle) {
                                bonus.isDestroyed = true;
                            }
                        }
                    }
                }



                return true;
            }
        }
        return false;
    }


    protected boolean isCollidingWithAnObstacle(Rectangle object){
        for (Rectangle obstacle : obstacles) {
            if (isCollidingWithASpecificObstacle(object, obstacle)) {
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



    /**
     * Plays a death animation for a given object with a given duration.
     * @param deathDuration the duration of the death animation in seconds
     * @param object the object that needs to play the death animation
     */
    private void playDeathAnimation(int deathDuration, Rectangle object){
        Rectangle death = new Rectangle(object.getX(),object.getY(),object.getWidth(),object.getHeight());
        death.setFill(this.death);
        gameContext.getChildren().add(death);
        gameContext.getChildren().remove(object);

        if (object instanceof Robot){
            onRobotKilled(object);
        }
        if (object == player.hitbox){
            obstacles.remove(player.hitbox);
        }
        AnimationTimer deathAnimation = new AnimationTimer() {
            int nbframes = 0;
            int nbSeconds = 0;
            @Override
            public void handle(long now) {

                if (nbframes == 60){
                    nbframes = 0;
                    nbSeconds++;
                }
                if (nbSeconds == deathDuration){
                    gameContext.getChildren().remove(death);
                    if (object == player.hitbox){
                        endOfGame();
                    }
                    stop();
                }

                nbframes++;
            }
        };
        deathAnimation.start();
    }



    /**
     * Finds and returns the nearest Robot to the Player.
     * @return the nearest Robot to the Player
     */
    protected Robot getNearestRobotFromPlayer(){
        Robot nearestRobot = null;
        double minDistance = Double.MAX_VALUE;

        for (Robot robot: robots){
            double distance = distance(this.player.hitbox,robot);

            if (distance < minDistance){
                minDistance = distance;
                nearestRobot = robot;
            }
        }
        return nearestRobot;
    }

    /**
     * Calculates and returns the distance between two rectangles.
     *
     * @param rect1 the first rectangle
     * @param rect2 the second rectangle
     * @return the distance between the two rectangles
     */
    private double distance(Rectangle rect1, Rectangle rect2) {
        double dx = rect2.getX() - rect1.getX();
        double dy = rect2.getY() - rect1.getY();

        // Calculate the distance using the Pythagorean theorem
        return Math.sqrt(dx*dx + dy*dy);
    }

    protected void endOfGame(){
        dispose();
        gameContext.showRoundStats(stats,this);

    }
}


