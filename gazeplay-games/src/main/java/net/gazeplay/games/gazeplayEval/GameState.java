package net.gazeplay.games.gazeplayEval;

import lombok.Data;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

@Data
public class GameState {
    public static GazeplayEval eval;
    public static boolean fourThree;
    public static IGameContext context;
    public static GazeplayEvalGameVariant variant;
    public static Stats stats;
}
