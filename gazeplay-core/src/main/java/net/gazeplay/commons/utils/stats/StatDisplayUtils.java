package net.gazeplay.commons.utils.stats;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.stats.ShootGamesStats;
import net.gazeplay.ui.scenes.stats.StatsContext;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import static javafx.scene.chart.XYChart.Data;

@Slf4j
public class StatDisplayUtils {

    public static HomeButton createHomeButtonInStatsScreen(GazePlay gazePlay, StatsContext statsContext) {
        EventHandler<Event> homeEvent = e -> closeStatsWindow();

        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        HomeButton homeButton = new HomeButton(screenDimension);
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        return homeButton;
    }

    static void closeStatsWindow() {
        Platform.exit();
        System.exit(0);
    }

    public static LineChart<String, Number> buildLineChart(Stats stats, final Region root) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

        // defining a series
        Series<String, Number> series = new Series<>();
        Series<String, Number> average = new Series<>();
        Series<String, Number> sdp = new Series<>();
        Series<String, Number> sdm = new Series<>();
        // populating the series with data

        final List<Long> shots;
        if (stats instanceof ShootGamesStats) {
            shots = stats.getSortedDurationsBetweenGoals();
        } else {
            shots = stats.getOriginalDurationsBetweenGoals();
        }

        double sd = stats.computeRoundsDurationStandardDeviation();

        String xValue = "0";

        average.getData().add(new Data<>(xValue, stats.computeRoundsDurationAverageDuration()));
        sdp.getData().add(new Data<>(xValue, stats.computeRoundsDurationAverageDuration() + sd));
        sdm.getData().add(new Data<>(xValue, stats.computeRoundsDurationAverageDuration() - sd));

        int i = 1;

        for (Long duration : shots) {
            xValue = Integer.toString(i);
            series.getData().add(new Data<>(xValue, duration));
            average.getData().add(new Data<>(xValue, stats.computeRoundsDurationAverageDuration()));

            sdp.getData().add(new Data<>(xValue, stats.computeRoundsDurationAverageDuration() + sd));
            sdm.getData().add(new Data<>(xValue, stats.computeRoundsDurationAverageDuration() - sd));
            i++;
        }

        xValue = Integer.toString(i);
        average.getData().add(new Data<>(xValue, stats.computeRoundsDurationAverageDuration()));
        sdp.getData().add(new Data<>(xValue, stats.computeRoundsDurationAverageDuration() + sd));
        sdm.getData().add(new Data<>(xValue, stats.computeRoundsDurationAverageDuration() - sd));

        lineChart.setCreateSymbols(false);

        lineChart.getData().add(average);
        lineChart.getData().add(sdp);
        lineChart.getData().add(sdm);
        lineChart.getData().add(series);

        series.getNode().setStyle("-fx-stroke-width: 3; -fx-stroke: red; -fx-stroke-dash-offset:5;");
        average.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: lightgreen;");
        sdp.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: grey;");
        sdm.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: grey;");

        EventHandler<Event> openLineChartEvent = createZoomInLineChartEventHandler(lineChart, root);

        lineChart.addEventHandler(MouseEvent.MOUSE_CLICKED, openLineChartEvent);

        lineChart.setLegendVisible(false);

        root.widthProperty().addListener((observable, oldValue, newValue) -> lineChart.setMaxWidth(newValue.doubleValue() * 0.4));
        root.heightProperty().addListener((observable, oldValue, newValue) -> lineChart.setMaxHeight(newValue.doubleValue() * 0.4));
        lineChart.setMaxWidth(root.getWidth() * 0.4);
        lineChart.setMaxHeight(root.getHeight() * 0.4);

