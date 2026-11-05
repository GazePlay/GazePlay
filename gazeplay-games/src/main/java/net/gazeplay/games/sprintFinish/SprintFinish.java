package net.gazeplay.games.sprintFinish;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
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
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;
import net.gazeplay.games.sprintFinish.SprintMap.Button;
import net.gazeplay.games.sprintFinish.SprintMap.Cell;
import net.gazeplay.games.sprintFinish.SprintMap.Log;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Game "Sprint Finish" (reviewed on 2/11/2026, maintainer's rules): the cat must reach its bowl without being caught by
 * the dogs, on maps made at random (see {@link SprintMapGenerator}), with hedges, fences opened by a button, and logs
 * going up and down. The cat moves with the keyboard (arrows, Z Q S D or W A S D: both keyboards work, without a
 * setting) or follows the gaze, alone; with two players, one moves the cat with the keyboard and the other stops the
 * dogs and the logs by looking at them, and opens the fences by a fixation on their button (see {@link SprintMode}).
 * Alone, a button opens its fences when the cat walks on it. A dog or a log catching the cat: the map starts again. The
 * bowl reached: the next map; after the last one of the difficulty (5, 10 or 15 maps), "Bravo", then the statistics.
 * <p>
 * The board is a grid scaled to the screen: positions are in cells, all moves in cells per second (one loop, stopped
 * at the end of each map: no dog keeps running after it). Nothing moves while the menu of the game is open.
 */
@Slf4j
public class SprintFinish implements GameLifeCycle {

    static final String FOLDER = "data/sprintFinish/";

    static final String TICK_SOUND = FOLDER + "sounds/tick.wav";
    static final String DOOR_SOUND = FOLDER + "sounds/door.wav";
    static final String CAUGHT_SOUND = FOLDER + "sounds/caught.wav";
    static final String DISH_SOUND = FOLDER + "sounds/dish.wav";

    /**
     * Speed of the cat, in cells per second.
     */
    static final double CAT_SPEED = 3.2;

    /**
     * Half the side of the square of the cat and of the dogs, in cells (a little smaller than the picture).
     */
    static final double HALF = 0.34;

    /**
     * Size of the pictures of the cat and of the dogs, in cells (a little more than a cell: they are long and low).
     */
    static final double ANIMAL_SIZE = 1.1;

    /**
     * Distance between the cat and a dog at which the dog catches it, in cells.
     */
    static final double CATCH_DISTANCE = 0.68;

    /**
     * Distance between the cat and its bowl at which it reaches it, in cells.
     */
    static final double DISH_DISTANCE = 0.55;

    /**
     * The colors of the buttons and of their fences.
     */
    static final String[] BUTTON_COLORS = {"#3A7BD5", "#9B59D0"};

    enum Phase { COUNTDOWN, PLAY, WON, CAUGHT, OVER }

    private final IGameContext gameContext;
    private final Stats stats;
    @Getter
    private final SprintMode mode;
    @Getter
    private final SprintDifficulty difficulty;
    private final ReplayablePseudoRandom random;

    /**
     * The maps of the game, made when first played (a map starting again is the same).
     */
    private final List<SprintMap> maps = new ArrayList<>();

    @Getter
    private int index;

    @Getter
    private Phase phase = Phase.OVER;

    private double cell;
    private double originX;
    private double originY;

    private Group layer;
    private Group board;
    private Group hud;
    private Node gazeArea;

    @Getter
    private double catX;
    @Getter
    private double catY;
    private ImageView catView;
    private ImageView dishView;

    @Getter
    private final List<Dog> dogs = new ArrayList<>();
    private final List<LogPiece> logs = new ArrayList<>();
    @Getter
    private final List<ButtonPiece> buttons = new ArrayList<>();

    /**
     * Where the cat goes when it follows the gaze, in cells; null before the first look.
     */
    private double[] target;

    private final Set<KeyCode> pressed = EnumSet.noneOf(KeyCode.class);
    private final EventHandler<KeyEvent> keyPressed = this::onKeyPressed;
    private final EventHandler<KeyEvent> keyReleased = this::onKeyReleased;

    /**
     * Nodes followed by the gaze, and their handlers, removed when the map changes and at the end.
     */
    private final Map<Node, EventHandler<Event>> gazeNodes = new HashMap<>();

    private final AnimationTimer loop = new AnimationTimer() {
        private long last = -1;

        @Override
        public void handle(final long now) {
            final double seconds = last < 0 ? 0 : Math.min(0.05, (now - last) / 1e9);
            last = now;
            if (Stats.isConfigMenuOpen()) {
                // The keys are not received while the menu is open: one released meanwhile must not stay pressed
                pressed.clear();
            } else {
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
    private Timeline countdown;
    private PauseTransition afterMap;

    private final Map<String, Image> images = new HashMap<>();

    public SprintFinish(final IGameContext gameContext, final Stats stats, final SprintMode mode,
                        final SprintDifficulty difficulty) {
        this(gameContext, stats, mode, difficulty, -1);
    }

    /**
     * @param gameSeed the seed of the game to replay, or a negative value for a new game
     */
    public SprintFinish(final IGameContext gameContext, final Stats stats, final SprintMode mode,
                        final SprintDifficulty difficulty, final double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.mode = mode;
        this.difficulty = difficulty;
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
        final double width = dimension.getWidth();
        final double height = dimension.getHeight();
        cell = Math.min(width / SprintMap.COLUMNS, height / SprintMap.ROWS);
        originX = (width - cell * SprintMap.COLUMNS) / 2;
        originY = (height - cell * SprintMap.ROWS) / 2;

        final ImageView meadow = new ImageView(image("meadow"));
        meadow.setFitWidth(width);
        meadow.setFitHeight(height);
        gazeArea = meadow;
        board = new Group();
        board.setLayoutX(originX);
        board.setLayoutY(originY);
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(meadow, board, hud);
        gameContext.getChildren().add(layer);

        if (mode.isKeyboardCat()) {
            gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, keyPressed);
            gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_RELEASED, keyReleased);
        }
        if (mode.isGazeCat()) {
            // The cat goes where the gaze (or the mouse) is, anywhere on the board (the gaze on the meadow: see
            // startMap)
            layer.addEventFilter(MouseEvent.MOUSE_MOVED,
                event -> gazeAt((event.getX() - originX) / cell, (event.getY() - originY) / cell));
            layer.addEventFilter(MouseEvent.MOUSE_DRAGGED,
                event -> gazeAt((event.getX() - originX) / cell, (event.getY() - originY) / cell));
        }

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        gameContext.firstStart();
        index = 0;
        startMap();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * Shows the map of {@link #index} (made at its first time), the cat and the dogs at their start, the fences
     * closed, then the countdown.
     */
    private void startMap() {
        stopTransitions();
        unfollowAll();
        if (mode.isGazeCat()) {
            follow(gazeArea, gazeHandlerOfArea);
        }
        board.getChildren().clear();
        dogs.clear();
        logs.clear();
        buttons.clear();
        pressed.clear();
        target = null;
        if (index >= maps.size()) {
            maps.add(SprintMapGenerator.generate(index, difficulty.getMaps(), random));
            stats.incrementNumberOfGoalsToReach();
        }
        final SprintMap map = maps.get(index);

        for (int column = 0; column < SprintMap.COLUMNS; column++) {
            for (int row = 0; row < SprintMap.ROWS; row++) {
                if (map.isHedge(column, row)) {
                    board.getChildren().add(hedge(column, row));
                }
            }
        }
        for (final Button button : map.getButtons()) {
            final ButtonPiece piece = new ButtonPiece(button);
            buttons.add(piece);
            board.getChildren().addAll(piece.fences);
            board.getChildren().add(piece.node);
        }
        dishView = picture("dish", 0.9);
        place(dishView, map.getDish().column() + 0.5, map.getDish().row() + 0.5, 0.9);
        board.getChildren().add(dishView);
        for (final Log log : map.getLogs()) {
            final LogPiece piece = new LogPiece(log);
            logs.add(piece);
            board.getChildren().add(piece.node);
        }
        for (final Cell start : map.getDogs()) {
            final Dog dog = new Dog(start.column() + 0.5, start.row() + 0.5);
            dogs.add(dog);
            board.getChildren().add(dog.node);
        }
        catX = map.getCat().column() + 0.5;
        catY = map.getCat().row() + 0.5;
        catView = picture("cat", ANIMAL_SIZE);
        board.getChildren().add(catView);
        render();

        hud.getChildren().clear();
        final Group counter = GameStyle.pill(gameContext.getTranslator().translate("SprintMap") + " " + (index + 1)
            + " / " + difficulty.getMaps(), cell * 0.32);
        counter.setLayoutX(originX + (cell * SprintMap.COLUMNS - counter.getBoundsInLocal().getWidth()) / 2);
        counter.setLayoutY(originY + cell * 0.08);
        hud.getChildren().add(counter);
        startCountdown();
    }

    private final EventHandler<Event> gazeHandlerOfArea = event -> {
        if (event instanceof GazeEvent gaze && (gaze.getEventType() == GazeEvent.GAZE_MOVED
            || gaze.getEventType() == GazeEvent.GAZE_ENTERED)) {
            gazeAt((gaze.getX() - originX) / cell, (gaze.getY() - originY) / cell);
        }
    };

    /**
     * 3, 2, 1 in the middle, a "tock" each, then the map is played.
     */
    private void startCountdown() {
        phase = Phase.COUNTDOWN;
        final Group[] shown = new Group[1];
        countdown = new Timeline();
        for (int i = 0; i < 3; i++) {
            final String number = String.valueOf(3 - i);
            countdown.getKeyFrames().add(new KeyFrame(Duration.seconds(i), event -> {
                hud.getChildren().remove(shown[0]);
                shown[0] = GameStyle.pill(number, cell * 0.9);
                shown[0].setLayoutX(originX + (cell * SprintMap.COLUMNS - shown[0].getBoundsInLocal().getWidth()) / 2);
                shown[0].setLayoutY(originY + (cell * SprintMap.ROWS - shown[0].getBoundsInLocal().getHeight()) / 2);
                hud.getChildren().add(shown[0]);
                gameContext.getSoundManager().add(TICK_SOUND);
            }));
        }
        countdown.getKeyFrames().add(new KeyFrame(Duration.seconds(3), event -> {
            hud.getChildren().remove(shown[0]);
            play();
        }));
        countdown.playFromStart();
    }

    /**
     * Ends the countdown: the map is played.
     */
    void play() {
        if (countdown != null) {
            countdown.stop();
        }
        hud.getChildren().removeIf(node -> node != hud.getChildren().getFirst());
        phase = Phase.PLAY;
    }

    /**
     * One step of the game, of a duration in seconds: the logs, the cat, the dogs move; then the cat caught, or at its
     * bowl.
     */
    void step(final double seconds) {
        if (phase != Phase.PLAY || seconds <= 0) {
            return;
        }
        final SprintMap map = maps.get(index);
        closed = closedFences();
        for (final LogPiece log : logs) {
            log.move(seconds);
        }
        moveCat(seconds);
        if (!mode.isHelperGaze()) {
            for (final ButtonPiece button : buttons) {
                if (!button.open && (int) Math.floor(catX) == button.button.cell().column()
                    && (int) Math.floor(catY) == button.button.cell().row()) {
                    button.open();
                }
            }
        }
        closed = closedFences();
        final int[][] distances = map.distancesFrom(new Cell((int) Math.floor(catX), (int) Math.floor(catY)),
            closed);
        for (final Dog dog : dogs) {
            dog.move(seconds, distances);
        }
        render();

        if (dogs.stream().anyMatch(dog -> Math.hypot(dog.x - catX, dog.y - catY) < CATCH_DISTANCE)
            || logs.stream().anyMatch(log -> log.touches(catX, catY))) {
            caught();
        } else if (Math.hypot(map.getDish().column() + 0.5 - catX, map.getDish().row() + 0.5 - catY)
            < DISH_DISTANCE) {
            won();
        }
    }

    private void moveCat(final double seconds) {
        double dx = 0;
        double dy = 0;
        if (mode.isKeyboardCat()) {
            dx = (isPressed(KeyCode.RIGHT, KeyCode.D) ? 1 : 0) - (isPressed(KeyCode.LEFT, KeyCode.Q, KeyCode.A) ? 1 : 0);
            dy = (isPressed(KeyCode.DOWN, KeyCode.S) ? 1 : 0) - (isPressed(KeyCode.UP, KeyCode.Z, KeyCode.W) ? 1 : 0);
        } else if (target != null) {
            dx = target[0] - catX;
            dy = target[1] - catY;
            if (Math.hypot(dx, dy) < 0.15) {
                dx = 0;
                dy = 0;
            }
        }
        final double length = Math.hypot(dx, dy);
        if (length == 0) {
            return;
        }
        double distance = CAT_SPEED * seconds;
        if (target != null && !mode.isKeyboardCat()) {
            distance = Math.min(distance, length);
        }
        final double stepX = dx / length * distance;
        final double stepY = dy / length * distance;
        final boolean movedX = !blocked(catX + stepX, catY);
        if (movedX) {
            catX += stepX;
        }
        final boolean movedY = !blocked(catX, catY + stepY);
        if (movedY) {
            catY += stepY;
        }
        if (mode.isGazeCat() && movedX != movedY) {
            // Against a hedge, the cat following the gaze slides along it at its whole speed, towards the gaze
            if (!movedX && Math.abs(dy) > 0.05) {
                final double slide = Math.copySign(Math.min(distance - Math.abs(stepY), Math.abs(dy)
                    - Math.abs(stepY)), dy);
                if (!blocked(catX, catY + slide)) {
                    catY += slide;
                }
            } else if (!movedY && Math.abs(dx) > 0.05) {
                final double slide = Math.copySign(Math.min(distance - Math.abs(stepX), Math.abs(dx)
                    - Math.abs(stepX)), dx);
                if (!blocked(catX + slide, catY)) {
                    catX += slide;
                }
            }
        }
        if (stepX != 0) {
            catView.setScaleX(stepX < 0 ? -1 : 1);
        }
    }

    private boolean isPressed(final KeyCode... codes) {
        for (final KeyCode code : codes) {
            if (pressed.contains(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return whether a square of the cat or a dog at a place would be in a hedge, a closed fence or out of the board
     */
    private boolean blocked(final double x, final double y) {
        if (x - HALF < 0 || y - HALF < 0 || x + HALF > SprintMap.COLUMNS || y + HALF > SprintMap.ROWS) {
            return true;
        }
        final SprintMap map = maps.get(index);
        for (int column = (int) Math.floor(x - HALF); column <= (int) Math.floor(x + HALF - 1e-9); column++) {
            for (int row = (int) Math.floor(y - HALF); row <= (int) Math.floor(y + HALF - 1e-9); row++) {
                if (map.isHedge(column, row) || closed.contains(new Cell(column, row))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return the next cell towards the cat from a cell, among its 8 neighbours (a diagonal only when both cells
     * beside it are free: never through the corner of a hedge), or null if none is nearer
     */
    static int[] nextCell(final int column, final int row, final int[][] distances) {
        int[] best = null;
        int bestDistance = distances[column][row];
        for (int dc = -1; dc <= 1; dc++) {
            for (int dr = -1; dr <= 1; dr++) {
                final int c = column + dc;
                final int r = row + dr;
                if ((dc == 0 && dr == 0) || !SprintMap.inside(c, r) || distances[c][r] < 0) {
                    continue;
                }
                if (dc != 0 && dr != 0 && (distances[column + dc][row] < 0 || distances[column][row + dr] < 0)) {
                    continue;
                }
                if (distances[c][r] < bestDistance) {
                    bestDistance = distances[c][r];
                    best = new int[]{c, r};
                }
            }
        }
        return best;
    }

    /**
     * @return whether the square of a dog can go in a straight line from a place to another, without touching a
     * hedge, a closed fence or the edge of the board
     */
    boolean clearLine(final double fromX, final double fromY, final double toX, final double toY) {
        final double length = Math.hypot(toX - fromX, toY - fromY);
        final int samples = (int) Math.ceil(length / 0.1);
        for (int i = 1; i <= samples; i++) {
            final double t = (double) i / samples;
            if (blocked(fromX + (toX - fromX) * t, fromY + (toY - fromY) * t)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The fences closed now, kept up to date (read many times at each step).
     */
    private List<Cell> closed = List.of();

    private List<Cell> closedFences() {
        final List<Cell> fences = new ArrayList<>();
        for (final ButtonPiece button : buttons) {
            if (!button.open) {
                fences.addAll(button.button.fences());
            }
        }
        return fences;
    }

    /**
     * The cat caught: it shakes, then the same map starts again.
     */
    private void caught() {
        phase = Phase.CAUGHT;
        pressed.clear();
        gameContext.getSoundManager().add(CAUGHT_SOUND);
        catView.setEffect(new ColorAdjust(0, 0, -0.3, 0));
        final TranslateTransition shake = new TranslateTransition(Duration.millis(60), catView);
        shake.setByX(cell * 0.12);
        shake.setAutoReverse(true);
        shake.setCycleCount(8);
        shake.play();
        afterMap = new PauseTransition(Duration.millis(1100));
        afterMap.setOnFinished(event -> startMap());
        afterMap.play();
    }

    /**
     * The bowl reached: the next map, or "Bravo" then the statistics after the last one.
     */
    private void won() {
        phase = Phase.WON;
        pressed.clear();
        stats.incrementNumberOfGoalsReached();
        gameContext.getSoundManager().add(DISH_SOUND);
        final ScaleTransition jump = new ScaleTransition(Duration.millis(200), catView);
        jump.setByY(0.25);
        jump.setByX(catView.getScaleX() * 0.25);
        jump.setAutoReverse(true);
        jump.setCycleCount(4);
        jump.play();
        afterMap = new PauseTransition(Duration.millis(1300));
        afterMap.setOnFinished(event -> nextMap());
        afterMap.play();
    }

    /**
     * After a map won: the next one, or the end.
     */
    void nextMap() {
        if (index + 1 >= difficulty.getMaps()) {
            phase = Phase.OVER;
            loop.stop();
            stats.takeScreenShot();
            gameContext.updateScore(stats, this);
            gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
        } else {
            index++;
            startMap();
        }
    }

    /**
     * Where the gaze (or the mouse) is, in cells: the cat goes there (alone with the gaze).
     */
    void gazeAt(final double x, final double y) {
        if (mode.isGazeCat()) {
            target = new double[]{x, y};
        }
    }

    void press(final KeyCode code) {
        if (phase == Phase.PLAY || phase == Phase.COUNTDOWN) {
            pressed.add(code);
        }
    }

    void release(final KeyCode code) {
        pressed.remove(code);
    }

    private void onKeyPressed(final KeyEvent event) {
        press(event.getCode());
    }

    private void onKeyReleased(final KeyEvent event) {
        release(event.getCode());
    }

    /**
     * Moves the pictures to the places of the cat, the dogs and the logs.
     */
    private void render() {
        place(catView, catX, catY, ANIMAL_SIZE);
        for (final Dog dog : dogs) {
            place(dog.view, dog.x, dog.y, ANIMAL_SIZE);
            dog.halo.setCenterX(dog.x * cell);
            dog.halo.setCenterY(dog.y * cell);
        }
        for (final LogPiece log : logs) {
            log.node.setLayoutY(log.y * cell);
        }
    }

    private void place(final ImageView view, final double x, final double y, final double size) {
        view.setLayoutX((x - size / 2) * cell);
        view.setLayoutY((y - size / 2) * cell);
    }

    private ImageView picture(final String name, final double size) {
        final ImageView view = new ImageView(image(name));
        view.setFitWidth(size * cell);
        view.setFitHeight(size * cell);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setMouseTransparent(true);
        return view;
    }

    private Image image(final String name) {
        return images.computeIfAbsent(name, key -> new Image(FOLDER + "images/" + key + ".png"));
    }

    /**
     * A hedge of one cell: a rounded bush with leaves.
     */
    private Group hedge(final int column, final int row) {
        final double x = column * cell;
        final double y = row * cell;
        final Rectangle body = new Rectangle(x + cell * 0.04, y + cell * 0.04, cell * 0.92, cell * 0.92);
        body.setArcWidth(cell * 0.5);
        body.setArcHeight(cell * 0.5);
        body.setFill(Color.web("#3E8E41"));
        body.setStroke(Color.web("#2B6630"));
        body.setStrokeWidth(Math.max(2, cell * 0.04));
        final Group group = new Group(body);
        final double[][] leaves = {{0.3, 0.32, 0.16}, {0.66, 0.3, 0.14}, {0.48, 0.66, 0.17}, {0.24, 0.7, 0.1}};
        for (final double[] leaf : leaves) {
            final Circle circle = new Circle(x + leaf[0] * cell, y + leaf[1] * cell, leaf[2] * cell);
            circle.setFill(Color.web("#5DAE55"));
            group.getChildren().add(circle);
        }
        group.setMouseTransparent(true);
        return group;
    }

    /**
     * Follows the gaze (and the mouse, through the same handler) on a node.
     */
    private void follow(final Node node, final EventHandler<Event> handler) {
        gameContext.getGazeDeviceManager().addEventFilter(node);
        node.addEventFilter(GazeEvent.ANY, handler);
        node.addEventFilter(MouseEvent.ANY, handler);
        gazeNodes.put(node, handler);
    }

    private void unfollowAll() {
        for (final Map.Entry<Node, EventHandler<Event>> entry : gazeNodes.entrySet()) {
            gameContext.getGazeDeviceManager().removeEventFilter(entry.getKey());
            entry.getKey().removeEventFilter(GazeEvent.ANY, entry.getValue());
            entry.getKey().removeEventFilter(MouseEvent.ANY, entry.getValue());
        }
        gazeNodes.clear();
    }

    private void stopTransitions() {
        if (countdown != null) {
            countdown.stop();
        }
        if (afterMap != null) {
            afterMap.stop();
        }
        for (final ButtonPiece button : buttons) {
            button.stopFixation();
        }
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    /**
     * For the tests: puts the cat at a place, in cells.
     */
    void placeCat(final double x, final double y) {
        catX = x;
        catY = y;
    }

    /**
     * For the tests: ends at once the pause after a map won or lost.
     */
    void skipPause() {
        if (afterMap != null && afterMap.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            afterMap.stop();
            afterMap.getOnFinished().handle(null);
        }
    }

    /**
     * @return the map played
     */
    SprintMap getMap() {
        return maps.get(index);
    }

    @Override
    public void dispose() {
        loop.stop();
        stopTransitions();
        unfollowAll();
        if (mode.isKeyboardCat()) {
            gameContext.getPrimaryScene().removeEventFilter(KeyEvent.KEY_PRESSED, keyPressed);
            gameContext.getPrimaryScene().removeEventFilter(KeyEvent.KEY_RELEASED, keyReleased);
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
        phase = Phase.OVER;
    }

    /**
     * A dog: it runs straight to the cat when nothing is in the way, else by the shortest way round the hedges and the
     * closed fences, diagonals included (maintainer's request of 2/11/2026: the dogs used to go only up, down, left or
     * right), stopped by a log in its way; with two players, it stops while it is looked at.
     */
    final class Dog {
        @Getter
        private double x;
        @Getter
        private double y;
        @Getter
        private boolean looked;
        private final ImageView view;
        private final Circle halo;
        private final Group node;

        Dog(final double x, final double y) {
            this.x = x;
            this.y = y;
            view = picture("dog", ANIMAL_SIZE);
            view.setMouseTransparent(false);
            view.setPickOnBounds(true);
            halo = new Circle(cell * 0.55);
            halo.setFill(Color.web("#D64545", 0.28));
            halo.setStroke(Color.web("#D64545"));
            halo.setStrokeWidth(Math.max(3, cell * 0.05));
            halo.setVisible(false);
            halo.setMouseTransparent(true);
            node = new Group(halo, view);
            if (mode.isHelperGaze()) {
                follow(view, event -> {
                    if (event.getEventType() == GazeEvent.GAZE_ENTERED
                        || event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                        setLooked(true);
                    } else if (event.getEventType() == GazeEvent.GAZE_EXITED
                        || event.getEventType() == MouseEvent.MOUSE_EXITED) {
                        setLooked(false);
                    }
                });
            }
        }

        /**
         * For the tests: puts the dog at a place, in cells.
         */
        void placeAt(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        void setLooked(final boolean looked) {
            this.looked = looked;
            halo.setVisible(looked);
            view.setEffect(looked ? new ColorAdjust(0, -0.6, 0, 0) : null);
        }

        void move(final double seconds, final int[][] distances) {
            if (looked) {
                return;
            }
            int column = (int) Math.floor(x);
            int row = (int) Math.floor(y);
            if (!SprintMap.inside(column, row) || distances[column][row] < 0) {
                // The cat cannot be reached (behind a closed fence): the dog waits
                return;
            }
            double goalX = catX;
            double goalY = catY;
            if (!clearLine(x, y, catX, catY)) {
                // The shortest way, diagonals included (never cutting the corner of a hedge): the dog runs straight
                // to its farthest cell still in sight, so that it goes smoothly round the hedges
                goalX = column + 0.5;
                goalY = row + 0.5;
                for (int stepCount = 0; stepCount < 8 && distances[column][row] > 0; stepCount++) {
                    final int[] next = nextCell(column, row, distances);
                    if (next == null) {
                        break;
                    }
                    column = next[0];
                    row = next[1];
                    if (!clearLine(x, y, column + 0.5, row + 0.5)) {
                        break;
                    }
                    goalX = column + 0.5;
                    goalY = row + 0.5;
                }
            }
            final double dx = goalX - x;
            final double dy = goalY - y;
            final double length = Math.hypot(dx, dy);
            if (length < 1e-6) {
                return;
            }
            final double distance = Math.min(length, mode.getDogSpeed() * seconds);
            final double nextX = x + dx / length * distance;
            final double nextY = y + dy / length * distance;
            if (logs.stream().anyMatch(log -> log.touches(nextX, nextY))) {
                return;
            }
            x = nextX;
            y = nextY;
            if (Math.abs(dx) > 1e-6) {
                view.setScaleX(dx < 0 ? -1 : 1);
            }
        }
    }

    /**
     * A log going up and down its column; with two players, it stops while it is looked at (it turns grey).
     */
    final class LogPiece {
        private final Log log;
        @Getter
        private double y;
        private double direction = 1;
        @Getter
        private boolean looked;
        private final Rectangle body;
        private final Group node;

        LogPiece(final Log log) {
            this.log = log;
            final double travel = SprintMap.ROWS - log.length();
            y = travel * log.phase();
            body = new Rectangle(log.column() * cell + cell * 0.12, 0, cell * 0.76, log.length() * cell);
            body.setArcWidth(cell * 0.5);
            body.setArcHeight(cell * 0.5);
            body.setStrokeWidth(Math.max(2, cell * 0.05));
            node = new Group(body);
            for (int i = 1; i < 4; i++) {
                final Line ring = new Line(log.column() * cell + cell * 0.25, i * log.length() * cell / 4,
                    log.column() * cell + cell * 0.75, i * log.length() * cell / 4);
                ring.setStroke(Color.web("#5C3A1A"));
                ring.setStrokeWidth(Math.max(1.5, cell * 0.03));
                ring.setStrokeLineCap(StrokeLineCap.ROUND);
                ring.setMouseTransparent(true);
                node.getChildren().add(ring);
            }
            setLooked(false);
            if (mode.isHelperGaze()) {
                follow(body, event -> {
                    if (event.getEventType() == GazeEvent.GAZE_ENTERED
                        || event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                        setLooked(true);
                    } else if (event.getEventType() == GazeEvent.GAZE_EXITED
                        || event.getEventType() == MouseEvent.MOUSE_EXITED) {
                        setLooked(false);
                    }
                });
            }
        }

        void setLooked(final boolean looked) {
            this.looked = looked;
            body.setFill(Color.web(looked ? "#A8A29A" : "#9C6A3A"));
            body.setStroke(Color.web(looked ? "#6E6A64" : "#5C3A1A"));
        }

        void move(final double seconds) {
            if (looked) {
                return;
            }
            final double travel = SprintMap.ROWS - log.length();
            y += direction * mode.getLogSpeed() * seconds;
            if (y >= travel) {
                y = travel;
                direction = -1;
            } else if (y <= 0) {
                y = 0;
                direction = 1;
            }
        }

        /**
         * @return whether the square of the cat or of a dog at a place touches the log
         */
        boolean touches(final double x, final double centerY) {
            final double left = log.column() + 0.12;
            final double right = log.column() + 0.88;
            return x + HALF > left && x - HALF < right && centerY + HALF > y && centerY - HALF < y + log.length();
        }
    }

    /**
     * A button and its fences. Alone, the cat opens them by walking on the button; with two players, a fixation on
     * the button opens them (a ring of the main color goes round it meanwhile). Once open, they stay open.
     */
    final class ButtonPiece {
        private final Button button;
        @Getter
        private boolean open;
        private final Group node;
        private final List<Group> fences = new ArrayList<>();
        private final Circle plate;
        private final Arc ring;
        private final DoubleProperty progress = new SimpleDoubleProperty(0);
        private Timeline fixation;

        ButtonPiece(final Button button) {
            this.button = button;
            final String color = BUTTON_COLORS[button.color() % BUTTON_COLORS.length];
            final double centerX = (button.cell().column() + 0.5) * cell;
            final double centerY = (button.cell().row() + 0.5) * cell;
            final Circle base = new Circle(centerX, centerY + cell * 0.05, cell * 0.36, Color.web("#4A4A4A"));
            plate = new Circle(centerX, centerY, cell * 0.3, Color.web(color));
            plate.setStroke(Color.web("#222222"));
            plate.setStrokeWidth(Math.max(2, cell * 0.035));
            final Circle shine = new Circle(centerX - cell * 0.08, centerY - cell * 0.09, cell * 0.08,
                Color.web("#FFFFFF", 0.45));
            ring = new Arc(centerX, centerY, cell * 0.45, cell * 0.45, 90, 0);
            ring.setType(ArcType.OPEN);
            ring.setFill(null);
            ring.setStrokeWidth(Math.max(4, cell * 0.07));
            ring.setStrokeLineCap(StrokeLineCap.ROUND);
            ring.setStyle("-fx-stroke: -gp-accent;");
            ring.setMouseTransparent(true);
            progress.addListener((observable, oldValue, newValue) -> ring.setLength(-360 * newValue.doubleValue()));
            // A larger zone than the plate, easier to look at
            final Circle zone = new Circle(centerX, centerY, cell * 0.5, Color.TRANSPARENT);
            node = new Group(zone, base, plate, shine, ring);
            for (final Cell fence : button.fences()) {
                fences.add(fence(fence, color));
            }
            if (mode.isHelperGaze()) {
                follow(zone, event -> {
                    if (event.getEventType() == GazeEvent.GAZE_ENTERED
                        || event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                        startFixation();
                    } else if (event.getEventType() == GazeEvent.GAZE_EXITED
                        || event.getEventType() == MouseEvent.MOUSE_EXITED) {
                        stopFixation();
                    }
                });
            } else {
                zone.setMouseTransparent(true);
            }
        }

        /**
         * A fence of one cell: wooden posts and planks, the top plank of the color of its button.
         */
        private Group fence(final Cell fence, final String color) {
            final double x = fence.column() * cell;
            final double y = fence.row() * cell;
            final Group group = new Group();
            for (final double post : new double[]{0.18, 0.5, 0.82}) {
                final Rectangle rectangle = new Rectangle(x + (post - 0.06) * cell, y + 0.06 * cell, 0.12 * cell,
                    0.88 * cell);
                rectangle.setFill(Color.web("#B07A45"));
                rectangle.setStroke(Color.web("#6E4A26"));
                rectangle.setStrokeWidth(Math.max(1.5, cell * 0.025));
                group.getChildren().add(rectangle);
            }
            for (final double plank : new double[]{0.22, 0.48, 0.74}) {
                final Rectangle rectangle = new Rectangle(x + 0.04 * cell, y + (plank - 0.06) * cell, 0.92 * cell,
                    0.12 * cell);
                rectangle.setFill(Color.web(plank < 0.3 ? color : "#C68B52"));
                rectangle.setStroke(Color.web("#6E4A26"));
                rectangle.setStrokeWidth(Math.max(1.5, cell * 0.025));
                group.getChildren().add(rectangle);
            }
            group.setMouseTransparent(true);
            return group;
        }

        private void startFixation() {
            if (open || phase != Phase.PLAY) {
                return;
            }
            stopFixation();
            fixation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(progress, 0)),
                new KeyFrame(Duration.millis(gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(progress, 1)));
            fixation.setOnFinished(event -> open());
            fixation.playFromStart();
        }

        void stopFixation() {
            if (fixation != null) {
                fixation.stop();
                fixation = null;
            }
            progress.set(0);
        }

        /**
         * Opens the fences of the button: they fade away; the button is pressed down.
         */
        void open() {
            if (open) {
                return;
            }
            open = true;
            stopFixation();
            gameContext.getSoundManager().add(DOOR_SOUND);
            plate.setFill(Color.web(GameStyle.SUCCESS_COLOR));
            plate.setScaleX(0.85);
            plate.setScaleY(0.85);
            for (final Group fence : fences) {
                final FadeTransition fade = new FadeTransition(Duration.millis(400), fence);
                fade.setToValue(0.12);
                fade.play();
            }
        }
    }
}
