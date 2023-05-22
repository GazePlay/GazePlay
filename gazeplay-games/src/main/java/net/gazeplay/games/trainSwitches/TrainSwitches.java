package net.gazeplay.games.trainSwitches;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.utils.stats.Stats;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class TrainSwitches implements GameLifeCycle {

    // Context
    private final IGameContext gameContext;
    private final TrainSwitchesGameVariant gameVariant;
    private final Stats stats;

    // Game
    private final ArrayList<Section> sections;
    private final ArrayList<Switch> switches;
    private final ArrayList<String> colors;
    private final ArrayList<Station> stations;
    private final ArrayList<PathTransition> transitions;
    private final Random random;
    private Timer sendTrainTimer;
    private final int delayBetweenTrains = 5000;
    private int levelWidth;
    private int levelHeight;
    private String initialTrainDirection;
    private int trainToSend;
    private int trainSent;
    private Instant lastTrainSentInstant;
    private Instant lastTimerStoppedInstant;

    // For pause variant
    private Train trainWaiting;
    private Switch switchWaiting;

    // UI
    private final double XOFFSET = 100;
    private final double YOFFSET = 100;
    private final ProgressIndicator progressIndicator;
    private Timeline progressTimeline;
    private Pane mainPane;
    private I18NButton resumeButton;
    private Label trainCountLabel;

    TrainSwitches(final IGameContext gameContext, TrainSwitchesGameVariant gameVariant, final Stats stats){
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.stats = stats;
        random = new Random();
        sections = new ArrayList<>();
        switches = new ArrayList<>();
        colors = new ArrayList<>();
        stations = new ArrayList<>();
        transitions = new ArrayList<>();
        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setOpacity(0);
        progressIndicator.setMouseTransparent(true);
        gameContext.getConfiguration().setAnimationSpeedRatio(3);
    }

    @Override
    public void launch() {

        trainSent = 0;
        gameContext.getRoot().setBackground(new Background(new BackgroundImage(new Image("data/trainSwitches/images/grassBackground.jpg"),null,null,null,null)));

        BorderPane borderPane = new BorderPane();
        gameContext.getChildren().add(borderPane);

        mainPane = new Pane();
        //gamePane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        borderPane.setCenter(mainPane);

        initLevel();
        for (Section section : sections) {
            mainPane.getChildren().add(section.getPath());
        }
        for (Station station : stations) {
            mainPane.getChildren().add(station.getShape());
        }
        for (Switch aSwitch : switches) {
            aSwitch.updateShape();
            mainPane.getChildren().add(aSwitch.getGroup());
        }

        HBox botBox = new HBox();
        botBox.setAlignment(Pos.CENTER);
        botBox.setPadding(new Insets(20,0,0,0));
        //botBox.setBorder(new Border(new BorderStroke(Color.ORANGE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        borderPane.setBottom(botBox);

        trainCountLabel = new Label("Trains : 0/"+ trainToSend);
        //trainCountLabel.setBorder(new Border(new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        trainCountLabel.setTextFill(Color.WHITE);
        trainCountLabel.setFont(new Font(60));
        trainCountLabel.setPadding(new Insets(0,50,0,0));
        botBox.getChildren().add(trainCountLabel);

        resumeButton = new I18NButton(gameContext.getTranslator(), "ResumeButton");
        resumeButton.setVisible(false);
        //resumeButton.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        resumeButton.setMaxHeight(500);
        resumeButton.setMinWidth(500);
        resumeButton.setMaxWidth(500);
        resumeButton.setOnMouseClicked(mouseEvent -> resume());
        resumeButton.addEventFilter(GazeEvent.GAZE_ENTERED, gazeEvent -> resumeEnterHandle());
        resumeButton.addEventFilter(GazeEvent.GAZE_EXITED, gazeEvent -> exitHandle());
        resumeButton.addEventFilter(MouseEvent.MOUSE_ENTERED, mouseEvent -> resumeEnterHandle());
        resumeButton.addEventFilter(MouseEvent.MOUSE_EXITED, mouseEvent -> exitHandle());
        botBox.getChildren().add(resumeButton);

        sendTrainTimer = new Timer();
        sendTrainTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendTrain();
            }
        }, 5000, delayBetweenTrains);

        gameContext.getChildren().add(progressIndicator);
        stats.notifyNewRoundReady();
        gameContext.start();
    }

    @Override
    public void dispose() {
        gameContext.clear();
    }

    public void resume(){
        if(gameVariant==TrainSwitchesGameVariant.pauseTrain) {
            for (PathTransition transition : transitions) {
                transition.play();
            }
            launchTrainOnSection(switchWaiting.getOutput(), trainWaiting);
            resumeButton.setVisible(false);
            sendTrainTimer = new Timer();
            long elapsed = java.time.Duration.between(lastTrainSentInstant, lastTimerStoppedInstant).toMillis();
            if(elapsed> delayBetweenTrains){
                elapsed = 0;
            }else{
                elapsed = delayBetweenTrains - elapsed;
            }
            sendTrainTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendTrain();
                }
            }, elapsed, delayBetweenTrains);
        }
    }

    public void resumeEnterHandle(){
        progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
        progressIndicator.setMinSize(gameContext.getConfiguration().getProgressBarSize(), gameContext.getConfiguration().getProgressBarSize());
        progressIndicator.setLayoutX(resumeButton.localToScene(resumeButton.getBoundsInLocal()).getMinX()+resumeButton.getWidth()/2-progressIndicator.getWidth()/2);
        progressIndicator.setLayoutY(resumeButton.localToScene(resumeButton.getBoundsInLocal()).getMinY()+resumeButton.getHeight()/2-progressIndicator.getHeight()/2);
        progressIndicator.setProgress(0);
        progressIndicator.setOpacity(1);

        progressTimeline = new Timeline();
        progressTimeline.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()),new KeyValue(progressIndicator.progressProperty(), 1)));
        progressTimeline.setOnFinished(actionEvent -> {
            progressIndicator.setOpacity(0);
            resume();
        });
        progressTimeline.play();
    }

    public void sendTrain(){
        lastTrainSentInstant = Instant.now();
        if(trainSent>= trainToSend){
            sendTrainTimer.cancel();
        }else{
            Platform.runLater(() -> {
                Train train = createTrain(colors.get(random.nextInt(colors.size())));
                mainPane.getChildren().add(train.getShape());
                launchTrainOnSection(sections.get(0), train);
                trainSent++;
                trainCountLabel.setText("Trains: "+trainSent+"/"+ trainToSend);
            });
        }
    }

    public void launchTrainOnSection(Section section, Train train){
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(section.getSize()/(gameContext.getConfiguration().getAnimationSpeedRatio()/(10.0/2))));
        pathTransition.setNode(train.getShape());
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pathTransition.setPath(section.getPath());
        pathTransition.setInterpolator(Interpolator.LINEAR);
        pathTransition.setOnFinished(actionEvent -> {
            transitions.remove(pathTransition);
            Switch aSwitch = getSwitch(train.getShape().getTranslateX()+train.getShape().getFitWidth()/2, train.getShape().getTranslateY()+train.getShape().getFitHeight()/2);
            Station station = getStation(train.getShape().getTranslateX()+train.getShape().getFitWidth()/2, train.getShape().getTranslateY()+train.getShape().getFitHeight()/2);
            if(aSwitch!=null){
                // Train is at a switch
                if(gameVariant==TrainSwitchesGameVariant.pauseTrain){
                    // Pause all trains
                    for (PathTransition transition : transitions) {
                        transition.pause();
                    }
                    trainWaiting = train;
                    switchWaiting = aSwitch;
                    resumeButton.setVisible(true);
                    sendTrainTimer.cancel();
                    lastTimerStoppedInstant = Instant.now();
                }else{
                    // Launch train on new section
                    launchTrainOnSection(aSwitch.getOutput(), train);
                }
            }else if(station!=null){
                // Train is at a station
                mainPane.getChildren().remove(train.getShape());
                ImageView img;
                if(train.getColor().equals(station.getColor())){
                    // Train reached correct station
                    gameContext.getSoundManager().add("data/trainSwitches/sounds/correct.mp3");
                    img = new ImageView(new Image("data/trainSwitches/images/check.png"));
                }else{
                    // Train reached wrong station
                    gameContext.getSoundManager().add("data/trainSwitches/sounds/wrong.mp3");
                    img = new ImageView(new Image("data/trainSwitches/images/cross.png"));
                }
                gameContext.getChildren().add(img);
                img.setPreserveRatio(true);
                img.setFitWidth(station.getShape().getFitWidth());
                img.setFitHeight(station.getShape().getFitHeight());
                img.setX(station.getShape().getX());
                img.setY(station.getShape().getY());
                FadeTransition ft = new FadeTransition(Duration.seconds(3), img);
                ft.setFromValue(1.0);
                ft.setToValue(0);
                ft.play();
            }
        });
        transitions.add(pathTransition);
        pathTransition.play();
    }

    public void initLevel(){

        levelWidth = 5;
        levelHeight = 3;
        trainToSend = 10;
        initialTrainDirection = "right";

        Switch s1 = createSwitch();
        s1.addCurve(createCurve(4, 0.75,4,1.25,4,1));
        s1.addCurve(createCurve(4, 0.75,3.75,1,4,1));

        Section p1 = createSection(5);
        p1.getPath().getElements().add(createMoveTo(0, 0));
        p1.getPath().getElements().add(createLineTo(4, 0));
        p1.getPath().getElements().add(createLineTo(4, 1));

        Section p2 = createSection(2);
        p2.getPath().getElements().add(createMoveTo(4, 1));
        p2.getPath().getElements().add(createLineTo(4, 2));
        p2.getPath().getElements().add(createLineTo(3, 2));
        s1.addOutput(p2);

        Section p3 = createSection(4);
        p3.getPath().getElements().add(createMoveTo(4, 1));
        p3.getPath().getElements().add(createLineTo(2, 1));
        p3.getPath().getElements().add(createLineTo(2, 2));
        p3.getPath().getElements().add(createLineTo(1, 2));
        s1.addOutput(p3);

        Switch s2 = createSwitch();
        s2.addCurve(createCurve(1.25, 2,1,1.75,1,2));
        s2.addCurve(createCurve(1.25, 2,0.75,2,1,2));

        Section p4 = createSection(1);
        p4.getPath().getElements().add(createMoveTo(1, 2));
        p4.getPath().getElements().add(createLineTo(1, 1));
        s2.addOutput(p4);

        Section p5 = createSection(2);
        p5.getPath().getElements().add(createMoveTo(1, 2));
        p5.getPath().getElements().add(createLineTo(0, 2));
        p5.getPath().getElements().add(createLineTo(0, 1));
        s2.addOutput(p5);

        createStation("blue", 0, 1);
        createStation("white", 1, 1);
        createStation("yellow", 3, 2);
    }

    public Section createSection(double size){
        Section section = new Section(size);
        sections.add(section);
        return section;
    }

    public MoveTo createMoveTo(int x, int y){
        MoveTo moveTo = new MoveTo();
        moveTo.xProperty().bind(gameContext.getPrimaryScene().widthProperty().divide(levelWidth).multiply(x).add(XOFFSET));
        moveTo.yProperty().bind(gameContext.getPrimaryScene().heightProperty().divide(levelHeight).multiply(y).add(YOFFSET));
        return moveTo;
    }

    public LineTo createLineTo(int x, int y){
        LineTo lineTo = new LineTo();
        lineTo.xProperty().bind(gameContext.getPrimaryScene().widthProperty().divide(levelWidth).multiply(x).add(XOFFSET));
        lineTo.yProperty().bind(gameContext.getPrimaryScene().heightProperty().divide(levelHeight).multiply(y).add(YOFFSET));
        return lineTo;
    }

    public void createStation(String color, int x, int y){
        Station station = new Station(color);
        colors.add(color);
        station.getShape().fitWidthProperty().bind(gameContext.getPrimaryScene().widthProperty().divide(levelWidth).divide(2));
        station.getShape().fitHeightProperty().bind(gameContext.getPrimaryScene().heightProperty().divide(levelHeight).divide(2));
        station.getShape().xProperty().bind(gameContext.getPrimaryScene().widthProperty().divide(levelWidth).multiply(x).add(XOFFSET).subtract(station.getShape().fitWidthProperty().divide(2)));
        station.getShape().yProperty().bind(gameContext.getPrimaryScene().heightProperty().divide(levelHeight).multiply(y).add(YOFFSET).subtract(station.getShape().fitHeightProperty().divide(2)));
        stations.add(station);
    }

    public Train createTrain(String color){
        Train train = new Train(color, initialTrainDirection);
        train.getShape().fitWidthProperty().bind(gameContext.getPrimaryScene().widthProperty().divide(levelWidth).divide(2));
        train.getShape().fitHeightProperty().bind(gameContext.getPrimaryScene().heightProperty().divide(levelHeight).divide(2));
        return train;
    }

    public QuadCurve createCurve(double startx, double starty, double endx, double endy, double ctrlx, double ctrly){
        QuadCurve curve = new QuadCurve();
        curve.startXProperty().bind(gameContext.getPrimaryScene().widthProperty().divide(levelWidth).multiply(startx).add(XOFFSET));
        curve.startYProperty().bind(gameContext.getPrimaryScene().heightProperty().divide(levelHeight).multiply(starty).add(YOFFSET));
        curve.endXProperty().bind(gameContext.getPrimaryScene().widthProperty().divide(levelWidth).multiply(endx).add(XOFFSET));
        curve.endYProperty().bind(gameContext.getPrimaryScene().heightProperty().divide(levelHeight).multiply(endy).add(YOFFSET));
        curve.controlXProperty().bind(gameContext.getPrimaryScene().widthProperty().divide(levelWidth).multiply(ctrlx).add(XOFFSET));
        curve.controlYProperty().bind(gameContext.getPrimaryScene().heightProperty().divide(levelHeight).multiply(ctrly).add(YOFFSET));
        curve.setStroke(Color.BEIGE);
        curve.setFill(Color.TRANSPARENT);
        curve.setStrokeWidth(20);
        return curve;
    }

    public Switch createSwitch(){
        Switch s = new Switch();
        s.getGroup().addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEvent -> enterSwitchHandle(s));
        s.getGroup().addEventHandler(MouseEvent.MOUSE_EXITED, mouseEvent -> exitHandle());
        s.getGroup().addEventHandler(GazeEvent.GAZE_ENTERED, gazeEvent -> enterSwitchHandle(s));
        s.getGroup().addEventHandler(GazeEvent.GAZE_EXITED, gazeEvent -> exitHandle());

        s.radius.bind(gameContext.getPrimaryScene().heightProperty().divide(levelHeight).divide(4));
        switches.add(s);
        return s;
    }

    public void enterSwitchHandle(Switch s){
        progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
        progressIndicator.setMinSize(gameContext.getConfiguration().getProgressBarSize(), gameContext.getConfiguration().getProgressBarSize());
        progressIndicator.setLayoutX(s.getCenter().getX()-progressIndicator.getWidth()/2);
        progressIndicator.setLayoutY(s.getCenter().getY()-progressIndicator.getHeight()/2);
        progressIndicator.setProgress(0);
        progressIndicator.setOpacity(1);

        progressTimeline = new Timeline();
        progressTimeline.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()),new KeyValue(progressIndicator.progressProperty(), 1)));
        progressTimeline.setOnFinished(actionEvent -> {
            gameContext.getSoundManager().add("data/trainSwitches/sounds/click.mp3");
            progressIndicator.setOpacity(0);
            s.changeOutputSelected();
            s.updateShape();
        });
        progressTimeline.play();
    }

    public void exitHandle(){
        progressIndicator.setOpacity(0);
        progressTimeline.stop();
    }

    public Switch getSwitch(double x, double y){
        for (Switch aSwitch : switches) {
            if(aSwitch.isInside(x,y)){
               return aSwitch;
            }
        }
        return null;
    }

    public Station getStation(double x, double y){
        for (Station station : stations) {
            if(station.isInside(x, y)){
                return station;
            }
        }
        return null;
    }

}
