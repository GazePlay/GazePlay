package net.gazeplay.games.pianosight;

import java.io.BufferedReader;
import java.io.IOException;

public class Parser {

    String myLine;
    BufferedReader bufRead;
    int currentIndex;

    public Parser() {
    }

    public char nextChar() {
        if (myLine != null) {
            if (currentIndex < myLine.length()) {
                char c = myLine.charAt(currentIndex);
                currentIndex++;
                return c;
            } else {
                try {
                    myLine = bufRead.readLine();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                currentIndex = 0;
                return nextChar();
            }

        } else {
            return '\0';
        }
    }

}
