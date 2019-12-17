package net.gazeplay.games.whereisit;

import javafx.geometry.Dimension2D;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.Random;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class WhereIsItTest {
    // Test Mocks
    static IGameContext mockGameContext = mock(IGameContext.class, Mockito.RETURNS_DEEP_STUBS);
    static Stats mockStats = mock(Stats.class);
    private static WhereIsIt whereIsIt;

    @BeforeAll
    static void setup() {
        whereIsIt = new WhereIsIt(WhereIsItGameType.ANIMALNAME, 2, 2, false, mockGameContext, mockStats);
    }

    @Test
    void shouldPickAndBuildRandomPictures() {
        Configuration mockConfig = mock(Configuration.class);
        when(mockConfig.getLanguage()).thenReturn("eng");

        Dimension2D mockDimension = new Dimension2D(20, 20);
        when(mockGameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(mockDimension);

        Random random = new Random();
        RoundDetails randomPictures = whereIsIt.pickAndBuildRandomPictures(mockConfig, 4, random, 0);
        assert randomPictures.getPictureCardList().size() == 4;
    }

}
