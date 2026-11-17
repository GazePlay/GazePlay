package net.gazeplay.games.colorblend;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "Mélanger les couleurs" is played (maintainer's choices of 13/11/2026): a color to make (the challenge). The free
 * mode (one's own mixes) was removed on 17/11/2026 (maintainer's decision): one mode only, kept as the group of the
 * variants (a single value: the window shows no switch for it).
 */
@Getter
@RequiredArgsConstructor
public enum ColorBlendMode {
    CHALLENGE("ColorBlendChallenge");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key: the challenge, the only one (a variant of the free mode of before is played so)
     */
    public static ColorBlendMode of(final String key) {
        return CHALLENGE;
    }
}
