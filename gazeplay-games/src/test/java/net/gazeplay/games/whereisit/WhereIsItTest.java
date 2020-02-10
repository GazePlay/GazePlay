package net.gazeplay.games.whereisit;

import javafx.geometry.Dimension2D;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.Random;

import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
@RunWith(MockitoJUnitRunner.class)
class WhereIsItTest {
    // Test Mocks
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext mockGameContext;

    @Mock
    Stats mockStats;

    @Mock
    Configuration mockConfig;

    private static final String FILESEPARATOR = File.separator;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @EnumSource(value = WhereIsItGameType.class, mode = EnumSource.Mode.EXCLUDE, names = {"CUSTOMIZED"})
    void shouldPickAndBuildRandomPictures(final WhereIsItGameType gameType) {
        final WhereIsIt whereIsIt = new WhereIsIt(gameType, 2, 2, false, mockGameContext, mockStats);
        when(mockConfig.getLanguage()).thenReturn("eng");

        final Dimension2D mockDimension = new Dimension2D(20, 20);
        when(mockGameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(mockDimension);
        when(mockGameContext.getConfiguration()).thenReturn(mockConfig);

        final Random random = new Random();
        final RoundDetails randomPictures = whereIsIt.pickAndBuildRandomPictures(4, random, 0);
        assert randomPictures.getPictureCardList().size() == 4;
    }

    @Test
    void shouldPickAndBuildRandomCustomPictures() {
        final WhereIsIt whereIsIt = new WhereIsIt(WhereIsItGameType.CUSTOMIZED, 2, 2, false, mockGameContext, mockStats);
        when(mockConfig.getLanguage()).thenReturn("eng");
        final String currentDir = System.getProperty("user.dir") +
            FILESEPARATOR + "src" +
            FILESEPARATOR + "test" +
            FILESEPARATOR + "resources";
        when(mockConfig.getWhereIsItDir()).thenReturn(currentDir);

        final Dimension2D mockDimension = new Dimension2D(20, 20);
        when(mockGameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(mockDimension);
        when(mockGameContext.getConfiguration()).thenReturn(mockConfig);

        final Random random = new Random();
        final RoundDetails randomPictures = whereIsIt.pickAndBuildRandomPictures(4, random, 0);
        assert randomPictures.getPictureCardList().size() == 4;
    }

}
