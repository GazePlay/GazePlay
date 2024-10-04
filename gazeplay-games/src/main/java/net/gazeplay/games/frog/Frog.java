package net.gazeplay.games.frog;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Random;

public class Frog implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom randomGenerator;

    ImageView[] nenuphars;
    ImageView frog;
    int iteration = 0;
    int maxIteration = 30;
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

        Image nenupharImg = new Image("data/frog/images/nenuphar.png");
        Image frogImg = new Image("data/frog/images/frog.png");

        this.screensBounds = Screen.getPrimary().getVisualBounds();
        this.screenWidth = screensBounds.getWidth();
        this.screenHeight = screensBounds.getHeight();

        frog = new ImageView(frogImg);

        this.drawNenuphars(this.screenWidth, this.screenHeight, nenupharImg);

        this.gameContext.getChildren().add(frog);
    }

    public void drawNenuphars(double screenWidth, double screenHeight, Image nenupharImg){

        double radiusX = screenWidth / 3;
        double radiusY = screenHeight / 3;
        double centerX = screenWidth / 2;
        double centerY = screenHeight / 2;

        double nenupharSize = (Math.min(radiusX, radiusY) / 3) * 2;

        nenuphars = new ImageView[nbNenuphars];
        for (int i=0; i<nbNenuphars; i++){
            double angle = 2 * Math.PI / nbNenuphars * i;

            double x = centerX + radiusX * Math.cos(angle) - nenupharSize / 2;
            double y = centerY + radiusY * Math.sin(angle) - nenupharSize / 2;

            ImageView nenupharImgVew = new ImageView(nenupharImg);
            nenupharImgVew.setFitWidth(nenupharSize);
            nenupharImgVew.setFitHeight(nenupharSize);
            nenupharImgVew.setX(x);
            nenupharImgVew.setY(y);
            this.gameContext.getChildren().add(nenupharImgVew);

            nenupharImgVew.setOnMouseClicked(event -> {
                if (iteration < maxIteration){
                    moveFrogTo(nenupharImgVew);
                    iteration++;
                    if (iteration == maxIteration){
                        this.dispose();
                    }
                }
            });

            nenuphars[i] = nenupharImgVew;
        }

        if (iteration == 0){
            Random random = new Random();
            int startPosition = random.nextInt(nbNenuphars);
            moveFrogTo(nenuphars[startPosition]);
        }
    }

    public void moveFrogTo(ImageView nenuphar){
        double frogSize = nenuphar.getFitWidth() * 0.5;
        frog.setFitWidth(frogSize);
        frog.setFitHeight(frogSize);
        frog.setX(nenuphar.getX() + (nenuphar.getFitWidth() - frogSize) / 2);
        frog.setY(nenuphar.getY() + (nenuphar.getFitHeight() - frogSize) / 2);
    }


    @Override
    public void dispose() {

    }
}
