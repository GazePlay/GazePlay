package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.*;
import javafx.scene.text.Text;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.games.bubbles.BubblesGamesStats;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
public class StatsContext extends GraphicalContext<BorderPane> {

    public static final String COLON = "Colon";
    private static Boolean ALIGN_LEFT = true;
    private static String currentLanguage;

    public static StatsContext newInstance(@NonNull GazePlay gazePlay, @NonNull Stats stats) throws IOException {
        BorderPane root = new BorderPane();

        return new StatsContext(gazePlay, root, stats);
    }

    public static StatsContext newInstance(@NonNull GazePlay gazePlay, @NonNull Stats stats,
            CustomButton continueButton) throws IOException {
        BorderPane root = new BorderPane();

        return new StatsContext(gazePlay, root, stats, continueButton);
    }

    private final Stats stats;

    private static void addToGrid(GridPane grid, AtomicInteger currentFormRow, I18NText label, Text value) {

        final int COLUMN_INDEX_LABEL_LEFT = 0;
        final int COLUMN_INDEX_INPUT_LEFT = 1;
        final int COLUMN_INDEX_LABEL_RIGHT = 1;
        final int COLUMN_INDEX_INPUT_RIGHT = 0;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");
        // label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14)); in CSS

        value.setId("item");
        // value.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14)); in CSS
        if (ALIGN_LEFT) {
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

    private StatsContext(GazePlay gazePlay, BorderPane root, Stats stats) throws IOException {
        this(gazePlay, root, stats, null);
    }

    private StatsContext(GazePlay gazePlay, BorderPane root, Stats stats, CustomButton continueButton)
            throws IOException {
        super(gazePlay, root);
        this.stats = stats;

        final Translator translator = gazePlay.getTranslator();

        currentLanguage = translator.currentLanguage();

        // Align right for Arabic Language
        if (currentLanguage.equals("ara")) {
            ALIGN_LEFT = false;
        }

        Configuration config = Configuration.getInstance();

        Multilinguism multilinguism = Multilinguism.getSingleton();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(100);
        grid.setVgap(50);
        grid.setPadding(new Insets(50, 50, 50, 50));

        AtomicInteger currentFormRow = new AtomicInteger(1);
        {
            I18NText label = new I18NText(translator, "TotalLength", COLON);
            Text value = new Text(StatsDisplay.convert(stats.computeTotalElapsedDuration()));
            addToGrid(grid, currentFormRow, label, value);
        }

        {
            final I18NText label;
            if (stats instanceof BubblesGamesStats) {
                label = new I18NText(translator, "BubbleShot", COLON);
            } else if (stats instanceof ShootGamesStats) {
                label = new I18NText(translator, "Shots", COLON);
            } else if (stats instanceof HiddenItemsGamesStats) {
                label = new I18NText(translator, "HiddenItemsShot", COLON);
            } else {
                label = new I18NText(translator, "Score", COLON);
            }
            Text value;
            if (stats instanceof BubblesGamesStats) {
                value = new Text(String.valueOf(stats.getNbGoals() / 2));

            } else {
                value = new Text(String.valueOf(stats.getNbGoals()));
            }
            if (!(stats instanceof ExplorationGamesStats)) {
                addToGrid(grid, currentFormRow, label, value);
            }
        }
        {
            final I18NText label;
            if (stats instanceof ShootGamesStats || stats instanceof BubblesGamesStats) {
                label = new I18NText(translator, "HitRate", COLON);
                Text value = new Text(String.valueOf(stats.getShotRatio() + "%"));
                if (!(stats instanceof ExplorationGamesStats)) {
                    addToGrid(grid, currentFormRow, label, value);
                }
            }
        }
        {
            I18NText label = new I18NText(translator, "Length", COLON);

            Text value = new Text(StatsDisplay.convert(stats.getRoundsTotalAdditiveDuration()));

            if (!(stats instanceof ExplorationGamesStats)) {
                addToGrid(grid, currentFormRow, label, value);
            }
        }
        {
            final I18NText label;

            if (stats instanceof ShootGamesStats) {
                label = new I18NText(translator, "ShotaverageLength", COLON);
            } else {
                label = new I18NText(translator, "AverageLength", COLON);
            }

            Text value = new Text(StatsDisplay.convert(stats.computeRoundsDurationAverageDuration()));

            if (!(stats instanceof ExplorationGamesStats)) {
                addToGrid(grid, currentFormRow, label, value);
            }
        }

        {
            final I18NText label;

            if (stats instanceof ShootGamesStats) {
                label = new I18NText(translator, "ShotmedianLength", COLON);
            } else {
                label = new I18NText(translator, "MedianLength", COLON);
            }

            Text value = new Text(StatsDisplay.convert(stats.computeRoundsDurationMedianDuration()));
            if (!(stats instanceof ExplorationGamesStats)) {
                addToGrid(grid, currentFormRow, label, value);
            }
        }

        {
            I18NText label = new I18NText(translator, "StandDev", COLON);

            Text value = new Text(StatsDisplay.convert((long) stats.computeRoundsDurationStandardDeviation()));
            if (!(stats instanceof ExplorationGamesStats)) {
                addToGrid(grid, currentFormRow, label, value);
            }
        }

        {
            if (stats instanceof ShootGamesStats && !(stats instanceof BubblesGamesStats)
                    && ((ShootGamesStats) stats).getNbUnCountedShots() != 0) {

                final I18NText label = new I18NText(translator, "UncountedShot", COLON);

                final Text value = new Text(String.valueOf(((ShootGamesStats) stats).getNbUnCountedShots()));
                if (!(stats instanceof ExplorationGamesStats)) {
                    addToGrid(grid, currentFormRow, label, value);
                }
            }
        }

        VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);

        {
            LineChart<String, Number> chart = StatsDisplay.buildLineChart(stats, root);
            centerPane.getChildren().add(chart);
        }

        if (config.isFixationSequenceDisabled() && !config.isHeatMapDisabled()) {
            ImageView heatMap = StatsDisplay.buildHeatChart(stats, root);

            root.widthProperty().addListener((observable, oldValue, newValue) -> {

                heatMap.setFitWidth(newValue.doubleValue() * 0.35);
            });
            root.heightProperty().addListener((observable, oldValue, newValue) -> {

                heatMap.setFitHeight(newValue.doubleValue() * 0.35);
            });

            heatMap.setFitWidth(root.getWidth() * 0.35);
            heatMap.setFitHeight(root.getHeight() * 0.35);

            centerPane.getChildren().add(heatMap);
        } else if (!config.isFixationSequenceDisabled() && config.isHeatMapDisabled()) {

            ImageView fixSequence = StatsDisplay.buildFSequenceChart(stats, root);

            root.widthProperty().addListener((observable, oldValue, newValue) -> {

                fixSequence.setFitWidth(newValue.doubleValue() * 0.35);
            });
            root.heightProperty().addListener((observable, oldValue, newValue) -> {

                fixSequence.setFitHeight(newValue.doubleValue() * 0.35);
            });

            fixSequence.setFitWidth(root.getWidth() * 0.35);
            fixSequence.setFitHeight(root.getHeight() * 0.35);

            centerPane.getChildren().add(fixSequence);

        } else if (!config.isFixationSequenceDisabled() && !config.isHeatMapDisabled()) { // display both (fixation
                                                                                          // sequence on top of heatmap)

            ImageView fixation_and_heatmap = StatsDisplay.buildFixSeq_and_HeatMap(stats, root);

            root.widthProperty().addListener((observable, oldValue, newValue) -> {

                fixation_and_heatmap.setFitWidth(newValue.doubleValue() * 0.35);
            });
            root.heightProperty().addListener((observable, oldValue, newValue) -> {

                fixation_and_heatmap.setFitHeight(newValue.doubleValue() * 0.35);
            });

            fixation_and_heatmap.setFitWidth(root.getWidth() * 0.35);
            fixation_and_heatmap.setFitHeight(root.getHeight() * 0.35);

            centerPane.getChildren().add(fixation_and_heatmap);

        }

        HomeButton homeButton = StatsDisplay.createHomeButtonInStatsScreen(gazePlay, this);

        //starts from here
        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                AreaOfInterest areaOfInterest = AreaOfInterest.newInstance(gazePlay,stats);
                gazePlay.onDisplayAOI(areaOfInterest);
//                statsContext.getRoot().setCursor(Cursor.WAIT); // Change cursor to wait style

//                gazePlay.onReturnToMenu();

//                statsContext.getRoot().setCursor(Cursor.DEFAULT); // Change cursor to default style
                // }
            }
        };

        HomeButton aoiButton = new HomeButton("data/common/images/aoibtn.png");
        aoiButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        //end here

        HBox controlButtonPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(controlButtonPane);
        controlButtonPane.setAlignment(Pos.CENTER_RIGHT);
        if(config.isAreaOfInterestEnabled())
        controlButtonPane.getChildren().add(aoiButton);
        controlButtonPane.getChildren().add(homeButton);

        if (continueButton != null) {
            controlButtonPane.getChildren().add(continueButton);
        }

        StackPane centerStackPane = new StackPane();
        centerStackPane.getChildren().add(centerPane);

        Text screenTitleText = new Text(multilinguism.getTrad("StatsTitle", config.getLanguage()));
        // screenTitleText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        screenTitleText.setId("title");
        // screenTitleText.setTextAlignment(TextAlignment.CENTER);

        StackPane topPane = new StackPane();
        topPane.getChildren().add(screenTitleText);

        root.setTop(topPane);
        if (ALIGN_LEFT) {
            root.setLeft(grid);
        } else { // Arabic alignment
            root.setRight(grid);
        }
        root.setCenter(centerStackPane);
        root.setBottom(controlButtonPane);
        root.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }
}
