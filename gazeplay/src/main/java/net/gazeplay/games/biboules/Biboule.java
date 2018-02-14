package net.gazeplay.games.biboules;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Created by schwab on 28/08/2016.
 */
@Slf4j
public class Biboule extends Parent implements GameLifeCycle {

    private static final int maxRadius = 70;
    private static final int minRadius = 30;

    private static final int maxTimeLength = 7;
    private static final int minTimeLength = 4;

    private double centerX;
    private double centerY;

    private final GameContext gameContext;

    private Image blue;
    private Image green;
    private Image yellow;
    private Image orange;
    private Image red;
    private Image flash;

    private String date;
    private Label text;
    private int score;

    private StackPane hand;

    private final Stats stats;

    private final Point[] endPoints;

    private final EventHandler<Event> enterEvent;

    // done
    public Biboule(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        LocalDate localDate = LocalDate.now();
        date = DateTimeFormatter.ofPattern("d MMMM uuuu ").format(localDate);
        score = 0;

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("dimension2D = {}", dimension2D);
        centerX = 8.7 * dimension2D.getWidth() / 29.7;
        centerY = 10 * dimension2D.getHeight() / 21;

        Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.setFill(new ImagePattern(new Image("data/biboule/images/Backgroung.jpg")));
        gameContext.getChildren().add(imageRectangle);
        gameContext.getChildren().add(this);

        EventHandler<Event> handEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
                    double x = ((MouseEvent) e).getX();
                    double y = ((MouseEvent) e).getY();
                    hand.setRotate(getAngle(new Point(x, y)));
                }
            }
        };
        imageRectangle.addEventFilter(MouseEvent.ANY, handEvent);

        blue = new Image("data/biboule/images/BlueBiboule.png");
        green = new Image("data/biboule/images/GreenBiboule.png");
        yellow = new Image("data/biboule/images/YellowBiboule.png");
        orange = new Image("data/biboule/images/OrangeBiboule.png");
        red = new Image("data/biboule/images/RedBiboule.png");
        flash = new Image("data/biboule/images/Flash.png");

        Point[] points = new Point[10];
        points[0] = new Point(0, 0);

        points[1] = new Point(0, imageRectangle.getHeight());
        points[2] = new Point(imageRectangle.getWidth() / 2, imageRectangle.getHeight());

        points[3] = new Point(imageRectangle.getWidth(), 0);
        points[4] = new Point(imageRectangle.getWidth(), imageRectangle.getHeight() / 2);

        points[5] = new Point(0, imageRectangle.getHeight() / 2);
        points[6] = new Point(imageRectangle.getWidth() / 2, 0);

        points[7] = new Point(imageRectangle.getWidth(), imageRectangle.getHeight());
        this.endPoints = points;

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    enter((Target) e.getTarget());
                    stats.incNbGoals();
                    stats.start();
                }
            }
        };

    }

    public float getAngle(Point target) {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        float angle = (float) Math
                .toDegrees(Math.atan2(target.x - (dimension2D.getWidth() / 2), -target.y + (dimension2D.getHeight())));

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    // done
    @Override
    public void launch() {

        Label sc = new Label();

        sc.setText(date + "\n\t" + "Score:" + score);
        sc.setTextFill(Color.WHITE);
        ;
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        ImageView iv1 = new ImageView(new Image("data/biboule/images/hand.png"));
        ImageView iv2 = new ImageView(new Image("data/biboule/images/handMagic.png"));
        StackPane iv = new StackPane();
        double x = dimension2D.getHeight();
        iv1.setPreserveRatio(true);
        iv1.setFitHeight(x);
        iv2.setPreserveRatio(true);
        iv2.setFitHeight(x);
        iv.getChildren().addAll(iv1, iv2);
        iv.getChildren().get(1).setOpacity(0);
        iv.setLayoutY(0);
        iv.setLayoutX(3 * (dimension2D.getWidth() / 7));
        iv.setLayoutY(dimension2D.getHeight() / 2);
        this.getChildren().add(iv);
        hand = (StackPane) this.getChildren().get(0);
        hand.toFront();

        sc.setFont(Font.font("AR BLANCA", dimension2D.getHeight() / 15));
        sc.setLayoutX(8.9 * dimension2D.getWidth() / 29.7);
        sc.setLayoutY(1.8 * dimension2D.getHeight() / 21);
        text = sc;
        this.getChildren().add(sc);

        this.gameContext.resetBordersToFront();
        iv.setMouseTransparent(true);

        for (int i = 0; i < 5; i++) {
            newCircle();
        }
        stats.start();

    }

    // done
    @Override
    public void dispose() {

    }

    private Transition restartTransition(Target t) {

        FadeTransition ft = new FadeTransition(Duration.millis(1), t);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt1 = new TranslateTransition(Duration.millis(1), t);
        tt1.setToY(0);
        tt1.setToX(0);

        ScaleTransition st = new ScaleTransition(Duration.millis(1), t);
        st.setToX(1);
        st.setToY(1);

        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(ft, tt1, st);

        return pt;
    }

    private void enter(Target t) {
        t.removeEventFilter(MouseEvent.ANY, enterEvent);
        t.removeEventFilter(GazeEvent.ANY, enterEvent);
        t.t.stop();

        text.setText(date + "\n\t" + "Score:" + score++);

        t.getChildren().get(0).setOpacity(1);
        hand.getChildren().get(1).setOpacity(1);
        FadeTransition ft = new FadeTransition(Duration.millis(500), t);
        ft.setFromValue(1);
        ft.setToValue(0);
        FadeTransition ft2 = new FadeTransition(Duration.millis(500), hand.getChildren().get(1));
        ft2.setFromValue(1);
        ft2.setToValue(0);
        ParallelTransition st = new ParallelTransition();
        st.getChildren().addAll(ft, ft2);
        st.play();

        st.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                getChildren().remove((getChildren().indexOf(t)));
                newCircle();
            }
        });

    }

    private void newCircle() {

        Target sp = buildCircle();
        sp.toBack();
        this.getChildren().add(sp);
        gameContext.getGazeDeviceManager().addEventFilter(sp);

        sp.addEventFilter(MouseEvent.ANY, enterEvent);
        sp.addEventHandler(GazeEvent.ANY, enterEvent);
        sp.setLayoutX(centerX);
        sp.setLayoutY(centerY);

        moveCircle(sp);
    }

    private void resize(ImageView i) {
        double d = minRadius;
        i.setFitHeight(d);
        i.setFitWidth(d * 5 / 4);
    }

    private Target buildCircle() {

        Target sp = new Target();

        ImageView b1 = new ImageView(blue);
        ImageView b2 = new ImageView(green);
        ImageView b3 = new ImageView(yellow);
        ImageView b4 = new ImageView(orange);
        ImageView b5 = new ImageView(red);

        ImageView f = new ImageView(flash);

        resize(b1);
        resize(b2);
        resize(b3);
        resize(b4);
        resize(b5);

        resize(f);

        sp.getChildren().addAll(f, b1, b2, b3, b4, b5);
        sp.getChildren().get(0).setOpacity(0);
        sp.getChildren().get(5).setOpacity(0);
        sp.getChildren().get(2).setOpacity(0);
        sp.getChildren().get(3).setOpacity(0);
        sp.getChildren().get(4).setOpacity(0);

        return sp;
    }

    private void moveCircle(Target sp) {

        double timelength = ((maxTimeLength - minTimeLength) * Math.random() + minTimeLength) * 1000;

        TranslateTransition tt1 = new TranslateTransition(new Duration(timelength), sp);
        double min = Math.ceil(0);
        double max = Math.floor(7);
        int r = (int) (Math.floor(Math.random() * (max - min + 1)) + min);
        Point randomPoint = endPoints[r];
        tt1.setToY(-centerY + randomPoint.y);
        tt1.setToX(-centerX + randomPoint.x);
        sp.destination = randomPoint;

        if (r == 2) {
            this.getChildren().get(this.getChildren().indexOf(sp)).toFront();
        } else {
            this.getChildren().get(this.getChildren().indexOf(sp)).toBack();
            text.toBack();
        }

        ScaleTransition st = new ScaleTransition(new Duration(timelength), sp);
        st.setByX(10);
        st.setByY(10);
        ParallelTransition pt = new ParallelTransition();

        FadeTransition btog = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(2));
        FadeTransition gtoy = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(3));
        FadeTransition ytoo = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(4));
        FadeTransition otor = new FadeTransition(new Duration(timelength / 4), sp.getChildren().get(5));

        btog.setFromValue(0);
        gtoy.setFromValue(0);
        ytoo.setFromValue(0);
        otor.setFromValue(0);

        btog.setToValue(1);
        gtoy.setToValue(1);
        ytoo.setToValue(1);
        otor.setToValue(1);

        SequentialTransition seqt = new SequentialTransition(btog, gtoy, ytoo, otor);

        pt.getChildren().addAll(seqt, tt1, st);

        sp.t = pt;

        pt.play();

        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                getChildren().remove((getChildren().indexOf(sp)));
                newCircle();
            }
        });

    }

    public void restart(Target sp) {
        Transition pt = restartTransition(sp);
        sp.addEventFilter(MouseEvent.ANY, enterEvent);
        sp.addEventHandler(GazeEvent.ANY, enterEvent);
        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                sp.getChildren().get(2).setOpacity(0);
                sp.getChildren().get(3).setOpacity(0);
                sp.getChildren().get(4).setOpacity(0);
                sp.getChildren().get(5).setOpacity(0);

                sp.getChildren().get(0).setOpacity(0);
                moveCircle(sp);
            }
        });
        pt.play();

    }
}
