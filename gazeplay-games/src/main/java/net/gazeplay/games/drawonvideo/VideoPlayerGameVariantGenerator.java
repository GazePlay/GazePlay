package net.gazeplay.games.drawonvideo;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.StringGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class VideoPlayerGameVariantGenerator implements IGameVariantGenerator {

    @Getter
    @Setter
    private String variantChooseText = "Choose the video";

    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new StringGameVariant("Big Buck Bunny", "YE7VzlLtp-4"),
            new StringGameVariant("Caminandes 2","Z4C82eyhwgU"),
            new StringGameVariant("Caminandes 3", "SkVqJ1SGeL0"),
            new StringGameVariant("Petit Ours Brun", "PUIou9gUVos"),
            new StringGameVariant("Zou s'amuse", "f9qKQ5snhOI"),
            new StringGameVariant("Tchoupi et ses amis", "aPX6q1HC4Ho"),
            new StringGameVariant("Princesse Sofia rencontre Belle", "szptWdF2B5s")
        ));
    }
}
