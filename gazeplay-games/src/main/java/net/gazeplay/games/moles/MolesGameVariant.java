package net.gazeplay.games.moles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "WhacAMole", chosen in the menu.
 */
@RequiredArgsConstructor
public enum MolesGameVariant {
    BIBOULE("Biboule"),
    USERP("UserP");

    @Getter
    private final String label;
}
