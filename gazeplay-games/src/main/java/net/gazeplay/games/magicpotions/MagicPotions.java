package net.gazeplay.games.magicpotions;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * "Magic Potions" (name code Potions; reviewed on 10/11/2026, maintainer's rules): a Biboule asks for a potion of a
 * color, shown in its bubble with the name of the color (and the potions to pour, with the recipe). A fixation on a
 * potion of the table (red, yellow, blue) pours it into the cauldron, which takes the color of the mixture; each
 * potion once per client, in any order. The potion asked for made: the client drinks it, one more served. A potion out
 * of the recipe: the cauldron explodes, a life is lost, and a new client comes. Three lives (hearts); the game ends
 * after 3, 5 or 10 potions served ("Bravo"), or never, or when the lives are lost ("Lost!"). The three potions together
 * make brown (black before).
 * <p>
 * The pictures are those drawn for the project in October 2026 (gradle/images/make_potions_images.py). One loop, in
 * real seconds: the potions are poured at the speed of the settings, the fixations last the length of the settings
 * (read at each step), nothing can be chosen before the delay of the settings nor while a potion is poured, and
 * nothing moves while the menu of the game is open. (Before the review: the pouring counted in images of the screen
 * as if there were always 60 a second, no end, no lives, only a spot of color for the request, a goal to reach added
 * at each right potion, the limiters.)
 */
public class MagicPotions implements GameLifeCycle {

    static final int LIVES = 3;
    /**
     * In seconds (at the speed of the settings): a potion poured (going, tilted, back), the explosion or the client
     * served, the end shown.
     */
    static final double POUR = 1.4;
    static final double RESULT = 1.8;
    static final double SHOW = 2.2;

    private static final String IMAGES = "data/potions/images/";
    private static final String SOUNDS = "data/potions/sounds/";
    private static final Color EMPTY = Color.rgb(226, 236, 246);

    enum Phase { WAIT, PLAY, POUR, FAILED, SERVED, SHOW, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final PotionsLevel level;
    @Getter
    private final PotionsEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private PotionMix request;
    @Getter
    private final Set<PotionMix.Primary> mixture = EnumSet.noneOf(PotionMix.Primary.class);
    @Getter
    private int lives;
    @Getter
    private int served;
    private boolean bravo;
    private PotionMix.Primary pouring;
    private double elapsed;
    @Getter
    private double waiting;

    private double width;
    private double height;
    private double potionWidth;
    private double potionHeight;
    private double potSize;
    private double potX;
    private double potY;
    private double clientScale;
    private double clientX;
    private double clientY;

    private Group layer;
    private final Map<PotionMix.Primary, ImageView> potions = new EnumMap<>(PotionMix.Primary.class);
    private final Map<PotionMix.Primary, Rectangle> zones = new EnumMap<>(PotionMix.Primary.class);
    private final Map<PotionMix.Primary, double[]> homes = new EnumMap<>(PotionMix.Primary.class);
    private final Map<PotionMix.Primary, Image> potionImages = new EnumMap<>(PotionMix.Primary.class);
    private Ellipse potColor;
    private Circle requestColor;
    private ImageView client;
    private Group requestText;
    private ImageView explosion;
    private FixationRing ring;
    private Group hud;
    private Group message;
    private final List<Node> gazeNodes = new ArrayList<>();

    private PotionMix.Primary looked;
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
    MagicPotions(final IGameContext gameContext, final Stats stats, final PotionsLevel level, final PotionsEnd end,
                 final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.level = level;
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

        // The cauldron at the left, the potions at the right, at the bottom
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
        potionHeight = height * 0.3;
        potionWidth = potionHeight / 2;
        final Group potionsLayer = new Group();
        final Group zonesLayer = new Group();
        final String[] files = {"potionRed.png", "potionYellow.png", "potionBlue.png"};
        for (final PotionMix.Primary primary : PotionMix.Primary.values()) {
            final Image image = new Image(IMAGES + files[primary.ordinal()]);
            potionImages.put(primary, image);
            final ImageView view = new ImageView(image);
            view.setFitWidth(potionWidth);
            view.setFitHeight(potionHeight);
            final double x = width * (0.5 + 0.16 * primary.ordinal());
            final double y = height * 0.95 - potionHeight / 2;
            homes.put(primary, new double[]{x, y});
            potions.put(primary, view);
            view.setMouseTransparent(true);
            potionsLayer.getChildren().add(view);
            // The gaze looks at the whole flask, its neck too (maintainer's remark after his trial of 10/11/2026: only
            // the round body reacted, the picture being picked by its drawn pixels), a little wider than it
            final Rectangle zone = new Rectangle(x - potionWidth * 0.6, y - potionHeight / 2, potionWidth * 1.2,
                potionHeight);
            zone.setFill(Color.TRANSPARENT);
            zones.put(primary, zone);
            zonesLayer.getChildren().add(zone);
            listen(zone, () -> lookAt(primary), () -> lookAway(primary));
        }

        // The client at the top right, its bubble asking for the potion
        clientScale = Math.min(width * 0.42 / 670, height * 0.42 / 422);
        clientX = width * 0.5 + (width * 0.5 - 670 * clientScale) / 2;
        clientY = height * 0.1;
        // Behind the transparent body of the flask of the bubble (centered on (449, 160), radius 46 in the picture)
        requestColor = new Circle(clientX + 449 * clientScale, clientY + 160 * clientScale, 50 * clientScale);
        client = new ImageView(new Image(IMAGES + "Biboule-Client.png"));
        client.setFitWidth(670 * clientScale);
        client.setFitHeight(422 * clientScale);
        client.setX(clientX);
        client.setY(clientY);
        client.setMouseTransparent(true);
        requestText = new Group();
        requestText.setMouseTransparent(true);
        explosion = new ImageView(new Image("data/common/images/explosion.gif"));
        // An animated picture of 700 x 316, growing from its middle
        explosion.setPreserveRatio(true);
        explosion.setFitWidth(potSize * 2.6);
        explosion.setX(potX + 75 * potScale - potSize * 1.3);
        explosion.setY(potY + 45 * potScale - potSize * 2.6 * 316 / 700 / 2);
        explosion.setVisible(false);
        explosion.setMouseTransparent(true);
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        layer = new Group(background, requestColor, client, requestText, potColor, pot, potionsLayer, zonesLayer,
            explosion, ring, hud, message);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        lives = LIVES;
        served = 0;
        elapsed = 0;
        waiting = configuration.getGameStartDelay() / 1000.0;
        phase = Phase.WAIT;
        placePotions();
        showHud();
        if (!manual) {
            loop.start();
        }
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
     * A new client, asking for another potion than the last one.
     */
    private void newClient() {
        PotionMix next;
        do {
            next = PotionMix.values()[random.nextInt(PotionMix.values().length)];
        } while (next == request);
        request = next;
        mixture.clear();
        pouring = null;
        potColor.setFill(EMPTY);
        explosion.setVisible(false);
        client.setScaleX(1);
        client.setScaleY(1);
        client.setTranslateY(0);
        requestColor.setFill(request.getColor());
        requestColor.setTranslateY(0);
        message.getChildren().clear();
        showRequest();
        looked = null;
        progress = 0;
        ring.stop();
        placePotions();
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        phase = Phase.PLAY;
        elapsed = 0;
        showHud();
    }

    /**
     * Under the bubble: the name of the color, and with the recipe the potions to pour.
     */
    private void showRequest() {
        final double centerX = clientX + 449 * clientScale;
        final Group name = GameStyle.pill(gameContext.getTranslator().translate(request.getKey()), height * 0.04);
        name.setLayoutX(centerX - name.getBoundsInLocal().getWidth() / 2);
        name.setLayoutY(clientY + 300 * clientScale);
        requestText.getChildren().setAll(name);
        if (level != PotionsLevel.WITH_RECIPE) {
            return;
        }
        final double small = height * 0.1;
        final List<PotionMix.Primary> recipe = new ArrayList<>(request.getRecipe());
        final double gap = small * 0.45;
        final double total = recipe.size() * small * 0.5 + (recipe.size() - 1) * gap;
        double x = centerX - total / 2;
        final double y = clientY + 300 * clientScale + name.getBoundsInLocal().getHeight() + height * 0.015;
        for (int i = 0; i < recipe.size(); i++) {
            final ImageView flask = new ImageView(potionImages.get(recipe.get(i)));
            flask.setFitWidth(small * 0.5);
            flask.setFitHeight(small);
            flask.setX(x);
            flask.setY(y);
            requestText.getChildren().add(flask);
            x += small * 0.5;
            if (i < recipe.size() - 1) {
                final Text plus = new Text("+");
                plus.setFont(GameStyle.titleFont(small * 0.45));
                plus.setFill(Color.WHITE);
                plus.setTextOrigin(VPos.CENTER);
                plus.setX(x + (gap - plus.getLayoutBounds().getWidth()) / 2);
                plus.setY(y + small * 0.6);
                requestText.getChildren().add(plus);
                x += gap;
            }
        }
    }

    private void placePotions() {
        for (final PotionMix.Primary primary : PotionMix.Primary.values()) {
            final ImageView view = potions.get(primary);
            final double[] home = homes.get(primary);
            if (primary != pouring) {
                view.setRotate(0);
                view.setX(home[0] - potionWidth / 2);
                view.setY(home[1] - potionHeight / 2);
            }
            view.setOpacity(mixture.contains(primary) && primary != pouring ? 0.35 : 1);
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
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= waiting) {
                    newClient();
                }
            }
            case PLAY -> fixate(seconds);
            case POUR -> pour(scaled);
            case FAILED -> {
                elapsed += scaled;
                if (elapsed >= RESULT) {
                    explosion.setVisible(false);
                    afterClient();
                }
            }
            case SERVED -> {
                elapsed += scaled;
                // The client jumps for joy, its potion drunk
                final double jump = Math.abs(Math.sin(elapsed * Math.PI * 2.5)) * height * 0.03;
                client.setTranslateY(-jump);
                requestColor.setTranslateY(-jump);
                if (elapsed >= RESULT) {
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
     * The potion goes above the cauldron, tilts and pours (the mixture changes color), then comes back.
     */
    private void pour(final double seconds) {
        final double before = elapsed;
        elapsed += seconds;
        final ImageView view = potions.get(pouring);
        final double[] home = homes.get(pouring);
        final double aboveX = potX + 75 * potSize / 136 + potionWidth * 0.6;
        final double aboveY = potY - potionHeight * 0.45;
        final double go = POUR * 0.35;
        final double back = POUR * 0.65;
        final double t;
        final double tilt;
        if (elapsed < go) {
            t = smooth(elapsed / go);
            tilt = 0;
        } else if (elapsed < back) {
            t = 1;
            tilt = Math.min(1, (elapsed - go) / (POUR * 0.15));
        } else {
            t = 1 - smooth(Math.min(1, (elapsed - back) / (POUR - back)));
            tilt = t;
        }
        view.setX(home[0] + (aboveX - home[0]) * t - potionWidth / 2);
        view.setY(home[1] + (aboveY - home[1]) * t - potionHeight / 2);
        view.setRotate(-110 * tilt);
        final double poured = POUR * 0.5;
        if (before < poured && elapsed >= poured) {
            mixture.add(pouring);
            final PotionMix made = PotionMix.of(mixture);
            potColor.setFill(made == null ? EMPTY : made.getColor());
            gameContext.getSoundManager().add(SOUNDS + "pour.wav");
        }
        if (elapsed >= POUR) {
            final PotionMix.Primary done = pouring;
            pouring = null;
            placePotions();
            poured(done);
        }
    }

    private static double smooth(final double t) {
        return t * t * (3 - 2 * t);
    }

    /**
     * A potion poured: out of the recipe, the cauldron explodes; the recipe complete, the client is served.
     */
    private void poured(final PotionMix.Primary primary) {
        elapsed = 0;
        if (!request.getRecipe().contains(primary)) {
            lives--;
            potColor.setFill(Color.rgb(70, 70, 76));
            explosion.setVisible(true);
            gameContext.getSoundManager().add(SOUNDS + "boom.wav");
            phase = Phase.FAILED;
        } else if (mixture.equals(request.getRecipe())) {
            served++;
            stats.incrementNumberOfGoalsReached();
            gameContext.getSoundManager().add(SOUNDS + "served.wav");
            phase = Phase.SERVED;
        } else {
            phase = Phase.PLAY;
        }
        showHud();
    }

    private void afterClient() {
        client.setTranslateY(0);
        requestColor.setTranslateY(0);
        if (lives <= 0) {
            bravo = false;
            final Group pill = GameStyle.pill(gameContext.getTranslator().translate("PotionsLost"), height * 0.05);
            pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
            pill.setLayoutY(height * 0.45 - pill.getBoundsInLocal().getHeight() / 2);
            message.getChildren().setAll(pill);
            phase = Phase.SHOW;
            elapsed = 0;
            return;
        }
        if (end.getServed() > 0 && served >= end.getServed()) {
            bravo = true;
            finish();
            return;
        }
        newClient();
    }

    private void fixate(final double seconds) {
        if (looked == null || mixture.contains(looked)) {
            return;
        }
        if (!ring.isVisible()) {
            showRing(looked);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final PotionMix.Primary primary = looked;
            progress = 0;
            ring.stop();
            choose(primary);
        }
    }

    void lookAt(final PotionMix.Primary primary) {
        if (looked == primary) {
            return;
        }
        looked = primary;
        progress = 0;
        ring.stop();
        showRing(primary);
    }

    void lookAway(final PotionMix.Primary primary) {
        if (looked == primary) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    private void showRing(final PotionMix.Primary primary) {
        if (phase != Phase.PLAY || mixture.contains(primary)) {
            return;
        }
        final double[] home = homes.get(primary);
        ring.setSize(potionWidth * 1.2);
        ring.show(home[0], home[1] + potionHeight * 0.18);
    }

    /**
     * A potion of the table chosen: it is poured into the cauldron.
     */
    void choose(final PotionMix.Primary primary) {
        if (phase != Phase.PLAY || mixture.contains(primary)) {
            return;
        }
        pouring = primary;
        looked = null;
        ring.stop();
        potions.get(primary).toFront();
        phase = Phase.POUR;
        elapsed = 0;
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
     * @return the zone looked at to choose a potion of the table
     */
    Rectangle zoneOf(final PotionMix.Primary primary) {
        return zones.get(primary);
    }

    /**
     * @return the picture of a potion of the table
     */
    ImageView potionOf(final PotionMix.Primary primary) {
        return potions.get(primary);
    }

    /**
     * @return the color of the cauldron
     */
    Color potColor() {
        return (Color) potColor.getFill();
    }

    /**
     * @return the potions of the recipe shown under the request (none without the recipe)
     */
    long recipeShown() {
        return requestText.getChildren().stream().filter(child -> child instanceof ImageView).count();
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final double margin = height * 0.025;
        final Group hearts = GameStyle.lives(lives, LIVES, height * 0.055, width * 0.3, true);
        hearts.setLayoutX(margin);
        hearts.setLayoutY(margin * 0.6);
        hud.getChildren().add(hearts);
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("PotionsScore") + " " + served
            + (end.getServed() > 0 ? " / " + end.getServed() : ""), fontSize);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(margin * 0.6);
        hud.getChildren().add(pill);
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
        if (ring != null) {
            ring.stop();
        }
        for (final Node node : new ArrayList<>(gazeNodes)) {
            gameContext.getGazeDeviceManager().removeEventFilter(node);
        }
        gazeNodes.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }
}
