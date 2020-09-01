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
    private String variantChooseText = "Choose Video";

    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(

            new StringGameVariant("Big Buck Bunny", "YE7VzlLtp-4"),

            new StringGameVariant("Caminandes 2: Gran Dillama - Blender Animated Short",
                "Z4C82eyhwgU"),

            new StringGameVariant("Caminandes 3: Llamigos - Funny 3D Animated Short",
                "SkVqJ1SGeL0"),

            new StringGameVariant("1H de Petit Ours Brun", "PUIou9gUVos"),

            new StringGameVariant("Zou s'amuse", "f9qKQ5snhOI"),

            new StringGameVariant("Tchoupi et ses amis", "aPX6q1HC4Ho"),

            // new StringGameVariant("Tchoupi à l'école", "a_KH2U2wqok"),

            new StringGameVariant("Princesse sofia rencontre Belle", "szptWdF2B5s")

            // new StringGameVariant("Lulu Vroumette", "2Eg7r6WGWhQ")

        ));
    }
}
