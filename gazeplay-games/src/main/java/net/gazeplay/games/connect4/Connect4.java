package net.gazeplay.games.connect4;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
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
 * "Quatre en ligne" (Four in a Row, name code Connect4, formerly "Puissance 4", the name of a commercial game; reviewed
 * on 10/11/2026, maintainer's rules): two players drop tokens in a grid of 7 columns and 6 rows; the first one to put
 * 4 tokens in a line, a column or a diagonal wins, a full grid is a draw. The player plays the red tokens against the
 * computer (3 levels, see {@link Connect4Computer}) or against a second player on the same screen (yellow). A fixation
 * on a column drops a token there: the column lights up and the token waits above it, with the ring. The first player
 * changes at each game; the match ends after 1, 3 or 5 games ("Bravo" if the player wins it, or at once the statistics
 * if not), or never.
 * <p>
 * Everything is drawn by the game. One loop, in real seconds: the fixations last the length of the settings (read at
 * each step), nothing can be chosen before the delay of the settings, and nothing moves while the menu of the game is
 * open. (Before the review: a computer playing at random, an error when the grid was full, "Bravo" when the computer
 * won, no end, no score, the former progress indicator, only the band above the grid looked at.)
 */
public class Connect4 implements GameLifeCycle {

    static final int RED = 1;
    static final int YELLOW = 2;
    /**
     * In seconds: the computer thinking (its token goes from column to column), the end of a game shown.
     */
    static final double THINK = 1.0;
    static final double SHOW = 2.5;
    /**
     * In boxes per second per second: the token falling.
     */
    private static final double GRAVITY = 60;

    private static final String SOUNDS = "data/connect4/sounds/";
    private static final Color BACKGROUND = Color.rgb(232, 236, 244);
    private static final Color BOARD = Color.rgb(70, 118, 208);
    private static final Color BOARD_DARK = Color.rgb(40, 78, 160);

    enum Phase { WAIT, PLAY, THINK, FALL, SHOW, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final Connect4Opponent opponent;
    @Getter
    private final Connect4End end;
    private final ReplayablePseudoRandom random;
    private final Connect4Computer computer;
    @Getter
    private final Connect4Board board = new Connect4Board();

    @Getter
    private Phase phase = Phase.OVER;
    /**
     * The player to play.
     */
    @Getter
    private int current;
    @Getter
    private int gamesPlayed;
    private final int[] scores = new int[3];
    private double elapsed;
    @Getter
    private double waiting;

    // The computer thinking: its column, and the columns its token passes over
    private int chosen;
    private final int[] sweep = new int[2];

    // The token falling
    private int fallingColumn;
    private int fallingRow;
    private double fallingY;
    private double fallingSpeed;
    private boolean bounced;
    private Group fallingToken;

    private double width;
    private double height;
    @Getter
    private double cell;
    private double boardX;
    private double boardY;

    private Group layer;
    private Group lightLayer;
    private Group tokensLayer;
    private Group marksLayer;
    private Group hud;
    private Group message;
    private Rectangle light;
    private Group ghost;
    private FixationRing ring;
    private final List<Rectangle> zones = new ArrayList<>();
    private final List<Node> gazeNodes = new ArrayList<>();

    /**
     * The column looked at, -1 if none, and how far its fixation is.
     */
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
    Connect4(final IGameContext gameContext, final Stats stats, final Connect4Opponent opponent, final Connect4End end,
             final double gameSeed) {
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
        computer = new Connect4Computer(opponent, random);
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
        lightLayer = new Group();
        lightLayer.setMouseTransparent(true);
        light = new Rectangle(cell, cell * Connect4Board.ROWS);
        // Pale yellow, seen in the empty holes of the column
        light.setFill(Color.rgb(255, 232, 150));
        light.setVisible(false);
        lightLayer.getChildren().add(light);
        tokensLayer = new Group();
        tokensLayer.setMouseTransparent(true);
        final Node grid = grid();
        marksLayer = new Group();
        marksLayer.setMouseTransparent(true);
        ghost = new Group();
        ghost.setMouseTransparent(true);
        ghost.setOpacity(0.75);
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        final Group zonesLayer = new Group();
        for (int column = 0; column < Connect4Board.COLUMNS; column++) {
            final Rectangle zone = new Rectangle(boardX + column * cell, boardY - cell * 1.1, cell,
                cell * (Connect4Board.ROWS + 1.1));
            zone.setFill(Color.TRANSPARENT);
            zones.add(zone);
            zonesLayer.getChildren().add(zone);
            final int index = column;
            listen(zone, () -> lookAt(index), () -> lookAway(index));
        }
        layer = new Group(background, lightLayer, tokensLayer, grid, marksLayer, ghost, zonesLayer, ring, hud, message);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        gamesPlayed = 0;
        scores[RED] = 0;
        scores[YELLOW] = 0;
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
     * Places the grid in the middle of the screen, under the band of the score, with a row above it for the token
     * waiting.
     */
    private void layOut() {
        final double top = height * 0.11;
        final double margin = Math.min(width, height) * 0.04;
        final double availableHeight = height - top - margin;
        cell = Math.min(availableHeight / (Connect4Board.ROWS + 1.3), (width - 2 * margin) / (Connect4Board.COLUMNS
            + 0.4));
        boardX = (width - cell * Connect4Board.COLUMNS) / 2;
        boardY = top + (availableHeight - cell * (Connect4Board.ROWS + 1.3)) / 2 + cell * 1.1;
    }

    private double columnX(final int column) {
        return boardX + (column + 0.5) * cell;
    }

    /**
     * @return the middle of a box on the screen, the rows counted from the bottom
     */
    private double rowY(final double row) {
        return boardY + (Connect4Board.ROWS - 0.5 - row) * cell;
    }

    private double ghostY() {
        return boardY - cell * 0.6;
    }

    /**
     * The grid seen from the front: a blue board with round holes, the tokens seen through them.
     */
    private Node grid() {
        final double frame = cell * 0.15;
        final Rectangle plate = new Rectangle(boardX - frame, boardY - frame * 0.6, cell * Connect4Board.COLUMNS
            + 2 * frame, cell * Connect4Board.ROWS + frame * 1.6);
        plate.setArcWidth(cell * 0.4);
        plate.setArcHeight(cell * 0.4);
        Shape board = plate;
        for (int column = 0; column < Connect4Board.COLUMNS; column++) {
            for (int row = 0; row < Connect4Board.ROWS; row++) {
                board = Shape.subtract(board, new Circle(columnX(column), rowY(row), cell * 0.39));
            }
        }
        board.setFill(BOARD);
        board.setStroke(BOARD_DARK);
        board.setStrokeWidth(Math.max(2, cell * 0.03));
        final InnerShadow shadow = new InnerShadow(cell * 0.12, Color.rgb(20, 40, 90, 0.6));
        board.setEffect(shadow);
        // The feet
        final Group grid = new Group();
        for (final double x : new double[]{boardX - frame, boardX + cell * Connect4Board.COLUMNS + frame - cell * 0.3}) {
            final Rectangle foot = new Rectangle(x, boardY + cell * Connect4Board.ROWS, cell * 0.3, cell * 0.25);
            foot.setArcWidth(cell * 0.12);
            foot.setArcHeight(cell * 0.12);
            foot.setFill(BOARD_DARK);
            grid.getChildren().add(foot);
        }
        grid.getChildren().add(board);
        grid.setMouseTransparent(true);
        return grid;
    }

    /**
     * @return a token of a player, centered on (0, 0): red with a white star, yellow with a white ring
     */
    Group token(final int player) {
        final Color color = player == RED ? Color.rgb(226, 62, 62) : Color.rgb(248, 198, 40);
        final Color dark = color.deriveColor(0, 1, 0.7, 1);
        final double radius = cell * 0.4;
        final Circle disc = new Circle(radius, color);
        disc.setStroke(dark);
        disc.setStrokeWidth(Math.max(1.5, cell * 0.03));
        final Circle relief = new Circle(radius * 0.74, null);
        relief.setStroke(dark.deriveColor(0, 1, 1, 0.55));
        relief.setStrokeWidth(Math.max(1.5, cell * 0.035));
        final Node symbol;
        if (player == RED) {
            final Polygon star = new Polygon();
            for (int point = 0; point < 10; point++) {
                final double angle = -Math.PI / 2 + point * Math.PI / 5;
                final double distance = point % 2 == 0 ? radius * 0.48 : radius * 0.2;
                star.getPoints().addAll(distance * Math.cos(angle), distance * Math.sin(angle) + radius * 0.03);
            }
            star.setFill(Color.WHITE);
            symbol = star;
        } else {
            final Circle round = new Circle(radius * 0.3, null);
            round.setStroke(Color.WHITE);
            round.setStrokeWidth(radius * 0.14);
            symbol = round;
        }
        final Group token = new Group(disc, relief, symbol);
        token.setMouseTransparent(true);
        return token;
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
     * An empty grid; the first player changes at each game (the red one for the first).
     */
    private void newGame() {
        board.clear();
        tokensLayer.getChildren().clear();
        marksLayer.getChildren().clear();
        message.getChildren().clear();
        fallingToken = null;
        current = gamesPlayed % 2 == 0 ? RED : YELLOW;
        progress = 0;
        ring.stop();
    }

    /**
     * The turn of the player to play: the computer thinks, or a column can be chosen.
     */
    private void startTurn() {
        elapsed = 0;
        progress = 0;
        ring.stop();
        if (opponent.isComputer() && current == YELLOW) {
            phase = Phase.THINK;
            chosen = computer.choose(board, YELLOW, RED);
            sweep[0] = random.nextInt(Connect4Board.COLUMNS);
            sweep[1] = random.nextInt(Connect4Board.COLUMNS);
            showGhost(sweep[0]);
        } else {
            phase = Phase.PLAY;
            showLooked();
        }
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
                final int column = elapsed < THINK / 3 ? sweep[0] : elapsed < THINK * 2 / 3 ? sweep[1] : chosen;
                showGhost(column);
                if (elapsed >= THINK) {
                    play(chosen);
                }
            }
            case FALL -> fall(seconds);
            case SHOW -> {
                elapsed += seconds;
                final double pulse = 0.6 + 0.4 * Math.cos(elapsed * 7);
                marksLayer.setOpacity(pulse);
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

    /**
     * Counts the fixation on the column looked at, with the length of the settings read at each step.
     */
    private void fixate(final double seconds) {
        if (looked < 0 || !board.canPlay(looked)) {
            return;
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final int column = looked;
            progress = 0;
            ring.stop();
            play(column);
        }
    }

    /**
     * The gaze (or the mouse) enters a column.
     */
    void lookAt(final int column) {
        if (looked == column) {
            return;
        }
        looked = column;
        progress = 0;
        ring.stop();
        if (phase == Phase.PLAY) {
            showLooked();
        }
    }

    void lookAway(final int column) {
        if (looked == column) {
            looked = -1;
            progress = 0;
            ring.stop();
            if (phase == Phase.PLAY) {
                showLooked();
            }
        }
    }

    /**
     * Lights the column looked at and shows the token of the player above it, with the ring; nothing for a full
     * column.
     */
    private void showLooked() {
        if (looked >= 0 && board.canPlay(looked)) {
            showGhost(looked);
            light.setX(boardX + looked * cell);
            light.setY(boardY);
            light.setVisible(true);
            ring.setSize(cell * 0.98);
            ring.show(columnX(looked), ghostY());
            ring.setProgress(progress);
        } else {
            ghost.setVisible(false);
            light.setVisible(false);
            ring.stop();
        }
    }

    private void showGhost(final int column) {
        ghost.getChildren().setAll(token(current));
        ghost.setLayoutX(columnX(column));
        ghost.setLayoutY(ghostY());
        ghost.setVisible(true);
    }

    /**
     * Drops a token of the player to play in a column: it falls from above the grid.
     */
    void play(final int column) {
        if ((phase != Phase.PLAY && phase != Phase.THINK) || !board.canPlay(column)) {
            return;
        }
        fallingColumn = column;
        fallingRow = board.drop(column, current);
        fallingToken = token(current);
        fallingToken.setLayoutX(columnX(column));
        fallingY = Connect4Board.ROWS + 0.6;
        fallingSpeed = 0;
        bounced = false;
        placeFalling();
        tokensLayer.getChildren().add(fallingToken);
        ghost.setVisible(false);
        light.setVisible(false);
        ring.stop();
        progress = 0;
        phase = Phase.FALL;
        showHud();
    }

    private void placeFalling() {
        fallingToken.setLayoutY(rowY(fallingY));
    }

    /**
     * The token falls, bounces a little once, then the game goes on.
     */
    private void fall(final double seconds) {
        fallingSpeed += GRAVITY * seconds;
        fallingY -= fallingSpeed * seconds;
        if (fallingY <= fallingRow) {
            if (!bounced && fallingSpeed > 4) {
                gameContext.getSoundManager().add(SOUNDS + "drop.wav");
                bounced = true;
                fallingY = fallingRow;
                fallingSpeed = -fallingSpeed * 0.22;
            } else {
                fallingY = fallingRow;
                placeFalling();
                if (!bounced) {
                    gameContext.getSoundManager().add(SOUNDS + "drop.wav");
                }
                landed();
                return;
            }
        }
        placeFalling();
    }

    private void landed() {
        if (board.lineThrough(fallingColumn, fallingRow, current)) {
            endGame(current);
        } else if (board.isFull()) {
            endGame(Connect4Board.EMPTY);
        } else {
            current = current == RED ? YELLOW : RED;
            startTurn();
        }
    }

    /**
     * The end of a game: the 4 tokens in a line are circled, a message tells who won.
     */
    private void endGame(final int winner) {
        gamesPlayed++;
        if (winner != Connect4Board.EMPTY) {
            scores[winner]++;
        }
        stats.incrementNumberOfGoalsToReach();
        if (!opponent.isComputer() || winner == RED) {
            stats.incrementNumberOfGoalsReached();
        }
        final int[][] line = board.winningLine();
        if (line != null) {
            for (final int[] box : line) {
                final Circle mark = new Circle(columnX(box[0]), rowY(box[1]), cell * 0.43, null);
                mark.setStroke(Color.WHITE);
                mark.setStrokeWidth(Math.max(3, cell * 0.07));
                marksLayer.getChildren().add(mark);
            }
        }
        final String text;
        if (winner == Connect4Board.EMPTY) {
            text = "Connect4Draw";
        } else if (opponent.isComputer()) {
            text = winner == RED ? "Connect4YouWin" : "Connect4ComputerWins";
        } else {
            text = winner == RED ? "Connect4RedWins" : "Connect4YellowWins";
        }
        final boolean happy = winner != Connect4Board.EMPTY && (!opponent.isComputer() || winner == RED);
        if (happy) {
            gameContext.getSoundManager().add(SOUNDS + "win.wav");
        } else if (winner != Connect4Board.EMPTY) {
            gameContext.getSoundManager().add(SOUNDS + "lose.wav");
        }
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate(text), height * 0.05);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        // In the row above the grid, empty at the end of a game: the tokens in a line stay in sight
        pill.setLayoutY(ghostY() - pill.getBoundsInLocal().getHeight() / 2);
        message.getChildren().setAll(pill);
        lastWinner = winner;
        phase = Phase.SHOW;
        elapsed = 0;
        showHud();
    }

    @Getter
    private int lastWinner = -1;

    /**
     * The end of the match: "Bravo" if the player won it (always with two players), else the statistics at once.
     */
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
        return !opponent.isComputer() || scores[RED] > scores[YELLOW];
    }

    /**
     * @return the games won by a player
     */
    int score(final int player) {
        return scores[player];
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final List<Node> parts = new ArrayList<>();
        if (phase != Phase.SHOW && phase != Phase.OVER && phase != Phase.WAIT) {
            final String turn;
            if (opponent.isComputer()) {
                turn = current == RED ? "Connect4YourTurn" : "Connect4ComputerTurn";
            } else {
                turn = "Connect4Turn";
            }
            final Group pill = GameStyle.pill(gameContext.getTranslator().translate(turn), fontSize);
            final Group icon = token(current);
            final double size = pill.getBoundsInLocal().getHeight();
            icon.setScaleX(size * 0.9 / (cell * 0.8));
            icon.setScaleY(size * 0.9 / (cell * 0.8));
            icon.setLayoutX(pill.getBoundsInLocal().getWidth() + size * 0.6);
            icon.setLayoutY(size / 2);
            parts.add(new Group(pill, icon));
        }
        final String left = gameContext.getTranslator().translate(opponent.isComputer() ? "Connect4You" : "Connect4Red");
        final String right = gameContext.getTranslator().translate(opponent.isComputer() ? "Connect4Computer"
            : "Connect4Yellow");
        parts.add(GameStyle.pill(left + " " + scores[RED] + " – " + scores[YELLOW] + " " + right, fontSize));
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

    /**
     * @return the column the computer thinks of playing
     */
    int getChosen() {
        return chosen;
    }

    boolean isGhostShown() {
        return ghost.isVisible();
    }

    /**
     * @return the zone looked at to play in a column
     */
    Rectangle zoneOf(final int column) {
        return zones.get(column);
    }

    /**
     * @return the grid on the screen (left, top, right, bottom), then the middle of the token waiting (its height)
     */
    double[] layout() {
        return new double[]{boardX, boardY, boardX + cell * Connect4Board.COLUMNS, boardY + cell * Connect4Board.ROWS,
            ghostY()};
    }

    /**
     * @return the circles around the tokens in a line, at the end of a game
     */
    int marks() {
        return marksLayer.getChildren().size();
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
        zones.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }
}
