package net.gazeplay.games.soundsoflife;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.Random;

@Slf4j
public class SoundMakingEntity extends Parent {
    private final ArrayList<String> audioClips;
    private final ProgressIndicator progressIndicator;
    private final Timeline progressTimeline;
    private final Timeline movetimeline;
    private int soundIter;
    private final Stats stats;

    public SoundMakingEntity(final ImageView imageView, final Stats stats, final ArrayList<String> audioClips,
                             final ProgressIndicator progressIndicator, final int fixationLength) {
        this.audioClips = audioClips;
        this.progressIndicator = progressIndicator;
        this.stats = stats;

        final Random random = new Random();
        soundIter = random.nextInt(audioClips.size());

        this.getChildren().addAll(imageView, progressIndicator);

        progressTimeline = new Timeline(
            new KeyFrame(new Duration(fixationLength), new KeyValue(progressIndicator.progressProperty(), 1)));

        progressTimeline.setOnFinished(e -> selected());

        movetimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), new KeyValue(imageView.rotateProperty(), 10)),
            new KeyFrame(Duration.seconds(1), new KeyValue(imageView.rotateProperty(), -10)),
            new KeyFrame(Duration.seconds(1.5), new KeyValue(imageView.rotateProperty(), 10)),
            new KeyFrame(Duration.seconds(2), new KeyValue(imageView.rotateProperty(), -10)),
            new KeyFrame(Duration.seconds(2.5), new KeyValue(imageView.rotateProperty(), 10)),
            new KeyFrame(Duration.seconds(3), new KeyValue(imageView.rotateProperty(), 0)));

        final EventHandler<Event> enterHandler = (Event event) -> {
            progressIndicator.setOpacity(1);
            progressTimeline.playFromStart();
        };

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterHandler);
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterHandler);

        final EventHandler<Event> exitHandler = (Event event) -> {
            progressIndicator.setOpacity(0);
            progressIndicator.setProgress(0);
            progressTimeline.stop();
        };

        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitHandler);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitHandler);
    }

    private void selected() {
        progressIndicator.setOpacity(0);
        progressIndicator.setProgress(0);

        movetimeline.playFromStart();
        try {
            ForegroundSoundsUtils.playSound(audioClips.get(soundIter));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        soundIter = (soundIter + 1) % audioClips.size();

        stats.incNbGoals();
    }
}
