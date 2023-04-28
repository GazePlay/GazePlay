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
    protected Cat cat;
    private int level;
    private Rectangle gamelle;


    private ArrayList<Rectangle> obstacles;
    private ArrayList<Interrupteur> interrupteurs;

    private ArrayList<Rectangle> walls;
    private ArrayList<MovingWall> wallsMoving;

    protected ArrayList<Cat> dogs;

    public CooperativeGame(final IGameContext gameContext, Stats stats, int level){
        this.gameContext = gameContext;
        this.stats = stats;
        this.level = level;
        this.obstacles = new ArrayList<>();
        this.dogs = new ArrayList<>();
        this.interrupteurs = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.wallsMoving = new ArrayList<>();
    }





    @Override
    public void launch() {
        this.endOfLevel = false;
        this.obstacles.clear();
        this.dogs.clear();
        this.interrupteurs.clear();
        this.walls.clear();
        this.wallsMoving.clear();
        gameContext.setLimiterAvailable();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Rectangle background = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());
        background.setFill(Color.WHITE);
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.getChildren().add(background);
        setLevel(level);
        initGameBox();
        stats.notifyNewRoundReady();
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
        int widthInterrupteur = 150;
        int heightInterrupteur = 125;


        this.cat = new Cat(0, 0, widthCat,heightCat,gameContext,stats,this, 10, true);

        if (this.level == 1){
            this.cat.hitbox.setX(100);
            this.cat.hitbox.setY(100);

            Cat dog = new Cat(900, dimension2D.getHeight()-500, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);

            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);
            this.dogs.add(dog);

        }else if (this.level == 2){
            this.cat.hitbox.setX(150);
            this.cat.hitbox.setY(150);
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
        }else if(this.level == 9){
            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(1100, 300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(500, dimension2D.getHeight()-300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);

            Rectangle mur = new Rectangle(dimension2D.getWidth()/2,0,50,dimension2D.getHeight()/2-100);
            Rectangle mur2 = new Rectangle(dimension2D.getWidth()/2,dimension2D.getHeight()/2+100,50,dimension2D.getHeight()/2+100);


            this.dogs.add(dog);
            this.dogs.add(dog2);
            this.walls.add(mur);
            this.walls.add(mur2);

        }else if(this.level == 10){
            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(1100, 300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(500, dimension2D.getHeight()-300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);

            Rectangle mur = new Rectangle(0,dimension2D.getHeight()/2,dimension2D.getWidth()/2-100,50);
            Rectangle mur2 = new Rectangle(dimension2D.getWidth()/2+100,dimension2D.getHeight()/2,dimension2D.getWidth()/2+100,50);


            this.dogs.add(dog);
            this.dogs.add(dog2);
            this.walls.add(mur);
            this.walls.add(mur2);
        }else if(this.level == 11){
            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(1100, 300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(500, dimension2D.getHeight()-300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);

            Rectangle mur = new Rectangle(dimension2D.getWidth()/2,0,50,dimension2D.getHeight()/2-100);
            Rectangle mur2 = new Rectangle(dimension2D.getWidth()/2,dimension2D.getHeight()/2+100,50,dimension2D.getHeight()/2+100);


            Interrupteur interrupteur = new Interrupteur(new Rectangle(200,dimension2D.getHeight()-300,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur.getPortes().add(new Rectangle(dimension2D.getWidth()/2,dimension2D.getHeight()/2-100, 50,200));

            Interrupteur interrupteur2 = new Interrupteur(new Rectangle(dimension2D.getWidth()-300,200,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur2.createDoorAroundAnObject(gamelle);

            this.dogs.add(dog);
            this.dogs.add(dog2);
            this.walls.add(mur);
            this.walls.add(mur2);
            this.interrupteurs.add(interrupteur);
            this.interrupteurs.add(interrupteur2);

        }else if(this.level == 12){
            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(1100, 300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(500, dimension2D.getHeight()-300, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);

            Rectangle mur = new Rectangle(0,dimension2D.getHeight()/2,dimension2D.getWidth()/2-100,50);
            Rectangle mur2 = new Rectangle(dimension2D.getWidth()/2+100,dimension2D.getHeight()/2,dimension2D.getWidth()/2+100,50);

            Interrupteur interrupteur = new Interrupteur(new Rectangle(dimension2D.getWidth()-300,200,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur.getPortes().add(new Rectangle(dimension2D.getWidth()/2-100,dimension2D.getHeight()/2, 200,50));

            Interrupteur interrupteur2 = new Interrupteur(new Rectangle(200,dimension2D.getHeight()-300,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur2.createDoorAroundAnObject(gamelle);


            this.dogs.add(dog);
            this.dogs.add(dog2);
            this.walls.add(mur);
            this.walls.add(mur2);
            this.interrupteurs.add(interrupteur);
            this.interrupteurs.add(interrupteur2);
        }else if (level == 13){
            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(dimension2D.getWidth()-300, dimension2D.getHeight()-500, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);

            MovingWall wallMoving = new MovingWall(dimension2D.getWidth()/2,50,50,200,this,gameContext,true,5);
            wallMoving.animationVertical(wallMoving.getY(),dimension2D.getHeight()-50);

            this.wallsMoving.add(wallMoving);
            this.dogs.add(dog);
        }else if (level == 14){
            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(dimension2D.getWidth()-300, dimension2D.getHeight()-500, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);

            MovingWall wallMoving = new MovingWall(dimension2D.getWidth()/2,50,50,200,this,gameContext,true,5);
            wallMoving.animationVertical(wallMoving.getY(),dimension2D.getHeight()/2);
            MovingWall wallMoving2 = new MovingWall(dimension2D.getWidth()/2,dimension2D.getHeight()-250,50,200,this,gameContext,true,5);
            wallMoving2.animationVertical(wallMoving2.getY(),dimension2D.getHeight()/2);

            this.wallsMoving.add(wallMoving);
            this.wallsMoving.add(wallMoving2);
            this.dogs.add(dog);
        }else if (level == 15){

            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(dimension2D.getWidth()-300, dimension2D.getHeight()-350, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);
            MovingWall wallMoving = new MovingWall(dimension2D.getWidth()/2,50,50,200,this,gameContext,true,5);
            wallMoving.animationVertical(wallMoving.getY(),dimension2D.getHeight()/2);
            MovingWall wallMoving2 = new MovingWall(dimension2D.getWidth()/2,dimension2D.getHeight()-250,50,200,this,gameContext,true,5);
            wallMoving2.animationVertical(wallMoving2.getY(),dimension2D.getHeight()/2);
            MovingWall wallMoving3 = new MovingWall(50,dimension2D.getHeight()/2,200,50,this,gameContext,true,5);
            wallMoving3.animationHorizontal(wallMoving3.getX(),dimension2D.getWidth()/2);
            MovingWall wallMoving4 = new MovingWall(dimension2D.getWidth()-250, dimension2D.getHeight()/2,200,50,this,gameContext,true,5);
            wallMoving4.animationHorizontal(wallMoving4.getX(),dimension2D.getWidth()/2+50);

            this.wallsMoving.add(wallMoving);
            this.wallsMoving.add(wallMoving2);
            this.wallsMoving.add(wallMoving3);
            this.wallsMoving.add(wallMoving4);
            this.dogs.add(dog);
        }else if (level == 16){
            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(dimension2D.getWidth()-300, dimension2D.getHeight()-450, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(250, dimension2D.getHeight()-250, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);

            Interrupteur interrupteur = new Interrupteur(new Rectangle(dimension2D.getWidth()-350,200,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur.createDoorAroundAnObject(gamelle);


            MovingWall wallMoving = new MovingWall(dimension2D.getWidth()/2,50,50,200,this,gameContext,true,5);
            wallMoving.animationVertical(wallMoving.getY(),dimension2D.getHeight()/2);
            MovingWall wallMoving2 = new MovingWall(dimension2D.getWidth()/2,dimension2D.getHeight()-250,50,200,this,gameContext,true,5);
            wallMoving2.animationVertical(wallMoving2.getY(),dimension2D.getHeight()/2);
            MovingWall wallMoving3 = new MovingWall(50,dimension2D.getHeight()/2,200,50,this,gameContext,true,5);
            wallMoving3.animationHorizontal(wallMoving3.getX(),dimension2D.getWidth()/2);
            MovingWall wallMoving4 = new MovingWall(dimension2D.getWidth()-250, dimension2D.getHeight()/2,200,50,this,gameContext,true,5);
            wallMoving4.animationHorizontal(wallMoving4.getX(),dimension2D.getWidth()/2+50);

            this.interrupteurs.add(interrupteur);
            this.wallsMoving.add(wallMoving);
            this.wallsMoving.add(wallMoving2);
            this.wallsMoving.add(wallMoving3);
            this.wallsMoving.add(wallMoving4);
            this.dogs.add(dog);
            this.dogs.add(dog2);
        }else if (level == 17){
            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);

            Cat dog = new Cat(dimension2D.getWidth()-600, 250, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(dimension2D.getWidth()/2, dimension2D.getHeight()-250, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            gamelle = new Rectangle(dimension2D.getWidth()-300,dimension2D.getHeight()-200, 100, 100);

            Interrupteur interrupteur = new Interrupteur(new Rectangle(dimension2D.getWidth()-350,200,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur.createDoorAroundAnObject(gamelle);
            Interrupteur interrupteur2 = new Interrupteur(new Rectangle(250,dimension2D.getHeight()-200,widthInterrupteur,heightInterrupteur),gameContext,this);
            interrupteur2.createDoorAroundAnObject(gamelle);


            MovingWall wallMoving = new MovingWall(50,dimension2D.getHeight()/2,400,50,this,gameContext,true,10);
            wallMoving.animationHorizontal(wallMoving.getX(),dimension2D.getWidth()-50);

            this.interrupteurs.add(interrupteur);
            this.interrupteurs.add(interrupteur2);
            this.wallsMoving.add(wallMoving);

            this.dogs.add(dog);
            this.dogs.add(dog2);
        }



        this.cat.initPos();
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
                interrupteur.getPortes().get(k).setFill(Color.BROWN);
                obstacles.add(interrupteur.getPortes().get(k));
                gameContext.getChildren().add(interrupteur.getPortes().get(k));
            }
        }

        for (Cat dog : this.dogs) {
            this.obstacles.add(dog.hitbox);
            dog.hitbox.toFront();
            gameContext.getChildren().add(dog.hitbox);
        }

        for (Rectangle mur : this.walls){
            mur.setFill(Color.BLACK);
            this.obstacles.add(mur);
            mur.toFront();
            gameContext.getChildren().add(mur);
        }

        for(Rectangle wallMoving: this.wallsMoving){
            wallMoving.setFill(Color.RED);
            this.obstacles.add(wallMoving);
            wallMoving.toFront();
            gameContext.getChildren().add(wallMoving);
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
        double nextX = object.getX();
        double nextY = object.getY();
        switch (direction) {
            case "left" -> nextX -= speed;
            case "right" -> nextX += speed;
            case "up" -> nextY -= speed;
            case "down" -> nextY += speed;
        }
        Rectangle futurePos = new Rectangle(nextX, nextY, object.getWidth(), object.getHeight());
        for (Rectangle obstacle : obstacles) {
            if (obstacle.equals(object)) {
                continue;
            }
            if (isCollidingWithASpecificObstacle(futurePos, obstacle)) {

                if (obstacle instanceof MovingWall movingWall){
                    if (movingWall.resetPos && isCollidingWithASpecificObstacle(movingWall,futurePos)){
                        if (object.equals(this.cat.hitbox)){
                            endOfGame(false);
                            return true;
                        }else{
                            for (Cat dog : this.dogs) {
                                if (dog.hitbox.equals(object)){
                                    if (isCollidingWithASpecificObstacle(movingWall, futurePos)) {
                                        dog.hitbox.setX(dog.initPosX);
                                        dog.hitbox.setY(dog.initPosY);
                                        return true;

                                    }
                                }
                            }
                        }
                    }
                }
                if (this.cat.hitbox.equals(object) && gamelle.equals(obstacle)) {
                    if (!endOfLevel) {
                        endOfGame(true);
                        return true;
                    }
                } else if (this.cat.hitbox.equals(object)) {
                    for (MovingWall wallMoving : wallsMoving) {
                        if (wallMoving.equals(obstacle) && wallMoving.resetPos && isCollidingWithASpecificObstacle(object, wallMoving)) {
                            endOfGame(false);
                            return true;
                        }
                    }
                } else {
                    for (Cat dog : this.dogs) {
                        if (dog.hitbox.equals(object) && this.cat.hitbox.equals(obstacle)) {
                            if (!endOfLevel) {
                                endOfGame(false);
                                return true;
                            }
                        }
                    }
                }
                for (MovingWall wallMoving : wallsMoving) {
                    if (object.equals(wallMoving) && wallMoving.resetPos) {
                        if (isCollidingWithASpecificObstacle(wallMoving, this.cat.hitbox)) {
                            endOfGame(false);
                            return true;
                        } else {
                            for (Cat dog : this.dogs) {
                                if (isCollidingWithASpecificObstacle(wallMoving, dog.hitbox)) {
                                    dog.hitbox.setX(dog.initPosX);
                                    dog.hitbox.setY(dog.initPosY);
                                    return true;
                                }
                            }
                        }
                    }
                }
                return true;
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
