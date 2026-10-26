package net.gazeplay.commons.configuration;

/**
 * Gives a value for each {@link BackgroundStyle}, without a switch on the enum.
 */
public interface BackgroundStyleVisitor<T> {
    T visitLight();
    T visitDark();
}
