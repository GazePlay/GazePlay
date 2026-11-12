package net.gazeplay.games.scratchcard;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
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
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.List;

/**
 * "Cartes à gratter" (name code ScratchCard; reviewed on 12/11/2026, maintainer's rules): a card covered with a silver
 * coating, scratched with a coin moved by the gaze (or the mouse, without clicking). The coin follows every gaze point,
 * smoothed, and scratches all along its way under it; it lifts when the gaze leaves the card or when the eye tracker
 * loses it for a moment. When 90 % of the card is scratched (scratching is the fun of the game: maintainer's choice,
 * 60 % at first), the rest of the coating melts, a star and "Bravo !" show, and another
 * card comes; the game ends after 3, 5 or 10 cards ("Bravo", then the statistics), or has no end.
 * <p>
 * One loop, in real seconds: nothing can be scratched before the delay of the settings nor while the menu of the game
 * is open. (Before the review: the game "Blocs" with other settings, a grid of 100 x 100 blocks the color of the
 * theme over the whole screen, removed around the block the gaze entered, so that the scraper was an oval and nothing
 * was scratched between two points; no goal to reach counted, no delay, no pause, no end, no variant.)
 */
@Slf4j
public class ScratchCard implements GameLifeCycle {

    /**
     * The part of the card to scratch for the picture to show (maintainer's choice at his trial: 90 %, scratching is
     * the fun of the game; it was 60 %, as before the review).
     */
    static final double TO_UNCOVER = 0.9;
    /**
     * How much a new gaze point moves the scraper (the rest of the way is kept: a smoothing of the trembling), and a
     * mouse point.
     */
    static final double GAZE_SMOOTHING = 0.35;
    static final double MOUSE_SMOOTHING = 0.8;
    /**
     * A source silent longer than this (seconds, e.g. the eye tracker losing the eyes for a blink) lifts the scraper.
     */
    static final double LIFT_AFTER = 0.35;
    /**
     * The seconds the rest of the coating takes to melt, then the seconds of the reward before the next card.
     */
    static final double MELT = 0.5;
    static final double REWARD = 2;
    /**
     * The columns of the grid counting the part scratched (the rows follow the shape of the card).
     */
    static final int COLUMNS = 64;

    private static final String SOUNDS = "data/scratchcard/sounds/";
    private static final double SCRATCH_SOUND = 0.2;

    enum Phase { WAIT, PLAY, REVEAL, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final ScratchCardPictures pictures;
    @Getter
    private final ScratchCardBrush brush;
    @Getter
    private final ScratchCardEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    /**
     * The cards uncovered.
     */
    @Getter
    private int uncovered;
    private double clock;
    private double elapsed;
    private double revealTime;
    private boolean rewarded;
    private double lastSound = -1;

    private double width;
    private double height;
    private double radius;
    private ImageLibrary animals;
    private List<File> userImages;
    private int lastImage = -1;

