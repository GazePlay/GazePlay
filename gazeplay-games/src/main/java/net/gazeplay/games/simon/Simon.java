package net.gazeplay.games.simon;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * "Répète après moi" (Repeat After Me, name code Simon, formerly "Simon", the name of a commercial game; reviewed on
 * 10/11/2026, maintainer's rules): a round device of 4 colored buttons, each with its symbol and its note (do, mi, sol
 * and the higher do: they always sound right together). The device plays a melody, and the player repeats it, a
 * fixation on each button (see {@link SimonMode} for the modes). A mistake shows at once: a low sound, and the right
 * button blinks. The melody grows by one note at each turn; the game ends when it has 5, 8 or 12 notes ("Bravo"), or
 * never; in the classic mode, 3 mistakes end it.
 * <p>
 * Everything is drawn by the game. One loop, in real seconds: the notes are played at the speed of the settings, the
 * fixations last the length of the settings (read at each step), nothing can be chosen before the delay of the
 * settings nor while the device plays, and nothing moves while the menu of the game is open. (Before the review: 3
 * seconds per note, a mistake seen only at the end of the melody, then the same melody without end, 4 notes drawn at
 * random among 7, a timer created 60 times a second, a device of 400 pixels whatever the screen, no goal counted, the
 * statistics saved with those of the Piano.)
 */
public class Simon implements GameLifeCycle {

    static final int BUTTONS = 4;
    static final int LIVES = 3;
    /**
     * In seconds (at the speed 1 of the settings): a note of the device, the silence after it, the silence before the
     * melody, a button lit when the player chooses it, the right button blinking after a mistake, the end shown.
     */
    static final double NOTE = 0.6;
    static final double GAP = 0.25;
    static final double BEFORE = 0.8;
    static final double PRESS = 0.3;
    static final double MISTAKE = 1.4;
    static final double SHOW = 2.5;

    private static final String SOUNDS = "data/simonGame/sounds/";
    private static final Color BACKGROUND = Color.rgb(232, 236, 244);
    /**
     * Green at the top left, red at the top right, yellow at the bottom left, blue at the bottom right.
     */
    private static final Color[] COLORS = {Color.rgb(52, 176, 92), Color.rgb(226, 64, 64), Color.rgb(246, 196, 40),
        Color.rgb(52, 116, 224)};
    private static final double[] ANGLES = {90, 0, 180, 270};

    enum Phase { WAIT, BEFORE, LISTEN, INPUT, ADD, MISTAKE, SHOW, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final SimonMode mode;
    @Getter
    private final SimonEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private final List<Integer> melody = new ArrayList<>();
    /**
     * The note of the melody the player must play next, or the note the device plays.
     */
    @Getter
    private int position;
    @Getter
    private int lives;
    /**
     * The longest melody repeated without a mistake.
     */
    @Getter
    private int best;
    /**
     * With two players: the one to play (1 or 2), and the winner (0 if none).
     */
    @Getter
    private int player = 1;
    @Getter
    private int winner;
    private boolean won;
    private double elapsed;
    @Getter
    private double waiting;
    private int lit = -1;
    private double litLeft;
    private int expected;

    private double width;
    private double height;
    @Getter
    private double radius;
    private double centerX;
    private double centerY;

    private Group layer;
    private final Shape[] buttons = new Shape[BUTTONS];
    private Text count;
    private Text state;
    private FixationRing ring;
    private Group hud;
    private Group message;
    private final List<Node> gazeNodes = new ArrayList<>();