        return lineChart;
    }

    public static AreaChart<Number, Number> buildAreaChart(LinkedList<FixationPoint> points, final Region root) {

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setTickLabelsVisible(false);
        yAxis.setLabel("Coordinates");

        AreaChart<Number, Number> colorBands = new AreaChart<>(xAxis, yAxis);
        colorBands.setTitle("Color Bands");

        colorBands.setCreateSymbols(true);
        colorBands.setLegendVisible(true);

        if (points.size() > 0) {

            Series<Number, Number> xEyeCoordinates = new Series<>();
            xEyeCoordinates.setName("X coordinate");

            Series<Number, Number> yEyeCoordinates = new Series<>();
            yEyeCoordinates.setName("Y coordinate");

            for (FixationPoint p : points) {
                xEyeCoordinates.getData().add(new Data<>(p.getTimeGaze(), p.getY()));
                yEyeCoordinates.getData().add(new Data<>(p.getTimeGaze(), p.getX()));
            }

            colorBands.getData().addAll(xEyeCoordinates, yEyeCoordinates);

            EventHandler<Event> openAreaChartEvent = createZoomInAreaChartEventHandler(colorBands, root);

            colorBands.addEventHandler(MouseEvent.MOUSE_CLICKED, openAreaChartEvent);

            root.widthProperty().addListener(
                (observable, oldValue, newValue) -> colorBands.setMaxWidth(newValue.doubleValue() * 0.4));
            root.heightProperty().addListener(
                (observable, oldValue, newValue) -> colorBands.setMaxHeight(newValue.doubleValue() * 0.4));
            colorBands.setMaxWidth(root.getWidth() * 0.4);
            colorBands.setMaxHeight(root.getHeight() * 0.4);

            return colorBands;
        } else
            return null;
    }

    public static LineChart<String, Number> buildLevelChart(Stats stats, final Region root) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        LineChart<String, Number> levelChart = new LineChart<>(xAxis, yAxis);

        // defining a series
        Series<String, Number> series = new Series<>();
        Series<String, Number> average = new Series<>();
        Series<String, Number> sdp = new Series<>();
        Series<String, Number> sdm = new Series<>();
        // populating the series with data

        final List<Long> shots = stats.getLevelsRounds();

        if (stats.getLevelsReport() != null) {
            double sd = stats.getLevelsReport().computeSD();

            String xValue = "0";

            average.getData().add(new Data<>(xValue, stats.getLevelsReport().computeAverageLevel()));
            sdp.getData().add(new Data<>(xValue, stats.getLevelsReport().computeAverageLevel() + sd));
            sdm.getData().add(new Data<>(xValue, stats.getLevelsReport().computeAverageLevel() - sd));

            int i = 1;

            for (Long duration : shots) {
                xValue = Integer.toString(i);
                series.getData().add(new Data<>(xValue, duration));
                average.getData().add(new Data<>(xValue, stats.getLevelsReport().computeAverageLevel()));

                sdp.getData().add(new Data<>(xValue, stats.getLevelsReport().computeAverageLevel() + sd));
                sdm.getData().add(new Data<>(xValue, stats.getLevelsReport().computeAverageLevel() - sd));
                i++;
            }

            xValue = Integer.toString(i);
            average.getData().add(new Data<>(xValue, stats.getLevelsReport().computeAverageLevel()));
            sdp.getData().add(new Data<>(xValue, stats.getLevelsReport().computeAverageLevel() + sd));
            sdm.getData().add(new Data<>(xValue, stats.getLevelsReport().computeAverageLevel() - sd));

            levelChart.setCreateSymbols(false);

            levelChart.getData().add(average);
            levelChart.getData().add(sdp);
            levelChart.getData().add(sdm);
            levelChart.getData().add(series);

            series.getNode().setStyle("-fx-stroke-width: 3; -fx-stroke: purple; -fx-stroke-dash-offset:5;");
            average.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: lightgreen;");
            sdp.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: grey;");
            sdm.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: grey;");

            EventHandler<Event> openLevelChartEvent = createZoomInLineChartEventHandler(levelChart, root);

            levelChart.addEventHandler(MouseEvent.MOUSE_CLICKED, openLevelChartEvent);

            levelChart.setLegendVisible(false);
        }
        root.widthProperty().addListener((observable, oldValue, newValue) -> levelChart.setMaxWidth(newValue.doubleValue() * 0.4));
        root.heightProperty().addListener((observable, oldValue, newValue) -> levelChart.setMaxHeight(newValue.doubleValue() * 0.4));
        levelChart.setMaxWidth(root.getWidth() * 0.4);
        levelChart.setMaxHeight(root.getHeight() * 0.4);

        return levelChart;
    }

    public static TableView<ChiData> buildTable(Stats stats) {
        TableView<ChiData> table = new TableView<>();
        table.setMaxWidth(1200);

        table.setEditable(false);

        if (stats.getChiReport() != null) {
            ObservableList<ChiData> data = stats.getChiReport().createData();

            TableColumn<ChiData, Integer> indexCol = new TableColumn<>("Index");
            indexCol.setMaxWidth(150);
            indexCol.setMinWidth(150);
            indexCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<ChiData, Integer> levelCol = new TableColumn<>("Level");
            levelCol.setMaxWidth(150);
            levelCol.setMinWidth(150);
            levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));

            TableColumn<ChiData, Integer> decisionCol = new TableColumn<>("Decision with a given theoretical chi2 ");
            decisionCol.setMaxWidth(900);
            decisionCol.setMinWidth(900);

            TableColumn<ChiData, String> alpha50 = new TableColumn<>("50%");
            alpha50.setMaxWidth(150);
            alpha50.setMinWidth(150);
            alpha50.setCellValueFactory(new PropertyValueFactory<>("alpha50"));

            TableColumn<ChiData, String> alpha25 = new TableColumn<>("25%");
            alpha25.setMaxWidth(150);
            alpha25.setMinWidth(150);
            alpha25.setCellValueFactory(new PropertyValueFactory<>("alpha25"));

            TableColumn<ChiData, String> alpha10 = new TableColumn<>("10%");
            alpha10.setMaxWidth(150);
            alpha10.setMinWidth(150);
            alpha10.setCellValueFactory(new PropertyValueFactory<>("alpha10"));

            TableColumn<ChiData, String> alpha5 = new TableColumn<>("5%");
            alpha5.setMaxWidth(150);
            alpha5.setMinWidth(150);
            alpha5.setCellValueFactory(new PropertyValueFactory<>("alpha5"));

            TableColumn<ChiData, String> alpha1 = new TableColumn<>("1%");
            alpha1.setMaxWidth(150);
            alpha1.setMinWidth(150);
            alpha1.setCellValueFactory(new PropertyValueFactory<>("alpha1"));

            TableColumn<ChiData, String> alpha05 = new TableColumn<>("0.5%");
            alpha05.setMaxWidth(150);
            alpha05.setMinWidth(150);
            alpha05.setCellValueFactory(new PropertyValueFactory<>("alpha05"));

            decisionCol.getColumns().addAll(alpha50, alpha25, alpha10, alpha5, alpha1, alpha05);

            table.setItems(data);
            table.getColumns().addAll(indexCol, levelCol, decisionCol);
        }
        return table;
    }

    public static ImageView buildGazeMetrics(Stats stats, final Region root) {
        ImageView gazeMetrics = new ImageView();
        gazeMetrics.setPreserveRatio(true);

        SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();
        savedStatsInfo.addObserver((o, arg) -> Platform.runLater(
            () -> gazeMetrics.setImage(new Image(savedStatsInfo.getGazeMetricsFileMouseAndGaze().toURI().toString()))));

        gazeMetrics.setImage(new Image(savedStatsInfo.getGazeMetricsFileMouseAndGaze().toURI().toString()));

        EventHandler<Event> openGazeMetricsEvent = createZoomInGazeMetricsEventHandler(gazeMetrics, root);
        gazeMetrics.addEventHandler(MouseEvent.MOUSE_CLICKED, openGazeMetricsEvent);

        return gazeMetrics;
    }

    private static void resetToOriginalIndexInParent(Node node, int originalIndexInParent) {
        Parent parent = node.getParent();

        VBox parentVBox = (VBox) parent;

        parentVBox.getChildren().remove(node);
        parentVBox.getChildren().add(originalIndexInParent, node);
    }

    private static int getOriginalIndexInParent(Node node) {
        Parent parent = node.getParent();
        VBox parentVBox = (VBox) parent;
        return parentVBox.getChildren().indexOf(node);
    }

    private static EventHandler<Event> createZoomOutAreaChartEventHandler(XYChart<Number, Number> chart,
                                                                          final Region root, int originalIndexInParent) {
        return new EventHandler<>() {
            @Override
            public void handle(Event e) {
                chart.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                zoomOutAndReset(chart);

                resetToOriginalIndexInParent(chart, originalIndexInParent);

                chart.addEventHandler(MouseEvent.MOUSE_CLICKED, createZoomInAreaChartEventHandler(chart, root));
            }

        };
    }

    private static EventHandler<Event> createZoomOutLineChartEventHandler(XYChart<String, Number> chart,
                                                                          final Region root, int originalIndexInParent) {
        return new EventHandler<>() {
            @Override
            public void handle(Event e) {
                chart.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                zoomOutAndReset(chart);

                resetToOriginalIndexInParent(chart, originalIndexInParent);

                chart.addEventHandler(MouseEvent.MOUSE_CLICKED, createZoomInLineChartEventHandler(chart, root));
            }

        };
    }

    private static EventHandler<Event> createZoomInAreaChartEventHandler(XYChart<Number, Number> chart,
                                                                         final Region root) {
        return new EventHandler<>() {
            @Override
            public void handle(Event e) {
                chart.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                int originalIndexInParent = getOriginalIndexInParent(chart);

                zoomInAndCenter(chart, chart.getWidth(), chart.getHeight(), false);

                chart.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    createZoomOutAreaChartEventHandler(chart, root, originalIndexInParent));
            }
        };
    }

    private static EventHandler<Event> createZoomInLineChartEventHandler(XYChart<String, Number> chart,
                                                                         final Region root) {
        return new EventHandler<>() {
            @Override
            public void handle(Event e) {
                chart.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                int originalIndexInParent = getOriginalIndexInParent(chart);

                zoomInAndCenter(chart, chart.getWidth(), chart.getHeight(), false);

                chart.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    createZoomOutLineChartEventHandler(chart, root, originalIndexInParent));
            }
        };
    }

    private static EventHandler<Event> createZoomOutGazeMetricsEventHandler(ImageView gazeMetrics, final Region root,
                                                                            int originalIndexInParent) {
        return new EventHandler<>() {
            @Override
            public void handle(Event e) {
                gazeMetrics.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                zoomOutAndReset(gazeMetrics);

                resetToOriginalIndexInParent(gazeMetrics, originalIndexInParent);

                gazeMetrics.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    createZoomInGazeMetricsEventHandler(gazeMetrics, root));
            }
        };
    }

    private static EventHandler<Event> createZoomInGazeMetricsEventHandler(ImageView gazeMetrics, final Region root) {
        return new EventHandler<>() {
            @Override
            public void handle(Event e) {
                gazeMetrics.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                int originalIndexInParent = getOriginalIndexInParent(gazeMetrics);

                zoomInAndCenter(gazeMetrics, gazeMetrics.getFitWidth(), gazeMetrics.getFitHeight(), true);

                gazeMetrics.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    createZoomOutGazeMetricsEventHandler(gazeMetrics, root, originalIndexInParent));
            }
        };
    }

    private static void zoomOutAndReset(Node node) {
        node.setScaleX(1);
        node.setScaleY(1);
        node.setTranslateX(0);
        node.setTranslateY(0);
    }

    private static void zoomInAndCenter(Node node, double initialWidth, double initialHeight, boolean preserveRatio) {
        Parent parent = node.getParent();

        node.toFront();

        Bounds parentBoundsInParent = parent.getBoundsInLocal();

        double xScaleRatio = parentBoundsInParent.getMaxX() / initialWidth;
        double yScaleRatio = parentBoundsInParent.getMaxY() / initialHeight;

        if (preserveRatio) {
            double bestScaleRatio = Math.min(xScaleRatio, yScaleRatio);
            node.setScaleX(bestScaleRatio);
            node.setScaleY(bestScaleRatio);
        } else {
            node.setScaleX(xScaleRatio);
            node.setScaleY(yScaleRatio);
        }

        Bounds boundsInParent = node.getBoundsInParent();

        double translateX = -1 * Math.abs(boundsInParent.getMinY());
        double translateY = -1 * Math.abs(boundsInParent.getMinY());

        node.setTranslateX(translateX);
        node.setTranslateY(translateY);
    }

    public static String convert(long totalTime) {
        Duration duration = Duration.ofMillis(totalTime);

        // Extract the days from the duration, then take it away so we can format the rest of the string.
        long days = duration.toDaysPart();
        Duration durationLessDays = duration.minusDays(days);

        String result = "";

        if (days > 0) {
            result += String.format("%dd ", days);
        }

        return result + durationLessDays.toString()
            .substring(2)
            .replaceAll("(\\d[HMS])(?!$)", "$1 ")
            .toLowerCase();
    }
}
