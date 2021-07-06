package net.gazeplay.games.opinions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OpinionsGameVariant {
    OPINIONS("Opinions"),
    ONHB("OuiNon_Haut-Bas"),
    ONBH("OuiNon_Bas-Haut"),
    ONGD("OuiNon_Gauche-Droite"),
    ONDG("OuiNon_Droite-Gauche");

    @Getter
    private final String label;
}
