package net.gazeplay.games.follow;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
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
import net.gazeplay.components.GameStyle;
import net.gazeplay.games.labyrinth.Maze;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * "Suivre le chemin" (name code Follow; reviewed on 13/11/2026, maintainer's rules): the Biboule goes where the gaze (or
 * the mouse) is, all the time, and the hedges stop it. In the coins, it picks up every coin of the map; in the keys, each
 * key opens the door of its color, up to the treasure. A new map is drawn at random each time (a labyrinth of the
 * Labyrinth, with loops for the coins, without for the keys: a door must not be gone round); the game ends after 1, 3 or
 * 5 maps ("Bravo"), or never.
 * <p>
 * One loop, in real seconds, at the animation speed of the settings: the Biboule goes at a speed in parts of the screen,
 * the same on every screen; it does not move before the delay of the settings at each map, nor while the menu of the game
 * is open. (Before the review: 3 fixed maps, one of 312 coins and one with an invisible trap; the Biboule a fiftieth of
 * the width, moving 4 pixels at each image, its loop never stopped; 13 pictures of unknown source; no sound, no end but
 * the win.)
 */
public class Follow implements GameLifeCycle {

    /**
     * In parts of the smaller side of the screen per second: the speed of the Biboule.
     */
    static final double SPEED = 0.35;
    /**
     * In seconds: a new map appearing (at least; the delay of the settings too), the joy after a map finished.
     */
    static final double APPEAR = 0.6;
    static final double JOY = 1.4;
    static final double DOOR_OPENING = 0.4;
    /**
     * In boxes: half the side of the square of the Biboule against the hedges, its picture, how near an item is taken.
     */
    static final double BODY = 0.27;
    static final double PICTURE = 0.72;
    static final double REACH = 0.4;

    private static final String BIBOULE = "data/shooter/biboule/images/Blue.png";
    private static final String SOUNDS = "data/follow/sounds/";
    private static final String WIN_SOUND = "data/goosegame/sounds/win.wav";
    private static final Color[] DOOR_COLORS = {Color.rgb(222, 72, 72), Color.rgb(60, 112, 222),
        Color.rgb(150, 82, 204)};
    private static final double EPSILON = 0.01;

    enum Phase { APPEAR, PLAY, JOY, OVER }

    enum Kind { COIN, KEY, CHEST }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final FollowMode mode;
    @Getter
    private final FollowLevel level;
    @Getter
    private final FollowEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private Maze maze;
    @Getter
    private int maps;
    @Getter
    private int coinsTaken;
    private double elapsed;
    private double time;

    private double width;
    private double height;
    @Getter
    private double cell;
    private double boardX;
    private double boardY;
    private double thickness;

    /**
     * The middle of the Biboule, and where it goes (once the gaze or the mouse has been seen).
     */
    @Getter
    private double x;
    @Getter
    private double y;
    private double aimX;
    private double aimY;
    private boolean aimed;

    private Group layer;
    private Group board;
    private Group doorsLayer;
    private Group itemsLayer;
    private Group wallsLayer;
    private Group hud;
    private Group message;
    private ImageView biboule;
    private Circle pointer;
    private Rectangle area;
    @Getter
    private final List<Item> items = new ArrayList<>();
    @Getter
    private final List<Door> doors = new ArrayList<>();
    private final List<double[]> walls = new ArrayList<>();

