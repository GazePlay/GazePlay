package net.gazeplay.games.gazeplayEval;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class GazeplayEval implements GameLifeCycle {

    private static final int NBMAXPICTO = 10;
    private static final double MAXSIZEPICTO = 250;
    private final int nbImagesPerRound = 2;
    private final IGameContext gameContext;
    private final GazeplayEvalGameVariant gameVariant;
    private final boolean fourThree;
    private final Stats stats;
    private final ArrayList<TargetAOI> targetAOIList;
    private final ReplayablePseudoRandom randomGenerator;
    private static final String BIP_SOUND = "data/common/sounds/bip.wav";
    private static final String SEE_TWO_IMAGES_SOUND = "data/common/sounds/seeTwoImages.wav";
    private String gameName = "GazePlayEval";
    private Boolean isAnonymous = false;
    private String IMAGE_SOUND = "";
    private String[][] listImages;
    private String[][] listImagesDescription;
    private long[][] timeImages;
    private String[] listValues;
    private String[] listSounds;
    private String[] listSoundsDescription;
    private String[] listNameScores;
    private String[] listTagScores;
    private String[] listCalculScores;
    private String[] resultsChoiceImages;
    private String[] userProfil;
    private int[] scores;
    private int[] scoreValue;
    private int[] maxValue;
    private final int nbLines = 1;
    private final int nbColumns = 2;
    private int nbImages = 0;
    private int nbSounds = 0;
    public int indexFileImage = 0;
    public int indexEndGame = 0;
    private boolean canRemoveItemManually = true;
    private RoundDetails currentRoundDetails;
    private Long currentRoundStartTime;
    private final Timeline timelineTransition = waitForTransition();
    private final Timeline timelineQuestion = waitForQuestion();
    private final Timeline timelineInput = waitForInput();
    public ImageView whiteCrossPicture;
    private Text questionText;
    public CustomInputEventHandlerKeyboard customInputEventHandlerKeyboard = new CustomInputEventHandlerKeyboard();
    public boolean reEntered = false;
    public boolean goNext = false;
    public int nbCountError = 0;
    public int nbCountErrorSave = 0;
    private int totalItemsAddedManually = 0;
    private String outputFile = "";

    public GazeplayEval(final boolean fourThree, final IGameContext gameContext, final GazeplayEvalGameVariant gameVariant, final Stats stats) {
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.fourThree = fourThree;
        this.stats = stats;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        this.loadGame();
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, customInputEventHandlerKeyboard);
    }

    public GazeplayEval(final boolean fourThree, final IGameContext gameContext, final GazeplayEvalGameVariant gameVariant, final Stats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.fourThree = fourThree;
        this.stats = stats;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        this.loadGame();
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, customInputEventHandlerKeyboard);

    }

    public void loadGame(){
        Configuration config = ActiveConfigurationContext.getInstance();
        File gameDirectory = new File(config.getFileDir() + "\\evals\\" + this.gameVariant.getNameGame() + "\\config.json");
        JsonParser jsonParser = new JsonParser();
        try  (FileReader reader = new FileReader(gameDirectory, StandardCharsets.UTF_8)) {
            JsonObject obj = jsonParser.parse(reader).getAsJsonObject();
            this.gameName = obj.get("EvalName").getAsString();
            JsonArray scores = obj.get("Scores").getAsJsonArray();
            JsonArray assets = obj.get("Assets").getAsJsonArray();
            this.outputFile = obj.get("Output").getAsString();
            this.nbImages = assets.size();
            this.nbSounds = assets.size();
            this.generateUser(obj);
            this.generateTabFromJson(scores, assets);
            this.indexEndGame = this.nbImages;
            this.setSound();
            this.resultsChoiceImages = new String[this.indexEndGame];
            this.scores = new int[scores.size()];
            Arrays.fill(this.scores, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateUser(JsonObject obj){
        this.isAnonymous = Boolean.valueOf(obj.get("Anonymous").getAsString());
        if (!this.isAnonymous){
            JsonArray user = obj.get("Profil").getAsJsonArray();
            this.userProfil = new String[user.size()];
            for (int i=0; i<user.size(); i++){
                this.userProfil[i] = user.get(i).getAsString();
            }
        }
    }

    public void generateTabFromJson(JsonArray scores, JsonArray assets){

        this.listImages = new String[assets.size()][2];
        this.listImagesDescription = new String[assets.size()][2];
        this.timeImages = new long[assets.size()][2];
        this.listValues = new String[assets.size()];
        this.listSounds = new String[assets.size()];
        this.listSoundsDescription = new String[assets.size()];
        this.listCalculScores = new String[assets.size()];

        for (int i=0; i<assets.size(); i++){
            this.listImages[i] = new String[]{assets.get(i).getAsJsonArray().get(0).getAsString(), assets.get(i).getAsJsonArray().get(2).getAsString()};
            this.listImagesDescription[i] = new String[]{assets.get(i).getAsJsonArray().get(1).getAsString(), assets.get(i).getAsJsonArray().get(3).getAsString()};
            this.listValues[i] = assets.get(i).getAsJsonArray().get(6).getAsString();
            this.listSounds[i] = assets.get(i).getAsJsonArray().get(4).getAsString();
            this.listSoundsDescription[i] = assets.get(i).getAsJsonArray().get(5).getAsString();
            this.listCalculScores[i] = assets.get(i).getAsJsonArray().get(7).getAsString();
        }


        this.listNameScores = new String[scores.size()];
        this.listTagScores = new String[scores.size()];
        this.scoreValue = new int[scores.size()];
        this.maxValue = new int[scores.size()];

        for (int i=0; i<scores.size(); i++){
            this.listNameScores[i] = scores.get(i).getAsJsonArray().get(0).getAsString();
            this.listTagScores[i] = scores.get(i).getAsJsonArray().get(1).getAsString();
            this.scoreValue[i] = scores.get(i).getAsJsonArray().get(2).getAsInt();
            this.maxValue[i] = scores.get(i).getAsJsonArray().get(3).getAsInt();
        }
    }

    public void setSound(){
        if (this.indexFileImage < this.indexEndGame){
            Configuration config = ActiveConfigurationContext.getInstance();
            final String directorySounds = config.getFileDir() + "/evals/" + this.gameVariant.getNameGame() + "/sounds/";
            this.IMAGE_SOUND = directorySounds + this.listSounds[this.indexFileImage];
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

    public void nextRound(){

        Configuration config = ActiveConfigurationContext.getInstance();

        Timeline transition = new Timeline();
        transition.getKeyFrames().add(new KeyFrame(new Duration(config.getTransitionTime())));
        transition.setOnFinished(event -> {
            this.launch();
        });
    }
    RoundDetails pickAndBuildRandomPictures(final int numberOfImagesToDisplayPerRound, final int winnerImageIndexAmongDisplayedImages) {

        Configuration configActive = ActiveConfigurationContext.getInstance();

        int posX = 0;
        int posY = 0;

        boolean winnerP1;
        boolean winnerP2;

        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        final List<PictureCard> pictureCardList = new ArrayList<>();
        String questionSoundPath = null;
        String question = null;
        List<Image> pictograms = null;

        String imageP1 = this.listImages[this.indexFileImage][0];
        String imageP2 = this.listImages[this.indexFileImage][1];

        if (Objects.equals(this.listValues[this.indexFileImage], "First")) {
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
            posYImage, widthImg, heightImg, gameContext, gameVariant,
            winnerP1, imageP1 + "", stats, this, 0);

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
            posYImage, widthImg, heightImg, gameContext, gameVariant,
            winnerP2, imageP2 + "", stats, this, 1);

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

    public void startTimer(){
        if (this.indexFileImage == 0){
            currentRoundStartTime = System.currentTimeMillis();
        }
    }

    public void increaseIndexFileImage(boolean correctAnswer) {
        if (correctAnswer){
            this.resultsChoiceImages[this.indexFileImage] = "Correct";
            this.calculScores();
        }else {
            this.resultsChoiceImages[this.indexFileImage] = "Incorrect";
        }
        this.getTimer();
        this.indexFileImage = this.indexFileImage + 1;
        this.setSound();
    }

    public void getTimer(){
        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            this.timeImages[this.indexFileImage][p.imgIndex] = p.timeImg;
        }
    }

    public void calculScores(){
        String[] tmp = this.listCalculScores[this.indexFileImage].split(",");
        for (int i=0; i<this.listTagScores.length; i++){
            for (String s : tmp) {
                if (s.equals(this.listTagScores[i])) {
                    this.scores[i] += this.scoreValue[i];
                }
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

    public void choicePicturePair(){
        this.whiteCrossPicture.setVisible(false);
        this.playSound(BIP_SOUND);
        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            p.hideProgressIndicator();
            p.setVisibleImagePicture(true);
            timelineInput.playFromStart();
        }
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

    public void resetFromReplay(){
        this.totalItemsAddedManually = 0;
        this.indexFileImage = 0;
        this.nbCountError = 0;
        this.nbCountErrorSave = 0;
        Arrays.fill(this.scores, 0);
    }

    public void finalStats() {

        stats.timeGame = System.currentTimeMillis() - this.currentRoundStartTime;
        stats.nameScores = this.listNameScores;
        stats.scores = this.scores.clone();
        stats.maxScores = this.maxValue;
        stats.totalItemsAddedManually = this.totalItemsAddedManually;

        switch (this.outputFile) {
            case "csv" -> createCsvFile();
            case "xls" -> createExcelFile();
            case "all" -> {
                createCsvFile();
                createExcelFile();
            }
            default -> {
                log.info("No Output set or wrong statement");
            }
        }
    }

    public String getDate(){
        Date now = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm:ss");

        return formatDate.format(now);
    }

    public void createCsvFile(){

        File pathDirectory = stats.getGameStatsOfTheDayDirectory();
        String pathFile = pathDirectory + "\\" + this.gameName + "-" + DateUtils.dateTimeNow() + ".csv";
        this.stats.actualFile = pathFile;

        try {
            PrintWriter out = new PrintWriter(pathFile, StandardCharsets.UTF_16);
            out.append("\r\n");
            out.append("Nom de l'évaluation : ").append(this.gameName).append("\r\n");
            out.append("Fait le ").append(this.getDate()).append("\r\n");
            out.append("Temps de l'évaluation : ").append(String.valueOf(stats.timeGame / 100.)).append(" secondes \r\n");
            out.append("Nombre d'images: ").append(String.valueOf(this.nbImages)).append("\r\n");
            out.append("Number de sons : ").append(String.valueOf(this.nbSounds)).append("\r\n");
            out.append("Nombre d'items ajoutés manuellement : ").append(String.valueOf(this.totalItemsAddedManually)).append("\r\n");
            out.append("\r\n");
            if (!this.isAnonymous){
                out.append("Profil de la personne : ").append("\r\n");
                out.append("Nom : ").append(this.userProfil[0]).append("\r\n");
                out.append("Prénom : ").append(this.userProfil[1]).append("\r\n");
                out.append("Genre : ").append(this.userProfil[2]).append("\r\n");
                out.append("Age : ").append(this.userProfil[3]).append("\r\n");
                out.append("Date de naissance : ").append(this.userProfil[4]).append("\r\n");
                out.append("Lieu de naissance : ").append(this.userProfil[5]).append("\r\n");
                out.append("\r\n");
            }
            for (int i=0; i<this.indexEndGame; i++){
                out.append("Item ").append(String.valueOf(i+1)).append("\r\n");
                for (int j=0; j<this.nbImagesPerRound; j++){
                    out.append("- Nom de l'image -> ").append(this.listImages[i][j]).append("\r\n");
                    out.append("- Description de l'image -> ").append(this.listImagesDescription[i][j]).append("\r\n");
                }
                out.append("- Son utilisé -> ").append(this.listSounds[i]).append("\r\n");
                out.append("- Description du son -> ").append(this.listSoundsDescription[i]).append("\r\n");
                out.append("- Résultat -> ").append(this.resultsChoiceImages[i]).append("\r\n");
                out.append("\r\n");
            }
            out.append("Scores ").append("\r\n");
            for (int k=0; k<this.listNameScores.length; k++){
                out.append("- ").append(this.listNameScores[k]).append(" -> ").append(String.valueOf(this.scores[k])).append(" / ").append(String.valueOf(this.maxValue[k])).append("\r\n");
            }
            out.close();
        } catch (Exception e) {
            log.info("Error creation csv for GazePlay Eval stats game !");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("PMD")
    public void createExcelFile(){

        File pathDirectory = stats.getGameStatsOfTheDayDirectory();
        String pathFile = pathDirectory + "\\" + this.gameName + "-" + DateUtils.dateTimeNow() + ".xlsx";
        this.stats.actualFile = pathFile;

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(this.gameName);

        Object[][] bookData = new Object[(this.indexEndGame*(5 + 2*this.nbImagesPerRound)) + 8 + this.listNameScores.length][2];

        int nbElems = (this.nbImagesPerRound * 2) + 3;
        int item = 1;
        int indexImage = 0;
        int nextImages = 0;
        int start = 6;

        bookData[0] = new Object[]{"Nom de l'évaluation : ", this.gameName};
        bookData[1] = new Object[]{"Fait le : ", this.getDate()};
        bookData[2] = new Object[]{"Temps de l'évaluation: ", stats.timeGame / 100. + " secondes"};
        bookData[3] = new Object[]{"Nombre d'images : ", String.valueOf(this.nbImages)};
        bookData[4] = new Object[]{"Nombre de sons : ", String.valueOf(this.nbSounds)};
        bookData[5] = new Object[]{"Nombre d'items ajoutés manuellement : ", String.valueOf(this.totalItemsAddedManually)};

        if (!this.isAnonymous){
            bookData[6] = new Object[]{"", ""};
            bookData[7] = new Object[]{"Profil de la personne : ", ""};
            bookData[8] = new Object[]{"Nom : ", this.userProfil[0]};
            bookData[9] = new Object[]{"Prénom : ", this.userProfil[1]};
            bookData[10] = new Object[]{"Genre : ", this.userProfil[2]};
            bookData[11] = new Object[]{"Age : ", this.userProfil[3]};
            bookData[12] = new Object[]{"Date de naissance : ", this.userProfil[4]};
            bookData[13] = new Object[]{"Lieu de naissance : ", this.userProfil[5]};
            start = 14;
        }

        for (int i=0; i<this.indexEndGame; i++){
            bookData[start] = new Object[]{"", ""};
            bookData[start+1] = new Object[]{"Item " + item, ""};

            for (int j=(start+2); j<this.nbImagesPerRound+(start+2); j=j+2){
                for (int k=0; k<this.nbImagesPerRound; k++){
                    bookData[j + nextImages] = new Object[]{"- Nom de l'image -> ", this.listImages[indexImage][k]};
                    bookData[j+1 + nextImages] = new Object[]{"- Description de l'image -> ", this.listImagesDescription[indexImage][k]};
                    nextImages += 2;
                }

                bookData[j + nextImages] = new Object[]{"- Son utilisé -> ", this.listSounds[indexImage]};
                bookData[j + nextImages +1] = new Object[]{"- Description du son -> ", this.listSoundsDescription[indexImage]};
                bookData[j + nextImages +2] = new Object[]{"- Résultat -> ", this.resultsChoiceImages[indexImage]};

                indexImage += 1;
            }

            item += 1;
            start = start + nbElems + 2;
            nextImages = 0;
        }
        bookData[start] = new Object[]{"", ""};
        bookData[start+1] = new Object[]{"Scores"};
        for (int l=0; l<this.listNameScores.length; l++){
            bookData[start+2+l] = new Object[]{"- " + this.listNameScores[l] + " -> ", this.scores[l] + " / " + this.maxValue[l]};
        }

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
            log.info("Error creation xls for GazePlay Eval stats game !");
            e.printStackTrace();
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
