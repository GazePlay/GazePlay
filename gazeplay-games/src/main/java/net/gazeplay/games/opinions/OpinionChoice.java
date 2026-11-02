package net.gazeplay.games.opinions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.components.GameStyle;

/**
 * The two answers of the game "Opinions": the person likes the image, or not.
 */
@RequiredArgsConstructor
public enum OpinionChoice {
    LIKE("aime", "OpinionsLike", "thumb_up_fill1", GameStyle.SUCCESS_COLOR),
    DISLIKE("n'aime pas", "OpinionsDislike", "thumb_down_fill1", "#D64545");

    /**
     * The answer as written in the file of the answers (in French whatever the language: a stable format).
     */
    @Getter
    private final String written;

    /**
     * Key of the translation of the button.
     */
    @Getter
    private final String label;

    /**
     * Icon of the button (data/icons).
     */
    @Getter
    private final String icon;

    /**
     * Color of the button.
     */
    @Getter
    private final String color;
}