    private Group layer;
    private Group cardLayer;
    private Group hud;
    private Group reward;
    /**
     * The coin that scratches, moved by the gaze or the mouse, of the size of the scraper.
     */
    @Getter
    private Group coin;
    /**
     * The coating, a picture whose pixels are scratched (made transparent), over the picture of the card; the zone
     * above it receives the gaze and the mouse.
     */
    @Getter
    private ImageView coating;
    @Getter
    private WritableImage coatingImage;
    private int[] coatingPixels;
    private PixelWriter coatingWriter;
    private int coatingWidth;
    private int coatingHeight;
    private Rectangle zone;
    private GraphicsContext scraper;
    private boolean[][] scratched;
    private int rows;
    @Getter
    private int scratchedCells;
    private final Pen gazePen = new Pen();
    private final Pen mousePen = new Pen();

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
    ScratchCard(final IGameContext gameContext, final Stats stats, final ScratchCardPictures pictures,
                final ScratchCardBrush brush, final ScratchCardEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.pictures = pictures;
        this.brush = brush;
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
        radius = height * brush.getRadius();

        final Rectangle room = new Rectangle(0, 0, width, height);
        room.setStyle("-fx-fill: -gp-bg;");
        room.setMouseTransparent(true);
        cardLayer = new Group();
        reward = new Group();
        reward.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        coin = coin();
        coin.setMouseTransparent(true);
        coin.setVisible(false);
        layer = new Group(room, cardLayer, coin, reward, hud);
        gameContext.getChildren().add(layer);
        gameContext.getGazeDeviceManager().addStats(stats);

        if (pictures == ScratchCardPictures.MY_IMAGES) {
            userImages = UserImagesVariant.images(ScratchCardPictures.USER_IMAGES_FOLDER);
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
        newCard();
        phase = Phase.WAIT;
        elapsed = 0;
        if (!manual) {
            loop.start();
        }
    }

    /**
     * A new card: a picture under a silver coating, sized to the picture in the space under the score.
     */
    private void newCard() {
        removeCard();
        reward.getChildren().clear();
        final Image image = nextImage();
        final double band = height * 0.1;
        final double margin = height * 0.04;
        final double spaceWidth = width - 2 * margin;
        final double spaceHeight = height - band - margin;
        // The shape of the picture, kept within reason (a very long picture is cropped)
        double ratio = image == null || image.getHeight() <= 0 ? 4.0 / 3 : image.getWidth() / image.getHeight();
        ratio = Math.max(0.6, Math.min(2.2, ratio));
        double cardWidth = spaceWidth;
        double cardHeight = cardWidth / ratio;
        if (cardHeight > spaceHeight) {
            cardHeight = spaceHeight;
            cardWidth = cardHeight * ratio;
        }
        cardWidth = Math.floor(cardWidth);
        cardHeight = Math.floor(cardHeight);
        final double x = Math.round((width - cardWidth) / 2);
        final double y = Math.round(band + (spaceHeight - cardHeight) / 2);
        final double arc = Math.min(cardWidth, cardHeight) * 0.08;

        final Rectangle paper = new Rectangle(x, y, cardWidth, cardHeight);
        paper.setArcWidth(arc);
        paper.setArcHeight(arc);
        paper.setFill(Color.WHITE);
        paper.setMouseTransparent(true);
        cardLayer.getChildren().add(paper);
        if (image != null) {
            final ImageView view = new ImageView(image);
            double w = image.getWidth();
            double h = image.getHeight();
            if (w / h > ratio) {
                w = h * ratio;
            } else {
                h = w / ratio;
            }
            view.setViewport(new Rectangle2D((image.getWidth() - w) / 2, (image.getHeight() - h) / 2, w, h));
            view.setFitWidth(cardWidth);
            view.setFitHeight(cardHeight);
            view.setSmooth(true);
            view.setLayoutX(x);
            view.setLayoutY(y);
            view.setClip(rounded(cardWidth, cardHeight, arc));
            view.setMouseTransparent(true);
            cardLayer.getChildren().add(view);
        }

        // The coating is painted once on a canvas, then kept as a picture whose pixels are scratched
        coatingWidth = (int) cardWidth;
        coatingHeight = (int) cardHeight;
        final Canvas painter = new Canvas(cardWidth, cardHeight);
        scraper = painter.getGraphicsContext2D();
        paintCoating(cardWidth, cardHeight);
        final javafx.scene.SnapshotParameters parameters = new javafx.scene.SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        coatingImage = painter.snapshot(parameters, new WritableImage(coatingWidth, coatingHeight));
        scraper = null;
        coatingPixels = new int[coatingWidth * coatingHeight];
        coatingImage.getPixelReader().getPixels(0, 0, coatingWidth, coatingHeight, PixelFormat.getIntArgbInstance(),
            coatingPixels, 0, coatingWidth);
        coatingWriter = coatingImage.getPixelWriter();
        coating = new ImageView(coatingImage);
        coating.setLayoutX(x);
        coating.setLayoutY(y);
        coating.setClip(rounded(cardWidth, cardHeight, arc));
        coating.setMouseTransparent(true);
        cardLayer.getChildren().add(coating);
        zone = new Rectangle(0, 0, cardWidth, cardHeight);
        zone.setLayoutX(x);
        zone.setLayoutY(y);
        zone.setFill(Color.TRANSPARENT);
        final EventHandler<Event> handler = this::onCoatingEvent;
        zone.addEventFilter(GazeEvent.ANY, handler);
        zone.addEventFilter(MouseEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(zone);

        final Rectangle frame = new Rectangle(x, y, cardWidth, cardHeight);
        frame.setArcWidth(arc);
        frame.setArcHeight(arc);
        frame.setFill(null);
        frame.setStyle("-fx-stroke: -gp-border; -fx-stroke-width: 3;");
        frame.setMouseTransparent(true);
        cardLayer.getChildren().addAll(frame, zone);

        rows = Math.max(1, (int) Math.round(COLUMNS * cardHeight / cardWidth));
        scratched = new boolean[COLUMNS][rows];
        scratchedCells = 0;
        gazePen.down = false;
        mousePen.down = false;
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        // The coin waits in the middle of the card
        moveCoin(cardWidth / 2, cardHeight / 2);
        coin.setVisible(true);
        coin.toFront();
        reward.toFront();
        hud.toFront();
        showScore();
    }

    private static Rectangle rounded(final double w, final double h, final double arc) {
        final Rectangle clip = new Rectangle(w, h);
        clip.setArcWidth(arc);
        clip.setArcHeight(arc);
        return clip;
    }

    /**
     * The silver coating: a shiny gradient, a fine grain, and a pattern of stars and question marks.
     */
    private void paintCoating(final double w, final double h) {
        scraper.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(176, 183, 194)), new Stop(0.35, Color.rgb(226, 230, 236)),
            new Stop(0.55, Color.rgb(196, 202, 211)), new Stop(0.8, Color.rgb(232, 235, 240)),
            new Stop(1, Color.rgb(170, 177, 189))));
        scraper.fillRect(0, 0, w, h);
        for (int i = 0; i < (int) (w * h / 120); i++) {
            final double light = random.nextDouble();
            scraper.setFill(light < 0.5 ? Color.rgb(255, 255, 255, 0.35) : Color.rgb(90, 98, 112, 0.18));
            scraper.fillRect(random.nextDouble() * w, random.nextDouble() * h, 1.5, 1.5);
        }
        final double cell = Math.max(48, Math.min(w, h) / 4.5);
        scraper.setTextAlign(TextAlignment.CENTER);
        scraper.setFont(GameStyle.titleFont(cell * 0.42));
        int k = 0;
        for (double cy = cell / 2; cy < h + cell / 2; cy += cell, k++) {
            final double shift = k % 2 == 0 ? 0 : cell / 2;
            int j = 0;
            for (double cx = cell / 2 - shift; cx < w + cell / 2; cx += cell, j++) {
                if ((j + k) % 2 == 0) {
                    scraper.setFill(Color.rgb(255, 255, 255, 0.55));
                    star(cx, cy, cell * 0.2);
                } else {
                    scraper.setFill(Color.rgb(120, 128, 142, 0.35));
                    scraper.fillText("?", cx, cy + cell * 0.15);
                }
            }
        }
    }

