package net.gazeplay.commons.configuration;

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

    public abstract <E> E accept(BackgroundStyleVisitor<E> visitor);
}
