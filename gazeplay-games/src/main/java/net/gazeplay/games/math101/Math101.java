package net.gazeplay.games.math101;

import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by EL HUSSEINI Wafaa on 14/03/2019.
 */

@Slf4j
public class Math101 implements GameLifeCycle {

    private static final float cardRatio = 0.8f;

    private static final float zoom_factor = 1.16f;

    private static final int minHeight = 30;

    private final MathGameType gameType;

    private final IGameContext gameContext;

    private final int maxValue;

    private final Stats stats;

    private final int nbLines;

    private final int nbColumns;

    private final javafx.geometry.Dimension2D gameDimension2D;

    private RoundDetails currentRoundDetails;

    public Math101(final MathGameType gameType, IGameContext gameContext, MathGameVariant gameVariant, Stats stats) {
        super();
        this.gameType = gameType;
        this.gameContext = gameContext;
        this.maxValue = gameVariant.getVariableRange().getMax();
        this.stats = stats;
        this.nbLines = 2;
        this.nbColumns = 3;
        this.gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
    }

    private static Formula generateRandomFormula(final MathGameType gameType, final int maxValue) {
        final Random r = new Random();

        // choose numbers
        int number1 = r.nextInt(maxValue + 1);
        int number2 = r.nextInt(maxValue + 1);

        final MathOperation operator = gameType.chooseOperator();

        final int correctAnswer;
        switch (operator) {
            case PLUS:
                // operator is +
                correctAnswer = number1 + number2;
                break;
            case MULTIPLY:
                // operator is *
                correctAnswer = number1 * number2;
                break;
            case MINUS:
                // operator is -
                if (number2 > number1) {
                    // To make sure we only have positive answers
                    int temp = number2;
                    number2 = number1;
                    number1 = temp;
                }
                correctAnswer = number1 - number2;
                break;
            case DIVID:
                // operator is /
                while ((number2 == 0 && number1 == 0) || (number1 % number2 != 0)) {
                    // both cannot be 0
                    number1 = r.nextInt(maxValue + 1);
                    number2 = r.nextInt(maxValue + 1);

                    if (number2 > number1) {
                        int temp = number2;
                        number2 = number1;
                        number1 = temp;
                    }

                    if (number2 == 0) {
                        int temp = number2;
                        number2 = number1;
                        number1 = temp;
                    }

                }
                correctAnswer = number1 / number2;
                break;
            default:
                throw new UnsupportedOperationException("not implemented " + operator);
        }

        return Formula.builder()
            .number1(number1)
            .number2(number2)
            .operator(operator)
            .correctAnswer(correctAnswer)
            .build();
    }

    private Text createQuestionText(Formula formula) {
        // Create Question
        final Text question = new Text(formula.createFormulaString());
        question.setX(100);
        question.setY(200);
        question.setFont(new Font("Grinched", 36));
        question.setFont(new Font("Tsukushi A Round Gothic Bold", 120));
        question.setFill(Color.WHITE);
        // question.setfill(Color.WHITE);
        new Scene(new Group(question));
        question.applyCss();

        return question;
    }

