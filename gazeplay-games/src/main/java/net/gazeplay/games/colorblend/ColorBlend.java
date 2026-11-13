package net.gazeplay.games.colorblend;

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
import net.gazeplay.games.colorblend.PaintMix.Pot;
import net.gazeplay.games.colorblend.PaintMix.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * "Mélanger les couleurs" (name code colorblend; reviewed on 13/11/2026, maintainer's rules): pots of paint poured in a
 * big pot, mixed as a painter does (see {@link PaintMix}), everything drawn by the game.
 * <p>
 * The challenge: a color to make is shown on a card; the pots of its recipe are poured one by one, in any order (two in
 * the easy and normal levels, three in the hard one), and the mix is checked after the last one. Right: "Bravo !" and
 * the next color (never the same twice in a row). Wrong: the pot shows the color obtained and its name when it has
 * one, a mistake is counted (no lives), the pot is emptied after 2 s and the same color is asked again; after two
 * mistakes on a color, the pots of its recipe are framed. The game ends after 5, 10 or 20 colors ("Bravo", then the
 * statistics), or has no end. The free mode: as many pots as wanted, the mix at each pot, its name when it is a known
 * color, a button to empty the pot; no end.
 * <p>
 * One loop, in real seconds: the fixation lasts the length of the settings (read at each step), nothing can be chosen
 * before the delay of the settings, while a pot is poured, nor while the menu of the game is open. (Before the review:
 * the red, green and blue of the screen averaged, so that yellow and blue made khaki; a click chose at once; sizes taken
 * from the whole screen; one progress indicator for ten colors, beside them; nothing removed at the end; a park and a
 * glass of unknown source.)
 */
@Slf4j
public class ColorBlend implements GameLifeCycle {

    /**
     * The seconds a drop falls, the paint turns, a wrong mix is shown, and the reward lasts.
     */
    static final double DROP = 0.35;
    static final double SWIRL = 0.45;
    static final double POUR = DROP + SWIRL;
    static final double RESULT = 2;
    static final double REWARD = 2;
    /**
     * Mistakes on a color after which the pots of its recipe are framed.
     */
    static final int HINT_AFTER = 2;
    /**
     * The most pots in a mix of the free mode.
     */
    static final int MOST_POTS = 20;

    private static final Color EMPTY = Color.rgb(236, 232, 226);
    private static final String SOUNDS = "data/colorblend/sounds/";

    enum Phase { WAIT, CHOOSE, POUR, RESULT, REWARD, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final ColorBlendMode mode;
    @Getter
    private final ColorBlendLevel level;
    @Getter
    private final ColorBlendEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private Target target;
    @Getter
    private final List<Pot> mix = new ArrayList<>();
    @Getter
    private int done;
    @Getter
    private int mistakesOnTarget;
    private double elapsed;
    private double phaseTime;
    private boolean rewarded;
    private Color shownColor = EMPTY;
    private Color pouredColor = EMPTY;
    private Choice poured;

    private double width;
    private double height;
    private double bowlX;
    private double rimY;
    private double bowlWidth;

    private Group layer;
    private Ellipse paint;
    private Group swirl;
    private Arc swirlArc;
    private Circle drop;
    private Group card;
    private Group buttonsLayer;
    private Group hud;
    private Group message;
    private FixationRing ring;
    @Getter
    private final List<Choice> choices = new ArrayList<>();
    @Getter
    private Choice looked;
    @Getter
    private double progress;
    private Choice empty;

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
    ColorBlend(final IGameContext gameContext, final Stats stats, final ColorBlendMode mode,
               final ColorBlendLevel level, final ColorBlendEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.level = mode == ColorBlendMode.FREE ? ColorBlendLevel.FREE
            : level == ColorBlendLevel.FREE ? ColorBlendLevel.EASY : level;
        this.end = mode == ColorBlendMode.FREE ? ColorBlendEnd.ENDLESS : end;
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
        bowlX = mode == ColorBlendMode.CHALLENGE ? width * 0.6 : width * 0.5;
        bowlWidth = Math.min(width * 0.3, height * 0.42);
        rimY = height * 0.42;

        final Rectangle room = new Rectangle(0, 0, width, height);
        room.setStyle("-fx-fill: -gp-bg;");
        room.setMouseTransparent(true);
        // A soft table under the pot
        final double tableY = rimY + bowlWidth * 0.55 + bowlWidth * 0.1;
        final Rectangle table = new Rectangle(0, tableY, width, height - tableY);
        table.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(236, 214, 186, 0.75)), new Stop(1, Color.rgb(222, 196, 164, 0.75))));
        table.setMouseTransparent(true);

        final Group bowl = bowl();
        card = new Group();
        card.setMouseTransparent(true);
        buttonsLayer = new Group();
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        drop = new Circle(bowlWidth * 0.045);
        drop.setVisible(false);
        drop.setMouseTransparent(true);
        layer = new Group(room, table, bowl, card, buttonsLayer, drop, ring, hud, message);
        gameContext.getChildren().add(layer);
        gameContext.getGazeDeviceManager().addStats(stats);
        done = 0;
        showPots();
        if (mode == ColorBlendMode.CHALLENGE) {
            newTarget();
        } else {
            stats.notifyNewRoundReady();
            showHud();
        }
        phase = Phase.WAIT;
        elapsed = 0;
        if (!manual) {
            loop.start();
        }
    }

    // --- The drawings ---

    /**
     * The big pot: a white jar seen a little from above, its paint at the top.
     */
    private Group bowl() {
        final double w = bowlWidth;
        final double h = w * 0.55;
        final double ry = w * 0.16;
        final Color ceramic = Color.rgb(250, 248, 244);
        final Color edge = Color.rgb(196, 186, 174);
        final double stroke = Math.max(2, w * 0.012);
        final Ellipse shadow = new Ellipse(bowlX, rimY + h + ry * 0.9, w * 0.56, ry * 0.7);
        shadow.setFill(Color.rgb(120, 90, 60, 0.18));
        final LinearGradient side = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(232, 228, 222)), new Stop(0.35, ceramic), new Stop(1, Color.rgb(226, 220, 212)));
        final Ellipse bottom = new Ellipse(bowlX, rimY + h, w / 2, ry);
        bottom.setFill(side);
        bottom.setStroke(edge);
        bottom.setStrokeWidth(stroke);
        final Rectangle body = new Rectangle(bowlX - w / 2, rimY, w, h);
        body.setFill(side);
        final Line left = new Line(bowlX - w / 2, rimY, bowlX - w / 2, rimY + h);
        final Line right = new Line(bowlX + w / 2, rimY, bowlX + w / 2, rimY + h);
        for (final Line line : List.of(left, right)) {
            line.setStroke(edge);
            line.setStrokeWidth(stroke);
        }
        final Rectangle band = new Rectangle(bowlX - w / 2, rimY + h * 0.45, w, h * 0.16);
        band.setStyle("-fx-fill: -gp-accent;");
        band.setOpacity(0.55);
        final Ellipse rim = new Ellipse(bowlX, rimY, w / 2, ry);
        rim.setFill(Color.rgb(240, 236, 230));
        rim.setStroke(edge);
        rim.setStrokeWidth(stroke);
        paint = new Ellipse(bowlX, rimY + ry * 0.08, w / 2 * 0.88, ry * 0.8);
        paint.setFill(EMPTY);
        swirlArc = new Arc(0, 0, w / 2 * 0.55, ry * 0.5, 0, 200);
        swirlArc.setType(ArcType.ROUND);
        swirl = new Group(swirlArc);
        swirl.setLayoutX(bowlX);
        swirl.setLayoutY(rimY + ry * 0.08);
        swirl.setVisible(false);
        final Ellipse gleam = new Ellipse(bowlX - w * 0.18, rimY - ry * 0.25, w * 0.12, ry * 0.18);
        gleam.setFill(Color.rgb(255, 255, 255, 0.4));
        final Group group = new Group(shadow, bottom, body, left, right, band, rim, paint, swirl, gleam);
        group.setMouseTransparent(true);
        return group;
    }

    /**
     * A pot of paint for a button: a little jar, its paint at the top and a drip down its side.
     */
    private Node potPicture(final Color color, final double size) {
        final double w = size;
        final double h = size * 0.8;
        final double ry = w * 0.18;
        final Color ceramic = Color.rgb(250, 248, 244);
        final Color edge = Color.rgb(170, 160, 150);
        final double stroke = Math.max(1.5, w * 0.03);
        final Ellipse bottom = new Ellipse(0, h, w / 2, ry);
        bottom.setFill(ceramic);
        bottom.setStroke(edge);
        bottom.setStrokeWidth(stroke);
        final Rectangle body = new Rectangle(-w / 2, 0, w, h);
        body.setFill(ceramic);
        final Line left = new Line(-w / 2, 0, -w / 2, h);
        final Line right = new Line(w / 2, 0, w / 2, h);
        for (final Line line : List.of(left, right)) {
            line.setStroke(edge);
            line.setStrokeWidth(stroke);
        }
        final Rectangle label = new Rectangle(-w / 2, h * 0.38, w, h * 0.34);
        label.setFill(color);
        // An outline: the white pot is seen on the white jar
        label.setStroke(edge);
        label.setStrokeWidth(stroke * 0.6);
        final Ellipse top = new Ellipse(0, 0, w / 2, ry);
        top.setFill(color);
        top.setStroke(edge);
        top.setStrokeWidth(stroke);
        final Rectangle drip = new Rectangle(w * 0.12, 0, w * 0.12, h * 0.42);
        drip.setArcWidth(w * 0.12);
        drip.setArcHeight(w * 0.12);
        drip.setFill(color);
        final Group group = new Group(bottom, body, left, right, label, drip, top);
        group.setLayoutY(-h / 2);
        return new Group(group);
    }

    /**
     * A splash of paint: a disc with round drops around it.
     */
    private Group splash(final Color color, final double r) {
        final Group group = new Group();
        final double[][] drops = {{0.95, -0.35, 0.28}, {-0.9, 0.45, 0.24}, {0.55, 0.85, 0.2}, {-0.6, -0.8, 0.18},
            {1.15, 0.4, 0.13}, {-1.15, -0.1, 0.12}};
        for (final double[] d : drops) {
            group.getChildren().add(new Circle(d[0] * r, d[1] * r, d[2] * r, color));
        }
        final Circle main = new Circle(0, 0, r, color);
        main.setStroke(color.darker());
        main.setStrokeWidth(Math.max(2, r * 0.03));
        group.getChildren().add(main);
        return group;
    }

    private double buttonRadius() {
        return Math.min(height * 0.075, width * 0.045);
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

    /**
     * The pots of the level in a row under the big pot, and the button to empty it in the free mode.
     */
    private void showPots() {
        clearButtons();
        final List<Choice> row = new ArrayList<>();
        for (final Pot pot : level.getPots()) {
            row.add(new Choice(pot, potPicture(PaintMix.color(pot), buttonRadius() * 1.1), null));
        }
        final double gap = buttonRadius() * 0.6;
        final double rowWidth = row.size() * 2 * buttonRadius() + (row.size() - 1) * gap;
        final double left = bowlX - rowWidth / 2;
        final double y = height - buttonRadius() - height * 0.05;
        for (int i = 0; i < row.size(); i++) {
            row.get(i).place(left + buttonRadius() + i * (2 * buttonRadius() + gap), y);
        }
        choices.addAll(row);
        if (mode == ColorBlendMode.FREE) {
            empty = new Choice(null, icon("restart_alt"), "ColorBlendEmpty");
            empty.place(left + rowWidth + gap * 2.5 + buttonRadius(), y);
            choices.add(empty);
        }
        updateMarks();
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
        empty = null;
    }

    /**
     * The number of times each pot is in the mix, and the frames of the hint.
     */
    private void updateMarks() {
        final boolean hint = mode == ColorBlendMode.CHALLENGE && target != null && mistakesOnTarget >= HINT_AFTER;
        for (final Choice choice : choices) {
            if (choice.pot == null) {
                continue;
            }
            final long count = mix.stream().filter(p -> p == choice.pot).count();
            choice.setCount((int) count);
            choice.setHinted(hint && target.getRecipe().contains(choice.pot));
        }
    }

    private void showHud() {
        final double size = height * 0.032;
        final String pots = gameContext.getTranslator().translate("ColorBlendPots") + " " + mix.size()
            + (mode == ColorBlendMode.CHALLENGE && target != null ? " / " + target.getRecipe().size() : "");
        final Group left = GameStyle.pill(pots, size);
        left.setLayoutX(height * 0.03);
        left.setLayoutY(height * 0.025);
        hud.getChildren().setAll(left);
        if (mode == ColorBlendMode.CHALLENGE) {
            final String text = gameContext.getTranslator().translate("ColorBlendDone") + " " + done
                + (end.getColors() > 0 ? " / " + end.getColors() : "");
            final Group score = GameStyle.pill(text, size);
            score.setLayoutX(width - score.getBoundsInLocal().getWidth() - height * 0.03);
            score.setLayoutY(height * 0.025);
            hud.getChildren().add(score);
        }
    }

    /**
     * The card of the color to make, at the left: a splash of it and its name.
     */
    private void showCard() {
        card.getChildren().clear();
        final double cardWidth = width * 0.3;
        final double cardHeight = height * 0.6;
        final double left = width * 0.06;
        final double top = height * 0.14;
        final Rectangle frame = GameStyle.targetCard(left, top, cardWidth, cardHeight);
        final Group title = GameStyle.pill(gameContext.getTranslator().translate("ColorBlendMake"), height * 0.03);
        title.setLayoutX(left + (cardWidth - title.getBoundsInLocal().getWidth()) / 2);
        title.setLayoutY(top + height * 0.025);
        final double r = Math.min(cardWidth, cardHeight) * 0.24;
        final Group blot = splash(target.color(), r);
        blot.setLayoutX(left + cardWidth / 2);
        blot.setLayoutY(top + cardHeight * 0.52);
        final Group name = GameStyle.pill(gameContext.getTranslator().translate(target.getKey()), height * 0.036);
        name.setLayoutX(left + (cardWidth - name.getBoundsInLocal().getWidth()) / 2);
        name.setLayoutY(top + cardHeight - name.getBoundsInLocal().getHeight() - height * 0.03);
        card.getChildren().addAll(frame, title, blot, name);
    }

    /**
     * The name of the mix on the band of the big pot, and a word above it ("Pas encore !", "Bravo !" with a star), or
     * nothing.
     */
    private void showMessage(final String nameKey, final String wordKey, final boolean star) {
        message.getChildren().clear();
        if (nameKey != null) {
            final Group name = GameStyle.pill(gameContext.getTranslator().translate(nameKey), height * 0.034);
            name.setLayoutX(bowlX - name.getBoundsInLocal().getWidth() / 2);
            name.setLayoutY(rimY + bowlWidth * 0.55 * 0.53 - name.getBoundsInLocal().getHeight() / 2);
            message.getChildren().add(name);
        }
        if (wordKey != null) {
            final Group word = GameStyle.pill(gameContext.getTranslator().translate(wordKey), height * 0.04);
            final double wordWidth = word.getBoundsInLocal().getWidth();
            final double wordHeight = word.getBoundsInLocal().getHeight();
            final double r = height * 0.04;
            final double gap = r * 0.4;
            final double total = wordWidth + (star ? 2 * r + gap : 0);
            final double left = bowlX - total / 2;
            final double cy = rimY - bowlWidth * 0.16 - height * 0.07;
            if (star) {
                final Polygon shape = new Polygon();
                for (int i = 0; i < 10; i++) {
                    final double a = -Math.PI / 2 + i * Math.PI / 5;
                    final double d = i % 2 == 0 ? r : r * 0.45;
                    shape.getPoints().addAll(left + r + d * Math.cos(a), cy + r * 0.08 + d * Math.sin(a));
                }
                shape.setFill(Color.rgb(255, 200, 40));
                shape.setStroke(Color.rgb(200, 120, 0));
                shape.setStrokeWidth(Math.max(2, r * 0.07));
                message.getChildren().add(shape);
            }
            word.setLayoutX(left + total - wordWidth);
            word.setLayoutY(cy - wordHeight / 2);
            message.getChildren().add(word);
        }
    }

    // --- The game ---

    /**
     * A new color to make, never the one just made.
     */
    private void newTarget() {
        final List<Target> targets = level.getTargets();
        Target next;
        do {
            next = targets.get(random.nextInt(targets.size()));
        } while (next == target && targets.size() > 1);
        target = next;
        mistakesOnTarget = 0;
        emptyPot();
        showCard();
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        showHud();
    }

    private void emptyPot() {
        mix.clear();
        shownColor = EMPTY;
        paint.setFill(EMPTY);
        swirl.setVisible(false);
        message.getChildren().clear();
        updateMarks();
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
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    setPhase(Phase.CHOOSE);
                }
            }
            case CHOOSE -> fixate(seconds);
            case POUR -> pour();
            case RESULT -> {
                if (phaseTime >= RESULT) {
                    emptyPot();
                    setPhase(Phase.CHOOSE);
                }
            }
            case REWARD -> {
                if (!rewarded) {
                    rewarded = true;
                    showMessage(target.getKey(), "ColorBlendWell", true);
                }
                if (phaseTime >= REWARD) {
                    if (end.getColors() > 0 && done >= end.getColors()) {
                        finish();
                    } else {
                        newTarget();
                        setPhase(Phase.CHOOSE);
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
     * A drop falls from the pot chosen into the big pot, then the paint turns into the new mix.
     */
    private void pour() {
        if (phaseTime < DROP) {
            final double u = phaseTime / DROP;
            final double x = poured.x + (bowlX - poured.x) * u;
            final double top = rimY - bowlWidth * 0.3;
            final double y = (1 - u) * (1 - u) * poured.y + 2 * (1 - u) * u * top + u * u * rimY;
            drop.setCenterX(x);
            drop.setCenterY(y);
            drop.setVisible(true);
            return;
        }
        drop.setVisible(false);
        final double u = Math.min(1, (phaseTime - DROP) / SWIRL);
        swirl.setVisible(u < 1);
        swirlArc.setFill(PaintMix.color(poured.pot));
        swirlArc.setStartAngle(-u * 540);
        swirlArc.setOpacity(1 - u);
        paint.setFill(shownColor.interpolate(pouredColor, u));
        if (u >= 1) {
            shownColor = pouredColor;
            paint.setFill(shownColor);
            poured();
        }
    }

    /**
     * After a pot is poured: the name of the mix in the free mode; in the challenge, the next pot, or the mix checked.
     */
    private void poured() {
        if (mode == ColorBlendMode.FREE) {
            showMessage(PaintMix.name(mix), null, false);
            setPhase(Phase.CHOOSE);
            return;
        }
        if (mix.size() < target.getRecipe().size()) {
            setPhase(Phase.CHOOSE);
            return;
        }
        if (PaintMix.sameRecipe(mix, target.getRecipe())) {
            done++;
            stats.incrementNumberOfGoalsReached();
            gameContext.getSoundManager().add("data/goosegame/sounds/win.wav");
            rewarded = false;
            showHud();
            setPhase(Phase.REWARD);
        } else {
            mistakesOnTarget++;
            stats.incrementNumberOfMistakes();
            gameContext.getSoundManager().add("data/dice/sounds/wrong.wav");
            showMessage(PaintMix.name(mix), "ColorBlendNotYet", false);
            setPhase(Phase.RESULT);
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
     * A button chosen: a pot poured, or the big pot emptied (free mode).
     */
    void choose(final Choice choice) {
        if (phase != Phase.CHOOSE) {
            return;
        }
        if (choice == empty) {
            emptyPot();
            return;
        }
        if (mix.size() >= MOST_POTS) {
            return;
        }
        mix.add(choice.pot);
        poured = choice;
        pouredColor = PaintMix.mix(mix);
        drop.setFill(PaintMix.color(choice.pot));
        gameContext.getSoundManager().add(SOUNDS + "drop.wav");
        message.getChildren().clear();
        updateMarks();
        showHud();
        setPhase(Phase.POUR);
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
     * @return the color in the big pot
     */
    Color getPotColor() {
        return (Color) paint.getFill();
    }

    /**
     * @return the button of a pot, for the tests
     */
    Choice choice(final Pot pot) {
        return choices.stream().filter(c -> c.pot == pot).findFirst().orElseThrow();
    }

    Choice getEmpty() {
        return empty;
    }

    /**
     * @return the names shown under the big pot and above it, for the tests
     */
    List<String> getMessages() {
        final List<String> texts = new ArrayList<>();
        for (final Node node : message.getChildren()) {
            if (node instanceof Group group) {
                for (final Node child : group.getChildren()) {
                    if (child instanceof Text text) {
                        texts.add(text.getText());
                    }
                }
            }
        }
        return texts;
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
     * A round button: a pot of paint, or the button to empty the big pot (with its label above), with the number of
     * times the pot is in the mix and the frame of the hint.
     */
    final class Choice {
        @Getter
        private final Pot pot;
        private final Node picture;
        private final String label;
        private final Circle disc;
        private final Circle hint;
        private final Group badge;
        private final Text badgeText;
        private double x;
        private double y;
        @Getter
        private int count;
        @Getter
        private boolean hinted;

        Choice(final Pot pot, final Node picture, final String label) {
            this.pot = pot;
            this.picture = picture;
            this.label = label;
            disc = new Circle(buttonRadius());
            disc.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
            hint = new Circle(buttonRadius() * 1.14);
            hint.setFill(null);
            hint.setStyle("-fx-stroke: -gp-accent;");
            hint.setStrokeWidth(Math.max(3, buttonRadius() * 0.08));
            hint.getStrokeDashArray().setAll(buttonRadius() * 0.18, buttonRadius() * 0.12);
            hint.setVisible(false);
            hint.setMouseTransparent(true);
            final double r = buttonRadius() * 0.3;
            final Circle dot = new Circle(0, 0, r);
            dot.setStyle("-fx-fill: -gp-accent;");
            badgeText = new Text();
            badgeText.setFont(GameStyle.titleFont(r * 1.2));
            badgeText.setStyle(GameStyle.titleFontStyle(r * 1.2) + " -fx-fill: -gp-on-accent;");
            badge = new Group(dot, badgeText);
            badge.setLayoutX(buttonRadius() * 0.72);
            badge.setLayoutY(-buttonRadius() * 0.72);
            badge.setVisible(false);
            badge.setMouseTransparent(true);
        }

        void setCount(final int count) {
            this.count = count;
            badge.setVisible(count > 0);
            badgeText.setText(Integer.toString(count));
            badgeText.setX(-badgeText.getLayoutBounds().getWidth() / 2 - badgeText.getLayoutBounds().getMinX());
            badgeText.setY(-badgeText.getLayoutBounds().getHeight() / 2 - badgeText.getLayoutBounds().getMinY());
        }

        void setHinted(final boolean hinted) {
            this.hinted = hinted;
            hint.setVisible(hinted);
        }

        void place(final double cx, final double cy) {
            x = cx;
            y = cy;
            final Group node = new Group(hint, disc);
            if (picture != null) {
                final Group holder = new Group(picture);
                holder.setMouseTransparent(true);
                if (pot != null) {
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
            node.getChildren().add(badge);
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
