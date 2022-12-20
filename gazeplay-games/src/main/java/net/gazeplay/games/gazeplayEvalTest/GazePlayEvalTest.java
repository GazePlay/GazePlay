package net.gazeplay.games.gazeplayEvalTest;

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
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.ResourceFileManager;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class GazePlayEvalTest implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private static final int NBMAXPICTO = 10;
    private static final double MAXSIZEPICTO = 250;
    private final String directoryRessource = "data/gazeplayEvalTest";
    private Text questionText;
    private final ArrayList<TargetAOI> targetAOIList;

    public CustomInputEventHandlerKeyboard customInputEventHandlerKeyboard = new CustomInputEventHandlerKeyboard();
    private boolean canRemoveItemManually = true;
    public boolean reEntered = false;
    public boolean goNext = false;

    public ImageView whiteCrossPicture;
    private final Timeline timelineTransition = waitForTransition();
    private final Timeline timelineQuestion = waitForQuestion();
    private final Timeline timelineInput = waitForInput();
    private Long currentRoundStartTime;
    private RoundDetails currentRoundDetails;

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

    //Word comprehension
    private int totalWordComprehension = 0;
    private int totalItemsAddedManually = 0;

    private int total = 0;

    private final int nbLines = 1;
    private final int nbColumns = 2;
    public int indexFileImage = 0;
    public int indexEndGame = 20;
    public int nbCountError = 0;
    public int nbCountErrorSave = 0;

    private static final String BIP_SOUND = "data/common/sounds/bip.wav";
    private static final String SEE_TWO_IMAGES_SOUND = "data/common/sounds/seeTwoImages.wav";
    private String IMAGE_SOUND = "";

    public GazePlayEvalTest(final IGameContext gameContext, final GazeplayEvalTestGameStats stats, final Translator translator) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.targetAOIList = new ArrayList<>();
        this.setFirstSound();
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, customInputEventHandlerKeyboard);
    }

    public void setFirstSound(){
            this.IMAGE_SOUND = "data/gazeplayEvalTest/sounds/01-Velo.wav";
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
            for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
                p.setVisibleProgressIndicator();
                p.resetMovedCursorOrGaze();
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
            p.hideProgressIndicator();
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

        String directory = resourcesDirectory + "/images/";
        this.indexEndGame = 2;

        directoryName = directory;

        // Here we filter out any unwanted resource folders, based on the difficulty JSON file
        Set<String> difficultySet;

        try {
            SourceSet sourceSet = new SourceSet(resourcesDirectory + "/variants.json");
            difficultySet = (sourceSet.getResources(Difficulty.NORMAL.toString()));
        } catch (FileNotFoundException fe) {
            log.info("No difficulty file found; Reading from all directories");
            difficultySet = Collections.emptySet();
        }

        Set<String> tempResourcesFolders = ResourceFileManager.getResourceFolders(directory);

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

        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns, false)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        final List<PictureCard> pictureCardList = new ArrayList<>();
        String questionSoundPath = null;
        String question = null;
        List<Image> pictograms = null;

        final String folder = resourcesFolders.get(this.indexFileImage);

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
        error.setX(root.getWidth() / 2. -100);
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
        if (correctAnswer && !customInputEventHandlerKeyboard.ignoreAnyInput) {
            switch (index) {

                case 0:
                    this.totalPhonology += 1;
                    this.complexScoreItemsPhonology += 1;
                    this.scoreRightTargetItemsPhonology += 1;
                    break;

                case 1:
                    this.totalPhonology += 1;
                    this.complexScoreItemsPhonology += 1;
                    this.scoreLeftTargetItemsPhonology += 1;
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
        this.IMAGE_SOUND = "data/gazeplayEvalTest/sounds/02-Ours.wav";
    }

    private void removeItemAddedManually() {
        if (this.totalItemsAddedManually != 0 && this.canRemoveItemManually) {
            this.nbCountError = this.nbCountErrorSave + 1;
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
