package net.gazeplay.games.simon;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class Simon  extends Parent implements GameLifeCycle {


    private IGameContext gameContext;
    private Stats stats;
    private SimonGameVariant gameVariant;

    public Simon(final IGameContext gameContext, final Stats stats, SimonGameVariant gameVariant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
    }



    public void startGame(){

        Borne borne = new Borne(gameContext,this);

    }

    @Override
    public void launch() {
        this.stats.reset();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        gameContext.setLimiterAvailable();
        Rectangle background = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());
        background.setFill(Color.WHITE);

        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.getChildren().add(background);

        startGame();

        stats.notifyNewRoundReady();
        gameContext.firstStart();
    }

    @Override
    public void dispose() {

    }
}
