package net.gazeplay.games.magiccards;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
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
import java.util.ArrayList;
import java.util.List;

/**
 * "Cartes magiques" (name code MagicCards; reviewed on 12/11/2026, maintainer's rules): cards face down, one of them
 * hiding a picture. A fixation of the length of the settings turns a card over: a wrong card shows an empty face with a
 * cross, counts a mistake (no lives) and stays turned over, greyed; the right one shows the picture, the others fade,
 * the picture grows in the middle, a star and "Bravo !", then another round. In the magic mode, the picture is first
 * shown on its card, the cards turn over and are shuffled before the eyes (two by two, on arcs), then the card is to be
 * found again. The game ends after 3, 5 or 10 pictures ("Bravo", then the statistics), or has no end.
 * <p>
 * One loop, in real seconds: the fixation lasts the length of the settings (read at each step), nothing can be chosen
 * before the delay of the settings, while the cards move, nor while the menu of the game is open. (Before the review:
 * a card back and a red cross of other sources, cards that could overflow the screen in three rows, the lines and the
 * columns of the variants swapped, an old progress indicator, no goal to reach, no mistake counted, no pause, no end.)
 */
@Slf4j
public class MagicCards implements GameLifeCycle {

    /**
     * The seconds a card takes to turn over, the picture is shown before the magic, a swap takes, and the reward lasts.
     */
    static final double FLIP = 0.35;
    static final double SHOW = 2;
    static final double SWAP = 0.55;
    static final double SWAP_PAUSE = 0.1;
    static final double GROW = 0.6;
    static final double REWARD = 2;

    private static final String FLIP_SOUND = "data/magiccards/sounds/flip.wav";
    private static final String SLIDE_SOUND = "data/cups/sounds/slide.wav";
    private static final String WRONG_SOUND = "data/dice/sounds/wrong.wav";
    private static final String WIN_SOUND = "data/goosegame/sounds/win.wav";

    enum Phase { WAIT, SHOW, SHUFFLE, PLAY, REVEAL, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final MagicCardsMode mode;
    @Getter
    private final MagicCardsPictures pictures;
    @Getter
    private final MagicCardsCount count;
    @Getter
    private final MagicCardsEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int found;
    private double elapsed;
    private double phaseTime;
    private boolean rewarded;
    private int swapsLeft;
    private Card swapA;
    private Card swapB;

    private double width;
    private double height;
    private double cardWidth;
    private double cardHeight;
    private double[][] slots;
    private ImageLibrary animals;
    private List<File> userImages;
    private int lastImage = -1;
    private String instruction;

    private Group layer;
    private Group cardsLayer;
    private Group hud;
    private Group reward;
    @Getter
    private final List<Card> cards = new ArrayList<>();
    @Getter
    private Card winner;
    @Getter
    private Card looked;
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
    MagicCards(final IGameContext gameContext, final Stats stats, final MagicCardsMode mode,
               final MagicCardsPictures pictures, final MagicCardsCount count, final MagicCardsEnd end,
               final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.pictures = pictures;
        this.count = count;
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

        final Rectangle room = new Rectangle(0, 0, width, height);
        room.setStyle("-fx-fill: -gp-bg;");
        room.setMouseTransparent(true);
        cardsLayer = new Group();
        reward = new Group();
        reward.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(room, cardsLayer, reward, hud);
        gameContext.getChildren().add(layer);
        gameContext.getGazeDeviceManager().addStats(stats);
        layout();

        if (pictures == MagicCardsPictures.MY_IMAGES) {
            userImages = UserImagesVariant.images(MagicCardsPictures.USER_IMAGES_FOLDER);
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
        found = 0;
        newRound();
        phase = Phase.WAIT;
        elapsed = 0;
        if (!manual) {
            loop.start();
        }
    }

    /**
     * The size of the cards (3 wide for 4 high) and the centers of their places, as large as the space under the band
     * allows.
     */
    private void layout() {
        final double band = height * 0.12;
        final double gap = height * 0.04;
        final int rows = count.getRows();
        final int columns = count.getColumns();
        final double spaceWidth = width - 2 * gap;
        final double spaceHeight = height - band - gap;
        cardHeight = Math.min((spaceHeight - (rows - 1) * gap) / rows,
            ((spaceWidth - (columns - 1) * gap) / columns) / 0.75);
        // Room above and below for the arcs of the magic
        cardHeight = Math.min(cardHeight, spaceHeight * 0.7);
        cardWidth = cardHeight * 0.75;
        final double gridWidth = columns * cardWidth + (columns - 1) * gap;
        final double gridHeight = rows * cardHeight + (rows - 1) * gap;
        final double left = (width - gridWidth) / 2;
        final double top = band + (spaceHeight - gridHeight) / 2;
        slots = new double[rows * columns][];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                slots[r * columns + c] = new double[]{left + c * (cardWidth + gap) + cardWidth / 2,
                    top + r * (cardHeight + gap) + cardHeight / 2};
            }
        }
    }

