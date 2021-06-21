package net.gazeplay.games.Charlie;

import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;

public class Charlie implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final DimensionGameVariant gameVariant;

    private final ReplayablePseudoRandom random;

    private final Translator translator;

    private final Dimension2D dimension2D;

    private String[][] Picture;
    private String[][] PictureName;

    Charlie(IGameContext gameContext, Stats stats, DimensionGameVariant gameVariant){

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;

        this.random = new ReplayablePseudoRandom();
        this.stats.setGameSeed(random.getSeed());

        this.translator = gameContext.getTranslator();

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
    }

    Charlie(IGameContext gameContext, Stats stats, DimensionGameVariant gameVariant, double gameSeed){

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;

        this.random = new ReplayablePseudoRandom(gameSeed);

        this.translator = gameContext.getTranslator();

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
    }

    public void launch(){

        Picture = new String[][]
            {

            };

        PictureName = new String[][]
            {

            };

        String path = "data/Charlie/";

        String question = translator.translate("Where is") + " ";

        Transition TransitionQuestion = CreateQuestionTransition(question);
        TransitionQuestion.play();

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    public void dispose(){

    }

    private Transition CreateQuestionTransition(final String question){
        Text questionText = new Text(question);

        questionText.setTranslateY(0);

        final String color = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<String>() {
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

        final double positionX = dimension2D.getWidth() / 2 - questionText.getBoundsInParent().getWidth() * 2;
        final double positionY = dimension2D.getHeight() / 2 - questionText.getBoundsInParent().getHeight() / 2;


        questionText.setX(positionX);
        questionText.setY(positionY);
        questionText.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(questionText, Pos.CENTER);

        gameContext.getChildren().add(questionText);
        final long timeStarted = System.currentTimeMillis();
        /*final TargetAOI targetAOI = new TargetAOI(dimension2D.getWidth() / 2, dimension2D.getHeight() / 2, (int) questionText.getBoundsInParent().getWidth(),
            timeStarted);
        targetAOI.setTimeEnded(timeStarted + gameContext.getConfiguration().getQuestionLength());
        targetAOIList.add(targetAOI);*/

        final TranslateTransition fullAnimation = new TranslateTransition(
            Duration.millis(gameContext.getConfiguration().getQuestionLength() / 2.0), questionText);

        fullAnimation.setDelay(Duration.millis(gameContext.getConfiguration().getQuestionLength()));

        final double bottomCenter = (0.9 * dimension2D.getHeight()) - questionText.getY()
            + questionText.getBoundsInParent().getHeight() * 3;
        fullAnimation.setToY(bottomCenter);

        fullAnimation.setOnFinished(actionEvent -> {
            questionText.toFront();

            stats.notifyNewRoundReady();

            gameContext.onGameStarted();
        });

        return fullAnimation;

    }
}
