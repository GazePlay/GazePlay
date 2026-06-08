package net.gazeplay.games.gazeplayEval;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import org.kordamp.ikonli.javafx.FontIcon;

@Slf4j
public class EvalOptionsCard extends Group {

    String evalOptionValue;
    IGameContext gameContext;
    GazeplayEval gameInstance;

    ProgressIndicator progressIndicator;
    CustomInputEventHandlerRestartButton customInputEventHandlerRestartButton;
    CustomInputEventHandlerContinueButton customInputEventHandlerContinueButton;
    Timeline progressIndicatorAnimationTimeLine;

    Button restartButton;
    Button continueButton;

    EvalOptionsCard(String evalOptionValue, @NonNull IGameContext gameContext, GazeplayEval gameInstance) {
        this.evalOptionValue = evalOptionValue;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;

        this.createButtons();
        this.progressIndicator = buildProgressIndicator();

        switch (evalOptionValue) {
            case "Autonomie_Soutient_Oral":
                this.manualOralOption();
                break;

            case "Autonomie_Soutient_Visuel":
                this.manualVisualOption();
                break;

            case "Autonomie_Total":
                this.autonomieTotalOption();
                break;

            default:
                break;
        }

        gameContext.getChildren().add(progressIndicator);
    }

    public void createButtons(){
        Label restartText = new Label("Recommencer");
        restartText.setStyle("-fx-font-size: 24px;");

        FontIcon restartIcon = new FontIcon("fas-redo-alt");
        restartIcon.setIconSize(80);

        VBox restartContent = new VBox(15);
        restartContent.setAlignment(Pos.CENTER);
        restartContent.getChildren().addAll(restartText, restartIcon);

        restartButton = new Button();
        restartButton.setGraphic(restartContent);

        restartButton.setPrefSize(260, 260);

        Label continueText = new Label("Continuer");
        continueText.setStyle("-fx-font-size: 24px;");

        FontIcon continueIcon = new FontIcon("fas-arrow-right");
        continueIcon.setIconSize(80);

        VBox continueContent = new VBox(15);
        continueContent.setAlignment(Pos.CENTER);
        continueContent.getChildren().addAll(continueText, continueIcon);

        continueButton = new Button();
        continueButton.setGraphic(continueContent);

        continueButton.setPrefSize(260, 260);

        HBox buttonsBox = new HBox(150);
        buttonsBox.setAlignment(Pos.CENTER);

        buttonsBox.getChildren().addAll(restartButton, continueButton);

        StackPane root = new StackPane();
        root.getChildren().add(buttonsBox);
        StackPane.setAlignment(buttonsBox, Pos.CENTER);

        root.setPrefSize(
            gameContext.getGamePanelDimensionProvider().getDimension2D().getWidth(),
            gameContext.getGamePanelDimensionProvider().getDimension2D().getHeight()
        );

        gameContext.getChildren().add(root);
    }

    public void manualOralOption(){
        this.continueButton.setOnMouseClicked(event -> {
            gameInstance.clearScreen();
            gameInstance.generateScreen();
        });

        this.restartButton.setOnMouseClicked(event -> {
            gameInstance.clearScreen();
            gameInstance.decreaseIndex();
            gameInstance.generateScreen();
        });
    }

    public void manualVisualOption(){
        this.manualOralOption();
        customInputEventHandlerRestartButton = new CustomInputEventHandlerRestartButton();
        customInputEventHandlerContinueButton = new CustomInputEventHandlerContinueButton();

        this.restartButton.addEventFilter(MouseEvent.ANY, customInputEventHandlerRestartButton);
        this.restartButton.addEventFilter(GazeEvent.ANY, customInputEventHandlerRestartButton);

        this.continueButton.addEventFilter(MouseEvent.ANY, customInputEventHandlerContinueButton);
        this.continueButton.addEventFilter(GazeEvent.ANY, customInputEventHandlerContinueButton);
    }

    public void autonomieTotalOption(){
        this.continueButton.setOnAction(event -> {
            gameInstance.clearScreen();
            gameInstance.generateScreen();
        });

        this.restartButton.setOnAction(event -> {
            gameInstance.clearScreen();
            gameInstance.decreaseIndex();
            gameInstance.generateScreen();
        });

        customInputEventHandlerRestartButton = new CustomInputEventHandlerRestartButton();
        customInputEventHandlerContinueButton = new CustomInputEventHandlerContinueButton();

        this.restartButton.addEventFilter(MouseEvent.ANY, customInputEventHandlerRestartButton);
        this.restartButton.addEventFilter(GazeEvent.ANY, customInputEventHandlerRestartButton);

        this.continueButton.addEventFilter(MouseEvent.ANY, customInputEventHandlerContinueButton);
        this.continueButton.addEventFilter(GazeEvent.ANY, customInputEventHandlerContinueButton);
    }

