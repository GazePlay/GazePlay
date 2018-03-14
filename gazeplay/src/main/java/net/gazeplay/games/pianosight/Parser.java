package net.gazeplay.games.pianosight;

import java.io.BufferedReader;
import java.io.IOException;

public class Parser {

    String myLine;
    BufferedReader bufRead;
    int currentIndex;

    public Parser() throws IOException {
    }

    public char nextChar() throws IOException {
        if (myLine != null) {
            if (currentIndex < myLine.length()) {
                char c = myLine.charAt(currentIndex);
                currentIndex++;
                return c;
            } else {
                myLine = bufRead.readLine();
                currentIndex = 0;
                return nextChar();
            }

        } else {
            return '\0';
        }
    }

}
