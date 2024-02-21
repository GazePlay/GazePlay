package net.gazeplay.games.gazeRace;

import javafx.animation.AnimationTimer;
import javafx.animation.RotateTransition;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.surviveAgainstRobots.SurviveAgainstRobots;


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

    private int nbRect = 0;
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

        // Create and add the score label to the game context's children
        Label score = new Label();
        score.setLayoutX(50);
        score.setLayoutY(50);
        score.setFont(new Font("Arial", 50));
        score.toFront();
        score.setText("Score : 0");
        score.setStyle("-fx-text-fill: gray ;");
        score.setOpacity(0.8);
        gameContext.getChildren().add(score);

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
            nbRect = 0;

            while(espacement <= dimension2D.getWidth()){
                espacement+=300;
                nbRect++;
            }

            espacement = 0;
            nbRect*=3;


            for (int i = 0; i < nbRect; i++){
                BackgroundRoad backgroundRoadTop = new BackgroundRoad(espacement,dimension2D.getHeight()/4,200,50,mphGeneral,gameVariant,false);
                backgroundRoadsTop.add(backgroundRoadTop);

                BackgroundRoad backgroundRoadBot = new BackgroundRoad(espacement,dimension2D.getHeight()*0.75,200,50,mphGeneral,gameVariant,false);
                backgroundRoadsBot.add(backgroundRoadBot);

                gameContext.getChildren().add(backgroundRoadTop);
                gameContext.getChildren().add(backgroundRoadBot);
                espacement+=300;
            }
        }else{
            delimiterRoad = new Rectangle(dimension2D.getWidth()/2-25,0,50,dimension2D.getHeight());
            nbRect = 0;

            while(espacement <= dimension2D.getWidth()){
                espacement+=300;
                nbRect++;
            }

            espacement = 0;
            nbRect*=3;
            for (int i = 0; i < nbRect; i++){
                BackgroundRoad backgroundRoadTop = new BackgroundRoad(dimension2D.getWidth()/4, dimension2D.getHeight()-espacement,50,250,mphGeneral,gameVariant,false);
                backgroundRoadsTop.add(backgroundRoadTop);

                BackgroundRoad backgroundRoadBot = new BackgroundRoad(dimension2D.getWidth()*0.75,dimension2D.getHeight()-espacement,50,250,mphGeneral,gameVariant,false);
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
                if (nbframes == 10){
                    score.setText("Score : " + stats.getNbGoalsReached());
                }


                if (nbframes == 60){
                    nbSecond++;
                    if (nbSecond%12 == 0){
                        BackgroundRoad obstacle;
                        if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){
                            obstacle = new BackgroundRoad(dimension2D.getWidth()+500,dimension2D.getHeight()/2-25,300,50,mphGeneral,gameVariant,true);
                        }else{
                            obstacle = new BackgroundRoad(dimension2D.getWidth()/2-25,-500,50,300,mphGeneral,gameVariant,true);
                            obstacle.dimension2D = dimension2D;
                        }



                        gameContext.getChildren().add(obstacle);
                        gameContext.getChildren().add(obstacle.imageRight);
                        gameContext.getChildren().add(obstacle.imageLeft);
                        obstacle.imageRight.toFront();
                        obstacle.setFill(Color.ORANGE);
                        obstacles.add(obstacle);
                    }
                    if (mphGeneral <= 15){
                        mphGeneral += 0.2;
                    }
                    System.out.println(mphGeneral);
                    if (nbSecond % 3 == 0){
                        stats.incrementNumberOfGoalsReached();
                        gameContext.updateScore(stats, GazeRace.this);
                    }
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

                car = new Car(x,y,widthRange,heightRange,mphGeneral,direction,colors.get(0), gameVariant);

            }else{

                if (x > dimension2D.getWidth()/2+50 && x < dimension2D.getWidth()){
                    car = new Car(x,y,widthRange,heightRange,mphGeneral,"up",colors.get(0), gameVariant);
                }else{
                    car = new Car(x,y,widthRange,heightRange,mphGeneral,"down",colors.get(0), gameVariant);
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
                    if (obstacle instanceof Car || obstacle instanceof BackgroundRoad){
                        boolean obstacledestroy = true;
                        if (obstacle instanceof BackgroundRoad backgroundRoad){
                            if (!backgroundRoad.obstacle){
                                continue;
                            }else{
                                obstacledestroy = false;
                            }
                        }
                        if (!player.isInvincible){
                            player.startTimerInvincible();
                            if (obstacledestroy){
                                playDeathAnimation(1,obstacle);
                            }
                            player.health--;
                            RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1), player);
                            rotateTransition.setByAngle(360); // Angle de rotation (360 degrés dans ce cas)
                            rotateTransition.play();

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
        private final boolean obstacle;
        protected ImageView imageLeft,imageRight;
        protected Dimension2D dimension2D;
        public BackgroundRoad(double x, double y, double width, double height, double speed2, Enum<GazeRaceVariant> gameVariant, boolean obstacle) {
            super(x, y, width, height);
            setFill(Color.WHITE);
            stopTimer = false;
            speed = speed2;
            this.obstacle = obstacle;
            imageLeft = null;
            imageRight = null;
            if (this.obstacle){
                imageLeft = new ImageView(new Image("data/gazeRace/cone-construction-road.png"));
                imageRight = new ImageView(new Image("data/gazeRace/cone-construction-road.png"));
                imageLeft.setFitWidth(50);
                imageLeft.setFitHeight(50);
                imageRight.setFitWidth(50);
                imageRight.setFitHeight(50);
                imageLeft.setX(getX());
                imageLeft.setY(getY());
                if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){
                    imageRight.setX(getX()+getWidth()-imageRight.getFitWidth());
                    imageRight.setY(getY());
                }else{
                    imageRight.setY(getY()+getHeight()-imageRight.getFitHeight());
                    imageRight.setX(getX());
                }
            }
            AnimationTimer timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (!stopTimer){
                        if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){
                            if (obstacle){
                                if (imageRight.getX() < -100){
                                    stopTimer = true;
                                }
                                imageLeft.setX(imageLeft.getX()-speed);
                                imageRight.setX(imageRight.getX()-speed);

                            }
                            setX(getX()-speed);

                        }else{
                            if (obstacle){
                                if (imageLeft.getY() > dimension2D.getHeight()+100){
                                    stopTimer = true;
                                }
                                imageLeft.setY(imageLeft.getY()+speed);
                                imageRight.setY(imageRight.getY()+speed);

                            }
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


