package net.gazeplay.games.cakes;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.gazeplay.IGameContext;
import net.gazeplay.components.ProgressButton;

import java.util.LinkedList;

public class ScreenCake extends LinkedList {

    public IGameContext gameContext;

    public ScreenCake(final int i, final CakeFactory cakef) {
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

    public void createScreenZero(final CakeFactory cakef) {
        gameContext = cakef.getGameContext();
        final double buttonSize = cakef.getButtonSize();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        for (int i = 0; i < 6; i++) { // HomePage of the game
            final ProgressButton bt = new ProgressButton();
            bt.getButton().setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; "
                + "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; "
                + "-fx-max-height: " + buttonSize + "px;");
            bt.setLayoutX((i + 1) * dimension2D.getWidth() / 6 - buttonSize / 2);
            final EventHandler<Event> buttonHandler = createprogessButtonHandler(i, cakef);
            if (i != 5) {
                createButton(i, bt, buttonHandler, cakef);
            } else {
                createValidationButton(i, bt, cakef);
            }

        }
    }

    public void createButton(final int i, final ProgressButton bt, final EventHandler<Event> buttonHandler, final CakeFactory cakef) {
        final double buttonSize = cakef.getButtonSize();
        final ImageView iv = new ImageView(new Image("data/cake/images/menu" + i + ".png"));
        iv.setFitWidth(2 * buttonSize / 3);
        iv.setPreserveRatio(true);
        bt.setImage(iv);
        bt.getButton().setRadius(buttonSize / 2);
        bt.assignIndicator(buttonHandler, cakef.getFixationLength());
        bt.active();
        cakef.getButtons()[i] = bt;
        if (i == 2) {
            cakef.getButtons()[i].setDisable(!cakef.isNappage());
        }
        this.add(bt);
        gameContext.getGazeDeviceManager().addEventFilter(bt.getButton());

    }

    public void createValidationButton(final int i, final ProgressButton bt, final CakeFactory cakef) {
        final double buttonSize = cakef.getButtonSize();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final ImageView iv = new ImageView(new Image("data/cake/images/validate.png"));
        iv.setFitWidth(2 * buttonSize / 3);
        iv.setPreserveRatio(true);
        bt.setImage(iv);
        bt.getButton().setRadius(buttonSize / 2);
        bt.setLayoutX(dimension2D.getWidth() - buttonSize);
        bt.setLayoutY(dimension2D.getHeight() - (1.2 * buttonSize));
        final EventHandler<Event> buttonHandler = e -> cakef.winFunction();
        bt.assignIndicator(buttonHandler, cakef.getFixationLength());
        bt.active();
        cakef.getButtons()[i] = bt;
        this.add(bt);
        gameContext.getGazeDeviceManager().addEventFilter(bt.getButton());
    }

    public EventHandler<Event> createprogessButtonHandler(final int i, final CakeFactory cakef) {
        final EventHandler<Event> buttonHandler;
        if (i != 4) {
            buttonHandler = e -> {
                for (final Node child : cakef.getP()[i + 1]) {
                    child.toFront();
                }
                for (int c = 0; c <= cakef.getMaxCake(); c++) {
                    cakef.getCake()[c].toFront();
                }
                cakef.active(i + 1);
                cakef.r.setFill(cakef.col[i + 1]);
            };
        } else {
            buttonHandler = e -> {
                if (!cakef.getVariant().equals(CakeGameVariant.FREE)) {
                    cakef.winButton(false);
                }
                if (cakef.getMaxCake() < 2) {
                    cakef.setMaxCake(cakef.getMaxCake() + 1);
                    cakef.setCurrentCake(cakef.getMaxCake());
                    cakef.createCake(cakef.getMaxCake());
                }
                if (!cakef.getVariant().equals(CakeGameVariant.FREE)) {
                    cakef.winButton(false);
                }
                if (cakef.getMaxCake() >= 2) {
                    if (e.getSource() instanceof ProgressButton) {
                        ((ProgressButton) e.getSource()).setDisable(true);
                    } else if (e.getSource() instanceof Button) {
                        ((Button) e.getSource()).setDisable(true);
                    }
                }

            };
        }
        return buttonHandler;
    }
}
