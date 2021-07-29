package net.gazeplay.games.cassebrique;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CasseBriqueGameVariant {
    BLOC("CBBLOC"),
    SPACE("CBSPACE"),
    ARC("CBARC"),
    SMILEY("CBSMILEY");

    @Getter
    private final String label;
}
