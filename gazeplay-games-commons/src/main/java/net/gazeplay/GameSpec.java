package net.gazeplay;

import lombok.Getter;
import lombok.NonNull;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;
import net.gazeplay.commons.gamevariants.generators.NoVariantGenerator;

/**
 * Everything the menu needs to know about a game: its summary (name, thumbnail, categories), its variants, and how to
 * launch it.
 */
public class GameSpec {

    @Getter
    @NonNull
    private final GameSummary gameSummary;

    @Getter
    @NonNull
    private final IGameVariantGenerator gameVariantGenerator;

    @Getter
    @NonNull
    private final IGameLauncher gameLauncher;

    public GameSpec(
        final GameSummary gameSummary,
        final IGameVariantGenerator gameVariantGenerator,
        final IGameLauncher gameLauncher
    ) {
        this.gameSummary = gameSummary;
        this.gameVariantGenerator = gameVariantGenerator;
        this.gameLauncher = gameLauncher;
    }

    /**
     * A game without variants.
     */
    public GameSpec(final GameSummary gameSummary, final IGameLauncher gameLauncher) {
        this(gameSummary, new NoVariantGenerator(), gameLauncher);
    }
}
