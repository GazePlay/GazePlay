package net.gazeplay.ui.scenes.stats;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.GazePlayArgs;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.I18NTooltip;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.*;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.StatDisplayUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.stats.ExplorationGamesStats;
import net.gazeplay.stats.HiddenItemsGamesStats;
import net.gazeplay.stats.ShootGamesStats;
import net.gazeplay.ui.GraphicalContext;

import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

//import javax.swing.text.TableView;

@Slf4j
public class StatsContext extends GraphicalContext<BorderPane> {

    private final String COLON = "Colon";

    private final double RATIO = 0.35;

    StatsContext(GazePlay gazePlay, BorderPane root, Stats stats, CustomButton continueButton) {
        super(gazePlay, root);

        final Translator translator = gazePlay.getTranslator();

        Locale currentLocale = translator.currentLocale();

        // Align right for Arabic Language
        boolean alignLeft = !currentLocale.getLanguage().equals("ara");

        Configuration config = ActiveConfigurationContext.getInstance();

        String gazeplayType = GazePlayArgs.returnArgs();

        if (gazeplayType.equals("bera")){
            GridPane grid = new GridPane();
            grid.setAlignment(Pos.TOP_CENTER);
            grid.setHgap(50);
            grid.setVgap(25);

            addAllToGrid(stats, translator, grid, alignLeft);

            HBox controlButtonPane;

            RadioButton colorBands = new RadioButton("Color Bands");

            controlButtonPane = createControlButtonPane(gazePlay, stats, config, colorBands, null, continueButton, false);

            StackPane centerStackPane = new StackPane();

            I18NText screenTitleText = new I18NText(translator, "StatsTitle");
            screenTitleText.setId("title");

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

            if (config.isBackgroundDark()) {
                root.setStyle("-fx-background-color: rgba(0,0,0,1); " + "-fx-background-radius: 8px; "
                    + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: rgba(60, 63, 65, 0.7); "
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);" + "-fx-text-fill: white;");
            }
            else {
                root.setStyle("-fx-background-color: #fffaf0; " + "-fx-background-radius: 8px; "
                    + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: white; "
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);"+"-fx-text-fill: black;");
            }

        }else {
            GridPane grid = new GridPane();
            grid.setAlignment(Pos.TOP_CENTER);
            grid.setHgap(100);
            grid.setVgap(50);
            grid.setPadding(new Insets(0, 50, 0, 50));

            addAllToGrid(stats, translator, grid, alignLeft);

            VBox centerPane = new VBox();
            centerPane.setAlignment(Pos.CENTER);

            ImageView displayedMetrics = StatDisplayUtils.buildGazeMetrics(stats, root);
            root.widthProperty().addListener(
                (observable, oldValue, newValue) -> displayedMetrics.setFitWidth(newValue.doubleValue() * RATIO));
            root.heightProperty().addListener(
                (observable, oldValue, newValue) -> displayedMetrics.setFitHeight(newValue.doubleValue() * RATIO));

            displayedMetrics.setFitWidth(root.getWidth() * RATIO);
            displayedMetrics.setFitHeight(root.getHeight() * RATIO);

            centerPane.getChildren().add(displayedMetrics);

            I18NButton displayMouseButton = createDisplayButton("Mouse", stats, displayedMetrics);
            I18NButton displayGazeButton = createDisplayButton("Gaze", stats, displayedMetrics);
            I18NButton displayMouseAndGazeButton = createDisplayButton("MouseAndGaze", stats, displayedMetrics);


            HBox buttonSwitchMetrics = new HBox(displayMouseButton, displayGazeButton, displayMouseAndGazeButton);
            buttonSwitchMetrics.setAlignment(Pos.CENTER);
            centerPane.getChildren().add(buttonSwitchMetrics);

            LineChart<String, Number> lineChart = StatDisplayUtils.buildLineChart(stats, root);
            centerPane.getChildren().add(lineChart);
            RadioButton colorBands = new RadioButton("Color Bands");
            RadioButton levelsInfo = new RadioButton("Levels Info");
            RadioButton chi2Info = new RadioButton("Chi2 Info");

            if (!config.isFixationSequenceDisabled()) {
                LinkedList<FixationPoint> tempSequenceList = new LinkedList<>();
                tempSequenceList.addAll(stats.getFixationSequence().get(FixationSequence.MOUSE_FIXATION_SEQUENCE));
                tempSequenceList.addAll(stats.getFixationSequence().get(FixationSequence.GAZE_FIXATION_SEQUENCE));
                AreaChart<Number, Number> areaChart = StatDisplayUtils.buildAreaChart(tempSequenceList, root);
                LineChart<String, Number> levelChart = StatDisplayUtils.buildLevelChart(stats, root);
                TableView chi2Chart = StatDisplayUtils.buildTable(stats);

                if(config.isBackgroundDark())
                {
                    colorBands.setTextFill(Color.WHITE);
                }
                else {
                    colorBands.setTextFill(Color.BLACK);
                }
                colorBands.getStylesheets().add("data/common/radio.css");

                colorBands.setOnAction(event -> {
                    if (colorBands.isSelected()) {
                        if (levelsInfo.isSelected()) {
                            centerPane.getChildren().remove(levelChart);
                            levelsInfo.setSelected(false);
                        }
                        else if (chi2Info.isSelected()) {
                            centerPane.getChildren().remove(chi2Chart);
                            chi2Info.setSelected(false);
                        }
                        else
                            centerPane.getChildren().remove(lineChart);
                        centerPane.getChildren().add(areaChart);
                        centerPane.getStylesheets().add("data/common/chart.css");
                    } else {
                        centerPane.getChildren().remove(areaChart);
                        centerPane.getChildren().add(lineChart);
                    }
                });

                if(config.isBackgroundDark())
                {
                    levelsInfo.setTextFill(Color.WHITE);
                }
                else {
                    levelsInfo.setTextFill(Color.BLACK);
                }
                levelsInfo.getStylesheets().add("data/common/radio.css");

                levelsInfo.setOnAction(event -> {
                    if (levelsInfo.isSelected()) {
                        if (colorBands.isSelected()) {
                            centerPane.getChildren().remove(areaChart);
                            colorBands.setSelected(false);
                        }
                        else if (chi2Info.isSelected()) {
                            centerPane.getChildren().remove(chi2Chart);
                            chi2Info.setSelected(false);
                        }
                        else
                            centerPane.getChildren().remove(lineChart);
                        //centerPane.getChildren().remove(lineChart);
                        centerPane.getChildren().add(levelChart);
                        centerPane.getStylesheets().add("data/common/chart.css");
                    } else {
                        centerPane.getChildren().remove(levelChart);
                        centerPane.getChildren().add(lineChart);
                    }
                });

                if(config.isBackgroundDark())
                {
                    chi2Info.setTextFill(Color.WHITE);
                }
                else {
                    chi2Info.setTextFill(Color.BLACK);
                }

                chi2Info.getStylesheets().add("data/common/radio.css");

                chi2Info.setOnAction(event -> {
                    if (chi2Info.isSelected()) {
                        if (colorBands.isSelected()) {
                            centerPane.getChildren().remove(areaChart);
                            colorBands.setSelected(false);
                        }
                        if (levelsInfo.isSelected()) {
                            centerPane.getChildren().remove(levelChart);
                            levelsInfo.setSelected(false);
                        }
                        else
                            centerPane.getChildren().remove(lineChart);
                        //centerPane.getChildren().remove(lineChart);
                        centerPane.getChildren().add(chi2Chart);
                        centerPane.getStylesheets().add("data/common/table.css");
                    } else {
                        centerPane.getChildren().remove(chi2Chart);
                        centerPane.getChildren().add(lineChart);
                    }
                });
            }

            HBox controlButtonPane;

            if (stats.getCurrentGameNameCode() != null && stats.getCurrentGameVariant() != null && stats.getCurrentGameNameCode().equals("Ninja") && stats.getCurrentGameVariant().contains("DYNAMIC"))
                controlButtonPane = createControlButtonPane(gazePlay, stats, config, colorBands, levelsInfo, continueButton, true);

            else if (stats.getCurrentGameNameCode() != null && stats.getCurrentGameVariant() != null && stats.getCurrentGameVariant().contains("Dynamic") && stats.getCurrentGameNameCode().contains("Memory"))
                controlButtonPane = createControlButtonPane(gazePlay, stats, config, colorBands, levelsInfo, continueButton, true);

            else if (stats.getCurrentGameNameCode() != null && stats.getCurrentGameVariant() != null && stats.getCurrentGameNameCode().equals("WhereIsTheAnimal") && stats.getCurrentGameVariant().contains("DYNAMIC"))
                controlButtonPane = createControlButtonPane(gazePlay, stats, config, levelsInfo, chi2Info, continueButton, true);

            else if (stats.getCurrentGameNameCode() != null && (stats.getCurrentGameVariant() != null &&
                (stats.getCurrentGameNameCode().contains("WhereIs")) || stats.getCurrentGameNameCode().contains("flags")))
                controlButtonPane = createControlButtonPane(gazePlay, stats, config, colorBands, chi2Info, continueButton, true);

            else
                controlButtonPane = createControlButtonPane(gazePlay, stats, config, colorBands, null, continueButton, false);

            StackPane centerStackPane = new StackPane();
            centerStackPane.getChildren().add(centerPane);

            I18NText screenTitleText = new I18NText(translator, "StatsTitle");
            screenTitleText.setId("title");

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

            if (config.isBackgroundDark()) {
                root.setStyle("-fx-background-color: rgba(0,0,0,1); " + "-fx-background-radius: 8px; "
                    + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: rgba(60, 63, 65, 0.7); "
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);" + "-fx-text-fill: white;");
            }
            else {
                root.setStyle("-fx-background-color: #fffaf0; " + "-fx-background-radius: 8px; "
                    + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: white; "
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);"+"-fx-text-fill: black;");
            }

        }

    }

