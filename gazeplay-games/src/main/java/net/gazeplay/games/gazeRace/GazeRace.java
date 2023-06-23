package net.gazeplay.games.gazeRace;

import javafx.animation.AnimationTimer;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;


import java.util.ArrayList;
import java.util.Random;

public class GazeRace extends Parent implements GameLifeCycle {


    private IGameContext gameContext;
    private Stats stats;
    private ArrayList<Rectangle> obstacles;
    private ArrayList<Car> cars;

    private Rectangle leftDeathBarrier;
    private Rectangle rightDeathBarrier;
    private final ImagePattern death = new ImagePattern(new Image("data/gazeRace/Flash.png"));

    private Player player;
    protected double mphGeneral;
    private Random random;
    private AnimationTimer generalAnimation;
    private Enum<GazeRaceVariant> gameVariant;

    public GazeRace(IGameContext gameContext, Stats stats, Enum<GazeRaceVariant> gameVariant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.obstacles = new ArrayList<>();
        this.cars = new ArrayList<>();
        this.random = new Random();
        this.gameVariant = gameVariant;
        this.mphGeneral = 5;
    }


    private void startGame(){


        this.player = new Player(300,300,100,100,this,gameContext,5,3);

        // Add the player's hitbox to the game context's children and obstacles list
        gameContext.getChildren().add(this.player);
        this.player.toFront();
        this.obstacles.add(this.player);


        generalAnimation = new AnimationTimer() {
            int nbframes = 0;
            int nbSecond = 0;
            int nbCar = 0;
            @Override
            public void handle(long now) {


                updateSpeedObject();
                if (nbframes == 30){
                    nbCar = random.nextInt(2,4);
                    for (int i = 0; i < nbCar; i++){
                        generateCar();
                    }
                }


                if (nbframes == 60){
                    nbSecond++;
                    mphGeneral += 0.2;
                    updateSpeedObject();
                    nbframes = 0;
                }
                nbframes++;
            }
        };
        generalAnimation.start();

    }
    private void updateSpeedObject(){
        for (Car car : cars){
            car.speed = mphGeneral;
        }
    }

    private void generateCar(){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double x = 0;
        double y = 0;
        double widthRange;
        double heightRange;

        boolean isValid = true;
        String direction = "left";
        widthRange = 75;
        heightRange = 75;
        Rectangle futureCar;
        int res = random.nextInt(1,3);
        if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){
            if (res == 1){
                direction = "left";
            }else{
                direction = "right";
            }
            if (direction.compareToIgnoreCase("left") == 0){
                x = random.nextDouble(dimension2D.getWidth()+250,dimension2D.getWidth()+1000);
                y = random.nextDouble(dimension2D.getHeight()/2+50,dimension2D.getHeight()-100);
            }else if (direction.compareToIgnoreCase("right") == 0){
                x = random.nextDouble(-1000,-250);
                y = random.nextDouble(100,dimension2D.getHeight()/2-50);
            }
            futureCar = new Rectangle(x,y,widthRange,heightRange);
            if (isCollidingWithAnObstacle(futureCar) || (y > dimension2D.getHeight()/2-25 && y < dimension2D.getHeight()/2+25)){
                isValid = false;
            }
        }else if (gameVariant.equals(GazeRaceVariant.VERTICAL)){
            System.out.println("vertical");




            /*if (isCollidingWithAnObstacle(futureCar) || (x > dimension2D.getWidth()/2-25 && x < dimension2D.getWidth()/2+25)){
                isValid = false;
            }*/
        }

        if (isValid){

            Car car;
            if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){

                car = new Car(x,y,widthRange,heightRange,gameContext,this,mphGeneral,direction);

            }else{
                if (x > dimension2D.getWidth()/2+50 && x < dimension2D.getWidth()){
                    car = new Car(x,y,widthRange,heightRange,gameContext,this,mphGeneral,"up");
                }else{
                    car = new Car(x,y,widthRange,heightRange,gameContext,this,mphGeneral,"down");
                }
            }

