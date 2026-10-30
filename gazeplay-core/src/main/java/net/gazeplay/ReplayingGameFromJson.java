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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.ReplayDataFormat;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;
import net.gazeplay.ui.scenes.ingame.GameContext;
import net.gazeplay.ui.scenes.loading.LoadingContext;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

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
import java.util.function.Supplier;

/**
 * Replay of a game from its replayData.json file: the game is started again with the same variant and random seed (so
 * the same targets appear), and the recorded gaze and mouse samples are dispatched to it at their recorded pace, drawn
 * as two trails (blue for the gaze, red for the mouse) on a canvas over the game.
 * <p>
 * Only the games of {@link #replayIsAllowed(String) the replayable list} are offered: the others do not replay
 * faithfully (see the list). The files of every version of the format are read (see {@code ReplayDataFormat}).
 */
@Slf4j
public class ReplayingGameFromJson {

    private final LinkedList<Point2D> lastGazeCoordinates = new LinkedList<Point2D>();
    private final LinkedList<Point2D> lastMouseCoordinates = new LinkedList<Point2D>();
    int numberOfelementToDisplay = 10;

    private static final ArrayList<String> replayableGameList = new ArrayList<String>(
        Arrays.asList(
            //OK: really small offsets problems
            "Bottle", "Cakes", "Creampie", "CupsBalls", "Letters", "Dice", "EggGame", "FindTheOddOneOut",
            "FlowerOfNumbers", "GooseGame", "Horses", "Horses Simplified", "MagicCards", "Memory",
            "MemoryLetters", "MemoryNumbers", "OpenMemory", "OpenMemoryLetters", "OpenMemoryNumbers", "Opinions",
            "Order", "PersonalizeEggGame", "Potions", "Puzzle", "ScratchCard", "Scribble", "SoundsOfLife", "VideoGrid",
            "VideoPlayer", "WhereIsIt", "WhereIsTheAnimal", "WhereIsTheColor", "WhereIsTheFlag", "WhereIsTheLetter",
            "WhereIsTheNumber", "WhereIsTheShape"

            // replay OK but the resize of the game itself is bad:
            // "SpotDifference", "MediaPlayer","RockPaperScissors",
            //"Math101: Addition","Math101: All operations","Math101: Division",
            //"Math101: Multiplication","Math101: Substraction"

            // Replay offset was really clear here,due to the gameContext.getRoot() instead of gameContext
            // "Coloring"

            // replay cursor coordinates issue, can lead to different display:
            // "Ninja","Piano","Divisor","Rabbits", "ColoredBubbles","PortraitBubbles",
            // "RushHour","FrogsRace",
            // "WhacAMole"

            // replay cursor coordinates issue:
            // "Blocks",

            // replay cursor frequency:
            // "BibJump", "Labyrinth",

            // lots of problem:
            // "ShootTheRobots","ShootTheBiboules","SpaceGame",

            // arranger le jeu:
            // "Pet",
            // "Room"
        )
    );

    List<GameSpec> gamesList;
    private final Supplier<GameContext> gameContextFactory;
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
    private double sceneAspectRatio;

    private Thread workingThread;

    private String fileName;

    public ReplayingGameFromJson(GazePlay gazePlay, Supplier<GameContext> gameContextFactory, List<GameSpec> games) {
        this.gameContextFactory = gameContextFactory;
        this.gazePlay = gazePlay;
        this.gameContext = gameContextFactory.get();
        this.gamesList = games;
    }

    /**
     * @return whether the replay of this game is offered
     */
    public static boolean replayIsAllowed(String gameCode) {
        return replayableGameList.contains(gameCode);
    }

