package net.gazeplay.games.flowerOfNumbers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Game "FlowerOfNumbers" (reviewed on 2/11/2026): a number (up to 10 to 50, the variant) is written in the heart of a
 * flower of 5 petals; each petal takes one kind of values (digits, words, fingers, dice, money), shown in a card of its
 * color around the flower. The values of all the petals together must make the number, each value put once at most
 * (see {@link FlowerMode}: the part of each petal given, or free, or free with every petal used). A fixation on a
 * value puts it in its petal (it is then dimmed in its card); a fixation on a value of a petal takes it back. The total
 * is shown under the flower, the sum of each petal at its tip (with its part when guided). Won: "Bravo", then another
 * number.
 * <p>
 * Everything is drawn on a board of {@link #BOARD_WIDTH} x {@link #BOARD_HEIGHT}, scaled to the game area and centered,
 * so that nothing overlaps from 800 x 600 to large screens. The pictures are drawn by
 * gradle/images/make_flowerofnumbers_images.py, the sounds by make_flowerofnumbers_sounds.py.
 */
@Slf4j
public class FlowerOfNumbersGame implements GameLifeCycle {

    static final double BOARD_WIDTH = 1366;
    static final double BOARD_HEIGHT = 768;

    private static final String FOLDER = "data/flowerOfNumbers/";
    static final String PUT_SOUND = FOLDER + "sounds/put.wav";
    static final String PETAL_SOUND = FOLDER + "sounds/petal.wav";

    /**
     * The flower: its center and the distance from it to the tip of a petal.
     */
    private static final double CX = 683;
    private static final double CY = 392;
    private static final double R = 270;

    /**
     * The two columns of cards, left and right of the flower.
     */
    private static final double COLUMN_WIDTH = 334;
    private static final double LEFT_X = 16;
    private static final double RIGHT_X = BOARD_WIDTH - 16 - COLUMN_WIDTH;
    private static final double CARD_PADDING = 12;
    private static final double CARD_GAP = 16;

    /**
     * The size of a value in a petal (a word: {@link #WORD_SLOT_WIDTH} x {@link #WORD_SLOT_HEIGHT}).
     */
    private static final double SLOT = 56;
    private static final double WORD_SLOT_WIDTH = 150;
    private static final double WORD_SLOT_HEIGHT = 48;

    /**
     * The places of the values in a petal of 4 places: along the petal (ratio of R) and across.
     */
    private static final double[][] SLOTS = {{0.47, -0.13}, {0.47, 0.13}, {0.76, -0.13}, {0.76, 0.13}};

    private static final int[] MONEY = {1, 2, 5, 10};

    /**
     * The words go up to 20.
     */
    private static final int LAST_WORD = 20;

    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom random;
    private final Multilinguism multilinguism;

    /**
     * The largest number of the heart: 10 to 50.
     */
    @Getter
    private final int maxNumber;

    @Getter
    private final FlowerMode mode;

    @Getter
    private final Flower flower = new Flower();

    /**
     * The part of each petal, in the guided mode.
     */
    private final Map<Petal, Integer> parts = new EnumMap<>(Petal.class);

    private final Map<String, Image> images = new HashMap<>();

    /**
     * The values to choose, by petal.
     */
    @Getter
    private final Map<Petal, List<ValueTarget>> choices = new EnumMap<>(Petal.class);

    /**
     * The values put in each petal, at their place (null: free).
     */
    @Getter
    private final Map<Petal, ValueTarget[]> placed = new EnumMap<>(Petal.class);

    @Getter
    private final Map<Petal, Ellipse> petals = new EnumMap<>(Petal.class);
    @Getter
    private final Map<Petal, Group> sumPills = new EnumMap<>(Petal.class);
    private final Map<Petal, Boolean> wasRight = new EnumMap<>(Petal.class);
    private final List<Timeline> flights = new ArrayList<>();

    private Group board;
    private Rectangle background;

    /**
     * The total under the flower.
     */
    @Getter
    private Group totalPill;
    private boolean wasOver;
    private int previousNumber;
    private boolean won;

    public FlowerOfNumbersGame(final IGameContext gameContext, final Stats stats, final int maxNumber,
                               final FlowerMode mode) {
        this(gameContext, stats, maxNumber, mode, -1);
    }

    /**
     * @param maxNumber the largest number of the heart (10 to 50)
     * @param gameSeed  the seed of the game to replay, or a negative value for a new game
     */
    public FlowerOfNumbersGame(final IGameContext gameContext, final Stats stats, final int maxNumber,
                               final FlowerMode mode, final double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.maxNumber = maxNumber;
        this.mode = mode;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            this.stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
        multilinguism = MultilinguismFactory.getForResource(FOLDER + "numbers.csv");
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        won = false;
        wasOver = false;
        flower.init();
        flower.setPistil(chooseNumber());
        if (mode == FlowerMode.GUIDED) {
            shareOut(flower.getPistil());
        }
        log.info("number = {}, parts = {}", flower.getPistil(), parts);

        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        background = new Rectangle(dimension.getWidth(), dimension.getHeight());
        background.setStyle("-fx-fill: -gp-bg;");
        background.setMouseTransparent(true);

        final double scale = Math.min(dimension.getWidth() / BOARD_WIDTH, dimension.getHeight() / BOARD_HEIGHT);
        board = new Group();
        board.getTransforms().addAll(new Translate((dimension.getWidth() - BOARD_WIDTH * scale) / 2,
            (dimension.getHeight() - BOARD_HEIGHT * scale) / 2), new Scale(scale, scale, 0, 0));

        drawFlower();
        createCards();
        for (final Petal petal : Petal.values()) {
            placed.put(petal, new ValueTarget[petal.getCapacity()]);
            wasRight.put(petal, false);
            updatePetal(petal);
        }
        updateTotal();
        gameContext.getChildren().addAll(background, board);

        stats.incrementNumberOfGoalsToReach();
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose() {
        flights.forEach(Timeline::stop);
        flights.clear();
        choices.values().forEach(list -> list.forEach(ValueTarget::dispose));
        placed.values().forEach(values -> {
            for (final ValueTarget value : values) {
                if (value != null) {
                    value.dispose();
                }
            }
        });
        choices.clear();
        placed.clear();
        petals.clear();
        sumPills.clear();
        parts.clear();
        totalPill = null;
        if (board != null) {
            gameContext.getChildren().removeAll(background, board);
            board = null;
        }
    }

    /**
     * @return a new number for the heart, from the smallest of the mode to the largest of the variant, different from
     * the previous one
     */
    private int chooseNumber() {
        final int smallest = mode.getSmallestNumber();
        int value = previousNumber;
        while (value == previousNumber) {
            value = smallest + random.nextInt(maxNumber - smallest + 1);
        }
        previousNumber = value;
        return value;
    }

    /**
     * @return the values proposed for a petal
     */
    int[] valuesOf(final Petal petal) {
        return switch (petal) {
            case DIGITS -> range(9);
            case WORDS -> range(Math.min(maxNumber, LAST_WORD));
            case FINGERS -> range(5);
            case DICE -> range(6);
            case MONEY -> MONEY;
        };
    }

    /**
     * @return the sums a petal can make with different values of its own, as many as its places at most
     */
    Set<Integer> reachableSums(final Petal petal) {
        final Set<Integer> sums = new TreeSet<>();
        reach(valuesOf(petal), 0, petal.getCapacity(), 0, sums);
        sums.remove(0);
        return sums;
    }

    private static void reach(final int[] values, final int from, final int places, final int sum,
                              final Set<Integer> sums) {
        sums.add(sum);
        if (places == 0) {
            return;
        }
        for (int i = from; i < values.length; i++) {
            reach(values, i + 1, places - 1, sum + values[i], sums);
        }
    }

    /**
     * Shares the number out among the 5 petals at random, at least 1 each, each part a sum its petal can make.
     */
    private void shareOut(final int number) {
        final Map<Petal, Set<Integer>> reachable = new EnumMap<>(Petal.class);
        for (final Petal petal : Petal.values()) {
            reachable.put(petal, reachableSums(petal));
        }
        final int count = Petal.values().length;
        while (true) {
            final Set<Integer> cuts = new TreeSet<>();
            while (cuts.size() < count - 1) {
                cuts.add(1 + random.nextInt(number - 1));
            }
            final List<Integer> bounds = new ArrayList<>(cuts);
            bounds.addFirst(0);
            bounds.add(number);
            boolean possible = true;
            for (int i = 0; i < count && possible; i++) {
                possible = reachable.get(Petal.values()[i]).contains(bounds.get(i + 1) - bounds.get(i));
            }
            if (possible) {
                for (int i = 0; i < count; i++) {
                    parts.put(Petal.values()[i], bounds.get(i + 1) - bounds.get(i));
                }
                return;
            }
        }
    }

    /**
     * @return the part of a petal in the guided mode, 0 in the others
     */
    int partOf(final Petal petal) {
        return mode == FlowerMode.GUIDED ? parts.getOrDefault(petal, 0) : 0;
    }

    /**
     * @return a point of a petal: {@code along} from the center of the flower in the direction of the petal, then
     * {@code across} to its side
     */
    static Point2D pointOf(final Petal petal, final double along, final double across) {
        final double angle = Math.toRadians(petal.getAngle());
        return new Point2D(CX + along * Math.cos(angle) - across * Math.sin(angle),
            CY + along * Math.sin(angle) + across * Math.cos(angle));
    }

    /**
     * Draws the stem, the petals and the heart of the flower with its number.
     */
    private void drawFlower() {
        final Line stem = new Line(CX, CY, CX, BOARD_HEIGHT + 40);
        stem.setStrokeWidth(16);
        stem.setStrokeLineCap(StrokeLineCap.ROUND);
        stem.setStroke(Color.web("#3E9A47"));
        stem.setMouseTransparent(true);
        board.getChildren().add(stem);

        for (final Petal petal : Petal.values()) {
            final Point2D center = pointOf(petal, 0.6 * R, 0);
            final Ellipse shape = new Ellipse(center.getX(), center.getY(), 0.42 * R, 0.29 * R);
            shape.setRotate(petal.getAngle());
            shape.setStrokeType(StrokeType.INSIDE);
            shape.setMouseTransparent(true);
            petals.put(petal, shape);
            board.getChildren().add(shape);
        }

        final Circle heart = new Circle(CX, CY, 0.22 * R);
        heart.setStyle("-fx-fill: #FFC83D; -fx-stroke: #E0A100; -fx-stroke-width: 4;");
        heart.setMouseTransparent(true);
        final Text number = new Text(String.valueOf(flower.getPistil()));
        number.setFont(GameStyle.titleFont(60));
        number.setStyle(GameStyle.titleFontStyle(60) + " -fx-fill: #3A2A00;");
        number.setX(CX - number.getLayoutBounds().getWidth() / 2 - number.getLayoutBounds().getMinX());
        number.setY(CY - number.getLayoutBounds().getHeight() / 2 - number.getLayoutBounds().getMinY());
        number.setMouseTransparent(true);
        board.getChildren().addAll(heart, number);
    }

    /**
     * Creates the cards of the values to choose: words and fingers on the left, digits, money and dice on the right.
     */
    private void createCards() {
        final int words = valuesOf(Petal.WORDS).length;
        double y = CARD_GAP;
        y = card(Petal.WORDS, (words + 4) / 5, true, 72, 60, 6, LEFT_X, y) + CARD_GAP;
        card(Petal.FINGERS, 3, false, 90, 108, 8, LEFT_X, y);
        y = CARD_GAP;
        y = card(Petal.DIGITS, 5, false, 54, 64, 8, RIGHT_X, y) + CARD_GAP;
        y = card(Petal.MONEY, 2, false, 150, 92, 8, RIGHT_X, y) + CARD_GAP;
        card(Petal.DICE, 3, false, 84, 84, 8, RIGHT_X, y);
    }

    private static int[] range(final int last) {
        final int[] values = new int[last];
        for (int i = 0; i < last; i++) {
            values[i] = i + 1;
        }
        return values;
    }

    /**
     * Creates the card of the values of a petal, bordered with its color, centered in its column.
     *
     * @param byColumn whether the values follow each other down the columns (else along the rows)
     * @return the bottom of the card
     */
    private double card(final Petal petal, final int columns, final boolean byColumn, final double cellWidth,
                        final double cellHeight, final double gap, final double columnX, final double top) {
        final int[] values = valuesOf(petal);
        final int rows = (values.length + columns - 1) / columns;
        final double width = columns * cellWidth + (columns - 1) * gap + 2 * CARD_PADDING;
        final double height = rows * cellHeight + (rows - 1) * gap + 2 * CARD_PADDING;
        final double left = columnX + (COLUMN_WIDTH - width) / 2;

        final Rectangle card = new Rectangle(left, top, width, height);
        card.setArcWidth(2 * GameStyle.cardRadius(width, height));
        card.setArcHeight(card.getArcWidth());
        card.setStrokeType(StrokeType.INSIDE);
        card.setStyle("-fx-fill: -gp-surface; -fx-stroke: " + petal.getColor() + "; -fx-stroke-width: 5;");
        card.setMouseTransparent(true);
        board.getChildren().add(card);

        final List<ValueTarget> targets = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            final int value = values[i];
            final int column = byColumn ? i / rows : i % columns;
            final int row = byColumn ? i % rows : i / columns;
            final ValueTarget target = new ValueTarget(gameContext, value,
                content(petal, value, cellWidth, cellHeight), cellWidth, cellHeight, "-gp-bg", null);
            target.setAction(() -> choose(petal, target));
            target.setLayoutX(left + CARD_PADDING + column * (cellWidth + gap));
            target.setLayoutY(top + CARD_PADDING + row * (cellHeight + gap));
            targets.add(target);
            board.getChildren().add(target);
        }
        choices.put(petal, targets);
        return top + height;
    }

    /**
     * @return the picture or the text of a value, fitting in a card of this size
     */
    private Node content(final Petal petal, final int value, final double width, final double height) {
        return switch (petal) {
            case DIGITS -> text(String.valueOf(value), true, height * 0.62, width * 0.9);
            case WORDS -> text(multilinguism.getTranslation(String.valueOf(value),
                gameContext.getConfiguration().getLanguage()), false, Math.min(height * 0.36, 20), width * 0.9);
            case FINGERS -> picture("fingers/fingers" + value, width, height);
            case DICE -> picture("dice/die" + value, width, height);
            case MONEY -> picture("money/money" + value, width, height);
        };
    }

    private static Text text(final String content, final boolean title, final double size, final double maxWidth) {
        final Text text = new Text(content);
        double fontSize = size;
        for (int i = 0; i < 2; i++) {
            if (title) {
                text.setFont(GameStyle.titleFont(fontSize));
                text.setStyle(GameStyle.titleFontStyle(fontSize) + " -fx-fill: -gp-text;");
            } else {
                text.setStyle(GameStyle.textFontStyle(fontSize) + " -fx-fill: -gp-text;");
                text.setFont(Font.font(GameStyle.TEXT_FONT_FAMILY, fontSize));
            }
            final double width = text.getLayoutBounds().getWidth();
            if (width <= maxWidth) {
                break;
            }
            fontSize = fontSize * maxWidth / width;
        }
        return text;
    }

    private ImageView picture(final String name, final double width, final double height) {
        final Image image = images.computeIfAbsent(name, key -> new Image(FOLDER + "images/" + key + ".png"));
        final ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setFitWidth(width * 0.86);
        view.setFitHeight(height * 0.86);
        return view;
    }

    /**
     * A value chosen: it flies from its card to the first free place of its petal, then can be taken back; meanwhile
     * it cannot be chosen again.
     */
    void choose(final Petal petal, final ValueTarget choice) {
        final int index = flower.getIndexFirstEmptyCell(petal);
        if (won || index < 0 || flower.petalHolds(petal, choice.getValue())) {
            return;
        }
        flower.add(petal, index, choice.getValue());
        log.info("add value {} to {} petal", choice.getValue(), petal);
        updateChoices(petal);

        final boolean word = petal == Petal.WORDS;
        final double width = word ? WORD_SLOT_WIDTH : SLOT;
        final double height = word ? WORD_SLOT_HEIGHT : SLOT;
        final ValueTarget value = new ValueTarget(gameContext, choice.getValue(),
            content(petal, choice.getValue(), width, height), width, height, "-gp-surface",
            () -> takeBack(petal, index));
        placed.get(petal)[index] = value;
        value.setEnabled(false);
        value.setOpacity(1);
        value.setMouseTransparent(true);
        value.setLayoutX(choice.getLayoutX() + (choice.getWidth() - width) / 2);
        value.setLayoutY(choice.getLayoutY() + (choice.getHeight() - height) / 2);
        board.getChildren().add(value);

        final Point2D place = word ? pointOf(petal, 0.62 * R, 0) : pointOf(petal, SLOTS[index][0] * R,
            SLOTS[index][1] * R);
        final Timeline flight = new Timeline(new KeyFrame(Duration.millis(600),
            new KeyValue(value.layoutXProperty(), place.getX() - width / 2, Interpolator.EASE_OUT),
            new KeyValue(value.layoutYProperty(), place.getY() - height / 2, Interpolator.EASE_OUT)));
        flight.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
        flight.setOnFinished(event -> {
            flights.remove(flight);
            landed(petal, value);
        });
        flights.add(flight);
        flight.play();
    }

    /**
     * A value arrived in its petal.
     */
    private void landed(final Petal petal, final ValueTarget value) {
        value.setMouseTransparent(false);
        value.setEnabled(!won);
        gameContext.getSoundManager().add(PUT_SOUND);
        stats.takeScreenShot();
        updatePetal(petal);
        updateTotal();
        checkFlower();
    }

    /**
     * A value of a petal looked at: it goes back to its card.
     */
    private void takeBack(final Petal petal, final int index) {
        if (won) {
            return;
        }
        final ValueTarget value = placed.get(petal)[index];
        log.info("remove value {} of {} petal", flower.get(petal, index), petal);
        flower.remove(petal, index);
        placed.get(petal)[index] = null;
        value.dispose();
        board.getChildren().remove(value);
        stats.takeScreenShot();
        updateChoices(petal);
        updatePetal(petal);
        updateTotal();
    }

    /**
     * A value can be chosen while its petal has a free place and it is not in the petal yet.
     */
    private void updateChoices(final Petal petal) {
        final boolean free = !won && !flower.petalIsFull(petal);
        choices.get(petal).forEach(choice -> choice.setEnabled(free && !flower.petalHolds(petal, choice.getValue())));
    }

    /**
     * Shows the state of a petal: its sum at its tip (with its part when guided); when guided, red when it goes over
     * its part, green when it makes it (with a sound the first time); in the mode of all the petals, pale while empty.
     */
    private void updatePetal(final Petal petal) {
        final int sum = flower.getSum(petal);
        final int part = partOf(petal);
        final boolean right = mode == FlowerMode.GUIDED ? sum == part : won;
        final boolean over = mode == FlowerMode.GUIDED && sum > part;
        final Ellipse shape = petals.get(petal);
        final String stroke;
        if (right) {
            stroke = "-fx-stroke: " + GameStyle.SUCCESS_COLOR + "; -fx-stroke-width: 9;";
            shape.setEffect(new DropShadow(24, Color.web(GameStyle.SUCCESS_COLOR)));
        } else if (over) {
            stroke = "-fx-stroke: -gp-danger; -fx-stroke-width: 9;";
            shape.setEffect(null);
        } else {
            stroke = "-fx-stroke: rgba(0, 0, 0, 0.15); -fx-stroke-width: 2;";
            shape.setEffect(null);
        }
        // In the mode of all the petals, an empty petal is pale (lighter, not transparent: the stem stays behind)
        final boolean pale = mode == FlowerMode.ALL_PETALS && flower.petalIsEmpty(petal);
        shape.setStyle("-fx-fill: " + (pale ? "derive(" + petal.getColor() + ", 60%)" : petal.getColor()) + "; "
            + stroke);
        if (over && !Boolean.TRUE.equals(shape.getProperties().get("over"))) {
            shake(shape);
        }
        shape.getProperties().put("over", over);
        if (mode == FlowerMode.GUIDED && right && !wasRight.get(petal)) {
            gameContext.getSoundManager().add(PETAL_SOUND);
        }
        wasRight.put(petal, right);

        if (sumPills.containsKey(petal)) {
            board.getChildren().remove(sumPills.get(petal));
        }
        final Group pill = GameStyle.pill(mode == FlowerMode.GUIDED ? sum + " / " + part : String.valueOf(sum), 22);
        if (right || over) {
            ((Text) pill.getChildren().get(1)).setStyle(GameStyle.titleFontStyle(22) + " -fx-fill: "
                + (right ? GameStyle.SUCCESS_COLOR : "-gp-danger") + ";");
        }
        final Point2D tip = pointOf(petal, 1.08 * R, 0);
        pill.setLayoutX(tip.getX() - pill.getBoundsInLocal().getWidth() / 2);
        pill.setLayoutY(tip.getY() - pill.getBoundsInLocal().getHeight() / 2);
        pill.setMouseTransparent(true);
        sumPills.put(petal, pill);
        board.getChildren().add(pill);
    }

    /**
     * Shows the total of the petals under the flower: red over the number, green when the flower is won.
     */
    private void updateTotal() {
        if (totalPill != null) {
            board.getChildren().remove(totalPill);
        }
        final int total = flower.getTotal();
        final boolean over = total > flower.getPistil();
        totalPill = GameStyle.pill(gameContext.getTranslator().translate("FlowerTotal") + " " + total + " / "
            + flower.getPistil(), 30);
        if (won || over) {
            ((Text) totalPill.getChildren().get(1)).setStyle(GameStyle.titleFontStyle(30) + " -fx-fill: "
                + (won ? GameStyle.SUCCESS_COLOR : "-gp-danger") + ";");
        }
        totalPill.setLayoutX(CX - totalPill.getBoundsInLocal().getWidth() / 2);
        totalPill.setLayoutY(BOARD_HEIGHT - 26 - totalPill.getBoundsInLocal().getHeight());
        totalPill.setMouseTransparent(true);
        board.getChildren().add(totalPill);
        if (over && !wasOver) {
            shake(totalPill);
        }
        wasOver = over;
    }

    private static void shake(final Node node) {
        final TranslateTransition shake = new TranslateTransition(Duration.millis(60), node);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setAutoReverse(true);
        shake.setCycleCount(6);
        shake.setOnFinished(event -> node.setTranslateX(0));
        shake.play();
    }

    /**
     * @return whether the flower is won: the total makes the number; when guided, each petal makes its part; in the
     * mode of all the petals, none is empty
     */
    boolean isWon() {
        if (flower.getTotal() != flower.getPistil()) {
            return false;
        }
        for (final Petal petal : Petal.values()) {
            if (mode == FlowerMode.GUIDED && flower.getSum(petal) != partOf(petal)
                || mode == FlowerMode.ALL_PETALS && flower.petalIsEmpty(petal)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Won: "Bravo", then another number.
     */
    private void checkFlower() {
        if (won || !isWon()) {
            return;
        }
        won = true;
        log.info("flower is complete: WIN");
        for (final Petal petal : Petal.values()) {
            updateChoices(petal);
            updatePetal(petal);
            for (final ValueTarget value : placed.get(petal)) {
                if (value != null) {
                    value.setEnabled(false);
                    value.setOpacity(1);
                }
            }
        }
        updateTotal();
        if (mode != FlowerMode.GUIDED) {
            gameContext.getSoundManager().add(PETAL_SOUND);
        }
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, this);
        gameContext.playWinTransition(500, event -> {
            dispose();
            gameContext.clear();
            launch();
        });
    }
}
