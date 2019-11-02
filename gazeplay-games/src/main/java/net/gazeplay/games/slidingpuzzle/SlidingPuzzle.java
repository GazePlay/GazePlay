/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.slidingpuzzle;

import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Peter Bardawil
 */

@Slf4j
public class SlidingPuzzle implements GameLifeCycle {

    @Data
    @AllArgsConstructor
    private static class RoundDetails {
        private final List<slidingpuzzlecard> cardList;
        private final int winnerImageIndexAmongDisplayedImages;
    }

    @Data
    @AllArgsConstructor
    private static class Coord {
        private final int x;
        private final int y;
    }

    SlidingPuzzle(Stats stats, IGameContext gameContext, int nbLines, int nbColumns, GameSpec.EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant) {
        this.gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.boxWidth = computeCardBoxWidth(gameDimension2D, nbColumns);
        this.boxHeight = computeCardBoxHeight(gameDimension2D, nbLines);
        this.cardHeight = (int) computeCardHeight(boxHeight);
        this.cardWidth = cardHeight;
        this.stats = stats;
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;

        this.picPath = gameVariant.getEnumValue().getResourcesPath();
    }

    private static final float cardRatio = 0.8f;
    private static final float zoom_factor = 1.33f;
    private static final int minHeight = 30;

    private final Stats stats;

    private final IGameContext gameContext;

    private final int nbLines;

    private final int nbColumns;

    private List<Coord> coordList = new ArrayList<>();

    private Random randomGenerator;

    private final double boxHeight;
    private final double boxWidth;

    private final double cardHeight;
    private final double cardWidth;

    private String picPath;

    private SlidingPuzzle.RoundDetails currentRoundDetails;

    private javafx.geometry.Dimension2D gameDimension2D;

