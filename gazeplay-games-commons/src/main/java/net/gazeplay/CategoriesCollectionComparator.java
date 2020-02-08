package net.gazeplay;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public class CategoriesCollectionComparator implements Comparator<SortedSet<GameCategories.Category>>, Serializable {

    private static final Map<GameCategories.Category, Integer> weightMap = createWeightMap();

    private static Map<GameCategories.Category, Integer> createWeightMap() {
        final GameCategories.Category[] values = GameCategories.Category.values();
        final Map<GameCategories.Category, Integer> result = new HashMap<>();
        int weight = values.length;
        for (final GameCategories.Category category : values) {
            result.put(category, (int) Math.pow(weight, 2));
            weight--;
        }
        return result;
    }

    private static int computeTotalWeight(final SortedSet<GameCategories.Category> categories) {
        int result = 0;
        for (final GameCategories.Category category : categories) {
            result += weightMap.get(category);
        }
        return result;
    }

    @Override
    public int compare(final SortedSet<GameCategories.Category> o1, final SortedSet<GameCategories.Category> o2) {
        final int o1Weight = computeTotalWeight(o1);
        final int o2Weight = computeTotalWeight(o2);
        final int diff = o2Weight - o1Weight;
        return Integer.compare(diff, 0);
    }

}
