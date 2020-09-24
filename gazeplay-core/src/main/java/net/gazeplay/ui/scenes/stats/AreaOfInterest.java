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

    private final List<List> allAOIListTemp;
    private final List<int[]> startAndEndIdx;
    private final List<Polygon> allAOIListPolygon;
    private final List<Double[]> allAOIListPolygonPt;

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

    private Polygon currentAreaDisplay;
    private GridPane currentInfoBox;
    private Line currentLineToInfoBox;
    private double score;
    private int colorIterator;
    private final Pane graphicsPane;
    private Double previousInfoBoxX;
    private Double previousInfoBoxY;
    private final ArrayList<InitialAreaOfInterestProps> combinedAreaList;
    private final int[] areaMap;
    private boolean playing = false;

    private double highestFixationTime = 0;
    private int intereatorAOI = 0;

    public AreaOfInterest(final GazePlay gazePlay, final Stats stats) {
        super(gazePlay, new BorderPane());

        config = ActiveConfigurationContext.getInstance();
        allAOIList = new ArrayList<>();
        areaOfInterestList = new ArrayList<>();
        graphicsPane = new Pane();
        movementHistory = stats.getMovementHistoryWithTime();
        allAOIListTemp = stats.getAllAOIListTemp();
        startAndEndIdx = stats.getStartAndEndIdx();
        allAOIListPolygon = stats.getAllAOIListPolygon();
        allAOIListPolygonPt = stats.getAllAOIListPolygonPt();

        this.dataTreatment();
        calculateAreaOfInterest(0, stats.getStartTime());

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
            final ArrayList<TargetAOI> targetAOIArrayList = stats.getTargetAOIList();
            calculateTargetAOI(targetAOIArrayList);

            long timeTargetAreaStart;
            long timeTargetAreaEnd;
            score = 0;
            int targetAOIIterator = 0;

            for (final AreaOfInterestProps areaOfInterestProps : allAOIList) {

                final long timeAreaStart = areaOfInterestProps.getAreaStartTime();
                final long timeAreaEnd = areaOfInterestProps.getAreaEndTime();
                double maxScore = 0;
                for (targetAOIIterator = 0; targetAOIIterator < targetAOIArrayList.size(); targetAOIIterator++) {

                    timeTargetAreaStart = targetAOIArrayList.get(targetAOIIterator).getTimeStarted();
                    timeTargetAreaEnd = targetAOIArrayList.get(targetAOIIterator).getTimeEnded();

                    if (timeTargetAreaStart <= timeAreaStart && timeAreaStart <= timeTargetAreaEnd) {
                        final Shape intersect = Shape.intersect(targetAOIArrayList.get(targetAOIIterator).getPolygon(),
                            areaOfInterestProps.getAreaOfInterest());
                        if (intersect.getBoundsInLocal().getWidth() != -1) {
                            if (intersect.getBoundsInLocal().getWidth()
                                / (areaOfInterestProps.getAreaOfInterest().getBoundsInLocal().getWidth() - 1)
                                > maxScore)
                                maxScore = intersect.getBoundsInLocal().getWidth()
                                    / (areaOfInterestProps.getAreaOfInterest().getBoundsInLocal().getWidth() - 1);
                        }
                    }
                }
                score += maxScore;
            }
            score /= allAOIList.size();
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
        if (stats.getTargetAOIList() != null) {
            topPane = new HBox(scoreLabel, timeLabel, region2, buttonBox);
        } else {
            topPane = new HBox(timeLabel, region2, buttonBox);
        }

        topPane.setPadding(new Insets(15, 15, 0, 15));
        topPane.setSpacing(10);
        topPane.setStyle("-fx-background-color: transparent; -fx-max-height: 80px;");

        final Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        final EventHandler<Event> aoiEvent = e -> {
            final StatsContext statsContext;
            statsContext = StatsContextFactory.newInstance(gazePlay, stats);

            if (config.isVideoRecordingEnabled()) {
                player.stop();
            }
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
        root.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
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

        playBtn.setOnAction(e -> {
            playButtonPressed(baseProgressRate);
        });
        slowBtn5.setOnAction(e -> {
            playButtonPressed(baseProgressRate * 5);
        });
        slowBtn8.setOnAction(e -> {
            playButtonPressed(baseProgressRate * 8);
        });
        slowBtn10.setOnAction(e -> {
            playButtonPressed(baseProgressRate * 10);
        });
        cancelBtn.setOnAction(e -> {
            if (playing) {
                playing = false;
            }
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

    private void playButtonPressed(final double progressRate) {
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
        for (final InitialAreaOfInterestProps initialAreaOfInterestProps : combinedAreaList) {
            graphicsPane.getChildren().add(initialAreaOfInterestProps.getAreaOfInterest());
        }
    }

    private void calculateAreaOfInterest(final int index, final double startTime) {
        int i;
        for (i = 0; i < allAOIListTemp.size(); i++) {
            areaOfInterestList = allAOIListTemp.get(i);
            final double areaStartTime = areaOfInterestList.get(0).getTimeStarted();
            final double areaEndTime = areaOfInterestList.get(areaOfInterestList.size() - 1).getTimeStarted()
                + areaOfInterestList.get(areaOfInterestList.size() - 1).getIntervalTime();
            final double ttff = (areaStartTime - startTime) / 1000.0;
            final double timeSpent = (areaEndTime - areaStartTime) / 1000.0;

            if (timeSpent > highestFixationTime) {
                highestFixationTime = timeSpent;
            }

            int centerX = 0;
            int centerY = 0;

            final int movementHistoryEndingIndex = startAndEndIdx.get(i)[1];
            final int movementHistoryStartingIndex = startAndEndIdx.get(i)[0];
            final Point2D[] points = new Point2D[areaOfInterestList.size()];
            int j;
            for (j = 0; j < areaOfInterestList.size(); j++) {
                CoordinatesTracker coordinate = areaOfInterestList.get(j);
                centerX += coordinate.getXValue();
                centerY += coordinate.getYValue();
                points[j] = new Point2D(coordinate.getXValue(), coordinate.getYValue());
            }

            centerX /= areaOfInterestList.size();
            centerY /= areaOfInterestList.size();

            final InfoBoxProps infoBox = calculateInfoBox("AOI number " + (allAOIList.size() + 1), ttff, timeSpent,
                areaOfInterestList.size(), centerX, centerY, allAOIListPolygon.get(i));
            final AreaOfInterestProps areaOfInterestProps = new AreaOfInterestProps(areaOfInterestList, centerX,
                centerY, allAOIListPolygonPt.get(i), points, movementHistoryStartingIndex, movementHistoryEndingIndex,
                allAOIListPolygon.get(i), infoBox, (long) areaStartTime, (long) areaEndTime);
            allAOIList.add(areaOfInterestProps);

        }
    }


    /**
     * Determines how the Info Box will be shown for a particular area of interest.
     *
     * @param aoiID              Area of Interest ID
     * @param ttff               Time to First Fixation
     * @param timeSpent          Time spent on the AOI
     * @param fixations          The number of fixations in that AOI
     * @param centerX            Center X value of the AOI
     * @param centerY            Center Y value of the AOI
     * @param currentAreaDisplay The area around which to display
     * @return The created InfoBoxProps object
     */
    InfoBoxProps calculateInfoBox(
        final String aoiID,
        final double ttff,
        final double timeSpent,
        final int fixations,
        final int centerX,
        final int centerY,
        final Polygon currentAreaDisplay
    ) {
        final GridPane infoBox = makeInfoBox(aoiID, new DecimalFormat("##.###s").format(ttff),
            new DecimalFormat("##.###s").format(timeSpent), fixations, 0);

        final Dimension2D screenDimension = getGazePlay().getCurrentScreenDimensionSupplier().get();
        final double screenWidthCenter = screenDimension.getWidth() / 2;
        final double widthOfArea = currentAreaDisplay.getBoundsInLocal().getWidth();

        final Line line = new Line();
        line.setStartY(centerY);
        line.setEndY(centerY);
        line.setStroke(Color.YELLOW);

        if (centerX > screenWidthCenter) {
            // will display infobox on the left side
            infoBox.setLayoutX(centerX - widthOfArea - 290);
            line.setStartX(currentAreaDisplay.getBoundsInLocal().getMinX());
            line.setEndX(centerX - widthOfArea - 30);
        } else {
            // will display infobox on the right
            infoBox.setLayoutX(centerX + widthOfArea + 100);
            line.setEndX(centerX + widthOfArea + 100);
            line.setStartX(currentAreaDisplay.getBoundsInLocal().getMaxX());
        }

        infoBox.setLayoutY(centerY - 60);
        infoBox.setStyle("-fx-background-color: rgba(255,255,153, 0.4);");
        return new InfoBoxProps(infoBox, line, aoiID, ttff, timeSpent, fixations);
    }

    /**
     * Calculates a rectangle that can enclose all the points provided, with
     * a set padding around the points.
     *
     * @param point2D The points to enclose.
     * @return Double array containing all X and Y values of each rectangle point in
     * sequence.
     */
    static Double[] calculateRectangle(final Point2D[] point2D) {
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

    /**
     * Determines the orientation of an ordered triplet of 2D points on a plane.
     * The points provided can be counterclockwise, clockwise and collinear.
     *
     * @param p1 First Point
     * @param p2 Second Point
     * @param p3 Third Point
     * @return Will return 0 if the points are collinear, 1 if the points are clockwise,
     * or -1 if the points are counterclockwise.
     */
    static int orientation(final Point2D p1, final Point2D p2, final Point2D p3) {

        final int val = (int) ((p2.getY() - p1.getY()) * (p3.getX() - p2.getX())
            - (p2.getX() - p1.getX()) * (p3.getY() - p2.getY()));

        if (val == 0) {
            return 0;
        }

        return (val > 0) ? 1 : -1;
    }

    /**
     * Implements Jarvis's Algorithm to calculate the points on a Convex Hull.
     * It starts by calculating the left-most point and will loop through all
     * the points until it reaches this point again, finding the most convex points.
     *
     * @param points The array of Point2D objects to calculate the hull around.
     * @return Double array containing all X and Y values of each hull point in
     * sequence.
     * @see Point2D
     */
    static Double[] calculateConvexHull(final Point2D[] points) {
        final int numberOfPoints = points.length;
        final ArrayList<Double> convexHullPoints = new ArrayList<>();
        final Vector<Point2D> hull = new Vector<>();

        // Finding the index of the lowest X value, or left-most point, in all points.
        int lowestValueIndex = 0;
        for (int i = 1; i < numberOfPoints; i++) {
            if (points[i].getX() < points[lowestValueIndex].getX()) {
                lowestValueIndex = i;
            }
        }

        int point = lowestValueIndex, q;
        do {
            hull.add(points[point]);
            q = (point + 1) % numberOfPoints;
            for (int i = 0; i < numberOfPoints; i++) {
                if (orientation(points[point], points[i], points[q]) < 0) { // Checking if the points are convex.
                    q = i;
                }
            }
            point = q;
        } while (point != lowestValueIndex);

        for (final Point2D temp : hull) {
            convexHullPoints.add(temp.getX());
            convexHullPoints.add(temp.getY());
        }

        Double[] hullPointsArray = new Double[convexHullPoints.size()];
        convexHullPoints.toArray(hullPointsArray);

        return hullPointsArray;
    }

    /**
     * Creates a rectangle surrounding each Area Of Interest in the provided
     * list. The rectangle is added as a transparent Polygon to each TargetAOI.
     *
     * @param targetAOIArrayList TargetAOIs to apply Polygons to.
     * @see Polygon
     */
    static void calculateTargetAOI(final ArrayList<TargetAOI> targetAOIArrayList) {
        for (final TargetAOI targetAOI : targetAOIArrayList) {
            log.debug("The target is at (" + targetAOI.getXValue() + ", " + targetAOI.getYValue() + ")");

            final int radius = targetAOI.getAreaRadius();
            final Point2D[] point2D = {
                new Point2D(targetAOI.getXValue() - 100, targetAOI.getYValue()),
                new Point2D(targetAOI.getXValue() + radius, targetAOI.getYValue() + 100),
                new Point2D(targetAOI.getXValue(), targetAOI.getYValue() - radius),
                new Point2D(targetAOI.getXValue() + radius, targetAOI.getYValue() - radius)
            };

            final Double[] polygonPoints;
            polygonPoints = calculateRectangle(point2D);

            final Polygon targetArea;
            targetArea = new Polygon();
            targetArea.getPoints().addAll(polygonPoints);
            targetArea.setFill(Color.rgb(255, 255, 255, 0.4));
            targetAOI.setPolygon(targetArea);
        }
    }


    /**
     * Will create a GridPane with the supplied values and their respective labels.
     *
     * @param aoiID     Area Of Interest ID
     * @param ttff      Time to First Fixation
     * @param timeSpent Time spent in the AOI
     * @param fixations Number of fixations
     * @param revisits  Number of revisits
     * @return The info box pane with formatted labels and values.
     */
    static GridPane makeInfoBox(
        final String aoiID,
        final String ttff,
        final String timeSpent,
        final int fixations,
        final int revisits
    ) {
        final GridPane infoBox = new GridPane();
        infoBox.setHgap(10);
        infoBox.setVgap(10);
        infoBox.setPadding(new Insets(10, 10, 10, 10));

        final Text boxID = new Text(aoiID);
        final Text ttffLabel = new Text("TTFF:");
        final Text ttffValue = new Text(ttff);
        final Text timeSpentLabel = new Text("Time Spent:");
        final Text timeSpentLabelValue = new Text(timeSpent);
        final Text fixationsLabel = new Text("Fixations:");
        final Text fixationsValue = new Text(String.valueOf(fixations));

        infoBox.add(boxID, 1, 0);
        infoBox.add(ttffLabel, 0, 2);
        infoBox.add(ttffValue, 2, 2);
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

    private ArrayList<InitialAreaOfInterestProps> computeConnectedArea() {
        for (int i = 0; i < allAOIList.size(); i++) {
            AreaOfInterestProps currentAOI = allAOIList.get(i);

            final double areaSize =
                currentAOI.getAreaOfInterest().getBoundsInLocal().getWidth() *
                    currentAOI.getAreaOfInterest().getBoundsInLocal().getHeight();

            for (int j = 0; j < allAOIList.size(); j++) {
                if (allAOIList.get(j).getAreaOfInterest() != currentAOI.getAreaOfInterest()) {
                    final Shape intersect = Shape.intersect(currentAOI.getAreaOfInterest(),
                        allAOIList.get(j).getAreaOfInterest());

                    if (intersect.getBoundsInLocal().getWidth() != -1) {
                        final double toCompareAreaSize =
                            intersect.getBoundsInLocal().getWidth() *
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

        final ArrayList<InitialAreaOfInterestProps> listOfCombinedPolygons = new ArrayList<>();

        for (int i = 0; i < allAOIList.size(); i++) {
            GridPane infoBox = new GridPane();
            Shape tempPolygon = null;
            AreaOfInterestProps currentAOI = allAOIList.get(i);

            if (areaMap[i] == -1) {
                tempPolygon = currentAOI.getAreaOfInterest();
                infoBox = currentAOI.getInfoBoxProps().getInfoBox();
            }

            if (areaMap[i] == i) {
                tempPolygon = currentAOI.getAreaOfInterest();
                final String aoiID = currentAOI.getInfoBoxProps().getAoiID();
                double ttff = currentAOI.getInfoBoxProps().getTTFF();
                double timeSpent = currentAOI.getInfoBoxProps().getTimeSpent();
                int fixations = currentAOI.getFixations();
                int revisit = 1;

                for (int j = i + 1; j < allAOIList.size(); j++) {
                    if (areaMap[j] == i) {
                        ttff += allAOIList.get(j).getInfoBoxProps().getTTFF();
                        revisit++;
                        timeSpent += allAOIList.get(j).getInfoBoxProps().getTimeSpent();
                        fixations += allAOIList.get(j).getInfoBoxProps().getTimeSpent();
                        tempPolygon = Shape.union(tempPolygon, allAOIList.get(j).getAreaOfInterest());
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
