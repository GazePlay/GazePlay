package net.gazeplay.games.magicPotions;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.LinkedList;

@Slf4j
public class MagicPotions extends Parent implements GameLifeCycle {

    private final GameContext gameContext;
    @Getter
    @Setter
    private Potion potionRed;
    @Getter
    @Setter
    private Potion potionYellow;
    @Getter
    @Setter
    private Potion potionBlue;

    // private int fixationLength;
    private Client client;

    // private final mixingPot pot ;

    private boolean potionMixAchieved = false;

    private final Stats stats;

    private Dimension2D gameDimension2D;

    public MagicPotions(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

    }

    @Override
    public void launch() {
        final Configuration config = Configuration.getInstance();

        /* BACKGROUND */
        Rectangle background = new Rectangle(0, 0, (int) gameDimension2D.getWidth(), (int) gameDimension2D.getHeight());
        background.widthProperty().bind(gameContext.getRoot().widthProperty());
        background.heightProperty().bind(gameContext.getRoot().heightProperty());
        background.setFill(new ImagePattern(new Image("data/potions/images/background-potions.jpg")));

        int coef = (Configuration.getInstance().isBackgroundWhite()) ? 1 : 0;
        background.setOpacity(1 - coef * 0.9);

        gameContext.getChildren().add(background);
        /* BIBOULE - CLIENT */
        Image bibouleClient = new Image("data/potions/images/Biboule-Client.png");

        double bibX = gameDimension2D.getWidth() * 2 / 3 - bibouleClient.getWidth() / 2;
        double bibY = 50;//

        // make random potion request
        Client.PotionMix request = Client.PotionMix.getRandomPotionRequest();

        client = new Client(bibX, bibY, bibouleClient.getWidth(), bibouleClient.getHeight(), bibouleClient, request);

        // since the background of the image is transparent this color will fill it
        Circle color = new Circle(gameDimension2D.getWidth() * 3 / 4, bibouleClient.getHeight() / 2.2,
                bibouleClient.getHeight() / 5);
        color.setFill(request.getColor());

        gameContext.getChildren().add(color);
        gameContext.getChildren().add(client.getClient());

        // 3 potions
        Image red = new Image("data/potions/images/potionRed.png");
        Image yellow = new Image("data/potions/images/potionYellow.png");
        Image blue = new Image("data/potions/images/potionBlue.png");
        potionRed = new Potion(gameDimension2D.getWidth() * 6 / 7 - (red.getWidth() + yellow.getWidth()) * 1.5,
                gameDimension2D.getHeight() - red.getHeight() - 10, red.getWidth(), red.getHeight(), red, Color.RED,
                this.gameContext, this.stats, Configuration.getInstance().getFixationLength());

        potionYellow = new Potion(gameDimension2D.getWidth() * 6 / 7 - yellow.getWidth() * 1.5,
                gameDimension2D.getHeight() - yellow.getHeight() - 10, yellow.getWidth(), yellow.getHeight(), yellow,
                Color.YELLOW, this.gameContext, this.stats, Configuration.getInstance().getFixationLength());

        potionBlue = new Potion(gameDimension2D.getWidth() * 6 / 7, gameDimension2D.getHeight() - blue.getHeight() - 10,
                blue.getWidth(), blue.getHeight(), blue, Color.BLUE, this.gameContext, this.stats,
                Configuration.getInstance().getFixationLength());

        LinkedList<Potion> potionsOnTable = new LinkedList<>();
        potionsOnTable.add(potionBlue);
        potionsOnTable.add(potionRed);
        potionsOnTable.add(potionYellow);
        gameContext.getChildren().addAll(potionsOnTable);

        // mixing Pot
        Image mixPotImage = new Image("data/potions/images/mixingPot.png");
        Rectangle mixPot = new Rectangle(gameDimension2D.getWidth() * 2 / 7,
                gameDimension2D.getHeight() - mixPotImage.getHeight(), mixPotImage.getWidth(), mixPotImage.getHeight());
        mixPot.setFill(new ImagePattern(mixPotImage, 0, 0, 1, 1, true));

        Ellipse mixPotColor = new Ellipse(gameDimension2D.getWidth() * 2 / 7 + mixPotImage.getWidth() / 2,
                gameDimension2D.getHeight() - mixPotImage.getHeight() / 1.5, mixPotImage.getHeight() / 2,
                mixPotImage.getWidth() / 4);
        if (potionRed.isChosen()) {
            mixPot.setFill(potionRed.getPotionColor());
        }
        mixPotColor.setFill(Color.GRAY);
        gameContext.getChildren().add(mixPotColor);
        gameContext.getChildren().add(mixPot);

    }

    @Override
    public void dispose() {

    }
}
