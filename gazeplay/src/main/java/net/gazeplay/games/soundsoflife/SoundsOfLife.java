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
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
public class SoundsOfLife implements GameLifeCycle {
    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Configuration config;

    public SoundsOfLife(GameContext gameContext, Stats stats, int gameVariant) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = Configuration.getInstance();

        String path = "data/soundsoflife/";
        switch (gameVariant) {
        case 0:
            path += "farm/";
            break;
        case 1:
            path += "jungle/";
            break;
        default:
            path += "farm/";
        }

        JsonParser parser = new JsonParser();
        JsonObject jsonRoot = null;
        try {
            jsonRoot = (JsonObject) parser.parse(new InputStreamReader(
                    Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(path + "elements.json")), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String backgroundPath = path + jsonRoot.get("background").getAsString();
        Image backgroundImage = new Image(backgroundPath);
        ImageView background = new ImageView(backgroundImage);
        double scaleRatio = Math.min(dimensions.getWidth() / backgroundImage.getWidth(),
                dimensions.getHeight() / backgroundImage.getHeight());
        background.setFitWidth(backgroundImage.getWidth() * scaleRatio);
        background.setFitHeight(backgroundImage.getHeight() * scaleRatio);
        double xOffset = (dimensions.getWidth() - background.getFitWidth()) / 2;
        double yOffset = (dimensions.getHeight() - background.getFitHeight()) / 2;
        background.setX(xOffset);
        background.setY(yOffset);

        gameContext.getChildren().add(background);

        JsonArray elements = jsonRoot.getAsJsonArray("elements");
        for (JsonElement element : elements) {
            JsonObject elementObj = (JsonObject) element;
            // Creating image
            String imagePath = path + elementObj.get("image").getAsString();
            Image image = new Image(imagePath);
            ImageView imageView = new ImageView(image);
            // Scaling image
            double scale = elementObj.get("scale").getAsDouble();
            log.info("width: {}, ratio: {}, scale: {}", image.getWidth(), scaleRatio, scale);
            imageView.setFitWidth(image.getWidth() * scaleRatio * scale);
            imageView.setFitHeight(image.getHeight() * scaleRatio * scale);
            // Positioning image
            JsonObject coordinates = elementObj.getAsJsonObject("coords");
            double x = coordinates.get("x").getAsDouble() * scaleRatio + xOffset;
            double y = coordinates.get("y").getAsDouble() * scaleRatio + yOffset;
            imageView.setX(x - imageView.getFitWidth() / 2);
            imageView.setY(y - imageView.getFitHeight() / 2);
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

            SoundMakingEntity entity = new SoundMakingEntity(imageView, sounds, progressIndicator,
                    config.getFixationLength());
            gameContext.getChildren().add(entity);
            gameContext.getGazeDeviceManager().addEventFilter(entity);
        }

    }

    @Override
    public void launch() {

    }

    @Override
    public void dispose() {

    }
}
