package net.gazeplay;

import lombok.Getter;
import lombok.NonNull;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;
import net.gazeplay.commons.gamevariants.generators.NoVariantGenerator;

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

    public GameSpec(final GameSummary gameSummary, final IGameLauncher gameLauncher) {
        this(gameSummary, new NoVariantGenerator(), gameLauncher);
    }
}
