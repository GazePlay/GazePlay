package net.gazeplay.games.gazeplayEval;

import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Random;
import java.util.Scanner;

@Slf4j
public class GazeplayEval implements GameLifeCycle {

    private final IGameContext gameContext;
    private final GazeplayEvalGameVariant gameVariant;
    private final boolean fourThree;
    private final Stats stats;

    Circle[] nenuphars;
    Circle frog;
    int iteration = 0;
    int maxIteration = 30;
    int nbNenuphars = 10;
    Rectangle2D screensBounds;
    double screenWidth;
    double screenHeight;

    public GazeplayEval(final boolean fourThree, final IGameContext gameContext, final GazeplayEvalGameVariant gameVariant, final Stats stats) {
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.fourThree = fourThree;
        this.stats = stats;
    }

    public GazeplayEval(final boolean fourThree, final IGameContext gameContext, final GazeplayEvalGameVariant gameVariant, final Stats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.fourThree = fourThree;
        this.stats = stats;

    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();

        this.screensBounds = Screen.getPrimary().getVisualBounds();
        this.screenWidth = screensBounds.getWidth();
        this.screenHeight = screensBounds.getHeight();

        this.drawNenuphars(this.screenWidth, this.screenHeight);

        frog = new Circle(20, Color.GREEN);
        this.gameContext.getChildren().add(frog);

        Random random = new Random();
        int startPosition = random.nextInt(nbNenuphars);
        moveFrogTo(nenuphars[startPosition]);
    }

    public void drawNenuphars(double screenWidth, double screenHeight){

        double radiusX = screenWidth / 3;
        double radiusY = screenHeight / 3;
        double centerX = screenWidth / 2;
        double centerY = screenHeight / 2;

        nenuphars = new Circle[nbNenuphars];
        for (int i=0; i<nbNenuphars; i++){
            double angle = 2 * Math.PI / nbNenuphars * i;
            double x = centerX + radiusX * Math.cos(angle);
            double y = centerY + radiusY * Math.sin(angle);

            Circle nenuphar = new Circle(x, y, Math.min(screenWidth, screenHeight) / 15, Color.LIGHTGREEN);
            nenuphar.setStroke(Color.BLACK);
            this.gameContext.getChildren().add(nenuphar);

            nenuphar.setOnMouseClicked(event -> {
                if (iteration < maxIteration){
                    moveFrogTo(nenuphar);
                    iteration++;
                    if (iteration == maxIteration){
                        this.dispose();
                    }
                }
            });

            nenuphars[i] = nenuphar;
        }
    }

    public void moveFrogTo(Circle nenuphar){
        frog.setCenterX(nenuphar.getCenterX());
        frog.setCenterY(nenuphar.getCenterY());
    }

    @Override
    public void dispose() {

    }
}
