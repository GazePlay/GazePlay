package net.gazeplay.games.whereisit;

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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.difficulty.SourceSet;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ResourceFileManager;
import net.gazeplay.commons.utils.games.WhereIsItVaildator;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static net.gazeplay.games.whereisit.WhereIsItGameType.*;

/**
 * Games of the "Where is it?" family (animals, colors, flags, letters, numbers, shapes, sounds, the odd one out, or the
 * pictures of the user): a question is asked, written and said, and the player chooses the matching picture among
 * several by a fixation. In the variant with levels, the level (1 to 5) chooses the set of pictures shown with the
 * right one; it goes up or down every 8 goals, depending on whether the answers differ from answers at random
 * (chi-squared test, shown on the statistics screen).
 * <p>
 * Created by Didier Schwab on the 18/11/2017
 */
@Slf4j
public class WhereIsIt implements GameLifeCycle {
    private static final int NBMAXPICTO = 10;
    private static final double MAXSIZEPICTO = 250;

    private Text questionText;

    @Getter
    private final WhereIsItGameType gameType;

    private final int nbLines;
    private final int nbColumns;
    private final boolean fourThree;

    private int level = 1;
    private int lvlReplays = 1;
    private int rightDecision = 0;
    private int wrongDecision = 0;
    private boolean firstWrong = false;

    private final IGameContext gameContext;
    private final Stats stats;
    private RoundDetails currentRoundDetails;
    private final ReplayablePseudoRandom randomGenerator;

