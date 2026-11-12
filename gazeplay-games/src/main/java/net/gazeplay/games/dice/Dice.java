package net.gazeplay.games.dice;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;
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

import java.util.ArrayList;
import java.util.List;

/**
 * "Dés" (name code Dice; reviewed on 12/11/2026, maintainer's rules), two modes on a green felt table.
 * <ul>
 * <li>Counting: a fixation on the "Roll" button rolls 1, 2 or 3 dice; three cards then show the right total and two
 * wrong totals close to it; a fixation on a card chooses it. The right card turns green and the next roll comes; a
 * wrong card is greyed and the player looks again, or costs one of 3 lives (the right total is then shown), depending
 * on the variant. The game ends after 5, 10 or 20 totals found ("Bravo"), or never, or when the lives are lost
 * ("Lost!").</li>
 * <li>Throwing: a tool for a board game, 1 to 6 dice; a fixation on the "Roll" button rolls them all, a fixation on
 * one die rolls it alone; the total is shown. No end.</li>
 * </ul>
 * One loop, in real seconds: the fixations last the length of the settings (read at each step), nothing can be chosen
 * before the delay of the settings nor while the dice roll, and nothing moves while the menu of the game is open.
 * (Before the review: the throwing tool only, a die could be rolled again while all were rolling, goals reached but
 * none to reach, the former progress indicator on the dice, the total in the font of the system, two copies of the
 * whole game, a button picture, a texture and a sound of unknown source.)
 */
public class Dice implements GameLifeCycle {

    static final int LIVES = 3;
    /**
     * In seconds: the dice rolling (the die turns for 1.5 s), the right total found, the right total shown after a
     * mistake with lives, a wrong card shaking, the end shown.
     */
    static final double ROLL = 1.6;
    static final double FOUND = 1.4;
    static final double REVEAL = 2.2;
    static final double SHAKE = 0.45;
    static final double SHOW = 2.2;

    private static final String SOUNDS = "data/dice/sounds/";
    private static final Color FELT_CENTER = Color.rgb(64, 142, 96);
    private static final Color FELT_EDGE = Color.rgb(34, 88, 58);
    private static final Color BUTTON = Color.rgb(242, 166, 90);
    private static final Color PIP = Color.rgb(38, 34, 47);
    private static final Color CARD = Color.WHITE;
    private static final Color CARD_LIT = Color.rgb(255, 238, 170);
    private static final Color CARD_BORDER = Color.rgb(196, 204, 220);
    private static final Color NUMBER = Color.rgb(48, 54, 72);
    private static final Color RIGHT = Color.web(GameStyle.SUCCESS_COLOR);
    private static final Color WRONG = Color.rgb(214, 64, 64);
    private static final Color GREYED = Color.rgb(150, 156, 170);

    enum Phase { WAIT, READY, ROLLING, ANSWER, FOUND, REVEAL, SHOW, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final DiceMode mode;
    @Getter
    private final int diceCount;
    @Getter
    private final DiceEnd end;
    @Getter
    private final DiceMistakes mistakes;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private final int[] faces;
    @Getter
    private int total;
    @Getter
    private int lives;
    @Getter
    private int found;
    @Getter
    private int rolls;
    @Getter
    private boolean bravo;
    private double elapsed;

    private double width;
    private double height;
    private double dieSide;
    private double diceY;
    private double buttonX;
    private double buttonY;
    private double buttonRadius;
    private double cardWidth;
    private double cardHeight;

    private Group layer;
    private Group diceLayer;
    private Group cardsLayer;
    private Group hud;
    private Group message;
    private FixationRing ring;
    private final List<DiceRoller> dice = new ArrayList<>();
    @Getter
    private final List<Spot> dieSpots = new ArrayList<>();
    @Getter
    private Spot rollSpot;
    @Getter
    private final List<Card> cards = new ArrayList<>();
    private final List<Node> gazeNodes = new ArrayList<>();

    @Getter
    private Spot looked;
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
     * @param diceCount the dice rolled: 1 to 3 to count, 1 to 6 to throw
     * @param gameSeed  the seed of the game to replay, or a negative value for a new game
     */
    Dice(final IGameContext gameContext, final Stats stats, final DiceMode mode, final int diceCount,
         final DiceEnd end, final DiceMistakes mistakes, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.diceCount = diceCount;
        this.end = end;
        this.mistakes = mistakes;
        this.faces = new int[diceCount];
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

        final Group table = table();
        diceLayer = new Group();
        cardsLayer = new Group();
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);

