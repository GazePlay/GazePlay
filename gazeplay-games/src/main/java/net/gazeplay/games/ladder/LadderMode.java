package net.gazeplay.games.ladder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "Échelle" is played (maintainer's choices of 13/11/2026): find the start that leads to the picture asked. The
 * free mode (choose a start and follow the Biboule) was removed on 17/11/2026 (maintainer's decision): one mode only,
 * kept as the group of the variants (a single value: the window shows no switch for it).
 */
@Getter
@RequiredArgsConstructor
public enum LadderMode {
    AIM("LadderAim");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key: aiming, the only one (a variant of the free mode of before is played aiming)
     */
    public static LadderMode of(final String key) {
        return AIM;
    }
}
