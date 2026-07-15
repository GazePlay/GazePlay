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
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    public boolean ignoreAnyInput = true;

    public List<List<Object>> allScreens;
    public Timeline transitionScreenT;
    public Timeline instructionScreenT;
    public Timeline stimuliScreenT;
    public Timeline delayBeforeSelectionT;
    public MediaPlayer mediaPlayerVideo;
    public int maxItemSelected;
    public int nbItemSelected = 0;
    public String actualSound;
    public Text instructionText;
    private Workbook workbook;
    private Sheet sheet;
    private CellStyle styleWorkbook;
    private int rowIndexExcel = 0;
    private String pathFileExcel;
    private String pathFileCsv;
    private MediaPlayer player;
    private Boolean previousGoodAnswer;
    private ArrayList<String> statsListStimuli = new ArrayList<>();
    public StringBuilder listStimuli = new StringBuilder();
    private ArrayList<String> statsListStimuliAnswer = new ArrayList<>();
    public StringBuilder listStimuliAnswer = new StringBuilder();
    public ArrayList<String> statsListStimuliAnswerGiven = new ArrayList<>();
    public StringBuilder listStimuliAnswerGiven = new StringBuilder();
    private ArrayList<String> statsListStimuliTimer = new ArrayList<>();
    private ArrayList<String> screenId = new ArrayList<>();
    private List<List<Object>> imagesAndSounds = new ArrayList<>();
    private boolean replayScreen = false;

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
        this.generateStatsFolder();
        //this.createExcelFile();
        this.createCsvFile();
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
        row.add(obj.get("Choix de sélection").getAsString());
        row.add(obj.get("Combien à sélectionner").getAsInt());
        row.add(obj.get("Position stimuli aléatoire").getAsBoolean());
        row.add(obj.get("Caché stimuli après selection").getAsBoolean());
        row.add(obj.get("Mettre un son").getAsBoolean());
        row.add(obj.get("Nom du fichier").getAsString());

        JsonObject stimuli = obj.getAsJsonObject("Liste des stimuli");

        stimuli.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e ->{
                row.add(e.getValue()
                    .getAsJsonObject()
                    .get("imageName")
                    .getAsString());

                row.add(e.getValue().
                    getAsJsonObject().
                    get("soundName").
                    getAsString());

                row.add(e.getValue().
                    getAsJsonObject().
                    get("goodAnswer").
                    getAsBoolean());
            }
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
        this.pathFileExcel = this.pathStatsGame  + "/Stats_" + DateUtils.today() + ".xlsx";
        this.pathFileCsv = this.pathStatsGame  + "/Stats_" + DateUtils.today() + ".csv";
        this.countStats = 0;
    }

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
        this.afterScreenHeatMap();
    }

    public void afterScreenHeatMap(){
        this.clearScreen();
        this.increaseIndex();
        this.statsListStimuliTimer.add(String.valueOf(System.currentTimeMillis() - currentRoundStartTime));
        this.checkOptionsEval();
    }

    @Override
    public void launch() {
        //this.startTimer();

        this.nbImageSee = 0;
        this.canRemoveItemManually = true;

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();

        this.generateScreen();
    }

    public boolean checkAllPictureCardChecked(boolean goodAnswer) {
        log.info("checkAllPictureCardChecked");
        log.info("value = {}", this.allScreens.get(this.indexFileImage).get(6));
        if (Objects.equals(this.allScreens.get(this.indexFileImage).get(6), "Tout")){
            log.info("Tout");
            this.nbItemSelected++;
        }else {
            log.info("Selection bonne réponse uniquement");
            if (goodAnswer){
                log.info("goodAnswer");
                this.nbItemSelected++;
            }
        }

        if (this.nbItemSelected == this.maxItemSelected){
            this.statsListStimuliAnswerGiven.add(this.listStimuliAnswerGiven.toString());
            this.listStimuliAnswerGiven.setLength(0);
        }

        log.info(String.valueOf(this.nbItemSelected == this.maxItemSelected));
        return this.nbItemSelected == this.maxItemSelected;
    }

    public boolean lastGoodPictureCard(boolean goodAnswer){
        if (Objects.equals(this.allScreens.get(this.indexFileImage).get(6), "Tout")){
            return (this.nbItemSelected + 1) == this.maxItemSelected;
        }else {
            if (goodAnswer){
                return (this.nbItemSelected + 1) == this.maxItemSelected;
            }else {
                return false;
            }
        }
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
        Configuration config = ActiveConfigurationContext.getInstance();

        gameContext.getSoundManager().stop();
        gameContext.getSoundManager().clear();
        this.stopMediaPlayerVideo();

        log.info("index file image = {}", this.indexFileImage);
        log.info("screen size = {}", this.allScreens.size());
        log.info("Eval options = {}", config.getEvalOptions());

        if (this.indexFileImage >= this.allScreens.size()){
            this.dispose();
            this.gameContext.clear();
            //this.closeExcelFile();
            this.closeCsvFile();
            this.gameContext.showRoundStats(stats, this);
        }else {
            String type = this.allScreens.get(this.indexFileImage).get(0).toString();
            this.ignoreAnyInput = true;

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
                        String videoPath = Paths.get(
                            config.getFileDir() +
                                "\\evals\\" +
                                this.gameVariant.getNameGame() +
                                "\\videos\\" +
                                this.allScreens.get(this.indexFileImage).get(5)
                        ).toUri().toString();

                        Media media = new Media(videoPath);
                        this.mediaPlayerVideo = new MediaPlayer(media);
                        MediaView mediaView = new MediaView(this.mediaPlayerVideo);

                        mediaView.setPreserveRatio(true);

                        Dimension2D dim = gameContext.getGamePanelDimensionProvider().getDimension2D();
                        mediaView.setFitWidth(dim.getWidth() * 0.8);
                        mediaView.setFitHeight(dim.getHeight() * 0.8);

                        StackPane centerPane = new StackPane(mediaView);
                        centerPane.setPrefSize(dim.getWidth(), dim.getHeight());
                        StackPane.setAlignment(mediaView, Pos.CENTER);

                        gameContext.getChildren().add(centerPane);

                        this.mediaPlayerVideo.setOnReady(() -> {
                            this.mediaPlayerVideo.play();
                        });
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
                this.maxItemSelected = (Integer) this.allScreens.get(this.indexFileImage).get(7);
                this.nbItemSelected = 0;
                this.generateStimuliScreen(
                    (Integer) this.allScreens.get(this.indexFileImage).get(1),
                    (Integer) this.allScreens.get(this.indexFileImage).get(2),
                    ((Number) this.allScreens.get(this.indexFileImage).get(5)).doubleValue(),
                    (String) this.allScreens.get(this.indexFileImage).get(11)
                    );
                if ((boolean) this.allScreens.get(this.indexFileImage).get(3)){
                    this.stimuliScreenT = new Timeline(new KeyFrame(Duration.seconds(((Number) this.allScreens.get(this.indexFileImage).get(4)).doubleValue()), event -> {

                        if (this.listStimuliAnswerGiven.toString().isEmpty()){
                            this.listStimuliAnswerGiven.append(" ");
                        }
                        this.statsListStimuliAnswerGiven.add(this.listStimuliAnswerGiven.toString());
                        this.listStimuliAnswerGiven.setLength(0);

                        this.statsListStimuliTimer.add(String.valueOf(this.allScreens.get(this.indexFileImage).get(4)));

                        this.stats.screenHeatMapGaze(this.pathStatsGame);
                        this.clearScreen();
                        this.increaseIndex();
                        this.checkOptionsEval();
                        log.info("Stimuli screen timeout");
                    }));
                    this.stimuliScreenT.setCycleCount(1);
                    this.stimuliScreenT.playFromStart();
                }
                this.currentRoundStartTime = System.currentTimeMillis();
            } else {
                this.dispose();
                this.gameContext.clear();
                this.gameContext.showRoundStats(stats, this);
            }

            if (config.getDelayBeforeSelectionTime() > 0) {
                log.info("Delay before selection time: " + config.getDelayBeforeSelectionTime());
                this.delayBeforeSelectionT = new Timeline(new KeyFrame(Duration.millis(config.getDelayBeforeSelectionTime()), event -> {
                    this.ignoreAnyInput = false;
                }));
                this.delayBeforeSelectionT.setCycleCount(1);
                this.delayBeforeSelectionT.playFromStart();
            }else {
                this.ignoreAnyInput = false;
            }
        }
    }

    public void playSound(String nameSound){
        Configuration config = ActiveConfigurationContext.getInstance();
        this.actualSound = config.getFileDir() + "/evals/" + this.gameVariant.getNameGame() + "/audio/" + nameSound;
        gameContext.getSoundManager().add(this.actualSound);
    }

    public void playSoundImage(String nameSound, boolean goodAnswer){
        Configuration config = ActiveConfigurationContext.getInstance();

        if (Objects.equals(nameSound, "")){
            if (this.checkAllPictureCardChecked(goodAnswer)){
                this.stopStimuliTimeline();
                this.getScreenHeatmapGaze();
            }
        }else {

            if (this.player != null){
                this.player.stop();
                this.player.dispose();

                if (this.lastGoodPictureCard(goodAnswer)){
                    this.ignoreAnyInput = true;
                }else {
                    if (this.checkAllPictureCardChecked(previousGoodAnswer)){
                        this.stopStimuliTimeline();
                        this.getScreenHeatmapGaze();
                    }
                }
            }

            this.previousGoodAnswer = goodAnswer;

            File soundFile = new File(
                config.getFileDir() + "/evals/"
                    + this.gameVariant.getNameGame()
                    + "/audio/" + nameSound
            );

            Media media = new Media(soundFile.toURI().toString());
            this.player = new MediaPlayer(media);

            player.setOnEndOfMedia(() -> {
                this.player.dispose();
                this.player = null;
                this.ignoreAnyInput = false;

                if (this.checkAllPictureCardChecked(goodAnswer)){
                    this.stopStimuliTimeline();
                    this.getScreenHeatmapGaze();
                }
            });

            this.player.setOnReady(() -> {
                this.player.play();
            });
        }
    }

    public void disableSelectionWithSound(String nameSound, boolean goodAnswer){
        Configuration config = ActiveConfigurationContext.getInstance();
        this.ignoreAnyInput = true;
        this.pauseStimuliTimeline();

        if (Objects.equals(nameSound, "")){
            this.ignoreAnyInput = false;
            if (this.checkAllPictureCardChecked(goodAnswer)){
                this.stopStimuliTimeline();
                this.getScreenHeatmapGaze();
            }else {
                this.playStimuliTimeline();
            }
        }else {
            File soundFile = new File(
                config.getFileDir() + "/evals/"
                    + this.gameVariant.getNameGame()
                    + "/audio/" + nameSound
            );

            Media media = new Media(soundFile.toURI().toString());
            this.player = new MediaPlayer(media);

            this.player.setOnEndOfMedia(() -> {
                this.player.dispose();
                this.ignoreAnyInput = false;

                if (this.checkAllPictureCardChecked(goodAnswer)){
                    this.stopStimuliTimeline();
                    this.getScreenHeatmapGaze();
                }else {
                    this.playStimuliTimeline();
                }
            });

            this.player.setOnReady(() -> {
                this.player.play();
            });
        }
    }

    public void replaySound(){
        if ((boolean) this.allScreens.get(this.indexFileImage).get(10)){
            gameContext.getSoundManager().add(this.actualSound);
        }
    }

    public boolean isSelectionDisabled(){
        Configuration config = ActiveConfigurationContext.getInstance();
        return config.isSoundEnabled();
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

    public void stopMediaPlayerVideo(){
        if (this.mediaPlayerVideo != null){
            this.mediaPlayerVideo.stop();
            this.mediaPlayerVideo.dispose();
            this.mediaPlayerVideo = null;
        }
    }

    public void stopStimuliTimeline(){
        log.info("Stop stimuli timeline");
        if (this.stimuliScreenT != null){
            this.stimuliScreenT.stop();
        }
    }

    public void pauseStimuliTimeline(){
        if (this.stimuliScreenT != null){
            this.stimuliScreenT.pause();
        }
    }

    public void playStimuliTimeline(){
        if (this.stimuliScreenT != null){
            this.stimuliScreenT.play();
        }
    }

    public void stopDelayTimeline(){
        log.info("Stop delay timeline");
        if (this.delayBeforeSelectionT != null){
            this.delayBeforeSelectionT.stop();
        }
    }
    
    public void increaseIndex(){
        this.indexFileImage++;
    }

    public void decreaseIndex(){
        this.indexFileImage--;
        this.replayScreen = true;
    }

    public void checkOptionsEval(){
        Configuration config = ActiveConfigurationContext.getInstance();
        String evalOptionsValue = config.getEvalOptions();

        if (evalOptionsValue.equals("Classique")){
            this.generateScreen();
        }else {
            this.generateEvalOptionsScreen(evalOptionsValue);
        }
    }

    public void generateEvalOptionsScreen(String evalOptionsValue){
        this.typeScreen = "evalOptions";

        new EvalOptionsCard(evalOptionsValue, gameContext, this);
    }

    public void generateStimuliScreen(int rows, int cols, double fixaTime, String nameSound){
        this.typeScreen = "stimuli";
        this.posX = 0;
        this.posY = 0;

        final GameSizing gameSizing = new GameSizingComputer(rows, cols, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        List<List<Object>> imagesAndSounds = this.getImages();

        for (int i=0; i<(rows*cols); i++){
            gameContext.getChildren().add(new PictureCard(
                gameSizing.width * posX,
                gameSizing.height * posY + 10,
                gameSizing.width,
                gameSizing.height -10,
                gameContext,
                gameVariant,
                (String) imagesAndSounds.get(i).get(0),
                (String) imagesAndSounds.get(i).get(1),
                (boolean) imagesAndSounds.get(i).get(2),
                fixaTime,
                stats,
                this,
                this.isFirstPosition()
            ));

            targetAOIList.add(new TargetAOI(
                gameSizing.width * posX,
                gameSizing.height * posY,
                (int) gameSizing.height,
                System.currentTimeMillis()
            ));

            this.incrementPos();

            listStimuli.append(checkImageName((String) imagesAndSounds.get(i).get(0))).append("\n");

            if ((boolean) imagesAndSounds.get(i).get(2)){
                listStimuliAnswer.append(checkImageName((String) imagesAndSounds.get(i).get(0))).append("\n");
            }
        }

        if ((boolean) this.allScreens.get(this.indexFileImage).get(10)){
            this.playSound(nameSound);
        }

        this.screenId.add(Integer.toString(this.indexFileImage));
        this.statsListStimuli.add(listStimuli.toString());
        this.statsListStimuliAnswer.add(listStimuliAnswer.toString());

        this.listStimuli.setLength(0);
        this.listStimuliAnswer.setLength(0);
    }

    public String checkImageName(String nameImage){
        if (Objects.equals(nameImage, "")){
            return "image_vide";
        }else {
            return nameImage;
        }
    }

    public List<List<Object>> getImages(){
        if (this.replayScreen){
            this.replayScreen = false;
        }else {
            this.imagesAndSounds = new ArrayList<>();
            int nbImages = (Integer) this.allScreens.get(this.indexFileImage).get(1) * (Integer) this.allScreens.get(this.indexFileImage).get(2);
            int startIndex = this.allScreens.get(this.indexFileImage).size() - (nbImages*3);
            for (int i=startIndex; i<this.allScreens.get(this.indexFileImage).size(); i+=3){
                this.imagesAndSounds.add(new ArrayList<>(List.of(
                    (String) this.allScreens.get(this.indexFileImage).get(i),
                    (String) this.allScreens.get(this.indexFileImage).get(i+1),
                    (boolean) this.allScreens.get(this.indexFileImage).get(i+2)
                )));
            }

            if ((boolean) this.allScreens.get(this.indexFileImage).get(8)){
                Collections.shuffle(this.imagesAndSounds);
            }
        }

        return this.imagesAndSounds;
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

        final String backgroundStyle = gameContext.getConfiguration().getBackgroundStyle()
            .accept(new BackgroundStyleVisitor<>() {
                @Override public String visitLight() { return "blackCrossMini.png"; }
                @Override public String visitDark()  { return "whiteCrossMini.png"; }
            });

        gameContext.getChildren().add(new ScreenCard(
            0,
            10,
            gameSizing.width,
            gameSizing.height-10,
            gameContext,
            gameVariant,
            backgroundStyle,
            this,
            "transition",
            true,
            setFixaCross,
            timeCross
        ));
    }

    public boolean isFirstPosition(){
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

    @SuppressWarnings("PMD")
    public void createExcelFile(){
        log.info("Create excel file");
        this.stats.actualFile = this.pathStatsGame  + "/Stats_" + DateUtils.today() + ".xlsx";

        this.workbook = new XSSFWorkbook();
        this.sheet = this.workbook.createSheet("Résultats");
        this.styleWorkbook = this.workbook.createCellStyle();
        this.styleWorkbook.setWrapText(true);

        // En-têtes
        Row header = sheet.createRow(this.rowIndexExcel++);

        header.createCell(0).setCellValue("Stimuli");
        header.createCell(1).setCellValue("ID écran");
        header.createCell(2).setCellValue("Réponse attendue");
        header.createCell(3).setCellValue("Réponse donnée");
        header.createCell(4).setCellValue("Temps écran (ms)");
    }

    public void addLineExcel() {

        Row row;

        for (int i = 0; i < this.statsListStimuli.size(); i++) {
            row = this.sheet.createRow(this.rowIndexExcel++);
            row.createCell(0).setCellValue(this.statsListStimuli.get(i));
            row.createCell(1).setCellValue(this.screenId.get(i));
            row.createCell(2).setCellValue(this.statsListStimuliAnswer.get(i));
            row.createCell(3).setCellValue(this.statsListStimuliAnswerGiven.get(i));
            row.createCell(4).setCellValue(this.statsListStimuliTimer.get(i));

            row.setHeight((short) -1);
        }
    }

    public void closeExcelFile() {

        log.info("Try Close excel file at -> " + this.pathFileExcel);

        try (FileOutputStream fileOut = new FileOutputStream(this.pathFileExcel)) {

            log.info("File created !");
            this.addLineExcel();

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            this.workbook.write(fileOut);
            this.workbook.close();

            log.info("File created !");

        } catch (IOException e) {
            log.info("Error close excel file -> " + e.getMessage());
        }
    }

    public void createCsvFile() {

        log.info("Create csv file");

        this.stats.actualFile = this.pathStatsGame
            + "/Stats_"
            + DateUtils.today()
            + ".csv";
    }

    public void closeCsvFile() {

        log.info("Try create csv file -> {}", this.pathFileCsv);

        CSVFormat format = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')                 // Compatible Excel FR
            .setRecordSeparator(System.lineSeparator())
            .setHeader(
                "Stimuli",
                "ID écran",
                "Réponse attendue",
                "Réponse donnée",
                "Temps écran (ms)")
            .build();

        try (OutputStream os = Files.newOutputStream(Path.of(this.pathFileCsv))) {

            // BOM UTF-8 pour Microsoft Excel
            os.write(0xEF);
            os.write(0xBB);
            os.write(0xBF);

            try (Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                 CSVPrinter printer = new CSVPrinter(writer, format)) {

                for (int i = 0; i < this.statsListStimuli.size(); i++) {

                    printer.printRecord(
                        this.statsListStimuli.get(i),
                        this.screenId.get(i),
                        this.statsListStimuliAnswer.get(i),
                        this.statsListStimuliAnswerGiven.get(i),
                        this.statsListStimuliTimer.get(i)
                    );
                }

                printer.flush();
            }

            log.info("CSV created!");

        } catch (IOException e) {
            log.error("Error creating csv file", e);
        }
    }

    private class CustomInputEventHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent key) {
            if (key.getCode().equals(KeyCode.SPACE)) {
                stopTransitionTimeline();
                stopInstructionTimeline();
                stopStimuliTimeline();
                stopDelayTimeline();
                getScreenHeatmapGaze();
                afterScreenHeatMap();
            } else if (key.getCode().equals(KeyCode.P)) {
                replaySound();
            }
        }
    }
}
