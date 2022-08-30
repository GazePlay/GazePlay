package net.gazeplay.games.beraV2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.difficulty.Difficulty;
import net.gazeplay.commons.gamevariants.difficulty.SourceSet;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.games.ResourceFileManager;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BeraV2 implements GameLifeCycle {

    private static final int NBMAXPICTO = 10;
    private static final double MAXSIZEPICTO = 250;
    private final String directoryRessource = "data/beraV2";
    private final int nbLines = 1;
    private final int nbColumns = 2;
    private final boolean fourThree;
    private final IGameContext gameContext;
    private final Stats stats;
    private final BeraV2GameVariant gameVariant;
    private final ReplayablePseudoRandom randomGenerator;
    private final ArrayList<TargetAOI> targetAOIList;
    private RoundDetails currentRoundDetails;
    private Text questionText;

    //Phonology
    private int totalPhonology = 0;
    private int simpleScoreItemsPhonology = 0;
    private int complexScoreItemsPhonology = 0;
    private int scoreLeftTargetItemsPhonology = 0;
    private int scoreRightTargetItemsPhonology = 0;

    //Semantics
    private int totalSemantic = 0;
    private int simpleScoreItemsSemantic = 0;
    private int complexScoreItemsSemantic = 0;
    private int frequentScoreItemSemantic = 0;
    private int infrequentScoreItemSemantic = 0;
    private int scoreLeftTargetItemsSemantic = 0;
    private int scoreRightTargetItemsSemantic = 0;

    //Morphosyntax
    private int totalMorphosyntax = 0;
    private int simpleScoreItemsMorphosyntax = 0;
    private int complexScoreItemsMorphosyntax = 0;
    private int scoreLeftTargetItemsMorphosyntax = 0;
    private int scoreRightTargetItemsMorphosyntax = 0;

    //Word comprehension
    private int totalWordComprehension = 0;
    private int totalItemsAddedManually = 0;

    private int total = 0;

    public int indexFileImage = 0;
    public int indexEndGame = 20;
    public int nbCountError = 0;
    public int nbCountErrorSave = 0;

    public CustomInputEventHandlerKeyboard customInputEventHandlerKeyboard = new CustomInputEventHandlerKeyboard();
    private boolean canRemoveItemManually = true;
    public boolean reEntered = false;
    public boolean goNext = false;

    public ImageView whiteCrossPicture;
    private final Timeline timelineTransition = waitForTransition();
    private final Timeline timelineQuestion = waitForQuestion();
    private final Timeline timelineInput = waitForInput();
    private Long currentRoundStartTime;

    private static final String BIP_SOUND = "data/common/sounds/bip.wav";
    private static final String SEE_TWO_IMAGES_SOUND = "data/common/sounds/seeTwoImages.wav";
    private String IMAGE_SOUND = "";

    public BeraV2(final boolean fourThree, final IGameContext gameContext, final Stats stats, final BeraV2GameVariant gameVariant) {
        this.gameContext = gameContext;
        this.fourThree = fourThree;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        this.setFirstSound();
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, customInputEventHandlerKeyboard);
    }

    public BeraV2(final boolean fourThree, final IGameContext gameContext, final Stats stats, final BeraV2GameVariant gameVariant, double gameSeed) {
        this.gameContext = gameContext;
        this.fourThree = fourThree;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        this.setFirstSound();
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, customInputEventHandlerKeyboard);
    }

    public void setFirstSound(){
        if (gameVariant == BeraV2GameVariant.WORD_COMPREHENSION_V2){
            this.IMAGE_SOUND = "data/beraV2/sounds/wordComprehension/01-Igloo.wav";
        }else {
            this.IMAGE_SOUND = "data/beraV2/sounds/sentenceComprehension/01-HommeSoignerParFemme.wav";
        }
    }

    public void playSound(String soundPath){
        Configuration config = ActiveConfigurationContext.getInstance();
        if (config.isSoundEnabled()){
            gameContext.getSoundManager().add(soundPath);
        }
    }

    @Override
    public void launch() {

        this.startTimer();

        this.canRemoveItemManually = true;

        gameContext.setLimiterAvailable();

        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;
        log.debug("numberOfImagesToDisplayPerRound = {}", numberOfImagesToDisplayPerRound);

        final int winnerImageIndexAmongDisplayedImages = 0;
        log.debug("winnerImageIndexAmongDisplayedImages = {}", winnerImageIndexAmongDisplayedImages);

        currentRoundDetails = pickAndBuildRandomPictures(numberOfImagesToDisplayPerRound, winnerImageIndexAmongDisplayedImages);

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();

        this.startGame();
    }

    public void startTimer(){
        if (this.indexFileImage == 0){
            currentRoundStartTime = System.currentTimeMillis();
        }
    }

    public void startGame() {
        final List<Rectangle> pictogramesList = new ArrayList<>(20); // storage of actual Pictogramm nodes in order to delete

        final List<Image> listOfPictos = currentRoundDetails.getPictos();

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

            final Dimension2D gamePaneDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            final double positionY = gamePaneDimension2D.getHeight() / 2 - questionText.getBoundsInParent().getHeight() / 2;

            for (final Image picto : listOfPictos) {

                final Rectangle pictoRectangle = new Rectangle(pictoSize, pictoSize);
                pictoRectangle.setFill(new ImagePattern(picto));
                pictoRectangle.setY(positionY + 100);
                pictoRectangle.setX(shift + (i++ * pictoSize * 1.1));
                pictogramesList.add(pictoRectangle);
            }

            gameContext.getChildren().addAll(pictogramesList);
        }

        gameContext.getChildren().removeAll(pictogramesList);

        gameContext.getChildren().addAll(currentRoundDetails.getPictureCardList());

        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            //  log.debug("p = {}", p);
            p.toFront();
            p.setOpacity(1);
        }

        stats.notifyNewRoundReady();

        gameContext.onGameStarted(2000);

        customInputEventHandlerKeyboard.ignoreAnyInput = false;

        this.playSound(SEE_TWO_IMAGES_SOUND);
    }

    public void checkAllPictureCardChecked() {
        boolean check = true;
        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            if (!p.isAlreadySee()) {
                check = false;
                break;
            }
        }
        if (check) {
            this.timelineTransition.playFromStart();
        }
    }

    public Timeline waitForInput(){
        Configuration config = ActiveConfigurationContext.getInstance();

        log.info("INPUT TIME : {}", config.getDelayBeforeSelectionTime());

        Timeline transition = new Timeline();
        transition.getKeyFrames().add(new KeyFrame(new Duration(config.getDelayBeforeSelectionTime())));
        transition.setOnFinished(event -> {
            for (final net.gazeplay.games.beraV2.PictureCard p : currentRoundDetails.getPictureCardList()) {
                p.setVisibleProgressIndicator();
            }
        });
        return transition;
    }

    public Timeline waitForTransition(){

        Configuration config = ActiveConfigurationContext.getInstance();

        log.info("TRANSITION TIME : {}", config.getTransitionTime());

        Timeline transition = new Timeline();
        transition.getKeyFrames().add(new KeyFrame(new Duration(config.getTransitionTime())));
        transition.setOnFinished(event -> {
            returnOnPictureCards();
        });
        return transition;
    }

    public void returnOnPictureCards(){

        Configuration config = ActiveConfigurationContext.getInstance();

        log.info("QUESTION TIME ENABLED : {}", config.isQuestionTimeEnabled());

        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            p.setVisibleProgressIndicator();
            p.setVisibleImagePicture(false);
            p.setNotifImageRectangle(false);
            this.reEntered = true;
        }
        this.createWhiteCross();

        if (config.isQuestionTimeEnabled()){
            this.timelineQuestion.playFromStart();
            this.playSound(IMAGE_SOUND);
        }else {
            this.playSound(IMAGE_SOUND);
        }
    }

    public Timeline waitForQuestion(){

        Configuration config = ActiveConfigurationContext.getInstance();

        log.info("QUESTION TIME : {}", config.getQuestionTime());

        Timeline question = new Timeline();
        question.getKeyFrames().add(new KeyFrame(new Duration(config.getQuestionTime())));
        question.setOnFinished(event -> {
            this.choicePicturePair();
        });
        return question;
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

    public void nextRound(){

        Configuration config = ActiveConfigurationContext.getInstance();

        Timeline transition = new Timeline();
        transition.getKeyFrames().add(new KeyFrame(new Duration(config.getTransitionTime())));
        transition.setOnFinished(event -> {
            this.launch();
        });
    }

    RoundDetails pickAndBuildRandomPictures(final int numberOfImagesToDisplayPerRound, final int winnerImageIndexAmongDisplayedImages) {

        final Configuration config = gameContext.getConfiguration();
        Configuration configActive = ActiveConfigurationContext.getInstance();

        int directoriesCount;
        final String directoryName;
        List<String> resourcesFolders = new LinkedList<>();

        final String resourcesDirectory = this.directoryRessource;
        String imagesDirectory = "";

        if (gameVariant == BeraV2GameVariant.WORD_COMPREHENSION_V2){
            imagesDirectory = resourcesDirectory + "/wordComprehension/";
            this.indexEndGame = 20;
        }else {
            imagesDirectory = resourcesDirectory + "/sentenceComprehension/";
            this.indexEndGame = 10;
        }

        directoryName = imagesDirectory;

        // Here we filter out any unwanted resource folders, based on the difficulty JSON file
        Set<String> difficultySet;

        try {
            SourceSet sourceSet = new SourceSet(resourcesDirectory + "/variants.json");
            difficultySet = (sourceSet.getResources(Difficulty.NORMAL.toString()));
        } catch (FileNotFoundException fe) {
            log.info("No difficulty file found; Reading from all directories");
            difficultySet = Collections.emptySet();
        }

        Set<String> tempResourcesFolders = ResourceFileManager.getResourceFolders(imagesDirectory);

        // If nothing can be found we take the entire folder contents.
        if (!difficultySet.isEmpty()) {
            Set<String> finalDifficultySet = difficultySet;
            tempResourcesFolders = tempResourcesFolders
                .parallelStream()
                .filter(s -> finalDifficultySet.parallelStream().anyMatch(s::contains))
                .collect(Collectors.toSet());
        }

        resourcesFolders.addAll(tempResourcesFolders);
        Collections.sort(resourcesFolders);

        directoriesCount = resourcesFolders.size();

        final String language = config.getLanguage();

        if (directoriesCount == 0) {
            log.warn("No images found in Directory " + directoryName);
            error(language);
            return null;
        }

        int posX = 0;
        int posY = 0;

        boolean winnerP1;
        boolean winnerP2;

        String imageP1 = "";
        String imageP2 = "";

        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        final List<PictureCard> pictureCardList = new ArrayList<>();
        String questionSoundPath = null;
        String question = null;
        List<Image> pictograms = null;

        final String folder = resourcesFolders.remove(this.indexFileImage);

        final Set<String> files = ResourceFileManager.getResourcePaths(folder);

        String randomImageFile1 = (String) files.toArray()[0];
        String randomImageFile2 = (String) files.toArray()[1];

        if (randomImageFile1.contains("First")) {
            imageP1 = randomImageFile1;
            imageP2 = randomImageFile2;
        } else {
            imageP1 = randomImageFile2;
            imageP2 = randomImageFile1;
        }

        if (imageP1.contains("Correct")) {
            winnerP1 = true;
            winnerP2 = false;
        } else {
            winnerP1 = false;
            winnerP2 = true;
        }

        double gap = 0;
        double widthImg = 0;
        double heightImg = 0;
        double posYImage = gameSizing.height * posY;

        if (configActive.isColumnarImagesEnabled()){
            gap = gameSizing.shift + 750;
            widthImg = (gameSizing.width - 50) / 2.0;
            heightImg = gameSizing.height / 2.0;
            posYImage = gameSizing.height * posY + 50;
        }else {
            gap = gameSizing.shift + 25;
            widthImg = gameSizing.width - 50;
            heightImg = gameSizing.height;
        }

        final PictureCard pictureCard1 = new PictureCard(
            gameSizing.width * posX + gap,
            posYImage, widthImg, heightImg, gameContext,
            winnerP1, imageP1 + "", stats, this);

        pictureCardList.add(pictureCard1);

        final TargetAOI targetAOI1 = new TargetAOI(
            gameSizing.width * (posX + 0.25),
            gameSizing.height * (posY + 1),
            (int) gameSizing.height,
            System.currentTimeMillis());

        targetAOIList.add(targetAOI1);

        if (configActive.isColumnarImagesEnabled()){
            posYImage = gameSizing.height / 1.5 - 100;
        }else {
            posX++;
        }

        final PictureCard pictureCard2 = new PictureCard(
            gameSizing.width * posX + gap,
            posYImage, widthImg, heightImg, gameContext,
            winnerP2, imageP2 + "", stats, this);

        pictureCardList.add(pictureCard2);

        final TargetAOI targetAOI2 = new TargetAOI(
            gameSizing.width * (posX + 0.25),
            gameSizing.height * (posY + 1),
            (int) gameSizing.height,
            System.currentTimeMillis());

        targetAOIList.add(targetAOI2);

        return new RoundDetails(pictureCardList, winnerImageIndexAmongDisplayedImages, questionSoundPath, question,
            pictograms);
    }

    private void error(final String language) {

        gameContext.clear();
        // HomeUtils.home(scene, group, choiceBox, null);

        final Multilinguism multilinguism = MultilinguismFactory.getSingleton();

        final Text error = new Text(multilinguism.getTranslation("WII-error", language));
        final Region root = gameContext.getRoot();
        error.setX(root.getWidth() / 2. - 100);
        error.setY(root.getHeight() / 2.);
        error.setId("item");
        gameContext.getChildren().addAll(error);
    }

    public void createWhiteCross(){

        final Image whiteSquare = new Image("data/common/images/whiteCross.png");
        this.whiteCrossPicture = new ImageView(whiteSquare);

        final Region root = gameContext.getRoot();

        this.whiteCrossPicture.setX((root.getWidth() / 2) - (whiteSquare.getWidth() / 2));
        this.whiteCrossPicture.setY((root.getHeight() / 2) - (whiteSquare.getHeight() / 2));
        this.whiteCrossPicture.setId("item");
        this.whiteCrossPicture.setOpacity(1);
        this.whiteCrossPicture.setVisible(true);

        gameContext.getChildren().addAll(this.whiteCrossPicture);

        this.goNext = true;
    }

    public void increaseIndexFileImage(boolean correctAnswer) {
        this.calculateStats(this.indexFileImage, correctAnswer);
        this.indexFileImage = this.indexFileImage + 1;
        this.nextSound(this.indexFileImage);
    }

    public void choicePicturePair(){
        this.whiteCrossPicture.setVisible(false);
        this.playSound(BIP_SOUND);
        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            p.hideProgressIndicator();
            p.setVisibleImagePicture(true);
            timelineInput.playFromStart();
        }
    }

    private void calculateStats(int index, boolean correctAnswer) {
        if (correctAnswer && !customInputEventHandlerKeyboard.ignoreAnyInput && gameVariant == BeraV2GameVariant.WORD_COMPREHENSION_V2) {
            switch (index) {

                case 0:
                    this.totalSemantic += 1;
                    this.simpleScoreItemsSemantic += 1;
                    this.infrequentScoreItemSemantic += 1;
                    this.scoreLeftTargetItemsSemantic += 1;
                    break;

                case 1:
                    this.totalPhonology += 1;
                    this.simpleScoreItemsPhonology += 1;
                    this.scoreLeftTargetItemsPhonology += 1;
                    break;

                case 2:
                    this.totalPhonology += 1;
                    this.complexScoreItemsPhonology += 1;
                    this.scoreRightTargetItemsPhonology += 1;
                    break;

                case 3:
                    this.totalPhonology += 1;
                    this.simpleScoreItemsPhonology += 1;
                    this.scoreLeftTargetItemsPhonology += 1;
                    break;

                case 4:
                    this.totalSemantic += 1;
                    this.complexScoreItemsSemantic += 1;
                    this.infrequentScoreItemSemantic += 1;
                    this.scoreRightTargetItemsSemantic += 1;
                    break;

                case 5:
                    this.totalPhonology += 1;
                    this.simpleScoreItemsPhonology += 1;
                    this.scoreRightTargetItemsPhonology += 1;
                    break;

                case 6:
                    this.totalSemantic += 1;
                    this.complexScoreItemsSemantic += 1;
                    this.infrequentScoreItemSemantic += 1;
                    this.scoreRightTargetItemsSemantic += 1;
                    break;

                case 7:
                    this.totalSemantic += 1;
                    this.simpleScoreItemsSemantic += 1;
                    this.frequentScoreItemSemantic += 1;
                    this.scoreRightTargetItemsSemantic += 1;
                    break;

                case 8:
                    this.totalSemantic += 1;
                    this.complexScoreItemsSemantic += 1;
                    this.infrequentScoreItemSemantic += 1;
                    this.scoreRightTargetItemsSemantic += 1;
                    break;

                case 9:
                    this.totalPhonology += 1;
                    this.complexScoreItemsPhonology += 1;
                    this.scoreLeftTargetItemsPhonology += 1;
                    break;

                case 10:
                    this.totalSemantic += 1;
                    this.simpleScoreItemsSemantic += 1;
                    this.frequentScoreItemSemantic += 1;
                    this.scoreLeftTargetItemsSemantic += 1;
                    break;

                case 11:
                    this.totalSemantic += 1;
                    this.complexScoreItemsSemantic += 1;
                    this.frequentScoreItemSemantic += 1;
                    this.scoreRightTargetItemsSemantic += 1;
                    break;

                case 12:
                    this.totalPhonology += 1;
                    this.simpleScoreItemsPhonology += 1;
                    this.scoreLeftTargetItemsPhonology += 1;
                    break;

                case 13:
                    this.totalPhonology += 1;
                    this.simpleScoreItemsPhonology += 1;
                    this.scoreRightTargetItemsPhonology += 1;
                    break;

                case 14:
                    this.totalPhonology += 1;
                    this.complexScoreItemsPhonology += 1;
                    this.scoreRightTargetItemsPhonology += 1;
                    break;

                case 15:
                    this.totalSemantic += 1;
                    this.simpleScoreItemsSemantic += 1;
                    this.infrequentScoreItemSemantic += 1;
                    this.scoreLeftTargetItemsSemantic += 1;
                    break;

                case 16:
                    this.totalPhonology += 1;
                    this.complexScoreItemsPhonology += 1;
                    this.scoreRightTargetItemsPhonology += 1;
                    break;

                case 17:
                    this.totalSemantic += 1;
                    this.complexScoreItemsSemantic += 1;
                    this.frequentScoreItemSemantic += 1;
                    this.scoreLeftTargetItemsSemantic += 1;
                    break;

                case 18:
                    this.totalPhonology += 1;
                    this.complexScoreItemsPhonology += 1;
                    this.scoreLeftTargetItemsPhonology += 1;
                    break;

                case 19:
                    this.totalSemantic += 1;
                    this.simpleScoreItemsSemantic += 1;
                    this.frequentScoreItemSemantic += 1;
                    this.scoreLeftTargetItemsSemantic += 1;
                    break;

                default:
                    break;
            }
        }else if (correctAnswer && !customInputEventHandlerKeyboard.ignoreAnyInput && gameVariant == BeraV2GameVariant.SENTENCE_COMPREHENSION_V2){
            switch (index){
                case 0:
                    this.totalMorphosyntax += 1;
                    this.complexScoreItemsMorphosyntax += 1;
                    this.scoreLeftTargetItemsMorphosyntax += 1;
                    break;

                case 1:
                    this.totalMorphosyntax += 1;
                    this.simpleScoreItemsMorphosyntax += 1;
                    this.scoreRightTargetItemsMorphosyntax += 1;
                    break;

                case 2:
                    this.totalMorphosyntax += 1;
                    this.complexScoreItemsMorphosyntax += 1;
                    this.scoreLeftTargetItemsMorphosyntax += 1;
                    break;

                case 3:
                    this.totalMorphosyntax += 1;
                    this.complexScoreItemsMorphosyntax += 1;
                    this.scoreLeftTargetItemsMorphosyntax += 1;
                    break;

                case 4:
                    this.totalMorphosyntax += 1;
                    this.complexScoreItemsMorphosyntax += 1;
                    this.scoreRightTargetItemsMorphosyntax += 1;
                    break;

                case 5:
                    this.totalMorphosyntax += 1;
                    this.simpleScoreItemsMorphosyntax += 1;
                    this.scoreLeftTargetItemsMorphosyntax += 1;
                    break;

                case 6:
                    this.totalMorphosyntax += 1;
                    this.simpleScoreItemsMorphosyntax += 1;
                    this.scoreLeftTargetItemsMorphosyntax += 1;
                    break;

                case 7:
                    this.totalMorphosyntax += 1;
                    this.complexScoreItemsMorphosyntax += 1;
                    this.scoreRightTargetItemsMorphosyntax += 1;
                    break;

                case 8:
                    this.totalMorphosyntax += 1;
                    this.simpleScoreItemsMorphosyntax += 1;
                    this.scoreRightTargetItemsMorphosyntax += 1;
                    break;

                case 9:
                    this.totalMorphosyntax += 1;
                    this.simpleScoreItemsMorphosyntax += 1;
                    this.scoreRightTargetItemsMorphosyntax += 1;
                    break;

                default:
                    break;
            }
        }
    }

    private void next(boolean value) {
        if (value) {
            this.nbCountErrorSave = this.nbCountError;
            this.totalItemsAddedManually += 1;
            currentRoundDetails.getPictureCardList().get(0).onCorrectCardSelected();
        } else {
            currentRoundDetails.getPictureCardList().get(0).onWrongCardSelected();
        }
    }

    private void nextSound(int index){
        if (gameVariant == BeraV2GameVariant.WORD_COMPREHENSION_V2) {
            switch (index) {

                case 1:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/02-Main.wav";
                    break;

                case 2:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/03-Mie.wav";
                    break;

                case 3:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/04-Chou.wav";
                    break;

                case 4:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/05-Renne.wav";
                    break;

                case 5:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/06-Champ.wav";
                    break;

                case 6:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/07-Ananas.wav";
                    break;

                case 7:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/08-Chevre.wav";
                    break;

                case 8:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/09-Ceinture.wav";
                    break;

                case 9:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/10-Mat.wav";
                    break;

                case 10:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/11-Tulipe.wav";
                    break;

                case 11:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/12-Trompette.wav";
                    break;

                case 12:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/13-Pas.wav";
                    break;

                case 13:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/14-Riz.wav";
                    break;

                case 14:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/15-Fut.wav";
                    break;

                case 15:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/16-Scie.wav";
                    break;

                case 16:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/17-Vent.wav";
                    break;

                case 17:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/18-Bottes.wav";
                    break;

                case 18:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/19-Bond.wav";
                    break;

                case 19:
                    this.IMAGE_SOUND="data/beraV2/sounds/wordComprehension/20-Oreille.wav";
                    break;

                default:
                    break;
            }
        }else if (gameVariant == BeraV2GameVariant.SENTENCE_COMPREHENSION_V2){
            switch (index){

                case 1:
                    this.IMAGE_SOUND="data/beraV2/sounds/sentenceComprehension/02-EmilieCourt.wav";
                    break;

                case 2:
                    this.IMAGE_SOUND="data/beraV2/sounds/sentenceComprehension/03-ChatSurChaise.wav";
                    break;

                case 3:
                    this.IMAGE_SOUND="data/beraV2/sounds/sentenceComprehension/04-ElleLit.wav";
                    break;

                case 4:
                    this.IMAGE_SOUND="data/beraV2/sounds/sentenceComprehension/05-EnfantTireChien.wav";
                    break;

                case 5:
                    this.IMAGE_SOUND="data/beraV2/sounds/sentenceComprehension/06-GarconPorteManteau.wav";
                    break;

                case 6:
                    this.IMAGE_SOUND="data/beraV2/sounds/sentenceComprehension/07-EllesEcriventLettre.wav";
                    break;

                case 7:
                    this.IMAGE_SOUND="data/beraV2/sounds/sentenceComprehension/08-BebeRecuBeaucoupPeluche.wav";
                    break;

                case 8:
                    this.IMAGE_SOUND="data/beraV2/sounds/sentenceComprehension/09-EstContent.wav";
                    break;

                case 9:
                    this.IMAGE_SOUND="data/beraV2/sounds/sentenceComprehension/10-ElleNourrit.wav";
                    break;

                default:
                    break;
            }
        }
    }

    private void removeItemAddedManually() {
        if (this.totalItemsAddedManually != 0 && this.canRemoveItemManually) {
            this.nbCountErrorSave = this.nbCountError;
            this.totalItemsAddedManually -= 1;
            this.canRemoveItemManually = false;
        }
    }

    public void removeEventHandlerPictureCard(){
        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            p.removeEventHandler();
        }
    }

    public void resetFromReplay(){

        //Phonology
        this.totalPhonology = 0;
        this.simpleScoreItemsPhonology = 0;
        this.complexScoreItemsPhonology = 0;
        this.scoreLeftTargetItemsPhonology = 0;
        this.scoreRightTargetItemsPhonology = 0;

        //Semantics
        this.totalSemantic = 0;
        this.simpleScoreItemsSemantic = 0;
        this.complexScoreItemsSemantic = 0;
        this.frequentScoreItemSemantic = 0;
        this.infrequentScoreItemSemantic = 0;
        this.scoreLeftTargetItemsSemantic = 0;
        this.scoreRightTargetItemsSemantic = 0;

        //Morphosyntax
        this.totalMorphosyntax = 0;
        this.simpleScoreItemsMorphosyntax = 0;
        this.complexScoreItemsMorphosyntax = 0;
        this.scoreLeftTargetItemsMorphosyntax = 0;
        this.scoreRightTargetItemsMorphosyntax = 0;

        //Word comprehension
        this.totalWordComprehension = 0;
        this.totalItemsAddedManually = 0;

        this.total = 0;

        this.indexFileImage = 0;
        this.nbCountError = 0;
        this.nbCountErrorSave = 0;
    }

    public void finalStats() {

        stats.timeGame = System.currentTimeMillis() - this.currentRoundStartTime;

        if (gameVariant == BeraV2GameVariant.WORD_COMPREHENSION_V2){

            stats.variantType = "WordComprehension";

            //Phonology
            stats.totalPhonology = this.totalPhonology;
            stats.simpleScoreItemsPhonology = this.simpleScoreItemsPhonology;
            stats.complexScoreItemsPhonology = this.complexScoreItemsPhonology;
            stats.scoreLeftTargetItemsPhonology = this.scoreLeftTargetItemsPhonology;
            stats.scoreRightTargetItemsPhonology = this.scoreRightTargetItemsPhonology;

            //Semantic
            stats.totalSemantic = this.totalSemantic;
            stats.simpleScoreItemsSemantic = this.simpleScoreItemsSemantic;
            stats.complexScoreItemsSemantic = this.complexScoreItemsSemantic;
            stats.frequentScoreItemSemantic = this.frequentScoreItemSemantic;
            stats.infrequentScoreItemSemantic = this.infrequentScoreItemSemantic;
            stats.scoreLeftTargetItemsSemantic = this.scoreLeftTargetItemsSemantic;
            stats.scoreRightTargetItemsSemantic = this.scoreRightTargetItemsSemantic;

            //World Comprehension
            stats.totalWordComprehension = this.scoreLeftTargetItemsPhonology +
                this.scoreRightTargetItemsPhonology +
                this.scoreLeftTargetItemsSemantic +
                this.scoreRightTargetItemsSemantic;
            stats.totalItemsAddedManually = this.totalItemsAddedManually;
            stats.total = this.totalWordComprehension + this.totalItemsAddedManually;

            createFileWordComprehension();
            createExcelWordComprehension();

        }else if (gameVariant == BeraV2GameVariant.SENTENCE_COMPREHENSION_V2){

            stats.variantType = "SentenceComprehension";

            //Morphosyntax
            stats.totalMorphosyntax = this.totalMorphosyntax;
            stats.simpleScoreItemsMorphosyntax = this.simpleScoreItemsMorphosyntax;
            stats.complexScoreItemsMorphosyntax = this.complexScoreItemsMorphosyntax;
            stats.scoreLeftTargetItemsMorphosyntax = this.scoreLeftTargetItemsMorphosyntax;
            stats.scoreRightTargetItemsMorphosyntax = this.scoreRightTargetItemsMorphosyntax;

            //Sentence Comprehension
            stats.totalItemsAddedManually = this.totalItemsAddedManually;
            stats.total = this.totalMorphosyntax + this.totalItemsAddedManually;

            createFileSentenceComprehension();
            createExcelSentenceComprehension();
        }
    }

    public void createFileWordComprehension(){
        this.totalWordComprehension = this.scoreLeftTargetItemsPhonology +
            this.scoreRightTargetItemsPhonology +
            this.scoreLeftTargetItemsSemantic +
            this.scoreRightTargetItemsSemantic;
        this.total = this.totalWordComprehension + this.totalItemsAddedManually;

        File pathDirectory = stats.getGameStatsOfTheDayDirectory();
        String pathFile = "statsBeraV2ComprehensionMots-" + DateUtils.dateTimeNow() + ".csv";
        File statsFile = new File(pathDirectory, pathFile);

        Date now = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm:ss");

        try {
            PrintWriter out = new PrintWriter(statsFile, StandardCharsets.UTF_16);
            out.append("\r\n");
            out.append("Fait le ").append(formatDate.format(now)).append("\r\n");
            out.append("\r\n");
            out.append("Temps de jeu : ").append(String.valueOf(stats.timeGame / 100.)).append(" secondes \r\n");
            out.append("\r\n");
            out.append("PHONOLOGIE \r\n");
            out.append(" - Total Phonologie : ").append(String.valueOf(this.totalPhonology)).append("/10 \r\n");
            out.append(" - Score items simples : ").append(String.valueOf(this.simpleScoreItemsPhonology)).append("/5 \r\n");
            out.append(" - Score items complexes : ").append(String.valueOf(this.complexScoreItemsPhonology)).append("/5 \r\n");
            out.append(" - Score items cibles gauche : ").append(String.valueOf(this.scoreLeftTargetItemsPhonology)).append("/5 \r\n");
            out.append(" - Score items cibles droite : ").append(String.valueOf(this.scoreRightTargetItemsPhonology)).append("/5 \r\n");
            out.append("\r\n");
            out.append("SEMANTIQUE \r\n");
            out.append(" - Total Sémantique : ").append(String.valueOf(this.totalSemantic)).append("/10 \r\n");
            out.append(" - Score items simples : ").append(String.valueOf(this.simpleScoreItemsSemantic)).append("/5 \r\n");
            out.append(" - Score items complexes : ").append(String.valueOf(this.complexScoreItemsSemantic)).append("/5 \r\n");
            out.append(" - Score items fréquents (F+) : ").append(String.valueOf(this.frequentScoreItemSemantic)).append("/5 \r\n");
            out.append(" - Score items peu fréquents (F-) : ").append(String.valueOf(this.infrequentScoreItemSemantic)).append("/5 \r\n");
            out.append(" - Score items cibles gauche : ").append(String.valueOf(this.scoreLeftTargetItemsSemantic)).append("/5 \r\n");
            out.append(" - Score items cibles droite : ").append(String.valueOf(this.scoreRightTargetItemsSemantic)).append("/5 \r\n");
            out.append("\r\n");
            out.append("COMPREHENSION DE MOTS \r\n");
            out.append(" - Total compréhension de mots : ").append(String.valueOf(this.totalWordComprehension)).append("/20 \r\n");
            out.append(" - Total items ajoutés manuellement : ").append(String.valueOf(this.totalItemsAddedManually)).append("/20 \r\n");
            out.append(" - Total compréhension de mots avec items sélectionnés manuellement : ").append(String.valueOf(this.total)).append("/20 \r\n");
            out.close();
        } catch (Exception e) {
            log.info("Error creation csv for BeraV2 stats game !");
            e.printStackTrace();
        }
    }

    public void createFileSentenceComprehension(){
        this.total = this.totalMorphosyntax + this.totalItemsAddedManually;

        File pathDirectory = stats.getGameStatsOfTheDayDirectory();
        String pathFile = "statsBeraV2ComprehensionPhrases-" + DateUtils.dateTimeNow() + ".csv";
        File statsFile = new File(pathDirectory, pathFile);

        Date now = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm:ss");

        try {
            PrintWriter out = new PrintWriter(statsFile, StandardCharsets.UTF_16);
            out.append("\r\n");
            out.append("Fait le ").append(formatDate.format(now)).append("\r\n");
            out.append("\r\n");
            out.append("Temps de jeu : ").append(String.valueOf(stats.timeGame / 100.)).append(" secondes \r\n");
            out.append("\r\n");
            out.append("MORPHOSYNTAXE \r\n");
            out.append(" - Total morphosyntaxe : ").append(String.valueOf(this.totalMorphosyntax)).append("/10 \r\n");
            out.append(" - Score items simples : ").append(String.valueOf(this.simpleScoreItemsMorphosyntax)).append("/5 \r\n");
            out.append(" - Score items complexes : ").append(String.valueOf(this.complexScoreItemsMorphosyntax)).append("/5 \r\n");
            out.append(" - Score items cibles gauche : ").append(String.valueOf(this.scoreLeftTargetItemsMorphosyntax)).append("/5 \r\n");
            out.append(" - Score items cibles droite : ").append(String.valueOf(this.scoreRightTargetItemsMorphosyntax)).append("/5 \r\n");
            out.append(" - Total items ajoutés manuellement : ").append(String.valueOf(this.totalItemsAddedManually)).append("/10 \r\n");
            out.append(" - Total compréhension de phrases : ").append(String.valueOf(this.total)).append("/10 \r\n");
            out.close();
        } catch (Exception e) {
            log.info("Error creation csv for BeraV2 stats game !");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("PMD")
    public void createExcelWordComprehension(){

        File pathDirectory = stats.getGameStatsOfTheDayDirectory();
        String pathFile = pathDirectory + "\\statsBeraV2ComprehensionMots-" + DateUtils.dateTimeNow() + ".xlsx";
        this.stats.actualFile = pathFile;

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Statistiques compréhension de mots");

        Object[][] bookData = {
            {"Temps de jeu : ", String.valueOf(stats.timeGame / 100.), "secondes"},
            {""},
            {"PHONOLOGIE : "},
            {" - Total Phonologie : ", String.valueOf(this.totalPhonology), "/10"},
            {" - Score items simples : ", String.valueOf(this.simpleScoreItemsPhonology), "/5"},
            {" - Score items complexes : ", String.valueOf(this.complexScoreItemsPhonology), "/5"},
            {" - Score items cibles gauche : ", String.valueOf(this.scoreLeftTargetItemsPhonology), "/5"},
            {" - Score items cibles droite : ", String.valueOf(this.scoreRightTargetItemsPhonology), "/5"},
            {""},
            {"SÉMANTIQUE : "},
            {" - Total Sémantique : ", String.valueOf(this.totalSemantic), "/10"},
            {" - Score items simples : ", String.valueOf(this.simpleScoreItemsSemantic), "/5"},
            {" - Score items complexes : ", String.valueOf(this.complexScoreItemsSemantic), "/5"},
            {" - Score items fréquents (F+) : ", String.valueOf(this.frequentScoreItemSemantic), "/5"},
            {" - Score items peu fréquents (F-) : ", String.valueOf(this.infrequentScoreItemSemantic), "/5"},
            {" - Score items cibles gauche : ", String.valueOf(this.scoreLeftTargetItemsSemantic), "/5"},
            {" - Score items cibles droite : ", String.valueOf(this.scoreRightTargetItemsSemantic), "/5"},
            {""},
            {"COMPRÉHENSION DE MOTS : "},
            {" - Total compréhension de mots : ", String.valueOf(this.totalWordComprehension), "/20"},
            {" - Total items ajoutés manuellement : ", String.valueOf(this.totalItemsAddedManually), "/20"},
            {" - Total compréhension de mots avec items sélectionnés manuellement : ", String.valueOf(this.total), "/20"},
        };

        int rowCount = 0;

        for (Object[] aBook : bookData) {
            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;

            for (Object field : aBook) {
                Cell cell = row.createCell(columnCount++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(pathFile)) {
            workbook.write(outputStream);
        } catch (Exception e){
            log.info("Creation of xlsx file don't work", e);
        }
    }

    @SuppressWarnings("PMD")
    public void createExcelSentenceComprehension(){

        File pathDirectory = stats.getGameStatsOfTheDayDirectory();
        String pathFile = pathDirectory + "\\statsBeraV2ComprehensionPhrases-" + DateUtils.dateTimeNow() + ".xlsx";
        this.stats.actualFile = pathFile;

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Statistiques compréhension de phrases");

        Object[][] bookData = {
            {"Temps de jeu : ", String.valueOf(stats.timeGame / 100.), "secondes"},
            {""},
            {"MORPHOSYNTAXE : "},
            {" - Total Morphosyntaxe : ", String.valueOf(this.totalMorphosyntax), "/10"},
            {" - Score items simples : ", String.valueOf(this.simpleScoreItemsMorphosyntax), "/5"},
            {" - Score items complexes : ", String.valueOf(this.complexScoreItemsMorphosyntax), "/5"},
            {" - Score items cibles gauche : ", String.valueOf(this.scoreLeftTargetItemsMorphosyntax), "/5"},
            {" - Score items cibles droite : ", String.valueOf(this.scoreRightTargetItemsMorphosyntax), "/5"},
            {" - Total items ajoutés manuellement : ", String.valueOf(this.totalItemsAddedManually), "/10"},
            {" - Total compréhension de phrases : ", String.valueOf(this.total), "/10"},
        };

        int rowCount = 0;

        for (Object[] aBook : bookData) {
            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;

            for (Object field : aBook) {
                Cell cell = row.createCell(columnCount++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(pathFile)) {
            workbook.write(outputStream);
        } catch (Exception e){
            log.info("Creation of xlsx file don't work", e);
        }
    }

    @SuppressWarnings("PMD")
    public void createSaveFileBackup(){

        File pathDirectory = stats.getGameStatsOfTheDayDirectory();
        String pathFile = "backupResults.csv";
        File statsFile = new File(pathDirectory, pathFile);

        Date now = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm:ss");

        if (gameVariant == BeraV2GameVariant.WORD_COMPREHENSION_V2){
            this.totalWordComprehension = this.scoreLeftTargetItemsPhonology +
                this.scoreRightTargetItemsPhonology +
                this.scoreLeftTargetItemsSemantic +
                this.scoreRightTargetItemsSemantic;
            this.total = this.totalWordComprehension + this.totalItemsAddedManually;

            try {
                PrintWriter out = new PrintWriter(statsFile, StandardCharsets.UTF_16);
                out.append("\r\n");
                out.append("Fait le ").append(formatDate.format(now)).append("\r\n");
                out.append("\r\n");
                out.append("PHONOLOGIE \r\n");
                out.append(" - Total Phonologie : ").append(String.valueOf(this.totalPhonology)).append("/10 \r\n");
                out.append(" - Score items simples : ").append(String.valueOf(this.simpleScoreItemsPhonology)).append("/5 \r\n");
                out.append(" - Score items complexes : ").append(String.valueOf(this.complexScoreItemsPhonology)).append("/5 \r\n");
                out.append(" - Score items cibles gauche : ").append(String.valueOf(this.scoreLeftTargetItemsPhonology)).append("/5 \r\n");
                out.append(" - Score items cibles droite : ").append(String.valueOf(this.scoreRightTargetItemsPhonology)).append("/5 \r\n");
                out.append("\r\n");
                out.append("SEMANTIQUE \r\n");
                out.append(" - Total Sémantique : ").append(String.valueOf(this.totalSemantic)).append("/10 \r\n");
                out.append(" - Score items simples : ").append(String.valueOf(this.simpleScoreItemsSemantic)).append("/5 \r\n");
                out.append(" - Score items complexes : ").append(String.valueOf(this.complexScoreItemsSemantic)).append("/5 \r\n");
                out.append(" - Score items fréquents (F+) : ").append(String.valueOf(this.frequentScoreItemSemantic)).append("/5 \r\n");
                out.append(" - Score items peu fréquents (F-) : ").append(String.valueOf(this.infrequentScoreItemSemantic)).append("/5 \r\n");
                out.append(" - Score items cibles gauche : ").append(String.valueOf(this.scoreLeftTargetItemsSemantic)).append("/5 \r\n");
                out.append(" - Score items cibles droite : ").append(String.valueOf(this.scoreRightTargetItemsSemantic)).append("/5 \r\n");
                out.append("\r\n");
                out.append("COMPREHENSION DE MOTS \r\n");
                out.append(" - Total compréhension de mots : ").append(String.valueOf(this.totalWordComprehension)).append("/20 \r\n");
                out.append(" - Total items ajoutés manuellement : ").append(String.valueOf(this.totalItemsAddedManually)).append("/20 \r\n");
                out.append(" - Total compréhension de mots avec items sélectionnés manuellement : ").append(String.valueOf(this.total)).append("/20 \r\n");
                out.close();
            } catch (Exception e) {
                log.info("Error creation csv for BeraV2 stats game !");
                e.printStackTrace();
            }
        }else {
            this.total = this.totalMorphosyntax + this.totalItemsAddedManually;

            try {
                PrintWriter out = new PrintWriter(statsFile, StandardCharsets.UTF_16);
                out.append("\r\n");
                out.append("Fait le ").append(formatDate.format(now)).append("\r\n");
                out.append("\r\n");
                out.append("MORPHOSYNTAXE \r\n");
                out.append(" - Total morphosyntaxe : ").append(String.valueOf(this.totalMorphosyntax)).append("/10 \r\n");
                out.append(" - Score items simples : ").append(String.valueOf(this.simpleScoreItemsMorphosyntax)).append("/5 \r\n");
                out.append(" - Score items complexes : ").append(String.valueOf(this.complexScoreItemsMorphosyntax)).append("/5 \r\n");
                out.append(" - Score items cibles gauche : ").append(String.valueOf(this.scoreLeftTargetItemsMorphosyntax)).append("/5 \r\n");
                out.append(" - Score items cibles droite : ").append(String.valueOf(this.scoreRightTargetItemsMorphosyntax)).append("/5 \r\n");
                out.append(" - Total items ajoutés manuellement : ").append(String.valueOf(this.totalItemsAddedManually)).append("/10 \r\n");
                out.append(" - Total compréhension de phrases : ").append(String.valueOf(this.total)).append("/10 \r\n");
                out.close();
            } catch (Exception e) {
                log.info("Error creation csv for BeraV2 stats game !");
                e.printStackTrace();
            }
        }
    }

    private class CustomInputEventHandlerKeyboard implements EventHandler<KeyEvent> {

        public boolean ignoreAnyInput = false;

        @Override
        public void handle(KeyEvent key) {

            if (key.getCode().isArrowKey() && goNext){
                timelineQuestion.stop();
                goNext = false;
                choicePicturePair();
            }

            if (ignoreAnyInput) {
                return;
            }

            if (key.getCode().getChar().equals("X")) {
                ignoreAnyInput = true;
                timelineTransition.stop();
                timelineQuestion.stop();
                next(true);
            } else if (key.getCode().getChar().equals("C")) {
                ignoreAnyInput = true;
                timelineTransition.stop();
                timelineQuestion.stop();
                next(false);
            } else if (key.getCode().getChar().equals("V")) {
                timelineTransition.stop();
                timelineQuestion.stop();
                removeItemAddedManually();
            }
        }
    }
}
