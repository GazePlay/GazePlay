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
    private final static int DELAY_BETWEEN_TRAINS = 5000;
    private final static int MAXSPEED = 2;
    private final ArrayList<Section> sections;
    private final ArrayList<Switch> switches;
    private final ArrayList<String> colors;
    private final ArrayList<Station> stations;
    private final ArrayList<PathTransition> transitions;
    private final Random random;
    private Timer sendTrainTimer;
    private int levelWidth;
    private int levelHeight;
    private String initialTrainDirection;
    private int trainToSend;
    private int trainSent;
    private int trainCorrect;
    private int trainReachedStation;
    private Instant lastTrainSentInstant;
    private Instant lastTimerStoppedInstant;

    // For pause variant
    private Train trainWaiting;
    private Switch switchWaiting;

    // UI
    private final static double XOFFSET = 100;
    private final static double YOFFSET = 100;
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
        trainCorrect = 0;
        gameContext.getRoot().setBackground(new Background(new BackgroundImage(new Image("data/trainSwitches/images/grassBackground.jpg"),null,null,null,null)));

        BorderPane borderPane = new BorderPane();
        gameContext.getChildren().add(borderPane);

        mainPane = new Pane();
        borderPane.setCenter(mainPane);

        initLevel2();
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
        borderPane.setBottom(botBox);

        trainCountLabel = new Label("Score : 0/0");
        trainCountLabel.setTextFill(Color.WHITE);
        trainCountLabel.setFont(new Font(60));
        trainCountLabel.setPadding(new Insets(0,50,0,0));
        botBox.getChildren().add(trainCountLabel);

        resumeButton = new I18NButton(gameContext.getTranslator(), "ResumeButton");
        resumeButton.setVisible(false);
        resumeButton.setMaxHeight(500);
        resumeButton.setMinWidth(500);
        resumeButton.setMaxWidth(500);
        resumeButton.setOnMouseClicked(mouseEvent -> resume());
        resumeButton.addEventFilter(GazeEvent.GAZE_ENTERED, gazeEvent -> resumeEnterHandle());
        resumeButton.addEventFilter(GazeEvent.GAZE_EXITED, gazeEvent -> exitHandle());
        resumeButton.addEventFilter(MouseEvent.MOUSE_ENTERED, mouseEvent -> resumeEnterHandle());
        resumeButton.addEventFilter(MouseEvent.MOUSE_EXITED, mouseEvent -> exitHandle());
        if(gameVariant==TrainSwitchesGameVariant.pauseTrain){
            botBox.getChildren().add(resumeButton);
        }

        sendTrainTimer = new Timer();
        if(gameVariant==TrainSwitchesGameVariant.uniqueTrain){
            sendTrainTimer.schedule(getSendTrainTask(), 5000);
        }else{
            sendTrainTimer.schedule(getSendTrainTask(), 5000, DELAY_BETWEEN_TRAINS);
        }

        gameContext.getChildren().add(progressIndicator);
        stats.notifyNewRoundReady();
        gameContext.start();
    }

    @Override
    public void dispose() {
        gameContext.clear();
        sections.clear();
        switches.clear();
        colors.clear();
        stations.clear();
        transitions.clear();
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
            if(elapsed> DELAY_BETWEEN_TRAINS){
                elapsed = 0;
            }else{
                elapsed = DELAY_BETWEEN_TRAINS - elapsed;
            }
            sendTrainTimer.schedule(getSendTrainTask(), elapsed, DELAY_BETWEEN_TRAINS);
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

    public void launchTrainOnSection(Section section, Train train){
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(section.getSize()/(gameContext.getConfiguration().getAnimationSpeedRatio()/(10.0/MAXSPEED))));
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
                    // TODO add minimum time between pause ?
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
                    trainCorrect++;
                    gameContext.getSoundManager().add("data/trainSwitches/sounds/correct.mp3");
                    img = new ImageView(new Image("data/trainSwitches/images/check.png"));
                }else{
                    // Train reached wrong station
                    gameContext.getSoundManager().add("data/trainSwitches/sounds/wrong.mp3");
                    img = new ImageView(new Image("data/trainSwitches/images/cross.png"));
                    if(gameVariant==TrainSwitchesGameVariant.infiniteTrain){
                        // If trains are already all sent, restart a timer
                        if (trainSent >= trainToSend++){
                            long startDelay = java.time.Duration.between(lastTimerStoppedInstant, Instant.now()).toMillis();
                            if(startDelay>= DELAY_BETWEEN_TRAINS){
                                startDelay = 0;
                            }else{
                                startDelay = DELAY_BETWEEN_TRAINS -startDelay;
                            }
                            sendTrainTimer = new Timer();
                            sendTrainTimer.schedule(getSendTrainTask(), startDelay, DELAY_BETWEEN_TRAINS);
                        }
                    }
                }
                if(gameVariant==TrainSwitchesGameVariant.uniqueTrain){
                    sendTrainTimer = new Timer();
                    sendTrainTimer.schedule(getSendTrainTask(), 0);
                }
                trainReachedStation++;
                trainCountLabel.setText("Score: "+trainCorrect+"/"+trainReachedStation);
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

    public TimerTask getSendTrainTask(){
        return new TimerTask() {
            @Override
            public void run() {
                lastTrainSentInstant = Instant.now();
                if(trainSent>= trainToSend){
                    sendTrainTimer.cancel();
                    lastTimerStoppedInstant = Instant.now();
                }else{
                    Platform.runLater(() -> {
                        Train train = createTrain(colors.get(random.nextInt(colors.size())));
                        mainPane.getChildren().add(train.getShape());
                        launchTrainOnSection(sections.get(0), train);
                        trainSent++;
                    });
                }
            }
        };
    }

    public void initLevel(){

        levelWidth = 5;
        levelHeight = 3;
        trainToSend = 10;
        initialTrainDirection = "right";

        Section p1 = createSection(0,0,5);
        p1.add(createLineTo(4, 0));
        p1.add(createLineTo(4, 1));

        // S1
        Switch s1 = createSwitch(4,1);
        s1.addCurve(createCurve(4,1,"up","down"));
        s1.addCurve(createCurve(4,1,"up","left"));

        s1.getOutput(0).setSize(2);
        s1.getOutput(0).add(createLineTo(4,2));
        s1.getOutput(0).add(createLineTo(3,2));

        s1.getOutput(1).setSize(4);
        s1.getOutput(1).add(createLineTo(2, 1));
        s1.getOutput(1).add(createLineTo(2, 2));
        s1.getOutput(1).add(createLineTo(1, 2));

        // S2
        Switch s2 = createSwitch(1,2);
        s2.addCurve(createCurve(1,2,"right","up"));
        s2.addCurve(createCurve(1,2,"right","left"));

        s2.getOutput(0).setSize(1);
        s2.getOutput(0).add(createLineTo(1, 1));

        s2.getOutput(1).setSize(2);
        s2.getOutput(1).add(createLineTo(0, 2));
        s2.getOutput(1).add(createLineTo(0, 1));

        // Stations
        createStation("lightblue", 0, 1);
        createStation("white", 1, 1);
        createStation("yellow", 3, 2);
    }

    public void initLevel2(){

        levelWidth = 7;
        levelHeight = 5;
        trainToSend = 10;
        initialTrainDirection = "left";

        Section p1 = createSection(6,4, 3);
        p1.add(createLineTo(3,4));

        // S1
        Switch s1 = createSwitch(3,4);

        s1.addCurve(createCurve(3,4,"right","up"));
        s1.getOutput(0).setSize(2);
        s1.getOutput(0).add(createLineTo(3,3));
        s1.getOutput(0).add(createLineTo(4,3));

        s1.addCurve(createCurve(3,4,"right","left"));
        s1.getOutput(1).setSize(2);
        s1.getOutput(1).add(createLineTo(2,4));
        s1.getOutput(1).add(createLineTo(2,3));

        // S2
        Switch s2 = createSwitch(2,3);

        s2.addCurve(createCurve(2,3,"down","up"));
        s2.getOutput(0).setSize(2);
        s2.getOutput(0).add(createLineTo(2,1));

        s2.addCurve(createCurve(2,3,"down","left"));
        s2.getOutput(1).setSize(3);
        s2.getOutput(1).add(createLineTo(0,3));
        s2.getOutput(1).add(createLineTo(0,2));

        // S3
        Switch s3 = createSwitch(0,2);

        s3.addCurve(createCurve(0,2,"down","up"));
        s3.getOutput(0).setSize(1);
        s3.getOutput(0).add(createLineTo(0,1));

        s3.addCurve(createCurve(0,2,"down","right"));
        s3.getOutput(1).setSize(1);
        s3.getOutput(1).add(createLineTo(1,2));

        // S4
        Switch s4 = createSwitch(2,1);

        s4.addCurve(createCurve(2,1,"down","up"));
        s4.getOutput(0).setSize(1);
        s4.getOutput(0).add(createLineTo(2,0));

        s4.addCurve(createCurve(2,1,"down","left"));
        s4.getOutput(1).setSize(1);
        s4.getOutput(1).add(createLineTo(1,1));

        // S5
        Switch s5 = createSwitch(4,3);

        s5.addCurve(createCurve(4,3,"left","up"));
        s5.getOutput(0).setSize(2);
        s5.getOutput(0).add(createLineTo(4,1));

        s5.addCurve(createCurve(4,3,"left","right"));
        s5.getOutput(1).setSize(3);
        s5.getOutput(1).add(createLineTo(6,3));
        s5.getOutput(1).add(createLineTo(6,2));

        // S6
        Switch s6 = createSwitch(4,1);

        s6.addCurve(createCurve(4,1,"down","up"));
        s6.getOutput(0).setSize(1);
        s6.getOutput(0).add(createLineTo(4,0));

        s6.addCurve(createCurve(4,1,"down","left"));
        s6.getOutput(1).setSize(1);
        s6.getOutput(1).add(createLineTo(3,1));

        // S7
        Switch s7 = createSwitch(6,2);

        s7.addCurve(createCurve(6,2,"down","up"));
        s7.getOutput(0).setSize(1);
        s7.getOutput(0).add(createLineTo(6,1));

        s7.addCurve(createCurve(6,2,"down","left"));
        s7.getOutput(1).setSize(1);
        s7.getOutput(1).add(createLineTo(5,2));

        // Stations
        createStation("lightblue", 0, 1);
        createStation("white", 1, 2);
        createStation("yellow", 1, 1);
        createStation("green", 2, 0);
        createStation("purple", 3, 1);
        createStation("pink", 5, 2);
        createStation("black", 6, 1);
        createStation("red", 4, 0);
    }

    public Section createSection(double startx, double starty, double size){
        Section section = new Section(size);
        section.add(createMoveTo(startx, starty));
        sections.add(section);
        return section;
    }

    public Section createSection(double startx, double starty){
        Section section = new Section();
        section.add(createMoveTo(startx, starty));
        sections.add(section);
        return section;
    }

    public MoveTo createMoveTo(double x, double y){
        MoveTo moveTo = new MoveTo();
        moveTo.xProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(x).add(XOFFSET));
        moveTo.yProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(y).add(YOFFSET));
        return moveTo;
    }

    public LineTo createLineTo(double x, double y){
        LineTo lineTo = new LineTo();
        lineTo.xProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(x).add(XOFFSET));
        lineTo.yProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(y).add(YOFFSET));
        return lineTo;
    }

    public void createStation(String color, double x, double y){
        Station station = new Station(color);
        colors.add(color);
        station.getShape().fitWidthProperty().bind(gameContext.getRoot().widthProperty().divide(levelWidth).divide(2));
        station.getShape().fitHeightProperty().bind(gameContext.getRoot().heightProperty().divide(levelHeight).divide(2));
        station.getShape().xProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(x).add(XOFFSET).subtract(station.getShape().fitWidthProperty().divide(2)));
        station.getShape().yProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(y).add(YOFFSET).subtract(station.getShape().fitHeightProperty().divide(2)));
        stations.add(station);
    }

    public Train createTrain(String color){
        Train train = new Train(color, initialTrainDirection);
        train.getShape().fitWidthProperty().bind(gameContext.getRoot().widthProperty().divide(levelWidth).divide(2));
        train.getShape().fitHeightProperty().bind(gameContext.getRoot().heightProperty().divide(levelHeight).divide(2));
        return train;
    }

    public QuadCurve createCurve(int x, int y, String input, String output){
        QuadCurve curve = new QuadCurve();
        curve.setStroke(Color.BEIGE);
        curve.setFill(Color.TRANSPARENT);
        curve.setStrokeWidth(20);
        curve.controlXProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(x).add(XOFFSET));
        curve.controlYProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(y).add(YOFFSET));

        double addx = 0;
        double addy = 0;
        switch (input){
            case "up" -> {
                addy = -0.25;
            }
            case "down" -> {
                addy = 0.25;
            }
            case "left" -> {
                addx = -0.25;
            }
            case "right" -> {
                addx = 0.25;
            }
            default -> {}
        }
        curve.startXProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(x+addx).add(XOFFSET));
        curve.startYProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(y+addy).add(YOFFSET));

        addx = 0;
        addy = 0;
        switch (output){
            case "up" -> {
                addy = -0.25;
            }
            case "down" -> {
                addy = 0.25;
            }
            case "left" -> {
                addx = -0.25;
            }
            case "right" -> {
                addx = 0.25;
            }
            default -> {}
        }
        curve.endXProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(x+addx).add(XOFFSET));
        curve.endYProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(y+addy).add(YOFFSET));

        return curve;
    }

    public Switch createSwitch(double x, double y){
        Switch s = new Switch();
        s.getGroup().addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEvent -> enterSwitchHandle(s));
        s.getGroup().addEventHandler(MouseEvent.MOUSE_EXITED, mouseEvent -> exitHandle());
        s.getGroup().addEventHandler(GazeEvent.GAZE_ENTERED, gazeEvent -> enterSwitchHandle(s));
        s.getGroup().addEventHandler(GazeEvent.GAZE_EXITED, gazeEvent -> exitHandle());
        s.xProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(x).add(XOFFSET));
        s.yProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(y).add(YOFFSET));
        s.radius.bind(gameContext.getRoot().heightProperty().divide(levelHeight).divide(4));
        s.addOutput(createSection(x,y));
        s.addOutput(createSection(x,y));
        switches.add(s);
        return s;
    }

    public void enterSwitchHandle(Switch s){
        progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
        progressIndicator.setMinSize(gameContext.getConfiguration().getProgressBarSize(), gameContext.getConfiguration().getProgressBarSize());
        progressIndicator.setLayoutX(s.xProperty().get()-progressIndicator.getWidth()/2);
        progressIndicator.setLayoutY(s.yProperty().get()-progressIndicator.getHeight()/2);
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
