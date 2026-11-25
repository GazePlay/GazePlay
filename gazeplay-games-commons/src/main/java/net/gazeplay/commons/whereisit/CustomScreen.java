package net.gazeplay.commons.whereisit;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A screen of a game made by the user for "Where is it?" (maintainer's request of 25/11/2026, point 86): the size of its
 * grid, its pictures, the right answer, and a sentence and a sound if the user wants.
 */
public final class CustomScreen {

    /**
     * The sizes of the grid, {lines, columns}: those of the family "Where is it?".
     */
    public static final List<int[]> SIZES = List.of(new int[]{1, 2}, new int[]{2, 2}, new int[]{2, 3}, new int[]{3, 2},
        new int[]{3, 3});

    /**
     * What is missing in a screen to be played.
     */
    public enum Problem { NONE, PICTURES, ANSWER }

    private static final int NO_ANSWER = -1;

    @Getter
    private int lines = 2;

    @Getter
    private int columns = 2;

    /**
     * The sentence shown, empty for none.
     */
    @Getter
    private String sentence = "";

    /**
     * The sound played with the sentence, null for none.
     */
    @Getter
    @Setter
    private Path sound;

    /**
     * The pictures by place (line by line), null where there is none. A smaller grid keeps the pictures beyond it, found
     * again with a larger one.
     */
    private final List<Path> pictures = new ArrayList<>();

    private int rightAnswer = NO_ANSWER;

    public CustomScreen() {
        setSize(2, 2);
    }

    /**
     * @return a screen with the same grid, pictures, answer, sentence and sound
     */
    public CustomScreen copy() {
        final CustomScreen copy = new CustomScreen();
        copy.setSize(lines, columns);
        copy.sentence = sentence;
        copy.sound = sound;
        copy.pictures.clear();
        copy.pictures.addAll(pictures);
        copy.rightAnswer = rightAnswer;
        return copy;
    }

    /**
     * @return the number of pictures of the grid
     */
    public int getPlaces() {
        return lines * columns;
    }

    public void setSize(final int lines, final int columns) {
        this.lines = lines;
        this.columns = columns;
        while (pictures.size() < getPlaces()) {
            pictures.add(null);
        }
    }

    public void setSentence(final String sentence) {
        this.sentence = sentence == null ? "" : sentence;
    }

    /**
     * @return the pictures of the grid, null where there is none
     */
    public List<Path> getPictures() {
        return Collections.unmodifiableList(pictures.subList(0, getPlaces()));
    }

    public void setPicture(final int place, final Path picture) {
        pictures.set(place, picture);
    }

    /**
     * @return the place of the right answer, -1 when it is not chosen (or beyond the grid)
     */
    public int getRightAnswer() {
        return rightAnswer < getPlaces() ? rightAnswer : NO_ANSWER;
    }

    public void setRightAnswer(final int rightAnswer) {
        this.rightAnswer = rightAnswer < 0 ? NO_ANSWER : rightAnswer;
    }

    /**
     * @return what is missing to play this screen: a picture (or its file), or the right answer
     */
    public Problem problem() {
        if (getPictures().stream().anyMatch(picture -> picture == null || !Files.isRegularFile(picture))) {
            return Problem.PICTURES;
        }
        return getRightAnswer() < 0 ? Problem.ANSWER : Problem.NONE;
    }

    public boolean isComplete() {
        return problem() == Problem.NONE;
    }
}
