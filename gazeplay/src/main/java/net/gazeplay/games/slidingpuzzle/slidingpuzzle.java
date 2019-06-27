/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.slidingpuzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.math101.Card;

/**
 *
 * @author Peter Bardawil
 * 
 */

@Slf4j
public class slidingpuzzle implements GameLifeCycle {

    @Data
    @AllArgsConstructor

    public static class RoundDetails {
        private final List<slidingpuzzlecard> cardList;
        private final int winnerImageIndexAmongDisplayedImages;
    }

    @Data
    @AllArgsConstructor
    public static class Coord {
        private final int x;
        private final int y;
    }

    public slidingpuzzle(Stats stats, GameContext gameContext, int nbLines, int nbColumns, int GameVariant) {
        this.gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.boxWidth = computeCardBoxWidth(gameDimension2D, nbColumns);
        this.boxHeight = computeCardBoxHeight(gameDimension2D, nbLines);
        this.cardHeight = (int) computeCardHeight(boxHeight);
        this.cardWidth = cardHeight;
        this.stats = stats;
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        if (GameVariant == 1)
            this.PicPath = "data/sliding-puzzle/tiles/tile";
        else if (GameVariant == 2)
            this.PicPath = "data/sliding-puzzle/monalisa/p";
        else if (GameVariant == 3)
            this.PicPath = "data/sliding-puzzle/fish/p";
        else if (GameVariant == 4)
            this.PicPath = "data/sliding-puzzle/biboule/p";

    }

    private static final float cardRatio = 0.8f;
    private static final float zoom_factor = 1.33f;
    private static final int minHeight = 30;

    private final Stats stats;

    private final GameContext gameContext;

    private final int nbLines;

    private final int nbColumns;

    private List<Coord> CoordList = new ArrayList<>();

    private Random randomGenerator;

    private final double boxHeight;
    private final double boxWidth;

    private final double cardHeight;
    private final double cardWidth;

    private String PicPath;

    private slidingpuzzle.RoundDetails currentRoundDetails;

    private javafx.geometry.Dimension2D gameDimension2D;

    @Override
    public void launch() {
        final Configuration config = Configuration.getInstance();
        slidingpuzzle.Coord c1 = new slidingpuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 1),
                (int) computePositionY(boxHeight, cardHeight, 1));
        CoordList.add(c1);
        slidingpuzzle.Coord c2 = new slidingpuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 2),
                (int) computePositionY(boxHeight, cardHeight, 1));
        CoordList.add(c2);
        slidingpuzzle.Coord c3 = new slidingpuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 3),
                (int) computePositionY(boxHeight, cardHeight, 1));
        CoordList.add(c3);
        slidingpuzzle.Coord c4 = new slidingpuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 1),
                (int) computePositionY(boxHeight, cardHeight, 2));
        CoordList.add(c4);
        slidingpuzzle.Coord c5 = new slidingpuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 2),
                (int) computePositionY(boxHeight, cardHeight, 2));
        CoordList.add(c5);
        slidingpuzzle.Coord c6 = new slidingpuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 3),
                (int) computePositionY(boxHeight, cardHeight, 2));
        CoordList.add(c6);
        slidingpuzzle.Coord c7 = new slidingpuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 1),
                (int) computePositionY(boxHeight, cardHeight, 3));
        CoordList.add(c7);
        slidingpuzzle.Coord c8 = new slidingpuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 2),
                (int) computePositionY(boxHeight, cardHeight, 3));
        CoordList.add(c8);
        slidingpuzzle.Coord c9 = new slidingpuzzle.Coord((int) computePositionX(boxWidth, cardWidth, 3),
                (int) computePositionY(boxHeight, cardHeight, 3));
        CoordList.add(c9);
        // Background Color
        Rectangle imageRectangle = new Rectangle(0, 0, gameDimension2D.getWidth(), gameDimension2D.getHeight());
        imageRectangle.widthProperty().bind(gameContext.getRoot().widthProperty());
        imageRectangle.heightProperty().bind(gameContext.getRoot().heightProperty());
        imageRectangle.setFill(Color.rgb(50, 50, 50));

        gameContext.getChildren().add(imageRectangle);

        List<slidingpuzzlecard> cardList = createCards(config);
        currentRoundDetails = new slidingpuzzle.RoundDetails(cardList, 1);

        gameContext.getChildren().addAll(cardList);

        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {
        // Collect all items to be removed from the User Interface
        List<slidingpuzzlecard> cardsToHide = new ArrayList<>();
        for (slidingpuzzlecard pictureCard : this.currentRoundDetails.cardList) {

            cardsToHide.add(pictureCard);

        }

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

        List<Integer> indexList = new ArrayList<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        indexList.add(4);
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        indexList.add(8);

        randomGenerator = new Random();

        final int fixationlength = config.getFixationLength();

        List<slidingpuzzlecard> result = new ArrayList<>();
        int counter = 1;
        // Initialize KingPos
        int index = randomGenerator.nextInt(CoordList.size());
        double kingPosX = CoordList.get(index).getX();
        double kingPosY = CoordList.get(index).getY();
        CoordList.remove(index);

        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {

                if (i == 3 && j == 3) {
                    slidingpuzzlecard card = new slidingpuzzlecard(counter, kingPosX, kingPosY, cardWidth, cardHeight,
                            PicPath + counter + ".png", fixationlength, gameContext, this, stats, kingPosX, kingPosY);
                    counter++;
                    card.setKing(true);
                    result.add(card);
                } else {

                    index = randomGenerator.nextInt(CoordList.size());

                    double positionX = CoordList.get(index).getX();
                    double positionY = CoordList.get(index).getY();
                    CoordList.remove(index);

                    slidingpuzzlecard card = new slidingpuzzlecard(counter, positionX, positionY, cardWidth, cardHeight,
                            PicPath + counter + ".png", fixationlength, gameContext, this, stats, kingPosX, kingPosY);
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

            slidingpuzzle.Coord c1 = new slidingpuzzle.Coord(pictureCard.getInitX(), pictureCard.getInitY());

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

        if (counter == 9)
            return true;
        else
            return false;

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
