package net.gazeplay.games.moles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MolesGameVariant {
    BIBOULE("Biboule");

    @Getter
    private final String label;
}
