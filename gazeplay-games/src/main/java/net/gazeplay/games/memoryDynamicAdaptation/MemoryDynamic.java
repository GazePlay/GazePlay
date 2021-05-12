package net.gazeplay.games.memoryDynamicAdaptation;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.*;

@Slf4j
public class MemoryDynamic implements GameLifeCycle {

    private static final float cardRatio = 0.75f;

    private static final int minHeight = 30;

    public enum MemoryGameType {

        LETTERS, NUMBERS, DEFAULT
    }

    @Data
    @AllArgsConstructor
    public static class RoundDetails {
        public final List<MemoryCardDynamic> cardList;
    }

    private int nbRemainingPeers;

    private final IGameContext gameContext;

    private int nbLines;
    private int nbColumns;

    private final Stats stats;

    private ImageLibrary imageLibrary;

    /*
     * HashMap of images selected for this game and their associated id The id is the same for the 2 same images
     */
    public HashMap<Integer, Image> images;

    public RoundDetails currentRoundDetails;

    public int nbTurnedCards;

    private final boolean isOpen;

    private final ReplayablePseudoRandom randomGenerator;

    private int nbWrongCards;

    private int nbCorrectCards;

    private List<Integer> listOfResults = new ArrayList<>();

    private int level = 2;



    public MemoryDynamic(final MemoryGameType gameType, final IGameContext gameContext, final int nbLines, final int nbColumns, final Stats stats,
                  final boolean isOpen) {
        super();
        this.isOpen = isOpen;
        final int cardsCount = nbLines * nbColumns;
        if ((cardsCount & 1) != 0) {
            // nbLines * nbColumns must be a multiple of 2
            throw new IllegalArgumentException("Cards count must be an even number in this game");
        }
        this.nbRemainingPeers = (nbLines * nbColumns) / 2;
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.stats = stats;
        this.nbCorrectCards = 0;
        this.nbWrongCards = 0;
        this.gameContext.startTimeLimiter();

        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        if (gameType == MemoryGameType.LETTERS) {

            this.imageLibrary = ImageUtils.createCustomizedImageLibrary(null, "common/letters", randomGenerator);

        } else if (gameType == MemoryGameType.NUMBERS) {

            this.imageLibrary = ImageUtils.createCustomizedImageLibrary(null, "common/numbers", randomGenerator);

        } else {
            this.imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("magiccards"),
                Utils.getImagesSubdirectory("default"), randomGenerator);
        }

    }

    public MemoryDynamic(final MemoryGameType gameType, final IGameContext gameContext, final int nbLines, final int nbColumns, final Stats stats,
                  final boolean isOpen, double gameSeed) {
        super();
        this.isOpen = isOpen;
        final int cardsCount = nbLines * nbColumns;
        if ((cardsCount & 1) != 0) {
            // nbLines * nbColumns must be a multiple of 2
            throw new IllegalArgumentException("Cards count must be an even number in this game");
        }
        this.nbRemainingPeers = (nbLines * nbColumns) / 2;
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.stats = stats;
        this.nbCorrectCards = 0;
        this.nbWrongCards = 0;
        this.gameContext.startTimeLimiter();

        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        if (gameType == MemoryGameType.LETTERS) {

            this.imageLibrary = ImageUtils.createCustomizedImageLibrary(null, "common/letters", randomGenerator);

        } else if (gameType == MemoryGameType.NUMBERS) {

            this.imageLibrary = ImageUtils.createCustomizedImageLibrary(null, "common/numbers", randomGenerator);

        } else {
            this.imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("magiccards"),
                Utils.getImagesSubdirectory("default"), randomGenerator);
        }

        gameContext.start();

    }

    HashMap<Integer, Image> pickRandomImages() {
        final int cardsCount = nbColumns * nbLines;
        final HashMap<Integer, Image> res = new HashMap<>();

        final Set<Image> images = imageLibrary.pickMultipleRandomDistinctImages(cardsCount / 2);

        int i = 0;
        for (final Image image : images) {
            res.put(i, image);
            i++;
        }
        return res;
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        final Configuration config = gameContext.getConfiguration();
        final int cardsCount = nbColumns * nbLines;

        images = pickRandomImages();

        final List<MemoryCardDynamic> cardList = createCards(images, config);

        nbRemainingPeers = cardsCount / 2;

        currentRoundDetails = new RoundDetails(cardList);

        gameContext.getChildren().addAll(cardList);

        stats.notifyNewRoundReady();

        gameContext.getGazeDeviceManager().addStats(stats);
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
        final List<MemoryCardDynamic> cardsToHide = new ArrayList<>();
        for (final MemoryCardDynamic pictureCard : this.currentRoundDetails.cardList) {
            if (pictureCard.isTurned()) {
                cardsToHide.add(pictureCard);
            }
        }
        nbRemainingPeers = nbRemainingPeers - 1;
        // remove all turned cards
        gameContext.getChildren().removeAll(cardsToHide);
    }

    private List<MemoryCardDynamic> createCards(final HashMap<Integer, Image> im, final Configuration config) {
        final javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        log.debug("Width {} ; height {}", gameDimension2D.getWidth(), gameDimension2D.getHeight());

        final double cardHeight = computeCardHeight(gameDimension2D, nbLines);
        final double cardWidth = cardHeight * cardRatio;

        log.debug("cardWidth {} ; cardHeight {}", cardWidth, cardHeight);

        final double width = computeCardWidth(gameDimension2D, nbColumns) - cardWidth;

        log.debug("width {} ", width);

        final List<MemoryCardDynamic> result = new ArrayList<>();

        // HashMap <index, number of times the index was used >
        final HashMap<Integer, Integer> indUsed = new HashMap<>();
        indUsed.clear();

        final int fixationlength = config.getFixationLength();

        for (int currentLineIndex = 0; currentLineIndex < nbLines; currentLineIndex++) {
            for (int currentColumnIndex = 0; currentColumnIndex < nbColumns; currentColumnIndex++) {

                final double positionX = width / 2d + (width + cardWidth) * currentColumnIndex;
                final double positionY = minHeight / 2d + (minHeight + cardHeight) * currentLineIndex;

                log.debug("positionX : {} ; positionY : {}", positionX, positionY);

                final int id = getRandomValue(indUsed);

                if (indUsed.containsKey(id)) {
                    indUsed.replace(id, 1, 2);
                } else {
                    indUsed.put(id, 1);
                }

                final Image image = images.get(id);

                final MemoryCardDynamic card = new MemoryCardDynamic(positionX, positionY, cardWidth, cardHeight, image, id, gameContext,
                    stats, this, fixationlength, isOpen);

                result.add(card);
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

    private int getRandomValue(final HashMap<Integer, Integer> indUsed) {
        int value;
        do {
            value = randomGenerator.nextInt(images.size());
        } while ((!images.containsKey(value)) || (indUsed.containsKey(value) && (indUsed.get(value) == 2)));
        // While selected image is already used 2 times (if it appears )
        return value;
    }

    public int getnbRemainingPeers() {
        return nbRemainingPeers;
    }

    //public int getNbLines() { return nbLines; }

    public int getNbColumns() { return nbColumns; }

    public void setNbColumns(int nbColumns) { this.nbColumns = nbColumns; }

    public int getNbLines() { return nbLines; }

    public void setNbLines(int nbLines) { this.nbLines = nbLines; }

    public int getNbWrongCards() { return  nbWrongCards; }

    public void incNbWrongCards() {nbWrongCards++; }

    public void resetNbWrongCards() { nbWrongCards = 0; }

    public int getNbCorrectCards() { return  nbCorrectCards; }

    public void incNbCorrectCards() {nbCorrectCards++; }

    public void resetNbCorrectCards() { nbCorrectCards = 0; }

    public int totalNbOfTries() { return getNbCorrectCards() + getNbWrongCards(); }

    public List<Integer> getListOfResults() {
        return listOfResults;
    }

    public void addRoundResult(int lastRoundResult) {
        listOfResults.add(lastRoundResult);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void adaptLevel() {

        if (level == 6) {
            setNbColumns(4);
            setNbLines(3);
        }

        else if (level == 7) {
            setNbColumns(4);
            setNbLines(4);
            level++;
        }

        else if (level == 8) {
            setNbColumns(4);
            setNbLines(4);
        }

        else if (level == 9) {
            setNbColumns(6);
            setNbLines(3);
        }

        else if (level == 10) {
            setNbColumns(5);
            setNbLines(4);
        }

        else {
            setNbColumns(level);
            setNbLines(2);
        }

    }



}
