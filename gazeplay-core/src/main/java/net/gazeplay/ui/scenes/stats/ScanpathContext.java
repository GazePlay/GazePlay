package net.gazeplay.ui.scenes.stats;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.FixationSequence;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.GraphicalContext;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ScanpathContext extends GraphicalContext<BorderPane> {

    private final Color[] colors = {Color.RED, Color.BLUE};

    public ScanpathContext(GazePlay gazePlay, Stats stats, CustomButton continueButton) {
        super(gazePlay, new BorderPane());

        final Pane center = buildCenterPane(stats);

        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        HomeButton homeButton = new HomeButton(screenDimension);
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> {
            StatsContext statsContext = StatsContextFactory.newInstance(gazePlay, stats, continueButton);
            this.clear();
            gazePlay.onDisplayStats(statsContext);
        });

        HBox bottom = new HBox();
        bottom.getChildren().add(homeButton);

        root.setCenter(center);
        root.setBottom(bottom);
    }

    private Pane buildCenterPane(Stats stats) {
        final Pane center = new Pane();

        SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();
        ImageView scanPathView = new ImageView(new Image(savedStatsInfo.getGazeMetricsFileMouseAndGaze().toURI().toString()));
        center.getChildren().add(scanPathView);

        final List<Circle> pointsMouse = initFixationSequenceCircleList(stats, center, FixationSequence.MOUSE_FIXATION_SEQUENCE);
        center.getChildren().addAll(pointsMouse);

        final List<Circle> pointsGaze = initFixationSequenceCircleList(stats, center, FixationSequence.GAZE_FIXATION_SEQUENCE);
        center.getChildren().addAll(pointsGaze);

        I18NButton displayMouseButton = new I18NButton(getGazePlay().getTranslator(), "Mouse");
        displayMouseButton.setOnMouseClicked((e) -> {
            center.getChildren().removeAll(pointsMouse);
            center.getChildren().removeAll(pointsGaze);
            center.getChildren().addAll(pointsMouse);
            scanPathView.setImage(new Image(savedStatsInfo.getGazeMetricsFileMouse().toURI().toString()));
        });

        I18NButton displayGazeButton = new I18NButton(getGazePlay().getTranslator(), "Gaze");
        displayGazeButton.setOnMouseClicked((e) -> {
            center.getChildren().removeAll(pointsMouse);
            center.getChildren().removeAll(pointsGaze);
            center.getChildren().addAll(pointsGaze);
            scanPathView.setImage(new Image(savedStatsInfo.getGazeMetricsFileGaze().toURI().toString()));
        });

        I18NButton displayMouseAndGazeButton = new I18NButton(getGazePlay().getTranslator(), "MouseAndGaze");
        displayMouseAndGazeButton.setOnMouseClicked((e) -> {
            center.getChildren().removeAll(pointsMouse);
            center.getChildren().removeAll(pointsGaze);
            center.getChildren().addAll(pointsMouse);
            center.getChildren().addAll(pointsGaze);
            scanPathView.setImage(new Image(savedStatsInfo.getGazeMetricsFileMouseAndGaze().toURI().toString()));
        });

        HBox buttonSwitchMetrics = new HBox(displayMouseButton, displayGazeButton, displayMouseAndGazeButton);


        center.getChildren().add(buttonSwitchMetrics);

        return center;
    }

    private List<Circle> initFixationSequenceCircleList(Stats stats, Pane center, int fixationIndex) {
        final List<Circle> points = new LinkedList<>();

        double maxDuration = 0;
        for (final FixationPoint point : stats.getFixationSequence().get(fixationIndex)) {
            if (maxDuration < Math.sqrt(point.getGazeDuration())) {
                maxDuration = Math.sqrt(point.getGazeDuration());
            }
        }

        final double finalMaxDuration = maxDuration;
        stats.getFixationSequence().get(fixationIndex).forEach(p -> {
            Circle newPoint = new Circle();
            newPoint.setOpacity(0);
            newPoint.setCenterX(p.getY());
            newPoint.setCenterY(p.getX());
            newPoint.setRadius((40d + 50d * (Math.sqrt(p.getGazeDuration()) / finalMaxDuration)) / 2);

            points.add(newPoint);

            Text label = new Text();
            label.setText(p.getGazeDuration() + " ms");
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
            label.setStrokeWidth(2);
            label.setStroke(Color.WHITE);
            label.setFill(colors[fixationIndex]);
            label.setX(newPoint.getCenterX() + newPoint.getRadius());
            label.setY(newPoint.getCenterY() - label.getLayoutY());

            newPoint.setOnMouseEntered(s -> {
                center.getChildren().add(label);
                newPoint.setOpacity(0.5);
            });
            newPoint.setOnMouseExited(s -> {
                center.getChildren().remove(label);
                newPoint.setOpacity(0);
            });
        });

        return points;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

}
