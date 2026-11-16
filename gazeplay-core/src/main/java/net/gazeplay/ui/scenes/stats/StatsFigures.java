package net.gazeplay.ui.scenes.stats;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.stats.ExplorationGamesStats;
import net.gazeplay.stats.HiddenItemsGamesStats;
import net.gazeplay.stats.SelectionGamesStats;
import net.gazeplay.stats.ShootGamesStats;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * What the statistics screen shows of a game (refonte of 16/11/2026, REVUE_DES_JEUX.md): its figures, simple and
 * detailed, and its views, chosen from the data of the game (no longer from the name of the game). Nothing here is
 * written in the files of the results.
 */
final class StatsFigures {

    /**
     * A figure: the key of its label and its value as shown.
     *
     * @param good whether it is shown in the color of a success
     */
    record Figure(String labelKey, String value, boolean good) {
    }

    /**
     * The views of the advanced mode, in the order of their tabs.
     */
    enum View {
        HEATMAP("StatsTabHeatmap", "StatsHeatmapTitle", "data/help/stats-heatmap-"),
        DURATIONS("StatsTabDurations", "StatsChartDurations", "data/help/stats-chart-"),
        POSITIONS("StatsTabPositions", "StatsChartPositions", "data/help/stats-positions-"),
        LEVELS("StatsTabLevels", "StatsChartLevels", "data/help/stats-levels-"),
        CHI2("StatsTabChi2", "StatsChartChi2", "data/help/stats-chi2-");

        final String tabKey;

        final String titleKey;

        final String helpPrefix;

        View(final String tabKey, final String titleKey, final String helpPrefix) {
            this.tabKey = tabKey;
            this.titleKey = titleKey;
            this.helpPrefix = helpPrefix;
        }
    }

    private StatsFigures() {
    }

    /**
     * @return the large figures of the simple mode: the time played, then what the kind of game counts (successes,
     *   mistakes, shots, hit rate) and the average time
     */
    static List<Figure> main(final Stats stats, final Locale locale) {
        final List<Figure> figures = new ArrayList<>();
        figures.add(new Figure("TotalLength", duration(stats.computeTotalElapsedDuration(), locale), false));
        if (stats instanceof ExplorationGamesStats) {
            return figures;
        }
        if (stats instanceof ShootGamesStats) {
            figures.add(new Figure("Shots", String.valueOf(stats.getNbGoalsReached()), true));
            figures.add(new Figure("HitRate", stats.getShotRatio() + " %", false));
            figures.add(new Figure("ShotaverageLength", duration(stats.computeRoundsDurationAverageDuration(), locale),
                false));
            return figures;
        }
        figures.add(new Figure(stats instanceof HiddenItemsGamesStats ? "HiddenItemsShot" : "StatsSuccesses",
            String.valueOf(stats.getNbGoalsReached()), true));
        if (stats instanceof SelectionGamesStats && stats.getNbMistakes() > -1) {
            figures.add(new Figure("Mistakes", String.valueOf(stats.getNbMistakes()), false));
        }
        figures.add(new Figure("AverageLength", duration(stats.computeRoundsDurationAverageDuration(), locale), false));
        return figures;
    }

    /**
     * @return the figures of the advanced mode: the durations of the rounds (total, average, median, standard
     *   deviation, fastest and slowest) and the goals; none for an exploration game
     */
    static List<Figure> details(final Stats stats, final Locale locale) {
        final List<Figure> figures = new ArrayList<>();
        if (stats instanceof ExplorationGamesStats) {
            return figures;
        }
        final boolean shoot = stats instanceof ShootGamesStats;
        figures.add(new Figure("Length", duration(stats.getRoundsTotalAdditiveDuration(), locale), false));
        figures.add(new Figure(shoot ? "ShotaverageLength" : "AverageLength",
            duration(stats.computeRoundsDurationAverageDuration(), locale), false));
        figures.add(new Figure(shoot ? "ShotmedianLength" : "MedianLength",
            duration(stats.computeRoundsDurationMedianDuration(), locale), false));
        figures.add(new Figure("StandDev", duration((long) stats.computeRoundsDurationStandardDeviation(), locale),
            false));
        final List<Long> durations = durations(stats);
        if (!durations.isEmpty()) {
            int fastest = 0;
            int slowest = 0;
            for (int i = 1; i < durations.size(); i++) {
                if (durations.get(i) < durations.get(fastest)) {
                    fastest = i;
                }
                if (durations.get(i) > durations.get(slowest)) {
                    slowest = i;
                }
            }
            // The rank of a round, unless the durations are sorted (shooting games)
            figures.add(new Figure("StatsFastest", duration(durations.get(fastest), locale)
                + (shoot ? "" : " (n° " + (fastest + 1) + ")"), false));
            figures.add(new Figure("StatsSlowest", duration(durations.get(slowest), locale)
                + (shoot ? "" : " (n° " + (slowest + 1) + ")"), false));
        }
        figures.add(new Figure("StatsGoals", stats.getNbGoalsReached() + " / " + stats.getNbGoalsToReach(), false));
        if (shoot && stats.getNbUnCountedGoalsReached() != 0) {
            figures.add(new Figure("UncountedShot", String.valueOf(stats.getNbUnCountedGoalsReached()), false));
        }
        return figures;
    }

