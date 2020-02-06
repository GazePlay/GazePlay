package net.gazeplay.games.videogrid;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
public class VideoGrid implements GameLifeCycle {

    private final static int GAP = 3;
    private final IGameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Configuration config;
    private final Multilinguism translate;

    private final int nbLines;
    private final int nbColumns;
    private final GridPane grid;
    private final File videoFolder;
    private final Random random;
    private final List<String> compatibleFileTypes;

    private final ColorAdjust greyscale;

    public VideoGrid(final IGameContext gameContext, final Stats stats, final int nbColumns, final int nbLines) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = gameContext.getConfiguration();
        this.random = new Random();
        this.translate = Multilinguism.getSingleton();

        grid = new GridPane();
        grid.setHgap(GAP);
        grid.setVgap(GAP);
        videoFolder = new File(config.getVideoFolder());
        compatibleFileTypes = new ArrayList<>(Arrays.asList("mp4", "m4a", "m4v"));

        // Greyscale effect for out of focus videos
        greyscale = new ColorAdjust();
        greyscale.setSaturation(-1);
    }

    @Override
    public void launch() {

        if (videoFolder.isDirectory()) {

            final File[] files = videoFolder.listFiles(f -> {
                // Filter out non compatible files
                return f.isFile() && f.canRead() && !f.isDirectory()
                    && compatibleFileTypes.contains(FilenameUtils.getExtension(f.getName()));
            });
            if (files == null || files.length == 0) {
                noVideoFound();
                return;
            }

            // Separate list where we will pick files from randomly. To reduce the number of duplicates
            final List<File> filesChooseFrom = Arrays.asList(files);

            for (int i = 0; i < nbColumns; i++) {
                for (int j = 0; j < nbLines; j++) {
                    // If there aren't enough videos to fill the grid, we use duplicates
                    if (filesChooseFrom.size() == 0) {
                        filesChooseFrom.addAll(Arrays.asList(files));
                    }
                    // Picking a random file from the array, and removing it
                    final int index = random.nextInt(filesChooseFrom.size());
                    final File file = filesChooseFrom.remove(index);
                    // Creating the mediaplayer
                    final Media media = new Media(file.toURI().toString());
                    final MediaPlayer mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.volumeProperty().bind(config.getEffectsVolumeProperty());
                    // Loop when the video is over
                    mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
                    // Creating mediaview, the graphic container which plays the mediaplayer's content
                    final MediaView mediaView = new MediaView();
                    mediaView.setMediaPlayer(mediaPlayer);
                    mediaView.setFitHeight(dimensions.getHeight() / nbLines - GAP);
                    mediaView.setFitWidth(dimensions.getWidth() / nbColumns - GAP);
                    mediaView.setEffect(greyscale);

                    // Play only when the mouse or gaze is on the video, otherwise add a greyscale effect to the video
                    final EventHandler<Event> enterEvent = (Event event) -> {
                        mediaPlayer.play();
                        mediaView.setEffect(null);
                    };
                    final EventHandler<Event> exitEvent = (Event event) -> {
                        mediaPlayer.pause();
                        mediaView.setEffect(greyscale);
                    };

                    mediaView.addEventFilter(MouseEvent.MOUSE_ENTERED, enterEvent);
                    mediaView.addEventFilter(GazeEvent.GAZE_ENTERED, enterEvent);

                    mediaView.addEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);
                    mediaView.addEventFilter(GazeEvent.GAZE_EXITED, exitEvent);

                    gameContext.getGazeDeviceManager().addEventFilter(mediaView);

                    // Adding the video to a stack pane with a grey background, this helps centering the video inside
                    // the grid square
                    final StackPane pane = new StackPane();
                    pane.setAlignment(Pos.CENTER);// j == 0?Pos.BOTTOM_CENTER:j==nbLines-1?Pos.TOP_CENTER:Pos.CENTER);
                    pane.getChildren().addAll(new Rectangle(dimensions.getWidth() / nbColumns - GAP,
                        dimensions.getHeight() / nbLines - GAP, Color.grayRgb(50)), mediaView);
                    grid.add(pane, i, j);

                    // When a video is larger than 1920x1080, it won't work, and sends an error
                    mediaPlayer.setOnError(() -> {
                        final Text errorText = new Text((String.format(
                            translate.getTrad("File %s is not supported", config.getLanguage()), file.getName())));
                        errorText.setFill(Color.WHITE);
                        errorText.setTextAlignment(TextAlignment.CENTER);
                        errorText.setWrappingWidth(dimensions.getWidth() / nbColumns);
                        pane.getChildren().add(errorText);
                    });
                }
            }
            gameContext.getChildren().add(grid);
        } else {
            noVideoFound();
            return;
        }
        stats.notifyNewRoundReady();
    }

    private void noVideoFound() {
        final I18NText errorText = new I18NText(gameContext.getTranslator(), "No video found");
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
