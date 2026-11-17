package net.gazeplay.ui.scenes.stats;

import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.AoiGeometry;
import net.gazeplay.commons.utils.stats.AreaOfInterestRecording;
import net.gazeplay.commons.utils.stats.CoordinatesTracker;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.ui.menus.MenuIcon;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.gazeplay.ui.scenes.stats.StatsScreenParts.text;

/**
 * The areas of interest screen, opened from the statistics (refonte of 17/11/2026, maquette B chosen by the
 * maintainer): the game screen, white, with the areas where the gaze (or the mouse) stayed, darker when it stayed
 * longer, each with its number; on the right, the list of the areas (maquette A, added at the maintainer's request),
 * in the order of their numbers, the chosen one (hovered on the screen or in the list) opened with its first fixation, its
 * time spent, its positions recorded and its returns to the area. A bar at the bottom replays the recorded positions
 * at the real speed or slowed down 5 or 10 times. One source at a time (R13), the gaze by default. Nothing shown here
 * is written to disk.
 */
@Slf4j
public class AreaOfInterest extends GraphicalContext<BorderPane> {

    /**
     * The areas of interest of each source are computed separately (R13): the screen shows one source at a time.
     */
    public enum Source {
        GAZE, MOUSE
    }

    /**
     * @return the gaze if it has samples (game played with an eye tracker), else the mouse
     */
    public static Source defaultSource(final Stats stats) {
        return stats.getGazeAreasOfInterest().getMovementHistory().isEmpty() ? Source.MOUSE : Source.GAZE;
    }

    /**
     * The speeds of the replay: how many times slower than the game.
     */
    static final int[] SLOWDOWNS = {1, 5, 10};

    private static final String[] SPEED_KEYS = {"AoiSpeedReal", "AoiSpeed5", "AoiSpeed10"};

    /**
     * Color of the areas (the main color of the light theme), its opacity going with the time spent.
     */
    private static final Color AREA_FILL = Color.rgb(242, 100, 58);

    private static final Color AREA_STROKE = Color.rgb(201, 72, 42);

    private static final Color BADGE_COLOR = Color.rgb(43, 33, 24);

    /**
     * A stay of the gaze (or the mouse) in one place, as recorded during the game.
     *
     * @param polygon       its outline, in the pixels of the game screen
     * @param firstFixation seconds from the start of the game to the stay
     * @param timeSpent     seconds of the stay
     * @param samples       positions recorded during the stay
     * @param startIndex    index of its first position in the movement history
     * @param endIndex      index of its last position in the movement history
     */
    record Stay(Polygon polygon, double firstFixation, double timeSpent, int samples, int startIndex, int endIndex,
                long startTime) {
    }

    /**
     * An area shown on the screen: one stay, or several stays at the same place merged into one.
     *
     * @param number        its number, in the order the areas were first reached
     * @param firstFixation seconds from the start of the game to its first stay
     * @param timeSpent     seconds of all its stays
     * @param samples       positions recorded in all its stays
     * @param returns       stays after the first one
     * @param stayIndexes   indexes of its stays in the list of the stays
     */
    record Zone(int number, Shape shape, double firstFixation, double timeSpent, int samples, int returns,
                List<Integer> stayIndexes) {
    }

    private final Stats stats;

    private final Source source;

    private final Translator translator;

    private final Locale locale;

    private final Configuration config;

    private final List<CoordinatesTracker> movementHistory;

    @Getter(AccessLevel.PACKAGE)
    private final List<Stay> stays = new ArrayList<>();

    @Getter(AccessLevel.PACKAGE)
    private final List<Zone> zones;

    /**
     * Share of the areas on the targets of the game (0 to 1), or a negative value when the game gives no target.
     */
    @Getter(AccessLevel.PACKAGE)
    private final double targetScore;

    /**
     * The game screen, in its own pixels: the video if any, the areas, the positions of the replay.
     */
    private final Pane screen = new Pane();

