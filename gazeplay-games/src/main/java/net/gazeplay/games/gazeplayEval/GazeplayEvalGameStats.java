package net.gazeplay.games.gazeplayEval;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Statistics of a session of an evaluation. All the files of a session are in its folder (decision of the maintainer,
 * 5/11/2026): the results are written by {@link EvalResultsWriter}, and here only the three images of the whole session,
 * read by the statistics screen, and the video. The other usual files of {@code Stats} are not written: their data are
 * in the files of the session (samples, results). Without session (left on the participant form, no evaluation
 * imported), nothing is saved and there is no statistics screen.
 */
@Slf4j
public class GazeplayEvalGameStats extends HiddenItemsGamesStats {

    static final String WHOLE_SESSION_GAZE = "passation-regard.png";

    static final String WHOLE_SESSION_MOUSE = "passation-souris.png";

    static final String WHOLE_SESSION_GAZE_AND_MOUSE = "passation-regard-et-souris.png";

    /**
     * The folder of the current session, null without session.
     */
    private File sessionFolder;

    public GazeplayEvalGameStats(Scene scene) {
        super(scene);
        this.gameName = "GazePlayEval";
    }

    public GazeplayEvalGameStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "GazePlayEval";
    }

    /**
     * A session starts: the files go to its folder, and the video starts, after the participant form (which must not be
     * filmed).
     */
    void startSession(final File folder) {
        sessionFolder = folder;
        if (!super.isSavingDisabled() && ActiveConfigurationContext.getInstance().isVideoRecordingEnabled()) {
            startVideoRecording();
        }
    }

    /**
     * A new round of the game: the previous session is over, and nothing goes to its folder any more.
     */
    void endSession() {
        sessionFolder = null;
    }

    @Override
    protected boolean startsTheVideoWithTheGame() {
        return false;
    }

    @Override
    public boolean isSavingDisabled() {
        return super.isSavingDisabled() || sessionFolder == null;
    }

    /**
     * @return the folder of the session (also the one opened by the statistics screen), created if missing
     */
    @Override
    public File getGameStatsOfTheDayDirectory() {
        if (sessionFolder == null) {
            return super.getGameStatsOfTheDayDirectory();
        }
        // Tried again here, if its creation failed at the start of the session
        if (!sessionFolder.isDirectory() && !sessionFolder.mkdirs()) {
            log.warn("Cannot create the folder of the session {}", sessionFolder);
        }
        return sessionFolder;
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {
        if (isSavingDisabled()) {
            return null;
        }
        final File heatmaps = new File(sessionFolder, GazeplayEval.HEATMAPS_FOLDER);
        Files.createDirectories(heatmaps.toPath());
        saveMetricsImagesOnly(new File(heatmaps, WHOLE_SESSION_MOUSE), new File(heatmaps, WHOLE_SESSION_GAZE),
            new File(heatmaps, WHOLE_SESSION_GAZE_AND_MOUSE));
        return getSavedStatsInfo();
    }
}
