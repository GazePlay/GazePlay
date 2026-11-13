package net.gazeplay.games.blocs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How a block of "Blocs" vanishes (maintainer's choice of 13/11/2026): as soon as the gaze enters it (the game of
 * before, to learn that the gaze acts), or after a fixation.
 */
@Getter
@RequiredArgsConstructor
public enum BlocsMode {
    GAZE("BlocsGaze"),
    FIXATION("BlocsFixation");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, at the gaze for an unknown one
     */
    public static BlocsMode of(final String key) {
        for (final BlocsMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return GAZE;
    }
}
