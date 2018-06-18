package net.gazeplay.games.pet;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.stats.Stats;

public class PetHouse extends Parent implements GameLifeCycle {

    private final GameContext gameContext;
    private final Stats stats;

    @Getter
    private Rectangle background;

    public PetHouse(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        this.background.setFill(new ImagePattern(new Image("background.jpg")));
        gameContext.getChildren().add(this.background);

        gameContext.getChildren().add(this);
    }

    @Override
    public void launch() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    public void setBath() {

    }

    public void setLunch() {

    }

    public void setSports() {

    }

}
