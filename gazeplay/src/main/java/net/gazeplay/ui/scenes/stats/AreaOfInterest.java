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
import javafx.geometry.Point2D;
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
import net.gazeplay.commons.ui.I18NLabel;
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
public class AreaOfInterest extends GraphicalContext<BorderPane> {

    private final List<CoordinatesTracker> movementHistory;
    private final Label timeLabel;
    private Timeline clock;
    private MediaPlayer player;
    private final List<AreaOfInterestProps> allAOIList;
    private List<CoordinatesTracker> areaOfInterestList;
    private final Color[] colors;
    private final Configuration config;
    private int intereatorAOI;
    private Polygon currentAreaDisplay;
    private GridPane currentInfoBox;
    private Line currentLineToInfoBox;
    private double score;
    private int colorIterator;
    private final Pane graphicsPane;
    private double progressRate = 1;
    private Double previousInfoBoxX;
    private Double previousInfoBoxY;
    private final ArrayList<InitialAreaOfInterestProps> combinedAreaList;
    private final int[] areaMap;
    private boolean playing = false;
    private double highestFixationTime;

    public AreaOfInterest(final GazePlay gazePlay, final Stats stats) {
        super(gazePlay, new BorderPane());
        colors = new Color[]{Color.PURPLE, Color.WHITE, Color.PINK, Color.ORANGE, Color.BLUE, Color.RED,
            Color.CHOCOLATE};
        config = ActiveConfigurationContext.getInstance();
        final GridPane grid = new GridPane();
        final StackPane stackPane = new StackPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(100);
        grid.setVgap(50);
        highestFixationTime = 0;
        intereatorAOI = 0;
        grid.setPadding(new Insets(50, 50, 50, 50));
        allAOIList = new ArrayList<>();
        final I18NLabel screenTitleText = new I18NLabel(gazePlay.getTranslator(), "AreaOfInterest");
        screenTitleText.setId("title");
        final HBox topPane;
        final VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);
        graphicsPane = new Pane();
        areaOfInterestList = new ArrayList<>();
        movementHistory = stats.getMovementHistoryWithTime();
        this.dataTreatment();
        for (int i = 0; i < movementHistory.size(); i++) {
            calculateAreaOfInterest(i, stats.getStartTime());
        }

        areaMap = new int[allAOIList.size()];
        Arrays.fill(areaMap, -1);
        combinedAreaList = computeConnectedArea();
        if (highestFixationTime != 0) {
            for (final AreaOfInterestProps areaOfInterestProps : allAOIList) {
                final double priority = areaOfInterestProps.getInfoBoxProps().getTimeSpent() / highestFixationTime * 0.6
                    + 0.10;
                areaOfInterestProps.getAreaOfInterest().setFill(Color.rgb(255, 0, 0, priority));
                areaOfInterestProps.setPriority(priority);
            }
        }
        for (final InitialAreaOfInterestProps initialAreaOfInterestProps : combinedAreaList) {
            graphicsPane.getChildren().add(initialAreaOfInterestProps.getAreaOfInterest());
        }

        if (stats.getTargetAOIList() != null) {
            calculateTargetAOI(stats.getTargetAOIList());
            final ArrayList<TargetAOI> targetAOIArrayList = stats.getTargetAOIList();

            long timeTargetAreaStart;
            long timeTargetAreaEnd;
            score = 0;

            for (final AreaOfInterestProps areaOfInterestProps : allAOIList) {
                int targetAOIIterator = 0;
                if (targetAOIIterator < targetAOIArrayList.size()) {
                    timeTargetAreaStart = targetAOIArrayList.get(targetAOIIterator).getTimeStarted();
                    timeTargetAreaEnd = targetAOIArrayList.get(targetAOIIterator).getTimeStarted()
                        + targetAOIArrayList.get(targetAOIIterator).getDuration();
                    final long timeAreaStart = areaOfInterestProps.getAreaStartTime();
                    final long timeAreaEnd = areaOfInterestProps.getAreaEndTime();

                    if (timeTargetAreaStart <= timeAreaStart) {

                        final Shape intersect = Shape.intersect(targetAOIArrayList.get(targetAOIIterator).getPolygon(),
                            areaOfInterestProps.getAreaOfInterest());
                        if (intersect.getBoundsInLocal().getWidth() != -1) {
                            score += (areaOfInterestProps.getAreaOfInterest().getBoundsInLocal().getWidth() - 1)
                                / intersect.getBoundsInLocal().getWidth();
                        }
                    }

                    if (timeAreaEnd > timeTargetAreaEnd) {
                        targetAOIIterator++;
                    }
                }
            }
            score = score / allAOIList.size();
        }

