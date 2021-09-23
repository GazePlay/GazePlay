package net.gazeplay.games.moles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MolesGameVariant {
    BIBOULE("Biboule"),
    USERP("UserP");

    @Getter
    private final String label;
}
