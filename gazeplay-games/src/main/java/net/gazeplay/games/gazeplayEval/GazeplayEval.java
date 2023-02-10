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
    private String gameName = "GazePlayEval";
    private String IMAGE_SOUND = "";
    private String[][] gameRules;
    private int[] rows;
    private int[] cols;
    private int[] nbImages;
    private int[] nbGoodImages;
    private String[][] listImages;
    private String[][] listGoodImages;
    private String[] listSounds;
    private int[] listLengthFixation;
    private String[][] listScores;
    private int[][] scoreGoodAnswer;
    private int[][] scoreWrongAnswer;
    private ArrayList<String> listNameScores = new ArrayList<>(20);
    private ArrayList<Integer> listScoresPoints = new ArrayList<>(20);
    public int indexFileImage = 0;
    public int indexEndGame = 0;
    public int posX = 0;
    public int posY = 0;
    private boolean canRemoveItemManually = true;
    private RoundDetails currentRoundDetails;
    private Long currentRoundStartTime;
    private final Timeline timelineQuestion = waitForQuestion();
    private final Timeline timelineInput = waitForInput();
    public ImageView whiteCrossPicture;
    public CustomInputEventHandlerKeyboard customInputEventHandlerKeyboard = new CustomInputEventHandlerKeyboard();
    public boolean reEntered = false;
    public boolean goNext = false;
    public int nbCountError = 0;
    public int nbCountErrorSave = 0;
    private int totalItemsAddedManually = 0;
    private int nbImageSee = 0;
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
        String jsonToArray = "";
        try  (FileReader reader = new FileReader(gameDirectory, StandardCharsets.UTF_8)) {
            JsonArray obj = jsonParser.parse(reader).getAsJsonArray();
            this.gameRules = new String[obj.size()][];
            for (int i=0; i<obj.size(); i++){
                jsonToArray = "";
                for (int j=0; j<obj.get(i).getAsJsonArray().size(); j++){
                    String value = obj.get(i).getAsJsonArray().get(j).getAsString();
                    if (Objects.equals(value, "")){
                        jsonToArray += "null;";
                    }else {
                        jsonToArray += obj.get(i).getAsJsonArray().get(j).getAsString() + ";";
                    }
                }
                this.gameRules[i] = jsonToArray.split(";");
            }
            this.indexEndGame = this.gameRules.length;
            this.generateTabFromJson();
            this.setSound();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateTabFromJson(){

        this.rows = new int[this.gameRules.length];
        this.cols = new int[this.gameRules.length];
        this.listImages = new String[this.gameRules.length][];
        this.listGoodImages = new String[this.gameRules.length][];
        this.listSounds = new String[this.gameRules.length];
        this.nbImages = new int[this.gameRules.length];
        this.nbGoodImages = new int[this.gameRules.length];
        this.listLengthFixation = new int[this.gameRules.length];
        this.listScores = new String[this.gameRules.length][];
        this.scoreGoodAnswer = new int[this.gameRules.length][];
        this.scoreWrongAnswer = new int[this.gameRules.length][];

        //Rows & Cols
        for (int i=0; i<this.gameRules.length; i++){
            this.rows[i] = Integer.parseInt(this.gameRules[i][0]);
            this.cols[i] = Integer.parseInt(this.gameRules[i][1]);


            //List images
            int nbImage = this.rows[i] * this.cols[i];
            String[] tmpImages = new String[nbImage];
            for (int j=0; j<nbImage; j++){
                tmpImages[j] = this.gameRules[i][j+2];
            }
            this.listImages[i] = tmpImages;

            //List sounds
            log.info("Sound = " + this.gameRules[i][nbImage+2]);
            this.listSounds[i] = this.gameRules[i][nbImage+2];

            //Number images to see
            log.info("Nb image to see = " + this.gameRules[i][nbImage+3]);
            this.nbImages[i] = Integer.parseInt(this.gameRules[i][nbImage+3]);

            //Number good images
            log.info("Nb good image = " + this.gameRules[i][nbImage+4]);
            this.nbGoodImages[i] = Integer.parseInt(this.gameRules[i][nbImage+4]);

            //List good images
            String[] tmpGoodImage = new String[this.nbGoodImages[i]];
            for (int k=0; k<this.nbGoodImages[i]; k++){
                tmpGoodImage[k] = this.gameRules[i][nbImage+5+k];
            }
            this.listGoodImages[i] = tmpGoodImage;

            //Fixation length
            if (Objects.equals(this.gameRules[i][nbImage+5+this.nbGoodImages[i]], "null")){
                this.listLengthFixation[i] = 0;
            }else {
                this.listLengthFixation[i] = Integer.parseInt(this.gameRules[i][nbImage+5+this.nbGoodImages[i]]);
            }

            //Scores
            int nbScore = (this.gameRules[i].length - (nbImage+5+this.nbGoodImages[i]) - 1) / 3;
            String[] tmpListScores = new String[nbScore];
            int[] tmpScoreGoodAnswer = new int[nbScore];
            int[] tmpScoreWrongAnswer = new int[nbScore];
            int tmpIndexScores = 0;
            for (int l=0; l<nbScore; l++){
                tmpListScores[l] = this.gameRules[i][nbImage+5+this.nbGoodImages[i]+1+tmpIndexScores];
                if (Objects.equals(this.gameRules[i][nbImage+5+this.nbGoodImages[i]+2+tmpIndexScores], "null")){
                    tmpScoreGoodAnswer[l] = 0;
                }else {
                    tmpScoreGoodAnswer[l] = Integer.parseInt(this.gameRules[i][nbImage+5+this.nbGoodImages[i]+2+tmpIndexScores]);
                }
                if (Objects.equals(this.gameRules[i][nbImage+5+this.nbGoodImages[i]+3+tmpIndexScores], "null")){
                    tmpScoreWrongAnswer[l] = 0;
                }else {
                    tmpScoreWrongAnswer[l] = Integer.parseInt(this.gameRules[i][nbImage+5+this.nbGoodImages[i]+3+tmpIndexScores]);
                }
                tmpIndexScores += 3;
            }
            this.listScores[i] = tmpListScores;
            this.scoreGoodAnswer[i] = tmpScoreGoodAnswer;
            this.scoreWrongAnswer[i] = tmpScoreWrongAnswer;
        }
        /*log.info("---TEST ROWS--- = " + Arrays.toString(this.rows));
        log.info("---TEST COLS--- = " + Arrays.toString(this.cols));
        log.info("---TEST IMG--- = " + Arrays.deepToString(this.listImages));
        log.info("---TEST GIMG--- = " + Arrays.deepToString(this.listGoodImages));
        log.info("---TEST LSON--- = " + Arrays.toString(this.listSounds));
        log.info("---TEST NI--- = " + Arrays.toString(this.nbImages));
        log.info("---TEST NBGI--- = " + Arrays.toString(this.nbGoodImages));
        log.info("---TEST LF--- = " + Arrays.toString(this.listLengthFixation));
        log.info("---TEST LS--- = " + Arrays.deepToString(this.listScores));
        log.info("---TEST SGA--- = " + Arrays.deepToString(this.scoreGoodAnswer));
        log.info("---TEST SWA--- = " + Arrays.deepToString(this.scoreWrongAnswer));*/
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

        this.nbImageSee = 0;
        this.canRemoveItemManually = true;

        gameContext.setLimiterAvailable();

        currentRoundDetails = pickAndBuildRandomPictures();

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
            final double positionY = gamePaneDimension2D.getHeight() / 2;

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
            p.toFront();
            p.setOpacity(1);
        }

        stats.notifyNewRoundReady();

        gameContext.onGameStarted(2000);

        customInputEventHandlerKeyboard.ignoreAnyInput = false;

        this.playSound(this.IMAGE_SOUND);
    }

    public boolean checkAllPictureCardChecked() {
        this.nbImageSee++;
        return this.nbImageSee == this.nbImages[this.indexFileImage];
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

    public void incrementPos(){
        this.posX++;
        if (this.posX == this.cols[this.indexFileImage]){
            this.posX = 0;
            this.posY++;
        }
    }

    public String valueImage(String img){
        if (this.nbGoodImages[this.indexFileImage] == 0){
            return "null";
        }else {
            String value = "False";
            for (int i=0; i<this.listGoodImages[this.indexFileImage].length; i++){
                if (Objects.equals(this.listGoodImages[this.indexFileImage][i], img)){
                    value = "True";
                    break;
                }
            }
            return value;
        }
    }

    RoundDetails pickAndBuildRandomPictures() {

        this.posX = 0;
        this.posY = 0;

        final GameSizing gameSizing = new GameSizingComputer(this.rows[this.indexFileImage], this.cols[this.indexFileImage], fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        final List<PictureCard> pictureCardList = new ArrayList<>();
        String questionSoundPath = null;
        String question = null;
        List<Image> pictograms = null;

        for (int i=0; i<this.listImages[this.indexFileImage].length; i++){
            if (Objects.equals(this.listImages[this.indexFileImage][i], "null")){
                this.incrementPos();
            } else {
                log.info("---TEST ---" + this.valueImage(this.listImages[this.indexFileImage][i]));
                log.info("---TEST ---" + this.listImages[this.indexFileImage][i]);
                log.info("---TEST ---" + this.listLengthFixation[this.indexFileImage]);
                pictureCardList.add(new PictureCard(
                    gameSizing.width * posX,
                    gameSizing.height * posY + 10,
                    gameSizing.width,
                    gameSizing.height -10,
                    gameContext,
                    gameVariant,
                    this.valueImage(this.listImages[this.indexFileImage][i]),
                    this.listImages[this.indexFileImage][i],
                    this.listLengthFixation[this.indexFileImage],
                    stats,
                    this));

                targetAOIList.add(new TargetAOI(
                    gameSizing.width * posX,
                    gameSizing.height * posY,
                    (int) gameSizing.height,
                    System.currentTimeMillis()
                ));
                this.incrementPos();
            }
        }

        return new RoundDetails(pictureCardList, 0, questionSoundPath, question,
            pictograms);
    }

    public void startTimer(){
        if (this.indexFileImage == 0){
            currentRoundStartTime = System.currentTimeMillis();
        }
    }

    public boolean increaseIndexFileImage() {
        this.indexFileImage = this.indexFileImage + 1;
        if (this.indexFileImage == this.indexEndGame){
            return true;
        }else {
            this.setSound();
            return false;
        }
    }

    public void calculScores(String value){
        log.info("---TEST CV---" + value);
        if (!Objects.equals(value, "null")) {
            for (int i = 0; i < this.listScores[this.indexFileImage].length; i++) {
                if (this.listNameScores.contains(this.listScores[this.indexFileImage][i])) {
                    int index = this.listNameScores.indexOf(this.listScores[this.indexFileImage][i]);
                    if (Objects.equals(value, "True")) {
                        this.listScoresPoints.add(index, this.listScoresPoints.get(index) + this.scoreGoodAnswer[this.indexFileImage][i]);
                    } else if (Objects.equals(value, "False")) {
                        this.listScoresPoints.add(index, this.listScoresPoints.get(index) + this.scoreWrongAnswer[this.indexFileImage][i]);
                    }
                } else {
                    this.listNameScores.add(this.listScores[this.indexFileImage][i]);
                    if (Objects.equals(value, "True")) {
                        this.listScoresPoints.add(this.scoreGoodAnswer[this.indexFileImage][i]);
                    } else if (Objects.equals(value, "False")) {
                        this.listScoresPoints.add(this.scoreWrongAnswer[this.indexFileImage][i]);
                    }
                }
            }
        }
    }

    private void next(String value) {
        currentRoundDetails.getPictureCardList().get(0).waitBeforeNextRound();
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
        this.nbImageSee = 0;
        //Arrays.fill(this.scores, 0);
    }

    public void finalStats() {

        stats.timeGame = System.currentTimeMillis() - this.currentRoundStartTime;
        stats.nameScores = this.listNameScores;
        stats.scores = this.listScoresPoints;
        stats.totalItemsAddedManually = this.totalItemsAddedManually;

        /*switch (this.outputFile) {
            case "csv" -> createCsvFile();
            case "xls" -> createExcelFile();
            case "all" -> {
                createCsvFile();
                createExcelFile();
            }
            default -> {
                log.info("No Output set or wrong statement");
            }
        }*/
    }

    public String getDate(){
        Date now = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm:ss");

        return formatDate.format(now);
    }

    public void createCsvFile(){

        /*File pathDirectory = stats.getGameStatsOfTheDayDirectory();
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
        }*/
    }

    @SuppressWarnings("PMD")
    public void createExcelFile(){

        /*File pathDirectory = stats.getGameStatsOfTheDayDirectory();
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
        }*/
    }

    private class CustomInputEventHandlerKeyboard implements EventHandler<KeyEvent> {

        public boolean ignoreAnyInput = false;

        @Override
        public void handle(KeyEvent key) {

            if (ignoreAnyInput) {
                return;
            }

            if (key.getCode().isArrowKey() && goNext){
                ignoreAnyInput = true;
                timelineQuestion.stop();
                next("null");
            } else if (key.getCode().getChar().equals("X")) {
                ignoreAnyInput = true;
                timelineQuestion.stop();
                next("True");
            } else if (key.getCode().getChar().equals("C")) {
                ignoreAnyInput = true;
                timelineQuestion.stop();
                next("False");
            } else if (key.getCode().getChar().equals("V")) {
                timelineQuestion.stop();
                removeItemAddedManually();
            }
        }
    }
}
