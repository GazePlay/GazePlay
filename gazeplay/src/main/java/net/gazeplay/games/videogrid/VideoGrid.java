package net.gazeplay.games.videogrid;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

@Slf4j
public class VideoGrid implements GameLifeCycle {

    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Configuration config;
    private final Multilinguism translate;

    private final int nbLines;
    private final int nbColumns;
    private final GridPane grid;
    private final File videoFolder;
    private final Random random;
    private final ArrayList<String> compatibleFileTypes;

    private final ColorAdjust grayscale;

    public VideoGrid(GameContext gameContext, Stats stats, int nbLines, int nbColumns) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = Configuration.getInstance();
        this.random = new Random();
        this.translate = Multilinguism.getSingleton();

        grid = new GridPane();
        videoFolder = new File(config.getVideoFolder());
        compatibleFileTypes = new ArrayList<>(Arrays.asList("mp4"));

        grayscale = new ColorAdjust();
        grayscale.setSaturation(-1);
    }

    @Override
    public void launch() {

        if (videoFolder.isDirectory()) {
            ArrayList<File> files = new ArrayList(Arrays.asList(videoFolder.listFiles()));
            // Filter out non compatible files
            files.removeIf(f -> !f.isFile() || !f.canRead() || f.isDirectory()
                    || !compatibleFileTypes.contains(FilenameUtils.getExtension(f.getName())));
            log.info("nb files: " + files.size());
            if (files.size() == 0)
                noVideosFound();

            /*
             * Different list where we will pick files from randomly. To reduce the number of duplicates
             */
            ArrayList<File> filesChooseFrom = new ArrayList<>(files);

            for (int i = 0; i < nbLines; i++) {
                for (int j = 0; j < nbColumns; j++) {
                    if (filesChooseFrom.size() == 0) {
                        filesChooseFrom.addAll(files);
                    }
                    int index = random.nextInt(filesChooseFrom.size());
                    String path = filesChooseFrom.remove(index).toURI().toString();
                    Media media = new Media(path);
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.volumeProperty().bind(config.getEffectsVolumeProperty());
                    mediaPlayer.setOnError(() -> log.info("ERROR: " + mediaPlayer.getError()));

                    // loop
                    mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));

                    MediaView mediaView = new MediaView();
                    mediaView.setMediaPlayer(mediaPlayer);
                    mediaView.setFitHeight(dimensions.getHeight() / nbLines);
                    mediaView.setFitWidth(dimensions.getWidth() / nbColumns);
                    mediaView.setEffect(grayscale);

                    EventHandler<Event> enterEvent = (Event event) -> {
                        mediaPlayer.play();
                        mediaView.setEffect(null);
                    };
                    EventHandler<Event> exitEvent = (Event event) -> {
                        mediaPlayer.pause();
                        mediaView.setEffect(grayscale);
                    };

                    mediaView.addEventFilter(MouseEvent.MOUSE_ENTERED, enterEvent);
                    mediaView.addEventFilter(GazeEvent.GAZE_ENTERED, enterEvent);

                    mediaView.addEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);
                    mediaView.addEventFilter(GazeEvent.GAZE_EXITED, exitEvent);

                    gameContext.getGazeDeviceManager().addEventFilter(mediaView);

                    grid.add(mediaView, i, j);
                }
            }

            gameContext.getChildren().add(grid);
        } else {
            noVideosFound();
        }
        stats.notifyNewRoundReady();
    }

    private void noVideosFound() {
        Text errorText = new Text(translate.getTrad("No videos found", config.getLanguage()));
        errorText.setY(dimensions.getHeight() / 2);
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
