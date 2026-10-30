package net.gazeplay.games.memory;

import lombok.Getter;

/**
 * What the cards of Memory show (the switches at the top of the window of the variants): pictures, letters, digits,
 * or the pictures of the user (folder images/Memory, chosen in the settings).
 */
public enum MemoryContent {
    IMAGES("MemoryContentImages"),
    LETTERS("MemoryContentLetters"),
    NUMBERS("MemoryContentNumbers"),
    USER_IMAGES("MemoryContentUserImages");

    /**
     * Folder of the pictures of the user, in the images folder of the profile.
     */
    static final String USER_IMAGES_FOLDER = "Memory";

    /**
     * Key of the translation of the switch, also the group of the variants (GroupedGameVariant).
     */
    @Getter
    private final String key;

    MemoryContent(final String key) {
        this.key = key;
    }

    /**
     * @return the content of a group of the variants, the pictures for an unknown one
     */
    static MemoryContent ofKey(final String key) {
        for (final MemoryContent content : values()) {
            if (content.key.equals(key)) {
                return content;
            }
        }
        return IMAGES;
    }
}
