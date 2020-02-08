package net.gazeplay;

import lombok.Getter;
import lombok.NonNull;

public class GameCategories {

    private static final String categoryThumbnailsPath = "data/common/images/categoriesThumbnails/";

    public enum Category {
        ACTION_REACTION("ActionReaction games", categoryThumbnailsPath + "action_reaction.png"),
        SELECTION("Selection games", categoryThumbnailsPath + "target.png"),
        MEMORIZATION("Memorization games", categoryThumbnailsPath + "male-brain.png"),
        LOGIC_MATHS("Logic and Maths games", categoryThumbnailsPath + "logic_icon.png"),
        LITERACY("Literacy games", categoryThumbnailsPath + "literacy_icon.png"),
        MULTIMEDIA("Multimedia", categoryThumbnailsPath + "utility-icon.png");

        @NonNull
        @Getter
        private final String gameCategory;

        @Getter
        @NonNull
        private final String thumbnail;

        Category(final String category, final String thumbnailResource) {
            this.gameCategory = category;
            this.thumbnail = thumbnailResource;
        }
    }

}
