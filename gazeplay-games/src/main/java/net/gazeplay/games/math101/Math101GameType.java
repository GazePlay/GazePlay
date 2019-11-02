package net.gazeplay.games.math101;

import javafx.scene.paint.Color;
import lombok.Getter;

import java.util.Random;

public enum Math101GameType {

    ADDITION("math-101-addition", Colors.pastelBlue, "+", new int[]{8, 12, 20}),

    SUBTRACTIONPOS("math-101-subtraction-pos", Colors.pastelBlue, "-", new int[]{8, 12, 20}),

    MULTIPLICATION("math-101-multiplication", Colors.pastelGreen, "*", new int[]{3, 5, 7, 9, 11, 12}),

    DIVISION("math-101-division", Colors.pastelGreen, "/", new int[]{10, 15, 20, 30}),

    ADDSUB("math-101-addition-subtraction", Colors.light_pastelRed, "+,-", new int[]{8, 12, 20}),

    MULTDIV("math-101-multiplication-division", Colors.pastelRed, "*,/", new int[]{5, 10, 15, 20, 30}),

    MATHALL("math-101-all", Colors.pastelRed, "+,-,/,*", new int[]{5, 10, 15, 20});

    @Getter
    private final String gameName;

    protected final Color backgroundColor;

    protected final String[] operators;

    protected final int[] variations;

    Math101GameType(String gameName, Color coulour, String operators, int[] variations) {
        this.gameName = gameName;
        this.backgroundColor = coulour;
        this.operators = operators.split(",");
        this.variations = variations;
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

