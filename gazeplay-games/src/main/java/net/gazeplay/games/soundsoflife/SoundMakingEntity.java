package net.gazeplay.games.soundsoflife;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.soundsmanager.SoundManager;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

import java.util.List;

/**
 * An animal of the SoundsOfLife game (reviewed on 30/10/2026): during a fixation, a ring of the main color goes round
 * it; then it plays one of its sounds (in turn), swings, and its name is shown above it in a label.
 */
@Slf4j
public class SoundMakingEntity extends Parent {

    /**
     * How long the name stays shown.
     */
    private static final Duration NAME_SHOWN = Duration.seconds(3);

    private final List<String> audioClips;
    private final Stats stats;
    private final SoundManager soundManager;
    private final IGameContext gameContext;
    private final Timeline swing;
    private final Circle ring;
    private final double ringPerimeter;
    private final DoubleProperty fixationProgress = new SimpleDoubleProperty(0);

    /**
     * The label of the name, shown when the sound plays.
     */
    @Getter
    private final Group nameLabel;

    private Timeline fixation;
    private SequentialTransition nameShown;
    private int soundIter;

    /**
     * @param name the name of the animal, translated
     */
    public SoundMakingEntity(final ImageView imageView, final String name, final Stats stats,
                             final List<String> audioClips, final IGameContext gameContext,
                             final SoundManager soundManager, final ReplayablePseudoRandom randomGenerator) {
        this.audioClips = audioClips;
        this.stats = stats;
        this.soundManager = soundManager;
        this.gameContext = gameContext;
        soundIter = randomGenerator.nextInt(audioClips.size());

        final double width = imageView.getFitWidth();
        final double height = imageView.getFitHeight();
        final double centerX = imageView.getX() + width / 2;
        final double centerY = imageView.getY() + height / 2;

        // The ring of the fixation, round the animal
        final double radius = Math.max(width, height) * 0.52;
        ring = new Circle(centerX, centerY, radius);
        ring.setFill(null);
        ring.setStrokeWidth(Math.max(5, radius * 0.07));
        ring.setStrokeLineCap(StrokeLineCap.BUTT);
        ring.setStyle("-fx-stroke: -gp-accent;");
        ring.setRotate(-90);
        ring.setVisible(false);
        ring.setMouseTransparent(true);
        ringPerimeter = 2 * Math.PI * radius;
        fixationProgress.addListener((observable, oldValue, newValue) ->
            ring.getStrokeDashArray().setAll(newValue.doubleValue() * ringPerimeter, ringPerimeter));

        // The name, above the animal
        final double fontSize = Math.max(18, Math.min(width, height) * 0.16);
        nameLabel = GameStyle.pill(name, fontSize);
        nameLabel.setLayoutX(centerX - nameLabel.getBoundsInLocal().getWidth() / 2);
        nameLabel.setLayoutY(Math.max(0, imageView.getY() - nameLabel.getBoundsInLocal().getHeight() - fontSize * 0.3));
        nameLabel.setOpacity(0);
        nameLabel.setMouseTransparent(true);

        this.getChildren().addAll(imageView, ring, nameLabel);

        swing = new Timeline(new KeyFrame(Duration.seconds(0.5), new KeyValue(imageView.rotateProperty(), 10)),
            new KeyFrame(Duration.seconds(1), new KeyValue(imageView.rotateProperty(), -10)),
            new KeyFrame(Duration.seconds(1.5), new KeyValue(imageView.rotateProperty(), 10)),
            new KeyFrame(Duration.seconds(2), new KeyValue(imageView.rotateProperty(), -10)),
            new KeyFrame(Duration.seconds(2.5), new KeyValue(imageView.rotateProperty(), 10)),
            new KeyFrame(Duration.seconds(3), new KeyValue(imageView.rotateProperty(), 0)));

        final EventHandler<Event> enterHandler = event -> startFixation();
        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterHandler);
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterHandler);
        final EventHandler<Event> exitHandler = event -> stopFixation();
        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitHandler);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitHandler);
    }

    private void startFixation() {
        stopFixation();
        fixationProgress.set(0);
        ring.setVisible(true);
        fixation = new Timeline(new KeyFrame(Duration.millis(gameContext.getConfiguration().getFixationLength()),
            new KeyValue(fixationProgress, 1)));
        fixation.setOnFinished(e -> selected());
        fixation.playFromStart();
    }

    /**
     * Stops the fixation (the gaze left the animal; it used to fail when the gaze left before entering).
     */
    private void stopFixation() {
        if (fixation != null) {
            fixation.stop();
            fixation = null;
        }
        ring.setVisible(false);
    }

    /**
     * Plays a sound of the animal, swings it, shows its name, and counts a goal.
     */
    void selected() {
        ring.setVisible(false);
        swing.playFromStart();
        soundManager.add(audioClips.get(soundIter));
        soundIter = (soundIter + 1) % audioClips.size();
        showName();
        stats.incrementNumberOfGoalsReached();
    }

    private void showName() {
        if (nameShown != null) {
            nameShown.stop();
        }
        final FadeTransition in = new FadeTransition(Duration.millis(200), nameLabel);
        in.setToValue(1);
        final FadeTransition out = new FadeTransition(Duration.millis(400), nameLabel);
        out.setToValue(0);
        nameShown = new SequentialTransition(in, new PauseTransition(NAME_SHOWN), out);
        nameShown.play();
    }
}