    private void star(final double cx, final double cy, final double r) {
        final double[] xs = new double[10];
        final double[] ys = new double[10];
        for (int i = 0; i < 10; i++) {
            final double a = -Math.PI / 2 + i * Math.PI / 5;
            final double d = i % 2 == 0 ? r : r * 0.45;
            xs[i] = cx + d * Math.cos(a);
            ys[i] = cy + d * Math.sin(a);
        }
        scraper.fillPolygon(xs, ys, 10);
    }

    private Image nextImage() {
        if (pictures == ScratchCardPictures.ANIMALS) {
            return animals.pickRandomImage();
        }
        Image loaded = null;
        for (int i = 0; i < userImages.size() && (loaded == null || loaded.isError()); i++) {
            int index = random.nextInt(userImages.size());
            if (index == lastImage && userImages.size() > 1) {
                index = (index + 1) % userImages.size();
            }
            lastImage = index;
            loaded = new Image(userImages.get(index).toURI().toString(), 1600, 1600, true, true);
        }
        if (loaded == null || loaded.isError()) {
            log.warn("No image of the user could be read: an animal instead");
            return animals.pickRandomImage();
        }
        return loaded;
    }

    private void showScore() {
        final String text = gameContext.getTranslator().translate("ScratchCards") + " " + uncovered
            + (end.getCards() > 0 ? " / " + end.getCards() : "");
        final Group score = GameStyle.pill(text, height * 0.035);
        score.setLayoutX((width - score.getBoundsInLocal().getWidth()) / 2);
        score.setLayoutY(height * 0.02);
        hud.getChildren().setAll(score);
    }

