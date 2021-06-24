package net.gazeplay.commons.utils.stats;

import lombok.Getter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChiReport {
    @Getter
    private final List<Double> chi2PerPlayedLevel = new ArrayList<>();

    public void addChiObs(final double chiObs) {
        this.chi2PerPlayedLevel.add(chiObs);
    }

    public List<Double> getLestChiObs() {
        return Collections.unmodifiableList(chi2PerPlayedLevel);
    }
}
