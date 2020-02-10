package net.gazeplay;

import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.ui.Translator;

import java.util.Comparator;

@RequiredArgsConstructor
public class GameSummaryComparator implements Comparator<GameSummary> {

    private final CategoriesCollectionComparator categoriesCollectionComparator = new CategoriesCollectionComparator();

    private final Translator translator;

    @Override
    public int compare(final GameSummary o1, final GameSummary o2) {
        return Comparator
            .comparing(GameSummary::getCategories, categoriesCollectionComparator)
            .thenComparing(Comparator.comparing(GameSummary::getPriority).reversed())
            .thenComparing(GameSummary::getNameCode, Comparator.comparing(translator::translate))
            .compare(o1, o2);
    }

}
