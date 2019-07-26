package net.gazeplay.commons.utils.stats;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.StatsContext;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.games.bubbles.BubblesGamesStats;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class StatsDisplay {

    public static HomeButton createHomeButtonInStatsScreen(GazePlay gazePlay, StatsContext statsContext) {

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                // if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                statsContext.getRoot().setCursor(Cursor.WAIT); // Change cursor to wait style

                gazePlay.onReturnToMenu();

                statsContext.getRoot().setCursor(Cursor.DEFAULT); // Change cursor to default style
                // }
            }
        };

        HomeButton homeButton = new HomeButton();
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        return homeButton;
    }

    public static LineChart<String, Number> buildLineChart(Stats stats, final Region root) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis);

        // lineChart.setTitle("Réaction");
        // defining a series
        XYChart.Series series = new XYChart.Series();
        // series.setName("Temps de réaction");

        XYChart.Series average = new XYChart.Series();

        XYChart.Series sdp = new XYChart.Series();

        XYChart.Series sdm = new XYChart.Series();
        // populating the series with data

        final List<Long> shoots;
        if (stats instanceof BubblesGamesStats) {
            shoots = stats.getSortedDurationsBetweenGoals();
        } else if (stats instanceof ShootGamesStats) {
            shoots = stats.getSortedDurationsBetweenGoals();
        } else {
            shoots = stats.getOriginalDurationsBetweenGoals();
        }
        // for (Long shoot : shoots) {
        // System.out.println("The interval is at " + shoot);
        // }
        double sd = stats.computeRoundsDurationStandardDeviation();

        int i = 0;

        average.getData().add(new XYChart.Data(0 + "", stats.computeRoundsDurationAverageDuration()));
        sdp.getData().add(new XYChart.Data(0 + "", stats.computeRoundsDurationAverageDuration() + sd));
        sdm.getData().add(new XYChart.Data(0 + "", stats.computeRoundsDurationAverageDuration() - sd));

        for (Long duration : shoots) {

            i++;
            series.getData().add(new XYChart.Data(i + "", duration.intValue()));
            average.getData().add(new XYChart.Data(i + "", stats.computeRoundsDurationAverageDuration()));

            sdp.getData().add(new XYChart.Data(i + "", stats.computeRoundsDurationAverageDuration() + sd));
            sdm.getData().add(new XYChart.Data(i + "", stats.computeRoundsDurationAverageDuration() - sd));
        }

        i++;
        average.getData().add(new XYChart.Data(i + "", stats.computeRoundsDurationAverageDuration()));
        sdp.getData().add(new XYChart.Data(i + "", stats.computeRoundsDurationAverageDuration() + sd));
        sdm.getData().add(new XYChart.Data(i + "", stats.computeRoundsDurationAverageDuration() - sd));

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

        root.widthProperty().addListener((observable, oldValue, newValue) -> {

            lineChart.setMaxWidth(newValue.doubleValue() * 0.4);
        });
        root.heightProperty().addListener((observable, oldValue, newValue) -> {

            lineChart.setMaxHeight(newValue.doubleValue() * 0.4);
        });
        lineChart.setMaxWidth(root.getWidth() * 0.4);
        lineChart.setMaxHeight(root.getHeight() * 0.4);

        return lineChart;
    }

    public static AreaChart<Number, Number> buildAreaChart(LinkedList<FixationPoint> points, final Region root) {

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Coordinates");

        AreaChart<Number, Number> colorBands = new AreaChart<Number, Number>(xAxis, yAxis);
        colorBands.setTitle("Color Bands");

        colorBands.setCreateSymbols(true);
        colorBands.setLegendVisible(true);

        XYChart.Series xEyeCoordinates = new XYChart.Series();
        xEyeCoordinates.setName("X coordinate");
        for (FixationPoint p : points) {
            xEyeCoordinates.getData().add(new XYChart.Data(p.getFirstGaze(), p.getY()));
        }

        XYChart.Series yEyeCoordinates = new XYChart.Series();
        yEyeCoordinates.setName("Y coordinate");
        for (FixationPoint p : points) {
            yEyeCoordinates.getData().add(new XYChart.Data(p.getFirstGaze(), p.getX()));
        }
        xAxis.setTickLabelsVisible(false);


        colorBands.getData().addAll(xEyeCoordinates, yEyeCoordinates);
       
        //colorBands.getStylesheets().add("data/common/chart.css"); // doesn't affect anything in

        EventHandler<Event> openAreaChartEvent = createZoomInAreaChartEventHandler(colorBands, root);

        colorBands.addEventHandler(MouseEvent.MOUSE_CLICKED, openAreaChartEvent);

        root.widthProperty().addListener((observable, oldValue, newValue) -> {

            colorBands.setMaxWidth(newValue.doubleValue() * 0.4);
        });
        root.heightProperty().addListener((observable, oldValue, newValue) -> {

            colorBands.setMaxHeight(newValue.doubleValue() * 0.4);
        });
        colorBands.setMaxWidth(root.getWidth() * 0.4);
        colorBands.setMaxHeight(root.getHeight() * 0.4);

        return colorBands;

    }

    public static ImageView buildGazeMetrics(Stats stats, final Region root) {
        ImageView gazeMetrics = new ImageView();
        gazeMetrics.setPreserveRatio(true);

        SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();
        savedStatsInfo.addObserver((o, arg) -> {
            Platform.runLater(
                    () -> gazeMetrics.setImage(new Image(savedStatsInfo.getGazeMetricsFile().toURI().toString())));
        });

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
        return new EventHandler<Event>() {
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
        return new EventHandler<Event>() {
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
        return new EventHandler<Event>() {
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
        return new EventHandler<Event>() {
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
        return new EventHandler<Event>() {
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
        return new EventHandler<Event>() {
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

        // if(Configuration.getInstance().isFixationSequenceDisabled()) {
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
        // }
        //
        // else{
        //
        // if(node.getParent() instanceof VBox){
        // VBox n1 = (VBox) node.getParent();
        // if(n1.getParent() instanceof StackPane){
        // StackPane n2 = (StackPane) n1.getParent();
        // if(n2.getParent() instanceof BorderPane){
        // BorderPane root = (BorderPane) n2.getParent();
        // ImageView scanpath = (ImageView) node;
        // scanpath.setFitHeight(scanpath.getImage().getHeight());
        // scanpath.setFitWidth(scanpath.getImage().getWidth());
        //
        // StackPane scanPath = new StackPane(scanpath);
        // scanPath.setAlignment(Pos.CENTER);
        // scanPath.setPrefHeight(root.getHeight());
        // scanPath.setPrefWidth(root.getWidth());
        // //root.getChildren().add(scanPath);
        // root.setCenter(scanPath);
        // scanPath.toFront();
        // }
        // }
        // }
        // }

    }

    public static String convert(long totalTime) {

        long days = TimeUnit.MILLISECONDS.toDays(totalTime);
        totalTime -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(totalTime);
        totalTime -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime);
        totalTime -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime);
        totalTime -= TimeUnit.SECONDS.toMillis(seconds);

        StringBuilder builder = new StringBuilder(1000);

        if (days > 0) {
            builder.append(days);
            builder.append(" d ");
        }
        if (hours > 0) {
            builder.append(hours);
            builder.append(" h ");
        }
        if (minutes > 0) {
            builder.append(minutes);
            builder.append(" m ");
        }
        if (seconds > 0) {
            builder.append(seconds);
            builder.append(" s ");
        }
        if (totalTime > 0) {
            builder.append(totalTime);
            builder.append(" ms");
        }

        return builder.toString();
    }
}
