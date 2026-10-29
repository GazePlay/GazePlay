package net.gazeplay;

import lombok.Getter;
import lombok.NonNull;

/**
 * Categories of the games, shown as filters in the menu.
 */
public class GameCategories {

    /**
     * A category: its translation key. The order of the values is the order of the games in the menu.
     */
    public enum Category {
        ACTION_REACTION("Action-Reaction games"),
        SELECTION("Selection games"),
        MEMORIZATION("Memorization games"),
        LOGIC_MATHS("Logic and Maths games"),
        LITERACY("Literacy games"),
        MULTIMEDIA("Multimedia");

        @NonNull
        @Getter
        private final String gameCategory;

        Category(final String category) {
            this.gameCategory = category;
        }
    }
}
