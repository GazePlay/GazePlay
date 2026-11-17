package net.gazeplay.ui.scenes.stats;

import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import lombok.AccessLevel;
import lombok.Getter;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.FixationSequence;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static net.gazeplay.ui.scenes.stats.StatsScreenParts.text;

/**
 * The scan path screen, opened from the statistics (refonte of 17/11/2026, maquette B chosen by the maintainer): the
 * fixations of the gaze (or the mouse, or both) on the white game screen, as numbered circles, larger when the
 * fixation lasted longer, joined in their order. At the start all of them are shown; the timeline at the bottom, the
 * buttons previous / play / next, or a click on the timeline go through them one by one, the last 3 staying visible.
 * Hovering a circle shows its moment and its duration. Nothing shown here is written to disk.
 */
public class ScanpathView extends GraphicalContext<BorderPane> {

    /**
     * The sources that can be shown, as the images of the statistics screen.
     */
    enum Shown {
        GAZE("Gaze"), MOUSE("Mouse"), BOTH("MouseAndGaze");

        final String key;

        Shown(final String key) {
            this.key = key;
        }
    }

    /**
     * How many fixations stay visible when going through them one by one.
     */
    static final int TRAIL = 3;

    private static final Color GAZE_COLOR = Color.rgb(47, 95, 196);

    private static final Color MOUSE_COLOR = Color.rgb(21, 151, 139);

    private static final Color CURRENT_COLOR = Color.rgb(242, 100, 58);

    /**
     * A fixation to draw: where (in the pixels of the game screen), when (milliseconds from the start of the game),
     * how long, and of which source.
     */
    record Fixation(double x, double y, long time, long duration, boolean gaze) {
    }

    private final Stats stats;

    private final Translator translator;

    private final Locale locale;

    /**
     * The fixations of the source shown, in the order of their time.
     */
    @Getter(AccessLevel.PACKAGE)
    private List<Fixation> fixations = List.of();

    /**
     * The fixation shown when going one by one, or -1 when all of them are shown.
     */
    @Getter(AccessLevel.PACKAGE)
    private int current = -1;

    @Getter(AccessLevel.PACKAGE)
    private Shown shown;

    private final Pane screen = new Pane();

    private final Pane overlay = new Pane();

    private final Pane timeline = new Pane();

    private final Region progress = new Region();

    private final Region thumb = new Region();

    private Label emptyLabel;

    private Label timeLabel;

    @Getter(AccessLevel.PACKAGE)
    private Button playButton;

    @Getter(AccessLevel.PACKAGE)
    private Button previousButton;

    @Getter(AccessLevel.PACKAGE)
    private Button nextButton;

    @Getter(AccessLevel.PACKAGE)
    private ToggleButton showAllButton;

    @Getter(AccessLevel.PACKAGE)
    private Button backButton;

    private VBox bubble;

    private AnimationTimer player;

    private long length;

    public ScanpathView(GazePlay gazePlay, Stats stats) {
        super(gazePlay, new BorderPane());
        this.stats = stats;
        this.translator = gazePlay.getTranslator();
        this.locale = StatsFigures.javaLocale(translator.currentLocale());
        final Configuration config = ActiveConfigurationContext.getInstance();

        final List<Shown> choices = shownChoices(EyeTracker.fromConfigValue(config.getEyeTracker()));
        shown = choices.contains(Shown.GAZE) && !sequence(FixationSequence.GAZE_FIXATION_SEQUENCE).isEmpty()
            ? Shown.GAZE : choices.get(0);

        StatsScreenParts.style(root, locale);
        Node sourceSwitch = null;
        if (choices.size() > 1) {
            sourceSwitch = StatsScreenParts.sourceSwitch(choices.stream().map(c -> text(translator, c.key)).toList(),
                choices.indexOf(shown), index -> show(choices.get(index)));
        }
        root.setTop(StatsScreenParts.header(translator, locale, stats, config, "StatsScanPath", List.of(),
            sourceSwitch));
        final Node body = body(gazePlay.getCurrentScreenDimensionSupplier().get());
        BorderPane.setMargin(body, new Insets(16, 0, 16, 0));
        root.setCenter(body);
        root.setBottom(bar());
        show(shown);
    }

    /**
     * @return what can be shown for the inputs of the game: the mouse alone, the gaze alone, or the gaze, the mouse
     *   and both
     */
    static List<Shown> shownChoices(final EyeTracker inputs) {
        if (!inputs.usesGaze()) {
            return List.of(Shown.MOUSE);
        }
        if (!inputs.acceptsMouse()) {
            return List.of(Shown.GAZE);
        }
        return List.of(Shown.GAZE, Shown.MOUSE, Shown.BOTH);
    }