    /**
     * A new round: the cards face down, one of them with the picture (face up in the magic mode, for the show).
     */
    private void newRound() {
        removeCards();
        reward.getChildren().clear();
        final int winnerIndex = random.nextInt(slots.length);
        final Image image = nextImage();
        for (int i = 0; i < slots.length; i++) {
            final Card card = new Card(i, i == winnerIndex ? image : null);
            cards.add(card);
            cardsLayer.getChildren().add(card.node);
        }
        winner = cards.get(winnerIndex);
        if (mode == MagicCardsMode.MAGIC) {
            winner.setFaceUp(true);
        }
        looked = null;
        progress = 0;
        phaseTime = 0;
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        showBand(mode == MagicCardsMode.MAGIC ? "MagicWatch" : "MagicFind");
    }

    private Image nextImage() {
        if (pictures == MagicCardsPictures.ANIMALS) {
            return animals.pickRandomImage();
        }
        Image loaded = null;
        for (int i = 0; i < userImages.size() && (loaded == null || loaded.isError()); i++) {
            int index = random.nextInt(userImages.size());
            if (index == lastImage && userImages.size() > 1) {
                index = (index + 1) % userImages.size();
            }
            lastImage = index;
            loaded = new Image(userImages.get(index).toURI().toString(), 1200, 1200, true, true);
        }
        if (loaded == null || loaded.isError()) {
            log.warn("No image of the user could be read: an animal instead");
            return animals.pickRandomImage();
        }
        return loaded;
    }

