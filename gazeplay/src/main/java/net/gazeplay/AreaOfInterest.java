package net.gazeplay;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.util.Duration;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.AreaOfInterestProps;
import net.gazeplay.commons.utils.stats.ConvexHullProps;
import net.gazeplay.commons.utils.stats.CoordinatesTracker;
import net.gazeplay.commons.utils.stats.Stats;
import org.apache.commons.lang.ArrayUtils;

import java.util.*;
import java.util.stream.Stream;

public class AreaOfInterest extends GraphicalContext<BorderPane>{


    private List<CoordinatesTracker> movementHistory;
    private Label timeLabel;
    private Timeline clock;
    private MediaPlayer player;
    private List<AreaOfInterestProps> allAOIList ;
    private ArrayList<CoordinatesTracker> areaOfInterestList;
    private Color[] colors;
    private Configuration config;
//    private ArrayList<Polygon> listOfPolygons;
    private int intereatorAOI;
    private Polygon currentAreaDisplay;
    private int colorIterator;



    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    public static AreaOfInterest newInstance(GazePlay gazePlay,Stats stats)
    {
        BorderPane root = new BorderPane();
        return new AreaOfInterest(gazePlay,root,stats);
    }

    private void plotMovement(int index, Pane graphicsPane)
    {
        new Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        CoordinatesTracker coordinatesTracker = movementHistory.get(index);
                        Circle circle;
                        if(movementHistory.get(index).getIntervalTime() > 10)
                        {
                            circle = new Circle(coordinatesTracker.getxValue(),coordinatesTracker.getyValue(),2);
                            circle.setStroke(Color.RED);
                        }else{
                            circle = new Circle(coordinatesTracker.getxValue(),coordinatesTracker.getyValue(),4);
                            circle.setStroke(Color.GREEN);
                        }
                        Platform.runLater(() -> {
                            if(intereatorAOI < allAOIList.size() )
                            {
                                if(index == allAOIList.get(intereatorAOI).getStartingIndex())
                                {
                                    currentAreaDisplay = allAOIList.get(intereatorAOI).getAreaOfInterest();
                                    graphicsPane.getChildren().add(currentAreaDisplay);
//                                    System.out.println("add an AOI");
                                }
                                if(index == allAOIList.get(intereatorAOI).getEndingIndex())
                                {
//                                    System.out.println("Remove an AOI");
                                    graphicsPane.getChildren().remove(currentAreaDisplay);
                                    intereatorAOI++;
                                }
                            }
                            graphicsPane.getChildren().add(circle);
                            new Timer().schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            Platform.runLater(() -> graphicsPane.getChildren().remove(circle));
                                        }
                                    },
                                    2000
                            );
                            if(index != movementHistory.size()-1)
                            {
                                plotMovement(index+1,graphicsPane);
                            }else {
                                for(int i = 0 ; i < allAOIList.size();i++)

                                clock.stop();
                            }
                        });
                    }
                },
                movementHistory.get(index).getIntervalTime()
        );
    }

    private void calculateAreaOfInterest( int index, long startTime)
    {
        if(index != 0)
        {
            double x1 = movementHistory.get(index).getxValue();
            double y1 = movementHistory.get(index).getyValue();
            double x2 = movementHistory.get(index-1).getxValue();
            double y2 = movementHistory.get(index-1).getyValue();
            double eDistance =  Math.sqrt(Math.pow(x2 - x1,2) + Math.pow(y2-y1,2));
            if(eDistance < 100 && movementHistory.get(index).getIntervalTime() > 10)
            {
                if(index == 1)
                    areaOfInterestList.add(movementHistory.get(0));
                areaOfInterestList.add(movementHistory.get(index));
                if(areaOfInterestList.size() == 1)
                    System.out.println("Start of AOE");
            }else  if(areaOfInterestList.size() != 0) {
                if (areaOfInterestList.size() > 2) {
                    System.out.println("End of AOE");
                    long AreaStartTime = areaOfInterestList.get(0).getTimeValue();
                    long AreaEndTime = areaOfInterestList.get(areaOfInterestList.size() - 1).getIntervalTime();
                    long TTFF = AreaStartTime - startTime;
                    long timeSpent = AreaEndTime - AreaStartTime;
                    int centerX = 0;
                    int centerY = 0;
                    int movementHistoryEndingIndex = index -1;
                    int movementHistoryStartingIndex = movementHistoryEndingIndex - areaOfInterestList.size() ;
                    Point2D[] point2DS = new Point2D[areaOfInterestList.size()];
                    for(int i = 0 ; i< areaOfInterestList.size() ; i++)
                    {
                        centerX += areaOfInterestList.get(i).getxValue();
                        centerY += areaOfInterestList.get(i).getyValue();
                        point2DS[i] = new Point2D(areaOfInterestList.get(i).getxValue(),areaOfInterestList.get(i).getyValue());
                    }
                    Double[] polygonPoints;
                    if(config.isConvexHullEnabled())
                    {
                        polygonPoints = calculateConvexHull(point2DS,areaOfInterestList.size());
                    }else{
                        polygonPoints = calculateSquarePoints(point2DS);
                    }
                    colorIterator = index % 5;
                    Polygon areaOfInterest = new Polygon();
                    areaOfInterest.getPoints().addAll(polygonPoints);
                    areaOfInterest.setStroke(colors[colorIterator]);
                    areaOfInterest.setFill(Color.rgb(0, 0, 255, 0.8));
                    centerX = centerX / areaOfInterestList.size();
                    centerY = centerY / areaOfInterestList.size();
                    AreaOfInterestProps areaOfInterestProps = new AreaOfInterestProps(TTFF, timeSpent, areaOfInterestList, centerX, centerY,polygonPoints,point2DS,movementHistoryStartingIndex,movementHistoryEndingIndex,areaOfInterest);
                    allAOIList.add(areaOfInterestProps);
                }else{
                    System.out.println("Not enough points to make a convex hull" + areaOfInterestList.size());
                }
                areaOfInterestList = new ArrayList<>();
            }
//            System.out.println("The distance between the points is "+ eDistance);
        }
        if(index != movementHistory.size()-1)
            calculateAreaOfInterest(index+1,startTime);
    }

    private Double[] calculateSquarePoints(Point2D[] point2D)
    {
        Double leftPoint = point2D[0].getX();
        Double rightPoint = point2D[0].getX();
        Double topPoint = point2D[0].getY();
        Double bottomPoint = point2D[0].getY();
        for(int i = 1; i < point2D.length ; i++)
        {
            if(point2D[i].getX() < leftPoint)
                leftPoint = point2D[i].getX();
            if(point2D[i].getX() > rightPoint)
                rightPoint = point2D[i].getX();
            if(point2D[i].getY() > topPoint)
                topPoint = point2D[i].getY();
            if(point2D[i].getY() < bottomPoint)
                bottomPoint = point2D[i].getY();
        }
        Double[] squarePoints = new Double[8];
        squarePoints[0] = leftPoint;
        squarePoints[1] = topPoint;
        squarePoints[2] = rightPoint;
        squarePoints[3] = topPoint;
        squarePoints[4] = rightPoint;
        squarePoints[5] = bottomPoint;
        squarePoints[6] = leftPoint;
        squarePoints[7] = bottomPoint;
        return squarePoints;
    }
    private int orientation(Point2D p1, Point2D p2, Point2D p3)
    {

        int val = (int) ((p2.getY() - p1.getY()) * (p3.getX() - p2.getX()) -
                        (p2.getX() - p1.getX()) * (p3.getY() - p2.getY()));

        if (val == 0) return 0;

        return (val > 0)? 1: 2;
    }

    private Double[] calculateConvexHull(Point2D[] points, int n)
    {
        ArrayList<Double> convexHullPoints = new ArrayList<>();
        Vector<Point2D> hull = new Vector<>();
        int l = 0;
        for (int i = 1; i < n; i++)
            if (points[i].getX() < points[l].getX())
                l = i;
        int point = l, q;
        do
        {
            hull.add(points[point]);
            q = (point + 1) % n;
            for (int i = 0; i < n; i++)
            {
                if (orientation(points[point], points[i], points[q]) == 2)
                    q = i;
            }
            point = q;
        } while (point != l);
        for (Point2D temp : hull)
        {
            convexHullPoints.add(temp.getX());
            convexHullPoints.add(temp.getY());
        }
        Double[] pointsToReturn = new Double[convexHullPoints.size()];
        for(int i = 0; i< convexHullPoints.size(); i++)
        {
            pointsToReturn[i] = convexHullPoints.get(i);
        }
        return pointsToReturn;
    }

    private AreaOfInterest(GazePlay gazePlay,BorderPane root,Stats stats){
        super(gazePlay, root);
        colors = new Color[]{Color.PURPLE, Color.WHITE, Color.PINK, Color.ORANGE, Color.BLUE};
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
        HBox topPane = new HBox();
        topPane.getChildren().add(screenTitleText);
        VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);
        Pane graphicsPane = new Pane();
        areaOfInterestList = new ArrayList<>();
        movementHistory = stats.getMovementHistoryWithTime();
        calculateAreaOfInterest(0,stats.getStartTime());

        System.out.println("The amount of AOIs is "+allAOIList.size());

        //Drawing center points of each AOI
