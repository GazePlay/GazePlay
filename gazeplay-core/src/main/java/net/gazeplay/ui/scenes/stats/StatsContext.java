package net.gazeplay.ui.scenes.stats;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.StatDisplayUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.ui.SystemFileManager;
import net.gazeplay.ui.menus.HelpDialog;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;
import net.gazeplay.ui.scenes.stats.StatsFigures.Figure;
import net.gazeplay.ui.scenes.stats.StatsFigures.View;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The statistics screen shown after a game or an evaluation (refonte of 16/11/2026, maquette A chosen by the
 * maintainer, REVUE_DES_JEUX.md), for an adult, in the look of the menus (menus.css, light and dark themes):
 * <ul>
 *   <li>at the top, the game, its variant, the profile, the date and time, and the switch "Simple | Advanced"; the
 *   screen always opens in the simple mode (maintainer's decision);</li>
 *   <li>simple: the main figures in large tiles, the heat map, and the buttons folder and menu (no "Continue" since
 *   17/11/2026, maintainer's request: the game is launched again from the menu);</li>
 *   <li>advanced (like a scientific calculator): the figures in small tiles, the detail of the durations, the views
 *   in tabs (heat map, durations, positions, levels, chi-squared: only those with data for this game), and the
 *   buttons of the areas of interest and of the scan path.</li>
 * </ul>
 * A click on the heat map or on a chart enlarges it to the whole screen, another click puts it back. Nothing shown
 * here changes the files of the results.
 */
@Slf4j
public class StatsContext extends GraphicalContext<BorderPane> {

    /**
     * Below this width, the screen takes the style class {@link #NARROW}: smaller texts and margins.
     */
    static final double NARROW_WIDTH = 1200;

    static final String NARROW = "gp-stats-narrow";

    private final Translator translator;

    private final Stats stats;

    private final Locale locale;

    /**
     * Whether the advanced mode is shown; false at the start.
     */
    @Getter(AccessLevel.PACKAGE)
    private final BooleanProperty advanced = new SimpleBooleanProperty(false);

    /**
     * Whether the view shown takes the whole screen (after a click on it).
     */
    @Getter(AccessLevel.PACKAGE)
    private final BooleanProperty enlarged = new SimpleBooleanProperty(false);

    @Getter(AccessLevel.PACKAGE)
    private final List<View> views;

    @Getter(AccessLevel.PACKAGE)
    private final Map<View, ToggleButton> tabs = new EnumMap<>(View.class);

    @Getter(AccessLevel.PACKAGE)
    private VBox simpleBody;

    @Getter(AccessLevel.PACKAGE)
    private VBox advancedBody;

    @Getter(AccessLevel.PACKAGE)
    private ToggleButton simpleSwitch;

    @Getter(AccessLevel.PACKAGE)
    private ToggleButton advancedSwitch;

    @Getter(AccessLevel.PACKAGE)
    private Button folderButton;

    @Getter(AccessLevel.PACKAGE)
    private Button menuButton;

    @Getter(AccessLevel.PACKAGE)
    private Button areasOfInterestButton;

    @Getter(AccessLevel.PACKAGE)
    private Button scanPathButton;

    @Getter(AccessLevel.PACKAGE)
    private View shownView;

    private Label viewTitle;

    private Button viewHelp;

    private StackPane viewContent;

    /**
     * Builds the screen of a game.
     */
    StatsContext(GazePlay gazePlay, BorderPane root, Stats stats) {
        super(gazePlay, root);
        this.translator = gazePlay.getTranslator();
        this.stats = stats;
        this.locale = StatsFigures.javaLocale(translator.currentLocale());

        final Configuration config = ActiveConfigurationContext.getInstance();
        this.views = StatsFigures.views(stats, !config.isFixationSequenceDisabled());

        MenuStyle.applyTo(root);
        root.getStyleClass().add("gp-stats-screen");
        // Arabic: the whole screen from right to left (the charts and images keep their own direction)
        if ("ar".equals(locale.getLanguage())) {
            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }

        root.setTop(header(config));
        simpleBody = simpleBody(config);
        advancedBody = advancedBody(config);
        final StackPane body = new StackPane(simpleBody, advancedBody);
        BorderPane.setMargin(body, new Insets(16, 0, 16, 0));
        root.setCenter(body);
        root.setBottom(footer(config));

        simpleBody.visibleProperty().bind(advanced.not());
        advancedBody.visibleProperty().bind(advanced);

        // Smaller texts and margins on a narrow screen (down to 800 pixels wide, menus.css)
        root.widthProperty().addListener((observable, before, width) -> {
            final boolean narrow = width.doubleValue() > 0 && width.doubleValue() < NARROW_WIDTH;
            if (narrow && !root.getStyleClass().contains(NARROW)) {
                root.getStyleClass().add(NARROW);
            } else if (!narrow) {
                root.getStyleClass().remove(NARROW);
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Header

    private Node header(final Configuration config) {
        final Label title = new Label(stats.getCurrentGameNameCode() == null ? translator.translate("StatsTitle")
            : translator.translate(stats.getCurrentGameNameCode()));
        title.getStyleClass().add("gp-stats-game-title");

        final FlowPane chips = new FlowPane(8, 8);
        final String variant = StatsFigures.variant(stats.getCurrentGameVariant(), stats.getCurrentGameNameCode(),
            translator);
        if (variant != null && !variant.isBlank()) {
            chips.getChildren().add(chip(variant));
        }
        final String user = config.getUserName();
        if (user != null && !user.isBlank()) {
            chips.getChildren().add(chip(translator.translate("StatsProfile") + " " + user));
        }
        chips.getChildren().add(chip(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale)
            .format(LocalDateTime.now())));

        final VBox identity = new VBox(8, title, chips);
        HBox.setHgrow(identity, Priority.ALWAYS);

        final ToggleGroup group = new ToggleGroup();
        simpleSwitch = modeSwitch(translator.translate("StatsSimple"), group);
        advancedSwitch = modeSwitch(translator.translate("StatsAdvanced"), group);
        simpleSwitch.setSelected(true);
        // One of the two is always chosen
        group.selectedToggleProperty().addListener((observable, before, now) -> {
            if (now == null) {
                group.selectToggle(before);
            } else {
                enlarged.set(false);
                advanced.set(now == advancedSwitch);
            }
        });
        final HBox modes = new HBox(4, simpleSwitch, advancedSwitch);
        modes.getStyleClass().add("gp-stats-mode");
        modes.setMaxHeight(Region.USE_PREF_SIZE);

        final HBox header = new HBox(24, identity, modes);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private static Label chip(final String text) {
        final Label chip = new Label(text);
        chip.getStyleClass().add("gp-stats-chip");
        return chip;
    }

    private static ToggleButton modeSwitch(final String text, final ToggleGroup group) {
        final ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(group);
        return button;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Bodies

    private VBox simpleBody(final Configuration config) {
        final GridPane tiles = tiles(StatsFigures.main(stats, locale), false);
        tiles.visibleProperty().bind(enlarged.not());
        tiles.managedProperty().bind(tiles.visibleProperty());

        final Label title = cardTitle(translator.translate(View.HEATMAP.titleKey));
        final HBox titleLine = new HBox(10, title, helpButton(View.HEATMAP));
        titleLine.setAlignment(Pos.CENTER_LEFT);
        final VBox heatmap = heatmap(config);
        // The sources go under the title when the line is too short
        final FlowPane top = new FlowPane(24, 8, titleLine, (Node) heatmap.getUserData());
        top.setAlignment(Pos.CENTER_LEFT);
        final VBox card = card(top, heatmap);
        VBox.setVgrow(heatmap, Priority.ALWAYS);

        final VBox body = new VBox(16, tiles, card);
        VBox.setVgrow(card, Priority.ALWAYS);
        return body;
    }

    private VBox advancedBody(final Configuration config) {
        final GridPane tiles = tiles(StatsFigures.main(stats, locale), true);
        tiles.visibleProperty().bind(enlarged.not());
        tiles.managedProperty().bind(tiles.visibleProperty());

        final HBox columns = new HBox(16);
        final List<Figure> details = StatsFigures.details(stats, locale);
        if (!details.isEmpty()) {
            final VBox detailsCard = detailsCard(details);
            detailsCard.visibleProperty().bind(enlarged.not());
            detailsCard.managedProperty().bind(detailsCard.visibleProperty());
            columns.getChildren().add(detailsCard);
        }
        final VBox viewsCard = viewsCard(config);
        HBox.setHgrow(viewsCard, Priority.ALWAYS);
        columns.getChildren().add(viewsCard);

        final VBox body = new VBox(14, tiles, columns);
        VBox.setVgrow(columns, Priority.ALWAYS);
        return body;
    }

    /**
     * @return the figures as tiles of the same width; small ones in the advanced mode
     */
    private GridPane tiles(final List<Figure> figures, final boolean compact) {
        final GridPane grid = new GridPane();
        grid.setHgap(compact ? 12 : 16);
        grid.getStyleClass().add("gp-stats-tiles");
        if (compact) {
            grid.getStyleClass().add("gp-stats-compact");
        }
        for (int i = 0; i < figures.size(); i++) {
            final Figure figure = figures.get(i);
            final Label label = new Label(translator.translate(figure.labelKey()));
            label.getStyleClass().add("gp-stats-tile-label");
            label.setWrapText(true);
            final Label value = new Label(figure.value());
            value.getStyleClass().add("gp-stats-tile-value");
            if (figure.good()) {
                value.getStyleClass().add("gp-stats-good");
            }
            final VBox tile = new VBox(compact ? 0 : 2, label, value);
            tile.getStyleClass().add("gp-stats-tile");
            tile.setMaxWidth(Double.MAX_VALUE);
            grid.add(tile, i, 0);
            final ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / figures.size());
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);
        }
        return grid;
    }

    /**
     * @return the card of the detail of the durations, with the note on the standard deviation
     */
    private VBox detailsCard(final List<Figure> details) {
        final GridPane rows = new GridPane();
        rows.setHgap(16);
        final ColumnConstraints name = new ColumnConstraints();
        name.setHgrow(Priority.ALWAYS);
        // The values are never cut: the names wrap instead
        final ColumnConstraints values = new ColumnConstraints();
        values.setMinWidth(Region.USE_PREF_SIZE);
        values.setHgrow(Priority.NEVER);
        rows.getColumnConstraints().addAll(name, values);
        for (int i = 0; i < details.size(); i++) {
            final Label label = new Label(translator.translate(details.get(i).labelKey()));
            label.getStyleClass().add("gp-stats-detail-label");
            label.setWrapText(true);
            final Label value = new Label(details.get(i).value());
            value.getStyleClass().add("gp-stats-detail-value");
            label.setMaxWidth(Double.MAX_VALUE);
            value.setMaxWidth(Double.MAX_VALUE);
            value.setAlignment(Pos.CENTER_RIGHT);
            final String row = i == details.size() - 1 ? "gp-stats-detail-last" : "gp-stats-detail-row";
            label.getStyleClass().add(row);
            value.getStyleClass().add(row);
            rows.add(label, 0, i);
            rows.add(value, 1, i);
        }
        final Label note = new Label(translator.translate("StatsSDNote"));
        note.getStyleClass().add("gp-stats-note");
        note.setWrapText(true);
        // The rows scroll when the screen is too low for them
        final ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMinHeight(0);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        final HBox titleLine = new HBox(10, cardTitle(translator.translate("StatsDurationDetails")),
            helpButton("StatsDurationDetails", "data/help/stats-details-"));
        titleLine.setAlignment(Pos.CENTER_LEFT);
        final VBox card = card(titleLine, scroll, note);
        // The same width whatever the view shown next to it, narrower on a narrow screen
        card.setMinWidth(260);
        card.setPrefWidth(400);
        card.setMaxWidth(400);
        return card;
    }

    /**
     * @return the card of the views of the advanced mode: a tab per view with data, its title and its "i", the view
     */
    private VBox viewsCard(final Configuration config) {
        final FlowPane tabLine = new FlowPane(6, 6);
        tabLine.setAlignment(Pos.CENTER_LEFT);
        final ToggleGroup group = new ToggleGroup();
        for (final View view : views) {
            final ToggleButton tab = new ToggleButton(translator.translate(view.tabKey));
            tab.getStyleClass().add("gp-stats-tab");
            tab.setToggleGroup(group);
            tab.setOnAction(e -> {
                tab.setSelected(true);
                show(view, config);
            });
            tabs.put(view, tab);
            tabLine.getChildren().add(tab);
        }
        viewTitle = cardTitle("");
        viewHelp = new Button();
        final HBox titleLine = new HBox(10, viewTitle);
        titleLine.setAlignment(Pos.CENTER_LEFT);
        viewContent = new StackPane();
        viewContent.setMinSize(0, 0);
        final VBox card = card(tabLine, titleLine, viewContent);
        VBox.setVgrow(viewContent, Priority.ALWAYS);
        card.setMinWidth(0);
        show(StatsFigures.firstAdvancedView(views), config);
        return card;
    }

    /**
     * Shows a view of the advanced mode.
     */
    void show(final View view, final Configuration config) {
        shownView = view;
        final ToggleButton tab = tabs.get(view);
        if (tab != null) {
            tab.setSelected(true);
        }
        viewTitle.setText(translator.translate(view.titleKey));
        final HBox titleLine = (HBox) viewTitle.getParent();
        if (titleLine != null) {
            titleLine.getChildren().remove(viewHelp);
            viewHelp = helpButton(view);
            titleLine.getChildren().add(viewHelp);
        }
        final Node content = switch (view) {
            case HEATMAP -> {
                final VBox heatmap = heatmap(config);
                final Node sources = (Node) heatmap.getUserData();
                final VBox box = new VBox(8, sources, heatmap);
                VBox.setVgrow(heatmap, Priority.ALWAYS);
                yield box;
            }
            case DURATIONS -> enlargeable(StatsCharts.durations(stats, translator));
            case POSITIONS -> enlargeable(StatsCharts.positions(stats, translator));
            case LEVELS -> enlargeable(StatsCharts.levels(stats, translator));
            case CHI2 -> StatsCharts.chi2(stats, translator, locale);
        };
        viewContent.getChildren().setAll(content);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // The heat map

    /**
     * @return the image of the results (heat map and path of the gaze), as large as its place allows; its user data is
     *   the row of buttons choosing the source (mouse, gaze, both), empty when only one input played
     */
    private VBox heatmap(final Configuration config) {
        final ImageView image = new ImageView();
        image.setPreserveRatio(true);
        image.setSmooth(true);
        image.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        final StackPane holder = enlargeable(fit(image));

        final List<String> shown = metricsShown(EyeTracker.fromConfigValue(config.getEyeTracker()));
        final HBox sources = new HBox(6);
        sources.setAlignment(Pos.CENTER_RIGHT);
        if (shown.size() > 1) {
            final ToggleGroup group = new ToggleGroup();
            for (final String name : shown) {
                final ToggleButton source = new ToggleButton(translator.translate(name));
                source.getStyleClass().add("gp-stats-tab");
                source.setToggleGroup(group);
                source.setOnAction(e -> {
                    source.setSelected(true);
                    showMetrics(name, stats, image);
                });
                sources.getChildren().add(source);
            }
            // Both inputs at the start, as before
            ((ToggleButton) sources.getChildren().get(sources.getChildren().size() - 1)).setSelected(true);
        }
        showMetrics(shown.get(shown.size() - 1), stats, image);

        final VBox box = new VBox(holder);
        VBox.setVgrow(holder, Priority.ALWAYS);
        box.setMinSize(0, 0);
        box.setUserData(sources);
        return box;
    }

    /**
     * @return a place where an image takes all the room it can, keeping its proportions, centered
     */
    private static StackPane fit(final ImageView image) {
        final StackPane holder = new StackPane(image);
        holder.setMinSize(0, 0);
        holder.setPrefSize(10, 10);
        image.setManaged(false);
        image.fitWidthProperty().bind(holder.widthProperty());
        image.fitHeightProperty().bind(holder.heightProperty());
        final ChangeListener<Object> center = (observable, before, now) -> {
            final Bounds bounds = image.getLayoutBounds();
            image.relocate((holder.getWidth() - bounds.getWidth()) / 2, (holder.getHeight() - bounds.getHeight()) / 2);
        };
        holder.widthProperty().addListener(center);
        holder.heightProperty().addListener(center);
        image.layoutBoundsProperty().addListener(center);
        return holder;
    }

    /**
     * @return the images of the results that can be shown, for the inputs of the game (EYE_TRACKER): the mouse alone,
     *   the gaze alone, or both with the mouse and gaze image too (decision of the maintainer, October 2026)
     */
    static List<String> metricsShown(EyeTracker inputs) {
        if (!inputs.usesGaze()) {
            return List.of("Mouse");
        }
        if (!inputs.acceptsMouse()) {
            return List.of("Gaze");
        }
        return List.of("Mouse", "Gaze", "MouseAndGaze");
    }

    /**
     * Shows a metrics image of the round in the image of the screen.
     */
    private static void showMetrics(String metricsName, Stats stats, ImageView metrics) {
        SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();
        if (savedStatsInfo == null) {
            return;
        }
        final File file = switch (metricsName) {
            case "Mouse" -> savedStatsInfo.getGazeMetricsFileMouse();
            case "Gaze" -> savedStatsInfo.getGazeMetricsFileGaze();
            default -> savedStatsInfo.getGazeMetricsFileMouseAndGaze();
        };
        if (file != null) {
            metrics.setImage(new Image(file.toURI().toString()));
        }
    }

    /**
     * A click on a view enlarges it to the whole screen (the tiles and the detail are hidden), another click puts it
     * back, as the former screen did.
     */
    private <T extends Region> T enlargeable(final T view) {
        Tooltip.install(view, MenuStyle.tooltip(translator.translate("StatsEnlarge")));
        view.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                enlarged.set(!enlarged.get());
            }
        });
        return view;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Footer

    private Node footer(final Configuration config) {
        areasOfInterestButton = button("StatsAreasOfInterest", MenuIcon.AREAS_OF_INTEREST, false,
            () -> getGazePlay().onDisplayAOI(stats));
        scanPathButton = button("StatsScanPath", MenuIcon.SCAN_PATH, false,
            () -> getGazePlay().onDisplayScanpath(new ScanpathView(getGazePlay(), stats)));
        final HBox advancedButtons = new HBox(12);
        // AREA_OF_INTEREST_DISABLED: true means the areas of interest are on (name kept, see CLAUDE.md)
        if (config.isAreaOfInterestDisabled()) {
            advancedButtons.getChildren().add(areasOfInterestButton);
        }
        if (!config.isFixationSequenceDisabled()) {
            advancedButtons.getChildren().add(scanPathButton);
        }
        advancedButtons.visibleProperty().bind(advanced);
        advancedButtons.managedProperty().bind(advanced);

        folderButton = button("StatsResultsFolder", MenuIcon.FOLDER, false, () -> seeGameFolder(stats));
        // The main action since "Continue" is gone (17/11/2026)
        menuButton = button("StatsMenu", MenuIcon.HOME, true, StatDisplayUtils::closeStatsWindow);
        final HBox mainButtons = new HBox(12, folderButton, menuButton);

        final Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        final HBox footer = new HBox(12, advancedButtons, gap, mainButtons);
        footer.setAlignment(Pos.CENTER_LEFT);
        return footer;
    }

    private Button button(final String key, final MenuIcon icon, final boolean primary, final Runnable action) {
        final Button button = new Button(translator.translate(key), icon.create(22));
        button.getStyleClass().addAll("gp-button", primary ? "gp-button-primary" : "gp-button-secondary");
        button.setOnAction(e -> action.run());
        return button;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Shared parts

    private static VBox card(final Node... children) {
        final VBox card = new VBox(12, children);
        card.getStyleClass().add("gp-stats-card");
        card.setMinHeight(0);
        return card;
    }

    private static Label cardTitle(final String text) {
        final Label title = new Label(text);
        title.getStyleClass().add("gp-stats-card-title");
        title.setWrapText(true);
        return title;
    }

    private Button helpButton(final View view) {
        return helpButton(view.titleKey, view.helpPrefix);
    }

    /**
     * @return the "i" button opening the explanation of a view, in the look of the help of the games
     */
    private Button helpButton(final String titleKey, final String helpPrefix) {
        return MenuStyle.smallRoundButton(MenuIcon.INFO, "teal", translator.translate("StatsExplain"),
            () -> new HelpDialog(getGazePlay().getPrimaryStage(), translator, translator.translate(titleKey),
                HelpDialog.read(helpPrefix, HelpDialog.language(locale)), null, null).show());
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    /**
     * Opens the folder where the files of this game (heatmaps, metrics, replay data...) were saved: its own folder,
     * statistics/&lt;game&gt;/&lt;day&gt;/&lt;time&gt;_&lt;variant&gt; (20/11/2026; before, the folder of the day).
     */
    void seeGameFolder(Stats stats) {
        // The folder of the saved files (for a replay, the folder of the replayed file)
        final SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();
        final File folder = savedStatsInfo != null && savedStatsInfo.getHeatMapCsvFile() != null
            ? savedStatsInfo.getHeatMapCsvFile().getParentFile()
            : stats.getGameFolder();
        SystemFileManager.openFolder(folder);
    }
}
