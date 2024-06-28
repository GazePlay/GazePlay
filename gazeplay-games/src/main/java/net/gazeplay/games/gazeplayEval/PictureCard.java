package net.gazeplay.games.gazeplayEval;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.File;
import java.util.Objects;

@Slf4j
@ToString
@Getter
class PictureCard extends Group {

    private final double minTime;
    private final IGameContext gameContext;
    private final GazeplayEvalGameVariant gameVariant;

    private final ImageView imageRectangle;
    private final Rectangle notifImageRectangle;

    private double initialWidth;
    private double initialHeight;

    private final double initialPositionX;
    private final double initialPositionY;

    private final Stats stats;
    private final String imageName;
    private final PictureCard.CustomInputEventHandlerMouse customInputEventHandlerMouse;
    private final GazeplayEval gameInstance;
    private ProgressIndicator progressIndicator;
    private Timeline progressIndicatorAnimationTimeLine;
    private boolean selected;
    private boolean alreadySee;
    private double valueProgressIndicator = 2000;
    private double valueTranslateX = 0;
    private double valueTranslateY = 0;

    PictureCard(double posX, double posY, double width, double height, @NonNull IGameContext gameContext, @NonNull GazeplayEvalGameVariant gameVariant,
                @NonNull String imageName, Double fixationLength, @NonNull Stats stats, GazeplayEval gameInstance) {

        log.info("imagePath = {}", imageName);

        final Configuration config = gameContext.getConfiguration();

        this.minTime = config.getFixationLength();
        this.initialPositionX = posX;
        this.initialPositionY = posY;
        this.initialWidth = width;
        this.initialHeight = height;
        this.selected = false;
        this.alreadySee = false;
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.stats = stats;
        this.gameInstance = gameInstance;
        this.imageName = imageName;
        this.valueProgressIndicator = fixationLength;

        this.imageRectangle = createImageView(this.initialPositionX, this.initialPositionY, this.initialWidth, this.initialHeight, imageName);

        this.progressIndicator = buildProgressIndicator(this.initialWidth, this.initialHeight);

        this.notifImageRectangle = createNotifImageRectangle();

        this.getChildren().add(imageRectangle);
        this.getChildren().add(progressIndicator);
        this.getChildren().add(notifImageRectangle);

        customInputEventHandlerMouse = new PictureCard.CustomInputEventHandlerMouse();

        gameContext.getGazeDeviceManager().addEventFilter(imageRectangle);

        this.addEventFilter(MouseEvent.ANY, customInputEventHandlerMouse);
        this.addEventFilter(GazeEvent.ANY, customInputEventHandlerMouse);

    }

    private Timeline createProgressIndicatorTimeLine(GazeplayEval gameInstance) {
        Timeline result = new Timeline();

        result.getKeyFrames()
            .add(new KeyFrame(new Duration(this.valueProgressIndicator), new KeyValue(progressIndicator.progressProperty(), 1)));

        EventHandler<ActionEvent> progressIndicatorAnimationTimeLineOnFinished = createProgressIndicatorAnimationTimeLineOnFinished(
            gameInstance);

        result.setOnFinished(progressIndicatorAnimationTimeLineOnFinished);

        return result;
    }

    private EventHandler<ActionEvent> createProgressIndicatorAnimationTimeLineOnFinished(GazeplayEval gameInstance) {
        return actionEvent -> {

            selected = true;
            this.setNotifImageRectangle(true);
            imageRectangle.removeEventFilter(MouseEvent.ANY, customInputEventHandlerMouse);
            imageRectangle.removeEventFilter(GazeEvent.ANY, customInputEventHandlerMouse);
            gameContext.getGazeDeviceManager().removeEventFilter(imageRectangle);
            this.checkedImage();
            customInputEventHandlerMouse.ignoreAnyInput = true;
            progressIndicator.setVisible(false);

            this.onCardSelected();
            if (gameInstance.checkAllPictureCardChecked()){
                this.waitBeforeNextRound();
            }
        };
    }

    public void setNotifImageRectangle(boolean value) { this.notifImageRectangle.setVisible(value); }

    public void checkedImage(){
        Configuration config = ActiveConfigurationContext.getInstance();

        if (Objects.equals(config.getFeedback(), "nothing")){
            notifImageRectangle.setOpacity(0);
            notifImageRectangle.setVisible(false);
        }else {
            notifImageRectangle.setOpacity(1);
            notifImageRectangle.setVisible(true);
        }
    }

    public void removeEventHandler(){
        customInputEventHandlerMouse.ignoreAnyInput = true;
    }

