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
    private ArrayList<ConvexHullProps> listOfPolygons;


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
                            graphicsPane.getChildren().add(circle);
//                            new Timer().schedule(
//                                    new java.util.TimerTask() {
//                                        @Override
//                                        public void run() {
//                                            Platform.runLater(() -> graphicsPane.getChildren().remove(circle));
//                                        }
//                                    },
//                                    2000
//                            );
                            if(index != movementHistory.size()-1)
                            {
                                plotMovement(index+1,graphicsPane);
                            }else {
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

            if(eDistance < 80 && movementHistory.get(index).getIntervalTime() > 10)
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
                    Point2D[] point2DS = new Point2D[areaOfInterestList.size()];
                    for(int i = 0 ; i< areaOfInterestList.size() ; i++)
                    {
                        centerX += areaOfInterestList.get(i).getxValue();
                        centerY += areaOfInterestList.get(i).getyValue();
                        point2DS[i] = new Point2D(areaOfInterestList.get(i).getxValue(),areaOfInterestList.get(i).getyValue());
                    }
                    // can do this later after combination of convex hulls;
                    Double[] convexHullPoints = calculateConvexHull(point2DS,areaOfInterestList.size());
                    centerX = centerX / areaOfInterestList.size();
                    centerY = centerY / areaOfInterestList.size();
                    AreaOfInterestProps areaOfInterestProps = new AreaOfInterestProps(TTFF, timeSpent, areaOfInterestList, centerX, centerY,convexHullPoints,point2DS);
                    allAOIList.add(areaOfInterestProps);
                }else{
                    System.out.println("Not enough points to make a convex hull" + areaOfInterestList.size());
                }
                areaOfInterestList = new ArrayList<>();

            }
            System.out.println("The distance between the points is "+ eDistance);
        }
        if(index != movementHistory.size()-1)
            calculateAreaOfInterest(index+1,startTime);
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
        Vector<Point2D> hull = new Vector<Point2D>();
        int l = 0;
        for (int i = 1; i < n; i++)
            if (points[i].getX() < points[l].getX())
                l = i;
        int p = l, q;
        do
        {
            hull.add(points[p]);
            q = (p + 1) % n;
            for (int i = 0; i < n; i++)
            {
                if (orientation(points[p], points[i], points[q])
                        == 2)
                    q = i;
            }
            p = q;
        } while (p != l);
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
        Configuration config = Configuration.getInstance();
        GridPane grid = new GridPane();
        StackPane stackPane = new StackPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(100);
        grid.setVgap(50);
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


//        Circle circle;
//        for(int i = 0; i < allAOIList.size() ; i++)
//        {
        //center of AOI
//            circle = new Circle(allAOIList.get(i).getCenterX(),allAOIList.get(i).getCenterY(),15);
//            circle.setRadius(15);
//            int times = i % 5;
//            circle.setStroke(colors[times]);
//            graphicsPane.getChildren().add(circle);
//        }

        ArrayList<Double[]> combinedPointsList;
        ArrayList<Point2D[]> listOfPoints;
        for (int i= 0 ; i < allAOIList.size() ; i++)
        {
            listOfPoints = new ArrayList<>();
            listOfPoints.add(allAOIList.get(i).getAllPoint2DOfConvex());
            for(int j=0; j< allAOIList.size() ; j++)
            {
                if (i!=j){
                    double euclideanDistance = Math.sqrt(Math.pow(allAOIList.get(i).getCenterX() - allAOIList.get(j).getCenterX(),2) + Math.pow(allAOIList.get(i).getCenterY()-allAOIList.get(j).getCenterY(),2));
                    if(euclideanDistance < 160)
                        listOfPoints.add(allAOIList.get(j).getAllPoint2DOfConvex());
                    System.out.println("The distance of convex " + i + " to "+ j + " is: "+euclideanDistance);

                }
            }
            System.out.println("The size of the combined convex hull is "+ listOfPoints.size());
        }

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

        //drawing all AOI
        listOfPolygons = new ArrayList<ConvexHullProps>();
        for (int i= 0 ; i < allAOIList.size() ; i++)
        {
            Polygon polygon = new Polygon();

            int times = i % 5;
            polygon.setStroke(colors[times]);
            Double[] convexHullPoints = allAOIList.get(i).getConvexPoints();
            polygon.getPoints().addAll(convexHullPoints);
            graphicsPane.getChildren().add(polygon);
            listOfPolygons.add(new ConvexHullProps(allAOIList.get(i).getCenterX(),allAOIList.get(i).getCenterY(),polygon,allAOIList.get(i).getConvexPoints()));
        }






        VBox pane = new VBox(1);
        timeLabel = new Label();
        timeLabel.setTextFill(Color.web("#FFFFFF"));
        Button playBtn = new Button("Play");
        playBtn.setAlignment(Pos.CENTER_RIGHT);
        playBtn.setOnAction(e -> {
            if(config.isVideoRecordingEnabled())
                player.play();
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