    /**
     * The number of each area on the game screen, at its center, to find it in the list.
     */
    private final List<Group> badges = new ArrayList<>();

    /**
     * The list of the areas on the right (maquette A), the one chosen opened with all its figures.
     */
    private final VBox zoneList = new VBox(6);

    private final Map<Zone, VBox> zoneItems = new HashMap<>();

    private ScrollPane zoneScroll;

    @Getter(AccessLevel.PACKAGE)
    private Zone selectedZone;

    @Getter(AccessLevel.PACKAGE)
    private Button replayButton;

    @Getter(AccessLevel.PACKAGE)
    private final List<ToggleButton> speedTabs = new ArrayList<>();

    @Getter(AccessLevel.PACKAGE)
    private Button backButton;

    private Label timeLabel;

    private MediaPlayer player;

    private AnimationTimer replay;

    @Getter(AccessLevel.PACKAGE)
    private boolean playing;

    private int slowdown = SLOWDOWNS[0];

    /**
     * Builds the screen for one source; the other one can be shown with the switch of the header.
     */
    public AreaOfInterest(final GazePlay gazePlay, final Stats stats, final Source shownSource) {
        super(gazePlay, new BorderPane());
        this.stats = stats;
        this.source = shownSource;
        this.translator = gazePlay.getTranslator();
        this.locale = StatsFigures.javaLocale(translator.currentLocale());
        this.config = ActiveConfigurationContext.getInstance();

        final AreaOfInterestRecording recording =
            shownSource == Source.GAZE ? stats.getGazeAreasOfInterest() : stats.getMouseAreasOfInterest();
        movementHistory = recording.getMovementHistory();
        computeDistances();
        computeStays(recording, stats.getStartTime());
        zones = mergeStays(stays);
        targetScore = stats.getTargetAOIList() == null ? -1 : targetScore(stays, stats.getTargetAOIList());

        StatsScreenParts.style(root, locale);
        root.setTop(header());
        final Node body = body(gazePlay.getCurrentScreenDimensionSupplier().get());
        BorderPane.setMargin(body, new Insets(16, 0, 16, 0));
        root.setCenter(body);
        root.setBottom(bar());
        showZones();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Computations (unchanged since the former screen, except the merged areas: see mergeStays)

    /**
     * Distance of each position to the previous one: a position close to the previous one is a fixation in the
     * replay.
     */
    private void computeDistances() {
        if (movementHistory.isEmpty()) {
            // No sample for this source (e.g. the mouse, in a game played with an eye tracker)
            return;
        }
        movementHistory.get(0).setDistance(0);
        for (int i = 1; i < movementHistory.size(); i++) {
            final double x = Math.pow(movementHistory.get(i).getXValue() - movementHistory.get(i - 1).getXValue(), 2);
            final double y = Math.pow(movementHistory.get(i).getYValue() - movementHistory.get(i - 1).getYValue(), 2);
            movementHistory.get(i).setDistance(Math.sqrt(x + y));
        }
    }

    private void computeStays(final AreaOfInterestRecording recording, final double startTime) {
        final List<List<CoordinatesTracker>> positions = recording.getAllAOIListTemp();
        for (int i = 0; i < positions.size(); i++) {
            final List<CoordinatesTracker> stay = positions.get(i);
            final double areaStartTime = stay.get(0).getTimeStarted();
            final CoordinatesTracker last = stay.get(stay.size() - 1);
            final double areaEndTime = last.getTimeStarted() + last.getIntervalTime();
            stays.add(new Stay(recording.getAllAOIListPolygon().get(i), (areaStartTime - startTime) / 1000.0,
                (areaEndTime - areaStartTime) / 1000.0, stay.size(), recording.getStartAndEndIdx().get(i)[0],
                recording.getStartAndEndIdx().get(i)[1], (long) areaStartTime));
        }
    }

    /**
     * Merges the stays at the same place (an overlap of more than 70 % of the smaller one) into one area. The first
     * fixation of a merged area is the one of its first stay (the former screen added them up), and its positions are
     * the positions of its stays (the former screen added their seconds).
     */
    static List<Zone> mergeStays(final List<Stay> stays) {
        final int[] areaMap = new int[stays.size()];
        java.util.Arrays.fill(areaMap, -1);
        for (int i = 0; i < stays.size(); i++) {
            final Polygon current = stays.get(i).polygon();
            final double areaSize = current.getBoundsInLocal().getWidth() * current.getBoundsInLocal().getHeight();
            for (int j = 0; j < stays.size(); j++) {
                if (stays.get(j).polygon() == current) {
                    continue;
                }
                final Shape intersect = Shape.intersect(current, stays.get(j).polygon());
                if (intersect.getBoundsInLocal().getWidth() != -1) {
                    final double overlap = intersect.getBoundsInLocal().getWidth()
                        * intersect.getBoundsInLocal().getHeight();
                    if (overlap / areaSize > 0.70) {
                        if (areaMap[j] != -1) {
                            areaMap[i] = areaMap[j];
                        } else {
                            areaMap[i] = i;
                            areaMap[j] = i;
                        }
                        break;
                    }
                }
            }
        }

        final List<Zone> zones = new ArrayList<>();
        for (int i = 0; i < stays.size(); i++) {
            final Stay first = stays.get(i);
            if (areaMap[i] == -1) {
                zones.add(new Zone(zones.size() + 1, first.polygon(), first.firstFixation(), first.timeSpent(),
                    first.samples(), 0, List.of(i)));
            } else if (areaMap[i] == i) {
                Shape shape = first.polygon();
                double firstFixation = first.firstFixation();
                double timeSpent = first.timeSpent();
                int samples = first.samples();
                int returns = 0;
                final List<Integer> indexes = new ArrayList<>(List.of(i));
                for (int j = 0; j < stays.size(); j++) {
                    if (j != i && areaMap[j] == i) {
                        final Stay other = stays.get(j);
                        firstFixation = Math.min(firstFixation, other.firstFixation());
                        timeSpent += other.timeSpent();
                        samples += other.samples();
                        returns++;
                        indexes.add(j);
                        shape = Shape.union(shape, other.polygon());
                    }
                }
                zones.add(new Zone(zones.size() + 1, shape, firstFixation, timeSpent, samples, returns,
                    List.copyOf(indexes)));
            }
        }
        return zones;
    }

    /**
     * @return the share of the stays on the targets of the game shown at their time (0 to 1)
     */
    static double targetScore(final List<Stay> stays, final List<TargetAOI> targets) {
        calculateTargetAOI(targets);
        if (stays.isEmpty()) {
            return 0;
        }
        double score = 0;
        for (final Stay stay : stays) {
            double maxScore = 0;
            for (final TargetAOI target : targets) {
                if (target.getTimeStarted() <= stay.startTime() && stay.startTime() <= target.getTimeEnded()) {
                    final Shape intersect = Shape.intersect(target.getPolygon(), stay.polygon());
                    final double width = stay.polygon().getBoundsInLocal().getWidth() - 1;
                    if (intersect.getBoundsInLocal().getWidth() != -1 && width > 0) {
                        maxScore = Math.max(maxScore, intersect.getBoundsInLocal().getWidth() / width);
                    }
                }
            }
            score += Math.min(1, maxScore);
        }
        return score / stays.size();
    }

    /**
     * Creates a rectangle surrounding each target of the game, as a transparent polygon of the target.
     */
    static void calculateTargetAOI(final List<TargetAOI> targets) {
        for (final TargetAOI targetAOI : targets) {
            final int radius = targetAOI.getAreaRadius();
            final Point2D[] point2D = {
                new Point2D(targetAOI.getXValue() - 100, targetAOI.getYValue()),
                new Point2D(targetAOI.getXValue() + radius, targetAOI.getYValue() + 100),
                new Point2D(targetAOI.getXValue(), targetAOI.getYValue() - radius),
                new Point2D(targetAOI.getXValue() + radius, targetAOI.getYValue() - radius)
            };
            final Polygon targetArea = new Polygon();
            targetArea.getPoints().addAll(AoiGeometry.calculateRectangle(point2D));
            targetArea.setFill(Color.rgb(255, 255, 255, 0.4));
            targetAOI.setPolygon(targetArea);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Header

    private Node header() {
        final List<Source> shown = sourcesShown(EyeTracker.fromConfigValue(config.getEyeTracker()));
        Node sourceSwitch = null;
        if (shown.size() > 1) {
            sourceSwitch = StatsScreenParts.sourceSwitch(List.of(text(translator, "Gaze"), text(translator, "Mouse")),
                source == Source.GAZE ? 0 : 1, index -> showSource(index == 0 ? Source.GAZE : Source.MOUSE));
        }
        final List<String> extraChips = targetScore < 0 ? List.of()
            : List.of(text(translator, "AoiTargetScore") + " " + Math.round(targetScore * 100) + " %");
        return StatsScreenParts.header(translator, locale, stats, config, "StatsAreasOfInterest", extraChips,
            sourceSwitch);
    }

    /**
     * @return the sources that can be shown for the inputs of the game, as the images of the statistics screen
     */
    static List<Source> sourcesShown(final EyeTracker inputs) {
        if (!inputs.usesGaze()) {
            return List.of(Source.MOUSE);
        }
        if (!inputs.acceptsMouse()) {
            return List.of(Source.GAZE);
        }
        return List.of(Source.GAZE, Source.MOUSE);
    }

    /**
     * Stops the replay and shows the areas of another source.
     */
    private void showSource(final Source shown) {
        if (shown == source) {
            return;
        }
        stopReplay();
        stopVideo();
        this.clear();
        getGazePlay().onDisplayAOI(stats, shown);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // The game view

    private Node body(final Dimension2D screenSize) {
        addVideo();
        final StatsScreenParts.GameView view = new StatsScreenParts.GameView(screen, screenSize.getWidth(),
            screenSize.getHeight());

        final StackPane stack = new StackPane(view);
        if (movementHistory.isEmpty()) {
            final Label empty = new Label(text(translator, "AoiNoData"));
            empty.getStyleClass().add("gp-stats-empty");
            stack.getChildren().add(empty);
        } else {
            final HBox legend = legend();
            StackPane.setAlignment(legend, Pos.BOTTOM_LEFT);
            StackPane.setMargin(legend, new Insets(16));
            stack.getChildren().add(legend);
        }
        stack.setMinSize(0, 0);

        final VBox card = new VBox(stack);
        VBox.setVgrow(stack, Priority.ALWAYS);
        card.getStyleClass().addAll("gp-stats-card", "gp-stats-view-card");
        card.setMinHeight(0);
        HBox.setHgrow(card, Priority.ALWAYS);
        if (zones.isEmpty()) {
            return card;
        }
        final HBox columns = new HBox(16, card, zonesCard());
        columns.setMinHeight(0);
        return columns;
    }

    /**
     * The video of the game, under the areas, when it was recorded.
     */
    private void addVideo() {
        if (!config.isVideoRecordingEnabled()) {
            return;
        }
        try {
            // The recorder is stopped asynchronously at the end of the game: the file must be complete
            if (!stats.awaitVideoRecordingEnd(10, TimeUnit.SECONDS)) {
                log.warn("The video recording did not end within 10 s, its file may be incomplete");
            }
            final File video = stats.getVideoFile().orElseThrow(() -> new IllegalStateException("No video recorded"));
            player = new MediaPlayer(new Media(VideoConverter.toMp4(video).toURI().toString()));
            final MediaView mediaView = new MediaView(player);
            mediaView.setMouseTransparent(true);
            screen.getChildren().add(mediaView);
        } catch (final Exception ex) {
            log.warn("Cannot play the video of the areas of interest", ex);
        }
    }

    /**
     * @return the card of the list of the areas (maquette A): in the order of their numbers, each one with its
     *   number, its time spent and its first fixation; the chosen one opened with all its figures
     */
    private VBox zonesCard() {
        final Label title = new Label(zones.size() == 1 ? text(translator, "AoiZoneCountOne")
            : text(translator, "AoiZoneCount").replace("{0}", String.valueOf(zones.size())));
        title.getStyleClass().add("gp-stats-card-title");

        // In the order of their numbers, the order in which they were first reached (maintainer's request)
        for (final Zone zone : zones) {
            final VBox item = zoneItem(zone);
            zoneItems.put(zone, item);
            zoneList.getChildren().add(item);
        }
        zoneList.setFillWidth(true);

        zoneScroll = new ScrollPane(zoneList);
        zoneScroll.setFitToWidth(true);
        zoneScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        zoneScroll.getStyleClass().add("gp-stats-zone-scroll");
        VBox.setVgrow(zoneScroll, Priority.ALWAYS);

        final VBox card = new VBox(6, title, zoneScroll);
        card.getStyleClass().addAll("gp-stats-card", "gp-stats-zones-card");
        card.setMinHeight(0);
        card.setMinWidth(300);
        card.setPrefWidth(340);
        card.setMaxWidth(340);
        return card;
    }

    private VBox zoneItem(final Zone zone) {
        final Label name = new Label(zoneName(zone));
        name.getStyleClass().add("gp-stats-zone-name");
        final Label summary = new Label(text(translator, "AoiSummary")
            .replace("{0}", StatsScreenParts.seconds(zone.timeSpent(), locale))
            .replace("{1}", StatsScreenParts.seconds(zone.firstFixation(), locale)));
        summary.getStyleClass().add("gp-stats-note");
        summary.setWrapText(true);

        final VBox details = new VBox(4);
        final String[][] rows = {
            {text(translator, "AoiFirstFixation"), StatsScreenParts.seconds(zone.firstFixation(), locale)},
            {text(translator, "AoiTimeSpent"), StatsScreenParts.seconds(zone.timeSpent(), locale)},
            {text(translator, "AoiSamples"), String.valueOf(zone.samples())},
            {text(translator, "AoiReturns"), String.valueOf(zone.returns())}
        };
        for (final String[] row : rows) {
            final Label label = new Label(row[0]);
            label.getStyleClass().add("gp-stats-detail-label");
            final Label value = new Label(row[1]);
            value.getStyleClass().add("gp-stats-detail-value");
            final Region gap = new Region();
            HBox.setHgrow(gap, Priority.ALWAYS);
            final HBox line = new HBox(12, label, gap, value);
            line.setAlignment(Pos.BASELINE_LEFT);
            details.getChildren().add(line);
        }
        details.setVisible(false);
        details.setManaged(false);

        final VBox item = new VBox(4, name, summary, details);
        item.getStyleClass().add("gp-stats-zone-item");
        item.setOnMouseClicked(e -> selectZone(zone));
        item.setOnMouseEntered(e -> {
            if (!playing) {
                selectZone(zone);
            }
        });
        return item;
    }

    private String zoneName(final Zone zone) {
        return text(translator, "AoiZone").replace("{0}", String.valueOf(zone.number()));
    }

    /**
     * Chooses an area: its item of the list opens (and comes into view), the area and its number are highlighted on
     * the game screen.
     */
    void selectZone(final Zone zone) {
        if (zone == selectedZone) {
            return;
        }
        selectedZone = zone;
        for (final Map.Entry<Zone, VBox> entry : zoneItems.entrySet()) {
            final boolean chosen = entry.getKey() == zone;
            final VBox item = entry.getValue();
            item.getStyleClass().remove("gp-stats-zone-selected");
            if (chosen) {
                item.getStyleClass().add("gp-stats-zone-selected");
            }
            // name, summary, details
            item.getChildren().get(1).setVisible(!chosen);
            item.getChildren().get(1).setManaged(!chosen);
            item.getChildren().get(2).setVisible(chosen);
            item.getChildren().get(2).setManaged(chosen);
        }
        paintZones();
        if (zone != null && zoneScroll != null) {
            final VBox item = zoneItems.get(zone);
            javafx.application.Platform.runLater(() -> {
                final double contentHeight = zoneList.getHeight() - zoneScroll.getViewportBounds().getHeight();
                if (contentHeight > 0) {
                    final double top = item.getBoundsInParent().getMinY();
                    final double bottom = item.getBoundsInParent().getMaxY();
                    final double viewTop = zoneScroll.getVvalue() * contentHeight;
                    final double viewBottom = viewTop + zoneScroll.getViewportBounds().getHeight();
                    if (top < viewTop) {
                        zoneScroll.setVvalue(top / contentHeight);
                    } else if (bottom > viewBottom) {
                        zoneScroll.setVvalue((bottom - zoneScroll.getViewportBounds().getHeight()) / contentHeight);
                    }
                }
            });
        }
    }

    private HBox legend() {
        final Label little = new Label(text(translator, "AoiLegendLittle"));
        final Label much = new Label(text(translator, "AoiLegendMuch"));
        little.getStyleClass().add("gp-stats-legend");
        much.getStyleClass().add("gp-stats-legend");
        final Region scale = new Region();
        scale.getStyleClass().add("gp-stats-aoi-scale");
        scale.setMinSize(120, 10);
        scale.setMaxSize(120, 10);
        final HBox legend = new HBox(10, little, scale, much);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.getStyleClass().add("gp-stats-overlay-pill");
        legend.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        legend.setMouseTransparent(true);
        return legend;
    }

    private double opacity(final Zone zone) {
        final double longest = zones.stream().mapToDouble(Zone::timeSpent).max().orElse(0);
        return longest > 0 ? zone.timeSpent() / longest * 0.6 + 0.10 : 0.10;
    }

    /**
     * Draws all the areas, darker when more time was spent in them, with their number; hovering one chooses it in the
     * list.
     */
    private void showZones() {
        badges.clear();
        for (final Zone zone : zones) {
            final Shape shape = zone.shape();
            shape.setStroke(AREA_STROKE);
            shape.setStrokeType(StrokeType.INSIDE);
            shape.setOnMouseEntered(e -> {
                if (!playing) {
                    selectZone(zone);
                }
            });
            shape.setOnMouseClicked(e -> selectZone(zone));
            if (!screen.getChildren().contains(shape)) {
                screen.getChildren().add(shape);
            }
        }
        for (final Zone zone : zones) {
            final Group badge = badge(zone);
            badges.add(badge);
            screen.getChildren().add(badge);
        }
        // At the start, the first area (maintainer's request); after a replay, the one chosen
        final Zone first = selectedZone == null && !zones.isEmpty() ? zones.get(0) : selectedZone;
        selectedZone = null;
        selectZone(first);
    }

    /**
     * Colors the areas and their numbers, the chosen one stronger and its number in the main color.
     */
    private void paintZones() {
        for (int i = 0; i < zones.size(); i++) {
            final Zone zone = zones.get(i);
            final boolean chosen = zone == selectedZone;
            zone.shape().setFill(AREA_FILL.deriveColor(0, 1, 1, Math.min(1, opacity(zone) + (chosen ? 0.15 : 0))));
            zone.shape().setStrokeWidth(chosen ? 7 : 3);
            if (i < badges.size()) {
                ((Circle) badges.get(i).getChildren().get(0)).setFill(chosen ? AREA_FILL : BADGE_COLOR);
            }
        }
    }

    /**
     * @return the number of an area in a dark round, at the center of the area, in the pixels of the game screen
     */
    private Group badge(final Zone zone) {
        final javafx.geometry.Bounds bounds = zone.shape().getBoundsInLocal();
        final double x = bounds.getCenterX();
        final double y = bounds.getCenterY();
        final Circle round = new Circle(x, y, 24, BADGE_COLOR);
        round.setStroke(Color.WHITE);
        round.setStrokeWidth(3);
        final Text number = new Text(String.valueOf(zone.number()));
        number.setFont(Font.font("Fredoka SemiBold", 26));
        number.setFill(Color.WHITE);
        number.setBoundsType(TextBoundsType.VISUAL);
        number.setX(x - number.getLayoutBounds().getWidth() / 2);
        number.setY(y + number.getLayoutBounds().getHeight() / 2);
        final Group badge = new Group(round, number);
        badge.setMouseTransparent(true);
        return badge;
    }

    private void hideZones() {
        for (final Zone zone : zones) {
            screen.getChildren().remove(zone.shape());
        }
        screen.getChildren().removeAll(badges);
    }

    /**
     * @return the area holding a stay
     */
    private Zone zoneOfStay(final int stayIndex) {
        for (final Zone zone : zones) {
            if (zone.stayIndexes().contains(stayIndex)) {
                return zone;
            }
        }
        return null;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // The bar: replay and back to the statistics

    private Node bar() {
        replayButton = StatsScreenParts.button(text(translator, "AoiReplay"), MenuIcon.PLAY, false,
            () -> {
                if (playing) {
                    stopReplay();
                } else {
                    startReplay();
                }
            });
        replayButton.setDisable(movementHistory.isEmpty());

        final Label speed = new Label(text(translator, "AoiSpeed"));
        speed.getStyleClass().add("gp-stats-legend");
        final ToggleGroup group = new ToggleGroup();
        final HBox speeds = new HBox(6, speed);
        speeds.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < SLOWDOWNS.length; i++) {
            final ToggleButton tab = StatsScreenParts.tab(text(translator, SPEED_KEYS[i]), group);
            final int chosen = SLOWDOWNS[i];
            tab.setOnAction(e -> {
                tab.setSelected(true);
                slowdown = chosen;
                if (player != null) {
                    player.setRate(1.0 / chosen);
                }
            });
            speedTabs.add(tab);
            speeds.getChildren().add(tab);
        }
        speedTabs.get(0).setSelected(true);

        timeLabel = new Label(StatsScreenParts.time(0, locale) + " / " + StatsScreenParts.time(gameLength(), locale));
        timeLabel.getStyleClass().add("gp-stats-time");

        backButton = StatsScreenParts.button(text(translator, "StatsBackToStats"), MenuIcon.BACK, true,
            this::backToStats);

        final Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        final HBox bar = new HBox(14, replayButton, speeds, timeLabel, gap, backButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("gp-stats-bar");
        return bar;
    }

    private void backToStats() {
        stopReplay();
        stopVideo();
        final StatsContext statsContext = StatsContextFactory.newAdvancedInstance(getGazePlay(), stats);
        this.clear();
        getGazePlay().onDisplayStats(statsContext);
    }

    /**
     * @return milliseconds from the first recorded position to the last one
     */
    private long gameLength() {
        if (movementHistory.isEmpty()) {
            return 0;
        }
        final CoordinatesTracker last = movementHistory.get(movementHistory.size() - 1);
        return last.getTimeStarted() + last.getIntervalTime() - movementHistory.get(0).getTimeStarted();
    }

    /**
     * Replays the recorded positions at the chosen speed: each position is a dot that disappears after 2 seconds,
     * orange for a fixation, blue otherwise; the stay being replayed is shown, and its area chosen in the list. Driven by the time of
     * the screen (one step per frame), so that the speed does not depend on the number of positions.
     */
    void startReplay() {
        if (playing || movementHistory.isEmpty()) {
            return;
        }
        playing = true;
        hideZones();
        replayButton.setText(text(translator, "AoiStop"));
        replayButton.setGraphic(MenuIcon.CLOSE.create(22));
        if (player != null) {
            player.stop();
            player.setRate(1.0 / slowdown);
            player.play();
        }
        final long firstTime = movementHistory.get(0).getTimeStarted();
        final long length = gameLength();
        final int chosenSlowdown = slowdown;
        final Deque<Circle> dots = new ArrayDeque<>();
        final Deque<Long> dotTimes = new ArrayDeque<>();
        replay = new AnimationTimer() {
            private long start = -1;
            private int next;
            private int nextStay;
            private Stay shownStay;
            private Polygon shownPolygon;

            @Override
            public void handle(final long now) {
                if (start < 0) {
                    start = now;
                }
                final long realElapsed = (now - start) / 1_000_000;
                final long gameElapsed = realElapsed / chosenSlowdown;
                while (next < movementHistory.size()
                    && movementHistory.get(next).getTimeStarted() - firstTime <= gameElapsed) {
                    final CoordinatesTracker position = movementHistory.get(next);
                    final Circle dot = dot(position);
                    screen.getChildren().add(dot);
                    dots.addLast(dot);
                    dotTimes.addLast(realElapsed);
                    if (shownStay != null && next > shownStay.endIndex()) {
                        screen.getChildren().remove(shownPolygon);
                        shownStay = null;
                    }
                    if (nextStay < stays.size() && next >= stays.get(nextStay).startIndex()) {
                        shownStay = stays.get(nextStay);
                        // The list follows the replay: the area of this stay is chosen
                        selectZone(zoneOfStay(nextStay));
                        nextStay++;
                        shownPolygon = shownStay.polygon();
                        shownPolygon.setFill(AREA_FILL.deriveColor(0, 1, 1, 0.3));
                        shownPolygon.setStrokeWidth(3);
                        if (!screen.getChildren().contains(shownPolygon)) {
                            screen.getChildren().add(shownPolygon);
                        }
                    }
                    next++;
                }
                while (!dotTimes.isEmpty() && realElapsed - dotTimes.peekFirst() > 2000) {
                    dotTimes.removeFirst();
                    screen.getChildren().remove(dots.removeFirst());
                }
                timeLabel.setText(StatsScreenParts.time(Math.min(gameElapsed, length), locale) + " / "
                    + StatsScreenParts.time(length, locale));
                if (next >= movementHistory.size() && dots.isEmpty()) {
                    stopReplay();
                }
            }
        };
        replay.start();
    }

    private Circle dot(final CoordinatesTracker position) {
        final boolean fixation = position.getIntervalTime() > 11 && position.getDistance() < 20;
        final Circle dot = new Circle(position.getXValue(), position.getYValue(), fixation ? 9 : 6);
        dot.setFill(fixation ? Color.rgb(242, 100, 58) : Color.rgb(47, 95, 196));
        dot.setStroke(Color.WHITE);
        dot.setStrokeWidth(2);
        dot.setMouseTransparent(true);
        return dot;
    }

    /**
     * Stops the replay and shows all the areas again, as before it started.
     */
    void stopReplay() {
        if (replay != null) {
            replay.stop();
            replay = null;
        }
        if (!playing) {
            return;
        }
        playing = false;
        if (player != null) {
            player.pause();
        }
        screen.getChildren().removeIf(node -> node instanceof Circle || node instanceof Polygon);
        for (final Stay stay : stays) {
            screen.getChildren().remove(stay.polygon());
        }
        replayButton.setText(text(translator, "AoiReplay"));
        replayButton.setGraphic(MenuIcon.PLAY.create(22));
        timeLabel.setText(StatsScreenParts.time(0, locale) + " / " + StatsScreenParts.time(gameLength(), locale));
        showZones();
    }

    private void stopVideo() {
        if (player != null) {
            player.stop();
        }
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }
}
