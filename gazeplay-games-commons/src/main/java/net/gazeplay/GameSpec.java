package net.gazeplay;

import javafx.scene.Scene;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
        public String getLabel(final Translator translator) {
            return width + "x" + height;
        }
    }

    @Data
    public static class IntGameVariant implements GameVariant {

        private final int number;

        @Override
        public String getLabel(final Translator translator) {
            return Integer.toString(number);
        }
    }

    @Data
    public static class StringGameVariant implements GameVariant {

        private final String label;

        private final String value;

        @Override
        public String getLabel(final Translator translator) {
            return translator.translate(label);
        }
    }

    @Data
    public static class EnumGameVariant<K extends Enum<K>> implements GameVariant {

        private final K enumValue;

        private final Function<K, String> extractLabelCodeFunction;

        @Override
        public String getLabel(final Translator translator) {
            return translator.translate(extractLabelCodeFunction.apply(enumValue));
        }

    }

    public interface GameVariantGenerator {

        default String getVariantChooseText() {
            return "Choose Game Variant";
        }

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

        @Getter
        @Setter
        private String variantChooseText = "Choose Game Variant";

        @Override
        public Set<GameVariant> getVariants() {
            final LinkedHashSet<GameVariant> result = new LinkedHashSet<>();
            for (final K value : enumValues) {
                result.add(new EnumGameVariant<>(value, extractLabelCodeFunction));
            }
            return result;
        }
    }

    @Data
    public static class IntRangeVariantGenerator implements GameVariantGenerator {

        private final String variantChooseText;

        private final int min;

        private final int max;

        @Override
        public Set<GameVariant> getVariants() {
            final LinkedHashSet<GameVariant> result = new LinkedHashSet<>();
            for (int i = min; i <= max; i++) {
                result.add(new IntGameVariant(i));
            }
            return result;
        }

    }

    @Data
    public static class IntListVariantGenerator implements GameVariantGenerator {

        private final String variantChooseText;

        private final int[] values;

        public IntListVariantGenerator(final String variantChooseText, final int... values) {
            this.variantChooseText = variantChooseText;
            this.values = values;
        }

        @Override
        public Set<GameVariant> getVariants() {
            final LinkedHashSet<GameVariant> result = new LinkedHashSet<>();
            for (final int i : values) {
                result.add(new IntGameVariant(i));
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
        final GameSummary gameSummary,
        final GameVariantGenerator gameVariantGenerator,
        final GameLauncher gameLauncher
    ) {
        this.gameSummary = gameSummary;
        this.gameVariantGenerator = gameVariantGenerator;
        this.gameLauncher = gameLauncher;
    }

    public GameSpec(final GameSummary gameSummary, final GameLauncher gameLauncher) {
        this(gameSummary, new NoVariantGenerator(), gameLauncher);
    }

}
