package net.gazeplay.games.math101;

import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by EL-Husseini Wafaa on 14/03/2019.
 */

@Slf4j
public class Math101 implements GameLifeCycle {

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
    private final int maxValue;
    private final int maxVariant0 = 8;
    private final int maxVariant1 = 12;
    private final int maxVariant2 = 20;

    private final Stats stats;

    // private final ImageLibrary imageLibrary;//needed???

    private Math101.RoundDetails currentRoundDetails;

    javafx.geometry.Dimension2D gameDimension2D;

    public Math101(GameContext gameContext, int maxValue, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.nbLines = 2;
        this.nbColumns = 3;
        this.stats = stats;

        if (maxValue == 0) {
            this.maxValue = maxVariant0;
        } else if (maxValue == 1) {
            this.maxValue = maxVariant1;
        } else {
            this.maxValue = maxVariant2;
        }

        gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Rectangle imageRectangle = new Rectangle(0, 0, gameDimension2D.getWidth(), gameDimension2D.getHeight());
        imageRectangle.widthProperty().bind(gameContext.getRoot().widthProperty());
        imageRectangle.heightProperty().bind(gameContext.getRoot().heightProperty());
        imageRectangle.setFill(Color.rgb(227, 255, 227));

        int coef = (Configuration.getInstance().isBackgroundWhite()) ? 1 : 0;
        imageRectangle.setOpacity(1 - coef * 0.9);

        gameContext.getChildren().add(imageRectangle);

    }

    @Override
    public void launch() {
        final Configuration config = Configuration.getInstance();
        // Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        // Setup the question parameters
        final int cardsCount = 3;

        Random r = new Random();
        final int winnerCardIndex = r.nextInt(cardsCount); // index in the list between 0 and 2

        int number1 = r.nextInt(maxValue + 1);
        int number2 = r.nextInt(maxValue + 1);

        int operator = r.nextInt(2);

        final int correctAnswer;
        final String operatorStr;

        if (operator == 0) {
            // operator is "+"
            operatorStr = "+";
            correctAnswer = number1 + number2;
        } else {
            // operator is "-"
            operatorStr = "-";
            if (number2 > number1) { // To make sure we only have positive answers
                int temp = number2;
                number2 = number1;
                number1 = temp;
            }

            correctAnswer = number1 - number2;
        }

        // Create Question
        Text question = createQuestion(number1, number2, operatorStr);
        question.setX(100);
        question.setY(200);
        question.setFont(new Font("Grinched", 36));
        question.setFont(new Font("Tsukushi A Round Gothic Bold", 120));
        question.setFill(Color.WHITE);
        // question.setfill(Color.WHITE);
        new Scene(new Group(question));
        question.applyCss();
        // make it white??
        // System.out.println("HEREEEEWAFAA: "+question.getLayoutBounds().getWidth());

        // Background Color
        Rectangle imageRectangle = new Rectangle(0, 0, gameDimension2D.getWidth(), gameDimension2D.getHeight());
        imageRectangle.widthProperty().bind(gameContext.getRoot().widthProperty());
        imageRectangle.heightProperty().bind(gameContext.getRoot().heightProperty());
        imageRectangle.setFill(Color.rgb(227, 255, 227));

        int coef = (Configuration.getInstance().isBackgroundWhite()) ? 1 : 0;
        imageRectangle.setOpacity(1 - coef * 0.9);
        gameContext.getChildren().add(imageRectangle);

        // Add biboule pictures
        double bibouleWidth = 370;
        double bibouleHeight = 280;// gameDimension2D.getHeight() - 50;
        double bibouleX = gameDimension2D.getWidth() - 50 - bibouleWidth;
        double bibouleY = 50;// gameDimension2D.getHeight() - 50;

        Rectangle bibouleRectangle = new Rectangle(bibouleX, bibouleY, bibouleWidth, bibouleHeight);
        bibouleRectangle.setFill(new ImagePattern(new Image("data/math101/images/biboule_hand.png"), 0, 0, 1, 1, true));

        gameContext.getChildren().add(bibouleRectangle);

        // Stack of blackboard
        StackPane stack = new StackPane();
        double boardWidth = gameDimension2D.getWidth() * 0.9 / 2;
        // System.out.println("boardWidth: "+boardWidth);
        // System.out.println("question.getWrappingWidth(): "+question.getWrappingWidth());

        if ((boardWidth - 50) < question.getLayoutBounds().getWidth()) {

            boardWidth = question.getLayoutBounds().getWidth() + 50;
        }

        // System.out.println("boardWidth updated: "+boardWidth);
        // System.out.println("question.getWrappingWidth() updated: "+question.getWrappingWidth());

        double boardHeight = gameDimension2D.getHeight() * 0.9 / 2;
        double boardX = (bibouleX - boardWidth) / 2;
        double boardY = gameDimension2D.getHeight() * 0.1 / 2;

        stack.setLayoutX(boardX);
        stack.setLayoutY(boardY);
        Rectangle boardRectangle = new Rectangle(boardX, boardY, boardWidth, boardHeight);
        boardRectangle.setFill(new ImagePattern(new Image("data/math101/images/blackboard.png"), 0, 0, 1, 1, true));

        // log.debug("WAFAA width {} ", correctAnswer);
        // System.out.println("WAFAA -- Okay so Test: "+correctAnswer);
        List<Card> cardList = createCards(winnerCardIndex, correctAnswer, config);

        currentRoundDetails = new Math101.RoundDetails(cardList, winnerCardIndex);

        // javafx.scene.text.Font.getFontNames();
        // Arrays.deepToString(javafx.scene.text.Font.getFontNames());

        // System.out.println("FONT NAMES "+Arrays.deepToString(javafx.scene.text.Font.getFontNames().toArray()));

        gameContext.getChildren().addAll(cardList);

        stack.getChildren().addAll(boardRectangle, question);

        gameContext.getChildren().add(stack);

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
        List<Card> cardsToHide = new ArrayList<>();
        for (Card pictureCard : this.currentRoundDetails.cardList) {
            if (!pictureCard.isWinner()) {
                cardsToHide.add(pictureCard);
            }
        }

        // remove all at once, in order to update the UserInterface only once
        gameContext.getChildren().removeAll(cardsToHide);
    }