    I18NButton createDisplayButton(String buttonName, Stats stats, ImageView metrics) {
        I18NButton displayButton = new I18NButton(getGazePlay().getTranslator(), buttonName);
        displayButton.setOnMouseClicked((e) -> {
            SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();
            switch (buttonName) {
                case "Mouse":
                    metrics.setImage(new Image(savedStatsInfo.getGazeMetricsFileMouse().toURI().toString()));
                    break;
                case "Gaze":
                    metrics.setImage(new Image(savedStatsInfo.getGazeMetricsFileGaze().toURI().toString()));
                    break;
                default: // "MouseAndGaze"
                    metrics.setImage(new Image(savedStatsInfo.getGazeMetricsFileMouseAndGaze().toURI().toString()));
                    break;
            }
        });
        return displayButton;
    }

    void addAllToGrid(Stats stats, Translator translator, GridPane grid, boolean alignLeft) {

        String gazeplayType = GazePlayArgs.returnArgs();

        if (gazeplayType.equals("bera")){
            AtomicInteger currentFormRow = new AtomicInteger(0);

            Text value;
            String labelValue;

            if (stats.variantType.equals("SentenceComprehension")){

                addToGridCenter(grid, currentFormRow, translator, "TimeGame", new Text(String.valueOf(stats.timeGame / 100.) + "s"));

                addToGridCenter(grid, currentFormRow, translator, "Morphosyntax", new Text(""));
                addToGridCenter(grid, currentFormRow, translator, "TotalMorphosyntax", new Text(String.valueOf(stats.totalMorphosyntax) + "/10"));
                addToGridCenter(grid, currentFormRow, translator, "SimpleScoreItems", new Text(String.valueOf(stats.simpleScoreItemsMorphosyntax) + "/5"));
                addToGridCenter(grid, currentFormRow, translator, "ComplexScoreItems", new Text(String.valueOf(stats.complexScoreItemsMorphosyntax) + "/5"));
                addToGridCenter(grid, currentFormRow, translator, "ScoreLeftTargetItems", new Text(String.valueOf(stats.scoreLeftTargetItemsMorphosyntax) + "/5"));
                addToGridCenter(grid, currentFormRow, translator, "ScoreRightTargetItems", new Text(String.valueOf(stats.scoreLeftTargetItemsMorphosyntax) + "/5"));

                addToGridCenter(grid, currentFormRow, translator, "SentenceComprehension", new Text(""));
                addToGridCenter(grid, currentFormRow, translator, "TotalItemsAddManually", new Text(String.valueOf(stats.totalItemsAddedManually) + "/10"));
                addToGridCenter(grid, currentFormRow, translator, "TotalSentenceComprehension", new Text(String.valueOf(stats.total) + "/10"));
            }else {

                addToGridCenter(grid, currentFormRow, translator, "TimeGame", new Text(String.valueOf(stats.timeGame / 100) + "s"));

                addToGridCenter(grid, currentFormRow, translator, "Phonology", new Text(""));
                addToGridCenter(grid, currentFormRow, translator, "TotalPhonology", new Text(String.valueOf(stats.totalPhonology) + "/10"));
                addToGridCenter(grid, currentFormRow, translator, "SimpleScoreItems", new Text(String.valueOf(stats.simpleScoreItemsPhonology) + "/5"));
                addToGridCenter(grid, currentFormRow, translator, "ComplexScoreItems", new Text(String.valueOf(stats.complexScoreItemsPhonology) + "/5"));
                addToGridCenter(grid, currentFormRow, translator, "ScoreLeftTargetItems", new Text(String.valueOf(stats.scoreLeftTargetItemsPhonology) + "/5"));
                addToGridCenter(grid, currentFormRow, translator, "ScoreRightTargetItems", new Text(String.valueOf(stats.scoreLeftTargetItemsPhonology) + "/5"));

                addToGridCenter(grid, currentFormRow, translator, "Semantic", new Text(""));
                addToGridCenter(grid, currentFormRow, translator, "TotalSemantic", new Text(String.valueOf(stats.totalSemantic) + "/10"));
                addToGridCenter(grid, currentFormRow, translator, "SimpleScoreItems", new Text(String.valueOf(stats.simpleScoreItemsPhonology) + "/5"));
                addToGridCenter(grid, currentFormRow, translator, "ComplexScoreItems", new Text(String.valueOf(stats.complexScoreItemsPhonology) + "/5"));
                addToGridCenter(grid, currentFormRow, translator, "FrequentScoreItem", new Text(String.valueOf(stats.frequentScoreItemSemantic) + "/5"));
                addToGridCenter(grid, currentFormRow, translator, "InfrequentScoreItem", new Text(String.valueOf(stats.infrequentScoreItemSemantic) + "/5"));
                addToGridCenter(grid, currentFormRow, translator, "ScoreLeftTargetItems", new Text(String.valueOf(stats.scoreLeftTargetItemsSemantic) + "/5"));
                addToGridCenter(grid, currentFormRow, translator, "ScoreRightTargetItems", new Text(String.valueOf(stats.scoreRightTargetItemsSemantic) + "/5"));

                addToGridCenter(grid, currentFormRow, translator, "WordComprehension", new Text(""));
                addToGridCenter(grid, currentFormRow, translator, "TotalWordComprehension", new Text(String.valueOf(stats.totalWordComprehension) + "/20"));
                addToGridCenter(grid, currentFormRow, translator, "TotalItemsAddManually", new Text(String.valueOf(stats.totalItemsAddedManually) + "/20"));
                addToGridCenter(grid, currentFormRow, translator, "Total", new Text(String.valueOf(stats.total) + "/20"));

            }
        }else {
            AtomicInteger currentFormRow = new AtomicInteger(1);

            Text value;
            String labelValue;

            value = new Text(StatDisplayUtils.convert(stats.computeTotalElapsedDuration()));
            addToGrid(grid, currentFormRow, translator, "TotalLength", value, alignLeft);

            if (!(stats instanceof ExplorationGamesStats)) {

                if (stats instanceof ShootGamesStats) {
                    labelValue = "Shots";
                } else if (stats instanceof HiddenItemsGamesStats) {
                    labelValue = "HiddenItemsShot";
                } else {
                    labelValue = "Score";
                }

                value = new Text(String.valueOf(stats.getNbGoalsReached()));
                addToGrid(grid, currentFormRow, translator, labelValue, value, alignLeft);
            }

            if (stats instanceof ShootGamesStats) {
                labelValue = "HitRate";
                value = new Text(stats.getShotRatio() + "% (" + stats.getNbGoalsReached() + "/" + stats.getNbGoalsToReach() + ")");
                addToGrid(grid, currentFormRow, translator, labelValue, value, alignLeft);
            }

            if (!(stats instanceof ExplorationGamesStats)) {
                labelValue = "Length";
                value = new Text(StatDisplayUtils.convert(stats.getRoundsTotalAdditiveDuration()));
                addToGrid(grid, currentFormRow, translator, labelValue, value, alignLeft);
            }

            if (!(stats instanceof ExplorationGamesStats)) {
                labelValue = stats instanceof ShootGamesStats ? "ShotaverageLength" : "AverageLength";
                value = new Text(StatDisplayUtils.convert(stats.computeRoundsDurationAverageDuration()));
                addToGrid(grid, currentFormRow, translator, labelValue, value, alignLeft);
            }

            if (!(stats instanceof ExplorationGamesStats)) {
                labelValue = stats instanceof ShootGamesStats ? "ShotmedianLength" : "MedianLength";
                value = new Text(StatDisplayUtils.convert(stats.computeRoundsDurationMedianDuration()));
                addToGrid(grid, currentFormRow, translator, labelValue, value, alignLeft);
            }

            if (!(stats instanceof ExplorationGamesStats)) {
                labelValue = "StandDev";
                value = new Text(StatDisplayUtils.convert((long) stats.computeRoundsDurationStandardDeviation()));
                addToGrid(grid, currentFormRow, translator, labelValue, value, alignLeft);
            }

            if (stats instanceof ShootGamesStats && stats.getNbUnCountedGoalsReached() != 0) {
                labelValue = "UncountedShot";
                value = new Text(String.valueOf(stats.getNbUnCountedGoalsReached()));
                addToGrid(grid, currentFormRow, translator, labelValue, value, alignLeft);
            }
        }
    }

