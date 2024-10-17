package net.gazeplay.games.frog;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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
    ImageView info;

    public Frog(IGameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(this.randomGenerator.getSeed());
    }

    public Frog(IGameContext gameContext, Stats stats, double gameSeed) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
    }

    @Override
    public void launch() {
        this.gameContext.setLimiterAvailable();
        this.stats.notifyNewRoundReady();
        this.gameContext.getGazeDeviceManager().addStats(this.stats);
        this.gameContext.firstStart();

        this.createInfo();
        Image nenupharImg = new Image("data/frog/images/lotus.png");
        Image frogImg = new Image("data/frog/images/frog.png");

        this.screensBounds = Screen.getPrimary().getVisualBounds();
        this.screenWidth = this.screensBounds.getWidth();
        this.screenHeight = this.screensBounds.getHeight();

        this.frog = new ImageView(frogImg);

        this.drawNenuphars(this.screenWidth, this.screenHeight, nenupharImg);
        this.generateRandomStart();

        this.gameContext.getChildren().add(this.frog);

        this.ia = new IA(this, this.gameContext);
        this.iaPlay();
    }

    public void iaPlay(){
        this.ia.iaMoves(this.actualIteration);
        this.actualIteration++;
    }

    public void generateRandomStart(){
        Random random = new Random();
        this.frogPosition = random.nextInt(this.nbNenuphars);
        moveFrogTo(this.nenuphars[this.frogPosition]);
    }

    public void drawNenuphars(double screenWidth, double screenHeight, Image nenupharImg){
        this.nenuphars = new Nenuphar[this.nbNenuphars];
        for (int i=0; i<this.nbNenuphars; i++){
            this.nenuphars[i] = new Nenuphar(screenWidth, screenHeight, nenupharImg, i, this.nbNenuphars, this.gameContext, this);
        }
    }

    public void moveFrogTo(Nenuphar nenuphar){
        double frogSize = nenuphar.nenupharImgView.getFitWidth() * 0.5;
        this.frog.setFitWidth(frogSize);
        this.frog.setFitHeight(frogSize);
        this.frog.setX(nenuphar.nenupharImgView.getX() + (nenuphar.nenupharImgView.getFitWidth() - frogSize) / 2);
        this.frog.setY(nenuphar.nenupharImgView.getY() + (nenuphar.nenupharImgView.getFitHeight() - frogSize) / 2);

        for (Nenuphar value : this.nenuphars) {
            value.haveFrog = false;
        }
        nenuphar.haveFrog = true;
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
                this.correctFrogPosition = this.frogPosition + 1;
                if (this.correctFrogPosition > 9){
                    this.correctFrogPosition = 0;
                }
                break;

            case "twoBack":
                this.correctFrogPosition = this.frogPosition - 2;
                if (this.correctFrogPosition == -1){
                    this.correctFrogPosition = this.nenuphars.length - 1;
                }else {
                    this.correctFrogPosition = this.nenuphars.length - 2;
                }
                break;

            case "jump":
                this.correctFrogPosition = this.ia.futureFrogPosition;
                break;

            default:
                break;
        }
    }

    public void playerTurn(){
        for (Nenuphar value : this.nenuphars) {
            value.ignoreInput = false;
            value.nenupharImgView.setOpacity(1);
        }
        this.info.setVisible(true);
    }

    public void iaTurn(){
        for (Nenuphar value : this.nenuphars) {
            value.ignoreInput = true;
            value.nenupharImgView.setOpacity(0.5);
            value.errorImgView.setVisible(false);
        }
        this.info.setVisible(false);
        this.iaPlay();
    }

    public void createInfo(){
        Region region = this.gameContext.getRoot();
        this.info = new ImageView("data/frog/images/frogInfo.png");
        this.info.setFitWidth(region.getWidth());
        this.info.setFitHeight(region.getHeight());
        this.info.setVisible(false);
        this.gameContext.getChildren().add(this.info);
    }

    @Override
    public void dispose() {
        this.gameContext.getChildren().clear();
        this.gameContext.showRoundStats(this.stats, this);
    }
}
