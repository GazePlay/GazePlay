package net.gazeplay;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Duration;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class AreaOfInterest extends GraphicalContext<BorderPane> {

    private List<CoordinatesTracker> movementHistory;
    private Label timeLabel;
    private Timeline clock;
    private MediaPlayer player;
    private List<AreaOfInterestProps> allAOIList;
    private ArrayList<CoordinatesTracker> areaOfInterestList;
    private Color[] colors;
    private Configuration config;
    private int intereatorAOI;
    private Polygon currentAreaDisplay;
    private GridPane currentInfoBox;
    private Line currentLineToInfoBox;
    private int colorIterator;
    private int bias = 10;
    private Pane graphicsPane;
    private int progressRate = 1;
    private Double previousInfoBoxX;
    private Double previousInfoBoxY;
    private ArrayList<InitialAreaOfInterestProps> combinedAreaList = new ArrayList<>();;
    private double combinationThreshHold = 0.70;
    int[] areaMap;

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    public static AreaOfInterest newInstance(GazePlay gazePlay, Stats stats) {
        BorderPane root = new BorderPane();
        return new AreaOfInterest(gazePlay, root, stats);
    }

    private void plotMovement(int movementIndex, Pane graphicsPane) {
        new Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                CoordinatesTracker coordinatesTracker = movementHistory.get(movementIndex);
                Circle circle;
                if (movementHistory.get(movementIndex).getIntervalTime() > 10) {
                    circle = new Circle(coordinatesTracker.getxValue(), coordinatesTracker.getyValue(), 2);
                    circle.setStroke(Color.RED);
                } else {
                    circle = new Circle(coordinatesTracker.getxValue(), coordinatesTracker.getyValue(), 4);
                    circle.setStroke(Color.GREEN);
                }
                Platform.runLater(() -> {
                    if (intereatorAOI < allAOIList.size()) {
                        if (movementIndex == allAOIList.get(intereatorAOI).getStartingIndex()) {
                            currentInfoBox = allAOIList.get(intereatorAOI).getInfoBoxProp().getInfoBox();
                            currentAreaDisplay = allAOIList.get(intereatorAOI).getAreaOfInterest();
                            currentLineToInfoBox = allAOIList.get(intereatorAOI).getInfoBoxProp().getLineToInfoBox();
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
                    new Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> graphicsPane.getChildren().remove(circle));
                        }
                    }, 2000);
                    if (movementIndex != movementHistory.size() - 1) {
                        plotMovement(movementIndex + 1, graphicsPane);
                    } else {
                        for (int i = 0; i < allAOIList.size(); i++)

                            clock.stop();
                    }
                });
            }
        }, (movementHistory.get(movementIndex).getIntervalTime() * progressRate));
    }

    private void calculateAreaOfInterest(int index, long startTime) {
        if (index != 0) {
            double x1 = movementHistory.get(index).getxValue();
            double y1 = movementHistory.get(index).getyValue();
            double x2 = movementHistory.get(index - 1).getxValue();
            double y2 = movementHistory.get(index - 1).getyValue();
            double eDistance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            if (eDistance < 120 && movementHistory.get(index).getIntervalTime() > 10) {
                if (index == 1)
                    areaOfInterestList.add(movementHistory.get(0));
                areaOfInterestList.add(movementHistory.get(index));
                if (areaOfInterestList.size() == 1)
                    System.out.println("Start of AOE");
            } else if (areaOfInterestList.size() != 0) {
                if (areaOfInterestList.size() > 2) {
                    System.out.println("End of AOE");
                    long AreaStartTime = areaOfInterestList.get(0).getTimeStarted();
                    long AreaEndTime = areaOfInterestList.get(areaOfInterestList.size() - 1).getTimeStarted()
                            + areaOfInterestList.get(areaOfInterestList.size() - 1).getIntervalTime();
                    long TTFF = (AreaStartTime - startTime) / 100;

                    long timeSpent = (AreaEndTime - AreaStartTime) / 100;
                    int centerX = 0;
                    int centerY = 0;
                    int movementHistoryEndingIndex = index - 1;
                    int movementHistoryStartingIndex = movementHistoryEndingIndex - areaOfInterestList.size();
                    Point2D[] point2DS = new Point2D[areaOfInterestList.size()];
                    for (int i = 0; i < areaOfInterestList.size(); i++) {
                        centerX += areaOfInterestList.get(i).getxValue();
                        centerY += areaOfInterestList.get(i).getyValue();
                        point2DS[i] = new Point2D(areaOfInterestList.get(i).getxValue(),
                                areaOfInterestList.get(i).getyValue());
                    }
                    Double[] polygonPoints;
                    if (config.isConvexHullEnabled()) {
                        polygonPoints = calculateConvexHull(point2DS, areaOfInterestList.size());
                    } else {
                        polygonPoints = calculateRectangle(point2DS);
                    }
                    Polygon areaOfInterest = new Polygon();
                    areaOfInterest.getPoints().addAll(polygonPoints);
                    colorIterator = index % 7;
                    areaOfInterest.setStroke(colors[colorIterator]);
                    areaOfInterest.setFill(Color.rgb(18, 121, 131, 0.15));
                    centerX = centerX / areaOfInterestList.size();
                    centerY = centerY / areaOfInterestList.size();
                    InfoBoxProps infoBox = calculateInfoBox("AOI number " + (allAOIList.size() + 1), TTFF, timeSpent,
                            areaOfInterestList.size(), centerX, centerY, areaOfInterest);
                    AreaOfInterestProps areaOfInterestProps = new AreaOfInterestProps(areaOfInterestList, centerX,
                            centerY, polygonPoints, point2DS, movementHistoryStartingIndex, movementHistoryEndingIndex,
                            areaOfInterest, infoBox);
                    allAOIList.add(areaOfInterestProps);
                } else {
                    System.out.println("Not enough points to make a convex hull" + areaOfInterestList.size());
                }
                areaOfInterestList = new ArrayList<>();
            }
        }

    }

    private InfoBoxProps calculateInfoBox(String aoiID, long TTFF, long TimeSpent, long Fixation, int centerX,
            int centerY, Polygon currentAreaDisplay) {
        Double ratioDouble = (currentAreaDisplay.getBoundsInLocal().getWidth()
                * currentAreaDisplay.getBoundsInLocal().getHeight())
                / (Screen.getPrimary().getBounds().getWidth() * Screen.getPrimary().getBounds().getHeight());

        GridPane infoBox = makeInfoBox(aoiID, TTFF + "", TimeSpent + "", Fixation + "", ratioDouble + " ", 0);
        double screenWidthCenter = Screen.getPrimary().getBounds().getWidth() / 2;
        double widthOfArea = currentAreaDisplay.getBoundsInLocal().getWidth();

        Line line = new Line();
        line.setStartY(centerY);
        line.setEndY(centerY);
        line.setStroke(Color.YELLOW);
        if (centerX > screenWidthCenter) {
            infoBox.setLayoutX(centerX - widthOfArea - 400);
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

    private Double[] calculateRectangle(Point2D[] point2D) {
        Double leftPoint = point2D[0].getX();
        Double rightPoint = point2D[0].getX();
        Double topPoint = point2D[0].getY();
        Double bottomPoint = point2D[0].getY();
        for (int i = 1; i < point2D.length; i++) {
            if (point2D[i].getX() < leftPoint)
                leftPoint = point2D[i].getX();
            if (point2D[i].getX() > rightPoint)
                rightPoint = point2D[i].getX();
            if (point2D[i].getY() > topPoint)
                topPoint = point2D[i].getY();
            if (point2D[i].getY() < bottomPoint)
                bottomPoint = point2D[i].getY();
        }
        Double[] squarePoints = new Double[8];
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

    private int orientation(Point2D p1, Point2D p2, Point2D p3) {

        int val = (int) ((p2.getY() - p1.getY()) * (p3.getX() - p2.getX())
                - (p2.getX() - p1.getX()) * (p3.getY() - p2.getY()));

        if (val == 0)
            return 0;

        return (val > 0) ? 1 : 2;
    }

    private Double[] calculateConvexHull(Point2D[] points, int n) {
        ArrayList<Double> convexHullPoints = new ArrayList<>();
        Vector<Point2D> hull = new Vector<>();
        int l = 0;
        for (int i = 1; i < n; i++)
            if (points[i].getX() < points[l].getX())
                l = i;
        int point = l, q;
        do {
            hull.add(points[point]);
            q = (point + 1) % n;
            for (int i = 0; i < n; i++) {
                if (orientation(points[point], points[i], points[q]) == 2)
                    q = i;
            }
            point = q;
        } while (point != l);
        for (Point2D temp : hull) {
            convexHullPoints.add(temp.getX());
            convexHullPoints.add(temp.getY());
        }
        Double[] pointsToReturn = new Double[convexHullPoints.size()];
        for (int i = 0; i < convexHullPoints.size(); i++) {
            pointsToReturn[i] = convexHullPoints.get(i);
        }
        return pointsToReturn;
    }

    private AreaOfInterest(GazePlay gazePlay, BorderPane root, Stats stats) {
        super(gazePlay, root);
        colors = new Color[] { Color.PURPLE, Color.WHITE, Color.PINK, Color.ORANGE, Color.BLUE, Color.RED,
                Color.CHOCOLATE };
        config = Configuration.getInstance();
        GridPane grid = new GridPane();
        StackPane stackPane = new StackPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(100);
        grid.setVgap(50);
        intereatorAOI = 0;
        grid.setPadding(new Insets(50, 50, 50, 50));
        allAOIList = new ArrayList<>();
        Multilinguism multilinguism = Multilinguism.getSingleton();
        Text screenTitleText = new Text(multilinguism.getTrad("AreaOfInterest", config.getLanguage()));
        screenTitleText.setId("title");
        HBox topPane;
        VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);
        graphicsPane = new Pane();
        areaOfInterestList = new ArrayList<>();
        movementHistory = stats.getMovementHistoryWithTime();
        System.out.println("The start time is " + stats.getStartTime());
        for(int i = 0 ; i < movementHistory.size();i++)
        calculateAreaOfInterest(i, stats.getStartTime());
        System.out.println("The amount of AOIs is " + allAOIList.size());

        areaMap = new int[allAOIList.size()];
        Arrays.fill(areaMap, -1);
        combinedAreaList = computeConnectedArea();
        for (InitialAreaOfInterestProps initialAreaOfInterestProps : combinedAreaList) {
            graphicsPane.getChildren().add(initialAreaOfInterestProps.getAreaOfInterest());
        }
        VBox pane = new VBox(1);
        timeLabel = new Label();
        timeLabel.setTextFill(Color.web("#FFFFFF"));
        Button slowBtn10 = new Button("10X Slow ");
        Button slowBtn8 = new Button("8X Slow ");
        Button slowBtn5 = new Button("5X Slow ");
        Button playBtn = new Button("Play ");
        Button quitBtn = new Button("Leave");
        quitBtn.setCancelButton(true);

        playBtn.setPrefSize(100, 20);
        slowBtn5.setPrefSize(100, 20);
        slowBtn8.setPrefSize(100, 20);
        slowBtn10.setPrefSize(100, 20);

        playBtn.setOnAction(e -> {
            progressRate = 1;
            playButtonPressed();
        });
        slowBtn5.setOnAction(e -> {
            progressRate = 5;
            playButtonPressed();
        });
        slowBtn8.setOnAction(e -> {
            progressRate = 8;
            playButtonPressed();
        });

        slowBtn10.setOnAction(e -> {
            progressRate = 10;
            playButtonPressed();
        });
        quitBtn.setOnAction(event -> {
            StatsContext statsContext = null;
            try {
                statsContext = StatsContext.newInstance(gazePlay, stats);
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.clear();

            gazePlay.onDisplayStats(statsContext);
        });

        if (config.isVideoRecordingEnabled()) {
            Media media = new Media(stats.getDirectoryOfVideo());
            player = new MediaPlayer(media);
            MediaView mediaView = new MediaView(player);
            stackPane.getChildren().add(mediaView);
        }
        timeLabel.setMinSize(100, 0);
        stackPane.getChildren().add(graphicsPane);
        // playBtn.setStyle("-fx-alignment: baseline-right;");

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);
        HBox buttonBox = new HBox(quitBtn, playBtn, slowBtn5, slowBtn8, slowBtn10);
        buttonBox.setSpacing(10);
        buttonBox.setFillHeight(true);
        buttonBox.setPadding(new Insets(10, 10, 10, 10));
        topPane = new HBox(timeLabel, region1, screenTitleText, region2, buttonBox);
        topPane.setSpacing(10);
        graphicsPane.setStyle("-fx-background-color: transparent;");
        pane.setStyle("-fx-background-color: #224488");
        root.setCenter(stackPane);
        root.setTop(topPane);
        root.setBottom(pane);
        root.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
    }

    private void playButtonPressed() {
        for (InitialAreaOfInterestProps areaOfInterestProps : combinedAreaList) {
            graphicsPane.getChildren().remove(areaOfInterestProps.getAreaOfInterest());
        }
        graphicsPane.getChildren().remove(currentInfoBox);
        if (config.isVideoRecordingEnabled())
            player.play();
        intereatorAOI = 0;
        plotMovement(0, graphicsPane);
        long startTime = System.currentTimeMillis();
        clock = new Timeline(new KeyFrame(Duration.ZERO, f -> {
            long theTime = System.currentTimeMillis() - startTime;
            timeLabel.setText(theTime + "");
        }), new KeyFrame(Duration.millis(100)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private GridPane makeInfoBox(String aoiID, String TTFF, String TimeSpent, String Fixation, String ratioDouble,
            int revisit) {
        GridPane infoBox = new GridPane();
        infoBox.setHgap(10);
        infoBox.setVgap(10);
        infoBox.setPadding(new Insets(10, 10, 10, 10));
        Text boxID = new Text(aoiID);
        Text TTFFLabel = new Text("TTFF:");
        Text TTFFV = new Text(TTFF);
        Text TimeSpentLabel = new Text("Time Spent:");
        Text TimeSpentLabelV = new Text(TimeSpent);
        Text Fixations = new Text("Fixations:");
        Text FixationV = new Text(Fixation);
        Text Ratio = new Text("Ratio:");
        Text RatioV = new Text(ratioDouble);

        infoBox.add(boxID, 1, 0);
        infoBox.add(TTFFLabel, 0, 2);
        infoBox.add(TTFFV, 2, 2);
        infoBox.add(TimeSpentLabel, 0, 3);
        infoBox.add(TimeSpentLabelV, 2, 3);
        infoBox.add(Fixations, 0, 4);
        infoBox.add(FixationV, 2, 4);
        infoBox.add(Ratio, 0, 5);
        infoBox.add(RatioV, 2, 5);
        if (revisit != 0) {
            Text Revisit = new Text("Revisits: ");
            Text RevisitV = new Text(revisit + "");
            infoBox.add(Revisit, 0, 6);
            infoBox.add(RevisitV, 2, 6);
        }
        return infoBox;
    }

    private ArrayList<InitialAreaOfInterestProps> computeConnectedArea() {
        for (int index = 0; index < allAOIList.size(); index++) {
            double areaSize = allAOIList.get(index).getAreaOfInterest().getBoundsInLocal().getWidth()
                    * allAOIList.get(index).getAreaOfInterest().getBoundsInLocal().getHeight();
            for (int i = 0; i < allAOIList.size(); i++) {
                if (allAOIList.get(i).getAreaOfInterest() != allAOIList.get(index).getAreaOfInterest()) {
                    Shape intersect = Shape.intersect(allAOIList.get(index).getAreaOfInterest(),
                            allAOIList.get(i).getAreaOfInterest());
                    if (intersect.getBoundsInLocal().getWidth() != -1) {
                        double toCompareAreaSize = intersect.getBoundsInLocal().getWidth()
                                * intersect.getBoundsInLocal().getHeight();
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
        ArrayList<InitialAreaOfInterestProps> listOfCombinedPolygons = new ArrayList<>();

        for (int i = 0; i < allAOIList.size(); i++) {
            GridPane infoBox = new GridPane();
            Shape tempPolygon = null;

            if (areaMap[i] == -1) {
                tempPolygon = allAOIList.get(i).getAreaOfInterest();
                infoBox = allAOIList.get(i).getInfoBoxProp().getInfoBox();
            }
            if (areaMap[i] == i) {
                tempPolygon = allAOIList.get(i).getAreaOfInterest();
                String aoiID = allAOIList.get(i).getInfoBoxProp().getAoiID();
                long TTFF = allAOIList.get(i).getInfoBoxProp().getTTFF();
                long TimeSpent = allAOIList.get(i).getInfoBoxProp().getTimeSpent();
                long Fixation = allAOIList.get(i).getInfoBoxProp().getTimeSpent();
                int revisit = 1;
                Double ratioDouble = 0.0;
                for (int j = i + 1; j < allAOIList.size(); j++) {
                    if (areaMap[j] == i) {
                        TTFF += allAOIList.get(j).getInfoBoxProp().getTTFF();
                        revisit++;
                        TimeSpent += allAOIList.get(j).getInfoBoxProp().getTimeSpent();
                        Fixation += allAOIList.get(j).getInfoBoxProp().getTimeSpent();
                        tempPolygon = Shape.union(tempPolygon, allAOIList.get(j).getAreaOfInterest());
                        tempPolygon.setFill(Color.rgb(249, 166, 2, 0.15));
                        ratioDouble = (tempPolygon.getBoundsInLocal().getWidth()
                                * tempPolygon.getBoundsInLocal().getHeight())
                                / (Screen.getPrimary().getBounds().getWidth()
                                        * Screen.getPrimary().getBounds().getHeight());
                    }
                }
                infoBox = makeInfoBox(aoiID, TTFF + "", TimeSpent + " ", Fixation + " ", ratioDouble + "", revisit);
            }
            if (tempPolygon != null) {
                Shape finalTempPolygon = tempPolygon;
                GridPane finalInfoBox = infoBox;
                int finalI = i;
                tempPolygon.setOnMouseEntered(event -> {
                    if (areaMap[finalI] == -1) {
                        finalTempPolygon.setFill(Color.rgb(18, 121, 131, 0.30));
                    } else {
                        finalTempPolygon.setFill(Color.rgb(249, 166, 2, 0.30));
                    }
                    previousInfoBoxX = finalInfoBox.getLayoutX();
                    previousInfoBoxY = finalInfoBox.getLayoutY();
                    currentInfoBox = new GridPane();
                    finalInfoBox.setLayoutY(Screen.getPrimary().getBounds().getHeight() - 250);
                    finalInfoBox.setLayoutX(0);
                    finalInfoBox.setStyle("-fx-background-color: rgba(255,255,153, 0.4);");
                    currentInfoBox = finalInfoBox;

                    graphicsPane.getChildren().add(finalInfoBox);
                });
                tempPolygon.setOnMouseExited(event -> {
                    graphicsPane.getChildren().remove(currentInfoBox);
                    finalInfoBox.setLayoutY(previousInfoBoxY);
                    finalInfoBox.setLayoutX(previousInfoBoxX);
                    if (areaMap[finalI] == -1) {
                        finalTempPolygon.setFill(Color.rgb(18, 121, 131, 0.15));
                    } else {
                        finalTempPolygon.setFill(Color.rgb(249, 166, 2, 0.15));
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
