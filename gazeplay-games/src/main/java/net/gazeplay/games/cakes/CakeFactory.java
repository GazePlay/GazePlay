package net.gazeplay.games.cakes;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * "La Fabrique à gâteaux" (name code Cakes; reviewed on 12/11/2026, maintainer's rules): a cake built layer by layer,
 * every part drawn by the game. Each layer goes through its steps in order: the cake (4 colors), the cream (5), then
 * an accessory around its side (a ribbon, pearls or hearts) for the layers under the top one, and for the top layer the
 * sweets (3) and the decoration (umbrellas or winged Biboules): sweets on a layer under another would be hidden
 * (maintainer's remark at his trial); a row of round buttons, chosen by
 * fixations, shows the choices of the step, each as the layer would look with it.
 * <p>
 * To copy the model (1 layer, 3 layers, or 3 layers with a model that hides after 5 s and is shown again by a button):
 * a choice that differs from the model is put on the cake anyway, the part of the model blinks in red and counts a
 * mistake (no lives), and the step is chosen again; a right choice goes on. In the free mode, one's own cake, up to
 * three layers (after the cream: "Encore un étage", then the accessory, or "Décorer", then the sweets and the
 * decoration). A finished cake: a star and "Bravo !", then another; the game ends
 * after 1, 3 or 5 cakes ("Bravo", then the statistics), or has no end.
 * <p>
 * One loop, in real seconds: the fixation lasts the length of the settings (read at each step), nothing can be chosen
 * before the delay of the settings, while a layer arrives, nor while the menu of the game is open. (Before the review:
 * 24 pictures and 3 sounds of unknown source, menus in menus with a back button, a new layer only once the one before
 * was right, a model shown again by the mouse only in the hard variant, no goal to reach.)
 */
@Slf4j
public class CakeFactory implements GameLifeCycle {

    static final int SPONGES = 4;
    static final int CREAMS = 5;
    static final int SWEETS = 3;
    static final int ACCESSORIES = 3;
    static final int DECORATIONS = 2;
    /**
     * The seconds a layer takes to arrive, the model shows in the hard level, is shown again by its button, blinks,
     * and the reward lasts.
     */
    static final double ARRIVE = 0.5;
    static final double MODEL_SHOWN = 5;
    static final double MODEL_AGAIN = 3;
    static final double BLINK = 1;
    static final double REWARD = 2.2;

    /**
     * Cakes: vanilla, chocolate, strawberry, blueberry. Icings: white chocolate, dark chocolate, caramel, blackcurrant,
     * mint. Chosen so that every color differs from the others at a glance, also with a red-green color blindness
     * (smallest CIEDE2000 difference 17, and 12 with deuteranopia), on the cake as on the small model.
     */
    private static final Color[] SPONGE = {Color.rgb(240, 214, 150), Color.rgb(150, 95, 60), Color.rgb(245, 165, 185),
        Color.rgb(120, 160, 235)};
    private static final Color[] CREAM = {Color.rgb(250, 246, 236), Color.rgb(70, 40, 25), Color.rgb(235, 140, 40),
        Color.rgb(120, 45, 150), Color.rgb(60, 150, 140)};
    private static final Color[] CONFETTI = {Color.rgb(230, 60, 70), Color.rgb(250, 190, 40), Color.rgb(60, 170, 90),
        Color.rgb(60, 120, 230), Color.rgb(170, 80, 200)};
    private static final String SOUNDS = "data/cake/sounds/";

    /**
     * The steps of a layer.
     */
    enum Step {
        SPONGE("CakesStepSponge"), CREAM("CakesStepCream"), NEXT("CakesStepNext"),
        ACCESSORY("CakesStepAccessory"), SWEETS("CakesStepSweets"), DECORATION("CakesStepDecoration");

        @Getter
        private final String key;

        Step(final String key) {
            this.key = key;
        }
    }

    enum Phase { WAIT, CHOOSE, ARRIVE, REWARD, OVER }

    /**
     * A layer: its cake, cream, and accessory (a layer under another) or sweets (the top layer), -1 before they are
     * chosen or when the layer has none.
     */
    static final class Layer {
        int sponge = -1;
        int cream = -1;
        int sweets = -1;
        int accessory = -1;

        Layer copy() {
            final Layer layer = new Layer();
            layer.sponge = sponge;
            layer.cream = cream;
            layer.sweets = sweets;
            layer.accessory = accessory;
            return layer;
        }

        int part(final Step step) {
            return switch (step) {
                case SPONGE -> sponge;
                case CREAM -> cream;
                case SWEETS -> sweets;
                case ACCESSORY -> accessory;
                default -> -1;
            };
        }
    }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final CakesMode mode;
    @Getter
    private final CakesLevel level;
    @Getter
    private final CakesEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private Step step = Step.SPONGE;
    @Getter
    private int cakes;
    private double elapsed;
    private double phaseTime;
    private double modelTime;
    private double modelAgain;
    private double blinkLeft;
    private boolean rewarded;
    @Getter
    private final List<Layer> layers = new ArrayList<>();
    @Getter
    private int decoration = -1;
    @Getter
    private final List<Layer> model = new ArrayList<>();
    @Getter
    private int modelDecoration;

    private double width;
    private double height;
    private double cakeX;
    private double plateY;
    private double baseWidth;

    private Group layer;
    private Group cakeGroup;
    private Group modelCard;
    private Group modelGroup;
    private Group modelHidden;
    private Group tabs;
    private Group buttonsLayer;
    private Group hud;
    private Group reward;
    private FixationRing ring;
    @Getter
    private final List<Choice> choices = new ArrayList<>();
    @Getter
    private Choice looked;
    @Getter
    private double progress;
    private Choice showModel;

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
    CakeFactory(final IGameContext gameContext, final Stats stats, final CakesMode mode, final CakesLevel level,
                final CakesEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.level = mode == CakesMode.FREE ? CakesLevel.FREE : level == CakesLevel.FREE ? CakesLevel.NORMAL : level;
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
        cakeX = mode == CakesMode.COPY ? width * 0.4 : width * 0.5;
        plateY = height * 0.7;
        baseWidth = Math.min(height * 0.44, width * 0.3);

        final Rectangle room = new Rectangle(0, 0, width, height);
        room.setStyle("-fx-fill: -gp-bg;");
        room.setMouseTransparent(true);
        // A soft table under the cake
        final Rectangle table = new Rectangle(0, plateY + height * 0.02, width, height - plateY);
        table.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(236, 214, 186, 0.75)), new Stop(1, Color.rgb(222, 196, 164, 0.75))));
        table.setMouseTransparent(true);
        final Ellipse plate = new Ellipse(cakeX, plateY + height * 0.01, baseWidth * 0.68, baseWidth * 0.14);
        plate.setFill(Color.rgb(250, 250, 252));
        plate.setStroke(Color.rgb(200, 204, 214));
        plate.setStrokeWidth(Math.max(2, height * 0.004));
        plate.setMouseTransparent(true);

        cakeGroup = new Group();
        cakeGroup.setMouseTransparent(true);
        modelCard = new Group();
        tabs = new Group();
        tabs.setMouseTransparent(true);
        buttonsLayer = new Group();
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        reward = new Group();
        reward.setMouseTransparent(true);
        layer = new Group(room, table, plate, cakeGroup, modelCard, tabs, buttonsLayer, ring, hud, reward);
        gameContext.getChildren().add(layer);
        gameContext.getGazeDeviceManager().addStats(stats);
        cakes = 0;
        newCake();
        phase = Phase.WAIT;
        elapsed = 0;
        if (!manual) {
            loop.start();
        }
    }

    /**
     * A new cake: a first layer to make, and a new model to copy.
     */
    private void newCake() {
        layers.clear();
        layers.add(new Layer());
        decoration = -1;
        reward.getChildren().clear();
        if (mode == CakesMode.COPY) {
            model.clear();
            for (int i = 0; i < level.getLayers(); i++) {
                final Layer part = new Layer();
                part.sponge = random.nextInt(SPONGES);
                part.cream = random.nextInt(CREAMS);
                if (i == level.getLayers() - 1) {
                    part.sweets = random.nextInt(SWEETS);
                } else {
                    part.accessory = random.nextInt(ACCESSORIES);
                }
                model.add(part);
            }
            modelDecoration = random.nextInt(DECORATIONS);
            buildModelCard();
            modelTime = 0;
            modelAgain = 0;
        }
        blinkLeft = 0;
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        setStep(Step.SPONGE);
        drawCake();
        showScore();
    }

    private void showScore() {
        final String text = gameContext.getTranslator().translate("CakesDone") + " " + cakes
            + (end.getCakes() > 0 ? " / " + end.getCakes() : "");
        final Group score = GameStyle.pill(text, height * 0.032);
        score.setLayoutX(width - score.getBoundsInLocal().getWidth() - height * 0.03);
        score.setLayoutY(height * 0.025);
        hud.getChildren().setAll(score);
    }

    /**
     * The steps as tabs at the top, the step of now framed.
     */
    private void showTabs() {
        tabs.getChildren().clear();
        final List<Step> shown = new ArrayList<>(List.of(Step.SPONGE, Step.CREAM));
        final boolean ending = step == Step.SWEETS || step == Step.DECORATION;
        if (mode == CakesMode.FREE && !isLastLayer() && !ending && step != Step.ACCESSORY) {
            shown.add(Step.NEXT);
        }
        if (step == Step.ACCESSORY || mode == CakesMode.COPY && !isLastLayer()) {
            shown.add(Step.ACCESSORY);
        }
        if (ending || isLastLayer()) {
            shown.add(Step.SWEETS);
            shown.add(Step.DECORATION);
        }
        final double size = height * 0.03;
        double x = height * 0.03;
        final String floor = gameContext.getTranslator().translate("CakesLayer") + " " + layers.size()
            + (mode == CakesMode.COPY ? " / " + model.size() : "");
        final Group floorPill = GameStyle.pill(floor, size);
        floorPill.setLayoutX(x);
        floorPill.setLayoutY(height * 0.025);
        tabs.getChildren().add(floorPill);
        x += floorPill.getBoundsInLocal().getWidth() + height * 0.03;
        for (final Step tab : shown) {
            final Group pill = GameStyle.pill(gameContext.getTranslator().translate(tab.getKey()), size);
            if (tab == step) {
                pill.getChildren().get(0).setStyle("-fx-fill: -gp-accent; -fx-stroke: -gp-accent;");
                pill.getChildren().get(1).setStyle(GameStyle.titleFontStyle(size) + " -fx-fill: -gp-on-accent;");
            } else {
                pill.setOpacity(0.7);
            }
            pill.setLayoutX(x);
            pill.setLayoutY(height * 0.025);
            tabs.getChildren().add(pill);
            x += pill.getBoundsInLocal().getWidth() + height * 0.015;
        }
    }

    /**
     * @return whether the layer being made is the last one: the last of the model, or the third in the free mode
     */
    private boolean isLastLayer() {
        return layers.size() >= (mode == CakesMode.COPY ? model.size() : level.getLayers());
    }

    /**
     * Goes to a step: its tabs and its buttons.
     */
    private void setStep(final Step next) {
        step = next;
        if (looked != null) {
            lookAway(looked);
        }
        clearButtons();
        final List<Choice> row = new ArrayList<>();
        final Layer current = currentLayer();
        switch (step) {
            case SPONGE -> {
                for (int i = 0; i < SPONGES; i++) {
                    final Layer preview = new Layer();
                    preview.sponge = i;
                    row.add(new Choice(step, i, layerPicture(preview, buttonRadius() * 1.3, 11), null));
                }
            }
            case CREAM -> {
                for (int i = 0; i < CREAMS; i++) {
                    final Layer preview = current.copy();
                    preview.cream = i;
                    row.add(new Choice(step, i, layerPicture(preview, buttonRadius() * 1.3, 11), null));
                }
            }
            case SWEETS -> {
                for (int i = 0; i < SWEETS; i++) {
                    final Layer preview = current.copy();
                    preview.sweets = i;
                    row.add(new Choice(step, i, layerPicture(preview, buttonRadius() * 1.3, 11 + i), null));
                }
            }
            case ACCESSORY -> {
                for (int i = 0; i < ACCESSORIES; i++) {
                    final Layer preview = current.copy();
                    preview.accessory = i;
                    row.add(new Choice(step, i, layerPicture(preview, buttonRadius() * 1.3, 11), null));
                }
            }
            case NEXT -> {
                row.add(new Choice(step, 0, icon("add"), "CakesMoreLayer"));
                row.add(new Choice(step, 1, icon("palette"), "CakesDecorate"));
            }
            case DECORATION -> {
                for (int i = 0; i < DECORATIONS; i++) {
                    final Group preview = new Group(layerPicture(current, buttonRadius() * 1.2, 11));
                    preview.getChildren().add(decorationPicture(i, buttonRadius() * 1.2, -buttonRadius() * 0.12));
                    preview.setLayoutY(buttonRadius() * 0.2);
                    row.add(new Choice(step, i, preview, null));
                }
            }
            default -> {
            }
        }
        // The two buttons of the free mode have their labels above: more room between them
        final double gap = buttonRadius() * (step == Step.NEXT ? 1.6 : 0.5);
        final double rowWidth = row.size() * 2 * buttonRadius() + (row.size() - 1) * gap;
        final double left = (mode == CakesMode.COPY ? width * 0.4 : width * 0.5) - rowWidth / 2;
        final double y = height - buttonRadius() - height * 0.05;
        for (int i = 0; i < row.size(); i++) {
            row.get(i).place(left + buttonRadius() + i * (2 * buttonRadius() + gap), y);
        }
        choices.addAll(row);
        if (mode == CakesMode.COPY && level.isHidden()) {
            showModel = new Choice(null, 0, icon("search"), "CakesShowModel");
            showModel.place(width * 0.8, height - buttonRadius() - height * 0.05);
            choices.add(showModel);
        }
        showTabs();
    }

    private double buttonRadius() {
        return Math.min(height * 0.075, width * 0.04);
    }

    private Layer currentLayer() {
        return layers.get(layers.size() - 1);
    }

    private Node icon(final String name) {
        final double size = buttonRadius();
        final SVGPath shape = GameStyle.icon(name, size);
        shape.setStyle("-fx-fill: -gp-accent;");
        final Group group = new Group(shape);
        group.setLayoutX(-size / 2);
        group.setLayoutY(-size / 2);
        return new Group(group);
    }

    private void clearButtons() {
        for (final Choice choice : choices) {
            gameContext.getGazeDeviceManager().removeEventFilter(choice.disc);
        }
        choices.clear();
        buttonsLayer.getChildren().clear();
        ring.stop();
        looked = null;
        progress = 0;
        showModel = null;
    }

    // --- Drawing ---

    /**
     * @return a layer drawn around the center of its top, of a width; the parts not chosen yet are left out, a layer
     * without its cake is a dashed outline
     */
    static Group layerPicture(final Layer part, final double w, final long seed) {
        final double h = w * 0.34;
        final double ry = w * 0.13;
        final Group group = new Group();
        if (part.sponge < 0) {
            final Ellipse top = new Ellipse(0, 0, w / 2, ry);
            final Ellipse bottom = new Ellipse(0, h, w / 2, ry);
            final Line left = new Line(-w / 2, 0, -w / 2, h);
            final Line right = new Line(w / 2, 0, w / 2, h);
            for (final var shape : List.of(top, bottom, left, right)) {
                shape.setFill(null);
                shape.setStroke(Color.rgb(150, 140, 130, 0.8));
                shape.setStrokeWidth(Math.max(1.5, w * 0.012));
                shape.getStrokeDashArray().setAll(w * 0.03, w * 0.025);
            }
            group.getChildren().addAll(bottom, left, right, top);
            return group;
        }
        final Color sponge = SPONGE[part.sponge];
        final Ellipse bottom = new Ellipse(0, h, w / 2, ry);
        bottom.setFill(sponge.darker());
        final Rectangle body = new Rectangle(-w / 2, 0, w, h);
        body.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop(0, sponge.darker()),
            new Stop(0.35, sponge.brighter()), new Stop(1, sponge.darker())));
        final Ellipse top = new Ellipse(0, 0, w / 2, ry);
        top.setFill(sponge.brighter());
        group.getChildren().addAll(bottom, body, top);
        final Random rnd = new Random(seed);
        if (part.cream >= 0) {
            final Color cream = CREAM[part.cream];
            final double band = h * 0.28;
            final Rectangle coat = new Rectangle(-w / 2 - 1, 0, w + 2, band);
            coat.setFill(cream);
            final Ellipse edge = new Ellipse(0, band, w / 2 + 1, ry);
            edge.setFill(cream);
            group.getChildren().addAll(coat, edge);
            // Drips down the front
            for (int i = 0; i < 7; i++) {
                final double x = -w * 0.42 + i * w * 0.14 + (rnd.nextDouble() - 0.5) * w * 0.04;
                final double front = band + ry * Math.sqrt(Math.max(0, 1 - Math.pow(x / (w / 2), 2)));
                final double length = h * (0.2 + rnd.nextDouble() * 0.38);
                final double dw = w * (0.025 + rnd.nextDouble() * 0.02);
                final Rectangle drip = new Rectangle(x - dw, front - dw, 2 * dw, length);
                drip.setArcWidth(2 * dw);
                drip.setArcHeight(2 * dw);
                drip.setFill(cream);
                group.getChildren().addAll(drip, new Circle(x, front + length - dw, dw * 1.2, cream));
            }
            final Ellipse surface = new Ellipse(0, 0, w / 2 + 1, ry + 1);
            surface.setFill(cream.brighter());
            final Ellipse gleam = new Ellipse(-w * 0.15, -ry * 0.25, w * 0.16, ry * 0.25);
            gleam.setFill(Color.rgb(255, 255, 255, 0.35));
            group.getChildren().addAll(surface, gleam);
        }
        if (part.accessory >= 0) {
            group.getChildren().add(accessory(part.accessory, w, h, ry));
        }
        if (part.sweets >= 0) {
            for (int i = 0; i < 16; i++) {
                final double a = rnd.nextDouble() * 2 * Math.PI;
                final double d = Math.sqrt(rnd.nextDouble()) * 0.82;
                final double x = Math.cos(a) * d * w / 2;
                final double y = Math.sin(a) * d * ry;
                group.getChildren().add(sweet(part.sweets, x, y, w, rnd));
            }
        }
        return group;
    }

    /**
     * @return an accessory around the side of a layer, low on its front: a ribbon with a bow, a garland of pearls, or a
     * row of hearts
     */
    private static Group accessory(final int kind, final double w, final double h, final double ry) {
        final Group group = new Group();
        final double y = h * 0.8;
        switch (kind) {
            case 0 -> {
                final Color ribbon = Color.rgb(90, 170, 230);
                final double half = h * 0.08;
                final Polygon band = new Polygon();
                final int n = 32;
                for (int i = 0; i <= n; i++) {
                    final double x = -w / 2 + w * i / n;
                    band.getPoints().addAll(x, y - half + front(x, w, ry));
                }
                for (int i = n; i >= 0; i--) {
                    final double x = -w / 2 + w * i / n;
                    band.getPoints().addAll(x, y + half + front(x, w, ry));
                }
                band.setFill(ribbon);
                final double cy = y + ry;
                final double size = h * 0.2;
                final Polygon left = new Polygon(0, cy, -size * 1.3, cy - size * 0.75, -size * 1.3, cy + size * 0.75);
                final Polygon right = new Polygon(0, cy, size * 1.3, cy - size * 0.75, size * 1.3, cy + size * 0.75);
                for (final Polygon wing : List.of(left, right)) {
                    wing.setFill(ribbon.brighter());
                    wing.setStroke(ribbon.darker());
                    wing.setStrokeWidth(Math.max(1, w * 0.006));
                    wing.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                }
                final Circle knot = new Circle(0, cy, size * 0.35, ribbon.darker());
                group.getChildren().addAll(band, left, right, knot);
            }
            case 1 -> {
                // Pearls hanging in garlands between five points of the front
                final int garlands = 4;
                for (int g = 0; g < garlands; g++) {
                    for (int i = 0; i <= 8; i++) {
                        final double u = (g + i / 8.0) / garlands;
                        final double x = -w * 0.46 + w * 0.92 * u;
                        final double sag = Math.sin(Math.PI * i / 8.0) * h * 0.12;
                        final Circle pearl = new Circle(x, y - h * 0.06 + sag + front(x, w, ry), w * 0.014,
                            Color.rgb(252, 250, 244));
                        pearl.setStroke(Color.rgb(196, 186, 170));
                        pearl.setStrokeWidth(Math.max(0.5, w * 0.003));
                        group.getChildren().add(pearl);
                    }
                }
            }
            default -> {
                for (int i = 0; i < 7; i++) {
                    final double x = -w * 0.39 + w * 0.13 * i;
                    group.getChildren().add(heart(x, y + front(x, w, ry), h * 0.11,
                        i % 2 == 0 ? Color.rgb(232, 60, 90) : Color.rgb(250, 130, 170)));
                }
            }
        }
        return group;
    }

    /**
     * @return how far below the middle line the front of a layer of a width is at a point (its edge is an ellipse)
     */
    private static double front(final double x, final double w, final double ry) {
        return ry * Math.sqrt(Math.max(0, 1 - Math.pow(x / (w / 2), 2)));
    }

    private static Node heart(final double cx, final double cy, final double size, final Color color) {
        final SVGPath heart = new SVGPath();
        heart.setContent("M 0 -0.3 C -0.15 -0.75 -0.95 -0.6 -0.9 -0.05 C -0.85 0.35 -0.3 0.6 0 0.9 "
            + "C 0.3 0.6 0.85 0.35 0.9 -0.05 C 0.95 -0.6 0.15 -0.75 0 -0.3 Z");
        heart.setFill(color);
        heart.setStroke(color.darker());
        heart.setStrokeWidth(0.08);
        heart.setScaleX(size);
        heart.setScaleY(size);
        heart.setLayoutX(cx);
        heart.setLayoutY(cy);
        return heart;
    }

    private static Node sweet(final int kind, final double x, final double y, final double w, final Random rnd) {
        return switch (kind) {
            case 0 -> {
                final Circle pearl = new Circle(x, y, w * 0.022, Color.rgb(252, 252, 255));
                pearl.setStroke(Color.rgb(200, 200, 215));
                pearl.setStrokeWidth(Math.max(0.5, w * 0.004));
                yield pearl;
            }
            case 1 -> {
                final Rectangle sprinkle = new Rectangle(x - w * 0.025, y - w * 0.008, w * 0.05, w * 0.016);
                sprinkle.setArcWidth(w * 0.016);
                sprinkle.setArcHeight(w * 0.016);
                sprinkle.setFill(CONFETTI[rnd.nextInt(CONFETTI.length)]);
                sprinkle.setRotate(rnd.nextDouble() * 180);
                yield sprinkle;
            }
            default -> {
                final Circle cookie = new Circle(x, y, w * 0.032, Color.rgb(222, 178, 112));
                cookie.setStroke(Color.rgb(160, 110, 60));
                cookie.setStrokeWidth(Math.max(0.5, w * 0.004));
                final Circle chip1 = new Circle(x - w * 0.01, y - w * 0.006, w * 0.008, Color.rgb(90, 50, 30));
                final Circle chip2 = new Circle(x + w * 0.012, y + w * 0.008, w * 0.007, Color.rgb(90, 50, 30));
                yield new Group(cookie, chip1, chip2);
            }
        };
    }

    /**
     * @return a decoration standing on a top at (0, y) of a layer of a width: two cocktail umbrellas, or three winged
     * Biboules flying above
     */
    static Group decorationPicture(final int kind, final double w, final double y) {
        final Group group = new Group();
        if (kind == 0) {
            final Color[] colors = {Color.rgb(60, 130, 230), Color.rgb(240, 90, 180)};
            for (int i = 0; i < 2; i++) {
                final double x = (i == 0 ? -0.2 : 0.22) * w;
                final double top = y - w * (i == 0 ? 0.42 : 0.34);
                final Line stick = new Line(x, y, x + w * 0.04 * (i == 0 ? -1 : 1), top);
                stick.setStroke(Color.rgb(170, 120, 70));
                stick.setStrokeWidth(Math.max(1.5, w * 0.018));
                stick.setStrokeLineCap(StrokeLineCap.ROUND);
                final double cx = x + w * 0.04 * (i == 0 ? -1 : 1);
                final Arc canopy = new Arc(cx, top + w * 0.02, w * 0.16, w * 0.11, 0, 180);
                canopy.setType(ArcType.CHORD);
                canopy.setFill(colors[i]);
                canopy.setStroke(colors[i].darker());
                canopy.setStrokeWidth(Math.max(1, w * 0.01));
                canopy.setRotate(i == 0 ? -12 : 14);
                final Circle knob = new Circle(cx, top - w * 0.09, w * 0.018, colors[i].darker());
                group.getChildren().addAll(stick, canopy, knob);
            }
        } else {
            final Color[] colors = {Color.rgb(250, 214, 70), Color.rgb(250, 150, 170), Color.rgb(120, 200, 240)};
            final double[][] places = {{-0.26, -0.3}, {0.02, -0.46}, {0.28, -0.28}};
            for (int i = 0; i < 3; i++) {
                group.getChildren().add(biboule(w * places[i][0], y + w * places[i][1], w * 0.075, colors[i]));
            }
        }
        return group;
    }

    /**
     * @return a small winged Biboule (the mascot of GazePlay), drawn again: a round body, two eyes, a smile, wings
     */
    private static Group biboule(final double x, final double y, final double r, final Color color) {
        final Ellipse wingLeft = new Ellipse(x - r * 0.95, y - r * 0.45, r * 0.7, r * 0.4);
        final Ellipse wingRight = new Ellipse(x + r * 0.95, y - r * 0.45, r * 0.7, r * 0.4);
        for (final Ellipse wing : List.of(wingLeft, wingRight)) {
            wing.setFill(Color.rgb(200, 236, 248, 0.9));
            wing.setStroke(Color.rgb(110, 170, 190));
            wing.setStrokeWidth(Math.max(0.8, r * 0.08));
        }
        wingLeft.setRotate(-25);
        wingRight.setRotate(25);
        final Circle body = new Circle(x, y, r, color);
        body.setStroke(color.darker());
        body.setStrokeWidth(Math.max(0.8, r * 0.08));
        final Group face = new Group();
        for (final double side : new double[]{-1, 1}) {
            final Circle eye = new Circle(x + side * r * 0.36, y - r * 0.12, r * 0.28, Color.WHITE);
            eye.setStroke(Color.rgb(60, 50, 50));
            eye.setStrokeWidth(Math.max(0.5, r * 0.05));
            final Circle pupil = new Circle(x + side * r * 0.32, y - r * 0.08, r * 0.14, Color.rgb(40, 30, 30));
            face.getChildren().addAll(eye, pupil);
        }
        final Arc smile = new Arc(x, y + r * 0.22, r * 0.3, r * 0.2, 200, 140);
        smile.setType(ArcType.OPEN);
        smile.setFill(null);
        smile.setStroke(Color.rgb(60, 40, 40));
        smile.setStrokeWidth(Math.max(0.8, r * 0.08));
        return new Group(wingLeft, wingRight, body, face, smile);
    }

    /**
     * @return a whole cake: its layers stacked from (0, 0), the bottom of the cake, and its decoration on top
     */
    private Group cakePicture(final List<Layer> parts, final int deco, final double width0, final int highlight) {
        final Group group = new Group();
        double bottom = 0;
        double topWidth = width0;
        for (int i = 0; i < parts.size(); i++) {
            final double w = width0 * (1 - 0.18 * i);
            final double h = w * 0.34;
            final Group picture = layerPicture(parts.get(i), w, 11 + i * 7L + Math.max(0, parts.get(i).sweets));
            picture.setLayoutY(bottom - h);
            group.getChildren().add(picture);
            if (i == highlight) {
                final Rectangle frame = new Rectangle(-w / 2 - w * 0.06, bottom - h - w * 0.2, w * 1.12, h + w * 0.34);
                frame.setArcWidth(w * 0.12);
                frame.setArcHeight(w * 0.12);
                frame.setFill(Color.rgb(230, 50, 60, 0.12));
                frame.setStroke(Color.rgb(230, 50, 60));
                frame.setStrokeWidth(Math.max(3, w * 0.025));
                group.getChildren().add(frame);
            }
            bottom -= h;
            topWidth = w;
        }
        if (deco >= 0) {
            group.getChildren().add(decorationPicture(deco, topWidth, bottom));
        }
        return group;
    }

    private void drawCake() {
        final Group cake = cakePicture(layers, decoration, baseWidth, -1);
        cake.setLayoutX(cakeX);
        cake.setLayoutY(plateY);
        cakeGroup.getChildren().setAll(cake);
    }

    /**
     * The card of the model at the right, its cake drawn at a smaller scale.
     */
    private void buildModelCard() {
        modelCard.getChildren().clear();
        final double cardWidth = width * 0.3;
        final double cardHeight = height * 0.62;
        final double left = width * 0.65;
        final double top = height * 0.12;
        final Rectangle card = GameStyle.targetCard(left, top, cardWidth, cardHeight);
        final Group title = GameStyle.pill(gameContext.getTranslator().translate("CakesModel"), height * 0.03);
        title.setLayoutX(left + (cardWidth - title.getBoundsInLocal().getWidth()) / 2);
        title.setLayoutY(top + height * 0.02);
        modelGroup = new Group();
        modelHidden = new Group();
        final Text mark = new Text("?");
        final double size = cardHeight * 0.4;
        mark.setFont(GameStyle.titleFont(size));
        mark.setStyle(GameStyle.titleFontStyle(size) + " -fx-fill: -gp-accent;");
        mark.setX(left + (cardWidth - mark.getLayoutBounds().getWidth()) / 2 - mark.getLayoutBounds().getMinX());
        mark.setY(top + (cardHeight - mark.getLayoutBounds().getHeight()) / 2 - mark.getLayoutBounds().getMinY());
        modelHidden.getChildren().add(mark);
        modelHidden.setVisible(false);
        modelCard.getChildren().addAll(card, title, modelGroup, modelHidden);
        modelCard.setMouseTransparent(true);
        drawModel(-1);
    }

    private void drawModel(final int highlight) {
        final double cardWidth = width * 0.3;
        final double w = Math.min(cardWidth * 0.62, baseWidth * 0.7);
        final Group cake = cakePicture(model, modelDecoration, w, highlight);
        cake.setLayoutX(width * 0.65 + cardWidth / 2);
        cake.setLayoutY(height * 0.12 + height * 0.62 * 0.86);
        modelGroup.getChildren().setAll(cake);
    }

    private boolean isModelShown() {
        return !level.isHidden() || modelTime < MODEL_SHOWN || modelAgain > 0 || blinkLeft > 0;
    }

    // --- The game ---

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        phaseTime += seconds;
        if (mode == CakesMode.COPY) {
            if (phase != Phase.WAIT) {
                modelTime += seconds;
            }
            modelAgain = Math.max(0, modelAgain - seconds);
            if (blinkLeft > 0) {
                blinkLeft = Math.max(0, blinkLeft - seconds);
                modelGroup.setOpacity(blinkLeft == 0 || ((int) (blinkLeft * 6)) % 2 == 0 ? 1 : 0.35);
                if (blinkLeft == 0) {
                    drawModel(-1);
                }
            }
            final boolean shown = isModelShown();
            modelGroup.setVisible(shown);
            modelHidden.setVisible(!shown);
        }
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    phase = Phase.CHOOSE;
                    phaseTime = 0;
                }
            }
            case CHOOSE -> fixate(seconds);
            case ARRIVE -> {
                final double u = Math.min(1, phaseTime / ARRIVE);
                final Node top = ((Group) cakeGroup.getChildren().get(0)).getChildren().get(layers.size() - 1);
                top.setTranslateY(-height * 0.25 * (1 - u) * (1 - u));
                if (u >= 1) {
                    phase = Phase.CHOOSE;
                    phaseTime = 0;
                }
            }
            case REWARD -> {
                if (!rewarded) {
                    rewarded = true;
                    showReward();
                }
                final double glow = 1 + 0.04 * Math.sin(phaseTime * 8) * Math.max(0, 1 - phaseTime / REWARD);
                cakeGroup.setScaleX(glow);
                cakeGroup.setScaleY(glow);
                if (phaseTime >= REWARD) {
                    cakeGroup.setScaleX(1);
                    cakeGroup.setScaleY(1);
                    if (end.getCakes() > 0 && cakes >= end.getCakes()) {
                        finish();
                    } else {
                        newCake();
                        phase = Phase.CHOOSE;
                        phaseTime = 0;
                    }
                }
            }
            default -> {
            }
        }
    }

    private void fixate(final double seconds) {
        if (looked == null || Stats.isConfigMenuOpen()) {
            return;
        }
        if (!ring.isVisible()) {
            ring.setSize(buttonRadius() * 2.5);
            ring.show(looked.x, looked.y);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Choice choice = looked;
            ring.stop();
            looked = null;
            progress = 0;
            choose(choice);
        }
    }

    void lookAt(final Choice choice) {
        if (looked == choice) {
            return;
        }
        looked = choice;
        progress = 0;
        ring.stop();
    }

    void lookAway(final Choice choice) {
        if (looked == choice) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * A button chosen: a part put on the cake (checked against the model), the next layer or the decoration, or the
     * model shown again.
     */
    void choose(final Choice choice) {
        if (phase != Phase.CHOOSE) {
            return;
        }
        if (choice == showModel) {
            modelAgain = MODEL_AGAIN;
            return;
        }
        if (choice.step == Step.NEXT) {
            setStep(choice.value == 0 ? Step.ACCESSORY : Step.SWEETS);
            return;
        }
        final Layer current = currentLayer();
        switch (choice.step) {
            case SPONGE -> current.sponge = choice.value;
            case CREAM -> current.cream = choice.value;
            case SWEETS -> current.sweets = choice.value;
            case ACCESSORY -> current.accessory = choice.value;
            case DECORATION -> decoration = choice.value;
            default -> {
            }
        }
        gameContext.getSoundManager().add(SOUNDS + (choice.step == Step.CREAM ? "cream.wav" : "plop.wav"));
        drawCake();
        if (mode == CakesMode.COPY && !matches(choice)) {
            stats.incrementNumberOfMistakes();
            gameContext.getSoundManager().add("data/dice/sounds/wrong.wav");
            blinkLeft = BLINK;
            drawModel(choice.step == Step.DECORATION ? model.size() - 1 : layers.size() - 1);
            return;
        }
        nextStep();
    }

    private boolean matches(final Choice choice) {
        if (choice.step == Step.DECORATION) {
            return choice.value == modelDecoration;
        }
        return model.get(layers.size() - 1).part(choice.step) == choice.value;
    }

    /**
     * After a part: the next part of the layer, then the next layer, the choice of the free mode, the decoration, or the
     * cake finished.
     */
    private void nextStep() {
        switch (step) {
            case SPONGE -> setStep(Step.CREAM);
            case CREAM -> {
                if (isLastLayer()) {
                    setStep(Step.SWEETS);
                } else if (mode == CakesMode.FREE) {
                    setStep(Step.NEXT);
                } else {
                    setStep(Step.ACCESSORY);
                }
            }
            case ACCESSORY -> addLayer();
            case SWEETS -> setStep(Step.DECORATION);
            case DECORATION -> cakeDone();
            default -> {
            }
        }
    }

    private void addLayer() {
        layers.add(new Layer());
        drawCake();
        gameContext.getSoundManager().add(SOUNDS + "plop.wav");
        setStep(Step.SPONGE);
        phase = Phase.ARRIVE;
        phaseTime = 0;
        final Node top = ((Group) cakeGroup.getChildren().get(0)).getChildren().get(layers.size() - 1);
        top.setTranslateY(-height * 0.25);
    }

    private void cakeDone() {
        clearButtons();
        cakes++;
        stats.incrementNumberOfGoalsReached();
        gameContext.getSoundManager().add("data/goosegame/sounds/win.wav");
        phase = Phase.REWARD;
        phaseTime = 0;
        rewarded = false;
        if (mode == CakesMode.COPY) {
            modelAgain = REWARD;
        }
        showScore();
        tabs.getChildren().clear();
    }

    /**
     * A star beside "Bravo !", under the cake.
     */
    private void showReward() {
        final double r = height * 0.045;
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("CakesWell"), height * 0.04);
        final double pillWidth = pill.getBoundsInLocal().getWidth();
        final double pillHeight = pill.getBoundsInLocal().getHeight();
        final double gap = r * 0.4;
        final double left = cakeX - (2 * r + gap + pillWidth) / 2;
        final double cy = height - height * 0.13;
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

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    boolean isModelVisible() {
        return modelGroup != null && modelGroup.isVisible();
    }

    Choice getShowModel() {
        return showModel;
    }

    /**
     * @return the button of a value of the step, for the tests
     */
    Choice choice(final int value) {
        return choices.stream().filter(c -> c != showModel && c.value == value).findFirst().orElseThrow();
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (buttonsLayer != null) {
            clearButtons();
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A round button of a choice: a picture of the result (or an icon), and a label for the buttons of the free mode.
     */
    final class Choice {
        @Getter
        private final Step step;
        @Getter
        private final int value;
        private final Node picture;
        private final String label;
        private final Circle disc;
        private double x;
        private double y;

        Choice(final Step step, final int value, final Node picture, final String label) {
            this.step = step;
            this.value = value;
            this.picture = picture;
            this.label = label;
            disc = new Circle(buttonRadius());
            disc.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        }

        void place(final double cx, final double cy) {
            x = cx;
            y = cy;
            final Group node = new Group(disc);
            if (picture != null) {
                final Group holder = new Group(picture);
                holder.setMouseTransparent(true);
                if (step != null && step != Step.NEXT) {
                    // A layer is drawn from the center of its top: centered in the button
                    final var bounds = picture.getBoundsInParent();
                    holder.setLayoutX(-(bounds.getMinX() + bounds.getMaxX()) / 2);
                    holder.setLayoutY(-(bounds.getMinY() + bounds.getMaxY()) / 2);
                }
                node.getChildren().add(holder);
            }
            if (label != null) {
                final Group pill = GameStyle.pill(gameContext.getTranslator().translate(label), height * 0.024);
                pill.setLayoutX(-pill.getBoundsInLocal().getWidth() / 2);
                pill.setLayoutY(-buttonRadius() - pill.getBoundsInLocal().getHeight() - height * 0.01);
                pill.setMouseTransparent(true);
                node.getChildren().add(pill);
            }
            node.setLayoutX(cx);
            node.setLayoutY(cy);
            final EventHandler<Event> handler = event -> {
                final var type = event.getEventType();
                if (type == MouseEvent.MOUSE_ENTERED || type == GazeEvent.GAZE_ENTERED) {
                    lookAt(this);
                } else if (type == MouseEvent.MOUSE_EXITED || type == GazeEvent.GAZE_EXITED) {
                    lookAway(this);
                }
            };
            disc.addEventFilter(MouseEvent.ANY, handler);
            disc.addEventFilter(GazeEvent.ANY, handler);
            gameContext.getGazeDeviceManager().addEventFilter(disc);
            buttonsLayer.getChildren().add(node);
        }
    }
}
