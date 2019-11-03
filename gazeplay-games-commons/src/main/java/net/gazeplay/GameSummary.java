package net.gazeplay;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

@Builder
@Slf4j
@EqualsAndHashCode
public class GameSummary implements Comparable<GameSummary> {

    private static final CategoriesCollectionComparator categoriesCollectionComparator = new CategoriesCollectionComparator();

    @Getter
    private final String nameCode;

    @Getter
    private final String gameThumbnail;

    @Getter
    @Singular("category")
    private final SortedSet<GameCategories.Category> categories;

    @Getter
    private final String backgroundMusicUrl;

    @Getter
    private final String description;

    /**
     * For games in the same categories, it can happen that the nameCode is not appropriate to order the games as expected.
     * <p>
     * We may like that some games are sorted in a specific order, but their nameCode natural order does not match the order we like.
     * But we don't want to mess with the nameCode just to have a different ordering.
     * <p>
     * Introducing priority :
     * Priority is used in the comparator chain : after the categories, but before the nameCode .
     * So the priority will be used to sort games in the same categories.
     * When priorities for multiple games is equal, then the nameCode is used to order them.
     * 
     * higher priority means games is sorted first (so comparator will compare on reversed order)
     */
    @Getter
    private final int priority;

    @Override
    public int compareTo(GameSummary o) {
        return Comparator
            .comparing(GameSummary::getCategories, categoriesCollectionComparator)
            .thenComparing(Comparator.comparing(GameSummary::getPriority).reversed())
            .thenComparing(GameSummary::getNameCode)
            .compare(this, o);
    }

    private static class CategoriesCollectionComparator implements Comparator<SortedSet<GameCategories.Category>> {

        private static final Map<GameCategories.Category, Integer> weightMap = createWeightMap();

        private static Map<GameCategories.Category, Integer> createWeightMap() {
            GameCategories.Category[] values = GameCategories.Category.values();
            Map<GameCategories.Category, Integer> result = new HashMap<>();
            int weight = values.length;
            for (GameCategories.Category category : values) {
                result.put(category, (int) Math.pow(weight, 2));
                weight--;
            }
            return result;
        }

        private static int computeTotalWeight(SortedSet<GameCategories.Category> categories) {
            int result = 0;
            for (GameCategories.Category category : categories) {
                result += weightMap.get(category);
            }
            return result;
        }

        @Override
        public int compare(SortedSet<GameCategories.Category> o1, SortedSet<GameCategories.Category> o2) {
            int o1Weight = computeTotalWeight(o1);
            int o2Weight = computeTotalWeight(o2);
            int diff = o2Weight - o1Weight;
            return Integer.compare(diff, 0);
        }

    }
}