    /**
     * Reads a replayData.json file (checked against its JSON schema; a mismatch is only logged), and the names of the
     * other result files of its round. Does nothing for a null name (file chooser cancelled).
     */
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
            log.warn("The replay file {} does not match its JSON schema", replayDataFile, e);
        }

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = Files.newBufferedReader(replayDataFile.toPath(), Charset.defaultCharset());
        } catch (IOException e) {
            log.error("Cannot read the replay file {}", replayDataFile, e);
        }
        assert bufferedReader != null;
        Gson gson = new Gson();
        JsonFile json = gson.fromJson(bufferedReader, JsonFile.class);
        currentGameSeed = json.getGameSeed();
        currentGameNameCode = json.getGameName();
        currentGameVariant = json.getGameVariant();
        coordinatesAndTimeStamp = json.getCoordinatesAndTimeStamp();
        // Files are read as written, whatever their version (they are never rewritten): version 2 stores the fixation
        // coordinates in the right fields, the memory keeps the convention of version 1
        fixationSequence = ReplayDataFormat.fromFile(ReplayDataFormat.versionOf(json.getFormatVersion()),
            json.getFixationSequence());
        nbGoalsReached = json.getStatsNbGoalsReached();
        nbGoalsToReach = json.getStatsNbGoalsToReach();
        nbUnCountedGoalsReached = json.getStatsNbUnCountedGoalsReached();
        sceneAspectRatio = json.getSceneAspectRatio();
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
    }

    /**
     * Asks the user to choose a replayData.json file.
     *
     * @return the chosen file, null when cancelled
     */
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

    /**
     * Replays the chosen file in a new JVM, in a window of the proportions of the scene of the recorded game.
     */
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

    /**
     * Shows a new game screen, and finds the game and the variant of the file.
     */
    public void getSpecAndVariant() {
        gameContext = gameContextFactory.get();
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

    /**
     * Starts the replay in this JVM: the game is created with the saved seed, and a background thread dispatches the
     * recorded samples at their pace; the statistics screen is shown at the end.
     */
    public void drawLines() {
        if (fileName == null) {
            return;
        }
        getSpecAndVariant();
        IGameLauncher gameLauncher = selectedGameSpec.getGameLauncher();
        final Scene scene = gazePlay.getPrimaryScene();
        // The variant and the Stats come from the same GameSpec as the launcher: their types match at run time
        @SuppressWarnings("unchecked")
        final Stats statsSaved = gameLauncher.createSavedStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        @SuppressWarnings("unchecked")
        GameLifeCycle currentGame = gameLauncher.replayGame(gameContext, gameVariant, statsSaved, currentGameSeed);
        gameContext.createControlPanel(gazePlay, statsSaved, currentGame, true);
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
            drawFixationLines(canvas, coordinatesAndTimeStamp);
            Platform.runLater(() -> exit(statsSaved, currentGame));
        });
        workingThread.start();
    }

    /**
     * Shows the loading screen and starts the JVM of the replay, of the given size.
     */
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

    /**
     * @return the command starting GazePlay in a new JVM to replay the file, with the JVM options of
     *   {@link GazePlayLauncher#jvmOptions()}, the GazePlay folder, the user, the game, the file and the size of the
     *   window
     */
    public ProcessBuilder createBuilder(int height, int width) {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
            File.separator + "bin" +
            File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        // The same JVM options as a game started from the menu (GameMenuController): the replay JVM used to start
        // without them, and Java 25 then warned about the native access of JavaFX and Tobii
        LinkedList<String> commands = new LinkedList<>(Arrays.asList(javaBin, "-Djdk.gtk.version=2"));
        commands.addAll(GazePlayLauncher.jvmOptions());
        // The same GazePlay folder as the menu: the game does not look for it, nor move it, again
        commands.add("-D" + GazePlayDirectories.GAZEPLAY_FOLDER_PROPERTY + "="
            + GazePlayDirectories.getGazePlayFolder().getAbsolutePath());
        commands.addAll(Arrays.asList("-cp", classpath, GazePlayLauncher.class.getName()));

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
        gameContext.exitReplayJsonGame(statsSaved, gazePlay, currentGame);
        gameContext.getGazeDeviceManager().setInReplayMode(false);
    }

    /**
     * Plays the recorded samples, on the background thread of the replay: each one is drawn and dispatched on the
     * JavaFX thread at its recorded time since the start of the round. Stops when the thread is interrupted (home
     * button).
     * <p>
     * The thread used to wait, after each sample, the interval since the previous sample of the same source: with the
     * gaze and the mouse, the waits of both sources added up and the replay lasted longer than the game.
     */
    private void drawFixationLines(Canvas canvas, JsonArray coordinatesAndTimeStamp) {
        Dimension2D dim2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double sceneWidth = dim2D.getWidth();
        double sceneHeight = dim2D.getHeight();
        final GraphicsContext graphics = canvas.getGraphicsContext2D();
        final long replayStart = System.nanoTime();
        synchronized (coordinatesAndTimeStamp) {
            for (JsonElement coordinateAndTimeStamp : coordinatesAndTimeStamp) {
                JsonObject coordinateAndTimeObj = coordinateAndTimeStamp.getAsJsonObject();
                String nextEvent = coordinateAndTimeObj.get("event").getAsString();
                int nextX = (int) (Double.parseDouble(coordinateAndTimeObj.get("X").getAsString()) * sceneWidth);
                int nextY = (int) (Double.parseDouble(coordinateAndTimeObj.get("Y").getAsString()) * sceneHeight);
                final long sampleTime = Long.parseLong(coordinateAndTimeObj.get("time").getAsString());
                final long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - replayStart);
                try {
                    TimeUnit.MILLISECONDS.sleep(delayBeforeSample(sampleTime, elapsed));
                } catch (InterruptedException e) {
                    log.info("Game has been interrupted");
                    break;
                }
                final String event = nextEvent.equals("gaze") ? "gaze" : "mouse";
                Platform.runLater(() -> paint(graphics, canvas, nextX, nextY, event));
            }
        }
    }

    /**
     * @param sampleTime time of a recorded sample since the start of the round, in ms
     * @param elapsed    time since the start of the replay, in ms
     * @return how long to wait before playing the sample: 0 when its time has come (the samples of the two sources can
     *   be slightly out of order since format version 3)
     */
    static long delayBeforeSample(final long sampleTime, final long elapsed) {
        return Math.max(0, sampleTime - elapsed);
    }

    /**
     * Adds a gaze position to the trail, keeping the last {@code numberOfelementToDisplay} ones.
     */
    public void updateGazeTab(int nextX, int nextY) {
        while (lastGazeCoordinates.size() >= numberOfelementToDisplay) {
            lastGazeCoordinates.pop();
        }
        lastGazeCoordinates.add(new Point2D(nextX, nextY));
    }

    /**
     * Adds a mouse position to the trail, keeping the last {@code numberOfelementToDisplay} ones.
     */
    public void updateMouseTab(int nextX, int nextY) {
        while (lastMouseCoordinates.size() >= numberOfelementToDisplay) {
            lastMouseCoordinates.pop();
        }
        lastMouseCoordinates.add(new Point2D(nextX, nextY));


    }

    /**
     * Draws the trails of the gaze and of the mouse.
     */
    public void drawOvals(GraphicsContext graphics) {

        int circleSize = 10;
        if (lastGazeCoordinates.size() > 0) {
            drawReplayLine(graphics, circleSize, Color.LIGHTBLUE, Color.DARKBLUE, lastGazeCoordinates);
        }
        if (lastMouseCoordinates.size() > 0) {
            drawReplayLine(graphics, circleSize, Color.INDIANRED, Color.DARKRED, lastMouseCoordinates);
        }
    }

    /**
     * Draws a trail: a line fading from the oldest to the newest position, and a dot on the newest one.
     */
    private void drawReplayLine(GraphicsContext graphics, int circleSize, Color strokeColor, Color fillColor, LinkedList<Point2D> lastGazeCoordinates) {
        Color tempStokeColor = strokeColor;
        Point2D point;
        graphics.beginPath();
        for (int i = lastGazeCoordinates.size() - 1; i >= 0; i--) {
            point = lastGazeCoordinates.get(i);
            if (point != null && i != lastGazeCoordinates.size() - 1) {
                tempStokeColor = Color.rgb(
                    (int) (strokeColor.getRed() * 255),
                    (int) (strokeColor.getGreen() * 255),
                    (int) (strokeColor.getBlue() * 255),
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

    /**
     * Draws a recorded sample and dispatches it to the game as a gaze or mouse event.
     */
    public void paint(GraphicsContext graphics, Canvas canvas, int nextX, int nextY, String event) {
        if (!gameContext.getChildren().contains(canvas)) {
            gameContext.getChildren().add(canvas);
        }

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

/**
 * Content of a replayData.json file, as read by Gson (the names of the fields are the keys of the file).
 */
class JsonFile {
    @Getter
    private double gameSeed;
    @Getter
    private String gameName;
    @Getter
    private String gameVariantClass;
    /**
     * Absent from the files written before version 2.
     */
    @Getter
    private Integer formatVersion;
    @Getter
    private String gameVariant;
    @Getter
    private double gameStartedTime;
    @Getter
    private String screenAspectRatio;
    @Getter
    private double sceneAspectRatio;
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
