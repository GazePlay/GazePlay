package net.gazeplay.games.videogrid;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class VideoGrid implements GameLifeCycle {

    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Configuration config;

    private final int nbLines;
    private final int nbColumns;
    private final GridPane grid;
    private final File videoFolder;
    private final Random random;

    public VideoGrid(GameContext gameContext, Stats stats, int nbLines, int nbColumns) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = Configuration.getInstance();
        this.random = new Random();

        grid = new GridPane();
        videoFolder = new File(config.getVideoFolder());
    }

    @Override
    public void launch() {

        if(videoFolder.isDirectory()) {
            ArrayList<File> files = new ArrayList(Arrays.asList(videoFolder.listFiles()));
            //Filter out non compatible files
            for (File f : files) {
                if (!f.isFile() || !f.canRead() || f.isDirectory()) {
                    files.remove(f);
                }
            }
            if(files.size() == 0)
                noVideosFound();
            //Whether all videos can be unique, if that is not the case, there will be duplicates
            Boolean allUnique = files.size() >= nbColumns * nbLines;

            for (int i = 0; i < nbLines; i++) {
                for (int j = 0; j < nbColumns; j++) {
                    int index = random.nextInt(files.size());
                    Media media = new Media(files.get(index).toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    MediaView mediaView = new MediaView(mediaPlayer);
                    grid.add(mediaView, i, j);
                    mediaView.getMediaPlayer().play();
                    if (allUnique) {
                        files.remove(index);
                    }
                }
            }

            gameContext.getChildren().add(grid);
        }else{
            noVideosFound();
        }
    }

    private void noVideosFound() {
        Text errorText = new Text("No videos found");
        errorText.setY(dimensions.getHeight()/2);
        errorText.setTextAlignment(TextAlignment.CENTER);
        errorText.setFill(config.isBackgroundWhite() ? Color.BLACK : Color.WHITE);
        errorText.setFont(new Font(dimensions.getHeight() / 10));
        errorText.setWrappingWidth(dimensions.getWidth());
        gameContext.getChildren().add(errorText);
    }

    @Override
    public void dispose() {
        grid.getChildren().clear();
        gameContext.getChildren().clear();
    }
}
