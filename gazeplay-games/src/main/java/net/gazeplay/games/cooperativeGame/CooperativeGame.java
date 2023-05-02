/**
 * The CooperativeGame class represents a cooperative game in which the player controls a cat to complete levels.
 * The objective of the game is for the cat to reach its food dish without being caught by the pursuing dogs. If the cat is caught by a dog, the level restarts.
 * The game is implemented as a JavaFX application and extends the Parent class.
 */
package net.gazeplay.games.cooperativeGame;

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

public class CooperativeGame extends Parent implements GameLifeCycle {

    /**
     * The game context for the current game instance.
     */
    private final IGameContext gameContext;

    /**
     * The game statistics for the current game instance.
     */
    private final Stats stats;

    /**
     * A flag indicating whether the current level has ended.
     */
    protected boolean endOfLevel;

    /**
     * The cat controlled by the player.
     */
    protected Cat cat;

    /**
     * The current level of the game.
     */
    private int level;

    /**
     * The cat's food dish in the game.
     */
    private Rectangle gamelle;

    /**
     * The obstacles in the game that the cat and other moving object must navigate around.
     */
    private ArrayList<Rectangle> obstacles;

    /**
     * The switches in the game that the cat can activate to open doors or move walls.
     */
    private ArrayList<Interrupteur> interrupteurs;

    /**
     * The walls in the game that the cat must navigate around.
     */
    private ArrayList<Rectangle> walls;

    /**
     * The moving walls in the game that the cat must navigate around.
     */
    private ArrayList<MovingWall> wallsMoving;

    /**
     * The dogs in the game that pursue the cat.
     */
    protected ArrayList<Cat> dogs;

    /**
     * Constructs a new CooperativeGame instance with the specified game context, statistics, and level.
     * @param gameContext the game context for the new game instance
     * @param stats the game statistics for the new game instance
     * @param level the level for the new game instance
     */
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





    /**
     * Launches a new level of the game by resetting the obstacles, dogs, and walls,
     * setting the end of the level to false, adding the stats to the gaze device manager,
     * creating a new background rectangle, initializing the game box,
     * notifying that a new round is ready, and starting the game.
     */
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
        int widthCat = 125;
        int heightCat = 125;
        int widthDog = 125;
        int heightDog = 125;
        int widthInterrupteur = 130;
        int heightInterrupteur = 100;
        int widthGamelle = 120;
        int heightGamelle = 80;

        this.cat = new Cat(0, 0, widthCat,heightCat,gameContext,stats,this, 10, true,null);
        this.gamelle = new Rectangle(1000,1000,widthGamelle,heightGamelle);

        if (this.level == 1){
            this.cat.hitbox.setX(100);
            this.cat.hitbox.setY(100);

            Cat dog = new Cat(900, dimension2D.getHeight()-500, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);

            this.dogs.add(dog);

        }else if (this.level == 2){
            this.cat.hitbox.setX(150);
            this.cat.hitbox.setY(150);
            Cat dog = new Cat(800, 600, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(1000, 450, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);

            this.gamelle.setX(dimension2D.getWidth()-350);
            this.gamelle.setY(dimension2D.getHeight()-250);

            this.dogs.add(dog);
            this.dogs.add(dog2);

        }else if (this.level == 3){
            this.cat.hitbox.setX(150);
            this.cat.hitbox.setY(150);
            Cat dog = new Cat(800, 600, widthDog, heightDog, gameContext, stats, this, dogSpeed+2, false, this.cat.hitbox);
            Cat dog2 = new Cat(1000, 450, widthDog, heightDog, gameContext, stats, this, dogSpeed+2, false, this.cat.hitbox);

            this.gamelle.setX(dimension2D.getWidth()-350);
            this.gamelle.setY(dimension2D.getHeight()-250);

            this.dogs.add(dog);
            this.dogs.add(dog2);



        }else if (this.level == 4){
            this.cat.hitbox.setX(200);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(800, 600, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            Cat dog2 = new Cat(1000, 450, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);

            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-600);

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

            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-650);

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

            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-400);

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
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);
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
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);
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
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);
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
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);
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
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);
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
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);
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
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);
            MovingWall wallMoving = new MovingWall(dimension2D.getWidth()/2,50,50,200,this,gameContext,true,5);
            wallMoving.animationVertical(wallMoving.getY(),dimension2D.getHeight()-50);

            this.wallsMoving.add(wallMoving);
            this.dogs.add(dog);
        }else if (level == 14){
            this.cat.hitbox.setX(250);
            this.cat.hitbox.setY(200);
            Cat dog = new Cat(dimension2D.getWidth()-300, dimension2D.getHeight()-500, widthDog, heightDog, gameContext, stats, this, dogSpeed, false, this.cat.hitbox);
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);
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
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);
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
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);
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
            this.gamelle.setX(dimension2D.getWidth()-300);
            this.gamelle.setY(dimension2D.getHeight()-200);
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
        }else{
            this.level = 1;
            dispose();
            launch();
            return;
        }

        this.cat.initPos();
        this.obstacles.add(this.cat.hitbox);
        this.cat.hitbox.toFront();

        this.gamelle.setFill(new ImagePattern(new Image("data/cooperativeGame/gamelle.png")));
        gameContext.getChildren().add(this.cat.hitbox);
        gameContext.getChildren().add(this.gamelle);
        this.obstacles.add(this.gamelle);
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
                // If the object collides with an obstacle, check the type of the obstacle
                if (obstacle instanceof MovingWall movingWall){
                    // If the obstacle is a moving wall and it has to reset the pos of the cat, check for collision with the object and dogs
                    if (movingWall.resetPos && isCollidingWithASpecificObstacle(movingWall,futurePos)){
                        if (object.equals(this.cat.hitbox)){
                            // If the object is the cat and if it collides with the moving wall, game over and return true
                            endOfGame(false);
                            return true;
                        }else{
                            // If the object is a dog and if it collides with the moving wall, reset its position and return true
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
                    // If the object is the cat and if it collides with the gamelle, the cat win and return true
                    if (!endOfLevel) {
                        endOfGame(true);
                        return true;
                    }
                } else {
                    // If the object is a dog and it collides with the cat, end the game and return true
                    for (Cat dog : this.dogs) {
                        if (dog.hitbox.equals(object) && this.cat.hitbox.equals(obstacle)) {
                            if (!endOfLevel) {
                                endOfGame(false);
                                return true;
                            }
                        }
                    }
                }

                // We add this for loop to avoid that when we don't move, the moving wall ignores the collisions
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
     Method to handle the end of the game.
     @param win true if the game is won, false otherwise
     */
    protected void endOfGame(boolean win){
        endOfLevel = true;
        if(win){
            // Increment the number of goals reached and update the score
            stats.incrementNumberOfGoalsReached();
            gameContext.updateScore(stats, this);
            // Play a win transition and launch the next level
            gameContext.playWinTransition(500, actionEvent -> {
                this.level++;
                dispose();
                launch();
            });
        }else{
            // Launch the same level again in case of a loss
            dispose();
            launch();
        }
    }




}
