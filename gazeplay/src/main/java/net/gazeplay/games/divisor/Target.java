package net.gazeplay.games.divisor;

import javafx.animation.FadeTransition;
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
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.Position;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Random;

/**
 *
 * @author vincent
 */
@Slf4j
class Target extends Portrait {

    private TranslateTransition currentTranslation;
    private final RandomPositionGenerator randomPosGenerator;
    private final Stats stats;
    private final int difficulty;
    private final int level;
    private final EventHandler<Event> enterEvent;
    private final GameContext gameContext;
    private final Divisor gameInstance;
    private final ImageLibrary imageLibrary;
    private final long startTime;
    private final Dimension2D dimension;
    private final boolean lapin;
    private Image explosion;

    public Target(GameContext gameContext, RandomPositionGenerator randomPositionGenerator, Stats stats,
            ImageLibrary imageLibrary, int level, long start, Divisor gameInstance, boolean lapin) {
        super((int) 180 / (level + 1), randomPositionGenerator, imageLibrary);
        this.level = level;
        this.difficulty = 3;
        this.randomPosGenerator = randomPositionGenerator;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.stats = stats;
        this.imageLibrary = imageLibrary;
        this.startTime = start;
        this.lapin = lapin;
        this.dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();

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
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    double x = ((MouseEvent) e).getX();
                    double y = ((MouseEvent) e).getY();
                    enter((int) x, (int) y);
                } else if (e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    double x = ((GazeEvent) e).getX();
                    double y = ((GazeEvent) e).getY();
                    enter((int) x, (int) y);
                }
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

        if (this.getPosition().getY() + this.getRadius() > (int) dimension.getHeight()) {
            this.setPosition(new Position(this.getPosition().getX(), this.getPosition().getY() - this.getRadius() * 2));
        }

        move();

        stats.start();
    }

    private void move() {
        Target bubble = this;

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(20), new EventHandler<ActionEvent>() {
            int dx = randomDirection();
            int dy = randomDirection();

            double height = dimension.getHeight();
            double width = dimension.getWidth();

            @Override
            public void handle(ActionEvent t) {
                Position newPos = new Position(bubble.getPosition().getX() + dx, bubble.getPosition().getY() + dy);
                bubble.setPosition(newPos);

                if (newPos.getX() <= (bubble.getRadius()) || newPos.getX() >= (width - bubble.getRadius())) {
                    dx = -dx;
                }

                if (newPos.getY() <= (bubble.getRadius()) || newPos.getY() >= (height - bubble.getRadius())) {
                    dy = -dy;
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void enter(int x, int y) {
        if (currentTranslation != null) {
            currentTranslation.stop();
        }

        explose(x, y);
    }

    public void explose(int x, int y) {
        stats.incNbGoals();

        this.removeEventFilter(MouseEvent.ANY, enterEvent);
        this.removeEventFilter(GazeEvent.ANY, enterEvent);

        Circle c = new Circle();
        c.setCenterX(x);
        c.setCenterY(y);
        c.setRadius((int) 180 / (level + 1));
        c.setFill(new ImagePattern(explosion, 0, 0, 1, 1, true));
        this.gameContext.getChildren().add(c);

        FadeTransition ft = new FadeTransition(Duration.millis(300), this);
        ft.setFromValue(1);
        ft.setToValue(0);

        gameContext.getChildren().remove(this);

        ft.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                gameContext.getChildren().remove(c);
                if (level < difficulty) {
                    createChildren(x, y);
                } else if (((!lapin) && (gameContext.getChildren().isEmpty()))
                        || ((lapin) && (gameContext.getChildren().size() <= 1))) {
                    long totalTime = (System.currentTimeMillis() - startTime) / 1000;
                    Label l = new Label("Temps : " + Long.toString(totalTime) + "s");
                    l.setTextFill(Color.WHITE);
                    l.setFont(Font.font(50));
                    l.setLineSpacing(10);
                    l.setLayoutX(15);
                    l.setLayoutY(14);
                    gameContext.getChildren().add(l);
                    gameContext.playWinTransition(50, new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent actionEvent) {
                            gameInstance.dispose();
                            gameContext.clear();
                            gameInstance.launch();
                        }
                    });
                }
            }
        });
        ft.play();
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

    private void createChildren(int x, int y) {
        for (int i = 0; i < 2; i++) {
            Target target = new Target(gameContext, randomPosGenerator, stats, imageLibrary, level + 1, startTime,
                    gameInstance, lapin);

            if (y + target.getRadius() > (int) dimension.getHeight()) {
                y = (int) dimension.getHeight() - (int) target.getRadius() * 2;
            }

            target.setPosition(new Position(x, y));
            gameContext.getChildren().add(target);

        }
    }
}
