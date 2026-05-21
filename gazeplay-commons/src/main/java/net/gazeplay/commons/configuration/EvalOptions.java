package net.gazeplay.commons.configuration;

public enum EvalOptions {

    Classique("Classique"),
    Autonomie_Total("Autonomie Total"),
    Autonomie_Soutient_Oral("Autonomie Soutient Oral"),
    Autonomie_Soutient_Visuel("Autonomie Soutient Visuel");

    private final String displayName;

    EvalOptions(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
