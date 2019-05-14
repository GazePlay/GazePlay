package net.gazeplay.games.spotthedifferences;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;


@Slf4j
public class SpotTheDifferences implements GameLifeCycle {

    private final Stats stats;
    private final GameContext gameContext;
    private final Dimension2D dimensions;
    private final Configuration config;

    private ImageView leftImage;
    private Bounds leftBounds;
    private ImageView rightImage;
    private Bounds rightBounds;
    private BorderPane borderPane;
    private Text scoreText;

    private int numberDiffFound;
    private int totalNumberDiff;

    private JsonArray instances;
    private JsonParser parser;

    private Random random;

    public SpotTheDifferences(GameContext gameContext, Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = Configuration.getInstance();

        random = new Random();

        borderPane = new BorderPane();
        Pane spring = new Pane();
        spring.setMinWidth(2);
        borderPane.setCenter(spring);

        VBox texts = new VBox();
        borderPane.setBottom(texts);

        scoreText = new Text(0, 50, "");
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.setFill(Color.WHITE);
        scoreText.setFont(new Font(70));
        scoreText.setWrappingWidth(dimensions.getWidth());

        Text findText = new Text(0, 50, "Spot all the differences");
        findText.setTextAlignment(TextAlignment.CENTER);
        findText.setFill(Color.WHITE);
        findText.setFont(new Font(50));
        findText.setWrappingWidth(dimensions.getWidth());

        Text foundText = new Text(0, 50, "Differences found:");
        foundText.setTextAlignment(TextAlignment.CENTER);
        foundText.setFill(Color.WHITE);
        foundText.setFont(new Font(50));
        foundText.setWrappingWidth(dimensions.getWidth());

        texts.getChildren().addAll(findText, foundText, scoreText );

        parser = new JsonParser();
        try {
            instances = (JsonArray)parser.parse(new FileReader(SpotTheDifferences.class.getClassLoader().getResource("data/spotthedifferences").getPath()+"/instances.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createDifference(double x, double y, double radius){
        Difference d1 = new Difference(gameContext, this, leftBounds.getMinX() + x, leftBounds.getMinY() + y, radius);
        Difference d2 = new Difference(gameContext, this, rightBounds.getMinX() + x, rightBounds.getMinY() + y, radius);
        d1.setPair(d2);
        d2.setPair(d1);
    }

    public void differenceFound(){
        numberDiffFound++;
        scoreText.setText(numberDiffFound + "/" + totalNumberDiff);
        if(numberDiffFound == totalNumberDiff){
            gameContext.playWinTransition(500, actionEvent -> {
                gameContext.clear();
                launch();
                stats.notifyNewRoundReady();
                gameContext.onGameStarted();
            });
        }
    }

    @Override
    public void launch() {
        leftImage = new ImageView();
        leftImage.setPreserveRatio(true);
        borderPane.setLeft(leftImage);
        borderPane.setAlignment(leftImage, Pos.CENTER_RIGHT);

        rightImage = new ImageView();
        rightImage.setPreserveRatio(true);
        borderPane.setRight(rightImage);
        borderPane.setAlignment(leftImage, Pos.CENTER_LEFT);

        gameContext.getChildren().add(borderPane);
        leftBounds = leftImage.localToScene(leftImage.getBoundsInLocal());
        rightBounds = rightImage.localToScene(rightImage.getBoundsInLocal());

        JsonObject instance = (JsonObject)instances.get(random.nextInt(instances.size()));

        leftImage.setImage(new Image("data/spotthedifferences/" + instance.get("image1").getAsString()));
        rightImage.setImage(new Image("data/spotthedifferences/" + instance.get("image2").getAsString()));

        if(leftImage.getImage().getWidth() > leftImage.getImage().getHeight()){
            leftImage.setFitWidth(dimensions.getWidth()/2);
            rightImage.setFitWidth(dimensions.getWidth()/2);
        }else{
            leftImage.setFitHeight(2*dimensions.getHeight()/3);
            rightImage.setFitHeight(2*dimensions.getHeight()/3);
        }

        JsonArray diffs = (JsonArray)instance.get("differences");
        double ratio = rightImage.getBoundsInLocal().getHeight() / rightImage.getImage().getHeight();
        for(JsonElement diff: diffs){
            JsonObject obj = (JsonObject)diff;
            createDifference(ratio * obj.get("x").getAsDouble(), ratio * obj.get("y").getAsDouble(), ratio * obj.get("radius").getAsDouble());
        }
        numberDiffFound = 0;
        totalNumberDiff = diffs.size();
        scoreText.setText(numberDiffFound + "/" + totalNumberDiff);
    }

    @Override
    public void dispose() {

    }
}
