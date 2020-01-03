package net.gazeplay.games.memory;

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

import java.util.*;

@Slf4j
public class Memory implements GameLifeCycle {

    private static final float cardRatio = 0.75f;

    private static final int minHeight = 30;

    public enum MemoryGameType {

        LETTERS, NUMBERS, DEFAULT
    }

    @Data
    @AllArgsConstructor
    public static class RoundDetails {
        public final List<MemoryCard> cardList;
    }

    private int nbRemainingPeers;

    private final IGameContext gameContext;

    private final int nbLines;
    private final int nbColumns;

    private final Stats stats;

    private ImageLibrary imageLibrary;

    /*
     * HashMap of images selected for this game and their associated id The id is the same for the 2 same images
     */
    public HashMap<Integer, Image> images;

    public RoundDetails currentRoundDetails;

    public int nbTurnedCards;

    private final boolean isOpen;

    public Memory(final MemoryGameType gameType, IGameContext gameContext, int nbLines, int nbColumns, Stats stats,
            boolean isOpen) {
        super();
        this.isOpen = isOpen;
        int cardsCount = nbLines * nbColumns;
        if ((cardsCount & 1) != 0) {
            // nbLines * nbColumns must be a multiple of 2
            throw new IllegalArgumentException("Cards count must be an even number in this game");
        }
        this.nbRemainingPeers = (nbLines * nbColumns) / 2;
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.stats = stats;

        if (gameType == MemoryGameType.LETTERS) {

            this.imageLibrary = ImageUtils.createCustomizedImageLibrary(null, "data/common/letters");
        } else if (gameType == MemoryGameType.NUMBERS) {

            this.imageLibrary = ImageUtils.createCustomizedImageLibrary(null, "data/common/numbers");
        } else
            this.imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubDirectory("magiccards"),
                    Utils.getImagesSubDirectory("default"));
    }

    private HashMap<Integer, Image> selectionAleaImages() {
        final int cardsCount = nbColumns * nbLines;
        HashMap<Integer, Image> res = new HashMap<>();

        Set<Image> images = imageLibrary.pickMultipleRandomDistinctImages(cardsCount / 2);

        int i = 0;
        for (Image image : images) {
            res.put(i, image);
            i++;
        }
        return res;
    }

    @Override
    public void launch() {
        Configuration config = gameContext.getConfiguration();
        final int cardsCount = nbColumns * nbLines;

        images = selectionAleaImages();

        List<MemoryCard> cardList = createCards(images, config);

        nbRemainingPeers = cardsCount / 2;

        currentRoundDetails = new RoundDetails(cardList);

        gameContext.getChildren().addAll(cardList);

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

    public void removeSelectedCards() {
        if (this.currentRoundDetails == null) {
            return;
        }
        List<MemoryCard> cardsToHide = new ArrayList<>();
        for (MemoryCard pictureCard : this.currentRoundDetails.cardList) {
            if (pictureCard.isTurned()) {
                cardsToHide.add(pictureCard);
            }
        }
        nbRemainingPeers = nbRemainingPeers - 1;
        // remove all turned cards
        gameContext.getChildren().removeAll(cardsToHide);
    }

    private List<MemoryCard> createCards(HashMap<Integer, Image> im, Configuration config) {
        javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        log.debug("Width {} ; height {}", gameDimension2D.getWidth(), gameDimension2D.getHeight());

        final double cardHeight = computeCardHeight(gameDimension2D, nbLines);
        final double cardWidth = cardHeight * cardRatio;

        log.debug("cardWidth {} ; cardHeight {}", cardWidth, cardHeight);

        double width = computeCardWidth(gameDimension2D, nbColumns) - cardWidth;

        log.debug("width {} ", width);

        List<MemoryCard> result = new ArrayList<>();

        // HashMap <index, number of times the index was used >
        HashMap<Integer, Integer> indUsed = new HashMap<>();
        indUsed.clear();

        final int fixationlength = config.getFixationLength();

        for (int currentLineIndex = 0; currentLineIndex < nbLines; currentLineIndex++) {
            for (int currentColumnIndex = 0; currentColumnIndex < nbColumns; currentColumnIndex++) {

                double positionX = width / 2 + (width + cardWidth) * currentColumnIndex;
                double positionY = minHeight / 2 + (minHeight + cardHeight) * currentLineIndex;

                log.debug("positionX : {} ; positionY : {}", positionX, positionY);

                int id = getRandomValue(indUsed);

                if (indUsed.containsKey(id)) {
                    indUsed.replace(id, 1, 2);
                } else {
                    indUsed.put(id, 1);
                }

                Image image = images.get(id);

                MemoryCard card = new MemoryCard(positionX, positionY, cardWidth, cardHeight, image, id, gameContext,
                        stats, this, fixationlength, isOpen);

                result.add(card);
            }
        }
        return result;
    }

    private static double computeCardHeight(Dimension2D gameDimension2D, int nbLines) {
        return gameDimension2D.getHeight() * 0.9 / nbLines;
    }

    private static double computeCardWidth(Dimension2D gameDimension2D, int nbColumns) {
        return gameDimension2D.getWidth() / nbColumns;
    }

    private int getRandomValue(HashMap<Integer, Integer> indUsed) {
        int value;
        Random rdm = new Random();
        do {
            value = rdm.nextInt(images.size());
        } while ((!images.containsKey(value)) || (indUsed.containsKey(value) && (indUsed.get(value) == 2)));
        // While selected image is already used 2 times (if it appears )
        return value;
    }

    public int getnbRemainingPeers() {
        return nbRemainingPeers;
    }
}
