package net.gazeplay.games.rushhour;

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
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "Embouteillage" (Traffic Jam, name code RushHour; reviewed on 9/11/2026, maintainer's rules): the red car must leave the
 * parking by the opening at the right, but other cars, trucks and buses are in the way. A fixation on a vehicle chooses
 * it; the free boxes of its line light up, and a fixation on one of them slides the vehicle there. A button puts the
 * parking back as it was. When the red car reaches the opening, it drives out, the parking counts, and a new one comes;
 * the game ends after 1, 3 or 5 parkings ("Bravo"), or never. The parkings, of 3 levels, are made by
 * gradle/images/make_trafficjam_levels.py, which computes the fewest moves of each.
 * <p>
 * One loop, in real seconds: the fixations are counted by the game (length of the settings, read at each step), nothing
 * can be chosen before the delay of the settings, and nothing moves while the menu of the game is open. (Before the
 * review: 33 parkings of unknown source, not in order of difficulty; a vehicle moved one box each time the gaze left
 * it; one picture tinted for every car; the former progress indicator; no end.)
 */
@Slf4j
public class RushHour implements GameLifeCycle {

    /**
     * In seconds: the red car driving out, the joy after.
     */
    static final double LEAVE = 1.0;
    static final double JOY = 0.8;
    /**
     * In boxes per second: how fast a vehicle slides.
     */
    private static final double SLIDE = 8;

    static final String LEVELS = "data/rushHour/levels.txt";
    private static final String CARS = "data/gazeRace/";
    private static final String DATA = "data/rushHour/";
    private static final String[] SMALL = {"car-2.png", "car-3.png", "car-4.png", "car-5.png", "car-player.png"};
    private static final String[] LARGE = {"truck-1.png", "bus.png", "truck-2.png"};
    private static final Map<String, Image> IMAGES = new HashMap<>();
    private static Map<String, List<String[]>> parkings;

    enum Phase { WAIT, PLAY, LEAVE, JOY, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final RushHourLevel level;
    @Getter
    private final RushHourEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private Parking parking;
    private int lastParking = -1;
    @Getter
    private int score;
    @Getter
    private int moves;
    private double elapsed;
    @Getter
    private double waiting;

    private double width;
    private double height;
    @Getter
    private double cell;
    private double boardX;
    private double boardY;

    private Group layer;
    private Group vehiclesLayer;
    private Group marksLayer;
    private Group hud;
    private FixationRing ring;
    private Group restartButton;
    private Group target;
    private double restartX;
    private double restartY;
    private double restartSize;
    private final List<Car> cars = new ArrayList<>();
    private final List<Mark> marks = new ArrayList<>();
    private final List<Node> gazeNodes = new ArrayList<>();
    private final List<Node> markNodes = new ArrayList<>();

    @Getter
    private Parking.Vehicle selected;
    /**
     * What is looked at (a car, a mark or the restart button), and how far its fixation is.
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
    RushHour(final IGameContext gameContext, final Stats stats, final RushHourLevel level, final RushHourEnd end,
             final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.level = level;
        this.end = end;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
    }

    /**
     * @return the parkings of the file, by level: their fewest moves and their boxes
     */
    static synchronized Map<String, List<String[]>> parkings() {
        if (parkings == null) {
            final Map<String, List<String[]>> read = new HashMap<>();
            final InputStream input = RushHour.class.getClassLoader().getResourceAsStream(LEVELS);
            if (input == null) {
                throw new IllegalStateException("Missing " + LEVELS);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) {
                        continue;
                    }
                    final String[] parts = line.trim().split(" ");
                    read.computeIfAbsent(parts[0], key -> new ArrayList<>()).add(new String[]{parts[1], parts[2]});
                }
            } catch (final IOException e) {
                throw new IllegalStateException("Cannot read " + LEVELS, e);
            }
            parkings = read;
        }
        return parkings;
    }

    @Override
    public void launch() {
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();
        layOut();

        final Rectangle background = new Rectangle(0, 0, width, height);
        background.setFill(Color.rgb(226, 230, 236));
        final double side = cell * Parking.SIZE;
        final double wall = cell * 0.2;
        final Rectangle walls = new Rectangle(boardX - wall, boardY - wall, side + 2 * wall, side + 2 * wall);
        walls.setArcWidth(wall * 2.5);
        walls.setArcHeight(wall * 2.5);
        walls.setFill(Color.rgb(150, 156, 168));
        final Rectangle asphalt = new Rectangle(boardX, boardY, side, side);
        asphalt.setFill(Color.rgb(98, 104, 118));
        // The opening at the right, on the exit line, and its arrow
        final Rectangle opening = new Rectangle(boardX + side - 1, boardY + Parking.EXIT_LINE * cell, wall + 2, cell);
        opening.setFill(Color.rgb(98, 104, 118));
        final double arrowX = boardX + side + wall + cell * 0.25;
        final double arrowY = boardY + (Parking.EXIT_LINE + 0.5) * cell;
        final Polygon arrow = new Polygon(arrowX, arrowY - cell * 0.28, arrowX + cell * 0.5, arrowY, arrowX,
            arrowY + cell * 0.28);
        arrow.setFill(Color.rgb(250, 200, 70));
        arrow.setStroke(Color.rgb(200, 140, 40));
        arrow.setStrokeWidth(Math.max(1, cell * 0.03));
        final Group ground = new Group(background, walls, asphalt, opening, arrow);
        for (int k = 1; k < Parking.SIZE; k++) {
            final Line across = new Line(boardX + k * cell, boardY + cell * 0.08, boardX + k * cell,
                boardY + side - cell * 0.08);
            final Line down = new Line(boardX + cell * 0.08, boardY + k * cell, boardX + side - cell * 0.08,
                boardY + k * cell);
            for (final Line line : new Line[]{across, down}) {
                line.setStroke(Color.rgb(124, 130, 144));
                line.setStrokeWidth(Math.max(1, cell * 0.025));
                ground.getChildren().add(line);
            }
        }
        target = target();
        ground.getChildren().add(target);
        ground.setMouseTransparent(true);
        marksLayer = new Group();
        vehiclesLayer = new Group();
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        restartButton = restartButton();
        layer = new Group(ground, marksLayer, vehiclesLayer, restartButton, ring, hud);
        gameContext.getChildren().add(layer);
        listen(restartButton, () -> lookAt(restartButton), () -> lookAway(restartButton));

        gameContext.getGazeDeviceManager().addStats(stats);
        score = 0;
        showHud();
        newParking();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * Places the parking in the middle of the screen, under the band of the score, with room at the left for the
     * restart button and at the right for the arrow of the exit.
     */
    private void layOut() {
        final double top = height * 0.11;
        final double margin = Math.min(width, height) * 0.04;
        final double availableHeight = height - top - margin;
        // The parking and its walls (0.4 box), and on each side the room of the button (1.65 boxes from the parking,
        // more than the arrow of the exit)
        cell = Math.min(availableHeight / (Parking.SIZE + 0.4), (width - 2 * margin) / (Parking.SIZE + 3.4));
        boardX = (width - cell * Parking.SIZE) / 2;
        boardY = top + (availableHeight - cell * Parking.SIZE) / 2;
        restartSize = cell * 1.1;
        restartX = boardX - cell * 0.2 - cell * 0.35 - restartSize / 2;
        restartY = boardY + cell * Parking.SIZE - restartSize / 2;
    }

    /**
     * The boxes the red car must reach, at the opening (maintainer's request after his trial of 9/11/2026): red, with
     * the star of the red car.
     */
    private Group target() {
        final double inset = cell * 0.06;
        final Rectangle boxes = new Rectangle(boardX + (Parking.SIZE - 2) * cell + inset,
            boardY + Parking.EXIT_LINE * cell + inset, 2 * cell - 2 * inset, cell - 2 * inset);
        boxes.setArcWidth(cell * 0.3);
        boxes.setArcHeight(cell * 0.3);
        boxes.setFill(Color.rgb(226, 60, 60, 0.3));
        boxes.setStroke(Color.rgb(236, 80, 80));
        boxes.setStrokeWidth(Math.max(2, cell * 0.05));
        boxes.getStrokeDashArray().addAll(cell * 0.16, cell * 0.1);
        final Polygon star = star(boxes.getX() + boxes.getWidth() / 2, boxes.getY() + boxes.getHeight() / 2,
            cell * 0.3);
        star.setFill(Color.rgb(255, 255, 255, 0.55));
        star.setStroke(Color.rgb(236, 80, 80));
        star.setStrokeWidth(Math.max(1.5, cell * 0.03));
        return new Group(boxes, star);
    }

    /**
     * @return a star with 5 points, of a center and a radius
     */
    static Polygon star(final double centerX, final double centerY, final double radius) {
        final Polygon star = new Polygon();
        for (int point = 0; point < 10; point++) {
            final double angle = -Math.PI / 2 + point * Math.PI / 5;
            final double distance = point % 2 == 0 ? radius : radius * 0.45;
            star.getPoints().addAll(centerX + distance * Math.cos(angle), centerY + distance * Math.sin(angle));
        }
        return star;
    }

    private Group restartButton() {
        final Circle disc = new Circle(restartSize / 2, Color.rgb(255, 252, 244, 0.95));
        disc.setStroke(Color.rgb(90, 96, 110));
        disc.setStrokeWidth(Math.max(2, restartSize * 0.03));
        final SVGPath icon = GameStyle.icon("restart_alt", restartSize * 0.62);
        icon.setStyle("-fx-fill: -gp-accent;");
        icon.setLayoutX(-restartSize * 0.31);
        icon.setLayoutY(-restartSize * 0.31);
        final Group button = new Group(disc, icon);
        button.setLayoutX(restartX);
        button.setLayoutY(restartY);
        return button;
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

    private static Image image(final String path) {
        return IMAGES.computeIfAbsent(path, Image::new);
    }

    /**
     * A new parking of the level, another one than the last.
     */
    private void newParking() {
        final List<String[]> all = parkings().get(level.getFileName());
        int index = random.nextInt(all.size() - (lastParking < 0 ? 0 : 1));
        if (lastParking >= 0 && index >= lastParking) {
            index++;
        }
        lastParking = index;
        parking = new Parking(all.get(index)[1], Integer.parseInt(all.get(index)[0]));

        cars.forEach(car -> forget(car.zone));
        cars.clear();
        vehiclesLayer.getChildren().clear();
        int small = random.nextInt(SMALL.length);
        int large = random.nextInt(LARGE.length);
        for (final Parking.Vehicle vehicle : parking.getVehicles()) {
            final String picture;
            if (vehicle.name == Parking.RED) {
                picture = CARS + "car-1.png";
            } else if (vehicle.length == 2) {
                picture = CARS + SMALL[small++ % SMALL.length];
            } else {
                picture = DATA + LARGE[large++ % LARGE.length];
            }
            final Car car = new Car(vehicle, image(picture), random.nextInt(2) == 0);
            cars.add(car);
            vehiclesLayer.getChildren().add(car.node);
            listen(car.zone, () -> lookAt(car), () -> lookAway(car));
        }
        moves = 0;
        selected = null;
        looked = null;
        progress = 0;
        ring.stop();
        clearMarks();
        elapsed = 0;
        waiting = configuration.getGameStartDelay() / 1000.0;
        phase = Phase.WAIT;
        stats.incrementNumberOfGoalsToReach();
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        final double scaled = seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        cars.forEach(car -> car.slide(scaled));
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
            case PLAY -> fixate(seconds);
            case LEAVE -> leave(seconds);
            case JOY -> {
                elapsed += seconds;
                if (elapsed >= JOY) {
                    if (end.getParkings() > 0 && score >= end.getParkings()) {
                        win();
                    } else {
                        newParking();
                    }
                }
            }
            default -> {
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
        if (done instanceof Car car) {
            looked = null;
            choose(car.vehicle);
        } else if (done instanceof Mark mark) {
            looked = null;
            slide(mark.position);
        } else if (done == restartButton) {
            looked = null;
            restart();
        }
    }

    private boolean isEnabled(final Object target) {
        if (phase != Phase.PLAY) {
            return false;
        }
        if (target instanceof Car car) {
            return car.vehicle != selected;
        }
        if (target instanceof Mark mark) {
            return marks.contains(mark);
        }
        return target == restartButton;
    }

    /**
     * The gaze (or the mouse) enters a vehicle, a free box lit up or the restart button.
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
        if (target instanceof Car car) {
            ring.setSize(cell * 0.8);
            ring.show(car.centerX(), car.centerY());
        } else if (target instanceof Mark mark) {
            ring.setSize(cell * 0.8);
            ring.show(boardX + (mark.column + 0.5) * cell, boardY + (mark.line + 0.5) * cell);
        } else {
            ring.setSize(restartSize * 1.15);
            ring.show(restartX, restartY);
        }
    }

    /**
     * Chooses a vehicle: it shines, and the free boxes of its line light up.
     */
    void choose(final Parking.Vehicle vehicle) {
        if (phase != Phase.PLAY) {
            return;
        }
        selected = vehicle;
        for (final Car car : cars) {
            car.shine(car.vehicle == vehicle);
        }
        showMarks();
    }

    /**
     * Slides the chosen vehicle to a position of its line; it stays chosen.
     */
    void slide(final int position) {
        if (phase != Phase.PLAY || selected == null || !parking.slide(selected, position)) {
            return;
        }
        moves++;
        if (parking.isSolved()) {
            reach();
        } else {
            showMarks();
        }
    }

    /**
     * Puts every vehicle back at its start, none chosen.
     */
    void restart() {
        if (phase != Phase.PLAY) {
            return;
        }
        parking.restart();
        moves = 0;
        selected = null;
        cars.forEach(car -> car.shine(false));
        clearMarks();
    }

    private void showMarks() {
        clearMarks();
        if (selected == null) {
            return;
        }
        for (final int position : parking.reachable(selected)) {
            final int[] box = Parking.markOf(selected, position);
            final Mark mark = new Mark(box[0], box[1], position);
            marks.add(mark);
            marksLayer.getChildren().add(mark.node);
            markNodes.add(mark.node);
            listen(mark.node, () -> lookAt(mark), () -> lookAway(mark));
        }
    }

    private void clearMarks() {
        markNodes.forEach(this::forget);
        markNodes.clear();
        marks.clear();
        if (marksLayer != null) {
            marksLayer.getChildren().clear();
        }
        if (looked instanceof Mark) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * The red car is at the opening: it drives out, one more point.
     */
    private void reach() {
        phase = Phase.LEAVE;
        elapsed = 0;
        score++;
        stats.incrementNumberOfGoalsReached();
        selected = null;
        looked = null;
        progress = 0;
        ring.stop();
        clearMarks();
        cars.forEach(car -> car.shine(false));
        showHud();
    }

    private void leave(final double seconds) {
        elapsed += seconds;
        final Car red = cars.get(0);
        if (red.shownX >= red.targetX() - 1e-6) {
            // Out of the parking, through the opening, then out of sight
            red.outside += seconds * (Parking.SIZE * cell) / LEAVE;
            red.place();
        }
        if (elapsed >= LEAVE) {
            phase = Phase.JOY;
            elapsed = 0;
        }
    }

    private void showHud() {
        hud.getChildren().clear();
        final String text = gameContext.getTranslator().translate("RushHourScore") + " " + score
            + (end.getParkings() > 0 ? " / " + end.getParkings() : "");
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
     * @return what the gaze looks at to choose a vehicle
     */
    Object carOf(final Parking.Vehicle vehicle) {
        for (final Car car : cars) {
            if (car.vehicle == vehicle) {
                return car;
            }
        }
        return null;
    }

    /**
     * @return the free box lit up for a position of the chosen vehicle, null if none
     */
    Object markOf(final int position) {
        for (final Mark mark : marks) {
            if (mark.position == position) {
                return mark;
            }
        }
        return null;
    }

    Object getRestartButton() {
        return restartButton;
    }

    /**
     * @return the boxes to reach on the screen (left, top, right, bottom)
     */
    double[] targetBounds() {
        final var bounds = target.getBoundsInParent();
        return new double[]{bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()};
    }

    /**
     * @return whether a vehicle (see {@link #carOf}) carries the star of the car to take out
     */
    boolean hasStar(final Object car) {
        return car instanceof Car shown && shown.badge != null && shown.node.getChildren().contains(shown.badge);
    }

    /**
     * @return the corners of the parking on the screen (left, top, right, bottom), then the middle and the size of the
     * restart button
     */
    double[] layout() {
        return new double[]{boardX, boardY, boardX + cell * Parking.SIZE, boardY + cell * Parking.SIZE, restartX,
            restartY, restartSize};
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (ring != null) {
            ring.stop();
        }
        new ArrayList<>(gazeNodes).forEach(this::forget);
        cars.clear();
        marks.clear();
        markNodes.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A vehicle on the screen: its picture (turned when it stands), and the zone of its boxes, which the gaze looks at.
     */
    private final class Car {
        private final Parking.Vehicle vehicle;
        private final Group node;
        private final ImageView view;
        private final Rectangle zone;
        private final Group badge;
        private double shownX;
        private double shownY;
        private double outside;

        Car(final Parking.Vehicle vehicle, final Image picture, final boolean facingUp) {
            this.vehicle = vehicle;
            final double gap = cell * 0.08;
            view = new ImageView(picture);
            view.setFitWidth(vehicle.length * cell - gap);
            view.setFitHeight(cell - gap);
            if (!vehicle.horizontal) {
                view.setRotate(facingUp ? -90 : 90);
            }
            view.setMouseTransparent(true);
            zone = new Rectangle(vehicle.horizontal ? vehicle.length * cell : cell,
                vehicle.horizontal ? cell : vehicle.length * cell, Color.TRANSPARENT);
            node = new Group(view);
            if (vehicle.name == Parking.RED) {
                // The car to take out: a star on its roof, as on the boxes to reach
                badge = new Group(new Circle(zone.getWidth() / 2, zone.getHeight() / 2, cell * 0.24, Color.WHITE),
                    star(zone.getWidth() / 2, zone.getHeight() / 2, cell * 0.19));
                ((Circle) badge.getChildren().get(0)).setStroke(Color.rgb(170, 30, 30));
                ((Circle) badge.getChildren().get(0)).setStrokeWidth(Math.max(1.5, cell * 0.03));
                ((Polygon) badge.getChildren().get(1)).setFill(Color.rgb(226, 50, 50));
                badge.setMouseTransparent(true);
                node.getChildren().add(badge);
            } else {
                badge = null;
            }
            node.getChildren().add(zone);
            shownX = targetX();
            shownY = targetY();
            place();
        }

        /**
         * @return the left of the vehicle on the screen, at its place in the parking
         */
        double targetX() {
            return boardX + (vehicle.horizontal ? vehicle.position : vehicle.fixed) * cell;
        }

        double targetY() {
            return boardY + (vehicle.horizontal ? vehicle.fixed : vehicle.position) * cell;
        }

        double centerX() {
            return shownX + zone.getWidth() / 2;
        }

        double centerY() {
            return shownY + zone.getHeight() / 2;
        }

        void slide(final double seconds) {
            final double most = SLIDE * cell * seconds;
            final double dx = targetX() - shownX;
            final double dy = targetY() - shownY;
            final double distance = Math.hypot(dx, dy);
            if (distance <= most) {
                shownX = targetX();
                shownY = targetY();
            } else {
                shownX += dx / distance * most;
                shownY += dy / distance * most;
            }
            place();
        }

        void place() {
            node.setLayoutX(shownX + outside);
            node.setLayoutY(shownY);
            zone.setLayoutX(0);
            // The picture, turned about its middle, in the middle of the zone
            view.setLayoutX(zone.getWidth() / 2 - view.getFitWidth() / 2);
            view.setLayoutY(zone.getHeight() / 2 - view.getFitHeight() / 2);
        }

        void shine(final boolean on) {
            if (on) {
                final DropShadow glow = new DropShadow(cell * 0.3, Color.rgb(255, 214, 90));
                glow.setSpread(0.55);
                view.setEffect(glow);
            } else {
                view.setEffect(null);
            }
        }
    }

    /**
     * A free box lit up: the chosen vehicle can slide until it covers it.
     */
    private final class Mark {
        private final int line;
        private final int column;
        private final int position;
        private final Group node;

        Mark(final int line, final int column, final int position) {
            this.line = line;
            this.column = column;
            this.position = position;
            final double inset = cell * 0.1;
            final Rectangle box = new Rectangle(boardX + column * cell + inset, boardY + line * cell + inset,
                cell - 2 * inset, cell - 2 * inset);
            box.setArcWidth(cell * 0.25);
            box.setArcHeight(cell * 0.25);
            box.setFill(Color.rgb(255, 214, 90, 0.35));
            box.setStroke(Color.rgb(255, 214, 90));
            box.setStrokeWidth(Math.max(2, cell * 0.04));
            box.getStrokeDashArray().addAll(cell * 0.12, cell * 0.08);
            node = new Group(box);
        }
    }
}
