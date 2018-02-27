package net.gazeplay.commons.utils.stats;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.StatsContext;
import net.gazeplay.commons.utils.HeatMapUtils;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.games.bubbles.BubblesGamesStats;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Slf4j
public class StatsDisplay {

    public static HomeButton createHomeButtonInStatsScreen(GazePlay gazePlay, StatsContext statsContext) {

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    statsContext.getScene().setCursor(Cursor.WAIT); // Change cursor to wait style

                    gazePlay.onReturnToMenu();

                    statsContext.getScene().setCursor(Cursor.DEFAULT); // Change cursor to default style
                }
            }
        };

        HomeButton homeButton = new HomeButton();
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        return homeButton;
    }

    public static LineChart<String, Number> buildLineChart(Stats stats, Scene scene) {

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

        ArrayList<Integer> shoots = null;

        if (stats instanceof BubblesGamesStats) {

            shoots = stats.getSortedLengthBetweenGoals();
        } else {

            shoots = stats.getLengthBetweenGoals();
        }

        double sd = stats.getSD();

        int i = 0;

        average.getData().add(new XYChart.Data(0 + "", stats.getAverageLength()));
        sdp.getData().add(new XYChart.Data(0 + "", stats.getAverageLength() + sd));
        sdm.getData().add(new XYChart.Data(0 + "", stats.getAverageLength() - sd));

        for (Integer I : shoots) {

            i++;
            series.getData().add(new XYChart.Data(i + "", I.intValue()));
            average.getData().add(new XYChart.Data(i + "", stats.getAverageLength()));

            sdp.getData().add(new XYChart.Data(i + "", stats.getAverageLength() + sd));
            sdm.getData().add(new XYChart.Data(i + "", stats.getAverageLength() - sd));
        }

        i++;
        average.getData().add(new XYChart.Data(i + "", stats.getAverageLength()));
        sdp.getData().add(new XYChart.Data(i + "", stats.getAverageLength() + sd));
        sdm.getData().add(new XYChart.Data(i + "", stats.getAverageLength() - sd));

        lineChart.setCreateSymbols(false);

        lineChart.getData().add(average);
        lineChart.getData().add(sdp);
        lineChart.getData().add(sdm);
        lineChart.getData().add(series);

        series.getNode().setStyle("-fx-stroke-width: 3; -fx-stroke: red; -fx-stroke-dash-offset:5;");
        average.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: lightgreen;");
        sdp.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: grey;");
        sdm.getNode().setStyle("-fx-stroke-width: 1; -fx-stroke: grey;");

        EventHandler<Event> openLineChartEvent = createZoomInLineChartEventHandler(lineChart, scene);

        lineChart.addEventHandler(MouseEvent.MOUSE_CLICKED, openLineChartEvent);

        lineChart.setLegendVisible(false);

        lineChart.setMaxWidth(scene.getWidth() * 0.4);
        lineChart.setMaxHeight(scene.getHeight() * 0.4);

        return lineChart;
    }

    public static ImageView buildHeatChart(Stats stats, Scene scene) {

        HeatMapUtils.buildHeatMap(stats.getHeatMap());

        Image image = new Image("file:" + HeatMapUtils.getHeatMapPath());

        ImageView heatMap = new ImageView(image);
        heatMap.setPreserveRatio(true);

        EventHandler<Event> openHeatMapEvent = createZoomInHeatMapEventHandler(heatMap, scene);

        heatMap.addEventHandler(MouseEvent.MOUSE_CLICKED, openHeatMapEvent);

        return heatMap;
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

    private static EventHandler<Event> createZoomOutLineChartEventHandler(LineChart<String, Number> lineChart,
            Scene scene, int originalIndexInParent) {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                lineChart.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                zoomOutAndReset(lineChart);

                resetToOriginalIndexInParent(lineChart, originalIndexInParent);

                lineChart.addEventHandler(MouseEvent.MOUSE_CLICKED,
                        createZoomInLineChartEventHandler(lineChart, scene));
            }

        };
    }

    private static EventHandler<Event> createZoomInLineChartEventHandler(LineChart<String, Number> lineChart,
            Scene scene) {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                lineChart.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                int originalIndexInParent = getOriginalIndexInParent(lineChart);

                zoomInAndCenter(lineChart, lineChart.getWidth(), lineChart.getHeight(), false);

                lineChart.addEventHandler(MouseEvent.MOUSE_CLICKED,
                        createZoomOutLineChartEventHandler(lineChart, scene, originalIndexInParent));
            }
        };
    }

    private static EventHandler<Event> createZoomOutHeatMapEventHandler(ImageView heatMap, Scene scene,
            int originalIndexInParent) {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                heatMap.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                zoomOutAndReset(heatMap);

                resetToOriginalIndexInParent(heatMap, originalIndexInParent);

                heatMap.addEventHandler(MouseEvent.MOUSE_CLICKED, createZoomInHeatMapEventHandler(heatMap, scene));
            }
        };
    }

    private static EventHandler<Event> createZoomInHeatMapEventHandler(ImageView heatMap, Scene scene) {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                heatMap.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                int originalIndexInParent = getOriginalIndexInParent(heatMap);

                zoomInAndCenter(heatMap, heatMap.getFitWidth(), heatMap.getFitHeight(), true);

                heatMap.addEventHandler(MouseEvent.MOUSE_CLICKED,
                        createZoomOutHeatMapEventHandler(heatMap, scene, originalIndexInParent));
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
