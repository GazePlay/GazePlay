package net.gazeplay.games.math101;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MathOperation {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVID("/");

    @Getter
    public final String text;
}