    private final EventHandler<Event> aimHandler = this::aim;

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
    Follow(final IGameContext gameContext, final Stats stats, final FollowMode mode, final FollowLevel level,
           final FollowEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.level = level;
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
        final double smaller = Math.min(width, height);
        final double gap = smaller * 0.03;
        final double top = height * 0.11;
        cell = Math.min((width - 2 * gap) / level.getColumns(), (height - top - gap) / level.getLines());
        boardX = (width - cell * level.getColumns()) / 2;
        boardY = top + (height - top - gap - cell * level.getLines()) / 2;
        thickness = Math.max(6, cell * 0.14);

        final Rectangle grass = new Rectangle(0, 0, width, height);
        grass.setFill(Color.rgb(150, 196, 120));
        final Rectangle floor = new Rectangle(boardX, boardY, cell * level.getColumns(), cell * level.getLines());
        floor.setFill(Color.rgb(244, 232, 200));
        floor.setArcWidth(cell * 0.3);
        floor.setArcHeight(cell * 0.3);
        doorsLayer = new Group();
        itemsLayer = new Group();
        wallsLayer = new Group();
        board = new Group(doorsLayer, itemsLayer, wallsLayer);
        biboule = new ImageView(new Image(BIBOULE));
        biboule.setPreserveRatio(true);
        biboule.setFitWidth(cell * PICTURE);
        biboule.setFitHeight(cell * PICTURE);
        pointer = new Circle(Math.max(5, cell * 0.05));
        pointer.setFill(Color.rgb(255, 255, 255, 0.55));
        pointer.setStyle("-fx-stroke: -gp-accent;");
        pointer.setStrokeWidth(2);
        pointer.setVisible(false);
        hud = new Group();
        message = new Group();
        // The whole screen receives the gaze and the mouse, as the setting of the inputs allows
        area = new Rectangle(0, 0, width, height);
        area.setFill(Color.TRANSPARENT);
        layer = new Group(grass, floor, board, biboule, pointer, hud, message, area);
        for (final Node node : List.of(grass, floor, board, biboule, pointer, hud, message)) {
            node.setMouseTransparent(true);
        }
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addEventFilter(area);
        area.addEventFilter(GazeEvent.ANY, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_MOVED, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_DRAGGED, aimHandler);
        gameContext.getGazeDeviceManager().addStats(stats);

        maps = 0;
        newMap();
        if (!manual) {
            loop.start();
        }
    }

    private void aim(final Event event) {
        if (event instanceof GazeEvent gaze && (gaze.getEventType() == GazeEvent.GAZE_MOVED
            || gaze.getEventType() == GazeEvent.GAZE_ENTERED)) {
            aimAt(gaze.getX(), gaze.getY());
            pointer.setCenterX(gaze.getX());
            pointer.setCenterY(gaze.getY());
            pointer.setVisible(true);
        } else if (event instanceof MouseEvent mouse) {
            aimAt(mouse.getX(), mouse.getY());
            // The mouse has its own pointer
            pointer.setVisible(false);
        }
    }

    /**
     * The place looked at (or of the mouse) on the screen, where the Biboule goes.
     */
    void aimAt(final double newX, final double newY) {
        aimX = newX;
        aimY = newY;
        aimed = true;
    }

    // --- The maps ---

    /**
     * A new map: a labyrinth, the Biboule in a corner, then the coins, or the doors, their keys and the treasure.
     */
    private void newMap() {
        items.clear();
        doors.clear();
        walls.clear();
        doorsLayer.getChildren().clear();
        itemsLayer.getChildren().clear();
        wallsLayer.getChildren().clear();
        message.getChildren().clear();
        coinsTaken = 0;

        final int[] start;
        if (mode == FollowMode.COINS) {
            maze = new Maze(level.getLines(), level.getColumns(), random);
            maze.openMore(level.getLoops(), random);
            start = corner();
            placeCoins(start);
        } else {
            start = placeKeys();
        }
        drawWalls();
        x = boxX(start[1]);
        y = boxY(start[0]);
        placeBiboule();

        elapsed = 0;
        board.setOpacity(0);
        phase = Phase.APPEAR;
        stats.incrementNumberOfGoalsToReach();
        showHud();
    }

    private int[] corner() {
        final int corner = random.nextInt(4);
        return new int[]{corner < 2 ? 0 : level.getLines() - 1, corner % 2 == 0 ? 0 : level.getColumns() - 1};
    }

    private void placeCoins(final int[] start) {
        final List<int[]> boxes = new ArrayList<>();
        for (int i = 0; i < level.getLines(); i++) {
            for (int j = 0; j < level.getColumns(); j++) {
                if (i != start[0] || j != start[1]) {
                    boxes.add(new int[]{i, j});
                }
            }
        }
        final int count = Math.min(level.getCoins(), boxes.size());
        for (int k = 0; k < count; k++) {
            final int[] box = boxes.remove(random.nextInt(boxes.size()));
            addItem(Kind.COIN, -1, box);
        }
    }