    @Override
    public void launch() {
        final Formula formula = generateRandomFormula(gameType, maxValue);

        final Text question = createQuestionText(formula);

        // Background Color
        Rectangle imageRectangle = new Rectangle(0, 0, gameDimension2D.getWidth(), gameDimension2D.getHeight());
        imageRectangle.widthProperty().bind(gameContext.getRoot().widthProperty());
        imageRectangle.heightProperty().bind(gameContext.getRoot().heightProperty());
        imageRectangle.setFill(gameType.getBackgroundColor());

        int coef = (ActiveConfigurationContext.getInstance().isBackgroundWhite()) ? 1 : 0;
        imageRectangle.setOpacity(1 - coef * 0.9);
        gameContext.getChildren().add(imageRectangle);

        // Add biboule pictures
        double bibouleWidth = gameDimension2D.getHeight() / 2 + 50; // 370;
        double bibouleHeight = gameDimension2D.getHeight() / 2 - 50; // 280??
        double bibouleX = gameDimension2D.getWidth() - 50 - bibouleWidth;
        double bibouleY = 50;// gameDimension2D.getHeight() - 50;
        Rectangle bibouleRectangle = new Rectangle(bibouleX, bibouleY, bibouleWidth, bibouleHeight);
        bibouleRectangle.setFill(new ImagePattern(new Image("data/math101/images/biboule_hand.png"), 0, 0, 1, 1, true));
        gameContext.getChildren().add(bibouleRectangle);

        // Stack of blackboard
        StackPane stack = new StackPane();
        double boardWidth = gameDimension2D.getWidth() * 0.9 / 2;

        if ((boardWidth - 50) < question.getLayoutBounds().getWidth()) {
            boardWidth = question.getLayoutBounds().getWidth() + 50;
        }
        double boardHeight = gameDimension2D.getHeight() * 0.9 / 2 - 50;
        double boardX = (bibouleX - boardWidth) / 2;
        double boardY = gameDimension2D.getHeight() * 0.1 / 2;
        stack.setLayoutX(boardX);
        stack.setLayoutY(boardY);
        Rectangle boardRectangle = new Rectangle(boardX, boardY, boardWidth, boardHeight);
        boardRectangle.setFill(new ImagePattern(new Image("data/math101/images/blackboard.png"), 0, 0, 1, 1, true));

        final Random r = new Random();
        // Setup the question parameters
        final int cardsCount = 3;
        final int winnerCardIndex = r.nextInt(cardsCount); // index in the list between 0 and 2

        final Configuration config = ActiveConfigurationContext.getInstance();

        // Creating the cards
        List<Card> cardList = createCards(winnerCardIndex, formula.getCorrectAnswer(), config, formula.getOperator());
        currentRoundDetails = new RoundDetails(cardList, winnerCardIndex);

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

    void removeAllIncorrectCards() {
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

    private List<Card> createCards(int winnerCardIndex, int correctAnswer, Configuration config, MathOperation operator) {

        final double boxHeight = computeCardBoxHeight(gameDimension2D, nbLines);
        final double boxWidth = computeCardBoxWidth(gameDimension2D, nbColumns);

        final double cardHeight = computeCardHeight(boxHeight);
        final double cardWidth = computeCardWidth(cardHeight);

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
                    image = new Image("data/math101/images/correct2.png");

                } else {
                    Random r = new Random();

                    int tempCurrent = correctAnswer;

                    while (tempCurrent == correctAnswer || resultInt.contains(tempCurrent)) {
                        if ((operator.equals(MathOperation.MULTIPLY) || operator.equals(MathOperation.DIVID)) && (correctAnswer > maxValue)) {
                            tempCurrent = r.nextInt(2 * correctAnswer);
                        } else {
                            tempCurrent = r.nextInt(2 * maxValue);
                        }
                    }
                    resultInt.add(tempCurrent);

                    currentValue = tempCurrent;

                    isWinnerCard = false;
                    image = new Image("data/common/images/error.png");
                }

                double positionX = computePositionX(boxWidth, cardWidth, currentColumnIndex);
                double positionY = computePositionY(boxHeight, cardHeight, currentLineIndex);

                Card card = new Card(positionX, positionY, cardWidth, cardHeight, image, isWinnerCard, currentValue, gameContext, stats, this, fixationlength);

                result.add(card);
                currentCardIndex++;
            }
        }

        return result;
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

    private static double computeCardWidth(double cardHeight) {
        return cardHeight * cardRatio;
    }

    private static double computePositionX(double cardBoxWidth, double cardWidth, int colIndex) {
        return ((cardBoxWidth - cardWidth) / 2) + (colIndex * cardBoxWidth);
    }

    private static double computePositionY(double cardboxHeight, double cardHeight, int rowIndex) {
        return (cardboxHeight - cardHeight) / 2 + (rowIndex * cardboxHeight) / zoom_factor;
    }

}