        final Button slowBtn10 = new Button("10X Slow ");
        final Button slowBtn8 = new Button("8X Slow ");
        final Button slowBtn5 = new Button("5X Slow ");
        final Button playBtn = new Button("Play ");
        final Button stopBtn = new Button("Stop ");
        final Button cancelBtn = new Button("Cancel ");

        playBtn.setPrefSize(100, 20);
        slowBtn5.setPrefSize(100, 20);
        slowBtn8.setPrefSize(100, 20);
        slowBtn10.setPrefSize(100, 20);
        stopBtn.setPrefSize(100, 20);
        cancelBtn.setPrefSize(100, 20);

        playBtn.setOnAction(e -> {
            progressRate = 0.60;
            // if (config.isVideoRecordingEnabled())
            // player.setRate(1.0);
            playButtonPressed();
        });
        slowBtn5.setOnAction(e -> {
            // if (config.isVideoRecordingEnabled())
            // player.setRate(0.5);

            playButtonPressed();
        });
        slowBtn8.setOnAction(e -> {
            // if (config.isVideoRecordingEnabled())
            // player.setRate(0.2);

            playButtonPressed();
        });

        slowBtn10.setOnAction(e -> {
            // if (config.isVideoRecordingEnabled())
            // player.setRate(0.1);

            playButtonPressed();
        });
        cancelBtn.setOnAction(e -> {
            if (playing) {
                playing = false;
                graphicsPane.getChildren().removeAll();
                addAllInitialArea();
            }
        });
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
            // stackPane.getChildren().add(screenshot);
        }

        final EventHandler<Event> AOIEvent = e -> {

            final StatsContext statsContext;
            statsContext = StatsContext.newInstance(gazePlay, stats);

            if (config.isVideoRecordingEnabled()) {
                player.stop();
            }
            this.clear();
            gazePlay.onDisplayStats(statsContext);
        };

        final Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        final Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);
        final HBox buttonBox = new HBox(cancelBtn, playBtn, slowBtn5, slowBtn8, slowBtn10);
        buttonBox.setSpacing(10);
        buttonBox.setFillHeight(true);
        buttonBox.setPadding(new Insets(10, 10, 10, 10));

        timeLabel = new Label();
        timeLabel.setTextFill(Color.web("#FFFFFF"));
        timeLabel.setMinSize(100, 0);
        timeLabel.setTextFill(Color.GREEN);

        final Label scoreLabel = new Label();
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", 20));
        scoreLabel.setText("Score: " + score);
        if (stats.getTargetAOIList() != null) {
            topPane = new HBox(scoreLabel, timeLabel, region2, buttonBox);

        } else {
            topPane = new HBox(timeLabel, region2, buttonBox);

        }
        topPane.setPadding(new Insets(15, 15, 0, 15));
        topPane.setSpacing(10);
        topPane.setStyle("-fx-background-color: transparent; -fx-max-height: 80px;");

        final Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        final HomeButton homeButton = new HomeButton(screenDimension);
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, AOIEvent);

        final HBox homebox = new HBox(homeButton);
        homebox.setStyle("-fx-background-color: transparent; -fx-max-height: 100px; -fx-max-width: 100px;");
        homebox.setPadding(new Insets(10, 10, 10, 10));
        stackPane.getChildren().add(graphicsPane);
        stackPane.getChildren().add(topPane);
        stackPane.getChildren().add(homebox);
        StackPane.setAlignment(homebox, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(topPane, Pos.TOP_CENTER);
        graphicsPane.setPickOnBounds(false);
        graphicsPane.setStyle("-fx-background-color: transparent;");
        root.setCenter(stackPane);
        root.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    private void dataTreatment() {
        // treating the data, post processing to take performance constraint of during the data collection
        movementHistory.get(0).setDistance(0);
        for (int i = 1; i < movementHistory.size(); i++) {
            final double x = Math.pow(movementHistory.get(i).getXValue() - movementHistory.get(i - 1).getXValue(), 2);
            final double y = Math.pow(movementHistory.get(i).getYValue() - movementHistory.get(i - 1).getYValue(), 2);
            final double distance = Math.sqrt(x + y);
            movementHistory.get(i).setDistance(distance);
        }
    }

    private void plotMovement(final int movementIndex, final Pane graphicsPane) {
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
                    if (intereatorAOI < allAOIList.size()) {
                        if (movementIndex == allAOIList.get(intereatorAOI).getStartingIndex()) {
                            currentInfoBox = allAOIList.get(intereatorAOI).getInfoBoxProps().getInfoBox();
                            currentAreaDisplay = allAOIList.get(intereatorAOI).getAreaOfInterest();
                            currentLineToInfoBox = allAOIList.get(intereatorAOI).getInfoBoxProps().getLineToInfoBox();
                            graphicsPane.getChildren().add(currentAreaDisplay);
                            graphicsPane.getChildren().add(currentInfoBox);
                            graphicsPane.getChildren().add(currentLineToInfoBox);
                        }
                        if (movementIndex == allAOIList.get(intereatorAOI).getEndingIndex()) {
                            graphicsPane.getChildren().remove(currentAreaDisplay);
                            graphicsPane.getChildren().remove(currentInfoBox);
                            graphicsPane.getChildren().remove(currentLineToInfoBox);
                            intereatorAOI++;
                        }
                    }
                    graphicsPane.getChildren().add(circle);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> graphicsPane.getChildren().remove(circle));
                        }
                    }, 2000);
                    if (movementIndex != movementHistory.size() - 1 && playing) {
                        plotMovement(movementIndex + 1, graphicsPane);
                    } else {
                        clock.stop();
                        //addAllInitialArea();
                        playing = false;
                        // if (config.isVideoRecordingEnabled())
                        // {
                        // stats.endVideoRecording();
                        // }
                    }
                });
            }
        }, (long) (movementHistory.get(movementIndex).getIntervalTime() * progressRate));
    }

    private void addAllInitialArea() {
        for (final InitialAreaOfInterestProps initialAreaOfInterestProps : combinedAreaList) {
            graphicsPane.getChildren().add(initialAreaOfInterestProps.getAreaOfInterest());
        }
    }

    private void calculateAreaOfInterest(final int index, final double startTime) {
        if (index != 0) {
            final double x1 = movementHistory.get(index).getXValue();
            final double y1 = movementHistory.get(index).getYValue();
            final double x2 = movementHistory.get(index - 1).getXValue();
            final double y2 = movementHistory.get(index - 1).getYValue();
            final double eDistance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            if (eDistance < 120 && movementHistory.get(index).getIntervalTime() > 10) {
                if (index == 1) {
                    areaOfInterestList.add(movementHistory.get(0));
                }
                areaOfInterestList.add(movementHistory.get(index));
            } else if (areaOfInterestList.size() != 0) {
                if (areaOfInterestList.size() > 2) {
                    final double AreaStartTime = areaOfInterestList.get(0).getTimeStarted();
                    final double AreaEndTime = areaOfInterestList.get(areaOfInterestList.size() - 1).getTimeStarted()
                        + areaOfInterestList.get(areaOfInterestList.size() - 1).getIntervalTime();
                    final double TTFF = (AreaStartTime - startTime) / 1000.0;
                    final double timeSpent = (AreaEndTime - AreaStartTime) / 1000.0;
                    if (timeSpent > highestFixationTime) {
                        highestFixationTime = timeSpent;
                    }
                    int centerX = 0;
                    int centerY = 0;
                    final int movementHistoryEndingIndex = index - 1;
                    final int movementHistoryStartingIndex = movementHistoryEndingIndex - areaOfInterestList.size();
                    final Point2D[] point2DS = new Point2D[areaOfInterestList.size()];
                    for (int i = 0; i < areaOfInterestList.size(); i++) {
                        centerX += areaOfInterestList.get(i).getXValue();
                        centerY += areaOfInterestList.get(i).getYValue();
                        point2DS[i] = new Point2D(areaOfInterestList.get(i).getXValue(),
                            areaOfInterestList.get(i).getYValue());
                    }
                    final Double[] polygonPoints;
                    if (config.getConvexHullDisabledProperty().getValue()) {
                        polygonPoints = calculateConvexHull(point2DS, areaOfInterestList.size());
                    } else {
                        polygonPoints = calculateRectangle(point2DS);
                    }
                    final Polygon areaOfInterest = new Polygon();
                    areaOfInterest.getPoints().addAll(polygonPoints);
                    colorIterator = index % 7;
                    areaOfInterest.setStroke(colors[colorIterator]);
                    centerX = centerX / areaOfInterestList.size();
                    centerY = centerY / areaOfInterestList.size();
                    final InfoBoxProps infoBox = calculateInfoBox("AOI number " + (allAOIList.size() + 1), TTFF, timeSpent,
                        areaOfInterestList.size(), centerX, centerY, areaOfInterest);
                    final AreaOfInterestProps areaOfInterestProps = new AreaOfInterestProps(areaOfInterestList, centerX,
                        centerY, polygonPoints, point2DS, movementHistoryStartingIndex, movementHistoryEndingIndex,
                        areaOfInterest, infoBox, (long) AreaStartTime, (long) AreaEndTime);
                    allAOIList.add(areaOfInterestProps);
                }
                areaOfInterestList = new ArrayList<>();
            }
        }

    }

    private InfoBoxProps calculateInfoBox(final String aoiID, final double TTFF, final double TimeSpent, final int Fixation, final int centerX,
                                          final int centerY, final Polygon currentAreaDisplay) {

        final GridPane infoBox = makeInfoBox(aoiID, new DecimalFormat("##.###s").format(TTFF),
            new DecimalFormat("##.###s").format(TimeSpent), Fixation + "", 0);

        final Dimension2D screenDimension = getGazePlay().getCurrentScreenDimensionSupplier().get();
        final double screenWidthCenter = screenDimension.getWidth() / 2;
        final double widthOfArea = currentAreaDisplay.getBoundsInLocal().getWidth();

        final Line line = new Line();
        line.setStartY(centerY);
        line.setEndY(centerY);
        line.setStroke(Color.YELLOW);
        if (centerX > screenWidthCenter) {
            infoBox.setLayoutX(centerX - widthOfArea - 290);
            line.setStartX(currentAreaDisplay.getBoundsInLocal().getMinX());
            line.setEndX(centerX - widthOfArea - 30);
            // will display infobox on the left side
        } else {
            infoBox.setLayoutX(centerX + widthOfArea + 100);
            line.setEndX(centerX + widthOfArea + 100);
            line.setStartX(currentAreaDisplay.getBoundsInLocal().getMaxX());
            // will display infobox on the right
        }
        infoBox.setLayoutY(centerY - 60);
        infoBox.setStyle("-fx-background-color: rgba(255,255,153, 0.4);");
        return new InfoBoxProps(infoBox, line, aoiID, TTFF, TimeSpent, Fixation);
    }

    private Double[] calculateRectangle(final Point2D[] point2D) {
        double leftPoint = point2D[0].getX();
        double rightPoint = point2D[0].getX();
        double topPoint = point2D[0].getY();
        double bottomPoint = point2D[0].getY();
        for (int i = 1; i < point2D.length; i++) {
            if (point2D[i].getX() < leftPoint) {
                leftPoint = point2D[i].getX();
            }
            if (point2D[i].getX() > rightPoint) {
                rightPoint = point2D[i].getX();
            }
            if (point2D[i].getY() > topPoint) {
                topPoint = point2D[i].getY();
            }
            if (point2D[i].getY() < bottomPoint) {
                bottomPoint = point2D[i].getY();
            }
        }
        final Double[] squarePoints = new Double[8];
        final int bias = 15;
        squarePoints[0] = leftPoint - bias;
        squarePoints[1] = topPoint + bias;
        squarePoints[2] = rightPoint + bias;
        squarePoints[3] = topPoint + bias;
        squarePoints[4] = rightPoint + bias;
        squarePoints[5] = bottomPoint - bias;
        squarePoints[6] = leftPoint - bias;
        squarePoints[7] = bottomPoint - bias;
        return squarePoints;
    }

    private int orientation(final Point2D p1, final Point2D p2, final Point2D p3) {

        final int val = (int) ((p2.getY() - p1.getY()) * (p3.getX() - p2.getX())
            - (p2.getX() - p1.getX()) * (p3.getY() - p2.getY()));

        if (val == 0) {
            return 0;
        }

        return (val > 0) ? 1 : 2;
    }

    private Double[] calculateConvexHull(final Point2D[] points, final int n) {
        final ArrayList<Double> convexHullPoints = new ArrayList<>();
        final Vector<Point2D> hull = new Vector<>();
        int l = 0;
        for (int i = 1; i < n; i++) {
            if (points[i].getX() < points[l].getX()) {
                l = i;
            }
        }
        int point = l, q;
        do {
            hull.add(points[point]);
            q = (point + 1) % n;
            for (int i = 0; i < n; i++) {
                if (orientation(points[point], points[i], points[q]) == 2) {
                    q = i;
                }
            }
            point = q;
        } while (point != l);
        for (final Point2D temp : hull) {
            convexHullPoints.add(temp.getX());
            convexHullPoints.add(temp.getY());
        }
        final Double[] pointsToReturn = new Double[convexHullPoints.size()];
        for (int i = 0; i < convexHullPoints.size(); i++) {
            pointsToReturn[i] = convexHullPoints.get(i);
        }
        return pointsToReturn;
    }

    private void calculateTargetAOI(final ArrayList<TargetAOI> targetAOIArrayList) {
        for (final TargetAOI targetAOI : targetAOIArrayList) {
            log.debug("The target is at " + targetAOI.getXValue() + " Y " + targetAOI.getYValue());
            final int radius = targetAOI.getAreaRadius();
            final Point2D[] point2D = {new Point2D(targetAOI.getXValue() - 100, targetAOI.getYValue()),
                new Point2D(targetAOI.getXValue() + radius, targetAOI.getYValue() + 100),
                new Point2D(targetAOI.getXValue(), targetAOI.getYValue() - radius),
                new Point2D(targetAOI.getXValue() + radius, targetAOI.getYValue() - radius)};
            final Double[] polygonPoints;
            polygonPoints = calculateRectangle(point2D);
            final Polygon targetArea;
            targetArea = new Polygon();
            targetArea.getPoints().addAll(polygonPoints);
            targetArea.setFill(Color.rgb(255, 255, 255, 0.4));
            targetAOI.setPolygon(targetArea);
        }
    }

    private void playButtonPressed() {
        if (!playing) {
            playing = true;
            for (final InitialAreaOfInterestProps areaOfInterestProps : combinedAreaList) {
                graphicsPane.getChildren().remove(areaOfInterestProps.getAreaOfInterest());
            }
            graphicsPane.getChildren().remove(currentInfoBox);

            if (config.isVideoRecordingEnabled()) {
                player.stop();
                player.play();
            }
            intereatorAOI = 0;
            plotMovement(0, graphicsPane);
            final long startTime = System.currentTimeMillis();
            clock = new Timeline(new KeyFrame(Duration.ZERO, f -> {
                final long theTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
                timeLabel.setText(theTime + "");
            }), new KeyFrame(Duration.seconds(1)));
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();
        }
    }

    private GridPane makeInfoBox(final String aoiID, final String TTFF, final String TimeSpent, final String Fixation, final int revisit) {
        final GridPane infoBox = new GridPane();
        infoBox.setHgap(10);
        infoBox.setVgap(10);
        infoBox.setPadding(new Insets(10, 10, 10, 10));
        final Text boxID = new Text(aoiID);
        final Text TTFFLabel = new Text("TTFF:");
        final Text TTFFV = new Text(TTFF);
        final Text TimeSpentLabel = new Text("Time Spent:");
        final Text TimeSpentLabelV = new Text(TimeSpent);
        final Text Fixations = new Text("Fixations:");
        final Text FixationV = new Text(Fixation);

        infoBox.add(boxID, 1, 0);
        infoBox.add(TTFFLabel, 0, 2);
        infoBox.add(TTFFV, 2, 2);
        infoBox.add(TimeSpentLabel, 0, 3);
        infoBox.add(TimeSpentLabelV, 2, 3);
        infoBox.add(Fixations, 0, 4);
        infoBox.add(FixationV, 2, 4);
        if (revisit != 0) {
            final Text Revisit = new Text("Revisits: ");
            final Text RevisitV = new Text(revisit + "");
            infoBox.add(Revisit, 0, 5);
            infoBox.add(RevisitV, 2, 5);
        }
        return infoBox;
    }

    private ArrayList<InitialAreaOfInterestProps> computeConnectedArea() {
        for (int index = 0; index < allAOIList.size(); index++) {
            final double areaSize = allAOIList.get(index).getAreaOfInterest().getBoundsInLocal().getWidth()
                * allAOIList.get(index).getAreaOfInterest().getBoundsInLocal().getHeight();
            for (int i = 0; i < allAOIList.size(); i++) {
                if (allAOIList.get(i).getAreaOfInterest() != allAOIList.get(index).getAreaOfInterest()) {
                    final Shape intersect = Shape.intersect(allAOIList.get(index).getAreaOfInterest(),
                        allAOIList.get(i).getAreaOfInterest());
                    if (intersect.getBoundsInLocal().getWidth() != -1) {
                        final double toCompareAreaSize = intersect.getBoundsInLocal().getWidth()
                            * intersect.getBoundsInLocal().getHeight();
                        final double combinationThreshHold = 0.70;
                        if ((toCompareAreaSize / areaSize) > combinationThreshHold) {
                            if (areaMap[index] == -1) {
                                if (areaMap[i] != -1) {
                                    areaMap[index] = areaMap[i];
                                } else {
                                    areaMap[index] = index;
                                }
                            }
                            if (areaMap[i] == -1) {
                                areaMap[i] = index;
                            }
                        }
                    }
                }
            }
        }
        final ArrayList<InitialAreaOfInterestProps> listOfCombinedPolygons = new ArrayList<>();

        for (int i = 0; i < allAOIList.size(); i++) {
            GridPane infoBox = new GridPane();
            Shape tempPolygon = null;

            if (areaMap[i] == -1) {
                tempPolygon = allAOIList.get(i).getAreaOfInterest();
                infoBox = allAOIList.get(i).getInfoBoxProps().getInfoBox();
            }
            if (areaMap[i] == i) {
                tempPolygon = allAOIList.get(i).getAreaOfInterest();
                final String aoiID = allAOIList.get(i).getInfoBoxProps().getAoiID();
                double TTFF = allAOIList.get(i).getInfoBoxProps().getTTFF();
                double TimeSpent = allAOIList.get(i).getInfoBoxProps().getTimeSpent();
                int Fixation = allAOIList.get(i).getFixations();
                int revisit = 1;
                // Double ratioDouble = 0.0;
                for (int j = i + 1; j < allAOIList.size(); j++) {
                    if (areaMap[j] == i) {
                        TTFF += allAOIList.get(j).getInfoBoxProps().getTTFF();
                        revisit++;
                        TimeSpent += allAOIList.get(j).getInfoBoxProps().getTimeSpent();
                        Fixation += allAOIList.get(j).getInfoBoxProps().getTimeSpent();
                        tempPolygon = Shape.union(tempPolygon, allAOIList.get(j).getAreaOfInterest());
                        tempPolygon.setFill(Color.rgb(249, 166, 2, 0.15));
                    }
                }
                infoBox = makeInfoBox(aoiID, new DecimalFormat("##.###s").format(TTFF),
                    new DecimalFormat("##.###s").format(TimeSpent), Fixation + " ", revisit);
            }
            if (tempPolygon != null) {
                final Shape finalTempPolygon = tempPolygon;
                final GridPane finalInfoBox = infoBox;
                final int finalI = i;
                tempPolygon.setOnMouseEntered(event -> {
                    if (!playing) {
                        if (areaMap[finalI] == -1) {
                            finalTempPolygon.setFill(Color.rgb(255, 0, 0, allAOIList.get(finalI).getPriority() + 0.15));
                        } else {
                            finalTempPolygon
                                .setFill(Color.rgb(249, 166, 2, allAOIList.get(finalI).getPriority() + 0.15));
                        }
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
                    if (areaMap[finalI] == -1) {
                        finalTempPolygon.setFill(Color.rgb(255, 0, 0, allAOIList.get(finalI).getPriority()));
                    } else {
                        finalTempPolygon.setFill(Color.rgb(249, 166, 2, allAOIList.get(finalI).getPriority()));
                    }
                });
                colorIterator = i % 7;
                tempPolygon.setStroke(colors[colorIterator]);
                listOfCombinedPolygons.add(new InitialAreaOfInterestProps(tempPolygon, infoBox));
            }
        }
        return listOfCombinedPolygons;
    }
}
