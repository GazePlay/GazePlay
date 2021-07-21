package net.gazeplay.games.cassebrique;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CasseBriqueGameVariant {
    LV1("LV1"),
    LV2("LV2"),
    LV3("LV3");

    @Getter
    private final String label;
}
