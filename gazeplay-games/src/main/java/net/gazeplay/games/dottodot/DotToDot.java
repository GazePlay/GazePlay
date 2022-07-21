package net.gazeplay.games.dottodot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Dimension2D;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class DotToDot implements GameLifeCycle {

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    private final DotToDotGameVariant gameVariant;


    @Getter
    private final ReplayablePseudoRandom randomGenerator;

    @Getter
    private ArrayList<DotEntity> dotList;

    @Getter
    private ArrayList<Line> lineList;

    @Getter
    @Setter
    private int previous;

    @Getter
    @Setter
    private int level = 0;

    @Getter
    @Setter
    private int fails = 0;

    @Getter
    private List<Integer> listOfFails = new LinkedList<>();


    public DotToDot(final IGameContext gameContext, final DotToDotGameVariant gameVariant, final Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setCurrentGameSeed(randomGenerator.getSeed());
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

        if (!gameVariant.getLabel().contains("Dynamic")) {
            level = getRandomGenerator().nextInt(8);
        }

        final String path = "data/dottodot/";
        final String folder = "level" + level + "/";

        int indexElement = randomGenerator.nextInt(5);
        log.info("level = {}, index = {}", level, indexElement);

        JsonParser parser = new JsonParser();
        JsonObject jsonRoot;
        jsonRoot = (JsonObject) parser.parse(new InputStreamReader(
            Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(path + folder + "elements" + level + indexElement + ".json")), StandardCharsets.UTF_8));

        /* Uncomment this part when images will be available */
//        Dimension2D dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
//        Configuration config = gameContext.getConfiguration();
//        String backgroundPath = path + jsonRoot.get("background").getAsString();
//        Image backgroundImage = new Image(backgroundPath);
//        ImageView background = new ImageView(backgroundImage);
//
//        double scaleRatio = Math.min(dimensions.getWidth() / backgroundImage.getWidth(),
//            dimensions.getHeight() / backgroundImage.getHeight());
//
//        if (config.isBackgroundEnabled()) {
//            createBackground(background, dimensions, scaleRatio, gameContext);
//        }

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
        fails++;
    }

    public void createDots(JsonArray elements) {
        int index = 0;
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        for (JsonElement element : elements) {
            index++;
            JsonObject elementObj = (JsonObject) element;

            // Creating a dot
            StackPane dotShape = new StackPane();

            // Positioning a dot
            double ratioX = dimension2D.getWidth() / 1920;
            double ratioY = dimension2D.getHeight() / 1080;

            JsonObject coordinates = elementObj.getAsJsonObject("coords");
            double x = ratioX * coordinates.get("x").getAsDouble();
            double y = ratioY * coordinates.get("y").getAsDouble();


            Circle smallDot = new Circle(10);
            smallDot.setFill(Color.STEELBLUE);
            Circle bigDot = new Circle(60);
            bigDot.setFill(Color.STEELBLUE);
            bigDot.setOpacity(0.2);
            dotShape.getChildren().addAll(bigDot, smallDot);
            dotShape.prefWidthProperty().bind(bigDot.radiusProperty().multiply(2));
            dotShape.prefHeightProperty().bind(bigDot.radiusProperty().multiply(2));

            dotShape.setLayoutX(x - bigDot.getRadius());
            dotShape.setLayoutY(y - bigDot.getRadius());

            // Creating text
            Text number = new Text(x - 70, y, Integer.toString(index));
            number.setStyle("-fx-font-size: 60");
            Color textColor = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<Color>() {
                @Override
                public Color visitLight() {
                    return Color.BLACK;
                }

                @Override
                public Color visitDark() {
                    return Color.WHITE;
                }
            });
            number.setFill(textColor);

            // Creating progress indicator
            ProgressIndicator progressIndicator = new ProgressIndicator(0);
            double progIndicSize = smallDot.getRadius() * 5;
            progressIndicator.setPrefSize(progIndicSize, progIndicSize);
            progressIndicator.setLayoutX(x - bigDot.getRadius() - progIndicSize / 2 + 50);
            progressIndicator.setLayoutY(y - bigDot.getRadius() - progIndicSize / 2 + 50);
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
