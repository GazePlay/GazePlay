package net.gazeplay.games.slidingpuzzle;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The sliding puzzle "Jeu de taquin" (reviewed on 9/11/2026, maintainer's rules): a picture (or numbers) cut into pieces,
 * with an empty place; a fixation on a piece next to the empty place slides it there. The picture is first shown whole,
 * then shuffled by moves that can be played back, so that every puzzle can be solved; the model stays beside the board.
 * When the picture is rebuilt, its last piece comes back, the puzzle counts, and a new one comes; the game ends after 1,
 * 3 or 5 puzzles ("Bravo"), or never.
 * <p>
 * One loop, in real seconds: the fixations are counted by the game (length of the settings, read at each step), nothing
 * can be chosen while the picture is shown whole and shuffled, or before the delay of the settings, and nothing moves
 * while the menu of the game is open. (Before the review: the 8 pieces put at random, so that one puzzle out of two could
 * not be solved, a piece with a Biboule in place of the empty place, 3 x 3 only, pictures cut beforehand, no model, the
 * board off center, no end.)
 */
public class SlidingPuzzle implements GameLifeCycle {

    /**
     * In seconds: the picture shown whole before the shuffle, the joy after a puzzle solved.
     */
    static final double SHOW = 1.5;
    static final double JOY = 1.5;
    /**
     * In seconds: the shuffle, the pieces sliding to their new places.
     */
    static final double SHUFFLE = 0.6;
    /**
     * In boxes per second: how fast a piece slides.
     */
    private static final double SLIDE = 7;

    static final String DATA = "data/sliding-puzzle/";
    static final String[] PICTURES = {"sea.png", "balloons.png", "biboules.png", "joconde.jpg"};

    enum Phase { SHOW, PLAY, JOY, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final PuzzleContent content;
    @Getter
    private final PuzzleLevel level;
    @Getter
    private final PuzzleEnd end;
    private final ReplayablePseudoRandom random;
    private final int size;

    @Getter
    private Phase phase = Phase.OVER;
    /**
     * The piece at each place, line by line; the piece {@code size * size - 1} is the empty place.
     */
    private int[] board;
    private int[] shuffled;
    @Getter
    private int score;
    private double elapsed;
    @Getter
    private double waiting;
    private int lastPicture = -1;
    private List<File> userImages = List.of();

    private double width;
    private double height;
    @Getter
    private double cell;
    private double boardX;
    private double boardY;
    private double modelX;
    private double modelY;
    private double modelSide;

    private Group layer;
    private Group piecesLayer;
    private Group modelLayer;
    private Group hud;
    private FixationRing ring;
    private final List<Piece> pieces = new ArrayList<>();
    private final List<Node> gazeNodes = new ArrayList<>();

    /**
     * The piece looked at, -1 if none, and how far its fixation is.
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
    SlidingPuzzle(final IGameContext gameContext, final Stats stats, final PuzzleContent content,
                  final PuzzleLevel level, final PuzzleEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.content = content;
        this.level = level;
        this.end = end;
        this.size = level.getSize();
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
        layOut();

        final Rectangle background = new Rectangle(0, 0, width, height);
        background.setFill(Color.rgb(236, 228, 214));
        final double side = cell * size;
        final double frame = cell * 0.12;
        final Rectangle wood = new Rectangle(boardX - frame, boardY - frame, side + 2 * frame, side + 2 * frame);
        wood.setArcWidth(frame * 2.4);
        wood.setArcHeight(frame * 2.4);
        wood.setFill(Color.rgb(176, 138, 100));
        final Rectangle well = new Rectangle(boardX - frame * 0.35, boardY - frame * 0.35, side + frame * 0.7,
            side + frame * 0.7);
        well.setArcWidth(frame);
        well.setArcHeight(frame);
        well.setFill(Color.rgb(120, 92, 70));
        piecesLayer = new Group();
        modelLayer = new Group();
        modelLayer.setMouseTransparent(true);
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(background, wood, well, modelLayer, piecesLayer, ring, hud);
        gameContext.getChildren().add(layer);
        gameContext.getGazeDeviceManager().addStats(stats);

        if (content == PuzzleContent.MY_IMAGES) {
            userImages = UserImagesVariant.images(PuzzleContent.USER_IMAGES_FOLDER);
            if (userImages.isEmpty()) {
                // The game started without going through the menu, which asks for images
                showMessage();
                return;
            }
        }
        score = 0;
        showHud();
        newPuzzle();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * Places the board and its model, side by side in the middle of the screen, under the band of the score.
     */
    private void layOut() {
        final double margin = Math.min(width, height) * 0.05;
        final double top = height * 0.11;
        final double availableHeight = height - top - margin;
        // The model is 0.36 of the board, one margin away from it; room is left for the frame of the board
        final double side = Math.min(availableHeight / 1.1, (width - 3 * margin) / 1.36);
        cell = side / size;
        modelSide = side * 0.36;
        boardX = (width - side - margin - modelSide) / 2;
        boardY = top + (availableHeight - side) / 2;
        modelX = boardX + side + margin;
        modelY = boardY;
    }

    /**
     * A new puzzle: the picture is shown whole, then shuffled.
     */
    private void newPuzzle() {
        clearPieces();
        Image picture = null;
        Rectangle2D square = null;
        if (content != PuzzleContent.NUMBERS) {
            picture = nextPicture();
            if (picture == null) {
                showMessage();
                phase = Phase.OVER;
                return;
            }
            final double smaller = Math.min(picture.getWidth(), picture.getHeight());
            square = new Rectangle2D((picture.getWidth() - smaller) / 2, (picture.getHeight() - smaller) / 2, smaller,
                smaller);
        }
        for (int piece = 0; piece < size * size; piece++) {
            final Piece made = new Piece(piece, picture, square);
            pieces.add(made);
            piecesLayer.getChildren().add(made.node);
            if (piece != empty()) {
                final int listened = piece;
                listen(made.node, () -> lookAt(listened), () -> lookAway(listened));
            }
        }
        showModel(picture, square);

        board = solved(size);
        shuffled = shuffle(size, level.getMoves(), random);
        for (final Piece piece : pieces) {
            piece.shownX = placeX(piece.number);
            piece.shownY = placeY(piece.number);
            piece.place();
        }
        elapsed = 0;
        waiting = Math.max(SHOW + SHUFFLE, configuration.getGameStartDelay() / 1000.0);
        looked = -1;
        progress = 0;
        ring.stop();
        phase = Phase.SHOW;
        stats.incrementNumberOfGoalsToReach();
    }

    /**
     * @return the picture of the next puzzle, another one than the last for the pictures of the game; null if no image
     * of the user can be read
     */
    private Image nextPicture() {
        if (content == PuzzleContent.PICTURES) {
            int index = random.nextInt(PICTURES.length - (lastPicture < 0 ? 0 : 1));
            if (lastPicture >= 0 && index >= lastPicture) {
                index++;
            }
            lastPicture = index;
            return new Image(DATA + PICTURES[index]);
        }
        final List<File> left = new ArrayList<>(userImages);
        while (!left.isEmpty()) {
            final File file = left.remove(random.nextInt(left.size()));
            final Image image = new Image(file.toURI().toString(), 1200, 1200, true, true);
            if (!image.isError() && image.getWidth() > 0) {
                return image;
            }
        }
        return null;
    }

    private void showMessage() {
        final Group message = GameStyle.card(gameContext.getTranslator().translate("NoUserImages"), height * 0.035,
            width * 0.5);
        message.setLayoutX((width - message.getBoundsInLocal().getWidth()) / 2);
        message.setLayoutY((height - message.getBoundsInLocal().getHeight()) / 2);
        hud.getChildren().add(message);
    }

    /**
     * The model beside the board: the picture whole, or the numbers in order with the empty place.
     */
    private void showModel(final Image picture, final Rectangle2D square) {
        modelLayer.getChildren().clear();
        final double frame = modelSide * 0.04;
        final Rectangle paper = new Rectangle(modelX - frame, modelY - frame, modelSide + 2 * frame,
            modelSide + 2 * frame);
        paper.setArcWidth(frame * 3);
        paper.setArcHeight(frame * 3);
        paper.setFill(Color.WHITE);
        modelLayer.getChildren().add(paper);
        if (picture != null) {
            final ImageView view = new ImageView(picture);
            view.setViewport(square);
            view.setFitWidth(modelSide);
            view.setFitHeight(modelSide);
            view.setLayoutX(modelX);
            view.setLayoutY(modelY);
            modelLayer.getChildren().add(view);
        } else {
            final double small = modelSide / size;
            for (int piece = 0; piece < size * size - 1; piece++) {
                final Group tile = numberTile(piece, small);
                final int line = piece / size;
                tile.setLayoutX(modelX + (piece % size) * small);
                tile.setLayoutY(modelY + line * small);
                modelLayer.getChildren().add(tile);
            }
        }
    }

    /**
     * A piece of the numbers: a soft square with its number, of a size, from (0, 0).
     */
    private static Group numberTile(final int piece, final double side) {
        final double gap = side * 0.05;
        final Rectangle square = new Rectangle(gap / 2, gap / 2, side - gap, side - gap);
        square.setArcWidth(side * 0.18);
        square.setArcHeight(side * 0.18);
        square.setFill(Color.rgb(252, 246, 232));
        square.setStroke(Color.rgb(196, 170, 130));
        square.setStrokeWidth(Math.max(1, side * 0.02));
        final Text number = new Text(String.valueOf(piece + 1));
        number.setFont(GameStyle.titleFont(side * 0.48));
        number.setStyle("-fx-fill: -gp-accent;");
        number.setTextAlignment(TextAlignment.CENTER);
        number.setTextOrigin(VPos.CENTER);
        number.setWrappingWidth(side);
        number.setLayoutY(side / 2);
        return new Group(square, number);
    }

    /**
     * The last piece of the numbers, which comes back when they are in order: a star in place of a number.
     */
    private static Group starTile(final double side) {
        final Group tile = numberTile(0, side);
        tile.getChildren().remove(1);
        final Polygon star = new Polygon();
        for (int k = 0; k < 10; k++) {
            final double angle = -Math.PI / 2 + k * Math.PI / 5;
            final double radius = side * (k % 2 == 0 ? 0.3 : 0.13);
            star.getPoints().addAll(side / 2 + radius * Math.cos(angle), side / 2 + radius * Math.sin(angle));
        }
        star.setFill(Color.rgb(250, 200, 70));
        star.setStroke(Color.rgb(200, 140, 40));
        star.setStrokeWidth(Math.max(1, side * 0.02));
        tile.getChildren().add(star);
        return tile;
    }

    private int empty() {
        return size * size - 1;
    }

    private double placeX(final int place) {
        return boardX + (place % size) * cell;
    }

    private double placeY(final int place) {
        final int line = place / size;
        return boardY + line * cell;
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

    private void clearPieces() {
        gazeNodes.forEach(node -> gameContext.getGazeDeviceManager().removeEventFilter(node));
        gazeNodes.clear();
        pieces.clear();
        if (piecesLayer != null) {
            piecesLayer.getChildren().clear();
        }
    }

    /**
     * @return the pieces in order, the empty place last
     */
    static int[] solved(final int size) {
        final int[] solved = new int[size * size];
        for (int i = 0; i < solved.length; i++) {
            solved[i] = i;
        }
        return solved;
    }

    static boolean isSolved(final int[] board) {
        for (int i = 0; i < board.length; i++) {
            if (board[i] != i) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return a shuffled board, never solved: by moves of the empty place from the picture rebuilt (never undoing the
     * last one), or for a full shuffle, any order of the pieces that can be solved
     */
    static int[] shuffle(final int size, final int moves, final ReplayablePseudoRandom random) {
        final int empty = size * size - 1;
        int[] board;
        do {
            board = solved(size);
            if (moves > 0) {
                int place = empty;
                int previous = -1;
                for (int k = 0; k < moves; k++) {
                    final List<Integer> next = new ArrayList<>();
                    for (final int neighbor : neighbors(size, place)) {
                        if (neighbor != previous) {
                            next.add(neighbor);
                        }
                    }
                    final int chosen = next.get(random.nextInt(next.size()));
                    board[place] = board[chosen];
                    board[chosen] = empty;
                    previous = place;
                    place = chosen;
                }
            } else {
                for (int i = board.length - 1; i > 0; i--) {
                    final int j = random.nextInt(i + 1);
                    final int kept = board[i];
                    board[i] = board[j];
                    board[j] = kept;
                }
                if (!isSolvable(board, size)) {
                    // Swapping two pieces (not the empty place) makes it solvable
                    final int first = board[0] == empty ? 1 : 0;
                    final int second = board[first + 1] == empty ? first + 2 : first + 1;
                    final int kept = board[first];
                    board[first] = board[second];
                    board[second] = kept;
                }
            }
        } while (isSolved(board));
        return board;
    }

    /**
     * @return whether a board can be solved: with an odd size, when the pairs of pieces in the wrong order are even; with
     * an even size, when they and the line of the empty place counted from the bottom (1 for the last line) add up odd
     */
    static boolean isSolvable(final int[] board, final int size) {
        final int empty = size * size - 1;
        int inversions = 0;
        int emptyPlace = 0;
        for (int i = 0; i < board.length; i++) {
            if (board[i] == empty) {
                emptyPlace = i;
                continue;
            }
            for (int j = i + 1; j < board.length; j++) {
                if (board[j] != empty && board[j] < board[i]) {
                    inversions++;
                }
            }
        }
        if (size % 2 != 0) {
            return inversions % 2 == 0;
        }
        return (inversions + size - emptyPlace / size) % 2 != 0;
    }

    /**
     * @return the places next to a place, above, at the right, below and at the left
     */
    static List<Integer> neighbors(final int size, final int place) {
        final List<Integer> neighbors = new ArrayList<>();
        final int line = place / size;
        final int column = place % size;
        if (line > 0) {
            neighbors.add(place - size);
        }
        if (column < size - 1) {
            neighbors.add(place + 1);
        }
        if (line < size - 1) {
            neighbors.add(place + size);
        }
        if (column > 0) {
            neighbors.add(place - 1);
        }
        return neighbors;
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        final double scaled = seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        slidePieces(scaled);
        switch (phase) {
            case SHOW -> show(seconds);
            case PLAY -> fixate(seconds);
            case JOY -> rejoice(seconds);
            default -> {
            }
        }
    }

    /**
     * The picture whole for {@link #SHOW} seconds, then the shuffle; then the game can be played, once the delay of the
     * settings is over too.
     */
    private void show(final double seconds) {
        elapsed += seconds;
        if (elapsed >= SHOW && board != shuffled) {
            board = shuffled;
            pieces.get(empty()).node.setVisible(false);
        }
        if (elapsed >= waiting) {
            phase = Phase.PLAY;
            updateMovable();
            stats.notifyNewRoundReady();
            if (looked >= 0 && isMovable(looked)) {
                showRing(looked);
            }
        }
    }

    /**
     * Counts the fixation on the piece looked at, with the length of the settings read at each step.
     */
    private void fixate(final double seconds) {
        if (looked < 0) {
            return;
        }
        if (!isMovable(looked)) {
            ring.stop();
            progress = 0;
            return;
        }
        if (!ring.isVisible()) {
            showRing(looked);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress < 1) {
            return;
        }
        final int done = looked;
        // The piece slides away from the gaze: the next one must be looked at
        looked = -1;
        progress = 0;
        ring.stop();
        slide(done);
    }

    private void rejoice(final double seconds) {
        elapsed += seconds;
        pieces.get(empty()).node.setOpacity(Math.min(1, elapsed / 0.5));
        if (elapsed >= JOY) {
            if (end.getPuzzles() > 0 && score >= end.getPuzzles()) {
                win();
            } else {
                newPuzzle();
            }
        }
    }

    /**
     * @return the place of a piece on the board
     */
    int placeOf(final int piece) {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == piece) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return whether a piece can slide: during the game, next to the empty place
     */
    boolean isMovable(final int piece) {
        return phase == Phase.PLAY && piece != empty()
            && neighbors(size, placeOf(empty())).contains(placeOf(piece));
    }

    /**
     * The gaze (or the mouse) enters a piece.
     */
    void lookAt(final int piece) {
        if (looked == piece) {
            return;
        }
        looked = piece;
        progress = 0;
        ring.stop();
        if (isMovable(piece)) {
            showRing(piece);
        }
    }

    void lookAway(final int piece) {
        if (looked == piece) {
            looked = -1;
            progress = 0;
            ring.stop();
        }
    }

    private void showRing(final int piece) {
        final int place = placeOf(piece);
        ring.setSize(cell * 0.8);
        ring.show(placeX(place) + cell / 2, placeY(place) + cell / 2);
    }

    /**
     * Slides a piece to the empty place.
     */
    void slide(final int piece) {
        if (!isMovable(piece)) {
            return;
        }
        final int from = placeOf(piece);
        final int to = placeOf(empty());
        board[to] = piece;
        board[from] = empty();
        updateMovable();
        if (isSolved(board)) {
            reach();
        }
    }

    /**
     * The picture is rebuilt: its last piece comes back, one more point, then a new puzzle, or the end.
     */
    private void reach() {
        phase = Phase.JOY;
        elapsed = 0;
        score++;
        stats.incrementNumberOfGoalsReached();
        looked = -1;
        progress = 0;
        ring.stop();
        updateMovable();
        final Piece last = pieces.get(empty());
        last.shownX = placeX(empty());
        last.shownY = placeY(empty());
        last.place();
        last.node.setOpacity(0);
        last.node.setVisible(true);
        showHud();
    }

    /**
     * Shows the pieces that can slide, by a colored border.
     */
    private void updateMovable() {
        for (final Piece piece : pieces) {
            piece.border.setVisible(isMovable(piece.number));
        }
    }

    private void slidePieces(final double seconds) {
        final double speed = (phase == Phase.SHOW ? 2.5 * size / SHUFFLE : SLIDE) * cell * seconds;
        for (final Piece piece : pieces) {
            final int place = placeOf(piece.number);
            final double targetX = placeX(place);
            final double targetY = placeY(place);
            final double distance = Math.hypot(targetX - piece.shownX, targetY - piece.shownY);
            if (distance <= speed) {
                piece.shownX = targetX;
                piece.shownY = targetY;
            } else {
                piece.shownX += (targetX - piece.shownX) / distance * speed;
                piece.shownY += (targetY - piece.shownY) / distance * speed;
            }
            piece.place();
        }
    }

    private void showHud() {
        hud.getChildren().clear();
        final String text = gameContext.getTranslator().translate("PuzzleScore") + " " + score
            + (end.getPuzzles() > 0 ? " / " + end.getPuzzles() : "");
        final Group pill = GameStyle.pill(text, height * 0.035);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(height * 0.02);
        hud.getChildren().add(pill);
    }

    private void win() {
        phase = Phase.OVER;
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

    /**
     * @return a copy of the board: the piece at each place, the empty place being the last piece
     */
    int[] getBoard() {
        return board == null ? null : board.clone();
    }

    /**
     * @return the middle of a place of the board on the screen
     */
    double[] placeCenter(final int place) {
        return new double[]{placeX(place) + cell / 2, placeY(place) + cell / 2};
    }

    /**
     * @return the corners of the board on the screen: left, top, right, bottom; then those of the model
     */
    double[] layout() {
        return new double[]{boardX, boardY, boardX + cell * size, boardY + cell * size, modelX, modelY,
            modelX + modelSide, modelY + modelSide};
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (ring != null) {
            ring.stop();
        }
        clearPieces();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A piece: its part of the picture (or its number) with round corners, and the border showing it can slide.
     */
    private final class Piece {
        private final int number;
        private final Group node;
        private final Rectangle border;
        private double shownX;
        private double shownY;

        Piece(final int number, final Image picture, final Rectangle2D square) {
            this.number = number;
            final double gap = cell * 0.04;
            final double inner = cell - gap;
            final Node face;
            if (picture != null) {
                final double part = square.getWidth() / size;
                final ImageView view = new ImageView(picture);
                final int line = number / size;
                view.setViewport(new Rectangle2D(square.getMinX() + (number % size) * part,
                    square.getMinY() + line * part, part, part));
                view.setFitWidth(inner);
                view.setFitHeight(inner);
                view.setLayoutX(gap / 2);
                view.setLayoutY(gap / 2);
                final Rectangle clip = new Rectangle(gap / 2, gap / 2, inner, inner);
                clip.setArcWidth(cell * 0.12);
                clip.setArcHeight(cell * 0.12);
                view.setClip(clip);
                face = view;
            } else if (number == size * size - 1) {
                face = starTile(cell);
            } else {
                face = numberTile(number, cell);
            }
            border = new Rectangle(gap / 2, gap / 2, inner, inner);
            border.setArcWidth(cell * 0.12);
            border.setArcHeight(cell * 0.12);
            border.setFill(Color.TRANSPARENT);
            border.setStrokeWidth(Math.max(3, cell * 0.035));
            border.setStyle("-fx-stroke: -gp-accent;");
            border.setVisible(false);
            border.setMouseTransparent(true);
            node = new Group(face, border);
        }

        void place() {
            node.setLayoutX(shownX);
            node.setLayoutY(shownY);
        }
    }
}
