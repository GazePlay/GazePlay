package net.gazeplay.games.whereisit;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.difficulty.SourceSet;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.games.ResourceFileManager;
import net.gazeplay.commons.utils.games.WhereIsItVaildator;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.components.GameStyle;

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
 * The question is first shown in the middle of the screen, then goes to a band at the bottom, with a button to hear
 * it again; the pictures are rounded cards above the band.
 * <p>
 * Created by Didier Schwab on the 18/11/2017
 */
@Slf4j
public class WhereIsIt implements GameLifeCycle {
    private static final int NBMAXPICTO = 10;
    private static final double MAXSIZEPICTO = 250;

    /**
     * Languages whose voices said the questions before October 2026, when the only ones looked for.
     */
    private static final Set<String> LANGUAGES_WITH_VOICES = Set.of("fra", "eng", "chn");

    /**
     * Longest move of the question from the middle of the screen to the band, in ms.
     */
    private static final double QUESTION_MOVE_MAX_LENGTH = 800;

    private Group questionPill;

    private ReplayQuestionButton replayButton;

    @Getter
    private final WhereIsItGameType gameType;

    private final int nbLines;
    private final int nbColumns;

    private int level = 1;
    private int lvlReplays = 1;
    private int rightDecision = 0;
    private int wrongDecision = 0;
    private boolean firstWrong = false;

    /**
     * Name of the folder of the right answer of the previous round: it is not the right answer of the next one.
     */
    private String previousWinnerFolderName;

    private final IGameContext gameContext;
    private final Stats stats;
    private RoundDetails currentRoundDetails;
    private final ReplayablePseudoRandom randomGenerator;

