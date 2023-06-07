package net.gazeplay.games.simon;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
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
    private int nbNotePlayer = 1;
    protected int nbNoteMax;
    private final ImageView right, wrong;
    private int i = 0;
    private boolean lastNotePlayer;
    private Label labelPlayers;
    private boolean player1,player2,turn;

    public Borne(IGameContext gameContext, Simon gameInstance){
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.boutons = new ArrayList<>();
        this.nbNoteMax = 0;
        this.radius = 400;
        this.resetComputerAnimation = false;
        this.nbSecond = 0;
        this.multiplayer = false;
        this.right = new ImageView(new Image("data/simonGame/right.png"));
        this.wrong = new ImageView(new Image("data/simonGame/wrong.png"));

        initBorne();


    }

    protected void generateBorne(){



        if (this.simonCopy){
            this.computerPlay = false;
        }else{
            this.computerPlay = true;
        }

        if (multiplayer){
            labelPlayers = new Label();
            labelPlayers.setFont(new Font("Arial",50));
            labelPlayers.toFront();
            labelPlayers.setText("Player 1 turn");
            labelPlayers.setStyle("-fx-text-fill: gray ;");
            gameContext.getChildren().add(labelPlayers);
            this.player1 = true;
            this.player2 = true;
            this.turn = true;
        }




        /*
        if (!simonCopy){
            timeBeforeResetComputerAnimation = new AnimationTimer() {
                int nbframes = 0;
                @Override
                public void handle(long now) {
                    if (nbframes == 60){
                        nbframes = 0;
                        nbSecond++;
                        if (nbSecond == secondsReset){
                            resetComputerAnimation = true;
                        }
                    }
                    nbframes++;
                }
            };
            timeBeforeResetComputerAnimation.start();
        }
*/
        nextNote = true;
        lastNotePlayer = false;
        computerPlayAnimation = new Timeline(new KeyFrame(Duration.millis(16), event -> {


            if (computerPlay){ //Le robot joue
                disableAllBoutons();
                if(multiplayer){

                    if (turn && player1){ //player 1 turn
                        labelPlayers.setText(gameContext.getTranslator().translate("Player 1 turn"));
                    }else if(!turn && player2){ //player 2 turn
                        labelPlayers.setText(gameContext.getTranslator().translate("Player 2 turn"));
                    }
                }
                Timeline waitForLastNote = new Timeline(new KeyFrame(new Duration(3000)));
                waitForLastNote.setOnFinished(actionEvent -> {
                    lastNotePlayer = true;
                    waitForLastNote.stop();
                });
                waitForLastNote.play();
                if (lastNotePlayer){
                    while(i < gameInstance.computerSequence.size() && nextNote){
                        String note = gameInstance.computerSequence.get(i);
                        for (Bouton bouton : boutons){
                            if (note.compareToIgnoreCase(bouton.couleur) == 0){
                                playSongAnimation(bouton);
                                i++;
                                break;
                            }
                        }
                    }
                }
                nbSecond = 0; //Rénitialisation du timer qui doit reset l'animation dans le cas où le joueur met trop de temps
            }
            if(i == gameInstance.computerSequence.size()){ //Si le robot à finit de jouer son animation, c'est au tour du joueur de jouer
                computerPlay = false;
                lastNotePlayer = false;
                if (nextNote){
                    activateAllBoutons();
                }
            }

            if (!simonCopy){
                if (gameInstance.playerSequence.size() == gameInstance.computerSequence.size() || resetComputerAnimation){
                    resetComputerAnimationClassic();
                }
            } else if (gameInstance.playerSequence.size() == nbNotePlayer){

                resetComputerAnimationCopy();
            }

            if (multiplayer && !player1 && !player2){
                computerPlayAnimation.stop();
                gameInstance.endGame();
            }else if (!multiplayer && gameInstance.computerSequence.size() == nbNoteMax){
                computerPlayAnimation.stop();
                gameInstance.endGame();
            }
        }));

        computerPlayAnimation.setCycleCount(Animation.INDEFINITE);
        computerPlayAnimation.play();
    }

    private void resetComputerAnimationCopy(){
        boolean verifIncrement = false;

        if (gameInstance.computerSequence.size() == 0){
            verifIncrement = true;
        }else{
            ArrayList<String> comp = new ArrayList<>(gameInstance.playerSequence);
            comp.remove(comp.size()-1);
            if (comp.equals(gameInstance.computerSequence)){
                verifIncrement = true;
            }
        }
        if (verifIncrement){
            gameInstance.computerSequence.add(gameInstance.playerSequence.get(gameInstance.playerSequence.size()-1));
            nbNotePlayer++;
        }
        playSuccessAnimation(verifIncrement);

        gameInstance.playerSequence.clear();

        i = 0;
        computerPlay = true;
        resetComputerAnimation = false;
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

        System.out.println("player 1 : " + player1);
        System.out.println("player 2 : " + player2);

        System.out.println("turn : " + turn);

        if (!increment){
            if (turn){
                player1 = false;
            }else{
                player2 = false;
            }
        }
        if (turn && player2){
            turn = false;
        }else if (!turn && player1){
            turn = true;
        }

        playSuccessAnimation(increment);
        i = 0;
        computerPlay = true;
        resetComputerAnimation = false;
    }

    private void playSuccessAnimation(boolean win){
        if (win){
            this.right.setOpacity(1);
        }else{
            this.wrong.setOpacity(1);
        }
        Timeline animation = new Timeline(new KeyFrame(new Duration(2000)));

        animation.setOnFinished(actionEvent ->{
            this.right.setOpacity(0);
            this.wrong.setOpacity(0);
        });
        animation.play();
    }

    private void playSongAnimation(Bouton bouton){
        System.out.println("playsong for : " + bouton.couleur);
        nextNote = false;
        bouton.isActivated = false;
        gameInstance.playSound(bouton.note);
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



        this.right.setX(dimension2D.getWidth()/2-80);
        this.wrong.setX(dimension2D.getWidth()/2-80);
        this.right.setY(dimension2D.getHeight()/2-80);
        this.wrong.setY(dimension2D.getHeight()/2-80);

        this.right.prefWidth(200);
        this.wrong.prefWidth(200);
        this.right.prefHeight(200);
        this.wrong.prefHeight(200);

        this.right.setOpacity(0);
        this.wrong.setOpacity(0);
        gameContext.getChildren().addAll(this.right,this.wrong);

    }

    private void initBoutons(){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Bouton topRight = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius-5,radius-5,0,90,gameContext,gameInstance, "rouge",gameInstance.musicNotes.get(0));
        topRight.setFill(Color.RED);

        Bouton topLeft = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius-5,radius-5,90,90,gameContext,gameInstance, "vert",gameInstance.musicNotes.get(1));
        topLeft.setFill(Color.web("2FDF3C"));

        Bouton bottomLeft = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius-5,radius-5,180,90,gameContext,gameInstance,"jaune",gameInstance.musicNotes.get(2));
        bottomLeft.setFill(Color.YELLOW);

        Bouton bottomRight = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius-5,radius-5,270,90,gameContext,gameInstance,"bleu",gameInstance.musicNotes.get(3));
        bottomRight.setFill(Color.web("2FB4DF"));

        this.boutons.add(topRight);
        this.boutons.add(topLeft);
        this.boutons.add(bottomRight);
        this.boutons.add(bottomLeft);
    }



}
