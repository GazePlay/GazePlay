package net.gazeplay;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public class CategoriesCollectionComparator implements Comparator<SortedSet<GameCategories.Category>>, Serializable {

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
