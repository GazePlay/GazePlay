package net.gazeplay;

import javafx.scene.Scene;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class GameSpec {

    public interface GameLauncher<T extends Stats, V extends GameVariant> {

        T createNewStats(Scene scene);

        GameLifeCycle createNewGame(IGameContext gameContext, V gameVariant, T stats);

    }

    /**
     * this is a basically marker interface, but it comes also with a label in order to recognise it by a text label
     */
    public interface GameVariant {

        String getLabel(Translator translator);

    }

    @Data
    public static class DimensionGameVariant implements GameVariant {

        private final int width;

        private final int height;

        @Override
        public String getLabel(Translator translator) {
            return width + "x" + height;
        }
    }

    @Data
    public static class IntGameVariant implements GameVariant {

        private final int number;
        private final String label;

        @Override
        public String getLabel(Translator translator) {
            return number + " " + translator.translate(label);
        }
    }

    @Data
    public static class StringGameVariant implements GameVariant {

        private final String label;

        private final String value;

        @Override
        public String getLabel(Translator translator) {
            return translator.translate(label) + " " + value;
        }
    }

    @Data
    public static class EnumGameVariant<K extends Enum<K>> implements GameVariant {

        private final K enumValue;

        private final Function<K, String> extractLabelCodeFunction;

        @Override
        public String getLabel(Translator translator) {
            return translator.translate(extractLabelCodeFunction.apply(enumValue));
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

    @Data
    public static class EnumGameVariantGenerator<K extends Enum<K>> implements GameVariantGenerator {

        private final K[] enumValues;

        private final Function<K, String> extractLabelCodeFunction;

        @Override
        public Set<GameVariant> getVariants() {
            LinkedHashSet<GameVariant> result = new LinkedHashSet<>();
            for (K value : enumValues) {
                result.add(new EnumGameVariant<>(value, extractLabelCodeFunction));
            }
            return result;
        }
    }

    @Data
    public static class IntRangeVariantGenerator implements GameVariantGenerator {

        private final int min;

        private final int max;

        private final String label;

        @Override
        public Set<GameVariant> getVariants() {
            LinkedHashSet<GameVariant> result = new LinkedHashSet<>();
            for (int i = min; i <= max; i++) {
                result.add(new IntGameVariant(i, label));
            }
            return result;
        }

    }

    @Data
    public static class IntListVariantGenerator implements GameVariantGenerator {

        private final String label;

        private final int[] values;
        
        public IntListVariantGenerator(String label, int... values) {
            this.label = label;
            this.values = values;
        }

        @Override
        public Set<GameVariant> getVariants() {
            LinkedHashSet<GameVariant> result = new LinkedHashSet<>();
            for (int i : values) {
                result.add(new IntGameVariant(i, label));
            }
            return result;
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

    public GameSpec(
        GameSummary gameSummary,
        GameVariantGenerator gameVariantGenerator,
        GameLauncher gameLauncher
    ) {
        this.gameSummary = gameSummary;
        this.gameVariantGenerator = gameVariantGenerator;
        this.gameLauncher = gameLauncher;
    }

    public GameSpec(GameSummary gameSummary, GameLauncher gameLauncher) {
        this(gameSummary, new NoVariantGenerator(), gameLauncher);
    }

}
