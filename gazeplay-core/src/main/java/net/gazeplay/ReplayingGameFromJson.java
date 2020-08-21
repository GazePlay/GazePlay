package net.gazeplay;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.scenes.configuration.ConfigurationContext;
import net.gazeplay.ui.scenes.ingame.GameContext;
import net.gazeplay.ui.scenes.ingame.GameContextFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

public class ReplayingGameFromJson {
    static List<GameSpec> gamesList;
    private static ApplicationContext applicationContext;
    private static GameContext gameContext;
    private static GazePlay gazePlay;
    private static double currentGameSeed;
    private static String currentGameNameCode;
    private static String currentGameVariant;
    private static GameSpec selectedGameSpec;
    private static IGameVariant gameVariant;
    private static JsonArray coordinatesAndTimeStamp;
    private static LinkedList<FixationPoint> fixationSequence;
    private static  int nbGoalsReached;
    private static  int nbGoalsToReach;
    private static  int nbUnCountedGoalsReached;
    private static LifeCycle lifeCycle;
    private static RoundsDurationReport roundsDurationReport;
    private static SavedStatsInfo savedStatsInfo;
    private static int x0, y0;
    private static int nextX, nextY, nextTime, prevTime;
    private static int delay = 0; // refresh rate
    private static boolean first = true;

    public ReplayingGameFromJson(GazePlay gazePlay, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.gazePlay = gazePlay;
        gameContext = applicationContext.getBean(GameContext.class);
    }

    public static void setGameList(List<GameSpec> games){
        gamesList = games;
    }

    public static void pickJSONFile() throws FileNotFoundException {
        final String fileName = getFileName();
        if (fileName == null) {
            return;
        }
        final File replayDataFile = new File(fileName);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(replayDataFile));
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
        final File gazeMetricsFile = new File(filePrefix + "-metrics.png");
        final File heatMapCsvFile = new File(filePrefix + "-heatmap.csv");
        final File screenShotFile = new File(filePrefix + "-screenshot.png");
        final File colorBandsFile = new File(filePrefix + "-colorBands.png");
        savedStatsInfo = new SavedStatsInfo(heatMapCsvFile, gazeMetricsFile, screenShotFile,
            colorBandsFile, replayDataFile);

        replayGame();
    }

    public static String getFileName() {
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

    public static void replayGame(){
        ConfigurationContext configContext = applicationContext.getBean(ConfigurationContext.class);
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

    private static void drawFixationLines(Canvas canvas, JsonArray coordinatesAndTimeStamp) {
        final GraphicsContext graphics = canvas.getGraphicsContext2D();
        for (JsonElement coordinateAndTimeStamp : coordinatesAndTimeStamp) {
            prevTime = nextTime;
            JsonObject coordinateAndTimeObj = coordinateAndTimeStamp.getAsJsonObject();
            nextX = Integer.parseInt(coordinateAndTimeObj.get("X").getAsString());
            nextY = Integer.parseInt(coordinateAndTimeObj.get("Y").getAsString());
            nextTime = Integer.parseInt(coordinateAndTimeObj.get("time").getAsString());
            delay = nextTime - prevTime;
            paint(graphics, canvas);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
    }

    public static void paint(GraphicsContext graphics, Canvas canvas) {
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphics.setStroke(javafx.scene.paint.Color.RED);
        graphics.strokeOval(nextX, nextY, 50, 50);
        graphics.setFill(javafx.scene.paint.Color.rgb(255, 255, 0, 0.5));
        graphics.fillOval(nextX, nextY, 50, 50);

        Point2D point = new Point2D(nextX, nextY);
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
    private LinkedList<FixationPoint> fixationSequence;
}