//        Circle circle;
//        for(int i = 0; i < allAOIList.size() ; i++)
//        {
//            circle = new Circle(allAOIList.get(i).getCenterX(),allAOIList.get(i).getCenterY(),15);
//            circle.setRadius(15);
//            int times = i % 5;
//            circle.setStroke(colors[times]);
//            graphicsPane.getChildren().add(circle);
//        }

//        ArrayList<Double[]> combinedPointsList;
//        ArrayList<Point2D[]> listOfPoints;
//        for (int i= 0 ; i < allAOIList.size() ; i++)
//        {
//            listOfPoints = new ArrayList<>();
//            listOfPoints.add(allAOIList.get(i).getAllPoint2DOfConvex());
//            for(int j=0; j< allAOIList.size() ; j++) {
//                if (i != j) {
//                    double euclideanDistance = Math.sqrt(Math.pow(allAOIList.get(i).getCenterX() - allAOIList.get(j).getCenterX(), 2) + Math.pow(allAOIList.get(i).getCenterY() - allAOIList.get(j).getCenterY(), 2));
//                    if (euclideanDistance < 160)
//                        listOfPoints.add(allAOIList.get(j).getAllPoint2DOfConvex());
//                    System.out.println("The distance of convex " + i + " to " + j + " is: " + euclideanDistance);
//                }
//            }
//            if(listOfPoints.size() > 1)
//            {
//                Point2D[] combinedPoints = listOfPoints.stream().flatMap(Arrays::stream).toArray(Point2D[]::new);
//                Double[] combinedDouble = calculateConvexHull(combinedPoints,listOfPoints.size());
//                Polygon polygon = new Polygon();
//                polygon.setStroke(Color.ORANGE);
//                polygon.getPoints().addAll(combinedDouble);
//                graphicsPane.getChildren().add(polygon);
//            }
//            System.out.println("The size of the combined convex hull is "+ listOfPoints.size());
//        }

        //calculating and concat AOI
