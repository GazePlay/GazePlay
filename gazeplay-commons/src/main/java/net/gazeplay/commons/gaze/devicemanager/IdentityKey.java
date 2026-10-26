package net.gazeplay.commons.gaze.devicemanager;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Map key comparing its value by identity (==): two different nodes stay two entries even if equals() says otherwise.
 */
@Slf4j
final class IdentityKey<T> {

    @NonNull
    private final T value;

    public IdentityKey(@NonNull T value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof IdentityKey)) {
            return false;
        }
        IdentityKey otherIdentityKey = (IdentityKey) other;
        return this.value == otherIdentityKey.value;
    }

}
