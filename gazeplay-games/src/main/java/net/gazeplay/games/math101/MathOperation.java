package net.gazeplay.games.math101;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The operators of the Math101 games, with their symbol.
 */
@RequiredArgsConstructor
public enum MathOperation {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVID("/");

    @Getter
    public final String text;
}
