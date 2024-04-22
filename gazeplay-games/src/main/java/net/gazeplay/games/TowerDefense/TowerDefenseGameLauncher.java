package net.gazeplay.games.TowerDefense;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;

public class TowerDefenseGameLauncher implements IGameLauncher<Stats, EnumGameVariant<TowerDefenseVariant>> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new TowerDefenseStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<TowerDefenseVariant> gameVariant, Stats stats) {
        return new TowerDefense(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<TowerDefenseVariant> gameVariant, Stats stats, double gameSeed) {
        return new TowerDefense(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return null;
    }
}
