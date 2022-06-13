package net.gazeplay;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
            //OK: really small offsets problems
            "Scribble", "Cakes", "Creampie", "WhereIsIt", "WhereIsTheAnimal", "letters",
            "WhereIsTheColor", "WhereIsTheLetter", "WhereIsTheNumber", "findodd", "flags",
            "ScratchCard", "Memory", "MemoryLetters", "MemoryNumbers", "OpenMemory", "OpenMemoryLetters",
            "OpenMemoryNumbers", "Dice", "EggGame", "PersonalizeEggGame", "GooseGame", "MagicCards", "Opinions", "Order",
            "Horses", "Horses Simplified", "puzzle", "Farm", "Jungle", "Savanna", "CupsBalls",
            "Potions", "VideoPlayer", "VideoGrid", "bottle"

            // replay OK but the resize of the game itself is bad:
            // "SpotDifference", "MediaPlayer","Paper-Scissors-Stone",
            //"Math101: Addition","Math101: All operations","Math101: Division",
            //"Math101: Multiplication","Math101: Substraction"

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
    private double currentGameSeed;
    @Getter
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
    private int nextTimeMouse;
    private int nextTimeGaze;
    private double sceneAspectRatio;
    private ArrayList<AreaOfInterest> AOIList;

    private Thread workingThread;

    private String fileName;

    public ReplayingGameFromJson(GazePlay gazePlay, ApplicationContext applicationContext, List<GameSpec> games) {
        this.applicationContext = applicationContext;
        this.gazePlay = gazePlay;
        this.gameContext = applicationContext.getBean(GameContext.class);
        this.gamesList = games;
    }

    public static boolean replayIsAllowed(String gameCode) {
        return replayableGameList.contains(gameCode);
    }

    public void pickJSONFile(String fileName) throws IOException {
        this.fileName = fileName;
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
        JsonFile json = gson.fromJson(bufferedReader, JsonFile.class);
        currentGameSeed = json.getGameSeed();
        currentGameNameCode = json.getGameName();
        currentGameVariant = json.getGameVariant();
        coordinatesAndTimeStamp = json.getCoordinatesAndTimeStamp();
        fixationSequence = json.getFixationSequence();
        nbGoalsReached = json.getStatsNbGoalsReached();
        nbGoalsToReach = json.getStatsNbGoalsToReach();
        nbUnCountedGoalsReached = json.getStatsNbUnCountedGoalsReached();
        sceneAspectRatio = json.getSceneAspectRatio();
        lifeCycle = json.getLifeCycle();
        roundsDurationReport = json.getRoundsDurationReport();
        AOIList = json.getAOIList();
        String filePrefix = fileName.substring(0, fileName.lastIndexOf("-"));
        final File gazeMetricsMouseFile = new File(filePrefix + "-metricsMouse.png");
        final File gazeMetricsGazeFile = new File(filePrefix + "-metricsGaze.png");
        final File gazeMetricsMouseAndGazeFile = new File(filePrefix + "-metricsMouseAndGaze.png");
        final File heatMapCsvFile = new File(filePrefix + "-heatmap.csv");
        final File screenShotFile = new File(filePrefix + "-screenshot.png");
        final File colorBandsFile = new File(filePrefix + "-colorBands.png");
        savedStatsInfo = new SavedStatsInfo(heatMapCsvFile, gazeMetricsMouseFile, gazeMetricsGazeFile, gazeMetricsMouseAndGazeFile, screenShotFile,
            colorBandsFile, replayDataFile);
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

    public void replayGame() {
        if (fileName == null) {
            return;
        }
        double height = gameContext.getCurrentScreenDimensionSupplier().get().getHeight();
        double width = gameContext.getCurrentScreenDimensionSupplier().get().getWidth();
        double screenRatio = height / width;
        if (sceneAspectRatio < screenRatio) {
            height = gameContext.getCurrentScreenDimensionSupplier().get().getWidth() * sceneAspectRatio;
        } else {
            width = height / sceneAspectRatio;
        }

        getSpecAndVariant();

        launchGame((int) width, (int) height);
    }

    public void getSpecAndVariant() {
        gameContext = applicationContext.getBean(GameContext.class);
        gazePlay.onGameLaunch(gameContext);
        for (GameSpec gameSpec : gamesList) {
            if (currentGameNameCode.equals(gameSpec.getGameSummary().getNameCode())) {
                selectedGameSpec = gameSpec;
            }
        }
        for (IGameVariant variant : selectedGameSpec.getGameVariantGenerator().getVariants()) {
            if (currentGameVariant.equals(variant.toString())) {
                gameVariant = variant;
            }
        }
    }

    public void drawLines() {
        if (fileName == null) {
            return;
        }
        getSpecAndVariant();
        IGameLauncher gameLauncher = selectedGameSpec.getGameLauncher();
        final Scene scene = gazePlay.getPrimaryScene();
        final Stats statsSaved = gameLauncher.createSavedStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, AOIList, savedStatsInfo);
        GameLifeCycle currentGame = gameLauncher.replayGame(gameContext, gameVariant, statsSaved, currentGameSeed);
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
            List<CoordinatesTracker> movementHistory;
            gameContext.getGazeDeviceManager().setInReplayMode(true);
            movementHistory = drawFixationLines(canvas, coordinatesAndTimeStamp);
            statsSaved.setMovementHistory(movementHistory);
            Platform.runLater(() -> exit(statsSaved, currentGame));
        });
        workingThread.start();
    }

    private void launchGame(final int width, final int height) {
        Task task = new Task<Void>() {
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
        String javaBin = javaHome +
            File.separator + "bin" +
            File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        LinkedList<String> commands = new LinkedList<>(Arrays.asList(javaBin, "-cp", classpath, GazePlayLauncher.class.getName()));

        String user = ActiveConfigurationContext.getInstance().getUserName();
        if (user != null && !user.equals("")) {
            commands.addAll(Arrays.asList("--user", user));
        } else {
            commands.add("--default-user");
        }

        commands.addAll(Arrays.asList("--game", selectedGameSpec.getGameSummary().getNameCode()));

        if (gameVariant != null) {
            commands.addAll(Arrays.asList("--variant", gameVariant.toString()));
        }


        if (height != 0 && width != 0) {
            commands.addAll(Arrays.asList("--height", "" + height, "--width", "" + width));
        }

        commands.addAll(Arrays.asList("-json", this.fileName));

        return new ProcessBuilder(commands);
    }

    private void exit(Stats statsSaved, GameLifeCycle currentGame) {
        gameContext.exitReplayGame(statsSaved, gazePlay, currentGame);
        gameContext.getGazeDeviceManager().setInReplayMode(false);
    }

    private List<CoordinatesTracker> drawFixationLines(Canvas canvas, JsonArray coordinatesAndTimeStamp) {
        Dimension2D dim2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double sceneWidth = dim2D.getWidth();
        double sceneHeight = dim2D.getHeight();
        final GraphicsContext graphics = canvas.getGraphicsContext2D();
        List<CoordinatesTracker> movementHistory = new ArrayList<>();
        synchronized (coordinatesAndTimeStamp) {
            for (JsonElement coordinateAndTimeStamp : coordinatesAndTimeStamp) {
                JsonObject coordinateAndTimeObj = coordinateAndTimeStamp.getAsJsonObject();
                String nextEvent = coordinateAndTimeObj.get("event").getAsString();
                int nextX = (int) (Double.parseDouble(coordinateAndTimeObj.get("X").getAsString()) * sceneWidth);
                int nextY = (int) (Double.parseDouble(coordinateAndTimeObj.get("Y").getAsString()) * sceneHeight);
                long timeInterval = Long.parseLong(coordinateAndTimeObj.get("timeInterval").getAsString());
                movementHistory.add(new CoordinatesTracker(nextX, nextY, timeInterval, 0));
                int delay;
                if (nextEvent.equals("gaze")) {
                    int prevTimeGaze = nextTimeGaze;
                    nextTimeGaze = Integer.parseInt(coordinateAndTimeObj.get("time").getAsString());
                    delay = nextTimeGaze - prevTimeGaze;
                    Platform.runLater(() -> paint(graphics, canvas, nextX, nextY, "gaze"));
                } else {
                    int prevTimeMouse = nextTimeMouse;
                    nextTimeMouse = Integer.parseInt(coordinateAndTimeObj.get("time").getAsString());
                    delay = nextTimeMouse - prevTimeMouse;
                    Platform.runLater(() -> paint(graphics, canvas, nextX, nextY, "mouse"));
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(delay);
                } catch (InterruptedException e) {
                    log.info("Game has been interrupted");
                    break;
                }
            }
            return movementHistory;
        }
    }

    public void updateGazeTab(int nextX, int nextY) {
        while (lastGazeCoordinates.size() >= numberOfElementToDisplay) {
            lastGazeCoordinates.pop();
        }
        lastGazeCoordinates.add(new Point2D(nextX, nextY));
    }

    public void updateMouseTab(int nextX, int nextY) {
        while (lastMouseCoordinates.size() >= numberOfElementToDisplay) {
            lastMouseCoordinates.pop();
        }
        lastMouseCoordinates.add(new Point2D(nextX, nextY));


    }

    public void drawOvals(GraphicsContext graphics) {

        int circleSize = 10;
        if (lastGazeCoordinates.size() > 0) {
            drawReplayLine(graphics, circleSize, Color.LIGHTBLUE, Color.DARKBLUE, lastGazeCoordinates);
        }
        if (lastMouseCoordinates.size() > 0) {
            drawReplayLine(graphics, circleSize, Color.INDIANRED, Color.DARKRED, lastMouseCoordinates);
        }
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

    public void paint(GraphicsContext graphics, Canvas canvas, int nextX, int nextY, String event) {

        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (event.equals("gaze")) {
            updateGazeTab(nextX, nextY);
        } else { // if (event.equals("mouse")) {
            updateMouseTab(nextX, nextY);
        }

        drawOvals(graphics);

        Point2D point = new Point2D(nextX, nextY);
        gameContext.getGazeDeviceManager().onSavedMovementsUpdate(point, event);
    }

}

@Getter
@AllArgsConstructor
class JsonFile {
    private double gameSeed;
    private String gameName;
    private String gameVariantClass;
    private String gameVariant;
    private double gameStartedTime;
    private String screenAspectRatio;
    private double sceneAspectRatio;
    private JsonArray coordinatesAndTimeStamp;
    private LifeCycle lifeCycle;
    private int statsNbGoalsReached;
    private int statsNbGoalsToReach;
    private int statsNbUnCountedGoalsReached;
    private RoundsDurationReport roundsDurationReport;
    private ArrayList<LinkedList<FixationPoint>> fixationSequence;
    private double[][] heatMap;
    private ArrayList<AreaOfInterest> AOIList;
}
