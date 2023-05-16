package net.gazeplay.games.trainSwitches;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import java.util.ArrayList;
import java.util.Random;

public class TrainSwitches implements GameLifeCycle {

    private final IGameContext gameContext;
    private final TrainSwitchesGameVariant gameVariant;
    private final Stats stats;

    // Game
    private ArrayList<Section> sections;
    private ArrayList<Switch> switches;
    private ArrayList<Train> trains;
    private ArrayList<Color> colors;
    private ArrayList<Station> stations;
    private Random random;

    private int levelWidth;
    private int levelHeight;

    private final double SPEED = 1;
    private final double XOFFSET = 100;
    private final double YOFFSET = 50;

    TrainSwitches(final IGameContext gameContext, TrainSwitchesGameVariant gameVariant, final Stats stats){
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.stats = stats;
        random = new Random();
        sections = new ArrayList<>();
        switches = new ArrayList<>();
        trains = new ArrayList<>();
        colors = new ArrayList<>();
        stations = new ArrayList<>();
    }

    @Override
    public void launch() {

        initLevel();
        for (Section section : sections) {
            gameContext.getChildren().add(section.getPath());
        }
        for (Station station : stations) {
            gameContext.getChildren().add(station.getShape());
            station.getShape().addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) event -> {
                Train train = new Train(colors.get(random.nextInt(colors.size())));
                gameContext.getChildren().add(train.getShape());
                launchTrain(sections.get(0), train);
            });
        }

        for (Switch aSwitch : switches) {
            aSwitch.updateShape();
            gameContext.getChildren().add(aSwitch.getGroup());
        }

        stats.notifyNewRoundReady();
        gameContext.start();
    }

    public void launchTrain(Section section, Train train){
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(section.getSize()/SPEED));
        pathTransition.setNode(train.getShape());
        pathTransition.setPath(section.getPath());
        pathTransition.setInterpolator(Interpolator.LINEAR);
        pathTransition.setOnFinished(actionEvent -> {
            Switch s = getSwitch(train.getShape().getTranslateX(), train.getShape().getTranslateY());
            if(s!=null){
                launchTrain(s.getOutput(), train);
            }else{
                gameContext.getChildren().remove(train.getShape());
            }
        });
        pathTransition.play();
    }

    @Override
    public void dispose() {
        gameContext.clear();
    }

    public void initLevel(){

        levelWidth = 5;
        levelHeight = 3;

        Switch s1 = createSwitch();
        s1.addCurve(createCurve(4, 0.75,4,1.25,4,1));
        s1.addCurve(createCurve(4, 0.75,3.75,1,4,1));

        Section p1 = createSection(5);
        p1.getPath().getElements().add(createMoveTo(0, 0));
        p1.getPath().getElements().add(createLineTo(4, 0));
        p1.getPath().getElements().add(createLineTo(4, 1));
        s1.setInput(p1);

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
        s2.setInput(p3);

        Section p4 = createSection(1);
        p4.getPath().getElements().add(createMoveTo(1, 2));
        p4.getPath().getElements().add(createLineTo(1, 1));
        s2.addOutput(p4);

        Section p5 = createSection(2);
        p5.getPath().getElements().add(createMoveTo(1, 2));
        p5.getPath().getElements().add(createLineTo(0, 2));
        p5.getPath().getElements().add(createLineTo(0, 1));
        s2.addOutput(p5);

        createStation(Color.RED, 0, 1);
        createStation(Color.BLUE, 1, 1);
        createStation(Color.PURPLE, 3, 2);
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

    public void createStation(Color color, int x, int y){
        Station station = new Station(color);
        colors.add(color);
        station.getShape().widthProperty().bind((gameContext.getPrimaryScene().widthProperty().divide(levelWidth).divide(2)));
        station.getShape().heightProperty().bind((gameContext.getPrimaryScene().heightProperty().divide(levelHeight).divide(2)));
        station.getShape().xProperty().bind(gameContext.getPrimaryScene().widthProperty().divide(levelWidth).multiply(x).add(XOFFSET).subtract(station.getShape().widthProperty().divide(2)));
        station.getShape().yProperty().bind(gameContext.getPrimaryScene().heightProperty().divide(levelHeight).multiply(y).add(YOFFSET).subtract(station.getShape().heightProperty().divide(2)));
        stations.add(station);
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
        s.getGroup().addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) event -> {
            s.changeOutputSelected();
            s.updateShape();
        });
        s.radius.bind(gameContext.getPrimaryScene().heightProperty().divide(levelHeight).divide(4));
        switches.add(s);
        return s;
    }

    public Switch getSwitch(double x, double y){
        for (Switch aSwitch : switches) {
            if(aSwitch.isInside(x,y)){
               return aSwitch;
            }
        }
        return null;
    }

}
