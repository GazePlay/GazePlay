package net.gazeplay.games.literacy;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.Getter;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * "Identifier les lettres" (name code Letters; reviewed on 12/11/2026, maintainer's rules). A letter is asked, written
 * large in the band at the top and said; a loudspeaker button says it again. A grid of cards hides a picture: a fixation
 * on a card of the letter asked turns it, uncovering a part of the picture; a card of another letter shakes in red, says
 * its letter, and counts a mistake. When all the cards of the letter are found, the whole picture shows: "Bravo", then
 * another letter. The game ends after 3, 5 or 10 pictures, or never.
 * <p>
 * Capitals, small letters (Andika, made for beginning readers) or both; Easy: 6 cards and one other letter which does not
 * look like it; Normal: 12 cards and 3 other letters; Hard: 20 cards with the letters which look like it. One loop, in
 * real seconds: the fixation lasts the length of the settings (read at each step), nothing can be chosen before the
 * delay of the settings, nothing moves while the menu of the game is open. (Before the review: a block moved by half
 * its size each time it was left, 60 % of the blocks of the letter asked, nothing for a wrong block, no goal to reach,
 * the statistics in the folder of the game Blocs, a font of the Mac only, colors drawn outside the replay, the images
 * of the user in silence, two copies of the whole game.)
 */
public class Letters implements GameLifeCycle {

    static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    /**
     * The small letters which look like each small letter.
     */
    static final Map<Character, String> LOOKALIKES = Map.ofEntries(
        Map.entry('a', "oe"), Map.entry('b', "dpq"), Map.entry('c', "eo"), Map.entry('d', "bpq"), Map.entry('e', "cfa"),
        Map.entry('f', "te"), Map.entry('g', "qy"), Map.entry('h', "nk"), Map.entry('i', "jlt"), Map.entry('j', "ig"),
        Map.entry('k', "xh"), Map.entry('l', "it"), Map.entry('m', "nw"), Map.entry('n', "muh"), Map.entry('o', "cq"),
        Map.entry('p', "qbd"), Map.entry('q', "pgd"), Map.entry('r', "nf"), Map.entry('s', "z"), Map.entry('t', "fl"),
        Map.entry('u', "nv"), Map.entry('v', "uyw"), Map.entry('w', "vm"), Map.entry('x', "ky"), Map.entry('y', "vx"),
        Map.entry('z', "s"));
    /**
     * The capitals which look like each capital (as small letters: "c" for C, G, O, Q...).
     */
    static final Map<Character, String> CAPITAL_LOOKALIKES = Map.ofEntries(
        Map.entry('a', "hv"), Map.entry('b', "pred"), Map.entry('c', "goq"), Map.entry('d', "ob"), Map.entry('e', "fb"),
        Map.entry('f', "ep"), Map.entry('g', "coq"), Map.entry('h', "nk"), Map.entry('i', "jlt"), Map.entry('j', "il"),
        Map.entry('k', "xr"), Map.entry('l', "ij"), Map.entry('m', "nw"), Map.entry('n', "mhw"), Map.entry('o', "cqdg"),
        Map.entry('p', "rbf"), Map.entry('q', "ocg"), Map.entry('r', "pbk"), Map.entry('s', "z"), Map.entry('t', "il"),
        Map.entry('u', "v"), Map.entry('v', "uy"), Map.entry('w', "mnv"), Map.entry('x', "ky"), Map.entry('y', "vx"),
        Map.entry('z', "s"));
    /**
     * In seconds: the question said before the cards can be chosen, a wrong card shaking, the whole picture shown.
     */
    static final double SHAKE = 0.5;
    static final double PICTURE = 2.2;

    private static final String SOUNDS = "data/literacy/sounds/";
    private static final Color[] CARD_COLORS = {Color.rgb(255, 214, 165), Color.rgb(182, 222, 196),
        Color.rgb(190, 214, 245), Color.rgb(226, 204, 240), Color.rgb(253, 196, 196), Color.rgb(250, 232, 160)};
    private static final Color INK = Color.rgb(48, 44, 64);
    private static final Color WRONG = Color.rgb(214, 64, 64);
    private static final String SMALL_FONT = "data/fonts/Andika-Regular.ttf";

    enum Phase { WAIT, PLAY, PICTURE, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final LettersLevel level;
    @Getter
    private final LettersCase letterCase;
    @Getter
    private final LettersPictures pictures;
    @Getter
    private final LettersEnd end;
    private final ReplayablePseudoRandom random;
    @Getter
    private final String language;

    @Getter
    private Phase phase = Phase.OVER;
    /**
     * The letter asked (small).
     */
    @Getter
    private char asked;
    @Getter
    private final List<Card> cards = new ArrayList<>();
    @Getter
    private int uncovered;
    @Getter
    private int left;
    private double elapsed;

    private double width;
    private double height;
    private double gridX;
    private double gridY;
    private double gridWidth;
    private double gridHeight;

    private Group layer;
    private Group pictureLayer;
    private Group cardsLayer;
    private Group band;
    private Group hud;
    private FixationRing ring;
    private Spot replay;
    private final List<Node> gazeNodes = new ArrayList<>();
    private ImageLibrary animals;
    private List<File> userImages = List.of();
    private int lastImage = -1;
    private boolean voiceFemale;

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
    Letters(final IGameContext gameContext, final Stats stats, final LettersLevel level, final LettersCase letterCase,
            final LettersPictures pictures, final LettersEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.level = level;
        this.letterCase = letterCase;
        this.pictures = pictures;
        this.end = end;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
        final Locale locale = gameContext.getTranslator().currentLocale();
        // "fra" in the settings of GazePlay (ISO 639-2), "fr" elsewhere
        language = locale != null && locale.getLanguage().toLowerCase(Locale.ROOT).startsWith("fr") ? "fra" : "eng";
    }

    @Override
    public void launch() {
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();

        final Rectangle background = new Rectangle(0, 0, width, height);
        background.setStyle("-fx-fill: -gp-bg;");
        background.setMouseTransparent(true);

        final double bandHeight = height * 0.17;
        gridY = bandHeight;
        gridHeight = height - bandHeight - height * 0.03;
        // Cards a little wider than high, the grid centered
        final double cell = Math.min(gridHeight / level.getLines(), width * 0.94 / level.getColumns() / 1.2);
        gridWidth = cell * 1.2 * level.getColumns();
        gridHeight = cell * level.getLines();
        gridX = (width - gridWidth) / 2;

        pictureLayer = new Group();
        pictureLayer.setMouseTransparent(true);
        cardsLayer = new Group();
        band = new Group();
        band.setMouseTransparent(true);
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        final Group replayButton = replayButton(bandHeight);
        layer = new Group(background, pictureLayer, cardsLayer, band, replayButton, ring, hud);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        if (pictures == LettersPictures.MY_IMAGES) {
            userImages = UserImagesVariant.images(LettersPictures.USER_IMAGES_FOLDER);
            if (userImages.isEmpty()) {
                // The game started without going through the menu, which asks for images
                final Group message = GameStyle.card(gameContext.getTranslator().translate("NoUserImages"),
                    height * 0.035, width * 0.5);
                message.setLayoutX((width - message.getBoundsInLocal().getWidth()) / 2);
                message.setLayoutY((height - message.getBoundsInLocal().getHeight()) / 2);
                hud.getChildren().add(message);
                return;
            }
        } else {
            animals = ImageUtils.createDefaultImageLibrary(null, random);
        }
        uncovered = 0;
        newRound();
        phase = Phase.WAIT;
        elapsed = 0;
        if (!manual) {
            loop.start();
        }
    }

    /**
     * @return the round button with a loudspeaker, at the right of the band: a fixation says the letter again
     */
    private Group replayButton(final double bandHeight) {
        final double size = bandHeight * 0.62;
        final double cx = width - size * 0.9;
        final double cy = bandHeight / 2;
        final Circle disc = new Circle(cx, cy, size / 2);
        disc.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        final double iconSize = size * 0.55;
        final SVGPath icon = GameStyle.icon("volume_up_fill1", iconSize);
        icon.setStyle("-fx-fill: -gp-accent;");
        final Group speaker = new Group(icon);
        speaker.setLayoutX(cx - iconSize / 2);
        speaker.setLayoutY(cy - iconSize / 2);
        speaker.setMouseTransparent(true);
        final Group button = new Group(disc, speaker);
        replay = new Spot(disc, cx, cy, size * 1.25, this::sayQuestion);
        listen(replay);
        return button;
    }

    /**
     * A new letter, a new picture under new cards.
     */
    private void newRound() {
        clearCards();
        final char previous = asked;
        asked = pickTarget(level, letterCase, random);
        for (int i = 0; i < 10 && asked == previous; i++) {
            asked = pickTarget(level, letterCase, random);
        }
        final List<Character> others = otherLetters(asked, level, letterCase, random);
        final List<Character> letters = new ArrayList<>();
        for (int i = 0; i < level.getTargets(); i++) {
            letters.add(asked);
        }
        for (int i = letters.size(); i < level.getCards(); i++) {
            letters.add(others.get((i - level.getTargets()) % others.size()));
        }
        Collections.shuffle(letters, new java.util.Random((long) (random.nextDouble() * Long.MAX_VALUE)));
        showPicture();
        final double cellWidth = gridWidth / level.getColumns();
        final double cellHeight = gridHeight / level.getLines();
        for (int i = 0; i < letters.size(); i++) {
            final int line = i / level.getColumns();
            final int column = i % level.getColumns();
            final char letter = letters.get(i);
            cards.add(new Card(letter, written(letter), CARD_COLORS[random.nextInt(CARD_COLORS.length)],
                gridX + column * cellWidth, gridY + line * cellHeight, cellWidth, cellHeight));
        }
        left = level.getTargets();
        showBand();
        showHud();
        voiceFemale = random.nextInt(2) == 0;
        sayQuestion();
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
    }

    /**
     * @return how a letter is written on a card: capital, small, or either (both)
     */
    private String written(final char letter) {
        return switch (letterCase) {
            case CAPITALS -> String.valueOf(Character.toUpperCase(letter));
            case SMALL -> String.valueOf(letter);
            case BOTH -> random.nextInt(2) == 0 ? String.valueOf(Character.toUpperCase(letter)) : String.valueOf(letter);
        };
    }

    /**
     * @return the letters which look like a letter written this way (both: as a capital or as a small letter)
     */
    static String lookalikes(final char letter, final LettersCase letterCase) {
        return switch (letterCase) {
            case CAPITALS -> CAPITAL_LOOKALIKES.get(letter);
            case SMALL -> LOOKALIKES.get(letter);
            case BOTH -> {
                final StringBuilder both = new StringBuilder(LOOKALIKES.get(letter));
                for (final char other : CAPITAL_LOOKALIKES.get(letter).toCharArray()) {
                    if (both.indexOf(String.valueOf(other)) < 0) {
                        both.append(other);
                    }
                }
                yield both.toString();
            }
        };
    }

    /**
     * @return a letter to ask: at the hard level, one which looks like at least two others written this way
     */
    static char pickTarget(final LettersLevel level, final LettersCase letterCase,
                           final ReplayablePseudoRandom random) {
        if (level == LettersLevel.HARD) {
            final List<Character> candidates = new ArrayList<>();
            for (final char letter : ALPHABET.toCharArray()) {
                if (lookalikes(letter, letterCase).length() >= 2) {
                    candidates.add(letter);
                }
            }
            return candidates.get(random.nextInt(candidates.size()));
        }
        return ALPHABET.charAt(random.nextInt(ALPHABET.length()));
    }

    /**
     * @return the other letters of the cards: at the easy level one which does not look like the letter asked; at the
     * hard level those which look like it first
     */
    static List<Character> otherLetters(final char asked, final LettersLevel level, final LettersCase letterCase,
                                        final ReplayablePseudoRandom random) {
        final List<Character> others = new ArrayList<>();
        final String lookalikes = lookalikes(asked, letterCase);
        if (level == LettersLevel.HARD) {
            for (final char letter : lookalikes.toCharArray()) {
                if (others.size() < level.getOthers()) {
                    others.add(letter);
                }
            }
        }
        while (others.size() < level.getOthers()) {
            final char letter = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
            final boolean alike = lookalikes.indexOf(letter) >= 0;
            if (letter != asked && !others.contains(letter) && (level == LettersLevel.HARD || !alike)) {
                others.add(letter);
            }
        }
        return others;
    }

    /**
     * The picture under the grid, cut to fill it, keeping its proportions.
     */
    private void showPicture() {
        pictureLayer.getChildren().clear();
        final Image image = nextImage();
        final ImageView view = new ImageView(image);
        view.setFitWidth(gridWidth);
        view.setFitHeight(gridHeight);
        view.setLayoutX(gridX);
        view.setLayoutY(gridY);
        if (image != null && image.getWidth() > 0 && image.getHeight() > 0) {
            final double ratio = gridWidth / gridHeight;
            double w = image.getWidth();
            double h = w / ratio;
            if (h > image.getHeight()) {
                h = image.getHeight();
                w = h * ratio;
            }
            view.setViewport(new Rectangle2D((image.getWidth() - w) / 2, (image.getHeight() - h) / 2, w, h));
        }
        final Rectangle clip = new Rectangle(gridWidth, gridHeight);
        clip.setArcWidth(gridHeight * 0.06);
        clip.setArcHeight(gridHeight * 0.06);
        view.setClip(clip);
        pictureLayer.getChildren().add(view);
    }

    private Image nextImage() {
        if (pictures == LettersPictures.ANIMALS) {
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
        return loaded;
    }

    /**
     * The band at the top: "Find the letter" and the letter, large.
     */
    private void showBand() {
        band.getChildren().clear();
        final double bandHeight = gridY;
        final Group label = GameStyle.pill(gameContext.getTranslator().translate("LettersFind"), bandHeight * 0.22);
        final double cardSize = bandHeight * 0.78;
        final Rectangle letterCard = new Rectangle(cardSize * (letterCase == LettersCase.BOTH ? 1.7 : 1), cardSize);
        letterCard.setArcWidth(cardSize * 0.25);
        letterCard.setArcHeight(cardSize * 0.25);
        letterCard.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-accent; -fx-stroke-width: 4;");
        final String shown = switch (letterCase) {
            case CAPITALS -> String.valueOf(Character.toUpperCase(asked));
            case SMALL -> String.valueOf(asked);
            case BOTH -> Character.toUpperCase(asked) + " " + asked;
        };
        final Text text = letterText(shown, cardSize * 0.72, letterCase == LettersCase.SMALL);
        text.setFill(INK);
        text.setX((letterCard.getWidth() - text.getLayoutBounds().getWidth()) / 2 - text.getLayoutBounds().getMinX());
        text.setY(cardSize / 2);
        final Group letterGroup = new Group(letterCard, text);
        final double total = label.getBoundsInLocal().getWidth() + bandHeight * 0.2 + letterCard.getWidth();
        label.setLayoutX((width - total) / 2);
        label.setLayoutY((bandHeight - label.getBoundsInLocal().getHeight()) / 2);
        letterGroup.setLayoutX((width - total) / 2 + label.getBoundsInLocal().getWidth() + bandHeight * 0.2);
        letterGroup.setLayoutY((bandHeight - cardSize) / 2);
        band.getChildren().addAll(label, letterGroup);
    }

    /**
     * @return a letter in the title font (capitals) or in Andika (small letters, as at school)
     */
    private static Text letterText(final String content, final double size, final boolean small) {
        final Text text = new Text(content);
        text.setTextOrigin(VPos.CENTER);
        text.setFont(small ? smallFont(size) : GameStyle.titleFont(size));
        return text;
    }

    private static Font smallFont(final double size) {
        final var url = Letters.class.getClassLoader().getResource(SMALL_FONT);
        final Font font = url == null ? null : Font.loadFont(url.toExternalForm(), size);
        return font != null ? font : GameStyle.titleFont(size);
    }

    private void showHud() {
        hud.getChildren().clear();
        final double margin = height * 0.025;
        final Group score = GameStyle.pill(gameContext.getTranslator().translate("LettersPictures") + " " + uncovered
            + (end.getPictures() > 0 ? " / " + end.getPictures() : ""), height * 0.03);
        score.setLayoutX(margin);
        score.setLayoutY((gridY - score.getBoundsInLocal().getHeight()) / 2);
        hud.getChildren().add(score);
    }

    void sayQuestion() {
        gameContext.getSoundManager().add(SOUNDS + language + (voiceFemale ? "/f" : "/m") + "/quest/"
            + Character.toUpperCase(asked) + ".mp3");
    }

    private void sayLetter(final char letter) {
        gameContext.getSoundManager().add(SOUNDS + language + (voiceFemale ? "/f" : "/m") + "/letter/"
            + Character.toUpperCase(letter) + ".mp3");
    }

    /**
     * Makes a spot receive the gaze and the mouse.
     */
    private void listen(final Spot spot) {
        final EventHandler<Event> handler = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                lookAt(spot);
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                lookAway(spot);
            }
        };
        spot.node.addEventFilter(MouseEvent.ANY, handler);
        spot.node.addEventFilter(GazeEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(spot.node);
        gazeNodes.add(spot.node);
    }

    private void forget(final Node node) {
        gameContext.getGazeDeviceManager().removeEventFilter(node);
        gazeNodes.remove(node);
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        cards.forEach(card -> card.shake(seconds));
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    phase = Phase.PLAY;
                }
            }
            case PLAY -> fixate(seconds);
            case PICTURE -> {
                elapsed += seconds;
                if (elapsed >= PICTURE) {
                    if (end.getPictures() > 0 && uncovered >= end.getPictures()) {
                        finish();
                    } else {
                        newRound();
                        phase = Phase.PLAY;
                    }
                }
            }
            default -> {
            }
        }
    }

    private boolean isEnabled(final Spot spot) {
        return phase == Phase.PLAY && (spot == replay || spot.card != null && !spot.card.found
            && !spot.card.tried);
    }

    private void fixate(final double seconds) {
        if (looked == null || !isEnabled(looked)) {
            return;
        }
        if (!ring.isVisible()) {
            showRing(looked);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Spot spot = looked;
            progress = 0;
            ring.stop();
            // Looked at again to choose once more (the loudspeaker, or a card tried)
            looked = null;
            spot.action.run();
        }
    }

    private void showRing(final Spot spot) {
        ring.setSize(spot.ringSize);
        ring.show(spot.x, spot.y);
    }

    void lookAt(final Spot spot) {
        if (looked == spot) {
            return;
        }
        looked = spot;
        progress = 0;
        ring.stop();
        if (isEnabled(spot)) {
            showRing(spot);
        }
    }

    void lookAway(final Spot spot) {
        if (spot.card != null) {
            spot.card.tried = false;
        }
        if (looked == spot) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * A card chosen: of the letter asked, it turns and uncovers a part of the picture; of another letter, it shakes in
     * red, says its letter and counts a mistake.
     */
    void choose(final Card card) {
        if (phase != Phase.PLAY || card.found) {
            return;
        }
        if (card.letter == asked) {
            card.found = true;
            card.turn();
            forget(card.node);
            sayLetter(card.letter);
            left--;
            if (left == 0) {
                uncovered++;
                stats.incrementNumberOfGoalsReached();
                // The whole picture shows
                cards.forEach(Card::turn);
                gameContext.getSoundManager().add("data/goosegame/sounds/win.wav");
                showHud();
                phase = Phase.PICTURE;
                elapsed = 0;
            }
            return;
        }
        card.tried = true;
        card.wrong();
        sayLetter(card.letter);
        stats.incrementNumberOfMistakes();
    }

    private void clearCards() {
        cards.forEach(card -> forget(card.node));
        cards.clear();
        cardsLayer.getChildren().clear();
        if (looked != null && looked != replay) {
            looked = null;
            progress = 0;
            ring.stop();
        }
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

    Spot getReplay() {
        return replay;
    }

    FixationRing getRing() {
        return ring;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (ring != null) {
            ring.stop();
        }
        new ArrayList<>(gazeNodes).forEach(this::forget);
        cards.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * Something chosen by a fixation: a card, or the loudspeaker.
     */
    final class Spot {
        @Getter
        private final Node node;
        private final double x;
        private final double y;
        private final double ringSize;
        private final Runnable action;
        private Card card;

        Spot(final Node node, final double x, final double y, final double ringSize, final Runnable action) {
            this.node = node;
            this.x = x;
            this.y = y;
            this.ringSize = ringSize;
            this.action = action;
        }
    }

    /**
     * A card of the grid: a rounded card of a soft color with a letter; turned (gone), it uncovers the picture under it.
     */
    final class Card {
        @Getter
        private final char letter;
        @Getter
        private final String written;
        @Getter
        private final Spot spot;
        private final Group node = new Group();
        private final Rectangle back;
        private final Text text;
        @Getter
        private boolean found;
        /**
         * Chosen while wrong: it must be left and looked at again to be chosen once more.
         */
        private boolean tried;
        private double shaking;
        private final Color color;

        Card(final char letter, final String written, final Color color, final double x, final double y,
             final double w, final double h) {
            this.letter = letter;
            this.written = written;
            this.color = color;
            final double gap = Math.min(w, h) * 0.05;
            back = new Rectangle(gap, gap, w - 2 * gap, h - 2 * gap);
            back.setArcWidth(Math.min(w, h) * 0.18);
            back.setArcHeight(Math.min(w, h) * 0.18);
            back.setFill(color);
            back.setStroke(color.darker());
            back.setStrokeWidth(Math.max(2, Math.min(w, h) * 0.02));
            text = letterText(written, h * 0.62, Character.isLowerCase(written.charAt(0)));
            text.setFill(INK);
            text.setX((w - text.getLayoutBounds().getWidth()) / 2 - text.getLayoutBounds().getMinX());
            text.setY(h / 2);
            text.setMouseTransparent(true);
            node.getChildren().addAll(back, text);
            node.setLayoutX(x);
            node.setLayoutY(y);
            cardsLayer.getChildren().add(node);
            spot = new Spot(back, x + w / 2, y + h / 2, Math.min(w, h) * 0.85, () -> choose(this));
            spot.card = this;
            listen(spot);
        }

        void turn() {
            node.setVisible(false);
        }

        void wrong() {
            back.setFill(WRONG);
            text.setFill(Color.WHITE);
            shaking = SHAKE;
        }

        void shake(final double seconds) {
            if (shaking <= 0) {
                return;
            }
            shaking = Math.max(0, shaking - seconds);
            final double t = SHAKE - shaking;
            node.setTranslateX(Math.sin(t * 50) * back.getWidth() * 0.06 * (shaking / SHAKE));
            if (shaking == 0) {
                back.setFill(color);
                text.setFill(INK);
            }
        }

        boolean isVisible() {
            return node.isVisible();
        }
    }
}
