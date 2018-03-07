package net.gazeplay.commons.gaze.devicemanager;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

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
        // return this.value.getClass().getName().hashCode();
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
