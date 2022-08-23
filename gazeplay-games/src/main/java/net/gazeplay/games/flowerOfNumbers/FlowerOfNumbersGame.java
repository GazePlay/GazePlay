package net.gazeplay.games.flowerOfNumbers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.multilinguism.Languages;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.ProgressButton;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Locale;

@Slf4j
public class FlowerOfNumbersGame implements GameLifeCycle {
    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom random;

    private final ReadOnlyDoubleProperty widthProperty;
    private final ReadOnlyDoubleProperty heightProperty;

    private final Configuration config;
    private final Multilinguism multilinguism;
    private final Font font = new Font(null, Font.PLAIN, 100);
    private final int maxNumberSize;
    private final int maxDigitSize;
    private final int maxWordSize;

    private final Flower flower;
    private int oldPistilValue;

    private final int indexPistil;
    private final int startIndexAuras;
    private final int startIndexNumbers;
    private final int startIndexOthers;

    public FlowerOfNumbersGame(final IGameContext gameContext, final Stats stats) {
        this(gameContext, stats, -1);
    }

    public FlowerOfNumbersGame(final IGameContext gameContext, final Stats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;

        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            this.stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }

        config = gameContext.getConfiguration();
        multilinguism = MultilinguismFactory.getForResource("data/flowerOfNumbers/numbers.csv");
        maxNumberSize = calculateMaxIntegerSize(Flower.NUMBER_LIMIT);
        maxDigitSize = calculateMaxIntegerSize(Petal.DIGITS.getNumberOfObjects());
        maxWordSize = calculateMaxWordSize();

        widthProperty = gameContext.getRoot().widthProperty();
        heightProperty = gameContext.getRoot().heightProperty();

        flower = new Flower();
        flower.init();
        oldPistilValue = flower.getPistil();

        createFlower();
        indexPistil = createPistil();
        startIndexAuras = createAura();
        startIndexNumbers = createNumbers();
        startIndexOthers = gameContext.getChildren().size();
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();

        flower.setPistil(choosePistilValue());
        log.info("value of pistil = {}", flower.getPistil());

        final Image image = createImageFromText(String.valueOf(flower.getPistil()), maxNumberSize);
        final Rectangle pistil = (Rectangle) gameContext.getChildren().get(indexPistil);
        pistil.setFill(new ImagePattern(image));

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose() {
        flower.init();
        for (Petal petal : Petal.values()) {
            gameContext.getChildren().get(startIndexAuras + petal.ordinal()).setVisible(false);
            checkIfPetalIsFull(petal);
        }
        if (gameContext.getChildren().size() > startIndexOthers) {
            gameContext.getChildren().subList(startIndexOthers, gameContext.getChildren().size()).clear();
        }
    }

    private void createFlower() {
        final Image image = new Image("data/flowerOfNumbers/images/flower.png");
        final Rectangle flower = createRectangle(image);
        gameContext.getChildren().add(flower);
        log.info("flower background create successfully");
    }

    private int createPistil() {
        final int res = gameContext.getChildren().size();
        final Image image = createImageFromText(String.valueOf(0), maxNumberSize);
        final Rectangle pistil = new Rectangle(0, 0, 10, 10);
        pistil.setFill(new ImagePattern(image));
        pistil.setMouseTransparent(true);
        pistil.widthProperty().bind(widthProperty.multiply(0.09));
        pistil.heightProperty().bind(widthProperty.multiply(0.09).multiply(image.getHeight()).divide(image.getWidth()));
        pistil.layoutXProperty().bind(widthProperty.multiply(0.50).subtract(pistil.widthProperty().divide(2)));
        pistil.layoutYProperty().bind(heightProperty.multiply(0.50).subtract(pistil.heightProperty().divide(2)));
        gameContext.getChildren().add(pistil);
        log.info("pistil value label create successfully");
        return res;
    }

    private int createAura() {
        final int res = gameContext.getChildren().size();
        for (Petal petal : Petal.values()) {
            final Image image = new Image("data/flowerOfNumbers/images/auras/" + petal + ".png");
            final Rectangle aura = createRectangle(image);
            aura.setVisible(false);
            gameContext.getChildren().add(aura);
        }
        log.info("{} aura images create successfully", Petal.values().length);
        return res;
    }

