package net.gazeplay.games.spotthedifferences;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.components.ProgressButton;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

@Slf4j
public class SpotTheDifferences implements GameLifeCycle {

    @Getter
    private final SpotTheDifferencesStats stats;
    private final IGameContext gameContext;
    private final Dimension2D dimensions;

    private final ImageView leftImage;
    private double leftGap;
    private final ImageView rightImage;
    private double rightGap;
    private final BorderPane borderPane;
    private final GridPane gridPane;
    private final Text scoreText;
    private final ProgressButton nextButton;

    private int numberDiffFound;
    private int totalNumberDiff;

    private final JsonArray instances;

    private int currentInstance;
    final ArrayList<TargetAOI> targetAOIList;

    public SpotTheDifferences(final IGameContext gameContext, final SpotTheDifferencesStats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final Configuration config = gameContext.getConfiguration();
        this.currentInstance = 0;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startTimeLimiter();

        final Multilinguism translate = MultilinguismFactory.getSingleton();
        final String language = config.getLanguage();

        borderPane = new BorderPane();
        gridPane = new GridPane();
        borderPane.setCenter(gridPane);
        gridPane.setMinWidth(dimensions.getWidth() / 2);
        gridPane.setHgap(5);
        gridPane.setAlignment(Pos.CENTER);

        leftImage = new ImageView();
        leftImage.setPreserveRatio(true);
        gridPane.add(leftImage, 0, 0);

        rightImage = new ImageView();
        rightImage.setPreserveRatio(true);
        gridPane.add(rightImage, 1, 0);

        final VBox texts = new VBox();
        borderPane.setBottom(texts);

        scoreText = new Text(0, 50, "");
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.setFill(Color.WHITE);
        scoreText.setFont(new Font(70));
        scoreText.setWrappingWidth(dimensions.getWidth());

        final Text findText = new Text(0, 50, translate.getTranslation("Spot all the differences", language));
        findText.setTextAlignment(TextAlignment.CENTER);
        findText.setFill(Color.WHITE);
        findText.setFont(new Font(50));
        findText.setWrappingWidth(dimensions.getWidth());

        final Text foundText = new Text(0, 50,
            translate.getTranslation("Differences found", language) + translate.getTranslation("Colon", language));
        foundText.setTextAlignment(TextAlignment.CENTER);
        foundText.setFill(Color.WHITE);
        foundText.setFont(new Font(50));
        foundText.setWrappingWidth(dimensions.getWidth());

        texts.getChildren().addAll(findText, foundText, scoreText);

        nextButton = new ProgressButton();
        final ImageView nextImage = new ImageView("data/spotthedifferences/next.png");
        nextImage.setFitHeight(dimensions.getHeight() / 8);
        nextImage.setFitWidth(dimensions.getHeight() / 8);
        nextButton.setLayoutX(dimensions.getWidth() / 2 - nextImage.getFitWidth() / 2);
        nextButton.setLayoutY(dimensions.getHeight() - 1.1 * nextImage.getFitHeight());
        nextButton.setImage(nextImage);
        nextButton.assignIndicatorUpdatable(event -> launch(), gameContext);
        this.gameContext.getGazeDeviceManager().addEventFilter(nextButton);
        nextButton.active();

        final JsonParser parser = new JsonParser();
        instances = (JsonArray) parser
            .parse(new InputStreamReader(
                Objects.requireNonNull(
                    ClassLoader.getSystemResourceAsStream("data/spotthedifferences/instances.json")),
                StandardCharsets.UTF_8));
    }

    private void createDifference(final double x, final double y, final double radius) {
        final Difference d1 = new Difference(gameContext, this, leftGap + x, y, radius);
        final TargetAOI targetAOI1 = new TargetAOI(leftGap + x, y, (int)radius, System.currentTimeMillis());
        targetAOIList.add(targetAOI1);
        d1.setTargetIdx(targetAOIList.size()-1);
        final Difference d2 = new Difference(gameContext, this, rightGap + x, y, radius);
        final TargetAOI targetAOI2 = new TargetAOI(rightGap + x, y, (int)radius, System.currentTimeMillis());
        targetAOIList.add(targetAOI2);
        d2.setTargetIdx(targetAOIList.size()-1);
        d1.setPair(d2);
        d2.setPair(d1);
    }

    void differenceFound() {
        numberDiffFound++;
        scoreText.setText(numberDiffFound + "/" + totalNumberDiff);
        stats.incrementNumberOfGoalsReached();
        if (numberDiffFound == totalNumberDiff) {
            gameContext.updateScore(stats,this);
            gameContext.playWinTransition(200, actionEvent -> gameContext.showRoundStats(stats, this));
        }
        String soundResource = "data/spotthedifferences/ding.wav";
        gameContext.getSoundManager().add(soundResource);
    }

    @Override
    public void launch() {
        gameContext.clear();
        gameContext.getChildren().addAll(borderPane, nextButton);

        gameContext.setLimiterAvailable();

        //Random random = new Random();
        final JsonObject instance = (JsonObject) instances.get(currentInstance);// random.nextInt(instances.size()));
        currentInstance = (currentInstance + 1) % instances.size();

        leftImage.setImage(new Image("data/spotthedifferences/" + instance.get("image1").getAsString()));
        rightImage.setImage(new Image("data/spotthedifferences/" + instance.get("image2").getAsString()));

        if (leftImage.getImage().getWidth() > leftImage.getImage().getHeight()) {
            leftImage.setFitWidth(dimensions.getWidth() / 2);
            rightImage.setFitWidth(dimensions.getWidth() / 2);
        } else {
            leftImage.setFitHeight(2 * dimensions.getHeight() / 3);
            rightImage.setFitHeight(2 * dimensions.getHeight() / 3);
        }

        leftGap = (dimensions.getWidth() / 2) - leftImage.getBoundsInLocal().getWidth();
        rightGap = dimensions.getWidth() / 2 + gridPane.getHgap();

        final JsonArray diffs = (JsonArray) instance.get("differences");
        final double ratio = rightImage.getBoundsInLocal().getHeight() / rightImage.getImage().getHeight();
        for (final JsonElement diff : diffs) {
            final JsonObject obj = (JsonObject) diff;
            createDifference(ratio * obj.get("x").getAsDouble(), ratio * obj.get("y").getAsDouble(),
                ratio * obj.get("radius").getAsDouble());
        }

        numberDiffFound = 0;
        totalNumberDiff = diffs.size();
        scoreText.setText(numberDiffFound + "/" + totalNumberDiff);
        stats.incrementNumberOfGoalsToReach(totalNumberDiff);
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose() {
        stats.setTargetAOIList(targetAOIList);
    }

}