    private int looked = -1;
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
    Simon(final IGameContext gameContext, final Stats stats, final SimonMode mode, final SimonEnd end,
          final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.end = end;
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
        final double top = height * 0.11;
        final double margin = Math.min(width, height) * 0.04;
        radius = Math.min(height - top - margin, width - 2 * margin) / 2;
        centerX = width / 2;
        centerY = top + (height - top - margin) / 2;

        final Rectangle background = new Rectangle(0, 0, width, height);
        background.setFill(BACKGROUND);
        background.setMouseTransparent(true);
        final Circle rim = new Circle(centerX, centerY, radius, Color.rgb(64, 66, 78));
        rim.setMouseTransparent(true);
        final Group device = new Group(rim);
        for (int button = 0; button < BUTTONS; button++) {
            buttons[button] = button(button);
            device.getChildren().add(buttons[button]);
            final int index = button;
            listen(buttons[button], () -> lookAt(index), () -> lookAway(index));
        }
        for (int button = 0; button < BUTTONS; button++) {
            final double angle = Math.toRadians(ANGLES[button] + 45);
            final Node symbol = symbol(button, radius * 0.2);
            symbol.setLayoutX(centerX + Math.cos(angle) * radius * 0.62);
            symbol.setLayoutY(centerY - Math.sin(angle) * radius * 0.62);
            device.getChildren().add(symbol);
        }
        final Circle middle = new Circle(centerX, centerY, radius * 0.3, Color.rgb(64, 66, 78));
        middle.setMouseTransparent(true);
        count = new Text();
        count.setFont(GameStyle.titleFont(radius * 0.2));
        count.setFill(Color.WHITE);
        count.setTextOrigin(VPos.CENTER);
        count.setMouseTransparent(true);
        state = new Text();
        state.setFont(GameStyle.titleFont(radius * 0.075));
        state.setFill(Color.rgb(220, 224, 236));
        state.setTextOrigin(VPos.CENTER);
        state.setTextAlignment(TextAlignment.CENTER);
        state.setWrappingWidth(radius * 0.5);
        state.setMouseTransparent(true);
        device.getChildren().addAll(middle, count, state);
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        layer = new Group(background, device, ring, hud, message);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        melody.clear();
        lives = LIVES;
        best = 0;
        player = 1;
        winner = 0;
        elapsed = 0;
        waiting = configuration.getGameStartDelay() / 1000.0;
        phase = Phase.WAIT;
        paint();
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * @return a quarter of the ring of the device, with gaps between the quarters and a hole in the middle
     */
    private Shape button(final int button) {
        final Arc quarter = new Arc(centerX, centerY, radius * 0.95, radius * 0.95, ANGLES[button], 90);
        quarter.setType(ArcType.ROUND);
        Shape shape = Shape.subtract(quarter, new Circle(centerX, centerY, radius * 0.36));
        final double gap = radius * 0.06;
        shape = Shape.subtract(shape, new Rectangle(centerX - gap / 2, centerY - radius, gap, radius * 2));
        shape = Shape.subtract(shape, new Rectangle(centerX - radius, centerY - gap / 2, radius * 2, gap));
        return shape;
    }

    /**
     * @return the symbol of a button (star, circle, square or triangle), white, centered on (0, 0)
     */
    static Node symbol(final int button, final double size) {
        final double r = size / 2;
        final Shape shape = switch (button) {
            case 1 -> {
                final Polygon star = new Polygon();
                for (int point = 0; point < 10; point++) {
                    final double angle = -Math.PI / 2 + point * Math.PI / 5;
                    final double distance = point % 2 == 0 ? r : r * 0.45;
                    star.getPoints().addAll(distance * Math.cos(angle), distance * Math.sin(angle) + r * 0.06);
                }
                yield star;
            }
            case 3 -> new Circle(r * 0.8);
            case 2 -> new Rectangle(-r * 0.72, -r * 0.72, r * 1.44, r * 1.44);
            default -> new Polygon(0, -r * 0.86, r * 0.92, r * 0.72, -r * 0.92, r * 0.72);
        };
        shape.setFill(Color.rgb(255, 255, 255, 0.85));
        shape.setMouseTransparent(true);
        return shape;
    }

    /**
     * Colors the buttons: pale, or bright and shining when lit; the middle shows the melody and what to do.
     */
    private void paint() {
        for (int button = 0; button < BUTTONS; button++) {
            final boolean on = button == lit;
            buttons[button].setFill(on ? COLORS[button].deriveColor(0, 0.9, 1.25, 1)
                : COLORS[button].deriveColor(0, 0.62, 0.9, 1));
            buttons[button].setEffect(on ? new DropShadow(radius * 0.12, COLORS[button].deriveColor(0, 1, 1.3, 1))
                : null);
        }
        count.setText(Integer.toString(melody.size()));
        count.setX(centerX - count.getLayoutBounds().getWidth() / 2);
        count.setY(centerY - radius * 0.07);
        final String key = switch (phase) {
            case LISTEN, BEFORE -> "SimonListen";
            case INPUT -> mode == SimonMode.FREE ? "SimonAddNote" : "SimonYourTurn";
            case ADD -> "SimonAddNote";
            default -> null;
        };
        state.setText(key == null ? "" : gameContext.getTranslator().translate(key));
        state.setX(centerX - radius * 0.25);
        state.setY(centerY + radius * 0.13);
    }

    /**
     * Makes a node receive the gaze and the mouse.
     */
    private void listen(final Node node, final Runnable enter, final Runnable exit) {
        final EventHandler<Event> handler = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                enter.run();
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                exit.run();
            }
        };
        node.addEventFilter(MouseEvent.ANY, handler);
        node.addEventFilter(GazeEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(node);
        gazeNodes.add(node);
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        final double scaled = seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        if (litLeft > 0) {
            litLeft -= scaled;
            if (litLeft <= 0) {
                lit = -1;
                paint();
            }
        }
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= waiting) {
                    stats.notifyNewRoundReady();
                    start();
                }
            }
            case BEFORE -> {
                elapsed += scaled;
                if (elapsed >= BEFORE) {
                    phase = Phase.LISTEN;
                    position = 0;
                    elapsed = 0;
                    light(melody.get(0), NOTE);
                    paint();
                }
            }
            case LISTEN -> {
                elapsed += scaled;
                if (elapsed >= NOTE + GAP) {
                    position++;
                    elapsed = 0;
                    if (position < melody.size()) {
                        light(melody.get(position), NOTE);
                    } else {
                        listened();
                    }
                }
            }
            case INPUT, ADD -> fixate(seconds);
            case MISTAKE -> {
                elapsed += scaled;
                // The right button blinks
                final boolean on = ((int) (elapsed / (MISTAKE / 6))) % 2 == 0;
                if (on != (lit == expected)) {
                    lit = on ? expected : -1;
                    litLeft = on ? MISTAKE : 0;
                    paint();
                }
                if (elapsed >= MISTAKE) {
                    lit = -1;
                    litLeft = 0;
                    afterMistake();
                }
            }
            case SHOW -> {
                elapsed += seconds;
                if (elapsed >= SHOW) {
                    finish();
                }
            }
            default -> {
            }
        }
    }

    /**
     * The first turn: the device plays a first note (classic), or a note is to be chosen (two players, free).
     */
    private void start() {
        if (mode == SimonMode.CLASSIC) {
            melody.add(random.nextInt(BUTTONS));
            listenAgain();
        } else {
            phase = mode == SimonMode.FREE ? Phase.INPUT : Phase.ADD;
            showLooked();
            paint();
            showHud();
        }
    }

    /**
     * The device plays the melody, after a short silence.
     */
    private void listenAgain() {
        phase = Phase.BEFORE;
        elapsed = 0;
        position = 0;
        ring.stop();
        progress = 0;
        if (mode != SimonMode.FREE) {
            stats.incrementNumberOfGoalsToReach();
        }
        paint();
        showHud();
    }

    /**
     * The device has played the melody: the player repeats it (or, in free mode, adds a note).
     */
    private void listened() {
        phase = Phase.INPUT;
        position = 0;
        elapsed = 0;
        showLooked();
        paint();
        showHud();
    }

    private void light(final int button, final double seconds) {
        lit = button;
        litLeft = seconds;
        gameContext.getSoundManager().add(SOUNDS + "note" + button + ".wav");
        paint();
    }

    private void fixate(final double seconds) {
        if (looked < 0) {
            return;
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            progress = 0;
            ring.stop();
            press(looked);
        }
    }

    /**
     * The gaze (or the mouse) enters a button.
     */
    void lookAt(final int button) {
        if (looked == button) {
            return;
        }
        looked = button;
        progress = 0;
        ring.stop();
        showLooked();
    }

    void lookAway(final int button) {
        if (looked == button) {
            looked = -1;
            progress = 0;
            ring.stop();
        }
    }

    private void showLooked() {
        if (looked >= 0 && (phase == Phase.INPUT || phase == Phase.ADD)) {
            final double angle = Math.toRadians(ANGLES[looked] + 45);
            ring.setSize(radius * 0.42);
            ring.show(centerX + Math.cos(angle) * radius * 0.62, centerY - Math.sin(angle) * radius * 0.62);
            ring.setProgress(progress);
        } else {
            ring.stop();
        }
    }

    /**
     * A button chosen by the player: it lights up with its note; it is checked against the melody, or added to it.
     */
    void press(final int button) {
        if (phase != Phase.INPUT && phase != Phase.ADD) {
            return;
        }
        light(button, PRESS);
        if (phase == Phase.ADD || mode == SimonMode.FREE) {
            melody.add(button);
            if (mode == SimonMode.FREE) {
                stats.incrementNumberOfGoalsToReach();
                stats.incrementNumberOfGoalsReached();
                best = melody.size();
            } else {
                player = 3 - player;
            }
            if (end.getNotes() > 0 && melody.size() >= end.getNotes() && mode == SimonMode.FREE) {
                won = true;
                endGame("SimonComplete");
                return;
            }
            listenAgain();
            return;
        }
        if (button == melody.get(position)) {
            position++;
            if (position == melody.size()) {
                repeated();
            }
            return;
        }
        // A mistake: a low sound, the right button blinks
        expected = melody.get(position);
        gameContext.getSoundManager().add(SOUNDS + "mistake.wav");
        phase = Phase.MISTAKE;
        elapsed = 0;
        ring.stop();
        progress = 0;
        if (mode == SimonMode.CLASSIC) {
            lives--;
        }
        paint();
        showHud();
    }

    /**
     * The whole melody repeated: one more goal; the melody grows, or the game is won.
     */
    private void repeated() {
        stats.incrementNumberOfGoalsReached();
        best = Math.max(best, melody.size());
        if (end.getNotes() > 0 && melody.size() >= end.getNotes()) {
            gameContext.getSoundManager().add(SOUNDS + "success.wav");
            won = true;
            winner = 0;
            endGame("SimonComplete");
            return;
        }
        if (mode == SimonMode.CLASSIC) {
            melody.add(random.nextInt(BUTTONS));
            listenAgain();
        } else {
            // Two players: the one who repeated adds a note
            phase = Phase.ADD;
            ring.stop();
            progress = 0;
            showLooked();
            paint();
            showHud();
        }
    }

    private void afterMistake() {
        if (mode == SimonMode.DUO) {
            winner = 3 - player;
            won = true;
            endGame(winner == 1 ? "SimonPlayer1Wins" : "SimonPlayer2Wins");
        } else if (lives <= 0) {
            won = false;
            endGame("SimonBest");
        } else {
            listenAgain();
        }
    }

    private void endGame(final String key) {
        String text = gameContext.getTranslator().translate(key);
        if ("SimonBest".equals(key)) {
            text = text + " " + best;
        }
        final Group pill = GameStyle.pill(text, height * 0.045);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(centerY - pill.getBoundsInLocal().getHeight() / 2);
        message.getChildren().setAll(pill);
        ring.stop();
        phase = Phase.SHOW;
        elapsed = 0;
        paint();
        showHud();
    }

    private void finish() {
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        if (won) {
            gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
        } else {
            gameContext.showRoundStats(stats, this);
        }
    }

    boolean isWon() {
        return won;
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final List<Node> parts = new ArrayList<>();
        if (mode == SimonMode.DUO) {
            parts.add(GameStyle.pill(gameContext.getTranslator().translate(player == 1 ? "SimonPlayer1"
                : "SimonPlayer2"), fontSize));
        }
        if (mode == SimonMode.CLASSIC) {
            // The lives: the hearts shared by the reviewed games, at the top left
            final double margin = height * 0.025;
            final Group hearts = GameStyle.lives(lives, LIVES, height * 0.055, width * 0.3, false);
            hearts.setLayoutX(margin);
            hearts.setLayoutY(margin * 0.6);
            hud.getChildren().add(hearts);
        }
        parts.add(GameStyle.pill(gameContext.getTranslator().translate("SimonNotes") + " " + melody.size()
            + (end.getNotes() > 0 ? " / " + end.getNotes() : ""), fontSize));
        final double gap = fontSize * 1.2;
        double total = -gap;
        for (final Node part : parts) {
            total += part.getBoundsInLocal().getWidth() + gap;
        }
        double x = (width - total) / 2;
        for (final Node part : parts) {
            part.setLayoutX(x);
            part.setLayoutY(height * 0.02);
            x += part.getBoundsInLocal().getWidth() + gap;
            hud.getChildren().add(part);
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
     * @return the button lit, -1 if none
     */
    int getLit() {
        return lit;
    }

    Shape buttonOf(final int button) {
        return buttons[button];
    }

    /**
     * @return the middle of the device and its radius on the screen
     */
    double[] layout() {
        return new double[]{centerX, centerY, radius};
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (ring != null) {
            ring.stop();
        }
        for (final Node node : new ArrayList<>(gazeNodes)) {
            gameContext.getGazeDeviceManager().removeEventFilter(node);
        }
        gazeNodes.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }
}
