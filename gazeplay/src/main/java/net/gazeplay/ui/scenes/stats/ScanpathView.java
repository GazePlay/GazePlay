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
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.GraphicalContext;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ScanpathView extends GraphicalContext<BorderPane> {

    public ScanpathView(GazePlay gazePlay, Stats stats) {
        super(gazePlay, new BorderPane());

        final Pane center = buildCenterPane(stats);

        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        HomeButton homeButton = new HomeButton(screenDimension);
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> {
            StatsContext statsContext = StatsContext.newInstance(gazePlay, stats);
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
        ImageView scanPathView = new ImageView(new Image(savedStatsInfo.getGazeMetricsFile().toURI().toString()));
        center.getChildren().add(scanPathView);

        final List<Circle> points = new LinkedList<>();

        stats.getFixationSequence().forEach(p -> {
            Circle newPoint = new Circle();
            newPoint.setOpacity(0);
            newPoint.setCenterX(p.getY());
            newPoint.setCenterY(p.getX());
            newPoint.setRadius((20d + Math.sqrt(p.getGazeDuration())) / 2);

            points.add(newPoint);

            Text label = new Text();
            label.setText(p.getGazeDuration() + " ms");
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
            label.setStrokeWidth(2);
            label.setStroke(Color.BLACK);
            label.setFill(Color.RED);
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

        center.getChildren().addAll(points);

        return center;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

}
