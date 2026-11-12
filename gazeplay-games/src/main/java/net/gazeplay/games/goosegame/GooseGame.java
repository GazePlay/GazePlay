package net.gazeplay.games.goosegame;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.DiceRoller;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * "Jeu de l'oie" (name code GooseGame; reviewed on 12/11/2026, maintainer's rules). A board in a spiral of 63 squares;
 * each player in turn rolls two dice (a fixation on the "Roll" button; the computer rolls by itself) and moves their
 * pawn, a Biboule. With the classic rules: a goose moves the pawn again by the same throw, the bridge (7) leads to 32,
 * the inn (19) makes it sleep two turns, the well (31) and the prison (52) keep it until another pawn comes or passes
 * by, the arrow (58) sends it back to the start; if all the pawns wait, they are all freed. With the simple rules, only
 * the geese. The end (63) must be reached exactly, else the pawn moves back. The first pawn on 63 wins: "Bravo" when a
 * person wins, then the statistics.
 * <p>
 * One player against 1 to 3 pawns of the computer, or 2 to 4 persons taking turns. One loop, in real seconds: the
 * fixation lasts the length of the settings (read at each step), nothing can be chosen before the delay of the
 * settings, and nothing moves while the menu of the game is open. (Before the review: persons only, the game stuck after
 * a win, everything shown twice when all the pawns waited, the throws added to the goals reached, piled-up messages in
 * the font of the system with "Player 2", a board, a button and sounds of unknown source, two copies of the whole game.)
 */
public class GooseGame implements GameLifeCycle {

    static final String[] COLORS = {"Blue", "Orange", "Green", "Yellow"};
    /**
     * The color of the ring of the base of each pawn.
     */
    /**
     * Where the body of a Biboule is in its picture, as a part of its height.
     */
    static final double BODY_CENTER = 0.56;
    private static final Color[] RINGS = {Color.rgb(58, 123, 213), Color.rgb(242, 140, 56), Color.rgb(76, 175, 80),
        Color.rgb(232, 185, 28)};
    /**
     * In seconds: the dice rolling (the die turns for 1.5 s), a hop of a pawn to the next square, a jump (bridge,
     * back to the start), the computer thinking, the end shown.
     */
    static final double ROLL = 1.6;
    static final double HOP = 0.28;
    static final double JUMP = 0.7;
    static final double COMPUTER_WAIT = 1.0;
    static final double SHOW = 2.4;

    private static final String BOARD = "data/goosegame/gooseboard.png";
    private static final String POSITIONS = "data/goosegame/positions.json";
    private static final String SOUNDS = "data/goosegame/sounds/";
    private static final String BIBOULES = "data/shooter/biboule/images/";
    private static final Color BUTTON = Color.rgb(242, 166, 90);
    private static final Color PIP = Color.rgb(38, 34, 47);

    enum Phase { WAIT, READY, ROLLING, MOVING, PAUSE, SHOW, OVER }

    private static final java.util.Map<String, Image> GAME_IMAGES = new java.util.HashMap<>();

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final GooseRules rules;
    @Getter
    private final GoosePlayers players;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private final List<Pawn> pawns = new ArrayList<>();
    @Getter
    private int current;
    @Getter
    private int lastThrow;
    @Getter
    private Pawn winner;
    @Getter
    private String lastMessage = "";
    private double elapsed;
    private double pauseLeft;
    private Runnable afterPause;

    // The pawn moving, its squares to go through, and what comes after
    private Pawn mover;
    private final List<Integer> hops = new ArrayList<>();
    private double hopLength;
    private double hopTime;
    private Point2D hopFrom;
    private Runnable afterMove;

    private double width;
    private double height;
    private final List<Point2D> centers = new ArrayList<>();
    private double pawnSize;

    private Group layer;
    private Group pawnsLayer;
    private Group diceGroup;
    private Group rollButton;
    private Group hud;
    private Group message;
    private FixationRing ring;
    private final List<DiceRoller> dice = new ArrayList<>();
    private double buttonX;
    private double buttonY;
    private double buttonRadius;
    private boolean lookingAtButton;
    @Getter
    private double progress;

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
    GooseGame(final IGameContext gameContext, final Stats stats, final GooseRules rules, final GoosePlayers players,
              final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.rules = rules;
        this.players = players;
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

        final Rectangle meadow = new Rectangle(0, 0, width, height);
        meadow.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(168, 214, 150)), new Stop(1, Color.rgb(128, 188, 118))));
        meadow.setMouseTransparent(true);

        // The board under a band for the turn and the messages, as large as it can be
        final Image boardImage = gameImage(BOARD);
        final double band = height * 0.13;
        final double scale = Math.min((height - band - height * 0.05) / boardImage.getHeight(),
            width * 0.97 / boardImage.getWidth());
        final double boardX = (width - boardImage.getWidth() * scale) / 2;
        final double boardY = band + (height - band - boardImage.getHeight() * scale) / 2;
        final ImageView board = new ImageView(boardImage);
        board.setFitWidth(boardImage.getWidth() * scale);
        board.setFitHeight(boardImage.getHeight() * scale);
        board.setLayoutX(boardX);
        board.setLayoutY(boardY);
        board.setMouseTransparent(true);
        final JsonArray positions = (JsonArray) JsonParser.parseReader(new InputStreamReader(
            Objects.requireNonNull(GooseGame.class.getClassLoader().getResourceAsStream(POSITIONS)),
            StandardCharsets.UTF_8));
        for (int i = 0; i <= GooseBoard.END; i++) {
            final JsonObject position = (JsonObject) positions.get(i);
            centers.add(new Point2D(boardX + position.get("x").getAsDouble() * scale,
                boardY + position.get("y").getAsDouble() * scale));
        }
        // A tile of the board is 94 pixels across in the image
        pawnSize = 94 * scale * 1.3;

        pawnsLayer = new Group();
        pawnsLayer.setMouseTransparent(true);
        for (int i = 0; i < players.getPawns(); i++) {
            final ImageView view = new ImageView(gameImage(BIBOULES + COLORS[i] + ".png"));
            view.setPreserveRatio(true);
            view.setFitWidth(pawnSize);
            view.setFitHeight(pawnSize);
            // The body of a Biboule is at the middle of the width of its picture and at 56 % of its height (the wings
            // above it; measured on the four pictures, 500 x 400): it is put on the center of the base
            final double shownHeight = pawnSize * view.getImage().getHeight() / Math.max(1, view.getImage().getWidth());
            view.setX(-pawnSize / 2);
            view.setY(-shownHeight * BODY_CENTER);
            // A white base ringed with the color of the pawn, so that it stands out on every square
            final Circle base = new Circle(0, 0, pawnSize * 0.36, Color.rgb(255, 255, 255, 0.92));
            base.setStroke(RINGS[i]);
            base.setStrokeWidth(Math.max(2, pawnSize * 0.07));
            final Group node = new Group(base, view);
            final Pawn pawn = new Pawn(i, COLORS[i], i < players.getHumans(), node, view.getImage());
            pawns.add(pawn);
            pawnsLayer.getChildren().add(node);
        }

        diceGroup = diceGroup();
        diceGroup.setVisible(false);
        rollButton = rollButton();
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        layer = new Group(meadow, board, pawnsLayer, diceGroup, rollButton, ring, hud, message);
        gameContext.getChildren().add(layer);
        placePawns();

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        current = 0;
        elapsed = 0;
        rollButton.setVisible(false);
        phase = Phase.WAIT;
        if (!manual) {
            loop.start();
        }
    }

    /**
     * @return an image of the game, loaded once: a game started again uses the same ones (the board is about 7.6 MB in
     * memory; the Biboules are loaded at 300 pixels, the most a pawn needs)
     */
    private static Image gameImage(final String path) {
        return GAME_IMAGES.computeIfAbsent(path, key -> key.startsWith(BIBOULES)
            ? new Image(key, 300, 300, true, true) : new Image(key));
    }

    /**
     * @return the two dice, on a dark rounded card in the middle of the screen
     */
    private Group diceGroup() {
        final double side = height * 0.13;
        final Rectangle card = new Rectangle(width / 2 - side * 2, height / 2 - side * 1.05, side * 4, side * 2.1);
        card.setArcWidth(side * 0.6);
        card.setArcHeight(side * 0.6);
        card.setFill(Color.rgb(34, 88, 58, 0.85));
        card.setStroke(Color.rgb(255, 255, 255, 0.6));
        card.setStrokeWidth(Math.max(2, side * 0.03));
        final Group group = new Group(card);
        for (int i = 0; i < 2; i++) {
            final DiceRoller die = new DiceRoller((float) (side / 2), gameContext.getSoundManager(), random);
            die.setLayoutX(width / 2 + (i == 0 ? -side * 0.95 : side * 0.95));
            die.setLayoutY(height / 2);
            dice.add(die);
            group.getChildren().add(die);
        }
        group.setMouseTransparent(true);
        return group;
    }

    /**
     * @return the round "Roll" button at the bottom right: an orange disc with a die, its name under it
     */
    private Group rollButton() {
        buttonRadius = height * 0.075;
        buttonX = width - buttonRadius * 1.7;
        buttonY = height - buttonRadius * 2.1;
        final Circle disc = new Circle(buttonX, buttonY, buttonRadius, BUTTON);
        disc.setStroke(Color.rgb(255, 250, 240));
        disc.setStrokeWidth(Math.max(3, buttonRadius * 0.07));
        final double side = buttonRadius;
        final Rectangle face = new Rectangle(buttonX - side / 2, buttonY - side / 2, side, side);
        face.setArcWidth(side * 0.3);
        face.setArcHeight(side * 0.3);
        face.setFill(Color.rgb(251, 250, 246));
        face.setStroke(Color.rgb(222, 217, 206));
        face.setMouseTransparent(true);
        final Group group = new Group(disc, face);
        final double[][] pips = {{-1, -1}, {1, -1}, {0, 0}, {-1, 1}, {1, 1}};
        for (final double[] pip : pips) {
            final Circle dot = new Circle(buttonX + pip[0] * side * 0.27, buttonY + pip[1] * side * 0.27,
                side * 0.1, PIP);
            dot.setMouseTransparent(true);
            group.getChildren().add(dot);
        }
        final Group label = GameStyle.pill(gameContext.getTranslator().translate("GooseRoll"), height * 0.03);
        label.setLayoutX(buttonX - label.getBoundsInLocal().getWidth() / 2);
        label.setLayoutY(buttonY + buttonRadius * 1.1);
        label.setMouseTransparent(true);
        group.getChildren().add(label);
        final EventHandler<Event> handler = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                lookAtButton(true);
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                lookAtButton(false);
            }
        };
        disc.addEventFilter(MouseEvent.ANY, handler);
        disc.addEventFilter(GazeEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(disc);
        return group;
    }

    void lookAtButton(final boolean looking) {
        lookingAtButton = looking;
        progress = 0;
        ring.stop();
        if (looking && phase == Phase.READY) {
            showRing();
        }
    }

    private void showRing() {
        ring.setSize(buttonRadius * 2.5);
        ring.show(buttonX, buttonY);
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    startTurn();
                }
            }
            case READY -> fixate(seconds);
            case ROLLING -> {
                elapsed += seconds;
                if (elapsed >= ROLL) {
                    move(lastThrow);
                }
            }
            case MOVING -> hop(seconds);
            case PAUSE, SHOW -> {
                pauseLeft -= seconds;
                if (pauseLeft <= 0) {
                    final Runnable next = afterPause;
                    afterPause = null;
                    if (phase == Phase.SHOW) {
                        finish();
                    } else if (next != null) {
                        next.run();
                    }
                }
            }
            default -> {
            }
        }
    }

    private void fixate(final double seconds) {
        if (!lookingAtButton) {
            return;
        }
        if (!ring.isVisible()) {
            showRing();
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            progress = 0;
            ring.stop();
            stats.incrementNumberOfGoalsReached();
            roll();
        }
    }

    /**
     * Waits, then goes on.
     */
    private void after(final double seconds, final Runnable next) {
        phase = Phase.PAUSE;
        pauseLeft = seconds;
        afterPause = next;
    }

    /**
     * The turn of the current pawn: a pawn asleep or waiting passes its turn; a person rolls with the button, the
     * computer by itself.
     */
    private void startTurn() {
        diceGroup.setVisible(false);
        if (pawns.stream().allMatch(Pawn::isWaiting)) {
            pawns.forEach(pawn -> pawn.waiting = false);
            say("GooseAllFree");
            after(1.6, this::startTurn);
            return;
        }
        final Pawn pawn = pawns.get(current);
        showTurn(pawn);
        if (pawn.asleep > 0) {
            pawn.asleep--;
            say("GooseSleeps", pawn);
            after(1.4, this::nextTurn);
            return;
        }
        if (pawn.waiting) {
            say("GooseWaits", pawn);
            after(1.4, this::nextTurn);
            return;
        }
        if (pawn.human) {
            if (players.againstComputer()) {
                say("GooseYourTurn");
            } else {
                say("GooseTurn", pawn);
            }
            stats.incrementNumberOfGoalsToReach();
            rollButton.setVisible(true);
            phase = Phase.READY;
            progress = 0;
            if (lookingAtButton) {
                showRing();
            }
        } else {
            say("GooseComputerTurn", pawn);
            after(COMPUTER_WAIT, this::roll);
        }
    }

    private void nextTurn() {
        current = (current + 1) % pawns.size();
        startTurn();
    }

    /**
     * The two dice roll; the pawn moves when they have settled.
     */
    private void roll() {
        int total = 0;
        for (final DiceRoller die : dice) {
            total += die.roll(null);
        }
        startRolling(total);
    }

    /**
     * For the tests: a throw of a known value.
     */
    void rollWith(final int total) {
        startRolling(total);
    }

    private void startRolling(final int total) {
        lastThrow = total;
        rollButton.setVisible(false);
        ring.stop();
        diceGroup.setVisible(true);
        phase = Phase.ROLLING;
        elapsed = 0;
    }

    private void move(final int steps) {
        final Pawn pawn = pawns.get(current);
        say("GooseMoves", pawn, steps);
        moveAlong(pawn, GooseBoard.way(pawn.square, steps), HOP, this::land);
    }

    /**
     * Moves a pawn through squares, one hop each, then goes on.
     */
    private void moveAlong(final Pawn pawn, final List<Integer> squares, final double length, final Runnable then) {
        mover = pawn;
        hops.clear();
        hops.addAll(squares);
        hopLength = length;
        hopTime = 0;
        hopFrom = pointOf(pawn);
        afterMove = then;
        phase = Phase.MOVING;
        if (hops.isEmpty()) {
            arrive();
        }
    }

    private void hop(final double seconds) {
        hopTime += seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        final double t = Math.min(1, hopTime / hopLength);
        final Point2D to = centers.get(hops.get(0));
        final double x = hopFrom.getX() + (to.getX() - hopFrom.getX()) * t;
        final double y = hopFrom.getY() + (to.getY() - hopFrom.getY()) * t - Math.sin(Math.PI * t) * pawnSize * 0.5;
        mover.node.setLayoutX(x);
        mover.node.setLayoutY(y);
        if (t >= 1) {
            mover.square = hops.remove(0);
            gameContext.getSoundManager().add(SOUNDS + "mvmt" + random.nextInt(6) + ".wav");
            freeAt(mover);
            hopFrom = to;
            hopTime = 0;
            if (hops.isEmpty()) {
                arrive();
            }
        }
    }

    private void arrive() {
        mover = null;
        placePawns();
        final Runnable then = afterMove;
        afterMove = null;
        if (then != null) {
            then.run();
        }
    }

    /**
     * A pawn coming on or passing by the well or the prison frees the pawn waiting there.
     */
    private void freeAt(final Pawn pawn) {
        final GooseBoard.Kind kind = GooseBoard.kind(pawn.square, rules);
        if (kind != GooseBoard.Kind.WELL && kind != GooseBoard.Kind.PRISON) {
            return;
        }
        for (final Pawn other : pawns) {
            if (other != pawn && other.waiting && other.square == pawn.square) {
                other.waiting = false;
                say("GooseFreed", pawn, other);
            }
        }
    }

    /**
     * What the square the pawn stops on does.
     */
    private void land() {
        final Pawn pawn = pawns.get(current);
        switch (GooseBoard.kind(pawn.square, rules)) {
            case END -> win(pawn);
            case GOOSE -> {
                say("GooseGoose", pawn, lastThrow);
                gameContext.getSoundManager().add(SOUNDS + "goose.wav");
                after(1.0, () -> moveAlong(pawn, GooseBoard.way(pawn.square, lastThrow), HOP, this::land));
            }
            case BRIDGE -> {
                say("GooseBridge", pawn);
                after(0.9, () -> moveAlong(pawn, List.of(GooseBoard.BRIDGE_END), JUMP,
                    () -> after(0.6, this::nextTurn)));
            }
            case INN -> {
                pawn.asleep = GooseBoard.INN_TURNS;
                say("GooseInn", pawn);
                gameContext.getSoundManager().add(SOUNDS + "trap.wav");
                after(1.6, this::nextTurn);
            }
            case WELL, PRISON -> {
                pawn.waiting = true;
                say(pawn.square == GooseBoard.WELL ? "GooseWell" : "GoosePrison", pawn);
                gameContext.getSoundManager().add(SOUNDS + "trap.wav");
                after(1.6, this::nextTurn);
            }
            case BACK -> {
                say("GooseBack", pawn);
                gameContext.getSoundManager().add(SOUNDS + "trap.wav");
                after(0.9, () -> moveAlong(pawn, List.of(GooseBoard.START), JUMP * 1.4,
                    () -> after(0.6, this::nextTurn)));
            }
            default -> after(0.5, this::nextTurn);
        }
    }

    private void win(final Pawn pawn) {
        winner = pawn;
        if (!players.againstComputer()) {
            say("GooseWins", pawn);
        } else {
            say(pawn.human ? "GooseYouWin" : "GooseComputerWins");
        }
        gameContext.getSoundManager().add(SOUNDS + "win.wav");
        phase = Phase.SHOW;
        pauseLeft = SHOW;
    }

    private void finish() {
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        if (winner != null && winner.human) {
            gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
        } else {
            gameContext.showRoundStats(stats, this);
        }
    }

    /**
     * Shows a message at the top, the names of the pawns ({0}, {1}) and a number ({1}) in it.
     */
    private void say(final String key, final Object... values) {
        String text = gameContext.getTranslator().translate(key);
        for (int i = 0; i < values.length; i++) {
            final Object value = values[i];
            final String shown = value instanceof Pawn pawn ? nameOf(pawn) : String.valueOf(value);
            text = text.replace("{" + i + "}", shown);
        }
        lastMessage = text;
        final Group pill = GameStyle.pill(text, height * 0.042);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(height * 0.025);
        message.getChildren().setAll(pill);
    }

    String nameOf(final Pawn pawn) {
        return gameContext.getTranslator().translate("Goose" + pawn.color);
    }

    /**
     * The pawn whose turn it is, at the top left: its Biboule and its name ("You" against the computer).
     */
    private void showTurn(final Pawn pawn) {
        hud.getChildren().clear();
        final double size = height * 0.085;
        final ImageView view = new ImageView(pawn.image);
        view.setPreserveRatio(true);
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setLayoutX(height * 0.025);
        view.setLayoutY(height * 0.02);
        final String name = players.againstComputer() && pawn.human
            ? gameContext.getTranslator().translate("GooseYou") : nameOf(pawn);
        final Group pill = GameStyle.pill(name, height * 0.032);
        pill.setLayoutX(height * 0.025 + size * 1.1);
        pill.setLayoutY(height * 0.02 + (size - pill.getBoundsInLocal().getHeight()) / 2);
        hud.getChildren().addAll(view, pill);
    }

    /**
     * Puts the pawns on their squares; pawns sharing a square are smaller and around its center.
     */
    private void placePawns() {
        for (final Pawn pawn : pawns) {
            if (pawn == mover) {
                continue;
            }
            final Point2D point = pointOf(pawn);
            final List<Pawn> together = pawns.stream().filter(other -> other != mover && other.square == pawn.square)
                .toList();
            final double scale = together.size() > 1 ? 0.72 : 1;
            pawn.node.setScaleX(scale);
            pawn.node.setScaleY(scale);
            pawn.node.setLayoutX(point.getX());
            pawn.node.setLayoutY(point.getY());
        }
    }

    /**
     * @return where a pawn stands on its square: the center, or around it when the square is shared
     */
    private Point2D pointOf(final Pawn pawn) {
        final Point2D center = centers.get(pawn.square);
        final List<Pawn> together = pawns.stream().filter(other -> other != mover && other.square == pawn.square)
            .toList();
        if (together.size() < 2 || !together.contains(pawn)) {
            return center;
        }
        final double angle = 2 * Math.PI * together.indexOf(pawn) / together.size() - Math.PI / 2;
        return center.add(Math.cos(angle) * pawnSize * 0.3, Math.sin(angle) * pawnSize * 0.3);
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    /**
     * For the tests: puts a pawn on a square.
     */
    void put(final int index, final int square) {
        pawns.get(index).square = square;
        placePawns();
    }

    Group getRollButton() {
        return rollButton;
    }

    FixationRing getRing() {
        return ring;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (ring != null) {
            ring.stop();
        }
        if (rollButton != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(rollButton.getChildren().get(0));
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A pawn: its Biboule on a base, its square, the turns it sleeps, and whether it waits in the well or the prison.
     */
    static final class Pawn {
        @Getter
        private final int index;
        private final String color;
        @Getter
        private final boolean human;
        private final Group node;
        private final Image image;
        @Getter
        private int square = GooseBoard.START;
        @Getter
        private int asleep;
        @Getter
        private boolean waiting;

        Pawn(final int index, final String color, final boolean human, final Group node, final Image image) {
            this.index = index;
            this.color = color;
            this.human = human;
            this.node = node;
            this.image = image;
        }

        /**
         * For the tests: puts the pawn on a square.
         */
        void setSquare(final int square) {
            this.square = square;
        }

        /**
         * @return its base and its Biboule, centered on (0, 0)
         */
        Group getNode() {
            return node;
        }
    }
}