    private List<Card> createCards(int winnerCardIndex, int correctAnswer, Configuration config) {

        log.debug("WAFAA Width {} ; height {}", gameDimension2D.getWidth(), gameDimension2D.getHeight());

        final double cardHeight = computeCardHeight(gameDimension2D, nbLines);
        final double cardWidth = cardHeight * cardRatio;

        log.debug("WAFAA cardWidth {} ; cardHeight {}", cardWidth, cardHeight);

        double width = computeCardWidth(gameDimension2D, nbColumns) - cardWidth;

        log.debug("WAFAA width {} ", width);

        List<Card> result = new ArrayList<>();
        List<Integer> resultInt = new ArrayList<>();
        resultInt.add(correctAnswer);
        int currentCardIndex = 0;

        final int fixationlength = config.getFixationLength();

        for (int currentLineIndex = 1; currentLineIndex < nbLines; currentLineIndex++) {
            for (int currentColumnIndex = 0; currentColumnIndex < nbColumns; currentColumnIndex++) {

                final boolean isWinnerCard;
                final Image image;
                final int currentValue;

                if (currentCardIndex == winnerCardIndex) {
                    isWinnerCard = true;
                    currentValue = correctAnswer;
                    image = new Image("data/math101/images/correct.png", (cardWidth * 0.85), (cardHeight * 0.85), true,
                            true);

                } else {
                    Random r = new Random();

                    int tempCurrent = correctAnswer;

                    while (tempCurrent == correctAnswer || resultInt.contains(tempCurrent)) {
                        tempCurrent = r.nextInt(2 * maxValue);
                    }
                    resultInt.add(tempCurrent);

                    currentValue = tempCurrent;

                    isWinnerCard = false;
                    image = new Image("data/common/images/error.png");
                }

                double positionX = width / 2 + (width + cardWidth) * currentColumnIndex;
                double positionY = minHeight / 2 + (minHeight + cardHeight) * currentLineIndex;

                log.debug("WAFAA positionX : {} ; positionY : {}", positionX, positionY);

                System.out.println("WAFAA -- Okay so Test X: " + positionX);
                System.out.println("WAFAA -- Okay so Test Y: " + positionY);

                Card card = new Card(positionX, positionY, cardWidth, cardHeight, image, isWinnerCard, currentValue,
                        gameContext, stats, this, fixationlength);

                result.add(card);
                currentCardIndex++;
            }
        }

        return result;
    }

    private Text createQuestion(int number1, int number2, String operator) {
        return new Text(number1 + " " + operator + " " + number2 + " = ? ");
    }

    private static double computeCardHeight(Dimension2D gameDimension2D, int nbLines) {
        return gameDimension2D.getHeight() * 0.9 / nbLines;
    }

    private static double computeCardWidth(Dimension2D gameDimension2D, int nbColumns) {
        return gameDimension2D.getWidth() / nbColumns;
    }

}