    private Rectangle createRectangle(final Image image) {
        final Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        rectangle.setFill(new ImagePattern(image));
        rectangle.setMouseTransparent(true);
        rectangle.widthProperty().bind(heightProperty);
        rectangle.heightProperty().bind(heightProperty);
        rectangle.xProperty().bind(widthProperty.subtract(heightProperty).divide(2));
        return rectangle;
    }

    private int createNumbers() {
        final int res = gameContext.getChildren().size();

        createButton(Petal.DIGITS, 1, 0.035, 0.74, 0.10);
        for (int i = 0; i < Petal.DIGITS.getNumberOfObjects() / 2; i++) {
            createButton(Petal.DIGITS, i + 2, 0.035, 0.79 + i * 0.05, 0.10);
            createButton(Petal.DIGITS, i + 6, 0.035, 0.79 + i * 0.05, 0.23);
        }
        log.info("{} digit objects create successfully", Petal.DIGITS.getNumberOfObjects());

        for (int i = 0; i < Petal.WORDS.getNumberOfObjects() / 2; i++) {
            createButton(Petal.WORDS, i + 1, 0.11, 0.06, 0.06 + i * 0.05);
            createButton(Petal.WORDS, i + 11, 0.11, 0.16, 0.06 + i * 0.05);
        }
        log.info("{} word objects create successfully", Petal.WORDS.getNumberOfObjects());

        createButton(Petal.FINGERS, 1, 0.0295, 0.04, 0.67);
        createButton(Petal.FINGERS, 2, 0.0295, 0.09, 0.67);
        createButton(Petal.FINGERS, 3, 0.0343, 0.14, 0.67);
        createButton(Petal.FINGERS, 4, 0.0381, 0.19, 0.67);
        createButton(Petal.FINGERS, 5, 0.0500, 0.24, 0.67);
        log.info("{} fingers objects create successfully", Petal.FINGERS.getNumberOfObjects());

        for (int i = 0; i < Petal.DICE.getNumberOfObjects() / 2; i++) {
            createButton(Petal.DICE, i + 1, 0.05, 0.79 + i * 0.07, 0.72);
            createButton(Petal.DICE, i + 4, 0.05, 0.79 + i * 0.07, 0.85);
        }
        log.info("{} die objects create successfully", Petal.DICE.getNumberOfObjects());

        createButton(Petal.MONEY, 1, 0.050, 0.82, 0.42);
        createButton(Petal.MONEY, 2, 0.050, 0.92, 0.42);
        createButton(Petal.MONEY, 5, 0.083, 0.82, 0.54);
        createButton(Petal.MONEY, 10, 0.085, 0.92, 0.54);
        log.info("{} money objects create successfully", Petal.MONEY.getNumberOfObjects());

        return res;
    }

