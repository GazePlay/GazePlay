package net.gazeplay.commons.utils.stats;

import javafx.beans.property.*;

public class ChiData {

    private final SimpleIntegerProperty id;

    private final SimpleIntegerProperty level;

    private final SimpleDoubleProperty chiObs;

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

    public final int getId() {
        return this.id.get();
    }

    public final void setId(int id) {
        this.id.set(id);
    }

    public final IntegerProperty idProperty() {
        return id;
    }

    public final int getLevel() {
        return this.level.get();
    }

    public final void setLevel(int lvl) {
        this.level.set(lvl);
    }

    public final IntegerProperty levelProperty() {
        return level;
    }

    public final double getChiObs() {
        return this.chiObs.get();
    }

    public final void setChiObs(double chi) {
        this.chiObs.set(chi);
    }

    public final DoubleProperty chiObsProperty() {
        return chiObs;
    }

    public final String getAlpha05() {
        return this.alpha05.get();
    }

    public final void setAlpha05(String alpha) {
        this.alpha05.set(alpha);
    }

    public SimpleStringProperty alpha05Property() {
        return alpha05;
    }

    public final String getAlpha1() {
        return this.alpha1.get();
    }

    public final void setAlpha1(String alpha) {
        this.alpha1.set(alpha);
    }

    public SimpleStringProperty alpha1Property() {
        return alpha1;
    }

    public final String getAlpha5() {
        return this.alpha5.get();
    }

    public final void setAlpha5(String alpha) {
        this.alpha5.set(alpha);
    }

    public SimpleStringProperty alpha5Property() {
        return alpha5;
    }

    public final String getAlpha10() {
        return this.alpha10.get();
    }

    public final void setAlpha10(String alpha) {
        this.alpha10.set(alpha);
    }

    public SimpleStringProperty alpha10Property() {
        return alpha10;
    }

    public final String getAlpha25() {
        return this.alpha25.get();
    }

    public final void setAlpha25(String alpha) {
        this.alpha25.set(alpha);
    }

    public SimpleStringProperty alpha25Property() {
        return alpha25;
    }

    public final String getAlpha50() {
        return this.alpha50.get();
    }

    public final void setAlpha50(String alpha) {
        this.alpha50.set(alpha);
    }

    public SimpleStringProperty alpha50Property() {
        return alpha50;
    }
}
