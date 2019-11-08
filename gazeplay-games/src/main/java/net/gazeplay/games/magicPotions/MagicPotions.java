package net.gazeplay.games.magicPotions;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.LinkedList;

/**
 *
 * @author Johana MARKU - the graphics are kindly provided by these -
 * @artists 0melapics,macrovector,vectorpouch / Freepik
 */

@Slf4j
public class MagicPotions extends Parent implements GameLifeCycle {

    @Data
    @AllArgsConstructor
    public static class RoundDetails {
        private final LinkedList<Color> potionsToMix;
        private final Client.PotionMix request;
    }

    private final IGameContext gameContext;

    private final MagicPotionsStats stats;

    public MagicPotions.RoundDetails currentRoundDetails;

    private Dimension2D gameDimension2D;
    @Getter
    private static Rectangle mixPot;
    @Getter
    private static Ellipse mixPotColor;
    @Getter
    private static Color colorRequest;
    @Getter
    @Setter
    private Potion potionRed;
    @Getter
    @Setter
    private Potion potionYellow;
    @Getter
    @Setter
    private Potion potionBlue;

    MagicPotions(IGameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = (MagicPotionsStats) stats;
        this.gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
    }

    @Override
    public void launch() {

        /* BACKGROUND */
        Rectangle background = new Rectangle(0, 0, (int) gameDimension2D.getWidth(), (int) gameDimension2D.getHeight());
        background.widthProperty().bind(gameContext.getRoot().widthProperty());
        background.heightProperty().bind(gameContext.getRoot().heightProperty());
        String image_PATH = "data/potions/images/";
        background.setFill(new ImagePattern(new Image(image_PATH + "background-potions.jpg")));

        int coef = (ActiveConfigurationContext.getInstance().isBackgroundWhite()) ? 1 : 0;
        background.setOpacity(1 - coef * 0.9);

        gameContext.getChildren().add(background);
        /* BIBOULE - CLIENT */
        Image bibouleClient = new Image(image_PATH + "Biboule-Client.png");

        double bibX = gameDimension2D.getWidth() * 2 / 3 - bibouleClient.getWidth() / 2;
        double bibY = 50;//

        // make random potion request
        Client.PotionMix request = Client.PotionMix.getRandomPotionRequest();
        colorRequest = request.getColor();
        Client client = new Client(bibX, bibY, bibouleClient.getWidth(), bibouleClient.getHeight(), bibouleClient, request);

        // since the background of the image is transparent this color will fill it
        Circle clientColor = new Circle(gameDimension2D.getWidth() * 3 / 4, bibouleClient.getHeight() / 2.2,
            bibouleClient.getHeight() / 5);
        clientColor.setFill(request.getColor());

        gameContext.getChildren().add(clientColor);
        gameContext.getChildren().add(client.getClient());

        currentRoundDetails = new MagicPotions.RoundDetails(client.getColorsToMix(), client.getPotionRequest());

        // 3 potions
        Image red = new Image(image_PATH + "potionRed.png");
        Image yellow = new Image(image_PATH + "potionYellow.png");
        Image blue = new Image(image_PATH + "potionBlue.png");
        potionRed = new Potion(gameDimension2D.getWidth() * 6 / 7 - (red.getWidth() + red.getWidth()) * 1.5,
                gameDimension2D.getHeight() - red.getHeight() - 10, red.getWidth(), red.getHeight(), red, Color.RED,
                gameContext, stats, this, ActiveConfigurationContext.getInstance().getFixationLength());

        potionYellow = new Potion(gameDimension2D.getWidth() * 6 / 7 - yellow.getWidth() * 1.5,
                gameDimension2D.getHeight() - yellow.getHeight() - 10, yellow.getWidth(), yellow.getHeight(), yellow,
                Color.YELLOW, gameContext, stats, this, ActiveConfigurationContext.getInstance().getFixationLength());

        potionBlue = new Potion(gameDimension2D.getWidth() * 6 / 7, gameDimension2D.getHeight() - blue.getHeight() - 10,
                blue.getWidth(), blue.getHeight(), blue, Color.BLUE, gameContext, stats, this,
                ActiveConfigurationContext.getInstance().getFixationLength());

        LinkedList<Potion> potionsOnTable = new LinkedList<>();
        potionsOnTable.add(potionBlue);
        potionsOnTable.add(potionRed);
        potionsOnTable.add(potionYellow);
        gameContext.getChildren().addAll(potionsOnTable);

        // mixing Pot
        Image mixPotImage = new Image(image_PATH + "mixingPot.png");
        mixPot = new Rectangle(gameDimension2D.getWidth() * 2 / 7,
                gameDimension2D.getHeight() - mixPotImage.getHeight(), mixPotImage.getWidth(), mixPotImage.getHeight());
        mixPot.setFill(new ImagePattern(mixPotImage, 0, 0, 1, 1, true));

        mixPotColor = new Ellipse(gameDimension2D.getWidth() * 2 / 7 + mixPotImage.getWidth() / 2,
                gameDimension2D.getHeight() - mixPotImage.getHeight() / 1.5, mixPotImage.getHeight() / 2,
                mixPotImage.getWidth() / 4);

        mixPotColor.setFill(Color.WHITE); // nothing in the mixing pot
        gameContext.getChildren().add(mixPotColor);
        gameContext.getChildren().add(mixPot);

        stats.notifyNewRoundReady();

    }

    @Override
    public void dispose() {
        currentRoundDetails.getPotionsToMix().clear();
        // potionRed.setChosen(false);
        // potionBlue.setChosen(false);
        // potionYellow.setChosen(false);
        Potion.getMixture().clear();

    }
}
