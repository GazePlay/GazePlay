package net.gazeplay.commons.utils;

import java.util.function.Supplier;

/**
 * Supplier that caches the value to prevent multiple calls.
 *
 * @see org.springframework.boot.context.properties.PropertyMapper.CachingSupplier
 */
public class ImmutableCachingSupplier<T> implements Supplier<T> {

    private final Supplier<T> supplier;

    private boolean hasResult;

    private T result;

    public ImmutableCachingSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (!this.hasResult) {
            this.result = this.supplier.get();
            this.hasResult = true;
        }
        return this.result;
    }

}