    private void addToGridCenter(
        GridPane grid,
        AtomicInteger currentFormRow,
        Translator translator,
        String labelText,
        Text value
    ){
        final int columnIndexLabel = 16;
        final int columnIndexInput = 17;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        I18NText label = new I18NText(translator, labelText, COLON);

        label.setId("item");
        value.setId("item");

        grid.add(label, columnIndexLabel, currentRowIndex);
        grid.add(value, columnIndexInput, currentRowIndex);

        GridPane.setHalignment(label, HPos.LEFT);
        GridPane.setHalignment(value, HPos.LEFT);
    }

    private void addToGrid(
        GridPane grid,
        AtomicInteger currentFormRow,
        Translator translator,
        String labelText,
        Text value,
        boolean alignLeft
    ) {
        final int columnIndexLabelLeft = 0;
        final int columnIndexInputLeft = 1;
        final int columnIndexLabelRight = 1;
        final int columnIndexInputRight = 0;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        I18NText label = new I18NText(translator, labelText, COLON);

        label.setId("item");
        value.setId("item");

        if (alignLeft) {
            grid.add(label, columnIndexLabelLeft, currentRowIndex);
            grid.add(value, columnIndexInputLeft, currentRowIndex);

            GridPane.setHalignment(label, HPos.LEFT);
            GridPane.setHalignment(value, HPos.LEFT);
        } else {
            grid.add(value, columnIndexInputRight, currentRowIndex);
            grid.add(label, columnIndexLabelRight, currentRowIndex);

            GridPane.setHalignment(label, HPos.RIGHT);
            GridPane.setHalignment(value, HPos.RIGHT);
        }
    }

