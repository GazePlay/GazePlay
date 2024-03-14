package net.gazeplay.games.follow;

import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GamesRules;
import net.gazeplay.components.SaveData;

import java.util.ArrayList;

@Slf4j
public class FollowEmmanuel implements GameLifeCycle {

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    @Getter
    @Setter
    private FollowEmmanuelGameVariant variant;

    private FollowEmmanuelGenerateLabyrinthLevel1 generateLabyrinthLevel1;
    private FollowEmmanuelGenerateLabyrinthLevel2 generateLabyrinthLevel2;
    private FollowEmmanuelGenerateLabyrinthLevel3 generateLabyrinthLevel3;

    private final Dimension2D dimension2D;

    //player's position
    private double px;
    private double py;

    //size of the player square
    double sizeP;

    //gaze's position
    private double rx;
    private double ry;

    //speed of the player
    private double speed;

    //player square
    private Rectangle rPlayer;

    //If the player can move or not
    private boolean canmove;

    //List of walls
    private final ArrayList<Rectangle> listWall;

    //List of EventItems
    private final ArrayList<EventItemEmmanuel> listEI;

    //Pointer of the gaze
    private Rectangle gaze;

    //number position item (x,y)
    private int x;
    private int y;

    //size wall/item
    private double sizeWw;
    private double sizeWh;

    //score with coins
    private int score;
    private int scoretoreach;
    public boolean firstGame = true;
    public CustomInputEventHandlerKeyboard customInputEventHandlerKeyboard = new CustomInputEventHandlerKeyboard();
    private PauseTransition next;
    public SaveData saveData;

