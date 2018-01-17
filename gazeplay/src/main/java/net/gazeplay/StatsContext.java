package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.HiddenItemsGamesStats;
import net.gazeplay.commons.utils.stats.ShootGamesStats;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.StatsDisplay;
import net.gazeplay.games.bubbles.BubblesGamesStats;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
public class StatsContext extends GraphicalContext<BorderPane> {

    public static StatsContext newInstance(@NonNull GazePlay gazePlay, @NonNull Stats stats) {
        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, gazePlay.getPrimaryStage().getWidth(), gazePlay.getPrimaryStage().getHeight(),
                Color.BLACK);

        return new StatsContext(gazePlay, root, scene, stats);
    }

    private final Stats stats;

    private static void addToGrid(GridPane grid, AtomicInteger currentFormRow, Text label, Text value) {

        final int COLUMN_INDEX_LABEL = 0;
        final int COLUMN_INDEX_VALUE = 1;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");
        label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));

        value.setId("item");
        value.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));

        grid.add(label, COLUMN_INDEX_LABEL, currentRowIndex);
        grid.add(value, COLUMN_INDEX_VALUE, currentRowIndex);

        GridPane.setHalignment(label, HPos.LEFT);
        GridPane.setHalignment(value, HPos.LEFT);
    }

    private StatsContext(GazePlay gazePlay, BorderPane root, Scene scene, Stats stats) {
        super(gazePlay, root, scene);
        this.stats = stats;

        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        Multilinguism multilinguism = Multilinguism.getSingleton();

        // to add or not a space before colon (:) according to the language
        String colon = multilinguism.getTrad("Colon", config.getLanguage());
        if (colon.equals("_noSpace")) {
            colon = ": ";
        } else {
            colon = " : ";
        }

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(100);
        grid.setVgap(50);
        grid.setPadding(new Insets(50, 50, 50, 50));

        AtomicInteger currentFormRow = new AtomicInteger(1);

        {
            Text label = new Text(multilinguism.getTrad("TotalLength", config.getLanguage()) + colon);

            Text value = new Text(StatsDisplay.convert(stats.getTotalLength()));

            addToGrid(grid, currentFormRow, label, value);
        }

        {
            final Text label;
            if (stats instanceof BubblesGamesStats) {
                label = new Text(multilinguism.getTrad("BubbleShoot", config.getLanguage()) + colon);
            } else if (stats instanceof ShootGamesStats) {
                label = new Text(multilinguism.getTrad("Shoots", config.getLanguage()) + colon);
            } else if (stats instanceof HiddenItemsGamesStats) {
                label = new Text(multilinguism.getTrad("HiddenItemsShoot", config.getLanguage()) + colon);
            } else {
                label = new Text(multilinguism.getTrad("Score", config.getLanguage()) + colon);
            }

            Text value = new Text(String.valueOf(stats.getNbGoals()));

            addToGrid(grid, currentFormRow, label, value);
        }

        {
            Text label = new Text(multilinguism.getTrad("Length", config.getLanguage()) + colon);

            Text value = new Text(StatsDisplay.convert(stats.getLength()));

            addToGrid(grid, currentFormRow, label, value);
        }

        {
            Text label = new Text();

            if (stats instanceof ShootGamesStats) {
                label = new Text(multilinguism.getTrad("ShootaverageLength", config.getLanguage()) + colon);
            } else if (stats instanceof HiddenItemsGamesStats || stats instanceof BubblesGamesStats) {
                label = new Text(multilinguism.getTrad("AverageLength", config.getLanguage()) + colon);
            }

            Text value = new Text(StatsDisplay.convert(stats.getAverageLength()));

            addToGrid(grid, currentFormRow, label, value);
        }

        {
            Text label = new Text();

            if (stats instanceof ShootGamesStats) {
                label = new Text(multilinguism.getTrad("ShootmedianLength", config.getLanguage()) + colon);
            } else if (stats instanceof HiddenItemsGamesStats || stats instanceof BubblesGamesStats) {
                label = new Text(multilinguism.getTrad("MedianLength", config.getLanguage()) + colon);
            }

            Text value = new Text(StatsDisplay.convert(stats.getMedianLength()));

            addToGrid(grid, currentFormRow, label, value);
        }

        {
            Text label = new Text(multilinguism.getTrad("StandDev", config.getLanguage()) + colon);

            Text value = new Text(StatsDisplay.convert((long) stats.getSD()));

            addToGrid(grid, currentFormRow, label, value);
        }

        {
            Text label = new Text();
            Text value = new Text();

            if (stats instanceof ShootGamesStats && !(stats instanceof BubblesGamesStats)
                    && ((ShootGamesStats) stats).getNbUnCountedShoots() != 0) {

                label = new Text(multilinguism.getTrad("label", config.getLanguage()) + colon);

                value = new Text(String.valueOf(((ShootGamesStats) stats).getNbUnCountedShoots()));
            }

            addToGrid(grid, currentFormRow, label, value);
        }

        VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);

        {
            LineChart<String, Number> chart = StatsDisplay.buildLineChart(stats, scene);
            centerPane.getChildren().add(chart);
        }

        {
            ImageView heatMap = StatsDisplay.BuildHeatChart(stats, scene);
            heatMap.setFitWidth(scene.getWidth() * 0.35);
            heatMap.setFitHeight(scene.getHeight() * 0.35);

            centerPane.getChildren().add(heatMap);
        }

        HomeButton homeButton = StatsDisplay.createHomeButtonInStatsScreen(gazePlay, this);

        HBox controlButtonPane = new HBox();
        controlButtonPane.setAlignment(Pos.TOP_RIGHT);
        controlButtonPane.getChildren().add(homeButton);

        StackPane centerStackPane = new StackPane();
        centerStackPane.getChildren().add(centerPane);

        Text screenTitleText = new Text(multilinguism.getTrad("StatsTitle", config.getLanguage()));
        screenTitleText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        screenTitleText.setId("title");
        screenTitleText.setTextAlignment(TextAlignment.CENTER);

        StackPane topPane = new StackPane();
        topPane.getChildren().add(screenTitleText);

        root.setTop(topPane);
        root.setLeft(grid);
        root.setCenter(centerStackPane);
        root.setBottom(controlButtonPane);

        CssUtil.setPreferredStylesheets(config, scene);

        root.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");

    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

}
