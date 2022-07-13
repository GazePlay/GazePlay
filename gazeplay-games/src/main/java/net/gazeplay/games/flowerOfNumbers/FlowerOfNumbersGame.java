package net.gazeplay.games.flowerOfNumbers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyle;
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
    private final int maxDigitSize;
    private final int maxWordSize;

    private final Rectangle backgroundImage;

    private final Flower flower;

    FlowerOfNumbersGame(final IGameContext gameContext, final Stats stats) {
        this(gameContext, stats, -1);
    }

    FlowerOfNumbersGame(final IGameContext gameContext, final Stats stats, double gameSeed) {
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
        maxDigitSize = calculateMaxDigitSize();
        maxWordSize = calculateMaxWordSize();

        widthProperty = gameContext.getRoot().widthProperty();
        heightProperty = gameContext.getRoot().heightProperty();

        backgroundImage = new Rectangle(0, 0, 10, 10);
        backgroundImage.setFill(new ImagePattern(new Image("data/flowerOfNumbers/images/flower.png")));
        backgroundImage.setMouseTransparent(true);
        backgroundImage.xProperty().bind(widthProperty.subtract(heightProperty).divide(2));
        backgroundImage.widthProperty().bind(heightProperty);
        backgroundImage.heightProperty().bind(heightProperty);

        flower = new Flower();
    }

    public void launch() {
        gameContext.setLimiterAvailable();

        gameContext.getChildren().clear();
        gameContext.getChildren().add(backgroundImage);
        createNumbers();

        flower.init();
        flower.setPistil(random.nextInt(19) + 1);
        log.info("pistil = {}", flower.getPistil());

        // TODO: display pistil

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    public void dispose() {
        log.info("DISPOSE");
        // TODO: dispose
    }

    void createNumbers() {
        createButton(Petal.DIGITS, 1, 30, 0.76, 0.05);
        for (int i = 0; i < 4; i++) {
            createButton(Petal.DIGITS, i + 2, 30, 0.80 + i * 0.04, 0.05);
            createButton(Petal.DIGITS, i + 6, 30, 0.80 + i * 0.04, 0.17);
        }

        for (int i = 0; i < 10; i++) {
            createButton(Petal.WORDS, i + 1, 9, 0.01, 0.05 + i * 0.05);
            createButton(Petal.WORDS, i + 11, 9, 0.11, 0.05 + i * 0.05);
        }

        for (int i = 0; i < 5; i++) {
            createButton(Petal.FINGERS, i + 1, 20, 0.01 + i * 0.05, 0.62);
        }

        for (int i = 0; i < 3; i++) {
            createButton(Petal.DICE, i + 1, 23, 0.79 + i * 0.06, 0.69);
            createButton(Petal.DICE, i + 4, 23, 0.79 + i * 0.06, 0.80);
        }

        createButton(Petal.MONEY, 1, 12, 0.78, 0.38);
        createButton(Petal.MONEY, 2, 12, 0.88, 0.38);
        createButton(Petal.MONEY, 5, 12, 0.78, 0.50);
        createButton(Petal.MONEY, 10, 12, 0.88, 0.50);
    }

    void createButton(Petal petal, int value, int size, double xMult, double yMult) {
        Image image = switch (petal) {
            case DIGITS -> createImageFromText(String.valueOf(value), maxDigitSize);
            case WORDS -> createImageFromText(multilinguism.getTranslation(String.valueOf(value), config.getLanguage()), maxWordSize);
            case FINGERS -> new Image("data/flowerOfNumbers/images/fingers/fingers" + value + ".png");
            case DICE -> new Image("data/flowerOfNumbers/images/dice/die" + value + ".png");
            case MONEY -> new Image("data/flowerOfNumbers/images/money/money" + value + ".png");
        };

        ImageView view = new ImageView(image);
        //DoubleProperty minSizeProperty = image.getWidth() <= image.getHeight() ? view.fitWidthProperty() : view.fitHeightProperty();

        ProgressButton button = new ProgressButton();
        button.setImage(view);
        button.getButton().setVisible(false);

        view.fitWidthProperty().bind(widthProperty.divide(size));
        view.fitHeightProperty().bind(widthProperty.divide(size).multiply(image.getHeight()).divide(image.getWidth()));
        button.layoutXProperty().bind(widthProperty.multiply(xMult));
        button.layoutYProperty().bind(heightProperty.multiply(yMult));
        // TODO: resolve radiusProperty binding problem
        //button.getButton().radiusProperty().bind(minSizeProperty.divide(2));

        gameContext.getChildren().add(button);

        button.assignIndicatorUpdatable(buttonEvent -> {
            int index = flower.getIndexFirstEmptyCell(petal);
            flower.add(petal, index, value);
            // TODO: check petal full -> disabled petal numbers
            // TODO: check flower complete -> bravo + new round

            ImageView newView = new ImageView(image);

            ProgressButton newButton = new ProgressButton();
            newButton.setImage(newView);
            newButton.getButton().setVisible(false);
            newButton.setDisable(true);
            newButton.getButton().setDisable(true);

            newView.fitWidthProperty().bind(widthProperty.divide(size));
            newView.fitHeightProperty().bind(widthProperty.divide(size).multiply(image.getHeight()).divide(image.getWidth()));
            newButton.setLayoutX(button.getLayoutX());
            newButton.setLayoutY(button.getLayoutY());
            //newButton.getButton().radiusProperty().bind(minSizeProperty.divide(2));

            gameContext.getChildren().add(newButton);

            Pair<Double, Double> pair = calculateCoordinates(petal, index);
            double newXMult = pair.getKey();
            double newYMult = pair.getValue();
            Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();

            KeyValue xKeyValue = new KeyValue(newButton.layoutXProperty(), dimension.getWidth() * newXMult, Interpolator.EASE_OUT);
            KeyValue yKeyValue = new KeyValue(newButton.layoutYProperty(), dimension.getHeight() * newYMult, Interpolator.EASE_OUT);
            KeyFrame keyFrame = new KeyFrame(new Duration(1000), xKeyValue, yKeyValue);
            Timeline timeline = new Timeline(keyFrame);

            timeline.setOnFinished(timelineEvent -> {
                newButton.getButton().setDisable(false);
                newButton.setDisable(false);

                newButton.layoutXProperty().bind(widthProperty.multiply(newXMult));
                newButton.layoutYProperty().bind(heightProperty.multiply(newYMult));

                newButton.assignIndicatorUpdatable(newButtonEvent -> {
                    flower.remove(petal, index);
                    // TODO: check petal not full -> enabled petal numbers

                    gameContext.getGazeDeviceManager().removeEventFilter(newButton);
                    gameContext.getChildren().remove(newButton);
                }, gameContext);
                gameContext.getGazeDeviceManager().addEventFilter(newButton);
                newButton.active();
            });

            timeline.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
            timeline.play();
        }, gameContext);
        gameContext.getGazeDeviceManager().addEventFilter(button);
        button.active();
    }

    Image createImageFromText(String text, int maxTextWidth) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2d.dispose();

        image = new BufferedImage(maxTextWidth < 0 ? textWidth : maxTextWidth, textHeight, BufferedImage.TYPE_INT_ARGB);
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
        g2d.setColor(config.getBackgroundStyle() == BackgroundStyle.LIGHT ? Color.BLACK : Color.WHITE);
        g2d.drawString(text, (maxTextWidth - textWidth) / 2, fm.getAscent());
        g2d.dispose();

        return SwingFXUtils.toFXImage(image, null);
    }

    int calculateMaxDigitSize() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        int maxSize = -1;
        for (int i = 1; i < 10; i++) {
            int curSize = fm.stringWidth(String.valueOf(i));
            if (curSize > maxSize) {
                maxSize = curSize;
            }
        }
        g2d.dispose();
        return maxSize;
    }

    int calculateMaxWordSize() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        int maxSize = -1;
        for (Locale locale : Languages.getAllCodes()) {
            String language = locale.getLanguage();
            for (int i = 1; i < 21; i++) {
                int curSize = fm.stringWidth(multilinguism.getTranslation(String.valueOf(i), language));
                if (curSize > maxSize) {
                    maxSize = curSize;
                }
            }
        }
        g2d.dispose();
        return maxSize;
    }

    Pair<Double, Double> calculateCoordinates(Petal petal, int index) {
        return switch (petal) {
            case DIGITS -> switch (index) {
                case 0 -> new Pair<>(0.45, 0.05);
                case 1 -> new Pair<>(0.51, 0.05);
                case 2 -> new Pair<>(0.45, 0.17);
                case 3 -> new Pair<>(0.51, 0.17);
                default -> null;
            };
            case WORDS -> index == 0 ? new Pair<>(0.28, 0.38) : null;
            case FINGERS -> switch (index) {
                case 0 -> new Pair<>(0.35, 0.63);
                case 1 -> new Pair<>(0.42, 0.63);
                case 2 -> new Pair<>(0.33, 0.77);
                case 3 -> new Pair<>(0.40, 0.77);
                default -> null;
            };
            case DICE -> switch (index) {
                case 0 -> new Pair<>(0.54, 0.66);
                case 1 -> new Pair<>(0.60, 0.66);
                case 2 -> new Pair<>(0.56, 0.77);
                case 3 -> new Pair<>(0.62, 0.77);
                default -> null;
            };
            case MONEY -> switch (index) {
                case 0 -> new Pair<>(0.57, 0.34);
                case 1 -> new Pair<>(0.66, 0.29);
                case 2 -> new Pair<>(0.58, 0.45);
                case 3 -> new Pair<>(0.67, 0.39);
                default -> null;
            };
        };
    }

    void enablePetal(Petal petal, boolean enable) {
        int startIndex = switch (petal) {
            case DIGITS -> 1;
            case WORDS -> 11;
            case FINGERS -> 32;
            case DICE -> 38;
            case MONEY -> 45;
        };
        int endIndex = switch (petal) {
            case DIGITS -> 10;
            case WORDS -> 31;
            case FINGERS -> 37;
            case DICE -> 44;
            case MONEY -> 49;
        };
        for (int i = startIndex; i < endIndex; i++) {
            gameContext.getChildren().get(5).setDisable(enable);
            ((ProgressButton) gameContext.getChildren().get(5)).getButton().setDisable(enable);
        }
    }
}