    private List<FixationPoint> sequence(final int index) {
        final List<? extends List<FixationPoint>> sequences = stats.getFixationSequence();
        return sequences == null || sequences.size() <= index ? List.of() : sequences.get(index);
    }

    /**
     * @return the fixations of a source; in memory, a fixation point holds the vertical coordinate in x and the
     *   horizontal one in y (FixationSequenceRecorder)
     */
    private List<Fixation> fixations(final int index, final boolean gaze) {
        final List<Fixation> list = new ArrayList<>();
        final long start = stats.getStartTime();
        for (final FixationPoint point : sequence(index)) {
            list.add(new Fixation(point.getY(), point.getX(), Math.max(0, point.getTimeGaze() - start),
                point.getGazeDuration(), gaze));
        }
        return list;
    }

    /**
     * Shows the fixations of a source, all of them.
     */
    void show(final Shown newShown) {
        stop();
        shown = newShown;
        final List<Fixation> list = new ArrayList<>();
        if (newShown != Shown.MOUSE) {
            list.addAll(fixations(FixationSequence.GAZE_FIXATION_SEQUENCE, true));
        }
        if (newShown != Shown.GAZE) {
            list.addAll(fixations(FixationSequence.MOUSE_FIXATION_SEQUENCE, false));
        }
        list.sort(Comparator.comparingLong(Fixation::time));
        fixations = List.copyOf(list);
        length = fixations.isEmpty() ? 0
            : fixations.get(fixations.size() - 1).time() + fixations.get(fixations.size() - 1).duration();
        if (emptyLabel != null) {
            emptyLabel.setVisible(fixations.isEmpty());
        }
        final boolean none = fixations.isEmpty();
        playButton.setDisable(none);
        previousButton.setDisable(none);
        nextButton.setDisable(none);
        showAllButton.setDisable(none);
        drawTimeline();
        showAll();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // The game view

    private Node body(final Dimension2D screenSize) {
        final StatsScreenParts.GameView view = new StatsScreenParts.GameView(screen, screenSize.getWidth(),
            screenSize.getHeight());
        overlay.setPickOnBounds(false);
        overlay.setMouseTransparent(true);
        overlay.setMinSize(0, 0);

        emptyLabel = new Label(text(translator, "AoiNoData"));
        emptyLabel.getStyleClass().add("gp-stats-empty");
        emptyLabel.setVisible(false);

        final Label trail = new Label(text(translator, "ScanLastThree"));
        trail.getStyleClass().addAll("gp-stats-legend", "gp-stats-overlay-pill");
        trail.setMouseTransparent(true);
        trail.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        trail.visibleProperty().bind(showAllButtonSelected().not());
        StackPane.setAlignment(trail, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(trail, new Insets(16));

        final StackPane stack = new StackPane(view, overlay, emptyLabel, trail);
        stack.setMinSize(0, 0);
        final VBox card = new VBox(stack);
        VBox.setVgrow(stack, Priority.ALWAYS);
        card.getStyleClass().addAll("gp-stats-card", "gp-stats-view-card");
        card.setMinHeight(0);
        return card;
    }

    private javafx.beans.property.BooleanProperty showAllButtonSelected() {
        if (showAllButton == null) {
            showAllButton = new ToggleButton(text(translator, "ScanShowAll"));
            showAllButton.getStyleClass().add("gp-stats-tab");
            showAllButton.setSelected(true);
        }
        return showAllButton.selectedProperty();
    }

    /**
     * Shows all the fixations, joined in their order.
     */
    void showAll() {
        current = -1;
        showAllButton.setSelected(true);
        draw(0, fixations.size() - 1, -1);
        updateTimeline();
    }

    /**
     * Shows one fixation and the ones just before it.
     */
    void showFixation(final int index) {
        if (fixations.isEmpty()) {
            return;
        }
        current = Math.max(0, Math.min(index, fixations.size() - 1));
        showAllButton.setSelected(false);
        draw(Math.max(0, current - TRAIL + 1), current, current);
        updateTimeline();
    }

    /**
     * Draws the fixations from {@code first} to {@code last}, the older ones paler, {@code highlighted} in the main
     * color with its bubble.
     */
    private void draw(final int first, final int last, final int highlighted) {
        hideBubble();
        screen.getChildren().clear();
        if (last < first) {
            return;
        }
        final double longest = Math.sqrt(fixations.stream().mapToLong(Fixation::duration).max().orElse(1));
        final Group lines = new Group();
        final Group circles = new Group();
        Fixation previousGaze = null;
        Fixation previousMouse = null;
        for (int i = first; i <= last; i++) {
            final Fixation fixation = fixations.get(i);
            final double fade = highlighted < 0 ? 1 : 0.35 + 0.65 * (i - first + 1) / (last - first + 1);
            final Color color = fixation.gaze() ? GAZE_COLOR : MOUSE_COLOR;
            final Fixation previous = fixation.gaze() ? previousGaze : previousMouse;
            if (previous != null) {
                final Line line = new Line(previous.x(), previous.y(), fixation.x(), fixation.y());
                line.setStroke(color.deriveColor(0, 1, 1, 0.45 * fade));
                line.setStrokeWidth(6);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
                line.setMouseTransparent(true);
                lines.getChildren().add(line);
            }
            if (fixation.gaze()) {
                previousGaze = fixation;
            } else {
                previousMouse = fixation;
            }
            circles.getChildren().add(circle(i, fixation, longest, i == highlighted ? CURRENT_COLOR : color, fade));
        }
        screen.getChildren().addAll(lines, circles);
        if (highlighted >= 0) {
            final Node target = circles.getChildren().get(circles.getChildren().size() - 1);
            // The bubble needs the place of the circle on the screen: after the layout of this pulse
            javafx.application.Platform.runLater(() -> {
                if (current == highlighted) {
                    showBubble(highlighted, target);
                }
            });
        }
    }

    /**
     * @return the circle of a fixation, its radius from 20 to 45 pixels of the game screen with its duration (as the
     *   former screen), its number inside
     */
    private Group circle(final int index, final Fixation fixation, final double longest, final Color color,
                         final double fade) {
        final double radius = (40d + 50d * (Math.sqrt(fixation.duration()) / Math.max(1, longest))) / 2;
        final Circle circle = new Circle(fixation.x(), fixation.y(), radius);
        circle.setFill(color.deriveColor(0, 1, 1, 0.25 * fade));
        circle.setStroke(color.deriveColor(0, 1, 1, fade));
        circle.setStrokeWidth(color == CURRENT_COLOR ? 5 : 3);

        final Text number = new Text(String.valueOf(index + 1));
        number.setFont(Font.font("Fredoka SemiBold", Math.max(16, radius * 0.7)));
        number.setFill(color.darker().deriveColor(0, 1, 1, fade));
        number.setBoundsType(TextBoundsType.VISUAL);
        number.setMouseTransparent(true);
        number.setX(fixation.x() - number.getLayoutBounds().getWidth() / 2);
        number.setY(fixation.y() + number.getLayoutBounds().getHeight() / 2);

        final Group group = new Group(circle, number);
        group.setOnMouseEntered(e -> showBubble(index, group));
        group.setOnMouseExited(e -> {
            if (current != index) {
                hideBubble();
            }
        });
        return group;
    }

    private void showBubble(final int index, final Node target) {
        hideBubble();
        final Fixation fixation = fixations.get(index);
        bubble = StatsScreenParts.bubble(text(translator, "ScanFixation")
                .replace("{0}", String.valueOf(index + 1)).replace("{1}", String.valueOf(fixations.size())),
            new String[][]{
                {text(translator, "ScanMoment"), StatsScreenParts.time(fixation.time(), locale)},
                {text(translator, "ScanDuration"), fixation.duration() + " ms"}
            });
        StatsScreenParts.placeBubble(overlay, bubble, target);
    }

    private void hideBubble() {
        if (bubble != null) {
            overlay.getChildren().remove(bubble);
            bubble = null;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // The timeline and its buttons

    private Node bar() {
        timeline.getStyleClass().add("gp-stats-timeline");
        timeline.setMinHeight(26);
        timeline.setPrefHeight(26);
        timeline.setMaxHeight(26);
        timeline.widthProperty().addListener((observable, before, now) -> {
            drawTimeline();
            updateTimeline();
        });
        timeline.setOnMouseClicked(e -> {
            if (!fixations.isEmpty() && timeline.getWidth() > 0) {
                stop();
                showFixation(fixationAt((long) (e.getX() / timeline.getWidth() * length)));
            }
        });
        progress.getStyleClass().add("gp-stats-timeline-progress");
        progress.setMouseTransparent(true);
        thumb.getStyleClass().add("gp-stats-timeline-thumb");
        thumb.setMouseTransparent(true);

        previousButton = MenuStyle.iconButton(MenuIcon.BACK, text(translator, "ScanPrevious"), () -> {
            stop();
            showFixation(current < 0 ? fixations.size() - 1 : current - 1);
        });
        playButton = StatsScreenParts.button(text(translator, "ScanPlay"), MenuIcon.PLAY, false, () -> {
            if (player != null) {
                stop();
            } else {
                play();
            }
        });
        nextButton = MenuStyle.iconButton(MenuIcon.FORWARD, text(translator, "ScanNext"), () -> {
            stop();
            showFixation(current < 0 ? 0 : current + 1);
        });
        timeLabel = new Label();
        timeLabel.getStyleClass().add("gp-stats-time");
        showAllButtonSelected();
        showAllButton.setOnAction(e -> {
            stop();
            showAll();
        });
        backButton = StatsScreenParts.button(text(translator, "StatsBackToStats"), MenuIcon.BACK, true, () -> {
            stop();
            final StatsContext statsContext = StatsContextFactory.newAdvancedInstance(getGazePlay(), stats);
            this.clear();
            getGazePlay().onDisplayStats(statsContext);
        });

        final Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        final HBox buttons = new HBox(10, previousButton, playButton, nextButton, timeLabel, gap, showAllButton,
            backButton);
        buttons.setAlignment(Pos.CENTER_LEFT);
        final VBox bar = new VBox(10, timeline, buttons);
        bar.getStyleClass().addAll("gp-stats-card", "gp-stats-timeline-card");
        return bar;
    }

    /**
     * @return the last fixation started at a moment of the game, or the first one
     */
    int fixationAt(final long time) {
        int found = 0;
        for (int i = 0; i < fixations.size(); i++) {
            if (fixations.get(i).time() <= time) {
                found = i;
            }
        }
        return found;
    }

    /**
     * Draws a mark for each fixation on the timeline, at its moment, as wide as its duration (at least 3 pixels).
     */
    private void drawTimeline() {
        timeline.getChildren().clear();
        final Region track = new Region();
        track.getStyleClass().add("gp-stats-timeline-track");
        track.setMouseTransparent(true);
        timeline.getChildren().addAll(track, progress);
        final double width = timeline.getWidth();
        track.resizeRelocate(0, 10, width, 6);
        if (length > 0 && width > 0) {
            for (final Fixation fixation : fixations) {
                final Region mark = new Region();
                mark.getStyleClass().add(fixation.gaze() ? "gp-stats-mark-gaze" : "gp-stats-mark-mouse");
                mark.setMouseTransparent(true);
                mark.resizeRelocate(fixation.time() * width / length, 7,
                    Math.max(3, fixation.duration() * width / length), 12);
                timeline.getChildren().add(mark);
            }
        }
        timeline.getChildren().add(thumb);
    }

    private void updateTimeline() {
        final double width = timeline.getWidth();
        final long time = current < 0 ? length : fixations.get(current).time();
        final double x = length > 0 ? time * width / length : 0;
        progress.resizeRelocate(0, 10, x, 6);
        thumb.setVisible(current >= 0);
        thumb.resizeRelocate(x - 11, 2, 22, 22);
        timeLabel.setText(StatsScreenParts.time(current < 0 ? 0 : time, locale) + " / "
            + StatsScreenParts.time(length, locale));
    }

    /**
     * Goes through the fixations at the speed of the game, from the one shown (or the first one).
     */
    void play() {
        if (fixations.isEmpty()) {
            return;
        }
        final int from = current < 0 || current >= fixations.size() - 1 ? 0 : current;
        showFixation(from);
        final long fromTime = fixations.get(from).time();
        playButton.setText(text(translator, "ScanPause"));
        player = new AnimationTimer() {
            private long start = -1;

            @Override
            public void handle(final long now) {
                if (start < 0) {
                    start = now;
                }
                final long time = fromTime + (now - start) / 1_000_000;
                final int index = fixationAt(time);
                if (index != current) {
                    showFixation(index);
                }
                if (time >= length) {
                    ScanpathView.this.stop();
                }
            }
        };
        player.start();
    }

    /**
     * Stops going through the fixations; the one shown stays.
     */
    void stop() {
        if (player != null) {
            player.stop();
            player = null;
        }
        if (playButton != null) {
            playButton.setText(text(translator, "ScanPlay"));
        }
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }
}
