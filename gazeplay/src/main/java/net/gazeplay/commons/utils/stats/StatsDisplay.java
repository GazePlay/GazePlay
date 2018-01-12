package net.gazeplay.commons.utils.stats;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GazePlay;
import net.gazeplay.StatsContext;
import net.gazeplay.commons.utils.HeatMapUtils;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.games.bubbles.BubblesGamesStats;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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

        EventHandler<Event> openLineChartEvent = openLineChart(lineChart, scene);

        lineChart.addEventHandler(MouseEvent.MOUSE_CLICKED, openLineChartEvent);

        lineChart.setLegendVisible(false);

        lineChart.setMaxWidth(scene.getWidth() * 0.4);
        lineChart.setMaxHeight(scene.getHeight() * 0.4);

        return lineChart;
    }

    public static Rectangle BuildHeatChart(Stats stats, Scene scene) {

        HeatMapUtils.buildHeatMap(stats.getHeatMap());

        Rectangle heatMap = new Rectangle();

        heatMap.setFill(new ImagePattern(new Image("file:" + HeatMapUtils.getHeatMapPath()), 0, 0, 1, 1, true));

        EventHandler<Event> openHeatMapEvent = openHeatMap(heatMap, scene);

        heatMap.addEventHandler(MouseEvent.MOUSE_CLICKED, openHeatMapEvent);

        return heatMap;
    }

    public static EventHandler<Event> closeLineChart(LineChart<String, Number> lineChart, Scene scene) {

        return new EventHandler<Event>() {

            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    lineChart.setTranslateX(scene.getWidth() * 1 / 10);
                    lineChart.setTranslateY(scene.getHeight() / 2);
                    lineChart.setMinWidth(scene.getWidth() * 0.4);
                    lineChart.setMinHeight(scene.getHeight() * 0.4);

                    /*
                     * lineChart.setTranslateX(scene.getWidth()*1/9); lineChart.setTranslateY(scene.getHeight()/2+15);
                     * lineChart.setMinWidth(scene.getWidth()*0.35); lineChart.setMinHeight(scene.getHeight()*0.35);
                     */

                    lineChart.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                    lineChart.addEventHandler(MouseEvent.MOUSE_CLICKED, openLineChart(lineChart, scene));

                }
            }

        };
    }

    public static EventHandler<Event> openLineChart(LineChart<String, Number> lineChart, Scene scene) {

        return new EventHandler<Event>() {

            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    lineChart.setTranslateX(scene.getWidth() * 0.05);
                    lineChart.setTranslateY(scene.getHeight() * 0.05);
                    lineChart.setMinWidth(scene.getWidth() * 0.9);
                    lineChart.setMinHeight(scene.getHeight() * 0.9);

                    lineChart.toFront();

                    lineChart.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                    lineChart.addEventHandler(MouseEvent.MOUSE_CLICKED, closeLineChart(lineChart, scene));

                }
            }
        };
    }

    public static EventHandler<Event> closeHeatMap(Rectangle heatMap, Scene scene) {

        return new EventHandler<Event>() {

            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    heatMap.setX(scene.getWidth() * 5 / 9);
                    heatMap.setY(scene.getHeight() / 2 + 15);
                    heatMap.setWidth(scene.getWidth() * 0.35);
                    heatMap.setHeight(scene.getHeight() * 0.35);

                    heatMap.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                    heatMap.addEventHandler(MouseEvent.MOUSE_CLICKED, openHeatMap(heatMap, scene));

                }
            }

        };
    }

    public static EventHandler<Event> openHeatMap(Rectangle heatMap, Scene scene) {

        return new EventHandler<Event>() {

            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    heatMap.setX(scene.getWidth() * 0.05);
                    heatMap.setY(scene.getHeight() * 0.05);
                    heatMap.setWidth(scene.getWidth() * 0.9);
                    heatMap.setHeight(scene.getHeight() * 0.9);

                    heatMap.toFront();

                    heatMap.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                    heatMap.addEventHandler(MouseEvent.MOUSE_CLICKED, closeHeatMap(heatMap, scene));

                }
            }

        };

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
