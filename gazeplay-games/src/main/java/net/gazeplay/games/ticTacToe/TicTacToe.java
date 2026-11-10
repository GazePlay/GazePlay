package net.gazeplay.games.ticTacToe;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
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
 * The Morpion (Tic-tac-toe, name code TicTacToe; reviewed on 10/11/2026, maintainer's rules): two players put their
 * marks in a grid of 3 x 3; the first one with 3 marks in a line, a column or a diagonal wins, a full grid is a draw.
 * The player plays the red crosses against the computer (3 levels, see {@link TicTacToeComputer}) or against a second
 * player on the same screen (blue noughts). A fixation on a box puts the mark there: the box lights up and the mark
 * waits in it, with the ring. The first player changes at each game; the match ends after 1, 3 or 5 games ("Bravo" if
 * the player wins it, or at once the statistics if not), or never.
 * <p>
 * Everything is drawn by the game. One loop, in real seconds: the fixations last the length of the settings (read at
 * each step), nothing can be chosen before the delay of the settings, and nothing moves while the menu of the game is
 * open. (Before the review: a computer playing at random, the game frozen at a draw against it, its wins not shown,
 * "Bravo" at the first win of the player, no goal counted, 3 pictures of unknown source, the code of a box written 9
 * times.)
 */
public class TicTacToe implements GameLifeCycle {

    static final int CROSS = 1;
    static final int NOUGHT = 2;
    /**
     * In seconds: the computer thinking (its mark goes from box to box), a mark drawn, the end of a game shown.
     */
    static final double THINK = 1.0;
    static final double DRAW = 0.35;
    static final double SHOW = 2.5;

    private static final String SOUNDS = "data/noughtsandcrosses/sounds/";
    private static final Color BACKGROUND = Color.rgb(232, 236, 244);
    private static final Color RED = Color.rgb(222, 62, 62);
    private static final Color BLUE = Color.rgb(52, 112, 222);

    enum Phase { WAIT, PLAY, THINK, DRAW, SHOW, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final TicTacToeOpponent opponent;
    @Getter
    private final TicTacToeEnd end;
    private final ReplayablePseudoRandom random;
    private final TicTacToeComputer computer;
    @Getter
    private final TicTacToeBoard board = new TicTacToeBoard();

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int current;
    @Getter
    private int gamesPlayed;
    private final int[] scores = new int[3];
    @Getter
    private int lastWinner = -1;
    private double elapsed;
    @Getter
    private double waiting;

    private int chosen;
    private final int[] sweep = new int[2];
    private int drawing;
    private Group drawingMark;

    private double width;
    private double height;
    @Getter
    private double cell;
    private double gridX;
    private double gridY;
    private double messageY;

    private Group layer;
    private final Rectangle[] boxes = new Rectangle[9];
    private Group marksLayer;
    private Group lineLayer;
    private Group ghost;
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
    TicTacToe(final IGameContext gameContext, final Stats stats, final TicTacToeOpponent opponent,
              final TicTacToeEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.opponent = opponent;
        this.end = end;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
        computer = new TicTacToeComputer(opponent, random);
    }

    @Override
    public void launch() {
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();
        layOut();

        final Rectangle background = new Rectangle(0, 0, width, height);
        background.setFill(BACKGROUND);
        background.setMouseTransparent(true);
        final Group grid = new Group();
        final double inset = cell * 0.05;
        for (int box = 0; box < 9; box++) {
            final int column = box % 3;
            final int row = box / 3;
            final Rectangle square = new Rectangle(gridX + column * cell + inset, gridY + row * cell + inset,
                cell - 2 * inset, cell - 2 * inset);
            square.setArcWidth(cell * 0.18);
            square.setArcHeight(cell * 0.18);
            square.setStroke(Color.rgb(196, 204, 220));
            square.setStrokeWidth(Math.max(2, cell * 0.02));
            boxes[box] = square;
            grid.getChildren().add(square);
            final int index = box;
            listen(square, () -> lookAt(index), () -> lookAway(index));
        }
        paintBoxes();
        marksLayer = new Group();
        marksLayer.setMouseTransparent(true);
        lineLayer = new Group();
        lineLayer.setMouseTransparent(true);
        ghost = new Group();
        ghost.setMouseTransparent(true);
        ghost.setOpacity(0.35);
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        // The line of a win under the marks: a wide yellow band, seen even along the strokes of the crosses
        layer = new Group(background, grid, lineLayer, ghost, marksLayer, ring, hud, message);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        gamesPlayed = 0;
        scores[CROSS] = 0;
        scores[NOUGHT] = 0;
        elapsed = 0;
        waiting = configuration.getGameStartDelay() / 1000.0;
        newGame();
        phase = Phase.WAIT;
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * Places the grid in the middle of the screen, under the band of the score and the row of the messages.
     */
    private void layOut() {
        final double top = height * 0.11;
        final double row = height * 0.09;
        final double margin = Math.min(width, height) * 0.04;
        final double side = Math.min(height - top - row - margin, width - 2 * margin);
        cell = side / 3;
        gridX = (width - side) / 2;
        gridY = top + row;
        messageY = top + row / 2;
    }

    private double boxX(final int box) {
        final int column = box % 3;
        return gridX + (column + 0.5) * cell;
    }

    private double boxY(final int box) {
        // The line of the box: an integer division
        final int row = box / 3;
        return gridY + (row + 0.5) * cell;
    }

    private void paintBoxes() {
        for (int box = 0; box < 9; box++) {
            final boolean lit = box == looked && phase == Phase.PLAY && board.canPlay(box);
            boxes[box].setFill(lit ? Color.rgb(255, 238, 170) : Color.WHITE);
        }
    }

    /**
     * @return a mark of a player, centered on (0, 0), drawn to a part (0 to 1): a red cross or a blue nought
     */
    Group mark(final int player, final double drawn) {
        final double half = cell * 0.3;
        final double width = cell * 0.11;
        final Group mark = new Group();
        if (player == CROSS) {
            final double first = Math.min(1, drawn * 2);
            final double second = Math.max(0, drawn * 2 - 1);
            final Line a = new Line(-half, -half, -half + 2 * half * first, -half + 2 * half * first);
            final Line b = new Line(half, -half, half - 2 * half * second, -half + 2 * half * second);
            for (final Line line : new Line[]{a, b}) {
                line.setStroke(RED);
                line.setStrokeWidth(width);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
            }
            mark.getChildren().add(a);
            if (second > 0) {
                mark.getChildren().add(b);
            }
        } else {
            final Arc nought = new Arc(0, 0, half, half, 90, 360 * drawn);
            nought.setType(ArcType.OPEN);
            nought.setFill(null);
            nought.setStroke(BLUE);
            nought.setStrokeWidth(width);
            nought.setStrokeLineCap(StrokeLineCap.ROUND);
            mark.getChildren().add(nought);
        }
        mark.setMouseTransparent(true);
        return mark;
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
     * An empty grid; the first player changes at each game (the crosses for the first).
     */
    private void newGame() {
        board.clear();
        marksLayer.getChildren().clear();
        lineLayer.getChildren().clear();
        message.getChildren().clear();
        drawingMark = null;
        current = gamesPlayed % 2 == 0 ? CROSS : NOUGHT;
        progress = 0;
        ring.stop();
    }

    private void startTurn() {
        elapsed = 0;
        progress = 0;
        ring.stop();
        if (opponent.isComputer() && current == NOUGHT) {
            phase = Phase.THINK;
            chosen = computer.choose(board, NOUGHT, CROSS);
            final List<Integer> free = board.free();
            sweep[0] = free.get(random.nextInt(free.size()));
            sweep[1] = free.get(random.nextInt(free.size()));
            showGhost(sweep[0]);
        } else {
            phase = Phase.PLAY;
            showLooked();
        }
        paintBoxes();
        showHud();
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
                if (elapsed >= waiting) {
                    stats.notifyNewRoundReady();
                    startTurn();
                }
            }
            case PLAY -> fixate(seconds);
            case THINK -> {
                elapsed += seconds;
                showGhost(elapsed < THINK / 3 ? sweep[0] : elapsed < THINK * 2 / 3 ? sweep[1] : chosen);
                if (elapsed >= THINK) {
                    play(chosen);
                }
            }
            case DRAW -> {
                elapsed += seconds;
                final Group shown = mark(current, Math.min(1, elapsed / DRAW));
                shown.setLayoutX(boxX(drawing));
                shown.setLayoutY(boxY(drawing));
                marksLayer.getChildren().set(marksLayer.getChildren().indexOf(drawingMark), shown);
                drawingMark = shown;
                if (elapsed >= DRAW) {
                    drawn();
                }
            }
            case SHOW -> {
                elapsed += seconds;
                lineLayer.setOpacity(0.7 + 0.3 * Math.cos(elapsed * 7));
                if (elapsed >= SHOW) {
                    if (end.getGames() > 0 && gamesPlayed >= end.getGames()) {
                        finish();
                    } else {
                        newGame();
                        stats.notifyNewRoundReady();
                        startTurn();
                    }
                }
            }
            default -> {
            }
        }
    }

    private void fixate(final double seconds) {
        if (looked < 0 || !board.canPlay(looked)) {
            return;
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final int box = looked;
            progress = 0;
            ring.stop();
            play(box);
        }
    }

    /**
     * The gaze (or the mouse) enters a box.
     */
    void lookAt(final int box) {
        if (looked == box) {
            return;
        }
        looked = box;
        progress = 0;
        ring.stop();
        if (phase == Phase.PLAY) {
            showLooked();
        }
    }

    void lookAway(final int box) {
        if (looked == box) {
            looked = -1;
            progress = 0;
            ring.stop();
            if (phase == Phase.PLAY) {
                showLooked();
            }
        }
    }

    /**
     * Lights the box looked at and shows the mark of the player in it, with the ring; nothing for a taken box.
     */
    private void showLooked() {
        if (looked >= 0 && board.canPlay(looked)) {
            showGhost(looked);
            ring.setSize(cell * 0.86);
            ring.show(boxX(looked), boxY(looked));
            ring.setProgress(progress);
        } else {
            ghost.setVisible(false);
            ring.stop();
        }
        paintBoxes();
    }

    private void showGhost(final int box) {
        ghost.getChildren().setAll(mark(current, 1));
        ghost.setLayoutX(boxX(box));
        ghost.setLayoutY(boxY(box));
        ghost.setVisible(true);
    }

    /**
     * Puts a mark of the player to play in a box: it is drawn, then the game goes on.
     */
    void play(final int box) {
        if ((phase != Phase.PLAY && phase != Phase.THINK) || !board.canPlay(box)) {
            return;
        }
        board.play(box, current);
        drawing = box;
        drawingMark = mark(current, 0);
        marksLayer.getChildren().add(drawingMark);
        ghost.setVisible(false);
        ring.stop();
        progress = 0;
        phase = Phase.DRAW;
        elapsed = 0;
        gameContext.getSoundManager().add(SOUNDS + "place.wav");
        paintBoxes();
        showHud();
    }

    private void drawn() {
        final int winner = board.winner();
        if (winner != TicTacToeBoard.EMPTY) {
            endGame(winner);
        } else if (board.isFull()) {
            endGame(TicTacToeBoard.EMPTY);
        } else {
            current = current == CROSS ? NOUGHT : CROSS;
            startTurn();
        }
    }

    /**
     * The end of a game: a line through the 3 marks, a message tells who won.
     */
    private void endGame(final int winner) {
        gamesPlayed++;
        lastWinner = winner;
        if (winner != TicTacToeBoard.EMPTY) {
            scores[winner]++;
        }
        stats.incrementNumberOfGoalsToReach();
        if (!opponent.isComputer() || winner == CROSS) {
            stats.incrementNumberOfGoalsReached();
        }
        final int[] line = board.winningLine();
        if (line != null) {
            final double dx = boxX(line[2]) - boxX(line[0]);
            final double dy = boxY(line[2]) - boxY(line[0]);
            final double length = Math.hypot(dx, dy);
            final double over = cell * 0.4 / length;
            final Line bar = new Line(boxX(line[0]) - dx * over, boxY(line[0]) - dy * over,
                boxX(line[2]) + dx * over, boxY(line[2]) + dy * over);
            bar.setStroke(Color.rgb(255, 200, 60));
            bar.setStrokeWidth(cell * 0.2);
            bar.setStrokeLineCap(StrokeLineCap.ROUND);
            lineLayer.getChildren().setAll(bar);
        }
        final String text;
        if (winner == TicTacToeBoard.EMPTY) {
            text = "TicTacToeDraw";
        } else if (opponent.isComputer()) {
            text = winner == CROSS ? "TicTacToeYouWin" : "TicTacToeComputerWins";
        } else {
            text = winner == CROSS ? "TicTacToeCrossWins" : "TicTacToeNoughtWins";
        }
        if (winner != TicTacToeBoard.EMPTY) {
            final boolean happy = !opponent.isComputer() || winner == CROSS;
            gameContext.getSoundManager().add(SOUNDS + (happy ? "win.wav" : "lose.wav"));
        }
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate(text), height * 0.045);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(messageY - pill.getBoundsInLocal().getHeight() / 2);
        message.getChildren().setAll(pill);
        phase = Phase.SHOW;
        elapsed = 0;
        paintBoxes();
        showHud();
    }

    private void finish() {
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        if (isWonByThePlayer()) {
            gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
        } else {
            gameContext.showRoundStats(stats, this);
        }
    }

    /**
     * @return whether the match is won by the player: more games won than the computer, or always with two players
     */
    boolean isWonByThePlayer() {
        return !opponent.isComputer() || scores[CROSS] > scores[NOUGHT];
    }

    int score(final int player) {
        return scores[player];
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final List<Node> parts = new ArrayList<>();
        if (phase == Phase.PLAY || phase == Phase.THINK || phase == Phase.DRAW) {
            final String turn = opponent.isComputer()
                ? (current == CROSS ? "TicTacToeYourTurn" : "TicTacToeComputerTurn") : "TicTacToeTurn";
            final Group pill = GameStyle.pill(gameContext.getTranslator().translate(turn), fontSize);
            final double size = pill.getBoundsInLocal().getHeight();
            final Group icon = mark(current, 1);
            icon.setScaleX(size * 0.8 / (cell * 0.7));
            icon.setScaleY(size * 0.8 / (cell * 0.7));
            icon.setLayoutX(pill.getBoundsInLocal().getWidth() + size * 0.6);
            icon.setLayoutY(size / 2);
            parts.add(new Group(pill, icon));
        }
        final String left = gameContext.getTranslator().translate(opponent.isComputer() ? "TicTacToeYou"
            : "TicTacToeCrosses");
        final String right = gameContext.getTranslator().translate(opponent.isComputer() ? "TicTacToeComputer"
            : "TicTacToeNoughts");
        parts.add(GameStyle.pill(left + " " + scores[CROSS] + " – " + scores[NOUGHT] + " " + right, fontSize));
        final double gap = fontSize * 1.2;
        double total = -gap;
        for (final Node part : parts) {
            total += part.getBoundsInLocal().getWidth() + gap;
        }
        double x = (width - total) / 2;
        for (final Node part : parts) {
            part.setLayoutX(x - part.getBoundsInLocal().getMinX());
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

    int getChosen() {
        return chosen;
    }

    boolean isGhostShown() {
        return ghost.isVisible();
    }

    boolean isLit(final int box) {
        return Color.rgb(255, 238, 170).equals(boxes[box].getFill());
    }

    Rectangle boxOf(final int box) {
        return boxes[box];
    }

    boolean hasLine() {
        return !lineLayer.getChildren().isEmpty();
    }

    /**
     * @return the grid on the screen (left, top, right, bottom), then the height of the middle of the messages
     */
    double[] layout() {
        return new double[]{gridX, gridY, gridX + 3 * cell, gridY + 3 * cell, messageY};
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
