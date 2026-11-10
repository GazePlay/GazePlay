package net.gazeplay.games.math101;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The operators of "Apprends les Maths", with their symbol as written at school (before the review of 10/11/2026:
 * "-", "*" and "/").
 */
@RequiredArgsConstructor
public enum MathOperation {
    PLUS("+"),
    MINUS("−"),
    MULTIPLY("×"),
    DIVID("÷");

    @Getter
    public final String text;
}
