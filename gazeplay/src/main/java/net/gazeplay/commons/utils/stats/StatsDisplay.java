package net.gazeplay.commons.utils.stats;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.StatsContext;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.games.bubbles.BubblesGamesStats;

import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.nio.Buffer;
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

    public static ImageView buildHeatChart(Stats stats, final Region root) {
        ImageView heatMap = new ImageView();
        heatMap.setPreserveRatio(true);

        SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();
        savedStatsInfo.addObserver((o, arg) -> {
            Platform.runLater(() -> heatMap.setImage(new Image(savedStatsInfo.getHeatMapPngFile().toURI().toString())));
        });

        heatMap.setImage(new Image(savedStatsInfo.getHeatMapPngFile().toURI().toString()));
        GaussianBlur blur = new GaussianBlur();
        blur.setRadius(10.0);
        heatMap.setEffect(blur);

        EventHandler<Event> openHeatMapEvent = createZoomInImageViewEventHandler(heatMap, root);
        heatMap.addEventHandler(MouseEvent.MOUSE_CLICKED, openHeatMapEvent);

        return heatMap;
    }

    public static ImageView buildFSequenceChart(Stats stats, final Region root) {
        ImageView fix_sequence = new ImageView();
        fix_sequence.setPreserveRatio(true);

        SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();
        savedStatsInfo.addObserver((o, arg) -> {
            Platform.runLater(() -> fix_sequence
                    .setImage(new Image(savedStatsInfo.getFixationPointsPngFile().toURI().toString())));
        });

        fix_sequence.setImage(new Image(savedStatsInfo.getFixationPointsPngFile().toURI().toString()));

        EventHandler<Event> openFixationSeqEvent = createZoomInImageViewEventHandler(fix_sequence, root);
        fix_sequence.addEventHandler(MouseEvent.MOUSE_CLICKED, openFixationSeqEvent);

        return fix_sequence;
    }

    public static ImageView buildFixSeq_and_HeatMap(Stats stats, final Region root) throws IOException {

        ImageView both = new ImageView();
        both.setPreserveRatio(true);

        SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();

        ImageView source = new ImageView();
        ImageView draw_on_top_of_source = new ImageView();

        savedStatsInfo.addObserver((o, arg) -> {
            Platform.runLater(() -> {
                source.setImage(new Image(savedStatsInfo.getHeatMapPngFile().toURI().toString()));
                draw_on_top_of_source.setImage(new Image(savedStatsInfo.getFixationPointsPngFile().toURI().toString()));
            });
        });

        source.setImage(new Image(savedStatsInfo.getHeatMapPngFile().toURI().toString()));
        draw_on_top_of_source.setImage(new Image(savedStatsInfo.getFixationPointsPngFile().toURI().toString()));

        BufferedImage sourceB = SwingFXUtils.fromFXImage(source.getImage(), null);
        BufferedImage drawOnTopB = SwingFXUtils.fromFXImage(draw_on_top_of_source.getImage(), null);

        Graphics g = sourceB.getGraphics();
        g.drawImage(drawOnTopB, 0, 0, null);
        g.dispose();

        Image fixationsOnTopOfHeatmap = SwingFXUtils.toFXImage(sourceB, null);
        both.setImage(fixationsOnTopOfHeatmap);

        EventHandler<Event> openBothEvent = createZoomInImageViewEventHandler(both, root);
        both.addEventHandler(MouseEvent.MOUSE_CLICKED, openBothEvent);

        return both;

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
            final Region root, int originalIndexInParent) {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                lineChart.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                zoomOutAndReset(lineChart);

                resetToOriginalIndexInParent(lineChart, originalIndexInParent);

                lineChart.addEventHandler(MouseEvent.MOUSE_CLICKED, createZoomInLineChartEventHandler(lineChart, root));
            }

        };
    }

    private static EventHandler<Event> createZoomInLineChartEventHandler(LineChart<String, Number> lineChart,
            final Region root) {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                lineChart.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                int originalIndexInParent = getOriginalIndexInParent(lineChart);

                zoomInAndCenter(lineChart, lineChart.getWidth(), lineChart.getHeight(), false);

                lineChart.addEventHandler(MouseEvent.MOUSE_CLICKED,
                        createZoomOutLineChartEventHandler(lineChart, root, originalIndexInParent));
            }
        };
    }

    private static EventHandler<Event> createZoomOutImageViewEventHandler(ImageView i, final Region root,
            int originalIndexInParent) {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                i.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                zoomOutAndReset(i);

                resetToOriginalIndexInParent(i, originalIndexInParent);

                i.addEventHandler(MouseEvent.MOUSE_CLICKED, createZoomInImageViewEventHandler(i, root));
            }
        };
    }

    private static EventHandler<Event> createZoomInImageViewEventHandler(ImageView i, final Region root) {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                i.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);

                int originalIndexInParent = getOriginalIndexInParent(i);

                zoomInAndCenter(i, i.getFitWidth(), i.getFitHeight(), true);

                i.addEventHandler(MouseEvent.MOUSE_CLICKED,
                        createZoomOutImageViewEventHandler(i, root, originalIndexInParent));
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
