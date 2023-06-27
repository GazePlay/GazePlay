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
import java.util.Collections;
import java.util.Random;

public class GazeRace extends Parent implements GameLifeCycle {


    private IGameContext gameContext;
    private Stats stats;
    private ArrayList<Rectangle> obstacles;
    private ArrayList<Car> cars;
    private ArrayList<Color> colors;

    private Rectangle leftDeathBarrier;
    private Rectangle rightDeathBarrier;
    private final ImagePattern death = new ImagePattern(new Image("data/gazeRace/Flash.png"));


    private Player player;
    protected double mphGeneral;
    private Random random;
    private AnimationTimer generalAnimation;
    private ArrayList<BackgroundRoad> backgroundRoadsTop,backgroundRoadsBot;

    private final Enum<GazeRaceVariant> gameVariant;

    public GazeRace(IGameContext gameContext, Stats stats, Enum<GazeRaceVariant> gameVariant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.obstacles = new ArrayList<>();
        this.cars = new ArrayList<>();
        this.random = new Random();
        this.backgroundRoadsTop = new ArrayList<>();
        this.backgroundRoadsBot = new ArrayList<>();
        this.colors = new ArrayList<>();
        this.gameVariant = gameVariant;
        this.mphGeneral = 5;
    }


    private void startGame(){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.colors.add(Color.RED);
        this.colors.add(Color.BROWN);
        this.colors.add(Color.GREEN);
        this.colors.add(Color.YELLOW);
        this.colors.add(Color.MAGENTA);
        Collections.shuffle(colors);

        if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){
            this.player = new Player(300,300,125,75,this,gameContext,7,3, true);
        }else{
            this.player = new Player(300,300,75,125,this,gameContext,7,3, false);
        }

        // Add the player's hitbox to the game context's children and obstacles list
        gameContext.getChildren().add(this.player);
        this.player.toFront();
        this.obstacles.add(this.player);
        int espacement = 0;

        Rectangle delimiterRoad;

