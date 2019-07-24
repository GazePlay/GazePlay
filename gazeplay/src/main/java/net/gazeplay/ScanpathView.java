package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.FixationSequence;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ScanpathView extends GraphicalContext<Pane> {

    private Stats stats;
    private ImageView scanPathView;
    private LinkedList<FixationPoint> points;

    public static ScanpathView newInstance(GazePlay gazePlay, Stats stats) {
        Pane root = new Pane();
        return new ScanpathView(gazePlay, root, stats);
    }

    private ScanpathView(GazePlay gazePlay, Pane root, Stats stats) {
        super(gazePlay, root);

        this.stats = stats;
        SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();
        this.scanPathView = new ImageView(new Image(savedStatsInfo.getGazeMetricsFile().toURI().toString()));
        root.getChildren().add(scanPathView);
        this.points = FixationSequence.getSequence();

        List<Ellipse> Points = new LinkedList<Ellipse>();
        for (FixationPoint p : this.points) {
            Ellipse newPoint = new Ellipse();
            //newPoint.setFill(Color.RED);
            newPoint.setOpacity(0.4);
            // newPoint.setStroke(Color.BLACK);
            // newPoint.setStrokeWidth(1);

            newPoint.setCenterX(p.getY());
            newPoint.setCenterY(p.getX());
            newPoint.setRadiusX(23 + (int) p.getGazeDuration() / 100);
            newPoint.setRadiusY(newPoint.getRadiusX());
            Points.add(newPoint);
        }
        log.info("nb points = " + Points.size());
        root.getChildren().addAll(Points);

        for (int i = 0; i < Points.size(); i++) {
            int index = i;
            /**
             * to add a labelBox in the top left corner uncomment the code and make the necessary modifications
             */
            // HBox labelBox = new HBox();
            Text label = new Text();
            Points.get(i).setOnMouseEntered(s -> {

                label.setText(stats.getFixationSequence().get(index).getGazeDuration() + " ms");
                label.setFont(new Font("Verdana", 20));
                label.setStrokeWidth(6);
                label.setFill(Color.BLACK);
                label.setX(Points.get(index).getCenterX() + Points.get(index).getRadiusX());
                label.setY(Points.get(index).getCenterY() - label.getLayoutY());

                // labelBox.setBackground(new Background(new BackgroundFill(Color.BLACK,CornerRadii.EMPTY,
                // Insets.EMPTY)));
                // labelBox.getChildren().add(label);
                root.getChildren().add(label);
            });

            Points.get(i).setOnMouseExited(s -> {
                root.getChildren().remove(label);
            });
        }

        EventHandler<Event> ExitScanpathView = e -> {

            StatsContext statsContext = null;
            try {
                statsContext = StatsContext.newInstance(gazePlay, stats);
            } catch (IOException er) {
                er.printStackTrace();
            }
            this.clear();
            gazePlay.onDisplayStats(statsContext);
        };

        HomeButton homeButton = new HomeButton("data/common/images/home-button.png");
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, ExitScanpathView);

        // StackPane homeButtonPane = new StackPane();
        Dimension2D dimension = GameContext.newInstance(gazePlay).getGamePanelDimensionProvider().getDimension2D();
        homeButton.relocate(15, dimension.getHeight());
        root.getChildren().add(homeButton);
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }
}
