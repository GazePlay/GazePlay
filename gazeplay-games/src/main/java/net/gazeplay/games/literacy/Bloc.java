package net.gazeplay.games.literacy;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

public class Bloc extends Parent {// Rectangle {

    private final double fixationlength;

    private final String letterStr;
    private final Text letter;

    @Getter
    private final boolean isMainLetter;

    private final double width;
    private final double height;

    private final Rectangle bloc;

    private final Letters gameInstance;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;

    final EventHandler<Event> enterEvent;
    final Stats stats;
    private final IGameContext gameContext;

    private Timeline currentTimeline;

    public Bloc(
        double x, double y,
        double width, double height,
        String currentLetter,
        String mainLetter,
        Letters gameInstance,
        Stats stats,
        IGameContext gameContext,
        int fixationlength
    ) {

        this.gameInstance = gameInstance;
        this.stats = stats;
        this.gameContext = gameContext;

        this.fixationlength = fixationlength;

        this.width = width;
        this.height = height;

        this.bloc = new Rectangle(x, y, width, height);

        this.letterStr = currentLetter;

        this.isMainLetter = currentLetter.equals(mainLetter);

        this.letter = new Text("" + letterStr.toUpperCase());
        this.letter.setFont(new Font("Tsukushi A Round Gothic Bold", 250));

        this.bloc.setFill(new Color(Math.random(), Math.random(), Math.random(), 1));

        StackPane stack = new StackPane();
        stack.getChildren().addAll(bloc, letter);

        stack.setLayoutX(x);
        stack.setLayoutY(y);

        this.getChildren().add(stack);

        this.progressIndicator = createProgressIndicator((width), (height));
        this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(bloc);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        currentTimeline = new Timeline();

    }

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(bloc.getX() + width * 0.25);
        indicator.setTranslateY(bloc.getY() + height * 0.2);
        indicator.setMinWidth(width * 0.5);
        indicator.setMinHeight(width * 0.5);
        indicator.setOpacity(0);
        return indicator;
    }

    private void onCorrectBlocSelected() {
        double finalZoom = 1.0;

        progressIndicator.setOpacity(0);
        letter.setOpacity(0);

        gameInstance.removeBloc(this);

        if (gameInstance.currentRoundDetails.remainingCount == 1) {
            // REMOVE ALL CARDS AND REVEAL THE IMAGE
            stats.incrementNumberOfGoalsReached();
            gameInstance.removeAllBlocs();
            gameInstance.currentRoundDetails.remainingCount = 0;
        } else {

            gameInstance.currentRoundDetails.remainingCount--;
        }

        playSound(createLetterSoundPath("" + this.gameInstance.getCurrentLanguage(), this.letterStr));

        currentTimeline.stop();
        currentTimeline = new Timeline();

        currentTimeline.getKeyFrames().add(
            new KeyFrame(new Duration(1000), new KeyValue(bloc.widthProperty(), bloc.getWidth() * finalZoom)));
        currentTimeline.getKeyFrames().add(
            new KeyFrame(new Duration(1000), new KeyValue(bloc.heightProperty(), bloc.getHeight() * finalZoom)));
        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(bloc.xProperty(), 0)));

        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(bloc.yProperty(), 0)));

        // currentTimeline.onFinishedProperty().set(new EventHandler<ActionEvent>() {
        if (gameInstance.currentRoundDetails.remainingCount == 0) {
            currentTimeline.onFinishedProperty().set(actionEvent -> gameContext.playWinTransition(500, actionEvent1 -> {
                gameInstance.dispose();

                gameContext.clear();

                gameInstance.launch();

                gameContext.onGameStarted();
            }));
        }

        currentTimeline.play();
    }

    private String createLetterSoundPath(String currentLanguage, String currentLetter) {
        ReplayablePseudoRandom r = new ReplayablePseudoRandom();
        if (r.nextBoolean()) {
            return "data/literacy/sounds/" + currentLanguage.toLowerCase() + "/f/letter/" + currentLetter.toUpperCase()
                + ".wav";
        }

        return "data/literacy/sounds/" + currentLanguage.toLowerCase() + "/m/letter/" + currentLetter.toUpperCase()
            + ".wav";

    }

    private void playSound(String path) {
        gameContext.getSoundManager().add(path);
    }

    private void onWrongBlocSelected() {

        currentTimeline.stop();
        currentTimeline = new Timeline();

        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(bloc.opacityProperty(), 0.9)));

        currentTimeline.play();
        progressIndicator.setOpacity(0);

    }

    private EventHandler<Event> buildEvent() {

        return e -> {

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                progressIndicator.setOpacity(1);
                progressIndicator.setProgress(0);

                currentTimeline.stop();
                currentTimeline = new Timeline();

                currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(fixationlength),
                    new KeyValue(progressIndicator.progressProperty(), 1)));

                currentTimeline.setOnFinished(actionEvent -> {

                    bloc.removeEventFilter(MouseEvent.ANY, enterEvent);
                    bloc.removeEventFilter(GazeEvent.ANY, enterEvent);

                    if (isMainLetter) {
                        onCorrectBlocSelected();
                    } else {// bad card
                        onWrongBlocSelected();
                    }
                });
                currentTimeline.play();
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                currentTimeline.stop();
                currentTimeline = new Timeline();

                currentTimeline.getKeyFrames().add(
                    new KeyFrame(new Duration(1), new KeyValue(bloc.xProperty(), bloc.getX() + (width) / 2)));
                currentTimeline.getKeyFrames().add(
                    new KeyFrame(new Duration(1), new KeyValue(bloc.yProperty(), bloc.getY() + (height) / 2)));
                currentTimeline.getKeyFrames()
                    .add(new KeyFrame(new Duration(1), new KeyValue(bloc.widthProperty(), width)));
                currentTimeline.getKeyFrames()
                    .add(new KeyFrame(new Duration(1), new KeyValue(bloc.heightProperty(), height)));

                currentTimeline.play();

                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);
            }
        };
    }
}
