package net.gazeplay.games.cakes;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.gazeplay.IGameContext;
import net.gazeplay.components.ProgressButton;

import java.util.LinkedList;

public class ScreenCake extends LinkedList {

    public IGameContext gameContext;

    public ScreenCake(final int i, final CakeFactory cakef, final boolean easy) {
        super();
        if (i == 0) {
            createScreenZero(cakef, easy);
        }
    }

    public void createScreenZero(final CakeFactory cakef, final boolean easy) {
        gameContext = cakef.getGameContext();
        final double buttonSize = cakef.getButtonSize();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        int n = 4;
        for (int i = 0; i < n; i++) { // HomePage of the game
            final ProgressButton bt = new ProgressButton();
            bt.getButton().setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; "
                + "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; "
                + "-fx-max-height: " + buttonSize + "px;");
            bt.setLayoutX((i + 1) * dimension2D.getWidth() / 5 - buttonSize / 2);
            final EventHandler<Event> buttonHandler = createprogessButtonHandler(i, cakef);
            createButton(i, bt, buttonHandler, cakef);

        }
    }

    public void createButton(final int i, final ProgressButton bt, final EventHandler<Event> buttonHandler, final CakeFactory cakef) {
        final double buttonSize = cakef.getButtonSize();
        final ImageView iv = new ImageView(new Image("data/cake/images/menu" + i + ".png"));
        iv.setFitWidth(2 * buttonSize / 3);
        iv.setPreserveRatio(true);
        bt.setImage(iv);
        bt.getButton().setRadius(buttonSize / 2);
        bt.assignIndicatorUpdatable(buttonHandler, this.gameContext);
        bt.active();
        cakef.getButtons()[i] = bt;
        this.add(bt);
        gameContext.getGazeDeviceManager().addEventFilter(bt.getButton());

    }

    public EventHandler<Event> createprogessButtonHandler(final int i, final CakeFactory cakef) {
        final EventHandler<Event> buttonHandler;
        buttonHandler = e -> {
            for (final Node child : cakef.getP()[i + 1]) {
                child.toFront();
            }
            for (int c = 0; c <= cakef.getMaxCake(); c++) {
                cakef.getCake()[c].toFront();
            }
            cakef.active(i + 1);
            cakef.updateBackgroundColor(cakef.col[i + 1]);
        };
        return buttonHandler;
    }

}