    /**
     * The gaze and the mouse on the card: each move scratches, a way out lifts the scraper.
     */
    private void onCoatingEvent(final Event event) {
        final var type = event.getEventType();
        if (type == GazeEvent.GAZE_MOVED || type == GazeEvent.GAZE_ENTERED) {
            final GazeEvent gaze = (GazeEvent) event;
            scratch(gazePen, gaze.getX(), gaze.getY(), GAZE_SMOOTHING);
        } else if (type == MouseEvent.MOUSE_MOVED || type == MouseEvent.MOUSE_ENTERED) {
            final MouseEvent mouse = (MouseEvent) event;
            scratch(mousePen, mouse.getX(), mouse.getY(), MOUSE_SMOOTHING);
        } else if (type == GazeEvent.GAZE_EXITED) {
            gazePen.down = false;
        } else if (type == MouseEvent.MOUSE_EXITED) {
            mousePen.down = false;
        }
    }

    /**
     * A point of a source on the card (in the coordinates of the card): the scraper goes part of the way to it and
     * scratches all along; it starts again where the source is when it was lifted or silent for a moment.
     */
    void scratch(final Pen source, final double x, final double y, final double smoothing) {
        if (phase != Phase.PLAY || Stats.isConfigMenuOpen()) {
            return;
        }
        final int before = scratchedCells;
        if (!source.down || clock - source.last > LIFT_AFTER) {
            source.x = x;
            source.y = y;
            source.down = true;
            stamp(x, y);
            moveCoin(x, y);
        } else {
            final double nx = source.x + (x - source.x) * smoothing;
            final double ny = source.y + (y - source.y) * smoothing;
            final double distance = Math.hypot(nx - source.x, ny - source.y);
            final int steps = Math.max(1, (int) Math.ceil(distance / (radius * 0.3)));
            for (int i = 1; i <= steps; i++) {
                stamp(source.x + (nx - source.x) * i / steps, source.y + (ny - source.y) * i / steps);
            }
            source.x = nx;
            source.y = ny;
            moveCoin(nx, ny);
        }
        source.last = clock;
        if (scratchedCells > before && clock - lastSound >= SCRATCH_SOUND) {
            lastSound = clock;
            gameContext.getSoundManager().add(SOUNDS + "scratch.wav");
        }
        if (scratchedPart() >= TO_UNCOVER) {
            reveal();
        }
    }

