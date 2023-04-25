package net.gazeplay.games.cooperativeGame;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;


public class CooperativeGame extends Parent implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    protected boolean endOfLevel;
    private Cat cat;
    private int level;
    private Rectangle gamelle;


    private ArrayList<Rectangle> obstacles;
    private ArrayList<Interrupteur> interrupteurs;

    private ArrayList<Cat> dogs;

    public CooperativeGame(final IGameContext gameContext, Stats stats, int level){
        this.gameContext = gameContext;
        this.stats = stats;
        this.level = level;
        this.obstacles = new ArrayList<>();
        this.dogs = new ArrayList<>();
        this.interrupteurs = new ArrayList<>();
    }





    @Override
    public void launch() {
        this.endOfLevel = false;
        this.obstacles.clear();
        this.dogs.clear();
        this.interrupteurs.clear();

        gameContext.setLimiterAvailable();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Rectangle background = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());
        background.setFill(Color.WHITE);
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.getChildren().add(background);
        initGameBox();
        setLevel(level);
        gameContext.firstStart();


    }

    private void setLevel(final int i){

        this.level = i;
        System.out.println("level : " + i);
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double dogSpeed = 3;
        int widthCat = 100;
        int heightCat = 100;
        int widthDog = 125;
        int heightDog = 125;
        int widthInterrupteur = 125;
        int heightInterrupteur = 125;
        this.cat = new Cat(0, 0, widthCat,heightCat,gameContext,stats,this, 10, true);



        if (this.level == 1){
            this.cat.hitbox.setX(100);
            this.cat.hitbox.setY(100);

            Cat dog = new Cat(900, dimension2D.getHeight()-500, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);

            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);
            this.dogs.add(dog);

        }else if (this.level == 2){
            this.cat = new Cat(150, 150, widthCat,heightCat,gameContext,stats,this, 10, true);
            Cat dog = new Cat(800, 600, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(1000, 450, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);

            gamelle = new Rectangle(dimension2D.getWidth()-350,dimension2D.getHeight()-250, 100, 100);

            this.dogs.add(dog);
            this.dogs.add(dog2);





        }else if (this.level == 3){
            this.cat.hitbox.setX(150);
            this.cat.hitbox.setY(150);
            Cat dog = new Cat(800, 600, widthDog, heightDog, gameContext, stats, this, dogSpeed+2, false, this.cat.hitbox);
            Cat dog2 = new Cat(1000, 450, widthDog, heightDog, gameContext, stats, this, dogSpeed+2, false, this.cat.hitbox);

            gamelle = new Rectangle(dimension2D.getWidth()-350,dimension2D.getHeight()-250, 100, 100);

            this.dogs.add(dog);
            this.dogs.add(dog2);





        }else if (this.level == 4){
            this.cat.hitbox.setX(200);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(800, 600, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(1000, 450, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-600, 100, 100);

            Interrupteur interrupteur = new Interrupteur(new Rectangle(600,600,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur.createDoorAroundAnObject(gamelle);

            this.dogs.add(dog);
            this.dogs.add(dog2);

            this.interrupteurs.add(interrupteur);


        }else if (this.level == 5){
            this.cat.hitbox.setX(200);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(200, 600, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(1100, 460, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-650, 100, 100);

            Interrupteur interrupteur = new Interrupteur(new Rectangle(400,600,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur.getPortes().add(new Rectangle(dimension2D.getWidth()/2,0, 50,dimension2D.getHeight()));

            this.dogs.add(dog);
            this.dogs.add(dog2);

            this.interrupteurs.add(interrupteur);


        }else if (this.level == 6){

            this.cat.hitbox.setX(200);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(200, 600, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(1100, 460, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-400, 100, 100);

            Interrupteur interrupteur = new Interrupteur(new Rectangle(400,600,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur.getPortes().add(new Rectangle(dimension2D.getWidth()/2,0, 50,dimension2D.getHeight()));

            Interrupteur interrupteur2 = new Interrupteur(new Rectangle(dimension2D.getWidth()-400,dimension2D.getHeight()-900,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur2.createDoorAroundAnObject(gamelle);


            this.dogs.add(dog);
            this.dogs.add(dog2);

            this.interrupteurs.add(interrupteur);
            this.interrupteurs.add(interrupteur2);


        }else if (this.level == 7){
            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(1100, 300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(500, dimension2D.getHeight()-300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);

            Interrupteur interrupteur = new Interrupteur(new Rectangle(dimension2D.getWidth()-300,200,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur.getPortes().add(new Rectangle(0,dimension2D.getHeight()/2, dimension2D.getWidth(),50));

            this.dogs.add(dog);
            this.dogs.add(dog2);

            this.interrupteurs.add(interrupteur);


        }else if(this.level == 8){
            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(1100, 300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(500, dimension2D.getHeight()-300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);

            Interrupteur interrupteur = new Interrupteur(new Rectangle(dimension2D.getWidth()-300,200,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur.getPortes().add(new Rectangle(0,dimension2D.getHeight()/2, dimension2D.getWidth(),50));

            Interrupteur interrupteur2 = new Interrupteur(new Rectangle(300,dimension2D.getHeight()-300,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur2.createDoorAroundAnObject(gamelle);

            this.dogs.add(dog);
            this.dogs.add(dog2);

            this.interrupteurs.add(interrupteur);
            this.interrupteurs.add(interrupteur2);
        }







        this.gamelle.setFill(Color.GREEN);
        gameContext.getChildren().add(this.gamelle);
        gameContext.getChildren().add(this.cat.hitbox);
        this.obstacles.add(this.cat.hitbox);
        this.obstacles.add(this.gamelle);
        this.cat.hitbox.toFront();
        this.gamelle.toFront();


        for (Interrupteur interrupteur : this.interrupteurs) {
            this.obstacles.add(interrupteur.getInterrupteur());
            for (int k = 0; k < interrupteur.getPortes().size(); k++){
                interrupteur.getPortes().get(k).setFill(Color.BLACK);
                obstacles.add(interrupteur.getPortes().get(k));
                gameContext.getChildren().add(interrupteur.getPortes().get(k));
                interrupteur.getPortes().get(k).toFront();
            }
        }

        for (Cat dog : this.dogs) {
            this.obstacles.add(dog.hitbox);
            dog.hitbox.toFront();
            gameContext.getChildren().add(dog.hitbox);
        }



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

        Rectangle futurePos = new Rectangle(nextX,nextY, object.getWidth(), object.getHeight());



        for (Rectangle obstacle : obstacles) {
            if (!obstacle.equals(object)){
                if (isCollidingWithASpecificObstacle(futurePos,obstacle)) {

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
    protected boolean isCollidingWithASpecificObstacle(Rectangle object1, Rectangle object2){

        return object1.getX() < object2.getX() + object2.getWidth() && object1.getX() + object1.getWidth() > object2.getX()
            && object1.getY() < object2.getY() + object2.getHeight() && object1.getY() + object1.getHeight() > object2.getY();

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
