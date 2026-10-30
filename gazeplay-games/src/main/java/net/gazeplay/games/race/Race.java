package net.gazeplay.games.race;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Game "FrogsRace" (reviewed on 30/10/2026, REVUE_DES_JEUX.md): bugs (the flying Biboules) fly in the sky; each bug the
 * gaze (or the mouse) enters is caught by the frog of the bank, which shoots its tongue at it, and the frog of the
 * player hops forward on its lane (the first one, marked "You"). The three other frogs hop on their own, at the speed
 * of the variant; when a frog reaches the finish, the ranking is shown, then a new race starts.
 * <p>
 * Statistics: a goal to reach for each bug that appears, a goal reached for each bug caught.
 */
@Slf4j
public class Race extends Parent implements GameLifeCycle {

    /**
     * Bugs to catch for the frog of the player to reach the finish: one hop each.
     */
    static final int HOPS_TO_FINISH = 20;

    /**
     * Bugs flying at the same time.
     */
    static final int BUGS = 8;

    static final int LANES = 4;

    /**
     * The mouth of the catching frog, as fractions of the size of its picture (see gradle/images/make_race_images.py).
     */
    private static final double CATCHER_MOUTH_X = 0.5;
    private static final double CATCHER_MOUTH_Y = 196.0 / 300;

    private static final String IMAGES = "data/race/images/";
    private static final String[] BUG_COLORS = {"Blue", "Green", "Yellow", "Orange", "Red"};
    private static final String[] OPPONENT_COLORS = {"blue", "orange", "violet"};

    private static final Duration HOP = Duration.millis(380);

    private final IGameContext gameContext;
    private final Stats stats;
    private final RaceVariant variant;
    private final ReplayablePseudoRandom random;

    private final Image catcherImage = new Image(IMAGES + "frog-catcher.png");
    private final Image flashImage = new Image(IMAGES + "Flash.png");
    private final Image[] bugImages = new Image[BUG_COLORS.length];

    /**
     * Animations of the race in progress, stopped when it ends.
     */
    private final List<Animation> animations = new ArrayList<>();

    @Getter
    private final List<Bug> bugs = new ArrayList<>();

    @Getter
    private final List<Racer> racers = new ArrayList<>();

    @Getter
    private Racer player;

    @Getter
    private int caught;

    @Getter
    private boolean raceOver;

    private Dimension2D dimension;
    private ImageView catcher;
    private Group scorePill;

    public Race(final IGameContext gameContext, final Stats stats, final RaceVariant variant) {
        this(gameContext, stats, variant, new ReplayablePseudoRandom());
        this.gameContext.startTimeLimiter();
        this.stats.setGameSeed(random.getSeed());
    }

    public Race(final IGameContext gameContext, final Stats stats, final RaceVariant variant, final double gameSeed) {
        this(gameContext, stats, variant, new ReplayablePseudoRandom(gameSeed));
    }

