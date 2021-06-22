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

    private String[][] Picture;
    private String[][] PictureName;

    private final int row = 9;
    private final int column = 16;

    private int rowWin;
    private int columnWin;

    private final List<ProgressButton> PBlist;

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

        Picture = new String[][]
            {
                {"BibouleBlue.png", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""}
            };

        PictureName = new String[][]
            {
                {"BibouleBlue", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""}
            };

        rowWin = random.nextInt(gameVariant.getWidth());
        columnWin = random.nextInt(gameVariant.getHeight());

        String path = "data/Charlie/";

        String question = translator.translate("Where is") + " ";

        for (int i=0; i<20; i++){
            shuffleColumn(random.nextInt(column), random.nextInt(column));
            shuffleRow(random.nextInt(row), random.nextInt(row));
        }

        ProgressButton PB;
        ImageView Im;

        for (int i=0; i<gameVariant.getWidth(); i++){
            for (int j=0; j<gameVariant.getHeight(); j++){
                PB = new ProgressButton();
                PB.setLayoutX((i*(0.9 - (0.9 - 1/16)/8)/(gameVariant.getWidth()-1) + 0.05)*dimension2D.getWidth());
                PB.setLayoutY(((j+1)*0.75/(gameVariant.getHeight()+1) + 0.05)*dimension2D.getHeight());
                Im = new ImageView(new Image(path+/*Picture[j][i]*/"BibouleBlue.png"));
                Im.setFitWidth((0.9 - 1/16)/8*dimension2D.getWidth());
                Im.setFitHeight((0.75 - 1/16)/8*dimension2D.getHeight());
                PB.setImage(Im);
                if (i==rowWin && j==columnWin){
                    Charlie = PB;
                }
                PB.setVisible(false);
                PBlist.add(PB);
                gameContext.getChildren().add(PB);
            }
        }

        Charlie.assignIndicatorUpdatable(e -> win(), gameContext);
        Charlie.disable();

        question+=translator.translate(PictureName[columnWin][rowWin]);

        Transition TransitionQuestion = CreateQuestionTransition(question);
        TransitionQuestion.play();

        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    public void dispose(){

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

    private void shuffleRow(int a, int b){
        String temp;
        for (int i=0; i<column; i++){
            temp = Picture[a][i];
            Picture[a][i] = Picture[b][i];
            Picture[b][i] = temp;

            temp = PictureName[a][i];
            PictureName[a][i] = PictureName[b][i];
            PictureName[b][i] = temp;
        }
    }

    private void shuffleColumn(int a, int b){
        String temp;
        for (int i=0; i<row; i++){
            temp = Picture[i][a];
            Picture[i][a] = Picture[i][b];
            Picture[i][b] = temp;

            temp = PictureName[i][a];
            PictureName[i][a] = PictureName[i][b];
            PictureName[i][b] = temp;
        }
    }

    private void win(){

    }
}
