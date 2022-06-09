package net.gazeplay.games.math101;

import javafx.scene.paint.Color;
import lombok.Getter;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

public enum MathGameType {

    ADDITION("math-101-addition", Colors.pastelBlue, MathOperation.PLUS),

    SUBTRACTIONPOS("math-101-subtraction-pos", Colors.pastelBlue, MathOperation.MINUS),

    MULTIPLICATION("math-101-multiplication", Colors.pastelGreen, MathOperation.MULTIPLY),

    DIVISION("math-101-division", Colors.pastelGreen, MathOperation.DIVID),

    ADDSUB("math-101-addition-subtraction", Colors.light_pastelRed, MathOperation.PLUS, MathOperation.MINUS),

    MULTDIV("math-101-multiplication-division", Colors.pastelRed, MathOperation.MULTIPLY, MathOperation.DIVID),

    MATHALL("math-101-all", Colors.pastelRed, MathOperation.values());

    @Getter
    private final String gameName;

    @Getter
    private final Color backgroundColor;

    private final MathOperation[] operators;


    MathGameType(String gameName, Color coulour, MathOperation... operators) {
        this.gameName = gameName;
        this.backgroundColor = coulour;
        this.operators = operators;
    }

    public MathOperation chooseOperator(ReplayablePseudoRandom random) {
        if (operators.length == 1) {
            return operators[0];
        } else {
            int operatorRand = random.nextInt(operators.length);
            return operators[operatorRand];
        }
    }

    public static class Colors {

        public static final Color pastelRed = Color.rgb(255, 227, 227);
        public static final Color light_pastelRed = Color.rgb(255, 245, 245);
        public static final Color pastelGreen = Color.rgb(227, 255, 227);
        public static final Color pastelBlue = Color.rgb(227, 227, 255);

    }


}

