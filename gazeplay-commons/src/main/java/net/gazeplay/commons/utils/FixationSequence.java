package net.gazeplay.commons.utils;

import lombok.Getter;
import lombok.Setter;

public class FixationSequence {

    /**
     * Will store the starting time when the gaze is on a position
     */
    @Setter
    @Getter
    private double initialFixation;

    /**
     * Will store the total time spent looking at a specific position
     */
    @Setter
    @Getter
    private double totalTimeOfFixation;

    public FixationSequence(double firstG, double totalT){
        this.initialFixation = firstG;
        this.totalTimeOfFixation = totalT;
    }
}
