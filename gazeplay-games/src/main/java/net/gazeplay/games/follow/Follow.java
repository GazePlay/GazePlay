package net.gazeplay.games.follow;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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

import java.util.ArrayList;

@Slf4j
public class Follow implements GameLifeCycle {

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    @Getter
    @Setter
    private FollowGameVariant variant;

    private final boolean inReplayMode;

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
    private final ArrayList<EventItem> listEI;

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

    Follow(final IGameContext gameContext, final Stats stats, final FollowGameVariant variant, final boolean inReplayMode) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;
        this.inReplayMode = inReplayMode;

        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        listWall = new ArrayList<>();

        listEI = new ArrayList<>();
    }

    @Override
    public void launch() {
        gameContext.setOffFixationLengthControl();

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

        if (variant.equals(FollowGameVariant.FKEY)) {
            fKEY();
        } else if (variant.equals(FollowGameVariant.FCOIN)) {
            fCOIN();
        } else if (variant.equals(FollowGameVariant.FKEYEASY)) {
            fKEYEASY();
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

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose() {
        stats.stop();
        listEI.clear();
        listWall.clear();
    }

    private void followthegaze() {
        PauseTransition next = new PauseTransition(Duration.millis(5));
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
        stats.stop();

        gameContext.updateScore(stats, this);

        gameContext.playWinTransition(500, actionEvent -> {

            canmove = false;

            dispose();

            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            gameContext.showRoundStats(stats, this);
        });
    }

    private void checkEI() {
        ArrayList<EventItem> remove = new ArrayList<>();
        for (EventItem eI : listEI) {
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
        EventItem coin;

        for (int i = 0; i < x - 2; i++) {
            for (int j = 0; j < y - 2; j++) {
                if (map[j][i] == 1) {
                    w = new Rectangle((i + 1) * sizeWw, (j + 1) * sizeWh, sizeWw, sizeWh);
                    w.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
                    listWall.add(w);
                    gameContext.getChildren().add(w);
                } else if (map[j][i] == 2) {
                    scoretoreach++;
                    coin = new EventItem((i + 1) * sizeWw, (j + 1) * sizeWh, sizeWw, sizeWh, new ImagePattern(new Image("data/follow/coin.png")), e -> {
                        if (!inReplayMode) {
                            stats.incrementNumberOfGoalsReached();
                        }
                        score++;
                        multigoals(); /*Maybe add a song*/
                    }, true);
                    listEI.add(coin);
                    gameContext.getChildren().add(coin);
                }
            }
        }
    }

    private void fKEYEASY() {
        int[][] map = new int[][]
            {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
            };

        build(map);

        {
            javafx.event.EventHandler<ActionEvent> eventwin = e -> {
                if (!inReplayMode) {
                    stats.incrementNumberOfGoalsReached();
                }
                win();
            };
            EventItem ruby = new EventItem(2 * sizeWw, 11 * sizeWh, 3 * sizeWw, 3 * sizeWh, new ImagePattern(new Image("data/follow/ruby1RS.png")), eventwin, true);
            listEI.add(ruby);
            gameContext.getChildren().add(ruby);
        }

        {
            Rectangle grille = new Rectangle(6 * sizeWw, 11 * sizeWh, sizeWw, 3 * sizeWh);
            grille.setFill(new ImagePattern(new Image("data/follow/jailbar1.png")));
            listWall.add(grille);
            gameContext.getChildren().add(grille);

            javafx.event.EventHandler<ActionEvent> eventkey = e -> {
                listWall.remove(grille);
                gameContext.getChildren().remove(grille);
                //Maybe add a song
                if (!inReplayMode) {
                    stats.incrementNumberOfGoalsReached();
                }
            };
            EventItem key = new EventItem(28 * sizeWw, 3 * sizeWh, sizeWw, sizeWh, new ImagePattern(new Image("data/follow/key.png")), eventkey, true);
            listEI.add(key);
            gameContext.getChildren().add(key);
        }
    }

    private void fKEY() {
        int[][] map = new int[][]
            {
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}
            };

        build(map);

        {
            Rectangle doorRED = new Rectangle(9 * sizeWw, 3 * sizeWh, sizeWw, 2 * sizeWh);
            doorRED.setFill(new ImagePattern(new Image("data/follow/door1rouge.png")));
            listWall.add(doorRED);
            gameContext.getChildren().add(doorRED);

            javafx.event.EventHandler<ActionEvent> eventkeyred = e -> {
                listWall.remove(doorRED);
                gameContext.getChildren().remove(doorRED);
                //Maybe add a song
                if (!inReplayMode) {
                    stats.incrementNumberOfGoalsReached();
                }
            };
            EventItem keyRED = new EventItem(3 * sizeWw, 7 * sizeWh, sizeWw, sizeWh, new ImagePattern(new Image("data/follow/keyred.png")), eventkeyred, true);
            listEI.add(keyRED);
            gameContext.getChildren().add(keyRED);
        }

        {
            Rectangle doorGREEN = new Rectangle(6 * sizeWw, (y - 3) * sizeWh, sizeWw, 2 * sizeWh);
            doorGREEN.setFill(new ImagePattern(new Image("data/follow/door1verte.png")));
            listWall.add(doorGREEN);
            gameContext.getChildren().add(doorGREEN);

            Rectangle doorGREEN2 = new Rectangle((x - 9) * sizeWw, (y - 7) * sizeWh, sizeWw, 2 * sizeWh);
            doorGREEN2.setFill(new ImagePattern(new Image("data/follow/door1verte.png")));
            listWall.add(doorGREEN2);
            gameContext.getChildren().add(doorGREEN2);

            javafx.event.EventHandler<ActionEvent> eventkeygreen = e -> {
                listWall.remove(doorGREEN);
                gameContext.getChildren().remove(doorGREEN);
                listWall.remove(doorGREEN2);
                gameContext.getChildren().remove(doorGREEN2);
                //Maybe add a sound
                if (!inReplayMode) {
                    stats.incrementNumberOfGoalsReached();
                }
            };
            EventItem keyGREEN = new EventItem((x - 3) * sizeWw, 4 * sizeWh, sizeWw, sizeWh, new ImagePattern(new Image("data/follow/keygreen.png")), eventkeygreen, true);
            listEI.add(keyGREEN);
            gameContext.getChildren().add(keyGREEN);
        }

        javafx.event.EventHandler<ActionEvent> eventwin = e -> {
            if (!inReplayMode) {
                stats.incrementNumberOfGoalsReached();
            }
            win();
        };
        EventItem ruby = new EventItem(2 * sizeWw, 2 * sizeWh, 2 * sizeWw, 2 * sizeWh, new ImagePattern(new Image("data/follow/ruby1RS.png")), eventwin, true);
        listEI.add(ruby);
        gameContext.getChildren().add(ruby);

        javafx.event.EventHandler<ActionEvent> eventtrap = e -> {
            Rectangle wallTrap = new Rectangle((x - 9) * sizeWw, sizeWh, sizeWw, 3 * sizeWh);
            wallTrap.setFill(new ImagePattern(new Image("data/follow/jailbar1.png")));
            gameContext.getChildren().add(wallTrap);
            listWall.add(wallTrap);
            //Maybe add a sound
        };
        EventItem trap = new EventItem((x - 8) * sizeWw, 5 * sizeWh, 3 * sizeWw, sizeWh, new ImagePattern(new Image("data/follow/nothing.png")), eventtrap, true);
        listEI.add(trap);
        gameContext.getChildren().add(trap);
    }

    private void fCOIN() {

        int[][] map = new int[][]
            {
                {1, 2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1},
                {2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 2, 1},
                {2, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 2, 2, 2, 1, 2, 1},
                {2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1},
                {2, 2, 2, 2, 2, 2, 1, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 2, 2, 1, 1, 1, 2, 2, 1, 2, 2, 1, 2, 2},
                {1, 1, 1, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 2, 1, 1, 1, 2, 2, 1, 2, 2},
                {1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 1, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2},
                {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 2},
                {2, 2, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 0, 0, 2, 1, 1, 2, 2, 2, 1, 2, 2, 2, 1, 1, 1, 2},
                {2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2},
                {2, 1, 1, 2, 1, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 1, 2, 2, 1, 1, 1, 2, 2, 1, 2},
                {2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 2},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2},
                {1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1}
            };

        build(map);
    }
}
