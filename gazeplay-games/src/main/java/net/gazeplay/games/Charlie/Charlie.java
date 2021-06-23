package net.gazeplay.games.Charlie;

import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import net.gazeplay.components.ProgressButton;

import java.util.ArrayList;
import java.util.List;

public class Charlie implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final DimensionGameVariant gameVariant;

    private final ReplayablePseudoRandom random;

    private final Translator translator;

    private final Dimension2D dimension2D;

    private List<String> PictureName;

    //List of the Picture Buttons
    private final List<ProgressButton> PBlist;

    //The goal to reach
    private ProgressButton Charlie;

    Charlie(IGameContext gameContext, Stats stats, DimensionGameVariant gameVariant){

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;

        this.random = new ReplayablePseudoRandom();
        this.stats.setGameSeed(random.getSeed());

        this.translator = gameContext.getTranslator();

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        PBlist = new ArrayList<>();
    }

    Charlie(IGameContext gameContext, Stats stats, DimensionGameVariant gameVariant, double gameSeed){

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;

        this.random = new ReplayablePseudoRandom(gameSeed);

        this.translator = gameContext.getTranslator();

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        PBlist = new ArrayList<>();
    }

    public void launch(){

        PictureName = new ArrayList<>();
        PictureName.add("BibouleBlue");
        PictureName.add("BibouleGreen");
        PictureName.add("BibouleOrange");
        PictureName.add("BibouleRed");
        PictureName.add("BibouleYellow");
        PictureName.add("blackrabbit");
        PictureName.add("brownrabbit");
        PictureName.add("mouse");
        PictureName.add("robot");
        PictureName.add("whiterabbit");

        int rowWin = random.nextInt(gameVariant.getWidth());
        int columnWin = random.nextInt(gameVariant.getHeight());

        String path = "data/Charlie/";

        String question = translator.translate("Where is") + " ";

        String CharlieName = PictureName.remove(random.nextInt(PictureName.size()));

        ProgressButton PB;
        ImageView Im;

        for (int i=0; i<gameVariant.getWidth(); i++){
            for (int j=0; j<gameVariant.getHeight(); j++){
                PB = new ProgressButton();
                PB.setLayoutX((i*(0.9 - (0.9 - 0.0625)/8)/(gameVariant.getWidth()-1) + 0.05)*dimension2D.getWidth());
                PB.setLayoutY(((j+1)*0.75/(gameVariant.getHeight()+1) + 0.05)*dimension2D.getHeight());
                if (i==rowWin && j==columnWin){
                    Charlie = PB;
                    Im = new ImageView(new Image(path +CharlieName+".png"));
                } else {
                    Im = new ImageView(new Image(path +PictureName.get(random.nextInt(PictureName.size()))+".png"));
                }
                Im.setFitWidth((0.9 - 0.0625)/8*dimension2D.getWidth());
                Im.setFitHeight((0.75 - 0.0625)/8*dimension2D.getHeight());
                PB.setImage(Im);
                PB.setVisible(false);
                PBlist.add(PB);
                gameContext.getChildren().add(PB);
            }
        }

        Charlie.assignIndicatorUpdatable(e -> win(), gameContext);
        Charlie.disable();

        question+=translator.translate(CharlieName);

        Transition TransitionQuestion = CreateQuestionTransition(question);
        TransitionQuestion.play();

        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    public void dispose(){
        PBlist.clear();
        PictureName.clear();
        gameContext.getChildren().clear();
    }

    private Transition CreateQuestionTransition(final String question){
        Text questionText = new Text(question);

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

        final double positionX = dimension2D.getWidth() / 2 - questionText.getBoundsInParent().getWidth() * 2;
        final double positionY = dimension2D.getHeight() / 2 - questionText.getBoundsInParent().getHeight() / 2;


        questionText.setX(positionX);
        questionText.setY(positionY);
        questionText.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(questionText, Pos.CENTER);

        gameContext.getChildren().add(questionText);

        final TranslateTransition fullAnimation = new TranslateTransition(
            Duration.millis(gameContext.getConfiguration().getQuestionLength() / 2.0), questionText);

        fullAnimation.setDelay(Duration.millis(gameContext.getConfiguration().getQuestionLength()));

        final double bottomCenter = (0.9 * dimension2D.getHeight()) - questionText.getY()
            + questionText.getBoundsInParent().getHeight() * 3;
        fullAnimation.setToY(bottomCenter);

        fullAnimation.setOnFinished(actionEvent -> {
            questionText.toFront();

            stats.notifyNewRoundReady();

            for (ProgressButton PB : PBlist){
                PB.setVisible(true);
            }
            Charlie.active();

            gameContext.onGameStarted();
        });

        return fullAnimation;

    }

    private void win(){

        stats.incrementNumberOfGoalsReached();

        Charlie.setDisable(true);

        stats.stop();

        gameContext.updateScore(stats, this);

        gameContext.playWinTransition(500, actionEvent -> {

            dispose();

            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            gameContext.showRoundStats(stats, this);
        });
    }
}
