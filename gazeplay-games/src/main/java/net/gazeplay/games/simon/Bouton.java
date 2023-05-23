package net.gazeplay.games.simon;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

public class Bouton extends Arc {

    private final IGameContext gameContext;
    private final Simon gameInstance;
    private EventHandler<Event> enterEvent;
    private ProgressIndicator progressIndicator;
    private Timeline timelineProgressBar;
    private String pos;

    public Bouton(double centerX, double centerY, double radiusX, double radiusY, double startAngle, double length, IGameContext gameContext, Simon gameInstance, String pos) {
        super(centerX, centerY, radiusX, radiusY, startAngle, length);
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.pos = pos;
        setType(ArcType.ROUND);
        this.enterEvent = buildEvent();
        this.progressIndicator = createProgressIndicator(150,150);

        this.gameContext.getGazeDeviceManager().addEventFilter(this);
        this.addEventFilter(GazeEvent.ANY, enterEvent);
        this.addEventFilter(MouseEvent.ANY, enterEvent);
        gameContext.getChildren().add(this.progressIndicator);
    }

    private ProgressIndicator createProgressIndicator(final double width, final double height){

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final ProgressIndicator indicator = new ProgressIndicator(0);

        switch (pos) {
            case "topleft" -> {
                indicator.setTranslateX(dimension2D.getWidth() / 2 - 250);
                indicator.setTranslateY(dimension2D.getHeight() / 2 - 250);
            }
            case "topright" -> {
                indicator.setTranslateX(dimension2D.getWidth() / 2 + 150);
                indicator.setTranslateY(dimension2D.getHeight() / 2 - 250);
            }
            case "bottomleft" -> {
                indicator.setTranslateX(dimension2D.getWidth() / 2 - 250);
                indicator.setTranslateY(dimension2D.getHeight() / 2 + 150);
            }
            case "bottomright" -> {
                indicator.setTranslateX(dimension2D.getWidth() / 2 + 150);
                indicator.setTranslateY(dimension2D.getHeight() / 2 + 150);
            }
        }

        indicator.setMouseTransparent(true);
        indicator.setMinWidth(width);
        indicator.setMinHeight(height);
        indicator.setOpacity(0);

        return indicator;
    }

    private EventHandler<Event> buildEvent(){
        return e->{
            if (e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == MouseEvent.MOUSE_ENTERED){
                initTimer();
            }else if (e.getEventType() == GazeEvent.GAZE_EXITED || e.getEventType() == MouseEvent.MOUSE_EXITED){
                stopTimer();
            }
        };
    }
    private void stopTimer(){
        if (timelineProgressBar != null){
            this.setOpacity(1);
            timelineProgressBar.stop();
            progressIndicator.setOpacity(0);
            progressIndicator.setProgress(0);

        }
    }

    private void initTimer(){
        this.setOpacity(0.8);
        progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
        progressIndicator.toFront();
        progressIndicator.setOpacity(1);
        progressIndicator.setProgress(0);
        timelineProgressBar = new Timeline();
        timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()),
            new KeyValue(progressIndicator.progressProperty(), 1)));

        timelineProgressBar.setOnFinished(actionEvent -> {
            System.out.println("oof");
        });

        timelineProgressBar.play();
    }
}
