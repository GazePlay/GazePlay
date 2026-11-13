package net.gazeplay.games.room;

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
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
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
import java.util.List;

/**
 * "La pièce" (name code Room; reviewed on 13/11/2026, maintainer's rules): rooms of a house seen wall by wall, in 2D,
 * walls, floors and furniture drawn by the game, the objects OpenMoji pictures (see {@link RoomObject}). Two round
 * arrows at the sides: a fixation on one slides the view to the next wall (after the fourth, the first again); a small
 * compass at the top tells which wall is seen.
 * <p>
 * The search: a card asks for an object ("Où est le lit ?", with its picture or its name only); fixed, it jumps, a star
 * and "Bravo !", then another question (never the same object twice in a row; in "all the rooms" in a room drawn at
 * random, reached by a fade); another object shakes, shows its name and counts a mistake (no lives); after 15 s without
 * finding it, the arrow leading to it the soonest blinks. The game ends after 5, 10 or 20 objects ("Bravo", then the
 * statistics), or has no end. The exploration: each object fixed jumps and shows its name, without end.
 * <p>
 * One loop, in real seconds: the fixation lasts the length of the settings (read at each step), nothing can be chosen
 * before the delay of the settings, while the view slides, nor while the menu of the game is open. (Before the review:
 * a room in 3D, which some graphic cards do not show, turned only while the mouse or the gaze moved on an arrow, the
 * speed depending on the number of gaze points; no goal, no end, pictures of unknown source, nothing removed at the
 * end.)
 */
@Slf4j
public class Room implements GameLifeCycle {

    /**
     * The seconds the view slides, an object jumps, shakes and shows its name, the reward lasts, and before the hint.
     */
    static final double TURN = 0.5;
    static final double JUMP = 0.5;
    static final double SHAKE = 0.5;
    static final double NAME = 1.6;
    static final double REWARD = 1.8;
    static final double HINT = 15;

    enum Phase { WAIT, PLAY, TURN, REWARD, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final RoomMode mode;
    @Getter
    private final RoomPlace choice;
    @Getter
    private final RoomEnd end;
    @Getter
    private final RoomLevel level;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private RoomPlace place;
    @Getter
    private int wall;
    @Getter
    private RoomObject target;
    @Getter
    private int found;
    private double elapsed;
    private double phaseTime;
    private double hintTime;
    private boolean rewarded;
    private int turning;

    private double width;
    private double height;
    private double floorY;

    private Group layer;
    private Group views;
    private WallView view;
    private WallView leaving;
    private Group card;
    private Group hud;
    private Group reward;
    private FixationRing ring;
    private Spot left;
    private Spot right;
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
     * @param gameSeed the seed of the game to replay, or a negative value for a new game
     */
    Room(final IGameContext gameContext, final Stats stats, final RoomMode mode, final RoomPlace choice,
         final RoomEnd end, final RoomLevel level, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.choice = mode == RoomMode.EXPLORE && choice == RoomPlace.ALL ? RoomPlace.LIVING : choice;
        this.end = mode == RoomMode.EXPLORE ? RoomEnd.ENDLESS : end;
        this.level = level;
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
        floorY = height * 0.8;
        views = new Group();
        final Group arrows = new Group();
        card = new Group();
        card.setMouseTransparent(true);
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        reward = new Group();
        reward.setMouseTransparent(true);
        final Rectangle clip = new Rectangle(0, 0, width, height);
        views.setClip(clip);
        layer = new Group(views, arrows, card, hud, reward, ring);
        gameContext.getChildren().add(layer);
        gameContext.getGazeDeviceManager().addStats(stats);
        left = arrow(arrows, -1);
        right = arrow(arrows, 1);
        found = 0;
        place = choice == RoomPlace.ALL ? RoomPlace.LIVING : choice;
        wall = 0;
        if (mode == RoomMode.SEARCH) {
            pickTarget();
            if (target.getPlace() != place) {
                place = target.getPlace();
            }
        } else {
            stats.notifyNewRoundReady();
        }
        view = new WallView(place, wall);
        views.getChildren().add(view.node);
        view.listen();
        showCard();
        showHud();
        phase = Phase.WAIT;
        phaseTime = 0;
        elapsed = 0;
        if (!manual) {
            loop.start();
        }
    }

