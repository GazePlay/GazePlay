package net.gazeplay.games.biboules;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Random;

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

    private final Stats stats;

    private final Image biboule;
    private final Image flash;

    private final Point[] endPoints;

    private final EventHandler<Event> enterEvent;

    
    //done
    public Biboule(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        biboule = new Image("data/biboule/images/Biboules.png");
        flash = new Image("data/biboule/images/Flash.png");
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("dimension2D = {}", dimension2D);
        centerX = 8.7 * dimension2D.getWidth() / 29.7;
        centerY = 11 * dimension2D.getHeight() / 21;

        Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.setFill(new ImagePattern(new Image("data/biboule/images/Backgroung.jpg")));

        gameContext.getChildren().add(imageRectangle);
        gameContext.getChildren().add(this);

        Point[] points = new Point[10];
        points[0] = new Point(0, 0);

        points[1] = new Point(0, imageRectangle.getHeight());
        points[2] = new Point(imageRectangle.getWidth() / 2, imageRectangle.getHeight());

        points[3] = new Point(imageRectangle.getWidth(), 0);
        points[4] = new Point(imageRectangle.getWidth(), imageRectangle.getHeight() / 2);

        points[5] = new Point(0, imageRectangle.getHeight() / 2);
        points[6] = new Point(imageRectangle.getWidth() / 2, 0);

        points[7] = new Point(0, imageRectangle.getHeight());
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

    //done
    @Override
    public void launch() {
        for (int i = 0; i < 10; i++) {
            newCircle();
        }
        stats.start();

    }

    //done
    @Override
    public void dispose() {

    }

    public void explose(Target sp) {
 
        newCircle();
    }
    
    private Transition restartTransition (Target t) {
    	FadeTransition ft = new FadeTransition(Duration.millis(1),t);
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
    	t.t.stop();
        t.removeEventFilter(MouseEvent.ANY, enterEvent);
        t.removeEventFilter(GazeEvent.ANY, enterEvent);
    	t.getChildren().get(1).setOpacity(1);
        FadeTransition ft = new FadeTransition(Duration.millis(500),t);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.play();
        ft.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
            	restart(t);
            }
        });
        
       

    }


    private void newCircle() {

        Target sp = buildCircle();
        sp.toBack();

        this.getChildren().add(sp);
        this.gameContext.resetBordersToFront();

        gameContext.getGazeDeviceManager().addEventFilter(sp);

        sp.addEventFilter(MouseEvent.ANY, enterEvent);
        sp.addEventHandler(GazeEvent.ANY, enterEvent);
        sp.setLayoutX(centerX);
        sp.setLayoutY(centerY);

        moveCircle(sp);
    }

    private Target buildCircle() {

        Target sp = new Target();
        ImageView C = new ImageView(biboule);
        double radius = minRadius;
        C.setFitHeight(radius);
        C.setFitWidth(radius * 5 / 4);

        ImageView C2 = new ImageView(flash);

        C2.setFitHeight(radius);
        C2.setFitWidth(radius * 5 / 4);

        sp.getChildren().addAll(C,C2);
        sp.getChildren().get(1).setOpacity(0);
        
        return sp;
    }

    private void moveCircle(Target sp) {

        double timelength = ((maxTimeLength - minTimeLength) * Math.random() + minTimeLength) * 1000;

        TranslateTransition tt1 = new TranslateTransition(new Duration(timelength), sp);
        double min = Math.ceil(0);
        double max = Math.floor(7);
        Point randomPoint = endPoints[(int) (Math.floor(Math.random() * (max - min + 1)) + min)];
        tt1.setToY(-centerY + randomPoint.y);
        tt1.setToX(-centerX + randomPoint.x);
        sp.destination = randomPoint;

        ScaleTransition st = new ScaleTransition(new Duration(timelength), sp);
        st.setByX(5);
        st.setByY(5);
        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(tt1, st);
        sp.t=pt;
        
        pt.play();     

        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                sp.setLayoutX(centerX);
                sp.setLayoutY(centerY);
            	restart(sp);
            }
        });

    }
    
    public void restart( Target sp) {
       Transition pt = restartTransition(sp);
        sp.addEventFilter(MouseEvent.ANY, enterEvent);
        sp.addEventHandler(GazeEvent.ANY, enterEvent);
        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                sp.getChildren().get(1).setOpacity(0);
                moveCircle(sp);
            }
        });
        pt.play();
        

    }
}
