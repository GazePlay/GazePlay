package net.gazeplay.games.cassebrique;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "CasseBrique", chosen in the menu.
 */
@RequiredArgsConstructor
public enum CasseBriqueGameVariant {
    BLOC("Bloc"),
    SPACE("Space"),
    ARC("Arc"),
    SMILEY("Smiley");

    @Getter
    private final String label;
}
