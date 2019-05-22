package net.gazeplay;

import lombok.*;

public class GameCategories {

    // FIXATION_GAME = "";
    // LOGIC_GAME = "";

    public enum Category {
        TARGET("Target games", "data/common/images/categoriesThumbnails/target.png"), MEMORIZATION("Memorization games",
                "data/common/images/categoriesThumbnails/male-brain.png"), SEARCHING("Searching games",
                        "data/common/images/categoriesThumbnails/searching-magnifying-glass.png");

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
