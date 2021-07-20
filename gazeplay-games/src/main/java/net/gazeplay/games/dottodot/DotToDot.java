package net.gazeplay.games.dottodot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Dimension2D;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
public class DotToDot implements GameLifeCycle {

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    private final DotToDotGameVariant gameVariant;

    @Getter
    private final ArrayList<TargetAOI> targetAOIList;

    private final ReplayablePseudoRandom randomGenerator;

    private ArrayList<DotEntity> dotList;

    @Getter
    private ArrayList<Line> lineList;

    @Getter @Setter
    private int previous;


    public DotToDot(final IGameContext gameContext, final DotToDotGameVariant gameVariant, final Stats stats) {
        //super();


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

        String path = "data/dottodot/";

        JsonParser parser = new JsonParser();
        JsonObject jsonRoot;
        jsonRoot = (JsonObject) parser.parse(new InputStreamReader(
            Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(path + "elements.json")), StandardCharsets.UTF_8));

        String backgroundPath = path + jsonRoot.get("background").getAsString();
        Image backgroundImage = new Image(backgroundPath);
        ImageView background = new ImageView(backgroundImage);

        double scaleRatio = Math.min(dimensions.getWidth() / backgroundImage.getWidth(),
            dimensions.getHeight() / backgroundImage.getHeight());

        if (config.isBackgroundEnabled()) {
            createBackground(background, dimensions, scaleRatio, gameContext);
        }

        JsonArray elements = jsonRoot.getAsJsonArray("elements");
        int index = 0;
        for (JsonElement element : elements) {
            index++;
            JsonObject elementObj = (JsonObject) element;

            // Creating a dot
            String imagePath = path + elementObj.get("image").getAsString();
            Image image = new Image(imagePath);
            ImageView imageView = new ImageView(image);

            // Scaling
            double scale = elementObj.get("scale").getAsDouble();
            imageView.setFitWidth(image.getWidth() * scale);
            imageView.setFitHeight(image.getHeight() * scale);

            // Positioning a dot
            JsonObject coordinates = elementObj.getAsJsonObject("coords");
            double x = coordinates.get("x").getAsDouble();
            double y = coordinates.get("y").getAsDouble();
            imageView.setX(x - imageView.getFitWidth() / 2);
            imageView.setY(y - imageView.getFitHeight() / 2);
            final TargetAOI targetAOI = new TargetAOI(imageView.getX(), y, (int) ((imageView.getFitWidth() + imageView.getFitHeight()) / 3),
                System.currentTimeMillis());
            targetAOIList.add(targetAOI);
            log.info("target list = {}", targetAOIList);

            // Creating progress indicator
            ProgressIndicator progressIndicator = new ProgressIndicator(0);
            double progIndicSize = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2;
            progressIndicator.setPrefSize(progIndicSize, progIndicSize);
            progressIndicator.setLayoutX(x - progIndicSize / 2);
            progressIndicator.setLayoutY(y - progIndicSize / 2 + 5);
            progressIndicator.setOpacity(0);

            DotEntity dot = new DotEntity(imageView, stats, progressIndicator, gameContext, gameVariant, this, index);
            dotList.add(dot);
            gameContext.getChildren().add(dot);
            gameContext.getGazeDeviceManager().addEventFilter(dot);
            log.info("x = {}, y = {}", imageView.getX(), imageView.getY());
            log.info("progress x = {}, progress y = {}", progressIndicator.getLayoutX(), progressIndicator.getLayoutY());
        }

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();

    }

    @Override
    public void dispose() {
        gameContext.clear();
    }
}
