package net.gazeplay.games.soundsoflife;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

/**
 * Game "SoundsOfLife" (reviewed on 30/10/2026, REVUE_DES_JEUX.md): a scene of the variant (farm, jungle, savanna,
 * sea) shows animals that play their sound after a fixation, and show their name; there is nothing to win, the player
 * explores. The scenes are described by data/soundsoflife/&lt;scene&gt;/elements.json (gradle/images/make_soundsoflife_images.py).
 */
@Slf4j
public class SoundsOfLife implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final ArrayList<TargetAOI> targetAOIList;
    private final SoundsOfLifeGameVariant gameVariant;
    private final ReplayablePseudoRandom randomGenerator;

    public SoundsOfLife(IGameContext gameContext, Stats stats, SoundsOfLifeGameVariant gameVariant) {
        this(gameContext, stats, gameVariant, -1);
    }

    public SoundsOfLife(IGameContext gameContext, Stats stats, SoundsOfLifeGameVariant gameVariant, double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.targetAOIList = new ArrayList<>();
        this.gameVariant = gameVariant;
        
        if (gameSeed < 0) {
            this.randomGenerator = new ReplayablePseudoRandom();
            this.stats.setGameSeed(randomGenerator.getSeed());
        } else {
            this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
        }
    }

    /**
     * Places the background picture of the scene, scaled and centered in the game area (the animals are placed after
     * it, also when it is not shown: 30/10/2026, they used to be shifted when the background was disabled).
     */
    private void placeBackground(ImageView background, Dimension2D dimensions, double scaleRatio) {
        background.setFitWidth(background.getImage().getWidth() * scaleRatio);
        background.setFitHeight(background.getImage().getHeight() * scaleRatio);

        double offsetX = (dimensions.getWidth() - background.getFitWidth()) / 2;
        double offsetY = (dimensions.getHeight() - background.getFitHeight()) / 2;

        background.setX(offsetX);
        background.setY(offsetY);
    }

    @Override
    public void launch() {
    
        gameContext.getChildren().clear();
    
        Dimension2D dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Configuration config = gameContext.getConfiguration();

        String path = "data/soundsoflife/" + gameVariant.toString().toLowerCase() + "/";

        JsonObject jsonRoot;
        jsonRoot = (JsonObject) JsonParser.parseReader(new InputStreamReader(
            Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(path + "elements.json")), StandardCharsets.UTF_8));

        String backgroundPath = path + jsonRoot.get("background").getAsString();
        Image backgroundImage = new Image(backgroundPath);
        ImageView background = new ImageView(backgroundImage);

        // use ratio in order to adapt images to screen
        double scaleRatio = Math.min(dimensions.getWidth() / backgroundImage.getWidth(),
            dimensions.getHeight() / backgroundImage.getHeight());

        placeBackground(background, dimensions, scaleRatio);
        if (config.isBackgroundEnabled()) {
            gameContext.getChildren().add(background);
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
            // Listing all the sound paths
            JsonArray soundPaths = elementObj.getAsJsonArray("sounds");
            ArrayList<String> sounds = new ArrayList<>();
            for (JsonElement sound : soundPaths) {
                sounds.add(path + sound.getAsString());
            }

            String name = elementObj.has("name") ? gameContext.getTranslator().translate(elementObj.get("name").getAsString())
                : "";
            SoundMakingEntity entity = new SoundMakingEntity(imageView, name, stats, sounds, gameContext,
                gameContext.getSoundManager(), randomGenerator);
            gameContext.getChildren().add(entity);
            gameContext.getGazeDeviceManager().addEventFilter(entity);
        }
    
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.pauseBackgroundMusic();
    }

    @Override
    public void dispose() {
        for (TargetAOI aoi : targetAOIList) {
            aoi.setTimeEnded(System.currentTimeMillis());
        }
        this.stats.setTargetAOIList(targetAOIList);
    }
}
