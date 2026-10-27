package net.gazeplay;

import lombok.Getter;

import java.util.List;

/**
 * The two modes of the application, chosen at startup before the profile: the games, or the evaluations of GazePlay
 * Eval. The mode is a choice of the session, held by {@link GazePlay} in the JVM of the menu: it is neither saved nor
 * given to the JVM of a game, which does not need it.
 */
public enum AppMode {

    /**
     * The menu of the games and their settings, without the evaluations.
     */
    GAMES("ModeGames"),

    /**
     * The menu of the evaluations and their settings, without the games.
     */
    EVALUATIONS("ModeEvaluations");

    /**
     * Translation key of the name of the mode.
     */
    @Getter
    private final String translationKey;

    AppMode(final String translationKey) {
        this.translationKey = translationKey;
    }

    /**
     * @return whether an entry of the catalog belongs to this mode
     */
    public boolean shows(final GameSpec gameSpec) {
        return gameSpec.getGameSummary().isEvaluation() == (this == EVALUATIONS);
    }

    /**
     * @return the entries of the catalog belonging to this mode, in the same order
     */
    public List<GameSpec> filter(final List<GameSpec> gameSpecs) {
        return gameSpecs.stream().filter(this::shows).toList();
    }
}
