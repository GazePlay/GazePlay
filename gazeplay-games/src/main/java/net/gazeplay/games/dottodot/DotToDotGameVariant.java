package net.gazeplay.games.dottodot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The variants of "DotToDot" before its review of 12/11/2026, kept only to read the variants saved then (shortcuts,
 * last variant): see {@link DotToDotGameLauncher}. The menu offers the variants of {@link DotToDotGameVariantGenerator}.
 */
@RequiredArgsConstructor
public enum DotToDotGameVariant {
    NUMBERS("ConnectNumbers"),
    ORDER("ConnectOrder"),
    NUMBER_DYNAMIC("ConnectNumbersDynamic"),
    ORDER_DYNAMIC("ConnectOrderDynamic");

    @Getter
    private final String label;
}
