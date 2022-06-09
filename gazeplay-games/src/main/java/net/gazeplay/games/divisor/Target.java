package net.gazeplay.games.divisor;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Position;

import java.util.ArrayList;

/**
 * @author vincent
 */
@Slf4j
class Target extends Parent {

    private final Stats stats;
    private final int difficulty;
    private final int level;
    private Position pos;
    private final double radius;
    private final EventHandler<Event> enterEvent;
    private final IGameContext gameContext;
    private final Divisor gameInstance;
    private final Dimension2D dimension;
    private final boolean isRabbit;
    private final ReplayablePseudoRandom randomGenerator;
    private final long startTime;

    @Getter
    private final ImageLibrary imgLib;
    private final Circle circle;
    private Timeline timeline;

    private final ReplayablePseudoRandom randomFragmentsGenerator = new ReplayablePseudoRandom();

    public Target(final IGameContext gameContext, final Stats stats, final ImageLibrary imgLib, final int level, final long start,
                  final Divisor gameInstance, final Position pos, final boolean isRabbit, ReplayablePseudoRandom random) {
        this.level = level;
        this.difficulty = 3;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.stats = stats;
        this.startTime = start;
        this.isRabbit = isRabbit;
        this.imgLib = imgLib;
        this.pos = pos;
        this.dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.radius = Math.min((dimension.getWidth() / 6) / (level + 1), (dimension.getHeight() / 6) / (level + 1));
        this.timeline = new Timeline();
        this.randomGenerator = random;

        this.circle = new Circle(pos.getX(), pos.getY(), this.radius);
        this.circle.setFill(new ImagePattern(this.imgLib.pickRandomImage(), 0, 0, 1, 1, true));
        this.getChildren().add(circle);

        enterEvent = e -> enter();

        if (level != 0) {
            final Timeline waitBeforeStart = new Timeline();

            waitBeforeStart.getKeyFrames().add(new KeyFrame(Duration.seconds(0.6)));
            waitBeforeStart.setOnFinished(actionEvent -> addEvent());
            waitBeforeStart.play();
        } else {
            addEvent();
        }

        move();

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

                    Target.this.circle.setCenterX(newCenterX);
                    Target.this.circle.setCenterY(newCenterY);

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
        stats.incrementNumberOfGoalsReached();

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
            particle.setFill(Color.color(randomFragmentsGenerator.nextDouble(), randomFragmentsGenerator.nextDouble(), randomFragmentsGenerator.nextDouble()));

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

        });
        timelineParticle.play();

        if (stats.getNbGoalsReached() == stats.getNbGoalsToReach()) {
            final long totalTime = (System.currentTimeMillis() - startTime) / 1000;
            final Label l = new Label("Score : " + totalTime + "s");
            final Color color = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<Color>() {
                @Override
                public Color visitLight() {
                    return Color.BLACK;
                }

                @Override
                public Color visitDark() {
                    return Color.WHITE;
                }
            });
            l.setTextFill(color);
            l.setFont(Font.font(50));
            l.setLineSpacing(10);
            l.setLayoutX(15);
            l.setLayoutY(14);
            gameContext.getChildren().add(l);

            gameContext.updateScore(stats, gameInstance);
            gameContext.playWinTransition(0, actionEvent1 -> gameInstance.restart());
        }
    }

    private void createChildren(final double x, double y) {
        double tempX = x;
        double tempY = y;
        for (int i = 0; i < 2; i++) {
            final Target target = new Target(gameContext, stats, this.imgLib, level + 1, startTime, gameInstance,
                new Position(tempX, tempY), isRabbit, randomGenerator);

            if (tempY + target.radius > (int) dimension.getHeight()) {
                tempY = (int) dimension.getHeight() - (int) target.radius * 2;
            }
            gameContext.getChildren().add(target);
        }
    }

    private int randomDirection() {
        int x = randomGenerator.nextInt(3) + 4;
        if (randomGenerator.nextInt(2) >= 1) {
            x = -x;
        }
        return x;
    }

    private Position randomPosWithRange(final Position start, final double range, final double radius) {
        final double minX = (start.getX() - range);
        final double minY = (start.getY() - range);
        final double maxX = (start.getX() + range);
        final double maxY = (start.getY() + range);

        double positionX = randomFragmentsGenerator.nextInt((int) (maxX - minX)) + minX;
        double positionY = randomFragmentsGenerator.nextInt((int) (maxY - minY)) + minY;

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

}