    @Override
    public void launch() {
        final Configuration config = ActiveConfigurationContext.getInstance();
        SlidingPuzzle.Coord c1 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 1),
            (int) computePositionY(boxHeight, cardHeight, 1));
        coordList.add(c1);
        SlidingPuzzle.Coord c2 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 2),
            (int) computePositionY(boxHeight, cardHeight, 1));
        coordList.add(c2);
        SlidingPuzzle.Coord c3 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 3),
            (int) computePositionY(boxHeight, cardHeight, 1));
        coordList.add(c3);
        SlidingPuzzle.Coord c4 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 1),
            (int) computePositionY(boxHeight, cardHeight, 2));
        coordList.add(c4);
        SlidingPuzzle.Coord c5 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 2),
            (int) computePositionY(boxHeight, cardHeight, 2));
        coordList.add(c5);
        SlidingPuzzle.Coord c6 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 3),
            (int) computePositionY(boxHeight, cardHeight, 2));
        coordList.add(c6);
        SlidingPuzzle.Coord c7 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 1),
            (int) computePositionY(boxHeight, cardHeight, 3));
        coordList.add(c7);
        SlidingPuzzle.Coord c8 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 2),
            (int) computePositionY(boxHeight, cardHeight, 3));
        coordList.add(c8);
        SlidingPuzzle.Coord c9 = new SlidingPuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 3),
            (int) computePositionY(boxHeight, cardHeight, 3));
        coordList.add(c9);
        // Background Color
        Rectangle imageRectangle = new Rectangle(0, 0, gameDimension2D.getWidth(), gameDimension2D.getHeight());
        imageRectangle.widthProperty().bind(gameContext.getRoot().widthProperty());
        imageRectangle.heightProperty().bind(gameContext.getRoot().heightProperty());
        imageRectangle.setFill(Color.rgb(50, 50, 50));

        gameContext.getChildren().add(imageRectangle);

        List<slidingpuzzlecard> cardList = createCards(config);
        currentRoundDetails = new SlidingPuzzle.RoundDetails(cardList, 1);

        gameContext.getChildren().addAll(cardList);

        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {
        // Collect all items to be removed from the User Interface
        List<slidingpuzzlecard> cardsToHide = new ArrayList<>(this.currentRoundDetails.cardList);

        // remove all at once, in order to update the UserInterface only once
        gameContext.getChildren().removeAll(cardsToHide);
    }

    private static double computeCardBoxHeight(Dimension2D gameDimension2D, int nbLines) {
        return gameDimension2D.getHeight() / nbLines;
    }

    private static double computeCardBoxWidth(Dimension2D gameDimension2D, int nbColumns) {
        return gameDimension2D.getWidth() / nbColumns;
    }

    private static double computeCardHeight(double boxHeight) {
        if ((boxHeight / zoom_factor) < minHeight) {
            return minHeight;
        } else {
            return boxHeight / zoom_factor;
        }
    }

    private List<slidingpuzzlecard> createCards(Configuration config) {
        randomGenerator = new Random();

        final int fixationlength = config.getFixationLength();

        List<slidingpuzzlecard> result = new ArrayList<>();
        int counter = 1;
        // Initialize KingPos
        int index = randomGenerator.nextInt(coordList.size());
        double kingPosX = coordList.get(index).getX();
        double kingPosY = coordList.get(index).getY();
        coordList.remove(index);

        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {

                if (i == 3 && j == 3) {
                    slidingpuzzlecard card = new slidingpuzzlecard(counter, kingPosX, kingPosY, cardWidth, cardHeight,
                        picPath + counter + ".png", fixationlength, gameContext, this, stats, kingPosX, kingPosY);
                    counter++;
                    card.setKing(true);
                    result.add(card);
                } else {

                    index = randomGenerator.nextInt(coordList.size());

                    double positionX = coordList.get(index).getX();
                    double positionY = coordList.get(index).getY();
                    coordList.remove(index);

                    slidingpuzzlecard card = new slidingpuzzlecard(counter, positionX, positionY, cardWidth, cardHeight,
                        picPath + counter + ".png", fixationlength, gameContext, this, stats, kingPosX, kingPosY);
                    counter++;

                    result.add(card);
                }
            }

        }

        return result;
    }

    void fixCoord(int id, int initX, int initY, int kx, int ky) {
        if (this.currentRoundDetails == null) {
            return;
        }
        for (slidingpuzzlecard pictureCard : this.currentRoundDetails.cardList) {
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

    void replaceCards(double fl, int x, int y, int id) {

        if (this.currentRoundDetails == null) {
            return;
        }

        for (slidingpuzzlecard pictureCard : this.currentRoundDetails.cardList) {

            if (pictureCard.getCardId() == 9) {

                pictureCard.isKingCardEvent(x, y);
                pictureCard.setKingPosX(x);
                pictureCard.setKingPosY(y);

            }

        }

    }

    void showCards() {

        for (slidingpuzzlecard pictureCard : this.currentRoundDetails.cardList) {
            log.info("index :" + pictureCard.getCardId());
            log.info("x Coordinate :" + pictureCard.getInitX());
            log.info("Y Coordinate :" + pictureCard.getInitY());
            log.info("KX Coordinate :" + pictureCard.getKingPosX());
            log.info("KY Coordinate :" + pictureCard.getKingPosY());
        }
    }

    boolean isGameOver() {
        int counter = 0;
        for (slidingpuzzlecard pictureCard : this.currentRoundDetails.cardList) {

            SlidingPuzzle.Coord c1 = new SlidingPuzzle.Coord(pictureCard.getInitX(), pictureCard.getInitY());

            if (pictureCard.getCardId() == 1 && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 1)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 1))
                counter++;
            else if (pictureCard.getCardId() == 2
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 2)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 1))
                counter++;
            else if (pictureCard.getCardId() == 3
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 3)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 1))
                counter++;
            else if (pictureCard.getCardId() == 4
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 1)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 2))
                counter++;
            else if (pictureCard.getCardId() == 5
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 2)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 2))
                counter++;
            else if (pictureCard.getCardId() == 6
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 3)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 2))
                counter++;
            else if (pictureCard.getCardId() == 7
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 1)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 3))
                counter++;
            else if (pictureCard.getCardId() == 8
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 2)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 3))
                counter++;
            else if (pictureCard.getCardId() == 9
                && pictureCard.getInitX() == (int) computePositionX(boxWidth, cardWidth, 3)
                && pictureCard.getInitY() == (int) computePositionY(boxHeight, cardHeight, 3))
                counter++;
        }

        return counter == 9;

    }

    private static double computeCardWidth(double cardHeight) {
        return cardHeight * cardRatio;
    }

    private static double computePositionX(double cardBoxWidth, double cardWidth, int colIndex) {
        return (cardBoxWidth / 3) + (colIndex * cardWidth);
    }

    private static double computePositionY(double cardboxHeight, double cardHeight, int rowIndex) {
        return (rowIndex * cardHeight) - 60;
    }
}
