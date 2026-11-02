package net.gazeplay.games.egg;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Game "Egg Game" (reviewed on 2/11/2026, maintainer's rules): an egg in its nest; looking at it cracks it (a ring of
 * the main color goes round it during the fixation length of the settings): staying on it cracks it again and again,
 * there is no need to look away. After the number of fixations of the variant (2 to 5), it hatches: the top of the
 * shell flies off and what was inside comes out (an animal born from an egg, or an image of the user). Then a new egg,
 * without end (the game is left by the gear).
 */
@Slf4j
public class EggGame implements GameLifeCycle {

    static final String FOLDER = "data/egg/";

    static final List<String> ANIMALS = List.of("chick", "duck", "turtle", "crocodile", "snake", "sauropod", "t-rex",
        "penguin", "lizard", "bird", "owl", "parrot", "swan", "flamingo");

    static final String CRACK_SOUND = FOLDER + "sounds/crack.wav";
    static final String HATCH_SOUND = FOLDER + "sounds/hatch.wav";

    /**
     * How long what came out of the egg stays shown before a new egg.
     */
    static final Duration HATCHED_SHOWN = Duration.millis(3200);

    /**
     * Where the nest is in the meadow (part of the width and of the height, see make_egg_images.py).
     */
    static final double NEST_X = 0.5;
    static final double NEST_Y = 700.0 / 900;

    enum Phase { READY, HATCHED, OVER }

    private final IGameContext gameContext;
    private final Stats stats;
    @Getter
    private final int steps;
    @Getter
    private final EggContent content;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;

    /**
     * The cracks made on the egg shown.
     */
    @Getter
    private int cracks;

    /**
     * The eggs hatched since the start.
     */
    @Getter
    private int hatched;

    private double width;
    private double height;
    private double eggWidth;
    private double eggHeight;
    private double eggX;
    private double eggY;

    private Group layer;
    private Group eggLayer;
    private Group egg;
    private Group crackLines;
    private Group hud;
    private Arc ring;
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private Timeline fixation;
    private PauseTransition next;
    private boolean looked;

    /**
     * The zigzag line where the egg splits, from left to right, in the coordinates of the egg (its center at 0, 0).
     */
    private double[] splitLine;

    /**
     * What comes out of the eggs (URLs), and the next one.
     */
    private final List<String> insides = new ArrayList<>();
    private int insideIndex;
    private boolean userImages;

    private final EventHandler<Event> handler = this::handle;

    public EggGame(final IGameContext gameContext, final Stats stats, final int steps, final EggContent content) {
        this(gameContext, stats, steps, content, -1);
    }

    /**
     * @param gameSeed the seed of the game to replay, or a negative value for a new game
     */
    public EggGame(final IGameContext gameContext, final Stats stats, final int steps, final EggContent content,
                   final double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.steps = Math.max(1, steps);
        this.content = content;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();
        eggHeight = height * 0.46;
        eggWidth = eggHeight * 0.76;
        eggX = width * NEST_X;
        // The bottom of the egg a little in the nest: its front edge hides it
        eggY = height * NEST_Y + height * 0.045 - eggHeight / 2;

        final ImageView meadow = picture("meadow");
        final ImageView nestFront = picture("nest-front");
        eggLayer = new Group();
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(meadow, eggLayer, nestFront, hud);
        meadow.setMouseTransparent(true);
        nestFront.setMouseTransparent(true);
        gameContext.getChildren().add(layer);

        if (content == EggContent.MY_IMAGES) {
            final List<File> images = UserImagesVariant.images(EggContent.USER_IMAGES_FOLDER);
            images.forEach(file -> insides.add(file.toURI().toString()));
            userImages = !insides.isEmpty();
        }
        if (insides.isEmpty()) {
            ANIMALS.forEach(animal -> insides.add(FOLDER + "images/animals/" + animal + ".png"));
        }
        shuffle(insides);

        // The ring of the fixation of the egg shown
        progress.addListener((observable, oldValue, newValue) -> {
            if (ring != null) {
                ring.setLength(-360 * newValue.doubleValue());
            }
        });
        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        gameContext.firstStart();
        newEgg();
    }

