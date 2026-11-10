package net.gazeplay.games.trainSwitches;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "Aiguillages" (Train Switches; reviewed on 10/11/2026, maintainer's rules): trains of colors come out of a tunnel, and
 * the player sends each one to the station of its color (the same color and the same symbol) by changing the switches
 * with fixations. A switch shows its two ways, the way taken bright, and a fixation on it changes the way. A train at
 * the right station is a goal, at a wrong one a mistake. After 5 trains, a new circuit of the level comes, another one
 * than the last; the game ends after 5, 10 or 20 trains ("Bravo"), or never. The trains come one at a time, or with a
 * pause before each switch (until "Resume" is fixed), or at a steady pace.
 * <p>
 * After the maintainer's trial (10/11/2026): a train goes at most one box during a fixation of the settings and a
 * reaction, and the next train is shown on the tunnel with the time left before it leaves (two fixations and a second
 * to get ready). The fixation length is read at each step, so a change in the menu of the game counts at once.
 * <p>
 * Everything is drawn by the game, seen from above. One loop, in real seconds: the trains move at the speed of the
 * settings, the fixations last the length of the settings (read at each step), nothing can be chosen before the delay
 * of the settings, and nothing moves while the menu of the game is open. (Before the review: 3 circuits, pictures of
 * unknown source, trains turned on their side in the bends, a switch showing one way only, the former progress
 * indicator, timers out of the thread of JavaFX, the speed of the settings changed by the game, a number of trains
 * typed in the window of the variants.)
 */
@Slf4j
public class TrainSwitches implements GameLifeCycle {

    /**
     * In boxes per second, at the speed 1 of the settings: the fastest trains. A train goes at most one box during a
     * fixation of the settings and a reaction (maintainer's request after his trial of 10/11/2026: with the fixation of
     * 2 seconds of the settings, the trains went almost two boxes during one fixation).
     */
    static final double SPEED = 0.9;
    /**
     * In seconds: the reaction added to the fixation for one box.
     */
    static final double REACTION = 0.5;
    /**
     * In seconds (at the speed 1): to get ready before a train leaves (the first one of a circuit, or the next one when
     * they come one at a time), added to two fixations of the settings; the next train and the time left are shown on
     * the tunnel.
     */
    static final double READY = 1.0;
    /**
     * In boxes along the rails: between two trains at a steady pace or with pauses.
     */
    static final double INTERVAL = 3.6;
    /**
     * In boxes: where a train stops before a switch, with pauses (the middle of the engine; its front stays out of the
     * disc of the switch).
     */
    static final double PAUSE_BEFORE = 0.85;
    /**
     * In boxes, along the rails from the tunnel: where a train starts, hidden in the tunnel.
     */
    static final double START = -1.6;
    /**
     * In seconds: the check or the cross on a station, the joy after the last train of a circuit.
     */
    static final double MARK = 1.6;
    static final double JOY = 1.2;
    /**
     * The trains of a circuit before the next one.
     */
    static final int PER_CIRCUIT = 5;

    /**
     * In boxes: the length of the engine and of its wagon, the gap between them.
     */
    private static final double ENGINE = 0.62;
    private static final double WAGON = 0.5;
    private static final double GAP = 0.08;

    private static final String SOUNDS = "data/trainSwitches/sounds/";
    private static final Color MEADOW = Color.rgb(196, 226, 160);
    private static final Color BALLAST = Color.rgb(216, 204, 176);
    private static final Color SLEEPER = Color.rgb(140, 102, 70);
    private static final Color RAIL = Color.rgb(112, 114, 126);
    private static final Color WAY = Color.rgb(92, 64, 44);

    enum Phase { WAIT, PLAY, CHANGE, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final TrainSwitchesLevel level;
    @Getter
    private final TrainSwitchesRhythm rhythm;
    @Getter
    private final TrainSwitchesEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private Circuit circuit;
    private int lastCircuit = -1;
    private final List<TrainColor> stationColors = new ArrayList<>();
    private TrainColor lastColor;
    /**
     * The color of the next train, shown on the tunnel.
     */
    @Getter
    private TrainColor nextColor;
    private Preview preview;
    private Circle hill;
    private final Map<String, Integer> ways = new HashMap<>();
    @Getter
    private final List<Train> trains = new ArrayList<>();
    /**
     * Trains sent, arrived and arrived at the right station, in the game and in the circuit.
     */
    @Getter
    private int sent;
    @Getter
    private int arrived;
    @Getter
    private int right;
    private int sentHere;
    private int arrivedHere;
    private int quota;
    private double clock;
    private double elapsed;
    @Getter
    private double waiting;
    /**
     * The train stopped before a switch, with pauses; null when the trains move.
     */
    @Getter
    private Train paused;

    private double width;
    private double height;
    @Getter
    private double cell;
    private double boardX;
    private double boardY;
    private double resumeX;
    private double resumeY;
    private double resumeSize;

    private Group layer;
    private Group ground;
    private Group railsLayer;
    private Group switchesLayer;
    private Group trainsLayer;
    private Group stationsLayer;
    private Group tunnelLayer;
    private Group marksLayer;
    private Group hud;
    private Circle halo;
    private FixationRing ring;
    private Group resumeButton;
    private final Map<String, SwitchView> switchViews = new HashMap<>();
    private final List<Mark> marks = new ArrayList<>();
    private final List<Node> gazeNodes = new ArrayList<>();

    /**
     * What is looked at (a switch or the resume button), and how far its fixation is.
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
    TrainSwitches(final IGameContext gameContext, final Stats stats, final TrainSwitchesLevel level,
                  final TrainSwitchesRhythm rhythm, final TrainSwitchesEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.level = level;
        this.rhythm = rhythm;
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

        ground = new Group();
        ground.setMouseTransparent(true);
        railsLayer = new Group();
        railsLayer.setMouseTransparent(true);
        switchesLayer = new Group();
        trainsLayer = new Group();
        trainsLayer.setMouseTransparent(true);
        stationsLayer = new Group();
        stationsLayer.setMouseTransparent(true);
        tunnelLayer = new Group();
        tunnelLayer.setMouseTransparent(true);
        marksLayer = new Group();
        marksLayer.setMouseTransparent(true);
        halo = new Circle();
        halo.setFill(null);
        halo.setStroke(Color.rgb(255, 196, 50));
        halo.setMouseTransparent(true);
        halo.setVisible(false);
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        resumeButton = new Group();
        layer = new Group(ground, railsLayer, switchesLayer, trainsLayer, stationsLayer, tunnelLayer, marksLayer, halo,
            resumeButton, ring, hud);
        gameContext.getChildren().add(layer);
        listen(resumeButton, () -> lookAt(resumeButton), () -> lookAway(resumeButton));

        gameContext.getGazeDeviceManager().addStats(stats);
        sent = 0;
        arrived = 0;
        right = 0;
        newCircuit();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * Places the grid of the circuit in the middle of the screen, under the band of the score, with room at each side
     * for the resume button.
     */
    private void layOut() {
        final double top = height * 0.11;
        final double margin = Math.min(width, height) * 0.04;
        final double availableHeight = height - top - margin;
        // A tunnel at the top or at the bottom: room for its hill and the next train above or under the grid
        final int[] direction = Circuit.startDirection(circuit.getFirst());
        final double extra = direction[1] != 0 ? Preview.BACK + 0.4 - 0.5 : 0;
        cell = Math.min(availableHeight / (circuit.getLines() + extra),
            (width - 2 * margin) / (circuit.getColumns() + 2.8));
        boardX = (width - cell * circuit.getColumns()) / 2;
        boardY = top + (availableHeight - cell * (circuit.getLines() + extra)) / 2 + (direction[1] > 0 ? cell * extra
            : 0);
        resumeSize = cell * 1.1;
        resumeX = boardX + cell * circuit.getColumns() + cell * 0.15 + resumeSize / 2;
        resumeY = boardY + cell * circuit.getLines() / 2;
    }

    private double sx(final double x) {
        return boardX + (x + 0.5) * cell;
    }

    private double sy(final double y) {
        return boardY + (y + 0.5) * cell;
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
     * A new circuit of the level, another one than the last, with the colors of its stations drawn at random.
     */
    private void newCircuit() {
        final List<Circuit> all = Circuit.all().get(level.getFileName());
        int index = random.nextInt(all.size() - (lastCircuit < 0 ? 0 : 1));
        if (lastCircuit >= 0 && index >= lastCircuit) {
            index++;
        }
        lastCircuit = index;
        circuit = all.get(index);

        final List<TrainColor> colors = new ArrayList<>(Arrays.asList(TrainColor.values()));
        stationColors.clear();
        for (int i = 0; i < circuit.getStations().size(); i++) {
            stationColors.add(colors.remove(random.nextInt(colors.size())));
        }
        lastColor = null;
        ways.clear();
        circuit.getJunctions().keySet().forEach(name -> ways.put(name, 0));

        switchViews.values().forEach(view -> forget(view.zone));
        switchViews.clear();
        trains.clear();
        marks.clear();
        for (final Group group : new Group[]{ground, railsLayer, switchesLayer, trainsLayer, stationsLayer, tunnelLayer,
            marksLayer, resumeButton}) {
            group.getChildren().clear();
        }
        layOut();
        drawGround();
        drawCircuit();
        drawResumeButton();
        resumeButton.setVisible(false);
        halo.setVisible(false);

        sentHere = 0;
        arrivedHere = 0;
        quota = end.getTrains() > 0 ? Math.min(PER_CIRCUIT, end.getTrains() - arrived) : PER_CIRCUIT;
        drawNextColor();
        clock = 0;
        paused = null;
        looked = null;
        progress = 0;
        ring.stop();
        elapsed = 0;
        waiting = configuration.getGameStartDelay() / 1000.0;
        phase = Phase.WAIT;
        showHud();
        preview.update();
    }

    private void drawGround() {
        final Rectangle meadow = new Rectangle(0, 0, width, height);
        meadow.setFill(MEADOW);
        ground.getChildren().add(meadow);
        // Tufts of grass, always at the same places
        final java.util.Random tufts = new java.util.Random(7);
        for (int i = 0; i < 40; i++) {
            final double x = tufts.nextDouble() * width;
            final double y = tufts.nextDouble() * height;
            final double size = Math.min(width, height) * (0.008 + tufts.nextDouble() * 0.008);
            for (int blade = -1; blade <= 1; blade++) {
                final Polygon leaf = new Polygon(x + blade * size * 0.7 - size * 0.25, y, x + blade * size * 0.7
                    + size * 0.25, y, x + blade * size * 1.1, y - size * (blade == 0 ? 2.2 : 1.6));
                leaf.setFill(Color.rgb(160, 200, 120));
                ground.getChildren().add(leaf);
            }
        }
    }

    private void drawCircuit() {
        for (final Circuit.Stretch stretch : circuit.getStretches()) {
            drawRails(stretch);
        }
        for (final Circuit.Junction junction : circuit.getJunctions().values()) {
            final SwitchView view = new SwitchView(junction);
            switchViews.put(junction.getName(), view);
            switchesLayer.getChildren().add(view.node);
            listen(view.zone, () -> lookAt(view), () -> lookAway(view));
        }
        for (int station = 0; station < circuit.getStations().size(); station++) {
            stationsLayer.getChildren().add(station(station));
        }
        tunnelLayer.getChildren().add(tunnel());
        final int[] direction = Circuit.startDirection(circuit.getFirst());
        preview = new Preview(sx(circuit.getTunnel()[0] - direction[0] * Preview.BACK),
            sy(circuit.getTunnel()[1] - direction[1] * Preview.BACK));
        tunnelLayer.getChildren().add(preview.node);
    }

    /**
     * Rails seen from above: the ballast, the sleepers, and two rails, the corners rounded.
     */
    private void drawRails(final Circuit.Stretch stretch) {
        final List<double[]> points = new Route(stretch.boxes()).sample(0.05);
        final Polyline ballast = new Polyline();
        final Polyline sleepers = new Polyline();
        final Polyline left = new Polyline();
        final Polyline rightRail = new Polyline();
        final double offset = 0.11;
        for (final double[] point : points) {
            final double angle = Math.toRadians(point[2]);
            final double nx = -Math.sin(angle);
            final double ny = Math.cos(angle);
            ballast.getPoints().addAll(sx(point[0]), sy(point[1]));
            sleepers.getPoints().addAll(sx(point[0]), sy(point[1]));
            left.getPoints().addAll(sx(point[0] + offset * nx), sy(point[1] + offset * ny));
            rightRail.getPoints().addAll(sx(point[0] - offset * nx), sy(point[1] - offset * ny));
        }
        ballast.setStroke(BALLAST);
        ballast.setStrokeWidth(cell * 0.44);
        ballast.setStrokeLineJoin(StrokeLineJoin.ROUND);
        ballast.setStrokeLineCap(StrokeLineCap.ROUND);
        sleepers.setStroke(SLEEPER);
        sleepers.setStrokeWidth(cell * 0.36);
        sleepers.setStrokeLineCap(StrokeLineCap.BUTT);
        sleepers.getStrokeDashArray().addAll(cell * 0.07, cell * 0.11);
        for (final Polyline rail : new Polyline[]{left, rightRail}) {
            rail.setStroke(RAIL);
            rail.setStrokeWidth(Math.max(2, cell * 0.04));
            rail.setStrokeLineJoin(StrokeLineJoin.ROUND);
        }
        railsLayer.getChildren().addAll(ballast, sleepers, left, rightRail);
    }

    /**
     * A station seen from above: a platform, its roof of the color, and the symbol of the color in the middle.
     */
    private Group station(final int station) {
        final int[] box = circuit.getStations().get(station);
        final TrainColor color = stationColors.get(station);
        final double size = cell * 0.84;
        final Rectangle platform = new Rectangle(-size / 2, -size / 2, size, size);
        platform.setArcWidth(size * 0.24);
        platform.setArcHeight(size * 0.24);
        platform.setFill(Color.rgb(236, 230, 216));
        platform.setStroke(Color.rgb(160, 150, 132));
        platform.setStrokeWidth(Math.max(1.5, cell * 0.025));
        final double roofSize = size * 0.8;
        final Rectangle roof = new Rectangle(-roofSize / 2, -roofSize / 2, roofSize, roofSize);
        roof.setArcWidth(roofSize * 0.3);
        roof.setArcHeight(roofSize * 0.3);
        roof.setFill(color.getColor());
        roof.setStroke(color.dark());
        roof.setStrokeWidth(Math.max(2, cell * 0.04));
        // The ridge of the roof
        final Rectangle ridge = new Rectangle(-roofSize / 2 + cell * 0.04, -cell * 0.02, roofSize - cell * 0.08,
            cell * 0.04);
        ridge.setFill(color.dark());
        final Circle badge = new Circle(cell * 0.2, Color.WHITE);
        badge.setStroke(color.dark());
        badge.setStrokeWidth(Math.max(1.5, cell * 0.025));
        final Group node = new Group(platform, roof, ridge, badge, color.symbol(cell * 0.28));
        node.setLayoutX(sx(box[0]));
        node.setLayoutY(sy(box[1]));
        return node;
    }

    /**
     * The tunnel the trains come out of: a hill of rocks over the rails, its dark mouth turned to the circuit.
     */
    private Group tunnel() {
        final int[] direction = Circuit.startDirection(circuit.getFirst());
        hill = new Circle(-cell * 0.42, 0, cell * 0.55, Color.rgb(156, 146, 132));
        hill.setStroke(Color.rgb(116, 106, 94));
        hill.setStrokeWidth(Math.max(2, cell * 0.03));
        final Circle rock1 = new Circle(-cell * 0.62, -cell * 0.22, cell * 0.14, Color.rgb(176, 166, 152));
        final Circle rock2 = new Circle(-cell * 0.56, cell * 0.26, cell * 0.1, Color.rgb(176, 166, 152));
        final Rectangle rim = new Rectangle(-cell * 0.34, -cell * 0.3, cell * 0.36, cell * 0.6);
        rim.setArcWidth(cell * 0.3);
        rim.setArcHeight(cell * 0.3);
        rim.setFill(Color.rgb(122, 112, 100));
        final Rectangle mouth = new Rectangle(-cell * 0.3, -cell * 0.24, cell * 0.3, cell * 0.48);
        mouth.setArcWidth(cell * 0.24);
        mouth.setArcHeight(cell * 0.24);
        mouth.setFill(Color.rgb(40, 36, 34));
        final Group node = new Group(hill, rock1, rock2, rim, mouth);
        node.setLayoutX(sx(circuit.getTunnel()[0]));
        node.setLayoutY(sy(circuit.getTunnel()[1]));
        // Turned about the box of the tunnel (setRotate would turn it about the middle of its drawing)
        node.getTransforms().add(new javafx.scene.transform.Rotate(
            Math.toDegrees(Math.atan2(direction[1], direction[0])), 0, 0));
        return node;
    }

    private void drawResumeButton() {
        final Circle disc = new Circle(resumeSize / 2, Color.rgb(255, 252, 244, 0.96));
        disc.setStroke(Color.rgb(90, 96, 110));
        disc.setStrokeWidth(Math.max(2, resumeSize * 0.03));
        final SVGPath icon = GameStyle.icon("play_arrow_fill1", resumeSize * 0.62);
        icon.setStyle("-fx-fill: -gp-accent;");
        icon.setLayoutX(-resumeSize * 0.29);
        icon.setLayoutY(-resumeSize * 0.31);
        resumeButton.getChildren().addAll(disc, icon);
        resumeButton.setLayoutX(resumeX);
        resumeButton.setLayoutY(resumeY);
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        final double scaled = seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        fadeMarks(seconds);
        preview.update();
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= waiting) {
                    phase = Phase.PLAY;
                    stats.notifyNewRoundReady();
                    if (looked != null && isEnabled(looked)) {
                        showRing(looked);
                    }
                }
            }
            case PLAY -> {
                fixate(seconds);
                if (phase == Phase.PLAY && paused == null) {
                    send(scaled);
                    move(scaled);
                }
                if (paused != null) {
                    halo.setOpacity(0.55 + 0.45 * Math.sin(elapsed * 6));
                    elapsed += seconds;
                }
            }
            case CHANGE -> {
                elapsed += seconds;
                if (elapsed >= JOY) {
                    if (end.getTrains() > 0 && arrived >= end.getTrains()) {
                        win();
                    } else {
                        newCircuit();
                    }
                }
            }
            default -> {
            }
        }
    }

    /**
     * @return the speed of the trains in boxes per second (at the speed 1 of the settings), for a fixation length in
     * milliseconds: at most one box during a fixation and a reaction, at most {@link #SPEED}
     */
    static double speedFor(final double fixationLength) {
        return Math.min(SPEED, 1 / (Math.max(0, fixationLength) / 1000 + REACTION));
    }

    /**
     * @return the time to get ready before a train leaves, in seconds (at the speed 1 of the settings), for a fixation
     * length in milliseconds: two fixations and {@link #READY}
     */
    static double readyFor(final double fixationLength) {
        return READY + 2 * Math.max(0, fixationLength) / 1000;
    }

    private double speed() {
        return speedFor(configuration.getFixationLength());
    }

    /**
     * @return how long the next train waits, in seconds at the speed 1 of the settings
     */
    private double nextWait() {
        if (sentHere == 0 || rhythm == TrainSwitchesRhythm.ONE_AT_A_TIME) {
            return readyFor(configuration.getFixationLength());
        }
        return INTERVAL / speed();
    }

    /**
     * @return whether the next train is waiting to leave (its time counted), false when the circuit has no train left
     * or when a train must arrive first
     */
    boolean isNextWaiting() {
        return sentHere < quota && (rhythm != TrainSwitchesRhythm.ONE_AT_A_TIME || trains.isEmpty());
    }

    /**
     * @return the seconds before the next train leaves, at the speed of the settings
     */
    double timeToNext() {
        return Math.max(0, nextWait() - clock) / Math.max(0.1, configuration.getAnimationSpeedRatio());
    }

    /**
     * Draws the color of the next train: one of the stations, never the same twice in a row.
     */
    private void drawNextColor() {
        final List<TrainColor> choices = new ArrayList<>(stationColors);
        if (choices.size() > 1 && lastColor != null) {
            choices.remove(lastColor);
        }
        nextColor = choices.get(random.nextInt(choices.size()));
        lastColor = nextColor;
        preview.setColor(nextColor);
    }

    /**
     * Sends the next train when it is time, if the circuit has trains left.
     */
    private void send(final double seconds) {
        if (!isNextWaiting()) {
            return;
        }
        clock += seconds;
        if (clock < nextWait()) {
            return;
        }
        clock = 0;
        final Train train = new Train(nextColor);
        trains.add(train);
        trainsLayer.getChildren().add(train.node);
        train.place();
        sentHere++;
        sent++;
        gameContext.getSoundManager().add(SOUNDS + "whistle.wav");
        if (sentHere < quota) {
            drawNextColor();
        }
    }

    /**
     * Moves the trains along the rails; at a switch, a train takes the way shown; with pauses, everything stops before a
     * switch.
     */
    private void move(final double seconds) {
        final double distance = speed() * seconds;
        for (final Train train : new ArrayList<>(trains)) {
            double target = train.position + distance;
            while (true) {
                final double length = train.route.length();
                if (rhythm == TrainSwitchesRhythm.PAUSE && train.toJunction != null
                    && !train.toJunction.equals(train.pausedFor)) {
                    final double stop = length - PAUSE_BEFORE;
                    if (target >= stop) {
                        train.position = Math.max(train.position, stop);
                        train.place();
                        pause(train);
                        return;
                    }
                }
                if (target < length) {
                    train.position = target;
                    break;
                }
                train.position = length;
                if (train.toJunction == null) {
                    arrive(train);
                    break;
                }
                final Circuit.Junction junction = circuit.getJunctions().get(train.toJunction);
                final Circuit.Stretch way = junction.getOuts().get(ways.get(train.toJunction));
                train.route.append(way.boxes());
                train.toJunction = way.toJunction();
                train.toStation = way.toStation();
            }
            if (trains.contains(train)) {
                train.place();
            }
        }
    }

    private void pause(final Train train) {
        paused = train;
        train.pausedFor = train.toJunction;
        final int[] box = circuit.getJunctions().get(train.toJunction).getBox();
        halo.setCenterX(sx(box[0]));
        halo.setCenterY(sy(box[1]));
        halo.setRadius(cell * 0.54);
        halo.setStrokeWidth(Math.max(3, cell * 0.06));
        halo.setVisible(true);
        resumeButton.setVisible(true);
        elapsed = 0;
        if (looked == resumeButton) {
            progress = 0;
            showRing(resumeButton);
        }
    }

    /**
     * The trains move again, with pauses.
     */
    void resume() {
        if (paused == null) {
            return;
        }
        paused = null;
        halo.setVisible(false);
        resumeButton.setVisible(false);
        if (looked == resumeButton) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * A train at the end of its rails: at the right station, one more goal; at a wrong one, a mistake.
     */
    private void arrive(final Train train) {
        trains.remove(train);
        trainsLayer.getChildren().remove(train.node);
        arrived++;
        arrivedHere++;
        clock = 0;
        stats.incrementNumberOfGoalsToReach();
        final boolean correct = stationColors.get(train.toStation) == train.color;
        if (correct) {
            right++;
            stats.incrementNumberOfGoalsReached();
            gameContext.getSoundManager().add(SOUNDS + "correct.wav");
        } else {
            stats.incrementNumberOfMistakes();
            gameContext.getSoundManager().add(SOUNDS + "wrong.wav");
        }
        final Mark mark = new Mark(circuit.getStations().get(train.toStation), correct);
        marks.add(mark);
        marksLayer.getChildren().add(mark.node);
        showHud();
        if (arrivedHere >= quota) {
            phase = Phase.CHANGE;
            elapsed = 0;
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    private void fadeMarks(final double seconds) {
        for (final Mark mark : new ArrayList<>(marks)) {
            mark.age += seconds;
            mark.node.setOpacity(Math.max(0, Math.min(1, (MARK - mark.age) / (MARK * 0.4))));
            if (mark.age >= MARK) {
                marks.remove(mark);
                marksLayer.getChildren().remove(mark.node);
            }
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
        looked = null;
        if (done instanceof SwitchView view) {
            change(view.junction.getName());
        } else if (done == resumeButton) {
            resume();
        }
    }

    private boolean isEnabled(final Object target) {
        if (phase != Phase.PLAY) {
            return false;
        }
        if (target == resumeButton) {
            return paused != null;
        }
        return target instanceof SwitchView;
    }

    /**
     * The gaze (or the mouse) enters a switch or the resume button.
     */
    void lookAt(final Object target) {
        if (looked == target) {
            return;
        }
        looked = target;
        progress = 0;
        ring.stop();
        if (isEnabled(target)) {
            showRing(target);
        }
    }

    void lookAway(final Object target) {
        if (looked == target) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    private void showRing(final Object target) {
        if (target instanceof SwitchView view) {
            ring.setSize(cell * 1.04);
            ring.show(sx(view.junction.getBox()[0]), sy(view.junction.getBox()[1]));
        } else {
            ring.setSize(resumeSize * 1.15);
            ring.show(resumeX, resumeY);
        }
    }

    /**
     * Changes the way of a switch.
     */
    void change(final String junction) {
        if (phase != Phase.PLAY) {
            return;
        }
        ways.put(junction, 1 - ways.get(junction));
        switchViews.get(junction).draw();
        gameContext.getSoundManager().add(SOUNDS + "click.wav");
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final Group good = GameStyle.pill(gameContext.getTranslator().translate("TrainSwitchesRight") + " " + right
            + " / " + arrived, fontSize);
        final Group count = GameStyle.pill(gameContext.getTranslator().translate("TrainSwitchesCount") + " " + arrived
            + (end.getTrains() > 0 ? " / " + end.getTrains() : ""), fontSize);
        final double gap = fontSize;
        final double total = good.getBoundsInLocal().getWidth() + gap + count.getBoundsInLocal().getWidth();
        good.setLayoutX((width - total) / 2);
        good.setLayoutY(height * 0.02);
        count.setLayoutX((width - total) / 2 + good.getBoundsInLocal().getWidth() + gap);
        count.setLayoutY(height * 0.02);
        hud.getChildren().addAll(good, count);
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
     * @return the way of a switch: 0 or 1, the index of its stretch going out
     */
    int wayOf(final String junction) {
        return ways.get(junction);
    }

    /**
     * @return what the gaze looks at to change a switch
     */
    Object switchOf(final String junction) {
        return switchViews.get(junction);
    }

    Object getResumeButton() {
        return resumeButton;
    }

    /**
     * @return whether the next train is shown on the tunnel
     */
    boolean isPreviewShown() {
        return preview.node.isVisible();
    }

    /**
     * @return the middle and the radius of the disc of the next train on the screen
     */
    double[] previewCircle() {
        return preview.center();
    }

    /**
     * @return the middle of the hill of the tunnel on the screen, as drawn
     */
    double[] hillCenter() {
        final javafx.geometry.Point2D center = hill.getParent().localToParent(hill.getCenterX(), hill.getCenterY());
        return new double[]{center.getX(), center.getY()};
    }

    /**
     * @return the seconds shown on the disc of the next train, empty when hidden
     */
    String previewSeconds() {
        return preview.badge.isVisible() ? preview.seconds.getText() : "";
    }

    boolean isResumeShown() {
        return resumeButton.isVisible();
    }

    /**
     * @return the color of a station
     */
    TrainColor colorOf(final int station) {
        return stationColors.get(station);
    }

    /**
     * @return the box of the grid of the circuit on the screen (left, top, right, bottom), then the middle and the size
     * of the resume button
     */
    double[] layout() {
        return new double[]{boardX, boardY, boardX + cell * circuit.getColumns(), boardY + cell * circuit.getLines(),
            resumeX, resumeY, resumeSize};
    }

    /**
     * @return the middle of a switch on the screen
     */
    double[] switchCenter(final String junction) {
        final int[] box = circuit.getJunctions().get(junction).getBox();
        return new double[]{sx(box[0]), sy(box[1])};
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (ring != null) {
            ring.stop();
        }
        new ArrayList<>(gazeNodes).forEach(this::forget);
        trains.clear();
        marks.clear();
        switchViews.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A train: an engine and its wagon of a color, with the symbol of the color, along its rails.
     */
    final class Train {
        @Getter
        private final TrainColor color;
        private final Route route;
        /**
         * The switch or the station at the end of its rails.
         */
        @Getter
        private String toJunction;
        @Getter
        private int toStation;
        /**
         * The last switch it stopped before, with pauses.
         */
        private String pausedFor;
        @Getter
        private double position = START;
        private final Group engine;
        private final Group wagon;
        private final Group node;

        Train(final TrainColor color) {
            this.color = color;
            final Circuit.Stretch first = circuit.getFirst();
            route = new Route(first.boxes());
            toJunction = first.toJunction();
            toStation = first.toStation();
            engine = engine();
            wagon = wagon();
            node = new Group(wagon, engine);
        }

        /**
         * The engine, seen from above, facing right, centered on (0, 0): its boiler, its chimney, its cab at the back,
         * the symbol on the boiler.
         */
        private Group engine() {
            final double length = ENGINE * cell;
            final double breadth = 0.38 * cell;
            final Rectangle body = new Rectangle(-length / 2, -breadth / 2, length, breadth);
            body.setArcWidth(breadth * 0.5);
            body.setArcHeight(breadth * 0.5);
            body.setFill(color.getColor());
            body.setStroke(color.dark());
            body.setStrokeWidth(Math.max(1.5, cell * 0.03));
            final Rectangle cab = new Rectangle(-length / 2 + cell * 0.03, -breadth / 2 + cell * 0.03, cell * 0.17,
                breadth - cell * 0.06);
            cab.setArcWidth(cell * 0.06);
            cab.setArcHeight(cell * 0.06);
            cab.setFill(color.dark());
            final Rectangle buffer = new Rectangle(length / 2 - cell * 0.01, -breadth * 0.36, cell * 0.04,
                breadth * 0.72);
            buffer.setFill(Color.rgb(70, 70, 78));
            final Circle chimney = new Circle(length / 2 - cell * 0.1, 0, cell * 0.055, Color.rgb(58, 58, 66));
            final Circle badge = new Circle(cell * 0.03, 0, cell * 0.125, Color.WHITE);
            badge.setStroke(color.dark());
            badge.setStrokeWidth(Math.max(1, cell * 0.02));
            final Node symbol = color.symbol(cell * 0.17);
            symbol.setLayoutX(cell * 0.03);
            return new Group(body, cab, buffer, chimney, badge, symbol);
        }

        /**
         * The wagon, seen from above, facing right, centered on (0, 0), with the symbol on its roof.
         */
        private Group wagon() {
            final double length = WAGON * cell;
            final double breadth = 0.36 * cell;
            final Rectangle body = new Rectangle(-length / 2, -breadth / 2, length, breadth);
            body.setArcWidth(breadth * 0.4);
            body.setArcHeight(breadth * 0.4);
            body.setFill(color.getColor().deriveColor(0, 0.75, 1.12, 1));
            body.setStroke(color.dark());
            body.setStrokeWidth(Math.max(1.5, cell * 0.03));
            final Circle badge = new Circle(cell * 0.11, Color.WHITE);
            badge.setStroke(color.dark());
            badge.setStrokeWidth(Math.max(1, cell * 0.02));
            return new Group(body, badge, color.symbol(cell * 0.15));
        }

        /**
         * Places the engine at its position, and the wagon behind it on the rails; each one shows when it is in the
         * tunnel.
         */
        void place() {
            put(engine, position, ENGINE);
            put(wagon, position - ENGINE / 2 - GAP - WAGON / 2, WAGON);
        }

        private void put(final Group part, final double at, final double length) {
            final double[] point = route.at(at);
            part.setLayoutX(sx(point[0]));
            part.setLayoutY(sy(point[1]));
            part.setRotate(point[2]);
            part.setVisible(at - length / 2 >= -0.85);
        }

        /**
         * @return the middle of the engine on the screen
         */
        double[] center() {
            return new double[]{engine.getLayoutX(), engine.getLayoutY()};
        }

        boolean isShown() {
            return engine.isVisible();
        }
    }

    /**
     * A switch: a disc showing its two ways, the way taken bright with an arrow, the other one grey. The gaze looks at
     * a zone a little larger than the disc.
     */
    private final class SwitchView {
        private final Circuit.Junction junction;
        private final Group node = new Group();
        private final Group drawing = new Group();
        private final Circle zone;

        SwitchView(final Circuit.Junction junction) {
            this.junction = junction;
            zone = new Circle(sx(junction.getBox()[0]), sy(junction.getBox()[1]), cell * 0.5, Color.TRANSPARENT);
            node.getChildren().addAll(drawing, zone);
            draw();
        }

        void draw() {
            drawing.getChildren().clear();
            final double cx = sx(junction.getBox()[0]);
            final double cy = sy(junction.getBox()[1]);
            final double radius = cell * 0.42;
            final Circle disc = new Circle(cx, cy, radius, Color.rgb(252, 248, 236));
            disc.setStroke(Color.rgb(120, 96, 70));
            disc.setStrokeWidth(Math.max(2, cell * 0.035));
            drawing.getChildren().add(disc);
            final int[] in = Circuit.endDirection(circuit.arrivingAt(junction.getName()));
            final double inX = cx - in[0] * radius * 0.86;
            final double inY = cy - in[1] * radius * 0.86;
            final int chosen = ways.get(junction.getName());
            for (final int way : new int[]{1 - chosen, chosen}) {
                final int[] out = Circuit.startDirection(junction.getOuts().get(way));
                final double outX = cx + out[0] * radius * 0.7;
                final double outY = cy + out[1] * radius * 0.7;
                final QuadCurve curve = new QuadCurve(inX, inY, cx, cy, outX, outY);
                curve.setFill(null);
                curve.setStrokeLineCap(StrokeLineCap.ROUND);
                if (way == chosen) {
                    final QuadCurve glow = new QuadCurve(inX, inY, cx, cy, outX, outY);
                    glow.setFill(null);
                    glow.setStroke(Color.rgb(255, 210, 80));
                    glow.setStrokeWidth(cell * 0.2);
                    glow.setStrokeLineCap(StrokeLineCap.ROUND);
                    curve.setStroke(WAY);
                    curve.setStrokeWidth(cell * 0.11);
                    // The arrow at the end of the way taken
                    final double tipX = cx + out[0] * radius * 0.92;
                    final double tipY = cy + out[1] * radius * 0.92;
                    final double backX = cx + out[0] * radius * 0.56;
                    final double backY = cy + out[1] * radius * 0.56;
                    final double side = cell * 0.13;
                    final Polygon arrow = new Polygon(tipX, tipY, backX - out[1] * side, backY + out[0] * side,
                        backX + out[1] * side, backY - out[0] * side);
                    arrow.setFill(WAY);
                    drawing.getChildren().addAll(glow, curve, arrow);
                } else {
                    curve.setStroke(Color.rgb(168, 160, 146));
                    curve.setStrokeWidth(cell * 0.08);
                    drawing.getChildren().add(curve);
                }
            }
        }
    }

    /**
     * The next train, on the hill of the tunnel (maintainer's request after his trial of 10/11/2026): a disc with the
     * symbol of its color, a ring of its color emptying until it leaves, and the seconds left. Pale while it waits for
     * the last train to arrive (one at a time).
     */
    private final class Preview {
        /**
         * In boxes: from the box of the tunnel to the middle of the disc, backwards (behind the mouth of the tunnel).
         */
        static final double BACK = 0.66;

        private final Group node = new Group();
        private final Circle disc;
        private final javafx.scene.shape.Arc countdown;
        private final Group symbol = new Group();
        private final Group badge;
        private final javafx.scene.text.Text seconds;
        private int shownSeconds = -1;

        Preview(final double x, final double y) {
            final double radius = cell * 0.3;
            disc = new Circle(radius, Color.WHITE);
            disc.setStrokeWidth(Math.max(2, cell * 0.025));
            countdown = new javafx.scene.shape.Arc(0, 0, radius - cell * 0.035, radius - cell * 0.035, 90, -360);
            countdown.setType(javafx.scene.shape.ArcType.OPEN);
            countdown.setFill(null);
            countdown.setStrokeWidth(cell * 0.06);
            countdown.setStrokeLineCap(StrokeLineCap.ROUND);
            final Circle badgeDisc = new Circle(cell * 0.14, Color.rgb(64, 66, 78));
            badgeDisc.setStroke(Color.WHITE);
            badgeDisc.setStrokeWidth(Math.max(1.5, cell * 0.02));
            seconds = new javafx.scene.text.Text();
            seconds.setFont(GameStyle.titleFont(cell * 0.17));
            seconds.setStyle(GameStyle.titleFontStyle(cell * 0.17));
            seconds.setFill(Color.WHITE);
            seconds.setTextOrigin(javafx.geometry.VPos.CENTER);
            badge = new Group(badgeDisc, seconds);
            badge.setLayoutX(radius * 0.8);
            badge.setLayoutY(-radius * 0.8);
            node.getChildren().addAll(disc, countdown, symbol, badge);
            node.setLayoutX(x);
            node.setLayoutY(y);
            node.setMouseTransparent(true);
        }

        void setColor(final TrainColor color) {
            disc.setStroke(color.dark());
            countdown.setStroke(color.getColor());
            symbol.getChildren().setAll(color.symbol(cell * 0.3));
        }

        /**
         * Shows the next train and the time left, or nothing when the circuit has no train left.
         */
        void update() {
            final boolean shown = nextColor != null && sentHere < quota
                && (phase == Phase.WAIT || phase == Phase.PLAY);
            node.setVisible(shown);
            if (!shown) {
                return;
            }
            final boolean counting = isNextWaiting();
            final double total = nextWait();
            final double left = counting ? Math.max(0, total - clock) : total;
            countdown.setLength(-360 * (total <= 0 ? 0 : left / total));
            node.setOpacity(counting ? 1 : 0.5);
            badge.setVisible(counting);
            final int wholeSeconds = (int) Math.ceil(timeToNext());
            if (wholeSeconds != shownSeconds) {
                shownSeconds = wholeSeconds;
                seconds.setText(Integer.toString(wholeSeconds));
                seconds.setX(-seconds.getLayoutBounds().getWidth() / 2);
            }
        }

        /**
         * @return the middle of the disc on the screen
         */
        double[] center() {
            return new double[]{node.getLayoutX(), node.getLayoutY(), cell * 0.3};
        }
    }

    /**
     * A check (right station) or a cross (wrong station) on a station, which fades away.
     */
    private final class Mark {
        private final Group node;
        private double age;

        Mark(final int[] box, final boolean correct) {
            final double radius = cell * 0.26;
            final Circle disc = new Circle(radius, correct ? Color.web(GameStyle.SUCCESS_COLOR)
                : Color.rgb(214, 64, 64));
            disc.setStroke(Color.WHITE);
            disc.setStrokeWidth(Math.max(2, cell * 0.035));
            final Polyline sign = correct
                ? new Polyline(-radius * 0.45, 0, -radius * 0.12, radius * 0.36, radius * 0.5, -radius * 0.36)
                : new Polyline(-radius * 0.4, -radius * 0.4, radius * 0.4, radius * 0.4, 0, 0, radius * 0.4,
                -radius * 0.4, -radius * 0.4, radius * 0.4);
            sign.setStroke(Color.WHITE);
            sign.setStrokeWidth(radius * 0.26);
            sign.setStrokeLineCap(StrokeLineCap.ROUND);
            sign.setStrokeLineJoin(StrokeLineJoin.ROUND);
            node = new Group(disc, sign);
            node.setLayoutX(sx(box[0]) + cell * 0.26);
            node.setLayoutY(sy(box[1]) - cell * 0.26);
        }
    }
}
