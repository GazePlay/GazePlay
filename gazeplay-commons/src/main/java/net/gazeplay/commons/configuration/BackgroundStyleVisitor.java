package net.gazeplay.commons.configuration;

public interface BackgroundStyleVisitor<T> {
    T visitLight();

    T visitDark();
}
