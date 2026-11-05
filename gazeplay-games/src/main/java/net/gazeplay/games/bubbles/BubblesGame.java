package net.gazeplay.games.bubbles;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Game "Bubbles" (reviewed on 2/11/2026, maintainer's rules): a picture is hidden under {@value #TILES} panes of frosted
 * glass; 10 bubbles go across the screen (up, down, to the right, to the left, or stay still); each bubble burst takes
 * one pane away. The picture all found: "Bravo", then a new picture (the game goes on: it is left by the menu). A bubble
 * bursts as soon as the gaze (or the mouse) enters it, or after a fixation, as chosen in the variants. The bubbles show
 * colors, sea animals (OpenMoji) or the images of the user; with the images of the user, the hidden pictures are theirs
 * too, else drawn underwater scenes.
 * <p>
 * One loop moves the bubbles, in real time, at the animation speed of the settings; nothing moves while the menu of the
 * game is open.
 */
@Slf4j
public class BubblesGame implements GameLifeCycle {

    static final String FOLDER = "data/bubble/";

    static final List<String> SCENES = List.of("reef", "wreck", "kelp");

    static final List<String> ANIMALS = List.of("fish", "tropical-fish", "blowfish", "octopus", "crab", "turtle",
        "whale", "dolphin", "jellyfish", "seal", "shrimp", "squid", "lobster", "penguin");

    static final List<String> POPS = List.of(FOLDER + "sounds/pop1.wav", FOLDER + "sounds/pop2.wav",
        FOLDER + "sounds/pop3.wav");

    static final String PICTURE_SOUND = FOLDER + "sounds/picture.wav";

    /**
     * The bright colors of the bubbles (no dark one: they must be seen on the picture and on the panes).
     */
    static final List<String> COLORS = List.of("#FF6B6B", "#FFA94D", "#FFD43B", "#69DB7C", "#38D9A9", "#4DABF7",
        "#748FFC", "#B197FC", "#F783AC", "#63E6BE");

    static final int COLUMNS = 6;
    static final int ROWS = 6;
    static final int TILES = COLUMNS * ROWS;
    static final int BUBBLES = 10;

    /**
     * Time a bubble takes to go across the screen, at the normal animation speed, in seconds (drawn between both).
     */
    static final double SLOWEST = 7;
    static final double FASTEST = 4;

    /**
     * Time a still bubble stays at its place before going elsewhere, at the normal animation speed, in seconds.
     */
    static final double STILL_TIME = 15;

    enum Phase { PLAY, FOUND, OVER }

    private final IGameContext gameContext;
    private final Stats stats;
    @Getter
    private final BubblesContent content;
    @Getter
    private final BubblesGameVariant direction;
    @Getter
    private final BubblesBurst burst;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;

    private double width;
    private double height;

    private Group layer;
    private ImageView picture;
    private Group panes;
    private Group bubblesLayer;
    private Group effects;
    private Group hud;

    @Getter
    private final List<Tile> tiles = new ArrayList<>();
    @Getter
    private final List<Bubble> bubbles = new ArrayList<>();

    /**
     * The pictures to find, in the order they come (the user's images, else the drawn scenes), and the next one.
     */
    private final List<String> pictures = new ArrayList<>();
    @Getter
    private int pictureIndex = -1;

    /**
     * The images shown in the bubbles (animals or the user's images), as URLs.
     */
    private final List<String> bubbleImages = new ArrayList<>();

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

    public BubblesGame(final IGameContext gameContext, final Stats stats, final BubblesContent content,
                       final BubblesGameVariant direction, final BubblesBurst burst) {
        this(gameContext, stats, content, direction, burst, -1);
    }

    /**
     * @param gameSeed the seed of the game to replay, or a negative value for a new game
     */
    public BubblesGame(final IGameContext gameContext, final Stats stats, final BubblesContent content,
                       final BubblesGameVariant direction, final BubblesBurst burst, final double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.content = content;
        this.direction = direction;
        this.burst = burst;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();

        final Rectangle backdrop = new Rectangle(width, height, Color.web("#0B2545"));
        picture = new ImageView();
        picture.setPreserveRatio(true);
        picture.setSmooth(true);
        panes = new Group();
        bubblesLayer = new Group();
        effects = new Group();
        effects.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(backdrop, picture, panes, bubblesLayer, effects, hud);
        layer.getChildren().forEach(node -> {
            if (node != bubblesLayer) {
                node.setMouseTransparent(true);
            }
        });
        gameContext.getChildren().add(layer);

        // The pictures to find and the images of the bubbles
        if (content == BubblesContent.MY_IMAGES) {
            final List<File> images = UserImagesVariant.images(BubblesContent.USER_IMAGES_FOLDER);
            images.forEach(file -> pictures.add(file.toURI().toString()));
            bubbleImages.addAll(pictures);
        }
        if (pictures.isEmpty()) {
            SCENES.forEach(scene -> pictures.add(FOLDER + "images/" + scene + ".png"));
        }
        if (content == BubblesContent.ANIMALS || (content == BubblesContent.MY_IMAGES && bubbleImages.isEmpty())) {
            ANIMALS.forEach(animal -> bubbleImages.add(FOLDER + "images/animals/" + animal + ".png"));
        }
        shuffle(pictures);

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        gameContext.firstStart();
        nextPicture();
        for (int i = 0; i < BUBBLES; i++) {
            final Bubble bubble = new Bubble();
            bubbles.add(bubble);
            bubblesLayer.getChildren().add(bubble.node);
            // Spread over their way at the start, so that the screen is not empty
            bubble.respawn(random.nextDouble());
        }
        if (!manual) {
            loop.start();
        }
    }

    private void shuffle(final List<String> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            final int j = random.nextInt(i + 1);
            final String other = list.get(i);
            list.set(i, list.get(j));
            list.set(j, other);
        }
    }

    /**
     * Hides the next picture under all the panes.
     */
    void nextPicture() {
        pictureIndex++;
        final String url = pictures.get(pictureIndex % pictures.size());
        final Image image = new Image(url, width * 1.2, height * 1.2, true, true);
        picture.setImage(image);
        picture.setFitWidth(width);
        picture.setFitHeight(height);
        final double ratio = image.getWidth() <= 0 ? width / height : image.getWidth() / image.getHeight();
        final double shownWidth = Math.min(width, height * ratio);
        final double shownHeight = Math.min(height, width / ratio);
        final double left = (width - shownWidth) / 2;
        final double top = (height - shownHeight) / 2;
        picture.setLayoutX(left);
        picture.setLayoutY(top);

        panes.getChildren().clear();
        tiles.clear();
        final double tileWidth = shownWidth / COLUMNS;
        final double tileHeight = shownHeight / ROWS;
        for (int column = 0; column < COLUMNS; column++) {
            for (int row = 0; row < ROWS; row++) {
                final Tile tile = new Tile(left + column * tileWidth, top + row * tileHeight, tileWidth, tileHeight);
                tiles.add(tile);
                panes.getChildren().add(tile.node);
            }
        }
        for (int i = 0; i < TILES; i++) {
            stats.incrementNumberOfGoalsToReach();
        }
        phase = Phase.PLAY;
        showCount();
    }

    /**
     * @return the picture hidden now (for the tests)
     */
    String pictureUrl() {
        return pictures.get(pictureIndex % pictures.size());
    }

    /**
     * @return the number of panes still hiding the picture
     */
    int panesLeft() {
        return (int) tiles.stream().filter(pane -> !pane.open).count();
    }

    private void showCount() {
        hud.getChildren().clear();
        final Group count = GameStyle.pill((TILES - panesLeft()) + " / " + TILES, height * 0.032);
        count.setLayoutX((width - count.getBoundsInLocal().getWidth()) / 2);
        count.setLayoutY(height * 0.015);
        hud.getChildren().add(count);
    }

    /**
     * One step of the game, of a duration in seconds: the bubbles move.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        for (final Bubble bubble : bubbles) {
            bubble.move(seconds);
        }
    }

    /**
     * A bubble burst: a pane goes away; the last one: the picture is found.
     */
    void burst(final Bubble bubble) {
        if (phase == Phase.OVER || !bubble.node.isVisible()) {
            return;
        }
        gameContext.getSoundManager().add(POPS.get(random.nextInt(POPS.size())));
        splash(bubble);
        if (phase == Phase.PLAY) {
            stats.incrementNumberOfGoalsReached();
            gameContext.updateScore(stats, this);
            final List<Tile> closed = tiles.stream().filter(pane -> !pane.open).toList();
            if (!closed.isEmpty()) {
                closed.get(random.nextInt(closed.size())).open();
            }
            showCount();
            if (panesLeft() == 0) {
                found();
            }
        }
        bubble.respawn(0);
    }

    /**
     * The picture all found: a chime, "Bravo", then a new picture.
     */
    private void found() {
        phase = Phase.FOUND;
        stats.takeScreenShot();
        gameContext.getSoundManager().add(PICTURE_SOUND);
        gameContext.playWinTransition(500, event -> {
            gameContext.endWinTransition();
            if (phase == Phase.FOUND) {
                nextPicture();
            }
        });
    }

    /**
     * Drops of the color of the bubble flying away from it.
     */
    private void splash(final Bubble bubble) {
        final ParallelTransition all = new ParallelTransition();
        final Color color = bubble.color;
        for (int i = 0; i < 8; i++) {
            final double angle = i * Math.PI / 4 + random.nextDouble() * 0.4;
            final Circle drop = new Circle(bubble.x, bubble.y, Math.max(3, bubble.radius * 0.14), color);
            effects.getChildren().add(drop);
            final TranslateTransition fly = new TranslateTransition(Duration.millis(350), drop);
            fly.setByX(Math.cos(angle) * bubble.radius * 1.4);
            fly.setByY(Math.sin(angle) * bubble.radius * 1.4);
            final FadeTransition fade = new FadeTransition(Duration.millis(350), drop);
            fade.setToValue(0);
            all.getChildren().addAll(fly, fade);
        }
        all.setOnFinished(event -> effects.getChildren().clear());
        all.play();
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
        loop.stop();
        phase = Phase.OVER;
        for (final Bubble bubble : bubbles) {
            bubble.dispose();
        }
        bubbles.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A pane of frosted glass hiding a part of the picture; it fades away when a bubble bursts.
     */
    static final class Tile {
        @Getter
        private boolean open;
        private final Rectangle node;

        Tile(final double x, final double y, final double width, final double height) {
            // Joined to the next ones (with a gap, lines of the picture used to show between them)
            node = new Rectangle(x, y, width + 0.5, height + 0.5);
            node.setFill(Color.web("#D6ECF7", 0.94));
            node.setStroke(Color.web("#FFFFFF", 0.75));
            node.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
            node.setStrokeWidth(2);
        }

        void open() {
            if (open) {
                return;
            }
            open = true;
            final FadeTransition fade = new FadeTransition(Duration.millis(450), node);
            fade.setToValue(0);
            fade.play();
        }
    }

    /**
     * A bubble: a soap bubble with a reflection, of a color, or with an animal or an image of the user inside. It goes
     * across the screen in the direction of the variant (wobbling a little), then comes back from the other side; a
     * still bubble floats at its place, then goes elsewhere. It bursts as chosen in the variants.
     */
    final class Bubble {
        @Getter
        private double x;
        @Getter
        private double y;
        private double radius;
        private double speed;
        private double wobblePhase;
        private double time;
        private double stillTime;
        private Color color;
        private final Group node = new Group();
        private final Arc ring;
        private final DoubleProperty progress = new SimpleDoubleProperty(0);
        private Timeline fixation;
        private final EventHandler<Event> handler = this::handle;

        Bubble() {
            ring = new Arc(0, 0, 10, 10, 90, 0);
            ring.setType(ArcType.OPEN);
            ring.setFill(null);
            ring.setStrokeLineCap(StrokeLineCap.ROUND);
            ring.setStyle("-fx-stroke: -gp-accent;");
            ring.setMouseTransparent(true);
            progress.addListener((observable, oldValue, newValue) -> ring.setLength(-360 * newValue.doubleValue()));
            gameContext.getGazeDeviceManager().addEventFilter(node);
            node.addEventFilter(GazeEvent.ANY, handler);
            node.addEventFilter(MouseEvent.ANY, handler);
        }

        private void handle(final Event event) {
            final boolean entered = event.getEventType() == GazeEvent.GAZE_ENTERED
                || event.getEventType() == MouseEvent.MOUSE_ENTERED;
            final boolean exited = event.getEventType() == GazeEvent.GAZE_EXITED
                || event.getEventType() == MouseEvent.MOUSE_EXITED;
            if (entered) {
                if (burst == BubblesBurst.AT_ONCE) {
                    burst(this);
                } else {
                    startFixation();
                }
            } else if (exited) {
                stopFixation();
            }
        }

        private void startFixation() {
            stopFixation();
            fixation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(progress, 0)),
                new KeyFrame(Duration.millis(gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(progress, 1)));
            fixation.setOnFinished(event -> burst(this));
            fixation.playFromStart();
        }

        private void stopFixation() {
            if (fixation != null) {
                fixation.stop();
                fixation = null;
            }
            progress.set(0);
        }

        /**
         * A new bubble (size, look) at the start of its way, or at a part of it (0 to 1).
         */
        void respawn(final double along) {
            stopFixation();
            final double smallest = Math.min(width, height) / 22;
            final double largest = Math.min(width, height) / 11;
            radius = smallest + (largest - smallest) * random.nextDouble();
            color = Color.web(COLORS.get(random.nextInt(COLORS.size())));
            wobblePhase = random.nextDouble() * Math.PI * 2;
            time = 0;
            final double ratio = Math.max(0.1, gameContext.getConfiguration().getAnimationSpeedRatio());
            final double crossing = (FASTEST + (SLOWEST - FASTEST) * random.nextDouble()) / ratio;
            switch (direction) {
                case TOP -> {
                    speed = (height + 2 * radius) / crossing;
                    x = radius + (width - 2 * radius) * random.nextDouble();
                    y = height + radius - along * (height + 2 * radius);
                }
                case BOTTOM -> {
                    speed = (height + 2 * radius) / crossing;
                    x = radius + (width - 2 * radius) * random.nextDouble();
                    y = -radius + along * (height + 2 * radius);
                }
                case RIGHT -> {
                    speed = (width + 2 * radius) / crossing;
                    x = -radius + along * (width + 2 * radius);
                    y = radius + (height - 2 * radius) * random.nextDouble();
                }
                case LEFT -> {
                    speed = (width + 2 * radius) / crossing;
                    x = width + radius - along * (width + 2 * radius);
                    y = radius + (height - 2 * radius) * random.nextDouble();
                }
                default -> {
                    speed = 0;
                    stillTime = STILL_TIME / ratio * (along > 0 ? along : 1);
                    // Not in the bottom left corner (old gear) nor under the count (top), nor on another bubble
                    int tries = 0;
                    do {
                        x = radius + (width - 2 * radius) * random.nextDouble();
                        y = radius + height * 0.08 + (height * 0.92 - 2 * radius) * random.nextDouble();
                    } while (tries++ < 40 && ((x < height * 0.22 + radius && y > height * 0.78 - radius)
                        || overlapsAnother()));
                }
            }
            draw();
            place();
        }

        /**
         * Draws the bubble: its content, then the soap film and its reflection, then the ring of the fixation.
         */
        private void draw() {
            node.getChildren().clear();
            final Circle body = new Circle(radius);
            if (bubbleImages.isEmpty()) {
                body.setFill(new RadialGradient(0, 0, 0.35, 0.3, 0.85, true, CycleMethod.NO_CYCLE,
                    new Stop(0, color.deriveColor(0, 0.5, 1.3, 0.55)), new Stop(1, color.deriveColor(0, 1, 1, 0.85))));
                node.getChildren().add(body);
            } else {
                body.setFill(Color.web("#FFFFFF", 0.85));
                final ImageView image = new ImageView(new Image(bubbleImages.get(random.nextInt(bubbleImages.size())),
                    radius * 3, radius * 3, true, true));
                image.setPreserveRatio(true);
                final boolean user = content == BubblesContent.MY_IMAGES && !bubbleImages.getFirst()
                    .startsWith(FOLDER);
                // A photo fills the bubble; an animal stays whole inside it
                final double side = user ? radius * 2 : radius * 1.45;
                if (user) {
                    final double imageRatio = image.getImage().getWidth() <= 0 ? 1
                        : image.getImage().getWidth() / image.getImage().getHeight();
                    if (imageRatio > 1) {
                        image.setFitHeight(side);
                    } else {
                        image.setFitWidth(side);
                    }
                } else {
                    image.setFitWidth(side);
                    image.setFitHeight(side);
                }
                image.setLayoutX(-image.getBoundsInLocal().getWidth() / 2);
                image.setLayoutY(-image.getBoundsInLocal().getHeight() / 2);
                image.setClip(new Circle(image.getBoundsInLocal().getWidth() / 2,
                    image.getBoundsInLocal().getHeight() / 2, radius));
                node.getChildren().addAll(body, image);
            }
            final Circle film = new Circle(radius);
            film.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0.75, Color.TRANSPARENT), new Stop(0.97, Color.web("#FFFFFF", 0.55)),
                new Stop(1, Color.web("#FFFFFF", 0.8))));
            film.setStroke(color.deriveColor(0, 1, 0.8, 0.9));
            film.setStrokeWidth(Math.max(1.5, radius * 0.05));
            final Ellipse shine = new Ellipse(-radius * 0.38, -radius * 0.42, radius * 0.22, radius * 0.12);
            shine.setRotate(-35);
            shine.setFill(Color.web("#FFFFFF", 0.75));
            ring.setRadiusX(radius * 1.12);
            ring.setRadiusY(radius * 1.12);
            ring.setStrokeWidth(Math.max(4, radius * 0.12));
            node.getChildren().addAll(film, shine, ring);
            node.getChildren().forEach(child -> child.setMouseTransparent(child == ring));
            node.setOpacity(1);
            node.setVisible(true);
        }

        /**
         * @return whether the bubble is on another one
         */
        private boolean overlapsAnother() {
            for (final Bubble other : bubbles) {
                if (other != this && Math.hypot(other.x - x, other.y - y) < other.radius + radius + 8) {
                    return true;
                }
            }
            return false;
        }

        private void place() {
            node.setLayoutX(x);
            node.setLayoutY(y);
        }

        void move(final double seconds) {
            time += seconds;
            final double wobble = Math.sin(time * 2.2 + wobblePhase) * radius * 0.5 * seconds * 2.2;
            switch (direction) {
                case TOP -> {
                    y -= speed * seconds;
                    x += wobble;
                    if (y < -radius) {
                        respawn(0);
                    }
                }
                case BOTTOM -> {
                    y += speed * seconds;
                    x += wobble;
                    if (y > height + radius) {
                        respawn(0);
                    }
                }
                case RIGHT -> {
                    x += speed * seconds;
                    y += wobble;
                    if (x > width + radius) {
                        respawn(0);
                    }
                }
                case LEFT -> {
                    x -= speed * seconds;
                    y += wobble;
                    if (x < -radius) {
                        respawn(0);
                    }
                }
                default -> {
                    // Floats at its place, then goes elsewhere
                    y += Math.sin(time * 1.6 + wobblePhase) * radius * 0.06 * seconds * 1.6;
                    stillTime -= seconds;
                    if (stillTime <= 0) {
                        respawn(0);
                    }
                }
            }
            place();
        }

        /**
         * @return the node of the bubble (for the tests)
         */
        Group node() {
            return node;
        }

        void dispose() {
            stopFixation();
            gameContext.getGazeDeviceManager().removeEventFilter(node);
        }
    }
}
