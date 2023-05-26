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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class TrainSwitches implements GameLifeCycle {

    // Context
    private final IGameContext gameContext;
    private final int level;
    private final String variantType;
    private final Stats stats;

    // Game
    private final static int DELAY_BETWEEN_TRAINS = 5000;
    private final static int INITIAL_DELAY = 5000;
    private final static int PAUSE_DELAY = 200;
    private final static int MAXSPEED = 2;
    private final ArrayList<Section> sections;
    private final ArrayList<Switch> switches;
    private final ArrayList<TrainColors> colors;
    private final ArrayList<Station> stations;
    private final ArrayList<PathTransition> transitions;
    private final Random random;
    private Timer sendTrainTimer;
    private int levelWidth;
    private int levelHeight;
    private int caveX;
    private int caveY;
    private String initialTrainDirection;
    private int trainToSend;
    private int trainSent;
    private int trainCorrect;
    private int trainReachedStation;
    // For pause variant
    private Instant lastTrainSentInstant;
    private Instant lastTimerStoppedInstant;
    private Instant lastResumeInstant;
    private Train trainWaiting;
    private Switch switchWaiting;
    private AtomicBoolean isPaused;

    // UI
    private final static double XOFFSET = 100;
    private final static double YOFFSET = 100;
    private final ProgressIndicator progressIndicator;
    private Timeline progressTimeline;
    private Pane mainPane;
    private I18NButton resumeButton;
    private Label trainCountLabel;
    private final MediaPlayer player;

    public TrainSwitches(final IGameContext gameContext, final Stats stats, int level, String variantType){
        this.gameContext = gameContext;
        this.level = level;
        System.out.println("level " + level);
        this.variantType = variantType;
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
        player = new MediaPlayer(new Media(ClassLoader.getSystemResource("data/trainSwitches/sounds/train.mp3").toString()));
        player.volumeProperty().bind(gameContext.getConfiguration().getEffectsVolumeProperty());
        player.setCycleCount(MediaPlayer.INDEFINITE);
    }

    @Override
    public void launch() {

        stats.notifyNewRoundReady();
        gameContext.start();

        trainSent = 0;
        trainCorrect = 0;
        trainReachedStation = 0;
        isPaused = new AtomicBoolean();
        lastResumeInstant = Instant.now();
        gameContext.getRoot().setBackground(new Background(new BackgroundImage(new Image("data/trainSwitches/images/background.png"),null,null,null,null)));

        BorderPane borderPane = new BorderPane();
        gameContext.getChildren().add(borderPane);

        mainPane = new Pane();
        borderPane.setCenter(mainPane);

        // Draw level
        initLevel(level);
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

        // Bottom box
        HBox botBox = new HBox();
        botBox.setAlignment(Pos.CENTER);
        botBox.setPadding(new Insets(20,0,0,0));
        borderPane.setBottom(botBox);

        trainCountLabel = new Label();
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
        if(variantType.equals("PauseTrain")){
            botBox.getChildren().add(resumeButton);
        }

        // Cave image
        ImageView caveImg = new ImageView(new Image("data/trainSwitches/images/cave.png"));
        caveImg.setManaged(false);
        caveImg.setPreserveRatio(true);
        caveImg.fitWidthProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(1.5));
        caveImg.fitHeightProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(1.5));
        caveImg.xProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(caveX).add(XOFFSET).subtract(caveImg.fitWidthProperty().divide(2)));
        caveImg.yProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(caveY).add(YOFFSET).subtract(caveImg.fitHeightProperty().divide(1.5)));
        mainPane.getChildren().add(caveImg);

        // Initial countdown
        StackPane stackPane = new StackPane();
        stackPane.layoutXProperty().bind(gameContext.getRoot().widthProperty().subtract(stackPane.widthProperty()).divide(2));
        stackPane.layoutYProperty().bind(gameContext.getRoot().heightProperty().subtract(stackPane.heightProperty()).divide(2));
        mainPane.getChildren().add(stackPane);

        Circle countdownCircle = new Circle();
        countdownCircle.setFill(Color.ORANGE);
        countdownCircle.setRadius(200);

        Label countdownLabel = new Label(Integer.toString(INITIAL_DELAY/1000));
        countdownLabel.setFont(new Font(100));

        stackPane.getChildren().addAll(countdownCircle, countdownLabel);

        Timeline t1 = new Timeline(new KeyFrame(Duration.seconds(1),
            e -> countdownLabel.setText(Integer.toString(Integer.parseInt(countdownLabel.getText())-1)),
            new KeyValue(countdownCircle.opacityProperty(), 0),
            new KeyValue(countdownCircle.radiusProperty(), 0),
            new KeyValue(countdownLabel.opacityProperty(), 0)
        ));
        t1.setCycleCount(INITIAL_DELAY/1000);
        t1.play();

        Timeline t2 = new Timeline(new KeyFrame(Duration.seconds((INITIAL_DELAY/1000.0) - 2),
            new KeyValue(countdownCircle.fillProperty(), Color.GREEN),
            new KeyValue(stackPane.translateXProperty(), caveImg.xProperty().add(caveImg.fitWidthProperty().divide(2)).subtract(stackPane.layoutXProperty()).get()),
            new KeyValue(stackPane.translateYProperty(), caveImg.yProperty().add(caveImg.fitHeightProperty().divide(2)).subtract(stackPane.layoutYProperty()).get())
        ));
        t2.setDelay(Duration.seconds(2));
        t2.play();

        // Timer to send train
        sendTrainTimer = new Timer();
        if(variantType.equals("UniqueTrain")){
            sendTrainTimer.schedule(getSendTrainTask(), INITIAL_DELAY);
        }else{
            sendTrainTimer.schedule(getSendTrainTask(), INITIAL_DELAY, DELAY_BETWEEN_TRAINS);
        }

        gameContext.getChildren().add(progressIndicator);
    }

    @Override
    public void dispose() {
        sendTrainTimer.cancel();
        gameContext.clear();
        sections.clear();
        switches.clear();
        colors.clear();
        stations.clear();
        for (PathTransition transition : transitions) {
            transition.stop();
        }
        transitions.clear();
    }

    // Resume the game when it has been paused in the pause variant
    private void resume(){
        lastResumeInstant = Instant.now();
        for (PathTransition transition : transitions) {
            transition.play();
        }
        isPaused.set(false);

        // Launch the train that triggerred the pause
        launchTrainOnSection(switchWaiting.getOutput(), trainWaiting);
        player.play();
        resumeButton.setVisible(false);

        // Restart a timer to send train with the right delay
        sendTrainTimer = new Timer();
        long elapsed = java.time.Duration.between(lastTrainSentInstant, lastTimerStoppedInstant).toMillis();
        if(elapsed> DELAY_BETWEEN_TRAINS){
            elapsed = 0;
        }else{
            elapsed = DELAY_BETWEEN_TRAINS - elapsed;
        }
        sendTrainTimer.schedule(getSendTrainTask(), elapsed, DELAY_BETWEEN_TRAINS);
    }

    // Handle for progress indicator when entering resume button
    private void resumeEnterHandle(){
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

    // Launch a train on a section
    private void launchTrainOnSection(Section section, Train train){
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
            if(aSwitch!=null){ // Train reached a switch
                if(variantType.equals("PauseTrain") && java.time.Duration.between(lastResumeInstant, Instant.now()).toMillis()>PAUSE_DELAY && isPaused.compareAndSet(false, true)){
                    // Pause all trains
                    for (PathTransition transition : transitions) {
                        transition.pause();
                    }
                    trainWaiting = train;
                    switchWaiting = aSwitch;
                    resumeButton.setVisible(true);
                    sendTrainTimer.cancel();
                    lastTimerStoppedInstant = Instant.now();
                    player.pause();
                }else{
                    // Launch train on new section
                    launchTrainOnSection(aSwitch.getOutput(), train);
                }
            }else if(station!=null){ // Train is at a station
                // Stop the train sound if the train is the last on the scene
                if(transitions.isEmpty()){
                    player.stop();
                }
                stats.incrementNumberOfGoalsToReach();
                mainPane.getChildren().remove(train.getShape());
                ImageView img;
                if(train.getColor().equals(station.getColor())){ // Train reached correct station
                    stats.incrementNumberOfGoalsReached();
                    trainCorrect++;
                    gameContext.getSoundManager().add("data/trainSwitches/sounds/correct.mp3");
                    img = new ImageView(new Image("data/trainSwitches/images/check.png"));
                }else{ // Train reached wrong station
                    stats.incrementNumberOfMistakes();
                    gameContext.getSoundManager().add("data/trainSwitches/sounds/wrong.mp3");
                    img = new ImageView(new Image("data/trainSwitches/images/cross.png"));
                    if(variantType.equals("InfiniteTrain")){
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
                if(variantType.equals("UniqueTrain")){
                    sendTrainTimer = new Timer();
                    sendTrainTimer.schedule(getSendTrainTask(), 0);
                }
                trainReachedStation++;
                trainCountLabel.setText("Score: "+trainCorrect+"/"+trainReachedStation);

                // Draw checkmark or cross to show if train reached correct station
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
        if(!isPaused.get()){
            pathTransition.play();
            // Start train sound if not already playing
            if(player.getStatus()!= MediaPlayer.Status.PLAYING){
                player.play();
            }
        }
    }

    // Timer task to send train on the first section
    private TimerTask getSendTrainTask(){
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
                        gameContext.getSoundManager().add("data/trainSwitches/sounds/whistle.mp3");
                        launchTrainOnSection(sections.get(0), train);
                        mainPane.getChildren().add(train.getShape());
                        trainSent++;
                    });
                }
            }
        };
    }

    private Section createSection(double startx, double starty, double size){
        Section section = new Section(size);
        section.add(createMoveTo(startx, starty));
        sections.add(section);
        return section;
    }

    private Section createSection(double startx, double starty){
        Section section = new Section();
        section.add(createMoveTo(startx, starty));
        sections.add(section);
        return section;
    }

    private MoveTo createMoveTo(double x, double y){
        MoveTo moveTo = new MoveTo();
        moveTo.xProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(x).add(XOFFSET));
        moveTo.yProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(y).add(YOFFSET));
        return moveTo;
    }

    private LineTo createLineTo(double x, double y){
        LineTo lineTo = new LineTo();
        lineTo.xProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(x).add(XOFFSET));
        lineTo.yProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(y).add(YOFFSET));
        return lineTo;
    }

    private void createStation(TrainColors color, double x, double y){
        Station station = new Station(color);
        colors.add(color);
        station.getShape().fitWidthProperty().bind(gameContext.getRoot().widthProperty().divide(levelWidth).divide(2));
        station.getShape().fitHeightProperty().bind(gameContext.getRoot().heightProperty().divide(levelHeight).divide(2));
        station.getShape().xProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(x).add(XOFFSET).subtract(station.getShape().fitWidthProperty().divide(2)));
        station.getShape().yProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(y).add(YOFFSET).subtract(station.getShape().fitHeightProperty().divide(2)));
        stations.add(station);
    }

    private Train createTrain(TrainColors color){
        Train train = new Train(color, initialTrainDirection);
        train.getShape().fitWidthProperty().bind(gameContext.getRoot().widthProperty().divide(levelWidth).divide(2));
        train.getShape().fitHeightProperty().bind(gameContext.getRoot().heightProperty().divide(levelHeight).divide(2));
        return train;
    }

    // Create curve used for switch in level creation
    private QuadCurve createCurve(int x, int y, String input, String output){
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

    private Switch createSwitch(double x, double y){
        Switch s = new Switch();
        s.getGroup().addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEvent -> enterSwitchHandle(s));
        s.getGroup().addEventHandler(MouseEvent.MOUSE_EXITED, mouseEvent -> exitHandle());
        s.getGroup().addEventHandler(GazeEvent.GAZE_ENTERED, gazeEvent -> enterSwitchHandle(s));
        s.getGroup().addEventHandler(GazeEvent.GAZE_EXITED, gazeEvent -> exitHandle());
        s.xProperty().bind(gameContext.getRoot().widthProperty().subtract(XOFFSET).divide(levelWidth).multiply(x).add(XOFFSET));
        s.yProperty().bind(gameContext.getRoot().heightProperty().subtract(YOFFSET).divide(levelHeight).multiply(y).add(YOFFSET));
        s.radiusProperty().bind(gameContext.getRoot().heightProperty().divide(levelHeight).divide(4));
        s.addOutput(createSection(x,y));
        s.addOutput(createSection(x,y));
        switches.add(s);
        return s;
    }

    private void enterSwitchHandle(Switch s){
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

    private void exitHandle(){
        progressIndicator.setOpacity(0);
        progressTimeline.stop();
    }

    // Returns the switch at the x,y position if it exists, null otherwise
    private Switch getSwitch(double x, double y){
        for (Switch aSwitch : switches) {
            if(aSwitch.isInside(x,y)){
               return aSwitch;
            }
        }
        return null;
    }

    // Returns the station at the x,y position if it exists, null otherwise
    private Station getStation(double x, double y){
        for (Station station : stations) {
            if(station.isInside(x, y)){
                return station;
            }
        }
        return null;
    }

    private void initLevel(int level){
        switch (level){
            case 3 -> initLevel3();
            case 8 -> initLevel8();
            case 13 -> initLevel13();
            default -> initLevel3();
        }
    }

    private void initLevel3(){

        // Level parameters
        levelWidth = 5;
        levelHeight = 3;
        // Number of trains to send
        trainToSend = 10;
        // Direction of the train when it's coming out of the cave
        initialTrainDirection = "right";
        // Position of the cave
        caveX = 0;
        caveY = 0;

        // First section just after the cave
        Section p1 = createSection(caveX,caveY,5);
        // Segments composing the section
        p1.add(createLineTo(4, 0));
        p1.add(createLineTo(4, 1));

        // First switch with its position
        Switch s1 = createSwitch(4,1);
        s1.addCurve(createCurve(4,1,"up","down"));
        s1.addCurve(createCurve(4,1,"up","left"));

        // Size of the first output section of the switch
        s1.getOutput(0).setSize(2);
        // Segments composing the section
        s1.getOutput(0).add(createLineTo(4,2));
        s1.getOutput(0).add(createLineTo(3,2));

        // Size of the first output section of the switch
        s1.getOutput(1).setSize(4);
        // Segment composing the section
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
        createStation(TrainColors.LIGHTBLUE, 0, 1);
        createStation(TrainColors.WHITE, 1, 1);
        createStation(TrainColors.YELLOW, 3, 2);
    }

    private void initLevel8(){

        levelWidth = 7;
        levelHeight = 5;
        trainToSend = 10;
        initialTrainDirection = "left";
        caveX = 6;
        caveY = 4;

        Section p1 = createSection(caveX,caveY, 3);
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
        createStation(TrainColors.LIGHTBLUE, 0, 1);
        createStation(TrainColors.WHITE, 1, 2);
        createStation(TrainColors.YELLOW, 1, 1);
        createStation(TrainColors.GREEN, 2, 0);
        createStation(TrainColors.PURPLE, 3, 1);
        createStation(TrainColors.PINK, 5, 2);
        createStation(TrainColors.BLACK, 6, 1);
        createStation(TrainColors.RED, 4, 0);
    }

    private void initLevel13(){

        levelWidth = 7;
        levelHeight = 5;
        trainToSend = 10;
        initialTrainDirection = "right";
        caveX = 0;
        caveY = 2;

        Section p1 = createSection(caveX,caveY, 3);
        p1.add(createLineTo(3,2));

        // S1
        Switch s1 = createSwitch(3,2);

        s1.addCurve(createCurve(3,2,"left","right"));
        s1.getOutput(0).setSize(1);
        s1.getOutput(0).add(createLineTo(4,2));

        s1.addCurve(createCurve(3,2,"left","down"));
        s1.getOutput(1).setSize(1);
        s1.getOutput(1).add(createLineTo(3,3));

        // s2
        Switch s2 = createSwitch(3,3);

        s2.addCurve(createCurve(3,3,"up","down"));
        s2.getOutput(0).setSize(1);
        s2.getOutput(0).add(createLineTo(3,4));

        s2.addCurve(createCurve(3,3,"up","left"));
        s2.getOutput(1).setSize(1);
        s2.getOutput(1).add(createLineTo(2,3));

        // s3
        Switch s3 = createSwitch(4,2);

        s3.addCurve(createCurve(4,2,"left","right"));
        s3.getOutput(0).setSize(1);
        s3.getOutput(0).add(createLineTo(5,2));

        s3.addCurve(createCurve(4,2,"left","down"));
        s3.getOutput(1).setSize(1);
        s3.getOutput(1).add(createLineTo(4,3));

        // s4
        Switch s4 = createSwitch(2,3);

        s4.addCurve(createCurve(2,3,"right","left"));
        s4.getOutput(0).setSize(1);
        s4.getOutput(0).add(createLineTo(1,3));

        s4.addCurve(createCurve(2,3,"right","down"));
        s4.getOutput(1).setSize(1);
        s4.getOutput(1).add(createLineTo(2,4));

        // s5
        Switch s5 = createSwitch(1,3);

        s5.addCurve(createCurve(1,3,"right","left"));
        s5.getOutput(0).setSize(1);
        s5.getOutput(0).add(createLineTo(0,3));

        s5.addCurve(createCurve(1,3,"right","down"));
        s5.getOutput(1).setSize(1);
        s5.getOutput(1).add(createLineTo(1,4));

        // s6
        Switch s6 = createSwitch(4,3);

        s6.addCurve(createCurve(4,3,"up","right"));
        s6.getOutput(0).setSize(1);
        s6.getOutput(0).add(createLineTo(5,3));

        s6.addCurve(createCurve(4,3,"up","down"));
        s6.getOutput(1).setSize(1);
        s6.getOutput(1).add(createLineTo(4,4));

        // s7
        Switch s7 = createSwitch(5,2);

        s7.addCurve(createCurve(5,2,"left","right"));
        s7.getOutput(0).setSize(1);
        s7.getOutput(0).add(createLineTo(6,2));

        s7.addCurve(createCurve(5,2,"left","up"));
        s7.getOutput(1).setSize(1);
        s7.getOutput(1).add(createLineTo(5,1));

        // s8
        Switch s8 = createSwitch(5,3);

        s8.addCurve(createCurve(5,3,"left","right"));
        s8.getOutput(0).setSize(1);
        s8.getOutput(0).add(createLineTo(6,3));

        s8.addCurve(createCurve(5,3,"left","down"));
        s8.getOutput(1).setSize(1);
        s8.getOutput(1).add(createLineTo(5,4));

        // s9
        Switch s9 = createSwitch(5,1);

        s9.addCurve(createCurve(5,1,"down","left"));
        s9.getOutput(0).setSize(1);
        s9.getOutput(0).add(createLineTo(4,1));

        s9.addCurve(createCurve(5,1,"down","up"));
        s9.getOutput(1).setSize(1);
        s9.getOutput(1).add(createLineTo(5,0));

        // s10
        Switch s10 = createSwitch(4,1);

        s10.addCurve(createCurve(4,1,"right","left"));
        s10.getOutput(0).setSize(1);
        s10.getOutput(0).add(createLineTo(3,1));

        s10.addCurve(createCurve(4,1,"right","up"));
        s10.getOutput(1).setSize(1);
        s10.getOutput(1).add(createLineTo(4,0));

        // s11
        Switch s11 = createSwitch(3,1);

        s11.addCurve(createCurve(3,1,"right","left"));
        s11.getOutput(0).setSize(1);
        s11.getOutput(0).add(createLineTo(2,1));

        s11.addCurve(createCurve(3,1,"right","up"));
        s11.getOutput(1).setSize(1);
        s11.getOutput(1).add(createLineTo(3,0));

        // s12
        Switch s12 = createSwitch(2,1);

        s12.addCurve(createCurve(2,1,"right","left"));
        s12.getOutput(0).setSize(2);
        s12.getOutput(0).add(createLineTo(1,1));
        s12.getOutput(0).add(createLineTo(1,0));

        s12.addCurve(createCurve(2,1,"right","up"));
        s12.getOutput(1).setSize(1);
        s12.getOutput(1).add(createLineTo(2,0));

        // Stations
        createStation(TrainColors.BLACK, 1, 0);
        createStation(TrainColors.BROWN, 2, 0);
        createStation(TrainColors.DARKBLUE, 3, 0);
        createStation(TrainColors.DARKGREEN, 4, 0);
        createStation(TrainColors.GREEN, 5, 0);
        createStation(TrainColors.GREY, 1, 4);
        createStation(TrainColors.LIGHTBLUE, 2, 4);
        createStation(TrainColors.ORANGE, 3, 4);
        createStation(TrainColors.PINK, 4, 4);
        createStation(TrainColors.PURPLE, 5, 4);
        createStation(TrainColors.RED, 0, 3);
        createStation(TrainColors.WHITE, 6, 2);
        createStation(TrainColors.YELLOW, 6, 3);
    }

}
