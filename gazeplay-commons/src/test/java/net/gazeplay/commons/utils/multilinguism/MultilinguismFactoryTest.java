package net.gazeplay.commons.utils.multilinguism;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MultilinguismFactoryTest {

    @Test
    void shouldGetSingleton() {
        Multilinguism result = MultilinguismFactory.getSingleton();

        assertNotNull(result);
    }

    @Test
    void shouldGetByResource() {
        String path = "data/multilinguism/multilinguism.csv";
        Multilinguism result = MultilinguismFactory.getForResource(path);

        assertNotNull(result);
    }
}