    private double buttonRadius() {
        return Math.min(height * 0.075, width * 0.045);
    }

    /**
     * A round arrow at a side, to turn the view to the next wall.
     */
    private Spot arrow(final Group arrows, final int direction) {
        final double r = buttonRadius();
        final double x = direction < 0 ? r + width * 0.02 : width - r - width * 0.02;
        final double y = height * 0.5;
        final Circle disc = new Circle(x, y, r);
        disc.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        final SVGPath icon = GameStyle.icon(direction < 0 ? "arrow_back" : "arrow_forward", r);
        icon.setStyle("-fx-fill: -gp-accent;");
        final Group iconGroup = new Group(icon);
        iconGroup.setLayoutX(x - r / 2);
        iconGroup.setLayoutY(y - r / 2);
        iconGroup.setMouseTransparent(true);
        final Group node = new Group(disc, iconGroup);
        arrows.getChildren().add(node);
        final Spot spot = new Spot(direction, null, disc, node, x, y, r * 2.5);
        spot.listen();
        return spot;
    }

    // --- The drawings ---

    /**
     * A wall of a room and its objects, built when it is shown.
     */
    private final class WallView {
        private final Group node = new Group();
        private final List<Spot> spots = new ArrayList<>();

        WallView(final RoomPlace room, final int index) {
            final Color color = room.getWall();
            final Rectangle back = new Rectangle(0, 0, width, floorY);
            back.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, color.brighter()), new Stop(1, color)));
            final Rectangle floor = new Rectangle(0, floorY, width, height - floorY);
            final Color floorColor = room.isTiles() ? Color.rgb(226, 222, 214) : Color.rgb(196, 150, 102);
            floor.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, floorColor.darker()),
                new Stop(1, floorColor)));
            node.getChildren().addAll(back, floor);
            final Color line = room.isTiles() ? Color.rgb(180, 176, 168) : Color.rgb(150, 108, 70);
            if (room.isTiles()) {
                for (int i = 1; i < 16; i++) {
                    final double x = width * i / 16;
                    node.getChildren().add(lineOf(x, floorY, x + (x - width / 2) * 0.3, height, line));
                }
                for (int i = 1; i < 4; i++) {
                    final double y = floorY + (height - floorY) * i / 4;
                    node.getChildren().add(lineOf(0, y, width, y, line));
                }
                // Tiles on the lower part of the wall
                for (int i = 0; i <= 20; i++) {
                    node.getChildren().add(lineOf(width * i / 20, floorY - height * 0.18, width * i / 20, floorY,
                        color.darker()));
                }
                node.getChildren().add(lineOf(0, floorY - height * 0.18, width, floorY - height * 0.18,
                    color.darker()));
            } else {
                for (int i = 1; i < 5; i++) {
                    final double y = floorY + (height - floorY) * i / 5;
                    node.getChildren().add(lineOf(0, y, width, y, line));
                }
            }
            final Rectangle plinth = new Rectangle(0, floorY - height * 0.018, width, height * 0.018);
            plinth.setFill(color.darker());
            node.getChildren().add(plinth);
            final List<RoomObject> objects = room.objects();
            final RoomObject first = objects.get(index * 2);
            final RoomObject second = objects.get(index * 2 + 1);
            final boolean withCounter = first.getKind() == RoomObject.Kind.COUNTER
                || second.getKind() == RoomObject.Kind.COUNTER;
            if (withCounter) {
                counter();
            }
            for (final RoomObject object : List.of(first, second)) {
                // Over a counter, a shelf higher on the wall
                spots.add(object(object, withCounter ? height * 0.36 : height * 0.52));
            }
            node.setMouseTransparent(false);
        }

        private Line lineOf(final double x1, final double y1, final double x2, final double y2, final Color color) {
            final Line l = new Line(x1, y1, x2, y2);
            l.setStroke(color);
            l.setStrokeWidth(Math.max(1, height * 0.002));
            l.setMouseTransparent(true);
            return l;
        }

        private void counter() {
            final double top = floorY - height * 0.24;
            final Rectangle body = new Rectangle(width * 0.12, top, width * 0.76, floorY - top);
            body.setFill(Color.rgb(240, 236, 228));
            body.setStroke(Color.rgb(180, 170, 158));
            final Rectangle worktop = new Rectangle(width * 0.11, top - height * 0.02, width * 0.78, height * 0.025);
            worktop.setFill(Color.rgb(150, 110, 80));
            node.getChildren().addAll(body, worktop);
            for (int i = 1; i < 4; i++) {
                final double x = width * 0.12 + width * 0.76 * i / 4;
                node.getChildren().add(lineOf(x, top + height * 0.02, x, floorY - height * 0.02,
                    Color.rgb(180, 170, 158)));
            }
        }

        /**
         * An object at its place on the wall, with the furniture it stands on.
         */
        private Spot object(final RoomObject object, final double shelfY) {
            final Image image = new Image(object.picture());
            final double h = object.getSize() * height;
            final double w = image.getHeight() > 0 ? h * image.getWidth() / image.getHeight() : h;
            final double x = width * (object.slot() == 0 ? 0.32 : 0.68);
            final double bottom;
            switch (object.getKind()) {
                case FLOOR -> bottom = floorY + h * 0.03;
                case WALL -> bottom = height * 0.42 + h / 2;
                case SHELF -> {
                    bottom = shelfY;
                    final double shelfWidth = Math.max(w * 1.6, height * 0.22);
                    final Rectangle shelf = new Rectangle(x - shelfWidth / 2, bottom, shelfWidth, height * 0.02);
                    shelf.setFill(Color.rgb(160, 112, 70));
                    final Polygon bracketLeft = new Polygon(x - shelfWidth * 0.35, bottom + height * 0.02,
                        x - shelfWidth * 0.35, bottom + height * 0.06, x - shelfWidth * 0.28, bottom + height * 0.02);
                    final Polygon bracketRight = new Polygon(x + shelfWidth * 0.35, bottom + height * 0.02,
                        x + shelfWidth * 0.35, bottom + height * 0.06, x + shelfWidth * 0.28, bottom + height * 0.02);
                    bracketLeft.setFill(Color.rgb(120, 84, 52));
                    bracketRight.setFill(Color.rgb(120, 84, 52));
                    node.getChildren().addAll(shelf, bracketLeft, bracketRight);
                }
                case TABLE -> {
                    bottom = floorY - height * 0.2;
                    final double tableWidth = Math.max(w * 1.7, height * 0.26);
                    final Rectangle top = new Rectangle(x - tableWidth / 2, bottom, tableWidth, height * 0.025);
                    top.setFill(Color.rgb(160, 112, 70));
                    final Rectangle legLeft = new Rectangle(x - tableWidth * 0.42, bottom, height * 0.018,
                        floorY - bottom);
                    final Rectangle legRight = new Rectangle(x + tableWidth * 0.42 - height * 0.018, bottom,
                        height * 0.018, floorY - bottom);
                    legLeft.setFill(Color.rgb(120, 84, 52));
                    legRight.setFill(Color.rgb(120, 84, 52));
                    node.getChildren().addAll(legLeft, legRight, top);
                }
                case COUNTER -> bottom = floorY - height * 0.26;
                case CEILING -> {
                    bottom = height * 0.26 + h;
                    final Line cord = lineOf(x, 0, x, height * 0.26 + h * 0.1, Color.rgb(80, 70, 70));
                    cord.setStrokeWidth(Math.max(2, height * 0.004));
                    node.getChildren().add(cord);
                }
                default -> bottom = floorY;
            }
            final ImageView picture = new ImageView(image);
            picture.setFitWidth(w);
            picture.setFitHeight(h);
            picture.setX(-w / 2);
            picture.setY(-h);
            picture.setMouseTransparent(true);
            // The zone looked at: the picture, a little larger
            final Rectangle zone = new Rectangle(-w / 2 - h * 0.08, -h * 1.08, w + h * 0.16, h * 1.16);
            zone.setFill(Color.TRANSPARENT);
            final Group holder = new Group(zone, picture);
            holder.setLayoutX(x);
            holder.setLayoutY(bottom);
            node.getChildren().add(holder);
            return new Spot(0, object, zone, holder, x, bottom - h / 2, Math.max(h, w) * 1.15);
        }

        void listen() {
            for (final Spot spot : spots) {
                spot.listen();
            }
        }

        void forget() {
            for (final Spot spot : spots) {
                gameContext.getGazeDeviceManager().removeEventFilter(spot.zone);
                if (looked == spot) {
                    lookAway(spot);
                }
            }
        }
    }

    private void showHud() {
        hud.getChildren().clear();
        final double size = height * 0.03;
        if (mode == RoomMode.SEARCH) {
            final Group count = GameStyle.pill(gameContext.getTranslator().translate("RoomFound") + " " + found
                + (end.getObjects() > 0 ? " / " + end.getObjects() : ""), size);
            count.setLayoutX(height * 0.03);
            count.setLayoutY(height * 0.025);
            hud.getChildren().add(count);
        }
        // The compass: the four walls, the one seen filled
        final double r = height * 0.014;
        final double cx = width - height * 0.03 - r * 11.4;
        final double cy = height * 0.025 + r * 2.2;
        final Rectangle backing = new Rectangle(cx - r * 2.4, cy - r * 2.2, r * 13.8, r * 4.4);
        backing.setArcWidth(r * 4.4);
        backing.setArcHeight(r * 4.4);
        backing.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        hud.getChildren().add(backing);
        for (int i = 0; i < 4; i++) {
            final Circle dot = new Circle(cx + i * r * 3, cy, r);
            dot.setStyle(i == wall ? "-fx-fill: -gp-accent;" : "-fx-fill: transparent; -fx-stroke: -gp-border;"
                + " -fx-stroke-width: 2;");
            hud.getChildren().add(dot);
        }
    }

    /**
     * The card of the question at the top: the picture of the object (with the picture level) and the question.
     */
    private void showCard() {
        card.getChildren().clear();
        if (mode != RoomMode.SEARCH || target == null) {
            return;
        }
        final Group question = GameStyle.pill(gameContext.getTranslator().translate(target.questionKey()),
            height * 0.036);
        final double qw = question.getBoundsInLocal().getWidth();
        final double qh = question.getBoundsInLocal().getHeight();
        final double pic = level == RoomLevel.PICTURE ? height * 0.1 : 0;
        final double pad = height * 0.015;
        final double cardWidth = qw + (pic > 0 ? pic + pad : 0) + 2 * pad;
        final double cardHeight = Math.max(qh, pic) + 2 * pad;
        final double cardLeft = (width - cardWidth) / 2;
        final double cardTop = height * 0.02;
        final Rectangle frame = GameStyle.targetCard(cardLeft, cardTop, cardWidth, cardHeight);
        card.getChildren().add(frame);
        double x = cardLeft + pad;
        if (pic > 0) {
            final Image image = new Image(target.picture());
            final ImageView picture = new ImageView(image);
            picture.setFitHeight(pic);
            picture.setFitWidth(pic);
            picture.setPreserveRatio(true);
            picture.setX(x);
            picture.setY(cardTop + pad);
            card.getChildren().add(picture);
            x += pic + pad;
        }
        question.setLayoutX(x);
        question.setLayoutY(cardTop + (cardHeight - qh) / 2);
        card.getChildren().add(question);
    }

    // --- The game ---

    /**
     * Another object to find, never the one just found: in the room chosen, or in all of them.
     */
    private void pickTarget() {
        final List<RoomObject> candidates = choice == RoomPlace.ALL ? List.of(RoomObject.values()) : choice.objects();
        RoomObject next;
        do {
            next = candidates.get(random.nextInt(candidates.size()));
        } while (next == target);
        target = next;
        hintTime = 0;
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        phaseTime += seconds;
        if (view != null) {
            for (final Spot spot : view.spots) {
                spot.animate(seconds);
            }
        }
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    setPhase(Phase.PLAY);
                }
            }
            case PLAY -> {
                fixate(seconds);
                hint(seconds);
            }
            case TURN -> slide();
            case REWARD -> {
                if (!rewarded) {
                    rewarded = true;
                    showReward();
                }
                if (phaseTime >= REWARD) {
                    reward.getChildren().clear();
                    if (end.getObjects() > 0 && found >= end.getObjects()) {
                        finish();
                    } else {
                        nextQuestion();
                    }
                }
            }
            default -> {
            }
        }
    }

    private void setPhase(final Phase next) {
        phase = next;
        phaseTime = 0;
    }

    /**
     * After a while without finding the object, the arrow leading to it the soonest blinks.
     */
    private void hint(final double seconds) {
        left.node.setOpacity(1);
        right.node.setOpacity(1);
        if (mode != RoomMode.SEARCH || target == null || target.wall() == wall) {
            return;
        }
        hintTime += seconds;
        if (hintTime >= HINT) {
            final Spot arrow = isHintRight() ? right : left;
            arrow.node.setOpacity(((int) (hintTime * 3)) % 2 == 0 ? 1 : 0.35);
        }
    }

    /**
     * @return whether the arrow at the right leads to the object the soonest
     */
    boolean isHintRight() {
        return (target.wall() - wall + 4) % 4 != 3;
    }

    boolean isHintShown() {
        return mode == RoomMode.SEARCH && target != null && target.wall() != wall && hintTime >= HINT;
    }

    /**
     * The next question: in the room seen, or in another room reached by a fade.
     */
    private void nextQuestion() {
        pickTarget();
        showCard();
        showHud();
        if (target.getPlace() != place) {
            changeView(target.getPlace(), 0, 0);
        } else {
            setPhase(Phase.PLAY);
        }
    }

    /**
     * Slides (direction -1 or 1) or fades (direction 0) to another wall.
     */
    private void changeView(final RoomPlace room, final int index, final int direction) {
        view.forget();
        leaving = view;
        place = room;
        wall = index;
        turning = direction;
        view = new WallView(room, index);
        view.node.setTranslateX(direction * width);
        view.node.setOpacity(direction == 0 ? 0 : 1);
        views.getChildren().add(view.node);
        showHud();
        setPhase(Phase.TURN);
    }

    private void slide() {
        final double u = Math.min(1, phaseTime / TURN);
        final double eased = 1 - (1 - u) * (1 - u);
        if (turning == 0) {
            leaving.node.setOpacity(1 - eased);
            view.node.setOpacity(eased);
        } else {
            leaving.node.setTranslateX(-turning * width * eased);
            view.node.setTranslateX(turning * width * (1 - eased));
        }
        if (u >= 1) {
            views.getChildren().remove(leaving.node);
            leaving = null;
            view.node.setTranslateX(0);
            view.node.setOpacity(1);
            view.listen();
            hintTime = Math.min(hintTime, HINT * 0.5);
            setPhase(Phase.PLAY);
        }
    }

    private void fixate(final double seconds) {
        if (looked == null || Stats.isConfigMenuOpen()) {
            return;
        }
        if (!ring.isVisible()) {
            ring.setSize(looked.ringSize);
            ring.show(looked.x, looked.y);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Spot spot = looked;
            ring.stop();
            looked = null;
            progress = 0;
            choose(spot);
        }
    }

    void lookAt(final Spot spot) {
        if (looked == spot) {
            return;
        }
        looked = spot;
        progress = 0;
        ring.stop();
    }

    void lookAway(final Spot spot) {
        if (looked == spot) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * An arrow or an object chosen: the view turned, or the object found, named or wrong.
     */
    void choose(final Spot spot) {
        if (phase != Phase.PLAY) {
            return;
        }
        if (spot.object == null) {
            changeView(place, (wall + spot.direction + 4) % 4, spot.direction);
            return;
        }
        if (mode == RoomMode.EXPLORE) {
            spot.jump = JUMP;
            spot.showName();
            return;
        }
        if (spot.object == target) {
            found++;
            stats.incrementNumberOfGoalsReached();
            gameContext.getSoundManager().add("data/goosegame/sounds/win.wav");
            spot.jump = JUMP;
            rewarded = false;
            showHud();
            setPhase(Phase.REWARD);
        } else {
            stats.incrementNumberOfMistakes();
            gameContext.getSoundManager().add("data/dice/sounds/wrong.wav");
            spot.shake = SHAKE;
            spot.showName();
        }
    }

    /**
     * A star beside "Bravo !" and the name of the object, under the card.
     */
    private void showReward() {
        final double r = height * 0.045;
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("RoomWell") + " "
            + gameContext.getTranslator().translate(target.nameKey()), height * 0.042);
        final double pillWidth = pill.getBoundsInLocal().getWidth();
        final double pillHeight = pill.getBoundsInLocal().getHeight();
        final double gap = r * 0.4;
        final double startX = width / 2 - (2 * r + gap + pillWidth) / 2;
        final double cy = height * 0.22;
        final Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            final double a = -Math.PI / 2 + i * Math.PI / 5;
            final double d = i % 2 == 0 ? r : r * 0.45;
            star.getPoints().addAll(startX + r + d * Math.cos(a), cy + r * 0.08 + d * Math.sin(a));
        }
        star.setFill(Color.rgb(255, 200, 40));
        star.setStroke(Color.rgb(200, 120, 0));
        star.setStrokeWidth(Math.max(2, r * 0.07));
        pill.setLayoutX(startX + 2 * r + gap);
        pill.setLayoutY(cy - pillHeight / 2);
        reward.getChildren().setAll(star, pill);
    }

    private void finish() {
        setPhase(Phase.OVER);
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
     * @return the spots of the wall seen, then the left and right arrows, for the tests
     */
    List<Spot> getSpots() {
        final List<Spot> spots = new ArrayList<>(view.spots);
        spots.add(left);
        spots.add(right);
        return spots;
    }

    Spot spot(final RoomObject object) {
        return view.spots.stream().filter(s -> s.object == object).findFirst().orElseThrow();
    }

    Spot getLeft() {
        return left;
    }

    Spot getRight() {
        return right;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (view != null) {
            view.forget();
        }
        if (left != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(left.zone);
            gameContext.getGazeDeviceManager().removeEventFilter(right.zone);
        }
        if (ring != null) {
            ring.stop();
        }
        looked = null;
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * Something to look at: an arrow (a direction) or an object of the wall seen; an object jumps, shakes and shows its
     * name.
     */
    final class Spot {
        private final int direction;
        @Getter
        private final RoomObject object;
        private final Node zone;
        private final Group node;
        private final double x;
        private final double y;
        private final double ringSize;
        private double jump;
        private double shake;
        private double name;
        private Group namePill;

        Spot(final int direction, final RoomObject object, final Node zone, final Group node, final double x,
             final double y, final double ringSize) {
            this.direction = direction;
            this.object = object;
            this.zone = zone;
            this.node = node;
            this.x = x;
            this.y = y;
            this.ringSize = ringSize;
        }

        void listen() {
            final EventHandler<Event> handler = event -> {
                final var type = event.getEventType();
                if (type == MouseEvent.MOUSE_ENTERED || type == GazeEvent.GAZE_ENTERED) {
                    lookAt(this);
                } else if (type == MouseEvent.MOUSE_EXITED || type == GazeEvent.GAZE_EXITED) {
                    lookAway(this);
                }
            };
            zone.addEventFilter(MouseEvent.ANY, handler);
            zone.addEventFilter(GazeEvent.ANY, handler);
            gameContext.getGazeDeviceManager().addEventFilter(zone);
        }

        void showName() {
            name = NAME;
            if (namePill != null) {
                node.getChildren().remove(namePill);
            }
            namePill = GameStyle.pill(gameContext.getTranslator().translate(object.nameKey()), height * 0.032);
            namePill.setMouseTransparent(true);
            final double h = object.getSize() * height;
            namePill.setLayoutX(-namePill.getBoundsInLocal().getWidth() / 2);
            namePill.setLayoutY(-h * 1.1 - namePill.getBoundsInLocal().getHeight());
            node.getChildren().add(namePill);
        }

        void animate(final double seconds) {
            if (jump > 0) {
                jump = Math.max(0, jump - seconds);
                final double u = 1 - jump / JUMP;
                node.setTranslateY(-Math.sin(Math.PI * u) * height * 0.04);
            }
            if (shake > 0) {
                shake = Math.max(0, shake - seconds);
                node.setTranslateX(shake == 0 ? 0 : Math.sin(shake * 40) * height * 0.012);
            }
            if (name > 0) {
                name = Math.max(0, name - seconds);
                if (name == 0 && namePill != null) {
                    node.getChildren().remove(namePill);
                    namePill = null;
                }
            }
        }

        boolean isJumping() {
            return jump > 0;
        }

        boolean isShaking() {
            return shake > 0;
        }

        boolean isNameShown() {
            return namePill != null;
        }

        String getName() {
            if (namePill == null) {
                return null;
            }
            for (final Node child : namePill.getChildren()) {
                if (child instanceof Text text) {
                    return text.getText();
                }
            }
            return null;
        }
    }
}
