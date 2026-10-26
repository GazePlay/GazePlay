package net.gazeplay.commons.utils;

import java.util.concurrent.ExecutionException;

/**
 * Unchecked wrapper of an {@link ExecutionException} (failure of a computation run elsewhere, e.g. in a cache).
 */
public class RuntimeExecutionException extends RuntimeException {

    public RuntimeExecutionException(final String message, final ExecutionException cause) {
        super(message, cause);
    }

    public RuntimeExecutionException(final ExecutionException cause) {
        super(cause);
    }

}
