package net.gazeplay.games.cakes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class cakeFactory extends Parent implements GameLifeCycle {

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
    private StackPane sp;

    private String date;
    private Label text;
    private Label textb;
    private int score;

    private Circle c;

    private StackPane hand;
    private ImageView cage;

    private Boolean left;

    private String gameType;

    private final Stats stats;

    private final EventHandler<Event> enterEvent;

    // done
    public cakeFactory(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        score = 0;
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("dimension2D = {}", dimension2D);
        centerX = 8.7 * dimension2D.getWidth() / 29.7;
        centerY = 10 * dimension2D.getHeight() / 21;
        sp = new StackPane();
        c = new Circle(dimension2D.getHeight() / 6);
        c.setCenterY(dimension2D.getHeight() / 2);
        c.setCenterX(dimension2D.getWidth() / 2);
        createStack(sp);
        gameContext.getChildren().add(sp);
        gameContext.getChildren().add(c);
        gameContext.getChildren().add(this);

        stats.notifyNewRoundReady();

        /*EventHandler<Event> handEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

            }
        };*/

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    stats.incNbGoals();
                    stats.notifyNewRoundReady();
                }
            }
        };

    }

    public void createStack(StackPane sp) {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Rectangle f0 = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        Rectangle f1 = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        Rectangle f2 = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        Rectangle f3 = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        Rectangle f4 = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        Rectangle f5 = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        Pane[] p = new Pane[6];
        for (int i = 0; i < 6; i++) {
            p[i] = new Pane();
        }
        f0.setFill(Color.AQUA);
        f1.setFill(Color.RED);
        f2.setFill(Color.GREEN);
        f3.setFill(Color.BLACK);
        f4.setFill(Color.YELLOW);
        f5.setFill(Color.PURPLE);
        p[0].getChildren().add(f0);
        p[1].getChildren().add(f1);
        p[2].getChildren().add(f2);
        p[3].getChildren().add(f3);
        p[4].getChildren().add(f4);
        p[5].getChildren().add(f5);

        double buttonSize = dimension2D.getWidth() / 8;

        for (int i = 0; i < 5; i++) { // HomePage of the game
            Button bt = new Button();
            bt.setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; "
                    + "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; "
                    + "-fx-max-height: " + buttonSize + "px;");
            bt.setLayoutX(i * (1.5 * buttonSize) + 0.5 * buttonSize);
            bt.setText("screen" + i);
            int index = i;
            EventHandler<Event> buttonHandler = new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    p[index + 1].toFront();
                    c.toFront();
                }
            };
            bt.addEventHandler(MouseEvent.MOUSE_PRESSED, buttonHandler);
            p[0].getChildren().add(bt);
        }

        // Other pages
        for (int j = 1; j < 6; j++) {
            for (int i = 0; i < 6; i++) { // HomePage of the game
                Button bt = new Button();
                bt.setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; "
                        + "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; "
                        + "-fx-max-height: " + buttonSize + "px;");
                if (i != 5) {
                    bt.setText("color" + i);
                    bt.setLayoutX(i * (1.5 * buttonSize) + 0.5 * buttonSize);
                    int index = i;
                    Color[] cols = new Color[5];
                    cols[0] = Color.ALICEBLUE;
                    cols[1] = Color.ANTIQUEWHITE;
                    cols[2] = Color.AQUA;
                    cols[3] = Color.AZURE;
                    cols[4] = Color.DARKRED;
                    EventHandler<Event> buttonHandler = new EventHandler<Event>() {
                        @Override
                        public void handle(Event e) {
                            c.setFill(cols[index]);
                        }
                    };
                    bt.addEventHandler(MouseEvent.MOUSE_PRESSED, buttonHandler);
                    p[j].getChildren().add(bt);
                } else {
                    bt.setText("return");
                    bt.setLayoutX(dimension2D.getWidth() - buttonSize);
                    bt.setLayoutY(dimension2D.getHeight() - (1.2 * buttonSize));
                    EventHandler<Event> buttonHandler = new EventHandler<Event>() {
                        @Override
                        public void handle(Event e) {
                            p[0].toFront();
                            c.toFront();
                        }
                    };
                    bt.addEventHandler(MouseEvent.MOUSE_PRESSED, buttonHandler);
                    p[j].getChildren().add(bt);
                }
            }
        }

        for (int i = 5; i >= 0; i--) {
            sp.getChildren().add(p[i]);
        }

    }

    // done
    @Override
    public void launch() {

        this.gameContext.resetBordersToFront();
        /*
         * Label sc = new Label(); Label tc = new Label();
         * 
         * String cst; if (gameType.equals("biboule")) { cst = date + "\n\t" + "Score:" + score; } else {// equals robot
         * tc.setText("Score:"); tc.setTextFill(Color.WHITE); cst = "" + score;
         * 
         * } sc.setText(cst); sc.setTextFill(Color.WHITE); ; Dimension2D dimension2D =
         * gameContext.getGamePanelDimensionProvider().getDimension2D(); ImageView iv1 = new ImageView(new Image("data/"
         * + gameType + "/images/hand.png")); ImageView iv2 = new ImageView(new Image("data/" + gameType +
         * "/images/handShot.png"));
         * 
         * StackPane iv = new StackPane(); double x = dimension2D.getHeight(); iv1.setPreserveRatio(true);
         * iv1.setFitHeight(x); iv2.setPreserveRatio(true); iv2.setFitHeight(x);
         * 
         * iv.getChildren().addAll(iv1, iv2); iv.getChildren().get(1).setOpacity(0); iv.setLayoutY(0); iv.setLayoutX(3 *
         * (dimension2D.getWidth() / 7));
         * 
         * double cst2; if (gameType.equals("biboule")) { cst2 = 2; } else {// equals robot cst2 = 1.7; }
         * iv.setLayoutY(dimension2D.getHeight() / cst2);
         * 
         * this.getChildren().add(iv); hand = (StackPane) this.getChildren().get(this.getChildren().indexOf(iv));
         * hand.toFront();
         * 
         * if (gameType.equals("biboule")) { sc.setFont(Font.font("AR BLANCA", dimension2D.getHeight() / 18));
         * sc.setLayoutX(8.9 * dimension2D.getWidth() / 29.7); sc.setLayoutY(1.8 * dimension2D.getHeight() / 21); } else
         * { sc.setFont(Font.font(dimension2D.getHeight() / 20)); sc.setLineSpacing(10); sc.setLayoutX(16 *
         * dimension2D.getWidth() / 29.7); sc.setLayoutY(15.2 * dimension2D.getHeight() / 21);
         * 
         * tc.setFont(Font.font(dimension2D.getHeight() / 20)); tc.setLineSpacing(10); tc.setLayoutX(15 *
         * dimension2D.getWidth() / 29.7); tc.setLayoutY(14 * dimension2D.getHeight() / 21);
         * 
         * this.getChildren().add(tc); } text = sc; this.getChildren().add(sc);
         * 
         * this.gameContext.resetBordersToFront(); iv.setMouseTransparent(true);
         * 
         * cage.setLayoutX(8.5 * dimension2D.getWidth() / 29.7);
         * 
         * double y; if (gameType.equals("biboule")) { cage.setLayoutY(8.5 * dimension2D.getHeight() / 21); y =
         * dimension2D.getHeight() / 6.5; } else {// equals robot textb = tc; cage.setLayoutY(3.5 *
         * dimension2D.getHeight() / 21); y = dimension2D.getHeight() / 8.5; }
         * 
         * cage.setPreserveRatio(true); cage.setFitHeight(y); cage.toBack(); left = true; this.getChildren().add(cage);
         * 
         * Timeline waitbeforestart = new Timeline(); waitbeforestart.getKeyFrames().add(new
         * KeyFrame(Duration.seconds(1))); waitbeforestart.play(); sta stats.notifyNewRoundReady();
         */

    }

    // done
    @Override
    public void dispose() {
        this.getChildren().clear();
    }

}
