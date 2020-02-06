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
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.games.ResourceFileManager;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.File;
import java.util.*;

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

        final Random random = new Random();
        final int winnerImageIndexAmongDisplayedImages = random.nextInt(numberOfImagesToDisplayPerRound);
        log.debug("winnerImageIndexAmongDisplayedImages = {}", winnerImageIndexAmongDisplayedImages);

        currentRoundDetails = pickAndBuildRandomPictures(numberOfImagesToDisplayPerRound, random,
            winnerImageIndexAmongDisplayedImages);

        if (currentRoundDetails != null) {

            final Transition animation = createQuestionTransition(currentRoundDetails.getQuestion(), currentRoundDetails.getPictos());
            animation.play();
            if (currentRoundDetails.getQuestionSoundPath() != null) {
                playQuestionSound();
            }
        }

    }

    private Transition createQuestionTransition(final String question, final List<Image> Pictos) {

        questionText = new Text(question);

        questionText.setTranslateY(0);

        final String color = (gameContext.getConfiguration().isBackgroundWhite()) ? "titleB" : "titleW";
        questionText.setId(color);

        final Dimension2D gamePaneDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final double positionX = gamePaneDimension2D.getWidth() / 2 - questionText.getBoundsInParent().getWidth() * 2;
        final double positionY = gamePaneDimension2D.getHeight() / 2 - questionText.getBoundsInParent().getHeight() / 2;

        questionText.setX(positionX);
        questionText.setY(positionY);
        questionText.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(questionText, Pos.CENTER);

        gameContext.getChildren().add(questionText);

        final List<Rectangle> pictogramesList = new ArrayList<>(20); // storage of actual Pictogramm nodes in order to delete
        // them
        // from the group later

        if (Pictos != null && !Pictos.isEmpty() && Pictos.size() <= NBMAXPICTO) {

            final Dimension2D screenDimension = gameContext.getCurrentScreenDimensionSupplier().get();
            final double screenWidth = screenDimension.getWidth();

            final double nbPicto = Pictos.size();

            double pictoSize = screenWidth / (nbPicto + 1);

            log.debug("screenWidth/(nbPicto) : {}", pictoSize);

            pictoSize = Math.min(pictoSize, MAXSIZEPICTO);

            log.debug("Picto Size: {}", pictoSize);

            int i = 0;
            final double shift = screenWidth / 2 - ((nbPicto / 2) * pictoSize * 1.1);

            log.debug("shift Size: {}", shift);

            for (final Image I : Pictos) {

                final Rectangle R = new Rectangle(pictoSize, pictoSize);
                R.setFill(new ImagePattern(I));
                R.setY(positionY + 100);
                R.setX(shift + (i++ * pictoSize * 1.1));
                pictogramesList.add(R);
            }

            gameContext.getChildren().addAll(pictogramesList);
        }

        final TranslateTransition fullAnimation = new TranslateTransition(
            Duration.millis(gameContext.getConfiguration().getQuestionLength() / 2.0), questionText);

        fullAnimation.setDelay(Duration.millis(gameContext.getConfiguration().getQuestionLength()));

        final double bottomCenter = (0.9 * gamePaneDimension2D.getHeight()) - questionText.getY()
            + questionText.getBoundsInParent().getHeight() * 3;
        fullAnimation.setToY(bottomCenter);

        fullAnimation.setOnFinished(actionEvent -> {
            // gameContext.getChildren().remove(questionText);

            gameContext.getChildren().removeAll(pictogramesList);

            //log.debug("Adding {} pictures", currentRoundDetails.getPictureCardList().size());
            gameContext.getChildren().addAll(currentRoundDetails.getPictureCardList());

            for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
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
        } catch (final Exception e) {
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
        final List<PictureCard> pictureCardsToHide = new ArrayList<>();
        for (final PictureCard pictureCard : this.currentRoundDetails.getPictureCardList()) {
            if (!pictureCard.isWinner()) {
                pictureCardsToHide.add(pictureCard);
            }
        }

        // remove all at once, in order to update the UserInterface only once
        gameContext.getChildren().removeAll(pictureCardsToHide);
    }

    RoundDetails pickAndBuildRandomPictures(final int numberOfImagesToDisplayPerRound, final Random random,
                                            final int winnerImageIndexAmongDisplayedImages) {

        final Configuration config = gameContext.getConfiguration();

        final int filesCount;
        final String directoryName;
        File[] imagesFolders = new File[1];
        Set<String> resourcesFolders = Collections.emptySet();

        if (this.gameType == CUSTOMIZED) {
            final File imagesDirectory = new File(config.getWhereIsItDir() + "/images/");
            directoryName = imagesDirectory.getPath();
            imagesFolders = imagesDirectory.listFiles();
            filesCount = imagesFolders == null ? 0 : imagesFolders.length;
        } else {
            final String imagesDirectory = "data/" + this.gameType.getResourcesDirectoryName() + "/images/";
            directoryName = imagesDirectory;
            resourcesFolders = ResourceFileManager.getResourceFolders(imagesDirectory);
            filesCount = resourcesFolders.size();
        }

        final String language = config.getLanguage();

        if (filesCount == 0) {
            log.warn("No images found in Directory " + directoryName);
            error(language);
            return null;
        }
        final int randomFolderIndex = random.nextInt(filesCount);

        final int step = 1;

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

                final String folder = (String) resourcesFolders.toArray()[(index) % filesCount];
                final String folderName = (new File(folder)).getName();

                final Set<String> files = ResourceFileManager.getResourcePaths(folder);

                final int numFile = random.nextInt(files.size());

                final String randomImageFile = (String) files.toArray()[numFile];

                if (winnerImageIndexAmongDisplayedImages == i) {

                    // TODO for now the line under is commented to avoid freeze
                    //questionSoundPath = getPathSound(imagesFolders[(index) % filesCount].getName(), language);

                    question = Multilinguism.getSingleton().getTrad("findodd", config.getLanguage());

                    pictograms = getPictogramms(folderName);

                }

                final PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext, winnerImageIndexAmongDisplayedImages == i,
                    randomImageFile + "", stats, this);

                pictureCardList.add(pictureCard);

                if ((i + 1) % nbColumns != 0) {
                    posX++;
                } else {
                    posY++;
                    posX = 0;
                }
            }

        } else if (this.gameType == CUSTOMIZED) {
            for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {

                final int index = (randomFolderIndex + step * i) % filesCount;

                final File folder = imagesFolders[(index) % filesCount];

                if (!folder.isDirectory()) {
                    continue;
                }

                final File[] files = getFiles(folder);

                final int numFile = random.nextInt(files.length);

                final File randomImageFile = files[numFile];

                if (winnerImageIndexAmongDisplayedImages == i) {

                    questionSoundPath = getPathSound(folder.getName(), language);

                    question = getQuestionText(folder.getName(), language);

                    pictograms = getPictogramms(folder.getName());

                }

                // The image file needs 'file:' prepended as this will get images from a local source, not resources.
                final PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext,
                    winnerImageIndexAmongDisplayedImages == i, "file:" + randomImageFile, stats, this);

                pictureCardList.add(pictureCard);


                if ((i + 1) % nbColumns != 0) {
                    posX++;
                } else {
                    posY++;
                    posX = 0;
                }
            }
        } else {
            for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {

                final int index = (randomFolderIndex + step * i) % filesCount;

                final String folder = (String) resourcesFolders.toArray()[(index) % filesCount];
                final String folderName = (new File(folder)).getName();

                final Set<String> files = ResourceFileManager.getResourcePaths(folder);

                final int numFile = random.nextInt(files.size());

                final String randomImageFile = (String) files.toArray()[numFile];

                if (winnerImageIndexAmongDisplayedImages == i) {

                    questionSoundPath = getPathSound(folderName, language);

                    question = getQuestionText(folderName, language);

                    pictograms = getPictogramms(folderName);

                }

                final PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext,
                    winnerImageIndexAmongDisplayedImages == i, randomImageFile + "", stats, this);

                pictureCardList.add(pictureCard);


                if ((i + 1) % nbColumns != 0) {
                    posX++;
                } else {
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
    private File[] getFiles(final File folder) {
        return folder.listFiles(file -> !file.getName().startsWith("."));
    }

    private void error(final String language) {

        gameContext.clear();
        // HomeUtils.home(scene, group, choiceBox, null);

        final Multilinguism multilinguism = Multilinguism.getSingleton();

        final Text error = new Text(multilinguism.getTrad("WII-error", language));
        final Region root = gameContext.getRoot();
        error.setX(root.getWidth() / 2. - 100);
        error.setY(root.getHeight() / 2.);
        error.setId("item");
        gameContext.getChildren().addAll(error);
    }

    private String getPathSound(final String folder, final String language) {
        if (this.gameType == CUSTOMIZED) {
            final Configuration config = gameContext.getConfiguration();
            try {
                log.debug("CUSTOMIZED");
                final String path = config.getWhereIsItDir() + "sounds/";
                final File soundsDirectory = new File(path);
                final String[] soundsDirectoryFiles = soundsDirectory.list();
                if (soundsDirectoryFiles != null) {
                    for (final String file : soundsDirectoryFiles) {
                        log.debug("file " + file);
                        log.debug("folder " + folder);
                        if (file.contains(folder)) {
                            final File f = new File(path + file);
                            log.debug("file " + f.getAbsolutePath());
                            return f.getAbsolutePath();
                        }
                    }
                }
            } catch (final Exception e) {
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

    private String getQuestionText(final String folder, final String language) {

        log.debug("folder: {}", folder);
        log.debug("language: {}", language);

        if (this.gameType == CUSTOMIZED) {

            final Configuration config = gameContext.getConfiguration();

            final File F = new File(config.getWhereIsItDir() + "/questions.csv");

            final Multilinguism localMultilinguism = Multilinguism.getForResource(F.toString());

            return localMultilinguism.getTrad(folder, language);
        }

        final Multilinguism localMultilinguism = Multilinguism.getForResource(gameType.getLanguageResourceLocation());

        return localMultilinguism.getTrad(folder, language);
    }

    private List<Image> getPictogramms(final String folder) {

        final String language = "pictos";

        if (this.gameType != CUSTOMIZED) {

            return null;
        }

        final Configuration config = gameContext.getConfiguration();

        final File questionFile = new File(config.getWhereIsItDir(), "questions.csv");

        final Multilinguism localMultilinguism = Multilinguism.getForResource(questionFile.toString());

        final String traduction = localMultilinguism.getTrad(folder, language);

        log.debug("traduction: {}", traduction);

        final StringTokenizer st = new StringTokenizer(traduction, ";");

        final List<Image> imageList = new ArrayList<>(20);

        while (st.hasMoreTokens()) {
            final String token = config.getWhereIsItDir() + "/pictos/" + st.nextToken().replace('\u00A0', ' ').trim();
            log.debug("token \"{}\"", token);
            final File tokenFile = new File(token);
            log.debug("Exists {}", tokenFile.exists());
            if (tokenFile.exists()) {
                imageList.add(new Image(tokenFile.toURI().toString(), 500, 500, true, false));
            }
        }

        log.debug("imageList: {}", imageList);
        return imageList;
    }
}
