package net.gazeplay.games.magicpotions;

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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;
import net.gazeplay.components.RingThickness;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * "Recettes magiques" (name code Potions; until 24/11/2026 "Magic Potions", a game of colors to mix, the same as
 * "Mélanger les couleurs": maintainer's decisions after the tester's feedback, point 82). A Biboule shows a recipe in
 * its bubble: 2, 3 or 4 ingredients in pictures (main choice of the variants, {@link RecipeSize}). The shelf shows them
 * and 2 others, in any order. A fixation on an ingredient of the recipe makes it fly into the cauldron, and it is ticked
 * in the bubble; the order does not count. An ingredient out of the recipe shakes and stays on the shelf, greyed: a
 * mistake is counted and, with the hearts, a life is lost (no explosion: point 20, a reaction too spectacular gives a
 * wish to fail on purpose). The recipe complete, the cauldron explodes (the ingredients merge into a potion), then the
 * Biboule drinks the potion and jumps for joy. The game ends after 3, 5 or 10 recipes ("Bravo"), never, or when the
 * lives are lost ("Lost!"); without hearts, it is never lost. No voice (maintainer's decision).
 * <p>
 * One loop, in real seconds: the ingredients fly at the speed of the settings, the fixations last the length of the
 * settings (read at each step), nothing can be chosen during the delay of the settings at the start of each recipe nor
 * while something moves, and nothing moves while the menu of the game is open.
 */
public class MagicPotions implements GameLifeCycle {

    static final int LIVES = 3;
    /**
     * In seconds (at the speed of the settings): an ingredient flying into the cauldron, an ingredient out of the
     * recipe shaking, the explosion of the recipe complete, the potion going to the Biboule, the Biboule jumping, and
     * the end shown (in real seconds).
     */
    static final double FLY = 0.9;
    static final double SHAKE = 0.6;
    static final double FUSION = 1.4;
    static final double DRINK = 1.0;
    static final double SERVED = 1.2;
    static final double SHOW = 2.2;

    private static final String IMAGES = "data/potions/images/";
    private static final String SOUNDS = "data/potions/sounds/";
    private static final String WRONG_SOUND = "data/dice/sounds/wrong.wav";
    private static final Color EMPTY = Color.rgb(226, 236, 246);
    private static final Color TICK = Color.rgb(46, 160, 67);
    /**
     * The potions made: their picture and the color of the cauldron.
     */
    private static final String[] POTIONS = {"potionRed.png", "potionYellow.png", "potionBlue.png"};
    private static final Color[] POTION_COLORS = {Color.rgb(229, 57, 53), Color.rgb(253, 216, 53),
        Color.rgb(30, 136, 229)};

    enum Phase { PLAY, FLY, WRONG, FUSION, DRINK, SERVED, SHOW, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final RecipeSize size;
    @Getter
    private final PotionsEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    /**
     * The ingredients asked for, in the order of the bubble.
     */
    @Getter
    private final List<Ingredient> recipe = new ArrayList<>();
    /**
     * The ingredients of the shelf, from left to right.
     */
    @Getter
    private final List<Ingredient> shelf = new ArrayList<>();
    /**
     * The ingredients of the recipe already in the cauldron.
     */
    @Getter
    private final Set<Ingredient> added = EnumSet.noneOf(Ingredient.class);
    /**
     * The ingredients out of the recipe already chosen for this recipe (greyed, no longer chosen).
     */
    private final Set<Ingredient> refused = EnumSet.noneOf(Ingredient.class);
    @Getter
    private int lives;
    /**
     * Whether the user chose the hearts (see {@code LivesVariant}); without them, no hearts shown and the game is never
     * lost; the ingredients out of the recipe are still counted as mistakes.
     */
    @Getter
    private boolean withLives = true;
    @Getter
    private int served;
    private boolean bravo;
    private double elapsed;
    /**
     * The ingredient flying into the cauldron, or shaking, and the slot of the shelf it comes from.
     */
    private Ingredient moving;
    private int movingSlot = -1;
    private int potion;

    private double width;
    private double height;
    private double cardSize;
    private double potSize;
    private double potX;
    private double potY;
    private double clientScale;
    private double clientX;
    private double clientY;

    private Group layer;
    private final List<Group> cards = new ArrayList<>();
    private final List<ImageView> cardPictures = new ArrayList<>();
    private final List<double[]> slots = new ArrayList<>();
    private final Map<Ingredient, Image> pictures = new EnumMap<>(Ingredient.class);
    private final Map<Ingredient, Group> ticks = new EnumMap<>(Ingredient.class);
    private Ellipse potColor;
    private ImageView client;
    private Group bubble;
    private ImageView flying;
    private ImageView flask;
    private ImageView explosion;
    private Rectangle ring;
    private double ringPerimeter;
    private Group hud;
    private Group message;
    private final List<Node> gazeNodes = new ArrayList<>();

    private int looked = -1;
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
    MagicPotions(final IGameContext gameContext, final Stats stats, final RecipeSize size, final PotionsEnd end,
                 final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.size = size;
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
        // The game counts the mistakes: 0 until the first one
        stats.countMistakes();
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();

        final Node background;
        if (configuration.isBackgroundEnabled()) {
            final ImageView picture = new ImageView(new Image(IMAGES + "background-potions.jpg"));
            picture.setFitWidth(width);
            picture.setFitHeight(height);
            background = picture;
        } else {
            background = new Rectangle(0, 0, width, height);
            ((Rectangle) background).setFill(Color.rgb(70, 54, 110));
        }
        background.setMouseTransparent(true);
        for (final Ingredient ingredient : Ingredient.values()) {
            pictures.put(ingredient, new Image(ingredient.picture()));
        }

        // The cauldron at the left, at the bottom
        potSize = height * 0.32;
        final double potScale = potSize / 136;
        potX = width * 0.22 - 75 * potScale;
        potY = height * 0.95 - potSize;
        // Inside the opening of the cauldron, not above its rim
        potColor = new Ellipse(potX + 75 * potScale, potY + 47 * potScale, 60 * potScale, 26 * potScale);
        potColor.setFill(EMPTY);
        final ImageView pot = new ImageView(new Image(IMAGES + "mixingPot.png"));
        pot.setFitWidth(150 * potScale);
        pot.setFitHeight(potSize);
        pot.setX(potX);
        pot.setY(potY);
        pot.setMouseTransparent(true);

        // The shelf at the right, at the bottom: white cards on a plank (point 29: pictures on white cards)
        final int count = size.getShelf();
        final double left = width * 0.4;
        final double right = width * 0.98;
        final double slot = (right - left) / count;
        cardSize = Math.min(height * 0.2, slot * 0.84);
        final double cardsY = height * 0.78;
        final Rectangle plank = new Rectangle(left - width * 0.01, cardsY + cardSize / 2 + height * 0.012,
            right - left + width * 0.02, height * 0.028);
        plank.setArcWidth(height * 0.012);
        plank.setArcHeight(height * 0.012);
        plank.setFill(Color.rgb(139, 94, 60));
        plank.setStroke(Color.rgb(34, 30, 38));
        plank.setStrokeWidth(2);
        plank.setMouseTransparent(true);
        final Group shelfLayer = new Group(plank);
        for (int i = 0; i < count; i++) {
            final double x = left + slot * (i + 0.5);
            slots.add(new double[]{x, cardsY});
            final Rectangle surface = GameStyle.pictureCard(x - cardSize / 2, cardsY - cardSize / 2, cardSize,
                cardSize);
            final ImageView view = new ImageView();
            view.setFitWidth(cardSize * 0.78);
            view.setFitHeight(cardSize * 0.78);
            view.setX(x - cardSize * 0.39);
            view.setY(cardsY - cardSize * 0.39);
            view.setMouseTransparent(true);
            final Group card = new Group(surface, view);
            cards.add(card);
            cardPictures.add(view);
            shelfLayer.getChildren().add(card);
            final int index = i;
            listen(card, () -> lookAt(index), () -> lookAway(index));
        }

        // The client at the top right, its bubble showing the recipe
        clientScale = Math.min(width * 0.42 / 670, height * 0.42 / 422);
        clientX = width * 0.5 + (width * 0.5 - 670 * clientScale) / 2;
        clientY = height * 0.06;
        client = new ImageView(new Image(IMAGES + "Biboule-Client.png"));
        client.setFitWidth(670 * clientScale);
        client.setFitHeight(422 * clientScale);
        client.setX(clientX);
        client.setY(clientY);
        client.setMouseTransparent(true);
        bubble = new Group();
        bubble.setMouseTransparent(true);

        flying = new ImageView();
        flying.setVisible(false);
        flying.setMouseTransparent(true);
        flask = new ImageView();
        flask.setVisible(false);
        flask.setMouseTransparent(true);
        explosion = new ImageView(new Image("data/common/images/explosion.gif"));
        // An animated picture of 700 x 316, growing from its middle
        explosion.setPreserveRatio(true);
        explosion.setFitWidth(potSize * 2.6);
        explosion.setX(potX + 75 * potScale - potSize * 1.3);
        explosion.setY(potY + 45 * potScale - potSize * 2.6 * 316 / 700 / 2);
        explosion.setVisible(false);
        explosion.setMouseTransparent(true);
        ring = newRing();
        ringPerimeter = GameStyle.clockwiseRing(ring);
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        layer = new Group(background, client, bubble, potColor, pot, shelfLayer, flying, flask, explosion, ring, hud,
            message);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        lives = LIVES;
        served = 0;
        elapsed = 0;
        newRecipe();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * The ring of a fixation, along the edge of the card looked at, as in Memory and Magic Cards (maintainer's request of
     * 24/11/2026: the ingredients are cards), centered on (0, 0).
     */
    private Rectangle newRing() {
        final double corner = GameStyle.cardRadius(cardSize, cardSize);
        final Rectangle created = new Rectangle(-cardSize / 2, -cardSize / 2, cardSize, cardSize);
        created.setArcWidth(2 * corner);
        created.setArcHeight(2 * corner);
        created.setFill(null);
        created.setStrokeType(StrokeType.INSIDE);
        created.setStrokeLineCap(StrokeLineCap.BUTT);
        RingThickness.apply(created, Math.max(5, cardSize * 0.045));
        created.setStyle("-fx-stroke: -gp-ring;");
        created.setMouseTransparent(true);
        created.setVisible(false);
        return created;
    }

    /**
     * Makes a node receive the gaze and the mouse.
     */
    private void listen(final Node node, final Runnable enter, final Runnable exit) {
        final EventHandler<Event> handler = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                enter.run();
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                exit.run();
            }
        };
        node.addEventFilter(MouseEvent.ANY, handler);
        node.addEventFilter(GazeEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(node);
        gazeNodes.add(node);
    }

    /**
     * A new client and its recipe, other ingredients than the last one, and the shelf: nothing can be chosen during the
     * delay of the settings (point 22, at the start of each recipe, the first one included, and nowhere else).
     */
    private void newRecipe() {
        gameContext.onGameStarted();
        final Set<Ingredient> last = new HashSet<>(recipe);
        do {
            recipe.clear();
            final List<Ingredient> all = new ArrayList<>(List.of(Ingredient.values()));
            for (int i = 0; i < size.getIngredients(); i++) {
                recipe.add(all.remove(random.nextInt(all.size())));
            }
        } while (new HashSet<>(recipe).equals(last));
        final List<Ingredient> others = new ArrayList<>(List.of(Ingredient.values()));
        others.removeAll(recipe);
        final List<Ingredient> onShelf = new ArrayList<>(recipe);
        while (onShelf.size() < size.getShelf()) {
            onShelf.add(others.remove(random.nextInt(others.size())));
        }
        shelf.clear();
        while (!onShelf.isEmpty()) {
            shelf.add(onShelf.remove(random.nextInt(onShelf.size())));
        }
        for (int i = 0; i < shelf.size(); i++) {
            cardPictures.get(i).setImage(pictures.get(shelf.get(i)));
            cardPictures.get(i).setVisible(true);
            cards.get(i).setOpacity(1);
            cards.get(i).setTranslateX(0);
        }
        added.clear();
        refused.clear();
        moving = null;
        movingSlot = -1;
        potColor.setFill(EMPTY);
        potColor.setScaleX(1);
        potColor.setScaleY(1);
        explosion.setVisible(false);
        flying.setVisible(false);
        flask.setVisible(false);
        client.setTranslateY(0);
        bubble.setTranslateY(0);
        message.getChildren().clear();
        showRecipe();
        looked = -1;
        progress = 0;
        ring.setVisible(false);
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        phase = Phase.PLAY;
        elapsed = 0;
        showHud();
    }

    /**
     * In the bubble (the ellipse (244, 8) - (666, 236) of the picture): the ingredients of the recipe in a row, each
     * with a green tick, hidden until it is in the cauldron.
     */
    private void showRecipe() {
        bubble.getChildren().clear();
        ticks.clear();
        final double centerX = clientX + 455 * clientScale;
        final double centerY = clientY + 122 * clientScale;
        final int n = recipe.size();
        final double slot = Math.min(422 * clientScale * 0.8 / n, 228 * clientScale * 0.6);
        final double icon = slot * 0.84;
        for (int i = 0; i < n; i++) {
            final Ingredient ingredient = recipe.get(i);
            final double x = centerX + (i - (n - 1) / 2.0) * slot;
            final ImageView view = new ImageView(pictures.get(ingredient));
            view.setFitWidth(icon);
            view.setFitHeight(icon);
            view.setX(x - icon / 2);
            view.setY(centerY - icon / 2);
            final double r = icon * 0.2;
            final Circle disc = new Circle(x + icon * 0.36, centerY + icon * 0.36, r, TICK);
            disc.setStroke(Color.WHITE);
            disc.setStrokeWidth(Math.max(2, r * 0.18));
            final Polyline check = new Polyline(x + icon * 0.36 - r * 0.5, centerY + icon * 0.36,
                x + icon * 0.36 - r * 0.12, centerY + icon * 0.36 + r * 0.4,
                x + icon * 0.36 + r * 0.52, centerY + icon * 0.36 - r * 0.38);
            check.setStroke(Color.WHITE);
            check.setStrokeWidth(Math.max(2, r * 0.32));
            check.setStrokeLineCap(StrokeLineCap.ROUND);
            check.setStrokeLineJoin(StrokeLineJoin.ROUND);
            final Group tick = new Group(disc, check);
            tick.setVisible(false);
            ticks.put(ingredient, tick);
            bubble.getChildren().addAll(view, tick);
        }
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        final double scaled = seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        switch (phase) {
            case PLAY -> fixate(seconds);
            case FLY -> fly(scaled);
            case WRONG -> shake(scaled);
            case FUSION -> {
                elapsed += scaled;
                if (elapsed >= FUSION) {
                    explosion.setVisible(false);
                    startDrink();
                }
            }
            case DRINK -> drink(scaled);
            case SERVED -> {
                elapsed += scaled;
                // The client jumps for joy, its potion drunk
                final double jump = Math.abs(Math.sin(elapsed * Math.PI * 2.5)) * height * 0.03;
                client.setTranslateY(-jump);
                bubble.setTranslateY(-jump);
                if (elapsed >= SERVED) {
                    afterClient();
                }
            }
            case SHOW -> {
                elapsed += seconds;
                if (elapsed >= SHOW) {
                    finish();
                }
            }
            default -> {
            }
        }
    }

    /**
     * The ingredient flies from the shelf into the cauldron, in an arc, getting smaller, and disappears in it.
     */
    private void fly(final double seconds) {
        elapsed += seconds;
        final double t = Math.min(1, elapsed / FLY);
        final double eased = t * t * (3 - 2 * t);
        final double[] from = slots.get(movingSlot);
        final double toX = potColor.getCenterX();
        final double toY = potColor.getCenterY();
        final double x = from[0] + (toX - from[0]) * eased;
        final double y = from[1] + (toY - from[1]) * eased - Math.sin(Math.PI * eased) * height * 0.25;
        final double shown = cardSize * 0.78 * (1 - 0.5 * eased);
        flying.setFitWidth(shown);
        flying.setFitHeight(shown);
        flying.setX(x - shown / 2);
        flying.setY(y - shown / 2);
        flying.setOpacity(t < 0.8 ? 1 : (1 - t) / 0.2);
        if (t >= 1) {
            flying.setVisible(false);
            added.add(moving);
            ticks.get(moving).setVisible(true);
            gameContext.getSoundManager().add(SOUNDS + "pour.wav");
            moving = null;
            movingSlot = -1;
            elapsed = 0;
            if (added.size() == recipe.size()) {
                // The recipe complete: the ingredients merge into a potion
                potion = random.nextInt(POTIONS.length);
                potColor.setFill(POTION_COLORS[potion]);
                explosion.setVisible(true);
                gameContext.getSoundManager().add(SOUNDS + "boom.wav");
                phase = Phase.FUSION;
            } else {
                // The surface of the cauldron moves a little
                potColor.setFill(EMPTY.interpolate(Color.rgb(180, 150, 230), (double) added.size() / recipe.size()));
                phase = Phase.PLAY;
            }
        }
    }

    /**
     * An ingredient out of the recipe shakes on the shelf, then stays there greyed.
     */
    private void shake(final double seconds) {
        elapsed += seconds;
        final Group card = cards.get(movingSlot);
        if (elapsed < SHAKE) {
            card.setTranslateX(Math.sin(elapsed / SHAKE * Math.PI * 6) * cardSize * 0.08 * (1 - elapsed / SHAKE));
            return;
        }
        card.setTranslateX(0);
        card.setOpacity(0.35);
        moving = null;
        movingSlot = -1;
        elapsed = 0;
        if (lives <= 0) {
            showLost();
        } else {
            phase = Phase.PLAY;
        }
    }

    private void startDrink() {
        flask.setImage(new Image(IMAGES + POTIONS[potion]));
        phase = Phase.DRINK;
        elapsed = 0;
        drink(0);
    }

    /**
     * The potion made goes from the cauldron to the mouth of the Biboule, getting smaller: it drinks it.
     */
    private void drink(final double seconds) {
        elapsed += seconds;
        final double t = Math.min(1, elapsed / DRINK);
        final double eased = t * t * (3 - 2 * t);
        final double fromX = potColor.getCenterX();
        final double fromY = potColor.getCenterY() - potSize * 0.45;
        // The mouth of the Biboule, drawn at (24, 392 - its height) and 300 wide in the picture
        final double toX = clientX + 174 * clientScale;
        final double toY = clientY + 345 * clientScale;
        final double h = potSize * 0.7 * (1 - 0.6 * eased);
        flask.setFitHeight(h);
        flask.setFitWidth(h / 2);
        flask.setX(fromX + (toX - fromX) * eased - h / 4);
        flask.setY(fromY + (toY - fromY) * eased - Math.sin(Math.PI * eased) * height * 0.12 - h / 2);
        flask.setOpacity(t < 0.85 ? 1 : (1 - t) / 0.15);
        flask.setVisible(true);
        if (t >= 1) {
            flask.setVisible(false);
            potColor.setFill(EMPTY);
            served++;
            stats.incrementNumberOfGoalsReached();
            gameContext.getSoundManager().add(SOUNDS + "served.wav");
            phase = Phase.SERVED;
            elapsed = 0;
            showHud();
        }
    }

    private void afterClient() {
        client.setTranslateY(0);
        bubble.setTranslateY(0);
        if (end.getServed() > 0 && served >= end.getServed()) {
            bravo = true;
            finish();
            return;
        }
        newRecipe();
    }

    private void showLost() {
        bravo = false;
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("PotionsLost"), height * 0.05);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(height * 0.45 - pill.getBoundsInLocal().getHeight() / 2);
        message.getChildren().setAll(pill);
        phase = Phase.SHOW;
        elapsed = 0;
    }

    private boolean choosable(final int index) {
        final Ingredient ingredient = shelf.get(index);
        return !added.contains(ingredient) && !refused.contains(ingredient);
    }

    private void fixate(final double seconds) {
        if (looked < 0 || !choosable(looked)) {
            return;
        }
        if (gameContext.isSelectionBlocked()) {
            // During the delay of a new recipe: the fixation does not start (it starts once the delay is over)
            progress = 0;
            ring.setVisible(false);
            return;
        }
        if (!ring.isVisible()) {
            showRing(looked);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        GameStyle.fillRing(ring, ringPerimeter, Math.min(1, progress));
        if (progress >= 1) {
            final Ingredient ingredient = shelf.get(looked);
            progress = 0;
            ring.setVisible(false);
            choose(ingredient);
        }
    }

    void lookAt(final int index) {
        if (looked == index) {
            return;
        }
        looked = index;
        progress = 0;
        ring.setVisible(false);
        showRing(index);
    }

    void lookAway(final int index) {
        if (looked == index) {
            looked = -1;
            progress = 0;
            ring.setVisible(false);
        }
    }

    /**
     * For the tests: looks at an ingredient of the shelf.
     */
    void lookAt(final Ingredient ingredient) {
        lookAt(shelf.indexOf(ingredient));
    }

    private void showRing(final int index) {
        if (phase != Phase.PLAY || !choosable(index) || gameContext.isSelectionBlocked()) {
            return;
        }
        final double[] slot = slots.get(index);
        ring.setLayoutX(slot[0]);
        ring.setLayoutY(slot[1]);
        GameStyle.fillRing(ring, ringPerimeter, progress);
        ring.setVisible(true);
    }

    /**
     * An ingredient of the shelf chosen: into the cauldron if it is in the recipe, otherwise it shakes.
     */
    void choose(final Ingredient ingredient) {
        final int index = shelf.indexOf(ingredient);
        if (phase != Phase.PLAY || index < 0 || !choosable(index)) {
            return;
        }
        looked = -1;
        ring.setVisible(false);
        moving = ingredient;
        movingSlot = index;
        elapsed = 0;
        if (recipe.contains(ingredient)) {
            // It leaves the shelf: an empty card stays (a refused ingredient stays greyed, its picture shown)
            cards.get(index).setOpacity(0.35);
            cardPictures.get(index).setVisible(false);
            flying.setImage(pictures.get(ingredient));
            flying.setOpacity(1);
            flying.setVisible(true);
            phase = Phase.FLY;
            fly(0);
        } else {
            refused.add(ingredient);
            stats.incrementNumberOfMistakes();
            if (withLives) {
                lives--;
            }
            gameContext.getSoundManager().add(WRONG_SOUND);
            phase = Phase.WRONG;
            showHud();
        }
    }

    private void finish() {
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        if (bravo) {
            gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
        } else {
            gameContext.showRoundStats(stats, this);
        }
    }

    boolean isBravo() {
        return bravo;
    }

    boolean isExploding() {
        return explosion.isVisible();
    }

    /**
     * @return the ring of the fixation, along the edge of a card
     */
    Rectangle getRing() {
        return ring;
    }

    /**
     * @return the card of an ingredient of the shelf
     */
    Group cardOf(final Ingredient ingredient) {
        return cards.get(shelf.indexOf(ingredient));
    }

    /**
     * @return the ingredients ticked in the bubble
     */
    long ticked() {
        return ticks.values().stream().filter(Node::isVisible).count();
    }

    /**
     * @return the ingredients shown in the bubble
     */
    long recipeShown() {
        return bubble.getChildren().stream().filter(child -> child instanceof ImageView).count();
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final double margin = height * 0.025;
        if (withLives) {
            final Group hearts = GameStyle.lives(lives, LIVES, height * 0.055, width * 0.3, true);
            hearts.setLayoutX(margin);
            hearts.setLayoutY(margin * 0.6);
            hud.getChildren().add(hearts);
        }
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("PotionsScore") + " " + served
            + (end.getServed() > 0 ? " / " + end.getServed() : ""), fontSize);
        pill.setLayoutX((width * 0.5 - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(margin * 0.6);
        hud.getChildren().add(pill);
    }

    /**
     * @param withLives whether the user chose the hearts (see {@code LivesVariant}), before the launch
     */
    void setWithLives(final boolean withLives) {
        this.withLives = withLives;
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
        for (final Node node : new ArrayList<>(gazeNodes)) {
            gameContext.getGazeDeviceManager().removeEventFilter(node);
        }
        gazeNodes.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * @return the settings of the menu (Esc) acting on this game: the others are greyed
     */
    @Override
    public java.util.Set<net.gazeplay.MenuSetting> menuSettings() {
        return net.gazeplay.MenuSetting.fixationWithSharedRing(net.gazeplay.MenuSetting.ANIMATION_SPEED);
    }
}
