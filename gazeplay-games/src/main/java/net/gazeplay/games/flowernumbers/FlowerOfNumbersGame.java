package net.gazeplay.games.flowernumbers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.ProgressButton;

import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class FlowerOfNumbersGame implements GameLifeCycle {
    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom random;

    private final Dimension2D dimension;
    private final ReadOnlyDoubleProperty widthProperty;
    private final ReadOnlyDoubleProperty heightProperty;

    private final Configuration config;
    private final Multilinguism multilinguism;
    private final int maxWordSize;

    private final Rectangle backgroundImage;

    private final Flower flower;

    FlowerOfNumbersGame(final IGameContext gameContext, final Stats stats) {
        this(gameContext, stats, -1);
    }

    FlowerOfNumbersGame(final IGameContext gameContext, final Stats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;

        if (gameSeed == -1) {
            random = new ReplayablePseudoRandom();
            this.stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }

        config = gameContext.getConfiguration();
        multilinguism = MultilinguismFactory.getForResource("data/flowernumbers/numbers.csv");
        maxWordSize = findMaxWordSize();

        dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.debug("dimension = {}", dimension);
        widthProperty = gameContext.getRoot().widthProperty();
        heightProperty = gameContext.getRoot().heightProperty();

        backgroundImage = new Rectangle(0, 0, dimension.getHeight(), dimension.getHeight());
        backgroundImage.setFill(new ImagePattern(new Image("data/flowernumbers/images/flower.png")));
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

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    public void dispose() {
        log.info("DISPOSE");
    }

    void createNumbers() {
        createButton(Petal.DIGITS, 1, 30, 0.76, 0.05);
        for (int i = 0; i < 4; i++) {
            createButton(Petal.DIGITS, i + 2, 30, 0.80 + i * 0.04, 0.05);
            createButton(Petal.DIGITS, i + 5, 30, 0.80 + i * 0.04, 0.17);
        }

        for (int i = 0; i < 10; i++) {
            createButton(Petal.WORDS, i + 1, 30, 0.03, 0.05 + i * 0.05);
            createButton(Petal.WORDS, i + 11, 30, 0.13, 0.05 + i * 0.05);
        }

        for (int i = 0; i < 5; i++) {
            createButton(Petal.FINGERS, i + 1, 22, 0.01 + i * 0.05, 0.65);
        }

        for (int i = 0; i < 3; i++) {
            createButton(Petal.DICE, i + 1, 25, 0.79 + i * 0.06, 0.69);
            createButton(Petal.DICE, i + 4, 25, 0.79 + i * 0.06, 0.80);
        }

        createButton(Petal.MONEY, 1, 20, 0.80, 0.38);
        createButton(Petal.MONEY, 2, 20, 0.89, 0.38);
        createButton(Petal.MONEY, 5, 11, 0.78, 0.50);
        createButton(Petal.MONEY, 10, 11, 0.88, 0.50);
    }

    void createButton(Petal petal, int value, int size, double xMult, double yMult) {
        Image image = switch (petal) {
            case DIGITS -> createImageFromText(String.valueOf(value), -1);
            case WORDS -> createImageFromText(multilinguism.getTranslation(String.valueOf(value), config.getLanguage()), -1);
            case FINGERS -> new Image("data/flowernumbers/images/fingers/fingers" + value + ".png");
            case DICE -> new Image("data/flowernumbers/images/dice/die" + value + ".png");
            case MONEY -> new Image("data/flowernumbers/images/money/money" + value + ".png");
        };

        ImageView view = new ImageView(image);
        DoubleProperty minSizeProperty = image.getWidth() <= image.getHeight() ? view.fitWidthProperty() : view.fitHeightProperty();

        ProgressButton button = new ProgressButton();
        button.setImage(view);
        button.getButton().setVisible(false);

        view.fitWidthProperty().bind(widthProperty.divide(size));
        view.fitHeightProperty().bind(widthProperty.divide(size).multiply(image.getHeight()).divide(image.getWidth()));
        button.layoutXProperty().bind(widthProperty.multiply(xMult));
        button.layoutYProperty().bind(heightProperty.multiply(yMult));
        button.getButton().radiusProperty().bind(minSizeProperty.divide(2));

        gameContext.getChildren().add(button);

        button.assignIndicatorUpdatable(event -> {
            log.info("BUTTON CLICK: type: " + petal + " ; value: " + value);
            flower.add(petal, value);
        }, gameContext);
        gameContext.getGazeDeviceManager().addEventFilter(button);
        button.active();
    }

    Image createImageFromText(String text, int maxTextSize) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, 100);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = maxTextSize < 0 ? fm.stringWidth(text) : maxTextSize;
        int height = fm.getHeight();
        g2d.dispose();

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
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
        g2d.drawString(text, 0, fm.getAscent());
        g2d.dispose();

        return SwingFXUtils.toFXImage(image, null);
    }

    int findMaxWordSize() {
        final String language = config.getLanguage();
        int maxSize = 0;
        for (int i = 1; i < 21; i++) {
            int curSize = multilinguism.getTranslation(String.valueOf(i), language).length();
            if (curSize > maxSize) {
                maxSize = curSize;
            }
        }
        return maxSize;
    }
}
