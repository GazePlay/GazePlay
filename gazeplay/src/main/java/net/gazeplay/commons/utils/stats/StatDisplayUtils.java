package net.gazeplay.commons.utils.stats;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.stats.ShootGamesStats;
import net.gazeplay.ui.scenes.stats.StatsContext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static javafx.scene.chart.XYChart.Data;

@Slf4j
public class StatDisplayUtils {

    public static HomeButton createHomeButtonInStatsScreen(GazePlay gazePlay, StatsContext statsContext) {
        EventHandler<Event> homeEvent = e -> returnToMenu(gazePlay, statsContext);

        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        HomeButton homeButton = new HomeButton(screenDimension);
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        return homeButton;
    }

    static void returnToMenu(GazePlay gazePlay, StatsContext statsContext) {
        statsContext.getRoot().setCursor(Cursor.WAIT); // Change cursor to wait style

        BackgroundMusicManager.getInstance().restorePlaylist();
        BackgroundMusicManager.getInstance().previous();
        gazePlay.onReturnToMenu();

        statsContext.getRoot().setCursor(Cursor.DEFAULT); // Change cursor to default style
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

    public static ImageView buildGazeMetrics(Stats stats, final Region root) {
        ImageView gazeMetrics = new ImageView();
        gazeMetrics.setPreserveRatio(true);

        SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();
        savedStatsInfo.addObserver((o, arg) -> Platform.runLater(
            () -> gazeMetrics.setImage(new Image(savedStatsInfo.getGazeMetricsFile().toURI().toString()))));

        gazeMetrics.setImage(new Image(savedStatsInfo.getGazeMetricsFile().toURI().toString()));

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
        Date date = new Date(totalTime);
        DateFormat df = new SimpleDateFormat("dd 'd' HH 'h' mm 'm' ss 's' S 'ms'");

        return df.format(date);
    }
}
