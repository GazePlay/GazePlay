package net.gazeplay.games.Charlie;

import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class Charlie implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final DimensionGameVariant gameVariant;

    private final ReplayablePseudoRandom random;

    private final Translator translator;

    private final Dimension2D dimension2D;

    //List of the Picture who may by in the game
    private List<String> PictureName;

    //List of the Picture in the game
    private final List<ProgressButton> Plist;

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

        Plist = new ArrayList<>();
    }

    Charlie(IGameContext gameContext, Stats stats, DimensionGameVariant gameVariant, double gameSeed){

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;

        this.random = new ReplayablePseudoRandom(gameSeed);

        this.translator = gameContext.getTranslator();

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Plist = new ArrayList<>();
    }

    public void launch(){

        initBackground();

        //my list of picture, maybe change the system ?
        PictureName = new ArrayList<>();
        PictureName.add("BibouleBlue");
        PictureName.add("BibouleGreen");
        PictureName.add("BibouleOrange");
        PictureName.add("BibouleRed");
        PictureName.add("BibouleYellow");

        PictureName.add("BibouleBlueM");
        PictureName.add("BibouleGreenM");
        PictureName.add("BibouleOrangeM");
        PictureName.add("BibouleRedM");
        PictureName.add("BibouleYellowM");

        PictureName.add("BibouleBlueL");
        PictureName.add("BibouleGreenL");
        PictureName.add("BibouleOrangeL");
        PictureName.add("BibouleRedL");
        PictureName.add("BibouleYellowL");

        PictureName.add("BibouleBlueML");
        PictureName.add("BibouleGreenML");
        PictureName.add("BibouleOrangeML");
        PictureName.add("BibouleRedML");
        PictureName.add("BibouleYellowML");

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
                PB.setLayoutY(((j+1)*0.75/(gameVariant.getHeight()+1))*dimension2D.getHeight());
                if (i==rowWin && j==columnWin){
                    Im = new ImageView(new Image(path + CharlieName + ".png"));
                } else {
                    Im = new ImageView(new Image(path + PictureName.get(random.nextInt(PictureName.size())) + ".png"));
                }
                Im.setFitWidth((0.9 - 0.0625)/8 * dimension2D.getWidth());
                Im.setFitHeight((0.75 - 0.0625)/8 * dimension2D.getHeight());
                PB.setImage(Im);
                PB.setVisible(false);
                Plist.add(PB);
                gameContext.getChildren().add(PB);
            }
        }
        Charlie = new ProgressButton();
        Charlie.setLayoutX((rowWin*(0.9 - (0.9 - 0.0625)/8)/(gameVariant.getWidth()-1) + 0.05)*dimension2D.getWidth());
        Charlie.setLayoutY(((columnWin+1)*0.75/(gameVariant.getHeight()+1))*dimension2D.getHeight());
        Im = new ImageView(new Image(path + "nothing.png"));
        Im.setFitWidth((0.9 - 0.0625)/8*dimension2D.getWidth());
        Im.setFitHeight((0.75 - 0.0625)/8*dimension2D.getHeight());
        Charlie.setImage(Im);
        gameContext.getChildren().add(Charlie);

        Charlie.assignIndicatorUpdatable(e -> win(), gameContext);
        Charlie.disable();

        if (CharlieName.contains("BibouleBlue")) {
            question += translator.translate("BibouleBlue");
        }
        else if (CharlieName.contains("BibouleRed")) {
            question += translator.translate("BibouleRed");
        }
        else if (CharlieName.contains("BibouleGreen")) {
            question += translator.translate("BibouleGreen");
        }
        else if (CharlieName.contains("BibouleOrange")) {
            question += translator.translate("BibouleOrange");
        }
        else if (CharlieName.contains("BibouleYellow")) {
            question += translator.translate("BibouleYellow");
        }
        else {
            log.error("Not a biboule+color picture or unknown color" + " : " + CharlieName);
            question += translator.translate(CharlieName);
        }
        question += "\n";

        if (CharlieName.contains("M")) {
            question += translator.translate("YMustache");
        } else {
            question += translator.translate("NMustache");
        }
        question += " ";

        question += translator.translate("and");
        question += " ";

        if (CharlieName.contains("L")) {
            question += translator.translate("YGlass");
        } else {
            question += translator.translate("NGlass");
        }

        Transition TransitionQuestion = CreateQuestionTransition(question);
        TransitionQuestion.play();

        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    public void dispose(){
        Plist.clear();
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

        final double bottomCenter = (0.9 * dimension2D.getHeight()) - questionText.getY();
        fullAnimation.setToY(bottomCenter);

        fullAnimation.setOnFinished(actionEvent -> {
            questionText.toFront();

            stats.notifyNewRoundReady();

            for (ProgressButton PB : Plist){
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

    void initBackground() {
        if (gameContext.getConfiguration().isBackgroundEnabled()) {
            Rectangle backgroundImage = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
            //Add a true background image
            //backgroundImage.setFill(new ImagePattern(new Image("data/Charlie/background.png")));
            gameContext.getChildren().add(0, backgroundImage);
        }
    }
}
