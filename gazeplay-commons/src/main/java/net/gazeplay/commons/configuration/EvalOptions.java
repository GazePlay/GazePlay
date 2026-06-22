package net.gazeplay.commons.configuration;

public enum EvalOptions {

    Classique("Classique"),
    Autonomie_Total("Autonomie Totale"),
    Autonomie_Soutien_Oral("Autonomie Soutien Oral"),
    Autonomie_Soutien_Visuel("Autonomie Soutien Visuel");

    private final String displayName;

    EvalOptions(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
