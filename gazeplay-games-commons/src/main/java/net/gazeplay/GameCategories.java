package net.gazeplay;

import lombok.Getter;
import lombok.NonNull;

public class GameCategories {

    private static final String categoryThumbnailsPath = "data/common/images/categoriesThumbnails/";

    public enum Category {
        ACTION_REACTION("ActionReaction games", categoryThumbnailsPath + "searching-magnifying-glass.png"),
        SELECTION("Selection games", categoryThumbnailsPath + "target.png"),
        MEMORIZATION("Memorization games", categoryThumbnailsPath + "male-brain.png"),
        LOGIC_MATHS("Logic and Maths games", categoryThumbnailsPath + "creativity-icon.png"),
        LITERACY("Literacy games", categoryThumbnailsPath + "logic_gear.png"),
        MULTIMEDIA("Multimedia", categoryThumbnailsPath + "utility-icon.png");

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
