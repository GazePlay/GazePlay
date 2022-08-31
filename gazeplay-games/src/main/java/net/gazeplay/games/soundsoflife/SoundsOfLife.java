package net.gazeplay.games.soundsoflife;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Dimension2D;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
public class SoundsOfLife implements GameLifeCycle {

    private final Stats stats;
    private final ReplayablePseudoRandom randomGenerator;
    private final int gameVariant;
    private final IGameContext gameContext;
    private final ArrayList<TargetAOI> targetAOIList;

    public SoundsOfLife(IGameContext gameContext, Stats stats, int gameVariant) {
        this.stats = stats;
        this.gameContext = gameContext;
        this.targetAOIList = new ArrayList<>();
        this.gameVariant = gameVariant;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());
    }

    public SoundsOfLife(IGameContext gameContext, Stats stats, int gameVariant, double gameSeed) {
        this.stats = stats;
        this.gameContext = gameContext;
        this.targetAOIList = new ArrayList<>();
        this.gameVariant = gameVariant;
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
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

        gameContext.getChildren().clear();

        Dimension2D dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Configuration config = gameContext.getConfiguration();

        String path = "data/soundsoflife/";
        switch (gameVariant) {
            case 0:
                path += "farm/";
                break;
            case 1:
                path += "jungle/";
                break;
            case 2:
                path += "savanna/";
                break;
            default:
                path += "sea/";
        }

        JsonParser parser = new JsonParser();
        JsonObject jsonRoot;
        jsonRoot = (JsonObject) parser.parse(new InputStreamReader(
            Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(path + "elements.json")), StandardCharsets.UTF_8));

        String backgroundPath = path + jsonRoot.get("background").getAsString();
        Image backgroundImage = new Image(backgroundPath);
        ImageView background = new ImageView(backgroundImage);

        // use ratio in order to adapt images to screen
        double scaleRatio = Math.min(dimensions.getWidth() / backgroundImage.getWidth(),
            dimensions.getHeight() / backgroundImage.getHeight());

        if (config.isBackgroundEnabled()) {
            createBackground(background, dimensions, scaleRatio, gameContext);
        }

        JsonArray elements = jsonRoot.getAsJsonArray("elements");
        for (JsonElement element : elements) {
            JsonObject elementObj = (JsonObject) element;
            // Creating image
            String imagePath = path + elementObj.get("image").getAsString();
            Image image = new Image(imagePath);
            ImageView imageView = new ImageView(image);
            // Scaling image
            double scale = elementObj.get("scale").getAsDouble();
            imageView.setFitWidth(image.getWidth() * scaleRatio * scale);
            imageView.setFitHeight(image.getHeight() * scaleRatio * scale);
            // Positioning image
            JsonObject coordinates = elementObj.getAsJsonObject("coords");
            double x = coordinates.get("x").getAsDouble() * scaleRatio + background.getX();
            double y = coordinates.get("y").getAsDouble() * scaleRatio + background.getY();
            imageView.setX(x - imageView.getFitWidth() / 2);
            imageView.setY(y - imageView.getFitHeight() / 2);
            final TargetAOI targetAOI = new TargetAOI(imageView.getX(), y, (int) ((imageView.getFitWidth() + imageView.getFitHeight()) / 3),
                System.currentTimeMillis());
            targetAOIList.add(targetAOI);
            // Creating progress indicator
            ProgressIndicator progressIndicator = new ProgressIndicator(0);
            double progIndicSize = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2;
            progressIndicator.setPrefSize(progIndicSize, progIndicSize);
            progressIndicator.setLayoutX(x - progIndicSize / 2);
            progressIndicator.setLayoutY(y - progIndicSize / 2);
            progressIndicator.setOpacity(0);

            // Listing all the sound paths
            JsonArray soundPaths = elementObj.getAsJsonArray("sounds");
            ArrayList<String> sounds = new ArrayList<>();
            for (JsonElement sound : soundPaths) {
                sounds.add(path + sound.getAsString());
            }

            SoundMakingEntity entity = new SoundMakingEntity(imageView, stats, sounds, progressIndicator,
                gameContext, gameContext.getSoundManager(), randomGenerator);
            gameContext.getChildren().add(entity);
            gameContext.getGazeDeviceManager().addEventFilter(entity);
        }

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();
        backgroundMusicManager.pause();
    }

    @Override
    public void dispose() {
        for (TargetAOI aoi : targetAOIList) {
            aoi.setTimeEnded(System.currentTimeMillis());
        }
        this.stats.setTargetAOIList(targetAOIList);
    }
}
