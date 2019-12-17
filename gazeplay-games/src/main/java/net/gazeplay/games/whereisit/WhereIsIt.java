package net.gazeplay.games.whereisit;

//It is repeated always, it works like a charm :)

import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.games.ImageDirectoryLocator;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import static net.gazeplay.games.whereisit.WhereIsItGameType.*;

/**
 * Created by Didier Schwab on the 18/11/2017
 */
@Slf4j
public class WhereIsIt implements GameLifeCycle {

    private static final int NBMAXPICTO = 10;
    private static final double MAXSIZEPICTO = 250;

    private Text questionText;

    private final WhereIsItGameType gameType;

    private final int nbLines;
    private final int nbColumns;
    private final boolean fourThree;

    private final IGameContext gameContext;
    private final Stats stats;
    private RoundDetails currentRoundDetails;

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns, final boolean fourThree,
                     final IGameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
        this.fourThree = fourThree;
        this.stats = stats;
    }

    @Override
    public void launch() {

        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;
        log.debug("numberOfImagesToDisplayPerRound = {}", numberOfImagesToDisplayPerRound);

        Random random = new Random();
        final int winnerImageIndexAmongDisplayedImages = random.nextInt(numberOfImagesToDisplayPerRound);
        log.debug("winnerImageIndexAmongDisplayedImages = {}", winnerImageIndexAmongDisplayedImages);

        final Configuration config = gameContext.getConfiguration();

        currentRoundDetails = pickAndBuildRandomPictures(config, numberOfImagesToDisplayPerRound, random,
            winnerImageIndexAmongDisplayedImages);

        if (currentRoundDetails != null) {

            Transition animation = createQuestionTransition(currentRoundDetails.getQuestion(), currentRoundDetails.getPictos());
            animation.play();
            if (currentRoundDetails.getQuestionSoundPath() != null)
                playQuestionSound();
        }

    }

    private Transition createQuestionTransition(String question, List<Image> Pictos) {

        questionText = new Text(question);

        questionText.setTranslateY(0);

        String color = (gameContext.getConfiguration().isBackgroundWhite()) ? "titleB" : "titleW";
        questionText.setId(color);

        final Dimension2D gamePaneDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double positionX = gamePaneDimension2D.getWidth() / 2 - questionText.getBoundsInParent().getWidth() * 2;
        double positionY = gamePaneDimension2D.getHeight() / 2 - questionText.getBoundsInParent().getHeight() / 2;

        questionText.setX(positionX);
        questionText.setY(positionY);
        questionText.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(questionText, Pos.CENTER);

        gameContext.getChildren().add(questionText);

        List<Rectangle> pictogramesList = new ArrayList<>(20); // storage of actual Pictogramm nodes in order to delete
        // them
        // from the group later

        if (Pictos != null && !Pictos.isEmpty() && Pictos.size() <= NBMAXPICTO) {

            double screenWidth = Screen.getPrimary().getBounds().getWidth();

            double nbPicto = Pictos.size();

            double pictoSize = screenWidth / (nbPicto + 1);

            log.debug("screenWidth/(nbPicto) : {}", pictoSize);

            pictoSize = Math.min(pictoSize, MAXSIZEPICTO);

            log.debug("Picto Size: {}", pictoSize);

            int i = 0;
            double shift = screenWidth / 2 - ((nbPicto / 2) * pictoSize * 1.1);

            log.debug("shift Size: {}", shift);

            for (Image I : Pictos) {

                Rectangle R = new Rectangle(pictoSize, pictoSize);
                R.setFill(new ImagePattern(I));
                R.setY(positionY + 100);
                R.setX(shift + (i++ * pictoSize * 1.1));
                pictogramesList.add(R);
            }

            gameContext.getChildren().addAll(pictogramesList);
        }

        TranslateTransition fullAnimation = new TranslateTransition(
            Duration.millis(gameContext.getConfiguration().getQuestionLength() / 2.0), questionText);

        fullAnimation.setDelay(Duration.millis(gameContext.getConfiguration().getQuestionLength()));

        double bottomCenter = (0.9 * gamePaneDimension2D.getHeight()) - questionText.getY()
            + questionText.getBoundsInParent().getHeight() * 3;
        fullAnimation.setToY(bottomCenter);

        fullAnimation.setOnFinished(actionEvent -> {
            // gameContext.getChildren().remove(questionText);

            gameContext.getChildren().removeAll(pictogramesList);

            //log.debug("Adding {} pictures", currentRoundDetails.getPictureCardList().size());
            gameContext.getChildren().addAll(currentRoundDetails.getPictureCardList());

            for (PictureCard p : currentRoundDetails.getPictureCardList()) {
                //  log.debug("p = {}", p);
                p.toFront();
                p.setOpacity(1);
            }

            questionText.toFront();

            stats.notifyNewRoundReady();

            gameContext.onGameStarted();
        });

        return fullAnimation;
    }

    void playQuestionSound() {
        try {
            log.debug("currentRoundDetails.questionSoundPath: {}", currentRoundDetails.getQuestionSoundPath());
            ForegroundSoundsUtils.playSound(currentRoundDetails.getQuestionSoundPath());
        } catch (Exception e) {
            log.warn("Can't play sound: no associated sound : " + e.toString());
        }
    }

    /**
     * this method should be called when exiting the game, or before starting a new round, in order to clean up all
     * resources in both UI and memory
     */
    @Override
    public void dispose() {
        if (currentRoundDetails != null) {
            if (currentRoundDetails.getPictureCardList() != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.getPictureCardList());
            }
            currentRoundDetails = null;
        }
    }

    void removeAllIncorrectPictureCards() {
        if (this.currentRoundDetails == null) {
            return;
        }

        // Collect all items to be removed from the User Interface
        List<PictureCard> pictureCardsToHide = new ArrayList<>();
        for (PictureCard pictureCard : this.currentRoundDetails.getPictureCardList()) {
            if (!pictureCard.isWinner()) {
                pictureCardsToHide.add(pictureCard);
            }
        }

        // remove all at once, in order to update the UserInterface only once
        gameContext.getChildren().removeAll(pictureCardsToHide);
    }

    RoundDetails pickAndBuildRandomPictures(final Configuration config,
                                            final int numberOfImagesToDisplayPerRound, final Random random,
                                            final int winnerImageIndexAmongDisplayedImages) {

        final File imagesDirectory = locateImagesDirectory(config);

        final String language = config.getLanguage();

        final File[] imagesFolders = imagesDirectory.listFiles();

        final int filesCount = imagesFolders == null ? 0 : imagesFolders.length;

        if (filesCount == 0) {
            log.warn("No image found in Directory " + imagesDirectory);
            error(language);
            return null;
        }
        final int randomFolderIndex = random.nextInt(filesCount);

        int step = 1;

        int posX = 0;
        int posY = 0;

        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        final List<PictureCard> pictureCardList = new ArrayList<>();
        String questionSoundPath = null;
        String question = null;
        List<Image> pictograms = null;
        if (this.gameType == FINDODD) {
            int index = ((randomFolderIndex + step) % filesCount) + 1;
            for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {

                if (i == winnerImageIndexAmongDisplayedImages) {
                    index = (index + 1) % filesCount;
                } else {
                    index = ((randomFolderIndex + step) % filesCount) + 1;
                }

                final File folder = imagesFolders[(index) % filesCount];

                if (!folder.isDirectory())
                    continue;

                // final File[] files = folder.listFiles();
                final File[] files = getFiles(folder);

                final int numFile = random.nextInt(files.length);

                final File randomImageFile = files[numFile];

                if (winnerImageIndexAmongDisplayedImages == i) {

                    // TODO check a new question path for 'Find the Odd one Out' question
                    // TODO for now the line under is commented to avoid freeze
                    //questionSoundPath = getPathSound(imagesFolders[(index) % filesCount].getName(), language);

                    question = "Find the Odd one Out";

                    pictograms = getPictogramms(imagesFolders[(index) % filesCount].getName());

                }

                PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext, winnerImageIndexAmongDisplayedImages == i,
                    randomImageFile + "", stats, this);

                pictureCardList.add(pictureCard);

                if ((i + 1) % nbColumns != 0)
                    posX++;
                else {
                    posY++;
                    posX = 0;
                }
            }

        } else {
            for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {

                final int index = (randomFolderIndex + step * i) % filesCount;

                final File folder = imagesFolders[(index) % filesCount];

                if (!folder.isDirectory())
                    continue;

                // final File[] files = folder.listFiles();
                final File[] files = getFiles(folder);

                final int numFile = random.nextInt(files.length);

                final File randomImageFile = files[numFile];

                if (winnerImageIndexAmongDisplayedImages == i) {

                    questionSoundPath = getPathSound(imagesFolders[(index) % filesCount].getName(), language);

                    question = getQuestionText(imagesFolders[(index) % filesCount].getName(), language);

                    pictograms = getPictogramms(imagesFolders[(index) % filesCount].getName());

                }

                PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext,
                    winnerImageIndexAmongDisplayedImages == i, randomImageFile + "", stats, this);

                pictureCardList.add(pictureCard);


                if ((i + 1) % nbColumns != 0)
                    posX++;
                else {
                    posY++;
                    posX = 0;
                }
            }
        }
        return new RoundDetails(pictureCardList, winnerImageIndexAmongDisplayedImages, questionSoundPath, question,
            pictograms);
    }

    /**
     * Return all files which don't start with a point
     */
    private File[] getFiles(File folder) {
        return folder.listFiles(file -> !file.getName().startsWith("."));
    }

    private void error(String language) {

        gameContext.clear();
        // HomeUtils.home(scene, group, choiceBox, null);

        Multilinguism multilinguism = Multilinguism.getSingleton();

        Text error = new Text(multilinguism.getTrad("WII-error", language));
        final Region root = gameContext.getRoot();
        error.setX(root.getWidth() / 2. - 100);
        error.setY(root.getHeight() / 2.);
        error.setId("item");
        gameContext.getChildren().addAll(error);
    }

    private File locateImagesDirectory(Configuration config) {

        File result;

        if (this.gameType == CUSTOMIZED) {

            result = new File(config.getWhereIsItDir() + "/images/");
        } else {

            result = ImageDirectoryLocator.locateImagesDirectoryInUnpackedDistDirectory(
                "data/" + this.gameType.getResourcesDirectoryName() + "/images/");

            if (result == null) {
                result = ImageDirectoryLocator.locateImagesDirectoryInExplodedClassPath(
                    "data/" + this.gameType.getResourcesDirectoryName() + "/images/");
            }
        }
        return result;
    }

    private String getPathSound(final String folder, String language) {
        if (this.gameType == CUSTOMIZED) {
            final Configuration config = gameContext.getConfiguration();
            try {
                log.debug("CUSTOMIZED");
                String path = config.getWhereIsItDir() + "sounds/";
                File soundsDirectory = new File(path);
                String[] soundsDirectoryFiles = soundsDirectory.list();
                if (soundsDirectoryFiles != null) {
                    for (String file : soundsDirectoryFiles) {
                        log.debug("file " + file);
                        log.debug("folder " + folder);
                        if (file.contains(folder)) {
                            File f = new File(path + file);
                            log.debug("file " + f.getAbsolutePath());
                            return f.getAbsolutePath();
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Problem with customized folder");
                error(config.getLanguage());
            }
            return "";
        }

        if (gameType == LETTERS || gameType == NUMBERS || gameType == FLAGS) {// no sound for now
            // erase when translation is complete
            return null;
        }

        if (!(language.equals("fra") || language.equals("eng"))) {
            // sound is only for English and French for animals and colors
            // erase when translation is complete
            return null;
        }

        log.debug("language is " + language);

        final String voice;
        if (Math.random() > 0.5) {
            voice = "m";
        } else {
            voice = "w";
        }

        return "data/" + this.gameType.getResourcesDirectoryName() + "/sounds/" + language + "/" + folder + "." + voice
            + "." + language + ".mp3";
    }

    private String getQuestionText(final String folder, String language) {

        log.debug("folder: {}", folder);
        log.debug("language: {}", language);

        if (this.gameType == CUSTOMIZED) {

            final Configuration config = gameContext.getConfiguration();

            File F = new File(config.getWhereIsItDir() + "questions.csv");

            Multilinguism localMultilinguism = Multilinguism.getForResource(F.toString());

            String traduction = localMultilinguism.getTrad(folder, language);

            return traduction;
        }

        Multilinguism localMultilinguism = Multilinguism.getForResource(gameType.getLanguageResourceLocation());

        String traduction = localMultilinguism.getTrad(folder, language);
        return traduction;
    }

    private List<Image> getPictogramms(final String folder) {

        final String language = "pictos";

        if (this.gameType != CUSTOMIZED) {

            return null;
        }

        final Configuration config = gameContext.getConfiguration();

        File F = new File(config.getWhereIsItDir() + "questions.csv");

        Multilinguism localMultilinguism = Multilinguism.getForResource(F.toString());

        String traduction = localMultilinguism.getTrad(folder, language);

        log.debug("traduction: {}", traduction);

        StringTokenizer st = new StringTokenizer(traduction, ";");

        String token;

        List<Image> L = new ArrayList<>(20);

        while (st.hasMoreTokens()) {

            token = config.getWhereIsItDir() + "pictos/" + st.nextToken().replace('\u00A0', ' ').trim();
            log.debug("token \"{}\"", token);
            File Ftoken = new File(token);
            log.debug("Exists {}", Ftoken.exists());
            if (Ftoken.exists()) {
                L.add(new Image(Ftoken.toURI().toString(), 500, 500, true, false));
            }
        }

        log.debug("L {}", L);
        return L;
    }
}
