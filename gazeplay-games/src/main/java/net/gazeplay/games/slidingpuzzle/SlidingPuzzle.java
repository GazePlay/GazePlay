package net.gazeplay.games.slidingpuzzle;

import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;

/**
 * Game "Puzzle" (sliding puzzle): a picture cut into tiles is shuffled, with one empty place; a fixation on a tile next
 * to the empty place slides it there. The round is won when the picture is rebuilt.
 *
 * @author Peter Bardawil
 */
@Slf4j
public class SlidingPuzzle implements GameLifeCycle {

    /**
     * The tiles of a round.
     */
    @Data
    @AllArgsConstructor
    private static class RoundDetails {
        private final List<SlidingPuzzleCard> cardList;
        private final int winnerImageIndexAmongDisplayedImages;
    }

    /**
     * A position of a tile.
     */
    @Data
    @AllArgsConstructor
    private static class Coord {
        private final int x;
        private final int y;
    }

    private final ReplayablePseudoRandom randomGenerator;

    SlidingPuzzle(final Stats stats, final IGameContext gameContext, final int nbLines, final int nbColumns, final EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant) {
        this.gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.boxWidth = computeCardBoxWidth(gameDimension2D, nbColumns);
        this.boxHeight = computeCardBoxHeight(gameDimension2D, nbLines);
        this.cardHeight = (int) computeCardHeight(boxHeight);
        this.cardWidth = cardHeight;
        this.stats = stats;
        this.gameContext = gameContext;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        this.picPath = gameVariant.getEnumValue().getResourcesPath();
    }

    SlidingPuzzle(final Stats stats, final IGameContext gameContext, final int nbLines, final int nbColumns, final EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant, double gameSeed) {
        this.gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.boxWidth = computeCardBoxWidth(gameDimension2D, nbColumns);
        this.boxHeight = computeCardBoxHeight(gameDimension2D, nbLines);
        this.cardHeight = (int) computeCardHeight(boxHeight);
        this.cardWidth = cardHeight;
        this.stats = stats;
        this.gameContext = gameContext;
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        this.picPath = gameVariant.getEnumValue().getResourcesPath();
    }

    private static final float zoom_factor = 1.33f;
    private static final int minHeight = 30;

    private final Stats stats;

    private final IGameContext gameContext;

    private final List<Coord> coordList = new ArrayList<>();

    private final double boxHeight;
    private final double boxWidth;

    private final double cardHeight;
    private final double cardWidth;

    private final String picPath;

    private SlidingPuzzle.RoundDetails currentRoundDetails;

    private final javafx.geometry.Dimension2D gameDimension2D;

