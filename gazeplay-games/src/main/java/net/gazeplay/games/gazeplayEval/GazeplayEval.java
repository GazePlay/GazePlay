package net.gazeplay.games.gazeplayEval;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private String pathStatsGame = "";
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
    public int countStats = 0;
    public ArrayList<Double> listGazePositionX = new ArrayList<>();
    public ArrayList<Double> listGazePositionY = new ArrayList<>();
    public ArrayList<String> idImg = new ArrayList<>();
    public ArrayList<String> posImgHG = new ArrayList<>();
    public ArrayList<String> posImgHD = new ArrayList<>();
    public ArrayList<String> posImgBG = new ArrayList<>();
    public ArrayList<String> posImgBD = new ArrayList<>();
    public String eyeTracker;
    public String typeScreen;

    public List<List<Object>> allScreens;
    public Timeline transitionScreenT;
    public Timeline instructionScreenT;
    public Timeline stimuliScreenT;
    public int maxItemSelected;
    public int nbItemSelected = 0;
    public String actualSound;
    public Text instructionText;

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

        this.testEval();
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

        this.testEval();
    }

    public void testEval() {
        Configuration config = ActiveConfigurationContext.getInstance();
        generateStatsFolder();
        Path jsonPath = Path.of(config.getFileDir() + "\\evals\\" + this.gameVariant.getNameGame() + "\\evalData.json");

        try {
            this.allScreens = JTCconvert(jsonPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.setStats();
        this.indexEndGame = this.allScreens.size();
    }

    public static List<List<Object>> JTCconvert(Path jsonPath) throws Exception {

        try (Reader reader = Files.newBufferedReader(jsonPath)) {

            JsonArray root = new JsonParser().parse(reader).getAsJsonArray();
            List<List<Object>> result = new ArrayList<>();

            for (JsonElement element : root) {
                JsonObject obj = element.getAsJsonObject();
                String type = obj.get("Type").getAsString().toLowerCase();

                switch (type) {
                    case "transition" -> result.add(parseTransition(obj));
                    case "instruction" -> result.add(parseInstruction(obj));
                    case "stimuli" -> result.add(parseStimuli(obj));
                }
            }
            return result;
        }
    }

    private static List<Object> parseTransition(JsonObject obj) {

        return List.of(
            "transition",
            obj.get("Mettre un temps avant passage à l'écran suivant").getAsBoolean(),
            obj.get("Combien de temps").getAsInt(),
            obj.get("Mettre une croix de fixation").getAsBoolean(),
            obj.get("Mettre un temps de fixation").getAsBoolean(),
            obj.get("Combien de temps de fixation").getAsInt()
        );
    }

    private static List<Object> parseInstruction(JsonObject obj) {

        return List.of(
            "instruction",
            obj.get("Mettre un temps avant passage à l'écran suivant").getAsBoolean(),
            obj.get("Combien de temps").getAsInt(),
            obj.get("Ajouter un media").getAsBoolean(),
            obj.get("Type de media").getAsString(),
            obj.get("Nom du fichier").getAsString(),
            obj.get("Mettre un temps de fixation").getAsBoolean(),
            obj.get("Combien de temps de fixation").getAsInt()
        );
    }

    private static List<Object> parseStimuli(JsonObject obj) {

        List<Object> row = new ArrayList<>();

        row.add("stimuli");
        row.add(obj.get("Nombre de lignes").getAsInt());
        row.add(obj.get("Nombre de colonnes").getAsInt());
        row.add(obj.get("Mettre un temps avant passage à l'écran suivant").getAsBoolean());
        row.add(obj.get("Combien de temps").getAsInt());
        row.add(obj.get("Combien de temps de fixation").getAsInt());
        row.add(obj.get("Combien de stimuli à sélectionner").getAsInt());
        row.add(obj.get("Position stimuli aléatoire").getAsBoolean());
        row.add(obj.get("Caché stimuli après selection").getAsBoolean());
        row.add(obj.get("Mettre un son").getAsBoolean());
        row.add(obj.get("Nom du fichier").getAsString());

        JsonObject stimuli = obj.getAsJsonObject("Liste des stimuli");

        stimuli.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e ->
                row.add(e.getValue()
                    .getAsJsonObject()
                    .get("imageName")
                    .getAsString())
            );

        return row;
    }

    public void setStats(){
        this.stats.nameScores = this.listNameScores;
        this.stats.scores = this.listScoresPoints;
    }

    public void generateStatsFolder(){
        String username = System.getProperty("user.name");
        String directory = "C:/Users/" + username + "/Documents/GazePlayLearning_Eval";

        File anrProjet = new File(directory);
        if (!anrProjet.exists()){
            anrProjet.mkdirs();
        }

        int index = 1;
        File dir = new File(directory, "Eval" + index + "_" + DateUtils.today());
        while (dir.exists()){
            index++;
            dir = new File(directory, "Eval" + index + "_" + DateUtils.today());
        }
        dir.mkdirs();
        this.pathStatsGame = "C:/Users/" + username + "/Documents/GazePlayLearning_Eval/" + "Eval" + index + "_" + DateUtils.today();
        this.countStats = 0;
    }

    /*public void generateTab(JsonArray configFile){
        this.rows = new int[configFile.size()-1];
        this.cols = new int[configFile.size()-1];
        this.listImages = new String[configFile.size()-1][];
        this.listSounds = new String[configFile.size()-1];
        this.nbImages = new int[configFile.size()-1];
        this.listLengthFixation = new double[configFile.size()-1];
        this.displayDuration = new double[configFile.size()-1];

        this.gameName = String.valueOf(configFile.get(0).getAsJsonArray().get(0)).replace("\"", "");
        configFile.remove(0);
    }*/

    /*public JsonArray shuffleJsonArray(JsonArray configFile){
        Random rnd = new Random();
        for (int i = configFile.size() - 1; i >= 0; i--) {
            int j = rnd.nextInt(i + 1);
            rnd.setSeed(System.currentTimeMillis());
            JsonElement object = configFile.get(j);
            configFile.set(j, configFile.get(i));
            configFile.set(i, object);
        }
        return configFile;
    }*/

    /*public void generateGame(String[] values, int index){
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
    }*/

    /*public void setSound(){
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
    }*/

    public void getGazePosition(Configuration config){
        log.info("Create timeline GP !");
        this.getGazePositionXY = new Timeline(new KeyFrame(Duration.millis((config.getFrameGazePosition()/1000.0)), ev -> {
            double[] pos = this.gameContext.getGazeDeviceManager().getPosition();
            this.countStats++;
            this.listGazePositionX.add(pos[0]);
            this.listGazePositionY.add(pos[1]);
            this.posImgHG.add(this.listImages[this.indexFileImage][0]);
            this.posImgHD.add(this.listImages[this.indexFileImage][1]);
            this.posImgBG.add(this.listImages[this.indexFileImage][2]);
            this.posImgBD.add(this.listImages[this.indexFileImage][3]);
        }));
        this.getGazePositionXY.setCycleCount(Timeline.INDEFINITE);
    }

    /*public void createDisplayDuration(){
        log.info("Create timeline DD !");
        this.createDisplayDuration = new Timeline(new KeyFrame(Duration.millis(this.displayDuration[this.indexFileImage]), event -> {
            log.info("DD passe !");
            if(this.increaseIndexFileImage()){
                this.removeEventHandlerPictureCard();
                this.stopGetGazePosition();
                this.stopDisplayDuration();
                this.getScreenHeatmapGaze();
                this.finalStats();
                this.gameContext.updateScore(stats, this);
                this.resetFromReplay();
                this.dispose();
                this.gameContext.clear();
                this.gameContext.showRoundStats(stats, this);
            }else {
                this.removeEventHandlerPictureCard();
                this.stopGetGazePosition();
                this.stopDisplayDuration();
                this.getScreenHeatmapGaze();
                this.gameContext.updateScore(stats, this);
                this.dispose();
                this.gameContext.clear();
                this.launch();
            }
        }));
        this.createDisplayDuration.setCycleCount(1);
    }*/

    public void getScreenHeatmapGaze(){
        this.stats.screenHeatMapGaze(this.pathStatsGame);
    }

    @Override
    public void launch() {
        this.startTimer();

        this.nbImageSee = 0;
        this.canRemoveItemManually = true;

        //gameContext.setLimiterAvailable();

        //currentRoundDetails = pickAndBuildRandomPictures();

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();

        this.generateScreen();
    }

    public boolean checkAllPictureCardChecked() {
        this.nbItemSelected++;
        return this.nbItemSelected == this.maxItemSelected;
    }

    public void incrementPos(){
        this.posX++;
        if (this.posX == (Integer) this.allScreens.get(this.indexFileImage).get(2)){
            this.posX = 0;
            this.posY++;
        }
    }

    public void clearScreen(){
        gameContext.clear();
    }

    public void generateScreen(){
        if (this.indexFileImage >= this.allScreens.size()){
            this.dispose();
            this.gameContext.clear();
            this.gameContext.showRoundStats(stats, this);
        }else {
            String type = this.allScreens.get(this.indexFileImage).get(0).toString();

            if (Objects.equals(type, "transition")){
                if ((boolean) this.allScreens.get(this.indexFileImage).get(3)){
                    this.generateCrossFixationScreen((boolean) this.allScreens.get(this.indexFileImage).get(4), (Integer) this.allScreens.get(this.indexFileImage).get(5));
                }
                if ((boolean) this.allScreens.get(this.indexFileImage).get(1)){
                    this.transitionScreenT = new Timeline(new KeyFrame(Duration.seconds(((Number) this.allScreens.get(this.indexFileImage).get(2)).doubleValue()), event -> {
                        this.clearScreen();
                        this.increaseIndex();
                        this.stats.resetHeatMapGaze();
                        this.generateScreen();
                    }));
                    this.transitionScreenT.setCycleCount(1);
                    this.transitionScreenT.playFromStart();
                }
            } else if (Objects.equals(type, "instruction")) {
                if ((boolean) this.allScreens.get(this.indexFileImage).get(3)){
                    if (Objects.equals(this.allScreens.get(this.indexFileImage).get(4), "Image")){
                        this.generateInstructionScreen((String) this.allScreens.get(this.indexFileImage).get(5), (boolean) this.allScreens.get(this.indexFileImage).get(6), (Integer) this.allScreens.get(this.indexFileImage).get(7));
                    }else if (Objects.equals(this.allScreens.get(this.indexFileImage).get(4), "Texte")){
                        Text instructionText = new Text(
                            (String) this.allScreens.get(this.indexFileImage).get(5)
                        );

                        instructionText.setTextAlignment(TextAlignment.CENTER);

                        instructionText.setWrappingWidth(
                            gameContext.getGamePanelDimensionProvider().getDimension2D().getWidth() * 0.8
                        );

                        final String color = gameContext.getConfiguration().getBackgroundStyle()
                            .accept(new BackgroundStyleVisitor<>() {
                                @Override public String visitLight() { return "titleB"; }
                                @Override public String visitDark()  { return "titleW"; }
                            });

                        instructionText.setId(color);

                        StackPane centerPane = new StackPane(instructionText);
                        StackPane.setAlignment(instructionText, Pos.CENTER);

                        centerPane.setPrefSize(
                            gameContext.getGamePanelDimensionProvider().getDimension2D().getWidth(),
                            gameContext.getGamePanelDimensionProvider().getDimension2D().getHeight()
                        );

                        gameContext.getChildren().add(centerPane);
                    }else if (Objects.equals(this.allScreens.get(this.indexFileImage).get(4), "Son")){
                        this.playSound((String) this.allScreens.get(this.indexFileImage).get(5));
                    }else if (Objects.equals(this.allScreens.get(this.indexFileImage).get(4), "Video")){
                        Configuration config = ActiveConfigurationContext.getInstance();
                        String videoPath = Paths.get(
                            config.getFileDir() +
                                "\\evals\\" +
                                this.gameVariant.getNameGame() +
                                "\\videos\\" +
                                this.allScreens.get(this.indexFileImage).get(5)
                        ).toUri().toString();

                        Media media = new Media(videoPath);
                        MediaPlayer mediaPlayer = new MediaPlayer(media);
                        MediaView mediaView = new MediaView(mediaPlayer);

                        mediaView.setPreserveRatio(true);

                        Dimension2D dim = gameContext.getGamePanelDimensionProvider().getDimension2D();
                        mediaView.setFitWidth(dim.getWidth() * 0.8);
                        mediaView.setFitHeight(dim.getHeight() * 0.8);

                        StackPane centerPane = new StackPane(mediaView);
                        centerPane.setPrefSize(dim.getWidth(), dim.getHeight());
                        StackPane.setAlignment(mediaView, Pos.CENTER);

                        gameContext.getChildren().add(centerPane);

                        mediaPlayer.play();
                    }
                }
                if ((boolean) this.allScreens.get(this.indexFileImage).get(1)){
                    this.instructionScreenT = new Timeline(new KeyFrame(Duration.seconds(((Number) this.allScreens.get(this.indexFileImage).get(2)).doubleValue()), event -> {
                        this.clearScreen();
                        this.increaseIndex();
                        this.stats.resetHeatMapGaze();
                        this.generateScreen();
                    }));
                    this.instructionScreenT.setCycleCount(1);
                    this.instructionScreenT.playFromStart();
                }
            } else if (Objects.equals(type, "stimuli")) {
                this.maxItemSelected = (Integer) this.allScreens.get(this.indexFileImage).get(6);
                this.nbItemSelected = 0;
                this.generateStimuliScreen(
                    (Integer) this.allScreens.get(this.indexFileImage).get(1),
                    (Integer) this.allScreens.get(this.indexFileImage).get(2),
                    ((Number) this.allScreens.get(this.indexFileImage).get(5)).doubleValue(),
                    (String) this.allScreens.get(this.indexFileImage).get(10)
                    );
                if ((boolean) this.allScreens.get(this.indexFileImage).get(3)){
                    this.stimuliScreenT = new Timeline(new KeyFrame(Duration.seconds(((Number) this.allScreens.get(this.indexFileImage).get(4)).doubleValue()), event -> {
                        this.clearScreen();
                        this.increaseIndex();
                        this.stats.resetHeatMapGaze();
                        this.generateScreen();
                    }));
                    this.stimuliScreenT.setCycleCount(1);
                    this.stimuliScreenT.playFromStart();
                }
            } else {
                this.dispose();
                this.gameContext.clear();
                this.gameContext.showRoundStats(stats, this);
            }
        }
    }

    public void playSound(String nameSound){
        Configuration config = ActiveConfigurationContext.getInstance();
        this.actualSound = config.getFileDir() + "/evals/" + this.gameVariant.getNameGame() + "/audio/" + nameSound;
        gameContext.getSoundManager().add(this.actualSound);
    }

    public void replaySound(){
        if ((boolean) this.allScreens.get(this.indexFileImage).get(9)){
            gameContext.getSoundManager().add(this.actualSound);
        }
    }

    public void stopTransitionTimeline(){
        log.info("Stop transition timeline");
        if (this.transitionScreenT != null){
            this.transitionScreenT.stop();
        }
    }

    public void stopInstructionTimeline(){
        log.info("Stop instruction timeline");
        if (this.instructionScreenT != null){
            this.instructionScreenT.stop();
        }
    }

    public void stopStimuliTimeline(){
        log.info("Stop stimuli timeline");
        if (this.stimuliScreenT != null){
            this.stimuliScreenT.stop();
        }
    }
    
    public void increaseIndex(){
        this.indexFileImage++;
    }

    public void generateStimuliScreen(int rows, int cols, double fixaTime, String nameSound){
        this.typeScreen = "stimuli";
        this.posX = 0;
        this.posY = 0;

        final GameSizing gameSizing = new GameSizingComputer(rows, cols, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        List<String> images = this.getImages();

        for (int i=0; i<(rows*cols); i++){
            gameContext.getChildren().add(new PictureCard(
                    gameSizing.width * posX,
                    gameSizing.height * posY + 10,
                    gameSizing.width,
                    gameSizing.height -10,
                    gameContext,
                    gameVariant,
                    images.get(i),
                    fixaTime,
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
        if ((boolean) this.allScreens.get(this.indexFileImage).get(9)){
            this.playSound(nameSound);
        }
    }

    public List<String> getImages(){
        List<String> images = new ArrayList<>();
        int nbImages = (Integer) this.allScreens.get(this.indexFileImage).get(1) * (Integer) this.allScreens.get(this.indexFileImage).get(2);
        int startIndex = this.allScreens.get(this.indexFileImage).size() - nbImages;
        for (int i=startIndex; i<this.allScreens.get(this.indexFileImage).size(); i++){
            images.add((String) this.allScreens.get(this.indexFileImage).get(i));
        }

        if ((Boolean) this.allScreens.get(this.indexFileImage).get(7)){
            Collections.shuffle(images);
        }

        return images;
    }

    public void generateInstructionScreen(String imageName, boolean setFixaImg, int timeImg){
        this.typeScreen = "instruction";
        final GameSizing gameSizing = new GameSizingComputer(1, 1, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        gameContext.getChildren().add(new ScreenCard(
            0,
            10,
            gameSizing.width,
            gameSizing.height-10,
            gameContext,
            gameVariant,
            imageName,
            this,
            "instruction",
            true,
            setFixaImg,
            timeImg
        ));
    }

    public void generateCrossFixationScreen(boolean setFixaCross, int timeCross){
        this.typeScreen = "transition";

        final GameSizing gameSizing = new GameSizingComputer(1, 1, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        gameContext.getChildren().add(new ScreenCard(
            0,
            10,
            gameSizing.width,
            gameSizing.height-10,
            gameContext,
            gameVariant,
            "blackCrossMini.png",
            this,
            "transition",
            true,
            setFixaCross,
            timeCross
        ));
    }

    public Boolean isFirstPosition(){
        return this.posX==0;
    }

    public void startTimer(){
        if (this.indexFileImage == 0){
            currentRoundStartTime = System.currentTimeMillis();
        }
    }

    /*public void startGetGazePosition(){
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

    public void pauseDisplayDuration(){
        log.info("Pause timeline DD");
        this.createDisplayDuration.pause();
    }

    public void continueDisplayDuration(){
        log.info("Continue timeline DD");
        this.createDisplayDuration.play();
    }

    public void stopDisplayDuration(){
        log.info("Stop timeline DD");
        this.createDisplayDuration.stop();
    }*/

    public void goToStats(){
        gameContext.showRoundStats(stats, this);
    }

    public boolean increaseIndexFileImage() {
        this.indexFileImage = this.indexFileImage + 1;
        if (this.indexFileImage == this.indexEndGame){
            return true;
        }else {
            return false;
        }
    }

    public void calculScores(String name){
        if (name.contains("C_")){
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
        //this.getGazePositionXY.stop();
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
        createTxtFile();
        createExcelFile();
    }

    public void createTxtFile(){
        File evalTraining = new File(this.pathStatsGame, "Eval_" + DateUtils.today() + ".txt");
        StringBuilder content = new StringBuilder();

        for (int i=0; i<this.listImages.length; i++){
            content.append("Planche ").append(i + 1).append(" :").append(System.lineSeparator());
            content.append(System.lineSeparator());
            content.append("Image en haut gauche : ").append(this.listImages[i][0]).append(System.lineSeparator());
            content.append("Image en haut droite : ").append(this.listImages[i][1]).append(System.lineSeparator());
            content.append("Image en bas gauche : ").append(this.listImages[i][2]).append(System.lineSeparator());
            content.append("Image en bas droite : ").append(this.listImages[i][3]).append(System.lineSeparator());
            content.append("Son : ").append(this.listSounds[i]).append(System.lineSeparator());
            content.append(System.lineSeparator());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(evalTraining))){
            writer.write(content.toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("PMD")
    public void createExcelFile(){

        String pathStats = this.pathStatsGame  + "/Stats_" + DateUtils.today() + ".xlsx";
        this.stats.actualFile = this.pathStatsGame  + "/Stats_" + DateUtils.today() + ".xlsx";

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet(this.gameName);

        Object[][] bookData = new Object[this.countStats+1][8];

        bookData[0][0] = "Timepoint";
        bookData[0][1] = "Coordonnées X";
        bookData[0][2] = "Coordonnées Y";
        bookData[0][3] = "Img ID";
        bookData[0][4] = "Img HG";
        bookData[0][5] = "Img HD";
        bookData[0][6] = "Img BG";
        bookData[0][7] = "Img BD";

        String nameTimepoint = "";
        int countTimepoint = 1;
        for (int i=1; i<this.countStats; i++){

            if (!nameTimepoint.equals(this.idImg.get(i - 1))){
                nameTimepoint = this.idImg.get(i - 1);
                countTimepoint = 1;
            }

            bookData[i][0] = String.valueOf(countTimepoint);
            bookData[i][1] = String.valueOf(this.listGazePositionX.get(i-1));
            bookData[i][2] = String.valueOf(this.listGazePositionY.get(i-1));
            bookData[i][3] = this.idImg.get(i-1);
            bookData[i][4] = this.posImgHG.get(i-1);
            bookData[i][5] = this.posImgHD.get(i-1);
            bookData[i][6] = this.posImgBG.get(i-1);
            bookData[i][7] = this.posImgBD.get(i-1);
            countTimepoint++;
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

        try (FileOutputStream outputStream = new FileOutputStream(pathStats)) {
            log.info("Excel file created !");
            workbook.write(outputStream);
        } catch (Exception e){
            log.info("Error creation xls for GazePlay Eval stats game !");
            e.printStackTrace();
        }
    }

    private class CustomInputEventHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent key) {
            if (key.getCode().equals(KeyCode.SPACE)) {
                stopTransitionTimeline();
                stopInstructionTimeline();
                getScreenHeatmapGaze();
                clearScreen();
                increaseIndex();
                generateScreen();
            } else if (key.getCode().equals(KeyCode.P)) {
                replaySound();
            }
        }
    }
}
