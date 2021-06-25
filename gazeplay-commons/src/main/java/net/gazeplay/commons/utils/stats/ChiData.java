package net.gazeplay.commons.utils.stats;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

public class ChiData {

    @Setter @Getter
    private final SimpleIntegerProperty id;
    @Setter @Getter
    private final SimpleIntegerProperty level;
    @Setter @Getter
    private final SimpleDoubleProperty chiObs;
    @Setter @Getter
    private final SimpleStringProperty alpha05, alpha1, alpha5, alpha10, alpha25, alpha50;

    public ChiData(int id, int level, double chiObs, String alpha05, String alpha1, String alpha5, String alpha10, String alpha25, String alpha50) {
        this.id = new SimpleIntegerProperty(id);
        this.level = new SimpleIntegerProperty(level);
        this.chiObs = new SimpleDoubleProperty(chiObs);
        this.alpha05 = new SimpleStringProperty(alpha05);
        this.alpha1 = new SimpleStringProperty(alpha1);
        this.alpha5 = new SimpleStringProperty(alpha5);
        this.alpha10 = new SimpleStringProperty(alpha10);
        this.alpha25 = new SimpleStringProperty(alpha25);
        this.alpha50 = new SimpleStringProperty(alpha50);
    }
}
