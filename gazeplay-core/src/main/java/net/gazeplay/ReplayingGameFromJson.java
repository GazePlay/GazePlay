package net.gazeplay;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;
import net.gazeplay.ui.scenes.ingame.GameContext;
import net.gazeplay.ui.scenes.loading.LoadingContext;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReplayingGameFromJson {

    private final LinkedList<Point2D> lastGazeCoordinates = new LinkedList<>();
    private final LinkedList<Point2D> lastMouseCoordinates = new LinkedList<>();
    int numberOfElementToDisplay = 10;

    private static final ArrayList<String> replayableGameList = new ArrayList<>(
        Arrays.asList(
            //OK: tiny offsets problems
            "Scribble", "Cakes", "Creampie", "WhereIsIt", "WhereIsTheAnimal", "letters",
            "WhereIsTheColor", "WhereIsTheLetter", "WhereIsTheNumber", "findodd", "flags",
            "ScratchCard", "Memory", "MemoryLetters", "MemoryNumbers", "OpenMemory", "OpenMemoryLetters",
            "OpenMemoryNumbers", "Dice", "EggGame", "PersonalizeEggGame", "GooseGame", "MagicCards", "Opinions", "Order",
            "Horses", "Horses Simplified", "puzzle", "Farm", "Jungle", "Savanna", "CupsBalls",
            "Potions", "VideoPlayer", "VideoGrid", "bottle"

            // replay OK but the resize of the game itself is bad:
            // "SpotDifference", "MediaPlayer","Paper-Scissors-Stone",
            //"Math101: Addition","Math101: All operations","Math101: Division",
            //"Math101: Multiplication","Math101: Subtraction"

            // Replay offset was really clear here,due to the gameContext.getRoot() instead of gameContext
            // "Colorsss"

            // replay cursor coordinates issue, can lead to different display:
            // "Ninja","Piano","Divisor","Lapins", "ColoredBubbles","PortraitBubbles",
            // "RushHour","FrogsRace",
            // "WhacAmole"

            // replay cursor coordinates issue:
            // "Blocks",

            // replay cursor frequency:
            // "BibJump", "Labyrinth",

            // lots of problem:
            // "Robots","Biboule","SpaceGame",

            // arranger le jeu:
            // "Pet",
            // "Room"
        )
    );

    List<GameSpec> gamesList;
    private final ApplicationContext applicationContext;
    private GameContext gameContext;
    private final GazePlay gazePlay;

    JsonFile json;

    private GameSpec selectedGameSpec;
    private IGameVariant gameVariant;
    private SavedStatsInfo savedStatsInfo;

    private Thread workingThread;
    private String fileName;

    public ReplayingGameFromJson(GazePlay gazePlay, ApplicationContext applicationContext, List<GameSpec> games) {
        this.gazePlay = gazePlay;
        this.applicationContext = applicationContext;
        this.gameContext = applicationContext.getBean(GameContext.class);
        this.gamesList = games;
    }

    public String getCurrentGameName() {
        return json.getGameName();
    }

    public static boolean replayIsAllowed(String gameCode) {
        return replayableGameList.contains(gameCode);
    }

    public void pickJSONFile(String fileName) throws IOException {
        this.fileName = fileName;
        if (fileName == null)
            return;

        final File replayDataFile = new File(fileName);

        InputStream in = Files.newInputStream(Paths.get(fileName));
        JSONTokener tokener = new JSONTokener(in);
        JSONObject object = new JSONObject(tokener);

        try (InputStream inputStream = ReplayingGameFromJson.class.getResourceAsStream("JSON-schema-replayData.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(object); // throws a ValidationException if this object is invalid
        } catch (ValidationException e) {
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
        json = gson.fromJson(bufferedReader, JsonFile.class);

        String filePrefix = fileName.substring(0, fileName.lastIndexOf("-"));
        final File gazeMetricsMouseFile = new File(filePrefix + "-metricsMouse.png");
        final File gazeMetricsGazeFile = new File(filePrefix + "-metricsGaze.png");
        final File gazeMetricsMouseAndGazeFile = new File(filePrefix + "-metricsMouseAndGaze.png");
        final File screenShotFile = new File(filePrefix + "-screenshot.png");
        final File colorBandsFile = new File(filePrefix + "-colorBands.png");
        savedStatsInfo = new SavedStatsInfo(gazeMetricsMouseFile, gazeMetricsGazeFile, gazeMetricsMouseAndGazeFile,
            screenShotFile, colorBandsFile, replayDataFile);
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
        if (selectedFile != null)
            selectedFileName = selectedFile.getAbsolutePath();
        return selectedFileName;
    }

    public void replayGame() {
        if (fileName == null)
            return;

        double height = gameContext.getCurrentScreenDimensionSupplier().get().getHeight();
        double width = gameContext.getCurrentScreenDimensionSupplier().get().getWidth();
        double screenRatio = height / width;

        double sceneAspectRatio = json.getSceneAspectRatio();
        if (sceneAspectRatio < screenRatio)
            height = gameContext.getCurrentScreenDimensionSupplier().get().getWidth() * sceneAspectRatio;
        else
            width = height / sceneAspectRatio;

        getSpecAndVariant();

        launchGame((int) width, (int) height);
    }

    public void getSpecAndVariant() {
        gameContext = applicationContext.getBean(GameContext.class);
        gazePlay.onGameLaunch(gameContext);
        for (GameSpec gameSpec : gamesList)
            if (json.getGameName().equals(gameSpec.getGameSummary().getNameCode()))
                selectedGameSpec = gameSpec;
        for (IGameVariant variant : selectedGameSpec.getGameVariantGenerator().getVariants())
            if (json.getGameVariant().equals(variant.toString()))
                gameVariant = variant;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void drawLines() {
        if (fileName == null)
            return;

        Configuration config = ActiveConfigurationContext.getInstance();
        config.getFixationLengthProperty().setValue(json.getConfigFixationLength());
        config.getQuestionLengthProperty().setValue(json.getConfigQuestionLength());
        config.getReaskQuestionOnFailProperty().setValue(json.isConfigReaskQuestionOnFail());
        config.getLimiterScoreProperty().setValue(json.isConfigLimiterScore());
        config.getLimiterScoreValueProperty().setValue(json.getConfigLimiterScoreValue());
        config.getLimiterTimeProperty().setValue(json.isConfigLimiterTime());
        config.getLimiterTimeValueProperty().setValue(json.getConfigLimiterTimeValue());
        config.getAnimationSpeedRatioProperty().setValue(json.getConfigAnimationSpeedRatio());
        config.getTransitionTimeProperty().setValue(json.getConfigTransitionTime());
        config.getDelayBeforeSelectionTimeProperty().setValue(json.getConfigDelayBeforeSelectionTime());

        getSpecAndVariant();
        IGameLauncher gameLauncher = selectedGameSpec.getGameLauncher();
        final Scene scene = gazePlay.getPrimaryScene();
        final Stats statsSaved = gameLauncher.createSavedStats(scene,
            json.getStatsNbGoalsReached(), json.getStatsNbGoalsToReach(), json.getStatsNbUncountedGoalsReached(),
            json.getLifeCycle(), json.getRoundsDurationReport(), json.getFixationSequence(), json.getMovementHistory(),
            json.getHeatMap(), json.getAOIList(), savedStatsInfo);
        GameLifeCycle currentGame = gameLauncher.replayGame(gameContext, gameVariant, statsSaved, json.getGameSeed());
        gameContext.createControlPanel(gazePlay, statsSaved, currentGame, "replay");
        gameContext.createQuitShortcut(gazePlay, statsSaved, currentGame);
        currentGame.launch();

        EventHandler<Event> homeEvent = e -> {
            gameContext.getRoot().setCursor(Cursor.WAIT); // Change cursor to wait style
            workingThread.interrupt();
            this.exit(statsSaved, currentGame);
            gameContext.getRoot().setCursor(Cursor.DEFAULT); // Change cursor to default style
        };

        gameContext.getHomeButton().addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        final Dimension2D screenDimension = gameContext.getCurrentScreenDimensionSupplier().get();
        //Drawing in canvas
        final javafx.scene.canvas.Canvas canvas = new Canvas(screenDimension.getWidth(), screenDimension.getHeight());
        gameContext.getChildren().add(canvas);

        workingThread = new Thread(() -> {
            gameContext.getGazeDeviceManager().setInReplayMode(true);
            drawFixationLines(canvas);
            Platform.runLater(() -> exit(statsSaved, currentGame));
        });
        workingThread.start();
    }

    private void launchGame(final int width, final int height) {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                gazePlay.getPrimaryScene().setCursor(Cursor.WAIT);
                gazePlay.getPrimaryScene().setRoot(new LoadingContext(gazePlay));
                return null;
            }
        };
        task.run();

        ProcessBuilder builder = createBuilder(height, width);

        GameMenuController.runProcessDisplayLoadAndWaitForNewJVMDisplayed(gazePlay, builder);
    }

    public ProcessBuilder createBuilder(int height, int width) {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        LinkedList<String> commands = new LinkedList<>(Arrays.asList(javaBin, "-cp", classpath, GazePlayLauncher.class.getName()));

        String user = ActiveConfigurationContext.getInstance().getUserName();
        if (user != null && !user.equals(""))
            commands.addAll(Arrays.asList("--user", user));
        else
            commands.add("--default-user");

        commands.addAll(Arrays.asList("--game", selectedGameSpec.getGameSummary().getNameCode()));

        if (gameVariant != null)
            commands.addAll(Arrays.asList("--variant", gameVariant.toString()));

        if (height != 0 && width != 0)
            commands.addAll(Arrays.asList("--height", "" + height, "--width", "" + width));

        commands.addAll(Arrays.asList("-json", this.fileName));

        return new ProcessBuilder(commands);
    }

    private void exit(Stats statsSaved, GameLifeCycle currentGame) {
        gameContext.exitReplayGame(statsSaved, gazePlay, currentGame);
        gameContext.getGazeDeviceManager().setInReplayMode(false);
    }

    private void drawFixationLines(Canvas canvas) {
        Dimension2D dim2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double sceneWidth = dim2D.getWidth();
        final double sceneHeight = dim2D.getHeight();
        final GraphicsContext graphics = canvas.getGraphicsContext2D();

        for (CoordinatesTracker coordinatesTracker : json.getMovementHistory()) {
            int x = (int) (coordinatesTracker.getXValue() * sceneWidth);
            int y = (int) (coordinatesTracker.getYValue() * sceneHeight);
            Point2D point = new Point2D(x, y);

            Platform.runLater(() -> paint(graphics, canvas, point, coordinatesTracker.getEvent()));

            try {
                TimeUnit.MILLISECONDS.sleep(coordinatesTracker.getIntervalTime());
            } catch (InterruptedException e) {
                log.info("Game has been interrupted");
                break;
            }
        }
    }

    public void paint(GraphicsContext graphics, Canvas canvas, Point2D point, String event) {
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (event.equals("gaze"))
            updateGazeTab(point);
        else // if (event.equals("mouse")) {
            updateMouseTab(point);

        drawOvals(graphics);

        gameContext.getGazeDeviceManager().onSavedMovementsUpdate(point, event);
    }

    public void updateGazeTab(Point2D point) {
        while (lastGazeCoordinates.size() >= numberOfElementToDisplay)
            lastGazeCoordinates.pop();
        lastGazeCoordinates.add(point);
    }

    public void updateMouseTab(Point2D point) {
        while (lastMouseCoordinates.size() >= numberOfElementToDisplay)
            lastMouseCoordinates.pop();
        lastMouseCoordinates.add(point);
    }

    public void drawOvals(GraphicsContext graphics) {
        int circleSize = 10;
        if (lastGazeCoordinates.size() > 0)
            drawReplayLine(graphics, circleSize, Color.LIGHTBLUE, Color.DARKBLUE, lastGazeCoordinates);
        if (lastMouseCoordinates.size() > 0)
            drawReplayLine(graphics, circleSize, Color.INDIANRED, Color.DARKRED, lastMouseCoordinates);
    }

    private void drawReplayLine(GraphicsContext graphics, int circleSize, Color strokeColor, Color fillColor, LinkedList<Point2D> lastGazeCoordinates) {
        Color tempStokeColor;
        Point2D point;
        graphics.beginPath();
        for (int i = lastGazeCoordinates.size() - 1; i >= 0; i--) {
            point = lastGazeCoordinates.get(i);
            if (point != null && i != lastGazeCoordinates.size() - 1) {
                tempStokeColor = Color.rgb(
                    (int) (strokeColor.getRed() * 255),
                    (int) (strokeColor.getBlue() * 255),
                    (int) (strokeColor.getGreen() * 255),
                    i * 1d / lastGazeCoordinates.size());
                graphics.setStroke(tempStokeColor);
                graphics.setLineWidth(5);
                graphics.lineTo(point.getX(), point.getY());
                graphics.stroke();
            } else if (point != null) {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        point = lastGazeCoordinates.getLast();
        graphics.setStroke(strokeColor);
        graphics.strokeOval(point.getX() - circleSize / 2d, point.getY() - circleSize / 2d, circleSize, circleSize);
        graphics.setFill(fillColor);
        graphics.fillOval(point.getX() - circleSize / 2d, point.getY() - circleSize / 2d, circleSize, circleSize);
    }
}


@Getter
@AllArgsConstructor
class JsonFile {
    private double gameSeed;
    private String gameName;
    private String gameVariant;
    private long gameStartedTime;
    private String screenAspectRatio;
    private double sceneAspectRatio;
    private int statsNbGoalsReached;
    private int statsNbGoalsToReach;
    private int statsNbUncountedGoalsReached;

    private int configFixationLength;
    private long configQuestionLength;
    private boolean configReaskQuestionOnFail;
    private boolean configLimiterScore;
    private int configLimiterScoreValue;
    private boolean configLimiterTime;
    private int configLimiterTimeValue;
    private double configAnimationSpeedRatio;
    private int configTransitionTime;
    private int configDelayBeforeSelectionTime;

    private LifeCycle lifeCycle;
    private RoundsDurationReport roundsDurationReport;
    private ArrayList<LinkedList<FixationPoint>> fixationSequence;
    private ArrayList<CoordinatesTracker> movementHistory;
    private double[][] heatMap;
    private ArrayList<AreaOfInterest> AOIList;
}
