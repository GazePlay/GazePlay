package net.gazeplay.games.divisor;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Position;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author vincent
 */
@Slf4j
class Target extends Parent {
    private TranslateTransition currentTranslation;
    private final Stats stats;
    private final int difficulty;
    private final int level;
    private Position pos;
    private final double radius;
    private final EventHandler<Event> enterEvent;
    private final IGameContext gameContext;
    private final Divisor gameInstance;
    private final long startTime;
    private final Dimension2D dimension;
    private final boolean lapin;
    private final ImageLibrary imgLib;
    private final Circle cercle;
    private Timeline timeline;

    public Target(final IGameContext gameContext, final Stats stats, final ImageLibrary imgLib, final int level, final long start,
                  final Divisor gameInstance, final Position pos, final boolean lapin) {
        this.level = level;
        this.difficulty = 3;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.stats = stats;
        this.startTime = start;
        this.lapin = lapin;
        this.imgLib = imgLib;
        this.pos = pos;
        this.dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.radius = 200d / (level + 1);
        this.timeline = new Timeline();

        this.cercle = new Circle(pos.getX(), pos.getY(), this.radius);
        this.cercle.setFill(new ImagePattern(this.imgLib.pickRandomImage(), 0, 0, 1, 1, true));
        this.getChildren().add(cercle);

        enterEvent = e -> enter();

        if (level != 0) {
            final Timeline waitbeforestart = new Timeline();

            waitbeforestart.getKeyFrames().add(new KeyFrame(Duration.seconds(0.6)));
            waitbeforestart.setOnFinished(actionEvent -> addEvent());
            waitbeforestart.play();
        } else {
            addEvent();
        }

        move();

        this.stats.notifyNewRoundReady();

        gameContext.getConfiguration().getAnimationSpeedRatioProperty().addListener((o) -> {
            timeline.stop();
            move();
        });
    }

    private void move() {
        timeline = new Timeline(new KeyFrame(Duration.millis(10),
            new EventHandler<>() {
                int dx = randomDirection();
                int dy = randomDirection();

                final double height = dimension.getHeight();
                final double width = dimension.getWidth();

                @Override
                public void handle(final ActionEvent t) {
                    final double newCenterX = Target.this.pos.getX() + dx;
                    final double newCenterY = Target.this.pos.getY() + dy;

                    Target.this.pos = new Position(newCenterX, newCenterY);

                    Target.this.cercle.setCenterX(newCenterX);
                    Target.this.cercle.setCenterY(newCenterY);

                    if (newCenterX <= (Target.this.radius) || newCenterX >= (width - Target.this.radius)) {
                        dx = -dx;
                    }

                    if (newCenterY <= (Target.this.radius) || newCenterY >= (height - Target.this.radius)) {
                        dy = -dy;
                    }
                }
            }));
        timeline.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
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

        final double x = this.pos.getX();
        final double y = this.pos.getY();

        explodeAnimation(x, y);

        gameContext.getChildren().remove(this);

        if (level < difficulty) {
            createChildren(x, y);
        }
    }

    private void explodeAnimation(final double x, final double y) {
        final double particleRadius = 2;
        final ArrayList<Circle> particles = new ArrayList<>();
        final Timeline timelineParticle = new Timeline();
        for (int i = 0; i < 30; i++) {
            final Circle particle = new Circle(x, y, particleRadius);
            particle.setFill(Color.color(Math.random(), Math.random(), Math.random()));

            particles.add(particle);
            this.gameContext.getChildren().add(particle);
            final Position particleDestination = randomPosWithRange(this.pos, this.radius * 1.5, particleRadius);
            timelineParticle.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(particles.get(i).centerXProperty(),
                    particleDestination.getX(), Interpolator.EASE_OUT)));
            timelineParticle.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(particles.get(i).centerYProperty(),
                    particleDestination.getY(), Interpolator.EASE_OUT)));
            timelineParticle.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(particles.get(i).opacityProperty(), 0.0)));
        }

        timelineParticle.setOnFinished(actionEvent -> {
            Target.this.gameContext.getChildren().removeAll(particles);
            if (((!lapin) && (gameContext.getChildren().isEmpty()))
                || ((lapin) && (gameContext.getChildren().size() <= 1))) {
                final long totalTime = (System.currentTimeMillis() - startTime) / 1000;
                final Label l = new Label("Score : " + totalTime + "s");
                final Color color = (gameContext.getConfiguration().isBackgroundWhite()) ? Color.BLACK : Color.WHITE;
                l.setTextFill(color);
                l.setFont(Font.font(50));
                l.setLineSpacing(10);
                l.setLayoutX(15);
                l.setLayoutY(14);
                gameContext.getChildren().add(l);
                gameContext.playWinTransition(30, actionEvent1 -> gameInstance.restart());
            }
        });
        timelineParticle.play();
    }

    private void createChildren(final double x, double y) {
        for (int i = 0; i < 2; i++) {
            final Target target = new Target(gameContext, stats, this.imgLib, level + 1, startTime, gameInstance,
                new Position(x, y), lapin);

            if (y + target.radius > (int) dimension.getHeight()) {
                y = (int) dimension.getHeight() - (int) target.radius * 2;
            }

            gameContext.getChildren().add(target);
        }
    }

    private int randomDirection() {
        final Random r = new Random();
        int x = r.nextInt(3) + 4;
        if (r.nextInt(2) >= 1) {
            x = -x;
        }
        return x;
    }

    private Position randomPosWithRange(final Position start, final double range, final double radius) {
        final Random random = new Random();

        final double minX = (start.getX() - range);
        final double minY = (start.getY() - range);
        final double maxX = (start.getX() + range);
        final double maxY = (start.getY() + range);

        double positionX = random.nextInt((int) (maxX - minX)) + minX;
        double positionY = random.nextInt((int) (maxY - minY)) + minY;

        if (positionX > this.dimension.getWidth()) {
            positionX = this.dimension.getWidth() - radius;
        }
        if (positionY > this.dimension.getHeight()) {
            positionY = this.dimension.getHeight() - radius;
        }

        return new Position((int) positionX, (int) positionY);
    }

    private void addEvent() {
        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        gameContext.getGazeDeviceManager().addEventFilter(this);
    }

    private void setPos(final Position pos) {
        this.pos = pos;
    }
}
