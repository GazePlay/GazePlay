package net.gazeplay.components;

import javafx.animation.*;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.utils.stats.TargetAOI;

import java.util.ArrayList;

public class GamesRules {

    private Text questionText;
    public Boolean enableEyeTracker = false;

    public GamesRules(){}

    public Transition createQuestionTransition(final IGameContext gameContext, final String question) {
        questionText = new Text(question);
        questionText.setTranslateY(0);
        questionText.setId("title");

        final Region root = gameContext.getRoot();
        final double positionX = (root.getWidth() / 2) - (questionText.getBoundsInParent().getWidth());
        final double positionY = (root.getHeight() / 2) - (questionText.getBoundsInParent().getHeight());

        questionText.setX(positionX);
        questionText.setY(positionY);
        questionText.setTextAlignment(TextAlignment.CENTER);
        questionText.toFront();
        StackPane.setAlignment(questionText, Pos.CENTER);

        gameContext.getChildren().add(questionText);

        final TranslateTransition fullAnimation = new TranslateTransition(
            Duration.millis(gameContext.getConfiguration().getQuestionLength() / 2.0), questionText);

        fullAnimation.setCycleCount(Animation.INDEFINITE);
        fullAnimation.setDelay(Duration.millis(gameContext.getConfiguration().getQuestionLength()));

        return fullAnimation;
    }

    public void startGaze(){
        final Timeline startGazeTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            this.enableEyeTracker = true;
        }));
        startGazeTimeline.setCycleCount(1);
        startGazeTimeline.playFromStart();
    }

    public Transition createQuestionTransitionAOI(final IGameContext gameContext, final String question, final ArrayList<TargetAOI> targetAOIList) {
        questionText = new Text(question);
        questionText.setTranslateY(0);

        final String color = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<>() {
            @Override
            public String visitLight() {
                return "titleB";
            }

            @Override
            public String visitDark() {
                return "titleW";
            }
        });

        questionText.setId(color);

        final Dimension2D gamePaneDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double positionX = gamePaneDimension2D.getWidth() / 2 - questionText.getBoundsInParent().getWidth() * 2;
        final double positionY = gamePaneDimension2D.getHeight() / 2 - questionText.getBoundsInParent().getHeight() / 2;

        questionText.setX(positionX);
        questionText.setY(positionY);
        questionText.setTextAlignment(TextAlignment.CENTER);
        questionText.toFront();
        StackPane.setAlignment(questionText, Pos.CENTER);

        gameContext.getChildren().add(questionText);
        final long timeStarted = System.currentTimeMillis();
        final TargetAOI targetAOI = new TargetAOI(gamePaneDimension2D.getWidth() / 2, gamePaneDimension2D.getHeight() / 2, (int) questionText.getBoundsInParent().getWidth(),
            timeStarted);
        targetAOI.setTimeEnded(timeStarted + gameContext.getConfiguration().getQuestionLength());
        targetAOIList.add(targetAOI);

        final TranslateTransition fullAnimation = new TranslateTransition(
            Duration.millis(gameContext.getConfiguration().getQuestionLength() / 2.0), questionText);

        fullAnimation.setDelay(Duration.millis(gameContext.getConfiguration().getQuestionLength()));

        return fullAnimation;
    }

    public void hideQuestionText(){
        this.questionText.setOpacity(0);
    }
}
