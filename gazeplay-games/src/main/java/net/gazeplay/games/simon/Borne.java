package net.gazeplay.games.simon;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;

import java.util.ArrayList;

public class Borne extends Parent {
    private final IGameContext gameContext;
    private final Simon gameInstance;
    private final ArrayList<Bouton> boutons;
    private final double radius;
    private Timeline computerPlayAnimation;
    private AnimationTimer timeBeforeResetComputerAnimation;
    private boolean computerPlay;
    private boolean nextNote;
    protected int secondsReset;
    private int nbSecond;
    private boolean resetComputerAnimation;
    protected boolean multiplayer;
    protected boolean simonCopy;
    private int i = 0;
    public Borne(IGameContext gameContext, Simon gameInstance){
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.boutons = new ArrayList<>();


        this.computerPlay = true;

        this.radius = 400;
        this.resetComputerAnimation = false;
        this.nbSecond = 0;
        this.multiplayer = false;
        this.simonCopy = false;

        initBorne();

        timeBeforeResetComputerAnimation = new AnimationTimer() {
            int nbframes = 0;
            @Override
            public void handle(long now) {
                if (nbframes == 60){
                    nbframes = 0;
                    nbSecond++;
                    System.out.println(secondsReset);
                    System.out.println(nbSecond);
                    if (nbSecond == secondsReset){
                        resetComputerAnimation = true;
                    }
                }
                nbframes++;
            }
        };
        timeBeforeResetComputerAnimation.start();

        nextNote = true;
        computerPlayAnimation = new Timeline(new KeyFrame(Duration.millis(16), event -> {


            if (computerPlay){
                disableAllBoutons();
                while(i < gameInstance.computerSequence.size() && nextNote){
                    String note = gameInstance.computerSequence.get(i);
                    for (Bouton bouton : boutons){
                        if (note.compareToIgnoreCase(bouton.note) == 0){
                            playSongAnimation(bouton);
                            i++;
                            break;
                        }
                    }
                }
                nbSecond = 0;
            }
            if(i == gameInstance.computerSequence.size()){
                computerPlay = false;
                if (nextNote){
                    activateAllBoutons();
                }
            }

            if (gameInstance.playerSequence.size() == gameInstance.computerSequence.size() || resetComputerAnimation){
                resetComputerAnimationClassic();
            }
            if (gameInstance.computerSequence.size() == 33){
                gameInstance.endGame();
            }
        }));

        computerPlayAnimation.setCycleCount(Animation.INDEFINITE);
        computerPlayAnimation.play();
    }

    private void resetComputerAnimationClassic(){
        boolean increment = gameInstance.playerSequence.equals(gameInstance.computerSequence);
        System.out.println(gameInstance.playerSequence.size() == gameInstance.computerSequence.size());
        System.out.println(resetComputerAnimation);
        if (!resetComputerAnimation){
            gameInstance.playerSequence.clear();
        }
        if (increment){
            gameInstance.addNoteToComputerSequence();
            System.out.println(gameInstance.computerSequence.size());
        }
        i = 0;
        computerPlay = true;
        resetComputerAnimation = false;
    }

    private void playSongAnimation(Bouton bouton){
        System.out.println("playsong for : " + bouton.note);
        nextNote = false;
        bouton.isActivated = false;
        Timeline animation = new Timeline(new KeyFrame(new Duration(2000)));
        bouton.setOpacity(0.6);
        animation.setOnFinished(actionEvent -> {
            Timeline delayForNextNote = new Timeline(new KeyFrame(new Duration(1000)));
            bouton.isActivated = true;
            bouton.setOpacity(1);
            delayForNextNote.setOnFinished(event ->{
                nextNote = true;
            });

            delayForNextNote.play();

        });

        animation.play();
    }
    private void disableAllBoutons(){
        for(Bouton bouton : boutons){
            bouton.setDisable(true);
        }
    }

    private void activateAllBoutons(){
        for (Bouton bouton : boutons){
            bouton.setDisable(false);
        }
    }


    private void initBorne(){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        //Circle that contains buttons
        Circle circle = new Circle(dimension2D.getWidth()/2, dimension2D.getHeight()/2,radius,Color.TRANSPARENT);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(15);

        initBoutons();

        Rectangle rectangleCenter = new Rectangle(dimension2D.getWidth()/2-150,(dimension2D.getHeight()/2)-150,300,300);
        rectangleCenter.setArcWidth(200);
        rectangleCenter.setArcHeight(200);

        Rectangle lineVertical = new Rectangle(dimension2D.getWidth()/2-50,dimension2D.getHeight()/2-400,100,800);
        Rectangle lineHorizontal = new Rectangle(dimension2D.getWidth()/2-400,dimension2D.getHeight()/2-50,800,100);

        gameContext.getChildren().add(circle);

        for (Bouton bouton : boutons){
            this.gameContext.getChildren().add(bouton);
        }
        gameContext.getChildren().addAll(rectangleCenter,lineVertical,lineHorizontal);

    }

    private void initBoutons(){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Bouton topRight = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius,radius,0,90,gameContext,gameInstance, "rouge");
        topRight.setFill(Color.RED);

        Bouton topLeft = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius,radius,90,90,gameContext,gameInstance, "vert");
        topLeft.setFill(Color.web("2FDF3C"));

        Bouton bottomLeft = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius,radius,180,90,gameContext,gameInstance,"jaune");
        bottomLeft.setFill(Color.YELLOW);

        Bouton bottomRight = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius,radius,270,90,gameContext,gameInstance,"bleu");
        bottomRight.setFill(Color.web("2FB4DF"));

        this.boutons.add(topRight);
        this.boutons.add(topLeft);
        this.boutons.add(bottomRight);
        this.boutons.add(bottomLeft);
    }



}
