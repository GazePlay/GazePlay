package net.gazeplay.games.magicpotions;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Johana MARKU - the graphics are kindly provided by these -
 * @artists 0melapics, macrovector, vectorpouch / Freepik
 */

@Slf4j
public class MagicPotions extends Parent implements GameLifeCycle {

    @Data
    public static class RoundDetails {

        private final List<Color> potionsToMix;

        private final PotionMix request;

        private final List<Color> mixture = new LinkedList<>(); // what we select to mix we put it in this list

        private Rectangle mixPot;

        private Ellipse mixPotColor;

        private Color colorRequest;

        public RoundDetails(final List<Color> potionsToMix, final PotionMix request) {
            this.potionsToMix = potionsToMix;
            this.request = request;
        }

    }


    private final IGameContext gameContext;

    private final MagicPotionsStats stats;


    public MagicPotions.RoundDetails currentRoundDetails;

    private final Dimension2D gameDimension2D;

    private final ReplayablePseudoRandom randomGenerator;

    @Getter
    @Setter
    private Potion potionRed;

    @Getter
    @Setter
    private Potion potionYellow;

    @Getter
    @Setter
    private Potion potionBlue;

    MagicPotions(final IGameContext gameContext, final Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = (MagicPotionsStats) stats;
        this.gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setCurrentGameSeed(randomGenerator.getSeed());
    }

    MagicPotions(final IGameContext gameContext, final Stats stats, double gameSeed) {
        super();
        this.gameContext = gameContext;
        this.stats = (MagicPotionsStats) stats;
        this.gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
    }

    @Override
    public void launch() {

        gameContext.setLimiterAvailable();
        final String imagePATH = "data/potions/images/";

        initBackground(imagePATH);

        /* BIBOULE - CLIENT */
        final Image bibouleClient = new Image(imagePATH + "Biboule-Client.png");

        final double bibX = gameDimension2D.getWidth() * 2 / 3 - bibouleClient.getWidth() / 2;
        final double bibY = 50;//

        // make random potion request
        final PotionMix request = PotionMix.getRandomPotionRequest(randomGenerator);

        final Client client = new Client(bibX, bibY, bibouleClient.getWidth(), bibouleClient.getHeight(), bibouleClient, request);

        // since the background of the image is transparent this color will fill it
        final Circle clientColor = new Circle(gameDimension2D.getWidth() * 0.735, bibouleClient.getHeight() * 0.42,
            bibouleClient.getHeight() * 0.23);
        clientColor.setFill(request.getColor());

        gameContext.getChildren().add(clientColor);
        gameContext.getChildren().add(client.getClient());

        currentRoundDetails = new MagicPotions.RoundDetails(client.getColorsToMix(), client.getPotionRequest());
        currentRoundDetails.colorRequest = request.getColor();

        // 3 potions
        final Image red = new Image(imagePATH + "potionRed.png");
        final Image yellow = new Image(imagePATH + "potionYellow.png");
        final Image blue = new Image(imagePATH + "potionBlue.png");

        double imageWidth = gameDimension2D.getWidth() / 9;
        double imageHeight = gameDimension2D.getHeight() / 4;

        potionRed = new Potion(gameDimension2D.getWidth() * 6 / 7 - (imageWidth + imageWidth) * 1.5,
            gameDimension2D.getHeight() - imageHeight - 10, imageWidth, imageHeight, red, Color.RED,
            gameContext, stats, this, gameContext.getConfiguration().getFixationLength());

        potionYellow = new Potion(gameDimension2D.getWidth() * 6 / 7 - imageWidth * 1.5,
            gameDimension2D.getHeight() - imageHeight - 10, imageWidth, imageHeight, yellow,
            Color.YELLOW, gameContext, stats, this, gameContext.getConfiguration().getFixationLength());

        potionBlue = new Potion(gameDimension2D.getWidth() * 6 / 7, gameDimension2D.getHeight() - imageHeight - 10,
            imageWidth, imageHeight, blue, Color.BLUE, gameContext, stats, this,
            gameContext.getConfiguration().getFixationLength());

        final LinkedList<Potion> potionsOnTable = new LinkedList<>();
        potionsOnTable.add(potionBlue);
        potionsOnTable.add(potionRed);
        potionsOnTable.add(potionYellow);
        gameContext.getChildren().addAll(potionsOnTable);

        // mixing Pot
        final Image mixPotImage = new Image(imagePATH + "mixingPot.png");
        currentRoundDetails.mixPot = new Rectangle(gameDimension2D.getWidth() * 2 / 7,
            gameDimension2D.getHeight() - mixPotImage.getHeight(), mixPotImage.getWidth(), mixPotImage.getHeight());
        currentRoundDetails.mixPot.setFill(new ImagePattern(mixPotImage, 0, 0, 1, 1, true));

        currentRoundDetails.mixPotColor = new Ellipse(gameDimension2D.getWidth() * 2 / 7 + mixPotImage.getWidth() / 2,
            gameDimension2D.getHeight() - mixPotImage.getHeight() / 1.5, mixPotImage.getHeight() / 2,
            mixPotImage.getWidth() / 4);

        currentRoundDetails.mixPotColor.setFill(Color.WHITE); // nothing in the mixing pot
        gameContext.getChildren().add(currentRoundDetails.mixPotColor);
        gameContext.getChildren().add(currentRoundDetails.mixPot);

        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();

    }

    void initBackground(final String imagePATH) {
        if (gameContext.getConfiguration().isBackgroundEnabled()) {
            Rectangle background = new Rectangle(0, 0, (int) gameDimension2D.getWidth(), (int) gameDimension2D.getHeight());
            background.widthProperty().bind(gameContext.getRoot().widthProperty());
            background.heightProperty().bind(gameContext.getRoot().heightProperty());
            background.setFill(new ImagePattern(new Image(imagePATH + "background-potions.jpg")));
            gameContext.getChildren().add(background);
        }
    }

    @Override
    public void dispose() {
        currentRoundDetails.getPotionsToMix().clear();
        currentRoundDetails = null;
    }
}
