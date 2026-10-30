package net.gazeplay.games.spotthedifferences;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.PauseTransition;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.components.GameStyle;
import net.gazeplay.components.GazeRoundButton;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Game "SpotDifference" (reviewed on 30/10/2026): two pictures side by side differ in 3, 5 or 7 places (the variant);
 * each difference found by a fixation is circled in green on both pictures. When all are found: "Bravo", then the next
 * picture (in an order drawn with the seed of the game, for the replay); the game does not stop by itself. After
 * {@link #HINT_DELAY} without finding, a difference left blinks. A round button skips the picture. The scenes are drawn
 * by gradle/images/make_spotdifferences_images.py, which writes instances.json.
 */
@Slf4j
public class SpotTheDifferences implements GameLifeCycle {

    /**
     * How long without finding before a hint.
     */
    static final Duration HINT_DELAY = Duration.seconds(20);

    private static final String FOLDER = "data/spotthedifferences/";

    @Getter
    private final SpotTheDifferencesStats stats;
    private final IGameContext gameContext;
    private final SpotTheDifferencesVariant variant;
    private final ReplayablePseudoRandom random;
    private final JsonArray instances;

    /**
     * The order of the pictures, drawn again when all were shown.
     */
    private final List<Integer> order = new ArrayList<>();

    @Getter
    private final List<Difference> differences = new ArrayList<>();

    private final ArrayList<TargetAOI> targetAOIList = new ArrayList<>();

    private final PauseTransition hintTimer = new PauseTransition(HINT_DELAY);

    private Group layer;
    private Group scorePill;
    private GazeRoundButton nextButton;
    private Dimension2D dimension;
    private int found;

    /**
     * The scene shown, for the tests.
     */
    @Getter
    private String scene;

    /**
     * @param gameSeed the seed of the game to replay, or a negative value for a new game
     */
    public SpotTheDifferences(final IGameContext gameContext, final SpotTheDifferencesStats stats,
                              final SpotTheDifferencesVariant variant, final double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;
        if (gameSeed < 0) {
            this.random = new ReplayablePseudoRandom();
            this.stats.setGameSeed(random.getSeed());
        } else {
            this.random = new ReplayablePseudoRandom(gameSeed);
        }
        this.gameContext.startTimeLimiter();
        instances = (JsonArray) JsonParser.parseReader(new InputStreamReader(
            Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(FOLDER + "instances.json")),
            StandardCharsets.UTF_8));
        hintTimer.setOnFinished(event -> showHint());
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        differences.clear();
        found = 0;

        final Rectangle background = new Rectangle(dimension.getWidth(), dimension.getHeight());
        background.setStyle("-fx-fill: -gp-bg;");
        background.setMouseTransparent(true);
        layer = new Group(background);
        gameContext.getChildren().add(layer);

        if (order.isEmpty()) {
            for (int i = 0; i < instances.size(); i++) {
                order.add(i);
            }
            for (int i = order.size() - 1; i > 0; i--) {
                final int j = random.nextInt(i + 1);
                order.set(i, order.set(j, order.get(i)));
            }
        }
        final JsonObject instance = (JsonObject) instances.get(order.removeFirst());
        scene = instance.get("scene").getAsString();
        final JsonObject level = instance.getAsJsonObject("levels").getAsJsonObject(variant.name());

        // The two pictures, as big as the space between the score (top) and the button (bottom) allows
        final double band = dimension.getHeight() * 0.11;
        final double gap = dimension.getWidth() * 0.02;
        final double side = Math.min(dimension.getHeight() - 2 * band, (dimension.getWidth() - 3 * gap) / 2);
        final double leftX = (dimension.getWidth() - 2 * side - gap) / 2;
        final double rightX = leftX + side + gap;
        final double top = band;
        final Image first = new Image(FOLDER + instance.get("image1").getAsString());
        final Image second = new Image(FOLDER + level.get("image2").getAsString());
        addPicture(first, leftX, top, side);
        addPicture(second, rightX, top, side);

        final double ratio = side / first.getWidth();
        final long now = System.currentTimeMillis();
        for (final JsonElement element : level.getAsJsonArray("differences")) {
            final JsonObject diff = (JsonObject) element;
            final double x = ratio * diff.get("x").getAsDouble();
            final double y = top + ratio * diff.get("y").getAsDouble();
            final double radius = ratio * diff.get("radius").getAsDouble();
            final Circle left = new Circle(leftX + x, y, radius);
            final Circle right = new Circle(rightX + x, y, radius);
            final List<TargetAOI> areas = List.of(new TargetAOI(leftX + x, y, (int) radius, now),
                new TargetAOI(rightX + x, y, (int) radius, now));
            targetAOIList.addAll(areas);
            differences.add(new Difference(gameContext, List.of(left, right), areas, this::differenceFound));
            layer.getChildren().addAll(left, right);
        }

        final double buttonSize = band * 0.8;
        nextButton = new GazeRoundButton(gameContext, buttonSize, "arrow_forward",
            gameContext.getTranslator().translate("Next"), this::nextPicture);
        nextButton.setLayoutX(rightX + side - buttonSize);
        nextButton.setLayoutY(top + side + (band - buttonSize) / 2);
        layer.getChildren().add(nextButton);

        updateScorePill();
        hintTimer.playFromStart();

        stats.incrementNumberOfGoalsToReach(differences.size());
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    private void addPicture(final Image image, final double x, final double y, final double side) {
        final ImageView view = new ImageView(image);
        view.setFitWidth(side);
        view.setFitHeight(side);
        view.setLayoutX(x);
        view.setLayoutY(y);
        view.setMouseTransparent(true);
        final Rectangle frame = new Rectangle(x, y, side, side);
        frame.setStyle("-fx-fill: transparent; -fx-stroke: -gp-border; -fx-stroke-width: 3;");
        frame.setMouseTransparent(true);
        layer.getChildren().addAll(view, frame);
    }

    private void updateScorePill() {
        if (scorePill != null) {
            layer.getChildren().remove(scorePill);
        }
        scorePill = GameStyle.pill(gameContext.getTranslator().translate("SpotDifferencesFound") + " " + found
            + " / " + differences.size(), dimension.getHeight() * 0.032);
        scorePill.setLayoutX((dimension.getWidth() - scorePill.getBoundsInLocal().getWidth()) / 2);
        scorePill.setLayoutY((dimension.getHeight() * 0.11 - scorePill.getBoundsInLocal().getHeight()) / 2);
        scorePill.setMouseTransparent(true);
        layer.getChildren().add(scorePill);
    }

    /**
     * Counts a difference found; when all are found, "Bravo", then the next picture.
     */
    private void differenceFound() {
        found++;
        updateScorePill();
        stats.incrementNumberOfGoalsReached();
        gameContext.getSoundManager().add(FOLDER + "ding.wav");
        if (found == differences.size()) {
            hintTimer.stop();
            nextButton.setEnabled(false);
            gameContext.updateScore(stats, this);
            gameContext.playWinTransition(500, event -> {
                dispose();
                gameContext.clear();
                launch();
            });
        } else {
            hintTimer.playFromStart();
        }
    }

    /**
     * Makes a difference left blink, then waits again.
     */
    void showHint() {
        final List<Difference> left = differences.stream().filter(difference -> !difference.isFound()).toList();
        if (!left.isEmpty()) {
            left.get(random.nextInt(left.size())).showHint();
            hintTimer.playFromStart();
        }
    }

    /**
     * The button: the next picture, without finding the differences left.
     */
    private void nextPicture() {
        dispose();
        gameContext.clear();
        launch();
    }

    @Override
    public void dispose() {
        hintTimer.stop();
        differences.forEach(Difference::release);
        if (nextButton != null) {
            nextButton.dispose();
            nextButton = null;
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
        scorePill = null;
        stats.setTargetAOIList(targetAOIList);
    }
}