    /**
     * @return the durations of the rounds, in ms, in their order (sorted for the shooting games, as before)
     */
    static List<Long> durations(final Stats stats) {
        final List<Long> durations = stats instanceof ShootGamesStats ? stats.getSortedDurationsBetweenGoals()
            : stats.getOriginalDurationsBetweenGoals();
        return durations == null ? List.of() : durations;
    }

    /**
     * @return the fixations of the game, mouse then gaze (as the former chart of the positions)
     */
    static List<FixationPoint> fixations(final Stats stats) {
        final List<FixationPoint> points = new ArrayList<>();
        final List<LinkedList<FixationPoint>> sequences = stats.getFixationSequence();
        if (sequences != null) {
            sequences.forEach(points::addAll);
        }
        return points;
    }

    /**
     * @param fixationSequence whether the fixation sequence is recorded (settings)
     * @return the views that have data for this game, in the order of their tabs: the heat map always
     */
    static List<View> views(final Stats stats, final boolean fixationSequence) {
        final List<View> views = new ArrayList<>(List.of(View.HEATMAP));
        if (!(stats instanceof ExplorationGamesStats) && !durations(stats).isEmpty()) {
            views.add(View.DURATIONS);
        }
        if (fixationSequence && !fixations(stats).isEmpty()) {
            views.add(View.POSITIONS);
        }
        if (stats.getLevelsReport() != null && !stats.getLevelsRounds().isEmpty()) {
            views.add(View.LEVELS);
        }
        if (stats.getChiReport() != null && !stats.getChiReport().getChi2PerPlayedLevel().isEmpty()) {
            views.add(View.CHI2);
        }
        return views;
    }

    /**
     * @return the view shown first in the advanced mode: the durations, else the heat map
     */
    static View firstAdvancedView(final List<View> views) {
        return views.contains(View.DURATIONS) ? View.DURATIONS : View.HEATMAP;
    }

    /**
     * @return the locale of the language of GazePlay for the numbers and dates: GazePlay names its languages with three
     *   letters ("fra"), which Java does not use for formatting ("18.4" instead of "18,4"); French when unknown
     */
    static Locale javaLocale(final Locale gazePlayLocale) {
        if (gazePlayLocale == null || gazePlayLocale.getLanguage().isEmpty()) {
            return Locale.FRENCH;
        }
        final String language = gazePlayLocale.getLanguage();
        if (language.length() == 3) {
            for (final String iso2 : Locale.getISOLanguages()) {
                try {
                    if (Locale.of(iso2).getISO3Language().equals(language)) {
                        return Locale.of(iso2, gazePlayLocale.getCountry());
                    }
                } catch (final java.util.MissingResourceException noCode) {
                    // A language without a code of three letters
                }
            }
        }
        return gazePlayLocale;
    }

    /**
     * @return a duration for a person: "1 h 02 min", "3 min 42 s", or "18,4 s" (one decimal, in the language)
     */
    static String duration(final long millis, final Locale locale) {
        final long ms = Math.max(0, millis);
        final long seconds = ms / 1000;
        if (seconds >= 3600) {
            return String.format("%d h %02d min", seconds / 3600, seconds % 3600 / 60);
        }
        if (seconds >= 60) {
            return String.format("%d min %02d s", seconds / 60, seconds % 60);
        }
        final NumberFormat format = NumberFormat.getNumberInstance(locale);
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        return format.format(ms / 1000.0) + " s";
    }

    /**
     * @return the variant of the game for a person, e.g. "Farm · 10 screens · 2 × 3": the switches of a variant in
     *   groups (but the one named as the game, the only group of a game without groups) then its value; the label of
     *   another variant; null without a variant or for one that cannot be read
     */
    static String variant(final String persistentForm, final String gameNameCode, final Translator translator) {
        if (persistentForm == null || persistentForm.isBlank()) {
            return null;
        }
        final IGameVariant variant;
        try {
            variant = IGameVariant.toGameVariant(persistentForm);
        } catch (final RuntimeException unreadable) {
            return null;
        }
        if (variant == null) {
            return null;
        }
        if (variant instanceof GroupedGameVariant grouped) {
            final List<String> parts = new ArrayList<>();
            for (final String value : grouped.getSwitches()) {
                if (value != null && !value.isBlank() && !value.equals(gameNameCode)) {
                    parts.add(translator.translate(value));
                }
            }
            parts.add(grouped.getLabel(translator));
            return String.join(" · ", parts);
        }
        return variant.getLabel(translator);
    }
}