        // The dice in a row, larger when there are fewer; all show 1 at the start
        final boolean counting = mode == DiceMode.COUNT;
        final double spacing = counting ? 1.55 : 1.45;
        dieSide = Math.min(height * (counting ? 0.24 : 0.22), width * 0.86 / (diceCount * spacing));
        diceY = height * (counting ? 0.34 : 0.43);
        final double startX = width / 2 - (diceCount - 1) * dieSide * spacing / 2;
        for (int i = 0; i < diceCount; i++) {
            faces[i] = 1;
            final double x = startX + i * dieSide * spacing;
            final DiceRoller die = new DiceRoller((float) (dieSide / 2), gameContext.getSoundManager(), random);
            die.setLayoutX(x);
            die.setLayoutY(diceY);
            die.setMouseTransparent(true);
            // A soft shadow on the felt, under the die
            final Ellipse shadow = new Ellipse(x + dieSide * 0.06, diceY + dieSide * 0.62, dieSide * 0.56,
                dieSide * 0.14);
            shadow.setFill(Color.rgb(0, 0, 0, 0.25));
            shadow.setMouseTransparent(true);
            final Circle zone = new Circle(x, diceY, dieSide * 0.82, Color.TRANSPARENT);
            diceLayer.getChildren().addAll(shadow, zone, die);
            dice.add(die);
            final int index = i;
            final Spot spot = new Spot(zone, x, diceY, dieSide * 1.75, () -> rollOne(index));
            dieSpots.add(spot);
            if (!counting) {
                listen(spot);
            }
        }

        buttonRadius = height * 0.085;
        buttonX = width / 2;
        buttonY = height * (counting ? 0.76 : 0.8);
        final Group button = rollButton();
        rollSpot = new Spot(button, buttonX, buttonY, buttonRadius * 2.5, this::rollAll);
        listen(rollSpot);

        cardHeight = Math.min(height * 0.24, width * 0.17);
        cardWidth = cardHeight * 1.05;

        layer = new Group(table, diceLayer, button, cardsLayer, ring, hud, message);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        lives = LIVES;
        found = 0;
        rolls = 0;
        bravo = false;
        elapsed = 0;
        phase = Phase.WAIT;
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * @return the green felt of the table, darker at the edges, with a light stitching
     */
    private Group table() {
        final Rectangle felt = new Rectangle(0, 0, width, height);
        felt.setFill(new RadialGradient(0, 0, 0.5, 0.45, 0.75, true, CycleMethod.NO_CYCLE,
            new Stop(0, FELT_CENTER), new Stop(1, FELT_EDGE)));
        final double inset = Math.min(width, height) * 0.035;
        final Rectangle stitch = new Rectangle(inset, inset, width - 2 * inset, height - 2 * inset);
        stitch.setArcWidth(inset * 2);
        stitch.setArcHeight(inset * 2);
        stitch.setFill(null);
        stitch.setStroke(Color.rgb(255, 255, 255, 0.28));
        stitch.setStrokeWidth(Math.max(2, height * 0.004));
        stitch.getStrokeDashArray().setAll(height * 0.018, height * 0.012);
        final Group group = new Group(felt, stitch);
        group.setMouseTransparent(true);
        return group;
    }

    /**
     * @return the round "Roll" button: an orange disc with a die, its name under it
     */
    private Group rollButton() {
        final Circle disc = new Circle(buttonX, buttonY, buttonRadius, BUTTON);
        disc.setStroke(Color.rgb(255, 250, 240));
        disc.setStrokeWidth(Math.max(3, buttonRadius * 0.07));
        final double side = buttonRadius * 1.0;
        final Rectangle face = new Rectangle(buttonX - side / 2, buttonY - side / 2, side, side);
        face.setArcWidth(side * 0.3);
        face.setArcHeight(side * 0.3);
        face.setFill(Color.rgb(251, 250, 246));
        face.setStroke(Color.rgb(222, 217, 206));
        face.setStrokeWidth(Math.max(1, side * 0.03));
        final Group group = new Group(disc, face);
        final double[][] pips = {{-1, -1}, {1, -1}, {0, 0}, {-1, 1}, {1, 1}};
        for (final double[] pip : pips) {
            final Circle dot = new Circle(buttonX + pip[0] * side * 0.27, buttonY + pip[1] * side * 0.27,
                side * 0.1, PIP);
            dot.setMouseTransparent(true);
            group.getChildren().add(dot);
        }
        face.setMouseTransparent(true);
        final Group label = GameStyle.pill(gameContext.getTranslator().translate("DiceRoll"), height * 0.032);
        label.setLayoutX(buttonX - label.getBoundsInLocal().getWidth() / 2);
        label.setLayoutY(buttonY + buttonRadius * 1.12);
        label.setMouseTransparent(true);
        group.getChildren().add(label);
        return group;
    }