//            for(int i = 0 ; i< listOfPolygons.size() ; i++)
//        {
//            combinedPointsList = new ArrayList<>();
//            combinedPointsList.add(listOfPolygons.get(i).getConvextPoint());
//            for(int j = 0 ; j< listOfPolygons.size() ; j++)
//            {
//                if (i!=j)
//                {
//                    double euclideanDistance = Math.sqrt(Math.pow(listOfPolygons.get(i).getCenterX() - listOfPolygons.get(j).getCenterX(),2) + Math.pow(listOfPolygons.get(i).getCenterY()-listOfPolygons.get(j).getCenterY(),2));
//                    if(euclideanDistance < 160)
//                        combinedPointsList.add(listOfPolygons.get(j).getConvextPoint());
//                    System.out.println("The distance of convex " + i + " to "+ j + " is: "+euclideanDistance);
//                }
//            }
//            Double[] combinedPoints = combinedPointsList.stream().flatMap(Arrays::stream).toArray(Double[]::new);
//
//        }



        for (AreaOfInterestProps areaOfInterestProps : allAOIList) {
            graphicsPane.getChildren().add(areaOfInterestProps.getAreaOfInterest());
        }
        VBox pane = new VBox(1);
        timeLabel = new Label();
        timeLabel.setTextFill(Color.web("#FFFFFF"));
        Button playBtn = new Button("Play");
        playBtn.setAlignment(Pos.CENTER_RIGHT);
        playBtn.setOnAction(e -> {
            for (AreaOfInterestProps areaOfInterestProps : allAOIList) {
                graphicsPane.getChildren().remove(areaOfInterestProps.getAreaOfInterest());
            }
            if(config.isVideoRecordingEnabled())
                player.play();
            intereatorAOI = 0;
            plotMovement(0,graphicsPane);
            long startTime = System.currentTimeMillis();
            clock = new Timeline(new KeyFrame(Duration.ZERO, f -> {
                long theTime = System.currentTimeMillis() - startTime;
                timeLabel.setText(theTime+"");
            }),
                    new KeyFrame(Duration.millis(100))
            );
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();
        });
        if(config.isVideoRecordingEnabled())
        {
            Media media = new Media(stats.getDirectoryOfVideo());
            player = new MediaPlayer(media);
            MediaView mediaView = new MediaView(player);
            stackPane.getChildren().add(mediaView);
        }
        stackPane.getChildren().add(graphicsPane);
        playBtn.setStyle("-fx-alignment: baseline-right;");
        topPane.getChildren().add(playBtn);
        topPane.getChildren().add(timeLabel);
        graphicsPane.setStyle("-fx-background-color: transparent;");
        pane.setStyle("-fx-background-color: #224488");
        root.setCenter(stackPane);
        root.setTop(topPane);
        root.setBottom(pane);
        root.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
    }
}