    @Override
    public void launch() {
        final Configuration config = gameContext.getConfiguration();
        final SlidingPuzzle.Coord c1 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 1),
            (int) computePositionY(boxHeight, cardHeight, 1));
        coordList.add(c1);
        final SlidingPuzzle.Coord c2 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 2),
            (int) computePositionY(boxHeight, cardHeight, 1));
        coordList.add(c2);
        final SlidingPuzzle.Coord c3 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 3),
            (int) computePositionY(boxHeight, cardHeight, 1));
        coordList.add(c3);
        final SlidingPuzzle.Coord c4 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 1),
            (int) computePositionY(boxHeight, cardHeight, 2));
        coordList.add(c4);
        final SlidingPuzzle.Coord c5 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 2),
            (int) computePositionY(boxHeight, cardHeight, 2));
        coordList.add(c5);
        final SlidingPuzzle.Coord c6 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 3),
            (int) computePositionY(boxHeight, cardHeight, 2));
        coordList.add(c6);
        final SlidingPuzzle.Coord c7 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 1),
            (int) computePositionY(boxHeight, cardHeight, 3));
        coordList.add(c7);
        final SlidingPuzzle.Coord c8 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 2),
            (int) computePositionY(boxHeight, cardHeight, 3));
        coordList.add(c8);
        final SlidingPuzzle.Coord c9 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 3),
            (int) computePositionY(boxHeight, cardHeight, 3));
        coordList.add(c9);
        // Background Color
        final Rectangle imageRectangle = new Rectangle(0, 0, gameDimension2D.getWidth(), gameDimension2D.getHeight());
        imageRectangle.widthProperty().bind(gameContext.getRoot().widthProperty());
        imageRectangle.heightProperty().bind(gameContext.getRoot().heightProperty());
        imageRectangle.setFill(Color.rgb(50, 50, 50));

        gameContext.getChildren().add(imageRectangle);

        final List<SlidingPuzzleCard> cardList = createCards(config);
        currentRoundDetails = new SlidingPuzzle.RoundDetails(cardList, 1);

        gameContext.getChildren().addAll(cardList);

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.onGameStarted(2000);
    }

    @Override
    public void dispose() {
        // Collect all items to be removed from the User Interface
        final List<SlidingPuzzleCard> cardsToHide = new ArrayList<>(this.currentRoundDetails.cardList);

        // remove all at once, in order to update the UserInterface only once
        gameContext.getChildren().removeAll(cardsToHide);
    }

    /**
     * @return the height of the box of a tile
     */
    private static double computeCardBoxHeight(final Dimension2D gameDimension2D, final int nbLines) {
        return gameDimension2D.getHeight() / nbLines;
    }

    /**
     * @return the width of the box of a tile
     */
    private static double computeCardBoxWidth(final Dimension2D gameDimension2D, final int nbColumns) {
        return gameDimension2D.getWidth() / nbColumns;
    }

    /**
     * @return the height of a tile, not below a minimum
     */
    private static double computeCardHeight(final double boxHeight) {
        if ((boxHeight / zoom_factor) < minHeight) {
            return minHeight;
        } else {
            return boxHeight / zoom_factor;
        }
    }

    /**
     * @return the tiles of the picture, at shuffled places
     */
    private List<SlidingPuzzleCard> createCards(final Configuration config) {

        final int fixationlength = config.getFixationLength();

        final List<SlidingPuzzleCard> result = new ArrayList<>();
        int counter = 1;
        // Initialize KingPos
        int index = randomGenerator.nextInt(coordList.size());
        final double kingPosX = coordList.get(index).getX();
        final double kingPosY = coordList.get(index).getY();
        coordList.remove(index);

        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {

                if (i == 3 && j == 3) {
                    final SlidingPuzzleCard card = new SlidingPuzzleCard(counter, kingPosX, kingPosY, cardWidth, cardHeight,
                        picPath + counter + ".png", fixationlength, gameContext, this, stats, kingPosX, kingPosY);
                    counter++;
                    card.setKing(true);
                    result.add(card);
                } else {

                    index = randomGenerator.nextInt(coordList.size());

                    final double positionX = coordList.get(index).getX();
                    final double positionY = coordList.get(index).getY();
                    coordList.remove(index);

                    final SlidingPuzzleCard card = new SlidingPuzzleCard(counter, positionX, positionY, cardWidth, cardHeight,
                        picPath + counter + ".png", fixationlength, gameContext, this, stats, kingPosX, kingPosY);
                    counter++;

                    result.add(card);
                }
            }

        }

        return result;
    }

    /**
     * Records the new position of a tile moved, and of the empty place.
     */
    void fixCoord(final int id, final int initX, final int initY, final int kx, final int ky) {
        if (this.currentRoundDetails == null) {
            return;
        }
        for (final SlidingPuzzleCard pictureCard : this.currentRoundDetails.cardList) {
            if (pictureCard.getCardId() == 9) {
                pictureCard.setInitX(initX);
                pictureCard.setInitY(initY);
                pictureCard.setKingPosX(initX);
                pictureCard.setKingPosY(initY);

            }
            if (pictureCard.getCardId() == id) {
                pictureCard.setInitX(kx);
                pictureCard.setInitY(ky);
            }
            pictureCard.setKingPosX(initX);
            pictureCard.setKingPosY(initY);
        }

    }

    /**
     * Updates the tiles after a move: the tiles next to the new empty place become movable.
     */
    void replaceCards(final double fl, final int x, final int y, final int id) {

        if (this.currentRoundDetails == null) {
            return;
        }

        for (final SlidingPuzzleCard pictureCard : this.currentRoundDetails.cardList) {

            if (pictureCard.getCardId() == 9) {

                pictureCard.isKingCardEvent(x, y);
                pictureCard.setKingPosX(x);
                pictureCard.setKingPosY(y);

            }

        }

    }

    /**
     * @return whether each tile is at its place
     */
    boolean isGameOver() {
        int counter = 0;
        for (final SlidingPuzzleCard pictureCard : this.currentRoundDetails.cardList) {
            if (pictureCard.getCardId() == 1 && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 1)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 1)) {
                counter++;
            } else if (pictureCard.getCardId() == 2
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 2)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 1)) {
                counter++;
            } else if (pictureCard.getCardId() == 3
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 3)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 1)) {
                counter++;
            } else if (pictureCard.getCardId() == 4
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 1)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 2)) {
                counter++;
            } else if (pictureCard.getCardId() == 5
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 2)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 2)) {
                counter++;
            } else if (pictureCard.getCardId() == 6
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 3)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 2)) {
                counter++;
            } else if (pictureCard.getCardId() == 7
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 1)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 3)) {
                counter++;
            } else if (pictureCard.getCardId() == 8
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 2)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 3)) {
                counter++;
            } else if (pictureCard.getCardId() == 9
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 3)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 3)) {
                counter++;
            }
        }

        return counter == 9;

    }

    /**
     * @return the horizontal position of a tile of a column
     */
    private static double computePositionX(final double cardBoxWidth, final double cardWidth, final int colIndex) {
        return (cardBoxWidth / 3) + (colIndex * cardWidth);
    }

    /**
     * @return the vertical position of a tile of a line
     */
    private static double computePositionY(final double cardboxHeight, final double cardHeight, final int rowIndex) {
        return (rowIndex * cardHeight) - 60;
    }
}
