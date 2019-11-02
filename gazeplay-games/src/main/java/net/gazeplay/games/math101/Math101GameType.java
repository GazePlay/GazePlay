package net.gazeplay.games.math101;

import javafx.scene.paint.Color;
import lombok.Getter;

import java.util.Random;

public enum Math101GameType {

    ADDITION("math-101-addition", Colors.pastelBlue, "+"),

    SUBTRACTIONPOS("math-101-subtraction-pos", Colors.pastelBlue, "-"),

    MULTIPLICATION("math-101-multiplication", Colors.pastelGreen, "*"),

    DIVISION("math-101-division", Colors.pastelGreen, "/"),

    ADDSUB("math-101-addition-subtraction", Colors.light_pastelRed, "+,-"),

    MULTDIV("math-101-multiplication-division", Colors.pastelRed, "*,/"),

    MATHALL("math-101-all", Colors.pastelRed, "+,-,/,*");

    @Getter
    private final String gameName;

    @Getter
    private final Color backgroundColor;

    @Getter
    private final String[] operators;

    Math101GameType(String gameName, Color coulour, String operators) {
        this.gameName = gameName;
        this.backgroundColor = coulour;
        this.operators = operators.split(",");
    }

    public String chooseOperator() {
        final String operatorStr;
        // choose operator
        if (operators.length == 1) {
            // operator is operators[0]
            operatorStr = operators[0];
        } else {
            Random r = new Random();
            int operatorRand = r.nextInt(operators.length);
            operatorStr = operators[operatorRand];
        }
        return operatorStr;
    }

    public static class Colors {

        public static final Color pastelRed = Color.rgb(255, 227, 227);
        public static final Color light_pastelRed = Color.rgb(255, 245, 245);
        public static final Color pastelGreen = Color.rgb(227, 255, 227);
        public static final Color pastelBlue = Color.rgb(227, 227, 255);

    }


}

