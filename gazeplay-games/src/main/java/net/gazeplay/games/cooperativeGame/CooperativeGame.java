/**
 * The CooperativeGame class represents a cooperative game in which the player controls a cat to complete levels.
 * The objective of the game is for the cat to reach its food dish without being caught by the pursuing dogs. If the cat is caught by a dog, the level restarts.
 * The game is implemented as a JavaFX application and extends the Parent class.
 */
package net.gazeplay.games.cooperativeGame;

import javafx.animation.Animation;
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
    protected ArrayList<Interrupteur> interrupteurs;

    /**
     * The walls in the game that the cat must navigate around.
     */
    private ArrayList<Rectangle> walls;

    /**
     * The moving walls in the game that the cat must navigate around.
     */
    protected ArrayList<MovingWall> wallsMoving;

    /**
     * The dogs in the game that pursue the cat.
     */
    protected ArrayList<Cat> dogs;

    /**
     * Specifies if the cat is playable with keyboard or not
     */
    protected boolean catNotKeyboard;

    /**
     * Specifies if a key is pressed
     */
    private boolean isKeyPressed;

    /**
     * Specifies if the game timer has ended or not
     */
    protected boolean gameTimerEnded;

    /**
     * Constructs a new CooperativeGame instance with the specified game context, statistics, and level.
     * @param gameContext the game context for the new game instance
     * @param stats the game statistics for the new game instance
     * @param level the level for the new game instance
     */
    public CooperativeGame(final IGameContext gameContext, Stats stats, int level, boolean catNotKeyboard){
        this.gameContext = gameContext;
        this.stats = stats;
        this.level = level;
        this.obstacles = new ArrayList<>();
        this.dogs = new ArrayList<>();
        this.interrupteurs = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.wallsMoving = new ArrayList<>();
        this.catNotKeyboard = catNotKeyboard;
        this.gameTimerEnded = false;
    }





    /**
     * Launches a new level of the game by resetting the obstacles, switches, dogs, and walls,
     * setting the end of the level to false, adding the stats to the gaze device manager,
     * creating a new background rectangle, initializing the game box,
     * notifying that a new round is ready, and starting the game.
     */
    @Override
    public void launch() {
        //Clear all arrayLists
        this.obstacles.clear();
        this.dogs.clear();
        this.interrupteurs.clear();
        this.walls.clear();
        this.wallsMoving.clear();

        this.endOfLevel = false;

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        gameContext.setLimiterAvailable();
        Rectangle background = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());
        background.setFill(Color.WHITE);
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.getChildren().add(background);
        setLevel(level);
        initGameBox();
        stats.notifyNewRoundReady();
        gameContext.firstStart();
        this.gameTimerEnded = false;


        if (catNotKeyboard){
            gameContext.getPrimaryScene().setOnKeyPressed(keyEvent -> {
                if (gameTimerEnded){
                    if (!endOfLevel){
                        if (!isKeyPressed){

                            // Perform different actions depending on the pressed key
                            switch (keyEvent.getCode()) {
                                // If the A key is pressed, stop the nearest dog
                                case A -> {
                                    isKeyPressed = true;
                                    if (!this.dogs.isEmpty()) {
                                        Cat nearestDog = getNearestDogFromCat(this.dogs);
                                        if (nearestDog != null) {
                                            nearestDog.canMove = false;
                                        }
                                    }
                                }
                                // If the Z key is pressed, activate the nearest interrupteur
                                case Z -> {
                                    isKeyPressed = true;
                                    if (!this.interrupteurs.isEmpty()){
                                        Interrupteur nearestInterrupteur = getNearestInterrupteurFromCat(this.interrupteurs);
                                        if (nearestInterrupteur != null){
                                            nearestInterrupteur.initTimerInterrupteur();
                                        }
                                    }
                                }
                                // If the E key is pressed, stop the nearest movingWall
                                case E -> {
                                    isKeyPressed = true;
                                    if (!this.wallsMoving.isEmpty()){
                                        MovingWall nearestMovingWall = getNearestWallMovingFromCat(this.wallsMoving);
                                        if (nearestMovingWall != null){
                                            nearestMovingWall.canMove = false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });

            gameContext.getPrimaryScene().setOnKeyReleased(keyEvent -> {
                if (gameTimerEnded){
                    if (!endOfLevel){
                        switch (keyEvent.getCode()) {
                            // If the A key is released, let the dogs move again
                            case A -> {
                                isKeyPressed = false;
                                if (!this.dogs.isEmpty()) {
                                    for (Cat dog : this.dogs) {
                                        dog.canMove = true;
                                    }
                                }
                            }
                            // If the Z key is released, stop the timers of all switches
                            case Z -> {
                                isKeyPressed = false;
                                if (!this.interrupteurs.isEmpty()){
                                    for (Interrupteur interrupteur : this.interrupteurs) {

                                        if (interrupteur.timelineProgressBar != null && interrupteur.timelineProgressBar.getStatus() == Animation.Status.RUNNING ){
                                            interrupteur.stopTimerInterrupteur();
                                        }

                                    }
                                }

                            }
                            // If the E key is released, let the movingWall move again
                            case E -> {
                                isKeyPressed = false;
                                if (!this.wallsMoving.isEmpty()){
                                    for (MovingWall wallMoving : this.wallsMoving) {
                                        wallMoving.canMove = true;
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }


    private void setLevel(final int i){

        this.level = i;
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double dogSpeed = 3.5;

        int widthCat = 125;
        int heightCat = 125;
        int widthDog = 125;
        int heightDog = 125;
        int widthInterrupteur = 130;
        int heightInterrupteur = 100;
        int widthGamelle = 120;
        int heightGamelle = 80;

        if (!catNotKeyboard){
            this.cat = new Cat(0, 0, widthCat,heightCat,gameContext,stats,this, 10, true,null);
        }else{
            this.cat = new Cat(0,0,widthCat,heightCat,gameContext,stats,this,8,true, new Rectangle(0,0,1,1));
        }

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


        initGameTimer(3);


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
                    // If the obstacle is a moving wall, and it has to reset the pos of the cat, check for collision with the object and dogs
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
                    // If the object is a dog, and it collides with the cat, end the game and return true
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




    /**
     * Returns the nearest Cat object to the current Cat object, from a given list of Cat objects.
     *
     * @param nbDog the list of Cat objects to search from (they represent the dogs)
     * @return the nearest Cat object, or null if the list is empty
     */
    private Cat getNearestDogFromCat(ArrayList<Cat> nbDog) {
        Cat nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Cat rect : nbDog) {
            // Calculate the distance between the current Cat object and the current Cat in the list (dog)
            double distance = distance(this.cat.hitbox, rect.hitbox);

            // If the distance is less than the current minimum distance, update the nearest Cat object and minimum distance
            if (distance < minDistance) {
                minDistance = distance;
                nearest = rect;
            }
        }

        // Return the nearest Cat object
        return nearest;
    }

    /**
     * Returns the nearest Interrupteur object to the current Cat object, from a given list of Interrupteur objects.
     *
     * @param nbInterrupteurs the list of Interrupteur objects to search from
     * @return the nearest Interrupteur object, or null if the list is empty
     */
    private Interrupteur getNearestInterrupteurFromCat(ArrayList<Interrupteur> nbInterrupteurs) {
        Interrupteur nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Interrupteur rect : nbInterrupteurs) {
            // Calculate the distance between the current Interrupteur object and the current Interrupteur in the list
            double distance = distance(this.cat.hitbox, rect.getInterrupteur());

            // If the distance is less than the current minimum distance, update the nearest Interrupteur object and minimum distance
            if (distance < minDistance) {
                minDistance = distance;
                nearest = rect;
            }
        }
        // Return the nearest Interrupteur object
        return nearest;
    }


    /**
     * Returns the nearest MovingWall object to the current Cat object, from a given list of MovingWall objects.
     *
     * @param nbMovingWalls the list of MovingWall objects to search from
     * @return the nearest MovingWall object, or null if the list is empty
     */
    private MovingWall getNearestWallMovingFromCat(ArrayList<MovingWall> nbMovingWalls) {
        MovingWall nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (MovingWall rect : nbMovingWalls) {
            // Calculate the distance between the current Cat object and the current MovingWall in the list
            double distance = distance(this.cat.hitbox, rect);

            // If the distance is less than the current minimum distance, update the nearest MovingWall object and minimum distance
            if (distance < minDistance) {
                minDistance = distance;
                nearest = rect;
            }
        }

        // Return the nearest MovingWall object
        return nearest;
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


    /**
     * Initializes an animation timer that counts the number of seconds specified by the 'sec' parameter.
     *
     * @param sec the number of seconds the timer should count
     */
    private void initGameTimer(int sec) {
        AnimationTimer gameTimer = new AnimationTimer() {
            int nbframe = 0;
            int nbSec = 0;

            @Override
            public void handle(long now) {
                // Increase the number of frames
                if (nbframe <= 60) {
                    nbframe++;
                } else { // If one second has passed, increase the number of seconds
                    nbSec++;
                    // If the number of seconds equals 'sec', set the 'gameTimerEnded' flag to true
                    if (nbSec == sec) {
                        gameTimerEnded = true;
                    }
                    // Reset the number of frames
                    nbframe = 0;
                }
            }
        };
        // Start the animation timer
        gameTimer.start();
    }

}