    HBox createControlButtonPane(
        GazePlay gazePlay,
        Stats stats,
        Configuration config,
        RadioButton colorBands,
        RadioButton addInfo,
        CustomButton continueButton,
        boolean additionButton
    ) {

        HBox controlButtonPane = new HBox();

        String gazeplayType = GazePlayArgs.returnArgs();

        if (gazeplayType.equals("bera")){
            HomeButton homeButton = StatDisplayUtils.createHomeButtonInStatsScreen(gazePlay, this);

            I18NTooltip tooltipBackToMenu = new I18NTooltip(gazePlay.getTranslator(), "BackToMenu");
            I18NTooltip.install(homeButton, tooltipBackToMenu);

            Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

            CustomButton aoiButton = new CustomButton("data/common/images/aoibtn.png", screenDimension);
            aoiButton.addEventHandler(
                MouseEvent.MOUSE_CLICKED,
                e -> gazePlay.onDisplayAOI(stats));

            EventHandler<Event> viewScanPath = s -> {
                ScanpathView scanPath = new ScanpathView(gazePlay, stats, continueButton);
                gazePlay.onDisplayScanpath(scanPath);
            };

            EventHandler<Event> openFile = s -> {
                this.openFile(stats);
            };

            EventHandler<Event> seeFile = s -> {
                this.seeFile(stats.actualFile);
            };

            CustomButton scanPathButton = new CustomButton("data/common/images/scanpathButton.png", screenDimension);
            scanPathButton.addEventFilter(MouseEvent.MOUSE_CLICKED, viewScanPath);

            CustomButton openExcelButton = new CustomButton("data/common/images/excelButton.png", screenDimension);
            openExcelButton.addEventFilter(MouseEvent.MOUSE_CLICKED, openFile);

            CustomButton seeFileButton = new CustomButton("data/common/images/seeFolder.png", screenDimension);
            seeFileButton.addEventFilter(MouseEvent.MOUSE_CLICKED, seeFile);

            ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(controlButtonPane);
            controlButtonPane.setAlignment(Pos.CENTER_RIGHT);

            controlButtonPane.getChildren().add(seeFileButton);
            controlButtonPane.getChildren().add(openExcelButton);
            controlButtonPane.getChildren().add(homeButton);

            if (continueButton != null) {
                controlButtonPane.getChildren().add(continueButton);
            }
        }else {
            HomeButton homeButton = StatDisplayUtils.createHomeButtonInStatsScreen(gazePlay, this);

            I18NTooltip tooltipBackToMenu = new I18NTooltip(gazePlay.getTranslator(), "BackToMenu");
            I18NTooltip.install(homeButton, tooltipBackToMenu);

            Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

            CustomButton aoiButton = new CustomButton("data/common/images/aoibtn.png", screenDimension);
            aoiButton.addEventHandler(
                MouseEvent.MOUSE_CLICKED,
                e -> gazePlay.onDisplayAOI(stats));

            EventHandler<Event> viewScanPath = s -> {
                ScanpathView scanPath = new ScanpathView(gazePlay, stats, continueButton);
                gazePlay.onDisplayScanpath(scanPath);
            };

            CustomButton scanPathButton = new CustomButton("data/common/images/scanpathButton.png", screenDimension);
            scanPathButton.addEventFilter(MouseEvent.MOUSE_CLICKED, viewScanPath);

            ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(controlButtonPane);
            controlButtonPane.setAlignment(Pos.CENTER_RIGHT);

            if (config.getAreaOfInterestDisabledProperty().getValue())
                controlButtonPane.getChildren().add(aoiButton);

            if (!config.isFixationSequenceDisabled()) {
                controlButtonPane.getChildren().add(colorBands);
                if(additionButton)
                    controlButtonPane.getChildren().add(addInfo);
                controlButtonPane.getChildren().add(scanPathButton);
            }

            controlButtonPane.getChildren().add(homeButton);

            if (continueButton != null) {
                controlButtonPane.getChildren().add(continueButton);
            }
        }

        return controlButtonPane;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    public void openFile(Stats stats){
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(new File(stats.actualFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void seeFile(String pathFile){
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")){
                Runtime.getRuntime().exec("explorer.exe /select," + pathFile);
            }else {
                Runtime.getRuntime().exec("cmd xdg-open," + pathFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
