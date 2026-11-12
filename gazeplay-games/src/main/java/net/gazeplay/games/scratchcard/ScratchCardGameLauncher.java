package net.gazeplay.games.scratchcard;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates "Cartes à gratter" (name code ScratchCard) and its statistics (folder "Scratchcard", as before), for a new
 * game or for the replay of a saved one (see {@code IGameLauncher}). It used to start the game "Blocs" with other
 * settings; without a variant (an old shortcut), it plays as close to before as can be: animals, medium scraper, no
 * end (maintainer's choice of 12/11/2026).
 */
public class ScratchCardGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new ScratchcardGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new ScratchcardGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static ScratchCard create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                              final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return new ScratchCard(gameContext, stats, ScratchCardPictures.of(grouped.getGroup()),
                ScratchCardBrush.of(grouped.getValue()), ScratchCardEnd.of(grouped.getSubGroup()), gameSeed);
        }
        return new ScratchCard(gameContext, stats, ScratchCardPictures.ANIMALS, ScratchCardBrush.MEDIUM,
            ScratchCardEnd.ENDLESS, gameSeed);
    }
}
