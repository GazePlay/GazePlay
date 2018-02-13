package net.gazeplay;

import javafx.scene.Scene;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.LinkedHashSet;
import java.util.Set;

public class GameSpec {

    public interface GameLauncher<T extends Stats, V extends GameVariant> {

        T createNewStats(Scene scene);

        GameLifeCycle createNewGame(GameContext gameContext, V gameVariant, T stats);

    }

    /**
     * this is a basically marker interface, but it comes also with a label in order to recognise it by a text label
     */
    public interface GameVariant {

        String getLabel();

    }

    @Data
    public static class DimensionGameVariant implements GameVariant {

        private final int width;

        private final int height;

        @Override
        public String getLabel() {
            return width + "x" + height;
        }
    }

    public interface GameVariantGenerator {

        Set<GameVariant> getVariants();

    }

    public static class NoVariantGenerator implements GameVariantGenerator {
        @Override
        public Set<GameVariant> getVariants() {
            return new LinkedHashSet<>();
        }
    }

    @Getter
    @NonNull
    private final GameSummary gameSummary;

    @Getter
    @NonNull
    private final GameVariantGenerator gameVariantGenerator;

    @Getter
    @NonNull
    private final GameLauncher gameLauncher;

    public GameSpec(@NonNull GameSummary gameSummary, @NonNull GameVariantGenerator gameVariantGenerator,
            @NonNull GameLauncher gameLauncher) {
        this.gameSummary = gameSummary;
        this.gameVariantGenerator = gameVariantGenerator;
        this.gameLauncher = gameLauncher;
    }

    public GameSpec(@NonNull GameSummary gameSummary, @NonNull GameLauncher gameLauncher) {
        this(gameSummary, new NoVariantGenerator(), gameLauncher);
    }

}
