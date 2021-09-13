package net.gazeplay.games.dottodot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Dimension2D;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ResourceFileManager;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class DotToDot implements GameLifeCycle {

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    private final DotToDotGameVariant gameVariant;

    @Getter
    private final ArrayList<TargetAOI> targetAOIList;

    @Getter
    private final ReplayablePseudoRandom randomGenerator;

    @Getter
    private ArrayList<DotEntity> dotList;

    @Getter
    private ArrayList<Line> lineList;

    @Getter @Setter
    private int previous;

    @Getter @Setter
    private int level = 0;

    @Getter @Setter
    private int fails = 0;

    @Getter
    private List<Integer> listOfFails = new LinkedList<>();


    public DotToDot(final IGameContext gameContext, final DotToDotGameVariant gameVariant, final Stats stats) {

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.targetAOIList = new ArrayList<>();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());
        this.dotList = new ArrayList<>();
        this.lineList = new ArrayList<>();
        this.previous = 1;

    }

    private void createBackground(ImageView background, Dimension2D dimensions, double scaleRatio, IGameContext gameContext) {

        background.setFitWidth(background.getImage().getWidth() * scaleRatio);
        background.setFitHeight(background.getImage().getHeight() * scaleRatio);

        double offsetX = (dimensions.getWidth() - background.getFitWidth()) / 2;
        double offsetY = (dimensions.getHeight() - background.getFitHeight()) / 2;

        background.setX(offsetX);
        background.setY(offsetY);

        gameContext.getChildren().add(background);
    }

    @Override
    public void launch() {
        Dimension2D dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Configuration config = gameContext.getConfiguration();

        if (!gameVariant.getLabel().contains("Dynamic"))
            level = getRandomGenerator().nextInt(8);

        final String path = "data/dottodot/";
        final String folder = "level" + level + "/";

        int indexElement = randomGenerator.nextInt(5);
        log.info("level = {}, index = {}", level, indexElement);

        JsonParser parser = new JsonParser();
        JsonObject jsonRoot;
        jsonRoot = (JsonObject) parser.parse(new InputStreamReader(
            Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(path + folder + "elements" + level +  indexElement  +  ".json")), StandardCharsets.UTF_8));

        String backgroundPath = path + jsonRoot.get("background").getAsString();
        Image backgroundImage = new Image(backgroundPath);
        ImageView background = new ImageView(backgroundImage);

        double scaleRatio = Math.min(dimensions.getWidth() / backgroundImage.getWidth(),
            dimensions.getHeight() / backgroundImage.getHeight());

        if (config.isBackgroundEnabled()) {
            createBackground(background, dimensions, scaleRatio, gameContext);
        }

        JsonArray elements = jsonRoot.getAsJsonArray("elements");

        createDots(elements);

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        stats.incrementNumberOfGoalsToReach();
        gameContext.firstStart();

    }

    @Override
    public void dispose() {
        gameContext.clear();
    }

    public void catchFail() {
        fails ++;
    }

    public void createDots(JsonArray elements) {
        int index = 0;
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        for (JsonElement element : elements) {
            index ++;
            JsonObject elementObj = (JsonObject) element;

            // Creating a dot
            Circle dotShape = new Circle(30);

            // Positioning a dot
            double ratioX = dimension2D.getWidth()/1920;
            double ratioY = dimension2D.getHeight()/1080;

            JsonObject coordinates = elementObj.getAsJsonObject("coords");
            double x = ratioX * coordinates.get("x").getAsDouble();
            double y = ratioY * coordinates.get("y").getAsDouble();

            dotShape.setCenterX(x);
            dotShape.setCenterY(y);

            final TargetAOI targetAOI = new TargetAOI(dotShape.getCenterX(), y, (int) ((dotShape.getRadius() + dotShape.getRadius()) / 3),
                System.currentTimeMillis());
            targetAOIList.add(targetAOI);

            // Creating text
            Text number = new Text(x - 70, y, Integer.toString(index));
            number.setStyle("-fx-font-size: 60");

            // Creating progress indicator
            ProgressIndicator progressIndicator = new ProgressIndicator(0);
            double progIndicSize = dotShape.getRadius() * 5;
            progressIndicator.setPrefSize(progIndicSize, progIndicSize);
            progressIndicator.setLayoutX(x - progIndicSize / 2 + 50);
            progressIndicator.setLayoutY(y - progIndicSize / 2 + 50);
            progressIndicator.setOpacity(0);

            DotEntity dot = new DotEntity(dotShape, stats, progressIndicator, number, gameContext, gameVariant, this, index);
            dotList.add(dot);

            if (gameVariant.getLabel().contains("Number")) {
                gameContext.getChildren().add(dot);
                gameContext.getGazeDeviceManager().addEventFilter(dot);
            }
        }

        if (gameVariant.getLabel().contains("Order")) {
            gameContext.getChildren().addAll(dotList.get(0), dotList.get(1));
            gameContext.getGazeDeviceManager().addEventFilter(dotList.get(0));
            gameContext.getGazeDeviceManager().addEventFilter(dotList.get(1));
        }

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        stats.incrementNumberOfGoalsToReach();
        gameContext.firstStart();

    }

    public void positioningDot(DotEntity dot) {
        gameContext.getChildren().add(dot);
        gameContext.getGazeDeviceManager().addEventFilter(dot);
    }

}
