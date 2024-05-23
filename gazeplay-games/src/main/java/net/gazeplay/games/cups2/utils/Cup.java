package net.gazeplay.games.cups2.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.CupsAndBalls;

public class Cup extends ImageView {

    private final ProgressIndicator progressIndicator;
    private final Callback<Cup, Void> selectionCallback;
    private Timeline progressTimeline;
    private boolean selectionEnabled = false;

    public void enableSelection() {
        selectionEnabled = true;
    }

    public void disableSelection() {
        handleEvent(new InputEvent(MouseEvent.MOUSE_EXITED));
        selectionEnabled = false;
    }

    private final Callback<Void, Void> updateCallback = unused -> {
        this.update();
        return null;
    };

    @Getter
    private int currentIndex;

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
        update();
    }

    @Getter
    Ball ball = null;

    public boolean hasBall() {
        return ball != null;
    }

    public Cup(int xIndex, Callback<Cup, Void> onSelect) {
        this.currentIndex = xIndex;

        setImage(new Image(Config.CUP_IMAGE_PATH));
        setFitWidth(Config.CUP_WIDTH);
        setFitHeight(getImage().getHeight() * Config.CUP_WIDTH / getImage().getWidth());
        setX(computeX(xIndex));
        setY((CupsAndBalls.getGameContext().getGamePanelDimensionProvider().getDimension2D().getHeight() - getFitHeight()) / 2);
        Config.nbCupsSubscribe(updateCallback);

        this.progressIndicator = createProgressIndicator();
        this.selectionCallback = onSelect;
        addEventFilter(MouseEvent.ANY, this::handleEvent);
        addEventFilter(GazeEvent.ANY, this::handleEvent);
        CupsAndBalls.getGameContext().getGazeDeviceManager().addEventFilter(this);
    }

    public void dispose() {
        Config.nbCupsUnsubscribe(updateCallback);
    }

    public void update() {
        setX(computeX(currentIndex));
        setTranslateX(0);
        if (hasBall())
            ball.update();
        progressIndicator.setTranslateX(getX());
    }

    private ProgressIndicator createProgressIndicator() {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(getX());
        indicator.setTranslateY(getY());
        indicator.setPrefSize(getFitWidth() / 5, getFitHeight() / 5);
        indicator.setMinWidth(getFitWidth() / 5);
        indicator.setMinHeight(getFitHeight() / 5);
        indicator.setStyle("-fx-progress-color:" + CupsAndBalls.getGameContext().getConfiguration().getProgressBarColor());
        indicator.setVisible(false);
        CupsAndBalls.getGameContext().getChildren().add(indicator);
        return indicator;
    }

    private void handleEvent(InputEvent e) {
        if (!selectionEnabled)
            return;
        if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
            progressTimeline = new Timeline(new KeyFrame(
                new Duration(CupsAndBalls.getGameContext().getConfiguration().getFixationLength()),
                new KeyValue(progressIndicator.progressProperty(), 1)
            ));
            progressTimeline.setOnFinished(f -> {
                progressIndicator.setVisible(false);
                selectionCallback.call(this);
            });
            progressIndicator.setProgress(0);
            progressIndicator.setVisible(true);
            progressIndicator.toFront();
            progressTimeline.play();
        } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
            progressIndicator.setVisible(false);
            if (progressTimeline != null)
                progressTimeline.stop();
        }
    }

    public static double computeX(int xIndex) {
        double width = CupsAndBalls.getGameContext().getGamePanelDimensionProvider().getDimension2D().getWidth();
        return Config.CUP_MARGIN + (xIndex + 0.5) * (width - 2 * Config.CUP_MARGIN) / Config.getNbCups() - Config.CUP_WIDTH / 2;
    }

    public static int indexDistance(Cup cupA, Cup cupB) {
        return Math.abs(cupA.getCurrentIndex() - cupB.getCurrentIndex());
    }

    public static void swapBall(Cup from, Cup to) {
        to.ball = from.ball;
        from.ball = null;
        to.ball.container = to;
    }
}