    private ProgressIndicator buildProgressIndicator() {
        // progressIndicator 2cm de diamètre
        double minWidth = 75;
        double minHeight = 75;

        final Region root = gameContext.getRoot();

        ProgressIndicator result = new ProgressIndicator(0);
        result.setTranslateX((root.getWidth()/2) - (minWidth/2));
        result.setTranslateY((root.getHeight()/2) - (minHeight/2));
        result.setMinWidth(minWidth);
        result.setMinHeight(minHeight);
        result.setOpacity(0.5);
        result.toFront();
        result.setVisible(false);

        return result;
    }

    private Timeline createProgressIndicatorTimeLine(Button selectedButton) {
        Timeline result = new Timeline();

        result.getKeyFrames()
            .add(new KeyFrame(new Duration(3000), new KeyValue(progressIndicator.progressProperty(), 1)));

        EventHandler<ActionEvent> progressIndicatorAnimationTimeLineOnFinished = createProgressIndicatorAnimationTimeLineOnFinished(
            selectedButton);

        result.setOnFinished(progressIndicatorAnimationTimeLineOnFinished);

        return result;
    }

    private EventHandler<ActionEvent> createProgressIndicatorAnimationTimeLineOnFinished(Button selectedButton) {
        return actionEvent -> {
            progressIndicator.setVisible(false);
            if (this.evalOptionValue.equals("Autonomie_Soutient_Visuel")){
                restartButton.setStyle("");
                continueButton.setStyle("");

                selectedButton.setStyle("""
                     -fx-border-color: red;
                     -fx-border-width: 5;
                """);

            } else if (this.evalOptionValue.equals("Autonomie_Total")) {
                selectedButton.fire();
            }

        };
    }

    private class CustomInputEventHandlerRestartButton implements EventHandler<Event> {

        private boolean moved = false;

        @Override
        public void handle(Event event) {
            if (gameInstance.eyeTracker.equals("tobii")){
                if (event.getEventType() == GazeEvent.GAZE_ENTERED) {
                    onEntered();
                } else if (event.getEventType() == GazeEvent.GAZE_MOVED){
                    onEnteredOnceWhileMoved();
                } else if (event.getEventType() == GazeEvent.GAZE_EXITED) {
                    onExited();
                }
            }else {
                if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    onEntered();
                } else if (event.getEventType() == MouseEvent.MOUSE_MOVED){
                    onEnteredOnceWhileMoved();
                } else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
                    onExited();
                }
            }
        }

        private void onEntered() {
            this.moved = true;
            progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(restartButton);
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
                progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(restartButton);
                progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                progressIndicator.setMinWidth(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
                progressIndicator.setMinHeight(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
                progressIndicator.setProgress(0);
                progressIndicator.setVisible(true);
                progressIndicatorAnimationTimeLine.playFromStart();
            }
        }

        private void onExited() {
            progressIndicatorAnimationTimeLine.stop();

            progressIndicator.setVisible(false);
            progressIndicator.setProgress(0);

            this.moved = false;
        }
    }

    private class CustomInputEventHandlerContinueButton implements EventHandler<Event> {

        private boolean moved = false;

        @Override
        public void handle(Event event) {
            if (gameInstance.eyeTracker.equals("tobii")){
                if (event.getEventType() == GazeEvent.GAZE_ENTERED) {
                    onEntered();
                } else if (event.getEventType() == GazeEvent.GAZE_MOVED){
                    onEnteredOnceWhileMoved();
                } else if (event.getEventType() == GazeEvent.GAZE_EXITED) {
                    onExited();
                }
            }else {
                if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    onEntered();
                } else if (event.getEventType() == MouseEvent.MOUSE_MOVED){
                    onEnteredOnceWhileMoved();
                } else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
                    onExited();
                }
            }
        }

        private void onEntered() {
            this.moved = true;
            progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(continueButton);
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
                progressIndicatorAnimationTimeLine = createProgressIndicatorTimeLine(continueButton);
                progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                progressIndicator.setMinWidth(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
                progressIndicator.setMinHeight(100.0 * gameContext.getConfiguration().getProgressBarSize() / 100);
                progressIndicator.setProgress(0);
                progressIndicator.setVisible(true);
                progressIndicatorAnimationTimeLine.playFromStart();
            }
        }

        private void onExited() {
            progressIndicatorAnimationTimeLine.stop();

            progressIndicator.setVisible(false);
            progressIndicator.setProgress(0);

            this.moved = false;
        }
    }
}
