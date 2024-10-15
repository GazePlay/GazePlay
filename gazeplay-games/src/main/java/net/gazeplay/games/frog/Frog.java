package net.gazeplay.games.frog;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Random;

@Slf4j
public class Frog implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom randomGenerator;
    IA ia;
    Nenuphar[] nenuphars;
    ImageView frog;
    int frogPosition;
    int correctFrogPosition;
    int actualIteration = 1;
    int nbNenuphars = 10;
    Rectangle2D screensBounds;
    double screenWidth;
    double screenHeight;

    public Frog(IGameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());
    }

    public Frog(IGameContext gameContext, Stats stats, double gameSeed) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();

        Image nenupharImg = new Image("data/frog/images/lotus.png");
        Image frogImg = new Image("data/frog/images/frog.png");

        this.screensBounds = Screen.getPrimary().getVisualBounds();
        this.screenWidth = screensBounds.getWidth();
        this.screenHeight = screensBounds.getHeight();

        frog = new ImageView(frogImg);

        this.drawNenuphars(this.screenWidth, this.screenHeight, nenupharImg);
        this.generateRandomStart();

        this.gameContext.getChildren().add(frog);

        this.ia = new IA(this, gameContext);
        this.iaPlay();
    }

    public void iaPlay(){
        log.info("Actual iteration -> " + this.actualIteration);
        this.ia.iaMoves(this.actualIteration);
        this.actualIteration++;
    }

    public void generateRandomStart(){
        Random random = new Random();
        frogPosition = random.nextInt(nbNenuphars);
        moveFrogTo(nenuphars[frogPosition]);
    }

    public void drawNenuphars(double screenWidth, double screenHeight, Image nenupharImg){
        nenuphars = new Nenuphar[nbNenuphars];
        for (int i=0; i<nbNenuphars; i++){
            nenuphars[i] = new Nenuphar(screenWidth, screenHeight, nenupharImg, i, nbNenuphars, gameContext, this);
        }
    }

    public void moveFrogTo(Nenuphar nenuphar){
        log.info("Frog position -> " + this.frogPosition);
        double frogSize = nenuphar.nenupharImgView.getFitWidth() * 0.5;
        frog.setFitWidth(frogSize);
        frog.setFitHeight(frogSize);
        frog.setX(nenuphar.nenupharImgView.getX() + (nenuphar.nenupharImgView.getFitWidth() - frogSize) / 2);
        frog.setY(nenuphar.nenupharImgView.getY() + (nenuphar.nenupharImgView.getFitHeight() - frogSize) / 2);

        for (Nenuphar value : this.nenuphars) {
            value.haveFrog = false;
        }
        nenuphar.haveFrog = true;
        log.info("Frog moved");
    }

    public void setGoodAnswer(String moveType){
        switch (moveType){
            case "oneBack":
                this.correctFrogPosition = this.frogPosition - 1;
                if (this.correctFrogPosition < 0){
                    this.correctFrogPosition = this.nenuphars.length - 1;
                }
                break;

            case "oneFront":
                break;

            case "twoBack":
                break;

            case "jump":
                break;

            default:
                break;
        }
    }

    public void playerTurn(){
        log.info("Player turn");
        for (Nenuphar value : this.nenuphars) {
            value.ignoreInput = false;
            value.nenupharImgView.setOpacity(1);
        }
    }

    public void iaTurn(){
        log.info("IA turn");
        for (Nenuphar value : this.nenuphars) {
            value.ignoreInput = true;
            value.nenupharImgView.setOpacity(0.5);
            value.errorImgView.setVisible(false);
        }
        this.iaPlay();
    }

    @Override
    public void dispose() {
        this.gameContext.getChildren().clear();
        this.gameContext.showRoundStats(stats, this);
    }
}