    /**
     * The band: what to do, and the pictures found.
     */
    private void showBand(final String key) {
        instruction = key;
        final double size = height * 0.034;
        final Group order = GameStyle.pill(gameContext.getTranslator().translate(key), size);
        order.setLayoutX((width - order.getBoundsInLocal().getWidth()) / 2);
        order.setLayoutY(height * 0.025);
        final String text = gameContext.getTranslator().translate("MagicFound") + " " + found
            + (end.getPictures() > 0 ? " / " + end.getPictures() : "");
        final Group score = GameStyle.pill(text, size);
        score.setLayoutX(width - score.getBoundsInLocal().getWidth() - height * 0.03);
        score.setLayoutY(height * 0.025);
        hud.getChildren().setAll(order, score);
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        for (final Card card : cards) {
            card.animate(seconds);
        }
        phaseTime += seconds;
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    startRound();
                }
            }
            case SHOW -> {
                if (phaseTime >= SHOW && !winner.isFlipping()) {
                    if (winner.isFaceUp()) {
                        winner.flip(false);
                        gameContext.getSoundManager().add(FLIP_SOUND);
                    } else {
                        phase = Phase.SHUFFLE;
                        phaseTime = SWAP + SWAP_PAUSE;
                        swapsLeft = count.getSwaps();
                    }
                }
            }
            case SHUFFLE -> shuffle();
            case PLAY -> fixate(seconds);
            case REVEAL -> reveal();
            default -> {
            }
        }
    }

    /**
     * After the delay, or the reward of the round before: the show of the magic mode, or the search.
     */
    private void startRound() {
        phaseTime = 0;
        if (mode == MagicCardsMode.MAGIC) {
            phase = Phase.SHOW;
        } else {
            phase = Phase.PLAY;
        }
    }

    /**
     * The magic: two cards swap their places on arcs, one swap after another; then the card is to be found.
     */
    private void shuffle() {
        if (phaseTime < SWAP + SWAP_PAUSE) {
            return;
        }
        if (swapA != null) {
            swapA.land();
            swapB.land();
            swapA = null;
            swapB = null;
        }
        if (swapsLeft == 0) {
            phase = Phase.PLAY;
            phaseTime = 0;
            showBand("MagicWhere");
            return;
        }
        swapsLeft--;
        phaseTime = 0;
        // The card of the picture moves more often than the others, so that it is to be followed
        final Card first = random.nextDouble() < 0.6 ? winner : cards.get(random.nextInt(cards.size()));
        Card second = cards.get(random.nextInt(cards.size()));
        while (second == first) {
            second = cards.get(random.nextInt(cards.size()));
        }
        swapA = first;
        swapB = second;
        final int slotA = first.slot;
        first.moveTo(second.slot, 1);
        second.moveTo(slotA, -1);
        gameContext.getSoundManager().add(SLIDE_SOUND);
    }

    private void fixate(final double seconds) {
        if (looked == null) {
            return;
        }
        if (!looked.isRingShown()) {
            looked.showRing(true);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        looked.setRing(progress);
        if (progress >= 1) {
            final Card card = looked;
            card.showRing(false);
            looked = null;
            progress = 0;
            choose(card);
        }
    }

    void lookAt(final Card card) {
        if (looked == card || !canChoose(card)) {
            return;
        }
        if (looked != null) {
            looked.showRing(false);
        }
        looked = card;
        progress = 0;
        card.setRing(0);
        card.showRing(true);
    }

    void lookAway(final Card card) {
        if (looked == card) {
            card.showRing(false);
            looked = null;
            progress = 0;
        }
    }

    private boolean canChoose(final Card card) {
        return phase == Phase.PLAY && !card.isTurned() && !Stats.isConfigMenuOpen();
    }

    /**
     * A card chosen: the picture found, or a wrong card.
     */
    void choose(final Card card) {
        if (!canChoose(card)) {
            return;
        }
        card.flip(true);
        gameContext.getSoundManager().add(FLIP_SOUND);
        if (card == winner) {
            phase = Phase.REVEAL;
            phaseTime = 0;
            rewarded = false;
            found++;
            stats.incrementNumberOfGoalsReached();
            if (looked != null) {
                looked.showRing(false);
                looked = null;
            }
        } else {
            card.markWrong();
            stats.incrementNumberOfMistakes();
            gameContext.getSoundManager().add(WRONG_SOUND);
        }
    }

    /**
     * The picture found: once turned over, the others fade and it grows in the middle; a star and "Bravo !"; then
     * another round, or the end.
     */
    private void reveal() {
        if (phaseTime < FLIP) {
            return;
        }
        final double u = Math.min(1, (phaseTime - FLIP) / GROW);
        final double ease = u * u * (3 - 2 * u);
        for (final Card card : cards) {
            if (card != winner) {
                card.node.setOpacity(card.baseOpacity() * (1 - ease));
            }
        }
        final double scale = Math.min((width * 0.6) / cardWidth, (height * 0.62) / cardHeight);
        final double[] from = slots[winner.slot];
        final double cx = width / 2;
        final double cy = height * 0.12 + (height * 0.88) * 0.45;
        winner.node.setLayoutX(from[0] + (cx - from[0]) * ease);
        winner.node.setLayoutY(from[1] + (cy - from[1]) * ease);
        winner.node.setScaleX(1 + (scale - 1) * ease);
        winner.node.setScaleY(1 + (scale - 1) * ease);
        winner.node.toFront();
        if (u >= 1 && !rewarded) {
            rewarded = true;
            gameContext.getSoundManager().add(WIN_SOUND);
            showReward(cy + cardHeight * scale / 2);
            showBand(instruction);
        }
        if (phaseTime >= FLIP + GROW + REWARD) {
            if (end.getPictures() > 0 && found >= end.getPictures()) {
                finish();
            } else {
                newRound();
                startRound();
            }
        }
    }

    /**
     * A star beside "Bravo !", under the picture grown.
     */
    private void showReward(final double bottom) {
        final double r = height * 0.045;
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("MagicWell"), height * 0.04);
        final double pillWidth = pill.getBoundsInLocal().getWidth();
        final double pillHeight = pill.getBoundsInLocal().getHeight();
        final double gap = r * 0.4;
        final double left = (width - (2 * r + gap + pillWidth)) / 2;
        final double cy = Math.min(height - r - height * 0.02, bottom + r + height * 0.02);
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

    private void removeCards() {
        for (final Card card : cards) {
            gameContext.getGazeDeviceManager().removeEventFilter(card.node);
        }
        cards.clear();
        cardsLayer.getChildren().clear();
        swapA = null;
        swapB = null;
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    Group getReward() {
        return reward;
    }

    double[] getSlot(final int index) {
        return slots[index];
    }

    double getCardHeight() {
        return cardHeight;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (cardsLayer != null) {
            removeCards();
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A card: its back (the main color, a frame and a "?"), its face (the picture, or empty with a cross), the ring of
     * the fixation; drawn around its center, which stands at the center of its place.
     */
    final class Card {
        private final Group node;
        private final Group back;
        private final Group face;
        private final Group cross;
        private final Rectangle ring;
        private final double ringPerimeter;
        @Getter
        private int slot;
        @Getter
        private boolean faceUp;
        @Getter
        private boolean turned;
        @Getter
        private boolean wrong;
        private double flipLeft;
        private boolean flipTo;
        private int moveFrom = -1;
        private double moveTime;
        private int arcSide;

        Card(final int slot, final Image image) {
            this.slot = slot;
            final double w = cardWidth;
            final double h = cardHeight;
            final Rectangle surface = GameStyle.targetCard(-w / 2, -h / 2, w, h);
            face = new Group(surface);
            if (image != null) {
                final ImageView view = new ImageView(image);
                view.setPreserveRatio(true);
                view.setSmooth(true);
                final double fit = 0.84;
                final double shownWidth = Math.min(w * fit, h * fit * image.getWidth() / image.getHeight());
                final double shownHeight = Math.min(h * fit, w * fit * image.getHeight() / image.getWidth());
                view.setFitWidth(shownWidth);
                view.setFitHeight(shownHeight);
                view.setLayoutX(-shownWidth / 2);
                view.setLayoutY(-shownHeight / 2);
                face.getChildren().add(view);
            }
            final double arm = Math.min(w, h) * 0.18;
            final Line one = new Line(-arm, -arm, arm, arm);
            final Line two = new Line(-arm, arm, arm, -arm);
            for (final Line line : List.of(one, two)) {
                line.setStroke(Color.rgb(200, 70, 80));
                line.setStrokeWidth(Math.max(3, arm * 0.28));
                line.setStrokeLineCap(StrokeLineCap.ROUND);
            }
            cross = new Group(one, two);
            cross.setVisible(false);
            face.getChildren().add(cross);
            back = back(w, h);
            face.setVisible(false);

            final double corner = GameStyle.cardRadius(w, h);
            ring = new Rectangle(-w / 2, -h / 2, w, h);
            ring.setArcWidth(2 * corner);
            ring.setArcHeight(2 * corner);
            ring.setFill(null);
            ring.setStrokeType(StrokeType.INSIDE);
            ring.setStrokeLineCap(StrokeLineCap.BUTT);
            ring.setStrokeWidth(Math.max(5, Math.min(w, h) * 0.045));
            ring.setVisible(false);
            ring.setMouseTransparent(true);
            ringPerimeter = GameStyle.clockwiseRing(ring);

            node = new Group(face, back, ring);
            node.setLayoutX(slots[slot][0]);
            node.setLayoutY(slots[slot][1]);
            final EventHandler<Event> handler = event -> {
                final var type = event.getEventType();
                if (type == MouseEvent.MOUSE_ENTERED || type == GazeEvent.GAZE_ENTERED) {
                    lookAt(this);
                } else if (type == MouseEvent.MOUSE_EXITED || type == GazeEvent.GAZE_EXITED) {
                    lookAway(this);
                }
            };
            node.addEventFilter(MouseEvent.ANY, handler);
            node.addEventFilter(GazeEvent.ANY, handler);
            gameContext.getGazeDeviceManager().addEventFilter(node);
        }

        private Group back(final double w, final double h) {
            final double corner = GameStyle.cardRadius(w, h);
            final Rectangle card = new Rectangle(-w / 2, -h / 2, w, h);
            card.setArcWidth(2 * corner);
            card.setArcHeight(2 * corner);
            card.setStyle("-fx-fill: -gp-accent; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
            final double margin = Math.min(w, h) * 0.08;
            final Rectangle frame = new Rectangle(-w / 2 + margin, -h / 2 + margin, w - 2 * margin, h - 2 * margin);
            frame.setArcWidth(2 * corner * 0.7);
            frame.setArcHeight(2 * corner * 0.7);
            frame.setFill(null);
            frame.setStyle("-fx-stroke: rgba(255, 255, 255, 0.55); -fx-stroke-width: " + Math.max(2, margin * 0.35)
                + ";");
            final double size = Math.min(w, h) * 0.5;
            final Text mark = new Text("?");
            mark.setFont(GameStyle.titleFont(size));
            mark.setStyle(GameStyle.titleFontStyle(size) + " -fx-fill: -gp-on-accent;");
            mark.setX(-mark.getLayoutBounds().getWidth() / 2 - mark.getLayoutBounds().getMinX());
            mark.setY(-mark.getLayoutBounds().getHeight() / 2 - mark.getLayoutBounds().getMinY());
            return new Group(card, frame, mark);
        }

        void setFaceUp(final boolean up) {
            faceUp = up;
            face.setVisible(up);
            back.setVisible(!up);
        }

        /**
         * Turns the card over, in {@link #FLIP} seconds (it narrows, changes side, widens).
         */
        void flip(final boolean up) {
            flipTo = up;
            flipLeft = FLIP;
            if (up) {
                turned = true;
            }
        }

        boolean isFlipping() {
            return flipLeft > 0;
        }

        void markWrong() {
            wrong = true;
            cross.setVisible(true);
        }

        double baseOpacity() {
            return wrong ? 0.55 : 1;
        }

        /**
         * Goes to another place, on an arc above (1) or below (-1) the straight way, in {@link #SWAP} seconds.
         */
        void moveTo(final int target, final int side) {
            moveFrom = slot;
            slot = target;
            moveTime = 0;
            arcSide = side;
            node.toFront();
        }

        /**
         * At its place, at the end of a swap.
         */
        void land() {
            moveFrom = -1;
            node.setLayoutX(slots[slot][0]);
            node.setLayoutY(slots[slot][1]);
        }

        void animate(final double seconds) {
            if (flipLeft > 0) {
                final boolean firstHalf = flipLeft > FLIP / 2;
                flipLeft = Math.max(0, flipLeft - seconds);
                if (firstHalf && flipLeft <= FLIP / 2) {
                    setFaceUp(flipTo);
                }
                node.setScaleX(Math.max(0.02, Math.abs(2 * flipLeft / FLIP - 1)) * node.getScaleY());
                if (flipLeft == 0) {
                    node.setScaleX(node.getScaleY());
                    if (wrong) {
                        node.setOpacity(baseOpacity());
                    }
                }
            }
            if (moveFrom >= 0) {
                moveTime = Math.min(SWAP, moveTime + seconds);
                final double u = moveTime / SWAP;
                final double ease = u * u * (3 - 2 * u);
                final double[] a = slots[moveFrom];
                final double[] b = slots[slot];
                final double dx = b[0] - a[0];
                final double dy = b[1] - a[1];
                final double length = Math.max(1, Math.hypot(dx, dy));
                final double lift = Math.sin(Math.PI * ease) * Math.min(cardHeight * 0.18, length * 0.3) * arcSide;
                node.setLayoutX(a[0] + dx * ease - dy / length * lift);
                node.setLayoutY(a[1] + dy * ease + dx / length * lift);
            }
        }

        boolean isRingShown() {
            return ring.isVisible();
        }

        void showRing(final boolean shown) {
            ring.setStyle("-fx-stroke: " + (back.isVisible() ? "-gp-on-accent" : "-gp-accent") + ";");
            ring.setVisible(shown);
        }

        void setRing(final double value) {
            GameStyle.fillRing(ring, ringPerimeter, Math.min(1, value));
        }

        Group getNode() {
            return node;
        }
    }
}
