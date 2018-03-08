package net.gazeplay.games.pianosight;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by schwab on 28/08/2016.
 */
@Slf4j
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
