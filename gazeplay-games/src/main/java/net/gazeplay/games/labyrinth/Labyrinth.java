package net.gazeplay.games.labyrinth;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Labyrinth (reviewed on 9/11/2026, maintainer's rules): the mouse goes to the cheese (or an animal to the house) in
 * a labyrinth of hedges drawn at random, where one path only joins two boxes. Each goal reached counts, then a new
 * labyrinth is dug; the game ends after 3, 5 or 10 goals ("Bravo"), or never. The character starts in a corner, the goal
 * at least half of the longest path away. The player moves the character by a fixation on an arrow next to it or around
 * the labyrinth, or by a fixation on the character, then a look at a box next to it.
 * <p>
 * One loop, in real seconds: the fixations are counted by the game (length of the settings, read at each step), nothing
 * can be chosen while a labyrinth is dug (about 1.5 s) or before the delay of the settings, and nothing moves while the
 * menu of the game is open. (Before the review: 4 x 6 boxes in blocks, a cheese larger than a box, no end, the former
 * progress indicator, arrows around the labyrinth a quarter of the screen high, nothing removed at the end.)
 */
public class Labyrinth implements GameLifeCycle {

    /**
     * In seconds: the digging of a labyrinth, the joy after a goal reached.
     */
    static final double DIGGING = 1.5;
    static final double JOY = 1.0;
    /**
     * In boxes per second: how fast the picture of the character follows its moves.
     */
    private static final double SLIDE = 6;

    private static final String DATA = "data/labyrinth/images/";
    private static final String ANIMALS = "data/common/default/images/noBackGround/";
    private static final String[] ANIMAL_PICTURES = {"bear", "chouette", "crabe", "dog", "ecureuil", "elephant", "fox",
        "frog", "giraffe", "herisson", "hippo", "lapin", "lion", "meduse", "penguin", "poulpe", "snake", "turtle"};
    private static final Map<String, Image> GAME_IMAGES = new HashMap<>();

    enum Phase { DIGGING, PLAY, JOY, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final LabyrinthStats stats;
    @Getter
    private final LabyrinthCharacter character;
    @Getter
    private final LabyrinthLevel level;
    @Getter
    private final LabyrinthMove move;
    @Getter
    private final LabyrinthEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private Maze maze;
    @Getter
    private int line;
    @Getter
    private int column;
    @Getter
    private int goalLine;
    @Getter
    private int goalColumn;
    @Getter
    private int score;
    @Getter
    private boolean selected;
    private double elapsed;
    @Getter
    private double waiting;
    private int opened;
    private double shownX;
    private double shownY;

    private double width;
    private double height;
    @Getter
    private double cell;
    private double boardX;
    private double boardY;

    private Group layer;
    private Group wallsLayer;
    private Group boxesLayer;
    private Group hud;
    private ImageView characterView;
    private ImageView goalView;
    private FixationRing ring;
    private final Map<String, Node> walls = new HashMap<>();
    private final Map<Maze.Direction, Arrow> arrows = new EnumMap<>(Maze.Direction.class);
    private Circle characterTarget;
    private final List<Node> gazeNodes = new ArrayList<>();

    /**
     * What is looked at (an arrow, or the character to choose it), and how far its fixation is.
     */
    private Object looked;
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
    Labyrinth(final IGameContext gameContext, final LabyrinthStats stats, final LabyrinthCharacter character,
              final LabyrinthLevel level, final LabyrinthMove move, final LabyrinthEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.character = character;
        this.level = level;
        this.move = move;
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
        layOut();

        final Rectangle grass = new Rectangle(0, 0, width, height);
        grass.setFill(Color.rgb(150, 196, 120));
        final Rectangle floor = new Rectangle(boardX, boardY, cell * level.getColumns(), cell * level.getLines());
        floor.setFill(Color.rgb(244, 232, 200));
        floor.setArcWidth(cell * 0.3);
        floor.setArcHeight(cell * 0.3);
        boxesLayer = new Group();
        wallsLayer = new Group();
        wallsLayer.setMouseTransparent(true);
        goalView = new ImageView();
        goalView.setMouseTransparent(true);
        characterView = new ImageView();
        characterView.setMouseTransparent(true);
        final Group arrowsLayer = new Group();
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(grass, floor, boxesLayer, goalView, characterView, wallsLayer, arrowsLayer, ring, hud);
        gameContext.getChildren().add(layer);

        if (move == LabyrinthMove.SELECT_THEN_GUIDE) {
            for (int i = 0; i < level.getLines(); i++) {
                for (int j = 0; j < level.getColumns(); j++) {
                    final Rectangle box = new Rectangle(boardX + j * cell, boardY + i * cell, cell, cell);
                    box.setFill(Color.TRANSPARENT);
                    final int boxLine = i;
                    final int boxColumn = j;
                    listen(box, () -> lookAtBox(boxLine, boxColumn), () -> { });
                    boxesLayer.getChildren().add(box);
                }
            }
            characterTarget = new Circle(cell * 0.42, Color.TRANSPARENT);
            listen(characterTarget, () -> lookAt(characterTarget), () -> lookAway(characterTarget));
            boxesLayer.getChildren().add(characterTarget);
        } else {
            final double size = move == LabyrinthMove.GLOBAL_ARROWS ? globalArrowSize() : cell * 0.62;
            for (final Maze.Direction direction : Maze.Direction.values()) {
                final Arrow arrow = new Arrow(direction, size);
                arrows.put(direction, arrow);
                arrowsLayer.getChildren().add(arrow.node);
                listen(arrow.node, () -> lookAt(arrow), () -> lookAway(arrow));
            }
            if (move == LabyrinthMove.GLOBAL_ARROWS) {
                placeGlobalArrows();
            }
        }

        gameContext.getGazeDeviceManager().addStats(stats);
        score = 0;
        showHud();
        newMaze();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * Places the labyrinth in the middle of the screen, under the band of the score, with room around it for the arrows
     * around the labyrinth.
     */
    private void layOut() {
        final double smaller = Math.min(width, height);
        final double gap = smaller * 0.03;
        final double top = height * 0.11;
        final double around = move == LabyrinthMove.GLOBAL_ARROWS ? globalArrowSize() + 2 * gap : gap * 1.5;
        final double topRoom = top + (move == LabyrinthMove.GLOBAL_ARROWS ? around : 0);
        final double availableWidth = width - 2 * around;
        final double availableHeight = height - topRoom - around;
        cell = Math.min(availableWidth / level.getColumns(), availableHeight / level.getLines());
        boardX = (width - cell * level.getColumns()) / 2;
        boardY = topRoom + (availableHeight - cell * level.getLines()) / 2;
    }

    private double globalArrowSize() {
        return Math.min(width, height) * 0.12;
    }

    private void placeGlobalArrows() {
        final double size = globalArrowSize();
        final double gap = Math.min(width, height) * 0.03;
        final double boardWidth = cell * level.getColumns();
        final double boardHeight = cell * level.getLines();
        arrows.get(Maze.Direction.UP).placeAt(boardX + boardWidth / 2, boardY - gap - size / 2);
        arrows.get(Maze.Direction.DOWN).placeAt(boardX + boardWidth / 2, boardY + boardHeight + gap + size / 2);
        arrows.get(Maze.Direction.LEFT).placeAt(boardX - gap - size / 2, boardY + boardHeight / 2);
        arrows.get(Maze.Direction.RIGHT).placeAt(boardX + boardWidth + gap + size / 2, boardY + boardHeight / 2);
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
     * @return an image of the game, loaded once
     */
    private static Image gameImage(final String path) {
        return GAME_IMAGES.computeIfAbsent(path, Image::new);
    }

    /**
     * A new labyrinth, all closed, which is dug before the eyes; the character starts in a corner, the goal is far.
     */
    private void newMaze() {
        maze = new Maze(level.getLines(), level.getColumns(), random);
        final int corner = random.nextInt(4);
        line = corner < 2 ? 0 : level.getLines() - 1;
        column = corner % 2 == 0 ? 0 : level.getColumns() - 1;
        final int[][] distances = maze.distancesFrom(line, column);
        int longest = 0;
        for (final int[] row : distances) {
            for (final int distance : row) {
                longest = Math.max(longest, distance);
            }
        }
        final int far = (int) Math.ceil(longest / 2.0);
        final List<int[]> places = new ArrayList<>();
        for (int i = 0; i < level.getLines(); i++) {
            for (int j = 0; j < level.getColumns(); j++) {
                if (distances[i][j] >= far && distances[i][j] > 0) {
                    places.add(new int[]{i, j});
                }
            }
        }
        final int[] goal = places.get(random.nextInt(places.size()));
        goalLine = goal[0];
        goalColumn = goal[1];

        final double size = cell * 0.78;
        if (character == LabyrinthCharacter.MOUSE) {
            characterView.setImage(gameImage(DATA + "mouse.png"));
            goalView.setImage(gameImage(DATA + "cheese.png"));
        } else {
            characterView.setImage(gameImage(ANIMALS + ANIMAL_PICTURES[random.nextInt(ANIMAL_PICTURES.length)]
                + ".png"));
            goalView.setImage(gameImage(DATA + "house.png"));
        }
        characterView.setFitWidth(size);
        characterView.setFitHeight(size);
        characterView.setRotate(0);
        characterView.setEffect(null);
        goalView.setFitWidth(size);
        goalView.setFitHeight(size);
        goalView.setScaleX(1);
        goalView.setScaleY(1);
        goalView.setLayoutX(boxX(goalColumn) - size / 2);
        goalView.setLayoutY(boxY(goalLine) - size / 2);
        shownX = boxX(column);
        shownY = boxY(line);
        placeCharacter();
        characterView.setVisible(false);
        goalView.setVisible(false);

        drawClosedWalls();
        opened = 0;
        elapsed = 0;
        waiting = Math.max(DIGGING, configuration.getGameStartDelay() / 1000.0);
        selected = false;
        looked = null;
        progress = 0;
        ring.stop();
        phase = Phase.DIGGING;
        arrows.values().forEach(arrow -> arrow.node.setVisible(false));
        stats.incrementNumberOfGoalsToReach();
    }

    /**
     * Every wall between two boxes, and the hedge around the labyrinth.
     */
    private void drawClosedWalls() {
        wallsLayer.getChildren().clear();
        walls.clear();
        final int lines = level.getLines();
        final int columns = level.getColumns();
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < columns; j++) {
                if (j < columns - 1) {
                    final double x = boardX + (j + 1) * cell;
                    addWall(i + ":" + j + ":0", x, boardY + i * cell, x, boardY + (i + 1) * cell);
                }
                if (i < lines - 1) {
                    final double y = boardY + (i + 1) * cell;
                    addWall(i + ":" + j + ":1", boardX + j * cell, y, boardX + (j + 1) * cell, y);
                }
            }
        }
        final double right = boardX + columns * cell;
        final double bottom = boardY + lines * cell;
        addWall("top", boardX, boardY, right, boardY);
        addWall("bottom", boardX, bottom, right, bottom);
        addWall("left", boardX, boardY, boardX, bottom);
        addWall("right", right, boardY, right, bottom);
    }

    /**
     * A hedge from a point to another, with round ends.
     */
    private void addWall(final String key, final double x1, final double y1, final double x2, final double y2) {
        final double thickness = Math.max(6, cell * 0.14);
        final Rectangle dark = new Rectangle(Math.min(x1, x2) - thickness / 2, Math.min(y1, y2) - thickness / 2,
            Math.abs(x2 - x1) + thickness, Math.abs(y2 - y1) + thickness);
        dark.setArcWidth(thickness);
        dark.setArcHeight(thickness);
        dark.setFill(Color.rgb(64, 128, 70));
        final double inset = thickness * 0.2;
        final Rectangle light = new Rectangle(dark.getX() + inset, dark.getY() + inset,
            dark.getWidth() - 2 * inset, dark.getHeight() - 2.5 * inset);
        light.setArcWidth(thickness - 2 * inset);
        light.setArcHeight(thickness - 2 * inset);
        light.setFill(Color.rgb(100, 164, 94));
        final Group hedge = new Group(dark, light);
        walls.put(key, hedge);
        wallsLayer.getChildren().add(hedge);
    }

    private double boxX(final int boxColumn) {
        return boardX + (boxColumn + 0.5) * cell;
    }

    private double boxY(final int boxLine) {
        return boardY + (boxLine + 0.5) * cell;
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        final double scaled = seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        slideCharacter(scaled);
        switch (phase) {
            case DIGGING -> dig(seconds);
            case PLAY -> fixate(seconds);
            case JOY -> rejoice(seconds);
            default -> {
            }
        }
    }

    /**
     * Opens the walls in the order they were dug, over {@link #DIGGING} seconds; then the game can be played, once the
     * delay of the settings is over too.
     */
    private void dig(final double seconds) {
        elapsed += seconds;
        final List<int[]> openings = maze.getOpenings();
        final int wanted = (int) Math.min(openings.size(), Math.floor(openings.size() * elapsed / DIGGING));
        while (opened < wanted) {
            final int[] opening = openings.get(opened++);
            wallsLayer.getChildren().remove(walls.remove(opening[0] + ":" + opening[1] + ":" + opening[2]));
        }
        if (elapsed >= waiting) {
            while (opened < openings.size()) {
                final int[] opening = openings.get(opened++);
                wallsLayer.getChildren().remove(walls.remove(opening[0] + ":" + opening[1] + ":" + opening[2]));
            }
            characterView.setVisible(true);
            goalView.setVisible(true);
            phase = Phase.PLAY;
            updateArrows();
            stats.notifyNewRoundReady();
        }
    }

    /**
     * Counts the fixation on what is looked at, with the length of the settings read at each step.
     */
    private void fixate(final double seconds) {
        if (looked == null) {
            return;
        }
        if (!isEnabled(looked)) {
            ring.stop();
            progress = 0;
            return;
        }
        if (!ring.isVisible()) {
            // Looked at during the digging, or towards a wall before the last move
            showRing(looked);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress < 1) {
            return;
        }
        final Object done = looked;
        progress = 0;
        ring.stop();
        if (done instanceof Arrow arrow) {
            // An arrow next to the character moves with it: it must be looked at again; one around the labyrinth can be
            // looked at still for the next move
            if (move == LabyrinthMove.LOCAL_ARROWS) {
                looked = null;
            }
            go(arrow.direction);
        } else {
            looked = null;
            selected = true;
            final DropShadow glow = new DropShadow(cell * 0.25, Color.rgb(255, 214, 90));
            glow.setSpread(0.5);
            characterView.setEffect(glow);
        }
    }

    private void rejoice(final double seconds) {
        elapsed += seconds;
        final double pulse = 1 + 0.25 * Math.sin(Math.min(1, elapsed / JOY) * Math.PI * 3);
        goalView.setScaleX(pulse);
        goalView.setScaleY(pulse);
        if (elapsed >= JOY) {
            if (end.getGoals() > 0 && score >= end.getGoals()) {
                win();
            } else {
                newMaze();
            }
        }
    }

    private boolean isEnabled(final Object target) {
        if (target instanceof Arrow arrow) {
            return arrow.node.isVisible() && maze.canGo(line, column, arrow.direction);
        }
        return target == characterTarget && !selected;
    }

    /**
     * The gaze (or the mouse) enters an arrow, or the character to choose it.
     */
    void lookAt(final Object target) {
        if (looked == target) {
            return;
        }
        looked = target;
        progress = 0;
        ring.stop();
        if (phase == Phase.PLAY && isEnabled(target)) {
            showRing(target);
        }
    }

    private void showRing(final Object target) {
        if (target instanceof Arrow arrow) {
            ring.setSize(arrow.size * 1.1);
            ring.show(arrow.x, arrow.y);
        } else {
            ring.setSize(cell * 0.9);
            ring.show(boxX(column), boxY(line));
        }
    }

    void lookAway(final Object target) {
        if (looked == target) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * Once the character chosen, the gaze (or the mouse) entering a box next to it, without a wall between them, takes it
     * there; looking further away lets it go.
     */
    void lookAtBox(final int boxLine, final int boxColumn) {
        if (phase != Phase.PLAY || !selected || (boxLine == line && boxColumn == column)) {
            return;
        }
        for (final Maze.Direction direction : Maze.Direction.values()) {
            if (boxLine == line + direction.lines && boxColumn == column + direction.columns) {
                if (maze.canGo(line, column, direction)) {
                    go(direction);
                }
                return;
            }
        }
        selected = false;
        characterView.setEffect(null);
    }

    /**
     * Moves the character one box in a direction, if no wall is in the way.
     */
    void go(final Maze.Direction direction) {
        if (phase != Phase.PLAY || !maze.canGo(line, column, direction)) {
            return;
        }
        line += direction.lines;
        column += direction.columns;
        if (character == LabyrinthCharacter.MOUSE) {
            characterView.setRotate(direction.angle);
        }
        updateArrows();
        if (line == goalLine && column == goalColumn) {
            reach();
        }
    }

    /**
     * The goal is reached: one more point, a moment of joy, then a new labyrinth, or the end.
     */
    private void reach() {
        phase = Phase.JOY;
        elapsed = 0;
        score++;
        stats.incrementNumberOfGoalsReached();
        selected = false;
        characterView.setEffect(null);
        looked = null;
        progress = 0;
        ring.stop();
        arrows.values().forEach(arrow -> arrow.node.setVisible(false));
        showHud();
    }

    /**
     * Puts the arrows next to the character in the boxes around it (inside the labyrinth), gray towards a wall.
     */
    private void updateArrows() {
        for (final Arrow arrow : arrows.values()) {
            if (move == LabyrinthMove.LOCAL_ARROWS) {
                final int arrowLine = line + arrow.direction.lines;
                final int arrowColumn = column + arrow.direction.columns;
                arrow.node.setVisible(maze.inside(arrowLine, arrowColumn));
                arrow.placeAt(boxX(arrowColumn), boxY(arrowLine));
            } else {
                arrow.node.setVisible(true);
            }
            arrow.node.setOpacity(maze.canGo(line, column, arrow.direction) ? 1 : 0.35);
        }
        if (characterTarget != null) {
            characterTarget.setCenterX(boxX(column));
            characterTarget.setCenterY(boxY(line));
        }
    }

    private void slideCharacter(final double seconds) {
        final double targetX = boxX(column);
        final double targetY = boxY(line);
        final double most = SLIDE * cell * seconds;
        final double distance = Math.hypot(targetX - shownX, targetY - shownY);
        if (distance <= most) {
            shownX = targetX;
            shownY = targetY;
        } else {
            shownX += (targetX - shownX) / distance * most;
            shownY += (targetY - shownY) / distance * most;
        }
        placeCharacter();
    }

    private void placeCharacter() {
        characterView.setLayoutX(shownX - characterView.getFitWidth() / 2);
        characterView.setLayoutY(shownY - characterView.getFitHeight() / 2);
    }

    private void showHud() {
        hud.getChildren().clear();
        final String text = gameContext.getTranslator().translate(character.getGoalsKey()) + " " + score
            + (end.getGoals() > 0 ? " / " + end.getGoals() : "");
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
     * @return the arrow of a direction, null when the character is chosen then guided
     */
    Object getArrow(final Maze.Direction direction) {
        return arrows.get(direction);
    }

    /**
     * @return what the gaze looks at to choose the character, null with the arrows
     */
    Object getCharacterTarget() {
        return characterTarget;
    }

    /**
     * @return whether the arrow of a direction is shown and can be chosen
     */
    boolean isArrowEnabled(final Maze.Direction direction) {
        final Arrow arrow = arrows.get(direction);
        return arrow != null && isEnabled(arrow);
    }

    /**
     * @return the middle of the arrow of a direction on the screen
     */
    double[] arrowCenter(final Maze.Direction direction) {
        final Arrow arrow = arrows.get(direction);
        return new double[]{arrow.x, arrow.y, arrow.size};
    }

    /**
     * @return the corners of the labyrinth on the screen: left, top, right, bottom
     */
    double[] board() {
        return new double[]{boardX, boardY, boardX + cell * level.getColumns(), boardY + cell * level.getLines()};
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (ring != null) {
            ring.stop();
        }
        gazeNodes.forEach(node -> gameContext.getGazeDeviceManager().removeEventFilter(node));
        gazeNodes.clear();
        arrows.clear();
        walls.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * An arrow: a round button with an arrow facing its direction.
     */
    private static final class Arrow {
        private final Maze.Direction direction;
        private final double size;
        private final Group node;
        private double x;
        private double y;

        Arrow(final Maze.Direction direction, final double size) {
            this.direction = direction;
            this.size = size;
            final Circle button = new Circle(size / 2);
            button.setFill(Color.rgb(255, 252, 244, 0.92));
            button.setStroke(Color.rgb(90, 96, 110));
            button.setStrokeWidth(Math.max(2, size * 0.03));
            final double d = size;
            final Polygon shape = new Polygon(0, -0.32 * d, 0.26 * d, -0.02 * d, 0.1 * d, -0.02 * d, 0.1 * d, 0.3 * d,
                -0.1 * d, 0.3 * d, -0.1 * d, -0.02 * d, -0.26 * d, -0.02 * d);
            shape.setStyle("-fx-fill: -gp-accent;");
            shape.setRotate(direction.angle);
            node = new Group(button, shape);
            node.setVisible(false);
        }

        void placeAt(final double newX, final double newY) {
            x = newX;
            y = newY;
            node.setLayoutX(newX);
            node.setLayoutY(newY);
        }
    }
}