    private void createButton(final Petal petal, final int value, final double size, final double x, final double y) {
        final Image image = switch (petal) {
            case DIGITS -> createImageFromText(String.valueOf(value), maxDigitSize);
            case WORDS -> createImageFromText(multilinguism.getTranslation(String.valueOf(value), config.getLanguage()), maxWordSize);
            case FINGERS -> new Image("data/flowerOfNumbers/images/fingers/fingers" + value + ".png");
            case DICE -> new Image("data/flowerOfNumbers/images/dice/die" + value + ".png");
            case MONEY -> new Image("data/flowerOfNumbers/images/money/money" + value + ".png");
        };

        final ImageView view = new ImageView(image);
        final DoubleProperty minSizeProperty = image.getWidth() <= image.getHeight() ? view.fitWidthProperty() : view.fitHeightProperty();

        final ProgressButton button = new ProgressButton(false);
        button.setImage(view);
        button.getButton().setVisible(false);

        view.fitWidthProperty().bind(widthProperty.multiply(size));
        view.fitHeightProperty().bind(widthProperty.multiply(size).multiply(image.getHeight()).divide(image.getWidth()));
        button.layoutXProperty().bind(widthProperty.multiply(x).subtract(view.fitWidthProperty().divide(2)));
        button.layoutYProperty().bind(heightProperty.multiply(y).subtract(view.fitHeightProperty().divide(2)));
        button.getButton().radiusProperty().bind(minSizeProperty.divide(2));

        gameContext.getChildren().add(button);

        button.assignIndicatorUpdatable(buttonEvent -> {
            final int index = flower.getIndexFirstEmptyCell(petal);
            flower.add(petal, index, value);
            log.info("add value {} to {} petal", value, petal);
            checkIfPetalIsFull(petal);
            checkIfFlowerIsComplete(true, false);

            final ImageView newView = new ImageView(image);

            final ProgressButton newButton = new ProgressButton(false);
            newButton.setImage(newView);
            newButton.getButton().setVisible(false);
            newButton.setDisable(true);
            newButton.getButton().setDisable(true);

            newView.fitWidthProperty().bind(widthProperty.multiply(size));
            newView.fitHeightProperty().bind(widthProperty.multiply(size).multiply(image.getHeight()).divide(image.getWidth()));
            newButton.setLayoutX(button.getLayoutX());
            newButton.setLayoutY(button.getLayoutY());
            newButton.getButton().radiusProperty().bind(minSizeProperty.divide(2));

            gameContext.getChildren().add(newButton);

            final Pair<Double, Double> pair = calculateCoordinates(petal, index);
            final double newX = pair.getKey();
            final double newY = pair.getValue();
            final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();

            final KeyValue xKeyValue = new KeyValue(newButton.layoutXProperty(), dimension.getWidth() * newX - newView.getFitWidth() / 2, Interpolator.EASE_OUT);
            final KeyValue yKeyValue = new KeyValue(newButton.layoutYProperty(), dimension.getHeight() * newY - newView.getFitHeight() / 2, Interpolator.EASE_OUT);
            final KeyFrame keyFrame = new KeyFrame(new Duration(1000), xKeyValue, yKeyValue);
            final Timeline timeline = new Timeline(keyFrame);

            timeline.setOnFinished(timelineEvent -> {
                newButton.layoutXProperty().bind(widthProperty.multiply(newX).subtract(newView.fitWidthProperty().divide(2)));
                newButton.layoutYProperty().bind(heightProperty.multiply(newY).subtract(newView.fitHeightProperty().divide(2)));
                stats.takeScreenShot();

                checkIfPetalIsComplete(petal);
                if (!checkIfFlowerIsComplete(false, true)) {
                    newButton.getButton().setDisable(false);
                    newButton.setDisable(false);

                    newButton.assignIndicatorUpdatable(newButtonEvent -> {
                        log.info("remove value {} of {} petal", flower.get(petal, index), petal);
                        flower.remove(petal, index);
                        checkIfPetalIsFull(petal);
                        checkIfPetalIsComplete(petal);
                        checkIfFlowerIsComplete(true, true);

                        gameContext.getGazeDeviceManager().removeEventFilter(newButton);
                        gameContext.getChildren().remove(newButton);
                        stats.takeScreenShot();
                    }, gameContext);
                    gameContext.getGazeDeviceManager().addEventFilter(newButton);
                    newButton.active();
                }
            });

            timeline.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
            timeline.play();
        }, gameContext);
        gameContext.getGazeDeviceManager().addEventFilter(button);
        button.active();
    }

    private int choosePistilValue() {
        int newPistilValue = oldPistilValue;
        while (newPistilValue == oldPistilValue) {
            newPistilValue = random.nextInt(Flower.NUMBER_LIMIT) + 1;
        }
        oldPistilValue = newPistilValue;
        return newPistilValue;
    }

    private void checkIfPetalIsFull(final Petal petal) {
        final int startIndex = startIndexNumbers + Petal.getTotalNumberOfObjectsBeforePetal(petal);
        final int endIndex = startIndex + petal.getNumberOfObjects();
        final boolean isFull = flower.petalIsFull(petal);
        for (int index = startIndex; index < endIndex; index++) {
            final ProgressButton button = (ProgressButton) gameContext.getChildren().get(index);
            button.setDisable(isFull);
            button.getButton().setDisable(isFull);
        }
        if (isFull) {
            log.info("{} petal is full", petal);
        }
    }

