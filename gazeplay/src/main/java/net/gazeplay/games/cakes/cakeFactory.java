package net.gazeplay.games.cakes;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class cakeFactory extends Parent implements GameLifeCycle {

    private double centerX;
    private double centerY;

    private final GameContext gameContext;

    private StackPane sp;

    private StackPane[] cake;
    private ImageView part[];
    private final double buttonSize;
    private int currentCake;

    private final Stats stats;
    private Circle c;

    private final EventHandler<Event> enterEvent;

    // done
    public cakeFactory(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("dimension2D = {}", dimension2D);
        this.stats = stats;
        centerX = dimension2D.getWidth() / 2;
        centerY = dimension2D.getHeight() / 2;
        part = new ImageView[4];
        buttonSize = dimension2D.getWidth() / 8;
        sp = new StackPane();
        cake = new StackPane[3];
        currentCake = 0;

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
        f0.setFill(Color.LIGHTPINK);
        f1.setFill(Color.LIGHTYELLOW);
        f2.setFill(Color.LIGHTGREEN);
        f3.setFill(Color.LIGHTBLUE);
        f4.setFill(Color.LIGHTCORAL);
        f5.setFill(Color.LIGHTSTEELBLUE);
        p[0].getChildren().add(f0);
        p[1].getChildren().add(f1);
        p[2].getChildren().add(f2);
        p[3].getChildren().add(f3);
        p[4].getChildren().add(f4);
        p[5].getChildren().add(f5);

        for (int i = 0; i < 5; i++) { // HomePage of the game
            Button bt = new Button();
            bt.setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; "
                    + "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; "
                    + "-fx-max-height: " + buttonSize + "px;");
            bt.setLayoutX(i * (1.5 * buttonSize) + 0.5 * buttonSize);
            bt.setText("screen" + i);
            int index = i;
            EventHandler<Event> buttonHandler ;
            if(i!=4) {
            	buttonHandler = new EventHandler<Event>() {
	                @Override
	                public void handle(Event e) {
	                    p[index + 1].toFront();
	                    for(int c = 0; c <= currentCake; c++) {
	                    	cake[c].toFront();
	                    }
	                }
	            };
            }else {
        		buttonHandler = new EventHandler<Event>() {
                    @Override
                    public void handle(Event e) {
                    	if (currentCake < 2) {
                    		currentCake++;
                    		createCake(currentCake);
                    		}
                    }
                };
        	}
            bt.addEventHandler(MouseEvent.MOUSE_PRESSED, buttonHandler);
            p[0].getChildren().add(bt);
        }

        for (int j = 1; j < 6; j++) {
            int k = 6;
            if (j == 1) {
                k = 5;
            }
            otherPages(j, k, p);
        }

        for (int i = 5; i >= 0; i--) {
            sp.getChildren().add(p[i]);
        }

    }

    public void otherPages(int j, int k, Pane[] p) {

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        // Other pages
        for (int i = 0; i < k; i++) { // HomePage of the game
            Button bt = new Button();
            bt.setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; "
                    + "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; "
                    + "-fx-max-height: " + buttonSize + "px;");
            if (i < k - 1) {
                bt.setText("color" + i);
                bt.setLayoutX(i * (1.5 * buttonSize) + 0.5 * buttonSize);
                int index = i;
                int jndex = j - 1;
                EventHandler<Event> buttonHandler = new EventHandler<Event>() {
                    @Override
                    public void handle(Event e) {
                        part[jndex] = new ImageView(new Image("data/cake/" + jndex + "" + (index + 1) + ".png"));
                        part[jndex].setFitWidth(dimension2D.getWidth()/(4+currentCake));
                        part[jndex].setPreserveRatio(true);
                        cake[currentCake].getChildren().set(jndex, part[jndex]);
                    }
                };
                bt.addEventHandler(MouseEvent.MOUSE_PRESSED, buttonHandler);
                p[j].getChildren().add(bt);           	
            }else {
                bt.setText("return");
                bt.setLayoutX(dimension2D.getWidth() - buttonSize);
                bt.setLayoutY(dimension2D.getHeight() - (1.2 * buttonSize));
                EventHandler<Event> buttonHandler = new EventHandler<Event>() {
                    @Override
                    public void handle(Event e) {
                        p[0].toFront();
                        for(int c = 0; c <= currentCake; c++) {
                        	cake[c].toFront();
                        }
                    }
                };
                bt.addEventHandler(MouseEvent.MOUSE_PRESSED, buttonHandler);
                p[j].getChildren().add(bt);
            }
        }
    }

    public void createCake(int i) {
    	cake[i] = new StackPane();
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        part[0] = new ImageView(new Image("data/cake/01.png"));
        part[0].setFitWidth(dimension2D.getWidth()/(4+i));
        part[0].setPreserveRatio(true);
        // gameContext.getChildren().add(c);
        part[1] = new ImageView();
        part[2] = new ImageView();
        part[3] = new ImageView();
        cake[i].getChildren().addAll(part);
        
        double height = ((part[0].getImage().getHeight()) * (dimension2D.getWidth()/(4+i)))/part[0].getImage().getWidth();
        if (i!=0) {
        centerY = centerY - height /2;}
    
        cake[i].setLayoutX( centerX - part[0].getFitWidth()/2);
        cake[i].setLayoutY(centerY);
        gameContext.getChildren().add(cake[i]);
    }
    
    // done
    @Override
    public void launch() {

        createStack(sp);
        gameContext.getChildren().add(sp);
        createCake(0);
        gameContext.getChildren().add(this);

        stats.notifyNewRoundReady();

        this.gameContext.resetBordersToFront();

    }

    // done
    @Override
    public void dispose() {
        this.getChildren().clear();
    }

}
