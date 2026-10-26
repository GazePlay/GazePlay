package net.gazeplay.commons.configuration;

/**
 * Light or dark background of the games (BACKGROUND_STYLE setting). The setting is still saved, but no page shows it
 * anymore: the background color (BACKGROUND_COLOR) replaced it.
 */
public enum BackgroundStyle {
    LIGHT {
        @Override
        public <E> E accept(BackgroundStyleVisitor<E> visitor) {
            return visitor.visitLight();
        }
    },
    DARK {
        @Override
        public <E> E accept(BackgroundStyleVisitor<E> visitor) {
            return visitor.visitDark();
        }

    };

    /**
     * @return the value the visitor gives for this style
     */
    public abstract <E> E accept(BackgroundStyleVisitor<E> visitor);
}
