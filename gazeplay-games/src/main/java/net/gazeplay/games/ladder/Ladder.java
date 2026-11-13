package net.gazeplay.games.ladder;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * "Échelle" (name code Ladder; reviewed on 13/11/2026, maintainer's rules): a Japanese ladder (amidakuji), everything
 * drawn by the game: 3, 4 or 5 colored ropes joined by horizontal rungs drawn at random (never two rungs at one point of
 * a rope, so that each start leads to its own picture), round starts at the top, pictures at the bottom (the animals of
 * the default images, all different, or the images of the user). A fixation on a start sends the Biboule down its
 * rope: it takes each rung it meets, its way drawn behind it.
 * <p>
 * Watch: wherever it arrives, the picture jumps, a star and "Bravo !", then another ladder. Aim: a picture is framed
 * ("Amène la Biboule jusqu'à l'image entourée"); the right start gives "Bravo !" and another ladder; another one shows
 * where the Biboule went, the picture shakes, "Pas encore !", a mistake (no lives), and the same ladder again; after two
 * mistakes, the right start is framed. The game ends after 3, 5 or 10 games ("Bravo", then the statistics), or has no
 * end.
 * <p>
 * One loop, in real seconds: the fixation lasts the length of the settings (read at each step), nothing can be chosen
 * before the delay of the settings, while the Biboule goes down, nor while the menu of the game is open. (Before the
 * review: no goal, the game over after the first descent, slanted rungs, black lines on white, five buttons copied,
 * the speed taken from the animation setting, nothing removed at the end.)
 */
@Slf4j
public class Ladder implements GameLifeCycle {

    /**
     * The rows of the ladder (the rungs go on rows 1 to ROWS - 1), the speed of the Biboule in heights of the game per
     * second, the seconds a wrong way is shown and the reward lasts, and the mistakes on a ladder before the hint.
     */
    static final int ROWS = 10;
    static final double SPEED = 0.35;
    static final double RESULT = 1.8;
    static final double REWARD = 2;
    static final int HINT_AFTER = 2;

    private static final Color[] ROPES = {Color.rgb(230, 110, 100), Color.rgb(90, 150, 220), Color.rgb(110, 185, 120),
        Color.rgb(230, 175, 60), Color.rgb(165, 120, 210)};
    private static final String BIBOULE = "data/shooter/biboule/images/Blue.png";

    enum Phase { WAIT, CHOOSE, RUN, RESULT, REWARD, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final LadderMode mode;
    @Getter
    private final LadderLevel level;
    @Getter
    private final LadderEnd end;
    @Getter
    private final LadderPictures pictures;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    /**
     * For each gap between two ropes, the rows of its rungs.
     */
    private final List<Set<Integer>> rungs = new ArrayList<>();
    @Getter
    private int target = -1;
    @Getter
    private int games;
    @Getter
    private int mistakesOnLadder;
    @Getter
    private int arrival = -1;
    private double elapsed;
    private double phaseTime;
    private boolean rewarded;
    private List<double[]> way = List.of();
    private double travelled;
    private double wayLength;

    private double width;
    private double height;
    private double left;
    private double columnWidth;
    private double top;
    private double rowHeight;

    private Group layer;
    private Group ladderLayer;
    private Group wayLayer;
    private Group picturesLayer;
    private Group startsLayer;
    private Group hud;
    private Group message;
    private FixationRing ring;
    private ImageView biboule;
    private final List<Start> starts = new ArrayList<>();
    private final List<Group> pictureNodes = new ArrayList<>();
    private List<File> userImages = List.of();
    @Getter
    private Start looked;
    @Getter
    private double progress;
    private double shake;
    private double jump;

    private final AnimationTimer loop = new AnimationTimer() {
        private long last = -1;

        @Override
        public void handle(final long now) {
            final double seconds = last < 0 ? 0 : Math.min(0.05, (now - last) / 1e9);
            last = now;
            if (!Stats.isConfigMenuOpen()) {
                step(seconds);
            }
        }

        @Override
        public void stop() {
            super.stop();
            last = -1;
        }
    };
    private boolean manual;

    /**
     * @param gameSeed the seed of the game to replay, or a negative value for a new game
     */
    Ladder(final IGameContext gameContext, final Stats stats, final LadderMode mode, final LadderLevel level,
           final LadderEnd end, final LadderPictures pictures, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.level = level;
        this.end = end;
        this.pictures = pictures;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
    }

    @Override
    public void launch() {
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();
        left = width * 0.15;
        columnWidth = width * 0.7 / (level.getLines() - 1);
        top = height * 0.27;
        rowHeight = height * 0.48 / ROWS;
        final Rectangle room = new Rectangle(0, 0, width, height);
        room.setStyle("-fx-fill: -gp-bg;");
        room.setMouseTransparent(true);
        ladderLayer = new Group();
        ladderLayer.setMouseTransparent(true);
        wayLayer = new Group();
        wayLayer.setMouseTransparent(true);
        picturesLayer = new Group();
        picturesLayer.setMouseTransparent(true);
        startsLayer = new Group();
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        biboule = new ImageView(new Image(BIBOULE));
        biboule.setPreserveRatio(true);
        biboule.setFitHeight(height * 0.08);
        biboule.setVisible(false);
        biboule.setMouseTransparent(true);
        layer = new Group(room, ladderLayer, wayLayer, picturesLayer, startsLayer, biboule, ring, hud, message);
        gameContext.getChildren().add(layer);
        gameContext.getGazeDeviceManager().addStats(stats);
        if (pictures == LadderPictures.MY_IMAGES) {
            userImages = UserImagesVariant.images(LadderPictures.USER_IMAGES_FOLDER);
            if (userImages.isEmpty()) {
                // The game started without going through the menu, which asks for images
                final Group card = GameStyle.card(gameContext.getTranslator().translate("NoUserImages"),
                    height * 0.035, width * 0.5);
                card.setLayoutX((width - card.getBoundsInLocal().getWidth()) / 2);
                card.setLayoutY((height - card.getBoundsInLocal().getHeight()) / 2);
                hud.getChildren().add(card);
                return;
            }
        }
        buildStarts();
        games = 0;
        newLadder();
        phase = Phase.WAIT;
        phaseTime = 0;
        elapsed = 0;
        if (!manual) {
            loop.start();
        }
    }

    private double x(final int column) {
        return left + column * columnWidth;
    }

    private double y(final double row) {
        return top + row * rowHeight;
    }

    private double startRadius() {
        return Math.min(height * 0.06, columnWidth * 0.3);
    }

    // --- The ladder ---

    /**
     * A new ladder: its rungs, its pictures, and in the aim the picture to reach.
     */
    private void newLadder() {
        rungs.clear();
        Set<Integer> previous = Set.of();
        for (int gap = 0; gap < level.getLines() - 1; gap++) {
            final List<Integer> free = new ArrayList<>();
            for (int row = 1; row < ROWS; row++) {
                if (!previous.contains(row)) {
                    free.add(row);
                }
            }
            Collections.shuffle(free, new java.util.Random(random.nextInt(Integer.MAX_VALUE)));
            final Set<Integer> rows = new HashSet<>(free.subList(0, Math.min(level.getRungs(), free.size())));
            rungs.add(rows);
            previous = rows;
        }
        drawLadder();
        drawPictures();
        clearWay();
        mistakesOnLadder = 0;
        target = mode == LadderMode.AIM ? random.nextInt(level.getLines()) : -1;
        showTarget();
        updateHint();
        message.getChildren().clear();
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        showHud();
    }

    private void drawLadder() {
        ladderLayer.getChildren().clear();
        final double stroke = Math.max(5, height * 0.012);
        for (int gap = 0; gap < rungs.size(); gap++) {
            for (final int row : rungs.get(gap)) {
                final Line rung = new Line(x(gap), y(row), x(gap + 1), y(row));
                rung.setStroke(Color.rgb(150, 110, 75));
                rung.setStrokeWidth(stroke);
                rung.setStrokeLineCap(StrokeLineCap.ROUND);
                ladderLayer.getChildren().add(rung);
            }
        }
        for (int column = 0; column < level.getLines(); column++) {
            final Line rope = new Line(x(column), y(0), x(column), y(ROWS));
            rope.setStroke(ROPES[column % ROPES.length]);
            rope.setStrokeWidth(stroke * 1.2);
            rope.setStrokeLineCap(StrokeLineCap.ROUND);
            ladderLayer.getChildren().add(rope);
        }
    }

    private void drawPictures() {
        picturesLayer.getChildren().clear();
        pictureNodes.clear();
        final List<Image> images = images(level.getLines());
        final double size = Math.min(height * 0.14, columnWidth * 0.75);
        for (int column = 0; column < level.getLines(); column++) {
            final Rectangle card = new Rectangle(-size / 2 - 6, -size / 2 - 6, size + 12, size + 12);
            card.setArcWidth(20);
            card.setArcHeight(20);
            card.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
            final ImageView view = new ImageView(images.get(column));
            view.setPreserveRatio(true);
            view.setFitWidth(size);
            view.setFitHeight(size);
            final double iw = view.getBoundsInLocal().getWidth();
            final double ih = view.getBoundsInLocal().getHeight();
            view.setX(-iw / 2);
            view.setY(-ih / 2);
            final Group node = new Group(card, view);
            node.setLayoutX(x(column));
            node.setLayoutY(y(ROWS) + height * 0.025 + size / 2 + 6);
            pictureNodes.add(node);
            picturesLayer.getChildren().add(node);
        }
    }

    /**
     * @return pictures for the bottom of the ladder: animals all different, or images of the user (different while
     * there are enough)
     */
    private List<Image> images(final int count) {
        final List<Image> out = new ArrayList<>();
        if (pictures == LadderPictures.MY_IMAGES) {
            final List<File> files = new ArrayList<>(userImages);
            Collections.shuffle(files, new java.util.Random(random.nextInt(Integer.MAX_VALUE)));
            for (int i = 0; i < count; i++) {
                final Image image = new Image(files.get(i % files.size()).toURI().toString(), 600, 600, true, true);
                if (!image.isError()) {
                    out.add(image);
                }
            }
        }
        if (out.size() < count) {
            out.clear();
            // Each animal is there twice (with and without background, noBackGround): one picture per animal
            final java.util.Map<String, Image> byAnimal = new java.util.LinkedHashMap<>();
            for (final Image image : ImageUtils.createDefaultImageLibrary(null, random).pickAllImages()) {
                byAnimal.putIfAbsent(animal(image), image);
            }
            final List<Image> animals = new ArrayList<>(byAnimal.values());
            Collections.shuffle(animals, new java.util.Random(random.nextInt(Integer.MAX_VALUE)));
            out.addAll(animals.subList(0, Math.min(count, animals.size())));
        }
        return out;
    }

    /**
     * @return the name of the animal of a default picture: its file name without its extension nor a number at its end
     * (elephant.jpg and elephant2.jpg are both elephants)
     */
    static String animal(final Image image) {
        final String url = image.getUrl() == null ? "" : image.getUrl();
        final String name = url.substring(url.lastIndexOf('/') + 1);
        final int dot = name.lastIndexOf('.');
        return (dot > 0 ? name.substring(0, dot) : name).replaceAll("\\d+$", "");
    }

    /**
     * @return the names of the animals at the bottom of the ladder, for the tests
     */
    List<String> getAnimals() {
        final List<String> names = new ArrayList<>();
        for (final Group node : pictureNodes) {
            names.add(animal(((ImageView) node.getChildren().get(1)).getImage()));
        }
        return names;
    }

    /**
     * In the aim, the picture to reach framed and the sentence asking for it.
     */
    private void showTarget() {
        for (int column = 0; column < pictureNodes.size(); column++) {
            final Group node = pictureNodes.get(column);
            final Rectangle card = (Rectangle) node.getChildren().get(0);
            if (column == target) {
                card.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-accent; -fx-stroke-width: 6;");
            } else {
                card.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
            }
        }
    }

    private void buildStarts() {
        for (int column = 0; column < level.getLines(); column++) {
            starts.add(new Start(column));
        }
    }

    private void updateHint() {
        final int right = mode == LadderMode.AIM && mistakesOnLadder >= HINT_AFTER ? startFor(target) : -1;
        for (final Start start : starts) {
            start.hint.setVisible(start.column == right);
        }
    }

    private void showHud() {
        final double size = height * 0.03;
        final Group count = GameStyle.pill(gameContext.getTranslator().translate("LadderGames") + " " + games
            + (end.getGames() > 0 ? " / " + end.getGames() : ""), size);
        count.setLayoutX(height * 0.03);
        count.setLayoutY(height * 0.02);
        hud.getChildren().setAll(count);
        if (mode == LadderMode.AIM) {
            final Group ask = GameStyle.pill(gameContext.getTranslator().translate("LadderAimText"), size);
            ask.setLayoutX((width - ask.getBoundsInLocal().getWidth()) / 2);
            ask.setLayoutY(height * 0.02);
            hud.getChildren().add(ask);
        }
    }

    // --- The ways ---

    /**
     * @return the points of the way from a start: down its rope, across each rung met, to the bottom
     */
    List<double[]> wayFrom(final int start) {
        final List<double[]> points = new ArrayList<>();
        int column = start;
        points.add(new double[]{column, 0});
        for (int row = 1; row < ROWS; row++) {
            if (column > 0 && rungs.get(column - 1).contains(row)) {
                points.add(new double[]{column, row});
                column--;
                points.add(new double[]{column, row});
            } else if (column < rungs.size() && rungs.get(column).contains(row)) {
                points.add(new double[]{column, row});
                column++;
                points.add(new double[]{column, row});
            }
        }
        points.add(new double[]{column, ROWS});
        return points;
    }

    /**
     * @return the picture a start leads to
     */
    int arrivalFrom(final int start) {
        final List<double[]> points = wayFrom(start);
        return (int) points.get(points.size() - 1)[0];
    }

    /**
     * @return the start leading to a picture
     */
    int startFor(final int picture) {
        for (int start = 0; start < level.getLines(); start++) {
            if (arrivalFrom(start) == picture) {
                return start;
            }
        }
        return -1;
    }

    private void clearWay() {
        wayLayer.getChildren().clear();
        biboule.setVisible(false);
        arrival = -1;
        for (final Group node : pictureNodes) {
            node.setTranslateX(0);
            node.setTranslateY(0);
        }
    }

    // --- The game ---

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        phaseTime += seconds;
        animatePicture(seconds);
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    setPhase(Phase.CHOOSE);
                }
            }
            case CHOOSE -> fixate(seconds);
            case RUN -> run(seconds);
            case RESULT -> {
                if (phaseTime >= RESULT) {
                    clearWay();
                    message.getChildren().clear();
                    updateHint();
                    setPhase(Phase.CHOOSE);
                }
            }
            case REWARD -> {
                if (!rewarded) {
                    rewarded = true;
                    showMessage("LadderWell", true);
                }
                if (phaseTime >= REWARD) {
                    if (end.getGames() > 0 && games >= end.getGames()) {
                        finish();
                    } else {
                        setPhase(Phase.CHOOSE);
                        newLadder();
                    }
                }
            }
            default -> {
            }
        }
    }

    private void setPhase(final Phase next) {
        phase = next;
        phaseTime = 0;
    }

    private void animatePicture(final double seconds) {
        if (arrival < 0) {
            return;
        }
        final Group node = pictureNodes.get(arrival);
        if (jump > 0) {
            jump = Math.max(0, jump - seconds);
            node.setTranslateY(-Math.abs(Math.sin(jump * 9)) * height * 0.03);
        }
        if (shake > 0) {
            shake = Math.max(0, shake - seconds);
            node.setTranslateX(Math.sin(shake * 40) * height * 0.012);
        }
    }

    private void fixate(final double seconds) {
        if (looked == null || Stats.isConfigMenuOpen()) {
            return;
        }
        if (!ring.isVisible()) {
            ring.setSize(startRadius() * 2.5);
            ring.show(x(looked.column), startY());
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Start start = looked;
            ring.stop();
            looked = null;
            progress = 0;
            choose(start);
        }
    }

    private double startY() {
        return top - height * 0.09;
    }

    void lookAt(final Start start) {
        if (looked == start) {
            return;
        }
        looked = start;
        progress = 0;
        ring.stop();
    }

    void lookAway(final Start start) {
        if (looked == start) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * A start chosen: the Biboule goes down from it.
     */
    void choose(final Start start) {
        if (phase != Phase.CHOOSE) {
            return;
        }
        way = wayFrom(start.column);
        wayLength = 0;
        for (int i = 1; i < way.size(); i++) {
            wayLength += length(way.get(i - 1), way.get(i));
        }
        travelled = 0;
        wayLayer.getChildren().clear();
        biboule.setVisible(true);
        placeBiboule(x(start.column), y(0));
        setPhase(Phase.RUN);
    }

    private double length(final double[] a, final double[] b) {
        return Math.hypot((b[0] - a[0]) * columnWidth, (b[1] - a[1]) * rowHeight);
    }

    private void placeBiboule(final double px, final double py) {
        biboule.setX(px - biboule.getBoundsInLocal().getWidth() / 2);
        biboule.setY(py - biboule.getBoundsInLocal().getHeight() / 2);
    }

    /**
     * The Biboule goes along its way, which is drawn behind it.
     */
    private void run(final double seconds) {
        travelled = Math.min(wayLength, travelled + seconds * SPEED * height);
        wayLayer.getChildren().clear();
        double done = 0;
        final double stroke = Math.max(8, height * 0.02);
        for (int i = 1; i < way.size(); i++) {
            final double[] a = way.get(i - 1);
            final double[] b = way.get(i);
            final double segment = length(a, b);
            final double part = Math.min(1, Math.max(0, (travelled - done) / Math.max(1e-9, segment)));
            if (part > 0) {
                final double ex = x((int) a[0]) + (x((int) b[0]) - x((int) a[0])) * part;
                final double ey = y(a[1]) + (y(b[1]) - y(a[1])) * part;
                final Line line = new Line(x((int) a[0]), y(a[1]), ex, ey);
                line.setStyle("-fx-stroke: -gp-accent;");
                line.setStrokeWidth(stroke);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
                line.setOpacity(0.75);
                wayLayer.getChildren().add(line);
                placeBiboule(ex, ey);
            }
            done += segment;
        }
        if (travelled >= wayLength) {
            arrived((int) way.get(way.size() - 1)[0]);
        }
    }

    /**
     * The Biboule at the bottom: in the watch and on the picture asked, "Bravo !"; else the picture shakes.
     */
    private void arrived(final int column) {
        arrival = column;
        if (mode == LadderMode.WATCH || column == target) {
            games++;
            stats.incrementNumberOfGoalsReached();
            gameContext.getSoundManager().add("data/goosegame/sounds/win.wav");
            jump = 1;
            rewarded = false;
            showHud();
            setPhase(Phase.REWARD);
        } else {
            mistakesOnLadder++;
            stats.incrementNumberOfMistakes();
            gameContext.getSoundManager().add("data/dice/sounds/wrong.wav");
            shake = 0.5;
            showMessage("LadderNotYet", false);
            setPhase(Phase.RESULT);
        }
    }

    /**
     * A word in the middle of the ladder: "Bravo !" with a star, or "Pas encore !".
     */
    private void showMessage(final String key, final boolean star) {
        final double r = height * 0.045;
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate(key), height * 0.045);
        final double pillWidth = pill.getBoundsInLocal().getWidth();
        final double pillHeight = pill.getBoundsInLocal().getHeight();
        final double gap = r * 0.4;
        final double total = pillWidth + (star ? 2 * r + gap : 0);
        final double startX = width / 2 - total / 2;
        final double cy = y(ROWS / 2.0);
        message.getChildren().clear();
        if (star) {
            final Polygon shape = new Polygon();
            for (int i = 0; i < 10; i++) {
                final double a = -Math.PI / 2 + i * Math.PI / 5;
                final double d = i % 2 == 0 ? r : r * 0.45;
                shape.getPoints().addAll(startX + r + d * Math.cos(a), cy + r * 0.08 + d * Math.sin(a));
            }
            shape.setFill(Color.rgb(255, 200, 40));
            shape.setStroke(Color.rgb(200, 120, 0));
            shape.setStrokeWidth(Math.max(2, r * 0.07));
            message.getChildren().add(shape);
        }
        pill.setLayoutX(startX + total - pillWidth);
        pill.setLayoutY(cy - pillHeight / 2);
        message.getChildren().add(pill);
    }

    private void finish() {
        setPhase(Phase.OVER);
        loop.stop();
        stats.takeScreenShot();
        gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    List<Start> getStarts() {
        return starts;
    }

    boolean isHintShown() {
        return starts.stream().anyMatch(s -> s.hint.isVisible());
    }

    /**
     * @return the seconds the Biboule takes from a start to the bottom
     */
    double runTime(final int start) {
        final List<double[]> points = wayFrom(start);
        double total = 0;
        for (int i = 1; i < points.size(); i++) {
            total += length(points.get(i - 1), points.get(i));
        }
        return total / (SPEED * height);
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        for (final Start start : starts) {
            gameContext.getGazeDeviceManager().removeEventFilter(start.disc);
        }
        starts.clear();
        if (ring != null) {
            ring.stop();
        }
        looked = null;
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A round start at the top of a rope, with an arrow down and the frame of the hint.
     */
    final class Start {
        @Getter
        private final int column;
        private final Circle disc;
        private final Circle hint;

        Start(final int column) {
            this.column = column;
            final double r = startRadius();
            final double cx = x(column);
            final double cy = startY();
            disc = new Circle(cx, cy, r);
            disc.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
            hint = new Circle(cx, cy, r * 1.15);
            hint.setFill(null);
            hint.setStyle("-fx-stroke: -gp-accent;");
            hint.setStrokeWidth(Math.max(3, r * 0.08));
            hint.getStrokeDashArray().setAll(r * 0.18, r * 0.12);
            hint.setVisible(false);
            hint.setMouseTransparent(true);
            final SVGPath icon = GameStyle.icon("keyboard_arrow_down", r * 1.2);
            icon.setStyle("-fx-fill: " + toCss(ROPES[column % ROPES.length].darker()) + ";");
            final Group iconGroup = new Group(icon);
            iconGroup.setLayoutX(cx - r * 0.6);
            iconGroup.setLayoutY(cy - r * 0.6);
            iconGroup.setMouseTransparent(true);
            startsLayer.getChildren().addAll(hint, disc, iconGroup);
            final EventHandler<Event> handler = event -> {
                final var type = event.getEventType();
                if (type == MouseEvent.MOUSE_ENTERED || type == GazeEvent.GAZE_ENTERED) {
                    lookAt(this);
                } else if (type == MouseEvent.MOUSE_EXITED || type == GazeEvent.GAZE_EXITED) {
                    lookAway(this);
                }
            };
            disc.addEventFilter(MouseEvent.ANY, handler);
            disc.addEventFilter(GazeEvent.ANY, handler);
            gameContext.getGazeDeviceManager().addEventFilter(disc);
        }
    }

    private static String toCss(final Color color) {
        return String.format("rgb(%d, %d, %d)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
}
