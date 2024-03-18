package net.gazeplay.games.gazeplayEval;

import lombok.Data;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

import static net.gazeplay.games.gazeplayEval.config.Const.ROOT_DIRECTORY;

@Data
public class GameState {
    public static GazeplayEval eval;
    public static boolean fourThree;
    public static IGameContext context;
    public static GazeplayEvalGameVariant variant;
    public static Stats stats;

    static GazeplayEval setup(IGameContext gameContext, GazeplayEvalGameVariant gameVariant, Stats gameStats, double gameSeed) {
        fourThree = false;  // For some reason it stayed false in the previous code, so I let it be
        context = gameContext;
        variant = gameVariant;
        stats = gameStats;
        eval = new GazeplayEval(gameSeed);
        return eval;
    }

    public static String getPathFor(String itemInGameDir) {
        return ROOT_DIRECTORY + variant.getNameGame() + "\\" + itemInGameDir;
    }
}
