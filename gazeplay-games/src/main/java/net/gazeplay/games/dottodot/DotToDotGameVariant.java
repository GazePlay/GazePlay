package net.gazeplay.games.dottodot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DotToDotGameVariant {
    NUMBERS("ConnectNumbers"),
    ORDER("ConnectOrder");

    @Getter
    private final String label;
}
