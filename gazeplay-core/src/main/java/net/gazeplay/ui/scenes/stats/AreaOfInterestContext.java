package net.gazeplay.ui.scenes.stats;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.ui.GraphicalContext;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.VideoAttributes;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AreaOfInterestContext extends GraphicalContext<BorderPane> {

    private final List<CoordinatesTracker> movementHistory;
    private final Label timeLabel;
    private Timeline clock;
    private MediaPlayer player;
    private final List<AreaOfInterest> AOIList;
    private final List<AreaOfInterestView> AOIViewList;

    private final Color[] colors = new Color[]{
        Color.PURPLE,
        Color.WHITE,
        Color.PINK,
        Color.ORANGE,
        Color.BLUE,
        Color.RED,
        Color.CHOCOLATE
    };

    private final Configuration config;

    private Polygon currentAOIDisplay;
    private GridPane currentInfoBox;
    private Line currentLineToInfoBox;
    private double score;
    private final Pane graphicsPane;
    private Double previousInfoBoxX;
    private Double previousInfoBoxY;
    private final ArrayList<InitialAreaOfInterest> combinedAreaList;
    private final int[] areaMap;
    private boolean playing = false;

    private int iteratorAOI = 0;

    public AreaOfInterestContext(final GazePlay gazePlay, final Stats stats) {
        super(gazePlay, new BorderPane());

        movementHistory = stats.getMovementHistory();
        AOIList = stats.getAOIList();
        config = ActiveConfigurationContext.getInstance();
        graphicsPane = new Pane();

        areaMap = new int[AOIList.size()];
        Arrays.fill(areaMap, -1);

        AOIViewList = computeAOIViewList();
        combinedAreaList = computeConnectedArea();
        for (final InitialAreaOfInterest initialAreaOfInterest : combinedAreaList)
            graphicsPane.getChildren().add(initialAreaOfInterest.getAreaOfInterest());

        if (stats.getTargetAOIList() != null) {
            final ArrayList<TargetAOI> targetAOIArrayList = stats.getTargetAOIList();
            long timeTargetAreaStart;
            long timeTargetAreaEnd;
            int targetAOIIterator;
            score = 0;

            for (int i = 0; i < AOIList.size(); i++) {
                final long timeAreaStart = AOIList.get(i).getAreaStartTime();
                double maxScore = 0;

                for (targetAOIIterator = 0; targetAOIIterator < targetAOIArrayList.size(); targetAOIIterator++) {
                    timeTargetAreaStart = targetAOIArrayList.get(targetAOIIterator).getTimeStarted();
                    timeTargetAreaEnd = targetAOIArrayList.get(targetAOIIterator).getTimeEnded();

                    if (timeTargetAreaStart <= timeAreaStart && timeAreaStart <= timeTargetAreaEnd) {
                        final Shape intersect = Shape.intersect(targetAOIArrayList.get(targetAOIIterator).getPolygon(),
                            AOIViewList.get(i).getAreaOfInterest());
                        if (intersect.getBoundsInLocal().getWidth() != -1) {
                            double newMaxScore = intersect.getBoundsInLocal().getWidth() /
                                (AOIViewList.get(i).getAreaOfInterest().getBoundsInLocal().getWidth() - 1);
                            if (newMaxScore > maxScore)
                                maxScore = newMaxScore;
                        }
                    }
                }
                score += maxScore;
            }
            score /= AOIList.size();
        }

        final StackPane stackPane = new StackPane();
        if (config.isVideoRecordingEnabled()) {
            final File source;
            final File target;
            try {
                source = new File(stats.getDirectoryOfVideo() + ".avi");
                target = new File(stats.getDirectoryOfVideo() + ".mp4");

                // Audio Attributes
                final VideoAttributes videoAttributes = new VideoAttributes();
                videoAttributes.setCodec("mpeg4");

                // Encoding attributes
                final EncodingAttributes attrs = new EncodingAttributes();
                attrs.setFormat("mp4");
                attrs.setVideoAttributes(videoAttributes);

                // Encode
                final Encoder encoder = new Encoder();
                encoder.encode(new MultimediaObject(source), target, attrs);
                final Media media = new Media(target.toURI().toString());
                player = new MediaPlayer(media);
                final MediaView mediaView = new MediaView(player);
                stackPane.getChildren().add(mediaView);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        } else {
            final SavedStatsInfo savedStatsInfo = stats.getSavedStatsInfo();
            final javafx.scene.image.ImageView screenshot = new javafx.scene.image.ImageView();
            screenshot.setPreserveRatio(true);
            screenshot.setImage(new Image(savedStatsInfo.getScreenshotFile().toURI().toString()));
        }

        final GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(100);
        grid.setVgap(50);
        grid.setPadding(new Insets(50, 50, 50, 50));

        final Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        final Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);

        timeLabel = new Label();
        timeLabel.setTextFill(Color.web("#FFFFFF"));
        timeLabel.setMinSize(100, 0);
        timeLabel.setTextFill(Color.GREEN);

        final Label scoreLabel = new Label();
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", 20));
        scoreLabel.setText("Score: " + score);

        final HBox topPane;
        final VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);

        final HBox buttonBox = createButtonBox();
        if (stats.getTargetAOIList() != null)
            topPane = new HBox(scoreLabel, timeLabel, region2, buttonBox);
        else
            topPane = new HBox(timeLabel, region2, buttonBox);

        topPane.setPadding(new Insets(15, 15, 0, 15));
        topPane.setSpacing(10);
        topPane.setStyle("-fx-background-color: transparent; -fx-max-height: 80px;");

        final Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        final EventHandler<Event> aoiEvent = e -> {
            final StatsContext statsContext;
            statsContext = StatsContextFactory.newInstance(gazePlay, stats);

            if (config.isVideoRecordingEnabled())
                player.stop();
            this.clear();
            gazePlay.onDisplayStats(statsContext);
        };

        final HomeButton homeButton = new HomeButton(screenDimension);
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, aoiEvent);

        final HBox homeBox = new HBox(homeButton);
        homeBox.setStyle("-fx-background-color: transparent; -fx-max-height: 100px; -fx-max-width: 100px;");
        homeBox.setPadding(new Insets(10, 10, 10, 10));

        stackPane.getChildren().add(graphicsPane);
        stackPane.getChildren().add(topPane);
        stackPane.getChildren().add(homeBox);
        StackPane.setAlignment(homeBox, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(topPane, Pos.TOP_CENTER);

        graphicsPane.setPickOnBounds(false);
        graphicsPane.setStyle("-fx-background-color: transparent;");
        root.setCenter(stackPane);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
    }

    HBox createButtonBox() {
        final Button slowBtn10 = new Button("10X Slow ");
        final Button slowBtn8 = new Button("8X Slow ");
        final Button slowBtn5 = new Button("5X Slow ");
        final Button playBtn = new Button("Play ");
        final Button cancelBtn = new Button("Cancel ");

        playBtn.setPrefSize(100, 20);
        slowBtn5.setPrefSize(100, 20);
        slowBtn8.setPrefSize(100, 20);
        slowBtn10.setPrefSize(100, 20);
        cancelBtn.setPrefSize(100, 20);

        final double baseProgressRate = 0.60;

        playBtn.setOnAction(e -> playButtonPressed(baseProgressRate));
        slowBtn5.setOnAction(e -> playButtonPressed(baseProgressRate * 5));
        slowBtn8.setOnAction(e -> playButtonPressed(baseProgressRate * 8));
        slowBtn10.setOnAction(e -> playButtonPressed(baseProgressRate * 10));
        cancelBtn.setOnAction(e -> {
            if (playing)
                playing = false;
            graphicsPane.getChildren().removeAll();
            addAllInitialArea();
        });

        final HBox buttonBox = new HBox(cancelBtn, playBtn, slowBtn5, slowBtn8, slowBtn10);
        buttonBox.setSpacing(10);
        buttonBox.setFillHeight(true);
        buttonBox.setPadding(new Insets(10, 10, 10, 10));

        return buttonBox;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    private void playButtonPressed(final double progressRate) {
        if (!playing) {
            playing = true;
            for (final InitialAreaOfInterest areaOfInterestProps : combinedAreaList)
                graphicsPane.getChildren().remove(areaOfInterestProps.getAreaOfInterest());
            graphicsPane.getChildren().remove(currentInfoBox);

            if (config.isVideoRecordingEnabled()) {
                player.stop();
                player.play();
            }
            iteratorAOI = 0;
            plotMovement(0, graphicsPane, progressRate);
            final long startTime = System.currentTimeMillis();
            clock = new Timeline(new KeyFrame(Duration.ZERO, f -> {
                final long theTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
                timeLabel.setText(theTime + "");
            }), new KeyFrame(Duration.seconds(1)));
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();
        }
    }

    private void plotMovement(final int movementIndex, final Pane graphicsPane, final double progressRate) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                final CoordinatesTracker coordinatesTracker = movementHistory.get(movementIndex);
                final Circle circle;
                if (movementHistory.get(movementIndex).getIntervalTime() > 11
                    && movementHistory.get(movementIndex).getDistance() < 20) {
                    circle = new Circle(coordinatesTracker.getXValue(), coordinatesTracker.getYValue(), 4);
                    circle.setStroke(Color.LIGHTYELLOW);
                    circle.setFill(Color.ORANGERED);
                } else {
                    circle = new Circle(coordinatesTracker.getXValue(), coordinatesTracker.getYValue(), 3);
                    circle.setStroke(Color.LIGHTGREEN);
                    circle.setFill(Color.GREEN);
                }

                Platform.runLater(() -> {
                    if (iteratorAOI < AOIList.size()) {
                        if (movementIndex == AOIList.get(iteratorAOI).getStartingIndex()) {
                            currentInfoBox = AOIViewList.get(iteratorAOI).getInfoBox();
                            currentLineToInfoBox = AOIViewList.get(iteratorAOI).getLineToInfoBox();
                            currentAOIDisplay = AOIViewList.get(iteratorAOI).getAreaOfInterest();
                            graphicsPane.getChildren().add(currentAOIDisplay);
                            graphicsPane.getChildren().add(currentInfoBox);
                            graphicsPane.getChildren().add(currentLineToInfoBox);
                        }
                        if (movementIndex == AOIList.get(iteratorAOI).getEndingIndex()) {
                            graphicsPane.getChildren().remove(currentAOIDisplay);
                            graphicsPane.getChildren().remove(currentInfoBox);
                            graphicsPane.getChildren().remove(currentLineToInfoBox);
                            iteratorAOI++;
                        }
                    }

                    graphicsPane.getChildren().add(circle);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> graphicsPane.getChildren().remove(circle));
                        }
                    }, 2000);

                    if (movementIndex < movementHistory.size() - 1 && playing) {
                        plotMovement(movementIndex + 1, graphicsPane, progressRate);
                    } else {
                        clock.stop();
                        timeLabel.setText("");
                        playing = false;
                        graphicsPane.getChildren().removeAll(); // Reset the view to how it was before playback started.
                        addAllInitialArea();
                    }
                });
            }
        }, (long) (movementHistory.get(movementIndex).getIntervalTime() * progressRate));
    }

    void addAllInitialArea() {
        for (final InitialAreaOfInterest initialAreaOfInterest : combinedAreaList)
            graphicsPane.getChildren().add(initialAreaOfInterest.getAreaOfInterest());
    }

    /**
     * Determines how the Info Box will be shown for a particular area of interest.
     *
     * @param areaOfInterest the area of interest
     * @return the created {@code AreaOfInterestView} object
     */
    AreaOfInterestView calculateAOIView(final AreaOfInterest areaOfInterest, int index) {
        final Polygon AOIPolygon = new Polygon();
        AOIPolygon.getPoints().addAll(areaOfInterest.getConvexPoints());
        AOIPolygon.setFill(Color.rgb(255, 0, 0, areaOfInterest.getPriority()));
        AOIPolygon.setStroke(colors[index % 7]);

        final GridPane infoBox = makeInfoBox(areaOfInterest.getID(),
            new DecimalFormat("##.###s").format(areaOfInterest.getTTFF()),
            new DecimalFormat("##.###s").format(areaOfInterest.getTimeSpent()),
            areaOfInterest.getFixations(), 0);

        final Dimension2D screenDimension = getGazePlay().getCurrentScreenDimensionSupplier().get();
        final double screenWidthCenter = screenDimension.getWidth() / 2;
        final double widthOfArea = AOIPolygon.getBoundsInLocal().getWidth();

        int centerX = areaOfInterest.getCenterX();
        int centerY = areaOfInterest.getCenterY();

        final Line line = new Line();
        line.setStartY(centerY);
        line.setEndY(centerY);
        line.setStroke(Color.YELLOW);

        if (centerX > screenWidthCenter) {
            // will display infobox on the left side
            infoBox.setLayoutX(centerX - widthOfArea - 290);
            line.setStartX(AOIPolygon.getBoundsInLocal().getMinX());
            line.setEndX(centerX - widthOfArea - 30);
        } else {
            // will display infobox on the right
            infoBox.setLayoutX(centerX + widthOfArea + 100);
            line.setEndX(centerX + widthOfArea + 100);
            line.setStartX(AOIPolygon.getBoundsInLocal().getMaxX());
        }

        infoBox.setLayoutY(centerY - 60);
        infoBox.setStyle("-fx-background-color: rgba(255,255,153, 0.4);");
        return new AreaOfInterestView(infoBox, line, AOIPolygon);
    }

    /**
     * Will create a {@link GridPane} with the supplied values and their respective labels.
     *
     * @param ID        the Area Of Interest ID
     * @param TTFF      the Time To First Fixation
     * @param timeSpent the time spent in the AOI
     * @param fixations the number of fixations
     * @param revisits  the number of revisits
     * @return the info box pane with formatted labels and values
     */
    static GridPane makeInfoBox(
        final String ID,
        final String TTFF,
        final String timeSpent,
        final int fixations,
        final int revisits
    ) {
        final GridPane infoBox = new GridPane();
        infoBox.setHgap(10);
        infoBox.setVgap(10);
        infoBox.setPadding(new Insets(10, 10, 10, 10));

        final Text boxID = new Text(ID);
        final Text TTFFLabel = new Text("TTFF:");
        final Text TTFFValue = new Text(TTFF);
        final Text timeSpentLabel = new Text("Time Spent:");
        final Text timeSpentLabelValue = new Text(timeSpent);
        final Text fixationsLabel = new Text("Fixations:");
        final Text fixationsValue = new Text(String.valueOf(fixations));

        infoBox.add(boxID, 1, 0);
        infoBox.add(TTFFLabel, 0, 2);
        infoBox.add(TTFFValue, 2, 2);
        infoBox.add(timeSpentLabel, 0, 3);
        infoBox.add(timeSpentLabelValue, 2, 3);
        infoBox.add(fixationsLabel, 0, 4);
        infoBox.add(fixationsValue, 2, 4);

        if (revisits != 0) {
            final Text revisitsLabel = new Text("Revisits: ");
            final Text revisitsValue = new Text(String.valueOf(revisits));
            infoBox.add(revisitsLabel, 0, 5);
            infoBox.add(revisitsValue, 2, 5);
        }
        return infoBox;
    }

    private ArrayList<AreaOfInterestView> computeAOIViewList() {
        final ArrayList<AreaOfInterestView> AOIViewList = new ArrayList<>();

        for (int i = 0; i < AOIList.size(); i++)
            AOIViewList.add(calculateAOIView(AOIList.get(i), i));

        return AOIViewList;
    }

    private ArrayList<InitialAreaOfInterest> computeConnectedArea() {
        for (int i = 0; i < AOIList.size(); i++) {
            AreaOfInterestView currentAOIView = AOIViewList.get(i);

            final double areaSize = currentAOIView.getAreaOfInterest().getBoundsInLocal().getWidth() *
                currentAOIView.getAreaOfInterest().getBoundsInLocal().getHeight();

            for (int j = 0; j < AOIList.size(); j++) {
                if (AOIViewList.get(j).getAreaOfInterest() != currentAOIView.getAreaOfInterest()) {
                    final Shape intersect = Shape.intersect(currentAOIView.getAreaOfInterest(),
                        AOIViewList.get(j).getAreaOfInterest());

                    if (intersect.getBoundsInLocal().getWidth() != -1) {
                        final double toCompareAreaSize = intersect.getBoundsInLocal().getWidth() *
                            intersect.getBoundsInLocal().getHeight();
                        final double combinationThreshHold = 0.70;

                        if ((toCompareAreaSize / areaSize) > combinationThreshHold) {
                            if (areaMap[j] != -1) {
                                areaMap[i] = areaMap[j];
                            } else {
                                areaMap[i] = i;
                                areaMap[j] = i;
                            }
                            break; // We break here as we have finished calculating this pair of areas.
                        }
                    }
                }
            }
        }

        final ArrayList<InitialAreaOfInterest> listOfCombinedPolygons = new ArrayList<>();

        for (int i = 0; i < AOIList.size(); i++) {
            GridPane infoBox = new GridPane();
            Shape tempPolygon = null;
            AreaOfInterest currentAOI = AOIList.get(i);
            AreaOfInterestView currentAOIView = AOIViewList.get(i);

            if (areaMap[i] == -1) {
                tempPolygon = currentAOIView.getAreaOfInterest();
                infoBox = currentAOIView.getInfoBox();
            }

            if (areaMap[i] == i) {
                tempPolygon = currentAOIView.getAreaOfInterest();
                final String aoiID = currentAOI.getID();
                double ttff = currentAOI.getTTFF();
                double timeSpent = currentAOI.getTimeSpent();
                int fixations = currentAOI.getFixations();
                int revisit = 1;

                for (int j = i + 1; j < AOIList.size(); j++) {
                    if (areaMap[j] == i) {
                        ttff += AOIList.get(j).getTTFF();
                        revisit++;
                        timeSpent += AOIList.get(j).getTimeSpent();
                        fixations += AOIList.get(j).getTimeSpent();
                        tempPolygon = Shape.union(tempPolygon, AOIViewList.get(j).getAreaOfInterest());
                        tempPolygon.setFill(Color.rgb(249, 166, 2, 0.15));
                    }
                }
                infoBox = makeInfoBox(aoiID, new DecimalFormat("##.###s").format(ttff),
                    new DecimalFormat("##.###s").format(timeSpent), fixations, revisit);
            }

            if (tempPolygon != null) {
                final Shape finalTempPolygon = tempPolygon;
                final GridPane finalInfoBox = infoBox;
                final int finalI = i;

                tempPolygon.setOnMouseEntered(event -> {
                    if (!playing) {
                        final double opacity = AOIList.get(finalI).getPriority() + 0.15;
                        final Color color = (areaMap[finalI] == -1) ?
                            Color.rgb(255, 0, 0, opacity) :
                            Color.rgb(249, 166, 2, opacity);
                        finalTempPolygon.setFill(color);

                        previousInfoBoxX = finalInfoBox.getLayoutX();
                        previousInfoBoxY = finalInfoBox.getLayoutY();
                        currentInfoBox = new GridPane();
                        finalInfoBox.setLayoutY(0);
                        finalInfoBox.setLayoutX(0);
                        finalInfoBox.setStyle("-fx-background-color: rgba(255,255,153, 0.4);");
                        currentInfoBox = finalInfoBox;
                        graphicsPane.getChildren().add(finalInfoBox);
                    }
                });

                tempPolygon.setOnMouseExited(event -> {
                    graphicsPane.getChildren().remove(currentInfoBox);
                    finalInfoBox.setLayoutY(previousInfoBoxY);
                    finalInfoBox.setLayoutX(previousInfoBoxX);

                    final double opacity = AOIList.get(finalI).getPriority();
                    final Color color = (areaMap[finalI] == -1) ?
                        Color.rgb(255, 0, 0, opacity) :
                        Color.rgb(249, 166, 2, opacity);
                    finalTempPolygon.setFill(color);
                });

                int colorIterator = i % 7;
                tempPolygon.setStroke(colors[colorIterator]);
                listOfCombinedPolygons.add(new InitialAreaOfInterest(tempPolygon, infoBox));
            }
        }
        return listOfCombinedPolygons;
    }
}
