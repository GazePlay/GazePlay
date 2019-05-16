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
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.ProgressButton;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.*;

@Slf4j
public class SpotTheDifferences implements GameLifeCycle {

    private final Stats stats;
    private final GameContext gameContext;
    private final Dimension2D dimensions;

    private ImageView leftImage;
    private double leftGap;
    private ImageView rightImage;
    private double rightGap;
    private BorderPane borderPane;
    private GridPane gridPane;
    private Text scoreText;
    private ProgressButton nextButton;

    private int numberDiffFound;
    private int totalNumberDiff;

    private JsonArray instances;

    private int currentInstance;

    public SpotTheDifferences(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Configuration config = Configuration.getInstance();
        this.currentInstance = 0;

        Multilinguism translate = Multilinguism.getSingleton();
        String language = config.getLanguage();

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

        VBox texts = new VBox();
        borderPane.setBottom(texts);

        scoreText = new Text(0, 50, "");
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.setFill(Color.WHITE);
        scoreText.setFont(new Font(70));
        scoreText.setWrappingWidth(dimensions.getWidth());

        Text findText = new Text(0, 50, translate.getTrad("Spot all the differences", language));
        findText.setTextAlignment(TextAlignment.CENTER);
        findText.setFill(Color.WHITE);
        findText.setFont(new Font(50));
        findText.setWrappingWidth(dimensions.getWidth());

        Text foundText = new Text(0, 50,
                translate.getTrad("Differences found", language) + translate.getTrad("Colon", language));
        foundText.setTextAlignment(TextAlignment.CENTER);
        foundText.setFill(Color.WHITE);
        foundText.setFont(new Font(50));
        foundText.setWrappingWidth(dimensions.getWidth());

        texts.getChildren().addAll(findText, foundText, scoreText);

        nextButton = new ProgressButton();
        ImageView nextImage = new ImageView("data/spotthedifferences/next.png");
        nextImage.setFitHeight(dimensions.getHeight() / 8);
        nextImage.setFitWidth(dimensions.getHeight() / 8);
        nextButton.setLayoutX(dimensions.getWidth() / 2 - nextImage.getFitWidth() / 2);
        nextButton.setLayoutY(dimensions.getHeight() - 1.1 * nextImage.getFitHeight());
        nextButton.setImage(nextImage);
        nextButton.assignIndicator(event -> launch(), config.getFixationLength());
        this.gameContext.getGazeDeviceManager().addEventFilter(nextButton);
        nextButton.active();

        JsonParser parser = new JsonParser();
        try {
            instances = (JsonArray) parser.parse(new InputStreamReader(new FileInputStream(
                    SpotTheDifferences.class.getClassLoader().getResource("data/spotthedifferences").getPath()
                            + "/instances.json"),
                    "utf-8"));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void createDifference(double x, double y, double radius) {
        Difference d1 = new Difference(gameContext, this, leftGap + x, y, radius);
        Difference d2 = new Difference(gameContext, this, rightGap + x, y, radius);
        d1.setPair(d2);
        d2.setPair(d1);
    }

    void differenceFound() {
        numberDiffFound++;
        scoreText.setText(numberDiffFound + "/" + totalNumberDiff);
        if (numberDiffFound == totalNumberDiff) {
            gameContext.playWinTransition(200, actionEvent -> {
                try {
                    gameContext.showRoundStats(stats, this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        stats.incNbGoals();
    }

    @Override
    public void launch() {
        gameContext.clear();
        gameContext.getChildren().addAll(borderPane, nextButton);

        JsonObject instance = (JsonObject) instances.get(currentInstance);// random.nextInt(instances.size()));
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

        JsonArray diffs = (JsonArray) instance.get("differences");
        double ratio = rightImage.getBoundsInLocal().getHeight() / rightImage.getImage().getHeight();
        for (JsonElement diff : diffs) {
            JsonObject obj = (JsonObject) diff;
            createDifference(ratio * obj.get("x").getAsDouble(), ratio * obj.get("y").getAsDouble(),
                    ratio * obj.get("radius").getAsDouble());
        }

        numberDiffFound = 0;
        totalNumberDiff = diffs.size();
        scoreText.setText(numberDiffFound + "/" + totalNumberDiff);

        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {

    }
}