    FollowEmmanuel(final IGameContext gameContext, final Stats stats, final FollowEmmanuelGameVariant variant) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;
        this.generateLabyrinthLevel1 = new FollowEmmanuelGenerateLabyrinthLevel1();
        this.generateLabyrinthLevel2 = new FollowEmmanuelGenerateLabyrinthLevel2();
        this.generateLabyrinthLevel3 = new FollowEmmanuelGenerateLabyrinthLevel3();
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, customInputEventHandlerKeyboard);
        this.saveData = new SaveData(this.stats, variant.getLabel());
        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        listWall = new ArrayList<>();
        listEI = new ArrayList<>();

        this.gameContext.startTimeLimiterEmmanuel(this.saveData);
    }

    @Override
    public void launch() {

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();

        if (this.firstGame){
            this.firstGame = false;
            String rule = "Déplacer le personnage dans le labyrinthe pour récupérer les clés assorties aux portes \n Vous avez 2 minutes pour faire le plus de tableaux possibles";
            final Transition animation = new GamesRules().createQuestionTransition(gameContext, rule);
            animation.play();
            animation.setOnFinished(event -> {
                this.generateGame();
            });
        }else {
            this.generateGame();
        }

    }

    public void generateGame(){
        gameContext.getChildren().clear();

        score = 0;
        scoretoreach = 0;

        canmove = true;

        x = 32;
        y = 18;

        sizeWw = dimension2D.getWidth() / x;
        sizeWh = dimension2D.getHeight() / y;

        py = dimension2D.getHeight() / 2;
        px = dimension2D.getWidth() / 2;

        sizeP = dimension2D.getWidth() / 50;

        //Make the paving of the floor
        paving();

        //Make the player
        player();

        //increase the speed but decrease the accuracy
        speed = 4;

        //Make the border of the screen
        contour();

        if (variant.equals(FollowEmmanuelGameVariant.Level1)) {
            getRubyLvl1();
        } else if (variant.equals(FollowEmmanuelGameVariant.Level2)){
            getRubyLvl2();
        }else if (variant.equals(FollowEmmanuelGameVariant.Level3)){
            getRubyLvl3();
        } else {
            log.error("Variant not found : " + variant.getLabel());
        }

        pointer();

        {
            Scene gameContextScene = gameContext.getPrimaryScene();

            EventHandler<GazeEvent> recordGazeMovements = e -> {
                Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                if (((toSceneCoordinate.getX() - dimension2D.getWidth() / 2) * (toSceneCoordinate.getX() - dimension2D.getWidth() / 2) + (toSceneCoordinate.getY() - dimension2D.getHeight() / 2) * (toSceneCoordinate.getY() - dimension2D.getHeight() / 2)) > 0.15) {
                    rx = toSceneCoordinate.getX();
                    ry = toSceneCoordinate.getY();
                }

            };

            EventHandler<MouseEvent> recordMouseMovements = e -> {
                Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                rx = toSceneCoordinate.getX();
                ry = toSceneCoordinate.getY();

            };

            gameContextScene.getRoot().addEventFilter(GazeEvent.GAZE_MOVED, recordGazeMovements);
            gameContextScene.getRoot().addEventFilter(MouseEvent.MOUSE_MOVED, recordMouseMovements);
        }

        startafterdelay();
    }

    @Override
    public void dispose() {
        listEI.clear();
        listWall.clear();
        next.stop();
        gameContext.getChildren().clear();
    }

    public void next(){
        listEI.clear();
        listWall.clear();
        next.stop();
        gameContext.getChildren().clear();
        this.saveData.addMouseMovements(this.stats.fixationSequence.get(0).size());
        this.saveData.addTrackerMovements(this.stats.fixationSequence.get(1).size());
    }

    private void followthegaze() {
        next = new PauseTransition(Duration.millis(5));
        next.setOnFinished(nextevent -> {
            followthegaze();
            position();
            double x = rx - px;
            double y = ry - py;
            double dist = Math.sqrt(x * x + y * y);
            if (canmove) {
                double tx;
                double ty;
                if (dist > speed) {
                    tx = px + speed * x / dist;
                    ty = py + speed * y / dist;
                } else {
                    tx = rx;
                    ty = ry;
                }

                boolean test = true;

                for (Rectangle wall : listWall) {
                    if (isInWall(wall, tx, py, sizeP)) {
                        if (x > 0) {
                            tx = wall.getX() - 1.001 * sizeP / 2;
                        } else {
                            tx = wall.getX() + wall.getWidth() + 1.001 * sizeP / 2;
                        }
                        test = false;
                    }
                    if (isInWall(wall, px, ty, sizeP)) {
                        if (y > 0) {
                            ty = wall.getY() - 1.001 * sizeP / 2;
                        } else {
                            ty = wall.getY() + wall.getHeight() + 1.001 * sizeP / 2;
                        }
                        test = false;
                    }
                }
                if (test) {
                    for (Rectangle wall : listWall) {
                        if (isInWall(wall, tx, ty, sizeP)) {
                            if (Math.abs(x) > Math.abs(y)) {
                                if (x > 0) {
                                    tx = wall.getX() - 1.001 * sizeP / 2;
                                } else {
                                    tx = wall.getX() + wall.getWidth() + 1.001 * sizeP / 2;
                                }
                            } else {
                                if (y > 0) {
                                    ty = wall.getY() - 1.001 * sizeP / 2;
                                } else {
                                    ty = wall.getY() + wall.getHeight() + 1.001 * sizeP / 2;
                                }
                            }
                        }
                    }
                }
                boolean in = false;
                for (Rectangle wall : listWall) {
                    in = in || isInWall(wall, tx, ty, sizeP);
                }
                if (!in) {
                    px = tx;
                    py = ty;
                }
                rPlayer.setX(px - sizeP / 2);
                rPlayer.setY(py - sizeP / 2);
                checkEI();
            }
        });
        next.play();
    }

    private void position() {
        gaze.setX(rx);
        gaze.setY(ry);
    }

    private void startafterdelay() {
        PauseTransition wait = new PauseTransition(Duration.millis(1000));
        wait.setOnFinished(waitevent -> followthegaze());
        wait.play();
    }

    private boolean isInWall(Rectangle wall, double x, double y, double size) {
        double wx = wall.getX() + size / 2;
        double wy = wall.getY() + size / 2;
        double ww = wall.getWidth();
        double wh = wall.getHeight();

        return (x + size > wx) && (y + size > wy) && (x < wx + ww) && (y < wy + wh);
    }

    private void win() {
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, this);
        next();
        launch();
    }

    private void checkEI() {
        ArrayList<EventItemEmmanuel> remove = new ArrayList<>();
        for (EventItemEmmanuel eI : listEI) {
            if (isInWall(eI, px, py, sizeP)) {
                if (eI.remove) {
                    remove.add(eI);
                    gameContext.getChildren().remove(eI);
                }
                eI.active();
            }
        }
        listEI.removeAll(remove);
    }

    private void multigoals() {
        if (score >= scoretoreach) {
            win();
        }
    }

    private void player() {
        rPlayer = new Rectangle(px - sizeP / 2, py - sizeP / 2, sizeP, sizeP);
        rPlayer.setFill(new ImagePattern(new Image("data/follow/Biboule.png")));
        gameContext.getChildren().add(rPlayer);
    }

    private void pointer() {
        gaze = new Rectangle(0, 0, dimension2D.getWidth() / 200, dimension2D.getWidth() / 200);
        gaze.setFill(new ImagePattern(new Image("data/follow/ruby1RS.png")));
        gameContext.getChildren().add(gaze);
    }

    private void contour() {
        Rectangle w;
        for (int i = 0; i < x; i++) {
            w = new Rectangle(i * sizeWw, 0, sizeWw, sizeWh);
            w.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            listWall.add(w);
            gameContext.getChildren().add(w);
            w = new Rectangle(i * sizeWw, dimension2D.getHeight() - sizeWh, sizeWw, sizeWh);
            w.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            listWall.add(w);
            gameContext.getChildren().add(w);
        }
        for (int i = 1; i < y - 1; i++) {
            w = new Rectangle(0, i * sizeWh, sizeWw, sizeWh);
            w.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            listWall.add(w);
            gameContext.getChildren().add(w);
            w = new Rectangle(dimension2D.getWidth() - sizeWw, i * sizeWh, sizeWw, sizeWh);
            w.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            listWall.add(w);
            gameContext.getChildren().add(w);
        }
    }

    private void paving() {
        Rectangle d;

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                d = new Rectangle(i * sizeWw, j * sizeWh, sizeWw, sizeWh);
                d.setFill(new ImagePattern(new Image("data/follow/slab1.png")));
                gameContext.getChildren().add(d);
            }
        }
    }

    private void build(int[][] map) {
        Rectangle w;

        for (int i = 0; i < x - 2; i++) {
            for (int j = 0; j < y - 2; j++) {
                if (map[j][i] == 1) {
                    w = new Rectangle((i + 1) * sizeWw, (j + 1) * sizeWh, sizeWw, sizeWh);
                    w.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
                    listWall.add(w);
                    gameContext.getChildren().add(w);
                }
            }
        }
    }

    private void getRubyLvl1() {
        int[][] map;

        EventHandler<ActionEvent> eventwin = e -> {
            win();
        };

        map = generateLabyrinthLevel1.generateLabyrinth(this.gameContext, this.listEI, this.listWall, this.sizeWw, this.sizeWh, eventwin, stats, this);
        build(map);
    }

    private void getRubyLvl2() {
        int[][] map;

        EventHandler<ActionEvent> eventwin = e -> {
            win();
        };

        map = generateLabyrinthLevel2.generateLabyrinth(this.gameContext, this.listEI, this.listWall, this.sizeWw, this.sizeWh, eventwin, stats, this);
        build(map);
    }

    private void getRubyLvl3() {
        int[][] map;

        EventHandler<ActionEvent> eventwin = e -> {
            win();
        };

        map = generateLabyrinthLevel3.generateLabyrinth(this.gameContext, this.listEI, this.listWall, this.sizeWw, this.sizeWh, eventwin, stats, this);
        build(map);
    }

    private class CustomInputEventHandlerKeyboard implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent key) {

            if (key.getCode().isArrowKey()){
                next();
                launch();
            }
        }
    }
}
