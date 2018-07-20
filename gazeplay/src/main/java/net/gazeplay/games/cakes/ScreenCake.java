package net.gazeplay.games.cakes;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.gazeplay.GameContext;
import net.gazeplay.commons.utils.ProgressButton;

import java.util.LinkedList;

public class ScreenCake extends LinkedList {

    public GameContext gameContext;

    public ScreenCake(int i, CakeFactory cakef) {
        super();
        /*
         * gameContext = cakef.getGameContext(); Dimension2D dimension2D =
         * gameContext.getGamePanelDimensionProvider().getDimension2D(); Rectangle r = new Rectangle(0, 0,
         * dimension2D.getWidth(), dimension2D.getHeight()); r.setFill(c); this.add(r); Rectangle back = new
         * Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight()); back.setFill(new ImagePattern(new
         * Image("data/cake/background.png"))); back.setMouseTransparent(true); this.add(back);
         */
        if (i == 0) {
            createScreenZero(cakef);
        }
    }

    public void createScreenZero(CakeFactory cakef) {
        gameContext = cakef.getGameContext();
        double buttonSize = cakef.getButtonSize();
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        for (int i = 0; i < 6; i++) { // HomePage of the game
            ProgressButton bt = new ProgressButton();
            bt.button.setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; "
                    + "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; "
                    + "-fx-max-height: " + buttonSize + "px;");
            bt.setLayoutX((i + 1) * dimension2D.getWidth() / 6 - buttonSize / 2);
            EventHandler<Event> buttonHandler = createprogessButtonHandler(i, cakef);
            if (i != 5) {
                createButton(i, bt, buttonHandler, cakef);
            } else {
                createValidationButton(i, bt, buttonHandler, cakef);
            }

        }
    }

    public void createButton(int i, ProgressButton bt, EventHandler<Event> buttonHandler, CakeFactory cakef) {
        double buttonSize = cakef.getButtonSize();
        ImageView iv = new ImageView(new Image("data/cake/images/menu" + i + ".png"));
        iv.setFitWidth(2 * buttonSize / 3);
        iv.setPreserveRatio(true);
        bt.setImage(iv);
        bt.button.setRadius(buttonSize / 2);
        bt.assignIndicator(buttonHandler, cakef.getFixationLength());
        bt.active();
        cakef.getButtons()[i] = bt;
        if (i == 2) {
            cakef.getButtons()[i].setDisable(!cakef.isNappage());
        }
        ;
        this.add(bt);
        gameContext.getGazeDeviceManager().addEventFilter(bt.button);

    }

    public void createValidationButton(int i, ProgressButton bt, EventHandler<Event> buttonHandler, CakeFactory cakef) {
        double buttonSize = cakef.getButtonSize();
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        ImageView iv = new ImageView(new Image("data/cake/images/validate.png"));
        iv.setFitWidth(2 * buttonSize / 3);
        iv.setPreserveRatio(true);
        bt.setImage(iv);
        bt.button.setRadius(buttonSize / 2);
        bt.setLayoutX(dimension2D.getWidth() - buttonSize);
        bt.setLayoutY(dimension2D.getHeight() - (1.2 * buttonSize));
        buttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                cakef.winFunction();
            }
        };
        bt.assignIndicator(buttonHandler, cakef.getFixationLength());
        bt.active();
        cakef.getButtons()[i] = bt;
        this.add(bt);
        gameContext.getGazeDeviceManager().addEventFilter(bt.button);
    }

    public EventHandler<Event> createprogessButtonHandler(int i, CakeFactory cakef) {
        EventHandler<Event> buttonHandler;
        if (i != 4) {
            buttonHandler = new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    for (Node child : cakef.getP()[i + 1]) {
                        child.toFront();
                    }
                    for (int c = 0; c <= cakef.getMaxCake(); c++) {
                        cakef.getCake()[c].toFront();
                    }
                    cakef.active(i + 1);
                    cakef.r.setFill(cakef.col[i + 1]);
                }
            };
        } else {
            buttonHandler = new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    if (cakef.getMode() != 0) {
                        cakef.winButton(false);
                    }
                    if (cakef.getMaxCake() < 2) {
                        cakef.setMaxCake(cakef.getMaxCake() + 1);
                        cakef.setCurrentCake(cakef.getMaxCake());
                        cakef.createCake(cakef.getMaxCake());
                    }
                    if (cakef.getMode() != 0) {
                        cakef.winButton(false);
                    }
                    if (cakef.getMaxCake() >= 2) {
                        if (e.getSource() instanceof ProgressButton) {
                            ((ProgressButton) e.getSource()).setDisable(true);
                        } else if (e.getSource() instanceof Button) {
                            ((Button) e.getSource()).setDisable(true);
                        }
                    }

                }
            };
        }
        return buttonHandler;
    }
}
