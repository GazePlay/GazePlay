package net.gazeplay.games.magiccards;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.HiddenItemsGamesStats;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schwab on 17/09/2016.
 */
public class MagicCards implements GameLifeCycle {

    @Data
    @AllArgsConstructor
    public static class RoundDetails {
        private final List<Card> cardList;
        private final int winnerImageIndexAmongDisplayedImages;
    }

    private static final float cardRatio = 0.75f;

    private static final int minHeight = 30;

    private final GameContext gameContext;

    private final int nbLines;
    private final int nbColumns;

    private final HiddenItemsGamesStats stats;

    private final Image[] images;

    private RoundDetails currentRoundDetails;

    public MagicCards(GameContext gameContext, int nbLines, int nbColumns, HiddenItemsGamesStats stats) {
        super();
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.stats = stats;

        images = Utils.images(Utils.getImagesFolder() + "magiccards" + Utils.FILESEPARATOR);
    }

    @Override
    public void launch() {
        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        final int cardsCount = nbColumns * nbLines;
        final int winnerCardIndex = (int) (cardsCount * Math.random());
        List<Card> cardList = createCards(winnerCardIndex, config);

        currentRoundDetails = new RoundDetails(cardList, winnerCardIndex);

        gameContext.getChildren().addAll(cardList);

        cardList.get(winnerCardIndex).toFront();

        stats.start();
    }

    @Override
    public void dispose() {
        if (currentRoundDetails != null) {
            if (currentRoundDetails.cardList != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.cardList);
            }
            currentRoundDetails = null;
        }
    }

    public void removeAllIncorrectCards() {
        if (this.currentRoundDetails == null) {
            return;
        }

        // Collect all items to be removed from the User Interface
        List<Card> cardsToHide = new ArrayList<>();
        for (Card pictureCard : this.currentRoundDetails.cardList) {
            if (!pictureCard.isWinner()) {
                cardsToHide.add(pictureCard);
            }
        }

        // remove all at once, in order to update the UserInterface only once
        gameContext.getChildren().removeAll(cardsToHide);
    }

    private List<Card> createCards(int winnerCardIndex, Configuration config) {
        Scene scene = gameContext.getScene();

        final double cardHeight = computeCardHeight(scene, nbLines);
        final double cardWidth = cardHeight * cardRatio;

        double width = computeCardWidth(scene, nbColumns) - cardWidth;

        List<Card> result = new ArrayList<>();

        int currentCardIndex = 0;

        final int fixationlength = config.getFixationlength();

        for (int currentLineIndex = 0; currentLineIndex < nbLines; currentLineIndex++) {
            for (int currentColumnIndex = 0; currentColumnIndex < nbLines; currentColumnIndex++) {

                final boolean isWinnerCard;
                final Image image;

                if (currentCardIndex == winnerCardIndex) {
                    isWinnerCard = true;
                    image = getRandomImage();
                } else {
                    isWinnerCard = false;
                    image = new Image("data/common/images/error.png");
                }

                double positionX = width / 2 + (width + cardWidth) * currentLineIndex;
                double positionY = minHeight / 2 + (minHeight + cardHeight) * currentColumnIndex;

                Card card = new Card(positionX, positionY, cardWidth, cardHeight, image, isWinnerCard, gameContext,
                        stats, this, fixationlength);

                result.add(card);
                currentCardIndex++;
            }
        }

        return result;
    }

    private static double computeCardHeight(Scene scene, int nbLines) {
        return scene.getHeight() * 0.9 / nbLines;
    }

    private static double computeCardWidth(Scene scene, int nbColumns) {
        return scene.getWidth() / nbColumns;
    }

    private Image getRandomImage() {
        int value = (int) Math.floor(Math.random() * images.length);
        return images[value];
    }

}
