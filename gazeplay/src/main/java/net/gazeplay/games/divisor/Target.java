package net.gazeplay.games.divisor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.Position;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Random;
import javafx.scene.Parent;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author vincent
 */
@Slf4j
class Target extends Parent {
    private TranslateTransition currentTranslation;
    private final Stats stats;
    private final int difficulty;
    private final int level;
    private Position pos;
    private double radius;
    private final EventHandler<Event> enterEvent;
    private final GameContext gameContext;
    private final Divisor gameInstance;
    private final long startTime;
    private final Dimension2D dimension;
    private final boolean lapin;
    private ImageLibrary imgLib;
    private Image explosion;
    private Rectangle rectangle;

    public Target(GameContext gameContext, Stats stats, ImageLibrary imgLib, int level, long start,
            Divisor gameInstance, boolean lapin) {
        this.level = level;
        this.difficulty = 3;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.stats = stats;
        this.startTime = start;
        this.lapin = lapin;
        this.imgLib = imgLib;
        this.dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.pos = this.gameContext.getRandomPositionGenerator().newRandomPosition(100);
        this.radius = 200 / (level + 1);

        this.rectangle = new Rectangle(pos.getX(), pos.getY(), 150, 150);
        this.rectangle.setFill(new ImagePattern(this.imgLib.pickRandomImage(), 0, 0, 1, 1, true));
        this.getChildren().add(rectangle);

        try {
            if (lapin) {
                this.explosion = new Image("data/divisor/images/coeur.png");
            } else {
                this.explosion = new Image("data/divisor/images/explosion.png");
            }
        } catch (Exception e) {
            log.info("Fichier non trouvé " + e.getMessage());
        }

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                enter();
            }
        };

        if (level != 0) {
            Timeline waitbeforestart = new Timeline();

            waitbeforestart.getKeyFrames().add(new KeyFrame(Duration.seconds(0.6)));
            waitbeforestart.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    addEvent();
                }
            });
            waitbeforestart.play();
        } else {
            addEvent();
        }

        move();

        this.stats.notifyNewRoundReady();
    }

    private void move() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(20), new EventHandler<ActionEvent>() {
            int dx = randomDirection();
            int dy = randomDirection();

            double height = dimension.getHeight();
            double width = dimension.getWidth();

            @Override
            public void handle(ActionEvent t) {
                double newCenterX = Target.this.pos.getX() + dx;
                double newCenterY = Target.this.pos.getY() + dy;

                Position newPos = new Position(newCenterX, newCenterY);
                Target.this.pos = newPos;
                Target.this.rectangle.setX(newCenterX);
                Target.this.rectangle.setY(newCenterY);

                if (newCenterX <= (Target.this.radius) || newCenterX >= (width - Target.this.radius)) {
                    dx = -dx;
                }

                if (newCenterY <= (Target.this.radius) || newCenterY >= (height - Target.this.radius)) {
                    dy = -dy;
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void enter() {
        stats.incNbGoals();
        if (currentTranslation != null) {
            currentTranslation.stop();
        }

        this.removeEventFilter(MouseEvent.ANY, enterEvent);
        this.removeEventFilter(GazeEvent.ANY, enterEvent);

        double x = this.pos.getX();
        double y = this.pos.getY();

        explodeAnimation(x, y);

        gameContext.getChildren().remove(this);

        if (level < difficulty) {
            createChildren(x, y);
        }
    }

    private void explodeAnimation(double x, double y) {
        Circle c = new Circle();
        c.setCenterX(x);
        c.setCenterY(y);
        c.setRadius((int) 180 / (level + 1));
        c.setFill(new ImagePattern(explosion, 0, 0, 1, 1, true));
        this.gameContext.getChildren().add(c);

        Timeline timer = new Timeline(new KeyFrame(Duration.millis(300)));
        timer.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                gameContext.getChildren().remove(c);
                if (((!lapin) && (gameContext.getChildren().isEmpty()))
                        || ((lapin) && (gameContext.getChildren().size() <= 1))) {
                    long totalTime = (System.currentTimeMillis() - startTime) / 1000;
                    Label l = new Label("Temps : " + Long.toString(totalTime) + "s");
                    l.setTextFill(Color.WHITE);
                    l.setFont(Font.font(50));
                    l.setLineSpacing(10);
                    l.setLayoutX(15);
                    l.setLayoutY(14);
                    gameContext.getChildren().add(l);
                    gameContext.playWinTransition(30, new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent actionEvent) {
                            gameInstance.restart();
                        }
                    });
                }
            }
        });
        timer.play();
    }

    private void createChildren(double x, double y) {
        for (int i = 0; i < 2; i++) {
            Target target = new Target(gameContext, stats, this.imgLib, level + 1, startTime, gameInstance, lapin);

            if (y + target.radius > (int) dimension.getHeight()) {
                y = (int) dimension.getHeight() - (int) target.radius * 2;
            }

            target.setPos(new Position(x, y));
            gameContext.getChildren().add(target);

        }
    }

    private int randomDirection() {
        Random r = new Random();
        int x = r.nextInt(3) + 4;
        if (r.nextInt(2) >= 1) {
            x = -x;
        }
        return x;
    }

    private void addEvent() {
        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        gameContext.getGazeDeviceManager().addEventFilter(this);
    }

    private void setPos(Position pos) {
        this.pos = pos;
    }
}
