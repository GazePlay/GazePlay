package net.gazeplay.games.openmemory;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Slf4j
public class OpenMemory implements GameLifeCycle {

    private static final float cardRatio = 0.75f;

    private static final int minHeight = 30;

    @Data
    @AllArgsConstructor
    public class RoundDetails {
        public final List<OpenMemoryCard> cardList;
    }

    private int nbRemainingPeers;

    private final GameContext gameContext;

    private final int nbLines;
    private final int nbColumns;

    private final Stats stats;

    private Image[] imagesAvail;

    /*
     * HashMap of images selected for this game and their associated id The id is the same for the 2 same images
     */
    public HashMap<Integer, Image> images;

    public RoundDetails currentRoundDetails;

    public int nbTurnedCards;

    public OpenMemory(GameContext gameContext, int nbLines, int nbColumns, Stats stats) {
        super();
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

        this.imagesAvail = Utils.images(Utils.getImagesFolder() + "magiccards" + Utils.FILESEPARATOR);

        // If there is not enough images in the folder : complete with defaults images
        int nbImagesDebut = this.imagesAvail.length;
        if (nbImagesDebut < cardsCount / 2) {
            Image[] imagesAvail2 = this.imagesAvail;
            this.imagesAvail = new Image[cardsCount / 2];
            for (int i = 0; i < nbImagesDebut; i++) {
                this.imagesAvail[i] = imagesAvail2[i];
            }
            Image[] def = Utils.images(Utils.getImagesFolder() + "default" + Utils.FILESEPARATOR);
            for (int i = nbImagesDebut; i < cardsCount / 2; i++) {
                this.imagesAvail[i] = def[i - nbImagesDebut];
            }
        }

    }

    private HashMap<Integer, Image> selectionAleaImages() {
        ArrayList<Integer> indUsed = new ArrayList<Integer>(); // id already used
        int alea;
        final int cardsCount = nbColumns * nbLines;
        HashMap<Integer, Image> res = new HashMap<Integer, Image>();
        Random rdm = new Random();
        for (int i = 0; i < cardsCount / 2; i++) {
            do {
                alea = rdm.nextInt(imagesAvail.length);
            } while (indUsed.contains(alea));
            indUsed.add(alea);
            // We put 2 times the couple (image, id) in the hashmap
            res.put(i, imagesAvail[alea]);
            res.put(i, imagesAvail[alea]);
        }
        return res;
    }

    @Override
    public void launch() {
        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        final int cardsCount = nbColumns * nbLines;

        images = selectionAleaImages();

        List<OpenMemoryCard> cardList = createCards(images, config);

        nbRemainingPeers = cardsCount / 2;

        currentRoundDetails = new RoundDetails(cardList);

        gameContext.getChildren().addAll(cardList);

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

    public void removeSelectedCards() {
        if (this.currentRoundDetails == null) {
            return;
        }
        List<OpenMemoryCard> cardsToHide = new ArrayList<>();
        for (OpenMemoryCard pictureCard : this.currentRoundDetails.cardList) {
            if (pictureCard.isSelected()) {
                cardsToHide.add(pictureCard);
            }
        }
        nbRemainingPeers = nbRemainingPeers - 1;
        // remove all selected cards
        gameContext.getChildren().removeAll(cardsToHide);
    }

    private List<OpenMemoryCard> createCards(HashMap<Integer, Image> im, Configuration config) {
        javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        log.info("Width{} ; height{}", gameDimension2D.getWidth(), gameDimension2D.getHeight());

        final double cardHeight = computeCardHeight(gameDimension2D, nbLines);
        final double cardWidth = cardHeight * cardRatio;

        log.info("cardWidth{} ; cardHeight{}", cardWidth, cardHeight);

        double width = computeCardWidth(gameDimension2D, nbColumns) - cardWidth;

        log.info("width{} ", width);

        List<OpenMemoryCard> result = new ArrayList<>();

        // HashMap <index, number of times the index was used >
        HashMap<Integer, Integer> indUsed = new HashMap<Integer, Integer>();
        indUsed.clear();

        final int fixationlength = config.getFixationlength();

        for (int currentLineIndex = 0; currentLineIndex < nbLines; currentLineIndex++) {
            for (int currentColumnIndex = 0; currentColumnIndex < nbColumns; currentColumnIndex++) {

                double positionX = width / 2 + (width + cardWidth) * currentColumnIndex;
                double positionY = minHeight / 2 + (minHeight + cardHeight) * currentLineIndex;

                log.info("positionX : {} ; positionY : {}", positionX, positionY);

                int id = getRandomValue(indUsed);

                if (indUsed.containsKey(id)) {
                    indUsed.replace(id, 1, 2);
                } else {
                    indUsed.put(id, 1);
                }

                Image image = images.get(id);

                OpenMemoryCard card = new OpenMemoryCard(positionX, positionY, cardWidth, cardHeight, image, id,
                        gameContext, stats, this, fixationlength);

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