    private ImageView picture(final String name) {
        final ImageView view = new ImageView(new Image(FOLDER + "images/" + name + ".png"));
        view.setFitWidth(width);
        view.setFitHeight(height);
        return view;
    }

    private void shuffle(final List<String> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            final int j = random.nextInt(i + 1);
            final String other = list.get(i);
            list.set(i, list.get(j));
            list.set(j, other);
        }
    }

    /**
     * @return the outline of an egg, pointed at the top, centered on (0, 0)
     */
    private Path eggShape() {
        final double w = eggWidth / 2;
        final double h = eggHeight / 2;
        final Path path = new Path(new MoveTo(0, -h),
            new CubicCurveTo(w * 0.84, -h, w, -h * 0.1, w, h * 0.24),
            new CubicCurveTo(w, h * 0.72, w * 0.56, h, 0, h),
            new CubicCurveTo(-w * 0.56, h, -w, h * 0.72, -w, h * 0.24),
            new CubicCurveTo(-w, -h * 0.1, -w * 0.84, -h, 0, -h),
            new ClosePath());
        path.setFill(new RadialGradient(0, 0, 0.38, 0.32, 0.8, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#FFFDF7")), new Stop(1, Color.web("#EED9B8"))));
        path.setStroke(Color.web("#B8956A"));
        path.setStrokeWidth(Math.max(2, eggWidth * 0.012));
        return path;
    }

    /**
     * The shell: the egg, a few freckles.
     */
    private Group shell() {
        final Group shell = new Group(eggShape());
        final double[][] freckles = {{-0.18, -0.25, 0.035}, {0.22, -0.05, 0.03}, {-0.05, 0.3, 0.04},
            {0.12, 0.42, 0.025}, {-0.3, 0.15, 0.025}};
        for (final double[] freckle : freckles) {
            final Ellipse spot = new Ellipse(freckle[0] * eggWidth, freckle[1] * eggHeight, freckle[2] * eggWidth,
                freckle[2] * eggWidth * 0.8);
            spot.setFill(Color.web("#D9BF97", 0.7));
            shell.getChildren().add(spot);
        }
        return shell;
    }

    /**
     * A new egg, whole, in the nest.
     */
    void newEgg() {
        stopFixation();
        eggLayer.getChildren().clear();
        cracks = 0;
        looked = false;
        // The line where it will split: a zigzag across, a little above the middle
        final int points = 11;
        splitLine = new double[points * 2];
        for (int i = 0; i < points; i++) {
            splitLine[2 * i] = -eggWidth * 0.56 + eggWidth * 1.12 * i / (points - 1);
            splitLine[2 * i + 1] = -eggHeight * 0.02 + (i % 2 == 0 ? -1 : 1) * eggHeight * (0.035
                + random.nextDouble() * 0.03);
        }
        crackLines = new Group();
        crackLines.setMouseTransparent(true);
        ring = new Arc(0, 0, eggWidth / 2 + eggWidth * 0.07, eggHeight / 2 + eggWidth * 0.07, 90, 0);
        ring.setType(ArcType.OPEN);
        ring.setFill(null);
        ring.setStrokeWidth(Math.max(5, eggWidth * 0.035));
        ring.setStrokeLineCap(StrokeLineCap.ROUND);
        ring.setStyle("-fx-stroke: -gp-accent;");
        ring.setMouseTransparent(true);
        egg = new Group(shell(), crackLines, ring);
        egg.setLayoutX(eggX);
        egg.setLayoutY(eggY);
        egg.setOpacity(0);
        eggLayer.getChildren().add(egg);
        gameContext.getGazeDeviceManager().addEventFilter(egg);
        egg.addEventFilter(GazeEvent.ANY, handler);
        egg.addEventFilter(MouseEvent.ANY, handler);
        final FadeTransition appear = new FadeTransition(Duration.millis(400), egg);
        appear.setToValue(1);
        appear.play();
        for (int i = 0; i < steps; i++) {
            stats.incrementNumberOfGoalsToReach();
        }
        phase = Phase.READY;
        showCount();
    }

    private void showCount() {
        hud.getChildren().clear();
        final Group count = GameStyle.pill(gameContext.getTranslator().translate("EggHatched") + " " + hatched,
            height * 0.032);
        count.setLayoutX((width - count.getBoundsInLocal().getWidth()) / 2);
        count.setLayoutY(height * 0.02);
        hud.getChildren().add(count);
    }

    private void handle(final Event event) {
        if (event.getEventType() == GazeEvent.GAZE_ENTERED || event.getEventType() == MouseEvent.MOUSE_ENTERED) {
            looked = true;
            startFixation();
        } else if (event.getEventType() == GazeEvent.GAZE_EXITED || event.getEventType() == MouseEvent.MOUSE_EXITED) {
            looked = false;
            stopFixation();
        }
    }

    private void startFixation() {
        if (phase != Phase.READY) {
            return;
        }
        stopFixation();
        fixation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(progress, 0)),
            new KeyFrame(Duration.millis(gameContext.getConfiguration().getFixationLength()),
                new KeyValue(progress, 1)));
        fixation.setOnFinished(event -> {
            crack();
            // Still looked at: the next crack comes without looking away
            if (looked) {
                startFixation();
            }
        });
        fixation.playFromStart();
    }

    private void stopFixation() {
        if (fixation != null) {
            fixation.stop();
            fixation = null;
        }
        progress.set(0);
    }

    /**
     * A fixation on the egg: a crack more (the egg shakes), or it hatches at the last one.
     */
    void crack() {
        if (phase != Phase.READY) {
            return;
        }
        cracks++;
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, this);
        if (cracks >= steps) {
            hatch();
            return;
        }
        gameContext.getSoundManager().add(CRACK_SOUND);
        drawCracks();
        final RotateTransition shake = new RotateTransition(Duration.millis(70), egg);
        shake.setFromAngle(-5);
        shake.setToAngle(5);
        shake.setAutoReverse(true);
        shake.setCycleCount(6);
        shake.setOnFinished(event -> egg.setRotate(0));
        shake.play();
    }

    /**
     * The cracks of the egg: the split line grows from the middle at each crack, with small branches.
     */
    private void drawCracks() {
        crackLines.getChildren().clear();
        final int points = splitLine.length / 2;
        final int middle = points / 2;
        final int reach = (int) Math.ceil((double) middle * cracks / Math.max(1, steps - 1));
        final Polyline line = new Polyline();
        for (int i = Math.max(0, middle - reach); i <= Math.min(points - 1, middle + reach); i++) {
            line.getPoints().addAll(splitLine[2 * i], splitLine[2 * i + 1]);
        }
        styleCrack(line);
        crackLines.getChildren().add(line);
        for (int i = Math.max(1, middle - reach); i < Math.min(points - 1, middle + reach); i += 3) {
            final Polyline branch = new Polyline(splitLine[2 * i], splitLine[2 * i + 1],
                splitLine[2 * i] + eggWidth * 0.04, splitLine[2 * i + 1] + (i % 2 == 0 ? -1 : 1) * eggHeight * 0.07);
            styleCrack(branch);
            crackLines.getChildren().add(branch);
        }
    }

    private void styleCrack(final Polyline line) {
        line.setStroke(Color.web("#6B4E2E"));
        line.setStrokeWidth(Math.max(2, eggWidth * 0.018));
        line.setStrokeLineJoin(StrokeLineJoin.MITER);
        line.setFill(null);
    }

    /**
     * The egg hatches: it splits along its crack, the top flies off, what was inside comes out with a little jump.
     */
    private void hatch() {
        phase = Phase.HATCHED;
        stopFixation();
        hatched++;
        stats.takeScreenShot();
        gameContext.getSoundManager().add(HATCH_SOUND);
        gameContext.getGazeDeviceManager().removeEventFilter(egg);
        eggLayer.getChildren().remove(egg);

        final Group bottom = half(false);
        final Group top = half(true);
        final Node inside = inside();
        eggLayer.getChildren().addAll(inside, bottom, top);

        final TranslateTransition fly = new TranslateTransition(Duration.millis(700), top);
        fly.setByY(-eggHeight * 0.45);
        fly.setByX(eggWidth * 0.35);
        final RotateTransition turn = new RotateTransition(Duration.millis(700), top);
        turn.setByAngle(35);
        final FadeTransition away = new FadeTransition(Duration.millis(500), top);
        away.setDelay(Duration.millis(900));
        away.setToValue(0);
        final TranslateTransition rise = new TranslateTransition(Duration.millis(650), inside);
        rise.setFromY(eggHeight * 0.3);
        // High enough to stand out of the bottom of the shell, only its feet inside
        rise.setToY(-eggHeight * 0.42);
        rise.setInterpolator(Interpolator.EASE_OUT);
        final ScaleTransition grow = new ScaleTransition(Duration.millis(650), inside);
        grow.setFromX(0.4);
        grow.setFromY(0.4);
        grow.setToX(1);
        grow.setToY(1);
        final TranslateTransition hop = new TranslateTransition(Duration.millis(220), inside);
        hop.setDelay(Duration.millis(800));
        hop.setByY(-eggHeight * 0.08);
        hop.setAutoReverse(true);
        hop.setCycleCount(4);
        new ParallelTransition(fly, turn, away, rise, grow, hop).play();
        showCount();

        next = new PauseTransition(HATCHED_SHOWN);
        next.setOnFinished(event -> {
            final FadeTransition fade = new FadeTransition(Duration.millis(350), eggLayer);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                eggLayer.setOpacity(1);
                if (phase == Phase.HATCHED) {
                    newEgg();
                }
            });
            fade.play();
        });
        next.play();
    }

    /**
     * @return the top or the bottom of the shell, cut along the split line
     */
    private Group half(final boolean upper) {
        final Group shell = shell();
        final Polygon clip = new Polygon(splitLine);
        final double edge = eggHeight;
        clip.getPoints().addAll(eggWidth, upper ? -edge : edge, -eggWidth, upper ? -edge : edge);
        shell.setClip(clip);
        final Polyline rim = new Polyline(splitLine);
        styleCrack(rim);
        final Group half = new Group(shell, rim);
        half.setLayoutX(eggX);
        half.setLayoutY(eggY);
        half.setMouseTransparent(true);
        return half;
    }

    /**
     * @return what comes out of the egg: an animal, or an image of the user in a round frame
     */
    private Node inside() {
        final String url = insides.get(insideIndex % insides.size());
        insideIndex++;
        final double size = eggWidth * 1.25;
        final ImageView view = new ImageView(new Image(url, size * 1.5, size * 1.5, true, true));
        view.setPreserveRatio(true);
        view.setSmooth(true);
        final Group group;
        if (userImages) {
            // Fills a round frame
            final double ratio = view.getImage().getWidth() <= 0 ? 1
                : view.getImage().getWidth() / view.getImage().getHeight();
            if (ratio > 1) {
                view.setFitHeight(size);
            } else {
                view.setFitWidth(size);
            }
            view.setLayoutX(-view.getBoundsInLocal().getWidth() / 2);
            view.setLayoutY(-view.getBoundsInLocal().getHeight() / 2);
            view.setClip(new Circle(view.getBoundsInLocal().getWidth() / 2, view.getBoundsInLocal().getHeight() / 2,
                size / 2));
            final Circle frame = new Circle(size / 2 + 6, Color.WHITE);
            frame.setStroke(Color.web("#B8956A"));
            frame.setStrokeWidth(4);
            group = new Group(frame, view);
            group.setLayoutY(eggY - size * 0.15);
        } else {
            view.setFitWidth(size);
            view.setFitHeight(size);
            view.setLayoutX(-size / 2);
            view.setLayoutY(-size / 2);
            group = new Group(view);
            group.setLayoutY(eggY);
        }
        group.setLayoutX(eggX);
        group.setMouseTransparent(true);
        return group;
    }

    /**
     * For the tests: ends at once the time the hatched egg stays shown.
     */
    void skipPause() {
        if (next != null) {
            next.stop();
        }
        if (phase == Phase.HATCHED) {
            newEgg();
        }
    }

    /**
     * @return the URL of what comes out of the next egg (for the tests)
     */
    String nextInside() {
        return insides.get(insideIndex % insides.size());
    }

    /**
     * @return the egg (for the tests)
     */
    Group egg() {
        return egg;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        stopFixation();
        if (next != null) {
            next.stop();
        }
        if (egg != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(egg);
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }
}