    /**
     * Scratches a disc of the coating, and counts the cells of the grid whose center it uncovers.
     */
    private void stamp(final double x, final double y) {
        // The pixels of the disc made transparent, with a soft edge of one pixel and a half
        final int x0 = Math.max(0, (int) Math.floor(x - radius - 1));
        final int x1 = Math.min(coatingWidth - 1, (int) Math.ceil(x + radius + 1));
        final int y0 = Math.max(0, (int) Math.floor(y - radius - 1));
        final int y1 = Math.min(coatingHeight - 1, (int) Math.ceil(y + radius + 1));
        if (x0 <= x1 && y0 <= y1) {
            for (int py = y0; py <= y1; py++) {
                for (int px = x0; px <= x1; px++) {
                    final double distance = Math.hypot(px + 0.5 - x, py + 0.5 - y);
                    if (distance > radius + 1.5) {
                        continue;
                    }
                    final int index = py * coatingWidth + px;
                    final int argb = coatingPixels[index];
                    final double keep = Math.max(0, Math.min(1, (distance - radius) / 1.5));
                    final int alpha = (int) Math.min(argb >>> 24, Math.round((argb >>> 24) * keep));
                    coatingPixels[index] = (alpha << 24) | (argb & 0xFFFFFF);
                }
            }
            coatingWriter.setPixels(x0, y0, x1 - x0 + 1, y1 - y0 + 1, PixelFormat.getIntArgbInstance(),
                coatingPixels, y0 * coatingWidth + x0, coatingWidth);
        }
        final double cellWidth = (double) coatingWidth / COLUMNS;
        final double cellHeight = (double) coatingHeight / rows;
        final int i0 = Math.max(0, (int) Math.floor((x - radius) / cellWidth));
        final int i1 = Math.min(COLUMNS - 1, (int) Math.floor((x + radius) / cellWidth));
        final int j0 = Math.max(0, (int) Math.floor((y - radius) / cellHeight));
        final int j1 = Math.min(rows - 1, (int) Math.floor((y + radius) / cellHeight));
        for (int i = i0; i <= i1; i++) {
            for (int j = j0; j <= j1; j++) {
                if (!scratched[i][j]
                    && Math.hypot((i + 0.5) * cellWidth - x, (j + 0.5) * cellHeight - y) <= radius) {
                    scratched[i][j] = true;
                    scratchedCells++;
                }
            }
        }
    }

    /**
     * @return the part of the card scratched, from 0 to 1
     */
    double scratchedPart() {
        return scratched == null ? 0 : (double) scratchedCells / (COLUMNS * rows);
    }

