package net.gazeplay.games.order;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.components.Position;


/**
 * @author vincent
 */
public class Target extends Parent {
    private final int num;
    private final EventHandler enterEvent;
    private final Order gameInstance;
    private final IGameContext gameContext;
    private final ProgressIndicator progressIndicator;
    private final Position pos;
    private final double radius;
    private Timeline timelineProgressBar;
    private final double fixationLength;

    public Target(final Order gameInstance, final IGameContext gameContext, final int num, final double fixLength) {
        this.num = num;
        this.gameInstance = gameInstance;
        this.gameContext = gameContext;
        this.pos = this.gameContext.getRandomPositionGenerator().newRandomPosition(100);
        this.radius = 75;
        this.fixationLength = fixLength;

        final Circle cercle = new Circle(pos.getX(), pos.getY(), this.radius);
        cercle.setFill(new ImagePattern(new Image("data/order/images/target.png"), 0, 0, 1, 1, true));
        this.getChildren().add(cercle);

        this.progressIndicator = createProgressIndicator(100);
        this.getChildren().add(this.progressIndicator);

        enterEvent = (EventHandler<Event>) e -> {
            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                enter();
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                if (timelineProgressBar != null) {
                    timelineProgressBar.stop();
                }

                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);
            }
        };
    }

    private void enter() {
        progressIndicator.setOpacity(1);
        progressIndicator.setProgress(0);
        timelineProgressBar = new Timeline();
        timelineProgressBar.getKeyFrames()
            .add(new KeyFrame(Duration.millis(fixationLength), new KeyValue(progressIndicator.progressProperty(), 1)));
        timelineProgressBar.setOnFinished(actionEvent -> {
            progressIndicator.setOpacity(0);
            Target.this.gameInstance.enter(Target.this);
        });
        timelineProgressBar.play();
    }

    private ProgressIndicator createProgressIndicator(final double diameter) {
        final ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(this.pos.getX() - (diameter / 2));
        indicator.setTranslateY(this.pos.getY() - (diameter / 2));
        indicator.setMinWidth(diameter * 0.9);
        indicator.setMinHeight(diameter * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    public void addEvent() {
        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        gameContext.getGazeDeviceManager().addEventFilter(this);
    }

    public Position getPos() {
        return this.pos;
    }

    public int getNum() {
        return this.num;
    }

    public double getRadius() {
        return this.radius;
    }
}
