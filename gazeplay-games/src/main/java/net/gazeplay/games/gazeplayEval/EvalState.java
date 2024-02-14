package net.gazeplay.games.gazeplayEval;

import lombok.Data;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.gazeplayEval.deprecated.GazeplayEval;

@Data
public class EvalState {
    public static GazeplayEval evalInstance;
    public static boolean fourThree;
    public static IGameContext gameContext;
    public static GazeplayEvalGameVariant gameVariant;
    public static Stats stats;
}
