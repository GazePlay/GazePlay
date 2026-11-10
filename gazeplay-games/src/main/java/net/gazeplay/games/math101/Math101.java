package net.gazeplay.games.math101;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
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
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * "Apprends les Maths" (name code Math101; reviewed on 10/11/2026, maintainer's rules). An operation is written on a
 * blackboard ("7 + 5 = ?"), a Biboule beside it, and three cards under it show numbers: the right answer and two wrong
 * answers close to it. A fixation on a card chooses it. The right card turns green with a check, the answer is written
 * on the board, the Biboule jumps for joy, and a new operation comes. A wrong card shows a cross; depending on the
 * variant, it is greyed and the player looks again (no lives), or one of 3 lives is lost and the right answer is shown
 * before the next operation. The game ends after 5, 10 or 20 operations solved ("Bravo"), or never, or when the lives
 * are lost ("Lost!").
 * <p>
 * Everything is drawn by the game, except the Biboule. One loop, in real seconds: the fixations last the length of the
 * settings (read at each step), nothing can be chosen before the delay of the settings, and nothing moves while the
 * menu of the game is open. (Before the review: 5 games of the menu, numbers from 0, wrong answers drawn up to twice the
 * largest number, no end, no lives, a mistake counted at the start, the former progress indicator, the limiters, the
 * areas of interest of the cards misplaced, a font of the Mac only, 6 pictures of unknown source.)
 */
public class Math101 implements GameLifeCycle {

    static final int LIVES = 3;
    /**
     * In seconds: the right answer found (the Biboule jumps), the right answer shown after a mistake with lives, a
     * wrong card shaking, the end shown.
     */
    static final double FOUND = 1.6;
    static final double REVEAL = 2.2;
    static final double SHAKE = 0.45;
    static final double SHOW = 2.2;

    private static final String SOUNDS = "data/math101/sounds/";
    private static final String BIBOULE = "data/math101/images/biboule.png";
    private static final Color BOARD = Color.rgb(40, 82, 64);
    private static final Color FRAME = Color.rgb(156, 102, 56);
    private static final Color CHALK = Color.rgb(250, 250, 244);
    private static final Color CHALK_ANSWER = Color.rgb(255, 222, 92);
    private static final Color CARD = Color.WHITE;
    private static final Color CARD_LIT = Color.rgb(255, 238, 170);
    private static final Color CARD_BORDER = Color.rgb(196, 204, 220);
    private static final Color NUMBER = Color.rgb(48, 54, 72);
    private static final Color RIGHT = Color.web(GameStyle.SUCCESS_COLOR);
    private static final Color WRONG = Color.rgb(214, 64, 64);
    private static final Color GREYED = Color.rgb(150, 156, 170);

    enum Phase { WAIT, PLAY, FOUND, REVEAL, SHOW, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final MathTopic topic;
    @Getter
    private final MathLevel level;
    @Getter
    private final MathEnd end;
    @Getter
    private final MathMistakes mistakes;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private MathQuestion question;
    @Getter
    private final List<Card> cards = new ArrayList<>();
    @Getter
    private int lives;
    @Getter
    private int solved;
    @Getter
    private boolean bravo;
    private double elapsed;
    private double waiting;

    private double width;
    private double height;
    private double boardX;
    private double boardY;
    private double boardWidth;
    private double boardHeight;
    private double cardWidth;
    private double cardHeight;
    private double bibouleBottom;

    private Group layer;
    private Group cardsLayer;
    private Group formula;
    private ImageView biboule;
    private FixationRing ring;
    private Group hud;
    private Group message;
    private final List<Node> gazeNodes = new ArrayList<>();
    private final ArrayList<TargetAOI> targetAOIList = new ArrayList<>();

    private Card looked;
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
    Math101(final IGameContext gameContext, final Stats stats, final MathTopic topic, final MathLevel level,
            final MathEnd end, final MathMistakes mistakes, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.topic = topic;
        this.level = level;
        this.end = end;
        this.mistakes = mistakes;
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

        final Rectangle background = new Rectangle(0, 0, width, height);
        background.setFill(topic.background());
        background.setMouseTransparent(true);

        // The board and the Biboule beside it, centered together under the band of the lives and the score
        boardHeight = height * 0.38;
        boardWidth = Math.min(width * 0.62, boardHeight * 2.3);
        final double bibouleSize = height * 0.27;
        final double gap = width * 0.025;
        boardX = (width - boardWidth - gap - bibouleSize) / 2;
        boardY = height * 0.13;
        final Group board = board();
        formula = new Group();
        formula.setMouseTransparent(true);
        biboule = new ImageView(new Image(BIBOULE));
        biboule.setPreserveRatio(true);
        biboule.setFitWidth(bibouleSize);
        biboule.setLayoutX(boardX + boardWidth + gap);
        bibouleBottom = boardY + boardHeight;
        biboule.setLayoutY(bibouleBottom - biboule.getBoundsInLocal().getHeight());
        biboule.setMouseTransparent(true);

        cardHeight = Math.min(height * 0.3, width * 0.2);
        cardWidth = cardHeight * 1.05;
        cardsLayer = new Group();
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        layer = new Group(background, board, formula, biboule, cardsLayer, ring, hud, message);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        lives = LIVES;
        solved = 0;
        bravo = false;
        elapsed = 0;
        waiting = configuration.getGameStartDelay() / 1000.0;
        phase = Phase.WAIT;
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * @return the blackboard: a wooden frame, a green board with traces of chalk, and a tray with two chalks
     */
    private Group board() {
        final double frame = boardHeight * 0.06;
        final Rectangle wood = new Rectangle(boardX - frame, boardY - frame, boardWidth + 2 * frame,
            boardHeight + 2 * frame);
        wood.setArcWidth(frame * 1.6);
        wood.setArcHeight(frame * 1.6);
        wood.setFill(FRAME);
        wood.setStroke(FRAME.darker());
        wood.setStrokeWidth(Math.max(1, frame * 0.15));
        final Rectangle slate = new Rectangle(boardX, boardY, boardWidth, boardHeight);
        slate.setFill(BOARD);
        final Group group = new Group(wood, slate);
        // Traces of chalk badly wiped
        final double[][] traces = {{0.18, 0.25, 0.14, 0.05}, {0.72, 0.78, 0.18, 0.06}, {0.5, 0.18, 0.1, 0.04},
            {0.86, 0.32, 0.08, 0.05}, {0.24, 0.8, 0.12, 0.04}};
        for (final double[] trace : traces) {
            final Ellipse smear = new Ellipse(boardX + trace[0] * boardWidth, boardY + trace[1] * boardHeight,
                trace[2] * boardWidth, trace[3] * boardHeight);
            smear.setFill(Color.rgb(255, 255, 255, 0.05));
            group.getChildren().add(smear);
        }
        final Rectangle tray = new Rectangle(boardX + boardWidth * 0.1, boardY + boardHeight + frame * 0.2,
            boardWidth * 0.8, frame * 0.9);
        tray.setArcWidth(frame * 0.6);
        tray.setArcHeight(frame * 0.6);
        tray.setFill(FRAME.darker());
        final Rectangle chalk = new Rectangle(boardX + boardWidth * 0.62, boardY + boardHeight + frame * 0.05,
            boardWidth * 0.07, frame * 0.5);
        chalk.setArcWidth(frame * 0.4);
        chalk.setArcHeight(frame * 0.4);
        chalk.setFill(CHALK);
        final Rectangle yellowChalk = new Rectangle(boardX + boardWidth * 0.72, boardY + boardHeight + frame * 0.05,
            boardWidth * 0.05, frame * 0.5);
        yellowChalk.setArcWidth(frame * 0.4);
        yellowChalk.setArcHeight(frame * 0.4);
        yellowChalk.setFill(CHALK_ANSWER);
        group.getChildren().addAll(tray, chalk, yellowChalk);
        group.setMouseTransparent(true);
        return group;
    }

    /**
     * A new operation, never the same as the previous one, with its three cards in an order drawn at random.
     */
    private void newQuestion() {
        cards.forEach(card -> forget(card.node));
        cards.clear();
        cardsLayer.getChildren().clear();
        looked = null;
        progress = 0;
        ring.stop();
        biboule.setTranslateY(0);

        final MathQuestion previous = question;
        MathQuestion next = MathQuestion.draw(topic, level, random);
        for (int i = 0; i < 20 && previous != null && next.text().equals(previous.text()); i++) {
            next = MathQuestion.draw(topic, level, random);
        }
        question = next;
        writeFormula(false);

        final List<Integer> values = new ArrayList<>(question.wrongAnswers(random));
        values.add(random.nextInt(3), question.getAnswer());
        final double gap = cardWidth * 0.35;
        final double left = (width - 3 * cardWidth - 2 * gap) / 2;
        final double top = height - cardHeight - height * 0.06;
        for (int i = 0; i < values.size(); i++) {
            final Card card = new Card(values.get(i), values.get(i) == question.getAnswer(),
                left + i * (cardWidth + gap), top);
            cards.add(card);
        }
        final Card right = cards.stream().filter(Card::isRight).findFirst().orElseThrow();
        targetAOIList.add(new TargetAOI(right.x + cardWidth / 2, right.y + cardHeight / 2,
            (int) ((cardWidth + cardHeight) / 4), System.currentTimeMillis()));

        elapsed = 0;
        phase = Phase.PLAY;
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        showHud();
    }

    /**
     * Writes the operation on the board, with a "?" in yellow chalk, or with its answer once found or shown.
     */
    private void writeFormula(final boolean answered) {
        double size = boardHeight * 0.4;
        Text left = chalk(question.text(), size, CHALK);
        Text answer = chalk(answered ? Integer.toString(question.getAnswer()) : "?", size, CHALK_ANSWER);
        final double space = size * 0.3;
        double total = left.getLayoutBounds().getWidth() + space + answer.getLayoutBounds().getWidth();
        if (total > boardWidth * 0.88) {
            size = size * boardWidth * 0.88 / total;
            left = chalk(question.text(), size, CHALK);
            answer = chalk(answered ? Integer.toString(question.getAnswer()) : "?", size, CHALK_ANSWER);
            total = left.getLayoutBounds().getWidth() + size * 0.3 + answer.getLayoutBounds().getWidth();
        }
        final double x = boardX + (boardWidth - total) / 2;
        final double y = boardY + boardHeight * 0.5;
        left.setX(x);
        left.setY(y);
        answer.setX(x + left.getLayoutBounds().getWidth() + size * 0.3);
        answer.setY(y);
        formula.getChildren().setAll(left, answer);
    }

    private static Text chalk(final String content, final double size, final Color color) {
        final Text text = new Text(content);
        text.setFont(GameStyle.titleFont(size));
        text.setFill(color);
        text.setTextOrigin(VPos.CENTER);
        return text;
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
                if (elapsed >= waiting) {
                    newQuestion();
                }
            }
            case PLAY -> fixate(seconds);
            case FOUND -> {
                elapsed += seconds;
                // The Biboule jumps for joy
                biboule.setTranslateY(-Math.abs(Math.sin(elapsed * Math.PI * 2.5)) * height * 0.035
                    * Math.max(0, 1 - elapsed / FOUND));
                if (elapsed >= FOUND) {
                    biboule.setTranslateY(0);
                    if (end.getOperations() > 0 && solved >= end.getOperations()) {
                        bravo = true;
                        finish();
                    } else {
                        newQuestion();
                    }
                }
            }
            case REVEAL -> {
                elapsed += seconds;
                if (elapsed >= REVEAL) {
                    if (lives <= 0) {
                        endGame("MathLost");
                    } else {
                        newQuestion();
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

    private void fixate(final double seconds) {
        if (looked == null || looked.done) {
            return;
        }
        if (!ring.isVisible()) {
            showRing(looked);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Card card = looked;
            progress = 0;
            ring.stop();
            choose(card);
        }
    }

    private void showRing(final Card card) {
        ring.setSize(cardWidth * 0.92);
        ring.show(card.x + cardWidth / 2, card.y + cardHeight / 2);
    }

    void lookAt(final Card card) {
        if (looked == card) {
            return;
        }
        looked = card;
        progress = 0;
        ring.stop();
        paintCards();
        if (phase == Phase.PLAY && !card.done) {
            showRing(card);
        }
    }

    void lookAway(final Card card) {
        if (looked == card) {
            looked = null;
            progress = 0;
            ring.stop();
            paintCards();
        }
    }

    private void paintCards() {
        cards.forEach(card -> card.paint(card == looked && phase == Phase.PLAY && !card.done));
    }

    /**
     * A card chosen: the right one ends the operation; a wrong one is greyed (looking again) or costs a life (the right
     * answer is then shown).
     */
    void choose(final Card card) {
        if (phase != Phase.PLAY || card.done) {
            return;
        }
        looked = null;
        if (card.right) {
            card.markRight();
            cards.stream().filter(other -> other != card).forEach(Card::fade);
            writeFormula(true);
            gameContext.getSoundManager().add(SOUNDS + "right.wav");
            solved++;
            stats.incrementNumberOfGoalsReached();
            endRound();
            phase = Phase.FOUND;
            elapsed = 0;
            showHud();
            return;
        }
        stats.incrementNumberOfMistakes();
        gameContext.getSoundManager().add(SOUNDS + "wrong.wav");
        if (mistakes == MathMistakes.SEARCH_AGAIN) {
            card.markWrong(true);
            paintCards();
            return;
        }
        card.markWrong(false);
        cards.stream().filter(Card::isRight).forEach(Card::markRight);
        writeFormula(true);
        lives--;
        endRound();
        phase = Phase.REVEAL;
        elapsed = 0;
        showHud();
    }

    private void endRound() {
        final TargetAOI last = targetAOIList.get(targetAOIList.size() - 1);
        if (last.getTimeEnded() == 0) {
            last.setTimeEnded(System.currentTimeMillis());
        }
    }

    private void endGame(final String key) {
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate(key), height * 0.05);
        // On the board, under the operation and its answer: the cards stay seen
        pill.setLayoutX(boardX + (boardWidth - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(boardY + boardHeight * 0.97 - pill.getBoundsInLocal().getHeight());
        message.getChildren().setAll(pill);
        phase = Phase.SHOW;
        elapsed = 0;
        showHud();
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

    private void showHud() {
        hud.getChildren().clear();
        final double margin = height * 0.025;
        if (mistakes == MathMistakes.LIVES) {
            final Group hearts = GameStyle.lives(lives, LIVES, height * 0.055, width * 0.3, false);
            hearts.setLayoutX(margin);
            hearts.setLayoutY(margin * 0.6);
            hud.getChildren().add(hearts);
        }
        final Group score = GameStyle.pill(gameContext.getTranslator().translate("MathSolved") + " " + solved
            + (end.getOperations() > 0 ? " / " + end.getOperations() : ""), height * 0.035);
        score.setLayoutX((width - score.getBoundsInLocal().getWidth()) / 2);
        score.setLayoutY(margin * 0.6);
        hud.getChildren().add(score);
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
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
        if (!targetAOIList.isEmpty()) {
            endRound();
            stats.setTargetAOIList(targetAOIList);
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A card: a rounded white card with a number; lit while looked at, green with a check when right, red with a cross
     * when wrong (then greyed when looking again), faded when the operation is over.
     */
    final class Card {
        @Getter
        private final int value;
        @Getter
        private final boolean right;
        private final double x;
        private final double y;
        private final Group node = new Group();
        private final Rectangle back;
        private final Text number;
        /**
         * Chosen, or greyed: it can no longer be chosen.
         */
        @Getter
        private boolean done;
        @Getter
        private boolean greyed;
        private double shaking;

        Card(final int value, final boolean right, final double x, final double y) {
            this.value = value;
            this.right = right;
            this.x = x;
            this.y = y;
            back = new Rectangle(cardWidth, cardHeight);
            back.setArcWidth(cardWidth * 0.18);
            back.setArcHeight(cardWidth * 0.18);
            back.setStrokeWidth(Math.max(2, cardWidth * 0.025));
            number = new Text(Integer.toString(value));
            double size = cardHeight * 0.5;
            number.setFont(GameStyle.titleFont(size));
            if (number.getLayoutBounds().getWidth() > cardWidth * 0.82) {
                size = size * cardWidth * 0.82 / number.getLayoutBounds().getWidth();
                number.setFont(GameStyle.titleFont(size));
            }
            number.setTextOrigin(VPos.CENTER);
            number.setX((cardWidth - number.getLayoutBounds().getWidth()) / 2);
            number.setY(cardHeight * 0.5);
            node.getChildren().addAll(back, number);
            node.setLayoutX(x);
            node.setLayoutY(y);
            paint(false);
            cardsLayer.getChildren().add(node);
            listen(node, () -> lookAt(this), () -> lookAway(this));
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

        double getX() {
            return x;
        }

        double getY() {
            return y;
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
