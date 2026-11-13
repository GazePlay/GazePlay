package net.gazeplay.games.blocs;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
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
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * "Blocs" (name code Blocks; reviewed on 13/11/2026, maintainer's rules): a picture hidden under a grid of blocks of soft
 * colors (3 × 3, 5 × 5 or 8 × 8), each block vanishing as soon as the gaze enters it (to learn that the gaze acts) or
 * after a fixation; the picture is one of the animals of the default images, or one of the images of the user. All the
 * blocks are to remove; after 5 s without removing one, those left blink softly. A picture uncovered: a star and "Bravo
 * !", the whole picture 2 s, then another (never the same twice in a row); the game ends after 3, 5 or 10 pictures
 * ("Bravo", then the statistics), or has no end.
 * <p>
 * One loop, in real seconds: the fixation lasts the length of the settings (read at each step), nothing can be removed
 * before the delay of the settings nor while the menu of the game is open. (Before the review: blocks of random,
 * often garish colors; no help for the last blocks; a picture uncovered without its own reward; the images of the user
 * without a variant; options never used; nothing removed at the end.)
 */
@Slf4j
public class Blocs implements GameLifeCycle {

    /**
     * The seconds a block takes to vanish, before the blocks left blink, and the reward lasts.
     */
    static final double VANISH = 0.3;
    static final double HELP = 5;
    static final double REWARD = 2;

    private static final Color[] PALETTE = {Color.rgb(120, 170, 230), Color.rgb(240, 150, 120),
        Color.rgb(130, 200, 150), Color.rgb(190, 150, 220), Color.rgb(245, 200, 90)};
    private static final String SOUNDS = "data/blocs/sounds/";

    enum Phase { WAIT, PLAY, REWARD, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final BlocsMode mode;
    @Getter
    private final BlocsLevel level;
    @Getter
    private final BlocsEnd end;
    @Getter
    private final BlocsPictures pictures;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int uncovered;
    private double elapsed;
    private double phaseTime;
    private double idle;
    private boolean rewarded;
    private int lastColor = -1;
    private int lastImage = -1;
    private String lastUrl;

    private double width;
    private double height;

    private Group layer;
    private Group pictureLayer;
    private Group blocksLayer;
    private Group hud;
    private Group reward;
    private FixationRing ring;
    private ImageLibrary animals;
    private List<File> userImages = List.of();
    @Getter
    private final List<Block> blocks = new ArrayList<>();
    private final List<Block> vanishing = new ArrayList<>();
    @Getter
    private Block looked;
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
    Blocs(final IGameContext gameContext, final Stats stats, final BlocsMode mode, final BlocsLevel level,
          final BlocsEnd end, final BlocsPictures pictures, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.level = level;
        this.end = end;
        this.pictures = pictures;
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
        final Rectangle room = new Rectangle(0, 0, width, height);
        room.setStyle("-fx-fill: -gp-bg;");
        room.setMouseTransparent(true);
        pictureLayer = new Group();
        pictureLayer.setMouseTransparent(true);
        blocksLayer = new Group();
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        reward = new Group();
        reward.setMouseTransparent(true);
        layer = new Group(room, pictureLayer, blocksLayer, ring, hud, reward);
        gameContext.getChildren().add(layer);
        gameContext.getGazeDeviceManager().addStats(stats);
        if (pictures == BlocsPictures.MY_IMAGES) {
            userImages = UserImagesVariant.images(BlocsPictures.USER_IMAGES_FOLDER);
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
        uncovered = 0;
        newPicture();
        phase = Phase.WAIT;
        phaseTime = 0;
        elapsed = 0;
        if (!manual) {
            loop.start();
        }
    }

    /**
     * A new picture, fitted under the score, under a new grid of blocks of one soft color (never the color nor the
     * picture just used).
     */
    private void newPicture() {
        clearBlocks();
        final Image image = nextImage();
        final double areaTop = height * 0.1;
        final double areaWidth = width * 0.92;
        final double areaHeight = height * 0.86;
        final double ratio = image.getWidth() > 0 && image.getHeight() > 0 ? image.getWidth() / image.getHeight() : 1;
        double w = areaWidth;
        double h = w / ratio;
        if (h > areaHeight) {
            h = areaHeight;
            w = h * ratio;
        }
        final double left = (width - w) / 2;
        final double top = areaTop + (areaHeight - h) / 2;
        final Rectangle frame = new Rectangle(left - 6, top - 6, w + 12, h + 12);
        frame.setArcWidth(24);
        frame.setArcHeight(24);
        frame.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        final ImageView view = new ImageView(image);
        view.setFitWidth(w);
        view.setFitHeight(h);
        view.setX(left);
        view.setY(top);
        pictureLayer.getChildren().setAll(frame, view);

        int colorIndex;
        do {
            colorIndex = random.nextInt(PALETTE.length);
        } while (colorIndex == lastColor);
        lastColor = colorIndex;
        final Color color = PALETTE[colorIndex];
        final int side = level.getSide();
        final double bw = w / side;
        final double bh = h / side;
        for (int column = 0; column < side; column++) {
            for (int line = 0; line < side; line++) {
                // A checkerboard of two shades of the color
                final Color shade = (column + line) % 2 == 0 ? color : color.interpolate(Color.WHITE, 0.25);
                blocks.add(new Block(left + column * bw, top + line * bh, bw, bh, shade));
            }
        }
        idle = 0;
        reward.getChildren().clear();
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        showHud();
    }

    private Image nextImage() {
        if (pictures == BlocsPictures.MY_IMAGES) {
            Image loaded = null;
            for (int i = 0; i < userImages.size() && (loaded == null || loaded.isError()); i++) {
                int index = random.nextInt(userImages.size());
                if (index == lastImage && userImages.size() > 1) {
                    index = (index + 1) % userImages.size();
                }
                lastImage = index;
                loaded = new Image(userImages.get(index).toURI().toString(), 1600, 1600, true, true);
            }
            if (loaded != null && !loaded.isError()) {
                return loaded;
            }
            log.warn("No image of the user could be read: an animal instead");
        }
        Image image = animals.pickRandomImage();
        for (int i = 0; i < 5 && image.getUrl() != null && image.getUrl().equals(lastUrl); i++) {
            image = animals.pickRandomImage();
        }
        lastUrl = image.getUrl();
        return image;
    }

    private void clearBlocks() {
        for (final Block block : blocks) {
            gameContext.getGazeDeviceManager().removeEventFilter(block.shape);
        }
        blocks.clear();
        vanishing.clear();
        blocksLayer.getChildren().clear();
        ring.stop();
        looked = null;
        progress = 0;
    }

    private void showHud() {
        final double size = height * 0.03;
        final int total = level.getSide() * level.getSide();
        final Group count = GameStyle.pill(gameContext.getTranslator().translate("BlocsRemoved") + " "
            + (total - blocks.size()) + " / " + total, size);
        count.setLayoutX(height * 0.03);
        count.setLayoutY(height * 0.02);
        final Group done = GameStyle.pill(gameContext.getTranslator().translate("BlocsPicturesDone") + " " + uncovered
            + (end.getPictures() > 0 ? " / " + end.getPictures() : ""), size);
        done.setLayoutX(width - done.getBoundsInLocal().getWidth() - height * 0.03);
        done.setLayoutY(height * 0.02);
        hud.getChildren().setAll(count, done);
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        phaseTime += seconds;
        vanish(seconds);
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    setPhase(Phase.PLAY);
                }
            }
            case PLAY -> {
                fixate(seconds);
                help(seconds);
            }
            case REWARD -> {
                if (!rewarded) {
                    rewarded = true;
                    showReward();
                }
                if (phaseTime >= REWARD) {
                    if (end.getPictures() > 0 && uncovered >= end.getPictures()) {
                        finish();
                    } else {
                        setPhase(Phase.PLAY);
                        newPicture();
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
     * After a while without removing a block, the blocks left blink softly.
     */
    private void help(final double seconds) {
        idle += seconds;
        final double opacity = idle >= HELP ? 0.7 + 0.3 * Math.cos((idle - HELP) * 5) : 1;
        for (final Block block : blocks) {
            block.shape.setOpacity(opacity);
        }
    }

    boolean isHelping() {
        return idle >= HELP;
    }

    private void vanish(final double seconds) {
        for (int i = vanishing.size() - 1; i >= 0; i--) {
            final Block block = vanishing.get(i);
            block.age += seconds;
            final double u = Math.min(1, block.age / VANISH);
            block.shape.setScaleX(1 - 0.8 * u);
            block.shape.setScaleY(1 - 0.8 * u);
            block.shape.setRotate(25 * u);
            block.shape.setOpacity(1 - u);
            if (u >= 1) {
                blocksLayer.getChildren().remove(block.shape);
                vanishing.remove(i);
            }
        }
    }

    private void fixate(final double seconds) {
        if (mode != BlocsMode.FIXATION || looked == null || Stats.isConfigMenuOpen()) {
            return;
        }
        if (!ring.isVisible()) {
            ring.setSize(Math.max(height * 0.07, Math.min(looked.w, looked.h) * 0.8));
            ring.show(looked.x + looked.w / 2, looked.y + looked.h / 2);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Block block = looked;
            ring.stop();
            looked = null;
            progress = 0;
            remove(block);
        }
    }

    void lookAt(final Block block) {
        if (phase != Phase.PLAY) {
            return;
        }
        if (mode == BlocsMode.GAZE) {
            remove(block);
            return;
        }
        if (looked == block) {
            return;
        }
        looked = block;
        progress = 0;
        ring.stop();
    }

    void lookAway(final Block block) {
        if (looked == block) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * A block removed: it vanishes; the last one uncovers the picture.
     */
    void remove(final Block block) {
        if (phase != Phase.PLAY || !blocks.contains(block)) {
            return;
        }
        blocks.remove(block);
        gameContext.getGazeDeviceManager().removeEventFilter(block.shape);
        block.shape.setMouseTransparent(true);
        vanishing.add(block);
        if (looked == block) {
            lookAway(block);
        }
        idle = 0;
        for (final Block left : blocks) {
            left.shape.setOpacity(1);
        }
        gameContext.getSoundManager().add(SOUNDS + "pop.wav");
        showHud();
        if (blocks.isEmpty()) {
            uncovered++;
            stats.incrementNumberOfGoalsReached();
            gameContext.getSoundManager().add("data/goosegame/sounds/win.wav");
            rewarded = false;
            showHud();
            setPhase(Phase.REWARD);
        }
    }

    /**
     * A star beside "Bravo !", at the top of the picture.
     */
    private void showReward() {
        final double r = height * 0.045;
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("BlocsWell"), height * 0.045);
        final double pillWidth = pill.getBoundsInLocal().getWidth();
        final double pillHeight = pill.getBoundsInLocal().getHeight();
        final double gap = r * 0.4;
        final double startX = width / 2 - (2 * r + gap + pillWidth) / 2;
        final double cy = height * 0.16;
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

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (blocksLayer != null) {
            clearBlocks();
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A block of the grid, a rectangle with a light relief.
     */
    final class Block {
        private final Rectangle shape;
        private final double x;
        private final double y;
        private final double w;
        private final double h;
        private double age;

        Block(final double x, final double y, final double w, final double h, final Color color) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            // 1 pixel more, so that no line of the picture shows between two blocks
            shape = new Rectangle(x, y, w + 1, h + 1);
            shape.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, color.interpolate(Color.WHITE, 0.2)), new Stop(1, color.darker())));
            shape.setStroke(Color.rgb(255, 255, 255, 0.55));
            shape.setStrokeWidth(Math.max(1.5, Math.min(w, h) * 0.02));
            final EventHandler<Event> handler = event -> {
                final var type = event.getEventType();
                if (type == MouseEvent.MOUSE_ENTERED || type == GazeEvent.GAZE_ENTERED) {
                    lookAt(this);
                } else if (type == MouseEvent.MOUSE_EXITED || type == GazeEvent.GAZE_EXITED) {
                    lookAway(this);
                }
            };
            shape.addEventFilter(MouseEvent.ANY, handler);
            shape.addEventFilter(GazeEvent.ANY, handler);
            gameContext.getGazeDeviceManager().addEventFilter(shape);
            blocksLayer.getChildren().add(shape);
        }
    }
}
