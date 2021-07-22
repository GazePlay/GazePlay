package net.gazeplay.games.dottodot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DotToDotGameVariant {
    NUMBERS("ConnectNumbers"),
    ORDER("ConnectOrder"),
    NUMBER_DYNAMIC("ConnectNumbersDynamic"),
    ORDER_DYNAMIC("ConnectOrderDynamic");

    @Getter
    private final String label;
}
