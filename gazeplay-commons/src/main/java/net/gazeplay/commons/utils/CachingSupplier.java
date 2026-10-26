package net.gazeplay.commons.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Supplier that caches the value to prevent multiple calls.
 * but also supports cache expiry
 */
public class CachingSupplier<T> implements Supplier<T> {

    private final Supplier<T> supplier;

    private final Cache<String, T> cache;

    /**
     * @param supplier           gives the value, at the first call and after each expiry
     * @param expiryDuration     how long a value is kept
     * @param expiryDurationUnit unit of {@code expiryDuration}
     */
    public CachingSupplier(final Supplier<T> supplier, final long expiryDuration, final TimeUnit expiryDurationUnit) {
        this.supplier = supplier;

        cache = CacheBuilder.newBuilder()
            .initialCapacity(1)
            .maximumSize(1)
            .expireAfterWrite(Duration.of(expiryDuration, expiryDurationUnit.toChronoUnit()))
            .build();
    }

    @Override
    public T get() {
        try {
            return cache.get("UNIQ", this.supplier::get);
        } catch (final ExecutionException e) {
            throw new RuntimeExecutionException(e);
        }
    }

}
