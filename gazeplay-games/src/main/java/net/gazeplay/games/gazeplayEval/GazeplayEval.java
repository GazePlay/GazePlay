package net.gazeplay.games.gazeplayEval;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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
    public int indexFileImage = 0;
    public int indexEndGame = 0;
    public int posX = 0;
    public int posY = 0;
    private boolean canRemoveItemManually = true;
    private RoundDetails currentRoundDetails;
    private PictureCard screen;
    private Long currentRoundStartTime;
    public ImageView whiteCrossPicture;
    public CustomInputEventHandler CustomInputEventHandler = new CustomInputEventHandler();
    public boolean reEntered = false;
    public boolean goNext = false;
    public int scores = 0;
    public int nbCountError = 0;
    public int nbCountErrorSave = 0;
    private int totalItemsAddedManually = 0;
    private int nbImageSee = 0;
    private ArrayList<String> listNameScores = new ArrayList<>(20);
    private ArrayList<Integer> listScoresPoints = new ArrayList<>(20);
    public Timeline getGazePositionXY;
    public Timeline createDisplayDuration;
    public ArrayList<Double> listGazePositionX = new ArrayList<>();
    public ArrayList<Double> listGazePositionY = new ArrayList<>();
    public String eyeTracker;
    public String typeScreen;

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
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, CustomInputEventHandler);
        this.eyeTracker = ActiveConfigurationContext.getInstance().getEyeTracker();

        this.loadConfig();
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
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, CustomInputEventHandler);
        this.eyeTracker = ActiveConfigurationContext.getInstance().getEyeTracker();

        this.loadConfig();

    }

    public void loadConfig(){
        Configuration config = ActiveConfigurationContext.getInstance();
        File gameDirectory = new File(config.getFileDir() + "\\evals\\" + this.gameVariant.getNameGame() + "\\config.json");
        JsonParser jsonParser = new JsonParser();
        try  (FileReader reader = new FileReader(gameDirectory)) {
            Object obj = jsonParser.parse(reader);
            JsonArray configFile = (JsonArray) obj;
            this.generateTab(configFile);
            configFile = this.shuffleJsonArray(configFile);
            for (int i=0; i<configFile.size(); i++){
                String value = String.valueOf(configFile.get(i));
                value = value.replace("[", "");
                value = value.replace("]", "");
                value = value.replace("\"", "");
                this.generateGame(value.split(","), i);
            }
            this.indexEndGame = configFile.size();
            this.setSound();
            this.getGazePosition();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateTab(JsonArray configFile){
        this.rows = new int[configFile.size()-1];
        this.cols = new int[configFile.size()-1];
        this.listImages = new String[configFile.size()-1][];
        this.listSounds = new String[configFile.size()-1];
        this.nbImages = new int[configFile.size()-1];
        this.listLengthFixation = new double[configFile.size()-1];
        this.displayDuration = new double[configFile.size()-1];

        this.gameName = String.valueOf(configFile.get(0).getAsJsonArray().get(0)).replace("\"", "");
        configFile.remove(0);
    }

    public JsonArray shuffleJsonArray(JsonArray configFile){
        Random rnd = new Random();
        for (int i = configFile.size() - 1; i >= 0; i--) {
            int j = rnd.nextInt(i + 1);
            rnd.setSeed(System.currentTimeMillis());
            JsonElement object = configFile.get(j);
            configFile.set(j, configFile.get(i));
            configFile.set(i, object);
        }
        return configFile;
    }

    public void generateGame(String[] values, int index){
        this.rows[index] = Integer.parseInt(values[0]);
        this.cols[index] = Integer.parseInt(values[1]);

        int nbImg = this.rows[index]*this.cols[index];
        String[] listImgTmp = new String[nbImg];
        for (int j=3; j<(3+nbImg); j++){
            listImgTmp[j-3] = values[j];
        }
        if (Objects.equals(values[nbImg + 7], "true")){
            List<String> list = Arrays.asList(listImgTmp);
            Collections.shuffle(list);
            list.toArray(listImgTmp);
        }
        this.listImages[index] = listImgTmp;
        this.listSounds[index] = values[nbImg+3];
        this.nbImages[index] = Integer.parseInt(values[nbImg+4]);
        this.listLengthFixation[index] = Double.parseDouble(values[nbImg+5]);
        this.displayDuration[index] = Double.parseDouble(values[nbImg+6]);
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
            if (config.isSoaEnabled()){
                Timeline soundSOA = new Timeline(new KeyFrame(Duration.millis(2000), event -> {
                    gameContext.getSoundManager().add(soundPath);
                }));
                soundSOA.setCycleCount(1);
                soundSOA.playFromStart();
            }else {
                gameContext.getSoundManager().add(soundPath);
            }
        }
    }

    public void getGazePosition(){
        log.info("Create timeline GP !");
        this.getGazePositionXY = new Timeline(new KeyFrame(Duration.millis(20), ev -> {
            double[] pos = this.gameContext.getGazeDeviceManager().getPosition();
            this.listGazePositionX.add(pos[0]);
            this.listGazePositionY.add(pos[1]);
        }));
        this.getGazePositionXY.setCycleCount(Timeline.INDEFINITE);
    }

    public void createDisplayDuration(){
        log.info("Create timeline DD !");
        this.createDisplayDuration = new Timeline(new KeyFrame(Duration.millis(this.displayDuration[this.indexFileImage]), event -> {
            log.info("DD passe !");
            if(this.increaseIndexFileImage()){
                this.finalStats();
                this.gameContext.updateScore(stats, this);
                this.resetFromReplay();
                this.dispose();
                this.gameContext.clear();
                this.gameContext.showRoundStats(stats, this);
            }else {
                this.stats.screenHeatMapGaze();
                this.stopGetGazePosition();
                this.dispose();
                this.gameContext.clear();
                this.launch();
            }
        }));
        this.createDisplayDuration.setCycleCount(1);
    }

    public void getScreenHeatmapGaze(){
        this.stats.screenHeatMapGaze();
    }

    @Override
    public void launch() {
        this.startTimer();

        this.nbImageSee = 0;
        this.canRemoveItemManually = true;

        gameContext.setLimiterAvailable();

        generateScreen();
        currentRoundDetails = pickAndBuildRandomPictures();

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();


        this.generateScreen();
    }

    public boolean checkAllPictureCardChecked() {
        this.nbImageSee++;
        return this.nbImageSee == this.nbImages[this.indexFileImage];
    }

    public void incrementPos(){
        this.posX++;
        if (this.posX == this.cols[this.indexFileImage]){
            this.posX = 0;
            this.posY++;
        }
    }

    public void clearScreen(){
        gameContext.clear();
    }

    public void generateScreen(){
        if (this.indexFileImage == 0){
            this.generateInstructionScreen();
        } else if (this.indexFileImage == (this.indexEndGame/2)) {
            this.generateBreakScreen();
        } else if (this.indexFileImage == this.indexEndGame){
            this.generateEndScreen();
        } else {
            this.generateCrossFixationScreen();
        }
    }

    public void generateInstructionScreen(){
        this.typeScreen = "instruction";
        final GameSizing gameSizing = new GameSizingComputer(1, 2, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        gameContext.getChildren().add(new ScreenCard(
            0,
            10,
            gameSizing.width,
            gameSizing.height-10,
            gameContext,
            gameVariant,
            "oculometrie.png",
            stats,
            this,
            "instruction",
            true
        ));

        gameContext.getChildren().add(new ScreenCard(
            gameSizing.width,
            10,
            gameSizing.width,
            gameSizing.height-10,
            gameContext,
            gameVariant,
            "question.png",
            stats,
            this,
            "instruction",
            false
        ));

        Configuration config = ActiveConfigurationContext.getInstance();
        final String soundPathInstruction = config.getFileDir() + "/evals/" + this.gameVariant.getNameGame() + "/sounds/Consigne.m4a";
        gameContext.getSoundManager().add(soundPathInstruction);
    }

    public void generateCrossFixationScreen(){
        this.typeScreen = "cross";

        final GameSizing gameSizing = new GameSizingComputer(1, 1, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        gameContext.getChildren().add(new ScreenCard(
            0,
            10,
            gameSizing.width,
            gameSizing.height-10,
            gameContext,
            gameVariant,
            "blackCross.png",
            stats,
            this,
            "cross",
            true
        ));
    }

    public void generateBreakScreen(){
        this.typeScreen = "break";

        final GameSizing gameSizing = new GameSizingComputer(1, 1, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        gameContext.getChildren().add(new ScreenCard(
            0,
            0,
            gameSizing.width,
            gameSizing.height,
            gameContext,
            gameVariant,
            "picto_pause.png",
            stats,
            this,
            "break",
            true
        ));
    }

    public void generateEndScreen(){
        this.typeScreen = "end";

        final GameSizing gameSizing = new GameSizingComputer(1, 1, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        gameContext.getChildren().add(new ScreenCard(
            0,
            0,
            gameSizing.width,
            gameSizing.height,
            gameContext,
            gameVariant,
            "feux_artifice.png",
            stats,
            this,
            "end",
            true
        ));

        Configuration config = ActiveConfigurationContext.getInstance();
        final String soundPathEnd = config.getFileDir() + "/evals/" + this.gameVariant.getNameGame() + "/sounds/Cloture.m4a";
        gameContext.getSoundManager().add(soundPathEnd);
    }

    public void generateGame(){
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

        this.playSound(this.IMAGE_SOUND);

        this.startGetGazePosition();
        this.startDisplayDuration();
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
                    this,
                    this.isFirstPosition()));

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

    public Boolean isFirstPosition(){
        return this.posX==0;
    }

    public void startTimer(){
        if (this.indexFileImage == 0){
            currentRoundStartTime = System.currentTimeMillis();
        }
    }

    public void startGetGazePosition(){
        log.info("Start timeline GP");
        this.getGazePositionXY.play();
    }

    public void stopGetGazePosition(){
        log.info("Stop timeline GP");
        this.getGazePositionXY.stop();
    }

    public void startDisplayDuration(){
        this.createDisplayDuration();
        log.info("Start timeline DD");
        this.createDisplayDuration.playFromStart();
    }

    public void stopDisplayDuration(){
        log.info("Stop timeline DD");
        this.createDisplayDuration.stop();
    }

    public void goToStats(){
        gameContext.showRoundStats(stats, this);
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

        this.stats.screenHeatMapGaze();
        stats.timeGame = System.currentTimeMillis() - this.currentRoundStartTime;
        stats.nameScores = this.listNameScores;
        stats.scores = this.listScoresPoints;
        stats.totalItemsAddedManually = this.totalItemsAddedManually;
        createExcelFile();
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
            bookData[i][0] = String.valueOf(this.listGazePositionX.get(i));
            bookData[i][1] = String.valueOf(this.listGazePositionY.get(i));
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

    private class CustomInputEventHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent key) {
            if (typeScreen.equals("instruction")){
                if (key.getCode().equals(KeyCode.SPACE)) {
                    clearScreen();
                    generateCrossFixationScreen();
                }
            } else if (typeScreen.equals("end")) {
                if (key.getCode().equals(KeyCode.SPACE)) {
                    clearScreen();
                    goToStats();
                }
            } else if (typeScreen.equals("break")) {
                if (key.getCode().equals(KeyCode.P)) {
                    clearScreen();
                    generateCrossFixationScreen();
                }
            }
        }
    }
}