    /**
     * A labyrinth without loops, the treasure far from the corner of the Biboule, the doors along the only way to it, and
     * each key before its door (but after the doors before it), away from the way if possible.
     *
     * @return the box of the start
     */
    private int[] placeKeys() {
        final int count = level.getDoors();
        int[] start;
        List<int[]> way;
        do {
            maze = new Maze(level.getLines(), level.getColumns(), random);
            start = corner();
            final int[][] distances = maze.distancesFrom(start[0], start[1]);
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
            final int[] chest = places.get(random.nextInt(places.size()));
            way = wayTo(distances, chest);
        } while (way.size() - 1 < count + 1);

        final int length = way.size() - 1;
        final List<int[][]> doorEdges = new ArrayList<>();
        for (int j = 1; j <= count; j++) {
            final int index = j * length / (count + 1);
            doorEdges.add(new int[][]{way.get(index), way.get(index + 1)});
        }
        final Set<String> onWay = new HashSet<>();
        way.forEach(box -> onWay.add(box[0] + ":" + box[1]));
        final int[] chest = way.get(length);
        boolean[][] before = new boolean[level.getLines()][level.getColumns()];
        before[start[0]][start[1]] = true;
        for (int j = 0; j < count; j++) {
            final boolean[][] reached = reach(start, doorEdges.subList(j, count));
            final List<int[]> off = new ArrayList<>();
            final List<int[]> on = new ArrayList<>();
            for (int i = 0; i < level.getLines(); i++) {
                for (int c = 0; c < level.getColumns(); c++) {
                    if (reached[i][c] && !before[i][c] && !(i == chest[0] && c == chest[1])) {
                        (onWay.contains(i + ":" + c) ? on : off).add(new int[]{i, c});
                    }
                }
            }
            final List<int[]> choices = off.isEmpty() ? on : off;
            addItem(Kind.KEY, j, choices.get(random.nextInt(choices.size())));
            addDoor(j, doorEdges.get(j)[0], doorEdges.get(j)[1]);
            before = reached;
        }
        addItem(Kind.CHEST, -1, chest);
        return start;
    }

    /**
     * @return the boxes of the shortest way from the start (distance 0) to a box
     */
    private List<int[]> wayTo(final int[][] distances, final int[] goal) {
        final List<int[]> way = new ArrayList<>();
        int[] box = goal;
        way.add(0, box);
        while (distances[box[0]][box[1]] > 0) {
            for (final Maze.Direction direction : Maze.Direction.values()) {
                final int line = box[0] + direction.lines;
                final int column = box[1] + direction.columns;
                if (maze.canGo(box[0], box[1], direction)
                    && distances[line][column] == distances[box[0]][box[1]] - 1) {
                    box = new int[]{line, column};
                    break;
                }
            }
            way.add(0, box);
        }
        return way;
    }

    /**
     * @return the boxes reached from the start, some passages closed
     */
    private boolean[][] reach(final int[] start, final List<int[][]> closed) {
        final boolean[][] reached = new boolean[level.getLines()][level.getColumns()];
        final Deque<int[]> queue = new ArrayDeque<>();
        reached[start[0]][start[1]] = true;
        queue.add(start);
        while (!queue.isEmpty()) {
            final int[] box = queue.poll();
            for (final Maze.Direction direction : Maze.Direction.values()) {
                final int line = box[0] + direction.lines;
                final int column = box[1] + direction.columns;
                if (maze.canGo(box[0], box[1], direction) && !reached[line][column]
                    && !isClosed(closed, box[0], box[1], line, column)) {
                    reached[line][column] = true;
                    queue.add(new int[]{line, column});
                }
            }
        }
        return reached;
    }

    private static boolean isClosed(final List<int[][]> closed, final int l1, final int c1, final int l2,
                                    final int c2) {
        for (final int[][] edge : closed) {
            if ((edge[0][0] == l1 && edge[0][1] == c1 && edge[1][0] == l2 && edge[1][1] == c2)
                || (edge[0][0] == l2 && edge[0][1] == c2 && edge[1][0] == l1 && edge[1][1] == c1)) {
                return true;
            }
        }
        return false;
    }

    private double boxX(final int column) {
        return boardX + (column + 0.5) * cell;
    }

    private double boxY(final int line) {
        return boardY + (line + 0.5) * cell;
    }

    // --- Drawing ---

