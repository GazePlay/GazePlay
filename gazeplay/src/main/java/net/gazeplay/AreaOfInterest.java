package net.gazeplay;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.CoordinatesTracker;
import net.gazeplay.commons.utils.stats.Stats;

import javax.swing.*;
import java.util.List;
import java.util.Timer;

public class AreaOfInterest extends GraphicalContext<BorderPane>{


    List<CoordinatesTracker> movementHistory;
    Stats stats;
    Label timeLabel;
    Timeline clock;

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    public static AreaOfInterest newInstance(GazePlay gazePlay,Stats stats)
    {
        BorderPane root = new BorderPane();
        return new AreaOfInterest(gazePlay,root,stats);
    }

    private void plotMovement(int index, Pane graphicsPane)
    {
        new Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        CoordinatesTracker coordinatesTracker = movementHistory.get(index);
                        Circle circle = new Circle(coordinatesTracker.getxValue(),coordinatesTracker.getyValue(),3);
                        circle.setStroke(Color.GREEN);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                graphicsPane.getChildren().add(circle);
                                new java.util.Timer().schedule(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {
                                                Platform.runLater(new Runnable() {
                                                    @Override public void run() {
                                                        graphicsPane.getChildren().remove(circle);
                                                    }
                                                });
                                            }
                                        },
                                        2000
                                );
                                if(index != movementHistory.size()-1)
                                {
                                    plotMovement(index+1,graphicsPane);
                                }else {
                                    clock.stop();
                                }
                            }
                        });

                    }
                },
                movementHistory.get(index).getIntervalTime()
        );
    }

    private AreaOfInterest(GazePlay gazePlay,BorderPane root,Stats stats){
        super(gazePlay, root);
        Configuration config = Configuration.getInstance();
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(100);
        grid.setVgap(50);
        grid.setPadding(new Insets(50, 50, 50, 50));

        Multilinguism multilinguism = Multilinguism.getSingleton();
        Text screenTitleText = new Text(multilinguism.getTrad("AreaOfInterest", config.getLanguage()));
        screenTitleText.setId("title");
        StackPane topPane = new StackPane();
        topPane.getChildren().add(screenTitleText);

        VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);
        Pane graphicsPane = new Pane();
        movementHistory = stats.getMovementHistoryWithTime();
        VBox pane = new VBox(1);

        timeLabel = new Label();
        timeLabel.setTextFill(Color.web("#FFFFFF"));
        Button button1 = new Button("Play");
        button1.setOnAction(e -> {
            plotMovement(0,graphicsPane);
            long startTime = System.currentTimeMillis();
            clock = new Timeline(new KeyFrame(Duration.ZERO, f -> {
                long theTime = System.currentTimeMillis() - startTime;
                timeLabel.setText(theTime+"");
            }),
                    new KeyFrame(Duration.millis(1))
            );
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();

        });
        pane.getChildren().add(button1);
        pane.getChildren().add(timeLabel);
        graphicsPane.setStyle("-fx-background-color: #aaaaaa");
        pane.setStyle("-fx-background-color: #224488");
        root.setCenter(graphicsPane);
        root.setTop(topPane);
        root.setBottom(pane);
        root.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
    }
}