    /**
     * Makes a spot receive the gaze and the mouse.
     */
    private void listen(final Spot spot) {
        final EventHandler<Event> handler = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                lookAt(spot);
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                lookAway(spot);
            }
        };
        spot.node.addEventFilter(MouseEvent.ANY, handler);
        spot.node.addEventFilter(GazeEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(spot.node);
        gazeNodes.add(spot.node);
    }

    private void forget(final Node node) {
        gameContext.getGazeDeviceManager().removeEventFilter(node);
        gazeNodes.remove(node);
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        cards.forEach(card -> card.shake(seconds));
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    ready();
                }
            }
            case READY, ANSWER -> fixate(seconds);
            case ROLLING -> {
                elapsed += seconds;
                if (elapsed >= ROLL) {
                    rolled();
                }
            }
            case FOUND -> {
                elapsed += seconds;
                if (elapsed >= FOUND) {
                    if (end.getTotals() > 0 && found >= end.getTotals()) {
                        bravo = true;
                        finish();
                    } else {
                        ready();
                    }
                }
            }
            case REVEAL -> {
                elapsed += seconds;
                if (elapsed >= REVEAL) {
                    if (lives <= 0) {
                        endGame();
                    } else {
                        ready();
                    }
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
     * The dice can be rolled: the "Roll" button shows (the cards of the last roll go).
     */
    private void ready() {
        clearCards();
        rollSpot.node.setVisible(true);
        phase = Phase.READY;
        elapsed = 0;
        if (looked != null && isEnabled(looked)) {
            showRing(looked);
        }
    }

    private boolean isEnabled(final Spot spot) {
        if (spot == rollSpot) {
            return phase == Phase.READY;
        }
        if (dieSpots.contains(spot)) {
            return phase == Phase.READY && mode == DiceMode.THROW;
        }
        return phase == Phase.ANSWER && spot.card != null && !spot.card.done;
    }

    private void fixate(final double seconds) {
        if (looked == null || !isEnabled(looked)) {
            return;
        }
        if (!ring.isVisible()) {
            showRing(looked);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Spot spot = looked;
            progress = 0;
            ring.stop();
            spot.action.run();
        }
    }

    private void showRing(final Spot spot) {
        ring.setSize(spot.ringSize);
        ring.show(spot.x, spot.y);
    }

    void lookAt(final Spot spot) {
        if (looked == spot) {
            return;
        }
        looked = spot;
        progress = 0;
        ring.stop();
        paintCards();
        if (isEnabled(spot)) {
            showRing(spot);
        }
    }

    void lookAway(final Spot spot) {
        if (looked == spot) {
            looked = null;
            progress = 0;
            ring.stop();
            paintCards();
        }
    }

    /**
     * All the dice roll; counting, a new total to find.
     */
    void rollAll() {
        if (phase != Phase.READY) {
            return;
        }
        for (int i = 0; i < diceCount; i++) {
            faces[i] = dice.get(i).roll(null);
        }
        startRolling();
    }

    /**
     * One die rolls alone (throwing only).
     */
    void rollOne(final int index) {
        if (phase != Phase.READY || mode != DiceMode.THROW) {
            return;
        }
        faces[index] = dice.get(index).roll(null);
        startRolling();
    }

    private void startRolling() {
        total = 0;
        for (final int face : faces) {
            total += face;
        }
        rolls++;
        looked = null;
        progress = 0;
        ring.stop();
        rollSpot.node.setVisible(mode == DiceMode.THROW);
        message.getChildren().clear();
        if (mode == DiceMode.THROW) {
            stats.incrementNumberOfGoalsToReach();
            stats.incrementNumberOfGoalsReached();
        }
        phase = Phase.ROLLING;
        elapsed = 0;
    }

    /**
     * The dice have settled: throwing, the total is shown; counting, the three cards come.
     */
    private void rolled() {
        if (mode == DiceMode.THROW) {
            showTotal();
            phase = Phase.READY;
            elapsed = 0;
            return;
        }
        final List<Integer> values = new ArrayList<>(wrongTotals(total, diceCount, diceCount * 6, random));
        values.add(random.nextInt(values.size() + 1), total);
        final double gap = cardWidth * 0.35;
        final double left = (width - values.size() * cardWidth - (values.size() - 1) * gap) / 2;
        final double top = height - cardHeight - height * 0.07;
        for (int i = 0; i < values.size(); i++) {
            cards.add(new Card(values.get(i), values.get(i) == total, left + i * (cardWidth + gap), top));
        }
        showQuestion("DiceHowMany");
        stats.incrementNumberOfGoalsToReach();
        phase = Phase.ANSWER;
        elapsed = 0;
    }

    /**
     * @return two wrong totals close to the right one, between the smallest and the largest total, different, never
     * one more and one less by the same amount (the right total would be the one in the middle)
     */
    static List<Integer> wrongTotals(final int total, final int smallest, final int largest,
                                     final ReplayablePseudoRandom random) {
        final List<Integer> candidates = new ArrayList<>();
        for (int distance = 1; distance <= 4 && candidates.size() < 4; distance++) {
            final List<Integer> pair = new ArrayList<>();
            if (total - distance >= smallest) {
                pair.add(total - distance);
            }
            if (total + distance <= largest) {
                pair.add(total + distance);
            }
            if (pair.size() == 2 && random.nextInt(2) == 1) {
                pair.add(pair.remove(0));
            }
            candidates.addAll(pair);
        }
        final int first = candidates.get(0);
        for (int i = 1; i < candidates.size(); i++) {
            final int second = candidates.get(i);
            if (second - total != total - first) {
                return List.of(first, second);
            }
        }
        return List.of(first, candidates.get(1));
    }

    /**
     * A card chosen: the right one ends the roll; a wrong one is greyed (looking again) or costs a life (the right
     * total is then shown).
     */
    void choose(final Card card) {
        if (phase != Phase.ANSWER || card.done) {
            return;
        }
        looked = null;
        if (card.right) {
            card.markRight();
            cards.stream().filter(other -> other != card).forEach(Card::fade);
            gameContext.getSoundManager().add(SOUNDS + "right.wav");
            found++;
            stats.incrementNumberOfGoalsReached();
            showQuestion(null);
            phase = Phase.FOUND;
            elapsed = 0;
            showHud();
            return;
        }
        stats.incrementNumberOfMistakes();
        gameContext.getSoundManager().add(SOUNDS + "wrong.wav");
        if (mistakes == DiceMistakes.SEARCH_AGAIN) {
            card.markWrong(true);
            paintCards();
            return;
        }
        card.markWrong(false);
        cards.stream().filter(Card::isRight).forEach(Card::markRight);
        lives--;
        showQuestion(null);
        phase = Phase.REVEAL;
        elapsed = 0;
        showHud();
    }

    private void endGame() {
        showQuestion("DiceLost");
        phase = Phase.SHOW;
        elapsed = 0;
    }

    private void finish() {
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        if (bravo) {
            gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
        } else {
            gameContext.showRoundStats(stats, this);
        }
    }

    private void clearCards() {
        cards.forEach(card -> forget(card.node));
        if (looked != null && looked.card != null) {
            looked = null;
            progress = 0;
            ring.stop();
        }
        cards.clear();
        cardsLayer.getChildren().clear();
        if (mode == DiceMode.COUNT) {
            message.getChildren().clear();
        }
    }

    private void paintCards() {
        cards.forEach(card -> card.paint(looked == card.spot && phase == Phase.ANSWER && !card.done));
    }

    /**
     * Throwing: the total, in a large label above the dice.
     */
    private void showTotal() {
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("DiceTotal") + " " + total,
            height * 0.075);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(height * 0.1);
        message.getChildren().setAll(pill);
    }

    /**
     * Counting: a short text between the dice and the cards ("How many points?", "Lost!"), none for null.
     */
    private void showQuestion(final String key) {
        if (key == null) {
            message.getChildren().clear();
            return;
        }
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate(key), height * 0.04);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(diceY + dieSide * 0.95);
        message.getChildren().setAll(pill);
    }

    private void showHud() {
        hud.getChildren().clear();
        if (mode != DiceMode.COUNT) {
            return;
        }
        final double margin = height * 0.025;
        if (mistakes == DiceMistakes.LIVES) {
            final Group hearts = GameStyle.lives(lives, LIVES, height * 0.055, width * 0.3, true);
            hearts.setLayoutX(margin * 2);
            hearts.setLayoutY(margin * 1.6);
            hud.getChildren().add(hearts);
        }
        final Group score = GameStyle.pill(gameContext.getTranslator().translate("DiceFound") + " " + found
            + (end.getTotals() > 0 ? " / " + end.getTotals() : ""), height * 0.035);
        score.setLayoutX((width - score.getBoundsInLocal().getWidth()) / 2);
        score.setLayoutY(margin * 1.6);
        hud.getChildren().add(score);
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    Group getMessage() {
        return message;
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
        new ArrayList<>(gazeNodes).forEach(this::forget);
        cards.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * Something chosen by a fixation: the "Roll" button, a die (throwing) or a card (counting).
     */
    final class Spot {
        @Getter
        private final Node node;
        private final double x;
        private final double y;
        private final double ringSize;
        private final Runnable action;
        private Card card;

        Spot(final Node node, final double x, final double y, final double ringSize, final Runnable action) {
            this.node = node;
            this.x = x;
            this.y = y;
            this.ringSize = ringSize;
            this.action = action;
        }
    }

    /**
     * A card: a rounded white card with a number; lit while looked at, green with a check when right, red with a cross
     * when wrong (then greyed when looking again), faded when the roll is over.
     */
    final class Card {
        @Getter
        private final int value;
        @Getter
        private final boolean right;
        @Getter
        private final Spot spot;
        private final Group node = new Group();
        private final Rectangle back;
        private final Text number;
        @Getter
        private boolean done;
        @Getter
        private boolean greyed;
        private double shaking;

        Card(final int value, final boolean right, final double x, final double y) {
            this.value = value;
            this.right = right;
            back = new Rectangle(cardWidth, cardHeight);
            back.setArcWidth(cardWidth * 0.18);
            back.setArcHeight(cardWidth * 0.18);
            back.setStrokeWidth(Math.max(2, cardWidth * 0.025));
            number = new Text(Integer.toString(value));
            number.setFont(GameStyle.titleFont(cardHeight * 0.5));
            number.setTextOrigin(VPos.CENTER);
            number.setX((cardWidth - number.getLayoutBounds().getWidth()) / 2);
            number.setY(cardHeight * 0.5);
            node.getChildren().addAll(back, number);
            node.setLayoutX(x);
            node.setLayoutY(y);
            paint(false);
            cardsLayer.getChildren().add(node);
            spot = new Spot(node, x + cardWidth / 2, y + cardHeight / 2, cardWidth * 0.92, () -> choose(this));
            spot.card = this;
            listen(spot);
        }

        void paint(final boolean lit) {
            if (done) {
                return;
            }
            back.setFill(lit ? CARD_LIT : CARD);
            back.setStroke(CARD_BORDER);
            number.setFill(NUMBER);
        }

        void markRight() {
            done = true;
            back.setFill(RIGHT);
            back.setStroke(RIGHT.darker());
            number.setFill(Color.WHITE);
            node.getChildren().add(badge(true));
            node.setOpacity(1);
        }

        /**
         * @param again whether the player looks again: the card shakes, then stays greyed
         */
        void markWrong(final boolean again) {
            done = true;
            back.setFill(again ? GREYED : WRONG);
            back.setStroke((again ? GREYED : WRONG).darker());
            number.setFill(Color.WHITE);
            node.getChildren().add(badge(false));
            if (again) {
                greyed = true;
                node.setOpacity(0.55);
            }
            shaking = SHAKE;
        }

        void fade() {
            if (!done) {
                done = true;
                node.setOpacity(0.35);
            }
        }

        void shake(final double seconds) {
            if (shaking <= 0) {
                return;
            }
            shaking = Math.max(0, shaking - seconds);
            final double t = SHAKE - shaking;
            node.setTranslateX(Math.sin(t * 50) * cardWidth * 0.06 * (shaking / SHAKE));
        }

        /**
         * @return a white badge at the top right corner, with a check or a cross
         */
        private Group badge(final boolean check) {
            final double r = cardHeight * 0.13;
            final double cx = cardWidth - r * 0.6;
            final double cy = r * 0.6;
            final Circle disc = new Circle(cx, cy, r, Color.WHITE);
            disc.setStroke(check ? RIGHT.darker() : WRONG.darker());
            disc.setStrokeWidth(Math.max(1, r * 0.12));
            final Polyline mark;
            if (check) {
                mark = new Polyline(cx - r * 0.45, cy, cx - r * 0.12, cy + r * 0.35, cx + r * 0.5, cy - r * 0.35);
                mark.setStroke(RIGHT);
            } else {
                final double a = r * 0.4;
                mark = new Polyline(cx - a, cy - a, cx + a, cy + a, cx, cy, cx + a, cy - a, cx - a, cy + a);
                mark.setStroke(WRONG);
            }
            mark.setStrokeWidth(r * 0.28);
            mark.setStrokeLineCap(StrokeLineCap.ROUND);
            mark.setStrokeLineJoin(StrokeLineJoin.ROUND);
            return new Group(disc, mark);
        }
    }
}