    private void checkIfPetalIsComplete(final Petal petal) {
        final boolean isComplete = flower.petalIsComplete(petal);
        final Rectangle aura = (Rectangle) gameContext.getChildren().get(startIndexAuras + petal.ordinal());
        aura.setVisible(isComplete);
        if (isComplete) {
            log.info("{} petal is complete", petal);
        }
    }

    private boolean checkIfFlowerIsComplete(final boolean disable, final boolean win) {
        if (flower.isComplete()) {
            if (disable) {
                for (Node node : gameContext.getChildren().subList(startIndexNumbers, gameContext.getChildren().size())) {
                    if (node instanceof ProgressButton progressButton) {
                        progressButton.setDisable(true);
                        progressButton.getButton().setDisable(true);
                    }
                }
            }
            if (win) {
                log.info("flower is complete: WIN");
                stats.incrementNumberOfGoalsReached();
                gameContext.updateScore(stats, this);
                gameContext.playWinTransition(500, winEvent -> {
                    if (!gameContext.getChildren().isEmpty()) {
                        dispose();
                        launch();
                    }
                });
            }
            return true;
        }
        return false;
    }

    private Pair<Double, Double> calculateCoordinates(final Petal petal, final int index) {
        return switch (petal) {
            case DIGITS -> switch (index) {
                case 0 -> new Pair<>(0.47, 0.11);
                case 1 -> new Pair<>(0.53, 0.11);
                case 2 -> new Pair<>(0.47, 0.25);
                case 3 -> new Pair<>(0.53, 0.25);
                default -> null;
            };
            case WORDS -> index == 0 ? new Pair<>(0.34, 0.40) : null;
            case FINGERS -> switch (index) {
                case 0 -> new Pair<>(0.37, 0.69);
                case 1 -> new Pair<>(0.44, 0.69);
                case 2 -> new Pair<>(0.35, 0.83);
                case 3 -> new Pair<>(0.42, 0.83);
                default -> null;
            };
            case DICE -> switch (index) {
                case 0 -> new Pair<>(0.555, 0.70);
                case 1 -> new Pair<>(0.625, 0.70);
                case 2 -> new Pair<>(0.575, 0.83);
                case 3 -> new Pair<>(0.645, 0.83);
                default -> null;
            };
            case MONEY -> switch (index) {
                case 0 -> new Pair<>(0.61, 0.38);
                case 1 -> new Pair<>(0.70, 0.33);
                case 2 -> new Pair<>(0.62, 0.49);
                case 3 -> new Pair<>(0.71, 0.43);
                default -> null;
            };
        };
    }

    private Image createImageFromText(final String text, final int maxTextWidth) {
        final Color color = config.getBackgroundStyle().accept(new BackgroundStyleVisitor<>() {
            @Override
            public Color visitLight() {
                return Color.BLACK;
            }

            @Override
            public Color visitDark() {
                return Color.WHITE;
            }
        });

        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        final int textWidth = fm.stringWidth(text);
        final int textHeight = fm.getHeight();
        g2d.dispose();

        final int finalMaxTextWidth = maxTextWidth < 0 ? textWidth : maxTextWidth;

        image = new BufferedImage(finalMaxTextWidth, textHeight, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(color);
        g2d.drawString(text, (finalMaxTextWidth - textWidth) / 2, fm.getAscent());
        g2d.dispose();

        return SwingFXUtils.toFXImage(image, null);
    }

    private int calculateMaxIntegerSize(final int endValue) {
        final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        final FontMetrics fm = g2d.getFontMetrics();

        int maxSize = -1;
        for (int value = 1; value <= endValue; value++) {
            final int curSize = fm.stringWidth(String.valueOf(value));
            if (curSize > maxSize) {
                maxSize = curSize;
            }
        }
        g2d.dispose();
        return maxSize;
    }

    private int calculateMaxWordSize() {
        final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        final FontMetrics fm = g2d.getFontMetrics();

        int maxSize = -1;
        for (Locale locale : Languages.getAllCodes()) {
            String language = locale.getLanguage();
            for (int value = 1; value <= Petal.WORDS.getNumberOfObjects(); value++) {
                final int curSize = fm.stringWidth(multilinguism.getTranslation(String.valueOf(value), language));
                if (curSize > maxSize) {
                    maxSize = curSize;
                }
            }
        }
        g2d.dispose();
        return maxSize;
    }
}
