package net.gazeplay;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.FileChooser;
import lombok.Getter;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.scenes.ingame.GameContext;
import org.springframework.context.ApplicationContext;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ReplayingGameFromJson {
    List<GameSpec> gamesList;
    private ApplicationContext applicationContext;
    private GameContext gameContext;
    private GazePlay gazePlay;
    private double currentGameSeed;
    private String currentGameNameCode;
    private String currentGameVariant;
    private GameSpec selectedGameSpec;
    private IGameVariant gameVariant;
    private JsonArray coordinatesAndTimeStamp;
    private ArrayList<LinkedList<FixationPoint>> fixationSequence;
    private int nbGoalsReached;
    private int nbGoalsToReach;
    private int nbUnCountedGoalsReached;
    private LifeCycle lifeCycle;
    private RoundsDurationReport roundsDurationReport;
    private SavedStatsInfo savedStatsInfo;
    private int nextX, nextY, nextTime, prevTime;
    private int delay = 0; // refresh rate

    public ReplayingGameFromJson(GazePlay gazePlay, ApplicationContext applicationContext, List<GameSpec> games) {
        this.applicationContext = applicationContext;
        this.gazePlay = gazePlay;
        this.gameContext = applicationContext.getBean(GameContext.class);
        this.gamesList = games;
    }

    public void setGameList(List<GameSpec> games){
        gamesList = games;
    }

    public void pickJSONFile() throws IOException {
        final String fileName = getFileName();
        if (fileName == null) {
            return;
        }

        final File replayDataFile = new File(fileName);

        InputStream in = Files.newInputStream(Paths.get(fileName));
        JSONTokener tokener = new JSONTokener(in);
        JSONObject object = new JSONObject(tokener);

        try (InputStream inputStream = ReplayingGameFromJson.class.getResourceAsStream("JSON-schema-replayData.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(object); // throws a ValidationException if this object is invalid
        } catch (ValidationException e){
            e.printStackTrace();
        }

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = Files.newBufferedReader(replayDataFile.toPath(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert bufferedReader != null;
        Gson gson = new Gson();
        JsonFile json = gson.fromJson(bufferedReader, JsonFile.class);
        currentGameSeed = json.getGameSeed();
        currentGameNameCode = json.getGameName();
        currentGameVariant = json.getGameVariant();
        coordinatesAndTimeStamp = json.getCoordinatesAndTimeStamp();
        fixationSequence = json.getFixationSequence();
        nbGoalsReached = json.getStatsNbGoalsReached();
        nbGoalsToReach = json.getStatsNbGoalsToReach();
        nbUnCountedGoalsReached = json.getStatsNbUnCountedGoalsReached();
        lifeCycle = json.getLifeCycle();
        roundsDurationReport = json.getRoundsDurationReport();
        String filePrefix = fileName.substring(0, fileName.lastIndexOf("-"));
        final File gazeMetricsMouseFile = new File(filePrefix + "-metricsMouse.png");
        final File gazeMetricsGazeFile = new File(filePrefix + "-metricsGaze.png");
        final File gazeMetricsMouseAndGazeFile = new File(filePrefix + "-metricsMouseAndGaze.png");
        final File heatMapCsvFile = new File(filePrefix + "-heatmap.csv");
        final File screenShotFile = new File(filePrefix + "-screenshot.png");
        final File colorBandsFile = new File(filePrefix + "-colorBands.png");
        savedStatsInfo = new SavedStatsInfo(heatMapCsvFile, gazeMetricsMouseFile, gazeMetricsGazeFile, gazeMetricsMouseAndGazeFile, screenShotFile,
            colorBandsFile, replayDataFile);

        replayGame();
    }

    public String getFileName() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JSON files", "*.json")
        );
        File selectedFile = fileChooser.showOpenDialog(gameContext.getPrimaryStage());
        String selectedFileName = null;
        if (selectedFile != null) {
            selectedFileName = selectedFile.getAbsolutePath();
        }
        return selectedFileName;
    }

    public void replayGame(){
        gameContext = applicationContext.getBean(GameContext.class);
        gazePlay.onGameLaunch(gameContext);
        for (GameSpec gameSpec : gamesList) {
            if (currentGameNameCode.equals(gameSpec.getGameSummary().getNameCode())){
                selectedGameSpec = gameSpec;
            }
        }
        final Translator translator = gazePlay.getTranslator();
        for (IGameVariant variant : selectedGameSpec.getGameVariantGenerator().getVariants()){
            if (currentGameVariant.equals(variant.getLabel(translator))){
                gameVariant = variant;
            }
        }
        IGameLauncher gameLauncher = selectedGameSpec.getGameLauncher();
        final Scene scene = gazePlay.getPrimaryScene();
        final Stats statsSaved = gameLauncher.createSavedStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        GameLifeCycle currentGame = gameLauncher.replayGame(gameContext, gameVariant, statsSaved, currentGameSeed);
        gameContext.createControlPanel(gazePlay, statsSaved, currentGame, "replay");
        gameContext.createQuitShortcut(gazePlay, statsSaved, currentGame);
        currentGame.launch();

        final Dimension2D screenDimension = gameContext.getCurrentScreenDimensionSupplier().get();
        //Drawing in canvas
        final javafx.scene.canvas.Canvas canvas = new Canvas(screenDimension.getWidth(), screenDimension.getHeight());
        gameContext.getChildren().add(canvas);

        Service<Void> loadingService = new Service<Void>(){
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        drawFixationLines(canvas, coordinatesAndTimeStamp);
                        return null;
                    }
                };
            };
        };

        loadingService.setOnSucceeded( e -> {
            loadingService.reset();
            gameContext.exitGame(statsSaved, gazePlay, currentGame, "replay");
        });

        loadingService.setOnFailed(e -> {
            loadingService.restart();
        });

        loadingService.setOnCancelled(e -> {
        });

        if (!loadingService.isRunning()) {
            loadingService.start();
        }
    }

    private void drawFixationLines(Canvas canvas, JsonArray coordinatesAndTimeStamp) {
        Dimension2D dimensions = gameContext.getCurrentScreenDimensionSupplier().get();
        int screenWidth = (int) dimensions.getWidth();
        int screenHeight = (int) dimensions.getHeight();
        final GraphicsContext graphics = canvas.getGraphicsContext2D();
        for (JsonElement coordinateAndTimeStamp : coordinatesAndTimeStamp) {
            prevTime = nextTime;
            JsonObject coordinateAndTimeObj = coordinateAndTimeStamp.getAsJsonObject();
            nextX =  (int)(Float.parseFloat(coordinateAndTimeObj.get("X").getAsString()) * screenWidth);
            nextY =  (int)(Float.parseFloat(coordinateAndTimeObj.get("Y").getAsString()) * screenHeight);
            nextTime = Integer.parseInt(coordinateAndTimeObj.get("time").getAsString());
            delay = nextTime - prevTime;
            paint(graphics, canvas);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
    }

    public void paint(GraphicsContext graphics, Canvas canvas) {
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphics.setStroke(javafx.scene.paint.Color.RED);
        graphics.strokeOval(nextX - 25, nextY - 25, 50, 50);
        graphics.setFill(javafx.scene.paint.Color.rgb(255, 255, 0, 0.5));
        graphics.fillOval(nextX - 25, nextY - 25, 50, 50);

        Point2D point = new Point2D((int) gameContext.getPrimaryStage().getX() + nextX, (int) gameContext.getPrimaryStage().getY() + nextY);
        gameContext.getGazeDeviceManager().onSavedMovementsUpdate(point);
    }

}
class JsonFile {
    @Getter
    private double gameSeed;
    @Getter
    private String gameName;
    @Getter
    private String gameVariantClass;
    @Getter
    private String gameVariant;
    @Getter
    private double gameStartedTime;
    @Getter
    private String screenAspectRatio;
    @Getter
    private JsonArray coordinatesAndTimeStamp;
    @Getter
    private LifeCycle lifeCycle;
    @Getter
    private int statsNbGoalsReached;
    @Getter
    private int statsNbGoalsToReach;
    @Getter
    private int statsNbUnCountedGoalsReached;
    @Getter
    private RoundsDurationReport roundsDurationReport;
    @Getter
    private ArrayList<LinkedList<FixationPoint>> fixationSequence;
}
