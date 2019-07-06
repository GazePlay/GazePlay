package net.gazeplay;

import lombok.*;

public class GameCategories {
    /**
     * Action-Reaction Skill
     *
     * Selection Skill
     *
     * Memorization Skill
     *
     * Logic Skill
     */

    public enum Category {
        SELECTION("Selection games", "data/common/images/categoriesThumbnails/target.png"), MEMORIZATION(
                "Memorization games",
                "data/common/images/categoriesThumbnails/male-brain.png"), ACTION_REACTION("ActionReaction games",
                        "data/common/images/categoriesThumbnails/searching-magnifying-glass.png"), LOGIC("Logic games",
                                "data/common/images/categoriesThumbnails/logic_gear.png");

        @NonNull
        @Getter
        private final String gameCategory;

        @Getter
        @NonNull
        private final String thumbnail;

        Category(String category, String thumbnailResource) {
            this.gameCategory = category;
            this.thumbnail = thumbnailResource;
        }
    }

}
