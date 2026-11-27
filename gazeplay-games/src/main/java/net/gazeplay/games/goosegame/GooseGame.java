package net.gazeplay.games.goosegame;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.DiceRoller;
import net.gazeplay.components.DiceView;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;
import net.gazeplay.components.RingThickness;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * "Jeu de l'oie" (name code GooseGame; reviewed on 12/11/2026, maintainer's rules). A board in a spiral of 63 squares;
 * each player in turn rolls two dice (a fixation on the "Roll" button; the computer rolls by itself) and moves their
 * pawn, a Biboule. With the classic rules: a goose moves the pawn again by the same throw, the bridge (7) leads to 32,
 * the inn (19), the well (31) and the prison (52) make it miss one turn. With the simple rules, only the geese. The end (63) must be reached exactly, else the pawn moves back. The first pawn on 63 wins: "Bravo" when a
 * person wins, then the statistics.
 * <p>
 * One player against 1 to 3 pawns of the computer, or 2 to 4 persons taking turns. One loop, in real seconds: the
 * fixation lasts the length of the settings (read at each step), nothing can be chosen before the delay of the
 * settings, and nothing moves while the menu of the game is open. (Before the review: persons only, the game stuck after
 * a win, everything shown twice when all the pawns waited, the throws added to the goals reached, piled-up messages in
 * the font of the system with "Player 2", a board, a button and sounds of unknown source, two copies of the whole game.)
 * <p>
 * Tester's feedback of 23/11/2026, point 65 (maintainer's choices of 27/11/2026): softer rules than the classic game, so
 * that it is not frustrating. With any players: no arrow back to the start, one turn missed on the inn, the well and the
 * prison (see {@link GooseBoard}); for each person, the third throw that can reach the end exactly does it. Against the
 * computer, when one of its pawns is ahead of the person's, the person's throw is helped: the end if it can be reached,
 * else a goose or the bridge, else 12 or 11 when they do not stop on a trap.
 * <p>
 * Point 64 (maquette validated by the maintainer on 27/11/2026): the pawns are bigger than the squares, on a white base
 * ringed with their color and with a shadow; the pawn whose turn it is stands in a yellow halo that pulses, and so does
 * the "Roll" button when it waits for a fixation; the simple rules have their own board, where only the geese, the
 * start and the end are drawn.
 * <p>
 * Point 66 (same maquette): to understand what happens, each person chooses the color of their Biboule before the
 * game, on the screen "Choisis ta Biboule" (4 cards chosen by a fixation, the ring along their edge; with several
 * persons, each in turn, the colors taken greyed; the computer takes the colors left); the pawns move more slowly (0.5 s
 * a square, 0.28 before); once thrown, the dice stay drawn flat at the bottom right, where the "Roll" button was, and a
 * pip of them fills with the color of the pawn at each square it moves by the throw.
 */
public class GooseGame implements GameLifeCycle {

    static final String[] COLORS = {"Blue", "Orange", "Green", "Yellow"};
    /**
     * The color of the ring of the base of each pawn.
     */
    /**
     * Where the body of a Biboule is in its picture, as a part of its height.
     */
    static final double BODY_CENTER = 0.56;
    private static final Color[] RINGS = {Color.rgb(58, 123, 213), Color.rgb(242, 140, 56), Color.rgb(76, 175, 80),
        Color.rgb(232, 185, 28)};
    /**
     * In seconds: the dice rolling (the die turns for 1.5 s), a hop of a pawn to the next square, a jump (the bridge),
     * the computer thinking, the end shown.
     */
    static final double ROLL = 1.6;
    static final double HOP = 0.5;
    static final double JUMP = 0.7;
    static final double COMPUTER_WAIT = 1.0;
    static final double SHOW = 2.4;
    /**
     * The throw that can reach the end exactly that does it, for a person (point 65).
     */
    static final int END_HELP = 3;

    private static final String BOARD = "data/goosegame/gooseboard.png";
    private static final String SIMPLE_BOARD = "data/goosegame/gooseboard-simple.png";
    /**
     * The size of a pawn (its Biboule), in squares of the board (point 64; 1.3 before), and the halo: its size in
     * squares, its pulse in seconds and how much it grows.
     */
    static final double PAWN_SQUARES = 1.8;
    static final double HALO_SQUARES = 1.0;
    static final double PULSE = 1.2;
    static final double PULSE_GROWTH = 0.12;
    private static final Color HALO = Color.rgb(255, 222, 80);
    private static final String POSITIONS = "data/goosegame/positions.json";
    private static final String SOUNDS = "data/goosegame/sounds/";
    private static final String BIBOULES = "data/shooter/biboule/images/";
    private static final Color BUTTON = Color.rgb(242, 166, 90);
    private static final Color PIP = Color.rgb(38, 34, 47);

    enum Phase { CHOOSE, WAIT, READY, ROLLING, MOVING, PAUSE, SHOW, OVER }

    /**
     * The places of the pips on a face of a die (1 to 6), in parts of its side from its center, in the order they fill.
     */
    private static final double[][][] PIPS = {
        {{0, 0}},
        {{-1, -1}, {1, 1}},
        {{-1, -1}, {0, 0}, {1, 1}},
        {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}},
        {{-1, -1}, {1, -1}, {0, 0}, {-1, 1}, {1, 1}},
        {{-1, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {1, 1}}};

