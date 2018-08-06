package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.games.bubbles.BubblesGamesStats;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
public class StatsContext extends GraphicalContext<BorderPane> {

    public static final String COLON = "Colon";

    public static StatsContext newInstance(@NonNull GazePlay gazePlay, @NonNull Stats stats) {
        BorderPane root = new BorderPane();

        return new StatsContext(gazePlay, root, stats);
    }

    private final Stats stats;

    private static void addToGrid(GridPane grid, AtomicInteger currentFormRow, I18NText label, Text value) {

        final int COLUMN_INDEX_LABEL = 0;
        final int COLUMN_INDEX_VALUE = 1;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");
        // label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14)); in CSS

        value.setId("item");
        // value.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14)); in CSS

        grid.add(label, COLUMN_INDEX_LABEL, currentRowIndex);
        grid.add(value, COLUMN_INDEX_VALUE, currentRowIndex);

        GridPane.setHalignment(label, HPos.LEFT);
        GridPane.setHalignment(value, HPos.LEFT);
    }

    private StatsContext(GazePlay gazePlay, BorderPane root, Stats stats) {
        super(gazePlay, root);
        this.stats = stats;

        Configuration config = Configuration.getInstance();

        Multilinguism multilinguism = Multilinguism.getSingleton();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(100);
        grid.setVgap(50);
        // grid.setPadding(new Insets(50, 50, 50, 50));

        final Translator translator = gazePlay.getTranslator();

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

            Text value = new Text(String.valueOf(stats.getNbGoals()));

            if (!(stats instanceof ExplorationGamesStats)) {
                addToGrid(grid, currentFormRow, label, value);
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

        {
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
        }

        HomeButton homeButton = StatsDisplay.createHomeButtonInStatsScreen(gazePlay, this);

        HBox controlButtonPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(controlButtonPane);
        controlButtonPane.setAlignment(Pos.CENTER_RIGHT);
        controlButtonPane.getChildren().add(homeButton);

        StackPane centerStackPane = new StackPane();
        centerStackPane.getChildren().add(centerPane);

        Text screenTitleText = new Text(multilinguism.getTrad("StatsTitle", config.getLanguage()));
        // screenTitleText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        screenTitleText.setId("title");
        // screenTitleText.setTextAlignment(TextAlignment.CENTER);

        StackPane topPane = new StackPane();
        topPane.getChildren().add(screenTitleText);

        root.setTop(topPane);
        root.setLeft(grid);
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
