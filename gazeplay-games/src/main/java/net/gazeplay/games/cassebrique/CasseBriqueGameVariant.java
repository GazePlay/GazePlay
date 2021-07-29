package net.gazeplay.games.cassebrique;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CasseBriqueGameVariant {
    SPACE("CBSPACE"),
    FACE("CBFACE"),
    SMILEY("CBSMILEY");

    @Getter
    private final String label;
}