        if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){
            delimiterRoad = new Rectangle(0,dimension2D.getHeight()/2-25,dimension2D.getWidth(),50);

            for (int i = 0; i < 21; i++){
                BackgroundRoad backgroundRoadTop = new BackgroundRoad(espacement,dimension2D.getHeight()/4,200,50,mphGeneral,gameVariant);
                backgroundRoadsTop.add(backgroundRoadTop);

                BackgroundRoad backgroundRoadBot = new BackgroundRoad(espacement,dimension2D.getHeight()*0.75,200,50,mphGeneral,gameVariant);
                backgroundRoadsBot.add(backgroundRoadBot);

                gameContext.getChildren().add(backgroundRoadTop);
                gameContext.getChildren().add(backgroundRoadBot);
                espacement+=300;
            }
        }else{
            delimiterRoad = new Rectangle(dimension2D.getWidth()/2-25,0,50,dimension2D.getHeight());

            for (int i = 0; i < 18; i++){
                BackgroundRoad backgroundRoadTop = new BackgroundRoad(dimension2D.getWidth()/4, dimension2D.getHeight()-espacement,50,250,mphGeneral,gameVariant);
                backgroundRoadsTop.add(backgroundRoadTop);

                BackgroundRoad backgroundRoadBot = new BackgroundRoad(dimension2D.getWidth()*0.75,dimension2D.getHeight()-espacement,50,250,mphGeneral,gameVariant);
                backgroundRoadsBot.add(backgroundRoadBot);

                gameContext.getChildren().add(backgroundRoadTop);
                gameContext.getChildren().add(backgroundRoadBot);
                espacement+=300;
            }
        }
        delimiterRoad.setFill(Color.WHITE);
        gameContext.getChildren().add(delimiterRoad);



        generalAnimation = new AnimationTimer() {
            int nbframes = 0;
            int nbSecond = 0;
            int nbCar = 0;
            @Override
            public void handle(long now) {

                player.toFront();
                if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){
                    if (backgroundRoadsTop.get(6).getX() <= 0){
                        resetBackgroundPos();
                    }
                }else{

                    if (backgroundRoadsTop.get(6).getY() >= dimension2D.getHeight()){
                        resetBackgroundPos();
                    }
                }

                if (nbframes == 30){
                    nbCar = random.nextInt(2,4);
                    for (int i = 0; i < nbCar; i++){
                        generateCar();
                    }
                }


                if (nbframes == 60){
                    nbSecond++;
                    if (mphGeneral >= 10.5){
                        mphGeneral += 0.2;
                    }
                    mphGeneral += 0.2;
                    updateSpeedObject();
                    nbframes = 0;
                }
                nbframes++;
            }
        };
        generalAnimation.start();

    }


    private void resetBackgroundPos(){
        int espacement = 0;
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        for (BackgroundRoad backgroundRoad : backgroundRoadsTop){
            if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){
                backgroundRoad.setX(espacement);
            }else{
                backgroundRoad.setY(dimension2D.getHeight()-espacement);
            }
            espacement+=300;
        }
        espacement = 0;
        for (BackgroundRoad backgroundRoad : backgroundRoadsBot){
            if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){
                backgroundRoad.setX(espacement);
            }else{
                backgroundRoad.setY(dimension2D.getHeight()-espacement);
            }
            espacement+=300;
        }
    }

    private void updateSpeedObject() {


        for (Car car : cars) {
            car.speed = mphGeneral;
        }

        for (BackgroundRoad backgroundRoad : backgroundRoadsTop){
            backgroundRoad.speed = mphGeneral;
        }

        for (BackgroundRoad backgroundRoad : backgroundRoadsBot){
            backgroundRoad.speed = mphGeneral;
        }

    }

    private void generateCar(){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Collections.shuffle(colors);
        double x = 0;
        double y = 0;
        double widthRange;
        double heightRange;

        boolean isValid = true;
        String direction = "left";
        widthRange = 125;
        heightRange = 75;
        Rectangle futureCar;
        int res = random.nextInt(1,3);
        if (res == 1){
            direction = "left";
        }else{
            direction = "right";
        }
        if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){

            if (direction.compareToIgnoreCase("left") == 0){
                x = random.nextDouble(dimension2D.getWidth()+250,dimension2D.getWidth()+1000);
                y = random.nextDouble(dimension2D.getHeight()/2+50,dimension2D.getHeight()-125);
            }else if (direction.compareToIgnoreCase("right") == 0){
                x = random.nextDouble(-1000,-250);
                y = random.nextDouble(100,dimension2D.getHeight()/2-100);
            }
            futureCar = new Rectangle(x,y,widthRange,heightRange);
            if (isCollidingWithAnObstacle(futureCar) || (y > dimension2D.getHeight()/2-25 && y < dimension2D.getHeight()/2+25)){
                isValid = false;
            }
        }else if (gameVariant.equals(GazeRaceVariant.VERTICAL)){
            System.out.println("vertical");
            widthRange = 75;
            heightRange = 125;

            if (direction.compareToIgnoreCase("left") == 0){
                x = random.nextDouble(150,dimension2D.getWidth()/2-100);
                y = random.nextDouble(dimension2D.getHeight()+250,dimension2D.getHeight()+1000);
            }else if (direction.compareToIgnoreCase("right") == 0){
                x = random.nextDouble(dimension2D.getWidth()/2+25,dimension2D.getWidth()-150);
                y = random.nextDouble(-1000,-250);
            }
            futureCar = new Rectangle(x,y,widthRange,heightRange);
            if (isCollidingWithAnObstacle(futureCar)){
                isValid = false;
            }
        }

        if (isValid){

            Car car;
            if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){

                car = new Car(x,y,widthRange,heightRange,gameContext,this,mphGeneral,direction,colors.get(0), gameVariant);

            }else{

                if (x > dimension2D.getWidth()/2+50 && x < dimension2D.getWidth()){
                    car = new Car(x,y,widthRange,heightRange,gameContext,this,mphGeneral,"up",colors.get(0), gameVariant);
                }else{
                    car = new Car(x,y,widthRange,heightRange,gameContext,this,mphGeneral,"down",colors.get(0), gameVariant);
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
        this.backgroundRoadsTop.clear();
        this.backgroundRoadsBot.clear();
        this.mphGeneral = 5;
        if (generalAnimation != null){
            generalAnimation.stop();
        }

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
                nextX += speed;
                nextY += speed;
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
                    if (obstacle instanceof Car){
                        if (!player.isInvincible){
                            player.startTimerInvincible();
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
                        playDeathAnimation(1,car);

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

        if(object instanceof Car car){
            car.isDestroyed = true;
            cars.remove(car);
            obstacles.remove(car);
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



            Rectangle upWall = new Rectangle(0,0,dimension2D.getWidth(),50);
            Rectangle downWall = new Rectangle(0,dimension2D.getHeight()-50,dimension2D.getWidth(),50);
            Rectangle leftWall = new Rectangle(0,0,50,dimension2D.getHeight());
            Rectangle rightWall = new Rectangle(dimension2D.getWidth()-50,0,50,dimension2D.getHeight());
        if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){

            upWall.setFill(Color.WHITE);
            downWall.setFill(Color.WHITE);
            leftWall.setFill(Color.TRANSPARENT);
            rightWall.setFill(Color.TRANSPARENT);

            this.leftDeathBarrier = new Rectangle(-60,dimension2D.getHeight()/2+50,50,dimension2D.getHeight());
            this.rightDeathBarrier = new Rectangle(dimension2D.getWidth()+50,-60,50,dimension2D.getHeight()/2-50);
        }else{

            upWall.setFill(Color.TRANSPARENT);
            downWall.setFill(Color.TRANSPARENT);
            leftWall.setFill(Color.WHITE);
            rightWall.setFill(Color.WHITE);

            this.leftDeathBarrier = new Rectangle(-60,-60, dimension2D.getWidth()/2-25,50);
            this.rightDeathBarrier = new Rectangle(dimension2D.getWidth()/2+25,dimension2D.getHeight()+60,dimension2D.getWidth(),50);

        }
        this.leftDeathBarrier.setFill(Color.TRANSPARENT);
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


    private static class BackgroundRoad extends Rectangle{

        protected boolean stopTimer;
        protected double speed;
        public BackgroundRoad(double x, double y, double width, double height, double speed2, Enum<GazeRaceVariant> gameVariant) {
            super(x, y, width, height);
            setFill(Color.WHITE);
            stopTimer = false;
            speed = speed2;
            AnimationTimer timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (!stopTimer){
                        if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){
                            setX(getX()-speed);
                        }else{
                            setY(getY()+speed);
                        }
                    }else{
                        stop();
                    }
                }
            };
            timer.start();
        }
    }

}


