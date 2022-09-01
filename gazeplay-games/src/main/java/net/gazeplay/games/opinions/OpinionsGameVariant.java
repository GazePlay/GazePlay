package net.gazeplay.games.opinions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OpinionsGameVariant {
    OPINIONS("Opinions"),
    ONHB("OuiNon Haut-Bas"),
    ONBH("OuiNon Bas-Haut"),
    ONGD("OuiNon Gauche-Droite"),
    ONDG("OuiNon Droite-Gauche");

    @Getter
    private final String label;
}
