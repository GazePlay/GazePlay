package net.gazeplay.games.gazeRace;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * GazeRace (reviewed on 9/11/2026, maintainer's rules): the car of the player goes where the gaze or the mouse is, on a
 * road going by, and avoids the other cars and the barriers of works. On one side of the road the cars overtake the
 * player, on the other they come the other way. A car hit costs a life, sends the road back to its starting speed and
 * makes the player invincible for a while. A point every 3 seconds. Three lives; the game has no end, or ends after 1 or
 * 3 minutes ("Bravo"), or after the third life ("Lost!"); then the statistics.
 * <p>
 * Everything is measured in parts of the screen, in real seconds, at the animation speed of the settings; nothing moves
 * while the menu of the game is open, nor before the delay of the settings at the start. The game is computed along the
 * road (its length) and across it, then shown from left to right or from the bottom to the top.
 * (Before the review: speeds in pixels per image and seconds counted as 60 images; the lives never shown; the animations
 * going on after the end; the car of the player still following the gaze after "Replay"; sizes in pixels.)
 */
public class GazeRace implements GameLifeCycle {

    static final int LIVES = 3;
    /**
     * In seconds: a point each, the invincibility after a hit, the turn of the car hit, a crash shown, a new barrier.
     */
    static final double SECONDS_PER_POINT = 3;
    static final double INVINCIBLE = 4;
    static final double SPIN = 1;
    static final double CRASH = 1;
    static final double BARRIER_EVERY = 12;
    /**
     * The seconds the road takes from its starting speed to its top speed.
     */
    static final double SECONDS_TO_TOP = 50;
    /**
     * In parts of the smaller side of the screen: the cars, and the speed of the player per second.
     */
    static final double CAR_LENGTH = 0.115;
    static final double CAR_WIDTH = 0.065;
    static final double PLAYER_SPEED = 0.39;
    /**
     * In parts of the width of the road (across): the grass and the line on each side, the fine line in the middle. The
     * cars drive up to the line in the middle (maintainer's choice of 9/11/2026: on the wide band of before, where no car
     * went, nothing could happen to the player).
     */
    static final double EDGE = 0.05;
    static final double MIDDLE = 0.012;
    /**
     * How near the line in the middle the middle of a car can be, in widths of the car: a quarter of the car covers it.
     */
    static final double LINE_COVER = 0.25;
    /**
     * In lengths of the road: the barrier, how far beyond the screen the cars and the barriers appear.
     */
    static final double BARRIER_LENGTH = 0.156;
    static final double NEAREST_START = 0.13;
    static final double FARTHEST_START = 0.52;
    /**
     * The part of a car (or of a barrier) that hurts: the corners of the pictures are round, and their shadow is not hit.
     */
    static final double HIT_BOX = 0.85;

    private static final String DATA = "data/gazeRace/";
    private static final int CAR_PICTURES = 5;
    private static final double DASH_LENGTH = 0.104;
    private static final double DASH_SPACING = 0.156;
    private static final Map<String, Image> GAME_IMAGES = new HashMap<>();

    enum Phase { PLAY, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final GazeRaceStats stats;
    @Getter
    private final GazeRaceVariant road;
    @Getter
    private final GazeRaceLevel level;
    @Getter
    private final GazeRaceEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int lives;
    @Getter
    private int score;
    @Getter
    private double timeLeft;
    @Getter
    private double waiting;
    /**
     * In lengths of the road per second.
     */
    @Getter
    private double speed;
    @Getter
    private double invincible;
    private double played;
    private double spin;
    private double nextCars;
    private double nextBarrier;
    private double dashShift;

    /**
     * The size of the screen, and of the road: its length (along which it goes by) and its width (across).
     */
    @Getter
    private double width;
    @Getter
    private double height;
    @Getter
    private double length;
    @Getter
    private double across;
    private double carLength;
    private double carWidth;

    @Getter
    private Mobile player;
    @Getter
    private final List<Mobile> mobiles = new ArrayList<>();
    private final List<Rectangle> dashes = new ArrayList<>();
    private final List<double[]> crashes = new ArrayList<>();
    private final List<ImageView> crashViews = new ArrayList<>();
    @Getter
    private double aimAlong;
    @Getter
    private double aimAcross;

    private Group layer;
    private Group roadLayer;
    private Group carsLayer;
    private Group hud;
    private Rectangle area;
    private PauseTransition lostPause;

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
    GazeRace(final IGameContext gameContext, final GazeRaceStats stats, final GazeRaceVariant road,
             final GazeRaceLevel level, final GazeRaceEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.road = road;
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
        final boolean horizontal = road == GazeRaceVariant.HORIZONTAL;
        length = horizontal ? width : height;
        across = horizontal ? height : width;
        final double smaller = Math.min(width, height);
        carLength = smaller * CAR_LENGTH;
        carWidth = smaller * CAR_WIDTH;

        // The whole screen receives the gaze and the mouse, as the setting of the inputs allows
        area = new Rectangle(0, 0, width, height);
        area.setFill(Color.TRANSPARENT);
        roadLayer = new Group();
        roadLayer.setMouseTransparent(true);
        drawRoad();
        carsLayer = new Group();
        carsLayer.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(roadLayer, area, carsLayer, hud);
        // The next cars wait beyond the screen, and the screenshots of the statistics must not show them
        layer.setClip(new Rectangle(0, 0, width, height));
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addEventFilter(area);
        area.addEventFilter(GazeEvent.ANY, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_MOVED, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_DRAGGED, aimHandler);

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();

        mobiles.clear();
        player = new Mobile(Kind.PLAYER, length * 0.2, across * 0.25, carLength, carWidth, 0, "car-player.png");
        carsLayer.getChildren().add(player.view);
        player.place();
        aimAlong = player.along;
        aimAcross = player.across;
        lives = LIVES;
        score = 0;
        played = 0;
        spin = 0;
        invincible = 0;
        speed = level.getStartSpeed();
        timeLeft = end.getSeconds();
        nextCars = 0;
        nextBarrier = BARRIER_EVERY;
        waiting = Math.max(0, configuration.getGameStartDelay() / 1000.0);
        phase = Phase.PLAY;
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * @return an image of the game, loaded once
     */
    private static Image gameImage(final String path) {
        return GAME_IMAGES.computeIfAbsent(path, Image::new);
    }

    /**
     * The grass, the road, its white lines on the sides and in the middle, and the dashes between the lanes.
     */
    private void drawRoad() {
        final Rectangle grass = new Rectangle(0, 0, width, height);
        grass.setFill(Color.rgb(122, 178, 104));
        roadLayer.getChildren().add(grass);
        final double edge = across * EDGE;
        roadLayer.getChildren().add(band(edge, across - 2 * edge, Color.rgb(92, 98, 112)));
        final double line = Math.max(2, across * 0.008);
        roadLayer.getChildren().add(band(edge - line, line, Color.rgb(236, 236, 230)));
        roadLayer.getChildren().add(band(across - edge, line, Color.rgb(236, 236, 230)));
        roadLayer.getChildren().add(band((across - across * MIDDLE) / 2, across * MIDDLE, Color.rgb(236, 236, 230)));
        dashes.clear();
        final double thickness = Math.max(2, across * 0.012);
        for (final double lane : new double[]{laneMiddle(true), laneMiddle(false)}) {
            // One more dash than the screen holds: they all go back by less than a spacing, then start again
            for (double along = 0; along < length + 2 * length * DASH_SPACING; along += length * DASH_SPACING) {
                final Rectangle dash = new Rectangle();
                dash.setFill(Color.rgb(222, 222, 216));
                dash.setArcWidth(thickness);
                dash.setArcHeight(thickness);
                dash.setUserData(new double[]{along, lane - thickness / 2, length * DASH_LENGTH, thickness});
                dashes.add(dash);
                roadLayer.getChildren().add(dash);
            }
        }
        placeDashes();
    }

    /**
     * @return a band along the whole road, from a place across it and of a width across it
     */
    private Rectangle band(final double from, final double size, final Color color) {
        final Rectangle band = new Rectangle();
        setBox(band, 0, from, length, size);
        band.setFill(color);
        return band;
    }

    /**
     * @return the middle, across the road, of the side where the cars overtake (or of the other one)
     */
    private double laneMiddle(final boolean overtaking) {
        final double edge = across * EDGE;
        final double middle = across * MIDDLE / 2;
        return overtaking ? (edge + across / 2 - middle) / 2 : (across / 2 + middle + across - edge) / 2;
    }

    private void placeDashes() {
        final double back = dashShift % (length * DASH_SPACING);
        for (final Rectangle dash : dashes) {
            final double[] box = (double[]) dash.getUserData();
            setBox(dash, box[0] - back, box[1], box[2], box[3]);
        }
    }

    /**
     * Puts a rectangle on the screen from a box along and across the road (its corner nearest to the start of the road
     * and to the side where the cars overtake).
     */
    private void setBox(final Rectangle rectangle, final double along, final double from, final double size,
                        final double thickness) {
        if (road == GazeRaceVariant.HORIZONTAL) {
            rectangle.setX(along);
            rectangle.setY(from);
            rectangle.setWidth(size);
            rectangle.setHeight(thickness);
        } else {
            rectangle.setX(from);
            rectangle.setY(height - along - size);
            rectangle.setWidth(thickness);
            rectangle.setHeight(size);
        }
    }

    private void aim(final Event event) {
        if (event instanceof GazeEvent gaze && (gaze.getEventType() == GazeEvent.GAZE_MOVED
            || gaze.getEventType() == GazeEvent.GAZE_ENTERED)) {
            aimAt(gaze.getX(), gaze.getY());
        } else if (event instanceof MouseEvent mouse) {
            aimAt(mouse.getX(), mouse.getY());
        }
    }

    /**
     * The place looked at (or of the mouse) on the screen, where the car of the player goes.
     */
    void aimAt(final double x, final double y) {
        if (road == GazeRaceVariant.HORIZONTAL) {
            aimAlong = x;
            aimAcross = y;
        } else {
            aimAlong = height - y;
            aimAcross = x;
        }
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase != Phase.PLAY || seconds <= 0) {
            return;
        }
        if (waiting > 0) {
            waiting = Math.max(0, waiting - seconds);
            return;
        }
        final double scaled = seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        speed = Math.min(level.getTopSpeed(),
            speed + (level.getTopSpeed() - level.getStartSpeed()) / SECONDS_TO_TOP * scaled);
        final double moved = speed * length * scaled;
        dashShift += moved;
        placeDashes();
        moveMobiles(moved);
        movePlayer(scaled);
        bringCars(scaled);
        updateCrashes(seconds);
        if (invincible > 0) {
            invincible = Math.max(0, invincible - seconds);
            player.view.setOpacity(invincible > 0 && (int) (invincible * 6) % 2 == 0 ? 0.35 : 1);
        }
        if (spin > 0) {
            spin = Math.max(0, spin - seconds);
            player.place();
        }
        hitSomething();
        if (phase != Phase.PLAY) {
            return;
        }
        countPoints(seconds);
        if (end.getSeconds() > 0) {
            final int before = (int) Math.ceil(timeLeft);
            timeLeft = Math.max(0, timeLeft - seconds);
            if (timeLeft <= 0) {
                win();
            } else if ((int) Math.ceil(timeLeft) != before) {
                showHud();
            }
        }
    }

    /**
     * The barriers stay on the road going by. On the screen, the cars that overtake go forward as fast as the road goes
     * back, those that come the other way go back twice as fast as the road (maintainer's remark of 9/11/2026: going only
     * as fast as the road, they looked parked on it; three times would cross the screen in 0.7 s at the top speed of
     * the normal level). All the cars of a side go at the same speed, so that they never catch up with one another.
     */
    private void moveMobiles(final double moved) {
        final Iterator<Mobile> iterator = mobiles.iterator();
        while (iterator.hasNext()) {
            final Mobile mobile = iterator.next();
            mobile.along += mobile.screenSpeed() * moved;
            final boolean gone = mobile.direction > 0 ? mobile.along - mobile.length / 2 > length + carLength
                : mobile.along + mobile.length / 2 < -carLength;
            if (gone) {
                carsLayer.getChildren().remove(mobile.view);
                iterator.remove();
            } else {
                mobile.place();
            }
        }
    }

    private void movePlayer(final double seconds) {
        final double most = PLAYER_SPEED * Math.min(width, height) * seconds;
        final double edge = across * EDGE;
        final double wantedAlong = clamp(aimAlong, player.length / 2, length - player.length / 2);
        final double wantedAcross = clamp(aimAcross, edge + player.width / 2, across - edge - player.width / 2);
        final double dAlong = wantedAlong - player.along;
        final double dAcross = wantedAcross - player.across;
        final double distance = Math.hypot(dAlong, dAcross);
        if (distance <= most) {
            player.along = wantedAlong;
            player.across = wantedAcross;
        } else {
            player.along += dAlong / distance * most;
            player.across += dAcross / distance * most;
        }
        player.place();
    }

    private static double clamp(final double value, final double low, final double high) {
        return Math.max(low, Math.min(high, value));
    }

    /**
     * Each second, a few cars beyond the screen, where there is room; every {@link #BARRIER_EVERY} seconds, a barrier
     * on the white band in the middle, ahead.
     */
    private void bringCars(final double seconds) {
        nextCars -= seconds;
        while (nextCars <= 0) {
            nextCars += 1;
            final int count = level.getFewestCars() + random.nextInt(level.getMostCars() - level.getFewestCars() + 1);
            for (int i = 0; i < count; i++) {
                addCar();
            }
        }
        nextBarrier -= seconds;
        if (nextBarrier <= 0) {
            nextBarrier += BARRIER_EVERY;
            final double barrierLength = length * BARRIER_LENGTH;
            final Mobile barrier = new Mobile(Kind.BARRIER, length + length * 0.26 + barrierLength / 2, across / 2,
                barrierLength, barrierLength / 6, -1, "barrier.png");
            mobiles.add(barrier);
            carsLayer.getChildren().add(barrier.view);
            barrier.place();
        }
    }

    /**
     * Adds a car beyond the screen on one side or the other, at a free place; none if there is no room after a few tries.
     *
     * @return the car added, or null
     */
    Mobile addCar() {
        final boolean overtaking = random.nextInt(2) == 0;
        final double edge = across * EDGE;
        // Some room along the edges of the road; along the line in the middle, a car can cover it by a quarter of its
        // width: only touching it, it hit a player on the line 3.6 times less often than one in the middle of a side
        final double margin = carWidth * 0.2;
        final double low = overtaking ? edge + carWidth / 2 + margin : across / 2 + carWidth * LINE_COVER;
        final double high = overtaking ? across / 2 - carWidth * LINE_COVER : across - edge - carWidth / 2 - margin;
        for (int attempt = 0; attempt < 8; attempt++) {
            final double distance = length * (NEAREST_START + (FARTHEST_START - NEAREST_START) * random.nextDouble());
            final double along = overtaking ? -distance - carLength / 2 : length + distance + carLength / 2;
            final double where = low + (high - low) * random.nextDouble();
            if (isFree(along, where)) {
                final Mobile car = new Mobile(Kind.CAR, along, where, carLength, carWidth, overtaking ? 1 : -1,
                    "car-" + (1 + random.nextInt(CAR_PICTURES)) + ".png");
                mobiles.add(car);
                carsLayer.getChildren().add(car.view);
                car.place();
                return car;
            }
        }
        return null;
    }

    /**
     * @return whether a new car fits there, with some room around it
     */
    private boolean isFree(final double along, final double where) {
        for (final Mobile mobile : mobiles) {
            if (Math.abs(mobile.along - along) < (mobile.length + carLength) / 2 + carLength * 0.5
                && Math.abs(mobile.across - where) < (mobile.width + carWidth) / 2 + carWidth * 0.2) {
                return false;
            }
        }
        return true;
    }

    /**
     * A car or a barrier touched (when not invincible): one life less, the road back to its starting speed, a turn of the
     * car and a while of invincibility; the car hit is crashed and removed, a barrier stays.
     */
    private void hitSomething() {
        if (invincible > 0) {
            return;
        }
        for (final Mobile mobile : mobiles) {
            if (player.touches(mobile)) {
                hit(mobile);
                return;
            }
        }
    }

    /**
     * Hits a car or a barrier.
     */
    void hit(final Mobile mobile) {
        if (phase != Phase.PLAY) {
            return;
        }
        if (mobile.kind == Kind.CAR) {
            showCrash(mobile.along, mobile.across);
            carsLayer.getChildren().remove(mobile.view);
            mobiles.remove(mobile);
        }
        lives--;
        speed = level.getStartSpeed();
        invincible = INVINCIBLE;
        spin = SPIN;
        showHud();
        if (lives <= 0) {
            showCrash(player.along, player.across);
            lose();
        }
    }

    private void showCrash(final double along, final double where) {
        final ImageView view = new ImageView(gameImage(DATA + "crash.png"));
        final double size = carLength * 1.1;
        view.setFitWidth(size);
        view.setFitHeight(size);
        final double[] center = toScreen(along, where);
        view.setLayoutX(center[0] - size / 2);
        view.setLayoutY(center[1] - size / 2);
        carsLayer.getChildren().add(view);
        crashes.add(new double[]{CRASH});
        crashViews.add(view);
    }

    private void updateCrashes(final double seconds) {
        for (int i = crashes.size() - 1; i >= 0; i--) {
            crashes.get(i)[0] -= seconds;
            if (crashes.get(i)[0] <= 0) {
                carsLayer.getChildren().remove(crashViews.get(i));
                crashes.remove(i);
                crashViews.remove(i);
            } else {
                crashViews.get(i).setOpacity(Math.min(1, crashes.get(i)[0] / (CRASH * 0.4)));
            }
        }
    }

    private void countPoints(final double seconds) {
        played += seconds;
        final int points = (int) (played / SECONDS_PER_POINT);
        if (points > score) {
            for (int point = score; point < points; point++) {
                stats.incrementNumberOfGoalsReached();
            }
            score = points;
            showHud();
        }
    }

    /**
     * @return the place on the screen of a place along and across the road
     */
    private double[] toScreen(final double along, final double where) {
        return road == GazeRaceVariant.HORIZONTAL ? new double[]{along, where} : new double[]{where, height - along};
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final double margin = height * 0.025;
        final double heart = height * 0.055;
        // The lives: the hearts shared by the reviewed games (GameStyle.lives)
        final Group hearts = GameStyle.lives(lives, LIVES, heart, width * 0.3, false);
        hearts.setLayoutX(margin);
        hearts.setLayoutY(margin * 0.6);
        hud.getChildren().add(hearts);
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("GazeRaceScore") + " " + score,
            fontSize);
        pill.setLayoutX(width - pill.getBoundsInLocal().getWidth() - margin);
        pill.setLayoutY(margin * 0.6);
        hud.getChildren().add(pill);
        if (end.getSeconds() > 0) {
            final int seconds = (int) Math.ceil(timeLeft);
            final Group clock = GameStyle.pill(String.format("%d:%02d", seconds / 60, seconds % 60), fontSize);
            clock.setLayoutX((width - clock.getBoundsInLocal().getWidth()) / 2);
            clock.setLayoutY(margin * 0.6);
            hud.getChildren().add(clock);
        }
    }

    private void win() {
        if (phase != Phase.PLAY) {
            return;
        }
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
    }

    private void lose() {
        phase = Phase.OVER;
        loop.stop();
        player.view.setVisible(false);
        stats.takeScreenShot();
        final Group card = GameStyle.card(gameContext.getTranslator().translate("GazeRaceLost") + "\n"
            + gameContext.getTranslator().translate("GazeRaceScore") + " " + score, height * 0.06, width * 0.6);
        card.setLayoutX(width / 2 - card.getBoundsInLocal().getWidth() / 2);
        card.setLayoutY(height / 2 - card.getBoundsInLocal().getHeight() / 2);
        hud.getChildren().add(card);
        lostPause = new PauseTransition(Duration.seconds(2.5));
        lostPause.setOnFinished(event -> gameContext.showRoundStats(stats, this));
        lostPause.play();
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    /**
     * @return the cars on the road, without the barriers
     */
    List<Mobile> getCars() {
        return mobiles.stream().filter(mobile -> mobile.kind == Kind.CAR).toList();
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (lostPause != null) {
            lostPause.stop();
        }
        if (area != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(area);
        }
        mobiles.clear();
        dashes.clear();
        crashes.clear();
        crashViews.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    enum Kind { PLAYER, CAR, BARRIER }

    /**
     * The car of the player, another car or a barrier: its middle along and across the road, its size along and across
     * it, the way it goes by (1: towards the end of the road, -1: towards its start, 0: the player) and its picture,
     * which faces the way it goes.
     */
    final class Mobile {
        @Getter
        private final Kind kind;
        @Getter
        private double along;
        @Getter
        private double across;
        @Getter
        private final double length;
        @Getter
        private final double width;
        @Getter
        private final int direction;
        private final ImageView view;

        Mobile(final Kind kind, final double along, final double across, final double length, final double width,
               final int direction, final String picture) {
            this.kind = kind;
            this.along = along;
            this.across = across;
            this.length = length;
            this.width = width;
            this.direction = direction;
            view = new ImageView(gameImage(DATA + picture));
            view.setFitWidth(length);
            view.setFitHeight(width);
            view.setMouseTransparent(true);
        }

        void place() {
            final double[] center = toScreen(along, across);
            view.setLayoutX(center[0] - length / 2);
            view.setLayoutY(center[1] - width / 2);
            // The pictures face right, the end of the horizontal road; a car coming the other way faces its start
            final double facing = kind == Kind.CAR && direction < 0 ? 180 : 0;
            final double turn = kind == Kind.PLAYER && spin > 0 ? 360 * (1 - spin / SPIN) : 0;
            view.setRotate((road == GazeRaceVariant.HORIZONTAL ? 0 : -90) + facing + turn);
        }

        /**
         * @return how fast it goes on the screen, in speeds of the road: 1 forward, -1 back with the road, -2 for a car
         * coming the other way
         */
        double screenSpeed() {
            return kind == Kind.CAR && direction < 0 ? -2 : direction;
        }

        /**
         * @return whether this one touches another, by the parts of them that hurt
         */
        boolean touches(final Mobile other) {
            return Math.abs(along - other.along) < (length + other.length) / 2 * HIT_BOX
                && Math.abs(across - other.across) < (width + other.width) / 2 * HIT_BOX;
        }

        /**
         * For the tests: puts it at a place along and across the road.
         */
        void moveTo(final double newAlong, final double newAcross) {
            along = newAlong;
            across = newAcross;
            place();
        }
    }
}
