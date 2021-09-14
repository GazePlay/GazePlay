package net.gazeplay.games.noughtsandcrosses;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NaCGameVariant {
    P2("P2"),
    IA("IA");

    @Getter
    private final String label;
}