    public void onCardSelected() {
        gameInstance.calculScores(this.imageName);
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, gameInstance);
    }

    public void waitBeforeNextRound(){
        Configuration config = ActiveConfigurationContext.getInstance();

        Timeline transition = new Timeline();
        transition.getKeyFrames().add(new KeyFrame(new Duration(config.getTransitionTime())));
        transition.setOnFinished(event -> {
            if(gameInstance.increaseIndexFileImage()){
                this.endGame();
            }else {
                gameInstance.stopDisplayDuration();
                gameInstance.stopGetGazePosition();
                gameInstance.getScreenHeatmapGaze();
                gameInstance.dispose();
                gameContext.clear();
                gameInstance.launch();
            }
        });
        gameInstance.removeEventHandlerPictureCard();
        transition.playFromStart();
    }

    private ImageView createImageView(double posX, double posY, double width, double height,
                                      @NonNull String imageName) {

        Configuration config = ActiveConfigurationContext.getInstance();
        File file = new File(config.getFileDir() + "\\evals\\" +  this.gameVariant.getNameGame() + "\\images\\" + imageName);
        final Image image = new Image(file.toURI().toString());

        ImageView result = new ImageView(image);

        result.setFitWidth(width);
        result.setFitHeight(height);

        double ratioX = result.getFitWidth() / image.getWidth();
        double ratioY = result.getFitHeight() / image.getHeight();

        double reducCoeff = Math.min(ratioX, ratioY);

        double w = image.getWidth() * reducCoeff;
        double h = image.getHeight() * reducCoeff;

        this.valueTranslateX = (result.getFitWidth() - w) / 2;
        this.valueTranslateY = (result.getFitHeight() - h) / 2;

        result.setX(posX);
        result.setY(posY);
        result.setTranslateX((result.getFitWidth() - w) / 2);
        result.setTranslateY((result.getFitHeight() - h) / 2);
        result.setPreserveRatio(true);

        return result;
    }

    private Rectangle createNotifImageRectangle() {

        Configuration config = ActiveConfigurationContext.getInstance();

        if (Objects.equals(config.getFeedback(), "standard")){
            final Image image = new Image("data/common/images/blackCircle.png");

            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double imageHeightToWidthRatio = imageHeight / imageWidth;

            double rectangleWidth = imageRectangle.getFitWidth() / 40;
            double rectangleHeight = imageHeightToWidthRatio * rectangleWidth;

            Rectangle notifImageRectangle = new Rectangle(rectangleWidth, rectangleHeight);
            notifImageRectangle.setFill(new ImagePattern(image));
            notifImageRectangle.setX(this.initialPositionX);
            notifImageRectangle.setY(this.initialPositionY);
            notifImageRectangle.setTranslateX(this.valueTranslateX);
            notifImageRectangle.setTranslateY(this.valueTranslateY);
            notifImageRectangle.setOpacity(0);
            notifImageRectangle.setVisible(false);
            return notifImageRectangle;
        }else {
            final Image image = new Image("data/common/images/redFrame.png");

            ImageView result = new ImageView(image);

            result.setFitWidth(this.initialWidth);
            result.setFitHeight(this.initialHeight);

            double ratioX = result.getFitWidth() / image.getWidth();
            double ratioY = result.getFitHeight() / image.getHeight();

            double reducCoeff = Math.min(ratioX, ratioY);

            double w = image.getWidth() * reducCoeff;
            double h = image.getHeight() * reducCoeff;

            Rectangle notifImageRectangle = new Rectangle(w, h);
            notifImageRectangle.setFill(new ImagePattern(image));
            notifImageRectangle.setX(this.initialPositionX);
            notifImageRectangle.setY(this.initialPositionY);
            notifImageRectangle.setTranslateX((result.getFitWidth() - w) / 2);
            notifImageRectangle.setTranslateY((result.getFitHeight() - h) / 2);
            notifImageRectangle.setVisible(false);
            return notifImageRectangle;
        }
    }

    private ProgressIndicator buildProgressIndicator(double parentWidth, double parentHeight) {
        // progressIndicator 2cm de diamètre
        double minWidth = 75;
        double minHeight = 75;

        double positionX = imageRectangle.getX() + (parentWidth - minWidth) / 2;
        double positionY = imageRectangle.getY() + (parentHeight - minHeight) / 2;

        ProgressIndicator result = new ProgressIndicator(0);
        result.setTranslateX(positionX);
        result.setTranslateY(positionY);
        result.setMinWidth(minWidth);
        result.setMinHeight(minHeight);
        result.setOpacity(0.5);
        result.setVisible(false);
        return result;
    }

    public void endGame() {

        progressIndicator.setVisible(false);
        gameInstance.stopDisplayDuration();
        gameInstance.stopGetGazePosition();
        gameInstance.getScreenHeatmapGaze();
        gameInstance.finalStats();
        gameContext.updateScore(stats, gameInstance);
        gameInstance.resetFromReplay();
        gameInstance.dispose();
        gameContext.clear();
        gameContext.showRoundStats(stats, gameInstance);
    }

    private class CustomInputEventHandlerMouse implements EventHandler<Event> {

        /**
         * this is used to temporarily indicate to ignore input for instance, when an animation is in progress, we
         * do not want the game to continue to process input, as the user input is irrelevant while the animation is
         * in progress
         */
        private boolean ignoreAnyInput = false;
        private boolean moved = false;

        @Override
        public void handle(Event e) {
            if (ignoreAnyInput) {
                return;
            }

            if (selected) {
                return;
            }

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                onEntered();
            } else if (e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_MOVED){
                onEnteredOnceWhileMoved();
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                onExited();
            }
        }

        private void onEntered() {
            this.moved = true;
            log.info("ENTERED {}", imageName);

            progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(gameInstance);
            progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
            progressIndicator.setMinWidth(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
            progressIndicator.setMinHeight(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
            progressIndicator.setProgress(0);
            progressIndicator.setVisible(true);
            progressIndicatorAnimationTimeLine.playFromStart();
        }

        private void onEnteredOnceWhileMoved(){
            if (!this.moved){

                this.moved = true;
                log.info("MOVED {}", imageName);

                progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(gameInstance);
                progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                progressIndicator.setMinWidth(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
                progressIndicator.setMinHeight(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
                progressIndicator.setProgress(0);
                progressIndicator.setVisible(true);
                progressIndicatorAnimationTimeLine.playFromStart();
            }
        }

        private void onExited() {
            log.info("EXITED {}", imageName);

            progressIndicatorAnimationTimeLine.stop();

            progressIndicator.setVisible(false);
            progressIndicator.setProgress(0);

            this.moved = false;
        }

    }
}
