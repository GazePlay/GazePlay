package net.gazeplay.games.pianosight;

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
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;
import net.gazeplay.components.RingThickness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * The Piano (name code Piano; reviewed on 13/11/2026, maintainer's rules): a straight keyboard of one octave, from the
 * middle C to the C above, 8 white keys each of its color with the name of its note, and 5 black keys; everything drawn
 * by the game, the notes played by the MIDI synthesizer of Java.
 * <p>
 * The songs: the next key is lit; a fixation on it plays the note and lights the next one; another key plays its note,
 * without a mistake (a lingering look is not punished), and does not go on. A song finished: "Bravo !", then the whole
 * tune is played again in its rhythm, its keys lit, then another song (four tunes of the public domain, see
 * {@link Song}, never the same twice in a row); the game ends after 1, 3 or 5 songs ("Bravo", then the statistics), or
 * has no end. The free mode: each key plays its note, without end.
 * <p>
 * One loop, in real seconds: the fixation lasts the length of the settings (read at each step), nothing can be played
 * before the delay of the settings, while a song is played again, nor while the menu of the game is open. (Before the
 * review: a round keyboard, a song of the repertoire protected by copyright (RIVER.mid), a first key lit wrong, the
 * free notes off any scale, a button to open a MIDI file with the mouse only, a new progress indicator at each look,
 * nothing removed at the end, five pictures of notes of unknown source.)
 * <p>
 * Tester's feedback of 23/11/2026, maquette validated by the maintainer on 26/11/2026: each black key has a round
 * pastille, half of the color of each white key beside it, where its ring fills (point 40); a song is played to the end
 * of its start only (its first phrase), then played again whole as a reward, or whole (point 38); the person chooses
 * the song on the screen "Choisis ta chanson", at the start and after each song: 5 cards chosen by a fixation (the ring
 * along their edge), the 4 songs and "Au hasard", over the greyed keyboard; or the game chooses at random (point 39). For
 * room above it, the keyboard of the songs is lower and smaller than the one of the free mode.
 */
@Slf4j
public class Piano implements GameLifeCycle {

    /**
     * The seconds a beat lasts when a song is played again, a key stays pressed, a flying note lives, and the wait
     * after the song played again.
     */
    static final double BEAT = 0.42;
    static final double PRESSED = 0.18;
    static final double FLIGHT = 0.9;
    static final double AFTER = 0.8;
    /**
     * The MIDI number of the middle C, the first key.
     */
    static final int MIDDLE_C = 60;

    /**
     * The white keys: their semitones above the middle C, the key of their name, their color.
     */
    private static final int[] WHITE = {0, 2, 4, 5, 7, 9, 11, 12};
    private static final String[] NAMES = {"PianoDo", "PianoRe", "PianoMi", "PianoFa", "PianoSol", "PianoLa",
        "PianoSi", "PianoDo"};
    private static final Color[] COLORS = {Color.rgb(230, 70, 70), Color.rgb(245, 145, 45), Color.rgb(240, 200, 40),
        Color.rgb(80, 180, 95), Color.rgb(80, 180, 230), Color.rgb(65, 105, 210), Color.rgb(150, 85, 200),
        Color.rgb(230, 70, 70)};
    /**
     * The black keys: their semitones, and the white key they follow.
     */
    private static final int[] BLACK = {1, 3, 6, 8, 10};
    private static final int[] BLACK_AFTER = {0, 1, 3, 4, 5};
    private static final String NOTE_SHAPE = "M 9 0 L 13 0 L 13 18 Q 20 20 22 27 L 18 26 Q 15 23 13 23 L 13 40 "
        + "C 13 46 7 50 3 49 C -1 48 -1 43 3 40 C 6 38 9 38 9 39 Z";

    /**
     * The greyed keyboard while the song is chosen, as on the maquette.
     */
    static final double GREYED = 0.35;

    enum Phase { WAIT, CHOOSE, PLAY, REWARD, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final PianoMode mode;
    @Getter
    private final PianoEnd end;
    private final ReplayablePseudoRandom random;
    private final Supplier<NotePlayer> playerFactory;
    private NotePlayer player;

    /**
     * The start of the songs or the whole songs (the whole songs by default, as before the feedback).
     */
    @Getter
    @Setter
    private PianoLength length = PianoLength.WHOLE;
    /**
     * The person choosing the songs, or the game (at random by default, as before the feedback).
     */
    @Getter
    @Setter
    private PianoChoice choice = PianoChoice.RANDOM;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private Song song;
    @Getter
    private int noteIndex;
    /**
     * The notes of the song to play: those of its start, or all of them.
     */
    @Getter
    private int target;
    @Getter
    private int songs;
    private double elapsed;
    private double phaseTime;
    private int replayed;
    private boolean rewarded;

    private double width;
    private double height;
    private double whiteWidth;
    private double whiteHeight;
    private double keyboardLeft;
    private double keyboardTop;

    private Group layer;
    private Group keyboard;
    private Group keysLayer;
    private Group choiceLayer;
    private final List<Rectangle> cards = new ArrayList<>();
    private Rectangle cardRing;
    private double cardRingPerimeter;
    @Getter
    private int lookedCard = -1;
    private double cardProgress;
    private Group flying;
    private Group hint;
    private Group hud;
    private Group reward;
    private FixationRing ring;
    @Getter
    private final List<Key> keys = new ArrayList<>();
    private final List<FlyingNote> notes = new ArrayList<>();
    @Getter
    private final List<Integer> played = new ArrayList<>();
    @Getter
    private Key looked;
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
    Piano(final IGameContext gameContext, final Stats stats, final PianoMode mode, final PianoEnd end,
          final double gameSeed) {
        this(gameContext, stats, mode, end, gameSeed, Instru::new);
    }

    /**
     * @param playerFactory what plays the notes, made at the launch (a recorder in the tests)
     */
    Piano(final IGameContext gameContext, final Stats stats, final PianoMode mode, final PianoEnd end,
          final double gameSeed, final Supplier<NotePlayer> playerFactory) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.end = mode == PianoMode.FREE ? PianoEnd.FREE : end == PianoEnd.FREE ? PianoEnd.ENDLESS : end;
        this.playerFactory = playerFactory;
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
        if (mode == PianoMode.SONG) {
            // Lower and smaller, as on the maquette: the cards of the choice and the picture of the song above it
            whiteWidth = Math.min(width * 0.73 / WHITE.length, height * 0.39 / 2.4);
            whiteHeight = whiteWidth * 2.4;
            keyboardTop = height * 0.547;
        } else {
            whiteWidth = width * 0.9 / WHITE.length;
            whiteHeight = Math.min(height * 0.55, whiteWidth * 3.2);
            keyboardTop = height * 0.3;
        }
        keyboardLeft = (width - whiteWidth * WHITE.length) / 2;
        player = playerFactory.get();

        final Rectangle room = new Rectangle(0, 0, width, height);
        room.setStyle("-fx-fill: -gp-bg;");
        room.setMouseTransparent(true);
        // The case of the piano around the keys
        final double margin = whiteWidth * 0.12;
        final Rectangle body = new Rectangle(keyboardLeft - margin, keyboardTop - margin * 2.2,
            whiteWidth * WHITE.length + 2 * margin, whiteHeight + margin * 3.2);
        body.setArcWidth(margin * 2);
        body.setArcHeight(margin * 2);
        body.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.rgb(92, 62, 48)),
            new Stop(1, Color.rgb(64, 42, 34))));
        body.setMouseTransparent(true);
        final Rectangle felt = new Rectangle(keyboardLeft, keyboardTop - margin * 0.6, whiteWidth * WHITE.length,
            margin * 0.6);
        felt.setFill(Color.rgb(170, 40, 50));
        felt.setMouseTransparent(true);

        keysLayer = new Group();
        hint = new Group();
        hint.setMouseTransparent(true);
        flying = new Group();
        flying.setMouseTransparent(true);
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        reward = new Group();
        reward.setMouseTransparent(true);
        keyboard = new Group(body, felt, keysLayer);
        choiceLayer = new Group();
        choiceLayer.setVisible(false);
        layer = new Group(room, keyboard, hint, flying, ring, choiceLayer, hud, reward);
        gameContext.getChildren().add(layer);
        gameContext.getGazeDeviceManager().addStats(stats);
        buildKeys();
        songs = 0;
        phaseTime = 0;
        elapsed = 0;
        if (mode == PianoMode.SONG && choice == PianoChoice.CHOOSE) {
            buildCards();
            showChoice();
        } else {
            if (mode == PianoMode.SONG) {
                newSong();
            } else {
                stats.notifyNewRoundReady();
            }
            phase = Phase.WAIT;
        }
        if (!manual) {
            loop.start();
        }
    }

    // --- The keyboard ---

    private void buildKeys() {
        final List<Rectangle> blackRects = new ArrayList<>();
        for (int i = 0; i < BLACK.length; i++) {
            final double w = whiteWidth * 0.6;
            final double x = keyboardLeft + whiteWidth * (BLACK_AFTER[i] + 1) - w / 2;
            blackRects.add(new Rectangle(x, keyboardTop, w, whiteHeight * 0.6));
        }
        // The white keys: their outline without the black keys, so that a look is on one key only
        for (int i = 0; i < WHITE.length; i++) {
            final double x = keyboardLeft + whiteWidth * i;
            final Rectangle rect = new Rectangle(x + 1.5, keyboardTop, whiteWidth - 3, whiteHeight);
            rect.setArcWidth(whiteWidth * 0.16);
            rect.setArcHeight(whiteWidth * 0.16);
            Shape shape = rect;
            for (final Rectangle black : blackRects) {
                shape = Shape.subtract(shape, black);
            }
            final Color color = COLORS[i];
            shape.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, color.interpolate(Color.WHITE, 0.82)), new Stop(1, color.interpolate(Color.WHITE, 0.6))));
            shape.setStroke(Color.rgb(120, 100, 90));
            shape.setStrokeWidth(Math.max(1.5, whiteWidth * 0.015));
            final double bandHeight = whiteHeight * 0.2;
            final Rectangle band = new Rectangle(x + whiteWidth * 0.1, keyboardTop + whiteHeight - bandHeight
                - whiteWidth * 0.08, whiteWidth * 0.8, bandHeight);
            band.setArcWidth(whiteWidth * 0.2);
            band.setArcHeight(whiteWidth * 0.2);
            band.setFill(color);
            band.setMouseTransparent(true);
            final double size = Math.min(bandHeight * 0.5, whiteWidth * 0.3);
            final Text name = new Text(gameContext.getTranslator().translate(NAMES[i]));
            name.setFont(GameStyle.titleFont(size));
            name.setStyle(GameStyle.titleFontStyle(size));
            name.setFill(Color.WHITE);
            name.setX(x + (whiteWidth - name.getLayoutBounds().getWidth()) / 2 - name.getLayoutBounds().getMinX());
            name.setY(band.getY() + (bandHeight - name.getLayoutBounds().getHeight()) / 2
                - name.getLayoutBounds().getMinY());
            name.setMouseTransparent(true);
            // The ring around the name of the note: the part of the key under the black keys is too narrow for it
            final Key key = new Key(WHITE[i], shape, color, x + whiteWidth / 2,
                band.getY() + bandHeight / 2, whiteWidth * 0.86);
            key.node.getChildren().addAll(band, name);
            keys.add(key);
        }
        for (int i = 0; i < BLACK.length; i++) {
            final Rectangle rect = blackRects.get(i);
            rect.setArcWidth(whiteWidth * 0.1);
            rect.setArcHeight(whiteWidth * 0.1);
            rect.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.rgb(70, 62, 78)),
                new Stop(1, Color.rgb(34, 30, 40))));
            rect.setStroke(Color.rgb(20, 18, 24));
            rect.setStrokeWidth(Math.max(1.5, whiteWidth * 0.015));
            final Color color = Color.rgb(70, 62, 78);
            // The pastille in the bottom of the key, where its ring fills: half of the color of each white key beside
            // it (point 40)
            final double cx = rect.getX() + rect.getWidth() / 2;
            final double cy = rect.getY() + rect.getHeight() * 0.79;
            final Key key = new Key(BLACK[i], rect, color, cx, cy, rect.getWidth() * 0.95);
            key.node.getChildren().add(pastille(cx, cy, rect.getWidth() * 0.29, COLORS[BLACK_AFTER[i]],
                COLORS[BLACK_AFTER[i] + 1]));
            keys.add(key);
        }
        // The black keys over the white ones
        for (final Key key : keys) {
            keysLayer.getChildren().add(key.node);
            key.listen();
        }
    }

    /**
     * @return a round pastille, its left half of one color and its right half of another, circled in white
     */
    static Group pastille(final double cx, final double cy, final double radius, final Color left, final Color right) {
        final Arc leftHalf = new Arc(cx, cy, radius, radius, 90, 180);
        leftHalf.setType(ArcType.ROUND);
        leftHalf.setFill(left);
        final Arc rightHalf = new Arc(cx, cy, radius, radius, -90, 180);
        rightHalf.setType(ArcType.ROUND);
        rightHalf.setFill(right);
        final Circle edge = new Circle(cx, cy, radius);
        edge.setFill(Color.TRANSPARENT);
        edge.setStroke(Color.WHITE);
        edge.setStrokeWidth(Math.max(2, radius * 0.15));
        final Group pastille = new Group(leftHalf, rightHalf, edge);
        pastille.setMouseTransparent(true);
        return pastille;
    }

    /**
     * @return the key of a semitone above the middle C
     */
    Key key(final int semitone) {
        return keys.stream().filter(k -> k.semitone == semitone).findFirst().orElseThrow();
    }

    /**
     * Frames the key to play and puts a bouncing arrow over it; nothing in the free mode or between two songs.
     */
    private void showHint() {
        hint.getChildren().clear();
        if (mode != PianoMode.SONG || phase == Phase.REWARD || phase == Phase.CHOOSE || song == null
            || noteIndex >= target) {
            return;
        }
        final Key key = key(song.getNotes()[noteIndex]);
        // The outline of the key itself (a white key goes around the black ones)
        final Shape frame = Shape.union(key.shape, new Rectangle(0, 0, 0, 0));
        frame.setFill(Color.TRANSPARENT);
        frame.setStyle("-fx-stroke: -gp-accent;");
        frame.setStrokeWidth(Math.max(4, whiteWidth * 0.05));
        final double size = whiteWidth * 0.32;
        final double top = keyboardTop - whiteWidth * 0.12 * 2.2 - size * 1.3;
        final Polygon arrow = new Polygon(key.x - size / 2, top, key.x + size / 2, top, key.x, top + size * 0.8);
        arrow.setStyle("-fx-fill: -gp-accent;");
        hint.getChildren().addAll(frame, arrow);
    }

    private void showHud() {
        hud.getChildren().clear();
        if (mode != PianoMode.SONG) {
            return;
        }
        final double size = height * 0.032;
        final Group done = GameStyle.pill(gameContext.getTranslator().translate("PianoDone") + " " + songs
            + (end.getSongs() > 0 ? " / " + end.getSongs() : ""), size);
        done.setLayoutX(width - done.getBoundsInLocal().getWidth() - height * 0.03);
        done.setLayoutY(height * 0.025);
        hud.getChildren().add(done);
        if (phase == Phase.CHOOSE) {
            final Group title = GameStyle.pill(gameContext.getTranslator().translate("PianoChooseSong"),
                height * 0.039);
            title.setLayoutX((width - title.getBoundsInLocal().getWidth()) / 2);
            title.setLayoutY(height * 0.045);
            hud.getChildren().add(title);
            return;
        }
        if (song == null) {
            return;
        }
        final Group title = GameStyle.pill(gameContext.getTranslator().translate(song.getKey()), size);
        title.setLayoutX(height * 0.03);
        title.setLayoutY(height * 0.025);
        final Group count = GameStyle.pill(gameContext.getTranslator().translate("PianoNotes") + " "
            + Math.min(noteIndex, target) + " / " + target, size);
        count.setLayoutX(height * 0.03 + title.getBoundsInLocal().getWidth() + height * 0.02);
        count.setLayoutY(height * 0.025);
        hud.getChildren().addAll(title, count);
    }

    // --- The choice of the song (point 39) ---

    /**
     * The 5 cards of the choice, in a row over the keyboard: the 4 songs with their picture and title, then "Au hasard"
     * with a die, its edge dashed; one ring along the edge of the card looked at.
     */
    private void buildCards() {
        final Song[] all = Song.values();
        final int count = all.length + 1;
        final double bodyTop = keyboardTop - whiteWidth * 0.12 * 2.2;
        final double top = height * 0.154;
        double cardWidth = Math.min(width * 0.146, width * 0.9 / (count + (count - 1) * 0.14));
        cardWidth = Math.min(cardWidth, (bodyTop - height * 0.04 - top) / 1.18);
        final double cardHeight = cardWidth * 1.18;
        final double gap = cardWidth * 0.14;
        final double left = (width - count * cardWidth - (count - 1) * gap) / 2;
        final double labelSize = cardHeight * 0.093;
        for (int i = 0; i < count; i++) {
            final double x = left + i * (cardWidth + gap);
            final Rectangle card = GameStyle.pictureCard(x, top, cardWidth, cardHeight);
            final Group content = new Group();
            content.setMouseTransparent(true);
            final double pictureSize = cardWidth * 0.6;
            final double pictureTop = top + cardHeight * 0.1;
            final String label;
            if (i < all.length) {
                final ImageView picture = new ImageView(new Image(all[i].getPicture()));
                picture.setFitWidth(pictureSize);
                picture.setFitHeight(pictureSize);
                picture.setPreserveRatio(true);
                picture.setX(x + (cardWidth - pictureSize) / 2);
                picture.setY(pictureTop);
                content.getChildren().add(picture);
                label = all[i].getKey();
            } else {
                card.setStyle("-fx-fill: " + GameStyle.PICTURE_BACKGROUND + "; -fx-stroke: #C9B597; -fx-stroke-width: 3;");
                card.getStrokeDashArray().setAll(cardWidth * 0.06, cardWidth * 0.04);
                content.getChildren().add(die(x + cardWidth / 2, pictureTop + pictureSize / 2, pictureSize * 0.72));
                label = "PianoRandomSong";
            }
            final Text name = new Text(gameContext.getTranslator().translate(label));
            name.setFont(GameStyle.titleFont(labelSize));
            name.setStyle(GameStyle.titleFontStyle(labelSize) + " -fx-fill: " + GameStyle.CARD_TEXT + ";");
            name.setTextAlignment(TextAlignment.CENTER);
            name.setWrappingWidth(cardWidth * 0.9);
            name.setX(x + cardWidth * 0.05 - name.getLayoutBounds().getMinX());
            final double labelTop = pictureTop + pictureSize + cardHeight * 0.04;
            final double labelRoom = top + cardHeight * 0.96 - labelTop;
            name.setY(labelTop + Math.max(0, (labelRoom - name.getLayoutBounds().getHeight()) / 2)
                - name.getLayoutBounds().getMinY());
            content.getChildren().add(name);
            final int index = i;
            final EventHandler<Event> handler = event -> {
                final var type = event.getEventType();
                if (type == MouseEvent.MOUSE_ENTERED || type == MouseEvent.MOUSE_MOVED
                    || type == GazeEvent.GAZE_ENTERED || type == GazeEvent.GAZE_MOVED) {
                    lookAtCard(index);
                } else if (type == MouseEvent.MOUSE_EXITED || type == GazeEvent.GAZE_EXITED) {
                    lookAwayCard(index);
                }
            };
            card.addEventFilter(MouseEvent.ANY, handler);
            card.addEventFilter(GazeEvent.ANY, handler);
            gameContext.getGazeDeviceManager().addEventFilter(card);
            cards.add(card);
            choiceLayer.getChildren().addAll(card, content);
        }
        // The ring along the edge of a card, as in Memory, Magic Cards and "Recettes magiques"
        cardRing = new Rectangle(0, 0, cardWidth, cardHeight);
        final double corner = GameStyle.cardRadius(cardWidth, cardHeight);
        cardRing.setArcWidth(2 * corner);
        cardRing.setArcHeight(2 * corner);
        cardRing.setFill(null);
        cardRing.setStrokeType(StrokeType.INSIDE);
        cardRing.setStrokeLineCap(StrokeLineCap.BUTT);
        RingThickness.apply(cardRing, Math.max(5, cardWidth * 0.045));
        cardRing.setStyle("-fx-stroke: -gp-ring;");
        cardRing.setMouseTransparent(true);
        cardRing.setVisible(false);
        cardRingPerimeter = GameStyle.clockwiseRing(cardRing);
        choiceLayer.getChildren().add(cardRing);
    }

    /**
     * @return a white die showing 5, centered on a point
     */
    private static Group die(final double cx, final double cy, final double size) {
        final Color ink = Color.rgb(107, 90, 74);
        final Rectangle face = new Rectangle(cx - size / 2, cy - size / 2, size, size);
        face.setArcWidth(size * 0.42);
        face.setArcHeight(size * 0.42);
        face.setFill(Color.WHITE);
        face.setStroke(ink);
        face.setStrokeWidth(Math.max(2, size * 0.06));
        final Group die = new Group(face);
        final double d = size * 0.26;
        for (final double[] dot : new double[][]{{-d, -d}, {d, -d}, {0, 0}, {-d, d}, {d, d}}) {
            die.getChildren().add(new Circle(cx + dot[0], cy + dot[1], size * 0.095, ink));
        }
        return die;
    }

    /**
     * The screen "Choisis ta chanson": the cards over the greyed keyboard, which plays nothing.
     */
    private void showChoice() {
        hint.getChildren().clear();
        reward.getChildren().clear();
        ring.stop();
        looked = null;
        progress = 0;
        cardProgress = 0;
        cardRing.setVisible(false);
        keyboard.setOpacity(GREYED);
        choiceLayer.setVisible(true);
        setPhase(Phase.CHOOSE);
        showHud();
    }

    void lookAtCard(final int index) {
        if (lookedCard == index) {
            return;
        }
        lookedCard = index;
        cardProgress = 0;
        cardRing.setVisible(false);
    }

    void lookAwayCard(final int index) {
        if (lookedCard == index) {
            lookedCard = -1;
            cardProgress = 0;
            cardRing.setVisible(false);
        }
    }

    /**
     * The fixation on a card, once the delay of the settings is over (new choices on the screen).
     */
    private void fixateCard(final double seconds) {
        if (lookedCard < 0 || phaseTime < configuration.getGameStartDelay() / 1000.0) {
            return;
        }
        final Rectangle card = cards.get(lookedCard);
        cardRing.setLayoutX(card.getX());
        cardRing.setLayoutY(card.getY());
        cardRing.setVisible(true);
        cardProgress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        GameStyle.fillRing(cardRing, cardRingPerimeter, Math.min(1, cardProgress));
        if (cardProgress >= 1) {
            chooseCard(lookedCard);
        }
    }

    /**
     * A card chosen: its song, or one at random for the last card; the keyboard back, after the delay of the settings.
     */
    void chooseCard(final int index) {
        if (phase != Phase.CHOOSE) {
            return;
        }
        final Song[] all = Song.values();
        final Song chosen = index < all.length ? all[index] : randomSong();
        cardProgress = 0;
        cardRing.setVisible(false);
        choiceLayer.setVisible(false);
        keyboard.setOpacity(1);
        elapsed = 0;
        setPhase(Phase.WAIT);
        startSong(chosen);
    }

    // --- The game ---

    /**
     * @return a song at random, never the one just played
     */
    private Song randomSong() {
        final Song[] all = Song.values();
        Song next;
        do {
            next = all[random.nextInt(all.length)];
        } while (next == song && all.length > 1);
        return next;
    }

    /**
     * A new song at random.
     */
    private void newSong() {
        startSong(randomSong());
    }

    private void startSong(final Song next) {
        song = next;
        noteIndex = 0;
        target = song.size(length);
        reward.getChildren().clear();
        stats.notifyNewRoundReady();
        // A goal per note to play, and one for the song finished
        stats.incrementNumberOfGoalsToReach(target + 1);
        showHint();
        showHud();
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        phaseTime += seconds;
        for (final Key key : keys) {
            key.release(seconds);
        }
        fly(seconds);
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    setPhase(Phase.PLAY);
                }
            }
            case CHOOSE -> fixateCard(seconds);
            case PLAY -> fixate(seconds);
            case REWARD -> replay();
            default -> {
            }
        }
    }

    private void setPhase(final Phase next) {
        phase = next;
        phaseTime = 0;
    }

    /**
     * The song played again in its rhythm, its keys pressed, then the next song or the end.
     */
    private void replay() {
        if (!rewarded) {
            rewarded = true;
            replayed = 0;
            showReward();
        }
        double start = 0.6;
        for (int i = 0; i < replayed; i++) {
            start += song.getBeats()[i] * BEAT;
        }
        while (replayed < song.size() && phaseTime >= start) {
            sound(key(song.getNotes()[replayed]), false);
            start += song.getBeats()[replayed] * BEAT;
            replayed++;
        }
        if (replayed >= song.size() && phaseTime >= start + AFTER) {
            if (end.getSongs() > 0 && songs >= end.getSongs()) {
                finish();
            } else if (choice == PianoChoice.CHOOSE) {
                showChoice();
            } else {
                // Back to the game first: the next key of the new song is framed
                setPhase(Phase.PLAY);
                newSong();
            }
        }
    }

    private void fixate(final double seconds) {
        if (looked == null || Stats.isConfigMenuOpen()) {
            return;
        }
        if (!ring.isVisible()) {
            ring.setSize(looked.ringSize);
            ring.show(looked.x, looked.ringY);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Key key = looked;
            ring.stop();
            looked = null;
            progress = 0;
            choose(key);
        }
    }

    void lookAt(final Key key) {
        if (looked == key) {
            return;
        }
        looked = key;
        progress = 0;
        ring.stop();
    }

    void lookAway(final Key key) {
        if (looked == key) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * A key chosen: its note played; in a song, the right key goes on to the next note, or finishes the song.
     */
    void choose(final Key key) {
        if (phase != Phase.PLAY) {
            return;
        }
        sound(key, true);
        if (mode != PianoMode.SONG || key.semitone != song.getNotes()[noteIndex]) {
            return;
        }
        stats.incrementNumberOfGoalsReached();
        noteIndex++;
        if (noteIndex >= target) {
            songs++;
            stats.incrementNumberOfGoalsReached();
            gameContext.getSoundManager().add("data/goosegame/sounds/win.wav");
            rewarded = false;
            setPhase(Phase.REWARD);
        }
        showHint();
        showHud();
    }

    /**
     * A note played: the key pressed, a note flying from it.
     */
    private void sound(final Key key, final boolean fly) {
        player.play(MIDDLE_C + key.semitone);
        played.add(key.semitone);
        key.press();
        if (fly) {
            final SVGPath shape = new SVGPath();
            shape.setContent(NOTE_SHAPE);
            final double scale = whiteWidth * 0.012;
            shape.setScaleX(scale);
            shape.setScaleY(scale);
            shape.setFill(key.color);
            shape.setStroke(Color.rgb(60, 50, 50, 0.6));
            shape.setStrokeWidth(1);
            final Group node = new Group(shape);
            node.setLayoutX(key.x);
            node.setLayoutY(keyboardTop - whiteWidth * 0.5);
            flying.getChildren().add(node);
            notes.add(new FlyingNote(node, random.nextDouble() - 0.5));
        }
    }

    private void fly(final double seconds) {
        final Iterator<FlyingNote> it = notes.iterator();
        while (it.hasNext()) {
            final FlyingNote note = it.next();
            note.age += seconds;
            final double u = note.age / FLIGHT;
            note.node.setTranslateY(-height * 0.18 * u);
            note.node.setTranslateX(note.drift * whiteWidth * 0.8 * u);
            note.node.setOpacity(Math.max(0, 1 - u));
            if (u >= 1) {
                flying.getChildren().remove(note.node);
                it.remove();
            }
        }
    }

    /**
     * A star beside "Bravo !", over the keyboard.
     */
    private void showReward() {
        final double r = height * 0.04;
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("PianoWell"), height * 0.04);
        final double pillWidth = pill.getBoundsInLocal().getWidth();
        final double pillHeight = pill.getBoundsInLocal().getHeight();
        final double gap = r * 0.4;
        final double left = width / 2 - (2 * r + gap + pillWidth) / 2;
        final double cy = height * 0.13;
        final Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            final double a = -Math.PI / 2 + i * Math.PI / 5;
            final double d = i % 2 == 0 ? r : r * 0.45;
            star.getPoints().addAll(left + r + d * Math.cos(a), cy + r * 0.08 + d * Math.sin(a));
        }
        star.setFill(Color.rgb(255, 200, 40));
        star.setStroke(Color.rgb(200, 120, 0));
        star.setStrokeWidth(Math.max(2, r * 0.07));
        pill.setLayoutX(left + 2 * r + gap);
        pill.setLayoutY(cy - pillHeight / 2);
        reward.getChildren().setAll(star, pill);
        if (target < song.size()) {
            final Group whole = GameStyle.pill(gameContext.getTranslator().translate("PianoWholeSong") + " "
                + song.size() + " notes", height * 0.029);
            whole.setLayoutX((width - whole.getBoundsInLocal().getWidth()) / 2);
            whole.setLayoutY(height * 0.414);
            reward.getChildren().add(whole);
        }
    }

    /**
     * @return whether the screen of the choice of the song is shown, for the tests
     */
    boolean isChoiceShown() {
        return choiceLayer != null && choiceLayer.isVisible();
    }

    /**
     * @return the opacity of the keyboard, for the tests
     */
    double getKeyboardOpacity() {
        return keyboard.getOpacity();
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
     * @return whether a key is framed as the one to play, for the tests
     */
    boolean isHinted() {
        return !hint.getChildren().isEmpty();
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        for (final Key key : keys) {
            gameContext.getGazeDeviceManager().removeEventFilter(key.shape);
        }
        keys.clear();
        for (final Rectangle card : cards) {
            gameContext.getGazeDeviceManager().removeEventFilter(card);
        }
        cards.clear();
        lookedCard = -1;
        if (ring != null) {
            ring.stop();
        }
        looked = null;
        if (player != null) {
            player.close();
            player = null;
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A note flying away from a key played.
     */
    private static final class FlyingNote {
        private final Group node;
        private final double drift;
        private double age;

        FlyingNote(final Group node, final double drift) {
            this.node = node;
            this.drift = drift;
        }
    }

    /**
     * A key: its note, its shape (looked at), where its ring goes; pressed for a moment when it plays.
     */
    final class Key {
        @Getter
        private final int semitone;
        private final Shape shape;
        private final Color color;
        private final double x;
        private final double ringY;
        private final double ringSize;
        private final Group node;
        private double pressed;

        Key(final int semitone, final Shape shape, final Color color, final double x, final double ringY,
            final double ringSize) {
            this.semitone = semitone;
            this.shape = shape;
            this.color = color;
            this.x = x;
            this.ringY = ringY;
            this.ringSize = ringSize;
            node = new Group(shape);
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
            shape.addEventFilter(MouseEvent.ANY, handler);
            shape.addEventFilter(GazeEvent.ANY, handler);
            gameContext.getGazeDeviceManager().addEventFilter(shape);
        }

        void press() {
            pressed = PRESSED;
            node.setTranslateY(whiteWidth * 0.04);
            node.setOpacity(0.85);
        }

        void release(final double seconds) {
            if (pressed > 0) {
                pressed = Math.max(0, pressed - seconds);
                if (pressed == 0) {
                    node.setTranslateY(0);
                    node.setOpacity(1);
                }
            }
        }

        boolean isPressed() {
            return pressed > 0;
        }
    }

    /**
     * @return the settings of the menu (Esc) acting on this game (17/11/2026): the others are greyed
     */
    @Override
    public java.util.Set<net.gazeplay.MenuSetting> menuSettings() {
        return net.gazeplay.MenuSetting.fixationWithSharedRing();
    }
}
