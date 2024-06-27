package net.gazeplay.games.gazeplayEval;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class GazeplayEval implements GameLifeCycle {

    private static final int NBMAXPICTO = 10;
    private static final double MAXSIZEPICTO = 250;
    private final IGameContext gameContext;
    private final GazeplayEvalGameVariant gameVariant;
    private final boolean fourThree;
    private final Stats stats;
    private final ArrayList<TargetAOI> targetAOIList;
    private final ReplayablePseudoRandom randomGenerator;
    private String gameName = "GazePlayEval";
    private String IMAGE_SOUND = "";
    private int[] rows;
    private int[] cols;
    private int[] nbImages;
    private String[][] listImages;
    private String[] listSounds;
    private double[] listLengthFixation;
    private double[] displayDuration;
    private ArrayList<String> listNameScores = new ArrayList<>(20);
    private ArrayList<Integer> listScoresPoints = new ArrayList<>(20);
    public int indexFileImage = 0;
    public int indexEndGame = 0;
    public int posX = 0;
    public int posY = 0;
    private boolean canRemoveItemManually = true;
    private RoundDetails currentRoundDetails;
    private Long currentRoundStartTime;
    public ImageView whiteCrossPicture;
    public CustomInputEventHandlerKeyboard customInputEventHandlerKeyboard = new CustomInputEventHandlerKeyboard();
    public boolean reEntered = false;
    public boolean goNext = false;
    public int scores = 0;
    public int nbCountError = 0;
    public int nbCountErrorSave = 0;
    private int totalItemsAddedManually = 0;
    private int nbImageSee = 0;
    private String outputFile = "";
    public Timeline getGazePositionXY;
    public ArrayList<Double> listGazePositionX = new ArrayList<>();
    public ArrayList<Double> listGazePositionY = new ArrayList<>();

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
        this.loadConfig();
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
        this.loadConfig();
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, customInputEventHandlerKeyboard);

    }

    public void loadConfig(){
        Configuration config = ActiveConfigurationContext.getInstance();
        File gameDirectory = new File(config.getFileDir() + "\\evals\\" + this.gameVariant.getNameGame() + "\\config.json");
        JsonParser jsonParser = new JsonParser();
        try  (FileReader reader = new FileReader(gameDirectory)) {
            Object obj = jsonParser.parse(reader);
            JsonArray configFile = (JsonArray) obj;
            this.generateTab(configFile.size());
            for (int i=1; i<configFile.size(); i++){
                String value = String.valueOf(configFile.get(i));
                value = value.replace("[", "");
                value = value.replace("]", "");
                value = value.replace("\"", "");
                this.generateGame(value.split(","), i);
            }
            this.indexEndGame = configFile.size()-1;
            this.setSound();
            this.getGazePosition();
            //this.logInfo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateTab(int size){
        this.rows = new int[size-1];
        this.cols = new int[size-1];
        this.listImages = new String[size-1][];
        this.listSounds = new String[size-1];
        this.nbImages = new int[size-1];
        this.listLengthFixation = new double[size-1];
        this.displayDuration = new double[size-1];
    }

    public void generateGame(String[] values, int index){
        this.rows[index-1] = Integer.parseInt(values[0]);
        this.cols[index-1] = Integer.parseInt(values[1]);

        int nbImg = this.rows[index-1]*this.cols[index-1];
        String[] listImgTmp = new String[nbImg];
        for (int j=3; j<(3+nbImg); j++){
            listImgTmp[j-3] = values[j];
        }
        if (Objects.equals(values[nbImg + 7], "true")){
            List<String> list = Arrays.asList(listImgTmp);
            Collections.shuffle(list);
            list.toArray(listImgTmp);
        }
        this.listImages[index-1] = listImgTmp;
        this.listSounds[index-1] = values[nbImg+3];
        this.nbImages[index-1] = Integer.parseInt(values[nbImg+4]);
        this.listLengthFixation[index-1] = Double.parseDouble(values[nbImg+5]);
        this.displayDuration[index-1] = Double.parseDouble(values[nbImg+6]);
    }

    public void logInfo(){
        log.info(String.valueOf(this.indexEndGame));
        log.info("Rows = " + Arrays.toString(this.rows));
        log.info("Cols = " + Arrays.toString(this.cols));
        log.info("List Img = " + Arrays.deepToString(this.listImages));
        log.info("List sounds = " + Arrays.toString(this.listSounds));
        log.info("Nb img = " + Arrays.toString(this.nbImages));
        log.info("List length fixation = " + Arrays.toString(this.listLengthFixation));
        log.info("List display duration = " + Arrays.toString(this.displayDuration));
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

    public void getGazePosition(){
        log.info("Create timeline !");
        this.getGazePositionXY = new Timeline(new KeyFrame(Duration.millis(20), ev -> {
            double[] pos = this.gameContext.getGazeDeviceManager().getPosition();
            log.info("Pos -> " + Arrays.toString(pos));
            this.listGazePositionX.add(pos[0]);
            this.listGazePositionY.add(pos[1]);
        }));
        this.getGazePositionXY.setCycleCount(Timeline.INDEFINITE);
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
        this.startGetGazePosition();
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
                pictureCardList.add(new PictureCard(
                    gameSizing.width * posX,
                    gameSizing.height * posY + 10,
                    gameSizing.width,
                    gameSizing.height -10,
                    gameContext,
                    gameVariant,
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

    public void startGetGazePosition(){
        log.info("Start timeline");
        this.getGazePositionXY.play();
    }

    public void stopGetGazePosition(){
        log.info("Start timeline");
        this.getGazePositionXY.stop();
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

    public void calculScores(String name){
        if (name.contains("isGoodImg")){
            this.scores++;
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
            //timelineInput.playFromStart();
        }
    }

    @Override
    public void dispose() {
        this.getGazePositionXY.stop();
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

        createExcelFile();
    }

    public String getDate(){
        Date now = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm:ss");

        return formatDate.format(now);
    }

    @SuppressWarnings("PMD")
    public void createExcelFile(){

        File pathDirectory = stats.getGameStatsOfTheDayDirectory();
        String pathFile = pathDirectory + "\\" + this.gameName + "-" + DateUtils.dateTimeNow() + ".xlsx";
        this.stats.actualFile = pathFile;

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(this.gameName);

        Object[][] bookData = new Object[this.listGazePositionX.size()][2];

        for (int i=0; i<this.listGazePositionX.size(); i++){
            log.info("Pos X -> " + this.listGazePositionX.get(0));
            log.info("Pos Y -> " + this.listGazePositionY.get(0));
            bookData[0][0] = this.listGazePositionX.get(0);
            bookData[0][1] = this.listGazePositionY.get(0);
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

            if (ignoreAnyInput) {
                return;
            }

            if (key.getCode().isArrowKey() && goNext){
                ignoreAnyInput = true;
                next("null");
            } else if (key.getCode().getChar().equals("X")) {
                ignoreAnyInput = true;
                next("True");
            } else if (key.getCode().getChar().equals("C")) {
                ignoreAnyInput = true;
                next("False");
            } else if (key.getCode().getChar().equals("V")) {
                removeItemAddedManually();
            }
        }
    }
}
