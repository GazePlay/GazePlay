package net.gazeplay.ui.scenes.stats;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.stats.StatDisplayUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.stats.ExplorationGamesStats;
import net.gazeplay.stats.HiddenItemsGamesStats;
import net.gazeplay.stats.ShootGamesStats;
import net.gazeplay.ui.GraphicalContext;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class StatsContext extends GraphicalContext<BorderPane> {

    private static final String COLON = "Colon";

    private static final double RATIO = 0.35;

    public static StatsContext newInstance(
        @NonNull GazePlay gazePlay,
        @NonNull Stats stats
    ) {
        BorderPane root = new BorderPane();
        return new StatsContext(gazePlay, root, stats);
    }

    public static StatsContext newInstance(
        @NonNull GazePlay gazePlay,
        @NonNull Stats stats,
        CustomButton continueButton
    ) {
        BorderPane root = new BorderPane();
        return new StatsContext(gazePlay, root, stats, continueButton);
    }

    private static void addToGrid(GridPane grid, AtomicInteger currentFormRow, I18NText label, Text value, boolean alignLeft) {

        final int COLUMN_INDEX_LABEL_LEFT = 0;
        final int COLUMN_INDEX_INPUT_LEFT = 1;
        final int COLUMN_INDEX_LABEL_RIGHT = 1;
        final int COLUMN_INDEX_INPUT_RIGHT = 0;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");
        // label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14)); in CSS

        value.setId("item");
        // value.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14)); in CSS
        if (alignLeft) {
            grid.add(label, COLUMN_INDEX_LABEL_LEFT, currentRowIndex);
            grid.add(value, COLUMN_INDEX_INPUT_LEFT, currentRowIndex);

            GridPane.setHalignment(label, HPos.LEFT);
            GridPane.setHalignment(value, HPos.LEFT);
        } else {
            grid.add(value, COLUMN_INDEX_INPUT_RIGHT, currentRowIndex);
            grid.add(label, COLUMN_INDEX_LABEL_RIGHT, currentRowIndex);

            GridPane.setHalignment(label, HPos.RIGHT);
            GridPane.setHalignment(value, HPos.RIGHT);
        }
    }

    private StatsContext(GazePlay gazePlay, BorderPane root, Stats stats) {
        this(gazePlay, root, stats, null);
    }

    private StatsContext(GazePlay gazePlay, BorderPane root, Stats stats, CustomButton continueButton) {
        super(gazePlay, root);

        final Translator translator = gazePlay.getTranslator();

        Locale currentLocale = translator.currentLocale();

        boolean alignLeft = true;
        // Align right for Arabic Language
        if (currentLocale.getLanguage().equals("ara")) {
            alignLeft = false;
        }

        Configuration config = ActiveConfigurationContext.getInstance();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(100);
        grid.setVgap(50);
        grid.setPadding(new Insets(0, 50, 0, 50));

        AtomicInteger currentFormRow = new AtomicInteger(1);
        {
            I18NText label = new I18NText(translator, "TotalLength", COLON);
            Text value = new Text(StatDisplayUtils.convert(stats.computeTotalElapsedDuration()));
            addToGrid(grid, currentFormRow, label, value, alignLeft);
        }

        {
            final I18NText label;
            if (stats instanceof ShootGamesStats) {
                label = new I18NText(translator, "Shots", COLON);
            } else if (stats instanceof HiddenItemsGamesStats) {
                label = new I18NText(translator, "HiddenItemsShot", COLON);
            } else {
                label = new I18NText(translator, "Score", COLON);
            }

            Text value;
            value = new Text(String.valueOf(stats.getNbGoals()));

            if (!(stats instanceof ExplorationGamesStats)) {
                addToGrid(grid, currentFormRow, label, value, alignLeft);
            }
        }
        {
            final I18NText label;
            if (stats instanceof ShootGamesStats) {
                label = new I18NText(translator, "HitRate", COLON);
                Text value = new Text(stats.getShotRatio() + "%");
                addToGrid(grid, currentFormRow, label, value, alignLeft);
            }
        }
        {
            I18NText label = new I18NText(translator, "Length", COLON);
            Text value = new Text(StatDisplayUtils.convert(stats.getRoundsTotalAdditiveDuration()));
            if (!(stats instanceof ExplorationGamesStats)) {
                addToGrid(grid, currentFormRow, label, value, alignLeft);
            }
        }
        {
            final I18NText label;

            if (stats instanceof ShootGamesStats) {
                label = new I18NText(translator, "ShotaverageLength", COLON);
            } else {
                label = new I18NText(translator, "AverageLength", COLON);
            }

            Text value = new Text(StatDisplayUtils.convert(stats.computeRoundsDurationAverageDuration()));

            if (!(stats instanceof ExplorationGamesStats)) {
                addToGrid(grid, currentFormRow, label, value, alignLeft);
            }
        }

        {
            final I18NText label;

            if (stats instanceof ShootGamesStats) {
                label = new I18NText(translator, "ShotmedianLength", COLON);
            } else {
                label = new I18NText(translator, "MedianLength", COLON);
            }

            Text value = new Text(StatDisplayUtils.convert(stats.computeRoundsDurationMedianDuration()));
            if (!(stats instanceof ExplorationGamesStats)) {
                addToGrid(grid, currentFormRow, label, value, alignLeft);
            }
        }

        {
            I18NText label = new I18NText(translator, "StandDev", COLON);

            Text value = new Text(StatDisplayUtils.convert((long) stats.computeRoundsDurationStandardDeviation()));
            if (!(stats instanceof ExplorationGamesStats)) {
                addToGrid(grid, currentFormRow, label, value, alignLeft);
            }
        }

        {
            if (stats instanceof ShootGamesStats && stats.getNbUnCountedShots() != 0) {
                final I18NText label = new I18NText(translator, "UncountedShot", COLON);
                final Text value = new Text(String.valueOf(stats.getNbUnCountedShots()));
                addToGrid(grid, currentFormRow, label, value, alignLeft);
            }
        }

        VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);

        ImageView gazeMetrics = StatDisplayUtils.buildGazeMetrics(stats, root);
        root.widthProperty().addListener((observable, oldValue, newValue) -> gazeMetrics.setFitWidth(newValue.doubleValue() * RATIO));
        root.heightProperty().addListener((observable, oldValue, newValue) -> gazeMetrics.setFitHeight(newValue.doubleValue() * RATIO));

        gazeMetrics.setFitWidth(root.getWidth() * RATIO);
        gazeMetrics.setFitHeight(root.getHeight() * RATIO);

        centerPane.getChildren().add(gazeMetrics);

        // charts

        LineChart<String, Number> lineChart = StatDisplayUtils.buildLineChart(stats, root);
        centerPane.getChildren().add(lineChart);
        AreaChart<Number, Number> areaChart;
        RadioButton colorBands = new RadioButton("Color Bands");
        if (!config.isFixationSequenceDisabled()) {
            areaChart = StatDisplayUtils.buildAreaChart(stats.getFixationSequence(), root);

            colorBands.setTextFill(Color.WHITE);
            colorBands.getStylesheets().add("data/common/radio.css");

            colorBands.setOnAction(event -> {
                if (colorBands.isSelected()) {
                    centerPane.getChildren().remove(lineChart);
                    centerPane.getChildren().add(areaChart);
                    centerPane.getStylesheets().add("data/common/chart.css");

                } else {
                    centerPane.getChildren().remove(areaChart);
                    centerPane.getChildren().add(lineChart);
                }
            });
        }

        HomeButton homeButton = StatDisplayUtils.createHomeButtonInStatsScreen(gazePlay, this);

        EventHandler<Event> AOIEvent = e -> {
            gazePlay.onDisplayAOI(stats);
        };

        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        CustomButton aoiButton = new CustomButton("data/common/images/aoibtn.png", screenDimension);
        aoiButton.addEventHandler(MouseEvent.MOUSE_CLICKED, AOIEvent);

        EventHandler<Event> viewScanpath = s -> {
            ScanpathView scanpath = new ScanpathView(gazePlay, stats);
            gazePlay.onDisplayScanpath(scanpath);
        };

        CustomButton scanpathButton = new CustomButton("data/common/images/scanpathButton.png", screenDimension);
        scanpathButton.addEventFilter(MouseEvent.MOUSE_CLICKED, viewScanpath);

        HBox controlButtonPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(controlButtonPane);
        controlButtonPane.setAlignment(Pos.CENTER_RIGHT);

        if (config.getAreaOfInterestDisabledProperty().getValue())
            controlButtonPane.getChildren().add(aoiButton);
        if (!config.isFixationSequenceDisabled()) {
            controlButtonPane.getChildren().add(colorBands);
            controlButtonPane.getChildren().add(scanpathButton);
        }

        controlButtonPane.getChildren().add(homeButton);

        if (continueButton != null) {
            controlButtonPane.getChildren().add(continueButton);
        }

        StackPane centerStackPane = new StackPane();
        centerStackPane.getChildren().add(centerPane);

        I18NText screenTitleText = new I18NText(translator, "StatsTitle");
        // screenTitleText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        screenTitleText.setId("title");
        // screenTitleText.setTextAlignment(TextAlignment.CENTER);

        StackPane topPane = new StackPane();
        topPane.getChildren().add(screenTitleText);

        root.setTop(topPane);

        BorderPane sidePane = new BorderPane();

        if (alignLeft) {
            sidePane.setTop(centerStackPane);
            root.setCenter(sidePane);
            root.setLeft(grid);
        } else { // Arabic alignment
            sidePane.setTop(grid);
            root.setCenter(centerStackPane);
            root.setRight(sidePane);
        }
        sidePane.setBottom(controlButtonPane);

        root.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }
}
