package net.gazeplay.games.literacy;

import javafx.animation.TranslateTransition;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.AspectRatioImageRectangleUtil;

import java.util.Collections;
import java.util.Locale;

/**
 * Game "Letters": a letter is asked (written and said); the player must uncover a hidden picture by fixing the blocks
 * of this letter among blocks of other letters.
 */
public class Letters implements GameLifeCycle {

    /**
     * State of a round: the asked letter, its blocks left, and the blocks.
     */
    @Data
    public static class CurrentRoundDetails {

        private final Bloc[][] blocs;

        private final String mainLetter;

        protected int remainingCount;

        CurrentRoundDetails(int nbLines, int nbColumns, String mainLetter) {
            this.remainingCount = nbColumns * nbLines;
            this.mainLetter = mainLetter;
            this.blocs = new Bloc[nbLines][nbColumns];
        }

    }

    // Latin Letters!
    private final String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
        "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    private final IGameContext gameContext;

    private final int nbLines;

    private final int nbColomns;

    private final Stats stats;

    @Getter
    private final String currentLanguage;

    private final ImageLibrary imageLibrary;

    @NonNull
    private final Translator translator;

    private final ReplayablePseudoRandom randomGenerator;

    protected CurrentRoundDetails currentRoundDetails;

    private int correctCount;

    public Letters(IGameContext gameContext, int nbLines, int nbColumns, Stats stats) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColomns = nbColumns;