    /**
     * Every wall still closed between two boxes, and the hedge around the map.
     */
    private void drawWalls() {
        final int lines = level.getLines();
        final int columns = level.getColumns();
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < columns; j++) {
                if (j < columns - 1 && !maze.canGo(i, j, Maze.Direction.RIGHT)) {
                    final double wallX = boardX + (j + 1) * cell;
                    addWall(wallX, boardY + i * cell, wallX, boardY + (i + 1) * cell);
                }
                if (i < lines - 1 && !maze.canGo(i, j, Maze.Direction.DOWN)) {
                    final double wallY = boardY + (i + 1) * cell;
                    addWall(boardX + j * cell, wallY, boardX + (j + 1) * cell, wallY);
                }
            }
        }
        final double right = boardX + columns * cell;
        final double bottom = boardY + lines * cell;
        addWall(boardX, boardY, right, boardY);
        addWall(boardX, bottom, right, bottom);
        addWall(boardX, boardY, boardX, bottom);
        addWall(right, boardY, right, bottom);
    }

    /**
     * A hedge from a point to another, with round ends (as in the Labyrinth), which stops the Biboule.
     */
    private void addWall(final double x1, final double y1, final double x2, final double y2) {
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
        wallsLayer.getChildren().add(new Group(dark, light));
        walls.add(new double[]{dark.getX(), dark.getY(), dark.getX() + dark.getWidth(),
            dark.getY() + dark.getHeight()});
    }

    /**
     * A door of a color in the passage between two boxes: a wooden bar of its color with a keyhole, across the passage.
     */
    private void addDoor(final int color, final int[] from, final int[] to) {
        final boolean across = from[0] == to[0];
        final double middleX = across ? boardX + Math.max(from[1], to[1]) * cell : boxX(from[1]);
        final double middleY = across ? boxY(from[0]) : boardY + Math.max(from[0], to[0]) * cell;
        final double depth = cell * 0.2;
        final double span = cell;
        final double w = across ? depth : span;
        final double h = across ? span : depth;
        final Rectangle bar = new Rectangle(middleX - w / 2, middleY - h / 2, w, h);
        bar.setArcWidth(depth * 0.5);
        bar.setArcHeight(depth * 0.5);
        bar.setFill(DOOR_COLORS[color]);
        bar.setStroke(DOOR_COLORS[color].darker());
        bar.setStrokeWidth(Math.max(2, depth * 0.1));
        final Color hole = Color.rgb(60, 40, 30);
        final Circle round = new Circle(middleX, middleY - depth * 0.1, depth * 0.17, hole);
        final Polygon foot = new Polygon(middleX - depth * 0.1, middleY - depth * 0.05, middleX + depth * 0.1,
            middleY - depth * 0.05, middleX + depth * 0.14, middleY + depth * 0.3, middleX - depth * 0.14,
            middleY + depth * 0.3);
        foot.setFill(hole);
        final Group node = new Group(bar, round, foot);
        doorsLayer.getChildren().add(node);
        doors.add(new Door(color, node, new double[]{bar.getX(), bar.getY(), bar.getX() + w, bar.getY() + h}));
    }

    private void addItem(final Kind kind, final int color, final int[] box) {
        final double itemX = boxX(box[1]);
        final double itemY = boxY(box[0]);
        final Group node = switch (kind) {
            case COIN -> coin(cell * 0.17);
            case KEY -> key(cell * 0.5, DOOR_COLORS[color]);
            case CHEST -> chest(cell * 0.62);
        };
        node.setLayoutX(itemX);
        node.setLayoutY(itemY);
        itemsLayer.getChildren().add(node);
        items.add(new Item(kind, color, itemX, itemY, node));
    }

    private static Group coin(final double r) {
        final Circle outer = new Circle(r, Color.rgb(255, 198, 40));
        outer.setStroke(Color.rgb(196, 132, 0));
        outer.setStrokeWidth(Math.max(2, r * 0.12));
        final Circle inner = new Circle(r * 0.66, Color.TRANSPARENT);
        inner.setStroke(Color.rgb(232, 162, 20));
        inner.setStrokeWidth(Math.max(1.5, r * 0.08));
        final Circle shine = new Circle(-r * 0.32, -r * 0.32, r * 0.16, Color.rgb(255, 255, 255, 0.75));
        return new Group(outer, inner, shine);
    }

    private static Group key(final double s, final Color color) {
        final Color edge = color.darker();
        final Circle head = new Circle(-s * 0.26, 0, s * 0.2, color);
        head.setStroke(edge);
        head.setStrokeWidth(Math.max(2, s * 0.04));
        final Circle hole = new Circle(-s * 0.26, 0, s * 0.08, Color.rgb(244, 232, 200));
        final Rectangle shaft = new Rectangle(-s * 0.08, -s * 0.055, s * 0.56, s * 0.11);
        final Rectangle tooth1 = new Rectangle(s * 0.26, s * 0.03, s * 0.07, s * 0.15);
        final Rectangle tooth2 = new Rectangle(s * 0.39, s * 0.03, s * 0.07, s * 0.2);
        for (final Rectangle part : List.of(shaft, tooth1, tooth2)) {
            part.setFill(color);
            part.setStroke(edge);
            part.setStrokeWidth(Math.max(1.5, s * 0.03));
        }
        final Group key = new Group(shaft, tooth1, tooth2, head, hole);
        key.setRotate(-25);
        return key;
    }

    private static Group chest(final double s) {
        final Color wood = Color.rgb(156, 96, 44);
        final Color dark = Color.rgb(96, 56, 22);
        final Color gold = Color.rgb(244, 192, 48);
        final Rectangle body = new Rectangle(-s * 0.42, -s * 0.08, s * 0.84, s * 0.44);
        body.setFill(wood);
        final Rectangle lid = new Rectangle(-s * 0.44, -s * 0.38, s * 0.88, s * 0.32);
        lid.setFill(wood.brighter());
        lid.setArcWidth(s * 0.3);
        lid.setArcHeight(s * 0.3);
        for (final Rectangle part : List.of(body, lid)) {
            part.setStroke(dark);
            part.setStrokeWidth(Math.max(2, s * 0.04));
        }
        final Rectangle band1 = new Rectangle(-s * 0.3, -s * 0.38, s * 0.08, s * 0.74);
        final Rectangle band2 = new Rectangle(s * 0.22, -s * 0.38, s * 0.08, s * 0.74);
        final Rectangle lock = new Rectangle(-s * 0.07, -s * 0.14, s * 0.14, s * 0.16);
        for (final Rectangle part : List.of(band1, band2, lock)) {
            part.setFill(gold);
            part.setStroke(gold.darker());
            part.setStrokeWidth(Math.max(1, s * 0.02));
        }
        return new Group(body, lid, band1, band2, lock);
    }

    private void placeBiboule() {
        biboule.setLayoutX(x - biboule.getFitWidth() / 2);
        biboule.setLayoutY(y - biboule.getFitHeight() / 2);
    }

    private void showHud() {
        final double size = height * 0.03;
        final Group count = GameStyle.pill(gameContext.getTranslator().translate("FollowMaps") + " " + maps
            + (end.getMaps() > 0 ? " / " + end.getMaps() : ""), size);
        count.setLayoutX(height * 0.03);
        count.setLayoutY(height * 0.02);
        hud.getChildren().setAll(count);
        if (mode == FollowMode.COINS) {
            final Group coins = GameStyle.pill(gameContext.getTranslator().translate("FollowCoinsTaken") + " "
                + coinsTaken + " / " + (int) items.stream().filter(item -> item.kind == Kind.COIN).count(), size);
            coins.setLayoutX((width - coins.getBoundsInLocal().getWidth()) / 2);
            coins.setLayoutY(height * 0.02);
            hud.getChildren().add(coins);
        }
    }

    /**
     * "Bravo !" with a star, in the middle of the map.
     */
    private void showWell() {
        final double r = height * 0.045;
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("FollowWell"), height * 0.045);
        final double pillWidth = pill.getBoundsInLocal().getWidth();
        final double pillHeight = pill.getBoundsInLocal().getHeight();
        final double gap = r * 0.4;
        final double total = pillWidth + 2 * r + gap;
        final double startX = width / 2 - total / 2;
        final double cy = boardY + cell * level.getLines() / 2;
        final Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            final double a = -Math.PI / 2 + i * Math.PI / 5;
            final double d = i % 2 == 0 ? r : r * 0.45;
            star.getPoints().addAll(startX + r + d * Math.cos(a), cy + r * 0.08 + d * Math.sin(a));
        }
        star.setFill(Color.rgb(255, 200, 40));
        star.setStroke(Color.rgb(200, 120, 0));
        star.setStrokeWidth(Math.max(2, r * 0.07));
        pill.setLayoutX(startX + total - pillWidth);
        pill.setLayoutY(cy - pillHeight / 2);
        message.getChildren().setAll(star, pill);
    }

    // --- The loop ---

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        openDoors(seconds);
        time += seconds;
        pulse();
        switch (phase) {
            case APPEAR -> {
                elapsed += seconds;
                board.setOpacity(Math.min(1, elapsed / APPEAR));
                if (elapsed >= Math.max(APPEAR, configuration.getGameStartDelay() / 1000.0)) {
                    board.setOpacity(1);
                    phase = Phase.PLAY;
                    stats.notifyNewRoundReady();
                }
            }
            case PLAY -> {
                move(seconds * Math.max(0.1, configuration.getAnimationSpeedRatio()));
                takeItems();
            }
            case JOY -> {
                elapsed += seconds;
                final double jump = Math.abs(Math.sin(Math.min(1, elapsed / JOY) * Math.PI * 3)) * cell * 0.15;
                biboule.setLayoutY(y - biboule.getFitHeight() / 2 - jump);
                if (elapsed >= JOY) {
                    if (end.getMaps() > 0 && maps >= end.getMaps()) {
                        finish();
                    } else {
                        newMap();
                    }
                }
            }
            default -> {
            }
        }
    }

    /**
     * Moves the Biboule towards the place looked at, at its speed, in small steps: each way (across, along) on its own,
     * so that it slides along a hedge; near the end of a hedge, it is pushed round it.
     */
    private void move(final double seconds) {
        if (!aimed) {
            return;
        }
        final double dx = aimX - x;
        final double dy = aimY - y;
        final double distance = Math.hypot(dx, dy);
        if (distance < 1) {
            return;
        }
        final double go = Math.min(distance, SPEED * Math.min(width, height) * seconds);
        final double half = BODY * cell;
        final int steps = Math.max(1, (int) Math.ceil(go / (half * 0.5)));
        for (int k = 0; k < steps; k++) {
            slide(true, dx / distance * go / steps);
            slide(false, dy / distance * go / steps);
        }
        placeBiboule();
    }

    /**
     * Moves the Biboule one way (across or along), up to the hedges and the closed doors.
     */
    private void slide(final boolean across, final double move) {
        if (move == 0) {
            return;
        }
        final double half = BODY * cell;
        final double newX = across ? x + move : x;
        final double newY = across ? y : y + move;
        final List<double[]> hit = blocking(newX, newY);
        if (hit.isEmpty()) {
            x = newX;
            y = newY;
            return;
        }
        // Near the end of a hedge, pushed round it (the other way)
        if (hit.size() == 1) {
            final double[] box = hit.get(0);
            final double low = across ? (y + half) - box[1] : (x + half) - box[0];
            final double high = across ? box[3] - (y - half) : box[2] - (x - half);
            final double nudge = Math.abs(move);
            if (low < half * 0.8) {
                tryAt(across ? x : x - Math.min(nudge, low + EPSILON), across ? y - Math.min(nudge, low + EPSILON) : y);
            } else if (high < half * 0.8) {
                tryAt(across ? x : x + Math.min(nudge, high + EPSILON), across ? y + Math.min(nudge, high + EPSILON) : y);
            }
        }
        // Up to the nearest obstacle
        double limit = across ? newX : newY;
        for (final double[] box : blocking(newX, newY)) {
            if (across) {
                limit = move > 0 ? Math.min(limit, box[0] - half - EPSILON) : Math.max(limit, box[2] + half + EPSILON);
            } else {
                limit = move > 0 ? Math.min(limit, box[1] - half - EPSILON) : Math.max(limit, box[3] + half + EPSILON);
            }
        }
        final double testX = across ? limit : x;
        final double testY = across ? y : limit;
        final boolean forward = across ? (limit - x) * move > 0 : (limit - y) * move > 0;
        if (forward && blocking(testX, testY).isEmpty()) {
            x = testX;
            y = testY;
        }
    }

    private void tryAt(final double newX, final double newY) {
        if (blocking(newX, newY).isEmpty()) {
            x = newX;
            y = newY;
        }
    }

    /**
     * @return the hedges and the closed doors the square of the Biboule would overlap at a place
     */
    private List<double[]> blocking(final double atX, final double atY) {
        final double half = BODY * cell;
        final List<double[]> hit = new ArrayList<>();
        for (final double[] box : walls) {
            if (overlaps(box, atX, atY, half)) {
                hit.add(box);
            }
        }
        for (final Door door : doors) {
            if (!door.open && overlaps(door.box, atX, atY, half)) {
                hit.add(door.box);
            }
        }
        return hit;
    }

    private static boolean overlaps(final double[] box, final double atX, final double atY, final double half) {
        return atX + half > box[0] && atX - half < box[2] && atY + half > box[1] && atY - half < box[3];
    }

    /**
     * Takes the items the Biboule reaches: a coin, a key (its door opens), the treasure.
     */
    private void takeItems() {
        for (final Item item : items) {
            if (item.taken || Math.hypot(item.x - x, item.y - y) > REACH * cell) {
                continue;
            }
            item.taken = true;
            itemsLayer.getChildren().remove(item.node);
            switch (item.kind) {
                case COIN -> {
                    coinsTaken++;
                    gameContext.getSoundManager().add(SOUNDS + "coin.wav");
                    showHud();
                    if (items.stream().allMatch(other -> other.taken)) {
                        mapDone();
                        return;
                    }
                }
                case KEY -> {
                    gameContext.getSoundManager().add(SOUNDS + "key.wav");
                    gameContext.getSoundManager().add(SOUNDS + "door.wav");
                    doors.stream().filter(door -> door.color == item.color).forEach(door -> door.open = true);
                }
                case CHEST -> {
                    mapDone();
                    return;
                }
            }
        }
    }

    /**
     * The doors opened shrink, then disappear.
     */
    private void openDoors(final double seconds) {
        for (final Door door : doors) {
            if (door.open && door.node.isVisible()) {
                door.opening = Math.min(1, door.opening + seconds / DOOR_OPENING);
                door.node.setOpacity(1 - door.opening);
                door.node.setScaleX(1 - 0.6 * door.opening);
                door.node.setScaleY(1 - 0.6 * door.opening);
                if (door.opening >= 1) {
                    door.node.setVisible(false);
                }
            }
        }
    }

    /**
     * The keys and the treasure beat softly, to be seen.
     */
    private void pulse() {
        final double beat = 1 + 0.07 * Math.sin(time * Math.PI * 2 * 0.8);
        for (final Item item : items) {
            if (item.kind != Kind.COIN && !item.taken) {
                item.node.setScaleX(beat);
                item.node.setScaleY(beat);
            }
        }
    }

    private void mapDone() {
        maps++;
        stats.incrementNumberOfGoalsReached();
        gameContext.getSoundManager().add(WIN_SOUND);
        showHud();
        showWell();
        elapsed = 0;
        phase = Phase.JOY;
    }

    private void finish() {
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
     * For the tests: the middle of a box on the screen.
     */
    double[] boxCenter(final int line, final int column) {
        return new double[]{boxX(column), boxY(line)};
    }

    /**
     * For the tests: whether the square of the Biboule overlaps a hedge or a closed door at its place.
     */
    boolean isInWall() {
        return !blocking(x, y).isEmpty();
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (area != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(area);
            area = null;
        }
        items.clear();
        doors.clear();
        walls.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A coin, a key or the treasure, in the middle of a box.
     */
    static final class Item {
        @Getter
        private final Kind kind;
        @Getter
        private final int color;
        @Getter
        private final double x;
        @Getter
        private final double y;
        private final Group node;
        @Getter
        private boolean taken;

        Item(final Kind kind, final int color, final double x, final double y, final Group node) {
            this.kind = kind;
            this.color = color;
            this.x = x;
            this.y = y;
            this.node = node;
        }
    }

    /**
     * A door of a color, closed until its key is taken.
     */
    static final class Door {
        @Getter
        private final int color;
        private final Group node;
        private final double[] box;
        @Getter
        private boolean open;
        private double opening;

        Door(final int color, final Group node, final double[] box) {
            this.color = color;
            this.node = node;
            this.box = box;
        }
    }
}
