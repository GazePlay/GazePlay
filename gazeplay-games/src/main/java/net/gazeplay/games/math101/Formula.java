package net.gazeplay.games.math101;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Formula {
    private final int number1;
    private final int number2;
    private final String operator;
    private final int correctAnswer;

    public String createFormulaString() {
        return number1 + " " + operator + " " + number2 + " = ? ";
    }

}
