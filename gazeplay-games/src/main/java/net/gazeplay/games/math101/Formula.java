package net.gazeplay.games.math101;

import lombok.Builder;
import lombok.Data;

/**
 * An operation to solve: two numbers, an operator and the answer.
 */
@Builder
@Data
public class Formula {
    private final int number1;
    private final int number2;
    private final MathOperation operator;
    private final int correctAnswer;

    /**
     * @return the question, e.g. "3 + 4 = ? "
     */
    public String createFormulaString() {
        return number1 + " " + operator.getText() + " " + number2 + " = ? ";
    }

}
