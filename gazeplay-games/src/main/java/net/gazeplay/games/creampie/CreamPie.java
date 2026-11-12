package net.gazeplay.games.creampie;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * "Tartes à la crème" (name code Creampie; reviewed on 12/11/2026, maintainer's rules): a round target with a picture
 * (an animal, or a photo of the user: of the family, of the school) appears in the top of the screen; after a fixation
 * of the length of the settings, or as soon as it is looked at, the hand at the bottom throws a cream pie at it, on an
 * arc. The pie really splats on the picture: cream with drips and a cherry, the picture shakes; it stays covered a
 * moment, fades, and another target appears elsewhere. The targets may glide slowly, to be followed by the gaze. The
 * game ends after 10, 20 or 30 pies ("Bravo", then the statistics), or has no end.
 * <p>
 * One loop, in real seconds: nothing is thrown before the delay of the settings nor while the menu of the game is open.
 * (Before the review: a hand, a pie and a sound of unknown source, of sizes in pixels; the target grew, turned and
 * vanished while the pie flew, so that the pie never landed; no pause, no end, no variant.)
 */
@Slf4j
public class CreamPie implements GameLifeCycle {

    /**
     * The seconds a target takes to appear, the pie to fly, the target stays covered with cream, then fades.
     */
    static final double APPEAR = 0.25;
    static final double THROW = 0.6;
    static final double SPLAT = 1.1;
    static final double FADE = 0.35;
    /**
     * The speed of the moving targets, as a part of the height of the screen per second.
     */
    static final double SPEED = 0.12;

    private static final String IMAGES = "data/creampie/images/";
    private static final String SOUNDS = "data/creampie/sounds/";

    enum Phase { WAIT, APPEAR, AIM, THROW, SPLAT, FADE, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final CreamPiePictures pictures;
    @Getter
    private final CreamPieSize size;
    @Getter
    private final CreamPieThrow way;
    @Getter
    private final CreamPieMotion motion;
    @Getter
    private final CreamPieEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int pies;
    private double elapsed;
    private double phaseTime;
    @Getter
    private boolean looked;
    @Getter
    private double progress;

    private double width;
    private double height;
    private double radius;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double handX;
    private double handY;
    private double pieSize;
    private ImageLibrary animals;
    private List<File> userImages;
    private int lastImage = -1;

    private Group layer;
    private Group targetLayer;
    private Group hud;
    private FixationRing ring;
    private ImageView hand;
    private double handRest;
    @Getter
    private ImageView pie;
    @Getter
    private Group target;
    @Getter
    private Group cream;
    private Circle zone;
    @Getter
    private double targetX;
    @Getter
    private double targetY;
    private double velocityX;
    private double velocityY;
    private double fromX;
    private double fromY;
    private final ArrayList<TargetAOI> targetAOIList = new ArrayList<>();

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
    CreamPie(final IGameContext gameContext, final Stats stats, final CreamPiePictures pictures,
             final CreamPieSize size, final CreamPieThrow way, final CreamPieMotion motion, final CreamPieEnd end,
             final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.pictures = pictures;
        this.size = size;
        this.way = way;
        this.motion = motion;
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
        radius = height * size.getRadius();

        final Rectangle room = new Rectangle(0, 0, width, height);
        room.setStyle("-fx-fill: -gp-bg;");
        room.setMouseTransparent(true);

        // The hand at the bottom, the pie on it
        final double handSize = height * 0.24;
        hand = new ImageView(new Image(IMAGES + "hand.png"));
        hand.setFitWidth(handSize);
        hand.setFitHeight(handSize);
        hand.setSmooth(true);
        hand.setMouseTransparent(true);
        handRest = height - handSize * 0.92;
        hand.setLayoutX((width - handSize) / 2);
        hand.setLayoutY(handRest);
        handX = width / 2;
        handY = handRest + handSize * 0.3;
        pieSize = height * 0.13;
        pie = new ImageView(new Image(IMAGES + "pie.png"));
        pie.setFitWidth(pieSize);
        pie.setFitHeight(pieSize);
        pie.setSmooth(true);
        pie.setMouseTransparent(true);
        placePie(handX, handY, 1, 0);

        // Where the targets go: under the band, above the hand
        final double margin = height * 0.03;
        minX = radius + margin;
        maxX = width - radius - margin;
        minY = height * 0.12 + radius;
        maxY = Math.max(minY, handRest - margin - radius);

        targetLayer = new Group();
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(room, targetLayer, ring, hand, pie, hud);
        gameContext.getChildren().add(layer);
        gameContext.getGazeDeviceManager().addStats(stats);

        if (pictures == CreamPiePictures.MY_IMAGES) {
            userImages = UserImagesVariant.images(CreamPiePictures.USER_IMAGES_FOLDER);
            if (userImages.isEmpty()) {
                // The game started without going through the menu, which asks for images
                final Group message = GameStyle.card(gameContext.getTranslator().translate("NoUserImages"),
                    height * 0.035, width * 0.5);
                message.setLayoutX((width - message.getBoundsInLocal().getWidth()) / 2);
                message.setLayoutY((height - message.getBoundsInLocal().getHeight()) / 2);
                hud.getChildren().add(message);
                return;
            }
        }
        animals = ImageUtils.createDefaultImageLibrary(null, random);
        pies = 0;
        showScore();
        newTarget();
        phase = Phase.WAIT;
        elapsed = 0;
        if (!manual) {
            loop.start();
        }
    }

    private void placePie(final double cx, final double cy, final double scale, final double angle) {
        pie.setLayoutX(cx - pieSize / 2);
        pie.setLayoutY(cy - pieSize / 2);
        pie.setScaleX(scale);
        pie.setScaleY(scale);
        pie.setRotate(angle);
    }

    /**
     * A new target, somewhere else than the one before, with a picture, ready to appear.
     */
    private void newTarget() {
        removeTarget();
        final double oldX = targetX;
        final double oldY = targetY;
        double x = 0;
        double y = 0;
        for (int i = 0; i < 20; i++) {
            x = minX + random.nextDouble() * (maxX - minX);
            y = minY + random.nextDouble() * (maxY - minY);
            if (Math.hypot(x - oldX, y - oldY) > radius * 2.5) {
                break;
            }
        }
        targetX = x;
        targetY = y;
        final double angle = random.nextDouble() * 2 * Math.PI;
        velocityX = Math.cos(angle) * SPEED * height;
        velocityY = Math.sin(angle) * SPEED * height;

        final Image image = nextImage();
        final Circle back = new Circle(radius, Color.WHITE);
        final ImageView view = new ImageView(image);
        if (image != null) {
            final double side = Math.min(image.getWidth(), image.getHeight());
            view.setViewport(new Rectangle2D((image.getWidth() - side) / 2, (image.getHeight() - side) / 2, side,
                side));
        }
        view.setFitWidth(radius * 2);
        view.setFitHeight(radius * 2);
        view.setSmooth(true);
        view.setLayoutX(-radius);
        view.setLayoutY(-radius);
        view.setClip(new Circle(radius, radius, radius));
        final Circle frame = new Circle(radius);
        frame.setFill(null);
        frame.setStyle("-fx-stroke: -gp-accent;");
        frame.setStrokeWidth(Math.max(3, radius * 0.07));
        cream = new Group();
        zone = new Circle(radius, Color.TRANSPARENT);
        target = new Group(back, view, frame, cream, zone);
        for (final var node : List.of(back, view, frame, cream)) {
            node.setMouseTransparent(true);
        }
        target.setLayoutX(targetX);
        target.setLayoutY(targetY);
        target.setScaleX(0.01);
        target.setScaleY(0.01);
        final EventHandler<Event> handler = event -> {
            final var type = event.getEventType();
            if (type == MouseEvent.MOUSE_ENTERED || type == GazeEvent.GAZE_ENTERED) {
                lookAt();
            } else if (type == MouseEvent.MOUSE_EXITED || type == GazeEvent.GAZE_EXITED) {
                lookAway();
            }
        };
        zone.addEventFilter(MouseEvent.ANY, handler);
        zone.addEventFilter(GazeEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(zone);
        targetLayer.getChildren().add(target);
        looked = false;
        progress = 0;
        phaseTime = 0;
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        targetAOIList.add(new TargetAOI(targetX, targetY, (int) radius, System.currentTimeMillis()));
    }

    private Image nextImage() {
        if (pictures == CreamPiePictures.ANIMALS) {
            return animals.pickRandomImage();
        }
        Image loaded = null;
        for (int i = 0; i < userImages.size() && (loaded == null || loaded.isError()); i++) {
            int index = random.nextInt(userImages.size());
            if (index == lastImage && userImages.size() > 1) {
                index = (index + 1) % userImages.size();
            }
            lastImage = index;
            loaded = new Image(userImages.get(index).toURI().toString(), 800, 800, true, true);
        }
        if (loaded == null || loaded.isError()) {
            log.warn("No image of the user could be read: an animal instead");
            return animals.pickRandomImage();
        }
        return loaded;
    }

    private void showScore() {
        final String text = gameContext.getTranslator().translate("PiePies") + " " + pies
            + (end.getPies() > 0 ? " / " + end.getPies() : "");
        final Group score = GameStyle.pill(text, height * 0.035);
        score.setLayoutX((width - score.getBoundsInLocal().getWidth()) / 2);
        score.setLayoutY(height * 0.025);
        hud.getChildren().setAll(score);
    }

    void lookAt() {
        looked = true;
    }

    void lookAway() {
        looked = false;
        progress = 0;
        ring.stop();
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        phaseTime += seconds;
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                grow(Math.min(1, phaseTime / APPEAR));
                placePie(handX, handY, Math.min(1, phaseTime / APPEAR), 0);
                glide(seconds);
                if (elapsed >= configuration.getGameStartDelay() / 1000.0 && phaseTime >= APPEAR) {
                    phase = Phase.AIM;
                    phaseTime = 0;
                }
            }
            case APPEAR -> {
                grow(Math.min(1, phaseTime / APPEAR));
                placePie(handX, handY, Math.min(1, phaseTime / APPEAR), 0);
                glide(seconds);
                if (phaseTime >= APPEAR) {
                    phase = Phase.AIM;
                    phaseTime = 0;
                }
            }
            case AIM -> aim(seconds);
            case THROW -> fly();
            case SPLAT -> {
                final double shake = phaseTime < 0.35 ? Math.sin(phaseTime * 70) * radius * 0.06 * (1 - phaseTime / 0.35)
                    : 0;
                target.setLayoutX(targetX + shake);
                if (phaseTime >= SPLAT) {
                    phase = Phase.FADE;
                    phaseTime = 0;
                }
            }
            case FADE -> {
                target.setOpacity(Math.max(0, 1 - phaseTime / FADE));
                if (phaseTime >= FADE) {
                    if (end.getPies() > 0 && pies >= end.getPies()) {
                        finish();
                    } else {
                        newTarget();
                        phase = Phase.APPEAR;
                        phaseTime = 0;
                    }
                }
            }
            default -> {
            }
        }
    }

    private void grow(final double u) {
        final double scale = Math.max(0.01, 1 - Math.pow(1 - u, 3));
        target.setScaleX(scale);
        target.setScaleY(scale);
    }

    /**
     * A moving target glides, and bounces on the edges of its space.
     */
    private void glide(final double seconds) {
        if (motion != CreamPieMotion.MOVING) {
            return;
        }
        targetX += velocityX * seconds;
        targetY += velocityY * seconds;
        if (targetX < minX || targetX > maxX) {
            velocityX = -velocityX;
            targetX = Math.max(minX, Math.min(maxX, targetX));
        }
        if (targetY < minY || targetY > maxY) {
            velocityY = -velocityY;
            targetY = Math.max(minY, Math.min(maxY, targetY));
        }
        target.setLayoutX(targetX);
        target.setLayoutY(targetY);
    }

    /**
     * The target can be hit: at once when it is looked at, or after the fixation of the settings.
     */
    private void aim(final double seconds) {
        glide(seconds);
        if (!looked || Stats.isConfigMenuOpen()) {
            return;
        }
        if (way == CreamPieThrow.LOOK) {
            throwPie();
            return;
        }
        if (!ring.isVisible()) {
            ring.setSize(radius * 2.3);
            ring.show(targetX, targetY);
        }
        ring.moveTo(targetX, targetY);
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            throwPie();
        }
    }

    /**
     * The hand throws the pie at the target, which stops.
     */
    void throwPie() {
        if (phase != Phase.AIM) {
            return;
        }
        ring.stop();
        progress = 0;
        phase = Phase.THROW;
        phaseTime = 0;
        fromX = handX;
        fromY = handY;
        gameContext.getSoundManager().add(SOUNDS + "throw.wav");
    }

    /**
     * The pie on its arc, turning and growing to the size of the target; the hand pushes then comes back.
     */
    private void fly() {
        final double u = Math.min(1, phaseTime / THROW);
        final double controlX = (fromX + targetX) / 2;
        final double controlY = Math.min(fromY, targetY) - height * 0.22;
        final double x = (1 - u) * (1 - u) * fromX + 2 * (1 - u) * u * controlX + u * u * targetX;
        final double y = (1 - u) * (1 - u) * fromY + 2 * (1 - u) * u * controlY + u * u * targetY;
        final double finalScale = radius * 2.1 / pieSize;
        placePie(x, y, 1 + (finalScale - 1) * u, 300 * u);
        hand.setLayoutY(handRest - height * 0.05 * Math.sin(Math.PI * Math.min(1, phaseTime / 0.35)));
        if (u >= 1) {
            splat();
        }
    }

    /**
     * The pie splats on the picture: cream, drips, a cherry; the pie counts.
     */
    private void splat() {
        phase = Phase.SPLAT;
        phaseTime = 0;
        pie.setVisible(false);
        hand.setLayoutY(handRest);
        drawCream();
        pies++;
        stats.incrementNumberOfGoalsReached();
        if (!targetAOIList.isEmpty()) {
            targetAOIList.get(targetAOIList.size() - 1).setTimeEnded(System.currentTimeMillis());
        }
        gameContext.getSoundManager().add(SOUNDS + "splat.wav");
        showScore();
    }

    private void drawCream() {
        final Color shade = Color.rgb(200, 188, 168);
        final Color white = Color.rgb(255, 253, 247);
        final List<Circle> shadows = new ArrayList<>();
        final List<javafx.scene.Node> blobs = new ArrayList<>();
        final double offset = radius * 0.04;
        blobs.add(new Circle(0, 0, radius * 0.5, white));
        shadows.add(new Circle(offset, offset * 1.5, radius * 0.5, shade));
        for (int i = 0; i < 10; i++) {
            final double a = 2 * Math.PI * i / 10 + random.nextDouble() * 0.4;
            final double d = radius * (0.45 + random.nextDouble() * 0.4);
            final double r = radius * (0.14 + random.nextDouble() * 0.14);
            blobs.add(new Circle(Math.cos(a) * d, Math.sin(a) * d, r, white));
            shadows.add(new Circle(Math.cos(a) * d + offset, Math.sin(a) * d + offset * 1.5, r, shade));
        }
        // Drips down the picture
        for (int i = 0; i < 4; i++) {
            final double x = radius * (-0.6 + 0.4 * i + random.nextDouble() * 0.15);
            final double top = radius * 0.3;
            final double length = radius * (0.45 + random.nextDouble() * 0.45);
            final double w = radius * (0.1 + random.nextDouble() * 0.06);
            final Rectangle drip = new Rectangle(x - w, top, 2 * w, length);
            drip.setArcWidth(2 * w);
            drip.setArcHeight(2 * w);
            drip.setFill(white);
            blobs.add(drip);
            blobs.add(new Circle(x, top + length, w * 1.25, white));
            shadows.add(new Circle(x + offset, top + length + offset * 1.5, w * 1.25, shade));
        }
        // Drops splashed around
        for (int i = 0; i < 7; i++) {
            final double a = random.nextDouble() * 2 * Math.PI;
            final double d = radius * (1.12 + random.nextDouble() * 0.3);
            blobs.add(new Circle(Math.cos(a) * d, Math.sin(a) * d, radius * (0.04 + random.nextDouble() * 0.05), white));
        }
        final Circle cherry = new Circle(radius * 0.05, -radius * 0.1, radius * 0.16, Color.rgb(214, 34, 52));
        cherry.setStroke(Color.rgb(130, 16, 30));
        cherry.setStrokeWidth(Math.max(1, radius * 0.02));
        final Circle gleam = new Circle(-radius * 0.0, -radius * 0.15, radius * 0.045, Color.rgb(255, 170, 176));
        cream.getChildren().setAll(shadows);
        cream.getChildren().addAll(blobs);
        cream.getChildren().addAll(cherry, gleam);
    }

    private void finish() {
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
    }

    private void removeTarget() {
        if (zone != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(zone);
        }
        if (targetLayer != null) {
            targetLayer.getChildren().clear();
        }
        ring.stop();
        if (pie != null) {
            pie.setVisible(true);
            placePie(handX, handY, 0.01, 0);
        }
        target = null;
        zone = null;
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    double getRadius() {
        return radius;
    }

    double getMaxY() {
        return maxY;
    }

    List<TargetAOI> getTargetAOIList() {
        return targetAOIList;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        stats.setTargetAOIList(targetAOIList);
        if (targetLayer != null) {
            removeTarget();
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }
}
