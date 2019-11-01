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

    @Override
    public int compareTo(GameSummary o) {
        return Comparator
            .comparing(GameSummary::getCategories, categoriesCollectionComparator)
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