    private final ArrayList<TargetAOI> targetAOIList;

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns, final boolean fourThree,
                     final IGameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
        this.fourThree = fourThree;
        this.stats = stats;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());
    }

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns, final boolean fourThree,
                     final IGameContext gameContext, final Stats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
        this.fourThree = fourThree;
        this.stats = stats;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();

        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;
        log.debug("numberOfImagesToDisplayPerRound = {}", numberOfImagesToDisplayPerRound);

        final int winnerImageIndexAmongDisplayedImages = randomGenerator.nextInt(numberOfImagesToDisplayPerRound);
        log.debug("winnerImageIndexAmongDisplayedImages = {}", winnerImageIndexAmongDisplayedImages);

        if (stats.nbGoalsReached > 0 && stats.nbGoalsReached % 8 == 0) {
            stats.getChiReport().addChiObs(chi2Obs(rightDecision, wrongDecision));
            stats.getChiReport().addChiLevel(level);
            boolean randomness = chi2decision(rightDecision, wrongDecision);
            lvlReplays++;

            if (randomness && rightDecision > wrongDecision) {
                if (level < 5) {
                    level++;
                }
                rightDecision = 0;
                wrongDecision = 0;
                lvlReplays = 1;
            }
            if (!randomness && level > 1) {
                level--;
                rightDecision = 0;
                wrongDecision = 0;
                lvlReplays = 1;
            }
        }

        currentRoundDetails = pickAndBuildRandomPictures(numberOfImagesToDisplayPerRound, randomGenerator,
            winnerImageIndexAmongDisplayedImages);

        if (currentRoundDetails != null) {
            final Transition animation = createQuestionTransition(currentRoundDetails.getQuestion(), currentRoundDetails.getPictos());
            animation.play();
            if (currentRoundDetails.getQuestionSoundPath() != null) {
                playQuestionSound();
            }
        }

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();

        if (gameType.toString().contains("SOUNDS")) {
            gameContext.pauseBackgroundMusic();
        }
    }

    /**
     * @return the animation showing the question (and its pictograms) in the middle of the screen, then moving it away
     *   before the pictures appear
     */
    private Transition createQuestionTransition(final String question, final List<Image> listOfPictos) {
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
        StackPane.setAlignment(questionText, Pos.CENTER);

        gameContext.getChildren().add(questionText);
        final long timeStarted = System.currentTimeMillis();
        final TargetAOI targetAOI = new TargetAOI(gamePaneDimension2D.getWidth() / 2, gamePaneDimension2D.getHeight() / 2, (int) questionText.getBoundsInParent().getWidth(),
            timeStarted);
        targetAOI.setTimeEnded(timeStarted + gameContext.getConfiguration().getQuestionLength());
        targetAOIList.add(targetAOI);

        final List<Rectangle> pictogramsList = new ArrayList<>(20); // storage of actual Pictogram nodes in order to delete

        if (listOfPictos != null && !listOfPictos.isEmpty() && listOfPictos.size() <= NBMAXPICTO) {
            final Dimension2D screenDimension = gameContext.getCurrentScreenDimensionSupplier().get();
            final double screenWidth = screenDimension.getWidth();

            final double nbPicto = listOfPictos.size();

            double pictoSize = screenWidth / (nbPicto + 1);
            log.debug("screenWidth/(nbPicto) : {}", pictoSize);

            pictoSize = Math.min(pictoSize, MAXSIZEPICTO);
            log.debug("Picto Size: {}", pictoSize);

            int i = 0;
            final double shift = screenWidth / 2 - ((nbPicto / 2) * pictoSize * 1.1);

            log.debug("shift Size: {}", shift);

            for (final Image picto : listOfPictos) {
                final Rectangle pictoRectangle = new Rectangle(pictoSize, pictoSize);
                pictoRectangle.setFill(new ImagePattern(picto));
                pictoRectangle.setY(positionY + 100);
                pictoRectangle.setX(shift + (i++ * pictoSize * 1.1));
                pictogramsList.add(pictoRectangle);
            }

            gameContext.getChildren().addAll(pictogramsList);
        }

        final TranslateTransition fullAnimation = new TranslateTransition(
            Duration.millis(gameContext.getConfiguration().getQuestionLength() / 2.0), questionText);

        fullAnimation.setDelay(Duration.millis(gameContext.getConfiguration().getQuestionLength()));

        final double bottomCenter = (0.9 * gamePaneDimension2D.getHeight()) - questionText.getY()
            + questionText.getBoundsInParent().getHeight() * 3;
        fullAnimation.setToY(bottomCenter);

        fullAnimation.setOnFinished(actionEvent -> {
            gameContext.getChildren().removeAll(pictogramsList);

            if (currentRoundDetails != null) {
                gameContext.getChildren().addAll(currentRoundDetails.getPictureCardList());

                for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
                    p.toFront();
                    p.setOpacity(1);
                }
            }
            questionText.toFront();

            stats.notifyNewRoundReady();

            gameContext.onGameStarted(2000);
        });

        return fullAnimation;
    }

    /**
     * Plays the sound of the question.
     */
    void playQuestionSound() {
        String soundResource = currentRoundDetails.getQuestionSoundPath();
        gameContext.getSoundManager().add(soundResource);
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
        stats.setTargetAOIList(targetAOIList);
    }

    /**
     * Removes all the pictures except the right one, and records the end of the round for the areas of interest.
     */
    void removeAllIncorrectPictureCards() {
        //set the target AOI end time for this round

        final long endTime = System.currentTimeMillis();
        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;

        for (int i = 1; i <= numberOfImagesToDisplayPerRound; i++) {
            targetAOIList.get(targetAOIList.size() - i).setTimeEnded(endTime);
        }

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

    /**
     * @return whether the system recognises a file as an image
     */
    static boolean fileIsImageFile(File file) {
        try {
            String mimetype = Files.probeContentType(file.toPath());
            if (mimetype != null && mimetype.split("/")[0].equals("image")) {
                return true;
            }
        } catch (IOException ignored) {

        }
        return false;
    }

    /**
     * @return the pictures of a round, drawn at random from the folders of the game (or of the custom folder), one of
     *   them the right answer; null when there are not enough folders
     */
    RoundDetails pickAndBuildRandomPictures(final int numberOfImagesToDisplayPerRound, final ReplayablePseudoRandom random,
                                            final int winnerImageIndexAmongDisplayedImages) {
        final Configuration config = gameContext.getConfiguration();

        int directoriesCount;
        final String directoryName;
        List<File> imagesFolders = new LinkedList<>();
        List<String> resourcesFolders = new LinkedList<>();
        List<String> winnerFolders = new LinkedList<>();

        if (this.gameType == CUSTOMIZED) {
            final File imagesDirectory = new File(config.getWhereIsItDir() + "/images/");
            directoryName = imagesDirectory.getPath();
            directoriesCount = WhereIsItVaildator.getNumberOfValidDirectories(config.getWhereIsItDir(), imagesFolders);
        } else if (this.gameType == ANIMALS_DYNAMIC) {
            final String resourcesDirectory = "data/" + this.gameType.getResourcesDirectoryName();
            directoryName = resourcesDirectory;

            final String winnerImagesDirectory = resourcesDirectory + "/images/cats/";
            final String imagesDirectoryLvl1 = resourcesDirectory + "/images/lvl1/";
            final String imagesDirectoryLvl2 = resourcesDirectory + "/images/lvl2/";
            final String imagesDirectoryLvl3 = resourcesDirectory + "/images/lvl3/";
            final String imagesDirectoryLvl4 = resourcesDirectory + "/images/lvl4/";
            final String imagesDirectoryLvl5 = resourcesDirectory + "/images/lvl5/";

            String[] lvlDirectories = {imagesDirectoryLvl1, imagesDirectoryLvl2, imagesDirectoryLvl3, imagesDirectoryLvl4, imagesDirectoryLvl5};
            Set<String> tempWinnerFolders = ResourceFileManager.getResourceFolders(winnerImagesDirectory);

            for (int i = 1; i < lvlDirectories.length + 1; i++) {
                if (level == i) {
                    resourcesFolders.addAll(ResourceFileManager.getResourceFolders(lvlDirectories[i - 1]));
                }
            }

            winnerFolders.addAll(tempWinnerFolders);
            directoriesCount = resourcesFolders.size();
        } else {
            final String resourcesDirectory = "data/" + this.gameType.getResourcesDirectoryName();
            final String imagesDirectory = resourcesDirectory + "/images/";
            directoryName = imagesDirectory;

            // Here we filter out any unwanted resource folders, based on the variants JSON file
            Set<String> variantSet;
            try {
                SourceSet sourceSet = new SourceSet(resourcesDirectory + "/variants.json");
                variantSet = (sourceSet.getResources(this.gameType.getVariant()));
            } catch (FileNotFoundException fe) {
                log.info("No difficulty file found; Reading from all directories");
                variantSet = Collections.emptySet();
            }

            Set<String> tempResourcesFolders = ResourceFileManager.getResourceFolders(imagesDirectory);

            // If nothing can be found we take the entire folder contents.
            if (!variantSet.isEmpty()) {
                Set<String> finalVariantSet = variantSet;
                tempResourcesFolders = tempResourcesFolders
                    .parallelStream()
                    .filter(s -> finalVariantSet.parallelStream().anyMatch(s::contains))
                    .collect(Collectors.toSet());
            }

            resourcesFolders.addAll(tempResourcesFolders);
            directoriesCount = resourcesFolders.size();
        }

        final String language = config.getLanguage();

        if (directoriesCount == 0) {
            log.warn("No images found in Directory " + directoryName);
            error(language);
            return null;
        }

        int posX = 0;
        int posY = 0;

        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        final List<PictureCard> pictureCardList = new ArrayList<>();
        String questionSoundPath = null;
        String question = null;
        List<Image> pictograms = null;

        if (this.gameType == FIND_ODD) {
            int index = random.nextInt(resourcesFolders.size());
            final String folder = resourcesFolders.remove((index) % directoriesCount);

            index = random.nextInt(resourcesFolders.size());
            final String winnerFolder = resourcesFolders.remove((index) % directoriesCount);
            final String folderName = (new File(winnerFolder)).getName();

            for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {
                final Set<String> files;
                if (i == winnerImageIndexAmongDisplayedImages) {
                    files = ResourceFileManager.getResourcePaths(winnerFolder);
                } else {
                    files = ResourceFileManager.getResourcePaths(folder);
                }

                final int numFile = random.nextInt(files.size());
                final String randomImageFile = (String) files.toArray()[numFile];

                if (winnerImageIndexAmongDisplayedImages == i) {
                    question = MultilinguismFactory.getSingleton().getTranslation("FindTheOddOneOut", config.getLanguage());
                    pictograms = getPictograms(folderName);
                }

                final PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext, winnerImageIndexAmongDisplayedImages == i,
                    randomImageFile + "", stats, this);

                final TargetAOI targetAOI = new TargetAOI(gameSizing.width * (posX + 0.25), gameSizing.height * (posY + 1), (int) gameSizing.height,
                    System.currentTimeMillis());
                targetAOIList.add(targetAOI);

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
                int index = random.nextInt(imagesFolders.size());

                final File folder = imagesFolders.remove((index) % directoriesCount);
                final File[] files = getFiles(folder);
                List<File> validImageFiles = new ArrayList<>();

                for (File file : files) {
                    if (WhereIsItVaildator.fileIsImageFile(file)) {
                        validImageFiles.add(file);
                    }
                }

                final int numFile = random.nextInt(validImageFiles.size());
                final File randomImageFile = validImageFiles.get(numFile);

                if (winnerImageIndexAmongDisplayedImages == i) {
                    questionSoundPath = getPathSound(folder.getName(), language);
                    question = getQuestionText(folder.getName(), language);
                    pictograms = getPictograms(folder.getName());
                }

                // The image file needs 'file:' prepended as this will get images from a local source, not resources.
                final PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext,
                    winnerImageIndexAmongDisplayedImages == i, "file:" + randomImageFile, stats, this);

                final TargetAOI targetAOI = new TargetAOI(gameSizing.width * (posX + 0.25), gameSizing.height * (posY + 1), (int) gameSizing.height,
                    System.currentTimeMillis());
                targetAOIList.add(targetAOI);

                pictureCardList.add(pictureCard);

                if ((i + 1) % nbColumns != 0) {
                    posX++;
                } else {
                    posY++;
                    posX = 0;
                }
            }
        } else if (this.gameType == ANIMALS_DYNAMIC) {
            int index = random.nextInt(resourcesFolders.size());
            final String folder = resourcesFolders.remove((index) % directoriesCount);

            final String winnerFolder = winnerFolders.remove(0);
            final String folderName = (new File(winnerFolder)).getName();
            log.info("WinnerFolderName = {}", folderName);

            for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {
                final Set<String> files;
                if (i == winnerImageIndexAmongDisplayedImages) {
                    files = ResourceFileManager.getResourcePaths(winnerFolder);
                } else {
                    files = ResourceFileManager.getResourcePaths(folder);
                }

                final int numFile = random.nextInt(files.size());
                final String randomImageFile = (String) files.toArray()[numFile];

                if (winnerImageIndexAmongDisplayedImages == i) {
                    questionSoundPath = getPathSound(folderName, language);
                    question = getQuestionText(folderName, language);
                    pictograms = getPictograms(folderName);
                }

                final PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext, winnerImageIndexAmongDisplayedImages == i,
                    randomImageFile + "", stats, this);

                final TargetAOI targetAOI = new TargetAOI(gameSizing.width * (posX + 0.25), gameSizing.height * (posY + 1), (int) gameSizing.height,
                    System.currentTimeMillis());
                targetAOIList.add(targetAOI);

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
                int index = random.nextInt(resourcesFolders.size());

                final String folder = resourcesFolders.remove((index) % directoriesCount);
                final String folderName = (new File(folder)).getName();

                final Set<String> files = ResourceFileManager.getResourcePaths(folder);
                final int numFile = random.nextInt(files.size());
                final String randomImageFile = (String) files.toArray()[numFile];

                if (winnerImageIndexAmongDisplayedImages == i) {
                    questionSoundPath = getPathSound(folderName, language);
                    question = getQuestionText(folderName, language);
                    pictograms = getPictograms(folderName);
                }

                final PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext,
                    winnerImageIndexAmongDisplayedImages == i, randomImageFile + "", stats, this);

                pictureCardList.add(pictureCard);

                final TargetAOI targetAOI = new TargetAOI(gameSizing.width * (posX + 0.25), gameSizing.height * (posY + 1), (int) gameSizing.height,
                    System.currentTimeMillis());
                targetAOIList.add(targetAOI);


                if ((i + 1) % nbColumns != 0) {
                    posX++;
                } else {
                    posY++;
                    posX = 0;
                }
            }
        }
        return new RoundDetails(pictureCardList, winnerImageIndexAmongDisplayedImages, questionSoundPath, question, pictograms);
    }

    /**
     * Return all files which don't start with a point
     */
    private File[] getFiles(final File folder) {
        return folder.listFiles(file -> !file.getName().startsWith("."));
    }

    /**
     * Shows an error message when the pictures cannot be found.
     */
    private void error(final String language) {
        gameContext.clear();

        final Multilinguism multilinguism = MultilinguismFactory.getSingleton();

        final Text error = new Text(multilinguism.getTranslation("WII-error", language));
        final Region root = gameContext.getRoot();
        error.setX(root.getWidth() / 2. - 100);
        error.setY(root.getHeight() / 2.);
        error.setId("item");
        gameContext.getChildren().addAll(error);
    }

    /**
     * @return the sound of the question of a folder, in the language of the settings
     */
    private String getPathSound(final String folder, final String language) {
        if (this.gameType == CUSTOMIZED) {
            final Configuration config = gameContext.getConfiguration();
            try {
                log.debug("CUSTOMIZED");
                final String path = config.getWhereIsItDir() + "/sounds/";
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

        if (!(language.equals("fra") || language.equals("eng") || language.equals("chn"))) {
            // sound is only for English, French and Chinese erase when translation is complete
            return null;
        }

        log.debug("language is " + language);

        final String voice = randomGenerator.nextDouble() > 0.5 ? "m" : "w";

        if (gameType.toString().contains("SOUNDS")) {
            return "data/" + this.gameType.getResourcesDirectoryName() + "/sounds/" + folder + ".mp3";
        }
        return "data/" + this.gameType.getResourcesDirectoryName() + "/sounds/" + language + "/" + folder + "." + voice
            + "." + language + ".mp3";
    }

    /**
     * @return the text of the question of a folder, in the language of the settings
     */
    private String getQuestionText(final String folder, final String language) {
        log.debug("folder: {}", folder);
        log.debug("language: {}", language);

        if (this.gameType == CUSTOMIZED) {
            final Configuration config = gameContext.getConfiguration();
            final File questionFile = new File(config.getWhereIsItDir() + "/questions.csv");
            final Multilinguism localMultilinguism = MultilinguismFactory.getForResource(questionFile.toString());

            return localMultilinguism.getTranslation(folder, language);
        }

        final Multilinguism localMultilinguism = MultilinguismFactory.getForResource(gameType.getLanguageResourceLocation());

        if (gameType.toString().contains("SOUNDS")) {
            return localMultilinguism.getTranslation("listen", language);
        }
        return localMultilinguism.getTranslation(folder, language);
    }

    /**
     * @return the pictograms illustrating the question of a folder
     */
    private List<Image> getPictograms(final String folder) {
        final String language = "pictos";

        if (this.gameType != CUSTOMIZED) {
            return null;
        }

        final Configuration config = gameContext.getConfiguration();
        final File questionFile = new File(config.getWhereIsItDir(), "questions.csv");
        final Multilinguism localMultilinguism = MultilinguismFactory.getForResource(questionFile.toString());
        final String traduction = localMultilinguism.getTranslation(folder, language);
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

    /**
     * Counts a right answer of the current level.
     */
    public void updateRight() {
        rightDecision++;
    }

    /**
     * Counts a wrong answer of the current level.
     */
    public void updateWrong() {
        wrongDecision++;
    }

    /**
     * @return whether the player chose a wrong picture first in this round
     */
    public boolean getFirstWrong() {
        return firstWrong;
    }

    /**
     * Records that the player chose a wrong picture first.
     */
    public void firstWrongCardSelected() {
        firstWrong = true;
    }

    /**
     * Records that the player chose the right picture first.
     */
    public void firstRightCardSelected() {
        firstWrong = false;
    }

    /**
     * @return n!
     */
    public int factorial(int n) {
        return n == 0 ? 1 : (n * factorial(n - 1));
    }

    /**
     * @return the binomial coefficient "k among n"
     */
    public float compute(int n, int k) {
        return (float) factorial(n) / (factorial(k) * factorial(n - k));
    }

    /**
     * @return the probability of k successes in n trials of probability p (binomial law)
     */
    public double binomProba(int n, int k, double p) {
        return compute(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    /**
     * @return the chi-squared value of the right ({@code tp}) and wrong ({@code fp}) answers, against answers at random
     *   (one chance in two), over 8 answers per replay of the level
     */
    public double chi2Obs(int tp, int fp) {
        double[] probas = {8 * lvlReplays * binomProba(1, 1, 0.5), 8 * lvlReplays * binomProba(1, 0, 0.5)};

        return Math.pow(tp - probas[0], 2) / probas[0] + Math.pow(fp - probas[1], 2) / probas[1];
    }

    /**
     * Critical values of the chi-squared law with one degree of freedom, for the risks 5 %, 10 %, 25 % and 50 %.
     */
    private static final double[] CHI2_THEORETIC = {3.84, 2.71, 1.32, 0.45};

    /**
     * @return whether the answers differ from random answers: the chi-squared value reaches the critical value of
     *   {@link #chi2Threshold(int)}
     */
    public boolean chi2decision(int tp, int fp) {
        double chi2Obs = chi2Obs(tp, fp);

        log.info("tp = {}, fp = {}", tp, fp);
        log.info("chi2_obs = {}", chi2Obs);
        log.info("chi2_th = {}, replays = {}", chi2Threshold(lvlReplays), lvlReplays);

        return chi2Threshold(lvlReplays) <= chi2Obs;
    }

    /**
     * The critical value used: 0.45 (risk 50 %). From the 5th replay of a level, the index used to be
     * {@code replays - 1}, outside the 4 values: the game ended with an IndexOutOfBoundsException (a player staying
     * at level 1 with answers judged random, after 40 right answers). The index is now kept on the last value, the one
     * of the first 4 replays: the decisions that did not fail are unchanged.
     *
     * @param replays number of replays of the current level
     * @return the critical value the observed chi-squared value is compared with
     */
    static double chi2Threshold(final int replays) {
        final int index = replays > 4 ? replays - 1 : 3;
        return CHI2_THEORETIC[Math.min(index, CHI2_THEORETIC.length - 1)];
    }
}
