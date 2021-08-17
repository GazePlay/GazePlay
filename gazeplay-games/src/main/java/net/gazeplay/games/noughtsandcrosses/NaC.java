package net.gazeplay.games.noughtsandcrosses;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;


@Slf4j
public class NaC extends Parent implements GameLifeCycle {

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    @Getter
    @Setter
    private NaCGameVariant variant;

    private final Dimension2D dimension2D;

    int[][] game;

    NaC(final IGameContext gameContext, final Stats stats, final NaCGameVariant variant) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;

        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        int[][] game = new int[][]
            {
                {0,0,0},
                {},
                {}
            };
    }

    @Override
    public void launch(){
        gameContext.getChildren().clear();


        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose(){
        stats.stop();
        gameContext.getChildren().clear();
    }
}
