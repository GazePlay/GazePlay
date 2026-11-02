package net.gazeplay.games.opinions;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Statistics of the game "Opinions" (see {@code Stats}), with the answers of the person (2/11/2026): besides the usual
 * files, {@link #saveStats()} writes the answers file "&lt;time&gt;-opinions.csv" (see {@link OpinionsAnswers}), named
 * like the other files of the game.
 */
@Slf4j
public class OpinionsGameStats extends Stats {

    private final OpinionsAnswers answers = new OpinionsAnswers();

    public OpinionsGameStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Opinions-game";
    }

    public OpinionsGameStats(Scene gameContextScene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "Opinions-game";
    }

    /**
     * Records the answer of the person on an image.
     */
    void addAnswer(final String image, final OpinionChoice choice) {
        answers.add(image, choice);
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {
        final SavedStatsInfo info = super.saveStats();
        if (info != null && info.getReplayDataFile() != null) {
            final String replayName = info.getReplayDataFile().getName();
            final String prefix = replayName.substring(0, replayName.length() - "-replayData.json".length());
            final File file = new File(info.getReplayDataFile().getParentFile(), prefix + OpinionsAnswers.FILE_SUFFIX);
            answers.write(file);
            log.info("Answers written in {}", file);
        }
        return info;
    }
}