    private static final java.util.Map<String, Image> GAME_IMAGES = new java.util.HashMap<>();

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final GooseRules rules;
    @Getter
    private final GoosePlayers players;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private final List<Pawn> pawns = new ArrayList<>();
    @Getter
    private int current;
    @Getter
    private int lastThrow;
    @Getter
    private Pawn winner;
    @Getter
    private String lastMessage = "";
    private double elapsed;
    private double pauseLeft;
    private Runnable afterPause;

    // The pawn moving, its squares to go through, and what comes after
    private Pawn mover;
    private final List<Integer> hops = new ArrayList<>();
    private double hopLength;
    private double hopTime;
    private Point2D hopFrom;
    private Runnable afterMove;

    private double width;
    private double height;
    private final List<Point2D> centers = new ArrayList<>();
    private double pawnSize;

    private Group layer;
    private ImageView board;
    private Group pawnsLayer;
    /**
     * The flat dice of the throw, their pips filling as the pawn moves (point 66), and the count of the squares.
     */
    private Group flatDice;
    private final List<Circle> pips = new ArrayList<>();
    private Group squaresCount;
    @Getter
    private int pipsFilled;
    private boolean countingPips;
    private Color pipColor = PIP;
    @Getter
    private final int[] faces = new int[2];
    /**
     * The choice of the colors (point 66): whether the persons choose them, the colors chosen, the person choosing, the
     * cards, the card looked at and its fixation.
     */
    private boolean chooseColors;
    private final List<Integer> chosen = new ArrayList<>();
    private Group choiceLayer;
    private final List<Rectangle> cards = new ArrayList<>();
    private final List<Group> cardContents = new ArrayList<>();
    private Rectangle cardRing;
    private double cardRingPerimeter;
    @Getter
    private int lookedCard = -1;
    private double cardProgress;
    private Circle halo;
    private Circle buttonHalo;
    private double pulseTime;
    private Group diceGroup;
    private Group rollButton;
    private Group hud;
    private Group message;
    private FixationRing ring;
    private final List<DiceRoller> dice = new ArrayList<>();
    private double buttonX;
    private double buttonY;
    private double buttonRadius;
    private boolean lookingAtButton;
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
    GooseGame(final IGameContext gameContext, final Stats stats, final GooseRules rules, final GoosePlayers players,
              final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.rules = rules;
        this.players = players;
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

        final Rectangle meadow = new Rectangle(0, 0, width, height);
        meadow.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(168, 214, 150)), new Stop(1, Color.rgb(128, 188, 118))));
        meadow.setMouseTransparent(true);

        // The board under a band for the turn and the messages, as large as it can be
        final Image boardImage = gameImage(rules == GooseRules.SIMPLE ? SIMPLE_BOARD : BOARD);
        final double band = height * 0.13;
        final double scale = Math.min((height - band - height * 0.05) / boardImage.getHeight(),
            width * 0.97 / boardImage.getWidth());
        final double boardX = (width - boardImage.getWidth() * scale) / 2;
        final double boardY = band + (height - band - boardImage.getHeight() * scale) / 2;
        board = new ImageView(boardImage);
        board.setFitWidth(boardImage.getWidth() * scale);
        board.setFitHeight(boardImage.getHeight() * scale);
        board.setLayoutX(boardX);
        board.setLayoutY(boardY);
        board.setMouseTransparent(true);
        final JsonArray positions = (JsonArray) JsonParser.parseReader(new InputStreamReader(
            Objects.requireNonNull(GooseGame.class.getClassLoader().getResourceAsStream(POSITIONS)),
            StandardCharsets.UTF_8));
        for (int i = 0; i <= GooseBoard.END; i++) {
            final JsonObject position = (JsonObject) positions.get(i);
            centers.add(new Point2D(boardX + position.get("x").getAsDouble() * scale,
                boardY + position.get("y").getAsDouble() * scale));
        }
        // A tile of the board is 94 pixels across in the image
        final double square = 94 * scale;
        pawnSize = square * PAWN_SQUARES;

        pawnsLayer = new Group();
        pawnsLayer.setMouseTransparent(true);
        // The halo of the pawn whose turn it is, under the pawns
        halo = new Circle(0, 0, square * HALO_SQUARES, new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
            CycleMethod.NO_CYCLE, new Stop(0, HALO.deriveColor(0, 1, 1, 0.95)), new Stop(0.72, HALO.deriveColor(0, 1, 1,
            0.95)), new Stop(1, HALO.deriveColor(0, 1, 1, 0))));
        halo.setVisible(false);
        pawnsLayer.getChildren().add(halo);

        diceGroup = diceGroup();
        diceGroup.setVisible(false);
        rollButton = rollButton();
        flatDice = new Group();
        flatDice.setMouseTransparent(true);
        flatDice.setVisible(false);
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        buttonHalo = new Circle(buttonX, buttonY, buttonRadius * 1.4, new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
            CycleMethod.NO_CYCLE, new Stop(0, HALO.deriveColor(0, 1, 1, 0.9)), new Stop(0.7, HALO.deriveColor(0, 1, 1,
            0.9)), new Stop(1, HALO.deriveColor(0, 1, 1, 0))));
        buttonHalo.setMouseTransparent(true);
        buttonHalo.visibleProperty().bind(rollButton.visibleProperty());
        choiceLayer = new Group();
        choiceLayer.setVisible(false);
        layer = new Group(meadow, board, pawnsLayer, diceGroup, flatDice, buttonHalo, rollButton, ring, choiceLayer, hud,
            message);
        gameContext.getChildren().add(layer);
        rollButton.setVisible(false);

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        current = 0;
        elapsed = 0;
        chosen.clear();
        if (chooseColors && players.getHumans() > 0) {
            buildCards();
            showChoice();
        } else {
            startGame();
        }
        if (!manual) {
            loop.start();
        }
    }

    /**
     * The pawns, each with its color: those the persons chose, then the colors left for the computer, in their order;
     * then the wait of the start.
     */
    private void startGame() {
        final List<Integer> colors = new ArrayList<>(chosen);
        for (int color = 0; color < COLORS.length && colors.size() < players.getPawns(); color++) {
            if (!colors.contains(color)) {
                colors.add(color);
            }
        }
        createPawns(colors);
        placePawns();
        board.setOpacity(1);
        choiceLayer.setVisible(false);
        elapsed = 0;
        phase = Phase.WAIT;
    }

    private void createPawns(final List<Integer> colors) {
        for (int i = 0; i < players.getPawns(); i++) {
            final int color = colors.get(i);
            final ImageView view = new ImageView(gameImage(BIBOULES + COLORS[color] + ".png"));
            view.setPreserveRatio(true);
            view.setFitWidth(pawnSize);
            view.setFitHeight(pawnSize);
            // The body of a Biboule is at the middle of the width of its picture and at 56 % of its height (the wings
            // above it; measured on the four pictures, 500 x 400): it is put on the center of the base
            final double shownHeight = pawnSize * view.getImage().getHeight() / Math.max(1, view.getImage().getWidth());
            view.setX(-pawnSize / 2);
            view.setY(-shownHeight * BODY_CENTER);
            // A white base ringed with the color of the pawn, so that it stands out on every square
            final Circle base = new Circle(0, 0, pawnSize * 0.36, Color.rgb(255, 255, 255, 0.92));
            base.setStroke(RINGS[color]);
            base.setStrokeWidth(Math.max(2, pawnSize * 0.07));
            base.setEffect(new DropShadow(pawnSize * 0.06, 0, pawnSize * 0.04, Color.rgb(0, 0, 0, 0.3)));
            final Group node = new Group(base, view);
            final Pawn pawn = new Pawn(i, COLORS[color], i < players.getHumans(), node, view.getImage(), color);
            pawns.add(pawn);
            pawnsLayer.getChildren().add(node);
        }
    }

    // --- The choice of the colors (point 66) ---

    /**
     * The 4 cards of the choice, in a row: a Biboule and its color each; one ring along the edge of the card looked at.
     */
    private void buildCards() {
        final double cardWidth = Math.min(width * 0.16, height * 0.36 / 1.2);
        final double cardHeight = cardWidth * 1.2;
        final double gap = cardWidth * 0.15;
        final double left = (width - COLORS.length * cardWidth - (COLORS.length - 1) * gap) / 2;
        final double top = height * 0.24;
        for (int i = 0; i < COLORS.length; i++) {
            final double x = left + i * (cardWidth + gap);
            final Rectangle card = GameStyle.pictureCard(x, top, cardWidth, cardHeight);
            card.setStyle("-fx-fill: " + GameStyle.PICTURE_BACKGROUND + "; -fx-stroke: " + toWeb(RINGS[i])
                + "; -fx-stroke-width: " + Math.max(3, cardWidth * 0.02) + ";");
            final double baseRadius = cardWidth * 0.33;
            final double cx = x + cardWidth / 2;
            final double cy = top + cardHeight * 0.4;
            final Circle base = new Circle(cx, cy, baseRadius, Color.WHITE);
            base.setStroke(RINGS[i]);
            base.setStrokeWidth(Math.max(3, baseRadius * 0.12));
            final ImageView view = new ImageView(gameImage(BIBOULES + COLORS[i] + ".png"));
            view.setPreserveRatio(true);
            final double size = baseRadius * 2.3;
            view.setFitWidth(size);
            final double shownHeight = size * view.getImage().getHeight() / Math.max(1, view.getImage().getWidth());
            view.setX(cx - size / 2);
            view.setY(cy - shownHeight * BODY_CENTER);
            final double fontSize = cardHeight * 0.1;
            final Text name = new Text(gameContext.getTranslator().translate("Goose" + COLORS[i]));
            name.setFont(GameStyle.titleFont(fontSize));
            name.setStyle(GameStyle.titleFontStyle(fontSize) + " -fx-fill: " + GameStyle.CARD_TEXT + ";");
            name.setTextAlignment(TextAlignment.CENTER);
            name.setX(cx - name.getLayoutBounds().getWidth() / 2 - name.getLayoutBounds().getMinX());
            name.setY(top + cardHeight * 0.86 - name.getLayoutBounds().getHeight() / 2 - name.getLayoutBounds().getMinY());
            final Group content = new Group(base, view, name);
            content.setMouseTransparent(true);
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
            cardContents.add(content);
            choiceLayer.getChildren().addAll(card, content);
        }
        // The ring along the edge of a card, as in Memory, Magic Cards and the Piano
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

    private static String toWeb(final Color color) {
        return String.format("#%02X%02X%02X", (int) Math.round(color.getRed() * 255),
            (int) Math.round(color.getGreen() * 255), (int) Math.round(color.getBlue() * 255));
    }

    /**
     * The screen "Choisis ta Biboule" for the next person: the colors taken greyed, the board faded behind.
     */
    private void showChoice() {
        phase = Phase.CHOOSE;
        elapsed = 0;
        cardProgress = 0;
        cardRing.setVisible(false);
        board.setOpacity(0.3);
        choiceLayer.setVisible(true);
        for (int i = 0; i < cards.size(); i++) {
            final boolean taken = chosen.contains(i);
            cards.get(i).setOpacity(taken ? 0.35 : 1);
            cardContents.get(i).setOpacity(taken ? 0.35 : 1);
        }
        if (players.againstComputer()) {
            say("GooseChoose");
        } else {
            say("GoosePlayerChooses", chosen.size() + 1);
        }
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
     * The fixation on a card of a color not taken, once the delay of the settings is over (new choices on the screen).
     */
    private void fixateCard(final double seconds) {
        elapsed += seconds;
        if (lookedCard < 0 || chosen.contains(lookedCard) || elapsed < configuration.getGameStartDelay() / 1000.0) {
            cardRing.setVisible(false);
            cardProgress = 0;
            return;
        }
        final Rectangle card = cards.get(lookedCard);
        cardRing.setLayoutX(card.getX());
        cardRing.setLayoutY(card.getY());
        cardRing.setVisible(true);
        cardProgress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        GameStyle.fillRing(cardRing, cardRingPerimeter, Math.min(1, cardProgress));
        if (cardProgress >= 1) {
            chooseColor(lookedCard);
        }
    }

    /**
     * A color chosen by the person choosing now; the next person chooses, or the game starts.
     */
    void chooseColor(final int color) {
        if (phase != Phase.CHOOSE || chosen.contains(color)) {
            return;
        }
        chosen.add(color);
        cardProgress = 0;
        cardRing.setVisible(false);
        lookedCard = -1;
        if (chosen.size() < players.getHumans()) {
            showChoice();
        } else {
            message.getChildren().clear();
            startGame();
        }
    }

    /**
     * @param chooseColors whether the persons choose the colors of their Biboules before the game (point 66), before the
     *                     launch; in the tests, the colors in their order
     */
    void setChooseColors(final boolean chooseColors) {
        this.chooseColors = chooseColors;
    }

    /**
     * @return an image of the game, loaded once: a game started again uses the same ones (the board is about 7.6 MB in
     * memory; the Biboules are loaded at 300 pixels, the most a pawn needs)
     */
    private static Image gameImage(final String path) {
        return GAME_IMAGES.computeIfAbsent(path, key -> key.startsWith(BIBOULES)
            ? new Image(key, 300, 300, true, true) : new Image(key));
    }

    /**
     * @return the two dice, on a dark rounded card in the middle of the screen
     */
    private Group diceGroup() {
        final double side = height * 0.13;
        final Rectangle card = new Rectangle(width / 2 - side * 2, height / 2 - side * 1.05, side * 4, side * 2.1);
        card.setArcWidth(side * 0.6);
        card.setArcHeight(side * 0.6);
        card.setFill(Color.rgb(34, 88, 58, 0.85));
        card.setStroke(Color.rgb(255, 255, 255, 0.6));
        card.setStrokeWidth(Math.max(2, side * 0.03));
        final Group group = new Group(card);
        for (int i = 0; i < 2; i++) {
            final DiceRoller die = new DiceRoller((float) (side / 2), gameContext.getSoundManager(), random);
            // In its own 3D sub-scene: a 3D die could come in front of the menu Esc (tester's feedback, point 58)
            final DiceView view = new DiceView(die, side);
            view.setLayoutX(width / 2 + (i == 0 ? -side * 0.95 : side * 0.95));
            view.setLayoutY(height / 2);
            dice.add(die);
            group.getChildren().add(view);
        }
        group.setMouseTransparent(true);
        return group;
    }

    /**
     * @return the round "Roll" button at the bottom right: an orange disc with a die, its name under it
     */
    private Group rollButton() {
        buttonRadius = height * 0.075;
        buttonX = width - buttonRadius * 1.7;
        buttonY = height - buttonRadius * 2.1;
        final Circle disc = new Circle(buttonX, buttonY, buttonRadius, BUTTON);
        disc.setStroke(Color.rgb(255, 250, 240));
        disc.setStrokeWidth(Math.max(3, buttonRadius * 0.07));
        final double side = buttonRadius;
        final Rectangle face = new Rectangle(buttonX - side / 2, buttonY - side / 2, side, side);
        face.setArcWidth(side * 0.3);
        face.setArcHeight(side * 0.3);
        face.setFill(Color.rgb(251, 250, 246));
        face.setStroke(Color.rgb(222, 217, 206));
        face.setMouseTransparent(true);
        final Group group = new Group(disc, face);
        final double[][] pips = {{-1, -1}, {1, -1}, {0, 0}, {-1, 1}, {1, 1}};
        for (final double[] pip : pips) {
            final Circle dot = new Circle(buttonX + pip[0] * side * 0.27, buttonY + pip[1] * side * 0.27,
                side * 0.1, PIP);
            dot.setMouseTransparent(true);
            group.getChildren().add(dot);
        }
        final Group label = GameStyle.pill(gameContext.getTranslator().translate("GooseRoll"), height * 0.03);
        label.setLayoutX(buttonX - label.getBoundsInLocal().getWidth() / 2);
        label.setLayoutY(buttonY + buttonRadius * 1.1);
        label.setMouseTransparent(true);
        group.getChildren().add(label);
        final EventHandler<Event> handler = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                lookAtButton(true);
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                lookAtButton(false);
            }
        };
        disc.addEventFilter(MouseEvent.ANY, handler);
        disc.addEventFilter(GazeEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(disc);
        return group;
    }

    void lookAtButton(final boolean looking) {
        lookingAtButton = looking;
        progress = 0;
        ring.stop();
        if (looking && phase == Phase.READY && !gameContext.isSelectionBlocked()) {
            showRing();
        }
    }

    private void showRing() {
        ring.setSize(buttonRadius * 2.5);
        ring.show(buttonX, buttonY);
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        switch (phase) {
            case CHOOSE -> fixateCard(seconds);
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    // The delay of the start is over: no other one for this first turn
                    startTurn(false);
                }
            }
            case READY -> fixate(seconds);
            case ROLLING -> {
                elapsed += seconds;
                if (elapsed >= ROLL) {
                    move(lastThrow);
                }
            }
            case MOVING -> hop(seconds);
            case PAUSE, SHOW -> {
                pauseLeft -= seconds;
                if (pauseLeft <= 0) {
                    final Runnable next = afterPause;
                    afterPause = null;
                    if (phase == Phase.SHOW) {
                        finish();
                    } else if (next != null) {
                        next.run();
                    }
                }
            }
            default -> {
            }
        }
        // After the move of this step, so that the halo is on the pawn
        if (phase != Phase.OVER) {
            pulse(seconds);
        }
    }

    private void fixate(final double seconds) {
        if (!lookingAtButton) {
            return;
        }
        if (gameContext.isSelectionBlocked()) {
            // During the delay after the button came back: the fixation starts once it is over
            progress = 0;
            ring.stop();
            return;
        }
        if (!ring.isVisible()) {
            showRing();
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            progress = 0;
            ring.stop();
            stats.incrementNumberOfGoalsReached();
            roll();
        }
    }

    /**
     * The halo on the pawn whose turn it is, following it when it moves, and the halo of the "Roll" button, both
     * pulsing; the button grows a little with its halo (point 64).
     */
    private void pulse(final double seconds) {
        pulseTime += seconds;
        if (pawns.isEmpty()) {
            return;
        }
        final double wave = (1 - Math.cos(2 * Math.PI * pulseTime / PULSE)) / 2;
        final Pawn pawn = pawns.get(current);
        halo.setVisible(phase != Phase.SHOW);
        halo.setCenterX(pawn.node.getLayoutX());
        halo.setCenterY(pawn.node.getLayoutY());
        halo.setScaleX(1 + PULSE_GROWTH * wave);
        halo.setScaleY(1 + PULSE_GROWTH * wave);
        halo.setOpacity(1 - 0.4 * wave);
        buttonHalo.setScaleX(1 + PULSE_GROWTH * wave);
        buttonHalo.setScaleY(1 + PULSE_GROWTH * wave);
        buttonHalo.setOpacity(1 - 0.4 * wave);
        final double grow = 1 + 0.05 * wave;
        rollButton.setScaleX(grow);
        rollButton.setScaleY(grow);
    }

    /**
     * For the tests: the halo of the pawn whose turn it is.
     */
    Circle getHalo() {
        return halo;
    }

    /**
     * For the tests: the halo of the "Roll" button.
     */
    Circle getButtonHalo() {
        return buttonHalo;
    }

    /**
     * For the tests: the size of a pawn (its Biboule) and of a square of the board, in pixels.
     */
    double getPawnSize() {
        return pawnSize;
    }

    /**
     * Waits, then goes on.
     */
    private void after(final double seconds, final Runnable next) {
        phase = Phase.PAUSE;
        pauseLeft = seconds;
        afterPause = next;
    }

    /**
     * The turn of the current pawn: a pawn caught by a trap misses its turn; a person rolls with the button, the
     * computer by itself.
     *
     * @param delayed whether the "Roll" button comes back after another turn, maybe under the pointer: nothing can be
     *                chosen during the delay of the settings (tester's feedback of 20/11/2026, point 22; the first
     *                turn follows the wait of the start)
     */
    private void startTurn(final boolean delayed) {
        diceGroup.setVisible(false);
        flatDice.setVisible(false);
        final Pawn pawn = pawns.get(current);
        showTurn(pawn);
        if (pawn.asleep > 0) {
            pawn.asleep--;
            say("GooseSleeps", pawn);
            after(1.4, this::nextTurn);
            return;
        }
        if (pawn.human) {
            if (players.againstComputer()) {
                say("GooseYourTurn");
            } else {
                say("GooseTurn", pawn);
            }
            stats.incrementNumberOfGoalsToReach();
            rollButton.setVisible(true);
            phase = Phase.READY;
            progress = 0;
            if (delayed) {
                gameContext.onGameStarted();
            }
            if (lookingAtButton && !gameContext.isSelectionBlocked()) {
                showRing();
            }
        } else {
            say("GooseComputerTurn", pawn);
            after(COMPUTER_WAIT, this::roll);
        }
    }

    private void nextTurn() {
        current = (current + 1) % pawns.size();
        startTurn(true);
    }

    /**
     * The two dice roll, showing the throw chosen to help a person when there is one; the pawn moves when they have
     * settled.
     */
    private void roll() {
        final int helped = helpedThrow(pawns.get(current));
        final int first = helped == 0 ? 0 : Math.min(6, helped - 1);
        final int[] forced = {first, helped - first};
        for (int i = 0; i < dice.size(); i++) {
            faces[i] = dice.get(i).roll(null, helped == 0 ? 0 : forced[i]);
        }
        startRolling(faces[0] + faces[1]);
    }

    /**
     * @return the throw (2 to 12) helping a person, or 0 for a throw at random: the end on the third throw that can
     * reach it exactly; against the computer, when one of its pawns is ahead, the end if it can be reached, else the
     * farthest goose or the bridge, else 12 or 11 when it does not stop on a trap
     */
    int helpedThrow(final Pawn pawn) {
        if (!pawn.human) {
            return 0;
        }
        final int toEnd = GooseBoard.END - pawn.square;
        final boolean endInReach = toEnd >= 2 && toEnd <= 12;
        if (endInReach) {
            pawn.endTries++;
            if (pawn.endTries >= END_HELP) {
                return toEnd;
            }
        } else {
            pawn.endTries = 0;
        }
        final boolean computerAhead = players.againstComputer()
            && pawns.stream().anyMatch(other -> !other.human && other.square > pawn.square);
        if (!computerAhead) {
            return 0;
        }
        if (endInReach) {
            return toEnd;
        }
        for (int total = 12; total >= 2; total--) {
            final GooseBoard.Kind kind = GooseBoard.kind(landing(pawn.square, total), rules);
            if (kind == GooseBoard.Kind.GOOSE || kind == GooseBoard.Kind.BRIDGE) {
                return total;
            }
        }
        for (int total = 12; total >= 11; total--) {
            if (!isTrap(GooseBoard.kind(landing(pawn.square, total), rules))) {
                return total;
            }
        }
        return 0;
    }

    private static int landing(final int from, final int total) {
        final List<Integer> way = GooseBoard.way(from, total);
        return way.get(way.size() - 1);
    }

    private static boolean isTrap(final GooseBoard.Kind kind) {
        return kind == GooseBoard.Kind.INN || kind == GooseBoard.Kind.WELL || kind == GooseBoard.Kind.PRISON;
    }

    /**
     * For the tests: a throw of a known value.
     */
    void rollWith(final int total) {
        faces[0] = Math.min(6, total - 1);
        faces[1] = total - faces[0];
        startRolling(total);
    }

    private void startRolling(final int total) {
        lastThrow = total;
        rollButton.setVisible(false);
        ring.stop();
        diceGroup.setVisible(true);
        phase = Phase.ROLLING;
        elapsed = 0;
    }

    private void move(final int steps) {
        final Pawn pawn = pawns.get(current);
        say("GooseMoves", pawn, steps);
        showFlatDice(pawn);
        moveAlong(pawn, GooseBoard.way(pawn.square, steps), HOP, this::land);
    }

    /**
     * The dice of the throw drawn flat at the bottom right, where the "Roll" button was, their pips empty: one fills with
     * the color of the pawn at each square it moves by the throw (point 66).
     */
    private void showFlatDice(final Pawn pawn) {
        diceGroup.setVisible(false);
        flatDice.getChildren().clear();
        pips.clear();
        pipColor = RINGS[pawn.colorIndex];
        final double side = buttonRadius * 1.15;
        final double gap = side * 0.22;
        final double cardWidth = 2 * side + 3 * gap;
        final double cardHeight = side + 2 * gap;
        final double cardX = Math.min(width - cardWidth - height * 0.01, buttonX - cardWidth / 2);
        final double cardY = buttonY - cardHeight / 2;
        final Rectangle card = new Rectangle(cardX, cardY, cardWidth, cardHeight);
        card.setArcWidth(side * 0.5);
        card.setArcHeight(side * 0.5);
        card.setFill(Color.rgb(34, 88, 58, 0.88));
        card.setStroke(Color.rgb(255, 255, 255, 0.65));
        card.setStrokeWidth(Math.max(2, side * 0.03));
        flatDice.getChildren().add(card);
        for (int d = 0; d < 2; d++) {
            final double x = cardX + gap + d * (side + gap);
            final double y = cardY + gap;
            final Rectangle face = new Rectangle(x, y, side, side);
            face.setArcWidth(side * 0.3);
            face.setArcHeight(side * 0.3);
            face.setFill(Color.rgb(251, 250, 246));
            face.setStroke(Color.rgb(222, 217, 206));
            flatDice.getChildren().add(face);
            for (final double[] place : PIPS[Math.max(1, Math.min(6, faces[d])) - 1]) {
                final Circle pip = new Circle(x + side / 2 + place[0] * side * 0.27, y + side / 2 + place[1] * side * 0.27,
                    side * 0.1, PIP);
                pip.setStroke(PIP);
                pip.setStrokeWidth(Math.max(1.5, side * 0.03));
                pips.add(pip);
                flatDice.getChildren().add(pip);
            }
        }
        squaresCount = new Group();
        flatDice.getChildren().add(squaresCount);
        flatDice.setVisible(true);
        startCounting();
    }

    /**
     * The pips all empty, counting again (a goose replays the throw).
     */
    private void startCounting() {
        pipsFilled = 0;
        countingPips = true;
        for (final Circle pip : pips) {
            pip.setFill(PIP);
            pip.setStroke(PIP);
        }
        showSquaresCount();
    }

    private void fillPip() {
        if (!countingPips || pipsFilled >= pips.size()) {
            return;
        }
        final Circle pip = pips.get(pipsFilled);
        pip.setFill(pipColor);
        pip.setStroke(Color.WHITE);
        pipsFilled++;
        showSquaresCount();
    }

    private void showSquaresCount() {
        if (squaresCount == null) {
            return;
        }
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("GooseSquaresDone")
            .replace("{0}", String.valueOf(pipsFilled)).replace("{1}", String.valueOf(pips.size())), height * 0.026);
        final javafx.geometry.Bounds card = flatDice.getChildren().get(0).getBoundsInLocal();
        pill.setLayoutX(card.getMinX() + (card.getWidth() - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(card.getMaxY() + height * 0.008);
        squaresCount.getChildren().setAll(pill);
    }

    /**
     * For the tests: the pips of the flat dice, filled or not.
     */
    List<Circle> getPips() {
        return pips;
    }

    /**
     * For the tests: whether the flat dice are shown.
     */
    boolean isFlatDiceShown() {
        return flatDice.isVisible();
    }

    /**
     * Moves a pawn through squares, one hop each, then goes on.
     */
    private void moveAlong(final Pawn pawn, final List<Integer> squares, final double length, final Runnable then) {
        mover = pawn;
        hops.clear();
        hops.addAll(squares);
        hopLength = length;
        hopTime = 0;
        hopFrom = pointOf(pawn);
        afterMove = then;
        phase = Phase.MOVING;
        if (hops.isEmpty()) {
            arrive();
        }
    }

    private void hop(final double seconds) {
        hopTime += seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        final double t = Math.min(1, hopTime / hopLength);
        final Point2D to = centers.get(hops.get(0));
        final double x = hopFrom.getX() + (to.getX() - hopFrom.getX()) * t;
        final double y = hopFrom.getY() + (to.getY() - hopFrom.getY()) * t - Math.sin(Math.PI * t) * pawnSize * 0.5;
        mover.node.setLayoutX(x);
        mover.node.setLayoutY(y);
        if (t >= 1) {
            mover.square = hops.remove(0);
            gameContext.getSoundManager().add(SOUNDS + "mvmt" + random.nextInt(6) + ".wav");
            fillPip();
            hopFrom = to;
            hopTime = 0;
            if (hops.isEmpty()) {
                arrive();
            }
        }
    }

    private void arrive() {
        mover = null;
        placePawns();
        final Runnable then = afterMove;
        afterMove = null;
        if (then != null) {
            then.run();
        }
    }

    /**
     * What the square the pawn stops on does.
     */
    private void land() {
        final Pawn pawn = pawns.get(current);
        switch (GooseBoard.kind(pawn.square, rules)) {
            case END -> win(pawn);
            case GOOSE -> {
                say("GooseGoose", pawn, lastThrow);
                gameContext.getSoundManager().add(SOUNDS + "goose.wav");
                // The same throw again: the pips count again
                after(1.0, () -> {
                    startCounting();
                    moveAlong(pawn, GooseBoard.way(pawn.square, lastThrow), HOP, this::land);
                });
            }
            case BRIDGE -> {
                countingPips = false;
                say("GooseBridge", pawn);
                after(0.9, () -> moveAlong(pawn, List.of(GooseBoard.BRIDGE_END), JUMP,
                    () -> after(0.6, this::nextTurn)));
            }
            case INN, WELL, PRISON -> {
                // One turn missed (point 65)
                pawn.asleep = GooseBoard.TRAP_TURNS;
                final GooseBoard.Kind kind = GooseBoard.kind(pawn.square, rules);
                say(kind == GooseBoard.Kind.INN ? "GooseInn" : kind == GooseBoard.Kind.WELL ? "GooseWell"
                    : "GoosePrison", pawn);
                gameContext.getSoundManager().add(SOUNDS + "trap.wav");
                after(1.6, this::nextTurn);
            }
            default -> after(0.5, this::nextTurn);
        }
    }

    private void win(final Pawn pawn) {
        winner = pawn;
        if (!players.againstComputer()) {
            say("GooseWins", pawn);
        } else {
            say(pawn.human ? "GooseYouWin" : "GooseComputerWins");
        }
        gameContext.getSoundManager().add(SOUNDS + "win.wav");
        phase = Phase.SHOW;
        pauseLeft = SHOW;
    }

    private void finish() {
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        if (winner != null && winner.human) {
            gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
        } else {
            gameContext.showRoundStats(stats, this);
        }
    }

    /**
     * Shows a message at the top, the names of the pawns ({0}, {1}) and a number ({1}) in it.
     */
    private void say(final String key, final Object... values) {
        String text = gameContext.getTranslator().translate(key);
        for (int i = 0; i < values.length; i++) {
            final Object value = values[i];
            final String shown = value instanceof Pawn pawn ? nameOf(pawn) : String.valueOf(value);
            text = text.replace("{" + i + "}", shown);
        }
        lastMessage = text;
        final Group pill = GameStyle.pill(text, height * 0.042);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(height * 0.025);
        message.getChildren().setAll(pill);
    }

    String nameOf(final Pawn pawn) {
        return gameContext.getTranslator().translate("Goose" + pawn.color);
    }

    /**
     * The pawn whose turn it is, at the top left: its Biboule and its name ("You" against the computer).
     */
    private void showTurn(final Pawn pawn) {
        hud.getChildren().clear();
        // The pawn whose turn it is in front of the others
        pawn.node.toFront();
        final double size = height * 0.085;
        final ImageView view = new ImageView(pawn.image);
        view.setPreserveRatio(true);
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setLayoutX(height * 0.025);
        view.setLayoutY(height * 0.02);
        final String name = players.againstComputer() && pawn.human
            ? gameContext.getTranslator().translate("GooseYou") : nameOf(pawn);
        final Group pill = GameStyle.pill(name, height * 0.032);
        pill.setLayoutX(height * 0.025 + size * 1.1);
        pill.setLayoutY(height * 0.02 + (size - pill.getBoundsInLocal().getHeight()) / 2);
        hud.getChildren().addAll(view, pill);
    }

    /**
     * Puts the pawns on their squares; pawns sharing a square are smaller and around its center.
     */
    private void placePawns() {
        for (final Pawn pawn : pawns) {
            if (pawn == mover) {
                continue;
            }
            final Point2D point = pointOf(pawn);
            final List<Pawn> together = pawns.stream().filter(other -> other != mover && other.square == pawn.square)
                .toList();
            final double scale = together.size() > 1 ? 0.72 : 1;
            pawn.node.setScaleX(scale);
            pawn.node.setScaleY(scale);
            pawn.node.setLayoutX(point.getX());
            pawn.node.setLayoutY(point.getY());
        }
    }

    /**
     * @return where a pawn stands on its square: the center, or around it when the square is shared
     */
    private Point2D pointOf(final Pawn pawn) {
        final Point2D center = centers.get(pawn.square);
        final List<Pawn> together = pawns.stream().filter(other -> other != mover && other.square == pawn.square)
            .toList();
        if (together.size() < 2 || !together.contains(pawn)) {
            return center;
        }
        final double angle = 2 * Math.PI * together.indexOf(pawn) / together.size() - Math.PI / 2;
        return center.add(Math.cos(angle) * pawnSize * 0.3, Math.sin(angle) * pawnSize * 0.3);
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    /**
     * For the tests: puts a pawn on a square.
     */
    void put(final int index, final int square) {
        pawns.get(index).square = square;
        placePawns();
    }

    Group getRollButton() {
        return rollButton;
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
        if (rollButton != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(rollButton.getChildren().get(0));
        }
        for (final Rectangle card : cards) {
            gameContext.getGazeDeviceManager().removeEventFilter(card);
        }
        cards.clear();
        cardContents.clear();
        lookedCard = -1;
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A pawn: its Biboule on a base, its square, the turns it misses, and the throws it has had that could reach the end
     * exactly.
     */
    static final class Pawn {
        @Getter
        private final int index;
        private final String color;
        @Getter
        private final boolean human;
        private final Group node;
        private final Image image;
        @Getter
        private int square = GooseBoard.START;
        @Getter
        private int asleep;
        @Getter
        private int endTries;

        /**
         * The place of its color in {@link #COLORS}.
         */
        @Getter
        private final int colorIndex;

        Pawn(final int index, final String color, final boolean human, final Group node, final Image image,
             final int colorIndex) {
            this.index = index;
            this.color = color;
            this.colorIndex = colorIndex;
            this.human = human;
            this.node = node;
            this.image = image;
        }

        /**
         * For the tests: puts the pawn on a square.
         */
        void setSquare(final int square) {
            this.square = square;
        }

        /**
         * @return its base and its Biboule, centered on (0, 0)
         */
        Group getNode() {
            return node;
        }
    }

    /**
     * @return the settings of the menu (Esc) acting on this game (17/11/2026): the others are greyed
     */
    @Override
    public java.util.Set<net.gazeplay.MenuSetting> menuSettings() {
        return net.gazeplay.MenuSetting.fixationWithSharedRing(net.gazeplay.MenuSetting.ANIMATION_SPEED);
    }
}