        this.stats = stats;

        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("blocs"), randomGenerator);
        translator = gameContext.getTranslator();

        Locale locale = translator.currentLocale();

        if (locale.getLanguage().equalsIgnoreCase("fra")) {
            this.currentLanguage = "fra";
        } else {
            this.currentLanguage = "eng";
        }

    }

    public Letters(IGameContext gameContext, int nbLines, int nbColumns, Stats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColomns = nbColumns;

        this.stats = stats;

        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("blocs"), randomGenerator);
        translator = gameContext.getTranslator();

        Locale locale = translator.currentLocale();

        if (locale.getLanguage().equalsIgnoreCase("fra")) {
            this.currentLanguage = "fra";
        } else {
            this.currentLanguage = "eng";
        }

    }

    /**
     * Shows the hidden picture, drawn from the images of the game.
     */
    private void setHiddenPicture(IGameContext gameContext) {
        final Image randomPicture = imageLibrary.pickRandomImage();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.setFill(new ImagePattern(randomPicture, 0, 0, 1, 1, true));

        AspectRatioImageRectangleUtil aspectRatioImageRectangleUtil = new AspectRatioImageRectangleUtil();
        aspectRatioImageRectangleUtil.setFillImageKeepingAspectRatio(imageRectangle, randomPicture, dimension2D);

        gameContext.getChildren().add(imageRectangle);
    }

    @Override
    public void launch() {

        final Configuration config = gameContext.getConfiguration();

        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final String mainLetter = pickRandomLetter();
        this.currentRoundDetails = new CurrentRoundDetails(nbLines, nbColomns, mainLetter);

        final Text questionText = new I18NText(this.translator, "Choose the Letter");

        questionText.setText(questionText.getText() + ": " + mainLetter.toUpperCase());

        questionText.setTranslateY(0);
        playSound(createQuestionSoundPath(currentLanguage, mainLetter));

        double positionX = dimension2D.getWidth() / 2d - questionText.getBoundsInParent().getWidth() * 2d;
        double positionY = dimension2D.getHeight() / 2d - questionText.getBoundsInParent().getHeight() / 2d;
        questionText.setX(positionX);
        questionText.setY(positionY);
        questionText.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(questionText, Pos.CENTER);
        questionText.setFill(Color.WHITE);
        questionText.setFont(new Font("Tsukushi A Round Gothic Bold", 60));
        gameContext.getChildren().add(questionText);

        TranslateTransition fullAnimation = new TranslateTransition(
            Duration.millis(gameContext.getConfiguration().getQuestionLength() / 2d), questionText);
        fullAnimation.setDelay(Duration.millis(gameContext.getConfiguration().getQuestionLength()));
        double bottomCenter = (0.9 * dimension2D.getHeight()) - questionText.getY()
            + questionText.getBoundsInParent().getHeight() * 3;
        fullAnimation.setToY(bottomCenter);

        fullAnimation.setOnFinished(actionEvent -> {

            questionText.toFront();
            questionText.setOpacity(0);

            setHiddenPicture(gameContext);

            double width = dimension2D.getWidth() / nbColomns;
            double height = dimension2D.getHeight() / nbLines;

            createCards(mainLetter, width, height, config);
            currentRoundDetails.remainingCount = correctCount;

            stats.notifyNewRoundReady();

            gameContext.onGameStarted(3000);
        });

        fullAnimation.play();

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    /**
     * @return a letter of the alphabet drawn at random
     */
    private String pickRandomLetter() {
        return alphabet[(randomGenerator.nextInt(alphabet.length))];
    }

    /**
     * Creates the grid of blocks: the asked letter on some of them, other letters on the others.
     */
    private Bloc[][] createCards(String mainLetter, double width, double height,
                                 Configuration config) {
        correctCount = 0;
        Bloc[][] blocs = new Bloc[nbLines][nbColomns];

        final int fixationlength = config.getFixationLength();

        final int rowTrue = randomGenerator.nextInt(nbLines);
        final int colTrue = randomGenerator.nextInt(nbColomns);

        for (int i = 0; i < nbLines; i++) {
            for (int j = 0; j < nbColomns; j++) {
                String currentLetter;

                float f = randomGenerator.nextFloat();

                if (i == rowTrue && j == colTrue) {
                    currentLetter = mainLetter;
                } else if (f < (0.6d - (1d / (nbColomns * nbLines)))) {
                    currentLetter = mainLetter;
                } else {
                    currentLetter = pickRandomLetter();
                }

                if (currentLetter.equals(mainLetter)) {
                    correctCount++;
                }

                Bloc bloc = new Bloc(j * width, i * height, width + 1, height + 1, currentLetter, mainLetter,
                    this, stats, gameContext, fixationlength);

                blocs[i][j] = bloc;

                this.currentRoundDetails.blocs[i][j] = bloc;
                this.gameContext.getChildren().add(bloc);
                bloc.toFront();

            }
        }

        return blocs;
    }

    @Override
    public void dispose() {
        if (currentRoundDetails != null) {
            if (currentRoundDetails.blocs != null) {
                gameContext.getChildren().removeAll(Collections.singleton(currentRoundDetails.blocs));
            }
            currentRoundDetails = null;
        }
    }

    /**
     * Removes all the blocks (end of the round).
     */
    void removeAllBlocs() {
        final Bloc[][] blocs = currentRoundDetails.blocs;
        int maxY = blocs[0].length;
        for (Bloc[] bloc : blocs) {
            for (int j = 0; j < maxY; j++) {
                removeBloc(bloc[j]);
            }
        }
    }

    /**
     * Removes a block, without its events.
     */
    void removeBloc(Bloc toRemove) {
        if (toRemove == null) {
            return;
        }
        toRemove.removeEventFilter(MouseEvent.ANY, toRemove.enterEvent);
        toRemove.removeEventFilter(GazeEvent.ANY, toRemove.enterEvent);
        gameContext.getGazeDeviceManager().removeEventFilter(toRemove);
        toRemove.setTranslateX(-10000);
        toRemove.setOpacity(0);
    }

    /**
     * @return the sound asking for a letter, said by a female or male voice drawn at random
     */
    private String createQuestionSoundPath(String currentLanguage, String currentLetter) {
        if (randomGenerator.nextBoolean()) {
            return "data/literacy/sounds/" + currentLanguage.toLowerCase() + "/f/quest/" + currentLetter.toUpperCase()
                + ".mp3";
        }
        return "data/literacy/sounds/" + currentLanguage.toLowerCase() + "/m/quest/" + currentLetter.toUpperCase()
            + ".mp3";
    }

    /**
     * Plays a sound.
     */
    private void playSound(String path) {
        gameContext.getSoundManager().add(path);
    }

}
