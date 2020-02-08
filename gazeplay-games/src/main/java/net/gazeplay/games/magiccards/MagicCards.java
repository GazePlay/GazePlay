package net.gazeplay.games.magiccards;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by schwab on 17/09/2016.
 */
@Slf4j
public class MagicCards implements GameLifeCycle {

    @Data
    @AllArgsConstructor
    private static class RoundDetails {
        private final List<Card> cardList;
        private final int winnerImageIndexAmongDisplayedImages;
    }

    private static final float cardRatio = 0.75f;

    private static final int minHeight = 30;

    private final IGameContext gameContext;

    private final int nbLines;
    private final int nbColumns;

    private final Stats stats;

    private final ImageLibrary imageLibrary;

    private RoundDetails currentRoundDetails;

    public MagicCards(final IGameContext gameContext, final int nbLines, final int nbColumns, final Stats stats) {
        super();
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.stats = stats;

        imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubDirectory("magiccards"));
    }

    @Override
    public void launch() {
        final Configuration config = gameContext.getConfiguration();

        final int cardsCount = nbColumns * nbLines;
        // final int winnerCardIndex = (int) (cardsCount * Math.random());
        final Random r = new Random();
        final int winnerCardIndex = r.nextInt(cardsCount);
        final List<Card> cardList = createCards(winnerCardIndex, config);

        currentRoundDetails = new RoundDetails(cardList, winnerCardIndex);

        gameContext.getChildren().addAll(cardList);

        cardList.get(winnerCardIndex).toFront();

        stats.notifyNewRoundReady();
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
        final List<Card> cardsToHide = new ArrayList<>();
        for (final Card pictureCard : this.currentRoundDetails.cardList) {
            if (!pictureCard.isWinner()) {
                cardsToHide.add(pictureCard);
            }
        }

        // remove all at once, in order to update the UserInterface only once
        gameContext.getChildren().removeAll(cardsToHide);
    }

    private List<Card> createCards(final int winnerCardIndex, final Configuration config) {
        final javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        log.debug("Width {} ; height {}", gameDimension2D.getWidth(), gameDimension2D.getHeight());

        final double cardHeight = computeCardHeight(gameDimension2D, nbLines);
        final double cardWidth = cardHeight * cardRatio;

        log.debug("cardWidth {} ; cardHeight {}", cardWidth, cardHeight);

        final double width = computeCardWidth(gameDimension2D, nbColumns) - cardWidth;

        log.debug("width {} ", width);

        final List<Card> result = new ArrayList<>();

        int currentCardIndex = 0;

        final int fixationlength = config.getFixationLength();

        for (int currentLineIndex = 0; currentLineIndex < nbLines; currentLineIndex++) {
            for (int currentColumnIndex = 0; currentColumnIndex < nbColumns; currentColumnIndex++) {

                final boolean isWinnerCard;
                final Image image;

                if (currentCardIndex == winnerCardIndex) {
                    isWinnerCard = true;
                    image = imageLibrary.pickRandomImage();
                } else {
                    isWinnerCard = false;
                    image = new Image("data/common/images/error.png");
                }

                final double positionX = width / 2 + (width + cardWidth) * currentColumnIndex;
                final double positionY = minHeight / 2d + (minHeight + cardHeight) * currentLineIndex;

                log.debug("positionX : {} ; positionY : {}", positionX, positionY);

                final Card card = new Card(positionX, positionY, cardWidth, cardHeight, image, isWinnerCard, gameContext,
                    stats, this, fixationlength);

                result.add(card);
                currentCardIndex++;
            }
        }

        return result;
    }

    private static double computeCardHeight(final Dimension2D gameDimension2D, final int nbLines) {
        return gameDimension2D.getHeight() * 0.9 / nbLines;
    }

    private static double computeCardWidth(final Dimension2D gameDimension2D, final int nbColumns) {
        return gameDimension2D.getWidth() / nbColumns;
    }

}
