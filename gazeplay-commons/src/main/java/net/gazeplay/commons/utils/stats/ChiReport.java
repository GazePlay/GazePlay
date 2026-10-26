package net.gazeplay.commons.utils.stats;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Chi-squared values of the levels of a game ("Where is it?" with levels), to tell whether the right answers were
 * chosen on purpose or at random.
 */
public class ChiReport {
    @Getter
    private final List<Double> chi2PerPlayedLevel = new ArrayList<>();
    private final List<Integer> chi2PlayedLevel = new ArrayList<>();

    /**
     * Adds the chi-squared value observed for a level played.
     */
    public void addChiObs(final double chiObs) {
        this.chi2PerPlayedLevel.add(chiObs);
    }

    /**
     * Adds the level played (same order as {@link #addChiObs(double)}).
     */
    public void addChiLevel(final int level) {
        this.chi2PlayedLevel.add(level);
    }

    /**
     * Compares an observed value with the critical values of the chi-squared law with one degree of freedom, for the
     * risks 50 %, 25 %, 10 %, 5 %, 1 % and 0.5 %.
     *
     * @return for each of these risks, in this order, "good" when the observed value reaches the critical value, else
     *   "random"
     */
    public ArrayList<String> decisions(double chiObs) {
        ArrayList<Double> chiTheoretical = new ArrayList<>();
        chiTheoretical.add(0.45);
        chiTheoretical.add(1.32);
        chiTheoretical.add(2.71);
        chiTheoretical.add(3.84);
        chiTheoretical.add(6.63);
        chiTheoretical.add(7.88);

        ArrayList<String> decisions = new ArrayList<>();
        for (int i= 0; i < 6; i ++) {
            if (chiObs >= chiTheoretical.get(i))
                decisions.add("good");
            else
                decisions.add("random");
        }

        return decisions;
    }

    /**
     * @return one line of the table per level played
     */
    public ObservableList<ChiData> createData() {

        ObservableList<ChiData> data = FXCollections.observableArrayList();

        for (int i = 0; i < chi2PerPlayedLevel.size(); i ++) {
            data.add(new ChiData(i, chi2PlayedLevel.get(i), chi2PerPlayedLevel.get(i),
                decisions(chi2PerPlayedLevel.get(i)).get(5),
                decisions(chi2PerPlayedLevel.get(i)).get(4),
                decisions(chi2PerPlayedLevel.get(i)).get(3),
                decisions(chi2PerPlayedLevel.get(i)).get(2),
                decisions(chi2PerPlayedLevel.get(i)).get(1),
                decisions(chi2PerPlayedLevel.get(i)).get(0)));
        }


        return data;
    }
}