    private Race(final IGameContext gameContext, final Stats stats, final RaceVariant variant,
                 final ReplayablePseudoRandom random) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;
        this.random = random;
        for (int i = 0; i < BUG_COLORS.length; i++) {
            bugImages[i] = new Image(IMAGES + BUG_COLORS[i] + ".png");
        }
    }

    @Override
    public void launch() {
        raceOver = false;
        caught = 0;
        getChildren().clear();
        animations.clear();
        bugs.clear();
        racers.clear();
        dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        gameContext.setLimiterAvailable();

        getChildren().add(createBackground());
        createLanes();
        createCatcher();
        updateScorePill();
        for (int i = 0; i < BUGS; i++) {
            addBug();
        }

        // Already there when the game area was not cleared before a new race
        if (!gameContext.getChildren().contains(this)) {
            gameContext.getChildren().add(this);
        }
        gameContext.start();
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.setOffFixationLengthControl();

        for (final Racer racer : racers) {
            if (!racer.player) {
                scheduleHop(racer);
            }
        }
    }

    @Override
    public void dispose() {
        animations.forEach(Animation::stop);
        animations.clear();
        for (final Bug bug : bugs) {
            gameContext.getGazeDeviceManager().removeEventFilter(bug);
        }
        bugs.clear();
        getChildren().clear();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Scenery

    /**
     * @return the background picture, as bright as the background settings ask (none when it is disabled)
     */
    private ImageView createBackground() {
        final ImageView background = new ImageView(new Image(IMAGES + "background.png"));
        background.setFitWidth(dimension.getWidth());
        background.setFitHeight(dimension.getHeight());
        final double styleCoefficient = gameContext.getConfiguration().getBackgroundStyle()
            .accept(new BackgroundStyleVisitor<Double>() {
                @Override
                public Double visitLight() {
                    return 0.5;
                }

                @Override
                public Double visitDark() {
                    return 1.d;
                }
            });
        background.setOpacity(gameContext.getConfiguration().isBackgroundEnabled() ? styleCoefficient : 0);
        background.setMouseTransparent(true);
        return background;
    }

    private double laneTop(final int lane) {
        return dimension.getHeight() * (0.56 + lane * 0.105);
    }

    private double laneHeight() {
        return dimension.getHeight() * 0.095;
    }

    private double startX() {
        return dimension.getWidth() * 0.11;
    }

    private double finishX() {
        return dimension.getWidth() * 0.92;
    }

    /**
     * The four lanes on the pond (the first one, of the player, in the main color, with "You"), the finish line, and a
     * frog at the start of each lane.
     */
    private void createLanes() {
        for (int lane = 0; lane < LANES; lane++) {
            final boolean ofPlayer = lane == 0;
            final Rectangle strip = new Rectangle(dimension.getWidth() * 0.01, laneTop(lane),
                dimension.getWidth() * 0.98, laneHeight());
            strip.setArcWidth(laneHeight());
            strip.setArcHeight(laneHeight());
            strip.setStyle(ofPlayer
                ? "-fx-fill: rgba(255, 255, 255, 0.35); -fx-stroke: -gp-accent; -fx-stroke-width: 4;"
                : "-fx-fill: rgba(255, 255, 255, 0.18);");
            strip.setMouseTransparent(true);
            getChildren().add(strip);
        }
        getChildren().add(createFinishLine());

        final Group you = GameStyle.pill(gameContext.getTranslator().translate("RaceYou"), laneHeight() * 0.32);
        you.setLayoutX(dimension.getWidth() * 0.018);
        you.setLayoutY(laneTop(0) + (laneHeight() - you.getBoundsInLocal().getHeight()) / 2);
        you.setMouseTransparent(true);
        getChildren().add(you);

        player = new Racer("green", 0, true, laneHeight() * 1.15);
        racers.add(player);
        for (int i = 0; i < OPPONENT_COLORS.length; i++) {
            final Racer opponent = new Racer(OPPONENT_COLORS[i], i + 1, false, laneHeight() * 0.95);
            final double raceSeconds = variant.getMinRaceSeconds()
                + random.nextDouble() * (variant.getMaxRaceSeconds() - variant.getMinRaceSeconds());
            opponent.hopInterval = raceSeconds * 1000 / HOPS_TO_FINISH;
            racers.add(opponent);
        }
        for (final Racer racer : racers) {
            getChildren().add(racer.view);
        }
    }

    /**
     * @return a checkered band across the lanes, at the finish
     */
    private Group createFinishLine() {
        final Group line = new Group();
        final double top = laneTop(0) - dimension.getHeight() * 0.01;
        final double bottom = laneTop(LANES - 1) + laneHeight() + dimension.getHeight() * 0.01;
        final double square = dimension.getHeight() * 0.018;
        int row = 0;
        for (double y = top; y < bottom; y += square, row++) {
            for (int column = 0; column < 2; column++) {
                final Rectangle cell = new Rectangle(finishX() + column * square, y, square,
                    Math.min(square, bottom - y));
                cell.setFill((row + column) % 2 == 0 ? Color.WHITE : Color.rgb(34, 30, 38));
                line.getChildren().add(cell);
            }
        }
        line.setMouseTransparent(true);
        return line;
    }

    /**
     * The frog of the bank, catching the bugs.
     */
    private void createCatcher() {
        catcher = new ImageView(catcherImage);
        catcher.setPreserveRatio(true);
        catcher.setFitHeight(dimension.getHeight() * 0.17);
        final double width = catcher.getFitHeight() * catcherImage.getWidth() / catcherImage.getHeight();
        catcher.setLayoutX((dimension.getWidth() - width) / 2);
        catcher.setLayoutY(dimension.getHeight() * 0.535 - catcher.getFitHeight());
        catcher.setMouseTransparent(true);
        getChildren().add(catcher);
    }

    private double mouthX() {
        final double width = catcher.getFitHeight() * catcherImage.getWidth() / catcherImage.getHeight();
        return catcher.getLayoutX() + width * CATCHER_MOUTH_X;
    }

    private double mouthY() {
        return catcher.getLayoutY() + catcher.getFitHeight() * CATCHER_MOUTH_Y;
    }

    /**
     * The number of bugs caught, in a label at the top left.
     */
    private void updateScorePill() {
        if (scorePill != null) {
            getChildren().remove(scorePill);
        }
        scorePill = GameStyle.pill(gameContext.getTranslator().translate("RaceCaught") + " " + caught,
            dimension.getHeight() * 0.03);
        scorePill.setLayoutX(dimension.getWidth() * 0.015);
        scorePill.setLayoutY(dimension.getHeight() * 0.02);
        scorePill.setMouseTransparent(true);
        getChildren().add(scorePill);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Bugs

    private double bugSize() {
        return dimension.getHeight() * 0.085;
    }

    /**
     * Sends a new bug in the sky, at a random place, of a random color.
     */
    private void addBug() {
        final Bug bug = new Bug(bugImages[random.nextInt(bugImages.length)], flashImage, bugSize());
        bug.setLayoutX(randomBugX());
        bug.setLayoutY(randomBugY());
        bug.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> catchBug(bug));
        bug.addEventHandler(GazeEvent.GAZE_ENTERED, e -> catchBug(bug));
        gameContext.getGazeDeviceManager().addEventFilter(bug);
        bugs.add(bug);
        // Behind the frog of the bank and the score
        getChildren().add(getChildren().indexOf(catcher), bug);
        stats.incrementNumberOfGoalsToReach();
        fly(bug);
    }

    private double randomBugX() {
        return dimension.getWidth() * 0.04 + random.nextDouble() * (dimension.getWidth() * 0.92 - bugSize());
    }

    private double randomBugY() {
        return dimension.getHeight() * 0.1 + random.nextDouble() * (dimension.getHeight() * 0.36 - bugSize());
    }

    /**
     * Moves a bug to another random place of the sky, then again, until it is caught.
     */
    private void fly(final Bug bug) {
        final double seconds = 3 + random.nextDouble() * 3;
        final TranslateTransition flight = new TranslateTransition(Duration.seconds(seconds), bug);
        flight.setToX(randomBugX() - bug.getLayoutX());
        flight.setToY(randomBugY() - bug.getLayoutY());
        flight.setInterpolator(Interpolator.EASE_BOTH);
        flight.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
        flight.setOnFinished(e -> {
            animations.remove(flight);
            if (!bug.caught) {
                fly(bug);
            }
        });
        bug.flight = flight;
        play(flight);
    }

    /**
     * The frog of the bank shoots its tongue at the bug, which is caught: counted, then replaced by another one; the
     * frog of the player hops.
     */
    private void catchBug(final Bug bug) {
        if (bug.caught || raceOver) {
            return;
        }
        bug.caught = true;
        bug.flight.stop();
        animations.remove(bug.flight);
        gameContext.getGazeDeviceManager().removeEventFilter(bug);
        stats.incrementNumberOfGoalsReached();
        caught++;
        updateScorePill();
        gameContext.getSoundManager().add("data/race/sounds/frog.WAV");
        bug.flash.setOpacity(1);

        final double bugX = bug.getLayoutX() + bug.getTranslateX() + bugSize() / 2;
        final double bugY = bug.getLayoutY() + bug.getTranslateY() + bugSize() / 2;
        final Line tongue = new Line(mouthX(), mouthY(), mouthX(), mouthY());
        tongue.setStroke(Color.rgb(240, 120, 150));
        tongue.setStrokeWidth(dimension.getHeight() * 0.012);
        tongue.setStrokeLineCap(StrokeLineCap.ROUND);
        tongue.setMouseTransparent(true);
        getChildren().add(getChildren().indexOf(catcher), tongue);

        // The tongue reaches the bug, then brings it back to the mouth
        final double toX = mouthX() - bugSize() / 2 - bug.getLayoutX();
        final double toY = mouthY() - bugSize() / 2 - bug.getLayoutY();
        final Timeline shot = new Timeline(
            new KeyFrame(Duration.millis(140), new KeyValue(tongue.endXProperty(), bugX),
                new KeyValue(tongue.endYProperty(), bugY),
                new KeyValue(bug.translateXProperty(), bug.getTranslateX()),
                new KeyValue(bug.translateYProperty(), bug.getTranslateY())),
            new KeyFrame(Duration.millis(340), new KeyValue(tongue.endXProperty(), mouthX()),
                new KeyValue(tongue.endYProperty(), mouthY()),
                new KeyValue(bug.translateXProperty(), toX),
                new KeyValue(bug.translateYProperty(), toY),
                new KeyValue(bug.scaleXProperty(), 0.3),
                new KeyValue(bug.scaleYProperty(), 0.3)));
        shot.setOnFinished(e -> {
            animations.remove(shot);
            getChildren().removeAll(tongue, bug);
            bugs.remove(bug);
            if (!raceOver) {
                addBug();
            }
        });
        play(shot);

        hop(player);
        gameContext.updateScore(stats, this);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Racers

    /**
     * The next hop of another frog, after its interval (a little shorter or longer each time).
     */
    private void scheduleHop(final Racer racer) {
        final PauseTransition wait = new PauseTransition(
            Duration.millis(racer.hopInterval * (0.75 + random.nextDouble() * 0.5)));
        wait.setOnFinished(e -> {
            animations.remove(wait);
            if (!raceOver) {
                hop(racer);
                scheduleHop(racer);
            }
        });
        play(wait);
    }

    /**
     * A frog hops one step forward (in its jumping pose, along an arc); the race ends when it reaches the finish.
     */
    void hop(final Racer racer) {
        if (raceOver || racer.hops >= HOPS_TO_FINISH) {
            return;
        }
        racer.hops++;
        final double step = (finishX() - startX() - racer.width()) / HOPS_TO_FINISH;
        final double height = laneHeight() * 0.6;
        racer.view.setImage(racer.jump);
        final Timeline jump = new Timeline(
            new KeyFrame(HOP.divide(2), new KeyValue(racer.view.translateYProperty(), -height, Interpolator.EASE_OUT)),
            new KeyFrame(HOP, new KeyValue(racer.view.translateXProperty(), racer.hops * step),
                new KeyValue(racer.view.translateYProperty(), 0, Interpolator.EASE_IN)));
        jump.setOnFinished(e -> {
            animations.remove(jump);
            racer.view.setImage(racer.sit);
        });
        play(jump);
        if (racer.hops >= HOPS_TO_FINISH) {
            endRace(racer);
        }
    }

    /**
     * Shows the ranking (the winner first, then the others by their progress), then starts a new race; "Bravo" when the
     * player wins.
     */
    private void endRace(final Racer winner) {
        raceOver = true;
        final List<Racer> ranking = new ArrayList<>(racers);
        ranking.sort(Comparator.comparingInt((Racer racer) -> racer == winner ? 1 : 0)
            .thenComparingInt(racer -> racer.hops).reversed());
        final StringBuilder text = new StringBuilder(gameContext.getTranslator()
            .translate(winner.player ? "RaceWon" : "RaceLost"));
        for (int i = 0; i < ranking.size(); i++) {
            text.append('\n').append(i + 1).append(". ")
                .append(gameContext.getTranslator().translate(ranking.get(i).nameKey));
        }
        final Group card = GameStyle.card(text.toString(), dimension.getHeight() * 0.04, dimension.getWidth() * 0.5);
        card.setLayoutX((dimension.getWidth() - card.getBoundsInLocal().getWidth()) / 2);
        card.setLayoutY((dimension.getHeight() - card.getBoundsInLocal().getHeight()) / 2);
        getChildren().add(card);

        final PauseTransition show = new PauseTransition(Duration.seconds(3.5));
        show.setOnFinished(e -> {
            if (winner.player) {
                getChildren().remove(card);
                gameContext.playWinTransition(200, event -> restart());
            } else {
                restart();
            }
        });
        play(show);
    }

    private void restart() {
        dispose();
        gameContext.clear();
        launch();
    }

    private void play(final Animation animation) {
        animations.add(animation);
        animation.play();
    }

    /**
     * A frog of the race: its pictures (sitting, jumping), its lane, its hops.
     */
    final class Racer {
        final Image sit;
        final Image jump;
        final ImageView view;
        final boolean player;
        final String nameKey;
        int hops;
        double hopInterval;

        Racer(final String color, final int lane, final boolean player, final double height) {
            this.player = player;
            this.nameKey = player ? "RaceYou" : "RaceFrog_" + color;
            sit = new Image(IMAGES + "frog-" + color + ".png");
            jump = new Image(IMAGES + "frog-" + color + "-jump.png");
            view = new ImageView(sit);
            view.setPreserveRatio(true);
            view.setFitHeight(height);
            view.setLayoutX(startX());
            view.setLayoutY(laneTop(lane) + laneHeight() - height);
            view.setMouseTransparent(true);
        }

        double width() {
            return view.getFitHeight() * sit.getWidth() / sit.getHeight();
        }
    }

    /**
     * A flying bug: its picture, and the flash shown when it is caught.
     */
    static final class Bug extends StackPane {
        final ImageView flash;
        TranslateTransition flight;
        boolean caught;

        Bug(final Image picture, final Image flashPicture, final double size) {
            final ImageView view = new ImageView(picture);
            view.setPreserveRatio(true);
            view.setFitHeight(size);
            flash = new ImageView(flashPicture);
            flash.setPreserveRatio(true);
            flash.setFitHeight(size * 1.3);
            flash.setOpacity(0);
            getChildren().addAll(flash, view);
            setPrefSize(size * 1.4, size);
            setMinSize(size * 1.4, size);
            setMaxSize(size * 1.4, size);
        }
    }
}
