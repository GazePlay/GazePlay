package net.gazeplay.games.cooperativeGame;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;


public class CooperativeGame extends Parent implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    protected boolean endOfLevel;
    private Cat cat;
    private int level;
    private Rectangle gamelle;
    protected Rectangle mouse;
    private double dogSpeed;
    private ArrayList<Rectangle> obstacles;
    private ArrayList<Cat> dogs;

    public CooperativeGame(final IGameContext gameContext, Stats stats, int level){
        this.gameContext = gameContext;
        this.stats = stats;
        this.level = level;
        this.obstacles = new ArrayList<>();
        this.dogs = new ArrayList<>();
        this.mouse = new Rectangle(0,0,25,25);
    }



    @Override
    public void launch() {
        this.endOfLevel = false;
        this.obstacles.clear();

        gameContext.setLimiterAvailable();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Rectangle background = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());
        background.setFill(Color.WHITE);
        gameContext.getChildren().add(background);
        initGameBox();
        setLevel(level);
        gameContext.firstStart();



    }

    private void setLevel(final int i){

        this.level = i;
        System.out.println("level : " + i);
        this.dogs.clear();
        this.dogSpeed = 3;


        if (this.level == 1){
            this.cat = new CatMovement(100, 100, 75,75,gameContext,stats,this, 10, true);
            Cat dog = new CatMovement(300, 600, 75, 75, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);

            gamelle = new Rectangle(400,100, 100, 100);
            gamelle.setFill(Color.GREEN);

            obstacles.add(dog.hitbox);
            this.dogs.add(dog);

            gameContext.getChildren().add(this.cat.hitbox);
            gameContext.getChildren().add(dog.hitbox);
            gameContext.getChildren().add(gamelle);


            this.cat.hitbox.toFront();
            dog.hitbox.toFront();
            gamelle.toFront();


        }else if (this.level == 2){

            this.cat = new CatMovement(200, 200, 75,75,gameContext,stats,this, 10, true);
            Cat dog = new CatMovement(300, 600, 75, 75, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new CatMovement(500, 450, 75, 75, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);

            gamelle = new Rectangle(450,100, 150, 150);
            gamelle.setFill(Color.GREEN);

            obstacles.add(dog.hitbox);
            obstacles.add(dog2.hitbox);
            this.dogs.add(dog);
            this.dogs.add(dog2);



            gameContext.getChildren().add(this.cat.hitbox);
            gameContext.getChildren().add(dog.hitbox);
            gameContext.getChildren().add(dog2.hitbox);
            gameContext.getChildren().add(gamelle);

            this.cat.hitbox.toFront();
            dog.hitbox.toFront();
            dog2.hitbox.toFront();
            gamelle.toFront();
        }
        this.obstacles.add(this.gamelle);
        this.obstacles.add(this.cat.hitbox);
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }



    private void initGameBox(){

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Rectangle upWall = new Rectangle(0,0,dimension2D.getWidth(),50);
        upWall.setFill(Color.BLACK);

        Rectangle downWall = new Rectangle(0,dimension2D.getHeight()-50,dimension2D.getWidth(),50);
        downWall.setFill(Color.BLACK);

        Rectangle leftWall = new Rectangle(0,0,50,dimension2D.getHeight());
        leftWall.setFill(Color.BLACK);

        Rectangle rightWall = new Rectangle(dimension2D.getWidth()-50,0,50,dimension2D.getHeight());
        rightWall.setFill(Color.BLACK);

        this.obstacles.add(upWall);
        this.obstacles.add(downWall);
        this.obstacles.add(leftWall);
        this.obstacles.add(rightWall);

        for (int i = 0; i < obstacles.size(); i++){
            gameContext.getChildren().add(obstacles.get(i));
            obstacles.get(i).toFront();
        }

    }

    /**
     * Method to check if the object will collide with an obstacle in a given direction.
     *
     * @param direction direction to check for collision
     * @param object object that has to be checked
     * @param speed speed of the object
     * @return true if the object will collide with an obstacle, false otherwise
     */
    protected boolean willCollideWithAnObstacle(String direction, double speed, Rectangle object){

        double nextX = object.getX();
        double nextY = object.getY();

        switch (direction) {
            case "left" -> nextX -= speed;
            case "right" -> nextX += speed;
            case "up" -> nextY -= speed;
            case "down" -> nextY += speed;
        }


        for (Rectangle obstacle : obstacles) {
            if (!obstacle.equals(object)){
                if (nextX < obstacle.getX() + obstacle.getWidth() && nextX +  object.getWidth() > obstacle.getX()
                    && nextY < obstacle.getY() + obstacle.getHeight() && nextY +  object.getHeight() > obstacle.getY()) {

                    if (this.cat.hitbox.equals(object) && gamelle.equals(obstacle)){
                        if (!endOfLevel){
                            endOfGame(true);
                        }
                    }else{
                        for (Cat dog : this.dogs) {
                            if (dog.hitbox.equals(object) && this.cat.hitbox.equals(obstacle)) {
                                if (!endOfLevel) {
                                    endOfGame(false);
                                    break;
                                }
                            }

                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }

    protected void endOfGame(boolean win){
        endOfLevel = true;
        if(win){
            stats.incrementNumberOfGoalsReached();
            gameContext.updateScore(stats, this);
            gameContext.playWinTransition(500, actionEvent -> {
                this.level++;
                dispose();
                launch();
            });
        }else{
            dispose();
            launch();
        }
    }


}
