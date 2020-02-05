package net.gazeplay.games.drawonvideo;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.GameSpec;

import java.util.Set;

public class VideoPlayerGameVariantGenerator implements GameSpec.GameVariantGenerator {

    @Getter
    @Setter
    private String variantChooseText = "Choose Video";

    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(

            new GameSpec.StringGameVariant("Big Buck Bunny", "YE7VzlLtp-4"),

            new GameSpec.StringGameVariant("Caminandes 2: Gran Dillama - Blender Animated Short",
                "Z4C82eyhwgU"),

            new GameSpec.StringGameVariant("Caminandes 3: Llamigos - Funny 3D Animated Short",
                "SkVqJ1SGeL0"),

            new GameSpec.StringGameVariant("1H de Petit Ours Brun", "PUIou9gUVos"),

            new GameSpec.StringGameVariant("Zou s'amuse", "f9qKQ5snhOI"),

            new GameSpec.StringGameVariant("Tchoupi et ses amis", "aPX6q1HC4Ho"),

            // new GameSpec.StringGameVariant("Tchoupi à l'école", "a_KH2U2wqok"),

            new GameSpec.StringGameVariant("Princesse sofia rencontre Belle", "szptWdF2B5s")

            // new GameSpec.StringGameVariant("Lulu Vroumette", "2Eg7r6WGWhQ")

        ));
    }
}
