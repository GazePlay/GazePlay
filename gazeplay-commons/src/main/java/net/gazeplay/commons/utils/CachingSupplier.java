package net.gazeplay.commons.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Supplier that caches the value to prevent multiple calls.
 * but also supports cache expiry
 *
 * @see org.springframework.boot.context.properties.PropertyMapper.CachingSupplier
 */
public class CachingSupplier<T> implements Supplier<T> {

    private final Supplier<T> supplier;

    private final Cache<String, T> cache = CacheBuilder.newBuilder()
        .initialCapacity(1)
        .maximumSize(1)
        //.refreshAfterWrite(2, TimeUnit.SECONDS)
        .expireAfterWrite(2, TimeUnit.SECONDS)
        .build();

    public CachingSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        try {
            return (T) cache.get("UNIQ", () -> this.supplier.get());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