            this.obstacles.add(car);
            this.gameContext.getChildren().add(car);
            this.cars.add(car);

        }else{
            generateCar();
        }
    }



    @Override
    public void launch() {

        this.obstacles.clear();
        this.cars.clear();
        this.random = new Random();
        this.mphGeneral = 5;

        if (this.player != null){
            this.player.playerAnimationMovement.stop();
        }

        this.stats.reset();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        gameContext.setLimiterAvailable();
        Rectangle background = new Rectangle(0,0,dimension2D.getWidth(), dimension2D.getHeight());
        background.setFill(Color.BLACK);

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

    protected boolean willCollideWithAnObstacle(String direction, double speed, Rectangle object) {
        // Calculate the next position of the object based on the given direction and speed
        double nextX = object.getX();
        double nextY = object.getY();
        switch (direction) {
            case "left" -> nextX -= speed;
            case "right" -> nextX += speed;
            case "up" -> nextY -= speed;
            case "down" -> nextY += speed;
            default -> {

            }
        }
        Rectangle futurePos = new Rectangle(nextX, nextY, object.getWidth(), object.getHeight());

        // Check if the object collides with any of the obstacles
        for (Rectangle obstacle : obstacles) {
            // If the object is the same as the obstacle, skip the current iteration
            if (obstacle.equals(object)) {
                continue;
            }

            if (isCollidingWithASpecificObstacle(futurePos, obstacle)) {
                if (object == player || obstacle == player){
                    if (obstacle instanceof Car car){
                        if (!player.isInvincible){
                            player.startTimerInvincible();
                            car.isDestroyed = true;
                            cars.remove(car);
                            obstacles.remove(car);
                            playDeathAnimation(1,obstacle);
                            player.health--;
                            mphGeneral = 5;
                            if (player.health == 0){
                                playDeathAnimation(1,player);
                            }
                        }
                        if (player.isInvincible){
                            return false;
                        }
                    }
                }

                if (object instanceof Car car){
                    if (obstacle.equals(leftDeathBarrier) || obstacle.equals(rightDeathBarrier)){
                        car.isDestroyed = true;
                        gameContext.getChildren().remove(object);
                        cars.remove(car);
                        obstacles.remove(car);

                    }
                }


                return true;
            }
        }
        return false;
    }

    private void playDeathAnimation(int deathDuration, Rectangle object){
        Rectangle death = new Rectangle(object.getX(),object.getY(),object.getWidth(),object.getHeight());
        death.setFill(this.death);
        gameContext.getChildren().add(death);
        gameContext.getChildren().remove(object);

        if (object == player){
            obstacles.remove(player);
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
                    if (object == player){
                        endOfGame();
                    }
                    stop();
                }

                nbframes++;
            }
        };
        deathAnimation.start();
    }

    private void initGameBox(){

        // Get the dimensions of the game panel from the game context
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        // Create and position the four wall obstacles
        Rectangle upWall = new Rectangle(0,0,dimension2D.getWidth(),50);
        upWall.setFill(Color.WHITE);

        Rectangle downWall = new Rectangle(0,dimension2D.getHeight()-50,dimension2D.getWidth(),50);
        downWall.setFill(Color.WHITE);

        Rectangle leftWall = new Rectangle(0,0,25,dimension2D.getHeight());
        leftWall.setFill(Color.TRANSPARENT);

        Rectangle rightWall = new Rectangle(dimension2D.getWidth()-25,0,25,dimension2D.getHeight());
        rightWall.setFill(Color.TRANSPARENT);
        this.leftDeathBarrier = new Rectangle(-60,dimension2D.getHeight()/2+50,50,dimension2D.getHeight());
        this.leftDeathBarrier.setFill(Color.TRANSPARENT);
        this.rightDeathBarrier = new Rectangle(dimension2D.getWidth()+50,-60,50,dimension2D.getHeight()/2-50);
        this.rightDeathBarrier.setFill(Color.TRANSPARENT);



        // Add the wall obstacles to the game's list of obstacles
        this.obstacles.add(upWall);
        this.obstacles.add(downWall);
        this.obstacles.add(leftWall);
        this.obstacles.add(rightWall);
        this.obstacles.add(leftDeathBarrier);
        this.obstacles.add(rightDeathBarrier);
        // Add the wall obstacles to the game panel
        gameContext.getChildren().add(upWall);
        gameContext.getChildren().add(downWall);
        gameContext.getChildren().add(leftWall);
        gameContext.getChildren().add(rightWall);
        gameContext.getChildren().add(leftDeathBarrier);
        gameContext.getChildren().add(rightDeathBarrier);
    }

    protected void endOfGame(){
        generalAnimation.stop();
        dispose();
        gameContext.showRoundStats(stats,this);

    }


    protected boolean isCollidingWithAnObstacle(Rectangle object){
        for (Rectangle obstacle : obstacles) {
            if (isCollidingWithASpecificObstacle(object, obstacle)) {
                return true;
            }
        }
        return false;
    }


    protected boolean isCollidingWithASpecificObstacle(Rectangle object1, Rectangle object2){

        return object1.getX() < object2.getX() + object2.getWidth() && object1.getX() + object1.getWidth() > object2.getX()
            && object1.getY() < object2.getY() + object2.getHeight() && object1.getY() + object1.getHeight() > object2.getY();

    }
}