    private final ArrayList<TargetAOI> targetAOIList;

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns,
                     final IGameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
        this.stats = stats;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());
    }

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns,
                     final IGameContext gameContext, final Stats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
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
     * @return the animation showing the question (with its pictograms, or a loudspeaker for the sounds) in the middle
     *   of the screen, then moving it to the band at the bottom before the pictures appear
     */
    private Transition createQuestionTransition(final String question, final List<Image> listOfPictos) {
        final Dimension2D gamePaneDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double width = gamePaneDimension2D.getWidth();
        final double height = gamePaneDimension2D.getHeight();
        final GameSizing sizing = new GameSizingComputer(nbLines, nbColumns).computeGameSizing(gamePaneDimension2D);
        final long questionLength = gameContext.getConfiguration().getQuestionLength();

        // In the middle: as large as the screen allows
        final double largeFont = Math.min(height / 9, width / 14);
        questionPill = GameStyle.pill(question == null ? "" : question, largeFont);
        double pillWidth = questionPill.getLayoutBounds().getWidth();
        if (pillWidth > width * 0.9) {
            questionPill = GameStyle.pill(question == null ? "" : question, largeFont * width * 0.9 / pillWidth);
            pillWidth = questionPill.getLayoutBounds().getWidth();
        }
        final double pillHeight = questionPill.getLayoutBounds().getHeight();
        final boolean withPictos = listOfPictos != null && !listOfPictos.isEmpty() && listOfPictos.size() <= NBMAXPICTO;
        final double pillCenterY = height * (withPictos ? 0.3 : 0.45);
        questionPill.setLayoutX(width / 2 - pillWidth / 2);
        questionPill.setLayoutY(pillCenterY - pillHeight / 2);
        gameContext.getChildren().add(questionPill);

        final long timeStarted = System.currentTimeMillis();
        final TargetAOI targetAOI = new TargetAOI(width / 2, pillCenterY, (int) pillWidth, timeStarted);
        targetAOI.setTimeEnded(timeStarted + questionLength);
        targetAOIList.add(targetAOI);

        // Shown with the question, removed when the pictures appear
        final List<Node> questionDecorations = new ArrayList<>();

        if (withPictos) {
            final double nbPicto = listOfPictos.size();
            final double pictoSize = Math.min(width / (nbPicto + 1), MAXSIZEPICTO);
            final double shift = width / 2 - ((nbPicto / 2) * pictoSize * 1.1);
            int i = 0;
            for (final Image picto : listOfPictos) {
                final Rectangle pictoRectangle = new Rectangle(pictoSize, pictoSize);
                pictoRectangle.setFill(new ImagePattern(picto));
                pictoRectangle.setY(pillCenterY + pillHeight / 2 + sizing.gap() * 2);
                pictoRectangle.setX(shift + (i++ * pictoSize * 1.1));
                questionDecorations.add(pictoRectangle);
            }
        }

        final List<Transition> decorationAnimations = new ArrayList<>();
        if (gameType.toString().contains("SOUNDS")) {
            // A loudspeaker beating above the question while the sound is played
            final double speakerSize = height * 0.22;
            final SVGPath speakerIcon = GameStyle.icon("volume_up_fill1", speakerSize);
            speakerIcon.setStyle("-fx-fill: -gp-accent;");
            final Group speaker = new Group(speakerIcon);
            speaker.setLayoutX(width / 2 - speakerSize / 2);
            speaker.setLayoutY(pillCenterY - pillHeight / 2 - sizing.gap() * 2 - speakerSize);
            final ScaleTransition beat = new ScaleTransition(new Duration(450), speaker);
            beat.setToX(1.15);
            beat.setToY(1.15);
            beat.setAutoReverse(true);
            beat.setCycleCount(Transition.INDEFINITE);
            decorationAnimations.add(beat);
            questionDecorations.add(speaker);
        }
        gameContext.getChildren().addAll(questionDecorations);
        decorationAnimations.forEach(Transition::play);

        // In the band: centered between the configuration button and the button to hear the question again
        final double bandFont = sizing.bandHeight() * 0.34;
        final double scale = Math.min(bandFont / Math.min(largeFont, largeFont * width * 0.9 / Math.max(1, pillWidth)),
            (sizing.bandRight() - sizing.bandLeft() - 2 * sizing.gap()) / pillWidth);
        final Duration moveLength = Duration.millis(Math.min(questionLength / 2.0, QUESTION_MOVE_MAX_LENGTH));

        final TranslateTransition move = new TranslateTransition(moveLength, questionPill);
        move.setToX(sizing.bandCenterX() - width / 2);
        move.setToY(sizing.bandTop() + sizing.bandHeight() / 2 - pillCenterY);
        final ScaleTransition shrink = new ScaleTransition(moveLength, questionPill);
        shrink.setToX(scale);
        shrink.setToY(scale);
        final ParallelTransition fullAnimation = new ParallelTransition(move, shrink);
        fullAnimation.setInterpolator(Interpolator.EASE_BOTH);
        fullAnimation.setDelay(Duration.millis(questionLength));

        fullAnimation.setOnFinished(actionEvent -> {
            decorationAnimations.forEach(Transition::stop);
            gameContext.getChildren().removeAll(questionDecorations);

            if (currentRoundDetails != null) {
                gameContext.getChildren().addAll(currentRoundDetails.getPictureCardList());

                for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
                    p.toFront();
                    p.setOpacity(0);
                    final FadeTransition appear = new FadeTransition(Duration.millis(300), p);
                    appear.setToValue(1);
                    appear.play();
                }

                if (currentRoundDetails.getQuestionSoundPath() != null) {
                    final double buttonSize = sizing.replayButtonSize();
                    replayButton = new ReplayQuestionButton(gameContext, width - sizing.gap() - buttonSize / 2,
                        sizing.bandTop() + sizing.bandHeight() / 2, buttonSize, this::playQuestionSound);
                    gameContext.getChildren().add(replayButton);
                }
            }
            questionPill.toFront();

            stats.notifyNewRoundReady();

            gameContext.onGameStarted(2000);
        });

        return fullAnimation;
    }

    /**
     * Plays the sound of the question, stopping the previous sound if it is still playing.
     */
    void playQuestionSound() {
        if (currentRoundDetails == null || currentRoundDetails.getQuestionSoundPath() == null) {
            return;
        }
        ForegroundSoundsUtils.stopSound();
        gameContext.getSoundManager().add(currentRoundDetails.getQuestionSoundPath());
    }

    /**
     * this method should be called when exiting the game, or before starting a new round, in order to clean up all
     * resources in both UI and memory
     */
    @Override
    public void dispose() {
        if (replayButton != null) {
            replayButton.dispose();
            gameContext.getChildren().remove(replayButton);
            replayButton = null;
        }
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
     * @param folders  resource folders of pictures (e.g. "data/whereIsTheLetter/images/A")
     * @param variant  names of the folders of a variant (e.g. "A", "E"); empty for all the folders
     * @return the folders whose name is one of the variant. The whole name is compared: a part of the path used to be
     *   enough, so that the vowels showed every letter ("I" is in "whereIsTheLetter") and the farm the pigeon ("pig")
     */
    static Set<String> filterFolders(final Set<String> folders, final Set<String> variant) {
        if (variant.isEmpty()) {
            return folders;
        }
        return folders.stream()
            .filter(folder -> variant.contains(new File(folder).getName()))
            .collect(Collectors.toSet());
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
            resourcesFolders.addAll(ResourceFileManager.getResourceFolders(resourcesDirectory + "/images/lvl" + level + "/"));
            winnerFolders.addAll(ResourceFileManager.getResourceFolders(winnerImagesDirectory));
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

            resourcesFolders.addAll(filterFolders(ResourceFileManager.getResourceFolders(imagesDirectory), variantSet));
            directoriesCount = resourcesFolders.size();
        }

        final String language = config.getLanguage();

        if (directoriesCount == 0) {
            log.warn("No images found in Directory " + directoryName);
            error(language);
            return null;
        }

        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        // The picture of each place of the grid. The draws are made in the order of the former versions (a folder,
        // then a picture of it, and the voice of the question at the place of the right answer), so that a saved game
        // is replayed with the same pictures, except where this version changes the rules (variants, repeated
        // pictures, right answer of the previous round)
        final List<String> imagePaths = new ArrayList<>();
        final List<String> folderNames = new ArrayList<>();
        String voice = null;
        String questionSoundPath;
        String question;
        List<Image> pictograms = null;

        if (this.gameType == FIND_ODD || this.gameType == ANIMALS_DYNAMIC) {
            int index = random.nextInt(resourcesFolders.size());
            String folder = resourcesFolders.remove((index) % directoriesCount);

            String winnerFolder;
            if (this.gameType == FIND_ODD) {
                index = random.nextInt(resourcesFolders.size());
                winnerFolder = resourcesFolders.remove((index) % directoriesCount);
                if (new File(winnerFolder).getName().equals(previousWinnerFolderName)) {
                    // Not the same odd one out twice in a row: the two sets of pictures swap
                    final String swapped = folder;
                    folder = winnerFolder;
                    winnerFolder = swapped;
                }
            } else {
                winnerFolder = winnerFolders.remove(0);
            }
            final String folderName = (new File(winnerFolder)).getName();
            log.info("WinnerFolderName = {}", folderName);
            previousWinnerFolderName = folderName;

            // The other pictures are all different, as long as the folder has enough of them
            final List<String> otherFiles = new ArrayList<>(ResourceFileManager.getResourcePaths(folder));
            final List<String> remainingOtherFiles = new ArrayList<>(otherFiles);
            for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {
                if (i == winnerImageIndexAmongDisplayedImages) {
                    final Set<String> files = ResourceFileManager.getResourcePaths(winnerFolder);
                    imagePaths.add((String) files.toArray()[random.nextInt(files.size())]);
                    if (this.gameType == ANIMALS_DYNAMIC) {
                        voice = drawVoice(language);
                    }
                } else {
                    if (remainingOtherFiles.isEmpty()) {
                        remainingOtherFiles.addAll(otherFiles);
                    }
                    imagePaths.add(remainingOtherFiles.remove(random.nextInt(remainingOtherFiles.size())));
                }
            }

            if (this.gameType == FIND_ODD) {
                question = MultilinguismFactory.getSingleton().getTranslation("FindTheOddOneOut", language);
                questionSoundPath = getPathSound("odd", language, drawVoice(language));
            } else {
                questionSoundPath = getPathSound(folderName, language, voice);
                question = getQuestionText(folderName, language);
            }
        } else {
            for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {
                if (this.gameType == CUSTOMIZED) {
                    int index = random.nextInt(imagesFolders.size());
                    final File folder = imagesFolders.remove((index) % directoriesCount);
                    final List<File> validImageFiles = new ArrayList<>();
                    for (File file : getFiles(folder)) {
                        if (WhereIsItVaildator.fileIsImageFile(file)) {
                            validImageFiles.add(file);
                        }
                    }
                    // The image file needs 'file:' prepended as this will get images from a local source, not resources.
                    imagePaths.add("file:" + validImageFiles.get(random.nextInt(validImageFiles.size())));
                    folderNames.add(folder.getName());
                } else {
                    int index = random.nextInt(resourcesFolders.size());
                    final String folder = resourcesFolders.remove((index) % directoriesCount);
                    final Set<String> files = ResourceFileManager.getResourcePaths(folder);
                    imagePaths.add((String) files.toArray()[random.nextInt(files.size())]);
                    folderNames.add(new File(folder).getName());
                }
                if (i == winnerImageIndexAmongDisplayedImages) {
                    voice = drawVoice(language);
                }
            }
            avoidSameWinnerAsPreviousRound(folderNames, imagePaths, winnerImageIndexAmongDisplayedImages);

            final String folderName = folderNames.get(winnerImageIndexAmongDisplayedImages);
            previousWinnerFolderName = folderName;
            questionSoundPath = getPathSound(folderName, language, voice);
            question = getQuestionText(folderName, language);
            pictograms = getPictograms(folderName);
        }

        final List<PictureCard> pictureCardList = new ArrayList<>();
        for (int i = 0; i < imagePaths.size(); i++) {
            final double x = gameSizing.cellX(i % nbColumns);
            final double y = gameSizing.cellY(i / nbColumns);
            pictureCardList.add(new PictureCard(x, y, gameSizing.cellWidth(), gameSizing.cellHeight(), gameContext,
                winnerImageIndexAmongDisplayedImages == i, imagePaths.get(i), stats, this));

            final TargetAOI targetAOI = new TargetAOI(x + gameSizing.cellWidth() / 2, y + gameSizing.cellHeight() / 2,
                (int) (Math.max(gameSizing.cellWidth(), gameSizing.cellHeight()) / 2), System.currentTimeMillis());
            targetAOIList.add(targetAOI);
        }
        return new RoundDetails(pictureCardList, winnerImageIndexAmongDisplayedImages, questionSoundPath, question, pictograms);
    }

    /**
     * Keeps the right answer of the previous round from being the right answer again: it swaps places with the next
     * picture. No other draw is made, so that a round without this case is drawn as before.
     *
     * @param folderNames the folder of the picture of each place
     * @param imagePaths  the picture of each place
     */
    void avoidSameWinnerAsPreviousRound(final List<String> folderNames, final List<String> imagePaths,
                                        final int winnerIndex) {
        if (folderNames.size() > 1 && folderNames.get(winnerIndex).equals(previousWinnerFolderName)) {
            final int other = (winnerIndex + 1) % folderNames.size();
            Collections.swap(folderNames, winnerIndex, other);
            Collections.swap(imagePaths, winnerIndex, other);
        }
    }

    /**
     * Return all files which don't start with a point
     */
    private File[] getFiles(final File folder) {
        return folder.listFiles(file -> !file.getName().startsWith("."));
    }

    /**
     * Shows an error message when the pictures cannot be found, on a rounded label in the middle of the game area.
     */
    private void error(final String language) {
        gameContext.clear();

        final Multilinguism multilinguism = MultilinguismFactory.getSingleton();
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double width = dimension == null ? 0 : dimension.getWidth();
        final double height = dimension == null ? 0 : dimension.getHeight();

        final String message = multilinguism.getTranslation("WII-error", language);
        final double fontSize = Math.max(16, height / 20);
        Group label = GameStyle.pill(message, fontSize);
        final Bounds bounds = label.getLayoutBounds();
        if (width > 0 && bounds.getWidth() > width * 0.9) {
            label = GameStyle.pill(message, fontSize * width * 0.9 / bounds.getWidth());
        }
        label.setLayoutX(width / 2 - label.getLayoutBounds().getWidth() / 2);
        label.setLayoutY(height / 2 - label.getLayoutBounds().getHeight() / 2);
        gameContext.getChildren().add(label);
    }

    /**
     * @return the voice saying the question ("m" or "w", drawn at random), or null in the custom game and in the
     *   languages without voices. It is drawn in the languages where the former versions drew it, so that a saved
     *   game is replayed with the same pictures.
     */
    String drawVoice(final String language) {
        if (this.gameType == CUSTOMIZED || !LANGUAGES_WITH_VOICES.contains(language)) {
            return null;
        }
        return randomGenerator.nextDouble() > 0.5 ? "m" : "w";
    }

    /**
     * @param voice the voice of the question ({@link #drawVoice(String)})
     * @return the sound of the question of a folder, in the language of the settings; null if there is none in this
     *   language
     */
    String getPathSound(final String folder, final String language, final String voice) {
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

        log.debug("language is " + language);

        if (gameType.toString().contains("SOUNDS")) {
            // The sound of the animal or the instrument is the same in every language
            return "data/" + this.gameType.getResourcesDirectoryName() + "/sounds/" + folder + ".mp3";
        }
        if (voice == null) {
            return null;
        }

        // The voices of the questions: a folder by language (a single one, "sound", for the odd one out)
        final String path = "data/" + this.gameType.getResourcesDirectoryName()
            + (this.gameType == FIND_ODD ? "/sound/" : "/sounds/") + language + "/" + folder + "." + voice + "."
            + language + ".mp3";
        return resourceExists(path) ? path : null;
    }

    /**
     * @return whether a resource (file or folder) is in the classpath
     */
    private static boolean resourceExists(final String path) {
        return WhereIsIt.class.getClassLoader().getResource(path) != null;
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
            final String token = config.getWhereIsItDir() + "/pictos/" + st.nextToken().replace(' ', ' ').trim();
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