    /**
     * Enough scratched: the rest melts, the card counts, a reward shows.
     */
    private void reveal() {
        phase = Phase.REVEAL;
        revealTime = 0;
        rewarded = false;
        uncovered++;
        stats.incrementNumberOfGoalsReached();
        gameContext.getSoundManager().add(SOUNDS + "reveal.wav");
        zone.setMouseTransparent(true);
        coin.setVisible(false);
        showScore();
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        clock += seconds;
        if (phase == Phase.WAIT) {
            elapsed += seconds;
            if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                phase = Phase.PLAY;
            }
        } else if (phase == Phase.REVEAL) {
            revealTime += seconds;
            coating.setOpacity(Math.max(0, 1 - revealTime / MELT));
            if (revealTime >= MELT && !rewarded) {
                rewarded = true;
                showReward();
            }
            if (rewarded) {
                final double grow = Math.min(1, (revealTime - MELT) / 0.3);
                reward.setScaleX(0.6 + 0.4 * grow);
                reward.setScaleY(0.6 + 0.4 * grow);
            }
            if (revealTime >= MELT + REWARD) {
                if (end.getCards() > 0 && uncovered >= end.getCards()) {
                    finish();
                } else {
                    newCard();
                    phase = Phase.PLAY;
                }
            }
        }
    }

    /**
     * A star and "Bravo !" at the bottom of the card, side by side, so as not to hide the picture uncovered.
     */
    private void showReward() {
        final double r = Math.min(coatingWidth, coatingHeight) * 0.09;
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("ScratchWell"), height * 0.04);
        final double pillWidth = pill.getBoundsInLocal().getWidth();
        final double pillHeight = pill.getBoundsInLocal().getHeight();
        final double gap = r * 0.4;
        final double left = coating.getLayoutX() + (coatingWidth - (2 * r + gap + pillWidth)) / 2;
        final double cy = coating.getLayoutY() + coatingHeight - Math.max(r, pillHeight / 2) - height * 0.025;
        final double cx = left + r;
        final Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            final double a = -Math.PI / 2 + i * Math.PI / 5;
            final double d = i % 2 == 0 ? r : r * 0.45;
            star.getPoints().addAll(cx + d * Math.cos(a), cy + r * 0.08 + d * Math.sin(a));
        }
        star.setFill(Color.rgb(255, 200, 40));
        star.setStroke(Color.rgb(200, 120, 0));
        star.setStrokeWidth(Math.max(2, r * 0.07));
        pill.setLayoutX(left + 2 * r + gap);
        pill.setLayoutY(cy - pillHeight / 2);
        reward.getChildren().setAll(star, pill);
    }

    private void finish() {
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
    }

    private void removeCard() {
        if (zone != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(zone);
        }
        cardLayer.getChildren().clear();
        coating = null;
        zone = null;
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    Pen getGazePen() {
        return gazePen;
    }

    Pen getMousePen() {
        return mousePen;
    }

    /**
     * Puts the coin at a point of the card (in the coordinates of the card).
     */
    private void moveCoin(final double x, final double y) {
        coin.setLayoutX(coating.getLayoutX() + x);
        coin.setLayoutY(coating.getLayoutY() + y);
    }

    /**
     * @return a golden coin around (0, 0), of the radius of the scraper, a little tilted: its rim, a star struck in the
     * middle, a gleam, and a soft shadow
     */
    private Group coin() {
        final javafx.scene.shape.Circle shadow = new javafx.scene.shape.Circle(radius * 0.08, radius * 0.12, radius,
            Color.rgb(0, 0, 0, 0.22));
        final javafx.scene.shape.Circle body = new javafx.scene.shape.Circle(radius);
        body.setFill(new javafx.scene.paint.RadialGradient(0, 0, -0.3, -0.3, 1.1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 236, 150)), new Stop(0.55, Color.rgb(242, 190, 60)),
            new Stop(1, Color.rgb(190, 130, 25))));
        body.setStroke(Color.rgb(150, 98, 15));
        body.setStrokeWidth(Math.max(1.5, radius * 0.06));
        final javafx.scene.shape.Circle rim = new javafx.scene.shape.Circle(radius * 0.78);
        rim.setFill(null);
        rim.setStroke(Color.rgb(170, 112, 20, 0.8));
        rim.setStrokeWidth(Math.max(1, radius * 0.05));
        rim.getStrokeDashArray().setAll(radius * 0.08, radius * 0.06);
        final Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            final double a = -Math.PI / 2 + i * Math.PI / 5;
            final double d = i % 2 == 0 ? radius * 0.48 : radius * 0.2;
            star.getPoints().addAll(d * Math.cos(a), radius * 0.04 + d * Math.sin(a));
        }
        star.setFill(Color.rgb(214, 152, 35));
        star.setStroke(Color.rgb(255, 228, 140));
        star.setStrokeWidth(Math.max(1, radius * 0.03));
        final javafx.scene.shape.Arc gleam = new javafx.scene.shape.Arc(0, 0, radius * 0.88, radius * 0.88, 100, 70);
        gleam.setType(javafx.scene.shape.ArcType.OPEN);
        gleam.setFill(null);
        gleam.setStroke(Color.rgb(255, 255, 255, 0.7));
        gleam.setStrokeWidth(Math.max(1.5, radius * 0.07));
        gleam.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        final Group face = new Group(body, rim, star, gleam);
        face.setRotate(-15);
        return new Group(shadow, face);
    }

    Group getReward() {
        return reward;
    }

    double getRadius() {
        return radius;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (cardLayer != null) {
            removeCard();
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * Where a source (the gaze, the mouse) has its scraper, whether it is down, and when it last moved.
     */
    static final class Pen {
        double x;
        double y;
        boolean down;
        double last;
    }
}
